package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.registry.SFItems;
import com.bonker.stardewfishing.registry.SFSoundEvents;
import com.bonker.stardewfishing.network.SFNetworking.S2CStartMinigamePayload;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class FishingMinigame {
    private static final int POINTS_TO_FINISH = 120;
    private static final int TREASURE_CHEST_TIME = 30;

    private static final float UP_ACCELERATION = 0.7F;
    private static final float GRAVITY = -0.7F;
    private static final int MAX_FISH_HEIGHT = 127;

    private final Random random = new Random();
    private final FishingScreen screen;
    private final S2CStartMinigamePayload packet;
    private final float lineStrength;
    private final int barSize;
    private final int maxBobberHeight;

    private boolean hasSonarBobber = false;
    private boolean hasTreasureBobber = false;

    private double bobberPos = 0;
    private double bobberVelocity = 0;

    private double fishPos = 0;
    private double fishVelocity = 0;
    private int fishTarget = -1;
    private boolean fishIsIdle = false;
    private int fishIdleTicks = 0;

    private boolean bobberOnFish = true;
    private boolean bobberOnChest = false;
    private float points = POINTS_TO_FINISH / 5F;
    private int successTicks = 0;
    private int totalTicks = 0;

    private final boolean goldenChest;
    private final int chestPos;
    private int chestAppearTime;
    private float chestTimer = 0;
    private boolean chestVisible = false;

    public FishingMinigame(FishingScreen screen, S2CStartMinigamePayload packet, PlayerEntity player, float lineStrength, int barSize) {
        this.screen = screen;
        this.packet = packet;
        this.goldenChest = packet.goldenChest();
        this.chestPos = packet.treasureChest() ? (int) (5 + 125 * random.nextFloat()) : 0;
        this.chestAppearTime = packet.treasureChest() ? (int) (20 + 40 * random.nextFloat()) : -1;
        this.lineStrength = lineStrength;
        this.barSize = barSize;
        this.maxBobberHeight = 142 - barSize;

        Hand hand = ItemUtils.getRodHand(player);
        if (hand != null) {
            ItemStack bobber = ItemUtils.getBobber(player.getStackInHand(hand), player.getRegistryManager());

            if (bobber.isOf(SFItems.SONAR_BOBBER)) {
                hasSonarBobber = true;
            } else if (bobber.isOf(SFItems.TREASURE_BOBBER)) {
                hasTreasureBobber = true;
            }
        }
    }

    public void tick(boolean mouseDown) {
        // bobber movement
        if (mouseDown) {
            if (bobberVelocity < 0) {
                bobberVelocity *= 0.9;
            }
            bobberVelocity += UP_ACCELERATION;
        } else if (bobberPos > 0) {
            bobberVelocity += GRAVITY;
        }

        bobberPos += bobberVelocity;
        if (bobberPos > maxBobberHeight) {
            bobberVelocity = 0;
            bobberPos = maxBobberHeight;
        } else if (bobberPos <= 0) {
            bobberPos = 0;
            if (bobberVelocity < 2 * GRAVITY) {
                bobberVelocity *= -0.4;
            } else {
                bobberVelocity = 0;
            }
        }

        // fish movement
        if (fishTarget == -1 || shouldMoveNow(fishIdleTicks, random)) {
            fishTarget = pickNextTargetPos((int) fishPos, random);
            fishIsIdle = false;
            fishIdleTicks = 0;
        }

        if (fishIsIdle) {
            fishIdleTicks++;
            if (Math.abs(fishVelocity) > 0) {
                boolean up = fishVelocity > 0;
                fishVelocity -= (up ? packet.upAcceleration() : packet.downAcceleration()) * Math.signum(fishVelocity);
                if (fishVelocity == 0 || up && fishVelocity < 0 || !up && fishVelocity > 0) {
                    fishVelocity = 0;
                }
            }
        } else {
            double distanceLeft = fishTarget - fishPos;
            double acceleration = (distanceLeft > 0 ? packet.upAcceleration() : packet.downAcceleration()) * Math.signum(distanceLeft);
            fishVelocity = MathHelper.clamp(fishVelocity + acceleration, -packet.topSpeed(), packet.topSpeed());
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

        // treasure chest timer
        if (chestAppearTime > 0 && --chestAppearTime == 0) {
            chestVisible = true;
        }

        // game logic
        int min = MathHelper.floor(bobberPos) - 2;
        int max = MathHelper.ceil(bobberPos) + barSize - 12;
        boolean wasOnFish = bobberOnFish;
        boolean wasOnChest = bobberOnChest;
        bobberOnFish = fishPos >= min && fishPos <= max;
        bobberOnChest = chestVisible && chestPos >= min && chestPos <= max;

        totalTicks++;
        if (bobberOnFish || (hasTreasureBobber && bobberOnChest)) {
            successTicks++;
        }

        if (wasOnFish != bobberOnFish) {
            screen.stopReelingSounds();
            screen.playSound(bobberOnFish ? SFSoundEvents.DWOP : SFSoundEvents.DWOP_REVERSE);
        }

        if (wasOnChest != bobberOnChest) {
            screen.playSound(bobberOnChest ? SFSoundEvents.DWOP : SFSoundEvents.DWOP_REVERSE);
        }

        if (!bobberOnChest && chestTimer > 0 && chestTimer < TREASURE_CHEST_TIME) {
            chestTimer -= 0.25F;
        }

        if (bobberOnChest && chestTimer < TREASURE_CHEST_TIME && ++chestTimer >= TREASURE_CHEST_TIME) {
            chestVisible = false;
        }

        if (bobberOnFish) {
            points += 1;
            if (points >= POINTS_TO_FINISH) {
                screen.setResult(true, (double) successTicks / totalTicks, gotChest(), isGoldenChest());
            }
        } else if (!hasTreasureBobber || !bobberOnChest) {
            // Line strength of 1.0  -> -1.0  per tick
            // Line strength of 1.33 -> -0.67 per tick
            points = Math.max(0, points - 2 + lineStrength);
            if (points <= 0) {
                screen.setResult(false, 0, false, false);
            }
        }
    }

    private boolean shouldMoveNow(int idleTicks, Random random) {
        if (packet.idleTime() == 0) return true;
        if (packet.idleTime() == 1) return idleTicks == 1;

        int variation = idleTicks / 2;
        float chancePerTick = 1F / variation;

        if (idleTicks >= packet.idleTime() - variation) {
            return random.nextFloat() <= chancePerTick;
        }

        return false;
    }

    private int pickNextTargetPos(int oldPos, Random random) {
        int shortestDistance = Math.max(10, packet.avgDistance() - packet.moveVariation());
        int longestDistance = Math.min(MAX_FISH_HEIGHT, packet.avgDistance() + packet.moveVariation());

        int downLowerLimit = oldPos - shortestDistance;
        int upLowerLimit = oldPos + shortestDistance;

        boolean canGoDown = downLowerLimit >= 0;
        boolean canGoUp = upLowerLimit <= MAX_FISH_HEIGHT;

        boolean goingUp;
        if (canGoUp && canGoDown) {
            goingUp = random.nextBoolean();
        } else {
            goingUp = canGoUp;
        }

        int distance = random.nextInt(shortestDistance, longestDistance + 1);

        return MathHelper.clamp(oldPos + distance * (goingUp ? 1 : -1), 0, MAX_FISH_HEIGHT);
    }

    public float getBobberPos() {
        return (float) bobberPos;
    }

    public float getFishPos() {
        return (float) fishPos;
    }

    public boolean isBobberOnFish() {
        return bobberOnFish;
    }

    public boolean isBobberOnChest() {
        return bobberOnChest;
    }

    public float getProgress() {
        return points / POINTS_TO_FINISH;
    }

    public int getChestPos() {
        return chestPos;
    }

    public boolean isChestVisible() {
        return chestVisible;
    }

    public boolean isGoldenChest() {
        return goldenChest;
    }

    public float getChestProgress() {
        return chestTimer / TREASURE_CHEST_TIME;
    }

    public boolean gotChest() {
        return chestTimer >= TREASURE_CHEST_TIME;
    }

    public boolean hasSonarBobber() {
        return hasSonarBobber;
    }

    public int getBarSize() {
        return barSize;
    }
}
