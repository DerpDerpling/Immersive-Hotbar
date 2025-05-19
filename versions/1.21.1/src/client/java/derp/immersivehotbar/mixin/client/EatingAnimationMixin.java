package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.ImmersiveHotbarClient.IS_DOUBLEHOTBAR_LOADED;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.foodAnimates;

@Mixin(LivingEntity.class)
public abstract class EatingAnimationMixin {

    @Inject(method = "spawnConsumptionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void onPlayConsumptionSound(ItemStack stack, int particleCount, CallbackInfo ci) {
        if (!foodAnimates) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof ClientPlayerEntity player)) return;
        if (stack.isEmpty()) return;

        UseAction action = stack.getUseAction();
        if (action != UseAction.EAT && action != UseAction.DRINK) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.inGameHud instanceof InGameHudAnimationHandler handler)) return;

        // Determine which hand is currently in use
        if (player.getActiveHand() == net.minecraft.util.Hand.MAIN_HAND) {
            int slot = player.getInventory().selectedSlot;
            handler.immersive_hotbar$triggerSlotAnimation(slot);
        } else if (player.getActiveHand() == net.minecraft.util.Hand.OFF_HAND) {
            if (IS_DOUBLEHOTBAR_LOADED) handler.immersive_hotbar$triggerSlotAnimation(18); else handler.immersive_hotbar$triggerSlotAnimation(9);
        }
    }

}