SET lock_timeout = '5s';

-- 90 kundan eski rekord/top-100 sessiyalar keyloglarini doimiy saqlash uchun arxiv jadvali
CREATE TABLE IF NOT EXISTS training.session_keylogs_archive (
  session_id   UUID NOT NULL,
  started_at   TIMESTAMPTZ NOT NULL,
  events       BYTEA NOT NULL,
  event_count  INT NOT NULL,
  keep_forever BOOLEAN NOT NULL DEFAULT TRUE,
  archived_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (session_id, started_at)
);

-- pg_partman mavjud bo'lgan ishlab chiqarish bazalarida partitsiyalarni avtomatlashtirish
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_partman') THEN
    PERFORM partman.create_parent(
      p_parent_table := 'training.sessions',
      p_control := 'started_at',
      p_type := 'native',
      p_interval := 'monthly',
      p_premake := 3
    );
    PERFORM partman.create_parent(
      p_parent_table := 'training.session_keylogs',
      p_control := 'started_at',
      p_type := 'native',
      p_interval := 'monthly',
      p_premake := 3
    );
    PERFORM partman.create_parent(
      p_parent_table := 'training.session_char_stats',
      p_control := 'started_at',
      p_type := 'native',
      p_interval := 'monthly',
      p_premake := 3
    );
  END IF;
END $$;
