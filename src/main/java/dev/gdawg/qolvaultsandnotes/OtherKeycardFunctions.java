// OtherKeycardFunctions.java
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class OtherKeycardFunctions {

    private static final String LOCKED_PISTON_TAG = "LockedPistonPos";

    public static InteractionResult handleKeycard(ItemStack heldItem, BlockState state,
                                                  Level level, BlockPos pos, Player player,
                                                  BlockHitResult hit) {

        // --- NETHER PORTAL ---
        if (state.is(Blocks.NETHER_PORTAL)) {
            if (!level.isClientSide()) {
                if (toggleConnectedPortal(level, pos)) {
                    level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.5f, 1.5f);
                    player.displayClientMessage(Component.literal("Portal toggled."), true);
                    heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        // --- END PORTAL ---
        if (state.is(Blocks.END_PORTAL)) {
            if (!level.isClientSide()) {
                if (toggleConnectedEndPortal(level, pos)) {
                    level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.5f, 1.5f);
                    player.displayClientMessage(Component.literal("Portal toggled."), true);
                    heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        // --- PISTON ---
        if (state.is(Blocks.PISTON) || state.is(Blocks.STICKY_PISTON)) {
            if (!level.isClientSide()) {
                CompoundTag tag = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();

                if (tag.contains(LOCKED_PISTON_TAG) && tag.getString(LOCKED_PISTON_TAG).equals(posKey)) {
                    tag.remove(LOCKED_PISTON_TAG);
                    level.setBlock(pos, state.setValue(BlockStateProperties.EXTENDED, false), 3);
                    player.displayClientMessage(Component.literal("Piston override released."), true);
                } else {
                    tag.putString(LOCKED_PISTON_TAG, posKey);
                    level.setBlock(pos, state.setValue(BlockStateProperties.EXTENDED, true), 3);
                    player.displayClientMessage(Component.literal("Piston locked extended."), true);
                }

                heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0f, 1.0f);
                heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static boolean toggleConnectedPortal(Level level, BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < 100) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            if (!level.getBlockState(current).is(Blocks.NETHER_PORTAL)) continue;
            visited.add(current);
            for (Direction d : Direction.values()) queue.add(current.relative(d));
        }

        if (visited.isEmpty()) return false;
        for (BlockPos p : visited) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    private static boolean toggleConnectedEndPortal(Level level, BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < 100) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            if (!level.getBlockState(current).is(Blocks.END_PORTAL)) continue;
            visited.add(current);
            for (Direction d : Direction.values()) queue.add(current.relative(d));
        }

        if (visited.isEmpty()) return false;
        for (BlockPos p : visited) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }
}