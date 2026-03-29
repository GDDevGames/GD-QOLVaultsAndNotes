package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockDetector {

    private static final int SIZE = 2;

    public static void onSafePlaced(Level level, BlockPos placedPos, Direction facing) {
        if (level.isClientSide()) return;

        /*YO! NOTE:
         * WE NEED TO ADD THAT THE SAFES IN THE 2x2x2 FORMATION HAVE A MAXIMUM
         * OF 72 ITEM SLOTS TAKEN UP!!! 72
         * MWAH MUCH LOVE GOD BLESS YOU
         * - GD
         * */

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

    private static boolean isCompleteFormation(Level level, BlockPos origin) {
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++)
                for (int z = 0; z < SIZE; z++)
                    if (!level.getBlockState(origin.offset(x, y, z)).is(ModBlocks.SAFE_BLOCK.get()))
                        return false;
        return true;

    }

    public static void formVault(Level level, BlockPos origin, Direction facing) {
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
    }

    public static void breakVault(Level level, BlockPos origin) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(ModBlocks.VAULT_BLOCK.get()) || state.is(ModBlocks.VAULT_PART_BLOCK.get())) {
                        level.setBlock(pos, ModBlocks.SAFE_BLOCK.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

}
