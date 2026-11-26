package io.github.kltyton.temple.artifact

import groovy.json.JsonSlurper
import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.toml.TomlParser
import io.github.kltyton.temple.workspace.ProjectFiles
import io.github.kltyton.temple.workspace.TargetDefinition
import java.security.MessageDigest
import java.util.zip.ZipFile
import java.util.jar.Manifest

final class DistributionInspector {
    static Map<String, Object> verify(File root, TargetDefinition target) {
        Properties identity = ProjectFiles.read(new File(root, 'gradle.properties'))
        File record = new File(target.directory, 'build/temple/artifact.properties')
        File artifact = new File(ProjectFiles.read(record).getProperty('artifact')).canonicalFile
        if (!artifact.toPath().startsWith(new File(target.directory, 'build').canonicalFile.toPath())) {
            throw new IllegalArgumentException('Artifact is outside target build output')
        }
        String metadata = target.loader == 'fabric' ? 'fabric.mod.json' :
                (target.loader == 'forge' ? 'META-INF/mods.toml' : 'META-INF/neoforge.mods.toml')
        String packageName = identity.getProperty('mod_java_package', identity.getProperty('mod_group_id'))
        String entry = identity.getProperty('mod_entry_class', packageName + '.TempleCommon').replace('.', '/') + '.class'
        int count = 0
        new ZipFile(artifact).withCloseable { zip ->
            List<String> names = zip.entries().collect { it.name }
            if (names.size() != names.toSet().size()) { throw new IllegalArgumentException('Duplicate ZIP entries') }
            [metadata, packageName.replace('.', '/') + '/BuildInfo.class', entry].each { name ->
                if (!(name in names)) { throw new IllegalArgumentException("Missing ${name}: ${artifact}") }
            }
            String text = zip.getInputStream(zip.getEntry(metadata)).getText('UTF-8')
            String modId = identity.getProperty('mod_id')
            if (target.loader == 'fabric') {
                def value = new JsonSlurper().parseText(text)
                if (value.id != modId || value.version != identity.getProperty('mod_version') || value.depends.minecraft != '='+target.minecraft &&
                        value.depends.minecraft != target.properties.getProperty('minecraft_version_range')) {
                    throw new IllegalArgumentException('Incorrect Fabric identity or Minecraft version')
                }
                value.entrypoints.values().flatten().each { item ->
                    String name = item instanceof Map ? item.value : item.toString()
                    String adapter = item instanceof Map ? item.get('adapter', 'default') : 'default'
                    if (adapter == 'default' && !(name.split('::', 2)[0].replace('.', '/') + '.class' in names)) {
                        throw new IllegalArgumentException("Missing Fabric entrypoint: ${name}")
                    }
                }
            } else {
                def value = new TomlParser().parse(text)
                List<UnmodifiableConfig> mods = value.get('mods')
                def mod = mods?.find { it.get('modId') == modId }
                if (mod == null) throw new IllegalArgumentException('Incorrect mod identity')
                String version = mod.get('version')
                Map substitutions = [:]
                UnmodifiableConfig properties = value.get('properties')
                if (properties != null) substitutions.putAll(properties.valueMap())
                def manifest = zip.getEntry('META-INF/MANIFEST.MF')
                if (manifest != null) zip.getInputStream(manifest).withCloseable {
                    substitutions.jarVersion = new Manifest(it).mainAttributes.getValue('Implementation-Version')
                }
                version = version.replaceAll(/\$\{file\.([^}]+)\}/) { match, key ->
                    if (substitutions[key] == null) throw new IllegalArgumentException("Unresolved version property: ${match}")
                    substitutions[key].toString()
                }
                if (version != identity.getProperty('mod_version')) throw new IllegalArgumentException('Incorrect mod version')
                List<UnmodifiableConfig> dependencies = value.get(['dependencies', modId])
                def minecraft = dependencies?.find { it.get('modId') == 'minecraft' }
                if (minecraft == null || minecraft.get('versionRange') != target.properties.getProperty('minecraft_version_range')) {
                    throw new IllegalArgumentException('Incorrect Minecraft range')
                }
                if (target.loader == 'forge') {
                    def packEntry = zip.getEntry('pack.mcmeta')
                    if (packEntry == null) throw new IllegalArgumentException('Missing pack.mcmeta')
                    def pack = zip.getInputStream(packEntry).withCloseable { new JsonSlurper().parse(it, 'UTF-8') }.pack
                    if (!(pack instanceof Map) || pack.description == null ||
                            !(pack.pack_format instanceof Number || pack.min_format != null && pack.max_format != null))
                        throw new IllegalArgumentException('Invalid pack.mcmeta')
                }
            }
            names.each { name ->
                if (name.startsWith(packageName.replace('.', '/') + '/') && name.endsWith('.class')) {
                    byte[] code = zip.getInputStream(zip.getEntry(name)).bytes
                    int major = ((code[6] & 0xff) << 8) | (code[7] & 0xff)
                    if (major > target.javaVersion + 44) { throw new IllegalArgumentException("Wrong bytecode: ${name}") }
                    count++
                }
                if (name.startsWith('.omo/') || name.startsWith('.codex/') || name.startsWith('buildSrc/') ||
                        name.endsWith('/SKILL.md') || name in ['AGENT.md', 'AGENTS.md', 'CLAUDE.md', 'GEMINI.md']) {
                    throw new IllegalArgumentException("Development file packaged: ${name}")
                }
            }
        }
        MessageDigest digest = MessageDigest.getInstance('SHA-256')
        artifact.withInputStream { stream ->
            byte[] buffer = new byte[8192]
            int length
            while ((length = stream.read(buffer)) != -1) { digest.update(buffer, 0, length) }
        }
        [target: target.id, artifact: artifact.absolutePath, sha256: digest.digest().encodeHex().toString(), classes: count]
    }
}
