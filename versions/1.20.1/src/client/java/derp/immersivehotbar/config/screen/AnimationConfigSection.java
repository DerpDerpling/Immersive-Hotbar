package derp.immersivehotbar.config.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.config.screen.ConfigUtil.bindAvailability;
import static derp.immersivehotbar.config.screen.ConfigUtil.bindPreviewReset;
import static derp.immersivehotbar.config.screen.ConfigUtil.previewed;

public final class AnimationConfigSection {
    private AnimationConfigSection() {
    }

    public static ConfigCategory create(ConfigUtil.Context context) {
        Option<Boolean> hotbarItemAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.hotbar_item_animations_enabled"))
                .description(previewed(Component.translatable("immersivehotbar.option.hotbar_item_animations_enabled.desc"), context.pickupPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> hotbarItemAnimationsEnabled, v -> hotbarItemAnimationsEnabled = v)
                .build();

        Option<Boolean> disableHotbarItemBobbingOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.disable_hotbar_item_bobbing"))
                .description(previewed(Component.translatable("immersivehotbar.option.disable_hotbar_item_bobbing.desc"), context.pickupPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> vanillaItemBobbing, v -> vanillaItemBobbing = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> pickupAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.pickup_animations_enabled"))
                .description(previewed(Component.translatable("immersivehotbar.option.pickup_animations_enabled.desc"), context.pickupPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> pickupAnimationsEnabled, v -> pickupAnimationsEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> useAnimationsEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.use_animations_enabled"))
                .description(previewed(Component.translatable("immersivehotbar.option.use_animations_enabled.desc"), context.usePreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> useAnimationsEnabled, v -> useAnimationsEnabled = v)
                .available(hotbarItemAnimationsEnabled)
                .build();

        Option<Boolean> shrinkOutOnEmptyEnabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.shrink_out_on_empty_enabled"))
                .description(previewed(Component.translatable("immersivehotbar.option.shrink_out_on_empty_enabled.desc"), context.shrinkOutPreview))
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
                .description(previewed(Component.translatable("immersivehotbar.option.bouncy_animation.desc"), context.bouncyPickupPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> bouncyAnimation, v -> bouncyAnimation = v)
                .build();

        Option<Float> bounceStiffnessOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.bounce_stiffness"))
                .description(previewed(Component.translatable("immersivehotbar.option.bounce_stiffness.desc"), context.bouncyPickupPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.01f, 2f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.3f, () -> bouncyStiffness, v -> bouncyStiffness = v)
                .available(bouncyAnimation)
                .build();

        Option<Float> bounceDampingOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.bounce_damping"))
                .description(previewed(Component.translatable("immersivehotbar.option.bounce_damping.desc"), context.bouncyPickupPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 0.99f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
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

        bindAvailability(
                bouncyEnabledOpt,
                bounceStiffnessOpt,
                bounceDampingOpt,
                toolsIgnoreBounceOpt,
                weaponsIgnoreBounceOpt
        );

        Option<Boolean> toolAnimatesOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tool_animates"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tool_animates.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> toolAnimates, v -> toolAnimates = v)
                .build();

        Option<Boolean> weaponAnimatesOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.weapon_animates"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.weapon_animates.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> weaponAnimates, v -> weaponAnimates = v)
                .build();

        Option<Boolean> durabilityAnimatesOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.durability_animates"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.durability_animates.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> durabilityAnimates, v -> durabilityAnimates = v)
                .build();

        Option<Boolean> foodAnimatesOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.food_animates"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.food_animates.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> foodAnimates, v -> foodAnimates = v)
                .build();

        Option<Float> animationIntensityOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.pop_scale"))
                .description(previewed(Component.translatable("immersivehotbar.option.pop_scale.desc"), context.pickupPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0f, 2f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.5f, () -> animationIntensity, v -> animationIntensity = v)
                .build();

        Option<Float> animationSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.animation_smoothness"))
                .description(previewed(Component.translatable("immersivehotbar.option.animation_smoothness.desc"), context.pickupPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.01f, 2.0f).step(0.01f).formatValue(ConfigUtil::formatTwoDecimals))
                .binding(0.1f, () -> animationSpeed, v -> animationSpeed = v)
                .build();

        Option<Float> shrinkSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.shrink_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.shrink_speed.desc"), context.shrinkOutPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 30.0f).step(0.1f))
                .binding(2.5f, () -> shrinkAnimationSpeed, v -> shrinkAnimationSpeed = v)
                .build();

        context.itemPreviewState.shouldGrowOpt = context.shouldGrowOpt;
        context.itemPreviewState.selectedScaleOpt = context.selectedScaleOpt;
        context.itemPreviewState.unselectedScaleOpt = context.unselectedScaleOpt;
        context.itemPreviewState.textScalingOpt = context.textScalingOpt;

        context.itemPreviewState.bouncyEnabledOpt = bouncyEnabledOpt;
        context.itemPreviewState.bounceStiffnessOpt = bounceStiffnessOpt;
        context.itemPreviewState.bounceDampingOpt = bounceDampingOpt;
        context.itemPreviewState.animationIntensityOpt = animationIntensityOpt;
        context.itemPreviewState.animationSpeedOpt = animationSpeedOpt;
        context.itemPreviewState.shrinkSpeedOpt = shrinkSpeedOpt;
        context.itemPreviewState.pickupAnimationsEnabledOpt = pickupAnimationsEnabledOpt;
        context.itemPreviewState.useAnimationsEnabledOpt = useAnimationsEnabledOpt;
        context.itemPreviewState.shrinkOutOnEmptyEnabledOpt = shrinkOutOnEmptyEnabledOpt;
        context.itemPreviewState.selectorScaleEnabledOpt = selectorScaleEnabledOpt;
        context.itemPreviewState.toolAnimatesOpt = toolAnimatesOpt;
        context.itemPreviewState.weaponAnimatesOpt = weaponAnimatesOpt;
        context.itemPreviewState.durabilityAnimatesOpt = durabilityAnimatesOpt;
        context.itemPreviewState.foodAnimatesOpt = foodAnimatesOpt;
        context.itemPreviewState.vanillaItemBobbingOpt = disableHotbarItemBobbingOpt;

        bindPreviewReset(
                context.pickupPreview::reset,
                hotbarItemAnimationsEnabledOpt,
                pickupAnimationsEnabledOpt,
                context.shouldGrowOpt,
                context.selectedScaleOpt,
                context.unselectedScaleOpt,
                context.textScalingOpt,
                animationIntensityOpt,
                animationSpeedOpt,
                bouncyEnabledOpt,
                bounceStiffnessOpt,
                bounceDampingOpt
        );

        bindPreviewReset(
                context.bouncyPickupPreview::reset,
                bouncyEnabledOpt,
                bounceStiffnessOpt,
                bounceDampingOpt,
                context.shouldGrowOpt,
                context.selectedScaleOpt,
                context.unselectedScaleOpt,
                context.textScalingOpt,
                animationIntensityOpt,
                animationSpeedOpt
        );

        bindPreviewReset(
                context.usePreview::reset,
                hotbarItemAnimationsEnabledOpt,
                useAnimationsEnabledOpt,
                durabilityAnimatesOpt,
                toolAnimatesOpt,
                weaponAnimatesOpt,
                foodAnimatesOpt,
                context.shouldGrowOpt,
                context.selectedScaleOpt,
                context.unselectedScaleOpt,
                context.textScalingOpt,
                animationSpeedOpt,
                bouncyEnabledOpt,
                bounceStiffnessOpt,
                bounceDampingOpt
        );

        bindPreviewReset(
                context.shrinkOutPreview::reset,
                hotbarItemAnimationsEnabledOpt,
                shrinkOutOnEmptyEnabledOpt,
                shrinkSpeedOpt,
                context.shouldGrowOpt,
                context.selectedScaleOpt,
                context.unselectedScaleOpt,
                context.textScalingOpt,
                bouncyEnabledOpt,
                bounceStiffnessOpt,
                bounceDampingOpt
        );

        return ConfigCategory.createBuilder()
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
                        .option(toolAnimatesOpt)
                        .option(weaponAnimatesOpt)
                        .option(durabilityAnimatesOpt)
                        .option(foodAnimatesOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.animation_feel"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.animation_feel.desc")))
                        .option(animationIntensityOpt)
                        .option(animationSpeedOpt)
                        .option(shrinkSpeedOpt)
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
                .build();
    }
}
