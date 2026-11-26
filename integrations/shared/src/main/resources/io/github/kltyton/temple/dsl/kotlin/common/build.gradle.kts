import groovy.json.JsonOutput
import org.gradle.api.artifacts.ProjectDependency
import java.util.Properties

plugins { `java-library` }

val commonRoot = buildFile.canonicalFile.parentFile
val templeRoot = commonRoot.parentFile
val identity = Properties().apply { templeRoot.resolve("gradle.properties").reader(Charsets.UTF_8).use { load(it) } }
val loader = rootProject.property("loader").toString()
val minecraft = rootProject.property("minecraft_version").toString()
val targetId = "$loader-$minecraft"
val activeFile = templeRoot.resolve(".temple/active-target")
val active = (if (activeFile.isFile) activeFile.readText().trim() else identity.getProperty("temple_default_target")) == targetId
val javaVersion = rootProject.property("java_version").toString().toInt()
group = identity.getProperty("mod_group_id")
version = identity.getProperty("mod_version")
base.archivesName.set("${identity.getProperty("mod_id")}-common-$targetId")
layout.buildDirectory.set(commonRoot.resolve("build/$targetId"))
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}
repositories { mavenCentral() }

val layers = listOf(commonRoot.resolve("src/main"), commonRoot.resolve("src/compat/minecraft/$minecraft"),
        commonRoot.resolve("src/compat/$loader"), commonRoot.resolve("src/compat/$loader/$minecraft")).filter { it.isDirectory }
val javaDirectories = layers.map { it.resolve("java") }.filter { it.isDirectory }
val resourceDirectories = layers.asReversed().map { it.resolve("resources") }.filter { it.isDirectory }
val packageName = identity.getProperty("mod_java_package", identity.getProperty("mod_group_id"))
val constants = layout.buildDirectory.get().asFile.resolve("generated/sources/temple")
val constantFile = constants.resolve("${packageName.replace('.', '/')}/BuildInfo.java")
val constantText = "package $packageName;\n\npublic final class BuildInfo {\n" +
        "    public static final String MOD_ID = ${JsonOutput.toJson(identity.getProperty("mod_id"))};\n" +
        "    private BuildInfo() {}\n}\n"
if (!constantFile.isFile || constantFile.readText() != constantText) {
    constantFile.parentFile.mkdirs()
    constantFile.writeText(constantText)
}
sourceSets {
    main {
        java.setSrcDirs(if (active) javaDirectories + constants else emptyList())
        resources.setSrcDirs(if (active) resourceDirectories else emptyList())
    }
    test {
        java.setSrcDirs(if (active) listOf(commonRoot.resolve("src/test/java")) else emptyList())
        resources.setSrcDirs(if (active) listOf(commonRoot.resolve("src/test/resources")) else emptyList())
    }
}
tasks.named<JavaCompile>("compileJava") { source(javaDirectories, constants) }
tasks.named<JavaCompile>("compileTestJava") { source(commonRoot.resolve("src/test/java")) }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8"; options.release.set(javaVersion) }
tasks.named<ProcessResources>("processResources") {
    from(resourceDirectories)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.named<ProcessResources>("processTestResources") { from(commonRoot.resolve("src/test/resources")) }
tasks.named<Jar>("sourcesJar") {
    from(javaDirectories, constants, resourceDirectories)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
val commonProjectPath = path
val commonSourceSets = sourceSets
rootProject.afterEvaluate {
    val sdk = rootProject.configurations.getByName("compileClasspath").copyRecursive(
            org.gradle.api.specs.Spec { dependency -> !(dependency is ProjectDependency && dependency.path == commonProjectPath) })
    commonSourceSets.main.get().compileClasspath += sdk
    commonSourceSets.test.get().compileClasspath += sdk
}
extra["templeResourceDirectories"] = resourceDirectories
extra["templeSourceDirectories"] = javaDirectories + constants
apply(from = templeRoot.resolve("gradle/target-conventions/ide-sources.gradle"))
