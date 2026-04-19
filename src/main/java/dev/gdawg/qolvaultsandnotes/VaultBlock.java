package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class VaultBlock extends BaseEntityBlock {

    public static final MapCodec<VaultBlock> CODEC = simpleCodec(VaultBlock::new);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public VaultBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /*@Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 32, 32, 32);
    }*/

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VaultBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                               BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof VaultBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new VaultMenu(id, inventory, blockEntity),
                    Component.translatable("container.qolvaultsandnotes.vault")
            ), pos);

            // Send full inventory to the client NOW, after the menu is open
            List<ItemStack> allItems = new ArrayList<>();
            for (int i = 0; i < VaultBlockEntity.SIZE; i++) {
                allItems.add(blockEntity.getItem(i).copy());
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    (ServerPlayer) player,
                    new VaultFullSyncPacket(allItems)
            );
        }
        return InteractionResult.SUCCESS;
    }

    /*@Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            // Drop all vault contents at the origin position
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VaultBlockEntity vault) {
                Containers.dropContents(level, pos, vault);
            }
            // Replace all 8 vault blocks back to safes
            MultiblockDetector.breakVault(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }*/
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            MultiblockDetector.breakVault(level, pos, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public void openFor(Level level, BlockPos pos, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        /*if (be instanceof VaultBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inventory, blockEntity, 6),
                    Component.literal("Vault")
            ));
        }*/
        if (be instanceof VaultBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new VaultMenu(id, inventory, blockEntity),
                    Component.translatable("container.qolvaultsandnotes.vault") //Component.translatable lets you grab from the lang/ files with translations. important
            ), pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
