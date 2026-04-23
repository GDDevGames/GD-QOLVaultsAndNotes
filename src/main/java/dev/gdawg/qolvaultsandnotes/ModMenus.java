/// ----- ModMenus -----
/// Registers the new GUI menus for the game to recognize.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    // Daniel: should maybe call it ModMenuTypes honestly bc that's what it's doing
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
        // Daniel: what registry we're registering in. BuiltInRegistries is mojang's existing ones
        BuiltInRegistries.MENU,
        QOLVaultsAndNotes.MODID
    );

    public static final Supplier<MenuType<VaultMenu>> VAULT_MENU = MENUS.register(
        "vault_menu",
        () -> IMenuTypeExtension.create(VaultMenu::new)
    );
    public static final Supplier<MenuType<BulletinBoardMenu>> BULLETINBOARD_MENU = MENUS.register(
        "bulletinboard_menu",
        () -> IMenuTypeExtension.create(BulletinBoardMenu::new)
    );


    // Send the menus to the event bus
    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
