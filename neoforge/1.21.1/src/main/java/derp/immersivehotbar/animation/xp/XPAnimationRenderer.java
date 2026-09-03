package derp.immersivehotbar.animation.xp;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class XPAnimationRenderer {
    private XPAnimationRenderer() {}

    @FunctionalInterface
    public interface LevelTextDraw {
        int draw(int x, int y);
    }

    public static void renderFrontGlow(GuiGraphics graphics, int x, XPAnimationState state) {
        Minecraft minecraft = Minecraft.getInstance();
        renderFrontGlow(graphics, x, minecraft.getWindow().getGuiScaledHeight() - 32 + 3, state);
    }

    public static void renderFrontGlow(GuiGraphics graphics, int x, int y, XPAnimationState state) {
        if (!xpGlowEnabled || state.frontGlow() <= 0.001f) return;

        int glowRgb = rgb(xpGlowColor);
        int barWidth = 182;
        int barHeight = 5;

        int filled = (int) (state.glowHeadProgress() * (barWidth + 1.0f));
        if (filled <= 0) return;

        int frontX = x + filled - 1;

        graphics.pose().pushPose();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        float t = state.frontGlow();

        int coreAlpha = (int) (t * 140.0f) << 24;
        graphics.fill(frontX, y - 1, frontX + 2, y + barHeight + 1, coreAlpha | glowRgb);

        int halo1Alpha = (int) (t * 80.0f) << 24;
        graphics.fill(frontX - 2, y - 2, frontX + 4, y + barHeight + 2, halo1Alpha | glowRgb);

        int halo2Alpha = (int) (t * 45.0f) << 24;
        graphics.fill(frontX - 5, y - 4, frontX + 7, y + barHeight + 4, halo2Alpha | glowRgb);

        int tail = Math.max(0, glowTailPx);
        int strips = Math.max(1, glowTailStrips);

        for (int i = 0; i < strips; i++) {
            float k0 = (float) i / strips;
            float k1 = (float) (i + 1) / strips;

            int x0 = frontX - (int) (tail * k1);
            int x1 = frontX - (int) (tail * k0);
            int alpha = (int) (t * 70.0f * (1.0f - k0)) << 24;

            graphics.fill(x0, y - 1, x1, y + barHeight + 1, alpha | glowRgb);
        }

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();

        graphics.pose().popPose();
    }

    public static int renderLevelText(GuiGraphics graphics, Font font, String text, int x, int y, XPAnimationState state, LevelTextDraw draw) {
        float scale = xpTextPulseEnabled ? state.pulseScale() : 1.0f;

        if (scale <= 1.0f) return draw.draw(x, y);

        int width = font.width(text);
        float centerX = x + width / 2.0f;
        float centerY = y + 4.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-centerX, -centerY, 0.0f);

        int result = draw.draw(x, y);

        graphics.pose().popPose();
        return result;
    }

    private static int rgb(Color color) {
        return (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }
    public static void renderBarPulse(GuiGraphics graphics, int x, XPAnimationState state) {
        Minecraft minecraft = Minecraft.getInstance();
        renderBarPulse(graphics, x, minecraft.getWindow().getGuiScaledHeight() - 32 + 3, state);
    }

    public static void renderBarPulse(GuiGraphics graphics, int x, int y, XPAnimationState state) {
        if (!xpBarPulseEnabled || state.barPulse() <= 0.001f) return;

        float pulse = state.barPulse();
        float progress = 1.0f - pulse;

        int expand = 1 + Math.round(progress * 8.0f);
        int rgb = rgb(xpBarPulseColor);

        int alphaOuter = (int) (pulse * 45.0f) << 24;
        int alphaMiddle = (int) (pulse * 70.0f) << 24;
        int alphaInner = (int) (pulse * 100.0f) << 24;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        renderOutline(graphics, x - expand, y - expand, x + 182 + expand, y + 5 + expand, alphaOuter | rgb);
        renderOutline(graphics, x - expand / 2 - 1, y - expand / 2 - 1, x + 183 + expand / 2, y + 6 + expand / 2, alphaMiddle | rgb);
        renderOutline(graphics, x - 1, y - 1, x + 183, y + 6, alphaInner | rgb);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    private static void renderOutline(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top + 1, left + 1, bottom - 1, color);
        graphics.fill(right - 1, top + 1, right, bottom - 1, color);
    }
}