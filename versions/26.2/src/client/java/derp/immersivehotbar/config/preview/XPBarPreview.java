package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.animation.xp.XPAnimationController;
import derp.immersivehotbar.animation.xp.XPAnimationRenderer;
import derp.immersivehotbar.animation.xp.XPAnimationState;
import derp.immersivehotbar.animation.xp.XPParticleController;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import net.minecraft.util.Mth;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class XPBarPreview implements ImageRenderer {
    private static final Identifier EXPERIENCE_BAR_BACKGROUND = Identifier.withDefaultNamespace("hud/experience_bar_background");
    private static final Identifier EXPERIENCE_BAR_PROGRESS = Identifier.withDefaultNamespace("hud/experience_bar_progress");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final XPAnimationState state = new XPAnimationState();
    private final XPParticleController particles = new XPParticleController();

    private long lastFrameTime = System.nanoTime();

    private float loopTimer = 0.0f;
    private float targetXpTotal = 4.15f;
    private int displayedLevel = 4;
    private boolean spawnedParticlesThisLoop = false;

    public XPBarPreview() {
        reset();
    }

    public void reset() {
        lastFrameTime = System.nanoTime();
        loopTimer = 0.0f;
        targetXpTotal = 4.15f;
        displayedLevel = 4;
        spawnedParticlesThisLoop = false;

        state.reset();
        state.animatedTotal(4.15f);
        state.animatedProgress(0.15f);
        state.glowHeadProgress(0.15f);
        state.lastLevel(4);

        particles.clear();
    }

    private float frameDelta() {
        long now = System.nanoTime();
        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }

    private void updateTarget() {
        if (loopTimer < 0.7f) {
            targetXpTotal = 4.15f;
            displayedLevel = 4;
        } else if (loopTimer < 2.3f) {
            targetXpTotal = 4.82f;
            displayedLevel = 4;
        } else if (loopTimer < 3.7f) {
            targetXpTotal = 5.18f;
            displayedLevel = 5;
        } else if (loopTimer < 5.0f) {
            targetXpTotal = 5.55f;
            displayedLevel = 5;
        } else {
            reset();
        }
    }

    private void update(float dt) {
        loopTimer += dt;

        int oldLevel = (int) Math.floor(state.animatedTotal());
        updateTarget();

        XPAnimationController.animateProgress(state, targetXpTotal, dt);
        XPAnimationController.updateGlowHead(state, state.animatedProgress(), dt);

        int newLevel = (int) Math.floor(targetXpTotal);
        boolean leveledUp = newLevel > oldLevel && loopTimer > 2.25f && loopTimer < 2.45f;
        boolean gaining = targetXpTotal > state.animatedTotal() + 0.002f;
        if (leveledUp && xpBarPulseEnabled) {
            int every = Math.max(1, xpBarPulseLevels);
            if (displayedLevel % every == 0) state.barPulse(1.0f);
        }

        if (state.barPulse() > 0.0f) {
            state.barPulse(Math.max(0.0f, state.barPulse() - dt * 0.08f * 20.0f));
        }

        XPAnimationController.updateGlow(state, gaining, leveledUp, dt);

        if (leveledUp) XPAnimationController.triggerPulse(state);
        XPAnimationController.updatePulse(state, dt);

        if (xpLevelUpParticlesEnabled && leveledUp && !spawnedParticlesThisLoop) {
            int every = Math.max(1, xpLevelUpParticleLevels);

            if (displayedLevel % every == 0) particles.spawn(91, 18);
            spawnedParticlesThisLoop = true;
        }
        particles.update(dt * 20.0f);
    }

    private void renderXpBar(GuiGraphicsExtractor graphics) {
        int x = 0;
        int y = 20;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND, x, y, 182, 5);

        int filled = (int) (state.animatedProgress() * 183.0f);
        if (filled > 0) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_PROGRESS, 182, 5, 0, 0, x, y, filled, 5);

        XPAnimationRenderer.renderBarPulse(graphics, x, y, state);
        XPAnimationRenderer.renderFrontGlow(graphics, x, y, state);
        particles.render(graphics);
        renderLevelText(graphics, y);


    }
    private void renderLevelText(GuiGraphicsExtractor graphics, int barY) {
        String text = String.valueOf(displayedLevel);
        int width = minecraft.font.width(text);
        int x = (182 - width) / 2;
        int y = barY - 13;

        XPAnimationRenderer.renderLevelText(graphics, minecraft.font, text, x, y, state, (drawX, drawY) -> {
            graphics.text(minecraft.font, text, drawX + 1, drawY, 0xFF000000, false);
            graphics.text(minecraft.font, text, drawX - 1, drawY, 0xFF000000, false);
            graphics.text(minecraft.font, text, drawX, drawY + 1, 0xFF000000, false);
            graphics.text(minecraft.font, text, drawX, drawY - 1, 0xFF000000, false);
            graphics.text(minecraft.font, text, drawX, drawY, 0xFF80FF20, false);
        });
    }

    @Override
    public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
        float dt = frameDelta();
        update(dt);

        int naturalWidth = 182;
        int naturalHeight = 32;
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

        renderXpBar(graphics);

        graphics.pose().popMatrix();
        return boxHeight;
    }

    @Override
    public void close() {}
}