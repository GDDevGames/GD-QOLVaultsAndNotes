package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    //should maybe call it ModMenuTypes honestly bc that's what it's doing
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            //what registry we're registering in. BuiltInRegistries is mojang's existing ones
            BuiltInRegistries.MENU,
            //give mod id
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

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
