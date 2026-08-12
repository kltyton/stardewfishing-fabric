package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.gameplay.items.PokemonPlaceholderItem;
import com.bonker.stardewfishing.gameplay.items.SFTooltipItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.function.Function;

/** Item and creative tab registrations. */
public final class SFItems {
    public static final int LEGENDARY_FISH_COLOR = 0xFFFFBE;
    public static final Text LEGENDARY_FISH_TOOLTIP =
            Text.translatable("tooltip.stardew_fishing.legendary_fish").setStyle(StardewFishing.LEGENDARY);

    public static final Item TRAP_BOBBER = registerItem("trap_bobber",
            prop -> new SFTooltipItem(prop.maxDamage(64)));
    public static final Item CORK_BOBBER = registerItem("cork_bobber",
            prop -> new SFTooltipItem(prop.maxDamage(64)));
    public static final Item SONAR_BOBBER = registerItem("sonar_bobber",
            prop -> new SFTooltipItem(prop.maxDamage(64)));
    public static final Item TREASURE_BOBBER = registerItem("treasure_bobber",
            prop -> new SFTooltipItem(prop.maxDamage(64)));
    public static final Item QUALITY_BOBBER = registerItem("quality_bobber",
            prop -> new SFTooltipItem(prop.maxDamage(64)) {
                @Override
                protected java.util.List<Text> makeTooltip() {
                    java.util.List<Text> tooltip = super.makeTooltip();
                    if (StardewFishing.QUALITY_FOOD_INSTALLED) {
                        tooltip.add(1, Text.translatable(getTranslationKey() + ".quality_food_tooltip")
                                .setStyle(StardewFishing.LIGHTER_COLOR));
                    }
                    return tooltip;
                }
            });

    public static final Item GOLIATH_GROUPER = registerItem("goliath_grouper", SFTooltipItem::new);
    public static final Item VAMPIRE_PAYARA = registerItem("vampire_payara", SFTooltipItem::new);
    public static final Item GOLDEN_SNOOK = registerItem("golden_snook", SFTooltipItem::new);
    public static final Item SABRETOOTHED_TIGERFISH = registerItem("sabretoothed_tigerfish", SFTooltipItem::new);
    public static final Item CHROMATIC_ARAPAIMA = registerItem("chromatic_arapaima", SFTooltipItem::new);
    public static final Item CYCLOPS_MAHIMAHI = registerItem("cyclops_mahimahi", SFTooltipItem::new);
    public static final Item STORM_TARPON = registerItem("storm_tarpon", SFTooltipItem::new);
    public static final Item BLAZING_OARFISH = registerItem("blazing_oarfish", SFTooltipItem::new);
    public static final Item CRYSTALLINE_SNAKEHEAD = registerItem("crystalline_snakehead", SFTooltipItem::new);
    public static final Item DEMON_GAR = registerItem("demon_gar", SFTooltipItem::new);

    public static final Item POKEMON_PLACEHOLDER = registerItem("pokemon_placeholder", PokemonPlaceholderItem::new);

    private SFItems() {
    }

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, StardewFishing.identifier("items"),
                ItemGroup.create(ItemGroup.Row.TOP, 0)
                        .displayName(Text.translatable("itemGroup.stardewFishing"))
                        .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH))
                        .entries((context, entries) -> {
                            entries.add(SFBlocks.FISH_DISPLAY);
                            entries.add(TRAP_BOBBER);
                            entries.add(CORK_BOBBER);
                            entries.add(SONAR_BOBBER);
                            entries.add(TREASURE_BOBBER);
                            entries.add(QUALITY_BOBBER);
                            entries.add(GOLIATH_GROUPER);
                            entries.add(VAMPIRE_PAYARA);
                            entries.add(GOLDEN_SNOOK);
                            entries.add(SABRETOOTHED_TIGERFISH);
                            entries.add(CHROMATIC_ARAPAIMA);
                            entries.add(CYCLOPS_MAHIMAHI);
                            entries.add(STORM_TARPON);
                            entries.add(BLAZING_OARFISH);
                            entries.add(CRYSTALLINE_SNAKEHEAD);
                            entries.add(DEMON_GAR);
                        })
                        .build());
    }

    private static Item registerItem(String id, Function<Item.Settings, Item> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, StardewFishing.identifier(id));
        return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().registryKey(key)));
    }

}
