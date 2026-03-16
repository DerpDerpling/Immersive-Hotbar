package derp.immersivehotbar;

import derp.immersivehotbar.config.ImmersiveHotbarConfigHandler;
import derp.immersivehotbar.util.TooltipAnimationState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
				lastUsedItem = client.player.getUseItem();
			} else if (wasUsingItem && !lastUsedItem.isEmpty()) {
				Item item = lastUsedItem.getItem();

				if (weaponAnimates && (item instanceof BowItem || item instanceof CrossbowItem)) {
					int slot = client.player.getMainHandItem() == lastUsedItem ? client.player.getInventory().selected : 9;
					triggerShrink(slot);
				}
				lastUsedItem = ItemStack.EMPTY;
			}

			wasUsingItem = isUsing;

			// crossbow mainhand
			ItemStack mainHandStack = client.player.getMainHandItem();
			if (mainHandStack.getItem() instanceof CrossbowItem) {
				boolean isCharged = CrossbowItem.isCharged(mainHandStack);
				if (wasCrossbowChargedMainhand && !isCharged && weaponAnimates) {
					int slot = client.player.getInventory().selected;
					triggerShrink(slot);
				}
				wasCrossbowChargedMainhand = isCharged;
			} else {
				wasCrossbowChargedMainhand = false;
			}

			// crossbow offhand
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
		});
		// track block breaking to trigger tool animations
		AtomicReference<BlockPos> lastBreakingPos = new AtomicReference<>();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level == null || client.player == null) return;


			HitResult r = client.hitResult;
			if (r != null && r.getType() == HitResult.Type.BLOCK && client.options.keyAttack.isDown()) {
				BlockPos targetPos = ((BlockHitResult) r).getBlockPos();
				lastBreakingPos.set(targetPos);
			}


			BlockPos pending = lastBreakingPos.get();
			if (pending != null) {

				if (client.level.getBlockState(pending).isAir()) {
					ItemStack stack = client.player.getMainHandItem();
					if (stack.getItem() instanceof TieredItem && toolAnimates) {
						int slot = client.player.getInventory().selected;
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
