package derp.immersivehotbar;

import derp.immersivehotbar.config.ImmersiveHotbarConfigHandler;
import derp.immersivehotbar.util.TooltipAnimationState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;

import java.util.Arrays;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.SlotAnimationState.*;

public class ImmersiveHotbarClient implements ClientModInitializer {
	private boolean wasUsingItem = false;
	private ItemStack lastUsedItem = ItemStack.EMPTY;
	private boolean wasCrossbowChargedMainhand = false;
	private boolean wasCrossbowChargedOffhand = false;
	public static final boolean IS_DOUBLEHOTBAR_LOADED = FabricLoader.getInstance().isModLoaded("double_hotbar");

	@Override
	public void onInitializeClient() {
		ImmersiveHotbarConfigHandler.load();

		ClientPlayConnectionEvents.DISCONNECT.register((client, world) -> Arrays.fill(lastSlotStacks, ItemStack.EMPTY));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> TooltipAnimationState.reset());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			boolean isUsing = client.player.isUsingItem();

			// track bow usage
			if (isUsing) {
				lastUsedItem = client.player.getActiveItem();
			} else if (wasUsingItem && !lastUsedItem.isEmpty()) {
				Item item = lastUsedItem.getItem();

				if (weaponAnimates && (item instanceof BowItem || item instanceof CrossbowItem)) {
					int slot = client.player.getMainHandStack() == lastUsedItem ? client.player.getInventory().selectedSlot : 9;
					triggerShrink(slot);
				}
				lastUsedItem = ItemStack.EMPTY;
			}

			wasUsingItem = isUsing;

			// crossbow mainhand
			ItemStack mainHandStack = client.player.getMainHandStack();
			if (mainHandStack.getItem() instanceof CrossbowItem) {
				boolean isCharged = CrossbowItem.isCharged(mainHandStack);
				if (wasCrossbowChargedMainhand && !isCharged && weaponAnimates) {
					int slot = client.player.getInventory().selectedSlot;
					triggerShrink(slot);
				}
				wasCrossbowChargedMainhand = isCharged;
			} else {
				wasCrossbowChargedMainhand = false;
			}

			// crossbow offhand
			ItemStack offHandStack = client.player.getOffHandStack();
			if (offHandStack.getItem() instanceof CrossbowItem) {
				boolean isCharged = CrossbowItem.isCharged(offHandStack);
				if (wasCrossbowChargedOffhand && !isCharged && weaponAnimates) {
					triggerShrink(9);
				}
				wasCrossbowChargedOffhand = isCharged;
			} else {
				wasCrossbowChargedOffhand = false;
			}
		});

		// tool break animation
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			ItemStack stack = player.getMainHandStack();
			if (stack.getItem() instanceof ToolItem && toolAnimates) {
				int slot = player.getInventory().selectedSlot;
				triggerShrink(slot);
			}
		});
	}

	private void triggerShrink(int slot) {
		if (slot >= 0 && slot < 9) {
			wasUsed[slot] = true;
			slotScales[slot] = nonSelectedItemSize - (shouldItemGrowWhenSelected ? 0.03f : 0.2f);
		} else if (slot == 9) {
			wasUsed[slot] = true;
			slotScales[slot] = nonSelectedItemSize - 0.2f;
		}
	}
}
