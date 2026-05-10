/// ----- PistonOverrideEvent -----
/// Event for toggling a piston with a keycard.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.PistonEvent;

@EventBusSubscriber(modid = QOLVaultsAndNotes.MODID)
public class PistonOverrideEvent {

    @SubscribeEvent
    public static void onPistonPre(PistonEvent.Pre event) {
        BlockPos pos = event.getPos();
        if (OtherKeycardFunctions.LOCKED_PISTONS.contains(pos)) {
            if (event.getPistonMoveType() == PistonEvent.PistonMoveType.RETRACT) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        BlockPos pos = event.getPos();

        if (OtherKeycardFunctions.LOCKED_PISTONS.contains(pos)) {
            event.setCanceled(true);
            return;
        }

        for (Direction dir : event.getNotifiedSides()) {
            BlockPos neighborPos = pos.relative(dir);
            if (OtherKeycardFunctions.LOCKED_PISTONS.contains(neighborPos)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}