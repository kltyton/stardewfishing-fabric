package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.PokemonPlaceholderItem;
import com.bonker.stardewfishing.common.item.SFTooltipItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Function;

@NullMarked
public class SFItems {
    public static final int LEGENDARY_FISH_COLOR = 0xFFFFBE;
    public static final Component LEGENDARY_FISH_TOOLTIP = Component.translatable("tooltip.stardew_fishing.legendary_fish")
            .withStyle(StardewFishing.LEGENDARY);

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, StardewFishing.identifier("items"));

    public static final Item TRAP_BOBBER = registerItem("trap_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final Item CORK_BOBBER = registerItem("cork_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final Item SONAR_BOBBER = registerItem("sonar_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final Item TREASURE_BOBBER = registerItem("treasure_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final Item QUALITY_BOBBER = registerItem("quality_bobber",
            prop -> new SFTooltipItem(prop.durability(64)) {
        @Override
        protected List<Component> makeTooltip() {
            List<Component> tooltip = super.makeTooltip();
            if (StardewFishing.QUALITY_FOOD_INSTALLED) {
                tooltip.add(1, Component.translatable(getDescriptionId() + ".quality_food_tooltip").withStyle(StardewFishing.LIGHTER_COLOR));
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

    public static void registerCreativeTab() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.stardewFishing"))
                .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH))
                .build());
    }

    public static void addToCreativeTab(FabricCreativeModeTabOutput output) {
        output.accept(SFBlocks.FISH_DISPLAY);
        output.accept(TRAP_BOBBER);
        output.accept(CORK_BOBBER);
        output.accept(SONAR_BOBBER);
        output.accept(TREASURE_BOBBER);
        output.accept(QUALITY_BOBBER);
        output.accept(GOLIATH_GROUPER);
        output.accept(VAMPIRE_PAYARA);
        output.accept(GOLDEN_SNOOK);
        output.accept(SABRETOOTHED_TIGERFISH);
        output.accept(CHROMATIC_ARAPAIMA);
        output.accept(CYCLOPS_MAHIMAHI);
        output.accept(STORM_TARPON);
        output.accept(BLAZING_OARFISH);
        output.accept(CRYSTALLINE_SNAKEHEAD);
        output.accept(DEMON_GAR);
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> constructor) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StardewFishing.identifier(name));
        return Registry.register(BuiltInRegistries.ITEM, key, constructor.apply(new Item.Properties().setId(key)));
    }
}
