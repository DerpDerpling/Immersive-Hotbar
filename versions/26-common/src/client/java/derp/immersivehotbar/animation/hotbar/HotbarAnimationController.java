package derp.immersivehotbar.animation.hotbar;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.ItemChecker.isTool;
import static derp.immersivehotbar.util.ItemChecker.isWeapon;


public final class HotbarAnimationController {
    private static final HotbarAnimationController INSTANCE = new HotbarAnimationController();

    private final HotbarAnimationState state = new HotbarAnimationState();

    private int currentRenderedSlot;
    private long lastRenderTime = System.nanoTime();
    private float deltaSeconds;
    private boolean initialized;

    private ItemStack lastOffhandStack = ItemStack.EMPTY;
    private int lastOffhandCount;
    private int lastOffhandDamage;
    private boolean suppressOffhandPickup;

    private HotbarAnimationController() {}

    public static HotbarAnimationController getInstance() {
        return INSTANCE;
    }

    public void beginHotbarRender() {
        currentRenderedSlot = 0;

        long now = System.nanoTime();
        deltaSeconds = Mth.clamp((now - lastRenderTime) / 1_000_000_000.0f, 0f, 0.05f);
        lastRenderTime = now;

        if (!initialized) {
            state.initialize(nonSelectedItemSize);
            initialized = true;
        }

        if (!hotbarItemAnimationsEnabled) {
            for (int i = 0; i < 9; i++) state.resetSlot(i, nonSelectedItemSize);
        }
    }

    public void prepareSlot(GuiGraphicsExtractor context, int x, int y, Player player, ItemStack stack) {
        int slot = currentRenderedSlot++;
        state.ensureCapacity(slot);

        if (!hotbarItemAnimationsEnabled) {
            trackWithoutAnimation(stack, slot);
            return;
        }

        boolean selected = slot == player.getInventory().getSelectedSlot();

        if (shrinkOutOnEmptyEnabled && shouldTriggerShrink(stack, slot)) startShrink(slot);

        if (!stack.isEmpty() && state.isShrinking(slot)) {
            state.shrinking(slot, false);
            state.shrinkProgress(slot, 0f);
            state.lastStack(slot, ItemStack.EMPTY);
            state.lastCount(slot, 0);
            if (state.scale(slot) < nonSelectedItemSize) state.scale(slot, nonSelectedItemSize);
        }

        if (!stack.isEmpty()) {
            handleItemChanges(stack, slot);
            updateLastSlotData(stack, slot);
        } else if (!shrinkOutOnEmptyEnabled) {
            if (!state.isShrinking(slot)) {
                state.previousStack(slot, ItemStack.EMPTY);
                state.lastStack(slot, ItemStack.EMPTY);
                state.lastCount(slot, 0);
            }
            state.scale(slot, nonSelectedItemSize);
            state.velocity(slot, 0f);
        } else {
            handleEmptySlot(slot);
        }

        if (selectorScaleEnabled) updateSelectorScale(slot, selected);
        else state.selectorScale(slot, 1.0f);

        updateSlotScale(stack, slot, selected);

        if (!stack.isEmpty()) {
            HotbarAnimationRenderer.drawSlotEffects(context, stack, x, y, selected);
        }

        if (offhandAnimationsEnabled) updateOffhand();
    }

    public void renderShrinkingItems(GuiGraphicsExtractor context) {
        HotbarAnimationRenderer.renderShrinkingItems(Minecraft.getInstance(), context, state, deltaSeconds);
    }

    public int currentSlot() {
        return Math.max(0, currentRenderedSlot - 1);
    }

    public float currentScale() {
        return state.scale(currentSlot());
    }

    public void triggerUse(int slot) {
        if (!hotbarItemAnimationsEnabled || !useAnimationsEnabled) return;
        state.ensureCapacity(slot);
        state.scale(slot, nonSelectedItemSize - 0.1f);
    }

    public void triggerShrink(int slot) {
        if (slot < 0) return;
        state.ensureCapacity(slot);
        state.used(slot, true);
        float amount = slot == HotbarSlots.offhand() ? 0.2f : (shouldItemGrowWhenSelected ? 0.03f : 0.2f);
        state.scale(slot, nonSelectedItemSize - amount);
    }

    public void triggerWeaponAttack(int slot) {
        if (!weaponAnimates) return;
        state.ensureCapacity(slot);
        state.used(slot, true);
        state.scale(slot, nonSelectedItemSize - 0.07f);
    }

    public void clearTrackedStacks() {
        state.clearTrackedStacks();
    }

    private void triggerOffhandPickupOrDamage() {
        if (!hotbarItemAnimationsEnabled || !offhandAnimationsEnabled) return;
        int slot = HotbarSlots.offhand();
        state.ensureCapacity(slot);
        state.scale(slot, animationIntensity);
    }

    private void trackWithoutAnimation(ItemStack stack, int slot) {
        if (!stack.isEmpty()) {
            state.lastStack(slot, stack.copy());
            state.lastCount(slot, stack.getCount());
            state.lastDamage(slot, stack.isDamageableItem() ? stack.getDamageValue() : 0);
        } else {
            state.lastStack(slot, ItemStack.EMPTY);
            state.lastCount(slot, 0);
            state.lastDamage(slot, 0);
        }
        state.previousStack(slot, stack);
        state.shrinking(slot, false);
        state.shrinkProgress(slot, 0f);
        state.velocity(slot, 0f);
        state.scale(slot, nonSelectedItemSize);
        state.selectorScale(slot, 1f);
        state.suppressNextPickup(slot, false);
        state.used(slot, false);
    }

    private boolean shouldTriggerShrink(ItemStack stack, int slot) {
        return (stack == null || stack.isEmpty()) && !state.lastStack(slot).isEmpty() && !state.isShrinking(slot);
    }

    private void startShrink(int slot) {
        state.shrinking(slot, true);
        state.shrinkProgress(slot, 0f);
        state.scale(slot, 1.3f);
    }

    private void handleEmptySlot(int slot) {
        if (!state.isShrinking(slot)) {
            state.previousStack(slot, ItemStack.EMPTY);
            state.lastStack(slot, ItemStack.EMPTY);
            state.lastCount(slot, 0);
        }
        state.scale(slot, state.scale(slot) + (nonSelectedItemSize - state.scale(slot)) * animationSpeed * deltaSeconds * 60f);
    }

    private void updateOffhand() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        ItemStack stack = minecraft.player.getOffhandItem();
        boolean tool = isTool(stack);
        boolean weapon = isWeapon(stack);
        boolean shouldAnimate = (!tool || toolAnimates) && (!weapon || weaponAnimates);

        boolean changed = !ItemStack.isSameItem(stack, lastOffhandStack) || !ItemStack.isSameItemSameComponents(stack, lastOffhandStack) || stack.getCount() != lastOffhandCount;

        if (useAnimationsEnabled && durabilityAnimates && stack.isDamageableItem()) {
            int damage = stack.getDamageValue();
            if (damage > lastOffhandDamage && shouldAnimate) {
                triggerOffhandPickupOrDamage();
                suppressOffhandPickup = true;
            }
            lastOffhandDamage = damage;
        }

        if (pickupAnimationsEnabled && changed && !suppressOffhandPickup) triggerOffhandPickupOrDamage();

        lastOffhandStack = stack.copy();
        lastOffhandCount = stack.getCount();
        suppressOffhandPickup = false;
    }

    private void handleItemChanges(ItemStack stack, int slot) {
        boolean tool = isTool(stack);
        boolean weapon = isWeapon(stack);
        boolean damageable = stack.isDamageableItem();
        boolean shouldAnimateType = (!tool || toolAnimates) && (!weapon || weaponAnimates);

        boolean wasEmpty = state.lastStack(slot).isEmpty();
        boolean itemChanged = !ItemStack.isSameItem(stack, state.lastStack(slot));
        boolean countIncreased = stack.getCount() > state.lastCount(slot);

        if (pickupAnimationsEnabled && !stack.isEmpty() && (wasEmpty || ((itemChanged || countIncreased) && !state.suppressNextPickup(slot)))) {
            state.scale(slot, animationIntensity);
        }

        if (useAnimationsEnabled && shouldAnimateType) {
            if (durabilityAnimates && damageable) {
                int damage = stack.getDamageValue();
                if (damage > state.lastDamage(slot) && !state.wasUsed(slot)) {
                    state.scale(slot, nonSelectedItemSize - 0.1f);
                    state.suppressNextPickup(slot, true);
                }
                state.lastDamage(slot, damage);
            }

            if (!damageable && stack.getCount() < state.lastCount(slot) && !state.wasUsed(slot)) {
                state.scale(slot, nonSelectedItemSize - 0.1f);
                state.suppressNextPickup(slot, true);
            }
        }

        state.previousStack(slot, stack);
        state.used(slot, false);
    }

    private void updateLastSlotData(ItemStack stack, int slot) {
        if (!state.isShrinking(slot)) {
            state.lastStack(slot, stack.copy());
            state.lastCount(slot, stack.getCount());
        }
        state.suppressNextPickup(slot, false);
    }

    private void updateSelectorScale(int slot, boolean selected) {
        float current = state.selectorScale(slot);
        float target = selected ? 1.2f : 1.0f;
        state.selectorScale(slot, current + (target - current) * 0.3f * deltaSeconds * 60f);
    }

    private void updateSlotScale(ItemStack stack, int slot, boolean selected) {
        float target = selected && shouldItemGrowWhenSelected ? selectedItemSize : nonSelectedItemSize;
        boolean bounce = bouncyAnimation
                && !(isTool(stack) && toolsIgnoreBounce)
                && !(isWeapon(stack) && weaponsIgnoreBounce);

        HotbarAnimationEngine.animateScale(state, slot, target, deltaSeconds, bounce, bouncyStiffness, bouncyDamping, animationSpeed);
    }
}
