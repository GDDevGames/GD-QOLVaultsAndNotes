package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CreateBlock extends Block {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CreateBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> Block.box(0, 0, 13, 16, 16, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 3);
            case EAST -> Block.box(0, 0, 0, 3, 16, 16);
            case WEST -> Block.box(13, 0, 0, 16, 16, 16);
            default -> Block.box(0, 0, 13, 16, 16, 16);
        };
    }
    // Block.box(x1, y1, z1, x2, y2, z2)
    // x1, y1, z1 — the starting corner (minimum point)
    // x2, y2, z2 — the ending corner (maximum point)
    // X goes 0 to 16 — full width
    // Y goes 0 to 16 — full height
    // Z goes 0 to 3 — only 3 units deep from the south face
}