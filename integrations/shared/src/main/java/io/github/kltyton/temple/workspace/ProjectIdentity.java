package io.github.kltyton.temple.workspace;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;

public final class ProjectIdentity {
    private static final Pattern MOD_ID = Pattern.compile("[a-z][a-z0-9_]{1,63}");
    private static final Pattern NAMESPACE = Pattern.compile("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+");
    private static final List<String> TEXT_FIELDS = List.of("mod_name", "mod_authors", "mod_version",
            "mod_license", "mod_description");

    public record Problem(String property, String messageKey) {}

    private ProjectIdentity() {}

    public static Problem problem(Properties values) {
        for (String key : List.of("mod_id", "mod_group_id")) {
            Problem problem = problem(key, values.getProperty(key));
            if (problem != null) return problem;
        }
        String javaPackage = values.getProperty("mod_java_package", values.getProperty("mod_group_id"));
        Problem packageProblem = problem("mod_java_package", javaPackage);
        if (packageProblem != null) return packageProblem;
        for (String key : TEXT_FIELDS) {
            Problem problem = problem(key, values.getProperty(key));
            if (problem != null) return problem;
        }
        return null;
    }

    public static Problem problem(String property, String value) {
        if (value == null || value.isBlank()) return new Problem(property, "requiredField");
        if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)
            return new Problem(property, "singleLineField");
        if ("mod_id".equals(property) && !MOD_ID.matcher(value).matches())
            return new Problem(property, "invalidModId");
        if ("mod_group_id".equals(property) && !NAMESPACE.matcher(value).matches())
            return new Problem(property, "invalidNamespace");
        if ("mod_java_package".equals(property)
                && (!NAMESPACE.matcher(value).matches() || !SourceVersion.isName(value, SourceVersion.RELEASE_17)))
            return new Problem(property, "invalidPackage");
        return null;
    }

    public static void validate(Properties values) {
        Problem problem = problem(values);
        if (problem != null)
            throw new IllegalArgumentException(problem.property() + ": " + switch (problem.messageKey()) {
                case "requiredField" -> "value is required";
                case "singleLineField" -> "value must be a single line";
                case "invalidModId" -> "invalid Mod ID";
                case "invalidNamespace" -> "invalid group namespace";
                case "invalidPackage" -> "invalid Java package";
                default -> throw new IllegalStateException("Unknown identity validation");
            });
    }
}
