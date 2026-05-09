/// ----- KeyItem -----
/// Base class for the key.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class KeyItem extends Item {

    // --- CONSTRUCTOR ---
    public KeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return OtherKeyFunctions.handleKey(
                context.getItemInHand(),
                context.getLevel().getBlockState(context.getClickedPos()),
                context.getLevel(),
                context.getClickedPos(),
                context.getPlayer(),
                context.getPlayer().level().isClientSide()
                        ? null
                        : new net.minecraft.world.phys.BlockHitResult(
                        context.getClickLocation(),
                        context.getClickedFace(),
                        context.getClickedPos(),
                        false
                )
        );
    }
}