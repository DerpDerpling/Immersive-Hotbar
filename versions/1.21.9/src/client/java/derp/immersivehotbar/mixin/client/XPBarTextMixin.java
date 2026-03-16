package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseEnabled;
import static derp.immersivehotbar.util.XPBarState.pulseScale;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class XPBarTextMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private void wrapDrawExperienceLevel(GuiGraphics context, Font textRenderer, int level, Operation<Void> original) {
        if (!xpTextPulseEnabled || minecraft.player == null || level <= 0) {
            original.call(context, textRenderer, level);
            return;
        }

        float scale = pulseScale;
        if (scale <= 1.0f) {
            original.call(context, textRenderer, level);
            return;
        }

        Component renderedText = Component.translatable("gui.experience.level", level);
        float textWidth = textRenderer.width(renderedText);

        float x = (context.guiWidth() - textWidth) / 2.0f;
        float y = context.guiHeight() - 24 - 9 - 2;

        float centerX = x + textWidth / 2.0f;
        float centerY = y + 4.5f;

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        matrices.translate(-centerX, -centerY);

        original.call(context, textRenderer, level);

        matrices.popMatrix();
    }
}