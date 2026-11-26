package io.github.kltyton.temple.ui;

import java.util.Locale;
import java.util.Map;

public final class UiText {
    private static final Map<String, String[]> WORDS = Map.ofEntries(
            Map.entry("sourceLayoutTitle", new String[]{"源码目录需要处理", "Source layout needs attention"}),
            Map.entry("sourceModelMissing", new String[]{"缺少目标源码模型", "Missing target source model"}),
            Map.entry("sourceModelStale", new String[]{"目标源码模型已过期", "Stale target source model"}),
            Map.entry("sourceModelInvalid", new String[]{"目标源码模型无效", "Invalid target source model"}),
            Map.entry("sourceSelectionMissing", new String[]{"编辑目标不存在", "Editing target does not exist"}),
            Map.entry("sourceModuleMissing", new String[]{"目标 common 模块尚未导入", "Target common module has not been imported"}),
            Map.entry("sourceUnassigned", new String[]{"没有目标使用此源码或资源路径", "No target uses this source or resource path"}),
            Map.entry("sourceDuplicate", new String[]{"同一目标包含重复 Java 路径", "Duplicate Java path in one target"}),
            Map.entry("sourceReadFailed", new String[]{"无法读取源码模型，详情见 IDEA 日志。", "Could not read source models. See the IDEA log for details."}),
            Map.entry("sourceAssignmentFailed", new String[]{"无法更新源码归属，详情见 IDEA 日志。", "Could not update source ownership. See the IDEA log for details."}),
            Map.entry("sourceMore", new String[]{"问题总数，完整清单见 verifySourceLayout 输出", "Total issues; see verifySourceLayout for the complete list"}),
            Map.entry("sourceLayoutHint", new String[]{"模型缺失或过期时同步所有 Gradle 项目；无归属目录须在 common 构建中接入对应目标。不要把不同 API 的适配文件全部标到一个 SDK 下。规则见 docs/dev/source-layout.md，完整清单可运行 verifySourceLayout。", "Sync all Gradle projects for missing or stale models. Assign unowned paths to matching targets in the common build. Keep incompatible API variants on their own SDKs. See docs/dev/source-layout.en.md; run verifySourceLayout for the complete list."}),
            Map.entry("language", new String[]{"界面语言", "Language"}),
            Map.entry("buildDsl", new String[]{"Gradle 脚本语言", "Gradle script language"}),
            Map.entry("defaultTarget", new String[]{"默认编辑目标", "Default editing target"}),
            Map.entry("defaultTargetHint", new String[]{"首次导入时 common 使用的 Minecraft / 加载器环境，也是根任务的默认目标。", "Minecraft / loader environment for common on first import, and the default for root tasks."}),
            Map.entry("minecraft", new String[]{"Minecraft 版本", "Minecraft version"}),
            Map.entry("loader", new String[]{"加载器", "Loader"}),
            Map.entry("loaderVersion", new String[]{"加载器版本", "Loader version"}),
            Map.entry("fabricApi", new String[]{"Fabric API 版本", "Fabric API version"}),
            Map.entry("toolchain", new String[]{"工具链", "Toolchain"}),
            Map.entry("preview", new String[]{"显示预发布版本", "Include preview versions"}),
            Map.entry("add", new String[]{"添加目标", "Add target"}),
            Map.entry("refresh", new String[]{"刷新版本目录", "Refresh versions"}),
            Map.entry("remove", new String[]{"移除所选目标", "Remove selected"}),
            Map.entry("loading", new String[]{"正在读取官方版本目录…", "Loading official version catalogs…"}),
            Map.entry("loadingJava", new String[]{"正在读取 Minecraft 的 Java 要求…", "Loading Minecraft Java requirement…"}),
            Map.entry("loadingVersions", new String[]{"正在读取兼容版本…", "Loading compatible versions…"}),
            Map.entry("downloadFailed", new String[]{"版本目录读取失败，请刷新重试。", "Version download failed. Refresh to retry."}),
            Map.entry("javaFailed", new String[]{"无法读取 Minecraft 的 Java 要求", "Could not read Minecraft Java requirement"}),
            Map.entry("duplicate", new String[]{"列表中已经有这个目标。", "This target is already in the list."}),
            Map.entry("selectTarget", new String[]{"至少添加一个目标", "Select at least one target"}),
            Map.entry("notReady", new String[]{"请等待兼容版本和工具链读取完成。", "Wait for compatible versions and toolchain metadata."}),
            Map.entry("recommended", new String[]{"推荐", "Recommended"}),
            Map.entry("releases", new String[]{"个 Minecraft 正式版本", "Minecraft releases"}),
            Map.entry("selected", new String[]{"个目标已添加", "targets selected"}),
            Map.entry("createTitle", new String[]{"KltytonTemple · 创建工程", "KltytonTemple · Create project"}),
            Map.entry("addTitle", new String[]{"KltytonTemple · 新增目标", "KltytonTemple · Add targets"}),
            Map.entry("confirm", new String[]{"确定", "Confirm"}),
            Map.entry("cancel", new String[]{"取消", "Cancel"}),
            Map.entry("name", new String[]{"Mod 名称", "Mod name"}),
            Map.entry("package", new String[]{"Java 包名", "Java package"}),
            Map.entry("authors", new String[]{"作者", "Authors"}),
            Map.entry("version", new String[]{"项目版本", "Project version"}),
            Map.entry("license", new String[]{"许可证", "License"}),
            Map.entry("description", new String[]{"描述", "Description"}),
            Map.entry("singleLineDescription", new String[]{"描述不能换行。", "Description must be a single line."}),
            Map.entry("requiredField", new String[]{"此项不能为空。", "This field is required."}),
            Map.entry("singleLineField", new String[]{"此项不能换行。", "Use a single line for this field."}),
            Map.entry("invalidModId", new String[]{"Mod ID 格式不正确。", "Invalid Mod ID."}),
            Map.entry("invalidNamespace", new String[]{"组命名空间格式不正确。", "Invalid group namespace."}),
            Map.entry("invalidPackage", new String[]{"Java 包名不合法，不能包含保留关键字。", "Invalid Java package; reserved keywords are not allowed."}),
            Map.entry("directory", new String[]{"新工程目录", "New project directory"}),
            Map.entry("browse", new String[]{"浏览…", "Browse…"}),
            Map.entry("added", new String[]{"目标已添加。新版本的游戏代码仍需按对应 API 适配。", "Targets added. Adapt game code to the selected version APIs."}),
            Map.entry("choices", new String[]{"可选版本", "available versions"}),
            Map.entry("errorDetail", new String[]{"查看错误详情", "Show error details"}),
            Map.entry("chooseDirectory", new String[]{"请选择新工程目录", "Choose a new project directory"}),
            Map.entry("createFailed", new String[]{"无法创建工程", "Could not create project"}),
            Map.entry("addFailed", new String[]{"无法添加目标", "Could not add targets"}),
            Map.entry("created", new String[]{"工程已创建，可在 IDEA 中打开：", "Project created. Open it in IDEA:"})
    );
    private final boolean chinese;

    public UiText(Locale locale) {
        chinese = "zh".equals(locale.getLanguage());
    }

    public static UiText system() {
        return new UiText(Locale.getDefault());
    }

    public String get(String key) {
        String[] values = WORDS.get(key);
        if (values == null) throw new IllegalArgumentException("Unknown UI text: " + key);
        return values[chinese ? 0 : 1];
    }
}
