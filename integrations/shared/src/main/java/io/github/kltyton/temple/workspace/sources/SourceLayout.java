package io.github.kltyton.temple.workspace.sources;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Reconciles Gradle's exported source roots with the current workspace. */
public final class SourceLayout {
    public record Descriptor(int schema, String target, String selection, String targetDirectory,
                             String projectDirectory, List<String> javaRoots, List<String> resourceRoots,
                             Map<String, String> inputs, List<String> inventory) {}
    public record SourceRoot(Path path, boolean resource) {}
    public record Target(String id, Path targetDirectory, Path projectDirectory, List<SourceRoot> roots) {}
    public record Problem(String code, String detail) {}
    public record Snapshot(Path common, String selection, List<Target> targets,
                           List<Problem> problems, List<String> unassigned) {
        public boolean modelsComplete() { return problems.isEmpty(); }
    }
    @FunctionalInterface public interface ModelReader { Descriptor read(Path path) throws IOException; }

    private SourceLayout() {}

    public static Snapshot read(Path directory, ModelReader reader) throws IOException {
        Path root = directory.toRealPath();
        Properties identity = new Properties();
        try (Reader input = Files.newBufferedReader(root.resolve("gradle.properties"))) { identity.load(input); }
        if (!"true".equals(identity.getProperty("temple_idea_project"))) return null;
        Path common = root.resolve("common").toRealPath();
        Path choice = root.resolve(".temple/active-target");
        String selection = Files.isRegularFile(choice) ? Files.readString(choice).trim() : identity.getProperty("temple_default_target");
        List<String> inventory = inventory(root);
        List<Target> targets = new ArrayList<>();
        List<Problem> problems = new ArrayList<>();
        boolean selected = false;
        try (var paths = Files.list(root.resolve("targets"))) {
            for (Path target : paths.filter(Files::isDirectory).sorted().toList()) {
                if (!Files.isRegularFile(target.resolve("gradle.properties"))) continue;
                String id = target.getFileName().toString();
                selected |= id.equals(selection);
                Path file = common.resolve(".gradle/contexts").resolve(id).resolve("ide-sources.json");
                if (!Files.isRegularFile(file)) {
                    problems.add(new Problem("sourceModelMissing", id));
                    continue;
                }
                try {
                    Descriptor model = reader.read(file);
                    if (model == null || model.schema() < 1 || model.schema() > 2 || !id.equals(model.target())
                            || model.targetDirectory() == null || model.projectDirectory() == null
                            || model.javaRoots() == null || model.resourceRoots() == null)
                        throw new IllegalArgumentException("Invalid source model");
                    if (model.schema() == 1 || !Objects.equals(selection, model.selection())
                            || !inputs(root, target).equals(model.inputs()) || !inventory.equals(model.inventory())) {
                        problems.add(new Problem("sourceModelStale", id));
                        continue;
                    }
                    Path targetDirectory = target.toRealPath();
                    Path expected = id.equals(selection) ? common : common.resolve(".gradle/contexts").resolve(id);
                    if (!Path.of(model.targetDirectory()).toRealPath().equals(targetDirectory)
                            || !Path.of(model.projectDirectory()).toRealPath().equals(expected.toRealPath()))
                        throw new IllegalArgumentException("Source model belongs to a different project");
                    List<SourceRoot> roots = new ArrayList<>();
                    addRoots(common, model.javaRoots(), false, roots);
                    addRoots(common, model.resourceRoots(), true, roots);
                    Set<Path> classes = new HashSet<>();
                    for (SourceRoot source : roots) {
                        if (source.resource()) continue;
                        try (var javaFiles = Files.walk(source.path())) {
                            for (Path javaFile : javaFiles.filter(Files::isRegularFile)
                                    .filter(path -> path.toString().endsWith(".java")).toList()) {
                                Path name = source.path().relativize(javaFile);
                                if (!classes.add(name)) problems.add(new Problem("sourceDuplicate", id + ": " + name));
                            }
                        }
                    }
                    targets.add(new Target(id, targetDirectory, expected, List.copyOf(roots)));
                } catch (IOException | IllegalArgumentException error) {
                    problems.add(new Problem("sourceModelInvalid", id + ": " + error.getMessage()));
                }
            }
        }
        if (!selected) problems.add(new Problem("sourceSelectionMissing", String.valueOf(selection)));
        targets.sort(Comparator.comparing((Target value) -> !value.id().equals(selection)).thenComparing(Target::id));
        Set<String> unassigned = new TreeSet<>();
        if (problems.stream().allMatch(problem -> problem.code().equals("sourceDuplicate"))) {
            List<SourceRoot> declared = targets.stream().flatMap(target -> target.roots().stream()).toList();
            Path src = common.resolve("src");
            if (Files.isDirectory(src)) {
                try (var files = Files.walk(src)) {
                    for (Path file : files.filter(Files::isRegularFile)
                            .filter(path -> !path.startsWith(src.resolve("test"))).toList()) {
                        boolean java = file.getFileName().toString().endsWith(".java");
                        if (declared.stream().anyMatch(source -> file.startsWith(source.path()) && (source.resource() || java))) continue;
                        String kind = java ? "J:" : "R:";
                        String path = inventory.stream().filter(entry -> entry.startsWith(kind)).map(entry -> entry.substring(2))
                                .filter(entry -> file.startsWith(root.resolve(entry)))
                                .max(Comparator.comparingInt(String::length)).orElse(relative(root, file));
                        unassigned.add(path);
                    }
                }
            }
        }
        return new Snapshot(common, selection, List.copyOf(targets), List.copyOf(problems), List.copyOf(unassigned));
    }

    private static void addRoots(Path common, List<String> paths, boolean resource, List<SourceRoot> roots) throws IOException {
        for (String value : paths) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Empty source root");
            Path path = Path.of(value).toRealPath();
            if (!Files.isDirectory(path) || (!path.startsWith(common.resolve("src")) && !path.startsWith(common.resolve("build"))))
                throw new IllegalArgumentException("Source root is outside common sources and build outputs: " + value);
            for (SourceRoot previous : roots)
                if (path.startsWith(previous.path()) || previous.path().startsWith(path))
                    throw new IllegalArgumentException("Overlapping source roots: " + value);
            roots.add(new SourceRoot(path, resource));
        }
    }

    /** Kept in step with the standalone Gradle exporter, which has no dependency on the root build. */
    public static Map<String, String> inputs(Path root, Path target) throws IOException {
        List<Path> files = new ArrayList<>();
        for (Path base : List.of(root, root.resolve("common"), target))
            for (String name : List.of("gradle.properties", "settings.gradle", "settings.gradle.kts", "build.gradle", "build.gradle.kts")) {
                Path file = base.resolve(name);
                if (Files.isRegularFile(file)) files.add(file);
            }
        Path conventions = root.resolve("gradle/target-conventions");
        if (Files.isDirectory(conventions)) {
            try (var paths = Files.walk(conventions)) {
                files.addAll(paths.filter(Files::isRegularFile).filter(SourceLayout::gradleScript).toList());
            }
        }
        Map<String, String> result = new TreeMap<>();
        for (Path file : files) result.put(relative(root, file), digest(Files.readAllBytes(file)));
        return result;
    }

    private static boolean gradleScript(Path file) {
        String name = file.getFileName().toString();
        return name.endsWith(".gradle") || name.endsWith(".gradle.kts");
    }

    public static List<String> inventory(Path root) throws IOException {
        Path src = root.resolve("common/src");
        if (!Files.isDirectory(src)) return List.of();
        Set<String> result = new TreeSet<>();
        try (var paths = Files.walk(src)) {
            for (Path file : paths.filter(Files::isRegularFile).filter(path -> !path.startsWith(src.resolve("test"))).toList()) {
                Path boundary = file.getParent();
                while (boundary != null && !boundary.equals(src)
                        && !Set.of("java", "resources").contains(boundary.getFileName().toString())) boundary = boundary.getParent();
                if (boundary != null && !boundary.equals(src)) {
                    boolean resource = boundary.getFileName().toString().equals("resources");
                    if (resource || file.toString().endsWith(".java")) result.add((resource ? "R:" : "J:") + relative(root, boundary));
                } else if (file.toString().endsWith(".java")) result.add("J:" + relative(root, file));
            }
        }
        return List.copyOf(result);
    }

    private static String relative(Path root, Path file) { return root.relativize(file).toString().replace('\\', '/'); }

    private static String digest(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException("SHA-256 is required by Java", error); }
    }
}
