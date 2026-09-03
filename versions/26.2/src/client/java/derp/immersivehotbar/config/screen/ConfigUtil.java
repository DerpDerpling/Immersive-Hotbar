package derp.immersivehotbar.config.screen;

import derp.immersivehotbar.config.preview.*;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.network.chat.Component;

public final class ConfigUtil {
    private ConfigUtil() {
    }

    static OptionDescription previewed(Component text, ImageRenderer preview) {
        return OptionDescription.createBuilder()
                .text(text)
                .customImage(preview)
                .build();
    }

    static void bindAvailability(Option<Boolean> controller, Option<?>... dependents) {
        boolean enabled = controller.pendingValue();

        for (Option<?> dependent : dependents) {
            dependent.setAvailable(enabled);
        }

        controller.addEventListener((option, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE
                    || event == OptionEventListener.Event.INITIAL) {

                boolean available = controller.pendingValue();

                for (Option<?> dependent : dependents) {
                    dependent.setAvailable(available);
                }
            }
        });
    }


    @SafeVarargs
    static void bindPreviewReset(Runnable reset, Option<?>... options) {
        for (Option<?> option : options) {
            option.addEventListener((ignored, event) -> {
                if (event == OptionEventListener.Event.STATE_CHANGE
                        || event == OptionEventListener.Event.INITIAL) {
                    reset.run();
                }
            });
        }
    }

    public static final class Context {
        public Context() {
        }

        final ItemAnimationPreviewState itemPreviewState =
                new ItemAnimationPreviewState();

        final ItemAnimationPreview selectedScalePreview =
                new ItemAnimationPreview(
                        itemPreviewState,
                        PreviewMode.SELECTED_SCALE_COMPARISON
                );

        final ItemAnimationPreview pickupPreview =
                new ItemAnimationPreview(
                        itemPreviewState,
                        PreviewMode.PICKUP_POP
                );

        final ItemAnimationPreview bouncyPickupPreview =
                new ItemAnimationPreview(
                        itemPreviewState,
                        PreviewMode.PICKUP_POP,
                        true
                );

        final ItemAnimationPreview usePreview =
                new ItemAnimationPreview(
                        itemPreviewState,
                        PreviewMode.USE_SHRINK
                );

        final ItemAnimationPreview shrinkOutPreview =
                new ItemAnimationPreview(
                        itemPreviewState,
                        PreviewMode.SHRINK_OUT_ON_EMPTY
                );

        final AnimatedTooltipPreview tooltipPreview =
                new AnimatedTooltipPreview();

        final XPBarPreview xpBarPreview =
                new XPBarPreview();
        final HotbarEffectsPreview durabilityGlowPreview = new HotbarEffectsPreview(hotbarEffectsPreviewState, HotbarEffectsPreview.Mode.DURABILITY_GLOW);

        final HotbarEffectsPreview backgroundPreview = new HotbarEffectsPreview(hotbarEffectsPreviewState, HotbarEffectsPreview.Mode.BACKGROUND);


        Option<Boolean> animatedXpBarOpt;
        Option<Float> xpBarSpeedOpt;

        Option<Boolean> shouldGrowOpt;
        Option<Float> selectedScaleOpt;
        Option<Float> unselectedScaleOpt;
        Option<Boolean> textScalingOpt;
    }

    public static Component formatTwoDecimals(Float value) {
        return Component.literal(String.format("%.2f", value));
    }

    static final HotbarEffectsPreviewState hotbarEffectsPreviewState =
            new HotbarEffectsPreviewState();

}
