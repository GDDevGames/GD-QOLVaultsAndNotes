/// ----- VaultBlock -----
/// Handles the non-entity vault block.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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

public class VaultBlock extends BaseEntityBlock {

    public static final MapCodec<VaultBlock> CODEC = simpleCodec(VaultBlock::new);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // --- CONSTRUCTOR ---
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

    // Check specific items being used on the safe
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state,
                                          Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {

        ItemStack heldItem = player.getItemInHand(hand);

        // -- KEY --
        if (heldItem.is(ModItems.KEY_ITEM.get())) {
            // Check that the item has a custom name, if it does then use it
            if (!heldItem.has(DataComponents.CUSTOM_NAME)) return InteractionResult.PASS;
            StringBuilder code = new StringBuilder(heldItem.get(DataComponents.CUSTOM_NAME).getString());

            // Validate that the name is an actual code
            if (!code.toString().matches("[.\\-]+") || code.length() > 18) {
                player.displayClientMessage(
                        Component.literal("Serial code must only contain . and -, max 18 characters."), true);
                return InteractionResult.PASS;
            }

            // If the name is shorter than 18 characters, the remaining code is blank
            if(code.length() < 18) {
                int blankAmount = 18 - code.length();
                for(int i =  0; i < blankAmount; i++) {
                    code.append("-");
                }
            }

            // Now handle assigning the code
            if (!level.isClientSide()) {
                VaultBlockEntity be = (VaultBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    // In order to lock the safe with a code, a lock must be used on it first
                    if (!be.isLocked()) {
                        player.displayClientMessage(Component.literal("Use a lock first."), true);
                        return InteractionResult.PASS;
                    }
                    // Is there already a code assigned to the lock?
                    if (!be.getAssignedCode().isEmpty()) {
                        player.displayClientMessage(Component.literal("A code is already assigned."), true);
                        return InteractionResult.PASS;
                    }
                    // If not, assign our new code
                    be.setAssignedCode(code.toString());
                    be.setLockedWithKeycard(false);
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Code assigned."), true);
                }
            }
            level.playSound(null, pos,
                    SoundEvents.ARMOR_EQUIP_NETHERITE.value(),
                    SoundSource.BLOCKS, 1.0f, 1.0f
            );
            return InteractionResult.SUCCESS;

        }
        // --- KEYCARD ---
        else if (heldItem.is(ModItems.KEYCARD_ITEM.get())) {
            // Check that the item has a custom name, if it does then use it
            if (!heldItem.has(DataComponents.CUSTOM_NAME)) return InteractionResult.PASS;
            String code = heldItem.get(DataComponents.CUSTOM_NAME).getString();

            // Is there no code in the name?
            if (code.isBlank()) return InteractionResult.PASS;

            if (!level.isClientSide()) {
                VaultBlockEntity be = (VaultBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    // In order to lock the safe with a code, a lock must be used on it first
                    if (!be.isLocked()) {
                        player.displayClientMessage(Component.literal("Use a lock first."), true);
                        return InteractionResult.PASS;
                    }
                    // Is there already a code assigned to the lock?
                    if (!be.getAssignedCode().isEmpty()) {
                        player.displayClientMessage(Component.literal("A code is already assigned."), true);
                        return InteractionResult.PASS;
                    }
                    // If not, assign our new code
                    be.setAssignedCode(code);
                    be.setLockedWithKeycard(true);
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Code assigned."), true);
                }
            }
            level.playSound(null, pos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0f, 1.0f
            );
            return InteractionResult.SUCCESS;
        }
        // --- LOCK ---
        else if (heldItem.is(ModItems.LOCK_ITEM.get())) {
            if (!level.isClientSide()) {
                VaultBlockEntity be = (VaultBlockEntity) level.getBlockEntity(pos);
                if (be != null) {
                    // Is there already a lock?
                    if (be.isLocked()) {
                        player.displayClientMessage(Component.literal("Vault locked."), true);
                        return InteractionResult.PASS;
                    }
                    be.setLocked(true);
                    be.setLockOwner(player.getUUID());
                    be.setChanged();
                    heldItem.shrink(1);
                    player.displayClientMessage(Component.literal("Vault locked."), true);
                }
            }
            level.playSound(null, pos,
                    SoundEvents.ARMOR_EQUIP_CHAIN.value(),
                    SoundSource.BLOCKS, 1.0f, 1.0f
            );
            return InteractionResult.SUCCESS;
        }

        return useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                               BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.CONSUME;

        VaultBlockEntity be = (VaultBlockEntity) level.getBlockEntity(pos);
        if (be == null) return InteractionResult.PASS;

        boolean hasCode = !be.getAssignedCode().isEmpty();
        boolean locked = be.isLocked();

        // If it's not locked, open
        if(!locked) {
            openFor(level, pos, player);
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(be.getLockOwner())) {
            // If the safe has no code, only the player who locked it can open it
            if (!hasCode){
                player.displayClientMessage(Component.literal("This vault is locked."), true);
                return InteractionResult.SUCCESS;
            }
            // If it does have a code, open the safe GUI
            else {
                openSafeScreen(be, pos, player);
            }
        }
        // If the player who owns the lock on the safe is opening it, still ask for a code if it has one
        else {
            if (hasCode) {
                openSafeScreen(be, pos, player);
            }
            // TODO: Remove
            // Alvin: This wasn't here in SafeBlock. Why is it here in VaultBlock?
            else {
                openFor(level, pos, player);
            };
            return InteractionResult.SUCCESS;
        }

        // If not, then just open it
        openFor(level, pos, player);
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
        }
    }

    private void openSafeScreen(VaultBlockEntity be, BlockPos pos, Player player) {
        PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                new OpenSafeScreenPacket(pos, be.isLockedWithKeycard())
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(ACTIVATED);
    }
}
