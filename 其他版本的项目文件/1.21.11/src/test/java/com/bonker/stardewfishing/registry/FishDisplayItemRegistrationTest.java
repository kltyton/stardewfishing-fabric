package com.bonker.stardewfishing.registry;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Locks the 1.21.11 block-to-item mapping required by Block.asItem(). */
public final class FishDisplayItemRegistrationTest {
    private FishDisplayItemRegistrationTest() {
    }

    public static void main(String[] args) throws Exception {
        Path sourcePath = Path.of("src/main/java/com/bonker/stardewfishing/registry/SFBlocks.java");
        String source = Files.readString(sourcePath, StandardCharsets.UTF_8);
        String requiredRegistration = "blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem);";
        if (!source.contains(requiredRegistration)) {
            throw new AssertionError("Fish display BlockItem must populate Item.BLOCK_ITEMS so Block.asItem() "
                    + "does not resolve to AIR");
        }
        System.out.println("FishDisplayItemRegistrationTest PASSED");
    }
}
