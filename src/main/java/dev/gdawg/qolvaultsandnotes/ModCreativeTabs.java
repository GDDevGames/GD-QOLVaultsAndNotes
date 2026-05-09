/// ----- ModCreativeTabs -----
/// Registers a new creative mode tab and adds the new items/blocks to them.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.gdawg.qolvaultsandnotes.ModItems.*;

public class ModCreativeTabs {
    // Tab register
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QOLVaultsAndNotes.MODID);

    // Register new tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> QOLVAULTSANDNOTES = CREATIVE_MODE_TABS.register("qolvaultsandnotes", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.qolvaultsandnotes"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> new ItemStack(Items.ARMOR_STAND))
        .displayItems((parameters, output) -> {
            output.accept(BULLETIN_BOARD_ITEM.get());
            output.accept(SAFE_ITEM.get());
            output.accept(KEY_ITEM.get());
            output.accept(KEYCARD_ITEM.get());
            output.accept(LOCK_ITEM.get());
        }).build());

    // Send the tab to the event bus
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}