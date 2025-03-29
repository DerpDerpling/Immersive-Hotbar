package derp.interactivehotbar.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class InteractiveHotbarConfig extends MidnightConfig {
    public static final String MAIN = "main";
    @Entry(category = MAIN) public static ShouldItemGrowWhenSelected shouldItemGrowWhenSelected = ShouldItemGrowWhenSelected.ENABLED;
    public enum ShouldItemGrowWhenSelected {
        ENABLED, DISABLED
    }

    @Entry(category = MAIN, name = "Selected Item Slot Scale", isSlider = true, min = 0.1f, max = 2f, precision = 10) public static float selectedItemSize = 1.2f;
    @Entry(category = MAIN) public static ToolAnimates toolAnimates = ToolAnimates.DISABLED;
    public enum ToolAnimates {
        ENABLED, DISABLED
    }
    @Entry(category = MAIN) public static WeaponAnimates weaponAnimates = WeaponAnimates.DISABLED;
    public enum WeaponAnimates {
        ENABLED, DISABLED
    }
    @Entry(category = MAIN, name = "Pop Animation Scale", isSlider = true, min = 0.1f, max = 0.9f) public static float animationIntensity = 0.5f;

    @Entry(category = MAIN, name = "Unselected Item Slot Scale", isSlider = true, min = 0f, max = 1f, precision = 10) public static float nonSelectedItemSize = 1.0f;
    @Entry(category = MAIN, name = "Animation Smoothness", isSlider = true, min = 0f, max = 1f) public static float animationSpeed = 0.1f;

}
