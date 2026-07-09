DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'author' AND column_name = 'name') THEN
        ALTER TABLE author RENAME COLUMN name TO last_name;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'client' AND column_name = 'name') THEN
        ALTER TABLE client RENAME COLUMN name TO last_name;
    END IF;
END $$;
