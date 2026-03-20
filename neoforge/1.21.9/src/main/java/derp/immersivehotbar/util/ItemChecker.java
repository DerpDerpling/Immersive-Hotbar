package derp.immersivehotbar.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;

public class ItemChecker {

    public static boolean isTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.HOES) || stack.getItem() instanceof ShearsItem;
    }

    public static boolean isWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT) || stack.is(Items.MACE);
    }
}
