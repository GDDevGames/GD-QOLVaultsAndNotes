/// ----- VaultBlockEntity -----
/// Handles the vault entity and interactions.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class VaultBlockEntity extends BaseContainerBlockEntity implements LidBlockEntity {
    private final ChestLidController chestLidController = new ChestLidController();
    public static final int SIZE = 72;
    private NonNullList<ItemStack> items = NonNullList.withSize(
        SIZE,
        // Since it's a NonNullList, specify what to fill the empty slots with
        ItemStack.EMPTY);

    private String assignedCode = "";
    private boolean lockedWithKeycard = false;

    public boolean isLockedWithKeycard() {
        return lockedWithKeycard;
    }
    public void setLockedWithKeycard(boolean val) {
        this.lockedWithKeycard = val;
    }

    public String getAssignedCode() {
        return assignedCode;
    }
    public void setAssignedCode(String code) {
        this.assignedCode = code;
    }

    private boolean locked = false;
    private UUID lockOwner = null;

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public UUID getLockOwner() {
        return lockOwner;
    }
    public void setLockOwner(UUID uuid) {
        this.lockOwner = uuid;
    }

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
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (this.canOpen(player)) {
            return this.createMenu(containerId, playerInventory);
        }
        else {
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

        // Save the additional safe-specific data
        ContainerHelper.saveAllItems(output, this.items);
        output.putString("assigned_code", assignedCode);
        output.putBoolean("locked_with_keycard", lockedWithKeycard);
        output.putBoolean("locked", locked);
        if (lockOwner != null) output.putString("lock_owner", lockOwner.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        // Load the additional safe-specific data
        ContainerHelper.loadAllItems(input, this.items);
        assignedCode = input.getStringOr("assigned_code", "");
        lockedWithKeycard = input.getBooleanOr("locked_with_keycard", false);
        locked = input.getBooleanOr("locked", false);
        String uuidStr = input.getStringOr("lock_owner", "");
        lockOwner = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
    }

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.setBlock(pos, state.setValue(VaultBlock.ACTIVATED, true), 3);
            level.playSound(null, pos,
                    SoundEvents.COPPER_CHEST_OPEN,
                    SoundSource.BLOCKS, 1.0f, 0.7f
            );
            level.playSound(null, pos,
                    SoundEvents.VAULT_OPEN_SHUTTER,
                    SoundSource.BLOCKS, 0.7f, 1.0f
            );
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.setBlock(pos, state.setValue(VaultBlock.ACTIVATED, false), 3);
            level.playSound(null, pos,
                    SoundEvents.COPPER_CHEST_CLOSE,
                    SoundSource.BLOCKS, 1.0f, 0.7f
            );
            level.playSound(null, pos,
                    SoundEvents.VAULT_BREAK,
                    SoundSource.BLOCKS, 0.7f, 0.7f
            );
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
            VaultBlockEntity.this.signalOpenCount(level, pos, state, count, openCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof VaultMenu)) {
                return false;
            }
            return ((VaultMenu) player.containerMenu).blockEntity == VaultBlockEntity.this;
        }
    };

    // TODO: Implement animations
    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, VaultBlockEntity entity) {
        boolean isOpen = state.getValue(VaultBlock.ACTIVATED);
        entity.chestLidController.shouldBeOpen(isOpen);
        entity.chestLidController.tickLid();
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.incrementOpeners(
                    user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(),
                    user.getContainerInteractionRange()
            );
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.decrementOpeners(
                    user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState()
            );
        }
    }

    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int eventId, int eventParam) {
        level.blockEvent(pos, state.getBlock(), 1, eventParam);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }
}