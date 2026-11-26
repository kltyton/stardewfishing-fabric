package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.SFTooltipItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public final class SFItems {
    public static final int LEGENDARY_FISH_COLOR = 0xFFFFBE;
    public static final Component LEGENDARY_FISH_TOOLTIP = Component.translatable("tooltip.stardew_fishing.legendary_fish")
            .withStyle(StardewFishing.LEGENDARY);

    public static final SFTooltipItem TRAP_BOBBER = registerBobber("trap_bobber");
    public static final SFTooltipItem CORK_BOBBER = registerBobber("cork_bobber");
    public static final SFTooltipItem SONAR_BOBBER = registerBobber("sonar_bobber");
    public static final SFTooltipItem TREASURE_BOBBER = registerBobber("treasure_bobber");
    public static final SFTooltipItem QUALITY_BOBBER = registerBobber("quality_bobber");

    public static final SFTooltipItem GOLIATH_GROUPER = register("goliath_grouper", SFTooltipItem::new);
    public static final SFTooltipItem VAMPIRE_PAYARA = register("vampire_payara", SFTooltipItem::new);
    public static final SFTooltipItem GOLDEN_SNOOK = register("golden_snook", SFTooltipItem::new);
    public static final SFTooltipItem SABRETOOTHED_TIGERFISH = register("sabretoothed_tigerfish", SFTooltipItem::new);
    public static final SFTooltipItem CHROMATIC_ARAPAIMA = register("chromatic_arapaima", SFTooltipItem::new);
    public static final SFTooltipItem CYCLOPS_MAHIMAHI = register("cyclops_mahimahi", SFTooltipItem::new);
    public static final SFTooltipItem STORM_TARPON = register("storm_tarpon", SFTooltipItem::new);
    public static final SFTooltipItem BLAZING_OARFISH = register("blazing_oarfish", SFTooltipItem::new);
    public static final SFTooltipItem CRYSTALLINE_SNAKEHEAD = register("crystalline_snakehead", SFTooltipItem::new);
    public static final SFTooltipItem DEMON_GAR = register("demon_gar", SFTooltipItem::new);
    public static final CreativeModeTab TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            StardewFishing.identifier("items"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.stardewFishing"))
                    .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH))
                    .displayItems((parameters, output) -> {
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
                    })
                    .build()
    );

    private SFItems() {
    }

    private static SFTooltipItem registerBobber(String name) {
        return register(name, properties -> new SFTooltipItem(properties.durability(64)));
    }

    public static void registerBlockItem(String name, net.minecraft.world.level.block.Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StardewFishing.identifier(name));
        Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory) {
        Identifier id = StardewFishing.identifier(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }
}
