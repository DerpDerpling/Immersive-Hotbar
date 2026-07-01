package derp.immersivehotbar.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TooltipAnimationState {
    public static float tooltipScale = 0.0f;
    public static float lastKnownFadeSeconds = 0;
    public static ItemStack lastStack = ItemStack.EMPTY;
    public static Component lastTooltipText = Component.empty();
    public static int lastTextWidth = 0;

    public static void reset() {
        tooltipScale = 0f;
        lastKnownFadeSeconds = 0f;
        lastTooltipText = Component.empty();
        lastTextWidth = 0;
        lastStack = ItemStack.EMPTY;
    }
}
