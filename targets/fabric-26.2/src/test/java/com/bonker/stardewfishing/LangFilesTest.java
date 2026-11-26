package com.bonker.stardewfishing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangFilesTest {
    private static final String IN_FISH_DISPLAY_KEY = "tag.item.stardew_fishing.in_fish_display";

    private static JsonObject lang(String language) throws Exception {
        String path = "/assets/stardew_fishing/lang/" + language + ".json";
        try (InputStream input = LangFilesTest.class.getResourceAsStream(path)) {
            assertNotNull(input, path);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
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
