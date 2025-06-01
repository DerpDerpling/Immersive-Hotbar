package derp.immersivehotbar.util;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class TooltipAnimationState {
    public static float tooltipScale = 0.0f;
    public static float lastKnownFadeSeconds = 0;
    public static ItemStack lastStack = ItemStack.EMPTY;
    public static Text lastTooltipText = Text.empty();
    public static int lastTextWidth = 0;

    public static void reset() {
        tooltipScale = 0f;
        lastKnownFadeSeconds = 0f;
        lastTooltipText = Text.empty();
        lastTextWidth = 0;
        lastStack = ItemStack.EMPTY;
    }
}
