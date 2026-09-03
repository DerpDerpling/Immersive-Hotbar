package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationRenderer;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AnimatedTooltipPreview implements ImageRenderer {
    private static final ResourceLocation WIDGETS = new ResourceLocation("minecraft", "textures/gui/widgets.png");

    private static final ItemStack PREVIEW_STACK = new ItemStack(Items.DIAMOND_SWORD);

    private static final Component PREVIEW_TEXT = Component.literal("Diamond Sword");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final TooltipAnimationController animation = new TooltipAnimationController();

    private long lastFrameTime = System.nanoTime();
    private float loopTimer = 0.0f;

    public void reset() {
        lastFrameTime = System.nanoTime();
        loopTimer = 0.0f;
        animation.reset();
    }

    private float frameDelta() {
        long now = System.nanoTime();
        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }

    private void update(float dt) {
        loopTimer += dt;
        if (loopTimer >= 5.0f) {
            loopTimer = 0.0f;
            animation.reset();
        }

        float fadeSeconds;
        if (loopTimer < 3.30f) {
            fadeSeconds = 0.20f;
        } else if (loopTimer < 3.55f) {
            float out = 1.0f - ((loopTimer - 3.30f) / 0.25f);
            fadeSeconds = 0.20f * Mth.clamp(out, 0.0f, 1.0f);
        } else {
            fadeSeconds = 0.0f;
        }

        animation.updatePreview(PREVIEW_STACK, fadeSeconds, dt);
    }

    private void renderHotbar(GuiGraphics graphics) {
        graphics.blit(WIDGETS, 0, 32, 0, 0, 182, 22);
        graphics.blit(WIDGETS, 79, 31, 0, 22, 24, 22);

        graphics.renderItem(PREVIEW_STACK, 83, 35);
        graphics.renderItemDecorations(minecraft.font, PREVIEW_STACK, 83, 35);
    }

    private void renderTooltip(GuiGraphics graphics) {
        int width = minecraft.font.width(PREVIEW_TEXT);
        int x = (182 - width) / 2;

        TooltipAnimationRenderer.renderPreview(graphics, minecraft, animation.state(), PREVIEW_TEXT, x, 10);
    }

    @Override
    public void tick() {
    }

    @Override
    public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
        float dt = frameDelta();
        update(dt);

        int naturalWidth = 182;
        int naturalHeight = 62;
        int padding = 6;

        float previewScale = Math.min(1.0f, (renderWidth - padding * 2) / (float) naturalWidth);

        int boxHeight = Math.round(naturalHeight * previewScale) + padding * 2;

        int scaledWidth = Math.round(naturalWidth * previewScale);
        int scaledHeight = Math.round(naturalHeight * previewScale);

        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxHeight - scaledHeight) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(startX, startY, 0.0f);
        graphics.pose().scale(previewScale, previewScale, 1.0f);

        renderTooltip(graphics);
        renderHotbar(graphics);

        graphics.pose().popPose();

        return boxHeight;
    }

    @Override
    public void close() {
    }
}