/// ----- ClientSetup -----
/// Ensures the vault block is rendered correctly in the world.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = QOLVaultsAndNotes.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            ModBlockEntities.VAULT_ENTITY.get(),
            VaultBlockEntityRenderer::new
        );
    }

    /*@SubscribeEvent
    public static void registerPacketHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
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
    }*/

    public static void openSafeScreen(BlockPos pos, boolean isKeycard) {
        Minecraft.getInstance().setScreen(new SafeCodeScreen(pos, isKeycard));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.VAULT_MENU.get(), VaultScreen::new);
        event.register(ModMenus.BULLETINBOARD_MENU.get(), BulletinBoardScreen::new);
    }
}