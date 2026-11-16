SET lock_timeout = '5s';

CREATE TABLE gamification.user_progress (
  user_id          BIGINT PRIMARY KEY REFERENCES identity.users(id),
  total_xp         BIGINT NOT NULL DEFAULT 0 CHECK (total_xp >= 0),
  level            INT    NOT NULL DEFAULT 1,
  current_streak   INT    NOT NULL DEFAULT 0,
  longest_streak   INT    NOT NULL DEFAULT 0,
  streak_start_day DATE,
  last_active_day  DATE,
  freezes_left     SMALLINT NOT NULL DEFAULT 2,
  xp_today         INT NOT NULL DEFAULT 0,
  xp_today_day     DATE,
  version          INT NOT NULL DEFAULT 0
);

CREATE TABLE gamification.xp_transactions (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     BIGINT NOT NULL,
  amount      INT NOT NULL,
  source_type VARCHAR(24) NOT NULL CHECK (source_type IN
              ('SESSION','ACHIEVEMENT','DAILY_CHALLENGE','WEEKLY_CHALLENGE','STREAK',
               'COACH_PLAN','GOAL','COMPETITION','ADMIN')),
  source_id   VARCHAR(80) NOT NULL,
  details     JSONB,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, source_type, source_id)
);
CREATE INDEX ix_xp_user_time ON gamification.xp_transactions (user_id, created_at DESC);

CREATE TABLE gamification.streak_freeze_usages (
  user_id BIGINT NOT NULL,
  day     DATE   NOT NULL,
  PRIMARY KEY (user_id, day)
);

CREATE TABLE gamification.achievements (
  id               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code             VARCHAR(48) NOT NULL UNIQUE,
  name             VARCHAR(80) NOT NULL,
  description      VARCHAR(300) NOT NULL,
  name_i18n        JSONB NOT NULL DEFAULT '{}',
  description_i18n JSONB NOT NULL DEFAULT '{}',
  category         VARCHAR(24) NOT NULL,
  criteria         JSONB NOT NULL,
  xp_reward        INT NOT NULL,
  hidden           BOOLEAN NOT NULL DEFAULT FALSE,
  is_active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE gamification.user_achievements (
  user_id        BIGINT NOT NULL,
  achievement_id INT NOT NULL REFERENCES gamification.achievements(id),
  tier           SMALLINT NOT NULL DEFAULT 0,
  progress       NUMERIC(14,2) NOT NULL DEFAULT 0,
  unlocked_at    TIMESTAMPTZ,
  PRIMARY KEY (user_id, achievement_id)
);

CREATE TABLE gamification.personal_records (
  user_id          BIGINT NOT NULL,
  language_id      SMALLINT NOT NULL,
  mode             VARCHAR(16) NOT NULL,
  duration_limit_s SMALLINT NOT NULL DEFAULT 0,
  best_wpm         NUMERIC(6,2) NOT NULL,
  accuracy         NUMERIC(5,2) NOT NULL,
  session_id       UUID NOT NULL,
  achieved_at      TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (user_id, language_id, mode, duration_limit_s)
);

CREATE TABLE gamification.challenge_templates (
  id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  kind       VARCHAR(8) NOT NULL CHECK (kind IN ('DAILY','WEEKLY')),
  title      VARCHAR(120) NOT NULL,
  title_i18n JSONB NOT NULL DEFAULT '{}',
  goal       JSONB NOT NULL,
  xp_reward  INT NOT NULL,
  weight     SMALLINT NOT NULL DEFAULT 1,
  is_active  BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE gamification.challenges (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  template_id INT NOT NULL REFERENCES gamification.challenge_templates(id),
  kind        VARCHAR(8) NOT NULL,
  period_key  VARCHAR(12) NOT NULL,
  goal        JSONB NOT NULL,
  xp_reward   INT NOT NULL,
  starts_at   TIMESTAMPTZ NOT NULL,
  ends_at     TIMESTAMPTZ NOT NULL,
  UNIQUE (kind, period_key)
);

CREATE TABLE gamification.user_challenges (
  user_id      BIGINT NOT NULL,
  challenge_id BIGINT NOT NULL REFERENCES gamification.challenges(id),
  progress     INT NOT NULL DEFAULT 0,
  attempts     SMALLINT NOT NULL DEFAULT 0,
  completed_at TIMESTAMPTZ,
  PRIMARY KEY (user_id, challenge_id)
);

CREATE TABLE gamification.leaderboard_entries (
  board_key   VARCHAR(64) NOT NULL,
  user_id     BIGINT NOT NULL,
  score       NUMERIC(12,2) NOT NULL,
  accuracy    NUMERIC(5,2) NOT NULL DEFAULT 0,
  session_id  UUID,
  achieved_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (board_key, user_id)
);
CREATE INDEX ix_lb_rank ON gamification.leaderboard_entries
  (board_key, score DESC, accuracy DESC, achieved_at, user_id);

CREATE TABLE gamification.competitions (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title            VARCHAR(120) NOT NULL,
  title_i18n       JSONB NOT NULL DEFAULT '{}',
  language_id      SMALLINT NOT NULL REFERENCES content.languages(id),
  duration_limit_s SMALLINT NOT NULL CHECK (duration_limit_s IN (30,60,120)),
  exercise_keys    UUID[] NOT NULL,
  starts_at        TIMESTAMPTZ NOT NULL,
  ends_at          TIMESTAMPTZ NOT NULL,
  max_attempts     SMALLINT NOT NULL DEFAULT 5,
  status           VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED'
                   CHECK (status IN ('SCHEDULED','ACTIVE','FINISHED','ARCHIVED')),
  rewards_granted  BOOLEAN NOT NULL DEFAULT FALSE,
  created_by       BIGINT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (ends_at > starts_at)
);

CREATE TABLE gamification.competition_entries (
  competition_id  BIGINT NOT NULL REFERENCES gamification.competitions(id),
  user_id         BIGINT NOT NULL,
  attempts        SMALLINT NOT NULL DEFAULT 0,
  best_score      NUMERIC(8,2),
  best_accuracy   NUMERIC(5,2),
  best_session_id UUID,
  joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (competition_id, user_id)
);
CREATE INDEX ix_comp_rank ON gamification.competition_entries
  (competition_id, best_score DESC NULLS LAST, best_accuracy DESC);

CREATE TABLE gamification.user_notifications (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  kind       VARCHAR(24) NOT NULL CHECK (kind IN
             ('ACHIEVEMENT','CHALLENGE_DONE','STREAK_MILESTONE','RANK_CHANGE','COMPETITION_RESULT','GOAL_ACHIEVED')),
  payload    JSONB NOT NULL,
  seen_at    TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_notif_unseen ON gamification.user_notifications (user_id, created_at) WHERE seen_at IS NULL;
