package derp.immersivehotbar.animation.hotbar;

import net.minecraft.util.Mth;


public final class HotbarAnimationEngine {
    private HotbarAnimationEngine() {}

    public static void animateScale(HotbarAnimationState state, int slot, float targetScale, float deltaSeconds, boolean bouncy, float stiffness, float damping, float speed) {
        state.ensureCapacity(slot);

        if (bouncy) {
            float scale = state.scale(slot);
            float velocity = state.velocity(slot);

            float force = (targetScale - scale) * stiffness;
            velocity += force * deltaSeconds * 60.0f;
            velocity *= (float) Math.pow(1.0f - damping, deltaSeconds * 60.0f);
            scale += velocity * deltaSeconds * 60.0f;

            state.scale(slot, scale);
            state.velocity(slot, velocity);
        } else {
            float factor = smoothingFactor(speed, deltaSeconds);
            float scale = state.scale(slot);
            state.scale(slot, scale + (targetScale - scale) * factor);
            state.velocity(slot, 0.0f);
        }
    }

    public static float shrinkOutScale(float baseScale, float progress, boolean bouncy) {
        float clamped = Mth.clamp(progress, 0.0f, 1.0f);
        float eased = 1.0f - clamped * clamped;

        if (!bouncy) return baseScale * eased;

        float bounce = (float) Math.sin(clamped * Math.PI) * 0.15f;
        return baseScale * eased + bounce;
    }

    private static float smoothingFactor(float speed, float deltaSeconds) {
        float clampedSpeed = Mth.clamp(speed, 0.0f, 0.9999f);
        return 1.0f - (float) Math.pow(1.0f - clampedSpeed, deltaSeconds * 60.0f);
    }
}
