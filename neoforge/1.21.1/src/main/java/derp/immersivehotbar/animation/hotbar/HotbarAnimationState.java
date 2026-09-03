package derp.immersivehotbar.animation.hotbar;

import net.minecraft.world.item.ItemStack;

import java.util.Arrays;


public final class HotbarAnimationState {
    private float[] slotScales = new float[20];
    public static ItemStack[] lastSlotStacks = createItemStackArray(20);
    public int[] lastSlotCounts = new int[20];
    private boolean[] shrinking = new boolean[20];
    private float[] shrinkProgress = new float[20];
    private float[] slotVelocities = new float[20];
    private float[] selectorScales = createFilledFloatArray(20, 1.0f);
    private boolean[] used = new boolean[20];
    private boolean[] suppressNextPickup = new boolean[20];
    private int[] lastSlotDamage = new int[20];
    private ItemStack[] previousStacks = createItemStackArray(20);

    public void initialize(float defaultScale) {
        Arrays.fill(slotScales, defaultScale);
        Arrays.fill(selectorScales, 1.0f);
        Arrays.fill(slotVelocities, 0.0f);
        Arrays.fill(shrinkProgress, 0.0f);
        Arrays.fill(shrinking, false);
        Arrays.fill(previousStacks, ItemStack.EMPTY);
    }

    public void resetSlot(int slot, float defaultScale) {
        ensureCapacity(slot);
        slotScales[slot] = defaultScale;
        selectorScales[slot] = 1.0f;
        slotVelocities[slot] = 0.0f;
        shrinkProgress[slot] = 0.0f;
        shrinking[slot] = false;
        previousStacks[slot] = ItemStack.EMPTY;
        lastSlotStacks[slot] = ItemStack.EMPTY;
        lastSlotCounts[slot] = 0;
        lastSlotDamage[slot] = 0;
        suppressNextPickup[slot] = false;
        used[slot] = false;
    }

    public void clearTrackedStacks() {
        Arrays.fill(lastSlotStacks, ItemStack.EMPTY);
    }

    public void ensureCapacity(int slot) {
        if (slot < slotScales.length) return;

        int oldSize = slotScales.length;
        int newSize = Math.max(slot + 1, oldSize * 2);

        slotScales = Arrays.copyOf(slotScales, newSize);
        lastSlotStacks = Arrays.copyOf(lastSlotStacks, newSize);
        lastSlotCounts = Arrays.copyOf(lastSlotCounts, newSize);
        shrinking = Arrays.copyOf(shrinking, newSize);
        shrinkProgress = Arrays.copyOf(shrinkProgress, newSize);
        slotVelocities = Arrays.copyOf(slotVelocities, newSize);
        selectorScales = Arrays.copyOf(selectorScales, newSize);
        used = Arrays.copyOf(used, newSize);
        suppressNextPickup = Arrays.copyOf(suppressNextPickup, newSize);
        lastSlotDamage = Arrays.copyOf(lastSlotDamage, newSize);
        previousStacks = Arrays.copyOf(previousStacks, newSize);

        for (int i = oldSize; i < newSize; i++) {
            lastSlotStacks[i] = ItemStack.EMPTY;
            previousStacks[i] = ItemStack.EMPTY;
            selectorScales[i] = 1.0f;
        }
    }

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

    public float scale(int slot) { ensureCapacity(slot); return slotScales[slot]; }
    public void scale(int slot, float value) { ensureCapacity(slot); slotScales[slot] = value; }
    public float selectorScale(int slot) { ensureCapacity(slot); return selectorScales[slot]; }
    public void selectorScale(int slot, float value) { ensureCapacity(slot); selectorScales[slot] = value; }
    public float velocity(int slot) { ensureCapacity(slot); return slotVelocities[slot]; }
    public void velocity(int slot, float value) { ensureCapacity(slot); slotVelocities[slot] = value; }
    public ItemStack lastStack(int slot) { ensureCapacity(slot); return lastSlotStacks[slot]; }
    public void lastStack(int slot, ItemStack value) { ensureCapacity(slot); lastSlotStacks[slot] = value; }
    public int lastCount(int slot) { ensureCapacity(slot); return lastSlotCounts[slot]; }
    public void lastCount(int slot, int value) { ensureCapacity(slot); lastSlotCounts[slot] = value; }
    public int lastDamage(int slot) { ensureCapacity(slot); return lastSlotDamage[slot]; }
    public void lastDamage(int slot, int value) { ensureCapacity(slot); lastSlotDamage[slot] = value; }
    public boolean isShrinking(int slot) { ensureCapacity(slot); return shrinking[slot]; }
    public void shrinking(int slot, boolean value) { ensureCapacity(slot); shrinking[slot] = value; }
    public float shrinkProgress(int slot) { ensureCapacity(slot); return shrinkProgress[slot]; }
    public void shrinkProgress(int slot, float value) { ensureCapacity(slot); shrinkProgress[slot] = value; }
    public boolean wasUsed(int slot) { ensureCapacity(slot); return used[slot]; }
    public void used(int slot, boolean value) { ensureCapacity(slot); used[slot] = value; }
    public boolean suppressNextPickup(int slot) { ensureCapacity(slot); return suppressNextPickup[slot]; }
    public void suppressNextPickup(int slot, boolean value) { ensureCapacity(slot); suppressNextPickup[slot] = value; }
    public void previousStack(int slot, ItemStack value) { ensureCapacity(slot); previousStacks[slot] = value; }
}
