package derp.immersivehotbar.animation.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class TooltipAnimationRenderer {
    private TooltipAnimationRenderer() {}

    @FunctionalInterface
    public interface TooltipDraw {
        void draw(int x, int y, int color);
    }

    public static void render(GuiGraphics graphics, Minecraft minecraft, TooltipAnimationState state, Component text, int x, int y, int width, int color, TooltipDraw draw) {
        if (state.scale() <= 0.01f || state.fadeSeconds() <= 0.0f) {
            draw.draw(x, y, color);
            return;
        }

        state.lastText(text);
        state.lastTextWidth(width);

        float fadeRatio = state.fadeRatio();
        if (fadeRatio < 0.1f) return;

        int drawY = resolveY(minecraft, y);
        int alpha = (int) (fadeRatio * 255.0f);
        int animatedColor = ARGB.color(alpha, color);

        Matrix3x2fStack matrices = graphics.pose();
        float centerX = x + width / 2.0f;
        float centerY = drawY + 4.0f;

        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(state.scale(), state.scale());
        matrices.translate(-centerX, -centerY);

        draw.draw(x, drawY, animatedColor);

        matrices.popMatrix();
    }

    public static void renderPreview(GuiGraphics graphics, Minecraft minecraft, TooltipAnimationState state, Component text, int x, int y) {
        if (state.scale() <= 0.01f || state.fadeSeconds() <= 0.0f) {
            return;
        }

        float fadeRatio = state.fadeRatio();
        if (fadeRatio <= 0.05f) {
            return;
        }

        int width = minecraft.font.width(text);
        int alpha = (int) (fadeRatio * 255.0f);
        int color = ARGB.color(alpha, 0xFFFFFF);

        Matrix3x2fStack matrices = graphics.pose();

        float centerX = x + width / 2.0f;
        float centerY = y + 4.0f;

        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(state.scale(), state.scale());
        matrices.translate(-centerX, -centerY);

        graphics.drawStringWithBackdrop(minecraft.font, text, x, y, width, color);

        matrices.popMatrix();
    }

    private static int resolveY(Minecraft minecraft, int vanillaY) {
        if (!tooltipYOffsetEnabled) {
            return vanillaY;
        }

        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int drawY;
        if (scaleTooltipOffset) {
            drawY = screenHeight - Math.round(tooltipYOffset * (screenHeight / 240.0f));
        } else {
            drawY = screenHeight - tooltipYOffset;
        }

        if (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer()) {
            drawY += 14;
        }

        return drawY;
    }
}