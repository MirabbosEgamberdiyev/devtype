SET lock_timeout = '5s';

-- 1. Mavzularni kiritish (topics)
INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'variables', 'Variables and Constants', 1, 'PUBLISHED', '{"en": "Variables and Constants", "uzl": "Oʻzgaruvchilar va Konstantalar", "uzc": "Ўзгарувчилар ва Константалар", "ru": "Переменные и константы"}'
FROM content.languages l WHERE l.slug = 'java'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'operators', 'Operators and Expressions', 2, 'PUBLISHED', '{"en": "Operators and Expressions", "uzl": "Operatorlar va Ifodalar", "uzc": "Операторлар ва Ифодалар", "ru": "Операторы и выражения"}'
FROM content.languages l WHERE l.slug = 'java'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'conditions', 'Conditionals and Branching', 3, 'PUBLISHED', '{"en": "Conditionals and Branching", "uzl": "Shartlar va Tarmoqlanish", "uzc": "Шартлар ва Тармоқланиш", "ru": "Условия и ветвление"}'
FROM content.languages l WHERE l.slug = 'java'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'loops', 'Loops and Iteration', 4, 'PUBLISHED', '{"en": "Loops and Iteration", "uzl": "Takrorlanish va Sikllar", "uzc": "Такрорланиш ва Цикллар", "ru": "Циклы и итерации"}'
FROM content.languages l WHERE l.slug = 'java'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

-- Python mavzulari
INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'variables', 'Variables and Data Types', 1, 'PUBLISHED', '{"en": "Variables and Data Types", "uzl": "Oʻzgaruvchilar va Maʼlumot Turlari", "uzc": "Ўзгарувчилар ва Маълумот Турлари", "ru": "Переменные и типы данных"}'
FROM content.languages l WHERE l.slug = 'python'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

-- SQL mavzulari
INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'select', 'SELECT Queries', 1, 'PUBLISHED', '{"en": "SELECT Queries", "uzl": "SELECT Soʻrovlari", "uzc": "SELECT Сўровлари", "ru": "Запросы SELECT"}'
FROM content.languages l WHERE l.slug = 'sql'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'where', 'WHERE Filtering', 2, 'PUBLISHED', '{"en": "WHERE Filtering", "uzl": "WHERE Filtrlash", "uzc": "WHERE Фильтрга олиш", "ru": "Фильтрация WHERE"}'
FROM content.languages l WHERE l.slug = 'sql'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

-- PL/SQL mavzulari
INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'fundamentals', 'PL/SQL Fundamentals', 1, 'PUBLISHED', '{"en": "PL/SQL Fundamentals", "uzl": "PL/SQL Asoslari", "uzc": "PL/SQL Асослари", "ru": "Основы PL/SQL"}'
FROM content.languages l WHERE l.slug = 'plsql'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

INSERT INTO content.topics (language_id, slug, name, sort_order, status, name_i18n)
SELECT l.id, 'bulk-collect', 'BULK COLLECT and Cursor Limits', 2, 'PUBLISHED', '{"en": "BULK COLLECT and Cursor Limits", "uzl": "BULK COLLECT va Kursor Cheklovlari", "uzc": "BULK COLLECT ва Курсор Чекловлари", "ru": "BULK COLLECT и ограничения курсоров"}'
FROM content.languages l WHERE l.slug = 'plsql'
ON CONFLICT (language_id, slug) DO UPDATE SET status = 'PUBLISHED';

-- 2. Namunaviy Mashqlarni kiritish (exercises)

-- Java BEGINNER (variables)
INSERT INTO content.exercises (
  exercise_key, version, language_id, topic_id, difficulty, exercise_type,
  title, note, code, code_hash, char_count, line_count, special_ratio,
  nesting_depth, char_freq, expected_seconds, ranked_pool, status, published_at
)
SELECT
  '0193a1f2-0000-7000-8000-000000000001'::uuid, 1, l.id, t.id, 'BEGINNER', 'SNIPPET',
  'Primitive Data Types and Constants',
  'Standard variable declarations and final constants in Java.',
  E'final int maxRetryAttempts = 3;\ndouble serverResponseTimeMs = 124.5;\nboolean isServiceHealthy = true;\nString activeProfile = "production";',
  encode(sha256(E'final int maxRetryAttempts = 3;\ndouble serverResponseTimeMs = 124.5;\nboolean isServiceHealthy = true;\nString activeProfile = "production";'::bytea), 'hex'),
  138, 4, 0.120, 0, '{}'::jsonb, 35, true, 'PUBLISHED', now()
FROM content.languages l
JOIN content.topics t ON t.language_id = l.id AND t.slug = 'variables'
WHERE l.slug = 'java'
ON CONFLICT (exercise_key, version) DO NOTHING;

-- Java INTERMEDIATE (operators)
INSERT INTO content.exercises (
  exercise_key, version, language_id, topic_id, difficulty, exercise_type,
  title, note, code, code_hash, char_count, line_count, special_ratio,
  nesting_depth, char_freq, expected_seconds, ranked_pool, status, published_at
)
SELECT
  '0193a1f2-0000-7000-8000-000000000002'::uuid, 1, l.id, t.id, 'INTERMEDIATE', 'SNIPPET',
  'Arithmetic and Bitwise Operations',
  'Arithmetic logic with shift operators and boundary guards.',
  E'int baseMask = 0x0F;\nint shiftedValue = (rawInput << 2) & baseMask;\nboolean withinBounds = (shiftedValue >= 0) && (shiftedValue <= 15);\nint checksum = (baseMask ^ shiftedValue) + (shiftedValue % 3);',
  encode(sha256(E'int baseMask = 0x0F;\nint shiftedValue = (rawInput << 2) & baseMask;\nboolean withinBounds = (shiftedValue >= 0) && (shiftedValue <= 15);\nint checksum = (baseMask ^ shiftedValue) + (shiftedValue % 3);'::bytea), 'hex'),
  194, 4, 0.210, 0, '{}'::jsonb, 50, true, 'PUBLISHED', now()
FROM content.languages l
JOIN content.topics t ON t.language_id = l.id AND t.slug = 'operators'
WHERE l.slug = 'java'
ON CONFLICT (exercise_key, version) DO NOTHING;

-- Java ADVANCED (conditions)
INSERT INTO content.exercises (
  exercise_key, version, language_id, topic_id, difficulty, exercise_type,
  title, note, code, code_hash, char_count, line_count, special_ratio,
  nesting_depth, char_freq, expected_seconds, ranked_pool, status, published_at
)
SELECT
  '0193a1f2-0000-7000-8000-000000000003'::uuid, 1, l.id, t.id, 'ADVANCED', 'SNIPPET',
  'Pattern Matching Switch and Validations',
  'Modern switch expression with record pattern matching.',
  E'String statusDescription = switch (status) {\n    case "ACTIVE" -> "Account is currently in good standing";\n    case "SUSPENDED" -> "Account flagged for security verification";\n    case "DELETED" -> "Account marked for permanent cleanup";\n    default -> throw new IllegalArgumentException("Unknown status");\n};',
  encode(sha256(E'String statusDescription = switch (status) {\n    case "ACTIVE" -> "Account is currently in good standing";\n    case "SUSPENDED" -> "Account flagged for security verification";\n    case "DELETED" -> "Account marked for permanent cleanup";\n    default -> throw new IllegalArgumentException("Unknown status");\n};'::bytea), 'hex'),
  308, 6, 0.180, 1, '{}'::jsonb, 75, true, 'PUBLISHED', now()
FROM content.languages l
JOIN content.topics t ON t.language_id = l.id AND t.slug = 'conditions'
WHERE l.slug = 'java'
ON CONFLICT (exercise_key, version) DO NOTHING;

-- SQL BEGINNER (select)
INSERT INTO content.exercises (
  exercise_key, version, language_id, topic_id, difficulty, exercise_type,
  title, note, code, code_hash, char_count, line_count, special_ratio,
  nesting_depth, char_freq, expected_seconds, ranked_pool, status, published_at
)
SELECT
  '0193a1f2-0000-7000-8000-000000000004'::uuid, 1, l.id, t.id, 'BEGINNER', 'SNIPPET',
  'Basic SELECT Statement',
  'Standard ANSI SQL select query.',
  E'SELECT user_id, email, username\nFROM identity.users\nWHERE is_active = TRUE\nORDER BY created_at DESC\nLIMIT 20;',
  encode(sha256(E'SELECT user_id, email, username\nFROM identity.users\nWHERE is_active = TRUE\nORDER BY created_at DESC\nLIMIT 20;'::bytea), 'hex'),
  113, 5, 0.110, 0, '{}'::jsonb, 30, true, 'PUBLISHED', now()
FROM content.languages l
JOIN content.topics t ON t.language_id = l.id AND t.slug = 'select'
WHERE l.slug = 'sql'
ON CONFLICT (exercise_key, version) DO NOTHING;

-- PL/SQL BEGINNER (fundamentals)
INSERT INTO content.exercises (
  exercise_key, version, language_id, topic_id, difficulty, exercise_type,
  title, note, code, code_hash, char_count, line_count, special_ratio,
  nesting_depth, char_freq, expected_seconds, ranked_pool, status, published_at
)
SELECT
  '0193a1f2-0000-7000-8000-000000000005'::uuid, 1, l.id, t.id, 'BEGINNER', 'SNIPPET',
  'Anonymous Block with Output',
  'Basic PL/SQL block with variable assignment.',
  E'DECLARE\n  v_msg VARCHAR2(50) := \'Hello from PL/SQL\';\nBEGIN\n  DBMS_OUTPUT.PUT_LINE(v_msg);\nEND;\n/',
  encode(sha256(E'DECLARE\n  v_msg VARCHAR2(50) := \'Hello from PL/SQL\';\nBEGIN\n  DBMS_OUTPUT.PUT_LINE(v_msg);\nEND;\n/'::bytea), 'hex'),
  96, 6, 0.220, 0, '{}'::jsonb, 25, true, 'PUBLISHED', now()
FROM content.languages l
JOIN content.topics t ON t.language_id = l.id AND t.slug = 'fundamentals'
WHERE l.slug = 'plsql'
ON CONFLICT (exercise_key, version) DO NOTHING;
