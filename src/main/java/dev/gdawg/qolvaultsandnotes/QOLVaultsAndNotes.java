package dev.gdawg.qolvaultsandnotes;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;

@Mod(QOLVaultsAndNotes.MODID)
public class QOLVaultsAndNotes {
    public static final String MODID = "qolvaultsandnotes";

    public QOLVaultsAndNotes(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.BULLETIN_BOARD_ITEM.get());
            event.accept(ModItems.SAFE_ITEM.get());
            event.accept(ModItems.LOCK_ITEM.get());
            event.accept(ModItems.KEY_ITEM.get());
            event.accept(ModItems.KEYCARD_ITEM.get());
        }
    }
}