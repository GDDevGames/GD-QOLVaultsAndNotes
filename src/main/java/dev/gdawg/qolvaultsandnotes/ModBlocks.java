/// ----- ModBlocks -----
/// Registers the new blocks for the game to recognize.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    // Block register
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(QOLVaultsAndNotes.MODID);

    // Register bulletin board
    public static final DeferredBlock<BulletinBoardBlock> BULLETIN_BOARD_BLOCK = BLOCKS.register("bulletin_board",
id -> new BulletinBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noOcclusion().strength(2f, 6f).requiresCorrectToolForDrops().randomTicks().setId(ResourceKey.create(Registries.BLOCK, id))));

    // Register safe
    public static final DeferredBlock<SafeBlock> SAFE_BLOCK = BLOCKS.register("safe",
id -> new SafeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).noOcclusion().strength(360f, 1600f).requiresCorrectToolForDrops().randomTicks().setId(ResourceKey.create(Registries.BLOCK, id))));

    // Register vault
    public static final DeferredBlock<VaultBlock> VAULT_BLOCK = BLOCKS.register("vault",
id -> new VaultBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noOcclusion().strength(1400f, 1600f).requiresCorrectToolForDrops().setId(ResourceKey.create(Registries.BLOCK, id))));

    // Register vault part
    public static final DeferredBlock<VaultPartBlock> VAULT_PART_BLOCK = BLOCKS.register("vault_part",
id -> new VaultPartBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noOcclusion().strength(1400f, 1600f).requiresCorrectToolForDrops().setId(ResourceKey.create(Registries.BLOCK, id))));

    // Send the new blocks to the event bus
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}