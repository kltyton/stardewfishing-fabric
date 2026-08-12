package com.bonker.stardewfishing.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FishBehavior(int idleTime, float topSpeed, float upAcceleration, float downAcceleration, int avgDistance, int moveVariation) {
    public static final Codec<FishBehavior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("idle_time").forGetter(FishBehavior::idleTime),
            Codec.FLOAT.fieldOf("top_speed").forGetter(FishBehavior::topSpeed),
            Codec.FLOAT.fieldOf("up_acceleration").forGetter(FishBehavior::upAcceleration),
            Codec.FLOAT.fieldOf("down_acceleration").forGetter(FishBehavior::downAcceleration),
            Codec.INT.fieldOf("avg_distance").forGetter(FishBehavior::avgDistance),
            Codec.INT.fieldOf("move_variation").forGetter(FishBehavior::moveVariation)
    ).apply(inst, FishBehavior::new));
}
