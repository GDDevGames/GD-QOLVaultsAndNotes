package dev.gdawg.qolvaultsandnotes;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, QOLVaultsAndNotes.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BulletinBoardBlockEntity>> BULLETIN_BOARD_ENTITY =
            BLOCK_ENTITIES.register("bulletin_board", () ->
                    new BlockEntityType<>(BulletinBoardBlockEntity::new, Set.of(ModBlocks.BULLETIN_BOARD_BLOCK.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SafeBlockEntity>> SAFE_ENTITY =
            BLOCK_ENTITIES.register("safe", () ->
                    new BlockEntityType<>(SafeBlockEntity::new, Set.of(ModBlocks.SAFE_BLOCK.get())));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}