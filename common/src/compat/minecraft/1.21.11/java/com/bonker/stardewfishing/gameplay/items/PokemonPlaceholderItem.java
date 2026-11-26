package com.bonker.stardewfishing.gameplay.items;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.registry.SFComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Placeholder used by the (intentionally inactive) Cobblemon integration. The
 * canonical NeoForge reference registers it and marks it as starting the
 * minigame; the Cobblemon spawn code itself is commented out there, so on
 * Fabric the item behaves identically (a named placeholder item).
 */
public class PokemonPlaceholderItem extends Item {
    public PokemonPlaceholderItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        String species = stack.get(SFComponentTypes.POKEMON_SPECIES);
        species = species == null ? "Unknown Pokémon" : species;
        if (Character.isLowerCase(species.charAt(0))) {
            species = Character.toUpperCase(species.charAt(0)) + species.substring(1);
        }
        return Text.literal(species).setStyle(StardewFishing.LIGHTER_COLOR);
    }
}
