package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.hotbar.HotbarSlots;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.foodAnimates;

@Mixin(LivingEntity.class)
public abstract class EatingAnimationMixin {
    @Inject(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void immersiveHotbar$onConsumptionEffect(ItemStack stack, int particleCount, CallbackInfo ci) {
        if (!foodAnimates) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof LocalPlayer player) || stack.isEmpty()) return;

        UseAnim action = stack.getUseAnimation();
        if (action != UseAnim.EAT && action != UseAnim.DRINK) return;

        HotbarAnimationController.getInstance().triggerUse(HotbarSlots.forHand(player, player.getUsedItemHand()));
    }
}
