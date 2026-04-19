package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SafeCodePacket(BlockPos pos, String enteredCode)
        implements CustomPacketPayload {

    public static final Type<SafeCodePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "qolvaultsandnotes", "safe_code"));

    public static final StreamCodec<FriendlyByteBuf, SafeCodePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SafeCodePacket::pos,
                    ByteBufCodecs.STRING_UTF8, SafeCodePacket::enteredCode,
                    SafeCodePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
