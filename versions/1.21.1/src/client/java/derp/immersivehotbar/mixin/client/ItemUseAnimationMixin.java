package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.toolAnimates;
import static derp.immersivehotbar.util.ItemChecker.isTool;
import static derp.immersivehotbar.util.ItemChecker.isWeapon;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ItemUseAnimationMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted()) {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.getItem() instanceof BlockItem blockItem) {
                var world = player.getWorld();
                var placedPos = hitResult.getBlockPos().offset(hitResult.getSide());

                if (world.getBlockState(placedPos).getBlock() == blockItem.getBlock()) {
                    triggerHotbarAnimation(hand);
                    return;
                }
                if (world.getBlockState(placedPos).isReplaceable()) {
                    triggerHotbarAnimation(hand);
                    return;
                }
            }

            if ((isTool(stack) || isWeapon(stack))) {
                if (toolAnimates) {
                    triggerHotbarAnimation(hand);
                }
            }
        }
    }

    @Unique
    private void triggerHotbarAnimation(Hand hand) {
        if (client.player == null || client.inGameHud == null) return;

        int slotIndex;
        if (hand == Hand.MAIN_HAND) {
            slotIndex = client.player.getInventory().selectedSlot;
        } else {
            slotIndex = 9;
        }

        ((InGameHudAnimationHandler) client.inGameHud).immersive_hotbar$triggerSlotAnimation(slotIndex);
    }
}
