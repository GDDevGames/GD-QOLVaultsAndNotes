package dev.gdawg.qolvaultsandnotes;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(QOLVaultsAndNotes.MODID)
public class QOLVaultsAndNotes {
    public static final String MODID = "qolvaultsandnotes";

    public QOLVaultsAndNotes(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}
