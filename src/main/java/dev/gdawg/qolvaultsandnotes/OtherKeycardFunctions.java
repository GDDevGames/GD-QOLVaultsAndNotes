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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.BlockHitResult;

import java.lang.reflect.Method;
import java.util.*;

public class OtherKeycardFunctions {

    private static final String LOCKED_PISTON_TAG = "LockedPistonPos";
    // Add this to OtherKeycardFunctions
    public static final Set<BlockPos> LOCKED_PISTONS = new HashSet<>();

    public static InteractionResult handleKeycard(ItemStack heldItem, BlockState state,
                                                  Level level, BlockPos pos, Player player,
                                                  BlockHitResult hit) {

        // --- NETHER PORTAL ---
        if (state.is(Blocks.OBSIDIAN)) {
            if (!level.isClientSide()) {
                // Search a wider area for connected portal blocks
                BlockPos portalPos = null;
                outer:
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos check = pos.offset(dx, dy, dz);
                            if (level.getBlockState(check).is(Blocks.NETHER_PORTAL)) {
                                portalPos = check;
                                break outer;
                            }
                        }
                    }
                }

                // Check item tag for a stored portal keyed to this obsidian position
                CompoundTag tag = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                String storeKey = "PortalBlocks_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();

                if (tag.contains(storeKey)) {
                    // Restore stored portal blocks
                    CompoundTag stored = tag.getCompoundOrEmpty(storeKey);
                    for (String key : stored.keySet()) {
                        String[] parts = key.split(",");
                        BlockPos p = new BlockPos(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]));
                        boolean xAxis = stored.getBooleanOr(key, false);
                        level.setBlock(p, Blocks.NETHER_PORTAL.defaultBlockState()
                                .setValue(NetherPortalBlock.AXIS,
                                        xAxis ? Direction.Axis.X : Direction.Axis.Z), 3);
                    }
                    tag.remove(storeKey);
                    heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    level.playSound(null, pos,
                            SoundEvents.BEACON_ACTIVATE,
                            SoundSource.BLOCKS, 0.8f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Portal restored."), true);
                    heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));

                } else if (portalPos != null) {
                    // Portal is lit — turn it off and store blocks keyed to this obsidian pos
                    Set<BlockPos> visited = new HashSet<>();
                    Queue<BlockPos> queue = new LinkedList<>();
                    queue.add(portalPos);

                    while (!queue.isEmpty() && visited.size() < 100) {
                        BlockPos current = queue.poll();
                        if (visited.contains(current)) continue;
                        if (!level.getBlockState(current).is(Blocks.NETHER_PORTAL)) continue;
                        visited.add(current);
                        for (Direction d : Direction.values()) queue.add(current.relative(d));
                    }

                    CompoundTag stored = new CompoundTag();
                    for (BlockPos p : visited) {
                        BlockState bs = level.getBlockState(p);
                        boolean xAxis = bs.getValue(NetherPortalBlock.AXIS) == Direction.Axis.X;
                        stored.putBoolean(p.getX() + "," + p.getY() + "," + p.getZ(), xAxis);
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                    tag.put(storeKey, stored);
                    heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    level.playSound(null, pos,
                            SoundEvents.BEACON_DEACTIVATE,
                            SoundSource.BLOCKS, 0.8f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Portal toggled off."), true);
                    heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));

                } else {
                    BlockPos insidePos = pos.relative(hit.getDirection());
                    Optional<PortalShape> shapeX = PortalShape.findEmptyPortalShape(
                            (LevelAccessor) level, insidePos, Direction.Axis.X);
                    Optional<PortalShape> shapeZ = PortalShape.findEmptyPortalShape(
                            (LevelAccessor) level, insidePos, Direction.Axis.Z);

                    if ((shapeX.isPresent() || shapeZ.isPresent()) && BaseFireBlock.canBePlacedAt(level, insidePos, player.getDirection())) {
                        BlockState fireState = BaseFireBlock.getState(level, insidePos);
                        level.setBlock(insidePos, fireState, 11);
                        level.gameEvent(player, GameEvent.BLOCK_PLACE, insidePos);
                        player.displayClientMessage(Component.literal("Portal lit."), true);
                        level.playSound(null, pos,
                                SoundEvents.BEACON_ACTIVATE,
                                SoundSource.BLOCKS, 0.8f, 1.0f
                        );
                        heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
                    } else {
                        player.displayClientMessage(Component.literal("No valid portal frame found."), true);
                    }
                }
            }
            level.playSound(null, pos,
                    SoundEvents.TRIDENT_RETURN,
                    SoundSource.BLOCKS, 1.2f, 0.5f
            );
            return InteractionResult.SUCCESS;
        }

        // --- PISTON ---
        if (state.is(Blocks.PISTON) || state.is(Blocks.STICKY_PISTON)) {
            if (!level.isClientSide()) {
                Direction facing = state.getValue(PistonBaseBlock.FACING);

                if (hit.getDirection() == facing) {
                    return InteractionResult.PASS;
                }

                PistonBaseBlock pistonBlock = (PistonBaseBlock) state.getBlock();

                if (LOCKED_PISTONS.contains(pos)) {
                    LOCKED_PISTONS.remove(pos);
                    try {
                        Method triggerEvent = PistonBaseBlock.class.getDeclaredMethod(
                                "triggerEvent", BlockState.class, Level.class, BlockPos.class, int.class, int.class);
                        triggerEvent.setAccessible(true);
                        // Pass extended=true state so the retract signal check passes
                        triggerEvent.invoke(pistonBlock,
                                state.setValue(PistonBaseBlock.EXTENDED, true),
                                level, pos, 1, facing.get3DDataValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    level.playSound(null, pos,
                            SoundEvents.PISTON_CONTRACT,
                            SoundSource.BLOCKS, 0.8f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Piston contracted."), true);
                } else {
                    LOCKED_PISTONS.add(pos);
                    try {
                        Method moveBlocks = PistonBaseBlock.class.getDeclaredMethod(
                                "moveBlocks", Level.class, BlockPos.class, Direction.class, boolean.class);
                        moveBlocks.setAccessible(true);
                        boolean success = (boolean) moveBlocks.invoke(state.getBlock(), level, pos, facing, true);
                        if (success) {
                            level.setBlock(pos, state.setValue(PistonBaseBlock.EXTENDED, true), 67);
                            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f,
                                    level.random.nextFloat() * 0.25f + 0.6f);
                            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(state));
                        } else {
                            LOCKED_PISTONS.remove(pos);
                        }
                    } catch (Exception e) {
                        LOCKED_PISTONS.remove(pos);
                        e.printStackTrace();
                    }
                    player.displayClientMessage(Component.literal("Piston extended."), true);
                }

                heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}