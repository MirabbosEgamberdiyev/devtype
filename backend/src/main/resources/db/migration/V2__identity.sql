SET lock_timeout = '5s';

CREATE TABLE identity.users (
  id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email                 CITEXT NOT NULL UNIQUE,
  username              CITEXT NOT NULL UNIQUE
                        CHECK (username ~ '^[A-Za-z][A-Za-z0-9_]{2,19}$'),
  display_name          VARCHAR(40),
  password_hash         VARCHAR(255),
  role                  VARCHAR(16) NOT NULL DEFAULT 'USER'
                        CHECK (role IN ('USER','EDITOR','ADMIN')),
  status                VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','BLOCKED','DELETED')),
  email_verified        BOOLEAN NOT NULL DEFAULT FALSE,
  country_code          CHAR(2),
  avatar_key            VARCHAR(200),
  bio                   VARCHAR(160),
  timezone              VARCHAR(64) NOT NULL DEFAULT 'UTC',
  timezone_changed_at   TIMESTAMPTZ,
  locale                VARCHAR(4) NOT NULL DEFAULT 'en'
                        CHECK (locale IN ('uzl','uzc','ru','en')),
  is_public             BOOLEAN NOT NULL DEFAULT TRUE,
  show_on_leaderboard   BOOLEAN NOT NULL DEFAULT TRUE,
  totp_secret           BYTEA,
  totp_enabled          BOOLEAN NOT NULL DEFAULT FALSE,
  onboarded             BOOLEAN NOT NULL DEFAULT FALSE,
  onboarding_step       SMALLINT NOT NULL DEFAULT 1,
  experience            VARCHAR(16) CHECK (experience IN ('NEW','MID','SENIOR')),
  blocked_reason        VARCHAR(300),
  deletion_scheduled_at TIMESTAMPTZ,
  last_login_at         TIMESTAMPTZ,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  version               INT NOT NULL DEFAULT 0
);

CREATE INDEX ix_users_country ON identity.users (country_code) WHERE status = 'ACTIVE';
CREATE INDEX ix_users_deletion ON identity.users (deletion_scheduled_at) WHERE status = 'DELETED';

CREATE TABLE identity.oauth_accounts (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id      BIGINT NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  provider     VARCHAR(16) NOT NULL CHECK (provider IN ('GITHUB','GOOGLE')),
  provider_uid VARCHAR(100) NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (provider, provider_uid),
  UNIQUE (user_id, provider)
);

CREATE TABLE identity.refresh_tokens (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id      BIGINT NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  token_hash   CHAR(64) NOT NULL UNIQUE,
  family_id    UUID NOT NULL,
  replaced_by  BIGINT,
  expires_at   TIMESTAMPTZ NOT NULL,
  revoked_at   TIMESTAMPTZ,
  user_agent   VARCHAR(300),
  ip           INET,
  last_used_at TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_refresh_user ON identity.refresh_tokens (user_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_refresh_family ON identity.refresh_tokens (family_id);

CREATE TABLE identity.one_time_tokens (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     BIGINT NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  purpose     VARCHAR(16) NOT NULL CHECK (purpose IN ('VERIFY_EMAIL','RESET_PASSWORD')),
  token_hash  CHAR(64) NOT NULL UNIQUE,
  expires_at  TIMESTAMPTZ NOT NULL,
  used_at     TIMESTAMPTZ,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.user_settings (
  user_id    BIGINT PRIMARY KEY REFERENCES identity.users(id) ON DELETE CASCADE,
  settings   JSONB NOT NULL DEFAULT '{}',
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.follows (
  follower_id BIGINT NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  followee_id BIGINT NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (follower_id, followee_id),
  CHECK (follower_id <> followee_id)
);

CREATE INDEX ix_follows_followee ON identity.follows (followee_id);
