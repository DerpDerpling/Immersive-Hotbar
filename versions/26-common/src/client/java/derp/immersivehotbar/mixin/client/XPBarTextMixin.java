package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.xp.XPAnimationController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseEnabled;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public abstract class XPBarTextMixin {
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void immersiveHotbar$animateExperienceLevel(GuiGraphicsExtractor graphics, Font font, int level, Operation<Void> original) {
        if (!xpTextPulseEnabled || minecraft.player == null || level <= 0) {
            original.call(graphics, font, level);
            return;
        }

        float scale = XPAnimationController.hud().state().pulseScale();

        if (scale <= 1.0f) {
            original.call(graphics, font, level);
            return;
        }

        Component text = Component.translatable("gui.experience.level", level);
        float width = font.width(text);

        float x = (graphics.guiWidth() - width) / 2.0f;
        float y = graphics.guiHeight() - 24 - 9 - 2;

        float centerX = x + width / 2.0f;
        float centerY = y + 4.5f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-centerX, -centerY);

        original.call(graphics, font, level);

        graphics.pose().popMatrix();
    }
}