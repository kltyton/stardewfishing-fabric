package com.kltyton.stardewfishingFabric.client;

import com.kltyton.stardewfishingFabric.StardewfishingFabric;
import com.kltyton.stardewfishingFabric.common.FishBehavior;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class FishingMinigame {
    // 完成小游戏所需的分数
    public static final int POINTS_TO_FINISH = 180;

    // 小游戏物理参数
    private static final float UP_ACCELERATION = 0.7F;
    private static final float GRAVITY = -0.7F;
    private static final int MAX_BOBBER_HEIGHT = 106;
    private static final int MAX_FISH_HEIGHT = FishBehavior.MAX_HEIGHT;

    // 随机数生成器，用于小游戏逻辑
    private final Random random = new Random();
    // 钓鱼屏幕对象
    private final FishingScreen screen;
    // 鱼类行为对象
    private final FishBehavior behavior;

    // 浮标位置和速度
    private double bobberPos = 0;
    private double bobberVelocity = 0;

    // 鱼的位置和速度
    private double fishPos = 0;
    private double fishVelocity = 0;
    // 鱼的目标位置
    private int fishTarget = -1;
    // 鱼是否空闲
    private boolean fishIsIdle = false;
    // 鱼空闲的刻数
    private int fishIdleTicks = 0;

    // 浮标是否在鱼上
    private boolean bobberOnFish = true;
    // 当前分数
    private int points = POINTS_TO_FINISH / 2;
    // 未命中时的扣分节流
    private int missDrainTicks = 0;
    // 成功刻数
    private int successTicks = 0;
    // 总刻数
    private int totalTicks = 0;

    // 构造函数，初始化小游戏
    public FishingMinigame(FishingScreen screen, FishBehavior behavior) {
        this.screen = screen;
        this.behavior = behavior;
    }

    // 每帧更新小游戏状态
    public void tick(boolean mouseDown) {
        // 浮标运动逻辑
        if (mouseDown) {
            if (bobberVelocity < 0) {
                bobberVelocity *= 0.9;
            }
            bobberVelocity += UP_ACCELERATION;
        } else if (bobberPos > 0) {
            bobberVelocity += GRAVITY;
        }

        bobberPos += bobberVelocity;
        if (bobberPos > MAX_BOBBER_HEIGHT) {
            bobberVelocity = 0;
            bobberPos = MAX_BOBBER_HEIGHT;
        } else if (bobberPos <= 0) {
            bobberPos = 0;
            if (bobberVelocity < 2 * GRAVITY) {
                bobberVelocity *= -0.4;
            } else {
                bobberVelocity = 0;
            }
        }

        // 鱼运动逻辑
        if (fishTarget == -1 || behavior.shouldMoveNow(fishIdleTicks, random)) {
            fishTarget = behavior.pickNextTargetPos((int) fishPos, random);
            fishIsIdle = false;
            fishIdleTicks = 0;
        }

        if (fishIsIdle) {
            fishIdleTicks++;
            if (Math.abs(fishVelocity) > 0) {
                boolean up = fishVelocity > 0;
                fishVelocity -= (up ? behavior.upAcceleration() : behavior.downAcceleration()) * Math.signum(fishVelocity);
                if (fishVelocity == 0 || up && fishVelocity < 0 || !up && fishVelocity > 0) {
                    fishVelocity = 0;
                }
            }
        } else {
            double distanceLeft = fishTarget - fishPos;
            double acceleration = (distanceLeft > 0 ? behavior.upAcceleration() : behavior.downAcceleration()) * Math.signum(distanceLeft);
            fishVelocity = MathHelper.clamp(fishVelocity + acceleration, -behavior.topSpeed(), behavior.topSpeed());
        }

        fishPos += fishVelocity;
        if (Math.abs(fishTarget - fishPos) < fishVelocity) {
            fishIsIdle = true;
        } else if (fishPos > MAX_FISH_HEIGHT) {
            fishVelocity = 0;
            fishPos = MAX_FISH_HEIGHT;
            fishIsIdle = true;
        } else if (fishPos < 0) {
            fishVelocity = 0;
            fishPos = 0;
            fishIsIdle = true;
        }

        // 游戏逻辑
        int min = MathHelper.floor(bobberPos) - 2;
        int max = MathHelper.ceil(bobberPos) + 24;
        boolean wasOnFish = bobberOnFish;
        bobberOnFish = fishPos >= min && fishPos <= max;

        totalTicks++;
        if (bobberOnFish) {
            successTicks++;
        }

        if (wasOnFish != bobberOnFish) {
            screen.stopReelingSounds();
            screen.playSound(StardewfishingFabric.DWOP);
            screen.reelSoundTimer = 1;
        }

        if (bobberOnFish) {
            points += 1;
            missDrainTicks = 0;
            if (points >= POINTS_TO_FINISH) {
                screen.setResult(true, (double) successTicks / totalTicks);
            }
        } else {
            if (++missDrainTicks >= 2) {
                missDrainTicks = 0;
                points -= 1;
            }
            if (points <= 0) {
                screen.setResult(false, 0);
            }
        }
    }

    // 获取浮标位置
    public float getBobberPos() {
        return (float) bobberPos;
    }

    // 获取鱼的位置
    public float getFishPos() {
        return (float) fishPos;
    }

    // 检查浮标是否在鱼上
    public boolean isBobberOnFish() {
        return bobberOnFish;
    }

    // 获取进度
    public float getProgress() {
        return (float) points / POINTS_TO_FINISH;
    }
}
