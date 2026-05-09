/// ----- SafeBlockEntity -----
/// Handles the safe entity & its functions (opening/closing, locking/unlocking)
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class SafeBlockEntity extends BlockEntity implements Container, LidBlockEntity {
    private String assignedCode = "";
    private NonNullList<ItemStack> items = NonNullList.withSize(18, ItemStack.EMPTY);
    private final ChestLidController chestLidController = new ChestLidController();
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

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.setBlock(pos, state.setValue(SafeBlock.ACTIVATED, true), 3);
            level.playSound(null, pos,
                    SoundEvents.IRON_TRAPDOOR_OPEN,
                    SoundSource.BLOCKS, 1.0f, 0.4f
            );
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.setBlock(pos, state.setValue(SafeBlock.ACTIVATED, false), 3);
            level.playSound(null, pos,
                    SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0f, 0.4f
            );
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
            SafeBlockEntity.this.signalOpenCount(level, pos, state, count, openCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu)) {
                return false;
            }
            net.minecraft.world.Container container = ((net.minecraft.world.inventory.ChestMenu) player.containerMenu).getContainer();
            return container == SafeBlockEntity.this;
        }
    };

    public SafeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAFE_ENTITY.get(), pos, state);
    }

    // TODO: Implement animations
    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, SafeBlockEntity entity) {
        boolean isOpen = state.getValue(SafeBlock.ACTIVATED);
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


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public int getContainerSize() {
        return 18;
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
}