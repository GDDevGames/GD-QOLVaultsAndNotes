/// ----- VaultBlockEntityRenderState -----
/// Class that's needed for modifying the vaults RenderState.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class VaultBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
}