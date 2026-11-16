SET lock_timeout = '5s';

CREATE TABLE analytics.user_language_stats (
  user_id         BIGINT   NOT NULL,
  language_id     SMALLINT NOT NULL,
  mode_group      VARCHAR(8) NOT NULL CHECK (mode_group IN ('CORE','DRILL')),
  sessions_cnt    INT    NOT NULL DEFAULT 0,
  total_time_ms   BIGINT NOT NULL DEFAULT 0,
  total_chars     BIGINT NOT NULL DEFAULT 0,
  sum_wpm         NUMERIC(14,2) NOT NULL DEFAULT 0,
  sum_cpm         NUMERIC(14,2) NOT NULL DEFAULT 0,
  max_wpm         NUMERIC(6,2)  NOT NULL DEFAULT 0,
  correct_keys    BIGINT NOT NULL DEFAULT 0,
  total_keys      BIGINT NOT NULL DEFAULT 0,
  error_cnt       BIGINT NOT NULL DEFAULT 0,
  special_correct BIGINT NOT NULL DEFAULT 0,
  special_total   BIGINT NOT NULL DEFAULT 0,
  recent_wpm      NUMERIC(6,2),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, language_id, mode_group)
);

CREATE TABLE analytics.user_daily_stats (
  user_id      BIGINT   NOT NULL,
  day          DATE     NOT NULL,
  language_id  SMALLINT NOT NULL,
  mode_group   VARCHAR(8) NOT NULL,
  sessions_cnt INT NOT NULL DEFAULT 0,
  time_ms      BIGINT NOT NULL DEFAULT 0,
  chars        BIGINT NOT NULL DEFAULT 0,
  sum_wpm      NUMERIC(12,2) NOT NULL DEFAULT 0,
  max_wpm      NUMERIC(6,2)  NOT NULL DEFAULT 0,
  correct_keys BIGINT NOT NULL DEFAULT 0,
  total_keys   BIGINT NOT NULL DEFAULT 0,
  xp           INT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, day, language_id, mode_group)
);

CREATE TABLE analytics.user_char_stats (
  user_id           BIGINT   NOT NULL,
  language_id       SMALLINT NOT NULL,
  token             VARCHAR(4) NOT NULL,
  expected_cnt      BIGINT NOT NULL DEFAULT 0,
  error_cnt         BIGINT NOT NULL DEFAULT 0,
  total_ms          BIGINT NOT NULL DEFAULT 0,
  timed_cnt         BIGINT NOT NULL DEFAULT 0,
  ewma_error        NUMERIC(6,4) NOT NULL DEFAULT 0,
  ewma_ms           NUMERIC(8,2) NOT NULL DEFAULT 0,
  observed_sessions INT NOT NULL DEFAULT 0,
  confusions        JSONB NOT NULL DEFAULT '{}',
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, language_id, token)
);

CREATE TABLE analytics.user_bigram_stats (
  user_id           BIGINT   NOT NULL,
  language_id       SMALLINT NOT NULL,
  token             VARCHAR(12) NOT NULL,
  expected_cnt      BIGINT NOT NULL DEFAULT 0,
  error_cnt         BIGINT NOT NULL DEFAULT 0,
  total_ms          BIGINT NOT NULL DEFAULT 0,
  timed_cnt         BIGINT NOT NULL DEFAULT 0,
  ewma_error        NUMERIC(6,4) NOT NULL DEFAULT 0,
  ewma_ms           NUMERIC(8,2) NOT NULL DEFAULT 0,
  observed_sessions INT NOT NULL DEFAULT 0,
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, language_id, token)
);

CREATE TABLE analytics.processed_sessions (
  session_id   UUID PRIMARY KEY,
  processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
