package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BulletinBoardBlock extends Block implements EntityBlock {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public BulletinBoardBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BulletinBoardBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVATED, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.CONSUME;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BulletinBoardBlockEntity blockEntity) {
            /*player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new ChestMenu(MenuType.GENERIC_9x2, id, inventory, blockEntity, 2),
                    Component.literal("Bulletin Board")
            ));*/
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new BulletinBoardMenu(id, inventory, blockEntity),
                    Component.literal("")
            ), pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED);
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
}