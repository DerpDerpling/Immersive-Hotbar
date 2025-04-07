package derp.immersivehotbar.config;



import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.util.Identifier;
import net.fabricmc.loader.api.FabricLoader;

public class ImmersiveHotbarConfigHandler {
    public static final ConfigClassHandler<ImmersiveHotbarConfig> HANDLER = ConfigClassHandler.createBuilder(ImmersiveHotbarConfig.class)
            .id(Identifier.of("immersivehotbar", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("immersive-hotbar.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();
    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
    }
}
