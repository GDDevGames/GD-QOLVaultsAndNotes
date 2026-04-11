package dev.gdawg.qolvaultsandnotes;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QOLVaultsAndNotes.MODID);

    public static final DeferredItem<BlockItem> BULLETIN_BOARD_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.BULLETIN_BOARD_BLOCK);
    public static final DeferredItem<BlockItem> SAFE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SAFE_BLOCK);

    public static final DeferredItem<Item> LOCK_ITEM = ITEMS.registerSimpleItem("lock");
    public static final DeferredItem<Item> KEY_ITEM = ITEMS.registerSimpleItem("key");
    public static final DeferredItem<Item> KEYCARD_ITEM = ITEMS.registerSimpleItem("keycard");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}