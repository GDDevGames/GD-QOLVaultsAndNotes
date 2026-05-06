/// ----- VaultBlock -----
/// Handles the non-entity vault block.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class VaultBlock extends BaseEntityBlock {

    public static final MapCodec<VaultBlock> CODEC = simpleCodec(VaultBlock::new);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public VaultBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(ACTIVATED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VaultBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof VaultBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new VaultMenu(id, inventory, blockEntity),
                Component.translatable("container.qolvaultsandnotes.vault")
            ), pos);
            blockEntity.startOpen(player);

            // Send full inventory to the client NOW, after the menu is open
            /*NonNullList<ItemStack> allItems = NonNullList.withSize(72, ItemStack.EMPTY);
            for (int i = 0; i < VaultBlockEntity.SIZE; i++) {
                allItems.set(i, blockEntity.getItem(i).copy());
            }*/
            /*PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                new VaultFullSyncPacket(allItems)
            );*/
        }
        //level.setBlock(pos, level.getBlockState(pos).setValue(ACTIVATED, true), 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            MultiblockDetector.breakVault(level, pos, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public void openFor(Level level, BlockPos pos, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof VaultBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new VaultMenu(id, inventory, blockEntity),
                Component.translatable("container.qolvaultsandnotes.vault")
                // Daniel: Component.translatable lets you grab from the lang/files with translations. important
            ), pos);
            blockEntity.startOpen(player);
            //level.setBlock(pos, level.getBlockState(pos).setValue(ACTIVATED, true), 3);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(ACTIVATED);
    }
}
