/*
/// ----- VaultFullSyncPacket -----
/// Packet for syncing the items contained in the vault.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record VaultFullSyncPacket(NonNullList<ItemStack> allItems) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VaultFullSyncPacket> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "vault_full_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultFullSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public VaultFullSyncPacket decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
            for (int i = 0; i < size; i++) {
                items.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return new VaultFullSyncPacket(items);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, VaultFullSyncPacket packet) {
            buf.writeInt(packet.allItems().size());
            for (ItemStack stack : packet.allItems()) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }
    };

    @Override
    public Type<VaultFullSyncPacket> type() {
        return TYPE;
    }
}*/
