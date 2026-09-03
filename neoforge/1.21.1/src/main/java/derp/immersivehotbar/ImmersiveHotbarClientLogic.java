package derp.immersivehotbar;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.hotbar.HotbarSlots;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.weaponAnimates;

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
                        ? client.player.getInventory().selected
                        : HotbarSlots.offhand();

                triggerShrink(slot);
            }

            lastUsedItem = ItemStack.EMPTY;
        }

        wasUsingItem = isUsing;

        ItemStack mainHandStack = client.player.getMainHandItem();

        if (mainHandStack.getItem() instanceof CrossbowItem) {
            boolean isCharged = CrossbowItem.isCharged(mainHandStack);

            if (wasCrossbowChargedMainhand && !isCharged && weaponAnimates) {
                triggerShrink(client.player.getInventory().selected);
            }

            wasCrossbowChargedMainhand = isCharged;
        } else {
            wasCrossbowChargedMainhand = false;
        }

        ItemStack offHandStack = client.player.getOffhandItem();

        if (offHandStack.getItem() instanceof CrossbowItem) {
            boolean isCharged = CrossbowItem.isCharged(offHandStack);

            if (wasCrossbowChargedOffhand && !isCharged && weaponAnimates) {
                triggerShrink(HotbarSlots.offhand());
            }

            wasCrossbowChargedOffhand = isCharged;
        } else {
            wasCrossbowChargedOffhand = false;
        }
    }

    private static void triggerShrink(int slot) {
        HotbarAnimationController.getInstance().triggerShrink(slot);
    }
}