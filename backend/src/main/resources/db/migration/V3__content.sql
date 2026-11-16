SET lock_timeout = '5s';

CREATE TABLE content.languages (
  id                SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  slug              VARCHAR(32) NOT NULL UNIQUE CHECK (slug ~ '^[a-z][a-z0-9]*$'),
  name              VARCHAR(64) NOT NULL,
  grammar_id        VARCHAR(32) NOT NULL,
  indent_style      VARCHAR(8)  NOT NULL DEFAULT 'SPACE' CHECK (indent_style IN ('SPACE','TAB')),
  indent_size       SMALLINT    NOT NULL DEFAULT 4 CHECK (indent_size BETWEEN 1 AND 8),
  keyword_case      VARCHAR(8)  NOT NULL DEFAULT 'LOWER' CHECK (keyword_case IN ('LOWER','UPPER')),
  token_patterns    JSONB NOT NULL DEFAULT '[]',
  special_templates JSONB NOT NULL DEFAULT '{}',
  forbidden_patterns JSONB NOT NULL DEFAULT '[]',
  release           SMALLINT NOT NULL DEFAULT 1,
  status            VARCHAR(16) NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
  sort_order        SMALLINT NOT NULL DEFAULT 0,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE content.topics (
  id               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  language_id      SMALLINT NOT NULL REFERENCES content.languages(id),
  slug             VARCHAR(64) NOT NULL CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  name             VARCHAR(100) NOT NULL,
  description      VARCHAR(500),
  name_i18n        JSONB NOT NULL DEFAULT '{}',
  description_i18n JSONB NOT NULL DEFAULT '{}',
  sort_order       SMALLINT NOT NULL,
  release          SMALLINT NOT NULL DEFAULT 1,
  status           VARCHAR(16) NOT NULL DEFAULT 'DRAFT'
                   CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (language_id, slug)
);

CREATE TABLE content.exercises (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  exercise_key     UUID NOT NULL,
  version          INT  NOT NULL DEFAULT 1,
  language_id      SMALLINT NOT NULL REFERENCES content.languages(id),
  topic_id         INT REFERENCES content.topics(id),
  difficulty       VARCHAR(16) NOT NULL
                   CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')),
  exercise_type    VARCHAR(16) NOT NULL DEFAULT 'SNIPPET'
                   CHECK (exercise_type IN ('SNIPPET','SPECIAL_CHARS','CHALLENGE')),
  title            VARCHAR(150) NOT NULL,
  note             VARCHAR(400),
  note_i18n        JSONB NOT NULL DEFAULT '{}',
  code             TEXT NOT NULL,
  code_hash        CHAR(64) NOT NULL,
  char_count       INT NOT NULL,
  line_count       INT NOT NULL,
  special_ratio    NUMERIC(4,3) NOT NULL,
  nesting_depth    SMALLINT NOT NULL,
  char_freq        JSONB NOT NULL,
  bigram_freq      JSONB NOT NULL DEFAULT '{}',
  expected_seconds INT NOT NULL,
  ranked_pool      BOOLEAN NOT NULL DEFAULT FALSE,
  source           VARCHAR(300) NOT NULL DEFAULT 'original',
  status           VARCHAR(16) NOT NULL DEFAULT 'DRAFT'
                   CHECK (status IN ('DRAFT','IN_REVIEW','PUBLISHED','ARCHIVED')),
  created_by       BIGINT REFERENCES identity.users(id),
  reviewed_by      BIGINT REFERENCES identity.users(id),
  published_at     TIMESTAMPTZ,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (exercise_key, version),
  UNIQUE (language_id, code_hash),
  CHECK ((exercise_type = 'SPECIAL_CHARS') = (topic_id IS NULL)),
  CHECK (reviewed_by IS NULL OR reviewed_by <> created_by)
);

CREATE INDEX ix_exercises_pick ON content.exercises (language_id, topic_id, difficulty)
  WHERE status = 'PUBLISHED';
CREATE INDEX ix_exercises_ranked ON content.exercises (language_id)
  WHERE status = 'PUBLISHED' AND ranked_pool;
CREATE UNIQUE INDEX ux_exercises_one_published ON content.exercises (exercise_key)
  WHERE status = 'PUBLISHED';
CREATE INDEX ix_exercises_review ON content.exercises (status, created_at)
  WHERE status IN ('DRAFT','IN_REVIEW');

CREATE TABLE content.tags (
  id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(40) NOT NULL UNIQUE CHECK (name ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

CREATE TABLE content.exercise_tags (
  exercise_id BIGINT NOT NULL REFERENCES content.exercises(id) ON DELETE CASCADE,
  tag_id      INT    NOT NULL REFERENCES content.tags(id) ON DELETE CASCADE,
  PRIMARY KEY (exercise_id, tag_id)
);

CREATE TABLE content.exercise_reports (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  exercise_id  BIGINT NOT NULL REFERENCES content.exercises(id),
  reporter_id  BIGINT NOT NULL REFERENCES identity.users(id),
  reason       VARCHAR(24) NOT NULL
               CHECK (reason IN ('SYNTAX_ERROR','WRONG_LEVEL','NOT_IDIOMATIC','TYPO','OTHER')),
  comment      VARCHAR(500),
  status       VARCHAR(16) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','RESOLVED','DISMISSED')),
  resolved_by  BIGINT REFERENCES identity.users(id),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at  TIMESTAMPTZ,
  UNIQUE (exercise_id, reporter_id)
);
