package derp.immersivehotbar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.immersiveToolTip;

@Mixin(Gui.class)
public class TooltipAnimationsMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private ItemStack lastToolHighlight;
    @Shadow private int toolHighlightTimer;

    @Unique
    private final TooltipAnimationController immersiveHotbar$tooltip = TooltipAnimationController.getInstance();

    @Unique private long immersiveHotbar$lastRenderTime = System.nanoTime();
    @Unique private boolean immersiveHotbar$spoofed = false;
    @Unique private ItemStack immersiveHotbar$realStack = ItemStack.EMPTY;
    @Unique private int immersiveHotbar$realTimer = 0;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void immersiveHotbar$tickTooltip(CallbackInfo ci) {
        if (!immersiveToolTip || minecraft.player == null) {
            return;
        }

        immersiveHotbar$tooltip.tick(minecraft.player.getInventory().getSelected());
    }

    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelected()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack immersiveHotbar$keepTooltipAlive(Inventory inventory, Operation<ItemStack> original) {
        ItemStack selected = original.call(inventory);

        if (!immersiveToolTip) {
            return selected;
        }

        return immersiveHotbar$tooltip.selectedStackForTick(selected, toolHighlightTimer);
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"))
    private void immersiveHotbar$prepareTooltip(GuiGraphics graphics, CallbackInfo ci) {
        immersiveHotbar$spoofed = false;

        if (!immersiveToolTip) {
            return;
        }

        long now = System.nanoTime();
        float dt = Mth.clamp((now - immersiveHotbar$lastRenderTime) / 1_000_000_000.0f, 0.0f, 0.05f);
        immersiveHotbar$lastRenderTime = now;

        boolean realMainHandEmpty = minecraft.player == null || minecraft.player.getInventory().getSelected().isEmpty();

        var override = immersiveHotbar$tooltip.prepareRender(lastToolHighlight, toolHighlightTimer, realMainHandEmpty, dt);

        if (override.spoofed()) {
            immersiveHotbar$realStack = lastToolHighlight;
            immersiveHotbar$realTimer = toolHighlightTimer;

            lastToolHighlight = override.stack();
            toolHighlightTimer = override.highlightTimer();
            immersiveHotbar$spoofed = true;
        }
    }

    @WrapOperation(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I"))
    private int immersiveHotbar$animateTooltipDraw(GuiGraphics graphics, Font font, Component text, int x, int y, int width, int color, Operation<Integer> original) {
        if (!immersiveToolTip) {
            return original.call(graphics, font, text, x, y, width, color);
        }

        return TooltipAnimationRenderer.render(graphics, minecraft, immersiveHotbar$tooltip.state(), text, x, y, width, color, (drawX, drawY, drawColor) -> original.call(graphics, font, text, drawX, drawY, width, drawColor));}

    @Inject(method = "renderSelectedItemName", at = @At("RETURN"))
    private void immersiveHotbar$restoreTooltip(GuiGraphics graphics, CallbackInfo ci) {
        if (!immersiveHotbar$spoofed) {
            return;
        }

        lastToolHighlight = immersiveHotbar$realStack;
        toolHighlightTimer = immersiveHotbar$realTimer;
        immersiveHotbar$spoofed = false;
    }
}
