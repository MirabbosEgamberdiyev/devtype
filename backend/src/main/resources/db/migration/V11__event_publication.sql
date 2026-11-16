SET lock_timeout = '5s';

CREATE TABLE IF NOT EXISTS event_publication (
  id               UUID NOT NULL PRIMARY KEY,
  listener_id      VARCHAR(512) NOT NULL,
  event_type       VARCHAR(512) NOT NULL,
  serialized_event TEXT NOT NULL,
  publication_date TIMESTAMPTZ NOT NULL,
  completion_date  TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS ix_event_pub_completion ON event_publication (completion_date);
CREATE INDEX IF NOT EXISTS ix_event_pub_date ON event_publication (publication_date);
