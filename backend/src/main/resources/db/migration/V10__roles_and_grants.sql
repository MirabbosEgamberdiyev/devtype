SET lock_timeout = '5s';

DO $$
BEGIN
  -- Agar devtype_app roli mavjud bo'lsa huquqlarni berish
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_app') THEN
    GRANT USAGE ON SCHEMA identity, content, training, analytics, gamification, coach, admin TO devtype_app;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA identity, content, training, analytics, gamification, coach TO devtype_app;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA admin TO devtype_app;
    -- audit_log da faqat SELECT va INSERT (UPDATE va DELETE taqiqlangan)
    REVOKE UPDATE, DELETE, TRUNCATE ON admin.audit_log FROM devtype_app;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA identity, content, training, analytics, gamification, coach, admin TO devtype_app;
  END IF;

  -- Agar devtype_readonly roli mavjud bo'lsa
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_readonly') THEN
    GRANT USAGE ON SCHEMA identity, content, training, analytics, gamification, coach, admin TO devtype_readonly;
    GRANT SELECT ON ALL TABLES IN SCHEMA identity, content, training, analytics, gamification, coach, admin TO devtype_readonly;
  END IF;
END $$;
