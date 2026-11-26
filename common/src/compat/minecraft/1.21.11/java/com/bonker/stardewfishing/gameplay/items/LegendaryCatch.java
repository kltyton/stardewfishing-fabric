package com.bonker.stardewfishing.gameplay.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Date;

/** Records who caught a legendary fish and when; shown in the item tooltip. */
public record LegendaryCatch(String player, long time) {
    public static final Codec<LegendaryCatch> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("player").forGetter(LegendaryCatch::player),
            Codec.LONG.fieldOf("time").forGetter(LegendaryCatch::time)
    ).apply(inst, LegendaryCatch::new));

    public static final PacketCodec<ByteBuf, LegendaryCatch> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, LegendaryCatch::player,
            PacketCodecs.VAR_LONG, LegendaryCatch::time,
            LegendaryCatch::new
    );

    public LegendaryCatch(PlayerEntity player) {
        this(player.getNameForScoreboard(), new Date().getTime());
    }
}
