package derp.immersivehotbar.config.preview;

import dev.isxander.yacl3.api.Option;

public final class ItemAnimationPreviewState {
    public Option<Boolean> shouldGrowOpt;
    public Option<Float> selectedScaleOpt;
    public Option<Float> unselectedScaleOpt;
    public Option<Boolean> textScalingOpt;

    public Option<Boolean> bouncyEnabledOpt;
    public Option<Float> bounceStiffnessOpt;
    public Option<Float> bounceDampingOpt;

    public Option<Float> animationIntensityOpt;
    public Option<Float> animationSpeedOpt;
    public Option<Float> shrinkSpeedOpt;

    public Option<Boolean> pickupAnimationsEnabledOpt;
    public Option<Boolean> useAnimationsEnabledOpt;
    public Option<Boolean> shrinkOutOnEmptyEnabledOpt;
    public Option<Boolean> selectorScaleEnabledOpt;

    public Option<Boolean> toolAnimatesOpt;
    public Option<Boolean> weaponAnimatesOpt;
    public Option<Boolean> durabilityAnimatesOpt;
    public Option<Boolean> foodAnimatesOpt;
    public Option<Boolean> vanillaItemBobbingOpt;
}