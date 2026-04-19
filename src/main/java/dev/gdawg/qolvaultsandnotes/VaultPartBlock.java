package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VaultPartBlock extends Block {
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 1);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 1);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 1);

    public VaultPartBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OFFSET_X, 0)
                .setValue(OFFSET_Y, 0)
                .setValue(OFFSET_Z, 0));
    }

    /*@Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }*/

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    public static BlockPos getOrigin(BlockPos partPos, BlockState state) {
        return partPos.offset(
                -state.getValue(OFFSET_X),
                -state.getValue(OFFSET_Y),
                -state.getValue(OFFSET_Z)
        );
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos origin = getOrigin(pos, state);
            MultiblockDetector.breakVault(level, origin, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel)) return InteractionResult.CONSUME;
        BlockPos origin = getOrigin(pos, state);
        BlockState masterState = level.getBlockState(origin);
        if (masterState.getBlock() instanceof VaultBlock vault) {
            vault.openFor(level, origin, player);
        }
        return InteractionResult.SUCCESS;
    }
}
