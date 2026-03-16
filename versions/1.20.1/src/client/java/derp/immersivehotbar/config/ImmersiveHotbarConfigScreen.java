package derp.immersivehotbar.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import java.awt.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;

import derp.immersivehotbar.config.ImmersiveHotbarConfig.shouldShowBackground;

public class ImmersiveHotbarConfigScreen {

    private static void bindAvailability(Option<Boolean> controller, Option<?>... dependents) {
        boolean enabledInit = controller.pendingValue();
        for (Option<?> dep : dependents) dep.setAvailable(enabledInit);

        controller.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE || event == OptionEventListener.Event.INITIAL) {
                boolean enabled = controller.pendingValue();
                for (Option<?> dep : dependents) dep.setAvailable(enabled);
            }
        });
    }

    public static Screen create(Screen parent) {
        var builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("immersivehotbar.title"));

        //general category
        Option<Boolean> animatedXpBarOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.animated_xp_bar"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.animated_xp_bar.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> animatedXpBar, v -> animatedXpBar = v)
                .build();

        Option<Float> xpBarSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_speed"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_bar_speed.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 5.0f).step(0.1f))
                .binding(1.0f, () -> xpBarSpeed, v -> xpBarSpeed = v)
                .available(animatedXpBar)
                .build();

        bindAvailability(animatedXpBarOpt, xpBarSpeedOpt);

        Option<Boolean> tooltipYOffsetToggleOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_y_offset_toggle"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tooltip_y_offset_toggle.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> tooltipYOffsetEnabled, v -> tooltipYOffsetEnabled = v)
                .build();

        Option<Integer> tooltipYOffsetOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_y_offset"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tooltip_y_offset.desc")))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(40, 300).step(1))
                .binding(52, () -> tooltipYOffset, v -> tooltipYOffset = v)
                .available(tooltipYOffsetEnabled)
                .build();

        Option<Boolean> scaleTooltipOffsetOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.scale_tooltip_offset"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.scale_tooltip_offset.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> scaleTooltipOffset, v -> scaleTooltipOffset = v)
                .available(tooltipYOffsetEnabled)
                .build();

        bindAvailability(tooltipYOffsetToggleOpt, tooltipYOffsetOpt, scaleTooltipOffsetOpt);

        Option<Boolean> shouldGrowOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.should_grow"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.should_grow.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> shouldItemGrowWhenSelected, v -> shouldItemGrowWhenSelected = v)
                .build();

        Option<Float> selectedScaleOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.selected_scale"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.selected_scale.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 2.0f).step(0.1f))
                .binding(1.2f, () -> selectedItemSize, v -> selectedItemSize = v)
                .available(shouldItemGrowWhenSelected)
                .build();

        bindAvailability(shouldGrowOpt, selectedScaleOpt);

        builder.category(ConfigCategory.createBuilder()
                .name(Component.translatable("immersivehotbar.category.general"))
                .tooltip(Component.translatable("immersivehotbar.tooltip.general"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.item_scaling"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.item_scaling.desc")))
                        .option(shouldGrowOpt)
                        .option(selectedScaleOpt)
                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.unselected_scale"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.unselected_scale.desc")))
                                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 1.0f).step(0.1f))
                                .binding(1.0f, () -> nonSelectedItemSize, v -> nonSelectedItemSize = v)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.text_scaling"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.text_scaling.desc")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(true, () -> textScales, v -> textScales = v)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.tooltip"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.tooltip.desc")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.tooltip_animation"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tooltip_animation.desc")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(true, () -> immersiveToolTip, v -> immersiveToolTip = v)
                                .build())
                        .option(tooltipYOffsetToggleOpt)
                        .option(tooltipYOffsetOpt)
                        .option(scaleTooltipOffsetOpt)
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.background_style"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.background_style.desc")))
                        .option(Option.<shouldShowBackground>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.show_background"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.show_background.desc")))
                                .controller(o -> EnumControllerBuilder.create(o).enumClass(shouldShowBackground.class))
                                .binding(shouldShowBackground.DISABLED, () -> showBackground, v -> showBackground = v)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.selection_color"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.selection_color.desc")))
                                .controller(o -> ColorControllerBuilder.create(o).allowAlpha(true))
                                .binding(new Color(255, 255, 255, 127), () -> hotbarSelectionColor, v -> hotbarSelectionColor = v)
                                .build())
                        .build())

                .build());

        //animations category
        Option<Boolean> hotbarItemAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.hotbar_item_animations_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.hotbar_item_animations_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> hotbarItemAnimationsEnabled, v -> hotbarItemAnimationsEnabled = v)
                .build();

        Option<Boolean> disableHotbarItemBobbingOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.disable_hotbar_item_bobbing"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.disable_hotbar_item_bobbing.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> vanillaItemBobbing, v -> vanillaItemBobbing = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> pickupAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.pickup_animations_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.pickup_animations_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> pickupAnimationsEnabled, v -> pickupAnimationsEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> useAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.use_animations_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.use_animations_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> useAnimationsEnabled, v -> useAnimationsEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> shrinkOutOnEmptyEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.shrink_out_on_empty_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.shrink_out_on_empty_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> shrinkOutOnEmptyEnabled, v -> shrinkOutOnEmptyEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> offhandAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.offhand_animations_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.offhand_animations_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> offhandAnimationsEnabled, v -> offhandAnimationsEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> selectorScaleEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.selector_scale_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.selector_scale_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> selectorScaleEnabled, v -> selectorScaleEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();
        bindAvailability(
                hotbarItemAnimationsEnabledOpt,
                disableHotbarItemBobbingOpt,
                pickupAnimationsEnabledOpt,
                useAnimationsEnabledOpt,
                shrinkOutOnEmptyEnabledOpt,
                offhandAnimationsEnabledOpt,
                selectorScaleEnabledOpt
        );

        Option<Boolean> bouncyEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.bouncy_animation"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("immersivehotbar.option.bouncy_animation.desc"))
                        .webpImage(ResourceLocation.tryBuild("immersive-hotbar", "textures/gui/bouncy_animation.webp"))
                        .build())
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> bouncyAnimation, v -> bouncyAnimation = v)
                .build();

        Option<Float> bounceStiffnessOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.bounce_stiffness"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.bounce_stiffness.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 1f).step(0.1f))
                .binding(0.3f, () -> bouncyStiffness, v -> bouncyStiffness = v)
                .available(bouncyAnimation)
                .build();

        Option<Float> bounceDampingOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.bounce_damping"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.bounce_damping.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 0.8f).step(0.1f))
                .binding(0.2f, () -> bouncyDamping, v -> bouncyDamping = v)
                .available(bouncyAnimation)
                .build();

        Option<Boolean> toolsIgnoreBounceOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tools_ignore_bounce"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tools_ignore_bounce.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> toolsIgnoreBounce, v -> toolsIgnoreBounce = v)
                .available(bouncyAnimation)
                .build();

        Option<Boolean> weaponsIgnoreBounceOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.weapons_ignore_bounce"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.weapons_ignore_bounce.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> weaponsIgnoreBounce, v -> weaponsIgnoreBounce = v)
                .available(bouncyAnimation)
                .build();

        bindAvailability(bouncyEnabledOpt, bounceStiffnessOpt, bounceDampingOpt, toolsIgnoreBounceOpt, weaponsIgnoreBounceOpt);

        builder.category(ConfigCategory.createBuilder()
                .name(Component.translatable("immersivehotbar.category.animations"))
                .tooltip(Component.translatable("immersivehotbar.tooltip.animations"))


                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.item_animations"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.item_animations.desc")))
                        .option(hotbarItemAnimationsEnabledOpt)
                        .option(disableHotbarItemBobbingOpt)
                        .option(pickupAnimationsEnabledOpt)
                        .option(useAnimationsEnabledOpt)
                        .option(shrinkOutOnEmptyEnabledOpt)
                        .option(offhandAnimationsEnabledOpt)
                        .option(selectorScaleEnabledOpt)
                        .build())


                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.animation_triggers"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.animation_triggers.desc")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.tool_animates"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tool_animates.desc")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(false, () -> toolAnimates, v -> toolAnimates = v)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.weapon_animates"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.weapon_animates.desc")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(false, () -> weaponAnimates, v -> weaponAnimates = v)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.durability_animates"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.durability_animates.desc")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(true, () -> durabilityAnimates, v -> durabilityAnimates = v)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.food_animates"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.food_animates.desc")))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(true, () -> foodAnimates, v -> foodAnimates = v)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.animation_feel"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.animation_feel.desc")))
                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.pop_scale"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.pop_scale.desc")))
                                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 0.9f).step(0.1f))
                                .binding(0.5f, () -> animationIntensity, v -> animationIntensity = v)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.animation_smoothness"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.animation_smoothness.desc")))
                                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 1.0f).step(0.1f))
                                .binding(0.1f, () -> animationSpeed, v -> animationSpeed = v)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Component.translatable("immersivehotbar.option.shrink_speed"))
                                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.shrink_speed.desc")))
                                .controller(o -> FloatSliderControllerBuilder.create(o).range(1f, 5.0f).step(0.1f))
                                .binding(2.5f, () -> shrinkAnimationSpeed, v -> shrinkAnimationSpeed = v)
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.bouncy_animation"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.bouncy_animation.desc")))
                        .option(bouncyEnabledOpt)
                        .option(bounceStiffnessOpt)
                        .option(bounceDampingOpt)
                        .option(toolsIgnoreBounceOpt)
                        .option(weaponsIgnoreBounceOpt)
                        .build())

                .build());

        //effects category
        Option<Boolean> durabilityGlowEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.durability_glow"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.durability_glow.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> lowDurabilityGlow, v -> lowDurabilityGlow = v)
                .build();

        Option<Float> durabilityGlowThresholdOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.glow_threshold"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.glow_threshold.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.0f, 1.0f).step(0.01f))
                .binding(0.8f, () -> durabilityGlowThreshold, v -> durabilityGlowThreshold = v)
                .available(lowDurabilityGlow)
                .build();

        bindAvailability(durabilityGlowEnabledOpt, durabilityGlowThresholdOpt);

        Option<Boolean> xpGlowEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_enabled"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_glow_enabled.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpGlowEnabled, v -> xpGlowEnabled = v)
                .build();

        Option<Boolean> xpTextPulseOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_text_pulse"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_text_pulse.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpTextPulseEnabled, v -> xpTextPulseEnabled = v)
                .build();

        Option<Color> xpGlowColorOpt = Option.<Color>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_color"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_glow_color.desc")))
                .controller(o -> ColorControllerBuilder.create(o).allowAlpha(true))
                .binding(new Color(255, 255, 85, 255), () -> xpGlowColor, v -> xpGlowColor = v)
                .available(xpGlowEnabled)
                .build();

        Option<Float> xpGlowFadeSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_fade_speed"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_glow_fade_speed.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.02f, 0.40f).step(0.01f))
                .binding(0.12f, () -> xpGlowFadeSpeed, v -> xpGlowFadeSpeed = v)
                .available(xpGlowEnabled)
                .build();

        Option<Float> xpGlowBoostOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_boost_on_gain"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_glow_boost_on_gain.desc")))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.05f, 1.00f).step(0.05f))
                .binding(0.35f, () -> xpGlowBoostOnGain, v -> xpGlowBoostOnGain = v)
                .available(xpGlowEnabled)
                .build();

        Option<Integer> xpGlowTailPxOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_tail_px"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_glow_tail_px.desc")))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(0, 60).step(1))
                .binding(18, () -> glowTailPx, v -> glowTailPx = v)
                .available(xpGlowEnabled)
                .build();

        Option<Integer> xpGlowTailStripsOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_glow_tail_strips"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_glow_tail_strips.desc")))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 16).step(1))
                .binding(6, () -> glowTailStrips, v -> glowTailStrips = v)
                .available(xpGlowEnabled)
                .build();

        bindAvailability(xpGlowEnabledOpt, xpGlowColorOpt, xpGlowFadeSpeedOpt, xpGlowBoostOpt, xpGlowTailPxOpt, xpGlowTailStripsOpt);

        Option<Boolean> xpParticlesEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_levelup_particles.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> xpLevelUpParticlesEnabled, v -> xpLevelUpParticlesEnabled = v)
                .build();

        Option<Color> xpParticlesColorOpt = Option.<Color>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles_color"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_levelup_particles_color.desc")))
                .controller(o -> ColorControllerBuilder.create(o).allowAlpha(true))
                .binding(new Color(255, 255, 85, 255), () -> xpLevelUpParticleColor, v -> xpLevelUpParticleColor = v)
                .available(xpLevelUpParticlesEnabled)
                .build();

        Option<Integer> xpParticlesEveryOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_levelup_particles_every"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.xp_levelup_particles_every.desc")))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 30).step(1))
                .binding(5, () -> xpLevelUpParticleLevels, v -> xpLevelUpParticleLevels = v)
                .available(xpLevelUpParticlesEnabled)
                .build();

        bindAvailability(xpParticlesEnabledOpt, xpParticlesColorOpt, xpParticlesEveryOpt);

        builder.category(ConfigCategory.createBuilder()
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
                        .option(animatedXpBarOpt)
                        .option(xpBarSpeedOpt)
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
                .build());

        return builder
                .save(ImmersiveHotbarConfigHandler.HANDLER::save)
                .build()
                .generateScreen(parent);
    }
}
