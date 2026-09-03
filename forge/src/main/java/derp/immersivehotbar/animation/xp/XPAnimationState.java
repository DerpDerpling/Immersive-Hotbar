package derp.immersivehotbar.animation.xp;

public final class XPAnimationState {
    private float animatedProgress = 0.0f;
    private float animatedTotal = 0.0f;
    private int lastLevel = -1;

    private float pulseScale = 1.0f;
    private float pulseTargetScale = 1.0f;

    private float frontGlow = 0.0f;
    private float lastRawProgress = 0.0f;
    private float glowHeadProgress = 0.0f;

    public float animatedProgress() {
        return animatedProgress;
    }

    public void animatedProgress(float value) {
        animatedProgress = value;
    }

    public float animatedTotal() {
        return animatedTotal;
    }

    public void animatedTotal(float value) {
        animatedTotal = value;
    }

    public int lastLevel() {
        return lastLevel;
    }

    public void lastLevel(int value) {
        lastLevel = value;
    }

    public float pulseScale() {
        return pulseScale;
    }

    public void pulseScale(float value) {
        pulseScale = value;
    }

    public float pulseTargetScale() {
        return pulseTargetScale;
    }

    public void pulseTargetScale(float value) {
        pulseTargetScale = value;
    }

    public float frontGlow() {
        return frontGlow;
    }

    public void frontGlow(float value) {
        frontGlow = value;
    }

    public float lastRawProgress() {
        return lastRawProgress;
    }

    public void lastRawProgress(float value) {
        lastRawProgress = value;
    }

    public float glowHeadProgress() {
        return glowHeadProgress;
    }

    public void glowHeadProgress(float value) {
        glowHeadProgress = value;
    }
    private float barPulse = 0.0f;

    public float barPulse() {
        return barPulse;
    }

    public void barPulse(float value) {
        barPulse = value;
    }

    public void reset() {
        animatedProgress = 0.0f;
        animatedTotal = 0.0f;
        lastLevel = -1;
        pulseScale = 1.0f;
        pulseTargetScale = 1.0f;
        frontGlow = 0.0f;
        lastRawProgress = 0.0f;
        glowHeadProgress = 0.0f;
        barPulse = 0.0f;
    }
}