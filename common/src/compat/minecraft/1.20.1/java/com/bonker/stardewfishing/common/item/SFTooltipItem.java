package com.bonker.stardewfishing.common.item;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SFTooltipItem extends Item {
    private List<Component> tooltip;

    public SFTooltipItem(Properties properties) {
        super(properties);
    }

    protected List<Component> makeTooltip() {
        List<Component> result = new ArrayList<>();
        result.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(StardewFishing.LIGHTER_COLOR));
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        if (tooltip == null) tooltip = List.copyOf(makeTooltip());
        lines.addAll(tooltip);
    }
}
