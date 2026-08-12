package com.bonker.stardewfishing.gameplay.items;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

/** The item stack attached to a fishing rod through the {@code bobber} data component. */
public record Bobber(ItemStack item) {
    public static final Codec<Bobber> CODEC = ItemStack.OPTIONAL_CODEC.xmap(Bobber::new, Bobber::item);

    public static final PacketCodec<RegistryByteBuf, Bobber> PACKET_CODEC =
            ItemStack.OPTIONAL_PACKET_CODEC.xmap(Bobber::new, Bobber::item);
}
