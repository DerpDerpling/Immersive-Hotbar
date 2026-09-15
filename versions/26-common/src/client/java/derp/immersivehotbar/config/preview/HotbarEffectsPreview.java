package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.config.ImmersiveHotbarConfig;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.awt.*;

public final class HotbarEffectsPreview implements ImageRenderer {
    public enum Mode {
        DURABILITY_GLOW,
        BACKGROUND
    }

    private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");
    private static final Identifier GRASS_BLOCK_ICON = Identifier.fromNamespaceAndPath("immersive-hotbar", "textures/gui/grass_block.png");
    private static final Identifier IRON_PICKAXE_ICON = Identifier.withDefaultNamespace("textures/item/iron_pickaxe.png");
    private static final Identifier DIAMOND_SWORD_ICON = Identifier.withDefaultNamespace("textures/item/diamond_sword.png");
    private static final Identifier GOLDEN_APPLE_ICON = Identifier.withDefaultNamespace("textures/item/golden_apple.png");

    private final Minecraft minecraft = Minecraft.getInstance();
    private final HotbarEffectsPreviewState state;
    private final Mode mode;

    public HotbarEffectsPreview(HotbarEffectsPreviewState state, Mode mode) {
        this.state = state;
        this.mode = mode;
    }

    private void renderPreview(GuiGraphicsExtractor graphics) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 24);
        int selectedSlot = 4;
        int selectedX = selectedSlot * 20 + 3;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION, selectedSlot * 20 - 1, -1, 24, 24);

        if (mode == Mode.DURABILITY_GLOW) {
            float damagePercent = Mth.clamp(durabilityThreshold() + 0.1f, 0.9f, 0.98f);
            renderDurabilityGlow(graphics, damagePercent, selectedX, 3);
            graphics.blit(RenderPipelines.GUI_TEXTURED, IRON_PICKAXE_ICON, selectedX, 3, 0, 0, 16, 16, 16, 16);
            renderDurabilityBar(graphics, selectedX, 3, damagePercent);
            return;
        }

        renderBackgrounds(graphics, selectedSlot);
        renderIcon(graphics, GRASS_BLOCK_ICON, 63, 3, 64);
        renderIcon(graphics, DIAMOND_SWORD_ICON, selectedX, 3, 1);
        renderIcon(graphics, GOLDEN_APPLE_ICON, 103, 3, 3);
    }

    private void renderIcon(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int count) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, 16, 16, 16, 16);
        if (count <= 1) return;
        String amount = String.valueOf(count);
        graphics.text(minecraft.font, amount, x + 17 - minecraft.font.width(amount), y + 9, 0xFFFFFFFF, true);
    }

    private void renderDurabilityGlow(GuiGraphicsExtractor graphics, float damagePercent, int x, int y) {
        if (!durabilityGlowEnabled()) return;
        float threshold = durabilityThreshold();
        if (damagePercent < threshold) return;
        float intensity = Math.min((damagePercent - threshold) / Math.max(1.0f - threshold, 0.001f), 1.0f);
        float alpha = Mth.clamp(0.6f + 0.4f * intensity, 0.0f, 1.0f);
        int glow = ((int) (alpha * 255.0f) << 24) | 0xFF0000;
        graphics.fill(x - 2, y - 2, x + 18, y, glow);
        graphics.fill(x - 2, y + 16, x + 18, y + 18, glow);
        graphics.fill(x - 2, y, x, y + 16, glow);
        graphics.fill(x + 16, y, x + 18, y + 16, glow);
    }

    private void renderDurabilityBar(GuiGraphicsExtractor graphics, int x, int y, float damagePercent) {
        float durability = Mth.clamp(1.0f - damagePercent, 0.0f, 1.0f);
        int width = Math.max(1, Math.round(13.0f * durability));
        graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
        graphics.fill(x + 2, y + 13, x + 2 + width, y + 14, 0xFFFF5555);
    }

    private void renderBackgrounds(GuiGraphicsExtractor graphics, int selectedSlot) {
        ImmersiveHotbarConfig.shouldShowBackground backgroundMode = showBackground();
        if (backgroundMode == ImmersiveHotbarConfig.shouldShowBackground.DISABLED) return;
        Color color = selectionColor();

        for (int slot = 0; slot < 9; slot++) {
            boolean selected = slot == selectedSlot;
            if (backgroundMode == ImmersiveHotbarConfig.shouldShowBackground.ONLY_WHEN_SELECTED && !selected) continue;
            int x = slot * 20 + 3;
            graphics.fill(x - 2, 1, x + 18, 21, color.getRGB());
        }
    }

    private boolean durabilityGlowEnabled() {
        return state.durabilityGlowEnabledOpt != null ? state.durabilityGlowEnabledOpt.pendingValue() : true;
    }

    private float durabilityThreshold() {
        return state.durabilityGlowThresholdOpt != null ? state.durabilityGlowThresholdOpt.pendingValue() : 0.8f;
    }

    private ImmersiveHotbarConfig.shouldShowBackground showBackground() {
        return state.showBackgroundOpt != null ? state.showBackgroundOpt.pendingValue() : ImmersiveHotbarConfig.shouldShowBackground.ENABLED;
    }

    private Color selectionColor() {
        return state.selectionColorOpt != null ? state.selectionColorOpt.pendingValue() : new Color(255, 255, 255, 127);
    }

    public void reset() {}

    @Override
    public void tick() {}

    @Override
    public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
        int naturalWidth = 182;
        int naturalHeight = 24;
        int padding = 6;
        float scale = Math.min(1.0f, (renderWidth - padding * 2) / (float) naturalWidth);
        int scaledWidth = Math.round(naturalWidth * scale);
        int scaledHeight = Math.round(naturalHeight * scale);
        int boxHeight = scaledHeight + padding * 2;
        int startX = x + (renderWidth - scaledWidth) / 2;
        int startY = y + (boxHeight - scaledHeight) / 2;
        graphics.pose().pushMatrix();
        graphics.pose().translate(startX, startY);
        graphics.pose().scale(scale, scale);
        renderPreview(graphics);
        graphics.pose().popMatrix();
        return boxHeight;
    }

    @Override
    public void close() {}
}
