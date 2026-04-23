/// ----- OpenSafeScreenPacket -----
/// Sends the packet to open the safe GUI.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenSafeScreenPacket(BlockPos pos, boolean isKeycard) implements CustomPacketPayload {
    public static final Type<OpenSafeScreenPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath("qolvaultsandnotes", "open_safe_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenSafeScreenPacket> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenSafeScreenPacket::pos,
            ByteBufCodecs.BOOL, OpenSafeScreenPacket::isKeycard,
            OpenSafeScreenPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}