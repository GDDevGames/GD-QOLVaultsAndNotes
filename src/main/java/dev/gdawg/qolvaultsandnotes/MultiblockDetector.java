/// ----- MultiBlockDetector -----
/// Checks and spawns vault blocks. Vaults are only created by making a 2x2x2 cube of safe blocks.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class MultiblockDetector {
    private static final int SIZE = 2;
    public static void onSafePlaced(Level level, BlockPos placedPos, Direction facing) {
        if (level.isClientSide()) return;
        // Check to see if there are any candidates for creating a vault around the block
        for (int dx = 0; dx > -2; dx--) {
            for (int dy = 0; dy > -2; dy--) {
                for (int dz = 0; dz > -2; dz--) {
                    BlockPos candidate = placedPos.offset(dx, dy, dz);
                    if (isCompleteFormation(level, candidate)) {
                        formVault(level, candidate, facing);
                        return;
                    }
                }
            }
        }
    }

    // Validate if we should create a vault
    private static boolean isCompleteFormation(Level level, BlockPos origin) {
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++)
                for (int z = 0; z < SIZE; z++)
                    if (!level.getBlockState(origin.offset(x, y, z)).is(ModBlocks.SAFE_BLOCK.get()))
                        return false;
        return true;
    }

    public static void formVault(Level level, BlockPos origin, Direction facing) {
        // Collect all items from all 8 safes
        List<ItemStack> collectedItems = new ArrayList<>();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof SafeBlockEntity safe) {
                        for (int i = 0; i < safe.getContainerSize(); i++) {
                            ItemStack stack = safe.getItem(i);
                            if (!stack.isEmpty()) {
                                collectedItems.add(stack.copy());
                            }
                        }
                        safe.clearContent();
                    }
                }
            }
        }
        // Place the vault part blocks
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState newState;
                    if (x == 0 && y == 0 && z == 0) {
                        newState = ModBlocks.VAULT_BLOCK.get().defaultBlockState()
                                .setValue(VaultBlock.FACING, facing);
                    } else {
                        newState = ModBlocks.VAULT_PART_BLOCK.get().defaultBlockState()
                                .setValue(VaultPartBlock.OFFSET_X, x)
                                .setValue(VaultPartBlock.OFFSET_Y, y)
                                .setValue(VaultPartBlock.OFFSET_Z, z);
                    }
                    level.setBlock(pos, newState, 3);
                }
            }
        }
        // Fill vault with up to 72 items, drop the rest
        BlockEntity be = level.getBlockEntity(origin);
        if (be instanceof VaultBlockEntity vault) {
            int slotIndex = 0;
            for (ItemStack stack : collectedItems) {
                if (slotIndex >= VaultBlockEntity.SIZE) {
                    net.minecraft.world.Containers.dropItemStack(
                            level, origin.getX(), origin.getY(), origin.getZ(), stack);
                } else {
                    vault.setItem(slotIndex, stack);
                    slotIndex++;
                }
            }
        }
    }

    public static void breakVault(Level level, BlockPos origin, BlockPos brokenPos) {
        // Collect all items from the vault
        List<ItemStack> vaultItems = new ArrayList<>();
        BlockEntity vaultBe = level.getBlockEntity(origin);
        if (vaultBe instanceof VaultBlockEntity vault) {
            for (int i = 0; i < vault.getContainerSize(); i++) {
                ItemStack stack = vault.getItem(i);
                if (!stack.isEmpty()) {
                    vaultItems.add(stack.copy());
                }
            }
            vault.clearContent();
        }
        // Replace 7 blocks back to safes, skipping the broken vault part
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (pos.equals(brokenPos)) continue; // skip broken block
                    BlockState state = level.getBlockState(pos);
                    if (state.is(ModBlocks.VAULT_BLOCK.get()) || state.is(ModBlocks.VAULT_PART_BLOCK.get())) {
                        level.setBlock(pos, ModBlocks.SAFE_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
        // Distribute items into the 7 remaining safes
        int itemIndex = 0;
        outer:
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (pos.equals(brokenPos)) continue; // skip broken block
                    BlockEntity safeBe = level.getBlockEntity(pos);
                    // Move the items into the selected safe entity until we can't
                    if (safeBe instanceof SafeBlockEntity safe) {
                        for (int slot = 0; slot < safe.getContainerSize(); slot++) {
                            if (itemIndex >= vaultItems.size()) break outer;
                            safe.setItem(slot, vaultItems.get(itemIndex));
                            itemIndex++;
                        }
                    }
                }
            }
        }
    }
}
