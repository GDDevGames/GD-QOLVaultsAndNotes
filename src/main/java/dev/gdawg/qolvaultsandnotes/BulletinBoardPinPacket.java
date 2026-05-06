package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BulletinBoardPinPacket(
        BlockPos pos, int slot, String title, String body, int colour, boolean isNew)
        implements CustomPacketPayload {

    public static final Type<BulletinBoardPinPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("qolvaultsandnotes", "bulletin_board_pin"));

    public static final StreamCodec<FriendlyByteBuf, BulletinBoardPinPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, BulletinBoardPinPacket::pos,
                    ByteBufCodecs.INT, BulletinBoardPinPacket::slot,
                    ByteBufCodecs.STRING_UTF8, BulletinBoardPinPacket::title,
                    ByteBufCodecs.STRING_UTF8, BulletinBoardPinPacket::body,
                    ByteBufCodecs.INT, BulletinBoardPinPacket::colour,
                    ByteBufCodecs.BOOL, BulletinBoardPinPacket::isNew,
                    BulletinBoardPinPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}