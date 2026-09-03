package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class AttackEntityMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void immersiveHotbar$onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        if (player == null || target == null) return;
        HotbarAnimationController.getInstance().triggerWeaponAttack(player.getInventory().getSelectedSlot());
    }
}
