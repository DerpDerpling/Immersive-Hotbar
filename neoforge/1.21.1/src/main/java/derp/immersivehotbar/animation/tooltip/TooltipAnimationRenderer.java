package derp.immersivehotbar.animation.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class TooltipAnimationRenderer {
    private TooltipAnimationRenderer() {}

    @FunctionalInterface
    public interface TooltipDraw {
        int draw(int x, int y, int color);
    }

    public static int render(GuiGraphics graphics, Minecraft minecraft, TooltipAnimationState state, Component text, int x, int y, int width, int color, TooltipDraw draw) {
        if (state.scale() <= 0.01f || state.fadeSeconds() <= 0.0f) {
            return draw.draw(x, y, color);
        }

        state.lastText(text);
        state.lastTextWidth(width);

        float fadeRatio = state.fadeRatio();
        if (fadeRatio < 0.1f) {
            return 0;
        }

        int drawY = resolveY(minecraft, y);
        int alpha = (int) (fadeRatio * 255.0f);
        int animatedColor = FastColor.ARGB32.color(alpha, color);

        PoseStack pose = graphics.pose();
        pose.pushPose();

        float centerX = x + width / 2.0f;
        float centerY = drawY + 4.0f;

        pose.translate(centerX, centerY, 0.0f);
        pose.scale(state.scale(), state.scale(), 1.0f);
        pose.translate(-centerX, -centerY, 0.0f);

        int result = draw.draw(x, drawY, animatedColor);

        pose.popPose();
        return result;
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
        int color = FastColor.ARGB32.color(alpha, 0xFFFFFF);

        PoseStack pose = graphics.pose();
        pose.pushPose();

        float centerX = x + width / 2.0f;
        float centerY = y + 4.0f;

        pose.translate(centerX, centerY, 0.0f);
        pose.scale(state.scale(), state.scale(), 1.0f);
        pose.translate(-centerX, -centerY, 0.0f);

        graphics.drawStringWithBackdrop(minecraft.font, text, x, y, width, color);

        pose.popPose();
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
