package derp.immersivehotbar.animation.xp;

import com.mojang.blaze3d.systems.RenderSystem;
import derp.immersivehotbar.util.UIParticle;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpLevelUpParticleColor;

public final class XPParticleController {
    private final List<UIParticle> particles = new ArrayList<>();

    public void spawn(int x, int y) {
        for (int i = 0; i < 25; i++) {
            UIParticle particle = new UIParticle(x, y);

            float variance = 0.10f;
            particle.tintR = 1.0f + (float) ((Math.random() * 2.0 - 1.0) * variance);
            particle.tintG = 1.0f + (float) ((Math.random() * 2.0 - 1.0) * variance);
            particle.tintB = 1.0f + (float) ((Math.random() * 2.0 - 1.0) * variance);

            particles.add(particle);
        }
    }

    public void update(float deltaTicks) {
        Iterator<UIParticle> iterator = particles.iterator();

        while (iterator.hasNext()) {
            if (!iterator.next().tick(deltaTicks)) iterator.remove();
        }
    }

    public void render(GuiGraphics graphics) {
        if (particles.isEmpty()) return;

        RenderSystem.enableBlend();

        Color base = xpLevelUpParticleColor;

        for (UIParticle particle : particles) {
            int r = Math.max(0, Math.min(255, (int) (base.getRed() * particle.tintR)));
            int g = Math.max(0, Math.min(255, (int) (base.getGreen() * particle.tintG)));
            int b = Math.max(0, Math.min(255, (int) (base.getBlue() * particle.tintB)));
            int color = ((int) (particle.alpha * 255.0f) << 24) | (r << 16) | (g << 8) | b;

            graphics.pose().pushPose();
            graphics.pose().translate(particle.x, particle.y, 0.0f);

            float size = 2.5f;
            graphics.fill((int) -size, (int) -size, (int) size, (int) size, color);

            graphics.pose().popPose();
        }

        RenderSystem.disableBlend();
    }

    public void clear() {
        particles.clear();
    }
}