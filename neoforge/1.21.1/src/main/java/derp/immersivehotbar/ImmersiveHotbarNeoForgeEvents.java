package derp.immersivehotbar;

import derp.immersivehotbar.util.TooltipAnimationState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Arrays;

import static derp.immersivehotbar.util.SlotAnimationState.lastSlotStacks;
import static net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class ImmersiveHotbarNeoForgeEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ImmersiveHotbarClientLogic.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        TooltipAnimationState.reset();
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        Arrays.fill(lastSlotStacks, ItemStack.EMPTY);
        TooltipAnimationState.reset();
    }
}