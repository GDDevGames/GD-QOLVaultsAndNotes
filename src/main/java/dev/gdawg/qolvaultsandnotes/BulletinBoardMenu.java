package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BulletinBoardMenu extends AbstractContainerMenu {
    NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    BulletinBoardBlockEntity blockEntity;


    public BulletinBoardMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BulletinBoardMenu(int containerId, Inventory playerInventory, @Nullable BlockEntity blockEntity) {
        super(ModMenus.BULLETINBOARD_MENU.get(), containerId);
        this.blockEntity = (BulletinBoardBlockEntity) blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
        /*
        READDDDDDDDDDDDDDDDDDDDDDDDD
                READ!
            RETURN TRUE JUST FOR TESTING PURPOSES.
                LATER MAKE SURE THAT PLAYER POSITION VS BLOCK POSITION DETERMINES IF THE PLAYER'S MENU
                WILL CLOSE IF THEY'RE TOO FAR AWAY'*/
    }
}