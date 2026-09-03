package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.xp.XPAnimationController;
import derp.immersivehotbar.animation.xp.XPAnimationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class XPBarMixin {
    @Unique private final XPAnimationController immersiveHotbar$xp = new XPAnimationController();

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void immersiveHotbar$updateXP(GuiGraphics graphics, int x, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        immersiveHotbar$xp.update(minecraft.player, minecraft.getTimer().getGameTimeDeltaTicks());
        immersiveHotbar$xp.particles().update(minecraft.getTimer().getGameTimeDeltaTicks());
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void immersiveHotbar$renderXPEffects(GuiGraphics graphics, int x, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        XPAnimationRenderer.renderBarPulse(graphics, x, immersiveHotbar$xp.state());
        XPAnimationRenderer.renderFrontGlow(graphics, x, immersiveHotbar$xp.state());
        XPAnimationRenderer.renderBarPulse(graphics, x, immersiveHotbar$xp.state());
        XPAnimationRenderer.renderFrontGlow(graphics, x, immersiveHotbar$xp.state());
        immersiveHotbar$xp.particles().render(graphics);
    }

    @WrapOperation(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int immersiveHotbar$animateLevelText(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        return XPAnimationRenderer.renderLevelText(graphics, font, text, x, y, immersiveHotbar$xp.state(), (drawX, drawY) -> original.call(graphics, font, text, drawX, drawY, color, shadow));
    }

    @Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceProgress:F", opcode = Opcodes.GETFIELD))
    private float immersiveHotbar$animatedProgress(LocalPlayer player) {
        return immersiveHotbar$xp.state().animatedProgress();
    }
}