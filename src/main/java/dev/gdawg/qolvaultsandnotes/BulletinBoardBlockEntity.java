/// ----- BulletinBoardBlockEntity -----
/// Handles block entity functions and events for the bulletin board.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BulletinBoardBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public static final int MAX_NOTES = 8;

    private final String[] noteTitles = new String[MAX_NOTES];
    private final String[] noteBodies = new String[MAX_NOTES];
    private final int[] noteColours = new int[MAX_NOTES]; // 0 = yellow, 1 = green, 2 = blue, 3 = pink
    private final boolean[] noteOccupied = new boolean[MAX_NOTES];

    public String getNoteTitle(int slot) {
        return noteTitles[slot] != null ? noteTitles[slot] : "";
    }
    public String getNoteBody(int slot) {
        return noteBodies[slot] != null ? noteBodies[slot] : "";
    }
    public int getNoteColour(int slot) {
        return noteColours[slot];
    }
    public boolean isNoteOccupied(int slot) {
        return noteOccupied[slot];
    }

    public void setNote(int slot, String title, String body, int colour) {
        noteTitles[slot] = title;
        noteBodies[slot] = body;
        noteColours[slot] = colour;
        noteOccupied[slot] = true;
        super.setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // TODO: Implement
    public void clearNote(int slot) {
        noteTitles[slot] = "";
        noteBodies[slot] = "";
        noteColours[slot] = 0;
        noteOccupied[slot] = false;
        super.setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getNextFreeSlot() {
        for (int i = 0; i < MAX_NOTES; i++) {
            if (!noteOccupied[i]) return i;
        }
        return 0; // review this
    }

    // --- CONSTRUCTOR ---
    public BulletinBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BULLETIN_BOARD_ENTITY.get(), pos, state);
        for(int i = 0; i < MAX_NOTES; i++) {
            noteTitles[i] = "";
            noteBodies[i] = "";
            noteColours[i] = 0;
            noteOccupied[i] = false;
        }

    }

    // TODO: Implement
    public boolean hasItems() {
        return items.stream().anyMatch(stack -> !stack.isEmpty());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        for (int i = 0; i < MAX_NOTES; i++) {
            /*output.putBoolean("note_occupied_" + i, noteOccupied[i]);
            output.putString("note_title_" + i, noteTitles[i] != null ? noteTitles[i] : "");
            output.putString("note_body_" + i, noteBodies[i] != null ? noteBodies[i] : "");
            output.putInt("note_colour_" + i, noteColours[i]);
*/
            output.store("note_occupied_" + i, Codec.BOOL, noteOccupied[i]);
            output.store("note_title_" + i, Codec.STRING, noteTitles[i]);
            output.store("note_body_" + i, Codec.STRING, noteBodies[i]);
            output.store("note_colour_" + i, Codec.INT, noteColours[i]);
        }
        for (int i = 0; i < items.size(); i++) {
            output.storeNullable("item_" + i, ItemStack.CODEC, items.get(i).isEmpty() ? null : items.get(i));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < MAX_NOTES; i++) {
            noteOccupied[i] = input.read("note_occupied_" + i, Codec.BOOL).orElse(false);
            noteTitles[i] = input.read("note_title_" + i, Codec.STRING).orElse("");
            noteBodies[i] = input.read("note_body_" + i, Codec.STRING).orElse("");
            noteColours[i] = input.read("note_colour_" + i, Codec.INT).orElse(0);
            /*
            noteOccupied[i] = input.getBooleanOr("note_occupied_" + i, false);
            noteTitles[i] = input.getStringOr("note_title_" + i, "");
            noteBodies[i] = input.getStringOr("note_body_" + i, "");
            noteColours[i] = input.getIntOr("note_colour_" + i, 0);*/
        }
        for (int i = 0; i < items.size(); i++) {
            items.set(i, input.read("item_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // For syncing to client when chunk loads
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
}