package derp.immersivehotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;

import java.util.Arrays;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.ItemChecker.isTool;
import static derp.immersivehotbar.util.SlotAnimationState.*;
import derp.immersivehotbar.util.TooltipAnimationState;

public class ImmersiveHotbarClientLogic {

    private static boolean wasUsingItem = false;
    private static ItemStack lastUsedItem = ItemStack.EMPTY;
    private static boolean wasCrossbowChargedMainhand = false;
    private static boolean wasCrossbowChargedOffhand = false;

    public static final boolean IS_DOUBLEHOTBAR_LOADED = ModList.get().isLoaded("double_hotbar");

    public static void onClientTick(Minecraft client) {
        if (client.player == null) return;

        boolean isUsing = client.player.isUsingItem();

        if (isUsing) {
            lastUsedItem = client.player.getUseItem();
        } else if (wasUsingItem && !lastUsedItem.isEmpty()) {

            Item item = lastUsedItem.getItem();

            if (weaponAnimates && (item instanceof BowItem || item instanceof CrossbowItem)) {

                int slot = client.player.getMainHandItem() == lastUsedItem
                        ? client.player.getInventory().getSelectedSlot()
                        : 9;

                triggerShrink(slot);
            }

            lastUsedItem = ItemStack.EMPTY;
        }

        wasUsingItem = isUsing;

        ItemStack mainHandStack = client.player.getMainHandItem();

        if (mainHandStack.getItem() instanceof CrossbowItem) {

            boolean isCharged = CrossbowItem.isCharged(mainHandStack);

            if (wasCrossbowChargedMainhand && !isCharged && weaponAnimates) {

                int slot = client.player.getInventory().getSelectedSlot();

                triggerShrink(slot);
            }

            wasCrossbowChargedMainhand = isCharged;

        } else {
            wasCrossbowChargedMainhand = false;
        }

        ItemStack offHandStack = client.player.getOffhandItem();

        if (offHandStack.getItem() instanceof CrossbowItem) {

            boolean isCharged = CrossbowItem.isCharged(offHandStack);

            if (wasCrossbowChargedOffhand && !isCharged && weaponAnimates) {
                triggerShrink(9);
            }

            wasCrossbowChargedOffhand = isCharged;

        } else {
            wasCrossbowChargedOffhand = false;
        }
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