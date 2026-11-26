package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.item.SFBobberItem;
import com.bonker.stardewfishing.common.item.SFTooltipItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SFItems {
    public static final int LEGENDARY_FISH_COLOR = 0xffffbe;
    public static final Component LEGENDARY_FISH_TOOLTIP = Component.translatable("tooltip.stardew_fishing.legendary_fish")
            .withStyle(StardewFishing.LEGENDARY);

    public static final Item FISH_DISPLAY = register("fish_display", new BlockItem(SFBlocks.FISH_DISPLAY, new Item.Properties()));
    public static final Item TRAP_BOBBER = register("trap_bobber", bobber());
    public static final Item CORK_BOBBER = register("cork_bobber", bobber());
    public static final Item SONAR_BOBBER = register("sonar_bobber", bobber());
    public static final Item TREASURE_BOBBER = register("treasure_bobber", bobber());
    public static final Item QUALITY_BOBBER = register("quality_bobber", bobber());
    public static final Item GOLIATH_GROUPER = register("goliath_grouper", fish());
    public static final Item VAMPIRE_PAYARA = register("vampire_payara", fish());
    public static final Item GOLDEN_SNOOK = register("golden_snook", fish());
    public static final Item SABRETOOTHED_TIGERFISH = register("sabretoothed_tigerfish", fish());
    public static final Item CHROMATIC_ARAPAIMA = register("chromatic_arapaima", fish());
    public static final Item CYCLOPS_MAHIMAHI = register("cyclops_mahimahi", fish());
    public static final Item STORM_TARPON = register("storm_tarpon", fish());
    public static final Item BLAZING_OARFISH = register("blazing_oarfish", fish());
    public static final Item CRYSTALLINE_SNAKEHEAD = register("crystalline_snakehead", fish());
    public static final Item DEMON_GAR = register("demon_gar", fish());

    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            StardewFishing.resource("items"), FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.stardewFishing"))
                    .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH))
                    .displayItems((parameters, output) -> {
                        output.accept(FISH_DISPLAY);
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
                    }).build());

    private SFItems() {
    }

    public static void initialize() {
    }

    private static Item bobber() {
        return new SFBobberItem(new Item.Properties().durability(64));
    }

    private static Item fish() {
        return new SFTooltipItem(new Item.Properties());
    }

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, StardewFishing.resource(name), item);
    }
}
