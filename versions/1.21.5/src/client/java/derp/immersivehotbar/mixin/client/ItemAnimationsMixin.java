package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.ItemChecker.*;
import static derp.immersivehotbar.util.SlotAnimationState.*;

@Mixin(InGameHud.class)
public abstract class ItemAnimationsMixin implements InGameHudAnimationHandler {

	@Mutable @Final @Shadow
	private final MinecraftClient client;

	@Unique
	private int currentHotbarSlot = 0;

	@Unique
	private long lastRenderTime = System.nanoTime();
	@Unique
	private float deltaSeconds = 0f;
	@Unique private ItemStack lastOffhandStack = ItemStack.EMPTY;
	@Unique private int lastOffhandCount = 0;
	@Unique private int lastOffhandDamage = 0;
	@Unique private boolean suppressOffhandPickup = false;

	public ItemAnimationsMixin(MinecraftClient client) {
		this.client = client;

	}

	@Override
	public void immersive_hotbar$triggerSlotAnimation(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < slotScales.length) {
			slotScales[slotIndex] = nonSelectedItemSize - 0.1f;
		}
	}

	@Unique
	public void immersive_hotbar$triggerOffhandAnimation(int slotIndex) {
		slotScales[slotIndex] = animationIntensity;
	}

	@Unique
	private boolean initialized = false;

	@Inject(method = "renderHotbar", at = @At("HEAD"))
	private void onRenderHotbarStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		currentHotbarSlot = 0;

		long currentTime = System.nanoTime();
		deltaSeconds = (currentTime - lastRenderTime) / 1_000_000_000.0f;
		deltaSeconds = MathHelper.clamp(deltaSeconds, 0f, 0.05f);
		lastRenderTime = currentTime;

		if (!initialized) {
			Arrays.fill(slotScales, nonSelectedItemSize);
			Arrays.fill(selectorScales, 1.0f);
			Arrays.fill(slotVelocities, 0.0f);
			Arrays.fill(shrinkProgress, 0.0f);
			Arrays.fill(isShrinking, false);
			Arrays.fill(previousStacks, ItemStack.EMPTY);
			initialized = true;
		}
	}


	@Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
	private void onRenderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		handleHotbarSlotRender(context, x, y, player, stack, seed, ci);
	}

	@Unique
	private void handleHotbarSlotRender(DrawContext context, int x, int y, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		int slotIndex = currentHotbarSlot++;
		int centerX = x + 8;
		int centerY = y + 8;
		boolean isSelected = (slotIndex == player.getInventory().getSelectedSlot());

		if (shouldTriggerShrink(stack, slotIndex)) {
			startShrinkAnimation(slotIndex);
		}

		if (isShrinking[slotIndex]) {
			if (handleShrinkAnimation(context, x, y, seed, player, slotIndex, ci)) return;
		}

		if (stack.isEmpty()) {
			handleEmptySlot(slotIndex);
			ci.cancel();
			return;
		}

		handleItemChangeAnimations(stack, slotIndex);
		updateLastSlotData(stack, slotIndex);


		updateSelectorScales(slotIndex);
		applySlotBounceOrSmoothScale(stack, slotIndex, isSelected);
		drawDurabilityGlow(stack, context, x, y);
		drawSlotBackground(context, x, y, isSelected);
		drawItemStack(context, player, stack, x, y, seed, centerX, centerY, slotIndex);
		handleOffhandSlotAnimations();
		ci.cancel();
	}

	@Unique
	private boolean shouldTriggerShrink(ItemStack stack, int slotIndex) {
		return (stack == null || stack.isEmpty()) && !lastSlotStacks[slotIndex].isEmpty() && !isShrinking[slotIndex];
	}

	@Unique
	private void startShrinkAnimation(int slotIndex) {
		isShrinking[slotIndex] = true;
		shrinkProgress[slotIndex] = 0f;
		slotScales[slotIndex] = 1.3f;
	}

	@Unique
	private void handleEmptySlot(int slotIndex) {
		previousStacks[slotIndex] = ItemStack.EMPTY;
		lastSlotStacks[slotIndex] = ItemStack.EMPTY;
		lastSlotCounts[slotIndex] = 0;
		slotScales[slotIndex] += (nonSelectedItemSize - slotScales[slotIndex]) * animationSpeed * deltaSeconds * 60f;
	}

	@Unique
	private void handleOffhandSlotAnimations() {
		assert client.player != null;
		ItemStack offhandStack = client.player.getOffHandStack();

		boolean isToolItem = isTool(offhandStack);
		boolean isWeaponItem = isWeapon(offhandStack);
		boolean shouldAnimate = (!isToolItem || toolAnimates) && (!isWeaponItem || weaponAnimates);

		boolean changed = !ItemStack.areItemsEqual(offhandStack, lastOffhandStack) || !ItemStack.areItemsAndComponentsEqual(offhandStack, lastOffhandStack) || offhandStack.getCount() != lastOffhandCount;

		boolean wasUsed = false;
		if (durabilityAnimates && offhandStack.isDamageable()) {
			int currentDamage = offhandStack.getDamage();
			if (currentDamage > lastOffhandDamage && !wasUsed && shouldAnimate) {
				immersive_hotbar$triggerOffhandAnimation(9);
				suppressOffhandPickup = true;
			}
			lastOffhandDamage = currentDamage;
		}

		if (changed && !suppressOffhandPickup && shouldAnimate) {
			immersive_hotbar$triggerOffhandAnimation(9);
		}

		lastOffhandStack = offhandStack.copy();
		lastOffhandCount = offhandStack.getCount();
		suppressOffhandPickup = false;
	}

	@Unique
	private void handleItemChangeAnimations(ItemStack stack, int slotIndex) {
		boolean isToolItem = isTool(stack);
		boolean isWeaponItem = isWeapon(stack);
		boolean isDamageable = stack.isDamageable();
		boolean shouldAnimate = (!isToolItem || toolAnimates) &&
				(!isWeaponItem || weaponAnimates);

		boolean wasEmpty = lastSlotStacks[slotIndex].isEmpty();
		boolean itemChanged = !ItemStack.areItemsEqual(stack, lastSlotStacks[slotIndex]);
		boolean countIncreased = stack.getCount() > lastSlotCounts[slotIndex];

		if (!stack.isEmpty() && (wasEmpty || ((itemChanged || countIncreased) && !suppressNextPickup[slotIndex]))) {
			slotScales[slotIndex] = animationIntensity;
		}

		if (durabilityAnimates && isDamageable) {
			int currentDamage = stack.getDamage();
			int lastDamage = lastSlotDamage[slotIndex];
			if (currentDamage > lastDamage && !wasUsed[slotIndex] && shouldAnimate) {
				slotScales[slotIndex] = nonSelectedItemSize - 0.1f;
				suppressNextPickup[slotIndex] = true;
			}
			lastSlotDamage[slotIndex] = currentDamage;
		}

		if (!isDamageable && stack.getCount() < lastSlotCounts[slotIndex]) {
			if (!wasUsed[slotIndex]) {
				slotScales[slotIndex] = nonSelectedItemSize - 0.1f;
				suppressNextPickup[slotIndex] = true;
			}
		}

		previousStacks[slotIndex] = stack;
	}

	@Unique
	private void updateLastSlotData(ItemStack stack, int slotIndex) {
		if (!isShrinking[slotIndex]) {
			lastSlotStacks[slotIndex] = stack.copy();
			lastSlotCounts[slotIndex] = stack.getCount();
		}
		suppressNextPickup[slotIndex] = false;
	}

	@Unique
	private void updateSelectorScales(int slotIndex) {
		float current = selectorScales[slotIndex];
		selectorScales[slotIndex] += ((slotIndex == currentHotbarSlot - 1) ? 1.2f : 1.0f - current) * 0.3f * deltaSeconds * 60f;
	}

	@Unique
	private void applySlotBounceOrSmoothScale(ItemStack stack, int slotIndex, boolean isSelected) {
		float targetScale = (isSelected && shouldItemGrowWhenSelected) ? selectedItemSize : nonSelectedItemSize;
		boolean shouldBounce = bouncyAnimation &&
				!(isTool(stack) && toolsIgnoreBounce) &&
				!(isWeapon(stack) && weaponsIgnoreBounce);

		if (shouldBounce) {
			float scale = slotScales[slotIndex];
			float velocity = slotVelocities[slotIndex];

			float force = (targetScale - scale) * bouncyStiffness;
			velocity += force * deltaSeconds * 60f;
			velocity *= (float) Math.pow(1.0f - bouncyDamping, deltaSeconds * 60f);
			scale += velocity * deltaSeconds * 60f;

			slotScales[slotIndex] = scale;
			slotVelocities[slotIndex] = velocity;
		} else {
			// smooth non-bouncy mode
			float factor = 1.0f - (float)Math.pow(1.0f - animationSpeed, deltaSeconds * 60f);
			slotScales[slotIndex] += (targetScale - slotScales[slotIndex]) * factor;
		}
	}




	@Unique
	private boolean handleShrinkAnimation(DrawContext context, int x, int y, int seed, PlayerEntity player, int slotIndex, CallbackInfo ci) {
		int centerX = x + 8;
		int centerY = y + 8;

		shrinkProgress[slotIndex] += deltaSeconds * shrinkAnimationSpeed;
		float progress = shrinkProgress[slotIndex];

		if (bouncyAnimation) {
			float bounce = (float) Math.sin(progress * Math.PI) * 0.15f;
			float eased = 1.0f - (progress * progress);
			slotScales[slotIndex] = nonSelectedItemSize * eased + bounce;
		} else {
			float eased = 1.0f - (progress * progress);
			slotScales[slotIndex] = nonSelectedItemSize * eased;
		}

		context.getMatrices().push();
		context.getMatrices().translate(centerX, centerY, 0);
		context.getMatrices().scale(slotScales[slotIndex], slotScales[slotIndex], slotScales[slotIndex]);
		context.getMatrices().translate(-centerX, -centerY, 0);
		context.drawItem(player, lastSlotStacks[slotIndex], x, y, seed);
		context.drawStackOverlay(this.client.textRenderer, lastSlotStacks[slotIndex], x, y);
		context.getMatrices().pop();

		if (progress >= 1.0f) {
			isShrinking[slotIndex] = false;
			lastSlotStacks[slotIndex] = ItemStack.EMPTY;
			lastSlotCounts[slotIndex] = 0;
			slotScales[slotIndex] = nonSelectedItemSize;
		}

		ci.cancel();
		return true;
	}


	@Unique
	private void drawDurabilityGlow(ItemStack stack, DrawContext context, int x, int y) {
		if (lowDurabilityGlow && stack.isDamageable()) {
			float percent = (float) stack.getDamage() / stack.getMaxDamage();
			if (percent >= durabilityGlowThreshold) {
				float intensity = (percent - durabilityGlowThreshold) / (1.0f - durabilityGlowThreshold);
				intensity = Math.min(intensity, 1.0f);

				float baseAlpha = 0.6f + 0.4f * intensity;
				float maxPulse = 0.3f;

				if (percent >= 0.95f) {
					double pulse = Math.sin(System.currentTimeMillis() / 80.0); // faster pulse
					baseAlpha += maxPulse * (float) pulse;
				}

				float alpha = Math.min(Math.max(baseAlpha, 0.0f), 1.0f);
				int redGlow = ((int)(alpha * 255) << 24) | 0xFF0000;

				context.getMatrices().push();
				context.getMatrices().translate(0, 0, 100);

				context.fill(x - 2, y - 2, x + 18, y, redGlow);
				context.fill(x - 2, y + 16, x + 18, y + 18, redGlow);
				context.fill(x - 2, y, x, y + 16, redGlow);
				context.fill(x + 16, y, x + 18, y + 16, redGlow);

				context.getMatrices().pop();
			}
		}
	}


	@Unique
	private void drawSlotBackground(DrawContext context, int x, int y, boolean isSelected) {
		if (showBackground == shouldShowBackground.ENABLED || (showBackground == shouldShowBackground.ONLY_WHEN_SELECTED && isSelected)) {

			context.getMatrices().push();
			context.getMatrices().translate(0, 0, 50);
			context.fill(x - 2, y - 2, x + 18, y + 18, hotbarSelectionColor.getRGB());
			context.getMatrices().pop();
		}
	}

	@Unique
	private void drawItemStack(DrawContext context, PlayerEntity player, ItemStack stack, int x, int y, int seed, int centerX, int centerY, int slotIndex) {
		context.getMatrices().push();
		context.getMatrices().translate(centerX, centerY, 0);
		context.getMatrices().scale(slotScales[slotIndex], slotScales[slotIndex], slotScales[slotIndex]);
		context.getMatrices().translate(-centerX, -centerY, 0);
		context.drawItem(player, stack, x, y, seed);
		context.getMatrices().pop();

		context.getMatrices().push();
		if (textScales) {
			context.getMatrices().translate(centerX, centerY, 0);
			context.getMatrices().scale(slotScales[slotIndex], slotScales[slotIndex], slotScales[slotIndex]);
			context.getMatrices().translate(-centerX, -centerY, 0);
			context.drawStackOverlay(this.client.textRenderer, stack, x, y);
		} else {
			context.getMatrices().translate(0, 0, 200);
			context.drawStackOverlay(this.client.textRenderer, stack, x, y);
		}
		context.getMatrices().pop();
	}
}