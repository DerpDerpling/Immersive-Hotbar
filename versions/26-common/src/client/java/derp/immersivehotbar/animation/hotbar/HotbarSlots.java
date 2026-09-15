package derp.immersivehotbar.animation.hotbar;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import static derp.immersivehotbar.ImmersiveHotbarClient.IS_DOUBLEHOTBAR_LOADED;

public final class HotbarSlots {
    private HotbarSlots() {}

    public static int offhand() {return IS_DOUBLEHOTBAR_LOADED ? 18 : 9;}

    public static int forHand(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : offhand();
    }
}
