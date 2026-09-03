package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.hotbar.HotbarAnimationRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.*;


@Mixin(Gui.class)
public abstract class ItemAnimationsMixin {
    @Unique
    private static final HotbarAnimationController ANIMATIONS = HotbarAnimationController.getInstance();

    @Inject(method = "extractItemHotbar", at = @At("HEAD"))
    private void immersiveHotbar$beginHotbarRender(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        ANIMATIONS.beginHotbarRender();
    }

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void immersiveHotbar$prepareSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, CallbackInfo ci) {
        ANIMATIONS.prepareSlot(graphics, x, y, player, itemStack);
    }

    @WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getPopTime()I"))
    private int immersiveHotbar$disableVanillaBobbing(ItemStack stack, Operation<Integer> original) {
        if (!hotbarItemAnimationsEnabled) return original.call(stack);
        return vanillaItemBobbing ? original.call(stack) : 0;
    }

    @WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void immersiveHotbar$scaleItem(GuiGraphicsExtractor instance, LivingEntity owner, ItemStack itemStack, int x, int y, int seed, Operation<Void> original) {
        if (!hotbarItemAnimationsEnabled) {
            original.call(instance, owner, itemStack, x, y, seed);
            return;
        }

        HotbarAnimationRenderer.withItemScale(instance, x, y, ANIMATIONS.currentScale(), () -> original.call(instance, owner, itemStack, x, y, seed));
    }

    @WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void immersiveHotbar$scaleDecorations(GuiGraphicsExtractor instance, Font font, ItemStack itemStack, int x, int y, Operation<Void> original) {
        if (!hotbarItemAnimationsEnabled || !textScales) {
            original.call(instance, font, itemStack, x, y);
            return;
        }

        HotbarAnimationRenderer.withItemScale(instance, x, y, ANIMATIONS.currentScale(), () -> original.call(instance, font, itemStack, x, y));
    }

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    private void immersiveHotbar$renderShrinkingItems(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ANIMATIONS.renderShrinkingItems(graphics);
    }
}
