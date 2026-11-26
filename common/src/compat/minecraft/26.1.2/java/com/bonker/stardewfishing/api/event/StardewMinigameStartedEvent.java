package com.bonker.stardewfishing.api.event;

import com.bonker.stardewfishing.gameplay.minigame.FishBehavior;
import com.bonker.stardewfishing.registry.SFAttributes;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

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
    private double goldenChestBonus;
    private double expMultiplier;
    private boolean forcedTreasureChest = false;
    private boolean forcedGoldenChest = false;
    private boolean lavaFishing;
    private int qualityBoost = 0;

    public StardewMinigameStartedEvent(ServerPlayer player, FishingHook hook, ItemStack fishingRod,
                                       ItemStack fish, FishBehavior behavior, boolean lavaFishing) {
        super(player, hook, fishingRod);
        this.fish = fish;
        this.idleTime = behavior.idleTime();
        this.topSpeed = behavior.topSpeed();
        this.upAcceleration = behavior.upAcceleration();
        this.downAcceleration = behavior.downAcceleration();
        this.avgDistance = behavior.avgDistance();
        this.moveVariation = behavior.moveVariation();
        this.lineStrength = player.getAttributeValue(SFAttributes.LINE_STRENGTH);
        this.barSize = (int) player.getAttributeValue(SFAttributes.BAR_SIZE);
        this.treasureChanceBonus = player.getAttributeValue(SFAttributes.TREASURE_CHANCE_BONUS);
        this.goldenChestBonus = player.getAttributeValue(SFAttributes.GOLDEN_CHEST_BONUS);
        this.expMultiplier = player.getAttributeValue(SFAttributes.EXP_MULTIPLIER);
        this.lavaFishing = lavaFishing;

        // according to the minecraft wiki, each level of luck grants a 2.1% higher chance of treasure
        float luckBonus = FishingItemSupport.getLuck(hook) * 0.021F;
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
        this.avgDistance = Mth.clamp(avgDistance, 1, 126);
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
        this.lineStrength = Mth.clamp(lineStrength, ranged(SFAttributes.LINE_STRENGTH).getMinValue(), ranged(SFAttributes.LINE_STRENGTH).getMaxValue());
    }

    public int getBarSize() {
        return barSize;
    }

    public void setBarSize(int barSize) {
        this.barSize = Mth.clamp(barSize, (int) ranged(SFAttributes.BAR_SIZE).getMinValue(), (int) ranged(SFAttributes.BAR_SIZE).getMaxValue());
    }

    public double getTreasureChanceBonus() {
        return treasureChanceBonus;
    }

    public void setTreasureChanceBonus(double treasureChanceBonus) {
        this.treasureChanceBonus = Mth.clamp(treasureChanceBonus, ranged(SFAttributes.TREASURE_CHANCE_BONUS).getMinValue(), ranged(SFAttributes.TREASURE_CHANCE_BONUS).getMaxValue());
    }

    public double getGoldenChanceBonus() {
        return goldenChestBonus;
    }

    public void setGoldenChanceBonus(double goldenChestBonus) {
        this.goldenChestBonus = Mth.clamp(goldenChestBonus, ranged(SFAttributes.GOLDEN_CHEST_BONUS).getMinValue(), ranged(SFAttributes.GOLDEN_CHEST_BONUS).getMaxValue());
    }

    public double getExpMultiplier() {
        return expMultiplier;
    }

    public void setExpMultiplier(double expMultiplier) {
        this.expMultiplier = Mth.clamp(expMultiplier, ranged(SFAttributes.EXP_MULTIPLIER).getMinValue(), ranged(SFAttributes.EXP_MULTIPLIER).getMaxValue());
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

    private static net.minecraft.world.entity.ai.attributes.RangedAttribute ranged(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        return (net.minecraft.world.entity.ai.attributes.RangedAttribute) attribute.value();
    }
}
