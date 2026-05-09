/// ----- BulletinBoardMenu -----
/// Overrides the default behavior for a blocks menu and replaces it with a custom GUI.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class BulletinBoardMenu extends AbstractContainerMenu {
    BulletinBoardBlockEntity blockEntity;

    public boolean pinNote(int slot, String title, String body, int colour, boolean isNew) {
        if (isNew) {
            // New note costs 2 paper
            /*ItemStack paper = blockEntity.getItem(1);
            if (paper.getCount() < 2) return false;
            paper.shrink(2);
            blockEntity.setItem(1, paper);*/
            consumePaper();
            consumeInk();
        }
        else {
            // Editing existing note costs 1 ink sac
            /*ItemStack ink = blockEntity.getItem(0);
            if (ink.getCount() < 1) return false;
            ink.shrink(1);
            blockEntity.setItem(0, ink);*/
            consumeInk();
        }
        blockEntity.setNote(slot, title, body, colour);

        return true;
    }

    // --- CONSTRUCTOR ---
    public BulletinBoardMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // --- CONSTRUCTOR ---
    public BulletinBoardMenu(int containerId, Inventory playerInventory, @Nullable BlockEntity blockEntity) {
        super(ModMenus.BULLETINBOARD_MENU.get(), containerId);
        this.blockEntity = (BulletinBoardBlockEntity) blockEntity;
    }

    // Don't bother, there's no items to move
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private boolean consumeInk() {
        int slot = 0;
        int amount = 1;
        ItemStack ink = this.blockEntity.getItem(slot);

        if(ink.isEmpty()) return false;

        this.blockEntity.getItem(0).shrink(1); // ink consume 1
        return true;
    }
    private boolean consumePaper()
    {
        int slot = 1;
        int amount = 4;
        ItemStack paper = this.blockEntity.getItem(slot);

        if(!(paper.getCount() >= amount)) return false;

        this.blockEntity.getItem(1).shrink(4); // paper consume 4
        return true;
    }
}