-- V18: Defensive type coercion for users table
-- Some legacy rows (or pre-V12 schemas) may have stored phone/fullName/email
-- as bytea. The admin user-search query runs LOWER() on these columns which
-- fails on bytea. Force them to text to make LOWER() safe.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'phone'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN phone TYPE VARCHAR(20)
            USING convert_from(phone, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'email'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN email TYPE VARCHAR(255)
            USING convert_from(email, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = CURRENT_SCHEMA()
          AND table_name = 'users'
          AND column_name = 'full_name'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN full_name TYPE VARCHAR(150)
            USING convert_from(full_name, 'UTF8');
    END IF;
END$$;
