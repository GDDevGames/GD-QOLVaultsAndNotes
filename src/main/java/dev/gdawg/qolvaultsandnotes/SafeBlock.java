/// ----- SafeBlock -----
/// Handles the non-entity safe block.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    // --- CONSTRUCTOR ---
    public SafeBlock(Properties props) {
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
        return new SafeBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVATED, false);
    }

    // Check specific items being used on the safe
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state,
                                          Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {

        ItemStack heldItem = player.getItemInHand(hand);

        // -- KEY --
        if (heldItem.is(ModItems.KEY_ITEM.get())) {
            // Server-side functionality.
            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    // Is there already a code assigned to the lock?
                    if (!be.getAssignedCode().isEmpty()) {
                        player.displayClientMessage(Component.literal("A code is already assigned."), true);
                        return InteractionResult.FAIL;
                    }

                    // In order to lock the safe with a code, a lock must be used on it first
                    if (!be.isLocked()) {
                        player.displayClientMessage(Component.literal("Use a lock first."), true);
                        return InteractionResult.FAIL;
                    }

                    // Check that the item has a custom name, if it does then use it
                    if (!heldItem.has(DataComponents.CUSTOM_NAME)) {
                        player.displayClientMessage(Component.literal("Name the key the serial code to assign."), true);
                        return InteractionResult.FAIL;
                    }
                    StringBuilder code = new StringBuilder(heldItem.get(DataComponents.CUSTOM_NAME).getString());

                    // Validate that the name is an actual code
                    if (!code.toString().matches("[.\\-]+") || code.length() > 18) {
                        player.displayClientMessage(
                                Component.literal("Serial code is limited to 18 characters, consisting of . and -"), true);
                        return InteractionResult.FAIL;
                    }

                    // If the name is shorter than 18 characters, the remaining code should be blank (fill with _)
                    if(code.length() < 18) {
                        int blankAmount = 18 - code.length();
                        for(int i =  0; i < blankAmount; i++) {
                            code.append("-");
                        }
                    }

                    // If not, assign our new code
                    be.setAssignedCode(code.toString());
                    be.setLockedWithKeycard(false);
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Code assigned."), true);

                    // Play the sound effect for the successful code assignment, and swing hand (SUCCESS_SERVER)
                    level.playSound(null, pos,
                            SoundEvents.ARMOR_EQUIP_NETHERITE.value(),
                            SoundSource.BLOCKS, 1.0f, 1.0f
                    );
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }

        // --- KEYCARD ---
        else if (heldItem.is(ModItems.KEYCARD_ITEM.get())) {
            // Server-side functionality.
            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    // Is there already a code assigned to the lock?
                    if (!be.getAssignedCode().isEmpty()) {
                        player.displayClientMessage(Component.literal("A code is already assigned."), true);
                        return InteractionResult.FAIL;
                    }

                    // In order to lock the safe with a code, a lock must be used on it first
                    if (!be.isLocked()) {
                        player.displayClientMessage(Component.literal("Use a lock first."), true);
                        return InteractionResult.FAIL;
                    }

                    // Check that the item has a custom name, if it does then use it
                    if (!heldItem.has(DataComponents.CUSTOM_NAME)) {
                        player.displayClientMessage(Component.literal("Name the keycard the pin code to assign."), true);
                        return InteractionResult.FAIL;
                    }
                    String code = heldItem.get(DataComponents.CUSTOM_NAME).getString();

                    // Validate that the name is an actual code
                    if (!code.matches("[0-9]+")) {
                        player.displayClientMessage(
                                Component.literal("Pin code must only contain digits."), true);
                        return InteractionResult.FAIL;
                    }

                    // If not, assign our new code
                    be.setAssignedCode(code);
                    be.setLockedWithKeycard(true);
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Code assigned."), true);

                    // Play the sound effect for the successful code assignment, and swing hand (SUCCESS_SERVER)
                    level.playSound(null, pos,
                            SoundEvents.ENCHANTMENT_TABLE_USE,
                            SoundSource.BLOCKS, 1.0f, 1.0f
                    );
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }

        // --- LOCK ---
        else if (heldItem.is(ModItems.LOCK_ITEM.get())) {
            // Server-side functionality.
            if (!level.isClientSide()) {
                SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    // Is there already a lock?
                    if (be.isLocked()) {
                        player.displayClientMessage(Component.literal("Already locked."), true);
                        return InteractionResult.FAIL;
                    }
                    be.setLocked(true);
                    be.setLockOwner(player.getUUID());
                    be.setChanged();
                    heldItem.shrink(1);
                    level.playSound(null, pos,
                            SoundEvents.ARMOR_EQUIP_CHAIN.value(),
                            SoundSource.BLOCKS, 1.0f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Safe locked."), true);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }

        // Server goes here when none of the items above were used. Client always goes here.
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                               BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.PASS;

        SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(pos);
        if (be == null) return InteractionResult.FAIL;

        boolean hasCode = !be.getAssignedCode().isEmpty();
        boolean locked = be.isLocked();

        // If it's not locked, open
        if(!locked) {
            openFor(player, be);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!player.getUUID().equals(be.getLockOwner())) {
            // If the safe has no code, only the player who locked it can open it
            if (!hasCode){
                player.displayClientMessage(Component.literal("This safe is locked."), true);
                return InteractionResult.FAIL;
            }
            // If it does have a code, open the safe GUI
            else {
                openSafeScreen(be, pos, player);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        // If the player owns the lock, still ask for a code if it has one
        else {
            if (hasCode) {
                openSafeScreen(be, pos, player);
                return InteractionResult.SUCCESS_SERVER;
            }
            // Otherwise allow opening the safe
            else {
                openFor(player, be);
                return InteractionResult.SUCCESS_SERVER;
            }
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