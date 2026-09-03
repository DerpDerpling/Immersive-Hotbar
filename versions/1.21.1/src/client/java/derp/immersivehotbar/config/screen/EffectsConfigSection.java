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

import java.awt.Color;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.config.screen.ConfigUtil.bindAvailability;
import static derp.immersivehotbar.config.screen.ConfigUtil.bindPreviewReset;
import static derp.immersivehotbar.config.screen.ConfigUtil.previewed;

public final class EffectsConfigSection {
    private EffectsConfigSection() {
    }

    public static ConfigCategory create(ConfigUtil.Context context) {
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

        bindAvailability(xpBarPulseOpt, xpBarPulseEveryOpt, xpBarPulseColorOpt);

        bindAvailability(
                xpGlowEnabledOpt,
                xpGlowColorOpt,
                xpGlowFadeSpeedOpt,
                xpGlowBoostOpt,
                xpGlowTailPxOpt,
                xpGlowTailStripsOpt
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
                xpGlowEnabledOpt,
                xpGlowColorOpt,
                xpGlowFadeSpeedOpt,
                xpGlowBoostOpt,
                xpGlowTailPxOpt,
                xpGlowTailStripsOpt,
                xpParticlesEnabledOpt,
                xpParticlesColorOpt,
                xpParticlesEveryOpt
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
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.xp_particles"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.xp_particles.desc")))
                        .option(xpParticlesEnabledOpt)
                        .option(xpParticlesColorOpt)
                        .option(xpParticlesEveryOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.xp_pulse"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.xp_pulse.desc")))
                        .option(xpBarPulseOpt)
                        .option(xpBarPulseEveryOpt)
                        .option(xpBarPulseColorOpt)
                        .build())
                .build();
    }
}
