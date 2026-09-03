package derp.immersivehotbar.config;

import derp.immersivehotbar.config.screen.*;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ImmersiveHotbarConfigScreen {
    private ImmersiveHotbarConfigScreen() {
    }

    public static Screen create(Screen parent) {
        var builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("immersivehotbar.title"));

        ConfigUtil.Context context = new ConfigUtil.Context();

        builder.category(GeneralConfigSection.create(context));
        builder.category(AnimationConfigSection.create(context));
        builder.category(TooltipConfigSection.create(context));
        builder.category(EffectsConfigSection.create(context));

        return builder
                .save(ImmersiveHotbarConfigHandler.HANDLER::save)
                .build()
                .generateScreen(parent);
    }
}
