package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.util.SlotAnimationState;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.nonSelectedItemSize;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.weaponAnimates;

@Mixin(MultiPlayerGameMode.class)
public class AttackEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        if (weaponAnimates)
            if (player == null || target == null) return;

        int slot = player.getInventory().getSelectedSlot();
        SlotAnimationState.wasUsed[slot] = true;
        if(weaponAnimates)
            SlotAnimationState.slotScales[slot] = nonSelectedItemSize - 0.07f;
    }


}
