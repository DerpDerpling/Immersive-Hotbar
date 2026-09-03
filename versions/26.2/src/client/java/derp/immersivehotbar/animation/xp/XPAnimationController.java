package derp.immersivehotbar.animation.xp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

public final class XPAnimationController {
    private final XPAnimationState state = new XPAnimationState();
    private final XPParticleController particles = new XPParticleController();

    public XPAnimationState state() {
        return state;
    }

    public XPParticleController particles() {
        return particles;
    }
    private static final XPAnimationController HUD = new XPAnimationController();

    public static XPAnimationController hud() {
        return HUD;
    }

    public void reset() {
        state.reset();
        particles.clear();
    }

    public void update(LocalPlayer player, float dt) {
        int previousLevel = state.lastLevel();
        int currentLevel = player.experienceLevel;
        float targetXp = player.experienceProgress;

        if (previousLevel == -1) {
            state.lastLevel(currentLevel);
            state.animatedTotal(currentLevel + targetXp);
            state.animatedProgress(targetXp);
            state.glowHeadProgress(targetXp);
            state.lastRawProgress(targetXp);
            state.frontGlow(0.0f);
            state.pulseScale(1.0f);
            state.pulseTargetScale(1.0f);
            return;
        }

        boolean levelDecreased = currentLevel < previousLevel;
        float headTarget;

        if (!animatedXpBar) {
            state.animatedTotal(currentLevel + targetXp);
            state.animatedProgress(targetXp);
            headTarget = targetXp;
            state.glowHeadProgress(headTarget);
        } else {
            float targetTotal = currentLevel + targetXp;

            if (levelDecreased) {
                state.animatedTotal(targetTotal);
                state.animatedProgress(targetXp);
                headTarget = targetXp;
                state.glowHeadProgress(headTarget);
                state.frontGlow(0.0f);
                state.pulseScale(1.0f);
            } else {
                float deltaTotal = targetTotal - state.animatedTotal();

                if (Math.abs(deltaTotal) > 0.0001f) {
                    float animatedTotal = state.animatedTotal() + deltaTotal * Math.min(xpBarSpeed * dt, 1.0f);

                    if (Math.abs(targetTotal - animatedTotal) < 0.001f) animatedTotal = targetTotal;
                    state.animatedTotal(animatedTotal);
                }

                state.animatedProgress(state.animatedTotal() - (float) Math.floor(state.animatedTotal()));
                headTarget = state.animatedProgress();
            }
        }

        float follow = Math.min(1.0f, 12.0f * dt);
        state.glowHeadProgress(state.glowHeadProgress() + (headTarget - state.glowHeadProgress()) * follow);

        boolean leveledUp = currentLevel > previousLevel;

        if (currentLevel != state.lastLevel()) {
            if (xpTextPulseEnabled) {
                state.pulseScale(1.08f);
                state.pulseTargetScale(2.0f);
            } else {
                state.pulseScale(1.0f);
                state.pulseTargetScale(1.0f);
            }

            if (xpLevelUpParticlesEnabled) {
                int every = Math.max(1, xpLevelUpParticleLevels);

                if (currentLevel > 0 && currentLevel % every == 0) {
                    Minecraft minecraft = Minecraft.getInstance();
                    particles.spawn(minecraft.getWindow().getGuiScaledWidth() / 2, minecraft.getWindow().getGuiScaledHeight() - 32);
                }
            }

            state.lastLevel(currentLevel);
        }

        updatePulse(dt);
        if (leveledUp && xpBarPulseEnabled) {
            int every = Math.max(1, xpBarPulseLevels);
            if (currentLevel > 0 && currentLevel % every == 0) state.barPulse(1.0f);
        }
        boolean progressIncreased = targetXp > state.lastRawProgress() + 0.0005f;

        if (xpGlowEnabled) {
            float frontGlow = state.frontGlow();

            if (leveledUp || progressIncreased) {
                frontGlow = Math.min(1.0f, frontGlow + xpGlowBoostOnGain);
                if (leveledUp) frontGlow = 1.0f;
            }

            if (frontGlow > 0.0f) frontGlow = Math.max(0.0f, frontGlow - dt * xpGlowFadeSpeed);
            state.frontGlow(frontGlow);
        } else {
            state.frontGlow(0.0f);
        }
        if (state.barPulse() > 0.0f) {
            state.barPulse(Math.max(0.0f, state.barPulse() - dt * 0.08f));
        }

        state.lastRawProgress(targetXp);
    }

    private void updatePulse(float dt) {
        if (!xpTextPulseEnabled) {
            state.pulseScale(1.0f);
            state.pulseTargetScale(1.0f);
            return;
        }

        float scale = state.pulseScale();
        float target = state.pulseTargetScale();

        if (target > scale) {
            scale += (target - scale) * Math.min(dt * 1.5f, 1.0f);

            if (Math.abs(target - scale) < 0.02f) {
                scale = target;
                target = 1.0f;
            }
        } else if (scale > 1.0f) {
            scale -= dt * 0.1f;
            if (scale < 1.0f) scale = 1.0f;
        }

        state.pulseScale(scale);
        state.pulseTargetScale(target);
    }
    public static void animateProgress(XPAnimationState state, float targetTotal, float deltaSeconds) {
        if (!animatedXpBar) {
            state.animatedTotal(targetTotal);
        } else {
            float delta = targetTotal - state.animatedTotal();
            state.animatedTotal(state.animatedTotal() + delta * Math.min(xpBarSpeed * deltaSeconds * 20.0f, 1.0f));
        }

        state.animatedProgress(state.animatedTotal() - (float) Math.floor(state.animatedTotal()));
    }

    public static void updateGlowHead(XPAnimationState state, float targetProgress, float deltaSeconds) {
        float follow = Math.min(1.0f, 12.0f * deltaSeconds);
        state.glowHeadProgress(state.glowHeadProgress() + (targetProgress - state.glowHeadProgress()) * follow);
    }

    public static void updateGlow(XPAnimationState state, boolean gaining, boolean leveledUp, float deltaSeconds) {
        if (!xpGlowEnabled) {
            state.frontGlow(0.0f);
            return;
        }

        float glow = state.frontGlow();

        if (gaining) glow = Math.min(1.0f, glow + xpGlowBoostOnGain * deltaSeconds * 10.0f);
        else glow = Math.max(0.0f, glow - deltaSeconds * xpGlowFadeSpeed * 20.0f);

        if (leveledUp) glow = 1.0f;
        state.frontGlow(glow);
    }

    public static void triggerPulse(XPAnimationState state) {
        if (!xpTextPulseEnabled) return;

        state.pulseScale(1.08f);
        state.pulseTargetScale(2.0f);
    }

    public static void updatePulse(XPAnimationState state, float deltaSeconds) {
        if (!xpTextPulseEnabled) {
            state.pulseScale(1.0f);
            state.pulseTargetScale(1.0f);
            return;
        }

        float scale = state.pulseScale();
        float target = state.pulseTargetScale();

        if (target > scale) {
            scale += (target - scale) * Math.min(deltaSeconds * 1.5f * 20.0f, 1.0f);

            if (Math.abs(target - scale) < 0.02f) {
                scale = target;
                target = 1.0f;
            }
        } else if (scale > 1.0f) {
            scale -= deltaSeconds * 0.1f * 20.0f;
            if (scale < 1.0f) scale = 1.0f;
        }

        state.pulseScale(scale);
        state.pulseTargetScale(target);
    }
}