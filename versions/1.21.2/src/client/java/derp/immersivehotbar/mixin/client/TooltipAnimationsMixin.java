package derp.immersivehotbar.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.immersiveToolTip;
import static derp.immersivehotbar.util.TooltipAnimationState.*;

@Mixin(InGameHud.class)
public class TooltipAnimationsMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ItemStack currentStack;
    @Shadow private int heldItemTooltipFade;
    @Unique private boolean tooltipLockedUntilFadeComplete = false;


    @Unique private long lastRenderTime = System.nanoTime();

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void onRenderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        if (!immersiveToolTip) return;
        ci.cancel();

        long currentTime = System.nanoTime();
        float deltaSeconds = (currentTime - lastRenderTime) / 1_000_000_000.0f;
        deltaSeconds = MathHelper.clamp(deltaSeconds, 0f, 0.05f);
        lastRenderTime = currentTime;


        boolean isCurrentEmpty = currentStack.isEmpty();
        boolean wasNotEmptyBefore = !lastStack.isEmpty();
        boolean justSwitchedToEmpty = isCurrentEmpty && wasNotEmptyBefore;

        boolean stackChanged = !currentStack.isEmpty() &&
                (!lastStack.isOf(currentStack.getItem()) ||
                        !lastStack.getName().getString().equals(currentStack.getName().getString()));
        boolean isHoldingItem = !currentStack.isEmpty();

        if (!isHoldingItem && heldItemTooltipFade <= 0 && lastKnownFadeSeconds <= 0) {
            tooltipScale = 0f;
            lastTooltipText = Text.empty();
            lastTextWidth = 0;
            return;
        }


        // Bounce animation when switching items
        if (!isCurrentEmpty && stackChanged && !tooltipLockedUntilFadeComplete) {
            tooltipScale = 1.2f;
        }


        lastStack = currentStack.copy();
        if (!isCurrentEmpty) {
            lastStack = currentStack.copy();

            MutableText mutableText = Text.empty()
                    .append(currentStack.getName())
                    .formatted(currentStack.getRarity().getFormatting());

            if (currentStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                mutableText.formatted(Formatting.ITALIC);
            }

            lastTooltipText = mutableText;
            lastTextWidth = client.textRenderer.getWidth(mutableText);
        }

        if (heldItemTooltipFade > 0) {
            lastKnownFadeSeconds = heldItemTooltipFade / 20.0f;
        } else if (justSwitchedToEmpty && lastTooltipText != null && !lastTooltipText.getString().isEmpty()) {
            lastKnownFadeSeconds = 0.2f;
            tooltipLockedUntilFadeComplete = true;
        } else if (lastKnownFadeSeconds > 0) {
            lastKnownFadeSeconds -= deltaSeconds;
        }

        float fadeSeconds = lastKnownFadeSeconds;

        if (fadeSeconds > 0 && lastTooltipText != null && !lastTooltipText.getString().isEmpty()) {
            float fadeRatio = Math.min(fadeSeconds / 0.2f, 1.0f);
            float targetScale = 0.5f + (fadeRatio * 0.5f);

            tooltipScale += (targetScale - tooltipScale) * (8.0f * deltaSeconds);
            tooltipScale = MathHelper.clamp(tooltipScale, 0.0f, 1.5f);

            int x = (context.getScaledWindowWidth() - lastTextWidth) / 2;
            int y = context.getScaledWindowHeight() - 59;
            if (client.interactionManager != null && !client.interactionManager.hasStatusBars()) {
                y += 14;
            }

            int alpha = (int)(fadeRatio * 255);
            if (fadeRatio < 0.1f) return;

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(x + lastTextWidth / 2.0f, y + 4, 0);
            matrices.scale(tooltipScale, tooltipScale, 1.0f);
            matrices.translate(-(x + lastTextWidth / 2.0f), -(y + 4), 0);

            context.drawTextWithBackground(client.textRenderer, lastTooltipText, x, y, lastTextWidth,
                    ColorHelper.withAlpha(alpha, -1));
            matrices.pop();
        } else {
            tooltipScale += (0.0f - tooltipScale) * (10.0f * deltaSeconds);
        }
        if (lastKnownFadeSeconds <= 0 || tooltipScale <= 0.01f) {
            tooltipLockedUntilFadeComplete = false;
            lastTooltipText = Text.empty();
            lastTextWidth = 0;
            tooltipScale = 0f;
        }
    }
}
