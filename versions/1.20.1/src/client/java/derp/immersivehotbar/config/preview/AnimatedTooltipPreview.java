package derp.immersivehotbar.config.preview;

import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AnimatedTooltipPreview implements ImageRenderer {
    private static final ResourceLocation WIDGETS =
            new ResourceLocation("minecraft", "textures/gui/widgets.png");

    private final Minecraft minecraft = Minecraft.getInstance();

    private long lastFrameTime = System.nanoTime();
    private float loopTimer = 0.0f;
    private float tooltipScale = 0.0f;

    public void reset() {
        lastFrameTime = System.nanoTime();
        loopTimer = 0.0f;
        tooltipScale = 0.0f;
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
            tooltipScale = 1.2f;
        }

        float fadeSeconds;

        if (loopTimer < 0.20f) {
            fadeSeconds = 0.20f;
        } else if (loopTimer < 3.30f) {
            fadeSeconds = 0.20f;
        } else if (loopTimer < 3.55f) {
            float out = 1.0f - ((loopTimer - 3.30f) / 0.25f);
            fadeSeconds = 0.20f * Mth.clamp(out, 0.0f, 1.0f);
        } else {
            fadeSeconds = 0.0f;
        }

        if (fadeSeconds > 0.0f) {
            float fadeRatio = Math.min(fadeSeconds / 0.2f, 1.0f);
            float targetScale = 0.5f + fadeRatio * 0.5f;
            tooltipScale += (targetScale - tooltipScale) * (8.0f * dt);
            tooltipScale = Mth.clamp(tooltipScale, 0.0f, 1.5f);
        } else {
            tooltipScale += (0.0f - tooltipScale) * (10.0f * dt);
            if (tooltipScale < 0.01f) tooltipScale = 0.0f;
        }
    }

    private float currentFadeRatio() {
        if (loopTimer < 3.30f) return 1.0f;
        if (loopTimer < 3.55f) {return Mth.clamp(1.0f - ((loopTimer - 3.30f) / 0.25f), 0.0f, 1.0f);}
        return 0.0f;
    }

    private void renderHotbar(GuiGraphics gfx) {
        gfx.blit(WIDGETS, 0, 32, 0, 0, 182, 22);
        gfx.blit(WIDGETS, 79, 31, 0, 22, 24, 22);

        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        gfx.renderItem(stack, 83, 35);
        gfx.renderItemDecorations(minecraft.font, stack, 83, 35);
    }

    private void renderTooltip(GuiGraphics gfx) {
        float fadeRatio = currentFadeRatio();
        if (fadeRatio <= 0.05f || tooltipScale <= 0.01f) return;

        Component text = Component.literal("Diamond Sword");

        int width = minecraft.font.width(text);
        int x = (182 - width) / 2;
        int y = 10;
        int alpha = (int) (fadeRatio * 255.0f);
        int color = (alpha << 24) | 0xFFFFFF;

        gfx.pose().pushPose();
        gfx.pose().translate(x + width / 2.0f, y + 4.0f, 0.0f);
        gfx.pose().scale(tooltipScale, tooltipScale, 1.0f);
        gfx.pose().translate(-(x + width / 2.0f), -(y + 4.0f), 0.0f);

        gfx.drawString(minecraft.font, text, x, y, color, true);

        gfx.pose().popPose();
    }

    @Override
    public void tick() {
    }

    @Override
    public int render(GuiGraphics gfx, int x, int y, int renderWidth, float tickDelta) {
        float dt = frameDelta();
        update(dt);

        int naturalWidth = 182;
        int naturalHeight = 62;
        int padding = 6;

        float previewScale = Math.min(1.0f, (renderWidth - padding * 2) / (float) naturalWidth);
        int boxH = Math.round(naturalHeight * previewScale) + padding * 2;

        int scaledWidth = Math.round(naturalWidth * previewScale);
        int scaledHeight = Math.round(naturalHeight * previewScale);

        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxH - scaledHeight) / 2;

        gfx.pose().pushPose();
        gfx.pose().translate(startX, startY, 0.0f);
        gfx.pose().scale(previewScale, previewScale, 1.0f);

        renderTooltip(gfx);
        renderHotbar(gfx);

        gfx.pose().popPose();

        return boxH;
    }

    @Override
    public void close() {
    }
}