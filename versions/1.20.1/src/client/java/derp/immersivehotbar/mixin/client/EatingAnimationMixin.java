package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.ImmersiveHotbarClient.IS_DOUBLEHOTBAR_LOADED;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.foodAnimates;

@Mixin(LivingEntity.class)
public abstract class EatingAnimationMixin {

    @Inject(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void onPlayConsumptionSound(ItemStack stack, int particleCount, CallbackInfo ci) {
        if (!foodAnimates) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof LocalPlayer player)) return;
        if (stack.isEmpty()) return;

        UseAnim action = stack.getUseAnimation();
        if (action != UseAnim.EAT && action != UseAnim.DRINK) return;

        Minecraft client = Minecraft.getInstance();
        if (!(client.gui instanceof InGameHudAnimationHandler handler)) return;

        // Determine which hand is currently in use
        if (player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            int slot = player.getInventory().selected;
            handler.immersive_hotbar$triggerSlotAnimation(slot);
        } else if (player.getUsedItemHand() == net.minecraft.world.InteractionHand.OFF_HAND) {
            if (IS_DOUBLEHOTBAR_LOADED) handler.immersive_hotbar$triggerSlotAnimation(18); else handler.immersive_hotbar$triggerSlotAnimation(9);
        }
    }

}