package com.bonker.stardewfishing.api.event;

import com.bonker.stardewfishing.gameplay.minigame.FishBehavior;
import com.bonker.stardewfishing.common.item.FishingItemSupport;
import com.bonker.stardewfishing.registry.SFAttributes;
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
    private boolean forcedTreasureChest;
    private boolean forcedGoldenChest;
    private boolean lavaFishing;
    private int qualityBoost;

    public StardewMinigameStartedEvent(ServerPlayer player, FishingHook hook, ItemStack fishingRod,
                                       ItemStack fish, FishBehavior behavior, boolean lavaFishing) {
        super(player, hook, fishingRod);
        this.fish = fish;
        setIdleTime(behavior.idleTime());
        setTopSpeed(behavior.topSpeed());
        setUpAcceleration(behavior.upAcceleration());
        setDownAcceleration(behavior.downAcceleration());
        setAvgDistance(behavior.avgDistance());
        setMoveVariation(behavior.moveVariation());
        setLineStrength(player.getAttributeValue(SFAttributes.LINE_STRENGTH));
        setBarSize((int) player.getAttributeValue(SFAttributes.BAR_SIZE));
        setTreasureChanceBonus(player.getAttributeValue(SFAttributes.TREASURE_CHANCE_BONUS));
        setGoldenChanceBonus(player.getAttributeValue(SFAttributes.GOLDEN_CHEST_BONUS));
        setExpMultiplier(player.getAttributeValue(SFAttributes.EXP_MULTIPLIER));
        this.lavaFishing = lavaFishing;
        setTreasureChanceBonus(treasureChanceBonus + FishingItemSupport.getLuck(hook) * 0.021F);
    }

    public ItemStack getFish() { return fish; }
    public int getIdleTime() { return idleTime; }
    public void setIdleTime(int value) { idleTime = Mth.clamp(value, 0, Short.MAX_VALUE); }
    public float getTopSpeed() { return topSpeed; }
    public void setTopSpeed(float value) { if (Float.isFinite(value)) topSpeed = Math.max(value, 0); }
    public float getUpAcceleration() { return upAcceleration; }
    public void setUpAcceleration(float value) { if (Float.isFinite(value)) upAcceleration = Math.max(value, 0); }
    public float getDownAcceleration() { return downAcceleration; }
    public void setDownAcceleration(float value) { if (Float.isFinite(value)) downAcceleration = Math.max(value, 0); }
    public int getAvgDistance() { return avgDistance; }
    public void setAvgDistance(int value) { avgDistance = Mth.clamp(value, 1, 126); }
    public int getMoveVariation() { return moveVariation; }
    public void setMoveVariation(int value) { moveVariation = Mth.clamp(value, 0, 127); }
    public double getLineStrength() { return lineStrength; }
    public void setLineStrength(double value) {
        if (Double.isFinite(value)) {
            lineStrength = Mth.clamp(value, SFAttributes.LINE_STRENGTH.getMinValue(), SFAttributes.LINE_STRENGTH.getMaxValue());
        }
    }
    public int getBarSize() { return barSize; }
    public void setBarSize(int value) {
        barSize = Mth.clamp(value, (int) SFAttributes.BAR_SIZE.getMinValue(), (int) SFAttributes.BAR_SIZE.getMaxValue());
    }
    public double getTreasureChanceBonus() { return treasureChanceBonus; }
    public void setTreasureChanceBonus(double value) {
        if (Double.isFinite(value)) {
            treasureChanceBonus = Mth.clamp(value, SFAttributes.TREASURE_CHANCE_BONUS.getMinValue(),
                    SFAttributes.TREASURE_CHANCE_BONUS.getMaxValue());
        }
    }
    public double getGoldenChanceBonus() { return goldenChestBonus; }
    public void setGoldenChanceBonus(double value) {
        if (Double.isFinite(value)) {
            goldenChestBonus = Mth.clamp(value, SFAttributes.GOLDEN_CHEST_BONUS.getMinValue(),
                    SFAttributes.GOLDEN_CHEST_BONUS.getMaxValue());
        }
    }
    public double getExpMultiplier() { return expMultiplier; }
    public void setExpMultiplier(double value) {
        if (Double.isFinite(value)) {
            expMultiplier = Mth.clamp(value, SFAttributes.EXP_MULTIPLIER.getMinValue(),
                    SFAttributes.EXP_MULTIPLIER.getMaxValue());
        }
    }
    public boolean isForcedTreasureChest() { return forcedTreasureChest; }
    public void setForcedTreasureChest(boolean value) { forcedTreasureChest = value; }
    public boolean isForcedGoldenChest() { return forcedGoldenChest; }
    public void setForcedGoldenChest(boolean value) { forcedGoldenChest = value; }
    public boolean isLavaFishing() { return lavaFishing; }
    public void setLavaFishing(boolean value) { lavaFishing = value; }
    public int getQualityBoost() { return qualityBoost; }
    public void setQualityBoost(int value) { qualityBoost = value; }
}
