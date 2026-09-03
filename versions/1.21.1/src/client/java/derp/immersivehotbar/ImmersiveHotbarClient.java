package derp.immersivehotbar;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.hotbar.HotbarSlots;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import derp.immersivehotbar.config.ImmersiveHotbarConfigHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.toolAnimates;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.weaponAnimates;

public class ImmersiveHotbarClient implements ClientModInitializer {
    private boolean wasUsingItem = false;
    private ItemStack lastUsedItem = ItemStack.EMPTY;
    private boolean wasCrossbowChargedMainhand = false;
    private boolean wasCrossbowChargedOffhand = false;

    public static final boolean IS_DOUBLEHOTBAR_LOADED = FabricLoader.getInstance().isModLoaded("double_hotbar");

    @Override
    public void onInitializeClient() {
        ImmersiveHotbarConfigHandler.load();

        ClientPlayConnectionEvents.DISCONNECT.register((client, world) -> HotbarAnimationController.getInstance().clearTrackedStacks());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> TooltipAnimationController.getInstance().reset());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            boolean isUsing = client.player.isUsingItem();

            if (isUsing) {
                lastUsedItem = client.player.getUseItem();
            } else if (wasUsingItem && !lastUsedItem.isEmpty()) {
                Item item = lastUsedItem.getItem();

                if (weaponAnimates && (item instanceof BowItem || item instanceof CrossbowItem)) {
                    int slot = client.player.getMainHandItem() == lastUsedItem ? client.player.getInventory().selected : HotbarSlots.offhand();

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
        });

        ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof TieredItem && toolAnimates) {
                triggerShrink(player.getInventory().selected);
            }
        });
    }

    private void triggerShrink(int slot) {
        HotbarAnimationController.getInstance().triggerShrink(slot);
    }
}
