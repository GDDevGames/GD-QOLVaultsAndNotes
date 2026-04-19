package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class VaultMenu extends AbstractContainerMenu {

    public final VaultBlockEntity blockEntity;
    private static final int VAULT_ROWS = 8;
    private static final int VAULT_COLS = 9;
    private static final int VISIBLE_ROWS = 6;
    public static final int VISIBLE_SLOTS = VISIBLE_ROWS * VAULT_COLS; // 54
    public static final int TOTAL_SLOTS = VAULT_ROWS * VAULT_COLS;    // 72
    public static final int SLOT_START_X = 8;
    public static final int SLOT_START_Y = 18;

    private int currentRowOffset = 0;

    private final NonNullList<ItemStack> clientItems =
            NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // Custom slot that redirects to a mutable vault index
    public class VaultSlot extends Slot {
        private int vaultIndex;
        private ItemStack clientItem = ItemStack.EMPTY; // client-side display cache

        public VaultSlot(int visibleRow, int col) {
            super(blockEntity, visibleRow * VAULT_COLS + col,
                    SLOT_START_X + col * 18,
                    SLOT_START_Y + visibleRow * 18);
            this.vaultIndex = visibleRow * VAULT_COLS + col;
        }

        public void setVaultIndex(int newIndex) {
            this.vaultIndex = newIndex;
        }

        public void setClientItem(ItemStack stack) {
            this.clientItem = stack;
        }

        @Override
        public ItemStack getItem() {
            // On the client, use the cached item so the display is always correct.
            // On the server, go straight to the block entity.
            if (net.minecraft.client.Minecraft.getInstance().level != null) {
                return clientItem;
            }
            return blockEntity.getItem(vaultIndex);
        }

        // ... rest of VaultSlot unchanged
    }

    public VaultMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public VaultMenu(int containerId, Inventory playerInventory, @Nullable BlockEntity blockEntity) {
        super(ModMenus.VAULT_MENU.get(), containerId);
        this.blockEntity = (VaultBlockEntity) blockEntity;

        addVaultSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addVaultSlots() {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < VAULT_COLS; col++) {
                this.addSlot(new VaultSlot(row, col));
            }
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9,
                        SLOT_START_X + j * 18, 140 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i,
                    SLOT_START_X + i * 18, 198));
        }
    }

    // rowOffset: 0 = show rows 0-5, 1 = show rows 1-6, 2 = show rows 2-7
    public void scrollTo(int rowOffset) {
        this.currentRowOffset = rowOffset;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < VAULT_COLS; col++) {
                int slotIndex = row * VAULT_COLS + col;
                VaultSlot slot = (VaultSlot) this.slots.get(slotIndex);
                slot.setVaultIndex((row + rowOffset) * VAULT_COLS + col);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < VISIBLE_SLOTS) {
                // From vault to player
                if (!this.moveItemStackTo(itemstack1, VISIBLE_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player to vault
                if (!this.moveItemStackTo(itemstack1, 0, VISIBLE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * Called on the CLIENT when the full-sync packet is received.
     * Stores all 72 items locally, then refreshes visible slots.
     */
    public void applyFullSync(List<ItemStack> items) {
        for (int i = 0; i < Math.min(items.size(), TOTAL_SLOTS); i++) {
            clientItems.set(i, items.get(i).copy());
        }
        refreshVisibleSlotsFromClientCache();
    }

    /**
     * Pushes the correct window of items from the client cache into
     * the 54 visible slots so the renderer sees the right items.
     */
    public void refreshVisibleSlotsFromClientCache() {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < VAULT_COLS; col++) {
                int slotIndex = row * VAULT_COLS + col;
                int vaultIndex = (row + currentRowOffset) * VAULT_COLS + col;
                VaultSlot slot = (VaultSlot) this.slots.get(slotIndex);
                // On the client, the slot's backing container is the blockEntity,
                // but we override the rendered item via the clientItems cache
                slot.setClientItem(clientItems.get(vaultIndex));
            }
        }
    }
}