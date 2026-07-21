package derp.immersivehotbar.config.preview;

import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemAnimationPreview implements ImageRenderer {
    private static final ResourceLocation WIDGETS =
            new ResourceLocation("minecraft", "textures/gui/widgets.png");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemAnimationPreviewState state;
    private final PreviewMode mode;
    private final boolean forceBouncy;

    private float scale = 1.0f;
    private float velocity = 0.0f;

    private float secondaryScale = 1.0f;
    private float secondaryVelocity = 0.0f;

    private boolean itemVisible = true;
    private boolean shrinkingOut = false;
    private float shrinkProgress = 0.0f;

    private float loopTimer = 0.0f;
    private long lastFrameTime = System.nanoTime();
    private boolean initialized = false;

    private int currentDamage = 110;

    private float stackPlusScale = 1.0f;
    private float stackPlusVelocity = 0.0f;

    private float newPickupScale = 1.0f;
    private float newPickupVelocity = 0.0f;

    private float stackMinusScale = 1.0f;
    private float stackMinusVelocity = 0.0f;

    private boolean centerVisible = true;

    private int plusCount = 16;
    private int centerCount = 1;
    private int minusCount = 16;
    private float previewPopTime = 0.0f;

    public ItemAnimationPreview(ItemAnimationPreviewState state, PreviewMode mode) {
        this(state, mode, false);
    }

    public ItemAnimationPreview(ItemAnimationPreviewState state, PreviewMode mode, boolean forceBouncy) {
        this.state = state;
        this.mode = mode;
        this.forceBouncy = forceBouncy;
    }

    public void reset() {
        float base = getUnselectedScale();
        scale = base;
        velocity = 0.0f;
        secondaryScale = base;
        secondaryVelocity = 0.0f;

        stackPlusScale = base;
        stackPlusVelocity = 0.0f;
        newPickupScale = base;
        newPickupVelocity = 0.0f;
        stackMinusScale = base;
        stackMinusVelocity = 0.0f;

        itemVisible = true;
        centerVisible = true;
        shrinkingOut = false;
        shrinkProgress = 0.0f;
        loopTimer = 0.0f;

        plusCount = 16;
        centerCount = 1;
        minusCount = 16;

        currentDamage = 110;
        lastFrameTime = System.nanoTime();
        initialized = true;
    }
    private boolean vanillaBobbingEnabled() {
        return state.vanillaItemBobbingOpt != null && state.vanillaItemBobbingOpt.pendingValue();
    }

    private void triggerVanillaBob() {
        previewPopTime = 5.0f;
    }

    private void tickVanillaBob(float dt) {
        if (previewPopTime > 0.0f) {
            previewPopTime -= dt * 20.0f;
            if (previewPopTime < 0.0f) {
                previewPopTime = 0.0f;
            }
        }
    }
    private float nextScale(float currentScale, float currentVelocity, float targetScale, float dt) {
        if (useBouncy()) {
            float force = (targetScale - currentScale) * getBouncyStiffness();
            currentVelocity += force * dt * 60.0f;
            currentVelocity *= (float) Math.pow(1.0f - getBouncyDamping(), dt * 60.0f);
            return currentScale + currentVelocity * dt * 60.0f;
        } else {
            float factor = 1.0f - (float) Math.pow(1.0f - getAnimationSpeed(), dt * 60.0f);
            return currentScale + (targetScale - currentScale) * factor;
        }
    }

    private float nextVelocity(float currentScale, float currentVelocity, float targetScale, float dt) {
        if (useBouncy()) {
            float force = (targetScale - currentScale) * getBouncyStiffness();
            currentVelocity += force * dt * 60.0f;
            currentVelocity *= (float) Math.pow(1.0f - getBouncyDamping(), dt * 60.0f);
            return currentVelocity;
        } else {
            return 0.0f;
        }
    }
    private float smoothToward(float current, float target, float dt) {
        float factor = 1.0f - (float) Math.pow(1.0f - getAnimationSpeed(), dt * 60.0f);
        return current + (target - current) * factor;
    }

    private float frameDelta() {
        long now = System.nanoTime();
        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }

    private boolean shouldGrow() {
        return state.shouldGrowOpt != null && state.shouldGrowOpt.pendingValue();
    }

    private float getSelectedScale() {
        return state.selectedScaleOpt != null ? state.selectedScaleOpt.pendingValue() : 1.2f;
    }

    private float getUnselectedScale() {
        return state.unselectedScaleOpt != null ? state.unselectedScaleOpt.pendingValue() : 1.0f;
    }

    private boolean textScales() {
        return state.textScalingOpt == null || state.textScalingOpt.pendingValue();
    }

    private boolean useBouncy() {
        return forceBouncy || (state.bouncyEnabledOpt != null && state.bouncyEnabledOpt.pendingValue());
    }

    private float getBouncyStiffness() {
        return state.bounceStiffnessOpt != null ? state.bounceStiffnessOpt.pendingValue() : 0.3f;
    }

    private float getBouncyDamping() {
        return state.bounceDampingOpt != null ? state.bounceDampingOpt.pendingValue() : 0.2f;
    }

    private float getAnimationIntensity() {
        return state.animationIntensityOpt != null ? state.animationIntensityOpt.pendingValue() : 0.5f;
    }

    private float getAnimationSpeed() {
        return state.animationSpeedOpt != null ? state.animationSpeedOpt.pendingValue() : 0.1f;
    }

    private float getShrinkSpeed() {
        return state.shrinkSpeedOpt != null ? state.shrinkSpeedOpt.pendingValue() : 2.5f;
    }

    private void animateToward(float dt, float targetScale) {
        if (useBouncy()) {
            float force = (targetScale - scale) * getBouncyStiffness();
            velocity += force * dt * 60.0f;
            velocity *= (float) Math.pow(1.0f - getBouncyDamping(), dt * 60.0f);
            scale += velocity * dt * 60.0f;
        } else {
            float factor = 1.0f - (float) Math.pow(1.0f - getAnimationSpeed(), dt * 60.0f);
            scale += (targetScale - scale) * factor;
            velocity = 0.0f;
        }
    }

    private void updateSelectedScaleComparison(float dt) {
        float unselectedTarget = getUnselectedScale();
        float selectedTarget = shouldGrow() ? getSelectedScale() : getUnselectedScale();

        if (useBouncy()) {
            float forceLeft = (unselectedTarget - scale) * getBouncyStiffness();
            velocity += forceLeft * dt * 60.0f;
            velocity *= (float) Math.pow(1.0f - getBouncyDamping(), dt * 60.0f);
            scale += velocity * dt * 60.0f;

            float forceRight = (selectedTarget - secondaryScale) * getBouncyStiffness();
            secondaryVelocity += forceRight * dt * 60.0f;
            secondaryVelocity *= (float) Math.pow(1.0f - getBouncyDamping(), dt * 60.0f);
            secondaryScale += secondaryVelocity * dt * 60.0f;
        } else {
            scale = smoothToward(scale, unselectedTarget, dt);
            secondaryScale = smoothToward(secondaryScale, selectedTarget, dt);
            velocity = 0.0f;
            secondaryVelocity = 0.0f;
        }

        itemVisible = true;
    }

    private void updatePickupPop(float dt) {
        loopTimer += dt;

        float target = shouldGrow() ? getSelectedScale() : getUnselectedScale();

        if (loopTimer < 0.55f) {
            stackPlusScale = target;
            stackPlusVelocity = 0.0f;

            newPickupScale = target;
            newPickupVelocity = 0.0f;
            centerVisible = false;

            stackMinusScale = target;
            stackMinusVelocity = 0.0f;

            plusCount = 16;
            centerCount = 1;
            minusCount = 16;
        } else if (loopTimer < 1.45f) {
            if (!centerVisible) {
                centerVisible = true;

                stackPlusScale = getAnimationIntensity();
                stackPlusVelocity = 0.0f;
                plusCount = 17;

                newPickupScale = getAnimationIntensity();
                newPickupVelocity = 0.0f;
                centerCount = 1;

                stackMinusScale = getUnselectedScale() - 0.1f;
                stackMinusVelocity = 0.0f;
                minusCount = 15;

                triggerVanillaBob();
            }

            stackPlusVelocity = nextVelocity(stackPlusScale, stackPlusVelocity, target, dt);
            stackPlusScale = nextScale(stackPlusScale, stackPlusVelocity, target, dt);

            newPickupVelocity = nextVelocity(newPickupScale, newPickupVelocity, target, dt);
            newPickupScale = nextScale(newPickupScale, newPickupVelocity, target, dt);

            stackMinusVelocity = nextVelocity(stackMinusScale, stackMinusVelocity, target, dt);
            stackMinusScale = nextScale(stackMinusScale, stackMinusVelocity, target, dt);
        } else {
            loopTimer = 0.0f;
            centerVisible = false;
            plusCount = 16;
            centerCount = 1;
            minusCount = 16;
        }

        itemVisible = true;
    }

    private void blitHotbar(GuiGraphics gfx) {
        gfx.blit(WIDGETS, 0, 0, 0, 0, 182, 22);
    }

    private void blitHotbarSelection(GuiGraphics gfx, int x, int y) {
        gfx.blit(WIDGETS, x, y, 0, 22, 24, 22);
    }

    private void renderPickupPopPreview(GuiGraphics gfx) {
        blitHotbar(gfx);

        int leftSlotIndex = 2;
        int centerSlotIndex = 4;
        int rightSlotIndex = 6;

        int leftItemX = leftSlotIndex * 20 + 3;
        int centerItemX = centerSlotIndex * 20 + 3;
        int rightItemX = rightSlotIndex * 20 + 3;
        int itemY = 4;

        blitHotbarSelection(gfx, centerSlotIndex * 20 - 1, -1);

        ItemStack plusStack = new ItemStack(Items.GRASS_BLOCK, plusCount);
        ItemStack centerStack = new ItemStack(Items.GRASS_BLOCK, centerCount);
        ItemStack minusStack = new ItemStack(Items.GRASS_BLOCK, minusCount);

        renderScaledItem(gfx, plusStack, leftItemX, itemY, stackPlusScale);

        if (centerVisible) {
            renderScaledItem(gfx, centerStack, centerItemX, itemY, newPickupScale);
        }

        renderScaledItem(gfx, minusStack, rightItemX, itemY, stackMinusScale);
    }
    private void updateUseShrink(float dt) {
        loopTimer += dt;

        float target = shouldGrow() ? getSelectedScale() : getUnselectedScale();

        if (loopTimer >= 1.6f) {
            loopTimer = 0.0f;
            scale = getUnselectedScale() - 0.1f;
            velocity = 0.0f;
            currentDamage = Math.min(currentDamage + 1, 145);
        }

        animateToward(dt, target);
        itemVisible = true;
    }

    private void updateShrinkOutOnEmpty(float dt) {
        loopTimer += dt;

        float baseScale = getUnselectedScale();

        if (!shrinkingOut && loopTimer >= 1.0f) {
            shrinkingOut = true;
            shrinkProgress = 0.0f;
        }

        if (shrinkingOut) {
            shrinkProgress += dt * getShrinkSpeed();
            float progress = Mth.clamp(shrinkProgress, 0.0f, 1.0f);

            float eased = 1.0f - (progress * progress);
            float renderScale;
            if (useBouncy()) {
                float bounce = (float) Math.sin(progress * Math.PI) * 0.15f;
                renderScale = baseScale * eased + bounce;
            } else {
                renderScale = baseScale * eased;
            }

            scale = renderScale;
            itemVisible = true;

            if (progress >= 1.0f) {
                shrinkingOut = false;
                itemVisible = false;
                loopTimer = -0.6f;
                scale = baseScale;
            }
        } else {
            scale = baseScale;
            velocity = 0.0f;
            itemVisible = loopTimer >= 0.0f;
        }
    }

    private void update(float dt) {
        tickVanillaBob(dt);
        switch (mode) {
            case SELECTED_SCALE_COMPARISON -> updateSelectedScaleComparison(dt);
            case PICKUP_POP -> updatePickupPop(dt);
            case USE_SHRINK -> updateUseShrink(dt);
            case SHRINK_OUT_ON_EMPTY -> updateShrinkOutOnEmpty(dt);

        }
    }

    private ItemStack createStackForMode() {
        return switch (mode) {
            case USE_SHRINK -> {
                ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
                stack.setDamageValue(currentDamage);
                yield stack;
            }
            case SHRINK_OUT_ON_EMPTY, PICKUP_POP, SELECTED_SCALE_COMPARISON -> new ItemStack(Items.GRASS_BLOCK, 64);
        };
    }

    private void renderScaledItem(GuiGraphics gfx, ItemStack stack, int itemX, int itemY, float renderScale) {
        int cx = itemX + 8;
        int cy = itemY + 8;

        gfx.pose().pushPose();
        gfx.pose().translate(cx, cy, 0.0f);
        gfx.pose().scale(renderScale, renderScale, 1.0f);
        gfx.pose().translate(-cx, -cy, 0.0f);

        boolean didVanillaPopPose = false;
        float f = previewPopTime;

        if (vanillaBobbingEnabled() && f > 0.0f) {
            float g = 1.0f + f / 5.0f;
            gfx.pose().pushPose();
            gfx.pose().translate((float) (itemX + 8), (float) (itemY + 12), 0.0f);
            gfx.pose().scale(1.0f / g, (g + 1.0f) / 2.0f, 1.0f);
            gfx.pose().translate((float) (-(itemX + 8)), (float) (-(itemY + 12)), 0.0f);
            didVanillaPopPose = true;
        }

        gfx.renderItem(stack, itemX, itemY);

        if (didVanillaPopPose) {
            gfx.pose().popPose();
        }

        gfx.pose().popPose();

        if (textScales()) {
            gfx.pose().pushPose();
            gfx.pose().translate(cx, cy, 0.0f);
            gfx.pose().scale(renderScale, renderScale, 1.0f);
            gfx.pose().translate(-cx, -cy, 0.0f);
            gfx.renderItemDecorations(minecraft.font, stack, itemX, itemY);
            gfx.pose().popPose();
        } else {
            gfx.renderItemDecorations(minecraft.font, stack, itemX, itemY);
        }
    }

    private void renderSelectedScaleComparison(GuiGraphics gfx) {
        blitHotbar(gfx);

        int leftSlotIndex = 3;
        int rightSlotIndex = 5;

        int leftItemX = leftSlotIndex * 20 + 3;
        int rightItemX = rightSlotIndex * 20 + 3;
        int itemY = 4;

        blitHotbarSelection(gfx, rightSlotIndex * 20 - 1, -1);

        ItemStack leftStack = new ItemStack(Items.GRASS_BLOCK, 64);
        ItemStack rightStack = new ItemStack(Items.GRASS_BLOCK, 64);

        renderScaledItem(gfx, leftStack, leftItemX, itemY, scale);
        renderScaledItem(gfx, rightStack, rightItemX, itemY, secondaryScale);
    }

    private void renderSingleSlot(GuiGraphics gfx) {
        blitHotbar(gfx);
        blitHotbarSelection(gfx, 79, -1);

        if (!itemVisible) return;

        ItemStack stack = createStackForMode();
        renderScaledItem(gfx, stack, 83, 3, scale);
    }

    private void renderContents(GuiGraphics gfx) {
        switch (mode) {
            case SELECTED_SCALE_COMPARISON -> renderSelectedScaleComparison(gfx);
            case PICKUP_POP -> renderPickupPopPreview(gfx);
            case USE_SHRINK, SHRINK_OUT_ON_EMPTY -> renderSingleSlot(gfx);
        }
    }

    @Override
    public void tick() {
        if (!initialized) reset();
    }

    @Override
    public int render(GuiGraphics gfx, int x, int y, int renderWidth, float tickDelta) {
        if (!initialized) reset();

        float dt = frameDelta();
        update(dt);

        int naturalWidth = 182;
        int naturalHeight = 24;
        int padding = 6;

        float previewScale = Math.min(1.0f, (renderWidth - padding * 2) / (float) naturalWidth);
        int boxH = Math.round(naturalHeight * previewScale) + padding * 2;

        int scaledWidth = Math.round(naturalWidth * previewScale);
        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxH - Math.round(naturalHeight * previewScale)) / 2;

        gfx.pose().pushPose();
        gfx.pose().translate(startX, startY, 0.0f);
        gfx.pose().scale(previewScale, previewScale, 1.0f);
        renderContents(gfx);
        gfx.pose().popPose();

        return boxH;
    }

    @Override
    public void close() {
    }
}