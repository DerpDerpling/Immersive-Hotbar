package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.hotbarItemAnimationsEnabled;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.textScales;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.vanillaItemBobbing;


@Mixin(Gui.class)
public abstract class ItemAnimationsMixin {
    @Unique
    private static final HotbarAnimationController ANIMATIONS = HotbarAnimationController.getInstance();

    @Inject(method = "renderItemHotbar", at = @At("HEAD"))
    private void immersiveHotbar$beginHotbarRender(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        ANIMATIONS.beginHotbarRender();
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void immersiveHotbar$prepareSlot(GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed, CallbackInfo ci) {
        ANIMATIONS.prepareSlot(context, x, y, player, stack);
    }

    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getPopTime()I"))
    private int immersiveHotbar$disableVanillaBobbing(ItemStack stack, Operation<Integer> original) {
        if (!hotbarItemAnimationsEnabled) return original.call(stack);
        return vanillaItemBobbing ? original.call(stack) : 0;
    }

    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void immersiveHotbar$scaleItem(GuiGraphics context, LivingEntity entity, ItemStack stack, int x, int y, int seed, Operation<Void> original) {
        if (!hotbarItemAnimationsEnabled) {
            original.call(context, entity, stack, x, y, seed);
            return;
        }

        HotbarAnimationRenderer.withItemScale(context, x, y, ANIMATIONS.currentScale(), () -> original.call(context, entity, stack, x, y, seed));
    }

    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void immersiveHotbar$scaleDecorations(GuiGraphics context, Font font, ItemStack stack, int x, int y, Operation<Void> original) {
        if (!hotbarItemAnimationsEnabled || !textScales) {
            original.call(context, font, stack, x, y);
            return;
        }

        HotbarAnimationRenderer.withItemScale(context, x, y, ANIMATIONS.currentScale(), () -> original.call(context, font, stack, x, y));
    }

    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void immersiveHotbar$renderShrinkingItems(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        ANIMATIONS.renderShrinkingItems(context);
    }
}
