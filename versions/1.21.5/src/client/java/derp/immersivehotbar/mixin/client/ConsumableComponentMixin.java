package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.ImmersiveHotbarClient.IS_DOUBLEHOTBAR_LOADED;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.foodAnimates;

@Mixin(ConsumableComponent.class)
public class ConsumableComponentMixin {

    @Inject(
            method = "spawnParticlesAndPlaySound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"
            )
    )
    private void onPlayEatingSound(Random random, LivingEntity user, ItemStack stack, int particleCount, CallbackInfo ci) {
        if (!foodAnimates) return;

        if (!(user instanceof ClientPlayerEntity player)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.inGameHud instanceof InGameHudAnimationHandler handler)) return;

        if (player.getActiveHand() == net.minecraft.util.Hand.MAIN_HAND) {
            handler.immersive_hotbar$triggerSlotAnimation(player.getInventory().getSelectedSlot());
        } else {
            if (IS_DOUBLEHOTBAR_LOADED) handler.immersive_hotbar$triggerSlotAnimation(18); else handler.immersive_hotbar$triggerSlotAnimation(9);
        }
    }
}
