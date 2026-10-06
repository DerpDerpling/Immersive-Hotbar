package derp.immersivehotbar.config.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;

import java.awt.*;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;
import static derp.immersivehotbar.config.screen.ConfigUtil.bindAvailability;
import static derp.immersivehotbar.config.screen.ConfigUtil.previewed;

public final class GeneralConfigSection {
    private GeneralConfigSection() {
    }

    public static ConfigCategory create(ConfigUtil.Context context) {
        Option<Boolean> animatedXpBarOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.animated_xp_bar"))
                .description(previewed(Component.translatable("immersivehotbar.option.animated_xp_bar.desc"), context.xpBarPreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> animatedXpBar, v -> animatedXpBar = v)
                .build();

        Option<Float> xpBarSpeedOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.xp_bar_speed"))
                .description(previewed(Component.translatable("immersivehotbar.option.xp_bar_speed.desc"), context.xpBarPreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.1f, 5.0f).step(0.1f))
                .binding(1.0f, () -> xpBarSpeed, v -> xpBarSpeed = v)
                .available(animatedXpBar)
                .build();

        bindAvailability(animatedXpBarOpt, xpBarSpeedOpt);

        context.animatedXpBarOpt = animatedXpBarOpt;
        context.xpBarSpeedOpt = xpBarSpeedOpt;

        Option<Boolean> shouldGrowOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.should_grow"))
                .description(previewed(Component.translatable("immersivehotbar.option.should_grow.desc"), context.selectedScalePreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> shouldItemGrowWhenSelected, v -> shouldItemGrowWhenSelected = v)
                .build();

        Option<Float> selectedScaleOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.selected_scale"))
                .description(previewed(Component.translatable("immersivehotbar.option.selected_scale.desc"), context.selectedScalePreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0.0f, 3.0f).step(0.01f).formatValue(v -> Component.literal(String.format("%.2f", v))))
                .binding(1.2f, () -> selectedItemSize, v -> selectedItemSize = v)
                .available(shouldItemGrowWhenSelected)
                .build();

        Option<Float> unselectedScaleOpt = Option.<Float>createBuilder()
                .name(Component.translatable("immersivehotbar.option.unselected_scale"))
                .description(previewed(Component.translatable("immersivehotbar.option.unselected_scale.desc"), context.selectedScalePreview))
                .controller(o -> FloatSliderControllerBuilder.create(o).range(0f, 3.0f).step(0.01f).formatValue(v -> Component.literal(String.format("%.2f", v))))
                .binding(1.0f, () -> nonSelectedItemSize, v -> nonSelectedItemSize = v)
                .build();

        Option<Boolean> textScalingOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("immersivehotbar.option.text_scaling"))
                .description(previewed(Component.translatable("immersivehotbar.option.text_scaling.desc"), context.selectedScalePreview))
                .controller(TickBoxControllerBuilder::create)
                .binding(true, () -> textScales, v -> textScales = v)
                .build();

        bindAvailability(shouldGrowOpt, selectedScaleOpt);

        context.shouldGrowOpt = shouldGrowOpt;
        context.selectedScaleOpt = selectedScaleOpt;
        context.unselectedScaleOpt = unselectedScaleOpt;
        context.textScalingOpt = textScalingOpt;

        return ConfigCategory.createBuilder()
                .name(Component.translatable("immersivehotbar.category.general"))
                .tooltip(Component.translatable("immersivehotbar.tooltip.general"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("immersivehotbar.group.item_scaling"))
                        .description(OptionDescription.of(Component.translatable("immersivehotbar.group.item_scaling.desc")))
                        .option(shouldGrowOpt)
                        .option(selectedScaleOpt)
                        .option(unselectedScaleOpt)
                        .option(textScalingOpt)
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
                .build();
    }
}
