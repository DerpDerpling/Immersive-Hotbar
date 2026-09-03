package derp.immersivehotbar.animation.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TooltipAnimationState {
    private float scale = 0.0f;
    private float fadeSeconds = 0.0f;
    private ItemStack lastStack = ItemStack.EMPTY;
    private Component lastText = Component.empty();
    private int lastTextWidth = 0;

    public float scale() {
        return scale;
    }

    public void scale(float scale) {
        this.scale = scale;
    }

    public float fadeSeconds() {
        return fadeSeconds;
    }

    public void fadeSeconds(float fadeSeconds) {
        this.fadeSeconds = fadeSeconds;
    }

    public ItemStack lastStack() {
        return lastStack;
    }

    public void lastStack(ItemStack stack) {
        this.lastStack = stack;
    }

    public Component lastText() {
        return lastText;
    }

    public void lastText(Component text) {
        this.lastText = text;
    }

    public int lastTextWidth() {
        return lastTextWidth;
    }

    public void lastTextWidth(int width) {
        this.lastTextWidth = width;
    }

    public float fadeRatio() {
        return Math.min(fadeSeconds / 0.2f, 1.0f);
    }

    public void reset() {
        scale = 0.0f;
        fadeSeconds = 0.0f;
        lastStack = ItemStack.EMPTY;
        lastText = Component.empty();
        lastTextWidth = 0;
    }
}
