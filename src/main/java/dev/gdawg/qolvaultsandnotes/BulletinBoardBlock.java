/// ----- BulletinBoardBlock -----
/// Handles block functions and item interactions for the bulletin board.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    // --- CONSTRUCTOR ---
    public BulletinBoardBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(ACTIVATED, false)
        );
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
        BulletinBoardBlockEntity be = (BulletinBoardBlockEntity)level.getBlockEntity(pos);
        if (be instanceof BulletinBoardBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new BulletinBoardMenu(id, inventory, blockEntity),
                    Component.literal("")
            ), pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BulletinBoardBlockEntity be = (BulletinBoardBlockEntity)level.getBlockEntity(pos);
        ItemStack heldItem = player.getItemInHand(hand);

        // Using an ink sac on the board
        if (heldItem.is(Items.INK_SAC.asItem())) {
            int fillableAmount = 64 - be.getItem(0).getCount();

            // If the amount that can be filled is more or equal to the amount you have
            if (fillableAmount >= heldItem.getCount()) {
                be.setItem(0, new ItemStack(heldItem.getItem(), heldItem.getCount() + be.getItem(0).getCount()));
                heldItem.shrink(heldItem.getCount());
                level.playSound(null, pos,
                        SoundEvents.INK_SAC_USE,
                        SoundSource.BLOCKS, 1.0f, 1.0f
                );
                if(be.getItem(0).getCount() == 64)
                {
                    player.displayClientMessage(Component.literal("This bulletin board is now filled with ink sacs."), true);
                }
                return InteractionResult.SUCCESS;
            }

            // If you cannot fill it anymore
            else if (fillableAmount == 0)
            {
                player.displayClientMessage(Component.literal("There is no space for ink sacs in this bulletin board."), true);
                return InteractionResult.FAIL;
            }

            // If the amount that can be filled is less than the amount that you have
            else {
                be.setItem(0, new ItemStack(heldItem.getItem(), 64));
                heldItem.shrink(fillableAmount);
                player.displayClientMessage(Component.literal("This bulletin board is now filled with ink sacs."), true);

                level.playSound(null, pos,
                        SoundEvents.INK_SAC_USE,
                        SoundSource.BLOCKS, 1.0f, 1.0f
                );
                return InteractionResult.SUCCESS;
            }

        }

        // Using paper on the board
        else if (heldItem.is(Items.PAPER.asItem())) {
            int fillableAmount = 64 - be.getItem(1).getCount();

            // If the amount that can be filled is more or equal to the amount you have
            if (fillableAmount >= heldItem.getCount()) {
                be.setItem(1, new ItemStack(heldItem.getItem(), heldItem.getCount() + be.getItem(1).getCount()));
                heldItem.shrink(heldItem.getCount());

                level.playSound(null, pos,
                        SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 1.0f, 1.0f
                );
                if(be.getItem(1).getCount() == 64)
                {
                    player.displayClientMessage(Component.literal("This bulletin board is now filled with paper."), true);
                }
                return InteractionResult.SUCCESS;
            }

            // If you cannot fill it anymore
            else if (fillableAmount == 0)
            {
                player.displayClientMessage(Component.literal("There is no space for paper in this bulletin board."), true);
                return InteractionResult.FAIL;
            }

            // If the amount that can be filled is less than the amount that you have
            else {
                be.setItem(1, new ItemStack(heldItem.getItem(), 64));
                heldItem.shrink(fillableAmount);
                player.displayClientMessage(Component.literal("This bulletin board is now filled with paper."), true);
                level.playSound(null, pos,
                        SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 1.0f, 1.0f
                );
                return InteractionResult.SUCCESS;
            }
        }

        // Return the super class (it returns InteractionResult.TRY_WITH_EMPTY_HAND leading us to useWithoutItem()
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
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