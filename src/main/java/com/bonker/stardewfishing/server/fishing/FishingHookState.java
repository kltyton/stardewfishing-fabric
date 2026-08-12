package com.bonker.stardewfishing.server.fishing;

import com.bonker.stardewfishing.api.event.StardewMinigameStartedEvent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface FishingHookState {
    LockableList<ItemStack> stardewFishing$getRewards();
    boolean stardewFishing$hasTreasureChest();
    void stardewFishing$setTreasureChest(boolean value);
    boolean stardewFishing$hasGoldenChest();
    void stardewFishing$setGoldenChest(boolean value);
    @Nullable StardewMinigameStartedEvent stardewFishing$getEvent();
    void stardewFishing$setEvent(@Nullable StardewMinigameStartedEvent event);

    static FishingHookState get(Object hook) {
        return (FishingHookState) hook;
    }
}
