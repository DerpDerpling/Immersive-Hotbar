package derp.animatedhotbar.mixin.client;

import derp.animatedhotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(InGameHud.class)
public abstract class GrowItemMixin implements InGameHudAnimationHandler {

	@Mutable
	@Final
	@Shadow
	private final MinecraftClient client;

	@Unique
	private final float[] slotScales = new float[9];
	@Unique
	private final ItemStack[] lastSlotStacks = new ItemStack[9];
	@Unique
	private final int[] lastSlotCounts = new int[9];


	public GrowItemMixin(MinecraftClient client) {
		this.client = client;
		Arrays.fill(slotScales, 1.0F);

		Arrays.fill(lastSlotStacks, ItemStack.EMPTY);
		Arrays.fill(lastSlotCounts, 0);
	}

	@Unique
	private boolean getToolType(ItemStack stack) {
		String name = stack.getItem().toString().toLowerCase();

		return name.contains("shovel") || name.contains("axe") || name.contains("pickaxe") || name.contains("hoe");
	}

	@Unique
	@Override
	public void animated_hotbar$triggerSlotAnimation(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < slotScales.length) {
			slotScales[slotIndex] = 0.8F;
		}
	}
	@Unique
	private int currentHotbarSlot = 0;

	@Inject(method = "renderHotbar", at = @At("HEAD"))
	private void animatedhotbar$resetSlotCounter(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		currentHotbarSlot = 0;
	}


	@Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
	private void renderHotbarItemHook(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		int slotIndex = currentHotbarSlot;
		if (slotIndex >= 9) {
			return;
		}
		currentHotbarSlot++;

		if (stack == null || stack.isEmpty()) {
			lastSlotStacks[slotIndex] = ItemStack.EMPTY;
			lastSlotCounts[slotIndex] = 0;
			slotScales[slotIndex] += (1.0F - slotScales[slotIndex]) * 0.1F;
			return;
		}

		int centerX = x + 8;
		int centerY = y + 8;
		boolean isSelected = (slotIndex == player.getInventory().selectedSlot);

		float targetScale = isSelected ? 1.2F : 1.0F;
		float lerpFactor = 0.1F;
		boolean isTool = getToolType(stack);

		if (lastSlotStacks[slotIndex] == null || lastSlotStacks[slotIndex].isEmpty() || lastSlotStacks[slotIndex].getItem() != stack.getItem() || stack.getCount() > lastSlotCounts[slotIndex]) {
			slotScales[slotIndex] = 0.8F;
			lastSlotStacks[slotIndex] = stack.copy();
		}

		if (stack.getCount() < lastSlotCounts[slotIndex]) {
			slotScales[slotIndex] = 1.0F;
		}
		lastSlotCounts[slotIndex] = stack.getCount();

		if (isTool) {
			if (isSelected) {
				if (slotScales[slotIndex] < 1.2F) {
					slotScales[slotIndex] += (1.21F - slotScales[slotIndex]) * lerpFactor;
					if (Math.abs(slotScales[slotIndex] - 1.2F) < 0.01F) {
						slotScales[slotIndex] = 1.2F;
					}
				}
			} else {
				slotScales[slotIndex] += (1.0F - slotScales[slotIndex]) * lerpFactor;
			}
		} else {
			slotScales[slotIndex] += (targetScale - slotScales[slotIndex]) * lerpFactor;
		}

		context.getMatrices().push();
		context.getMatrices().translate(centerX, centerY, 0);
		context.getMatrices().scale(slotScales[slotIndex], slotScales[slotIndex], slotScales[slotIndex]);
		context.getMatrices().translate(-centerX, -centerY, 0);
		context.drawItem(player, stack, x, y, seed);
		context.drawItemInSlot(this.client.textRenderer, stack, x, y);
		context.getMatrices().pop();

		ci.cancel();
	}

}

