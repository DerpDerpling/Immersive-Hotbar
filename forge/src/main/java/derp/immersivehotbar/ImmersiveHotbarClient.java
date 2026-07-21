package derp.immersivehotbar;

import derp.immersivehotbar.util.TooltipAnimationState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.ModList;

import java.util.Arrays;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.SlotAnimationState.*;

public final class ImmersiveHotbarClient {
    private static boolean wasUsingItem;
    private static ItemStack lastUsedItem = ItemStack.EMPTY;
    private static boolean wasCrossbowChargedMainhand;
    private static boolean wasCrossbowChargedOffhand;
    private static BlockPos lastBreakingPos;

    public static final boolean IS_DOUBLEHOTBAR_LOADED = ModList.get().isLoaded("double_hotbar");

    private ImmersiveHotbarClient() {}

    public static void tick(Minecraft client) {
        if (client.player == null) return;

        boolean isUsing = client.player.isUsingItem();
        if (isUsing) {
            lastUsedItem = client.player.getUseItem();
        } else if (wasUsingItem && !lastUsedItem.isEmpty()) {
            Item item = lastUsedItem.getItem();
            if (weaponAnimates && (item instanceof BowItem || item instanceof CrossbowItem)) {
                int slot = client.player.getMainHandItem() == lastUsedItem
                        ? client.player.getInventory().selected : 9;
                triggerShrink(slot);
            }
            lastUsedItem = ItemStack.EMPTY;
        }
        wasUsingItem = isUsing;

        ItemStack mainHandStack = client.player.getMainHandItem();
        if (mainHandStack.getItem() instanceof CrossbowItem) {
            boolean charged = CrossbowItem.isCharged(mainHandStack);
            if (wasCrossbowChargedMainhand && !charged && weaponAnimates) {
                triggerShrink(client.player.getInventory().selected);
            }
            wasCrossbowChargedMainhand = charged;
        } else {
            wasCrossbowChargedMainhand = false;
        }

        ItemStack offHandStack = client.player.getOffhandItem();
        if (offHandStack.getItem() instanceof CrossbowItem) {
            boolean charged = CrossbowItem.isCharged(offHandStack);
            if (wasCrossbowChargedOffhand && !charged && weaponAnimates) triggerShrink(9);
            wasCrossbowChargedOffhand = charged;
        } else {
            wasCrossbowChargedOffhand = false;
        }

        if (client.level != null) {
            HitResult hit = client.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK && client.options.keyAttack.isDown()) {
                lastBreakingPos = ((BlockHitResult) hit).getBlockPos();
            }
            if (lastBreakingPos != null && client.level.getBlockState(lastBreakingPos).isAir()) {
                if (client.player.getMainHandItem().getItem() instanceof TieredItem && toolAnimates) {
                    triggerShrink(client.player.getInventory().selected);
                }
                lastBreakingPos = null;
            }
        }
    }

    public static void resetJoin() {
        TooltipAnimationState.reset();
    }

    public static void resetDisconnect() {
        Arrays.fill(lastSlotStacks, ItemStack.EMPTY);
        TooltipAnimationState.reset();
        lastBreakingPos = null;
    }

    private static void triggerShrink(int slot) {
        if (slot >= 0 && slot < 9) {
            wasUsed[slot] = true;
            slotScales[slot] = nonSelectedItemSize - (shouldItemGrowWhenSelected ? 0.03f : 0.2f);
        } else if (slot == 9) {
            wasUsed[slot] = true;
            slotScales[slot] = nonSelectedItemSize - 0.2f;
        }
    }
}
