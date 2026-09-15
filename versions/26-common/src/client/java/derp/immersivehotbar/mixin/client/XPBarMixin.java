package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.animation.xp.XPAnimationController;
import derp.immersivehotbar.animation.xp.XPAnimationRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ExperienceBar.class)
public class XPBarMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"))
    private void immersiveHotbar$updateXP(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        float dt = deltaTracker.getGameTimeDeltaTicks();

        XPAnimationController.hud().update(minecraft.player, dt);
        XPAnimationController.hud().particles().update(dt);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void immersiveHotbar$renderXPEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        int x = graphics.guiWidth() / 2 - 91;

        XPAnimationController xp = XPAnimationController.hud();

        XPAnimationRenderer.renderBarPulse(graphics, x, xp.state());
        XPAnimationRenderer.renderFrontGlow(graphics, x, xp.state());
        xp.particles().render(graphics);
    }

    @Redirect(method = "extractBackground", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceProgress:F", opcode = Opcodes.GETFIELD))
    private float immersiveHotbar$animatedProgress(LocalPlayer player) {
        return XPAnimationController.hud().state().animatedProgress();
    }
}