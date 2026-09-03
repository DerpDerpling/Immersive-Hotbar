package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationEngine;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

@Mixin(Gui.class)
public abstract class OverlayMessageAnimationsMixin {
    @Shadow private int overlayMessageTime;

    @Unique private Component immersiveHotbar$lastOverlayMessage = Component.empty();
    @Unique private float immersiveHotbar$overlayScale = 0.0f;
    @Unique private float immersiveHotbar$shrinkProgress = 0.0f;
    @Unique private boolean immersiveHotbar$forcedShrink = false;
    @Unique private boolean immersiveHotbar$naturalShrinkStarted = false;
    @Unique private long immersiveHotbar$lastOverlayFrameTime = System.nanoTime();

    @WrapOperation(method = "extractOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
    private void immersiveHotbar$animateOverlayMessage(GuiGraphicsExtractor instance, Font font, Component str, int textX, int textY, int textWidth, int textColor, Operation<Void> original) {
        if (!animateOverlayMessages) {
            original.call(instance, font, str, textX, textY, textWidth, textColor);
            return;
        }

        float dt = immersiveHotbar$frameDelta();
        boolean empty = str.getString().isEmpty();

        if (empty && !immersiveHotbar$lastOverlayMessage.getString().isEmpty()) {
            if (!immersiveHotbar$forcedShrink) {
                immersiveHotbar$forcedShrink = true;

                if (!immersiveHotbar$naturalShrinkStarted) {
                    immersiveHotbar$shrinkProgress = 0.0f;
                    immersiveHotbar$overlayScale = 1.0f;
                }
            }
        } else if (!empty && !str.equals(immersiveHotbar$lastOverlayMessage)) {
            immersiveHotbar$lastOverlayMessage = str;
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
            } else if (overlayMessageTime <= 1) {
                immersiveHotbar$lastOverlayMessage = Component.empty();
                immersiveHotbar$shrinkProgress = 0.0f;
                immersiveHotbar$naturalShrinkStarted = false;
            }

            return;
        }

        Component drawText = immersiveHotbar$forcedShrink ? immersiveHotbar$lastOverlayMessage : str;
        int drawWidth = font.width(drawText);
        float vanillaCenterX = textX + textWidth / 2.0f;
        int drawX = immersiveHotbar$forcedShrink ? Math.round(vanillaCenterX - drawWidth / 2.0f) : textX;

        float centerX = drawX + drawWidth / 2.0f;
        float centerY = textY + font.lineHeight / 2.0f;

        instance.pose().pushMatrix();
        instance.pose().translate(centerX, centerY);
        instance.pose().scale(immersiveHotbar$overlayScale, immersiveHotbar$overlayScale);
        instance.pose().translate(-centerX, -centerY);

        original.call(instance, font, drawText, drawX, textY, drawWidth, textColor);

        instance.pose().popMatrix();

        if (!immersiveHotbar$forcedShrink && overlayMessageTime <= 1) {
            immersiveHotbar$lastOverlayMessage = Component.empty();
            immersiveHotbar$overlayScale = 0.0f;
            immersiveHotbar$shrinkProgress = 0.0f;
            immersiveHotbar$naturalShrinkStarted = false;
        }
    }

    @Unique
    private float immersiveHotbar$frameDelta() {
        long now = System.nanoTime();
        float dt = (now - immersiveHotbar$lastOverlayFrameTime) / 1_000_000_000.0f;
        immersiveHotbar$lastOverlayFrameTime = now;

        return Mth.clamp(dt, 0.0f, 0.05f);
    }
}