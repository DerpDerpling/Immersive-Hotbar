package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.ImmersiveHotbarClient.IS_DOUBLEHOTBAR_LOADED;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.foodAnimates;

@Mixin(Consumable.class)
public class ConsumableComponentMixin {

    @Inject(
            method = "emitParticlesAndSounds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
            )
    )
    private void onPlayEatingSound(RandomSource random, LivingEntity user, ItemStack stack, int particleCount, CallbackInfo ci) {
        if (!foodAnimates) return;

        if (!(user instanceof LocalPlayer player)) return;

        Minecraft client = Minecraft.getInstance();
        if (!(client.gui instanceof InGameHudAnimationHandler handler)) return;

        if (player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            handler.immersive_hotbar$triggerSlotAnimation(player.getInventory().selected);
        } else {
            if (IS_DOUBLEHOTBAR_LOADED) handler.immersive_hotbar$triggerSlotAnimation(18); else handler.immersive_hotbar$triggerSlotAnimation(9);
        }
    }
}
