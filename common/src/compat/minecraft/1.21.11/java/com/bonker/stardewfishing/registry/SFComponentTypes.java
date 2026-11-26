package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.items.Bobber;
import com.bonker.stardewfishing.gameplay.items.LegendaryCatch;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Item data component type registrations. */
public final class SFComponentTypes {
    public static final ComponentType<Bobber> BOBBER =
            register("bobber", ComponentType.<Bobber>builder()
                    .codec(Bobber.CODEC)
                    .packetCodec(Bobber.PACKET_CODEC)
                    .build());

    public static final ComponentType<LegendaryCatch> LEGENDARY_CATCH =
            register("legendary_catch", ComponentType.<LegendaryCatch>builder()
                    .codec(LegendaryCatch.CODEC)
                    .packetCodec(LegendaryCatch.PACKET_CODEC)
                    .build());

    public static final ComponentType<String> POKEMON_SPECIES =
            register("pokemon_species", ComponentType.<String>builder()
                    .codec(Codec.STRING)
                    .packetCodec(PacketCodecs.STRING)
                    .build());

    private SFComponentTypes() {
    }

    private static <T> ComponentType<T> register(String id, ComponentType<T> type) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, StardewFishing.identifier(id), type);
    }
}
