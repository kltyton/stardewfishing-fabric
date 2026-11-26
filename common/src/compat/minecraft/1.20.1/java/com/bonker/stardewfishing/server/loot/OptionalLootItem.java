package com.bonker.stardewfishing.server.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.bonker.stardewfishing.registry.SFLootPoolEntryTypes;
import com.bonker.stardewfishing.compat.tide.TideCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class OptionalLootItem extends LootPoolSingletonContainer {
    private final ResourceLocation itemId;
    private final @Nullable Item item;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new OptionalEntryBase() {
        @Override
        public void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
            OptionalLootItem.this.createItemStack(
                    LootItemFunction.decorate(OptionalLootItem.this.compositeFunction, consumer, context), context);
        }
    };

    private OptionalLootItem(ResourceLocation itemId, int weight, int quality,
                             LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.itemId = itemId;
        Item resolved = TideCompat.resolveRenamedItem(itemId);
        this.item = resolved == Items.AIR ? null : resolved;
        this.compositeFunction = LootItemFunctions.compose(functions);
    }

    @Override
    public boolean expand(LootContext context, Consumer<LootPoolEntry> consumer) {
        if (!canRun(context)) return false;
        consumer.accept(entry);
        return true;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        if (item != null) consumer.accept(new ItemStack(item));
    }

    @Override
    public LootPoolEntryType getType() {
        return SFLootPoolEntryTypes.OPTIONAL;
    }

    private abstract class OptionalEntryBase extends LootPoolSingletonContainer.EntryBase {
        @Override
        public int getWeight(float luck) {
            return item == null ? 0 : super.getWeight(luck);
        }
    }

    public static final class Serializer extends LootPoolSingletonContainer.Serializer<OptionalLootItem> {
        @Override
        public void serializeCustom(JsonObject json, OptionalLootItem entry, JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("name", entry.itemId.toString());
        }

        @Override
        protected OptionalLootItem deserialize(JsonObject json, JsonDeserializationContext context, int weight,
                                               int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
            return new OptionalLootItem(new ResourceLocation(GsonHelper.getAsString(json, "name")),
                    weight, quality, conditions, functions);
        }
    }
}
