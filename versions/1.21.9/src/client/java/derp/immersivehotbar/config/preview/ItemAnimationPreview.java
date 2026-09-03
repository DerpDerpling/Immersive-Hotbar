package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationEngine;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationRenderer;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationState;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemAnimationPreview implements ImageRenderer {
    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");

    private static final int PRIMARY = 0;
    private static final int SECONDARY = 1;
    private static final int PICKUP_PLUS = 2;
    private static final int PICKUP_NEW = 3;
    private static final int PICKUP_MINUS = 4;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemAnimationPreviewState options;
    private final PreviewMode mode;
    private final boolean forceBouncy;
    private final HotbarAnimationState animation = new HotbarAnimationState();

    private long lastFrameTime = System.nanoTime();
    private float loopTimer;
    private float shrinkProgress;
    private float vanillaPopTime;
    private boolean initialized;
    private boolean itemVisible = true;
    private boolean pickupCenterVisible = true;

    private int currentDamage = 110;
    private int plusCount = 16;
    private int centerCount = 1;
    private int minusCount = 16;

    public ItemAnimationPreview(ItemAnimationPreviewState state, PreviewMode mode) {
        this(state, mode, false);
    }

    public ItemAnimationPreview(ItemAnimationPreviewState state, PreviewMode mode, boolean forceBouncy) {
        this.options = state;
        this.mode = mode;
        this.forceBouncy = forceBouncy;
    }
    private boolean isSettled(int slot, float target) {
        return Math.abs(animation.scale(slot) - target) < 0.01f
                && Math.abs(animation.velocity(slot)) < 0.01f;
    }

    public void reset() {
        float base = unselectedScale();
        animation.initialize(base);

        loopTimer = 0.0f;
        shrinkProgress = 0.0f;
        vanillaPopTime = 0.0f;
        itemVisible = true;
        pickupCenterVisible = true;

        currentDamage = 110;
        plusCount = 16;
        centerCount = 1;
        minusCount = 16;

        lastFrameTime = System.nanoTime();
        initialized = true;
    }

    private float frameDelta() {
        long now = System.nanoTime();
        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }

    private void update(float dt) {
        tickVanillaBob(dt);

        switch (mode) {
            case SELECTED_SCALE_COMPARISON -> updateSelectedComparison(dt);
            case PICKUP_POP -> updatePickupDemo(dt);
            case USE_SHRINK -> updateUseDemo(dt);
            case SHRINK_OUT_ON_EMPTY -> updateEmptyDemo(dt);
        }
    }

    private void updateSelectedComparison(float dt) {
        animate(PRIMARY, unselectedScale(), dt);
        animate(SECONDARY, shouldGrow() ? selectedScale() : unselectedScale(), dt);
        itemVisible = true;
    }

    private void updatePickupDemo(float dt) {
        loopTimer += dt;
        float target = selectedTarget();

        if (loopTimer < 0.55f) {
            setScale(PICKUP_PLUS, target);
            setScale(PICKUP_NEW, target);
            setScale(PICKUP_MINUS, target);
            pickupCenterVisible = false;
            plusCount = 16;
            centerCount = 1;
            minusCount = 16;
            return;
        }

        if (loopTimer < 1.45f) {
            if (!pickupCenterVisible) {
                pickupCenterVisible = true;
                plusCount = 17;
                centerCount = 1;
                minusCount = 15;

                setScale(PICKUP_PLUS, animationIntensity());
                setScale(PICKUP_NEW, animationIntensity());
                setScale(PICKUP_MINUS, unselectedScale() - 0.1f);
                triggerVanillaBob();
            }

            animate(PICKUP_PLUS, target, dt);
            animate(PICKUP_NEW, target, dt);
            animate(PICKUP_MINUS, target, dt);
            return;
        }

        loopTimer = 0.0f;
        pickupCenterVisible = false;
        plusCount = 16;
        centerCount = 1;
        minusCount = 16;
    }

    private void updateUseDemo(float dt) {
        loopTimer += dt;

        if (loopTimer >= 1.6f) {
            loopTimer = 0.0f;
            setScale(PRIMARY, unselectedScale() - 0.1f);
            currentDamage = Math.min(currentDamage + 1, 145);
        }

        animate(PRIMARY, selectedTarget(), dt);
        itemVisible = true;
    }

    private void updateEmptyDemo(float dt) {
        loopTimer += dt;
        float base = unselectedScale();

        if (loopTimer >= 1.0f && shrinkProgress <= 0.0f) {
            shrinkProgress = Float.MIN_VALUE;
        }

        if (shrinkProgress > 0.0f) {
            shrinkProgress += dt * shrinkSpeed();
            float progress = Mth.clamp(shrinkProgress, 0.0f, 1.0f);
            animation.scale(PRIMARY, HotbarAnimationEngine.shrinkOutScale(base, progress, useBouncy()));
            animation.velocity(PRIMARY, 0.0f);
            itemVisible = true;

            if (progress >= 1.0f) {
                shrinkProgress = 0.0f;
                itemVisible = false;
                loopTimer = -0.6f;
                setScale(PRIMARY, base);
            }
        } else {
            setScale(PRIMARY, base);
            itemVisible = loopTimer >= 0.0f;
        }
    }

    private void animate(int slot, float target, float dt) {
        HotbarAnimationEngine.animateScale(animation, slot, target, dt, useBouncy(), bounceStiffness(), bounceDamping(), animationSpeed());
    }

    private void setScale(int slot, float scale) {
        animation.scale(slot, scale);
        animation.velocity(slot, 0.0f);
    }

    private void triggerVanillaBob() {
        vanillaPopTime = 5.0f;
    }

    private void tickVanillaBob(float dt) {
        if (vanillaPopTime <= 0.0f) return;
        vanillaPopTime = Math.max(0.0f, vanillaPopTime - dt * 20.0f);
    }

    private void renderContents(GuiGraphics gfx) {
        switch (mode) {
            case SELECTED_SCALE_COMPARISON -> renderSelectedComparison(gfx);
            case PICKUP_POP -> renderPickupDemo(gfx);
            case USE_SHRINK, SHRINK_OUT_ON_EMPTY -> renderSingleSlot(gfx);
        }
    }

    private void renderSelectedComparison(GuiGraphics gfx) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);

        int leftSlot = 3;
        int rightSlot = 5;
        int itemY = 4;

        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, rightSlot * 20 - 1, -1, 24, 24);

        ItemStack left = new ItemStack(Items.GRASS_BLOCK, 64);
        ItemStack right = new ItemStack(Items.GRASS_BLOCK, 64);
        renderScaledItem(gfx, left, leftSlot * 20 + 3, itemY, animation.scale(PRIMARY));
        renderScaledItem(gfx, right, rightSlot * 20 + 3, itemY, animation.scale(SECONDARY));
    }

    private void renderPickupDemo(GuiGraphics gfx) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);

        int leftSlot = 2;
        int centerSlot = 4;
        int rightSlot = 6;
        int itemY = 4;

        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, centerSlot * 20 - 1, -1, 24, 24);

        renderScaledItem(gfx, new ItemStack(Items.GRASS_BLOCK, plusCount), leftSlot * 20 + 3, itemY, animation.scale(PICKUP_PLUS));

        if (pickupCenterVisible) {
            renderScaledItem(gfx, new ItemStack(Items.GRASS_BLOCK, centerCount), centerSlot * 20 + 3, itemY, animation.scale(PICKUP_NEW));
        }

        renderScaledItem(gfx, new ItemStack(Items.GRASS_BLOCK, minusCount), rightSlot * 20 + 3, itemY, animation.scale(PICKUP_MINUS));
    }

    private void renderSingleSlot(GuiGraphics gfx) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, 79, -1, 24, 24);

        if (!itemVisible) return;
        renderScaledItem(gfx, createStackForMode(), 83, 3, animation.scale(PRIMARY));
    }

    private ItemStack createStackForMode() {
        if (mode == PreviewMode.USE_SHRINK) {
            ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
            stack.setDamageValue(currentDamage);
            return stack;
        }
        return new ItemStack(Items.GRASS_BLOCK, 64);
    }

    private void renderScaledItem(GuiGraphics gfx, ItemStack stack, int x, int y, float scale) {
        HotbarAnimationRenderer.withItemScale(gfx, x, y, scale, () -> {
            boolean vanillaPose = vanillaBobbingEnabled() && vanillaPopTime > 0.0f;

            if (vanillaPose) {
                float bobScale = 1.0f + vanillaPopTime / 5.0f;
                gfx.pose().pushMatrix();
                gfx.pose().translate(x + 8.0f, y + 12.0f);
                gfx.pose().scale(1.0f / bobScale, (bobScale + 1.0f) / 2.0f);
                gfx.pose().translate(-(x + 8.0f), -(y + 12.0f));
            }

            gfx.renderItem(stack, x, y);

            if (vanillaPose) gfx.pose().popMatrix();
            if (textScales()) gfx.renderItemDecorations(minecraft.font, stack, x, y);
        });

        if (!textScales()) {
            gfx.renderItemDecorations(minecraft.font, stack, x, y);
        }
    }

    private boolean shouldGrow() {
        return options.shouldGrowOpt != null && options.shouldGrowOpt.pendingValue();
    }

    private float selectedTarget() {
        return shouldGrow() ? selectedScale() : unselectedScale();
    }

    private float selectedScale() {
        return options.selectedScaleOpt != null ? options.selectedScaleOpt.pendingValue() : 1.2f;
    }

    private float unselectedScale() {
        return options.unselectedScaleOpt != null ? options.unselectedScaleOpt.pendingValue() : 1.0f;
    }

    private boolean textScales() {
        return options.textScalingOpt == null || options.textScalingOpt.pendingValue();
    }

    private boolean useBouncy() {
        return forceBouncy || (options.bouncyEnabledOpt != null && options.bouncyEnabledOpt.pendingValue());
    }

    private float bounceStiffness() {
        return options.bounceStiffnessOpt != null ? options.bounceStiffnessOpt.pendingValue() : 0.3f;
    }

    private float bounceDamping() {
        return options.bounceDampingOpt != null ? options.bounceDampingOpt.pendingValue() : 0.2f;
    }

    private float animationIntensity() {
        return options.animationIntensityOpt != null ? options.animationIntensityOpt.pendingValue() : 0.5f;
    }

    private float animationSpeed() {
        return options.animationSpeedOpt != null ? options.animationSpeedOpt.pendingValue() : 0.1f;
    }

    private float shrinkSpeed() {
        return options.shrinkSpeedOpt != null ? options.shrinkSpeedOpt.pendingValue() : 2.5f;
    }

    private boolean vanillaBobbingEnabled() {
        return options.vanillaItemBobbingOpt != null && options.vanillaItemBobbingOpt.pendingValue();
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
        int scaledWidth = Math.round(naturalWidth * previewScale);
        int scaledHeight = Math.round(naturalHeight * previewScale);
        int boxHeight = scaledHeight + padding * 2;

        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxHeight - scaledHeight) / 2;

        gfx.pose().pushMatrix();
        gfx.pose().translate(startX, startY);
        gfx.pose().scale(previewScale, previewScale);
        renderContents(gfx);
        gfx.pose().popMatrix();

        return boxHeight;
    }

    @Override
    public void close() {
    }
}