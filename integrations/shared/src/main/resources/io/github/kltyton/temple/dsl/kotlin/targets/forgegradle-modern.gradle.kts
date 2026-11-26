import net.minecraftforge.gradle.ForgeGradleExtensionForProject
import net.minecraftforge.gradle.MinecraftExtensionForProject

plugins {
    `java-library`
    id("net.minecraftforge.gradle")
    id("me.modmuss50.mod-publish-plugin")
}
apply(from = file("../../gradle/target-conventions/base.gradle.kts"))
val minecraft_version: String by project
val loader_version: String by project
val mod_id: String by project
val minecraft = extensions.getByType<MinecraftExtensionForProject>()
val fg = extensions.getByType<ForgeGradleExtensionForProject>()
minecraft.apply {
    mappings("official", minecraft_version)
    runs.configureEach {
        workingDir.set(layout.projectDirectory.dir("run"))
        mods.register(mod_id) {
            source(sourceSets.main.get())
            source(project(":common").extensions.getByType<SourceSetContainer>().named("main").get())
        }
    }
    runs.register("client")
    runs.register("server") { args("--nogui") }
    runs.register("data") {
        workingDir.set(layout.projectDirectory.dir("run-data"))
        args("--mod", mod_id, "--all", "--output", file("src/generated/resources").absolutePath,
                *(project.extra["templeDatagenExistingArgs"] as Array<String>))
    }
}
val mavenizerRepository = repositories.maven(minecraft.mavenizer)
repositories.remove(mavenizerRepository)
repositories.addFirst(mavenizerRepository)
repositories.maven(fg.forgeMaven)
repositories.maven(fg.minecraftLibsMaven)
dependencies { addProvider("implementation", minecraft.dependency("net.minecraftforge:forge:$minecraft_version-$loader_version").asProvider()) }
apply(from = file("../../gradle/target-conventions/pack-metadata.gradle.kts"))
apply(from = file("../../gradle/target-conventions/publish.gradle.kts"))
