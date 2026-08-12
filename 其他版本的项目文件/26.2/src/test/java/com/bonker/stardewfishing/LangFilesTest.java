package com.bonker.stardewfishing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangFilesTest {
    private static final String IN_FISH_DISPLAY_KEY = "tag.item.stardew_fishing.in_fish_display";

    private static JsonObject lang(String language) throws Exception {
        Path path = Path.of("src/main/resources/assets/stardew_fishing/lang", language + ".json");
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    void supportedLanguagesProvideInFishDisplayTagName() throws Exception {
        assertEquals("Fish Display Items", lang("en_us").get(IN_FISH_DISPLAY_KEY).getAsString());
        assertEquals("鱼类展示物品", lang("zh_cn").get(IN_FISH_DISPLAY_KEY).getAsString());
        assertEquals("Предметы для витрины", lang("ru_ru").get(IN_FISH_DISPLAY_KEY).getAsString());
    }

    @Test
    void blockNameKeyExistsForFishDisplay() throws Exception {
        for (String language : new String[] {"en_us", "zh_cn", "ru_ru"}) {
            assertTrue(lang(language).has("block.stardew_fishing.fish_display"),
                    language + " must define block.stardew_fishing.fish_display");
        }
    }
}
