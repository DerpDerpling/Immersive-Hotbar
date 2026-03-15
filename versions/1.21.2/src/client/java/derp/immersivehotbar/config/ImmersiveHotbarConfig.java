package derp.immersivehotbar.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.awt.*;

public class ImmersiveHotbarConfig {

    @SerialEntry
    public static float selectedItemSize = 1.2f;

    @SerialEntry
    public static float bouncyStiffness = 0.3f;

    @SerialEntry
    public static float bouncyDamping = 0.2f;

    @SerialEntry
    public static boolean shouldItemGrowWhenSelected = true;

    @SerialEntry
    public static boolean durabilityAnimates = true;

    @SerialEntry
    public static boolean toolAnimates = false;

    @SerialEntry
    public static boolean weaponAnimates = false;

    @SerialEntry
    public static boolean bouncyAnimation = false;

    @SerialEntry
    public static boolean foodAnimates = true;

    @SerialEntry
    public static boolean toolsIgnoreBounce = false;

    @SerialEntry
    public static boolean weaponsIgnoreBounce = false;

    @SerialEntry
    public static boolean textScales = true;

    @SerialEntry
    public static Color hotbarSelectionColor = new Color(255, 255, 255, 127);

    @SerialEntry
    public static boolean lowDurabilityGlow = false;

    @SerialEntry
    public static boolean immersiveToolTip = true;

    @SerialEntry
    public static boolean tooltipYOffsetEnabled = false;

    @SerialEntry
    public static int tooltipYOffset = 52;

    @SerialEntry
    public static boolean scaleTooltipOffset = true;

    @SerialEntry
    public static float durabilityGlowThreshold = 0.8f;

    @SerialEntry
    public static shouldShowBackground showBackground = shouldShowBackground.DISABLED;

    @SerialEntry
    public static float animationIntensity = 0.5f;

    @SerialEntry
    public static float nonSelectedItemSize = 1.0f;

    @SerialEntry
    public static float shrinkAnimationSpeed = 2.5f;

    @SerialEntry
    public static float animationSpeed = 0.1f;


    @SerialEntry
    public static boolean hotbarItemAnimationsEnabled = true;

    @SerialEntry
    public static boolean vanillaItemBobbing = false;

    @SerialEntry
    public static boolean pickupAnimationsEnabled = true;

    @SerialEntry
    public static boolean useAnimationsEnabled = true;

    @SerialEntry
    public static boolean shrinkOutOnEmptyEnabled = true;

    @SerialEntry
    public static boolean offhandAnimationsEnabled = true;

    @SerialEntry
    public static boolean selectorScaleEnabled = true;


    @SerialEntry
    public static boolean animatedXpBar = true;

    @SerialEntry
    public static boolean xpTextPulseEnabled = true;

    @SerialEntry
    public static float xpBarSpeed = 1.0f;

    @SerialEntry
    public static boolean xpGlowEnabled = true;

    @SerialEntry
    public static Color xpGlowColor = new Color(255, 255, 85, 255);

    @SerialEntry
    public static float xpGlowFadeSpeed = 0.12f;

    @SerialEntry
    public static float xpGlowBoostOnGain = 0.35f;

    @SerialEntry
    public static int glowTailPx = 18;

    @SerialEntry
    public static int glowTailStrips = 6;

    @SerialEntry
    public static boolean xpLevelUpParticlesEnabled = true;

    @SerialEntry
    public static Color xpLevelUpParticleColor = new Color(255, 255, 85, 255);

    @SerialEntry
    public static int xpLevelUpParticleLevels = 5;




    public enum shouldShowBackground {
        DISABLED,
        ENABLED,
        ONLY_WHEN_SELECTED;

        @Override
        public String toString() {
            return switch (this) {
                case DISABLED -> "Disabled";
                case ENABLED -> "Always";
                case ONLY_WHEN_SELECTED -> "Only When Selected";
            };
        }
    }

}
