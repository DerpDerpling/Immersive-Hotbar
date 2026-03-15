package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
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

@Mixin(InGameHud.class)
public class TooltipAnimationsMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private ItemStack currentStack;
    @Shadow private int heldItemTooltipFade;

    @Unique private static final int EMPTY_FADE_TICKS = 4;

    @Unique private long lastRenderTime = System.nanoTime();
    @Unique private ItemStack cachedTooltipStack = ItemStack.EMPTY;
    @Unique private int emptyFadeTicksRemaining = 0;
    @Unique private boolean emptyFadeArmed = false;

    @Unique private boolean spoofedThisCall = false;
    @Unique private ItemStack realCurrentStack = ItemStack.EMPTY;
    @Unique private int realHeldItemTooltipFade = 0;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        if (!immersiveToolTip || client.player == null) return;

        ItemStack realMainHand = client.player.getInventory().getSelectedStack();

        if (!realMainHand.isEmpty()) {
            cachedTooltipStack = realMainHand.copy();
            emptyFadeArmed = false;
        }

        if (emptyFadeTicksRemaining > 0) {
            if (realMainHand.isEmpty()) emptyFadeTicksRemaining--;
            else emptyFadeTicksRemaining = 0;
        }
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getSelectedStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack keepTooltipAliveWhenMovingToEmpty(PlayerInventory inventory) {
        ItemStack nextStack = inventory.getSelectedStack();
        if (!immersiveToolTip) return nextStack;

        boolean shouldStartEmptyFade = !emptyFadeArmed
                && emptyFadeTicksRemaining <= 0
                && nextStack.isEmpty()
                && !cachedTooltipStack.isEmpty()
                && this.heldItemTooltipFade > 0;

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

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"))
    private void updateTooltipAnimationState(DrawContext context, CallbackInfo ci) {
        spoofedThisCall = false;
        if (!immersiveToolTip) return;

        long currentTime = System.nanoTime();
        float deltaSeconds = (currentTime - lastRenderTime) / 1_000_000_000.0f;
        deltaSeconds = MathHelper.clamp(deltaSeconds, 0f, 0.05f);
        lastRenderTime = currentTime;

        boolean realMainHandEmpty = client.player == null || client.player.getInventory().getSelectedStack().isEmpty();
        boolean doingEmptyFade = realMainHandEmpty && emptyFadeTicksRemaining > 0 && !cachedTooltipStack.isEmpty();

        if (doingEmptyFade) {
            realCurrentStack = this.currentStack;
            realHeldItemTooltipFade = this.heldItemTooltipFade;
            this.currentStack = cachedTooltipStack;
            this.heldItemTooltipFade = emptyFadeTicksRemaining;
            spoofedThisCall = true;
        }

        ItemStack actualStack = this.currentStack;
        boolean isCurrentEmpty = actualStack.isEmpty();
        boolean isHoldingItem = !isCurrentEmpty;
        boolean stackChanged = !actualStack.isEmpty() && (!lastStack.isOf(actualStack.getItem()) || !ItemStack.areItemsEqual(lastStack, actualStack));

        if (!isHoldingItem && heldItemTooltipFade <= 0 && lastKnownFadeSeconds <= 0f) {
            tooltipScale = 0f;
            return;
        }

        if (!isCurrentEmpty && stackChanged) tooltipScale = 1.2f;
        if (!isCurrentEmpty) lastStack = actualStack.copy();

        if (heldItemTooltipFade > 0) lastKnownFadeSeconds = heldItemTooltipFade / 20.0f;
        else if (lastKnownFadeSeconds > 0f) {
            lastKnownFadeSeconds -= deltaSeconds;
            if (lastKnownFadeSeconds < 0f) lastKnownFadeSeconds = 0f;
        }

        float fadeSeconds = lastKnownFadeSeconds;
        if (fadeSeconds > 0f) {
            float fadeRatio = Math.min(fadeSeconds / 0.2f, 1.0f);
            float targetScale = 0.5f + (fadeRatio * 0.5f);
            tooltipScale += (targetScale - tooltipScale) * (8.0f * deltaSeconds);
            tooltipScale = MathHelper.clamp(tooltipScale, 0.0f, 1.5f);
        } else {
            tooltipScale += (0.0f - tooltipScale) * (10.0f * deltaSeconds);
        }

        if (lastKnownFadeSeconds <= 0f && tooltipScale <= 0.01f) {
            tooltipScale = 0f;
            if (realMainHandEmpty) cachedTooltipStack = ItemStack.EMPTY;
        }
    }

    @WrapOperation(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"))
    private int wrapTooltipDraw(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int width, int color, Operation<Integer> original) {
        if (!immersiveToolTip || tooltipScale <= 0.01f) return original.call(context, textRenderer, text, x, y, width, color);
        if (lastKnownFadeSeconds <= 0f) return original.call(context, textRenderer, text, x, y, width, color);

        lastTooltipText = text;
        lastTextWidth = width;

        float fadeRatio = Math.min(lastKnownFadeSeconds / 0.2f, 1.0f);
        int alpha = (int)(fadeRatio * 255);
        if (fadeRatio < 0.1f) return 0;

        int drawY = y;
        if (tooltipYOffsetEnabled) {
            int screenHeight = client.getWindow().getScaledHeight();
            drawY = screenHeight - Math.round(tooltipYOffset * (screenHeight / 240f));
            if (client.interactionManager != null && !client.interactionManager.hasStatusBars()) drawY += 14;
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x + width / 2.0f, drawY + 4, 0);
        matrices.scale(tooltipScale, tooltipScale, 1.0f);
        matrices.translate(-(x + width / 2.0f), -(drawY + 4), 0);

        int result = original.call(context, textRenderer, text, x, drawY, width, ColorHelper.withAlpha(alpha, color));
        matrices.pop();
        return result;
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("RETURN"))
    private void restoreSpoofedTooltipState(DrawContext context, CallbackInfo ci) {
        if (!spoofedThisCall) return;
        this.currentStack = realCurrentStack;
        this.heldItemTooltipFade = realHeldItemTooltipFade;
        spoofedThisCall = false;
    }
}