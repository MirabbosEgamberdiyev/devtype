SET lock_timeout = '5s';

CREATE TABLE coach.goals (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id       BIGINT   NOT NULL,
  language_id   SMALLINT NOT NULL,
  start_wpm     NUMERIC(6,2) NOT NULL,
  target_wpm    NUMERIC(6,2) NOT NULL,
  target_date   DATE,
  status        VARCHAR(20) NOT NULL DEFAULT 'INSUFFICIENT_DATA' CHECK (status IN
                ('INSUFFICIENT_DATA','ON_TRACK','BEHIND','STALLED','ACHIEVED','CANCELLED')),
  slope_per_day NUMERIC(8,4),
  eta_date      DATE,
  projected_at  TIMESTAMPTZ,
  achieved_at   TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_goal_active ON coach.goals (user_id, language_id)
  WHERE status NOT IN ('ACHIEVED','CANCELLED');

CREATE TABLE coach.plans (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id      BIGINT   NOT NULL,
  language_id  SMALLINT NOT NULL,
  day          DATE     NOT NULL,
  completed_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, language_id, day)
);

CREATE TABLE coach.plan_items (
  plan_id      BIGINT NOT NULL REFERENCES coach.plans(id) ON DELETE CASCADE,
  seq          SMALLINT NOT NULL,
  mode         VARCHAR(16) NOT NULL,
  params       JSONB NOT NULL DEFAULT '{}',
  reason_code  VARCHAR(32) NOT NULL,
  reason_args  JSONB NOT NULL DEFAULT '{}',
  completed_at TIMESTAMPTZ,
  session_id   UUID,
  PRIMARY KEY (plan_id, seq)
);

CREATE TABLE coach.reports (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  week_key   VARCHAR(8) NOT NULL,
  payload    JSONB NOT NULL,
  narrative  JSONB,
  emailed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, week_key)
);

CREATE TABLE coach.llm_calls (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id      BIGINT NOT NULL,
  purpose      VARCHAR(16) NOT NULL CHECK (purpose IN ('WEEKLY_REPORT','EXPLAIN')),
  payload_hash CHAR(64) NOT NULL,
  locale       VARCHAR(4) NOT NULL,
  status       VARCHAR(16) NOT NULL CHECK (status IN ('OK','SCHEMA_ERROR','TIMEOUT','PROVIDER_ERROR','BUDGET')),
  tokens_in    INT,
  tokens_out   INT,
  latency_ms   INT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_llm_user_time ON coach.llm_calls (user_id, created_at DESC);
