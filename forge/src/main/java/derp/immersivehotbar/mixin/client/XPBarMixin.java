package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.animation.xp.XPAnimationController;
import derp.immersivehotbar.animation.xp.XPAnimationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseEnabled;


@Mixin(Gui.class)
public abstract class XPBarMixin {
    @Unique private final XPAnimationController immersiveHotbar$xp = new XPAnimationController();
    @Shadow private int screenWidth;
    @Shadow private int screenHeight;
    @Shadow public abstract Font getFont();

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void immersiveHotbar$updateXP(GuiGraphics graphics, int x, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        float dt = minecraft.getDeltaFrameTime();
        immersiveHotbar$xp.update(minecraft.player, dt);
        immersiveHotbar$xp.particles().update(dt);
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void immersiveHotbar$renderXPEffects(GuiGraphics graphics, int x, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        XPAnimationRenderer.renderBarPulse(graphics, x, immersiveHotbar$xp.state());
        XPAnimationRenderer.renderFrontGlow(graphics, x, immersiveHotbar$xp.state());
        immersiveHotbar$xp.particles().render(graphics);
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 0, shift = At.Shift.BEFORE))
    private void immersiveHotbar$pushLevelScale(GuiGraphics graphics, int x, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.experienceLevel <= 0) return;
        float scale = xpTextPulseEnabled ? immersiveHotbar$xp.state().pulseScale() : 1.0f;
        if (scale <= 1.0f) return;

        String text = String.valueOf(minecraft.player.experienceLevel);
        int textX = (screenWidth - getFont().width(text)) / 2;
        int textY = screenHeight - 31 - 4;
        float centerX = textX + getFont().width(text) / 2.0f;
        float centerY = textY + 4.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-centerX, -centerY, 0.0f);
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", ordinal = 4, shift = At.Shift.AFTER))
    private void immersiveHotbar$popLevelScale(GuiGraphics graphics, int x, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.experienceLevel <= 0) return;
        float scale = xpTextPulseEnabled ? immersiveHotbar$xp.state().pulseScale() : 1.0f;
        if (scale > 1.0f) graphics.pose().popPose();
    }

    @Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceProgress:F", opcode = Opcodes.GETFIELD))
    private float immersiveHotbar$animatedProgress(LocalPlayer player) {
        return immersiveHotbar$xp.state().animatedProgress();
    }
}
