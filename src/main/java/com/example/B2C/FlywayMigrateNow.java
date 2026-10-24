package com.example.B2C;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

/**
 * One-shot helper that connects to Supabase using credentials from .env (loaded
 * by spring-dotenv only when the Spring app starts) or process env, and runs
 * Flyway migrate. Used to apply migration V20 while the Spring Boot app is
 * still running on the same port — we don't restart the app, we just push the
 * new migration through the Flyway API directly.
 *
 * Usage: ./gradlew flywayMigrateNow
 */
public final class FlywayMigrateNow {
    private FlywayMigrateNow() {}

    public static void main(String[] args) {
        String url = firstNonBlank(
                System.getenv("SUPABASE_DB_URL"),
                "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres"
        );
        String user = firstNonBlank(
                System.getenv("SUPABASE_DB_USER"),
                "postgres.ipkqyubcjvpuomnxlcuy"
        );
        String password = firstNonBlank(
                System.getenv("DB_PASSWORD"),
                System.getenv("SUPABASE_DB_PASSWORD")
        );
        if (password == null) {
            // Fall back to reading .env manually (spring-dotenv isn't active here)
            password = DotEnvLoader.read("DB_PASSWORD");
        }
        if (password == null) {
            System.err.println("ERROR: DB_PASSWORD not set (env or .env)");
            System.exit(2);
        }

        // Add prepareThreshold=0 to skip the first round-trip — same as app config
        String jdbcUrl = url.contains("?")
                ? url + "&prepareThreshold=0"
                : url + "?prepareThreshold=0";

        System.out.println("[flywayMigrateNow] url=" + jdbcUrl);
        System.out.println("[flywayMigrateNow] user=" + user);

        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        long t0 = System.currentTimeMillis();
        // Some prior migrations had their files edited after apply, so the
        // checksum in the schema history is now stale. Repair rebuilds it.
        try {
            flyway.repair();
            System.out.println("[flywayMigrateNow] repair completed");
        } catch (Exception e) {
            System.out.println("[flywayMigrateNow] repair failed (continuing): " + e.getMessage());
        }
        // Verify counts
        try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, user, password)) {
            String[] q = {
                "SELECT COUNT(*) AS products FROM product",
                "SELECT COUNT(*) AS variants FROM product_variant",
                "SELECT COUNT(*) AS images FROM product_image",
                "SELECT category_id, COUNT(*) AS n FROM product GROUP BY category_id ORDER BY category_id"
            };
            for (String sql : q) {
                try (var stmt = conn.createStatement();
                     var rs = stmt.executeQuery(sql)) {
                    var meta = rs.getMetaData();
                    StringBuilder sb = new StringBuilder("[flywayMigrateNow] ");
                    while (rs.next()) {
                        sb.append(rs.getObject(1));
                        if (meta.getColumnCount() > 1) {
                            sb.append("=").append(rs.getObject(2));
                        }
                        sb.append("  ");
                    }
                    System.out.println(sb);
                }
            }
        } catch (Exception e) {
            System.out.println("[flywayMigrateNow] count check failed: " + e.getMessage());
        }
        MigrateResult result = flyway.migrate();
        long ms = System.currentTimeMillis() - t0;

        System.out.println("[flywayMigrateNow] success=" + result.success
                + " migrationsExecuted=" + result.migrationsExecuted
                + " target=" + result.targetSchemaVersion
                + " elapsedMs=" + ms);
        if (result.migrationsExecuted > 0) {
            result.migrations.forEach(m ->
                System.out.println("  - " + m.version + " " + m.description
                        + " (" + m.executionTime + "ms)"));
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
