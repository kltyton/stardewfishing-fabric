import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.util.zip.ZipFile

val commonProject = project(":common")
@Suppress("UNCHECKED_CAST")
val commonResources = commonProject.extra["templeResourceDirectories"] as List<File>
val manualMetadata = (listOf(file("src/main/resources"), file("src/generated/resources")) + commonResources)
        .map { it.resolve("pack.mcmeta") }
val sdkResources = if (project.extra.has("templeMinecraftResources")) files(project.extra["templeMinecraftResources"])
        else configurations.getByName("compileClasspath").incoming.artifactView {
            componentFilter { id -> id is ModuleComponentIdentifier &&
                    (id.group == "net.minecraft" || id.group == "net.minecraftforge" && id.module == "forge") }
        }.files
val selectedResources = providers.provider { if (manualMetadata.any { it.isFile }) files() else sdkResources }
val metadataDirectory = layout.buildDirectory.dir("generated/temple/pack-metadata")
val minecraftVersion = project.property("minecraft_version").toString()
val modName = project.property("mod_name").toString()
val generatePackMetadata = tasks.register("generatePackMetadata") {
    inputs.files(manualMetadata).withPropertyName("manualPackMetadata")
    inputs.files(selectedResources).withPropertyName("minecraftSdk")
    inputs.properties(mapOf("minecraft_version" to minecraftVersion, "mod_name" to modName))
    outputs.dir(metadataDirectory)
    doLast {
        val output = metadataDirectory.get().file("pack.mcmeta").asFile
        if (manualMetadata.any { it.isFile }) {
            java.nio.file.Files.deleteIfExists(output.toPath())
        } else {
            val formats = selectedResources.get().filter { it.name.endsWith(".jar") }.mapNotNull { jar ->
                ZipFile(jar).use { zip ->
                    zip.getEntry("version.json")?.let { entry ->
                        val version = zip.getInputStream(entry).use { JsonSlurper().parse(it, "UTF-8") } as Map<*, *>
                        if (version["id"] == minecraftVersion) version["pack_version"] as Map<*, *> else null
                    }
                }
            }.distinct()
            require(formats.size == 1) { "Expected one Minecraft $minecraftVersion pack-version definition in the resolved SDK, found ${formats.size}" }
            val format = formats.single()
            val pack = linkedMapOf<String, Any>("description" to modName)
            if (format["resource"] is Number && format["data"] is Number) {
                pack["pack_format"] = format["data"]!!
                pack["forge:client_resources_pack_format"] = format["resource"]!!
                pack["forge:server_data_pack_format"] = format["data"]!!
            } else if (format["data_major"] is Number && format["data_minor"] is Number) {
                pack["min_format"] = listOf(format["data_major"], format["data_minor"])
                pack["max_format"] = listOf(format["data_major"], format["data_minor"])
            } else error("Unsupported Minecraft pack-version definition: $format")
            output.parentFile.mkdirs()
            output.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("pack" to pack))) + "\n", Charsets.UTF_8)
        }
    }
}
tasks.named<ProcessResources>("processResources") { from(generatePackMetadata) }
