package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationEngine;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

@Mixin(Gui.class)
public abstract class OverlayMessageAnimationsMixin {
    @Shadow private int overlayMessageTime;

    @Unique private Component immersiveHotbar$lastOverlayMessage = Component.empty();
    @Unique private String immersiveHotbar$lastOverlayString = "";
    @Unique private float immersiveHotbar$overlayScale = 0.0f;
    @Unique private float immersiveHotbar$shrinkProgress = 0.0f;
    @Unique private boolean immersiveHotbar$forcedShrink = false;
    @Unique private boolean immersiveHotbar$naturalShrinkStarted = false;
    @Unique private long immersiveHotbar$lastOverlayFrameTime = System.nanoTime();

    @Unique private int immersiveHotbar$changedStart = -1;
    @Unique private int immersiveHotbar$changedEnd = -1;
    @Unique private float immersiveHotbar$changedFlash = 0.0f;
    @Unique private long immersiveHotbar$lastOverlayChangeTime = 0L;

    @Unique
    private boolean immersiveHotbar$isIncrementalUpdate(String oldText, String newText) {
        if (oldText.isEmpty() || newText.isEmpty()) return false;

        int prefix = 0;
        int min = Math.min(oldText.length(), newText.length());

        while (prefix < min && oldText.charAt(prefix) == newText.charAt(prefix)) prefix++;

        int suffix = 0;
        while (suffix < min - prefix
                && oldText.charAt(oldText.length() - 1 - suffix)
                == newText.charAt(newText.length() - 1 - suffix)) {
            suffix++;
        }

        return prefix + suffix >= min * 0.25f;
    }

    @WrapOperation(method = "extractOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
    private void immersiveHotbar$animateOverlayMessage(GuiGraphicsExtractor instance, Font font, Component str, int textX, int textY, int textWidth, int textColor, Operation<Void> original) {
        if (!animateOverlayMessages) {
            original.call(instance, font, str, textX, textY, textWidth, textColor);
            return;
        }

        float dt = immersiveHotbar$frameDelta();
        immersiveHotbar$changedFlash = Math.max(0.0f, immersiveHotbar$changedFlash - dt * 5.0f);

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
            String newText = str.getString();
            String oldText = immersiveHotbar$lastOverlayString;

            long now = System.currentTimeMillis();
            boolean rapidUpdate = now - immersiveHotbar$lastOverlayChangeTime < 750L;

            boolean updatingExisting =
                    !immersiveHotbar$forcedShrink
                            && immersiveHotbar$overlayScale > 0.0f
                            && (rapidUpdate || immersiveHotbar$isIncrementalUpdate(oldText, newText));

            immersiveHotbar$lastOverlayChangeTime = now;

            if (updatingExisting) {
                immersiveHotbar$findChangedRegion(oldText, newText);
                immersiveHotbar$changedFlash = 1.0f;
                immersiveHotbar$overlayScale = 1.0f;
            } else {
                immersiveHotbar$overlayScale = 1.2f;
                immersiveHotbar$changedStart = -1;
                immersiveHotbar$changedEnd = -1;
                immersiveHotbar$changedFlash = 0.0f;
            }

            immersiveHotbar$lastOverlayMessage = str;
            immersiveHotbar$lastOverlayString = newText;
            immersiveHotbar$shrinkProgress = 0.0f;
            immersiveHotbar$forcedShrink = false;
            immersiveHotbar$naturalShrinkStarted = false;
        }

        if (immersiveHotbar$forcedShrink || overlayMessageTime <= 18) {
            if (!immersiveHotbar$forcedShrink) immersiveHotbar$naturalShrinkStarted = true;

            immersiveHotbar$shrinkProgress += dt * shrinkAnimationSpeed;
            immersiveHotbar$overlayScale = HotbarAnimationEngine.shrinkOutScale(
                    1.0f,
                    immersiveHotbar$shrinkProgress,
                    bouncyAnimation
            );
        } else {
            immersiveHotbar$overlayScale = TooltipAnimationController.updateScale(
                    immersiveHotbar$overlayScale,
                    1.0f,
                    dt
            );

            immersiveHotbar$shrinkProgress = 0.0f;
            immersiveHotbar$naturalShrinkStarted = false;
        }

        if (immersiveHotbar$overlayScale <= 0.01f) {
            immersiveHotbar$overlayScale = 0.0f;

            if (immersiveHotbar$forcedShrink || overlayMessageTime <= 1) {
                immersiveHotbar$resetOverlayState();
            }

            return;
        }

        Component drawText = immersiveHotbar$forcedShrink ? immersiveHotbar$lastOverlayMessage : str;
        int drawWidth = font.width(drawText);

        float vanillaCenterX = textX + textWidth / 2.0f;
        int drawX = immersiveHotbar$forcedShrink
                ? Math.round(vanillaCenterX - drawWidth / 2.0f)
                : textX;

        float centerX = drawX + drawWidth / 2.0f;
        float centerY = textY + font.lineHeight / 2.0f;

        instance.pose().pushMatrix();
        instance.pose().translate(centerX, centerY);
        instance.pose().scale(immersiveHotbar$overlayScale, immersiveHotbar$overlayScale);
        instance.pose().translate(-centerX, -centerY);

        original.call(instance, font, drawText, drawX, textY, drawWidth, textColor);

        instance.pose().popMatrix();

        if (!immersiveHotbar$forcedShrink) {
            immersiveHotbar$renderChangedFlash(instance, font, drawText, drawX, textY);
        }

        if (!immersiveHotbar$forcedShrink && overlayMessageTime <= 1) {
            immersiveHotbar$resetOverlayState();
        }
    }

    @Unique
    private void immersiveHotbar$renderChangedFlash(GuiGraphicsExtractor instance, Font font, Component text, int drawX, int textY) {
        if (immersiveHotbar$changedFlash <= 0.001f
                || immersiveHotbar$changedStart < 0
                || immersiveHotbar$changedEnd <= immersiveHotbar$changedStart) {
            return;
        }

        String full = text.getString();

        if (immersiveHotbar$changedStart >= full.length()
                || immersiveHotbar$changedEnd > full.length()) {
            return;
        }

        Component before = immersiveHotbar$sliceComponent(text, 0, immersiveHotbar$changedStart);
        Component changed = immersiveHotbar$sliceComponent(
                text,
                immersiveHotbar$changedStart,
                immersiveHotbar$changedEnd
        );

        int changedX = drawX + font.width(before);
        int alpha = Mth.clamp((int) (immersiveHotbar$changedFlash * 180.0f), 0, 180);
        int color = (alpha << 24) | 0xFFFFFF;

        instance.text(font, changed, changedX, textY, color, false);
    }

    @Unique
    private Component immersiveHotbar$sliceComponent(Component component, int start, int end) {
        MutableComponent result = Component.empty();
        int[] offset = {0};

        component.visit((style, text) -> {
            int segmentStart = offset[0];
            int segmentEnd = segmentStart + text.length();

            int localStart = Math.max(start, segmentStart);
            int localEnd = Math.min(end, segmentEnd);

            if (localStart < localEnd) {
                result.append(Component.literal(
                        text.substring(localStart - segmentStart, localEnd - segmentStart)
                ).withStyle(style));
            }

            offset[0] = segmentEnd;
            return Optional.empty();
        }, component.getStyle());

        return result;
    }

    @Unique
    private void immersiveHotbar$findChangedRegion(String oldText, String newText) {
        int prefix = 0;
        int minLength = Math.min(oldText.length(), newText.length());

        while (prefix < minLength && oldText.charAt(prefix) == newText.charAt(prefix)) {
            prefix++;
        }

        int oldEnd = oldText.length();
        int newEnd = newText.length();

        while (oldEnd > prefix
                && newEnd > prefix
                && oldText.charAt(oldEnd - 1) == newText.charAt(newEnd - 1)) {
            oldEnd--;
            newEnd--;
        }

        immersiveHotbar$changedStart = prefix;
        immersiveHotbar$changedEnd = newEnd;
    }

    @Unique
    private void immersiveHotbar$resetOverlayState() {
        immersiveHotbar$lastOverlayMessage = Component.empty();
        immersiveHotbar$lastOverlayString = "";
        immersiveHotbar$overlayScale = 0.0f;
        immersiveHotbar$shrinkProgress = 0.0f;
        immersiveHotbar$forcedShrink = false;
        immersiveHotbar$naturalShrinkStarted = false;
        immersiveHotbar$changedStart = -1;
        immersiveHotbar$changedEnd = -1;
        immersiveHotbar$changedFlash = 0.0f;
    }

    @Unique
    private float immersiveHotbar$frameDelta() {
        long now = System.nanoTime();
        float dt = (now - immersiveHotbar$lastOverlayFrameTime) / 1_000_000_000.0f;
        immersiveHotbar$lastOverlayFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }
}