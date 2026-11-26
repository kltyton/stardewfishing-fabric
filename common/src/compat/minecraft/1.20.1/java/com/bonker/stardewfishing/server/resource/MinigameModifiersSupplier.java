package com.bonker.stardewfishing.server.resource;

import net.minecraft.world.item.Item;

import java.util.Map;

public interface MinigameModifiersSupplier {
    Map<Item, MinigameModifiers> getData();
}
