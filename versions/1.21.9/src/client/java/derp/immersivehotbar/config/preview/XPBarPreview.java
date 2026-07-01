package derp.immersivehotbar.config.preview;


import derp.immersivehotbar.util.UIParticle;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class XPBarPreview implements ImageRenderer {
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND =
            ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS =
            ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final List<UIParticle> particles = new ArrayList<>();

    private long lastFrameTime = System.nanoTime();

    private float loopTimer = 0.0f;
    private float animatedXpTotal = 4.15f;
    private float animatedXpProgress = 0.15f;
    private float targetXpTotal = 4.15f;

    private float glowHeadProgress = 0.15f;
    private boolean spawnedParticlesThisLoop = false;
    private int displayedLevel = 4;
    private float xpFrontGlow = 0.0f;

    private float pulseScale = 1.0f;
    private float pulseTargetScale = 1.0f;

    public void reset() {
        lastFrameTime = System.nanoTime();
        loopTimer = 0.0f;
        animatedXpTotal = 4.15f;
        animatedXpProgress = 0.15f;
        targetXpTotal = 4.15f;
        glowHeadProgress = 0.15f;
        xpFrontGlow = 0.0f;
        displayedLevel = 4;
        pulseScale = 1.0f;
        pulseTargetScale = 1.0f;
        spawnedParticlesThisLoop = false;
        particles.clear();
    }

    private float frameDelta() {
        long now = System.nanoTime();
        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        return Mth.clamp(dt, 0.0f, 0.05f);
    }

    private static int rgb(Color c) {
        return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
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

        int oldLevel = (int) Math.floor(animatedXpTotal);
        updateTarget();

        if (animatedXpBar) {
            float delta = targetXpTotal - animatedXpTotal;
            animatedXpTotal += delta * Math.min(xpBarSpeed * dt * 20.0f, 1.0f);
        } else {
            animatedXpTotal = targetXpTotal;
        }

        animatedXpProgress = animatedXpTotal - (float) Math.floor(animatedXpTotal);

        float follow = Math.min(1.0f, 12.0f * dt);
        glowHeadProgress += (animatedXpProgress - glowHeadProgress) * follow;

        int newLevel = (int) Math.floor(targetXpTotal);
        boolean leveledUp = newLevel > oldLevel && loopTimer > 2.25f && loopTimer < 2.45f;

        if (xpGlowEnabled) {
            boolean gaining = targetXpTotal > animatedXpTotal + 0.002f;

            if (gaining) {
                xpFrontGlow = Math.min(1.0f, xpFrontGlow + xpGlowBoostOnGain * dt * 10.0f);
            } else {
                xpFrontGlow = Math.max(0.0f, xpFrontGlow - dt * xpGlowFadeSpeed * 20.0f);
            }

            if (leveledUp) {
                xpFrontGlow = 1.0f;
            }
        } else {
            xpFrontGlow = 0.0f;
        }

        if (xpTextPulseEnabled && leveledUp) {
            pulseScale = 1.08f;
            pulseTargetScale = 2.0f;
        }

        if (!xpTextPulseEnabled) {
            pulseScale = 1.0f;
            pulseTargetScale = 1.0f;
        } else {
            if (pulseTargetScale > pulseScale) {
                pulseScale += (pulseTargetScale - pulseScale) * Math.min(dt * 1.5f * 20.0f, 1.0f);
                if (Math.abs(pulseTargetScale - pulseScale) < 0.02f) {
                    pulseScale = pulseTargetScale;
                    pulseTargetScale = 1.0f;
                }
            } else if (pulseScale > 1.0f) {
                pulseScale -= dt * 0.1f * 20.0f;
                if (pulseScale < 1.0f) pulseScale = 1.0f;
            }
        }

        if (xpLevelUpParticlesEnabled && leveledUp && !spawnedParticlesThisLoop) {
            spawnParticles();
            spawnedParticlesThisLoop = true;
        }

        Iterator<UIParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            UIParticle p = iterator.next();

            float particleDt = dt * 20.0f;

            if (!p.tick(particleDt)) {
                iterator.remove();
            }
        }
    }

    private void spawnParticles() {
        int every = Math.max(1, xpLevelUpParticleLevels);
        if (displayedLevel % every != 0) return;

        for (int i = 0; i < 25; i++) {
            UIParticle p = new UIParticle(91, 18);

            float variance = 0.10f;
            p.tintR = 1.0f + (float) ((Math.random() * 2 - 1) * variance);
            p.tintG = 1.0f + (float) ((Math.random() * 2 - 1) * variance);
            p.tintB = 1.0f + (float) ((Math.random() * 2 - 1) * variance);

            particles.add(p);
        }
    }

    private void renderXpBar(GuiGraphics gfx) {
        int x = 0;
        int y = 20;

        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND, x, y, 182, 5);

        int filled = (int) (animatedXpProgress * 183.0f);
        if (filled > 0) {
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_PROGRESS, 182, 5, 0, 0, x, y, filled, 5);
        }

        renderGlow(gfx, x, y);
        renderParticles(gfx);
        renderLevelText(gfx, y);
    }

    private void renderGlow(GuiGraphics gfx, int x, int y) {
        if (!xpGlowEnabled || xpFrontGlow <= 0.001f) return;

        int glowRgb = rgb(xpGlowColor);
        int filled = (int) (glowHeadProgress * 183.0f);
        if (filled <= 0) return;

        int frontX = x + filled - 1;
        int barHeight = 5;


        float t = xpFrontGlow;

        gfx.fill(frontX, y - 1, frontX + 2, y + barHeight + 1, ((int) (t * 140) << 24) | glowRgb);
        gfx.fill(frontX - 2, y - 2, frontX + 4, y + barHeight + 2, ((int) (t * 80) << 24) | glowRgb);
        gfx.fill(frontX - 5, y - 4, frontX + 7, y + barHeight + 4, ((int) (t * 45) << 24) | glowRgb);

        int tail = Math.max(0, glowTailPx);
        int strips = Math.max(1, glowTailStrips);

        for (int i = 0; i < strips; i++) {
            float k0 = (float) i / strips;
            float k1 = (float) (i + 1) / strips;

            int x0 = frontX - (int) (tail * k1);
            int x1 = frontX - (int) (tail * k0);

            int alpha = (int) (t * 70.0f * (1.0f - k0)) << 24;
            gfx.fill(x0, y - 1, x1, y + barHeight + 1, alpha | glowRgb);
        }
    }

    private void renderLevelText(GuiGraphics gfx, int barY) {
        String text = String.valueOf(displayedLevel);
        int textWidth = minecraft.font.width(text);
        int x = (182 - textWidth) / 2;
        int y = barY - 13;

        if (pulseScale > 1.0f) {
            Matrix3x2fStack matrices = gfx.pose();
            matrices.pushMatrix();

            float centerX = x + textWidth / 2.0f;
            float centerY = y + 4.0f;

            matrices.translate(centerX, centerY);
            matrices.scale(pulseScale, pulseScale);
            matrices.translate(-centerX, -centerY);

            gfx.drawString(minecraft.font, text, x + 1, y, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x - 1, y, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x, y + 1, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x, y - 1, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x, y, 0xFF80FF20, false);

            matrices.popMatrix();
        } else {
            gfx.drawString(minecraft.font, text, x + 1, y, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x - 1, y, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x, y + 1, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x, y - 1, 0xFF000000, false);
            gfx.drawString(minecraft.font, text, x, y, 0xFF80FF20, false);
        }
    }

    private void renderParticles(GuiGraphics gfx) {
        if (particles.isEmpty()) return;

        Color base = xpLevelUpParticleColor;

        for (UIParticle p : particles) {
            int r = Math.min(255, Math.max(0, (int) (base.getRed() * p.tintR)));
            int g = Math.min(255, Math.max(0, (int) (base.getGreen() * p.tintG)));
            int b = Math.min(255, Math.max(0, (int) (base.getBlue() * p.tintB)));

            int color = ((int) (p.alpha * 255) << 24) | (r << 16) | (g << 8) | b;

            gfx.fill((int) (p.x - 2.5f), (int) (p.y - 2.5f), (int) (p.x + 2.5f), (int) (p.y + 2.5f), color);
        }

    }

    @Override
    public void tick() {
    }

    @Override
    public int render(GuiGraphics gfx, int x, int y, int renderWidth, float tickDelta) {
        float dt = frameDelta();
        update(dt);

        int naturalWidth = 182;
        int naturalHeight = 32;
        int padding = 6;

        float previewScale = Math.min(1.0f, (renderWidth - padding * 2) / (float) naturalWidth);
        int boxH = Math.round(naturalHeight * previewScale) + padding * 2;

        int scaledWidth = Math.round(naturalWidth * previewScale);
        int scaledHeight = Math.round(naturalHeight * previewScale);

        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxH - scaledHeight) / 2;

        Matrix3x2fStack matrices = gfx.pose();
        matrices.pushMatrix();
        matrices.translate(startX, startY);
        matrices.scale(previewScale, previewScale);

        renderXpBar(gfx);

        matrices.popMatrix();

        return boxH;
    }

    @Override
    public void close() {
    }
}