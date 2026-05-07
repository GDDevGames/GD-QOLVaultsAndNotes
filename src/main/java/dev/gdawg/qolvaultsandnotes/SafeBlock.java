package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class SafeBlock extends BaseEntityBlock {
    public static final MapCodec<SafeBlock> CODEC = simpleCodec(SafeBlock::new);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SafeBlock(Properties props) {
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
        return new SafeBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVATED, false);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state,
                                          Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.is(ModItems.KEY_ITEM.get())) {
            if (!heldItem.has(DataComponents.CUSTOM_NAME)) return InteractionResult.PASS;  // ← checks name exists
            String code = heldItem.get(DataComponents.CUSTOM_NAME).getString();             // ← reads the name

            if (!code.matches("[.\\-]+") || code.length() > 18) {
                player.displayClientMessage(
                        Component.literal("Serial code must only contain . and -, max 18 characters."), true);
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    if (!be.isLocked()) {
                        player.displayClientMessage(Component.literal("Use a lock first."), true);
                        return InteractionResult.PASS;
                    }
                    if (!be.getAssignedCode().isEmpty()) {
                        player.displayClientMessage(Component.literal("A code is already assigned."), true);
                        return InteractionResult.PASS;
                    }
                    be.setAssignedCode(code);
                    be.setLockedWithKeycard(false);
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Code assigned."), true);
                }
            }
            return InteractionResult.SUCCESS;

        } else if (heldItem.is(ModItems.KEYCARD_ITEM.get())) {
            if (!heldItem.has(DataComponents.CUSTOM_NAME)) return InteractionResult.PASS;

            String code = heldItem.get(DataComponents.CUSTOM_NAME).getString();

            if (code.isBlank()) return InteractionResult.PASS;

            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    if (!be.isLocked()) {
                        player.displayClientMessage(Component.literal("Use a lock first."), true);
                        return InteractionResult.PASS;
                    }
                    if (!be.getAssignedCode().isEmpty()) {
                        player.displayClientMessage(Component.literal("A code is already assigned."), true);
                        return InteractionResult.PASS;
                    }
                    be.setAssignedCode(code);
                    be.setLockedWithKeycard(true);
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Code assigned."), true);
                }
            }
            return InteractionResult.SUCCESS;

        } else if (heldItem.is(ModItems.LOCK_ITEM.get())) {
            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    if (be.isLocked()) {
                        player.displayClientMessage(Component.literal("Already locked."), true);
                        return InteractionResult.PASS;
                    }
                    be.setLocked(true);
                    be.setLockOwner(player.getUUID());
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Safe locked."), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Anything else — fall through to useWithoutItem
        return useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                               BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.CONSUME;

        SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
        if (be == null) return InteractionResult.PASS;

        boolean hasCode = !be.getAssignedCode().isEmpty();
        boolean locked = be.isLocked();

        //if it's not unlocked, open. otherwise the code underneath this will run.
        if(!locked) {
            openFor(player, be);
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(be.getLockOwner())) {
            if(!hasCode){
                player.displayClientMessage(Component.literal("This safe is locked."), true);
                return InteractionResult.SUCCESS;
            } else {
                openSafeScreen(be, pos, player);
            }
        } else {
            //IF we want even the player who locked the safe to have to see the code screen, then this should run. Otherwise we should remove what's underneath.
            openSafeScreen(be, pos, player);
        }
        return InteractionResult.SUCCESS;

    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(1, 0, 1, 15, 16, 15);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        Player player = (Player) placer;
        MultiblockDetector.onSafePlaced(level, pos, state.getValue(FACING), player);

    }

    public void openFor(Player player, SafeBlockEntity be) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new ChestMenu(MenuType.GENERIC_9x2, id, inventory, be, 2),
                Component.translatable("container.qolvaultsandnotes.safe")
        ));
    }

    private void openSafeScreen(SafeBlockEntity be, BlockPos pos, Player player) {
        PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                new OpenSafeScreenPacket(pos, be.isLockedWithKeycard())
        );
    }
}