package derp.immersivehotbar.config.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.config.screen.ConfigUtil.*;

public final class TooltipConfigSection {
    private TooltipConfigSection() {}

    public static ConfigCategory create(ConfigUtil.Context context) {
        Option<Boolean> enabledOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_animation"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_animation.desc"), context.tooltipPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> immersiveToolTip, v -> immersiveToolTip = v)
                .build();

        Option<Boolean> itemChangeOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_item_change"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_item_change.desc"), context.tooltipPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> tooltipAnimateOnItemChange, v -> tooltipAnimateOnItemChange = v)
                .build();

        Option<Boolean> emptySlotOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_empty_slot"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_empty_slot.desc"), context.tooltipPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> tooltipAnimateOnEmptySlot, v -> tooltipAnimateOnEmptySlot = v)
                .build();

        Option<Float> popScaleOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_pop_scale"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_pop_scale.desc"), context.tooltipPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(1.0f, 2.0f).step(0.01f).formatValue(v -> Component.literal(String.format("%.2f", v))))
                .binding(1.2f, () -> tooltipPopScale, v -> tooltipPopScale = v)
                .build();

        Option<Float> animationSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_animation_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_animation_speed.desc"), context.tooltipPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(1.0f, 20.0f).step(0.1f).formatValue(v -> Component.literal(String.format("%.1f", v))))
                .binding(8.0f, () -> tooltipAnimationSpeed, v -> tooltipAnimationSpeed = v)
                .build();

        Option<Float> shrinkSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_shrink_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_shrink_speed.desc"), context.tooltipPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(1.0f, 25.0f).step(0.1f).formatValue(v -> Component.literal(String.format("%.1f", v))))
                .binding(10.0f, () -> tooltipShrinkSpeed, v -> tooltipShrinkSpeed = v)
                .build();

        Option<Float> minimumScaleOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_minimum_scale"))
                .description(previewed(Component.translatable("immersivehotbar.option.tooltip_minimum_scale.desc"), context.tooltipPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.0f, 1.0f).step(0.01f).formatValue(v -> Component.literal(String.format("%.2f", v))))
                .binding(0f, () -> tooltipMinimumFadeScale, v -> tooltipMinimumFadeScale = v)
                .build();

        Option<Boolean> yOffsetToggleOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_y_offset_toggle"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tooltip_y_offset_toggle.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(false, () -> tooltipYOffsetEnabled, v -> tooltipYOffsetEnabled = v)
                .build();

        Option<Integer> yOffsetOpt = Option.<Integer>createBuilder()
                .name(Component.translatable("immersivehotbar.option.tooltip_y_offset"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.tooltip_y_offset.desc")))
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(40, 300).step(1))
                .binding(52, () -> tooltipYOffset, v -> tooltipYOffset = v)
                .build();

        Option<Boolean> scaleOffsetOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.scale_tooltip_offset"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.scale_tooltip_offset.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> scaleTooltipOffset, v -> scaleTooltipOffset = v)
                .build();
        Option<Boolean> overlayMessagesOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.animate_overlay_messages"))
                .description(OptionDescription.of(Component.translatable("immersivehotbar.option.animate_overlay_messages.desc")))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> animateOverlayMessages, v -> animateOverlayMessages = v)
                .build();

        bindAvailability(enabledOpt, itemChangeOpt, emptySlotOpt, popScaleOpt, animationSpeedOpt, shrinkSpeedOpt, minimumScaleOpt);
        bindAvailability(yOffsetToggleOpt, yOffsetOpt, scaleOffsetOpt);
        bindPreviewReset(context.tooltipPreview::reset, enabledOpt, itemChangeOpt, emptySlotOpt, popScaleOpt, animationSpeedOpt, shrinkSpeedOpt, minimumScaleOpt);

        return ConfigCategory.createBuilder()
                .name(Component.translatable("immersivehotbar.category.tooltip"))
                .tooltip(Component.translatable("immersivehotbar.tooltip.tooltip"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.tooltip_animation"))
                        .option(enabledOpt)
                        .option(overlayMessagesOpt)
                        .option(itemChangeOpt)
                        .option(emptySlotOpt)
                        .option(popScaleOpt)
                        .option(animationSpeedOpt)
                        .option(shrinkSpeedOpt)
                        .option(minimumScaleOpt)
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.tooltip_position"))
                        .option(yOffsetToggleOpt)
                        .option(yOffsetOpt)
                        .option(scaleOffsetOpt)
                        .build())
                .build();
    }
}