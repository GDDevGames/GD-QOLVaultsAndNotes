package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BulletinBoardBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(18, ItemStack.EMPTY);

    public BulletinBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BULLETIN_BOARD_ENTITY.get(), pos, state);
    }

    public boolean hasItems() {
        return items.stream().anyMatch(stack -> !stack.isEmpty());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            boolean hasItems = hasItems();
            if (state.getValue(BulletinBoardBlock.ACTIVATED) != hasItems) {
                level.setBlock(worldPosition, state.setValue(BulletinBoardBlock.ACTIVATED, hasItems), 3);
            }
        }
    }

    @Override
    public int getContainerSize() {
        return items.size();
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
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = items.get(slot).split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack item = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return item;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                output.store("item_" + i, ItemStack.CODEC, items.get(i));
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, input.read("item_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
    }
}