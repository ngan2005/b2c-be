package com.example.B2C;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class SupabaseSmokeTest {
    public static void main(String[] args) throws Exception {
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("DB_PASSWORD env not set");
        }
        // Try direct connection first (may fail on IPv6-only host)
        String[] urls = {
            // Pooler - Tokyo region (matches project's ap-northeast-1 location)
            "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres",
            "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres",
            // Pooler - Singapore (kept as fallback)
            "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres",
            // Direct
            "jdbc:postgresql://db.ipkqyubcjvpuomnxlcuy.supabase.co:5432/postgres"
        };
        String[] users = {
            "postgres.ipkqyubcjvpuomnxlcuy",
            "postgres"
        };

        for (String url : urls) {
            for (String user : users) {
                System.out.println("\n=== Trying " + url + " as " + user + " ===");
                Properties props = new Properties();
                props.setProperty("user", user);
                props.setProperty("password", password);
                props.setProperty("loginTimeout", "10");
                props.setProperty("connectTimeout", "10");
                props.setProperty("ApplicationName", "B2C-smoke-test");
                try (Connection conn = DriverManager.getConnection(url, props)) {
                    System.out.println("CONNECTED!");
                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT current_database(), current_user, version()")) {
                        if (rs.next()) {
                            System.out.println("  database = " + rs.getString(1));
                            System.out.println("  user     = " + rs.getString(2));
                            System.out.println("  version  = " + rs.getString(3));
                        }
                    }
                    // List existing tables
                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery(
                             "SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename")) {
                        int n = 0;
                        while (rs.next()) {
                            System.out.println("  table: " + rs.getString(1));
                            n++;
                        }
                        System.out.println("  (" + n + " tables)");
                    }
                    // Try insert test row into a safe temp table
                    String testTable = "_b2c_smoke_test";
                    try (Statement st = conn.createStatement()) {
                        st.execute("CREATE TABLE IF NOT EXISTS " + testTable + " (id serial PRIMARY KEY, note text, created_at timestamptz DEFAULT now())");
                        st.execute("INSERT INTO " + testTable + " (note) VALUES ('hello from b2c smoke test')");
                        try (ResultSet rs = st.executeQuery("SELECT count(*) FROM " + testTable)) {
                            rs.next();
                            System.out.println("  inserted; total rows in " + testTable + " = " + rs.getInt(1));
                        }
                        st.execute("DROP TABLE " + testTable);
                        System.out.println("  cleanup ok");
                    }
                    System.out.println("=== OK with " + url + " / " + user + " ===");
                    return; // success, stop
                } catch (Exception e) {
                    System.out.println("FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
        System.out.println("\nAll attempts failed.");
    }
}