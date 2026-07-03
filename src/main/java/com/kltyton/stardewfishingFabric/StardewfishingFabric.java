package com.kltyton.stardewfishingFabric;

import com.kltyton.stardewfishingFabric.common.CommonEvents;
import com.kltyton.stardewfishingFabric.common.networking.SFNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class StardewfishingFabric implements ModInitializer {
    /*
        常量定义
    */
    public static final String MODID = "stardew_fishing";
    public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENT_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of(MODID, "sound_events"));
    public static final TagKey<Item> STARTS_MINIGAME = TagKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "starts_minigame"));
    public static final Registry<SoundEvent> SOUND_EVENTS = FabricRegistryBuilder.createSimple(SOUND_EVENT_REGISTRY_KEY).buildAndRegister();
    /*
        声音事件定义
    */
    public static final SoundEvent CAST = registerSound("cast");
    public static final SoundEvent COMPLETE = registerSound("complete");
    public static final SoundEvent DWOP = registerSound("dwop");
    public static final SoundEvent FISH_ESCAPE = registerSound("fish_escape");
    public static final SoundEvent FISH_BITE = registerSound("fish_bite");
    public static final SoundEvent FISH_HIT = registerSound("fish_hit");
    public static final SoundEvent PULL_ITEM = registerSound("pull_item");
    public static final SoundEvent REEL_CREAK = registerSound("reel_creak");
    public static final SoundEvent REEL_FAST = registerSound("reel_fast");
    public static final SoundEvent REEL_SLOW = registerSound("reel_slow");

    @Override
    public void onInitialize() {
        SFNetworking.registerPayloads();
        SFNetworking.registerServerReceivers();
        CommonEvents.initialize();
        // 注册声音事件到注册表
        registerSoundEvent("cast", CAST);
        registerSoundEvent("complete", COMPLETE);
        registerSoundEvent("dwop", DWOP);
        registerSoundEvent("fish_escape", FISH_ESCAPE);
        registerSoundEvent("fish_bite", FISH_BITE);
        registerSoundEvent("fish_hit", FISH_HIT);
        registerSoundEvent("pull_item", PULL_ITEM);
        registerSoundEvent("reel_creak", REEL_CREAK);
        registerSoundEvent("reel_fast", REEL_FAST);
        registerSoundEvent("reel_slow", REEL_SLOW);
    }
    // 创建新的声音事件
    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of(MODID, name);
        return SoundEvent.of(id);
    }

    private static void registerSoundEvent(String name, SoundEvent soundEvent) {
        // 将声音事件注册到注册表
        Registry.register(SOUND_EVENTS, Identifier.of(MODID, name), soundEvent);
    }
}
