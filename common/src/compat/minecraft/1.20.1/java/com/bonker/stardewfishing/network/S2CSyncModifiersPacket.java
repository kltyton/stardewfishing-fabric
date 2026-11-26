package com.bonker.stardewfishing.network;

import com.bonker.stardewfishing.server.resource.MinigameModifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncModifiersPacket(Map<Item, MinigameModifiers> data) {
    public S2CSyncModifiersPacket(FriendlyByteBuf buf) {
        this(read(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(data.size());
        data.forEach((item, modifiers) -> {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
            modifiers.write(buf);
        });
    }

    private static Map<Item, MinigameModifiers> read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > BuiltInRegistries.ITEM.size()) {
            throw new IllegalArgumentException("Invalid modifier map size: " + size);
        }
        Map<Item, MinigameModifiers> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            result.put(BuiltInRegistries.ITEM.get(buf.readResourceLocation()), MinigameModifiers.read(buf));
        }
        return result;
    }
}
