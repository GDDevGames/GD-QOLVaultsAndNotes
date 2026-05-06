/// ----- QOLVaultsAndNotes -----
/// Initializes the mod serverside. Registers new event buses and several packets.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(QOLVaultsAndNotes.MODID)
public class QOLVaultsAndNotes {
    public static final String MODID = "qolvaultsandnotes";

    public QOLVaultsAndNotes(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerPackets);
    }

    // Packets accompanying various block entities (primarily GUIs)
    private void registerPackets(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToServer(
            BulletinBoardPinPacket.TYPE,
            BulletinBoardPinPacket.STREAM_CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    Level level = player.level();
                    BulletinBoardBlockEntity be =
                            (BulletinBoardBlockEntity) level.getBlockEntity(packet.pos());
                    if (be != null && player.containerMenu instanceof BulletinBoardMenu menu) {
                        boolean success = menu.pinNote(packet.slot(), packet.title(),
                                packet.body(), packet.colour(), packet.isNew());
                        if (success) {
                            be.setNote(packet.slot(), packet.title(), packet.body(), packet.colour());
                        }
                    }
                });
            }
        );

        // Scrolling packet for the vault
        registrar.playToServer(
            VaultScrollPacket.TYPE,
            VaultScrollPacket.STREAM_CODEC,
            (packet, context) -> {
                ServerPlayer player = (ServerPlayer) context.player();
                if (player.containerMenu instanceof VaultMenu vaultMenu) {
                    System.out.println("Server received scroll: " + packet.rowOffset());
                    vaultMenu.scrollTo(packet.rowOffset());
                }
            }
        );

        // Safe PIN code packet
        registrar.playToServer(
            SafeCodePacket.TYPE,
            SafeCodePacket.STREAM_CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    Level level = player.level();
                    SafeBlockEntity be = (SafeBlockEntity) level.getBlockEntity(packet.pos());
                    if (be != null && be.getAssignedCode().equals(packet.enteredCode())) {
                        player.openMenu((MenuProvider) be);
                    } else {
                        player.displayClientMessage(Component.literal("Incorrect code."), true);
                    }
                });
            }
        );

        // Sync packet for the vault entity
        /*registrar.playToClient(
            VaultFullSyncPacket.TYPE,
            VaultFullSyncPacket.STREAM_CODEC,
            (packet, context) -> {
                // Must run on the main client thread
                context.enqueueWork(() -> {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null &&
                        mc.player.containerMenu instanceof VaultMenu vaultMenu) {
                            vaultMenu.applyFullSync(packet.allItems());
                    }
                });
            }
        );*/

        // Packet for creating the safe code screen
        registrar.playToClient(
            OpenSafeScreenPacket.TYPE,
            OpenSafeScreenPacket.STREAM_CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> {
                    Minecraft.getInstance().setScreen(
                        new SafeCodeScreen(packet.pos(), packet.isKeycard())
                    );
                });
            }
        );
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.VAULT_MENU.get(), VaultScreen::new);
        event.register(ModMenus.BULLETINBOARD_MENU.get(), BulletinBoardScreen::new);
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