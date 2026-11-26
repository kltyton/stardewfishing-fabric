package com.bonker.stardewfishing.gameplay.items;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Item with a cached tooltip from {@code <item>.tooltip} translation keys. */
public class SFTooltipItem extends Item {
    private List<Text> tooltip;

    public SFTooltipItem(Settings settings) {
        super(settings);
    }

    protected List<Text> makeTooltip() {
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").setStyle(StardewFishing.LIGHTER_COLOR));
        return tooltip;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
                              Consumer<Text> tooltipAdder, TooltipType type) {
        if (tooltip == null) {
            tooltip = makeTooltip();
        }
        for (Text component : tooltip) {
            tooltipAdder.accept(component);
        }
    }
}
