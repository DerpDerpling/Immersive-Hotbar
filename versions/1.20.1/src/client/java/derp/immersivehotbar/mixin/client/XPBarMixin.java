package derp.immersivehotbar.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import derp.immersivehotbar.util.UIParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class XPBarMixin {
    @Unique
    private final List<UIParticle> uiParticles = new ArrayList<>();
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;
    @Unique
    private float animatedXpProgress = 0f;
    @Unique
    private int lastLevel = -1;
    @Unique
    private float pulseScale = 1.0f;
    @Unique
    private float animatedXpTotal = 0f;
    @Unique
    private float xpFrontGlow = 0f;
    @Unique
    private float lastRawXpProgress = 0f;
    @Unique
    private float glowHeadProgress = 0f;
    @Unique
    private boolean xpGainThisFrame = false;
    @Unique
    private boolean leveledUpThisFrame = false;
    @Unique
    private float pulseTargetScale = 1.0f;

    @Unique
    private static int rgb(Color c) {
        return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void onRenderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        tickXPAnimation();
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void renderXpFrontGlow(DrawContext context, int x, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int glowRgb = rgb(xpGlowColor);
        int barWidth = 182;
        int barHeight = 5;
        int y = client.getWindow().getScaledHeight() - 32 + 3;

        int filled = (int) (glowHeadProgress * (float) (barWidth + 1));
        if (filled <= 0) return;

        int frontX = x + filled - 1;

        context.getMatrices().push();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

        float t = xpFrontGlow;
        if (xpGlowEnabled && t > 0.001f) {
            int a0 = (int) (t * 140) << 24;
            context.fill(frontX, y - 1, frontX + 2, y + barHeight + 1, a0 | glowRgb);

            int a1 = (int) (t * 80) << 24;
            context.fill(frontX - 2, y - 2, frontX + 4, y + barHeight + 2, a1 | glowRgb);

            int a2 = (int) (t * 45) << 24;
            context.fill(frontX - 5, y - 4, frontX + 7, y + barHeight + 4, a2 | glowRgb);

            int tail = Math.max(0, glowTailPx);
            int strips = Math.max(1, glowTailStrips);

            for (int i = 0; i < strips; i++) {
                float k0 = (float) i / (float) strips;
                float k1 = (float) (i + 1) / (float) strips;

                int x0 = frontX - (int) (tail * k1);
                int x1 = frontX - (int) (tail * k0);

                float local = 1f - k0;
                int at = (int) (t * 70f * local) << 24;

                context.fill(x0, y - 1, x1, y + barHeight + 1, at | glowRgb);
            }
        }

        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();

        context.getMatrices().pop();
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void renderParticles(DrawContext context, int x, CallbackInfo ci) {
        if (uiParticles.isEmpty()) return;

        RenderSystem.enableBlend();

        Iterator<UIParticle> iterator = uiParticles.iterator();
        while (iterator.hasNext()) {
            UIParticle p = iterator.next();

            if (!p.tick()) {
                iterator.remove();
                continue;
            }

            context.getMatrices().push();
            context.getMatrices().translate(p.x, p.y, 0);

            float size = 2.5f;
            Color base = xpLevelUpParticleColor;

            int r = Math.min(255, Math.max(0, (int) (base.getRed() * p.tintR)));
            int g = Math.min(255, Math.max(0, (int) (base.getGreen() * p.tintG)));
            int b = Math.min(255, Math.max(0, (int) (base.getBlue() * p.tintB)));

            int rgb = (r << 16) | (g << 8) | b;
            int color = ((int) (p.alpha * 255) << 24) | rgb;

            context.fill((int) -size, (int) -size, (int) size, (int) size, color);
            context.getMatrices().pop();
        }

        RenderSystem.disableBlend();
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", ordinal = 0, shift = At.Shift.BEFORE))
    private void pushXpLevelScale(DrawContext context, int x, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.experienceLevel <= 0) return;

        float scale = xpTextPulseEnabled ? pulseScale : 1.0f;
        if (scale <= 1.0f) return;

        String text = "" + client.player.experienceLevel;
        int textX = (this.scaledWidth - this.getTextRenderer().getWidth(text)) / 2;
        int textY = this.scaledHeight - 31 - 4;

        float centerX = textX + this.getTextRenderer().getWidth(text) / 2f;
        float centerY = textY + 4f;

        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.getMatrices().translate(-centerX, -centerY, 0.0F);
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", ordinal = 4, shift = At.Shift.AFTER))
    private void popXpLevelScale(DrawContext context, int x, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.experienceLevel <= 0) return;

        float scale = xpTextPulseEnabled ? pulseScale : 1.0f;
        if (scale <= 1.0f) return;

        context.getMatrices().pop();
    }

    @Unique
    public void tickXPAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float dt = client.getLastFrameDuration();

        xpGainThisFrame = false;
        leveledUpThisFrame = false;

        int prevLevel = lastLevel;
        int currentLevel = client.player.experienceLevel;
        float targetXp = client.player.experienceProgress;

        if (lastLevel == -1) {
            lastLevel = currentLevel;
            animatedXpTotal = currentLevel + targetXp;
            animatedXpProgress = targetXp;
            glowHeadProgress = targetXp;
            lastRawXpProgress = targetXp;
            xpFrontGlow = 0f;
            pulseScale = 1.0f;
            xpGainThisFrame = false;
            leveledUpThisFrame = false;
            return;
        }

        float headTarget;
        boolean levelDecreased = currentLevel < prevLevel;

        if (!animatedXpBar) {
            animatedXpTotal = currentLevel + targetXp;
            animatedXpProgress = targetXp;
            headTarget = targetXp;
            glowHeadProgress = headTarget;
        } else {
            float targetTotal = currentLevel + targetXp;

            if (levelDecreased) {
                animatedXpTotal = targetTotal;
                animatedXpProgress = targetXp;
                headTarget = targetXp;
                glowHeadProgress = headTarget;
                xpFrontGlow = 0f;
                pulseScale = 1.0f;
            } else {
                float deltaTotal = targetTotal - animatedXpTotal;
                if (Math.abs(deltaTotal) > 0.0001f) {
                    float speed = xpBarSpeed;
                    animatedXpTotal += deltaTotal * Math.min(speed * dt, 1f);
                }
                animatedXpProgress = animatedXpTotal - (float) Math.floor(animatedXpTotal);
                headTarget = animatedXpProgress;
            }
        }

        float follow = Math.min(1f, 12f * dt);
        glowHeadProgress += (headTarget - glowHeadProgress) * follow;

        boolean leveledUp = currentLevel > prevLevel;

        if (currentLevel != lastLevel) {
            if (xpTextPulseEnabled) {
                pulseScale = 1.08f;
                pulseTargetScale = 2.0f;
            } else {
                pulseScale = 1.0f;
                pulseTargetScale = 1.0f;
            }

            if (xpLevelUpParticlesEnabled) {
                int every = Math.max(1, xpLevelUpParticleLevels);
                if (currentLevel > 0 && currentLevel % every == 0) {
                    spawnUIParticles();
                }
            }

            lastLevel = currentLevel;
        }

        if (!xpTextPulseEnabled) {
            pulseScale = 1.0f;
            pulseTargetScale = 1.0f;
        } else {
            if (pulseTargetScale > pulseScale) {
                pulseScale += (pulseTargetScale - pulseScale) * Math.min(dt * 1.5f, 1f);

                if (Math.abs(pulseTargetScale - pulseScale) < 0.02f) {
                    pulseScale = pulseTargetScale;
                    pulseTargetScale = 1.0f;
                }
            } else if (pulseScale > 1.0f) {
                pulseScale -= dt * 0.1f;
                if (pulseScale < 1.0f) pulseScale = 1.0f;
            }
        }

        boolean progressIncreased = targetXp > lastRawXpProgress + 0.0005f;

        if (xpGlowEnabled) {
            if (leveledUp || progressIncreased) {
                xpGainThisFrame = progressIncreased;
                leveledUpThisFrame = leveledUp;

                xpFrontGlow = Math.min(1.0f, xpFrontGlow + xpGlowBoostOnGain);
                if (leveledUp) {
                    xpFrontGlow = 1.0f;
                }
            }

            if (xpFrontGlow > 0f) {
                xpFrontGlow = Math.max(0f, xpFrontGlow - dt * xpGlowFadeSpeed);
            }
        } else {
            xpFrontGlow = 0f;
        }

        lastRawXpProgress = targetXp;
    }

    @Unique
    private void spawnUIParticles() {
        int x = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int y = MinecraftClient.getInstance().getWindow().getScaledHeight() - 32;

        for (int i = 0; i < 25; i++) {
            UIParticle p = new UIParticle(x, y);

            float variance = 0.10f;
            p.tintR = 1.0f + (float) ((Math.random() * 2 - 1) * variance);
            p.tintG = 1.0f + (float) ((Math.random() * 2 - 1) * variance);
            p.tintB = 1.0f + (float) ((Math.random() * 2 - 1) * variance);

            uiParticles.add(p);
        }
    }

    @Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;experienceProgress:F", opcode = Opcodes.GETFIELD))
    private float redirectExperienceProgress(ClientPlayerEntity instance) {
        return animatedXpProgress;
    }
}