package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.config.ImmersiveHotbarConfig;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.*;

public final class HotbarEffectsPreview implements ImageRenderer {
    public enum Mode {
        DURABILITY_GLOW,
        BACKGROUND
    }

    private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final HotbarEffectsPreviewState state;
    private final Mode mode;

    public HotbarEffectsPreview(HotbarEffectsPreviewState state, Mode mode) {
        this.state = state;
        this.mode = mode;
    }

    private void renderPreview(GuiGraphics graphics) {
        graphics.blitSprite(RenderType::guiTextured, HOTBAR, 0, 0, 182, 24);

        int selectedSlot = 4;
        int selectedX = selectedSlot * 20 + 3;

        graphics.blitSprite(RenderType::guiTextured, HOTBAR_SELECTION, selectedSlot * 20 - 1, -1, 24, 24);

        if (mode == Mode.DURABILITY_GLOW) {
            ItemStack stack = new ItemStack(Items.IRON_PICKAXE);

            float threshold = durabilityThreshold();
            float damagePercent = Math.clamp(threshold + 0.1f, 0.9f, 0.98f);

            stack.setDamageValue(Math.round(stack.getMaxDamage() * damagePercent));

            renderDurabilityGlow(graphics, stack, selectedX, 3);
            graphics.renderItem(stack, selectedX, 3);
            graphics.renderItemDecorations(minecraft.font, stack, selectedX, 3);
            return;
        }

        renderBackgrounds(graphics, selectedSlot);

        ItemStack left = new ItemStack(Items.GRASS_BLOCK, 64);
        ItemStack selected = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack right = new ItemStack(Items.GOLDEN_APPLE, 3);

        graphics.renderItem(left, 63, 3);
        graphics.renderItemDecorations(minecraft.font, left, 63, 3);

        graphics.renderItem(selected, selectedX, 3);
        graphics.renderItemDecorations(minecraft.font, selected, selectedX, 3);

        graphics.renderItem(right, 103, 3);
        graphics.renderItemDecorations(minecraft.font, right, 103, 3);
    }

    private void renderDurabilityGlow(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!durabilityGlowEnabled() || !stack.isDamageableItem()) return;

        float percent = (float) stack.getDamageValue() / stack.getMaxDamage();
        float threshold = durabilityThreshold();

        if (percent < threshold) return;

        float intensity = Math.min((percent - threshold) / Math.max(1.0f - threshold, 0.001f), 1.0f);
        float baseAlpha = 0.6f + 0.4f * intensity;
        float alpha = Math.clamp(baseAlpha, 0.0f, 1.0f);
        int glow = ((int) (alpha * 255.0f) << 24) | 0xFF0000;

        graphics.fill(x - 2, y - 2, x + 18, y, glow);
        graphics.fill(x - 2, y + 16, x + 18, y + 18, glow);
        graphics.fill(x - 2, y, x, y + 16, glow);
        graphics.fill(x + 16, y, x + 18, y + 16, glow);
    }

    private void renderBackgrounds(GuiGraphics graphics, int selectedSlot) {
        ImmersiveHotbarConfig.shouldShowBackground mode = showBackground();
        if (mode == ImmersiveHotbarConfig.shouldShowBackground.DISABLED) return;

        Color color = selectionColor();

        for (int slot = 0; slot < 9; slot++) {
            boolean selected = slot == selectedSlot;

            if (mode == ImmersiveHotbarConfig.shouldShowBackground.ONLY_WHEN_SELECTED && !selected) continue;

            int x = slot * 20 + 3;
            graphics.fill(x - 2, 1, x + 18, 21, color.getRGB());
        }
    }

    private boolean durabilityGlowEnabled() {
        return state.durabilityGlowEnabledOpt != null
                ? state.durabilityGlowEnabledOpt.pendingValue()
                : true;
    }

    private float durabilityThreshold() {
        return state.durabilityGlowThresholdOpt != null
                ? state.durabilityGlowThresholdOpt.pendingValue()
                : 0.8f;
    }

    private ImmersiveHotbarConfig.shouldShowBackground showBackground() {
        return state.showBackgroundOpt != null
                ? state.showBackgroundOpt.pendingValue()
                : ImmersiveHotbarConfig.shouldShowBackground.ENABLED;
    }

    private Color selectionColor() {
        return state.selectionColorOpt != null
                ? state.selectionColorOpt.pendingValue()
                : new Color(255, 255, 255, 127);
    }

    public void reset() {}

    @Override
    public void tick() {}

    @Override
    public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
        int naturalWidth = 182;
        int naturalHeight = 24;
        int padding = 6;

        float scale = Math.min(1.0f, (renderWidth - padding * 2) / (float) naturalWidth);
        int scaledWidth = Math.round(naturalWidth * scale);
        int scaledHeight = Math.round(naturalHeight * scale);
        int boxHeight = scaledHeight + padding * 2;

        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxHeight - scaledHeight) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(startX, startY, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);

        renderPreview(graphics);

        graphics.pose().popPose();
        return boxHeight;
    }

    @Override
    public void close() {}
}