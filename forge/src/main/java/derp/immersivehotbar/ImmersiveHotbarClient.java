package derp.immersivehotbar;

import derp.immersivehotbar.animation.hotbar.HotbarAnimationController;
import derp.immersivehotbar.animation.hotbar.HotbarSlots;
import derp.immersivehotbar.animation.tooltip.TooltipAnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.ModList;

import static derp.immersivehotbar.config.ImmersiveHotbarConfig.toolAnimates;
import static derp.immersivehotbar.config.ImmersiveHotbarConfig.weaponAnimates;

public final class ImmersiveHotbarClient {
    private static boolean wasUsingItem;
    private static ItemStack lastUsedItem = ItemStack.EMPTY;
    private static boolean wasCrossbowChargedMainhand;
    private static boolean wasCrossbowChargedOffhand;
    private static BlockPos lastBreakingPos;

    public static final boolean IS_DOUBLEHOTBAR_LOADED = ModList.get().isLoaded("double_hotbar");

    private ImmersiveHotbarClient() {}

    public static void tick(Minecraft client) {
        if (client.player == null) return;

        boolean isUsing = client.player.isUsingItem();
        if (isUsing) {
            lastUsedItem = client.player.getUseItem();
        } else if (wasUsingItem && !lastUsedItem.isEmpty()) {
            Item item = lastUsedItem.getItem();
            if (weaponAnimates && (item instanceof BowItem || item instanceof CrossbowItem)) {
                int slot = client.player.getMainHandItem() == lastUsedItem ? client.player.getInventory().selected : HotbarSlots.offhand();
                triggerShrink(slot);
            }
            lastUsedItem = ItemStack.EMPTY;
        }
        wasUsingItem = isUsing;

        ItemStack mainHandStack = client.player.getMainHandItem();
        if (mainHandStack.getItem() instanceof CrossbowItem) {
            boolean isCharged = CrossbowItem.isCharged(mainHandStack);
            if (wasCrossbowChargedMainhand && !isCharged && weaponAnimates) triggerShrink(client.player.getInventory().selected);
            wasCrossbowChargedMainhand = isCharged;
        } else {
            wasCrossbowChargedMainhand = false;
        }

        ItemStack offHandStack = client.player.getOffhandItem();
        if (offHandStack.getItem() instanceof CrossbowItem) {
            boolean isCharged = CrossbowItem.isCharged(offHandStack);
            if (wasCrossbowChargedOffhand && !isCharged && weaponAnimates) triggerShrink(HotbarSlots.offhand());
            wasCrossbowChargedOffhand = isCharged;
        } else {
            wasCrossbowChargedOffhand = false;
        }

        if (client.level != null) {
            HitResult hit = client.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK && client.options.keyAttack.isDown()) {
                lastBreakingPos = ((BlockHitResult) hit).getBlockPos();
            }

            if (lastBreakingPos != null && client.level.getBlockState(lastBreakingPos).isAir()) {
                ItemStack stack = client.player.getMainHandItem();
                if (stack.getItem() instanceof TieredItem && toolAnimates) triggerShrink(client.player.getInventory().selected);
                lastBreakingPos = null;
            }
        }
    }

    public static void resetJoin() {
        TooltipAnimationController.getInstance().reset();
    }

    public static void resetDisconnect() {
        HotbarAnimationController.getInstance().clearTrackedStacks();
        TooltipAnimationController.getInstance().reset();
        lastBreakingPos = null;
        wasUsingItem = false;
        lastUsedItem = ItemStack.EMPTY;
        wasCrossbowChargedMainhand = false;
        wasCrossbowChargedOffhand = false;
    }

    private static void triggerShrink(int slot) {
        HotbarAnimationController.getInstance().triggerShrink(slot);
    }
}