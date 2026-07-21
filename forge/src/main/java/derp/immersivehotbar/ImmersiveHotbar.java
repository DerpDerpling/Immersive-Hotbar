package derp.immersivehotbar;

import derp.immersivehotbar.config.ImmersiveHotbarConfigHandler;
import derp.immersivehotbar.config.ImmersiveHotbarConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(ImmersiveHotbar.MOD_ID)
public final class ImmersiveHotbar {
    public static final String MOD_ID = "immersivehotbar";

    public ImmersiveHotbar() {
        ImmersiveHotbarConfigHandler.load();
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> ImmersiveHotbarConfigScreen.create(parent)
                )
        );
    }
}
