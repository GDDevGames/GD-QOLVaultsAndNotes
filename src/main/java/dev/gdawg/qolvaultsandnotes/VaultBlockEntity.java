package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class VaultBlockEntity extends BaseContainerBlockEntity {

    public static final int SIZE = 72;
    private NonNullList<ItemStack> items = NonNullList.withSize(
            //firstly the size, how many slots
            SIZE,
            //secondly since it's a NonNullList, what to fill empty slots with:
            ItemStack.EMPTY);

    public VaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAULT_ENTITY.get(), pos, state);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(this.items, slot);
        this.setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.limitSize(this.getMaxStackSize(stack));
        items.set(slot, stack);
        this.setChanged();
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
        this.setChanged();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.qolvaultsandnotes.vault");
        //this requires that we make an entry for the container name in our lang file.
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (this.canOpen(player)) {
            return this.createMenu(containerId, playerInventory);
        } else {
            sendChestLockedNotifications(this.getBlockPos().getCenter(), player, this.getDisplayName());
            return null;
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new VaultMenu(containerId, playerInventory, this);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
    }
}