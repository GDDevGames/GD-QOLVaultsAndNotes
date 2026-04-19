package dev.gdawg.qolvaultsandnotes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record VaultFullSyncPacket(List<ItemStack> allItems) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VaultFullSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "vault_full_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultFullSyncPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public VaultFullSyncPacket decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readInt();
                    List<ItemStack> items = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        items.add(ItemStack.STREAM_CODEC.decode(buf));
                    }
                    return new VaultFullSyncPacket(items);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, VaultFullSyncPacket packet) {
                    buf.writeInt(packet.allItems().size());
                    for (ItemStack stack : packet.allItems()) {
                        ItemStack.STREAM_CODEC.encode(buf, stack);
                    }
                }
            };

    @Override
    public Type<VaultFullSyncPacket> type() {
        return TYPE;
    }
}