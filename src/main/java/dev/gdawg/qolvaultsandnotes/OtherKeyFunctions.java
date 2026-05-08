// OtherKeyFunctions.java
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public class OtherKeyFunctions {

    public static InteractionResult handleKey(ItemStack heldItem, BlockState state,
                                              Level level, BlockPos pos, Player player,
                                              BlockHitResult hit) {

        // --- IRON DOOR ---
        if (state.is(Blocks.IRON_DOOR)) {
            if (!level.isClientSide()) {
                boolean isOpen = state.getValue(BlockStateProperties.OPEN);

                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, !isOpen), 3);

                BlockPos otherHalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                        ? pos.above()
                        : pos.below();

                BlockState otherState = level.getBlockState(otherHalf);
                if (otherState.is(Blocks.IRON_DOOR)) {
                    level.setBlock(otherHalf, otherState.setValue(BlockStateProperties.OPEN, !isOpen), 3);
                }

                level.playSound(null, pos,
                        !isOpen ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                        SoundSource.BLOCKS, 1.0f, 1.0f);

                heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
            }
            return InteractionResult.SUCCESS;
        }

        // --- TRAPPED CHEST ---
        if (state.is(Blocks.TRAPPED_CHEST)) {
            if (!level.isClientSide()) {
                // 1. Get the block entity FIRST before touching the block
                BlockEntity be = level.getBlockEntity(pos);

                if (!(be instanceof TrappedChestBlockEntity oldBE)) {
                    return InteractionResult.PASS; // Safety: no valid BE found
                }

                // 2. Save the inventory contents
                NonNullList<ItemStack> savedItems = NonNullList.withSize(oldBE.getContainerSize(), ItemStack.EMPTY);
                for (int i = 0; i < oldBE.getContainerSize(); i++) {
                    savedItems.set(i, oldBE.getItem(i).copy()); // .copy() is critical!
                }

                // 3. Build the new blockstate, mirroring facing AND waterlogged if present
                BlockState normalChest = Blocks.CHEST.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING,
                                state.getValue(BlockStateProperties.HORIZONTAL_FACING));

                // 4. Replace the block (this destroys the old BE)
                oldBE.clearContent();
                level.setBlock(pos, normalChest, 3);

                // 5. Restore inventory into the new ChestBlockEntity
                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe instanceof ChestBlockEntity newBE) {
                    for (int i = 0; i < savedItems.size(); i++) {
                        newBE.setItem(i, savedItems.get(i));
                    }
                }

                level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f); // change the sound here
                player.displayClientMessage(Component.literal("Trapped chest has been untrapped!"), true);
                heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
            }
            return InteractionResult.SUCCESS;
            }
        return InteractionResult.PASS;
    }
}