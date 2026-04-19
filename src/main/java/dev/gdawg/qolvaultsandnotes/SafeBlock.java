package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, ModBlockEntities.SAFE_ENTITY.get(), SafeBlockEntity::lidAnimateTick)
                : null;
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
        if (be instanceof SafeBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new ChestMenu(MenuType.GENERIC_9x2, id, inventory, blockEntity, 2),
                    Component.literal("Safe")
            ));
            level.setBlock(pos, state.setValue(ACTIVATED, true), 3);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state,
                                          Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.is(ModItems.KEY_ITEM.get())) {
            if (!heldItem.has(DataComponents.CUSTOM_NAME)) return InteractionResult.PASS;

            String code = heldItem.get(DataComponents.CUSTOM_NAME).getString();

            // Key = numeric only, max 18 characters
            if (!code.matches("\\d+") || code.length() > 18) {
                player.displayClientMessage(
                        Component.literal("Key code must be numeric, max 18 digits."), true);
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                be.setAssignedCode(code);
                be.setLockedWithKeycard(false); // key = pincode screen
                be.setChanged();
                heldItem.shrink(1);
                player.displayClientMessage(Component.literal("Code assigned."), true);
            }
            return InteractionResult.SUCCESS;

        } else if (heldItem.is(ModItems.KEYCARD_ITEM.get())) {
            if (!heldItem.has(DataComponents.CUSTOM_NAME)) return InteractionResult.PASS;

            String code = heldItem.get(DataComponents.CUSTOM_NAME).getString();

            // Keycard = any characters, no length limit
            if (code.isBlank()) return InteractionResult.PASS;

            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                be.setAssignedCode(code);
                be.setLockedWithKeycard(true); // keycard = serial screen
                be.setChanged();
                heldItem.shrink(1);
                player.displayClientMessage(Component.literal("Code assigned."), true);
            }
            return InteractionResult.SUCCESS;

        } else {
            // Not holding key/keycard — open the code entry screen or inventory
            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null && !be.getAssignedCode().isEmpty()) {
                    // Has a code set — open the appropriate PIN screen
                    // We send a packet to the client to open it, or handle via openMenu
                } else {
                    // No code set — open inventory directly
                    player.openMenu((MenuProvider) level.getBlockEntity(pos));
                }
            }
            return InteractionResult.SUCCESS;
        }
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
        MultiblockDetector.onSafePlaced(level, pos, state.getValue(FACING));
    }
}