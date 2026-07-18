package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.otsutsukimiho.nozomiaddon.utils.hitbox.Lever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin {
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void modifyLeverHitbox(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        VoxelShape oldShape = Lever.getShape(state.getValue(LeverBlock.FACE), state.getValue(LeverBlock.FACING));

        if (oldShape != null) {
            cir.setReturnValue(oldShape);
        }
    }
}