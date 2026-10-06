package derp.immersivehotbar;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "immersivehotbar", value = Dist.CLIENT)
public class ImmersiveHotbarNeoForgeEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ImmersiveHotbarClientLogic.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        TooltipAnimationController.getInstance().reset();
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        HotbarAnimationController.getInstance().clearTrackedStacks();
        TooltipAnimationController.getInstance().reset();
        ImmersiveHotbarClientLogic.reset();
    }
}