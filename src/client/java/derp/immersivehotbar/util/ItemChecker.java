package derp.immersivehotbar.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.tag.ItemTags;

public class ItemChecker {

    public static boolean isTool(ItemStack stack) {
        return stack.isIn(ItemTags.PICKAXES) || stack.isIn(ItemTags.SHOVELS) || stack.isIn(ItemTags.AXES) || stack.isIn(ItemTags.HOES) || stack.getItem() instanceof ShearsItem;
    }

    public static boolean isWeapon(ItemStack stack) {
        return stack.isIn(ItemTags.SWORDS) || stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW) || stack.isOf(Items.TRIDENT) || stack.isOf(Items.MACE);
    }
}
