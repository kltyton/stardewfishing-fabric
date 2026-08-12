package com.bonker.stardewfishing.gameplay.loot;

import com.bonker.stardewfishing.registry.SFLootPoolEntryTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Loot entry that only generates a stack when the referenced item is
 * registered (used by the hand-written treasure chest tables for optional
 * cross-mod items).
 */
public class OptionalLootItem extends LeafEntry {
    public static final MapCodec<OptionalLootItem> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("name").forGetter(o -> o.itemId)
    ).and(addLeafFields(inst)).apply(inst, OptionalLootItem::new));

    private final Identifier itemId;
    @Nullable
    private final Item item;
    private final BiFunction<ItemStack, LootContext, ItemStack> compiledOptionalFunctions;
    private final LootChoice choice = new LeafEntry.Choice() {
        @Override
        public int getWeight(float luck) {
            return item == null ? 0 : super.getWeight(luck);
        }

        @Override
        public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
            OptionalLootItem.this.generateLoot(
                    LootFunction.apply(OptionalLootItem.this.compiledOptionalFunctions, lootConsumer, context), context);
        }
    };

    protected OptionalLootItem(Identifier itemId, int weight, int quality,
                               List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.compiledOptionalFunctions = LootFunctionTypes.join(functions);
        this.itemId = itemId;
        Item item = Registries.ITEM.get(itemId);
        this.item = item == Items.AIR ? null : item;
    }

    @Override
    public boolean expand(LootContext context, Consumer<LootChoice> consumer) {
        if (this.test(context)) {
            consumer.accept(choice);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void generateLoot(Consumer<ItemStack> stackConsumer, LootContext context) {
        stackConsumer.accept(item == null ? ItemStack.EMPTY : new ItemStack(item));
    }

    @Override
    public LootPoolEntryType getType() {
        return SFLootPoolEntryTypes.MOD_LOADED;
    }
}
