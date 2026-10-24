package com.example.B2C;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/** Tiny .env loader so {@link FlywayMigrateNow} can pick up DB_PASSWORD
 *  when spring-dotenv hasn't been bootstrapped. */
final class DotEnvLoader {
    private DotEnvLoader() {}

    /** Read a key from the project-root .env file. Returns null if not present. */
    static String read(String key) {
        Path envPath = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.exists(envPath)) {
            // Walk up the tree to find the nearest .env
            Path parent = envPath;
            for (int i = 0; i < 6 && parent != null; i++) {
                parent = parent.getParent();
                if (parent == null) break;
                Path candidate = parent.resolve(".env");
                if (Files.exists(candidate)) {
                    envPath = candidate;
                    break;
                }
            }
        }
        if (!Files.exists(envPath)) return null;
        try {
            Map<String, String> map = Files.readAllLines(envPath).stream()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#") && l.contains("="))
                    .collect(Collectors.toMap(
                            l -> stripQuotes(l.substring(0, l.indexOf('=')).trim()),
                            l -> stripQuotes(l.substring(l.indexOf('=') + 1).trim()),
                            (a, b) -> b
                    ));
            return map.get(key);
        } catch (IOException e) {
            return null;
        }
    }

    private static String stripQuotes(String s) {
        if (s.length() >= 2
                && (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"'
                || s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'')) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

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
        if (password == null) password = read("DB_PASSWORD");
        if (password == null) { System.err.println("No password"); System.exit(2); }
        String jdbcUrl = url.contains("?") ? url + "&prepareThreshold=0" : url + "?prepareThreshold=0";
        try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, user, password);
             var stmt = conn.createStatement()) {
            String sql = "SELECT u.id, u.email, r.code AS primary_role, "
                    + " (SELECT string_agg(r2.code, ',') FROM user_roles ur JOIN role r2 ON r2.id = ur.role_id "
                    + "  WHERE ur.user_id = u.id) AS join_roles "
                    + "FROM users u LEFT JOIN role r ON r.id = u.role_id "
                    + "WHERE u.email IN ('demo.seller@example.com','demo.buyer@example.com','luu.kimngan205@gmail.com')";
            try (var rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    System.out.printf("  id=%s email=%s primary=%s join=%s%n",
                            rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                }
            }
            // Also list variants and stock
            try (var rs = stmt.executeQuery(
                "SELECT v.id, v.product_id, v.sku, v.stock_quantity FROM product_variant v "
                + "ORDER BY v.id LIMIT 5")) {
                System.out.println("Sample variants:");
                while (rs.next()) {
                    System.out.printf("  variant_id=%s product_id=%s sku=%s stock=%s%n",
                            rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                }
            }
        } catch (Exception e) {
            System.err.println("Query failed: " + e.getMessage());
            System.exit(3);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }
}

