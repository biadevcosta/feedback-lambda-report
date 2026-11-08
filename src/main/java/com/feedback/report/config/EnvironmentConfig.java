package com.feedback.report.config;

/** Environment variables set by Terraform; the only place that reads them. */
public record EnvironmentConfig(String tableName, String indexName, String userPoolId, String adminGroup,
        String sesSender, int days) {

    public static EnvironmentConfig fromEnvironment() {
        return new EnvironmentConfig(
                required("TABLE_NAME"),
                required("INDEX_NAME"),
                required("USER_POOL_ID"),
                required("ADMIN_GROUP"),
                required("SES_SENDER"),
                Integer.parseInt(required("DAYS")));
    }

    // Fails at init instead of on the first run when the function is misconfigured
    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable " + name);
        }
        return value;
    }
}
