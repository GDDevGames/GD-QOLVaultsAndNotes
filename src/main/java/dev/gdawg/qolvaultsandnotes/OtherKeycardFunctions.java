/// ----- OtherKeycardFunctions -----
/// Misc. keycard functions (toggling pistons & nether portals)
/// ------------------------------------
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
    // This gets handled in PistonOverrideEvent
    public static final Set<BlockPos> LOCKED_PISTONS = new HashSet<>();

    public static InteractionResult handleKeycard(ItemStack heldItem, BlockState state,
                                                  Level level, BlockPos pos, Player player,
                                                  BlockHitResult hit) {

        // --- NETHER PORTAL ---
        if (state.is(Blocks.OBSIDIAN)) {
            if (!level.isClientSide()) {
                // First check around for any portal blocks
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

                // Check item tag for a stored portal linked to this obsidian position
                CompoundTag tag = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                String storeKey = "PortalBlocks_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();

                // If the keycard has linked portal blocks with this portal, restore them
                if (tag.contains(storeKey)) {
                    CompoundTag stored = tag.getCompoundOrEmpty(storeKey);
                    // Unpack all the stored blocks and parse the information
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
                    // Remove the linked portals because we've just restored them
                    tag.remove(storeKey);
                    heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    level.playSound(null, pos,
                            SoundEvents.BEACON_ACTIVATE,
                            SoundSource.BLOCKS, 0.8f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Portal restored."), true);
                    heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
                }
                // If there are portal blocks around the obsidian, then there's probably a lit portal
                else if (portalPos != null) {
                    Set<BlockPos> visited = new HashSet<>();
                    Queue<BlockPos> queue = new LinkedList<>();
                    queue.add(portalPos);

                    // Visit up to 100 blocks and log if that has a portal block or not
                    while (!queue.isEmpty() && visited.size() < 100) {
                        BlockPos current = queue.poll();
                        // Both statements down below will confirm if we're not currently on a portal block.
                        if (visited.contains(current)) continue; // Have we already visited this block?
                        if (!level.getBlockState(current).is(Blocks.NETHER_PORTAL)) continue; // Does this block not contain a portal?

                        // If neither statement above is true, add the current block to our visited portal list.
                        visited.add(current);
                        for (Direction d : Direction.values()) queue.add(current.relative(d)); // Save the correct axis
                    }

                    CompoundTag stored = new CompoundTag();
                    // For each visited block
                    for (BlockPos p : visited) {
                        BlockState bs = level.getBlockState(p);
                        boolean xAxis = bs.getValue(NetherPortalBlock.AXIS) == Direction.Axis.X; // Get the axis the portal is facing
                        stored.putBoolean(p.getX() + "," + p.getY() + "," + p.getZ(), xAxis);
                        // Finally, replace the current block with air and continue until we're out of blocks to replace.
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }

                    // Save the blocks we've just replaced
                    tag.put(storeKey, stored);
                    heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    level.playSound(null, pos,
                            SoundEvents.BEACON_DEACTIVATE,
                            SoundSource.BLOCKS, 0.8f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Portal toggled off."), true);
                    heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));

                }
                // Otherwise it's valid to assume the portal is unlit with no saved state
                else {
                    BlockPos insidePos = pos.relative(hit.getDirection());
                    Optional<PortalShape> shapeX = PortalShape.findEmptyPortalShape(
                            (LevelAccessor) level, insidePos, Direction.Axis.X);
                    Optional<PortalShape> shapeZ = PortalShape.findEmptyPortalShape(
                            (LevelAccessor) level, insidePos, Direction.Axis.Z);

                    // Recreate normally lighting a portal as if it was with flint & steel
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

            // Play the default interaction sound
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
                // If the piston we're looking at has been logged, it's probably activated
                if (LOCKED_PISTONS.contains(pos)) {
                    LOCKED_PISTONS.remove(pos);

                    // Now we have to notify the block that it's being retracted
                    try {
                        Method triggerEvent = PistonBaseBlock.class.getDeclaredMethod(
                                "triggerEvent", BlockState.class, Level.class, BlockPos.class, int.class, int.class);
                        triggerEvent.setAccessible(true);
                        triggerEvent.invoke(pistonBlock,
                                state.setValue(PistonBaseBlock.EXTENDED, true),
                                level, pos, 1, facing.get3DDataValue()
                        );
                    } catch (Exception e) {
                        // There's a chance it could fail because technically we're overriding redstone
                        // signals. We can't handle that so just print the stack trace if it errors out
                        e.printStackTrace();
                    }

                    // Play the piston sound
                    level.playSound(null, pos,
                            SoundEvents.PISTON_CONTRACT,
                            SoundSource.BLOCKS, 0.8f, 1.0f
                    );
                    player.displayClientMessage(Component.literal("Piston contracted."), true);
                }
                // If not, the piston is inactive
                else {
                    // Log the position of the extended piston so we know that it's actually active
                    LOCKED_PISTONS.add(pos);

                    // Now we have to try and move the block in front of the piston forward like normal
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
                        // If there was an error, we have the luxury of being able to undo everything and
                        // act as if the piston never tried to be extended in the first place.
                        // Along with a stack trace.
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