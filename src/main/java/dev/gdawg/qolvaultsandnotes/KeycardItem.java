/// ----- KeycardItem -----
/// Base class for the keycard.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class KeycardItem extends Item {

    // --- CONSTRUCTOR ---
    public KeycardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return OtherKeycardFunctions.handleKeycard(
                context.getItemInHand(),
                context.getLevel().getBlockState(context.getClickedPos()),
                context.getLevel(),
                context.getClickedPos(),
                context.getPlayer(),
                new net.minecraft.world.phys.BlockHitResult(
                        context.getClickLocation(),
                        context.getClickedFace(),
                        context.getClickedPos(),
                        false
                )
        );
    }
}