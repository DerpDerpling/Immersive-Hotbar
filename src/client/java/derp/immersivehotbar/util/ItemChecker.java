package derp.immersivehotbar.util;

import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Unique;

public class ItemChecker {
    @Unique
    public static boolean isTool(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof PickaxeItem || stack.getItem() instanceof HoeItem || stack.getItem() instanceof ShearsItem;
    }
    @Unique
    public static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof TridentItem || stack.getItem() instanceof MaceItem;
    }
}
