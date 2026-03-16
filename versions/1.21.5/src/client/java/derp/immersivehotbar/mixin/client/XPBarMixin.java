package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.util.UIParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
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
@Mixin(Gui.class)
public class XPBarMixin {
    @Unique
    private final List<UIParticle> uiParticles = new ArrayList<>();
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

    @Unique
    private static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void renderXpFrontGlow(GuiGraphics context, int x, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !xpGlowEnabled) return;

        int glowRgb = rgb(xpGlowColor);
        int barWidth = 182;
        int barHeight = 5;
        int y = client.getWindow().getGuiScaledHeight() - 32 + 3;

        int filled = (int) (glowHeadProgress * (float) (barWidth + 1));
        if (filled <= 0) return;

        int frontX = x + filled - 1;
        float t = xpFrontGlow;
        if (t <= 0.001f) return;

        context.pose().pushPose();

        // Core
        context.fill(frontX, y - 1, frontX + 2, y + barHeight + 1, argb((int) (t * 140), glowRgb));

        // Halo 1
        context.fill(frontX - 2, y - 2, frontX + 4, y + barHeight + 2, argb((int) (t * 80), glowRgb));

        // Halo 2
        context.fill(frontX - 5, y - 4, frontX + 7, y + barHeight + 4, argb((int) (t * 45), glowRgb));

        int tail = Math.max(0, glowTailPx);
        int strips = Math.max(1, glowTailStrips);

        for (int i = 0; i < strips; i++) {
            float k0 = (float) i / (float) strips;
            float k1 = (float) (i + 1) / (float) strips;

            int x0 = frontX - (int) (tail * k1);
            int x1 = frontX - (int) (tail * k0);

            float local = 1f - k0;
            int alpha = (int) (t * 70f * local);

            context.fill(x0, y - 1, x1, y + barHeight + 1, argb(alpha, glowRgb));
        }

        context.pose().popPose();
    }

    @Unique
    public void tickXPAnimation() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        float dt = client.getDeltaTracker().getGameTimeDeltaTicks();

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

    @WrapOperation(
            method = "renderExperienceLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"
            )
    )
    private int wrapDrawLevelText(GuiGraphics context, Font textRenderer, String text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.experienceLevel <= 0) {
            return original.call(context, textRenderer, text, x, y, color, shadow);
        }

        float scale = xpTextPulseEnabled ? pulseScale : 1.0f;

        if (scale > 1.0f) {
            context.pose().pushPose();

            int textWidth = textRenderer.width(text);
            float centerX = x + textWidth / 2f;
            float centerY = y + 4f;

            context.pose().translate(centerX, centerY, 0);
            context.pose().scale(scale, scale, 1f);
            context.pose().translate(-centerX, -centerY, 0);
        }

        int result = original.call(context, textRenderer, text, x, y, color, shadow);

        if (scale > 1.0f) {
            context.pose().popPose();
        }

        return result;
    }

    @Unique
    private void spawnUIParticles() {
        int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 32;

        for (int i = 0; i < 25; i++) {
            UIParticle p = new UIParticle(x, y);

            float variance = 0.10f;
            p.tintR = 1.0f + (float) ((Math.random() * 2 - 1) * variance);
            p.tintG = 1.0f + (float) ((Math.random() * 2 - 1) * variance);
            p.tintB = 1.0f + (float) ((Math.random() * 2 - 1) * variance);

            uiParticles.add(p);
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("TAIL"))
    private void renderParticles(GuiGraphics context, int x, CallbackInfo ci) {
        if (uiParticles.isEmpty()) return;

        Iterator<UIParticle> iterator = uiParticles.iterator();
        while (iterator.hasNext()) {
            UIParticle p = iterator.next();

            if (!p.tick()) {
                iterator.remove();
                continue;
            }

            context.pose().pushPose();
            context.pose().translate(p.x, p.y, 0);

            float size = 2.5f;
            Color base = xpLevelUpParticleColor;

            int r = Math.min(255, Math.max(0, (int) (base.getRed() * p.tintR)));
            int g = Math.min(255, Math.max(0, (int) (base.getGreen() * p.tintG)));
            int b = Math.min(255, Math.max(0, (int) (base.getBlue() * p.tintB)));

            int rgb = (r << 16) | (g << 8) | b;
            int color = ((int) (p.alpha * 255) << 24) | rgb;

            context.fill((int) -size, (int) -size, (int) size, (int) size, color);
            context.pose().popPose();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void onRenderExperienceBar(GuiGraphics context, int x, CallbackInfo ci) {
        tickXPAnimation();
    }

    @Redirect(
            method = "renderExperienceBar",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/player/LocalPlayer;experienceProgress:F",
                    opcode = Opcodes.GETFIELD
            )
    )
    private float redirectExperienceProgress(LocalPlayer instance) {
        return animatedXpProgress;
    }
}