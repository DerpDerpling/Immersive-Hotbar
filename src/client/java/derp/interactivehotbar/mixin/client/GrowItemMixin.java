package derp.interactivehotbar.mixin.client;

import derp.interactivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.interactivehotbar.config.InteractiveHotbarConfig.*;

@Mixin(InGameHud.class)
public abstract class GrowItemMixin implements InGameHudAnimationHandler {

	@Mutable
	@Final
	@Shadow
	private final MinecraftClient client;

	@Unique
	private final float[] slotScales = new float[9];

	@Unique
	private final ItemStack[] lastSlotStacks = new ItemStack[] {
			ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
			ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
			ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
	};

	@Unique private final int[] lastSlotCounts = new int[9];
	@Unique
	private final boolean[] isShrinking = new boolean[9];
	@Unique private final float[] shrinkProgress = new float[9]; // 0.0f → 1.0f




	public GrowItemMixin(MinecraftClient client) {
		this.client = client;
	}
	@Unique
	private boolean handleShrinkAnimation(DrawContext context, int x, int y, int seed, PlayerEntity player, int slotIndex, CallbackInfo ci) {
		int centerX = x + 8;
		int centerY = y + 8;

		shrinkProgress[slotIndex] += 0.1f;
		if (shrinkProgress[slotIndex] > 1.0f) shrinkProgress[slotIndex] = nonSelectedItemSize;

		float eased = 1.0f - (shrinkProgress[slotIndex] * shrinkProgress[slotIndex]); // ease out
		slotScales[slotIndex] = nonSelectedItemSize * eased;

		context.getMatrices().push();
		context.getMatrices().translate(centerX, centerY, 0);
		context.getMatrices().scale(slotScales[slotIndex], slotScales[slotIndex], slotScales[slotIndex]);
		context.getMatrices().translate(-centerX, -centerY, 0);
		context.drawItem(player, lastSlotStacks[slotIndex], x, y, seed);
		context.drawItemInSlot(this.client.textRenderer, lastSlotStacks[slotIndex], x, y);
		context.getMatrices().pop();

		if (shrinkProgress[slotIndex] >= 1.0f) {
			isShrinking[slotIndex] = false;
			lastSlotStacks[slotIndex] = ItemStack.EMPTY;
			lastSlotCounts[slotIndex] = 0;
			slotScales[slotIndex] = nonSelectedItemSize;
		}

		ci.cancel();
		return true;
	}


	@Unique
	private boolean isTool(ItemStack stack) {
		return stack.getItem() instanceof ShovelItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof PickaxeItem || stack.getItem() instanceof HoeItem || stack.getItem() instanceof ShearsItem;
	}
	@Unique
	private boolean isWeapon(ItemStack stack) {
		return stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof TridentItem || stack.getItem() instanceof MaceItem;
	}

	@Unique
	@Override
	public void interactive_hotbar$triggerSlotAnimation(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < slotScales.length) {
			slotScales[slotIndex] = nonSelectedItemSize;
		}
	}
	@Unique
	private int currentHotbarSlot = 0;

	@Inject(method = "renderHotbar", at = @At("HEAD"))
	private void interactivehotbar$resetSlotCounter(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		currentHotbarSlot = 0;
	}


	@Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
	private void renderHotbarItemHook(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		int slotIndex = currentHotbarSlot;
		if (slotIndex >= 9) return;
		currentHotbarSlot++;

		int centerX = x + 8;
		int centerY = y + 8;
		boolean isSelected = (slotIndex == player.getInventory().selectedSlot);
		if ((stack == null || stack.isEmpty()) && !lastSlotStacks[slotIndex].isEmpty() && !isShrinking[slotIndex]) {
			isShrinking[slotIndex] = true;
			shrinkProgress[slotIndex] = 0f;
			stack = lastSlotStacks[slotIndex];
			slotScales[slotIndex] = 1.3f;
		}
		if (isShrinking[slotIndex]) {
			if (handleShrinkAnimation(context, x, y, seed, player, slotIndex, ci)) return;
		}
		if (stack == null || stack.isEmpty()) {
			lastSlotStacks[slotIndex] = ItemStack.EMPTY;
			lastSlotCounts[slotIndex] = 0;
			slotScales[slotIndex] += (nonSelectedItemSize - slotScales[slotIndex]) * animationSpeed;
			ci.cancel();
			return;
		}

		boolean isTool = isTool(stack);
		boolean isWeapon = isWeapon(stack);
		boolean shouldAnimate = (!isTool && !isWeapon)
				|| (isTool && toolAnimates == ToolAnimates.ENABLED)
				|| (isWeapon && weaponAnimates == WeaponAnimates.ENABLED);


		if (shouldAnimate && (lastSlotStacks[slotIndex] == null || !ItemStack.areEqual(stack, lastSlotStacks[slotIndex]))) {
			if (shouldItemGrowWhenSelected == ShouldItemGrowWhenSelected.ENABLED) {
				slotScales[slotIndex] = (isTool || isWeapon) ? nonSelectedItemSize : animationIntensity;
			} else {
				slotScales[slotIndex] = animationIntensity;
			}
			lastSlotStacks[slotIndex] = stack.copy();
		}



		if (stack.getCount() < lastSlotCounts[slotIndex]) {
			slotScales[slotIndex] = nonSelectedItemSize - 0.1f;
		}
		lastSlotCounts[slotIndex] = stack.getCount();

		float targetScale = (isSelected && shouldItemGrowWhenSelected == ShouldItemGrowWhenSelected.ENABLED) ? selectedItemSize : nonSelectedItemSize;

		slotScales[slotIndex] += (targetScale - slotScales[slotIndex]) * animationSpeed;

		// Draw normal item
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

