package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseEnabled;
import static derp.immersivehotbar.util.XPBarState.pulseScale;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class XPBarTextMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"))
    private void wrapDrawExperienceLevel(DrawContext context, TextRenderer textRenderer, int level, Operation<Void> original) {
        if (!xpTextPulseEnabled || client.player == null || level <= 0) {
            original.call(context, textRenderer, level);
            return;
        }

        float scale = pulseScale;
        if (scale <= 1.0f) {
            original.call(context, textRenderer, level);
            return;
        }

        Text renderedText = Text.translatable("gui.experience.level", level);
        float textWidth = textRenderer.getWidth(renderedText);

        float x = (context.getScaledWindowWidth() - textWidth) / 2.0f;
        float y = context.getScaledWindowHeight() - 24 - 9 - 2;

        float centerX = x + textWidth / 2.0f;
        float centerY = y + 4.5f;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        matrices.translate(-centerX, -centerY);

        original.call(context, textRenderer, level);

        matrices.popMatrix();
    }
}