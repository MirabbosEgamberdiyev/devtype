SET lock_timeout = '5s';

CREATE TABLE admin.audit_log (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actor_id    BIGINT NOT NULL,
  action      VARCHAR(48) NOT NULL,
  entity_type VARCHAR(32) NOT NULL,
  entity_id   VARCHAR(64) NOT NULL,
  reason      VARCHAR(300),
  old_value   JSONB,
  new_value   JSONB,
  ip          INET,
  trace_id    VARCHAR(32),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_entity ON admin.audit_log (entity_type, entity_id, created_at DESC);
CREATE INDEX ix_audit_actor  ON admin.audit_log (actor_id, created_at DESC);

CREATE TABLE admin.product_daily_metrics (
  day               DATE PRIMARY KEY,
  dau               INT NOT NULL,
  new_users         INT NOT NULL,
  activated_users   INT NOT NULL,
  sessions_total    INT NOT NULL,
  sessions_rejected INT NOT NULL,
  sessions_by_mode  JSONB NOT NULL,
  sessions_by_lang  JSONB NOT NULL
);
