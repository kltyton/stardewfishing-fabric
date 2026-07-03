package com.kltyton.stardewfishingFabric.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public record FishBehavior(
        int idleTime, // 空闲时间
        float topSpeed, // 最高速度
        float upAcceleration, // 向上加速度
        float downAcceleration, // 向下加速度
        int avgDistance, // 平均距离
        int moveVariation // 移动变化量
) {
    public static final Codec<FishBehavior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("idle_time").forGetter(FishBehavior::idleTime),
            Codec.FLOAT.fieldOf("top_speed").forGetter(FishBehavior::topSpeed),
            Codec.FLOAT.fieldOf("up_acceleration").forGetter(FishBehavior::upAcceleration),
            Codec.FLOAT.fieldOf("down_acceleration").forGetter(FishBehavior::downAcceleration),
            Codec.INT.fieldOf("avg_distance").forGetter(FishBehavior::avgDistance),
            Codec.INT.fieldOf("move_variation").forGetter(FishBehavior::moveVariation)
    ).apply(inst, FishBehavior::new));

    public static final PacketCodec<RegistryByteBuf, FishBehavior> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, FishBehavior::idleTime,
            PacketCodecs.FLOAT, FishBehavior::topSpeed,
            PacketCodecs.FLOAT, FishBehavior::upAcceleration,
            PacketCodecs.FLOAT, FishBehavior::downAcceleration,
            PacketCodecs.VAR_INT, FishBehavior::avgDistance,
            PacketCodecs.VAR_INT, FishBehavior::moveVariation,
            FishBehavior::new
    );

    public static final int MAX_HEIGHT = 127;

    public boolean shouldMoveNow(int idleTicks, Random random) {
        if (idleTime == 0) return true;
        if (idleTime == 1) return idleTicks == 1;

        int variation = idleTicks / 2;
        float chancePerTick = 1F / variation;

        if (idleTicks >= idleTime - variation) {
            return random.nextFloat() <= chancePerTick;
        }

        return false;
    }

    public int pickNextTargetPos(int oldPos, Random random) {
        int shortestDistance = avgDistance - moveVariation;
        int longestDistance = avgDistance + moveVariation;

        int downLowerLimit = oldPos - shortestDistance;
        int upLowerLimit = oldPos + shortestDistance;

        boolean canGoDown = downLowerLimit >= 0;
        boolean canGoUp = upLowerLimit <= MAX_HEIGHT;

        boolean goingUp;
        if (canGoUp && canGoDown) {
            goingUp = random.nextBoolean();
        } else {
            goingUp = canGoUp;
        }

        int distance = random.nextInt(shortestDistance, longestDistance + 1);

        return MathHelper.clamp(oldPos + distance * (goingUp ? 1 : -1), 0, MAX_HEIGHT);
    }
}
