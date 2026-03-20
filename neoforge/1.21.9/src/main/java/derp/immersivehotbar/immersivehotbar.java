package derp.immersivehotbar;

import com.mojang.logging.LogUtils;
import derp.immersivehotbar.config.ImmersiveHotbarConfigHandler;
import derp.immersivehotbar.config.ImmersiveHotbarConfigScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(immersivehotbar.MODID)
public class immersivehotbar {
    public static final String MODID = "immersivehotbar";
    private static final Logger LOGGER = LogUtils.getLogger();

    public immersivehotbar(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (client, parent) -> ImmersiveHotbarConfigScreen.create(parent)
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ImmersiveHotbarConfigHandler.load();
        LOGGER.info("HELLO FROM COMMON SETUP");
    }
}