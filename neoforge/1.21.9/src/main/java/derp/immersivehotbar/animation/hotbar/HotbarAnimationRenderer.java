package derp.immersivehotbar.animation.hotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class HotbarAnimationRenderer {
    private HotbarAnimationRenderer() {}

    public static void withItemScale(GuiGraphics context, int x, int y, float scale, Runnable draw) {
        int centerX = x + 8;
        int centerY = y + 8;

        context.pose().pushMatrix();
        context.pose().translate(centerX, centerY);
        context.pose().scale(scale, scale);
        context.pose().translate(-centerX, -centerY);
        draw.run();
        context.pose().popMatrix();
    }

    public static void drawSlotEffects(GuiGraphics context, ItemStack stack, int x, int y, boolean selected) {
        drawBackground(context, x, y, selected);
        drawDurabilityGlow(context, stack, x, y);
    }

    public static void renderShrinkingItems(Minecraft minecraft, GuiGraphics context, HotbarAnimationState state, float deltaSeconds) {
        if (!hotbarItemAnimationsEnabled || !shrinkOutOnEmptyEnabled) return;

        Player player = minecraft.player;
        if (player == null) return;

        int centerXBase = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() - 19;

        for (int i = 0; i < 9; i++) {
            if (!state.isShrinking(i)) continue;

            ItemStack stack = state.lastStack(i);
            if (stack.isEmpty()) continue;

            float progress = state.shrinkProgress(i) + deltaSeconds * shrinkAnimationSpeed;
            state.shrinkProgress(i, progress);

            float scale = HotbarAnimationEngine.shrinkOutScale(
                    nonSelectedItemSize,
                    progress,
                    bouncyAnimation
            );
            state.scale(i, scale);

            int x = (centerXBase - 91) + i * 20 + 3;
            withItemScale(context, x, y, scale, () -> {
                context.renderItem(player, stack, x, y, 0);
                context.renderItemDecorations(minecraft.font, stack, x, y);
            });

            if (progress >= 1.0f) {
                state.shrinking(i, false);
                state.lastStack(i, ItemStack.EMPTY);
                state.lastCount(i, 0);
                state.scale(i, nonSelectedItemSize);
            }
        }
    }

    private static void drawDurabilityGlow(GuiGraphics context, ItemStack stack, int x, int y) {
        if (!lowDurabilityGlow || !stack.isDamageableItem()) return;

        float percent = (float) stack.getDamageValue() / stack.getMaxDamage();
        if (percent < durabilityGlowThreshold) return;

        float intensity = Math.min((percent - durabilityGlowThreshold) / (1.0f - durabilityGlowThreshold), 1.0f);
        float baseAlpha = 0.6f + 0.4f * intensity;

        if (percent >= 0.95f) {
            baseAlpha += 0.3f * (float) Math.sin(System.currentTimeMillis() / 80.0);
        }

        float alpha = Math.clamp(baseAlpha, 0.0f, 1.0f);
        int redGlow = ((int) (alpha * 255) << 24) | 0xFF0000;

        context.pose().pushMatrix();
        context.pose().translate(0, 0);
        context.fill(x - 2, y - 2, x + 18, y, redGlow);
        context.fill(x - 2, y + 16, x + 18, y + 18, redGlow);
        context.fill(x - 2, y, x, y + 16, redGlow);
        context.fill(x + 16, y, x + 18, y + 16, redGlow);
        context.pose().popMatrix();
    }

    private static void drawBackground(GuiGraphics context, int x, int y, boolean selected) {
        if (showBackground == shouldShowBackground.ENABLED || (showBackground == shouldShowBackground.ONLY_WHEN_SELECTED && selected)) {
            context.pose().pushMatrix();
            context.pose().translate(0, 0);
            context.fill(x - 2, y - 2, x + 18, y + 18, hotbarSelectionColor.getRGB());
            context.pose().popMatrix();
        }
    }
}
