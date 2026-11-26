package com.bonker.stardewfishing.gameplay.minigame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

}
