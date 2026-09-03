package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationEngine;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

@Mixin(ForgeGui.class)
public abstract class OverlayMessageAnimationsMixin {

    @Unique private Component immersiveHotbar$lastOverlayMessage = Component.empty();
    @Unique private float immersiveHotbar$overlayScale = 0.0f;
    @Unique private float immersiveHotbar$shrinkProgress = 0.0f;
    @Unique private boolean immersiveHotbar$forcedShrink = false;
    @Unique private boolean immersiveHotbar$naturalShrinkStarted = false;
    @Unique private long immersiveHotbar$lastOverlayFrameTime = System.nanoTime();

    @WrapOperation(
            method = "renderRecordOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
                    remap = true
            ),
            remap = false
    )
    private int immersiveHotbar$animateOverlayMessage(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color, Operation<Integer> original) {
        if (!animateOverlayMessages) return original.call(graphics, font, text, x, y, color);

        GuiOverlayAccessor accessor = (GuiOverlayAccessor) this;
        int overlayMessageTime = accessor.immersiveHotbar$getOverlayMessageTime();
        Component overlayMessageString = accessor.immersiveHotbar$getOverlayMessageString();

        float dt = immersiveHotbar$frameDelta();
        Component current = overlayMessageString == null ? Component.empty() : overlayMessageString;
        boolean empty = current.getString().isEmpty();

        if (empty && !immersiveHotbar$lastOverlayMessage.getString().isEmpty()) {
            if (!immersiveHotbar$forcedShrink) {
                immersiveHotbar$forcedShrink = true;
                if (!immersiveHotbar$naturalShrinkStarted) {
                    immersiveHotbar$shrinkProgress = 0.0f;
                    immersiveHotbar$overlayScale = 1.0f;
                }
            }
        } else if (!empty && !current.equals(immersiveHotbar$lastOverlayMessage)) {
            immersiveHotbar$lastOverlayMessage = current;
            immersiveHotbar$overlayScale = 1.2f;
            immersiveHotbar$shrinkProgress = 0.0f;
            immersiveHotbar$forcedShrink = false;
            immersiveHotbar$naturalShrinkStarted = false;
        }

        if (immersiveHotbar$forcedShrink || overlayMessageTime <= 18) {
            if (!immersiveHotbar$forcedShrink) immersiveHotbar$naturalShrinkStarted = true;
            immersiveHotbar$shrinkProgress += dt * shrinkAnimationSpeed;
            immersiveHotbar$overlayScale = HotbarAnimationEngine.shrinkOutScale(1.0f, immersiveHotbar$shrinkProgress, bouncyAnimation);
        } else {
            immersiveHotbar$overlayScale = TooltipAnimationController.updateScale(immersiveHotbar$overlayScale, 1.0f, dt);
            immersiveHotbar$shrinkProgress = 0.0f;
            immersiveHotbar$naturalShrinkStarted = false;
        }

        if (immersiveHotbar$overlayScale <= 0.01f) {
            immersiveHotbar$overlayScale = 0.0f;

            if (immersiveHotbar$forcedShrink) {
                immersiveHotbar$lastOverlayMessage = Component.empty();
                immersiveHotbar$forcedShrink = false;
                immersiveHotbar$shrinkProgress = 0.0f;
                immersiveHotbar$naturalShrinkStarted = false;
            }

            return 0;
        }

        FormattedCharSequence drawText = immersiveHotbar$forcedShrink
                ? immersiveHotbar$lastOverlayMessage.getVisualOrderText()
                : text;

        int drawWidth = font.width(drawText);
        float vanillaCenterX = x + font.width(text) / 2.0f;
        int drawX = immersiveHotbar$forcedShrink ? Math.round(vanillaCenterX - drawWidth / 2.0f) : x;

        float centerX = drawX + drawWidth / 2.0f;
        float centerY = y + font.lineHeight / 2.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0f);
        graphics.pose().scale(immersiveHotbar$overlayScale, immersiveHotbar$overlayScale, 1.0f);
        graphics.pose().translate(-centerX, -centerY, 0.0f);

        int result = original.call(graphics, font, drawText, drawX, y, color);

        graphics.pose().popPose();
        return result;
    }

    @Unique
    private float immersiveHotbar$frameDelta() {
        long now = System.nanoTime();
        float dt = (now - immersiveHotbar$lastOverlayFrameTime) / 1_000_000_000.0f;
        immersiveHotbar$lastOverlayFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }
}