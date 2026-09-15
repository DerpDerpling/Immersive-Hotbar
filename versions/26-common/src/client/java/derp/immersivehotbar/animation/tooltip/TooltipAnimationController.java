package derp.immersivehotbar.animation.tooltip;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class TooltipAnimationController {
    private static final int EMPTY_FADE_TICKS = 4;
    private static final TooltipAnimationController INSTANCE = new TooltipAnimationController();

    private final TooltipAnimationState state = new TooltipAnimationState();

    private ItemStack cachedTooltipStack = ItemStack.EMPTY;
    private int emptyFadeTicksRemaining = 0;
    private boolean emptyFadeArmed = false;

    public static TooltipAnimationController getInstance() {
        return INSTANCE;
    }

    public TooltipAnimationState state() {
        return state;
    }

    public void reset() {
        state.reset();
        cachedTooltipStack = ItemStack.EMPTY;
        emptyFadeTicksRemaining = 0;
        emptyFadeArmed = false;
    }

    public void tick(ItemStack realMainHand) {
        if (!realMainHand.isEmpty()) {
            cachedTooltipStack = realMainHand.copy();
            emptyFadeArmed = false;
        }

        if (emptyFadeTicksRemaining > 0) {
            if (realMainHand.isEmpty()) emptyFadeTicksRemaining--;
            else emptyFadeTicksRemaining = 0;
        }
    }

    public ItemStack selectedStackForTick(ItemStack selectedStack, int highlightTimer) {
        if (!tooltipAnimateOnEmptySlot) {
            if (!selectedStack.isEmpty()) cachedTooltipStack = selectedStack.copy();
            return selectedStack;
        }

        boolean shouldStartEmptyFade = !emptyFadeArmed && emptyFadeTicksRemaining <= 0 && selectedStack.isEmpty() && !cachedTooltipStack.isEmpty() && highlightTimer > 0;

        if (shouldStartEmptyFade) {
            emptyFadeTicksRemaining = EMPTY_FADE_TICKS;
            emptyFadeArmed = true;
        }

        if (emptyFadeTicksRemaining > 0 && selectedStack.isEmpty() && !cachedTooltipStack.isEmpty()) return cachedTooltipStack;

        if (!selectedStack.isEmpty()) cachedTooltipStack = selectedStack.copy();
        return selectedStack;
    }

    public RenderOverride prepareRender(ItemStack guiStack, int highlightTimer, boolean realMainHandEmpty, float deltaSeconds) {
        if (tooltipAnimateOnEmptySlot && realMainHandEmpty && !emptyFadeArmed && emptyFadeTicksRemaining <= 0 && !cachedTooltipStack.isEmpty() && (highlightTimer > 0 || state.fadeSeconds() > 0.0f)) {
            emptyFadeTicksRemaining = EMPTY_FADE_TICKS;
            emptyFadeArmed = true;
        }

        boolean spoof = tooltipAnimateOnEmptySlot && realMainHandEmpty && emptyFadeTicksRemaining > 0 && !cachedTooltipStack.isEmpty();

        ItemStack effectiveStack = spoof ? cachedTooltipStack : guiStack;
        int effectiveTimer = spoof ? emptyFadeTicksRemaining : highlightTimer;

        updateAnimation(effectiveStack, effectiveTimer, realMainHandEmpty, deltaSeconds);
        return new RenderOverride(effectiveStack, effectiveTimer, spoof);
    }

    public void updatePreview(ItemStack stack, float fadeSeconds, float deltaSeconds) {
        boolean changed = !stack.isEmpty() && (!state.lastStack().is(stack.getItem()) || !ItemStack.isSameItem(state.lastStack(), stack));

        if (changed && tooltipAnimateOnItemChange) state.scale(tooltipPopScale);
        if (!stack.isEmpty()) state.lastStack(stack.copy());

        state.fadeSeconds(Math.max(fadeSeconds, 0.0f));
        updateScale(deltaSeconds);
    }

    private void updateAnimation(ItemStack stack, int highlightTimer, boolean realMainHandEmpty, float deltaSeconds) {
        boolean holdingItem = !stack.isEmpty();
        boolean changed = holdingItem && (!state.lastStack().is(stack.getItem()) || !ItemStack.isSameItem(state.lastStack(), stack));

        if (!holdingItem && highlightTimer <= 0 && state.fadeSeconds() <= 0.0f) {
            state.scale(0.0f);
            return;
        }

        if (changed && tooltipAnimateOnItemChange) state.scale(tooltipPopScale);
        if (holdingItem) state.lastStack(stack.copy());

        if (highlightTimer > 0) state.fadeSeconds(highlightTimer / 20.0f);
        else if (state.fadeSeconds() > 0.0f) state.fadeSeconds(Math.max(0.0f, state.fadeSeconds() - deltaSeconds));

        updateScale(deltaSeconds);

        if (state.fadeSeconds() <= 0.0f && state.scale() <= 0.01f) {
            state.scale(0.0f);
            if (realMainHandEmpty) cachedTooltipStack = ItemStack.EMPTY;
        }
    }

    private void updateScale(float deltaSeconds) {
        state.scale(updateScale(state.scale(), state.fadeRatio(), deltaSeconds));
    }

    public static float updateScale(float scale, float fadeRatio, float deltaSeconds) {
        if (fadeRatio > 0.0f) {
            float targetScale = tooltipMinimumFadeScale + fadeRatio * (1.0f - tooltipMinimumFadeScale);
            scale += (targetScale - scale) * (tooltipAnimationSpeed * deltaSeconds);
            return Mth.clamp(scale, 0.0f, Math.max(1.5f, tooltipPopScale));
        }

        scale += (0.0f - scale) * (tooltipShrinkSpeed * deltaSeconds);
        return scale < 0.01f ? 0.0f : scale;
    }

    public record RenderOverride(ItemStack stack, int highlightTimer, boolean spoofed) {}
}