package derp.immersivehotbar.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;

public class InteractiveBlockChecker {
    private static final Method BASE_ON_USE_METHOD;

    static {
        try {
            BASE_ON_USE_METHOD = AbstractBlock.class.getDeclaredMethod(
                    "onUse", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, BlockHitResult.class
            );
            BASE_ON_USE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to get AbstractBlock#onUse", e);
        }
    }

    public static boolean isRightClickable(Block block) {
        try {
            Method method = block.getClass().getMethod(
                    "onUse", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, BlockHitResult.class
            );
            return method.getDeclaringClass() != BASE_ON_USE_METHOD.getDeclaringClass();
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isInteractive(ClientPlayerEntity player, BlockHitResult hitResult) {
        BlockState state = player.getWorld().getBlockState(hitResult.getBlockPos());
        Block block = state.getBlock();
        return isRightClickable(block) || state.hasBlockEntity();
    }
}

