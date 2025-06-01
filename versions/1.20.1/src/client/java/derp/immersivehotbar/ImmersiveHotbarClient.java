package derp.immersivehotbar;

import derp.immersivehotbar.config.ImmersiveHotbarConfigHandler;
import derp.immersivehotbar.util.TooltipAnimationState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

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
		// track block breaking to trigger tool animations
		AtomicReference<BlockPos> lastBreakingPos = new AtomicReference<>();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) return;


			HitResult r = client.crosshairTarget;
			if (r != null && r.getType() == HitResult.Type.BLOCK && client.options.attackKey.isPressed()) {
				BlockPos targetPos = ((BlockHitResult) r).getBlockPos();
				lastBreakingPos.set(targetPos);
			}


			BlockPos pending = lastBreakingPos.get();
			if (pending != null) {

				if (client.world.getBlockState(pending).isAir()) {
					ItemStack stack = client.player.getMainHandStack();
					if (stack.getItem() instanceof ToolItem && toolAnimates) {
						int slot = client.player.getInventory().selectedSlot;
						triggerShrink(slot);
					}

					lastBreakingPos.set(null);
				}
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
