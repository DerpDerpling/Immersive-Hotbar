package derp.immersivehotbar.util;

import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class SlotAnimationState {
    public static final  float[] slotScales = new float[10];
    public static final ItemStack[] lastSlotStacks = new ItemStack[] {
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY
    };

    public static final int[] lastSlotCounts = new int[10];
    public static final boolean[] isShrinking = new boolean[10];
    public static final float[] shrinkProgress = new float[10];
    public static float[] slotVelocities = new float[10];
    public static float[] selectorScales = new float[10];

    public static final boolean[] wasUsed = new boolean[10];
    public static final boolean[] suppressNextPickup = new boolean[10];
    public static final int[] lastSlotDamage = new int[10];
    public static final ItemStack[] previousStacks = new ItemStack[10];

    static {
        Arrays.fill(selectorScales, 1.0f);
    }
}

