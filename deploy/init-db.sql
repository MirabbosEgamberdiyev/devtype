-- DevType PostgreSQL boshlang'ich rollari
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_app') THEN
    CREATE ROLE devtype_app WITH LOGIN PASSWORD 'devtype_app_password';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_readonly') THEN
    CREATE ROLE devtype_readonly WITH LOGIN PASSWORD 'devtype_readonly_password';
  END IF;
END $$;
