package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationRenderer;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class AnimatedTooltipPreview implements ImageRenderer {
    private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");
    private static final Identifier DIAMOND_SWORD_ICON = Identifier.withDefaultNamespace("textures/item/diamond_sword.png");
    private static final Component PREVIEW_TEXT = Component.literal("Diamond Sword");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final TooltipAnimationController animation = new TooltipAnimationController();

    private long lastFrameTime = System.nanoTime();
    private float loopTimer = 0.0f;

    public AnimatedTooltipPreview() {
        reset();
    }

    public void reset() {
        lastFrameTime = System.nanoTime();
        loopTimer = 0.0f;
        animation.reset();
        animation.state().scale(1.2f);
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
            animation.state().scale(1.2f);
        }

        float fadeSeconds;
        if (loopTimer < 3.30f) fadeSeconds = 0.20f;
        else if (loopTimer < 3.55f) fadeSeconds = 0.20f * Mth.clamp(1.0f - ((loopTimer - 3.30f) / 0.25f), 0.0f, 1.0f);
        else fadeSeconds = 0.0f;

        animation.state().fadeSeconds(fadeSeconds);
        animation.state().scale(TooltipAnimationController.updateScale(animation.state().scale(), animation.state().fadeRatio(), dt));
    }

    private void renderHotbar(GuiGraphicsExtractor graphics) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 32, 182, 24);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, 79, 31, 24, 24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, DIAMOND_SWORD_ICON, 83, 35, 0, 0, 16, 16, 16, 16);
    }

    private void renderTooltip(GuiGraphicsExtractor graphics) {
        int width = minecraft.font.width(PREVIEW_TEXT);
        int x = (182 - width) / 2;
        TooltipAnimationRenderer.renderPreview(graphics, minecraft, animation.state(), PREVIEW_TEXT, x, 10);
    }

    @Override
    public void tick() {}

    @Override
    public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
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
        graphics.pose().pushMatrix();
        graphics.pose().translate(startX, startY);
        graphics.pose().scale(previewScale, previewScale);
        renderTooltip(graphics);
        renderHotbar(graphics);
        graphics.pose().popMatrix();
        return boxHeight;
    }

    @Override
    public void close() {}
}
