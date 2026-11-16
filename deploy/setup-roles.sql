DO $init$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_migrator') THEN
    CREATE ROLE devtype_migrator WITH LOGIN SUPERUSER PASSWORD 'devtype_migrator_password';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_app') THEN
    CREATE ROLE devtype_app WITH LOGIN PASSWORD 'devtype_app_password';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'devtype_readonly') THEN
    CREATE ROLE devtype_readonly WITH LOGIN PASSWORD 'devtype_readonly_password';
  END IF;
END $init$;
