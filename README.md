# Stardew Fishing Fabric

Fabric port of Stardew Fishing for Minecraft 1.20.1, 1.21.1, 1.21.11, 26.1.2 and 26.2.

## Building

The root Gradle project uses [KltytonTemple](https://github.com/kltyton/KltytonTemple) to select and build independent targets. Install JDK 25 for Gradle and the toolchain required by your target (17 for 1.20.1, 21 for 1.21.1 and 1.21.11, and 25 for 26.x).

- Run `./gradlew listTargets` to see the targets or `./gradlew buildAllTargets` to build all five.
- Run `./gradlew build_fabric_1_21_11` to build one target.
- Run `./gradlew select_fabric_1_21_11` to select the shared sources shown by the IDE.
- Find the JARs under each target's `build/libs` directory.

Shared gameplay, assets and data live in `common/src/main`. Minecraft API and resource format differences live in `common/src/compat/minecraft`. Fabric metadata, access wideners and target tests live under `targets/fabric-*`.

## 许可与署名

原模组作者为 [bonker](https://modrinth.com/user/bonker)。项目按 [MIT License](LICENSE) 发布。
