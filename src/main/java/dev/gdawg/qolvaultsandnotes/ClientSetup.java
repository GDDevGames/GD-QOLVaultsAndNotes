/// ----- ClientSetup -----
/// Ensures the vault block is rendered correctly in the world.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = QOLVaultsAndNotes.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            ModBlockEntities.VAULT_ENTITY.get(),
            VaultBlockEntityRenderer::new
        );
    }
}