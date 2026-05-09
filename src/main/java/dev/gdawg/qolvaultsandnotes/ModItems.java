/// ----- ModItems -----
/// Registers the new items for the game to recognize.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // Item register
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QOLVaultsAndNotes.MODID);

    // Register bulletin board
    public static final DeferredItem<BlockItem> BULLETIN_BOARD_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.BULLETIN_BOARD_BLOCK);

    // Register safe
    public static final DeferredItem<BlockItem> SAFE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SAFE_BLOCK);

    // Register lock
    public static final DeferredItem<Item> LOCK_ITEM = ITEMS.registerSimpleItem("lock");

    // Key and Keycard use custom classes because of the required useOn method
    // Register key
    public static final DeferredItem<KeyItem> KEY_ITEM = ITEMS.registerItem(
            "key",
            KeyItem::new, // The factory that the properties will be passed into.
            props -> props // A unary operator of the properties to use.
    );

    // Register keycard
    public static final DeferredItem<KeycardItem> KEYCARD_ITEM = ITEMS.registerItem(
            "keycard",
            KeycardItem::new, // The factory that the properties will be passed into.
            props -> props // A unary operator of the properties to use.
    );

    // Send the new items to the event bus
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}