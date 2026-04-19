package dev.gdawg.qolvaultsandnotes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record VaultScrollPacket(int rowOffset) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VaultScrollPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "vault_scroll"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultScrollPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, VaultScrollPacket::rowOffset,
                    VaultScrollPacket::new
            );

    @Override
    public CustomPacketPayload.Type<VaultScrollPacket> type() {
        return TYPE;
    }
}