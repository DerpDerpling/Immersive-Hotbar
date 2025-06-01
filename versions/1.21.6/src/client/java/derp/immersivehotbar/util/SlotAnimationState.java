package derp.immersivehotbar.util;

import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class SlotAnimationState {
    public static float[] slotScales = new float[20];
    public static ItemStack[] lastSlotStacks = createItemStackArray(20);
    public static int[] lastSlotCounts = new int[20];
    public static boolean[] isShrinking = new boolean[20];
    public static float[] shrinkProgress = new float[20];
    public static float[] slotVelocities = new float[20];
    public static long[] lastShrinkTime = new long[20];
    public static float[] selectorScales = createFilledFloatArray(20, 1.0f);
    public static boolean[] wasUsed = new boolean[20];
    public static boolean[] suppressNextPickup = new boolean[20];
    public static int[] lastSlotDamage = new int[20];
    public static ItemStack[] previousStacks = createItemStackArray(20);

    private static ItemStack[] createItemStackArray(int size) {
        ItemStack[] array = new ItemStack[size];
        Arrays.fill(array, ItemStack.EMPTY);
        return array;
    }

    private static float[] createFilledFloatArray(int size, float value) {
        float[] array = new float[size];
        Arrays.fill(array, value);
        return array;
    }

    public static void ensureCapacity(int slot) {
        if (slot < lastSlotStacks.length) {
            return;
        }

        int newSize = Math.max(slot + 1, lastSlotStacks.length * 2);

        slotScales = Arrays.copyOf(slotScales, newSize);
        lastSlotStacks = Arrays.copyOf(lastSlotStacks, newSize);
        lastSlotCounts = Arrays.copyOf(lastSlotCounts, newSize);
        isShrinking = Arrays.copyOf(isShrinking, newSize);
        shrinkProgress = Arrays.copyOf(shrinkProgress, newSize);
        slotVelocities = Arrays.copyOf(slotVelocities, newSize);
        lastShrinkTime = Arrays.copyOf(lastShrinkTime, newSize);
        selectorScales = Arrays.copyOf(selectorScales, newSize);
        wasUsed = Arrays.copyOf(wasUsed, newSize);
        suppressNextPickup = Arrays.copyOf(suppressNextPickup, newSize);
        lastSlotDamage = Arrays.copyOf(lastSlotDamage, newSize);
        previousStacks = Arrays.copyOf(previousStacks, newSize);

        for (int i = 0; i < newSize; i++) {
            if (lastSlotStacks[i] == null) lastSlotStacks[i] = ItemStack.EMPTY;
            if (previousStacks[i] == null) previousStacks[i] = ItemStack.EMPTY;
        }

        for (int i = selectorScales.length / 2; i < selectorScales.length; i++) {
            selectorScales[i] = 1.0f;
        }
    }
}
