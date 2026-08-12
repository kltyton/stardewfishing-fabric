package com.bonker.stardewfishing.gameplay.minigame;

import com.bonker.stardewfishing.gameplay.FishBehavior;
import com.bonker.stardewfishing.gameplay.ItemUtils;
import com.bonker.stardewfishing.registry.SFAttributes;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

/** Mutable minigame parameters; modifiers apply through {@code MinigameModifiers#apply}. */
public class StardewMinigameStartedEvent extends StardewMinigameEvent {
    private final ItemStack fish;
    private int idleTime;
    private float topSpeed;
    private float upAcceleration;
    private float downAcceleration;
    private int avgDistance;
    private int moveVariation;
    private double lineStrength;
    private int barSize;
    private double treasureChanceBonus;
    private double goldenChanceBonus;
    private double expMultiplier;
    private boolean forcedTreasureChest = false;
    private boolean forcedGoldenChest = false;
    private boolean lavaFishing;
    private int qualityBoost = 0;

    public StardewMinigameStartedEvent(ServerPlayerEntity player, FishingBobberEntity hook, ItemStack fishingRod,
                                       ItemStack fish, FishBehavior behavior, boolean lavaFishing) {
        super(player, hook, fishingRod);
        this.fish = fish;
        this.idleTime = behavior.idleTime();
        this.topSpeed = behavior.topSpeed();
        this.upAcceleration = behavior.upAcceleration();
        this.downAcceleration = behavior.downAcceleration();
        this.avgDistance = behavior.avgDistance();
        this.moveVariation = behavior.moveVariation();
        this.lineStrength = player.getAttributeValue(net.minecraft.registry.Registries.ATTRIBUTE.getEntry(SFAttributes.LINE_STRENGTH));
        this.barSize = (int) player.getAttributeValue(net.minecraft.registry.Registries.ATTRIBUTE.getEntry(SFAttributes.BAR_SIZE));
        this.treasureChanceBonus = player.getAttributeValue(net.minecraft.registry.Registries.ATTRIBUTE.getEntry(SFAttributes.TREASURE_CHANCE_BONUS));
        this.goldenChanceBonus = player.getAttributeValue(net.minecraft.registry.Registries.ATTRIBUTE.getEntry(SFAttributes.GOLDEN_CHEST_BONUS));
        this.expMultiplier = player.getAttributeValue(net.minecraft.registry.Registries.ATTRIBUTE.getEntry(SFAttributes.EXP_MULTIPLIER));
        this.lavaFishing = lavaFishing;

        // According to the Minecraft wiki, each level of luck grants a 2.1% higher chance of treasure.
        float luckBonus = ItemUtils.getLuck(hook) * 0.021F;
        this.treasureChanceBonus += luckBonus;
    }

    public ItemStack getFish() {
        return fish;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = Math.max(idleTime, 0);
    }

    public float getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(float topSpeed) {
        this.topSpeed = Math.max(topSpeed, 0);
    }

    public float getUpAcceleration() {
        return upAcceleration;
    }

    public void setUpAcceleration(float upAcceleration) {
        this.upAcceleration = Math.max(upAcceleration, 0);
    }

    public float getDownAcceleration() {
        return downAcceleration;
    }

    public void setDownAcceleration(float downAcceleration) {
        this.downAcceleration = Math.max(downAcceleration, 0);
    }

    public int getAvgDistance() {
        return avgDistance;
    }

    public void setAvgDistance(int avgDistance) {
        this.avgDistance = MathHelper.clamp(avgDistance, 1, 126);
    }

    public int getMoveVariation() {
        return moveVariation;
    }

    public void setMoveVariation(int moveVariation) {
        this.moveVariation = Math.max(moveVariation, 0);
    }

    public double getLineStrength() {
        return lineStrength;
    }

    public void setLineStrength(double lineStrength) {
        this.lineStrength = MathHelper.clamp(lineStrength,
                SFAttributes.LINE_STRENGTH.getMinValue(), SFAttributes.LINE_STRENGTH.getMaxValue());
    }

    public int getBarSize() {
        return barSize;
    }

    public void setBarSize(int barSize) {
        this.barSize = MathHelper.clamp(barSize,
                (int) SFAttributes.BAR_SIZE.getMinValue(), (int) SFAttributes.BAR_SIZE.getMaxValue());
    }

    public double getTreasureChanceBonus() {
        return treasureChanceBonus;
    }

    public void setTreasureChanceBonus(double treasureChanceBonus) {
        this.treasureChanceBonus = MathHelper.clamp(treasureChanceBonus,
                SFAttributes.TREASURE_CHANCE_BONUS.getMinValue(), SFAttributes.TREASURE_CHANCE_BONUS.getMaxValue());
    }

    public double getGoldenChanceBonus() {
        return goldenChanceBonus;
    }

    public void setGoldenChanceBonus(double goldenChanceBonus) {
        this.goldenChanceBonus = MathHelper.clamp(goldenChanceBonus,
                SFAttributes.GOLDEN_CHEST_BONUS.getMinValue(), SFAttributes.GOLDEN_CHEST_BONUS.getMaxValue());
    }

    public double getExpMultiplier() {
        return expMultiplier;
    }

    public void setExpMultiplier(double expMultiplier) {
        this.expMultiplier = MathHelper.clamp(expMultiplier,
                SFAttributes.EXP_MULTIPLIER.getMinValue(), SFAttributes.EXP_MULTIPLIER.getMaxValue());
    }

    public boolean isForcedTreasureChest() {
        return forcedTreasureChest;
    }

    public void setForcedTreasureChest(boolean forcedTreasureChest) {
        this.forcedTreasureChest = forcedTreasureChest;
    }

    public boolean isForcedGoldenChest() {
        return forcedGoldenChest;
    }

    public void setForcedGoldenChest(boolean forcedGoldenChest) {
        this.forcedGoldenChest = forcedGoldenChest;
    }

    public boolean isLavaFishing() {
        return lavaFishing;
    }

    public void setLavaFishing(boolean lavaFishing) {
        this.lavaFishing = lavaFishing;
    }

    public int getQualityBoost() {
        return qualityBoost;
    }

    public void setQualityBoost(int qualityBoost) {
        this.qualityBoost = qualityBoost;
    }
}
