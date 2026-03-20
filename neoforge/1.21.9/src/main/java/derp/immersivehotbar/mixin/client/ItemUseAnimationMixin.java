package derp.immersivehotbar.mixin.client;

import derp.immersivehotbar.InGameHudAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import static derp.immersivehotbar.ImmersiveHotbarClientLogic.IS_DOUBLEHOTBAR_LOADED;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.toolAnimates;
import static derp.immersivehotbar.util.ItemChecker.isTool;
import static derp.immersivehotbar.util.ItemChecker.isWeapon;

@Mixin(MultiPlayerGameMode.class)
public abstract class ItemUseAnimationMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private net.minecraft.world.level.block.state.BlockState preBlockState;

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void storePreBlockState(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        preBlockState = player.level().getBlockState(hitResult.getBlockPos().relative(hitResult.getDirection()));
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void onInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) return;

        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof BlockItem) {
            var world = player.level();
            var placedPos = hitResult.getBlockPos().relative(hitResult.getDirection());
            var newState = world.getBlockState(placedPos);

            if (!newState.is(preBlockState.getBlock())) {
                triggerHotbarAnimation(hand);
                return;
            }
            if (world.getBlockState(placedPos).canBeReplaced()) {
                triggerHotbarAnimation(hand);
                return;
            }
        }

        if (!isTool(stack) && !isWeapon(stack) && !(stack.getItem() instanceof BlockItem)) {
            triggerHotbarAnimation(hand);
        }

        if ((isTool(stack) || isWeapon(stack))) {
            if (toolAnimates) {
                triggerHotbarAnimation(hand);
            }
        }
    }

    //fixes #12, where item animations wouldn't play when interacting with them.
    @Inject(method = "useItem", at = @At("RETURN"))
    private void onInteractItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction()) {
            ItemStack stack = player.getItemInHand(hand);
            ItemUseAnimation action = stack.getUseAnimation();
            if (!isTool(stack) && !isWeapon(stack) && action != ItemUseAnimation.EAT && action != ItemUseAnimation.DRINK) {
                triggerHotbarAnimation(hand);
            }
        }
    }

    @Inject(method = "interact", at = @At("RETURN"))
    private void onInteractEntity(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction()) {
            player.getItemInHand(hand);
            triggerHotbarAnimation(hand);
        }
    }

    @Unique
    private void triggerHotbarAnimation(InteractionHand hand) {
        if (minecraft.player == null || minecraft.gui == null) return;

        int slotIndex;
        if (hand == InteractionHand.MAIN_HAND) {
            slotIndex = minecraft.player.getInventory().getSelectedSlot();
        } else {
            if (IS_DOUBLEHOTBAR_LOADED) slotIndex = 18; else slotIndex = 9;
        }

        ((InGameHudAnimationHandler) minecraft.gui).immersive_hotbar$triggerSlotAnimation(slotIndex);
    }
}
