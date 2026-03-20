package derp.immersivehotbar.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.neoforged.fml.loading.FMLPaths;

public class ImmersiveHotbarConfigHandler {
    private static ConfigClassHandler<ImmersiveHotbarConfig> HANDLER;

    public static ConfigClassHandler<ImmersiveHotbarConfig> getHandler() {
        if (HANDLER == null) {
            HANDLER = ConfigClassHandler.createBuilder(ImmersiveHotbarConfig.class)
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FMLPaths.CONFIGDIR.get().resolve("immersive-hotbar.json5"))
                            .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                            .setJson5(true)
                            .build())
                    .build();
        }
        return HANDLER;
    }

    public static void load() {
        getHandler().load();
    }

    public static void save() {
        getHandler().save();
    }
}