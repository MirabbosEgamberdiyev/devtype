package com.devtype;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationTests {

    private static final String JDBC_URL = System.getProperty(
        "test.db.url",
        "jdbc:postgresql://localhost:5432/devtype_test"
    );
    private static final String USER = System.getProperty("test.db.user", "devtype_migrator");
    private static final String PASSWORD = System.getProperty("test.db.password", "devtype_migrator_password");

    @Test
    @Tag("AC-BE-1")
    @DisplayName("AC-BE-1: Bo'sh PostgreSQL bazasida Flyway V1..V10 migratsiyalari to'liq va muvaffaqiyatli o'tadi")
    void testFlywayMigrations() throws Exception {
        // Avval sxemalarni tozalash (test takroriyligi uchun)
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS identity CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS content CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS training CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS analytics CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS gamification CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS coach CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS admin CASCADE");
            stmt.execute("DROP TABLE IF EXISTS flyway_schema_history CASCADE");
        }

        Flyway flyway = Flyway.configure()
            .dataSource(JDBC_URL, USER, PASSWORD)
            .locations("classpath:db/migration")
            .placeholderReplacement(false)
            .cleanDisabled(false)
            .load();

        var result = flyway.migrate();
        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(10);

        // Barcha 7 sxema mavjudligini tekshirish
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            Set<String> schemas = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name IN " +
                "('identity', 'content', 'training', 'analytics', 'gamification', 'coach', 'admin')"
            )) {
                while (rs.next()) {
                    schemas.add(rs.getString("schema_name"));
                }
            }
            assertThat(schemas).containsExactlyInAnyOrder(
                "identity", "content", "training", "analytics", "gamification", "coach", "admin"
            );

            // Barcha 44 ta jadval yaratilganini tekshirish
            Set<String> tables = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery(
                "SELECT table_schema || '.' || table_name AS full_name " +
                "FROM information_schema.tables " +
                "WHERE table_schema IN ('identity', 'content', 'training', 'analytics', 'gamification', 'coach', 'admin')"
            )) {
                while (rs.next()) {
                    tables.add(rs.getString("full_name"));
                }
            }

            assertThat(tables).contains(
                // identity
                "identity.users", "identity.oauth_accounts", "identity.refresh_tokens",
                "identity.one_time_tokens", "identity.user_settings", "identity.follows",
                // content
                "content.languages", "content.topics", "content.exercises",
                "content.tags", "content.exercise_tags", "content.exercise_reports",
                // training
                "training.user_languages", "training.user_topic_progress", "training.user_exercise_best",
                "training.sessions", "training.session_exercises", "training.session_keylogs",
                "training.session_char_stats",
                // analytics
                "analytics.user_language_stats", "analytics.user_daily_stats", "analytics.user_char_stats",
                "analytics.user_bigram_stats", "analytics.processed_sessions",
                // gamification
                "gamification.user_progress", "gamification.xp_transactions", "gamification.streak_freeze_usages",
                "gamification.achievements", "gamification.user_achievements", "gamification.personal_records",
                "gamification.challenge_templates", "gamification.challenges", "gamification.user_challenges",
                "gamification.leaderboard_entries", "gamification.competitions", "gamification.competition_entries",
                "gamification.user_notifications",
                // coach
                "coach.goals", "coach.plans", "coach.plan_items", "coach.reports", "coach.llm_calls",
                // admin
                "admin.audit_log", "admin.product_daily_metrics"
            );

            // Repeatable migratsiya orqali 16 ta til kiritilganini tekshirish
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) AS cnt FROM content.languages")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt("cnt")).isEqualTo(16);
            }
        }
    }
}
