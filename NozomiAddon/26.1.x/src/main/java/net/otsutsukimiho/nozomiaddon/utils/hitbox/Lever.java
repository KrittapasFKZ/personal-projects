package net.otsutsukimiho.nozomiaddon.utils.hitbox;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.otsutsukimiho.nozomiaddon.features.HitBox;

public class Lever {
    protected static final VoxelShape FLOOR_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    protected static final VoxelShape CEILING_SHAPE = Block.box(4.0D, 6.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    protected static final VoxelShape NORTH_SHAPE = Block.box(5.0D, 3.0D, 10.0D, 11.0D, 13.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(5.0D, 3.0D, 0.0D, 11.0D, 13.0D, 6.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 3.0D, 5.0D, 6.0D, 13.0D, 11.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(10.0D, 3.0D, 5.0D, 16.0D, 13.0D, 11.0D);

    @SuppressWarnings("incomplete-switch")
    public static VoxelShape getShape(AttachFace face, Direction direction) {
        if (!HitBox.hitboxLever.isEnabled()) return null;
        if (face == AttachFace.FLOOR) {
            return FLOOR_SHAPE;
        } else if (face == AttachFace.CEILING) {
            return CEILING_SHAPE;
        } else if (face == AttachFace.WALL) {
            switch (direction) {
                case EAST: return EAST_SHAPE;
                case WEST: return WEST_SHAPE;
                case SOUTH: return SOUTH_SHAPE;
                case NORTH: return NORTH_SHAPE;
            }
        }
        return null;
    }
}