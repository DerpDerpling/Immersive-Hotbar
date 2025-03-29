package derp.interactivehotbar;

import derp.interactivehotbar.config.InteractiveHotbarConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;

public class InteractiveHotbarClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		MidnightConfig.init("interactive-hotbar", InteractiveHotbarConfig.class);

	}
}