package derp.immersivehotbar.config.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;

import java.awt.*;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpBarPulseLines;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpBarPulseSize;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpBarPulseSpeed;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpGlowClampTail;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpLevelUpParticleAmount;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpLevelUpParticleSize;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseReturnSpeed;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseScale;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.xpTextPulseSpeed;
import static derp.immersivehotbar.config.screen.ConfigUtil.*;

public final class EffectsConfigSection {
    private EffectsConfigSection() {
    }

    public static ConfigCategory create(Context context) {
        Option<Boolean> durabilityGlowEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.durability_glow"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.durability_glow.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> lowDurabilityGlow, v -> lowDurabilityGlow = v)
                .build();

        Option<Float> durabilityGlowThresholdOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.glow_threshold"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.glow_threshold.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.0f, 1.0f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.8f, () -> durabilityGlowThreshold, v -> durabilityGlowThreshold = v)
                .available(lowDurabilityGlow)
                .build();

        bindAvailability(durabilityGlowEnabledOpt, durabilityGlowThresholdOpt);

        Option<Boolean> xpGlowEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_enabled"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_enabled.desc"), context.xpBarPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpGlowEnabled, v -> xpGlowEnabled = v)
                .build();

        Option<Boolean> xpTextPulseOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_text_pulse"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_text_pulse.desc"), context.xpBarPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpTextPulseEnabled, v -> xpTextPulseEnabled = v)
                .build();

        Option<Color> xpGlowColorOpt = Option.<Color>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_color"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_color.desc"), context.xpBarPreview))
                .controller(o -> ColorControllerBuilder.create(o).allowAlpha(true))
                .binding(new Color(255, 255, 85, 255), () -> xpGlowColor, v -> xpGlowColor = v)
                .available(xpGlowEnabled)
                .build();

        Option<Float> xpGlowFadeSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_fade_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_fade_speed.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.02f, 0.40f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.04f, () -> xpGlowFadeSpeed, v -> xpGlowFadeSpeed = v)
                .available(xpGlowEnabled)
                .build();
        Option<Float> xpTextPulseScaleOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_text_pulse_scale"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_text_pulse_scale.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(1.0f, 3.0f).step(0.05f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(2.0f, () -> xpTextPulseScale, v -> xpTextPulseScale = v)
                .available(xpTextPulseEnabled)
                .build();

        Option<Float> xpTextPulseSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_text_pulse_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_text_pulse_speed.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 5.0f).step(0.1f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(1.5f, () -> xpTextPulseSpeed, v -> xpTextPulseSpeed = v)
                .available(xpTextPulseEnabled)
                .build();

        Option<Float> xpTextPulseReturnSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_text_pulse_return_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_text_pulse_return_speed.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.01f, 1.0f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.1f, () -> xpTextPulseReturnSpeed, v -> xpTextPulseReturnSpeed = v)
                .available(xpTextPulseEnabled)
                .build();
        Option<Boolean> xpGlowClampTailOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_clamp_tail"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_clamp_tail.desc"), context.xpBarPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpGlowClampTail, v -> xpGlowClampTail = v)
                .available(xpGlowEnabled)
                .build();
        Option<Integer> xpParticlesAmountOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles_amount"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_levelup_particles_amount.desc"), context.xpBarPreview))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 500).step(1))
                .binding(25, () -> xpLevelUpParticleAmount, v -> xpLevelUpParticleAmount = v)
                .available(xpLevelUpParticlesEnabled)
                .build();
        Option<Integer> xpBarPulseLinesOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_pulse_lines"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_pulse_lines.desc"), context.xpBarPreview))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 8).step(1))
                .binding(3, () -> xpBarPulseLines, v -> xpBarPulseLines = v)
                .available(xpBarPulseEnabled)
                .build();

        Option<Float> xpParticlesSizeOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles_size"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_levelup_particles_size.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(1f, 8.0f).step(0.25f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(2.5f, () -> xpLevelUpParticleSize, v -> xpLevelUpParticleSize = v)
                .available(xpLevelUpParticlesEnabled)
                .build();
        Option<Float> xpBarPulseSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_pulse_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_pulse_speed.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.01f, 1.0f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.08f, () -> xpBarPulseSpeed, v -> xpBarPulseSpeed = v)
                .available(xpBarPulseEnabled)
                .build();

        Option<Float> xpBarPulseSizeOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_pulse_size"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_pulse_size.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.0f, 32.0f).step(1.0f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(8.0f, () -> xpBarPulseSize, v -> xpBarPulseSize = v)
                .available(xpBarPulseEnabled)
                .build();

        Option<Float> xpGlowBoostOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_boost_on_gain"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_boost_on_gain.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.05f, 1.00f).step(0.05f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.35f, () -> xpGlowBoostOnGain, v -> xpGlowBoostOnGain = v)
                .available(xpGlowEnabled)
                .build();

        Option<Integer> xpGlowTailPxOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_tail_px"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_tail_px.desc"), context.xpBarPreview))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(0, 60).step(1))
                .binding(18, () -> glowTailPx, v -> glowTailPx = v)
                .available(xpGlowEnabled)
                .build();

        Option<Integer> xpGlowTailStripsOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_tail_strips"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_glow_tail_strips.desc"), context.xpBarPreview))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 16).step(1))
                .binding(6, () -> glowTailStrips, v -> glowTailStrips = v)
                .available(xpGlowEnabled)
                .build();
        Option<Boolean> xpBarPulseOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_pulse"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_pulse.desc"), context.xpBarPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpBarPulseEnabled, v -> xpBarPulseEnabled = v)
                .build();

        Option<Integer> xpBarPulseEveryOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_pulse_every"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_pulse_every.desc"), context.xpBarPreview))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 30).step(1))
                .binding(1, () -> xpBarPulseLevels, v -> xpBarPulseLevels = v)
                .available(xpBarPulseEnabled)
                .build();

        Option<Color> xpBarPulseColorOpt = Option.<Color>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_pulse_color"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_pulse_color.desc"), context.xpBarPreview))
                .controller(o -> ColorControllerBuilder.create(o).allowAlpha(true))
                .binding(new Color(255, 255, 85, 255), () -> xpBarPulseColor, v -> xpBarPulseColor = v)
                .available(xpBarPulseEnabled)
                .build();

        bindAvailability(
                xpBarPulseOpt,
                xpBarPulseEveryOpt,
                xpBarPulseColorOpt,
                xpBarPulseSpeedOpt,
                xpBarPulseSizeOpt
        );
        bindAvailability(
                xpTextPulseOpt,
                xpTextPulseScaleOpt,
                xpTextPulseSpeedOpt,
                xpTextPulseReturnSpeedOpt
        );

        bindAvailability(
                xpGlowEnabledOpt,
                xpGlowColorOpt,
                xpGlowFadeSpeedOpt,
                xpGlowBoostOpt,
                xpGlowTailPxOpt,
                xpGlowTailStripsOpt,
                xpGlowClampTailOpt
        );

        Option<Boolean> xpParticlesEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_levelup_particles.desc"), context.xpBarPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpLevelUpParticlesEnabled, v -> xpLevelUpParticlesEnabled = v)
                .build();

        Option<Color> xpParticlesColorOpt = Option.<Color>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles_color"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_levelup_particles_color.desc"), context.xpBarPreview))
                .controller(o -> ColorControllerBuilder.create(o).allowAlpha(true))
                .binding(new Color(255, 255, 85, 255), () -> xpLevelUpParticleColor, v -> xpLevelUpParticleColor = v)
                .available(xpLevelUpParticlesEnabled)
                .build();

        Option<Integer> xpParticlesEveryOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles_every"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_levelup_particles_every.desc"), context.xpBarPreview))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 30).step(1))
                .binding(5, () -> xpLevelUpParticleLevels, v -> xpLevelUpParticleLevels = v)
                .available(xpLevelUpParticlesEnabled)
                .build();

        bindAvailability(xpParticlesEnabledOpt, xpParticlesColorOpt, xpParticlesEveryOpt);

        bindPreviewReset(
                context.xpBarPreview::reset,
                context.animatedXpBarOpt,
                context.xpBarSpeedOpt,
                xpTextPulseOpt,
                xpTextPulseScaleOpt,
                xpTextPulseSpeedOpt,
                xpTextPulseReturnSpeedOpt,
                xpGlowEnabledOpt,
                xpGlowColorOpt,
                xpGlowFadeSpeedOpt,
                xpGlowBoostOpt,
                xpGlowTailPxOpt,
                xpGlowTailStripsOpt,
                xpGlowClampTailOpt,
                xpParticlesEnabledOpt,
                xpParticlesColorOpt,
                xpParticlesEveryOpt,
                xpParticlesAmountOpt,
                xpParticlesSizeOpt,
                xpBarPulseOpt,
                xpBarPulseEveryOpt,
                xpBarPulseColorOpt,
                xpBarPulseSpeedOpt,
                xpBarPulseSizeOpt
        );

        return ConfigCategory.createBuilder()
                .name(Component.translatable("immersivehotbar.category.effects"))
                .tooltip(Component.translatable("immersivehotbar.tooltip.effects"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.durability_glow"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.durability_glow.desc")))
                        .option(durabilityGlowEnabledOpt)
                        .option(durabilityGlowThresholdOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.xp_bar"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.xp_bar.desc")))
                        .option(context.animatedXpBarOpt)
                        .option(context.xpBarSpeedOpt)
                        .option(xpTextPulseOpt)
                        .option(xpTextPulseScaleOpt)
                        .option(xpTextPulseSpeedOpt)
                        .option(xpTextPulseReturnSpeedOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.xp_glow"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.xp_glow.desc")))
                        .option(xpGlowEnabledOpt)
                        .option(xpGlowColorOpt)
                        .option(xpGlowFadeSpeedOpt)
                        .option(xpGlowBoostOpt)
                        .option(xpGlowTailPxOpt)
                        .option(xpGlowTailStripsOpt)
                        .option(xpGlowClampTailOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.xp_particles"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.xp_particles.desc")))
                        .option(xpParticlesEnabledOpt)
                        .option(xpParticlesColorOpt)
                        .option(xpParticlesEveryOpt)
                        .option(xpParticlesAmountOpt)
                        .option(xpParticlesSizeOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.xp_pulse"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.xp_pulse.desc")))
                        .option(xpBarPulseOpt)
                        .option(xpBarPulseEveryOpt)
                        .option(xpBarPulseLinesOpt)
                        .option(xpBarPulseColorOpt)
                        .option(xpBarPulseSpeedOpt)
                        .option(xpBarPulseSizeOpt)
                        .build())
                .build();
    }
}
