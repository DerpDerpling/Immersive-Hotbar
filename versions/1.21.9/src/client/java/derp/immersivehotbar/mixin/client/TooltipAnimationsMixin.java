package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.TooltipAnimationState.*;

@Mixin(Gui.class)
public class TooltipAnimationsMixin {
    @Unique
    private static final int EMPTY_FADE_TICKS = 4;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ItemStack lastToolHighlight;
    @Shadow
    private int toolHighlightTimer;
    @Unique
    private long lastRenderTime = System.nanoTime();
    @Unique
    private ItemStack cachedTooltipStack = ItemStack.EMPTY;
    @Unique
    private int emptyFadeTicksRemaining = 0;
    @Unique
    private boolean emptyFadeArmed = false;

    @Unique
    private boolean spoofedThisCall = false;
    @Unique
    private ItemStack realCurrentStack = ItemStack.EMPTY;
    @Unique
    private int realHeldItemTooltipFade = 0;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        if (!immersiveToolTip || minecraft.player == null) return;

        ItemStack realMainHand = minecraft.player.getInventory().getSelectedItem();

        if (!realMainHand.isEmpty()) {
            cachedTooltipStack = realMainHand.copy();
            emptyFadeArmed = false;
        }

        if (emptyFadeTicksRemaining > 0) {
            if (realMainHand.isEmpty()) emptyFadeTicksRemaining--;
            else emptyFadeTicksRemaining = 0;
        }
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack keepTooltipAliveWhenMovingToEmpty(Inventory inventory) {
        ItemStack nextStack = inventory.getSelectedItem();
        if (!immersiveToolTip) return nextStack;

        boolean shouldStartEmptyFade = !emptyFadeArmed && emptyFadeTicksRemaining <= 0 && nextStack.isEmpty() && !cachedTooltipStack.isEmpty() && this.toolHighlightTimer > 0;

        if (shouldStartEmptyFade) {
            emptyFadeTicksRemaining = EMPTY_FADE_TICKS;
            emptyFadeArmed = true;
        }

        if (emptyFadeTicksRemaining > 0 && nextStack.isEmpty() && !cachedTooltipStack.isEmpty()) {
            return cachedTooltipStack;
        }

        if (!nextStack.isEmpty()) cachedTooltipStack = nextStack.copy();
        return nextStack;
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"))
    private void updateTooltipAnimationState(GuiGraphics context, CallbackInfo ci) {
        spoofedThisCall = false;
        if (!immersiveToolTip) return;

        long currentTime = System.nanoTime();
        float deltaSeconds = (currentTime - lastRenderTime) / 1_000_000_000.0f;
        deltaSeconds = Mth.clamp(deltaSeconds, 0f, 0.05f);
        lastRenderTime = currentTime;

        boolean realMainHandEmpty = minecraft.player == null || minecraft.player.getInventory().getSelectedItem().isEmpty();
        boolean doingEmptyFade = realMainHandEmpty && emptyFadeTicksRemaining > 0 && !cachedTooltipStack.isEmpty();

        if (doingEmptyFade) {
            realCurrentStack = this.lastToolHighlight;
            realHeldItemTooltipFade = this.toolHighlightTimer;
            this.lastToolHighlight = cachedTooltipStack;
            this.toolHighlightTimer = emptyFadeTicksRemaining;
            spoofedThisCall = true;
        }

        ItemStack actualStack = this.lastToolHighlight;
        boolean isCurrentEmpty = actualStack.isEmpty();
        boolean isHoldingItem = !isCurrentEmpty;
        boolean stackChanged = !actualStack.isEmpty() && (!lastStack.is(actualStack.getItem()) || !ItemStack.isSameItem(lastStack, actualStack));

        if (!isHoldingItem && toolHighlightTimer <= 0 && lastKnownFadeSeconds <= 0f) {
            tooltipScale = 0f;
            return;
        }

        if (!isCurrentEmpty && stackChanged) tooltipScale = 1.2f;
        if (!isCurrentEmpty) lastStack = actualStack.copy();

        if (toolHighlightTimer > 0) lastKnownFadeSeconds = toolHighlightTimer / 20.0f;
        else if (lastKnownFadeSeconds > 0f) {
            lastKnownFadeSeconds -= deltaSeconds;
            if (lastKnownFadeSeconds < 0f) lastKnownFadeSeconds = 0f;
        }

        float fadeSeconds = lastKnownFadeSeconds;
        if (fadeSeconds > 0f) {
            float fadeRatio = Math.min(fadeSeconds / 0.2f, 1.0f);
            float targetScale = 0.5f + (fadeRatio * 0.5f);
            tooltipScale += (targetScale - tooltipScale) * (8.0f * deltaSeconds);
            tooltipScale = Mth.clamp(tooltipScale, 0.0f, 1.5f);
        } else {
            tooltipScale += (0.0f - tooltipScale) * (10.0f * deltaSeconds);
        }

        if (lastKnownFadeSeconds <= 0f && tooltipScale <= 0.01f) {
            tooltipScale = 0f;
            if (realMainHandEmpty) cachedTooltipStack = ItemStack.EMPTY;
        }
    }

    @WrapOperation(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
    private void wrapTooltipDraw(GuiGraphics context, Font textRenderer, Component text, int x, int y, int width, int color, Operation<Void> original) {
        if (!immersiveToolTip || tooltipScale <= 0.01f) {
            original.call(context, textRenderer, text, x, y, width, color);
            return;
        }

        if (lastKnownFadeSeconds <= 0f) {
            original.call(context, textRenderer, text, x, y, width, color);
            return;
        }

        lastTooltipText = text;
        lastTextWidth = width;

        float fadeRatio = Math.min(lastKnownFadeSeconds / 0.2f, 1.0f);
        int alpha = (int) (fadeRatio * 255);
        if (fadeRatio < 0.1f) {
            return;
        }

        int drawY = y;
        if (tooltipYOffsetEnabled) {
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            drawY = screenHeight - Math.round(tooltipYOffset * (screenHeight / 240f));
            if (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer()) {
                drawY += 14;
            }
        }

        org.joml.Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x + width / 2.0f, drawY + 4);
        matrices.scale(tooltipScale, tooltipScale);
        matrices.translate(-(x + width / 2.0f), -(drawY + 4));

        original.call(context, textRenderer, text, x, drawY, width, ARGB.color(alpha, color));

        matrices.popMatrix();
    }

    @Inject(method = "renderSelectedItemName", at = @At("RETURN"))
    private void restoreSpoofedTooltipState(GuiGraphics context, CallbackInfo ci) {
        if (!spoofedThisCall) return;
        this.lastToolHighlight = realCurrentStack;
        this.toolHighlightTimer = realHeldItemTooltipFade;
        spoofedThisCall = false;
    }
}