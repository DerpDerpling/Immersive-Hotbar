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

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ItemUseAnimationMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted() && player.isInCreativeMode()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof BlockItem) {
                triggerHotbarAnimation();
            }
        }
    }
    @Unique
    private void triggerHotbarAnimation() {
        assert client.player != null;
        if (client.player.isInCreativeMode() && client.player == null || client.inGameHud == null) return;


        int slotIndex = client.player.getInventory().selectedSlot;
        ((InGameHudAnimationHandler) client.inGameHud).immersive_hotbar$triggerSlotAnimation(slotIndex);
    }
}
