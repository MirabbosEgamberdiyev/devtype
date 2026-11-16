SET lock_timeout = '5s';

CREATE TABLE training.user_languages (
  user_id        BIGINT   NOT NULL REFERENCES identity.users(id),
  language_id    SMALLINT NOT NULL REFERENCES content.languages(id),
  is_primary     BOOLEAN  NOT NULL DEFAULT FALSE,
  is_active      BOOLEAN  NOT NULL DEFAULT TRUE,
  start_level    VARCHAR(16) NOT NULL DEFAULT 'BEGINNER',
  placement_done BOOLEAN NOT NULL DEFAULT FALSE,
  added_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, language_id)
);
CREATE UNIQUE INDEX ux_user_primary_lang ON training.user_languages (user_id) WHERE is_primary;

CREATE TABLE training.user_topic_progress (
  user_id       BIGINT NOT NULL REFERENCES identity.users(id),
  topic_id      INT    NOT NULL REFERENCES content.topics(id),
  difficulty    VARCHAR(16) NOT NULL,
  state         VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE'
                CHECK (state IN ('LOCKED','AVAILABLE','IN_PROGRESS','COMPLETED','MASTERED')),
  completed_cnt INT NOT NULL DEFAULT 0,
  mastered_cnt  INT NOT NULL DEFAULT 0,
  attempts_cnt  INT NOT NULL DEFAULT 0,
  completed_at  TIMESTAMPTZ,
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, topic_id, difficulty)
);
CREATE INDEX ix_utp_resume ON training.user_topic_progress (user_id, state, updated_at DESC);

CREATE TABLE training.user_exercise_best (
  user_id         BIGINT NOT NULL REFERENCES identity.users(id),
  exercise_key    UUID   NOT NULL,
  best_wpm        NUMERIC(6,2) NOT NULL,
  best_accuracy   NUMERIC(5,2) NOT NULL,
  completed       BOOLEAN NOT NULL DEFAULT FALSE,
  attempts        INT NOT NULL DEFAULT 1,
  last_session_id UUID NOT NULL,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, exercise_key)
);

CREATE TABLE training.sessions (
  id               UUID NOT NULL,
  user_id          BIGINT REFERENCES identity.users(id),
  guest_id         UUID,
  language_id      SMALLINT NOT NULL REFERENCES content.languages(id),
  mode             VARCHAR(16) NOT NULL CHECK (mode IN
                   ('PRACTICE','SPEED_TEST','ACCURACY','CHALLENGE','SPECIAL_CHARS','WEAKNESS','CODE_SPRINT')),
  topic_id         INT,
  difficulty       VARCHAR(16),
  duration_limit_s SMALLINT,
  accuracy_master  BOOLEAN NOT NULL DEFAULT FALSE,
  is_placement     BOOLEAN NOT NULL DEFAULT FALSE,
  challenge_id     BIGINT,
  competition_id   BIGINT,
  special_category VARCHAR(16),
  gen_seed         BIGINT,
  status           VARCHAR(16) NOT NULL DEFAULT 'STARTED'
                   CHECK (status IN ('STARTED','COMPLETED','ABANDONED','REJECTED')),
  input_device     VARCHAR(16) NOT NULL DEFAULT 'PHYSICAL' CHECK (input_device IN ('PHYSICAL','TOUCH')),
  strict_indent    BOOLEAN NOT NULL DEFAULT FALSE,
  engine_version   VARCHAR(16) NOT NULL,
  started_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at      TIMESTAMPTZ,
  elapsed_ms       INT,
  wpm              NUMERIC(6,2),
  raw_wpm          NUMERIC(6,2),
  cpm              NUMERIC(7,2),
  accuracy         NUMERIC(5,2),
  consistency      NUMERIC(5,2),
  correct_chars    INT,
  uncorrected_errors INT,
  correct_keys     INT,
  incorrect_keys   INT,
  total_typed      INT,
  backspace_count  INT,
  special_correct  INT,
  special_total    INT,
  completion_pct   NUMERIC(5,2),
  client_wpm       NUMERIC(6,2),
  client_accuracy  NUMERIC(5,2),
  xp_awarded       INT NOT NULL DEFAULT 0,
  ranked           BOOLEAN NOT NULL DEFAULT FALSE,
  ranked_reason    VARCHAR(32),
  reject_reason    VARCHAR(32),
  suspicion_score  SMALLINT NOT NULL DEFAULT 0,
  suspicion_reasons TEXT[] NOT NULL DEFAULT '{}',
  review_status    VARCHAR(16) CHECK (review_status IN ('PENDING','CLEARED','CONFIRMED')),
  reviewed_by      BIGINT,
  result_json      JSONB,
  PRIMARY KEY (id, started_at),
  CHECK (user_id IS NOT NULL OR guest_id IS NOT NULL)
) PARTITION BY RANGE (started_at);

-- Partitsiya uchun default jadval
CREATE TABLE training.sessions_default PARTITION OF training.sessions DEFAULT;

CREATE INDEX ix_sessions_id        ON training.sessions (id);
CREATE INDEX ix_sessions_user_time ON training.sessions (user_id, started_at DESC);
CREATE INDEX ix_sessions_user_lang ON training.sessions (user_id, language_id, started_at DESC)
  WHERE status = 'COMPLETED';
CREATE INDEX ix_sessions_guest     ON training.sessions (guest_id) WHERE user_id IS NULL;
CREATE INDEX ix_sessions_review    ON training.sessions (started_at DESC) WHERE review_status = 'PENDING';
CREATE INDEX ix_sessions_started   ON training.sessions (started_at) WHERE status = 'STARTED';

CREATE TABLE training.session_exercises (
  session_id     UUID     NOT NULL,
  seq            SMALLINT NOT NULL,
  exercise_id    BIGINT   REFERENCES content.exercises(id),
  generated_code TEXT,
  typed_chars    INT,
  correct_chars  INT,
  accuracy       NUMERIC(5,2),
  completed      BOOLEAN,
  started_at     TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (session_id, seq),
  CHECK ((exercise_id IS NULL) <> (generated_code IS NULL))
);
CREATE INDEX ix_sess_ex_exercise ON training.session_exercises (exercise_id, started_at DESC);

CREATE TABLE training.session_keylogs (
  session_id   UUID NOT NULL,
  started_at   TIMESTAMPTZ NOT NULL,
  events       BYTEA NOT NULL,
  event_count  INT NOT NULL,
  keep_forever BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (session_id, started_at)
) PARTITION BY RANGE (started_at);

CREATE TABLE training.session_keylogs_default PARTITION OF training.session_keylogs DEFAULT;

CREATE TABLE training.session_char_stats (
  session_id   UUID NOT NULL,
  started_at   TIMESTAMPTZ NOT NULL,
  token        VARCHAR(12) NOT NULL,
  is_pattern   BOOLEAN NOT NULL DEFAULT FALSE,
  expected_cnt INT NOT NULL,
  error_cnt    INT NOT NULL,
  total_ms     INT NOT NULL,
  timed_cnt    INT NOT NULL,
  PRIMARY KEY (session_id, started_at, token)
) PARTITION BY RANGE (started_at);

CREATE TABLE training.session_char_stats_default PARTITION OF training.session_char_stats DEFAULT;
