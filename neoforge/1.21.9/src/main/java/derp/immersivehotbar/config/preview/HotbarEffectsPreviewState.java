package derp.immersivehotbar.config.preview;

import derp.immersivehotbar.config.ImmersiveHotbarConfig;
import dev.isxander.yacl3.api.Option;

import java.awt.*;

public final class HotbarEffectsPreviewState {
    public Option<Boolean> durabilityGlowEnabledOpt;
    public Option<Float> durabilityGlowThresholdOpt;

    public Option<ImmersiveHotbarConfig.shouldShowBackground> showBackgroundOpt;
    public Option<Color> selectionColorOpt;
}