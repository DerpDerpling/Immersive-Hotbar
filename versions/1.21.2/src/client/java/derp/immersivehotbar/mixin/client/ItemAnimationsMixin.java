package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static derp.immersivehotbar.ImmersiveHotbarClient.IS_DOUBLEHOTBAR_LOADED;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.util.ItemChecker.isTool;
import static derp.immersivehotbar.util.ItemChecker.isWeapon;
import static derp.immersivehotbar.util.SlotAnimationState.*;

@Mixin(InGameHud.class)
public abstract class ItemAnimationsMixin implements InGameHudAnimationHandler {

    @Mutable
    @Final
    @Shadow
    private final MinecraftClient client;

    @Unique
    private int currentHotbarSlot = 0;

    @Unique
    private long lastRenderTime = System.nanoTime();
    @Unique
    private float deltaSeconds = 0f;
    @Unique
    private boolean initialized = false;

    @Unique
    private ItemStack lastOffhandStack = ItemStack.EMPTY;
    @Unique
    private int lastOffhandCount = 0;
    @Unique
    private int lastOffhandDamage = 0;
    @Unique
    private boolean suppressOffhandPickup = false;

    public ItemAnimationsMixin(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void immersive_hotbar$triggerSlotAnimation(int slotIndex) {
        if (!hotbarItemAnimationsEnabled) return;
        if (!useAnimationsEnabled) return;

        if (slotIndex >= 0 && slotIndex < slotScales.length) {
            slotScales[slotIndex] = nonSelectedItemSize - 0.1f;
        }
    }

    @Unique
    public void immersive_hotbar$triggerOffhandAnimation(int slotIndex) {
        if (!hotbarItemAnimationsEnabled) return;
        if (!offhandAnimationsEnabled) return;

        slotScales[slotIndex] = animationIntensity;
    }

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

        if (!hotbarItemAnimationsEnabled) {
            for (int i = 0; i < 9; i++) {
                slotScales[i] = nonSelectedItemSize;
                selectorScales[i] = 1.0f;
                slotVelocities[i] = 0.0f;
                shrinkProgress[i] = 0.0f;
                isShrinking[i] = false;
                previousStacks[i] = ItemStack.EMPTY;
                lastSlotStacks[i] = ItemStack.EMPTY;
                lastSlotCounts[i] = 0;
                lastSlotDamage[i] = 0;
                suppressNextPickup[i] = false;
                wasUsed[i] = false;
            }
        }
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    private void onRenderHotbarItemHead(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        int slotIndex = currentHotbarSlot++;
        ensureCapacity(slotIndex);

        if (!hotbarItemAnimationsEnabled) {
            if (!stack.isEmpty()) {
                lastSlotStacks[slotIndex] = stack.copy();
                lastSlotCounts[slotIndex] = stack.getCount();
                lastSlotDamage[slotIndex] = stack.isDamageable() ? stack.getDamage() : 0;
            } else {
                lastSlotStacks[slotIndex] = ItemStack.EMPTY;
                lastSlotCounts[slotIndex] = 0;
                lastSlotDamage[slotIndex] = 0;
            }
            previousStacks[slotIndex] = stack;
            isShrinking[slotIndex] = false;
            shrinkProgress[slotIndex] = 0f;
            slotVelocities[slotIndex] = 0f;
            slotScales[slotIndex] = nonSelectedItemSize;
            selectorScales[slotIndex] = 1.0f;
            suppressNextPickup[slotIndex] = false;
            wasUsed[slotIndex] = false;
            return;
        }

        boolean isSelected = (slotIndex == player.getInventory().selectedSlot);


        if (shrinkOutOnEmptyEnabled && shouldTriggerShrink(stack, slotIndex)) {
            startShrinkAnimation(slotIndex);
        }

        if (!stack.isEmpty() && isShrinking[slotIndex]) {
            isShrinking[slotIndex] = false;
            shrinkProgress[slotIndex] = 0f;
            lastSlotStacks[slotIndex] = ItemStack.EMPTY;
            lastSlotCounts[slotIndex] = 0;
            if (slotScales[slotIndex] < nonSelectedItemSize) {
                slotScales[slotIndex] = nonSelectedItemSize;
            }
        }

        if (!stack.isEmpty()) {
            handleItemChangeAnimations(stack, slotIndex);
            updateLastSlotData(stack, slotIndex);
        } else {
            if (!shrinkOutOnEmptyEnabled) {
                if (!isShrinking[slotIndex]) {
                    previousStacks[slotIndex] = ItemStack.EMPTY;
                    lastSlotStacks[slotIndex] = ItemStack.EMPTY;
                    lastSlotCounts[slotIndex] = 0;
                }
                slotScales[slotIndex] = nonSelectedItemSize;
                slotVelocities[slotIndex] = 0.0f;
            } else {
                handleEmptySlot(slotIndex);
            }
        }

        if (selectorScaleEnabled) {
            updateSelectorScales(slotIndex, isSelected);
        } else {
            selectorScales[slotIndex] = 1.0f;
        }

        applySlotBounceOrSmoothScale(stack, slotIndex, isSelected);
        if (!stack.isEmpty()) {
            drawSlotBackground(context, x, y, isSelected);
            drawDurabilityGlow(context, stack, x, y);
        }

        if (offhandAnimationsEnabled) {
            handleOffhandSlotAnimations();
        }
    }

    @WrapOperation(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getBobbingAnimationTime()I"))
    private int disableBobbing(ItemStack stack, Operation<Integer> original) {
        if (!hotbarItemAnimationsEnabled) return original.call(stack);
        return !vanillaItemBobbing ? 0 : original.call(stack);
    }

    @WrapOperation(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;III)V"))
    private void wrapDrawItem(DrawContext ctx, LivingEntity entity, ItemStack stack, int x, int y, int seed, Operation<Void> original) {
        if (!hotbarItemAnimationsEnabled) {
            original.call(ctx, entity, stack, x, y, seed);
            return;
        }

        int slotIndex = currentHotbarSlot - 1;
        ensureCapacity(slotIndex);

        float scale = slotScales[slotIndex];
        int cx = x + 8;
        int cy = y + 8;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().scale(scale, scale, 1);
        ctx.getMatrices().translate(-cx, -cy, 0);
        original.call(ctx, entity, stack, x, y, seed);
        ctx.getMatrices().pop();
    }

    @WrapOperation(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"))
    private void wrapDrawCount(DrawContext instance, TextRenderer textRenderer, ItemStack itemStack, int x, int y, Operation<Void> original) {
        int slotIndex = currentHotbarSlot - 1;
        ensureCapacity(slotIndex);

        boolean doScaleText = hotbarItemAnimationsEnabled && textScales;

        if (!doScaleText) {
            original.call(instance, textRenderer, itemStack, x, y);
            return;
        }

        float scale = slotScales[slotIndex];
        int cx = x + 8;
        int cy = y + 8;

        instance.getMatrices().push();
        instance.getMatrices().translate(cx, cy, 0);
        instance.getMatrices().scale(scale, scale, 1);
        instance.getMatrices().translate(-cx, -cy, 0);

        original.call(instance, textRenderer, itemStack, x, y);
        instance.getMatrices().pop();
    }

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void renderShrinkingItems(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!hotbarItemAnimationsEnabled || !shrinkOutOnEmptyEnabled) return;

        PlayerEntity player = client.player;
        if (player == null) return;

        int centerXBase = client.getWindow().getScaledWidth() / 2;
        int y = client.getWindow().getScaledHeight() - 16 - 3;

        for (int i = 0; i < 9; i++) {
            if (!isShrinking[i]) continue;

            ItemStack stack = lastSlotStacks[i];
            if (stack.isEmpty()) continue;

            shrinkProgress[i] += deltaSeconds * shrinkAnimationSpeed;
            float progress = shrinkProgress[i];

            float eased = 1.0f - (progress * progress);
            float scale;
            if (bouncyAnimation) {
                float bounce = (float) Math.sin(progress * Math.PI) * 0.15f;
                scale = nonSelectedItemSize * eased + bounce;
            } else {
                scale = nonSelectedItemSize * eased;
            }
            slotScales[i] = scale;

            int x = (centerXBase - 91) + i * 20 + 3;
            int cx = x + 8;
            int cy = y + 8;

            context.getMatrices().push();
            context.getMatrices().translate(cx, cy, 0);
            context.getMatrices().scale(scale, scale, 1);
            context.getMatrices().translate(-cx, -cy, 0);

            context.drawItem(player, stack, x, y, 0);
            context.drawStackOverlay(client.textRenderer, stack, x, y);

            context.getMatrices().pop();

            if (progress >= 1.0f) {
                isShrinking[i] = false;
                lastSlotStacks[i] = ItemStack.EMPTY;
                lastSlotCounts[i] = 0;
                slotScales[i] = nonSelectedItemSize;
            }
        }
    }

    @Unique
    private boolean shouldTriggerShrink(ItemStack stack, int slotIndex) {
        return (stack == null || stack.isEmpty())
                && !lastSlotStacks[slotIndex].isEmpty()
                && !isShrinking[slotIndex];
    }

    @Unique
    private void startShrinkAnimation(int slotIndex) {
        isShrinking[slotIndex] = true;
        shrinkProgress[slotIndex] = 0f;
        slotScales[slotIndex] = 1.3f;
    }

    @Unique
    private void handleEmptySlot(int slotIndex) {
        if (!isShrinking[slotIndex]) {
            previousStacks[slotIndex] = ItemStack.EMPTY;
            lastSlotStacks[slotIndex] = ItemStack.EMPTY;
            lastSlotCounts[slotIndex] = 0;
        }
        slotScales[slotIndex] += (nonSelectedItemSize - slotScales[slotIndex])
                * animationSpeed * deltaSeconds * 60f;
    }

    @Unique
    private void handleOffhandSlotAnimations() {
        if (!offhandAnimationsEnabled) return;
        if (client.player == null) return;

        ItemStack offhandStack = client.player.getOffHandStack();

        boolean isToolItem = isTool(offhandStack);
        boolean isWeaponItem = isWeapon(offhandStack);
        boolean shouldAnimate = (!isToolItem || toolAnimates) && (!isWeaponItem || weaponAnimates);

        boolean changed = !ItemStack.areItemsEqual(offhandStack, lastOffhandStack)
                || !ItemStack.areItemsAndComponentsEqual(offhandStack, lastOffhandStack)
                || offhandStack.getCount() != lastOffhandCount;

        boolean wasUsed = false;
        int offhandIndex = IS_DOUBLEHOTBAR_LOADED ? 18 : 9;

        if (useAnimationsEnabled && durabilityAnimates && offhandStack.isDamageable()) {
            int currentDamage = offhandStack.getDamage();
            if (currentDamage > lastOffhandDamage && !wasUsed && shouldAnimate) {
                immersive_hotbar$triggerOffhandAnimation(offhandIndex);
                suppressOffhandPickup = true;
            }
            lastOffhandDamage = currentDamage;
        }

        if (pickupAnimationsEnabled && changed && !suppressOffhandPickup && shouldAnimate) {
            immersive_hotbar$triggerOffhandAnimation(offhandIndex);
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

        boolean shouldAnimateType = (!isToolItem || toolAnimates) && (!isWeaponItem || weaponAnimates);

        boolean wasEmpty = lastSlotStacks[slotIndex].isEmpty();
        boolean itemChanged = !ItemStack.areItemsEqual(stack, lastSlotStacks[slotIndex]);
        boolean countIncreased = stack.getCount() > lastSlotCounts[slotIndex];

        if (pickupAnimationsEnabled && shouldAnimateType) {
            if (!stack.isEmpty() && (wasEmpty || ((itemChanged || countIncreased) && !suppressNextPickup[slotIndex]))) {
                slotScales[slotIndex] = animationIntensity;
            }
        }

        if (useAnimationsEnabled && shouldAnimateType) {
            if (durabilityAnimates && isDamageable) {
                int currentDamage = stack.getDamage();
                int lastDamage = lastSlotDamage[slotIndex];
                if (currentDamage > lastDamage && !wasUsed[slotIndex]) {
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
        }

        previousStacks[slotIndex] = stack;
        wasUsed[slotIndex] = false;
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
    private void updateSelectorScales(int slotIndex, boolean isSelected) {
        float current = selectorScales[slotIndex];
        float target = isSelected ? 1.2f : 1.0f;
        selectorScales[slotIndex] += (target - current) * 0.3f * deltaSeconds * 60f;
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
            float factor = 1.0f - (float) Math.pow(1.0f - animationSpeed, deltaSeconds * 60f);
            slotScales[slotIndex] += (targetScale - slotScales[slotIndex]) * factor;
        }
    }

    @Unique
    private void drawDurabilityGlow(DrawContext context, ItemStack stack, int x, int y) {
        if (!lowDurabilityGlow || !stack.isDamageable()) return;

        float percent = (float) stack.getDamage() / stack.getMaxDamage();
        if (percent < durabilityGlowThreshold) return;

        float intensity = (percent - durabilityGlowThreshold) / (1.0f - durabilityGlowThreshold);
        intensity = Math.min(intensity, 1.0f);

        float baseAlpha = 0.6f + 0.4f * intensity;
        float maxPulse = 0.3f;

        if (percent >= 0.95f) {
            double pulse = Math.sin(System.currentTimeMillis() / 80.0);
            baseAlpha += maxPulse * (float) pulse;
        }

        float alpha = Math.min(Math.max(baseAlpha, 0.0f), 1.0f);
        int redGlow = ((int) (alpha * 255) << 24) | 0xFF0000;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 100);
        context.fill(x - 2, y - 2, x + 18, y, redGlow);
        context.fill(x - 2, y + 16, x + 18, y + 18, redGlow);
        context.fill(x - 2, y, x, y + 16, redGlow);
        context.fill(x + 16, y, x + 18, y + 16, redGlow);
        context.getMatrices().pop();
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
}
