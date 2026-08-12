package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.PokemonPlaceholderItem;
import com.bonker.stardewfishing.common.items.SFTooltipItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SFItems {
    public static final int LEGENDARY_FISH_COLOR = 0xFFFFBE;
    public static final Component LEGENDARY_FISH_TOOLTIP = Component.translatable("tooltip.stardew_fishing.legendary_fish")
            .withStyle(StardewFishing.LEGENDARY);

    public static final Supplier<SFTooltipItem> TRAP_BOBBER = register("trap_bobber", p -> new SFTooltipItem(p.durability(64)));
    public static final Supplier<SFTooltipItem> CORK_BOBBER = register("cork_bobber", p -> new SFTooltipItem(p.durability(64)));
    public static final Supplier<SFTooltipItem> SONAR_BOBBER = register("sonar_bobber", p -> new SFTooltipItem(p.durability(64)));
    public static final Supplier<SFTooltipItem> TREASURE_BOBBER = register("treasure_bobber", p -> new SFTooltipItem(p.durability(64)));
    public static final Supplier<SFTooltipItem> QUALITY_BOBBER = register("quality_bobber", p -> new SFTooltipItem(p.durability(64)) {
        @Override
        protected List<Component> makeTooltip() {
            List<Component> tooltip = super.makeTooltip();
            if (StardewFishing.QUALITY_FOOD_INSTALLED) {
                tooltip.add(1, Component.translatable(getDescriptionId() + ".quality_food_tooltip")
                        .withStyle(StardewFishing.LIGHTER_COLOR));
            }
            return tooltip;
        }
    });

    public static final Supplier<SFTooltipItem> GOLIATH_GROUPER = register("goliath_grouper", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> VAMPIRE_PAYARA = register("vampire_payara", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> GOLDEN_SNOOK = register("golden_snook", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> SABRETOOTHED_TIGERFISH = register("sabretoothed_tigerfish", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> CHROMATIC_ARAPAIMA = register("chromatic_arapaima", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> CYCLOPS_MAHIMAHI = register("cyclops_mahimahi", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> STORM_TARPON = register("storm_tarpon", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> BLAZING_OARFISH = register("blazing_oarfish", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> CRYSTALLINE_SNAKEHEAD = register("crystalline_snakehead", SFTooltipItem::new);
    public static final Supplier<SFTooltipItem> DEMON_GAR = register("demon_gar", SFTooltipItem::new);
    public static final Supplier<Item> POKEMON_PLACEHOLDER = register("pokemon_placeholder", PokemonPlaceholderItem::new);

    private static <T extends Item> Supplier<T> register(String id, Function<Item.Properties, T> factory) {
        T item = Registry.register(BuiltInRegistries.ITEM, StardewFishing.resource(id), factory.apply(new Item.Properties()));
        return () -> item;
    }

    public static void initialize() {
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.stardewFishing"))
                .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH.get()))
                .displayItems((parameters, output) -> {
                    output.accept(SFBlocks.FISH_DISPLAY.get());
                    output.accept(TRAP_BOBBER.get());
                    output.accept(CORK_BOBBER.get());
                    output.accept(SONAR_BOBBER.get());
                    output.accept(TREASURE_BOBBER.get());
                    output.accept(QUALITY_BOBBER.get());
                    output.accept(GOLIATH_GROUPER.get());
                    output.accept(VAMPIRE_PAYARA.get());
                    output.accept(GOLDEN_SNOOK.get());
                    output.accept(SABRETOOTHED_TIGERFISH.get());
                    output.accept(CHROMATIC_ARAPAIMA.get());
                    output.accept(CYCLOPS_MAHIMAHI.get());
                    output.accept(STORM_TARPON.get());
                    output.accept(BLAZING_OARFISH.get());
                    output.accept(CRYSTALLINE_SNAKEHEAD.get());
                    output.accept(DEMON_GAR.get());
                }).build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, StardewFishing.resource("items"), tab);
    }

    private SFItems() {
    }
}
