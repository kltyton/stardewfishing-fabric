package com.bonker.stardewfishing.common.block;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.util.Objects;

/**
 * Lifecycle contract regression test for the fish display drop hook.
 *
 * <p>In vanilla 26.1.2 {@code LevelChunk.setBlockState} removes the block entity
 * ({@code removeBlockEntity}) <b>before</b> invoking
 * {@code BlockState.affectNeighborsAfterRemoval}, so a drop implemented in the block hook
 * can never read the block entity. The drop must live in
 * {@code FishDisplayBlockEntity.preRemoveSideEffects}, which vanilla invokes while the block
 * entity is still alive (same pattern as {@code JukeboxBlockEntity.preRemoveSideEffects}).
 *
 * <p>The assertions inspect the compiled class files (JDK ClassFile API) so no Minecraft
 * classes are loaded in this bare-JVM test.
 */
public final class FishDisplayLifecycleTest {
    private static final String BLOCK = "com/bonker/stardewfishing/common/block/FishDisplayBlock.class";
    private static final String BLOCK_ENTITY = "com/bonker/stardewfishing/common/block/FishDisplayBlockEntity.class";

    private FishDisplayLifecycleTest() {
    }

    public static void main(String[] args) throws IOException {
        assertDeclares(BLOCK_ENTITY, "preRemoveSideEffects");
        assertDoesNotDeclare(BLOCK, "affectNeighborsAfterRemoval");
        System.out.println("FishDisplayLifecycleTest passed");
    }

    private static void assertDeclares(String resource, String methodName) throws IOException {
        boolean found = methods(resource).contains(methodName);
        if (!found) {
            throw new AssertionError(resource + " must declare " + methodName + "()");
        }
    }

    private static void assertDoesNotDeclare(String resource, String methodName) throws IOException {
        boolean found = methods(resource).contains(methodName);
        if (found) {
            throw new AssertionError(resource + " must not declare " + methodName + "(): "
                    + "the block entity is already removed when affectNeighborsAfterRemoval runs");
        }
    }

    private static java.util.Set<String> methods(String resource) throws IOException {
        InputStream in = FishDisplayLifecycleTest.class.getClassLoader().getResourceAsStream(resource);
        Objects.requireNonNull(in, "missing class resource " + resource);
        ClassModel model;
        try (in) {
            model = ClassFile.of().parse(in.readAllBytes());
        }
        java.util.Set<String> names = new java.util.HashSet<>();
        model.methods().forEach(method -> names.add(method.methodName().stringValue()));
        return names;
    }
}
