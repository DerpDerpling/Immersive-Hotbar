package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.util.SlotAnimationState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.nonSelectedItemSize;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.weaponAnimates;

@Mixin(ClientPlayerInteractionManager.class)
public class AttackEntityMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (weaponAnimates)
            if (player == null || target == null) return;

        int slot = player.getInventory().selectedSlot;
        SlotAnimationState.wasUsed[slot] = true;
        SlotAnimationState.slotScales[slot] = nonSelectedItemSize - 0.07f;
    }


}
