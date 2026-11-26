package io.github.kltyton.temple.workspace.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Keeps inactive Loader sources available without registering them as production code. */
public final class SourceBlueprints {
    private static final List<String> LOADERS = List.of("fabric", "forge", "neoforge");
    private static final String SUFFIX = ".template";

    private SourceBlueprints() {}

    public static void selectLoaders(Path root, Collection<String> selected) throws IOException {
        if (!LOADERS.containsAll(selected)) throw new IllegalArgumentException("Unknown Loader selection");
        Set<String> active = Set.copyOf(selected);
        for (String loader : LOADERS) {
            if (active.contains(loader)) activate(root, loader);
            else transfer(root.resolve("common/src/compat").resolve(loader), blueprint(root, loader), true);
        }
    }

    public static void activate(Path root, String loader) throws IOException {
        if (!LOADERS.contains(loader)) throw new IllegalArgumentException("Unknown Loader: " + loader);
        transfer(blueprint(root, loader), root.resolve("common/src/compat").resolve(loader), false);
    }

    private static Path blueprint(Path root, String loader) {
        return root.resolve("gradle/target-blueprints/common").resolve(loader);
    }

    private static void transfer(Path from, Path to, boolean staging) throws IOException {
        if (Files.notExists(from)) return;
        if (!Files.isDirectory(from)) throw new IOException("Loader source blueprint must be a directory: " + from);
        if (Files.isSymbolicLink(from)) throw new IOException("Source blueprint contains a symbolic link: " + from);
        List<Path> contents;
        try (var paths = Files.walk(from)) { contents = paths.toList(); }
        for (Path path : contents) {
            if (Files.isSymbolicLink(path)) throw new IOException("Source blueprint contains a symbolic link: " + path);
            if (!Files.isRegularFile(path)) continue;
            Path destination = destination(from, to, path, staging);
            if (Files.exists(destination) && Files.mismatch(path, destination) != -1)
                throw new IOException("Loader source already exists with different contents: " + destination);
        }
        for (Path path : contents) {
            if (!Files.isRegularFile(path)) continue;
            Path destination = destination(from, to, path, staging);
            Files.createDirectories(destination.getParent());
            if (Files.exists(destination)) Files.delete(path);
            else Files.move(path, destination);
        }
        for (Path path : contents.stream().sorted(Comparator.reverseOrder()).toList()) {
            if (!Files.isDirectory(path)) continue;
            boolean empty;
            try (var children = Files.list(path)) {
                empty = children.findAny().isEmpty();
            }
            if (empty) Files.delete(path);
        }
    }

    private static Path destination(Path from, Path to, Path source, boolean staging) throws IOException {
        Path relative = from.relativize(source);
        String name = relative.getFileName().toString();
        if (!staging && !name.endsWith(SUFFIX)) throw new IOException("Invalid Loader source blueprint: " + source);
        String output = staging ? name + SUFFIX : name.substring(0, name.length() - SUFFIX.length());
        return to.resolve(relative).resolveSibling(output);
    }
}
