package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationEngine;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationState;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class ItemAnimationPreview implements ImageRenderer {
    private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");
    private static final Identifier GRASS_BLOCK_ICON = Identifier.fromNamespaceAndPath("immersive-hotbar", "textures/gui/grass_block.png");
    private static final Identifier IRON_PICKAXE_ICON = Identifier.withDefaultNamespace("textures/item/iron_pickaxe.png");

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

        if (loopTimer >= 1.0f && shrinkProgress <= 0.0f) shrinkProgress = Float.MIN_VALUE;

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

    private void renderContents(GuiGraphicsExtractor gfx) {
        switch (mode) {
            case SELECTED_SCALE_COMPARISON -> renderSelectedComparison(gfx);
            case PICKUP_POP -> renderPickupDemo(gfx);
            case USE_SHRINK, SHRINK_OUT_ON_EMPTY -> renderSingleSlot(gfx);
        }
    }

    private void renderSelectedComparison(GuiGraphicsExtractor gfx) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);
        int leftSlot = 3;
        int rightSlot = 5;
        int itemY = 4;
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, rightSlot * 20 - 1, -1, 24, 24);
        renderScaledItemIcon(gfx, GRASS_BLOCK_ICON, leftSlot * 20 + 3, itemY, animation.scale(PRIMARY), 64);
        renderScaledItemIcon(gfx, GRASS_BLOCK_ICON, rightSlot * 20 + 3, itemY, animation.scale(SECONDARY), 64);
    }

    private void renderPickupDemo(GuiGraphicsExtractor gfx) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);
        int leftSlot = 2;
        int centerSlot = 4;
        int rightSlot = 6;
        int itemY = 4;
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, centerSlot * 20 - 1, -1, 24, 24);
        renderScaledItemIcon(gfx, GRASS_BLOCK_ICON, leftSlot * 20 + 3, itemY, animation.scale(PICKUP_PLUS), plusCount);
        if (pickupCenterVisible) renderScaledItemIcon(gfx, GRASS_BLOCK_ICON, centerSlot * 20 + 3, itemY, animation.scale(PICKUP_NEW), centerCount);
        renderScaledItemIcon(gfx, GRASS_BLOCK_ICON, rightSlot * 20 + 3, itemY, animation.scale(PICKUP_MINUS), minusCount);
    }

    private void renderSingleSlot(GuiGraphicsExtractor gfx) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, 79, -1, 24, 24);
        if (!itemVisible) return;
        renderScaledItemIcon(gfx, mode == PreviewMode.USE_SHRINK ? IRON_PICKAXE_ICON : GRASS_BLOCK_ICON, 83, 3, animation.scale(PRIMARY), mode == PreviewMode.USE_SHRINK ? 1 : 64);
    }

    private void renderScaledItemIcon(GuiGraphicsExtractor gfx, Identifier icon, int x, int y, float scale, int count) {
        int cx = x + 8;
        int cy = y + 8;

        gfx.pose().pushMatrix();
        gfx.pose().translate(cx, cy);
        gfx.pose().scale(scale, scale);
        gfx.pose().translate(-cx, -cy);

        boolean vanillaPose = vanillaBobbingEnabled() && vanillaPopTime > 0.0f;
        if (vanillaPose) {
            float bobScale = 1.0f + vanillaPopTime / 5.0f;
            gfx.pose().pushMatrix();
            gfx.pose().translate(x + 8.0f, y + 12.0f);
            gfx.pose().scale(1.0f / bobScale, (bobScale + 1.0f) / 2.0f);
            gfx.pose().translate(-(x + 8.0f), -(y + 12.0f));
        }

        gfx.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, 16, 16, 16, 16);
        if (vanillaPose) gfx.pose().popMatrix();
        gfx.pose().popMatrix();

        if (count > 1) renderCount(gfx, x, y, scale, count);
        if (mode == PreviewMode.USE_SHRINK) renderDurabilityBar(gfx, x, y, scale);
    }

    private void renderCount(GuiGraphicsExtractor gfx, int x, int y, float scale, int count) {
        String amount = String.valueOf(count);
        int countX = x + 17 - minecraft.font.width(amount);
        int countY = y + 9;

        if (!textScales()) {
            gfx.text(minecraft.font, amount, countX, countY, 0xFFFFFFFF, true);
            return;
        }

        int cx = x + 8;
        int cy = y + 8;
        gfx.pose().pushMatrix();
        gfx.pose().translate(cx, cy);
        gfx.pose().scale(scale, scale);
        gfx.pose().translate(-cx, -cy);
        gfx.text(minecraft.font, amount, countX, countY, 0xFFFFFFFF, true);
        gfx.pose().popMatrix();
    }

    private void renderDurabilityBar(GuiGraphicsExtractor gfx, int x, int y, float scale) {
        float durability = Mth.clamp(1.0f - currentDamage / 250.0f, 0.0f, 1.0f);
        int width = Math.round(13.0f * durability);
        int cx = x + 8;
        int cy = y + 8;

        gfx.pose().pushMatrix();
        gfx.pose().translate(cx, cy);
        gfx.pose().scale(scale, scale);
        gfx.pose().translate(-cx, -cy);
        gfx.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
        gfx.fill(x + 2, y + 13, x + 2 + width, y + 14, 0xFF55FF55);
        gfx.pose().popMatrix();
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
    public int render(GuiGraphicsExtractor gfx, int x, int y, int renderWidth, float tickDelta) {
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
    public void close() {}
}
