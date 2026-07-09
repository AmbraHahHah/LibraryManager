-- ============================================================
-- V43_4: Repair FK references so they match the expected UUIDs from V43_3_Seed_data
-- 
-- Problem: V43_3 uses ON CONFLICT (email) DO NOTHING / ON CONFLICT (isbn) DO NOTHING,
-- so if client/copy rows already exist with different UUIDs, they are kept as-is.
-- This causes FK violations when orders, stock, etc. reference the seed UUIDs that
-- don't exist.
-- 
-- Solution: Drop FK constraints, update the referenced IDs to match the seed UUIDs
-- based on email (for client) and isbn (for copy), then re-add the FKs.
-- ============================================================

-- Temporarily drop FK constraints referencing client(id)
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_customer_id_fkey;
ALTER TABLE review DROP CONSTRAINT IF EXISTS review_client_id_fkey;

-- Temporarily drop FK constraints referencing copy(id)
ALTER TABLE order_line DROP CONSTRAINT IF EXISTS order_line_copy_id_fkey;
ALTER TABLE stock DROP CONSTRAINT IF EXISTS stock_copy_id_fkey;
ALTER TABLE stock_movement DROP CONSTRAINT IF EXISTS stock_movement_copy_id_fkey;

-- ============================================================
-- 1. CLIENT: align old UUIDs to seed UUIDs based on email
-- ============================================================
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000001' WHERE email = 'jean.dupont@email.com' AND id != 'c1000001-0000-0000-0000-000000000001';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000002' WHERE email = 'sophie.martin@email.com' AND id != 'c1000001-0000-0000-0000-000000000002';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000003' WHERE email = 'john.smith@email.com' AND id != 'c1000001-0000-0000-0000-000000000003';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000004' WHERE email = 'emily.johnson@email.com' AND id != 'c1000001-0000-0000-0000-000000000004';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000005' WHERE email = 'yuki.tanaka@email.com' AND id != 'c1000001-0000-0000-0000-000000000005';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000006' WHERE email = 'marie.bernard@email.com' AND id != 'c1000001-0000-0000-0000-000000000006';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000007' WHERE email = 'james.williams@email.com' AND id != 'c1000001-0000-0000-0000-000000000007';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000008' WHERE email = 'lucia.garcia@email.com' AND id != 'c1000001-0000-0000-0000-000000000008';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000009' WHERE email = 'hans.muller@email.com' AND id != 'c1000001-0000-0000-0000-000000000009';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000010' WHERE email = 'maria.rossi@email.com' AND id != 'c1000001-0000-0000-0000-000000000010';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000011' WHERE email = 'sarah.brown@email.com' AND id != 'c1000001-0000-0000-0000-000000000011';
UPDATE client SET id = 'c1000001-0000-0000-0000-000000000012' WHERE email = 'pierre.lefevre@email.com' AND id != 'c1000001-0000-0000-0000-000000000012';

-- ============================================================
-- 2. COPY: update old UUIDs to seed UUIDs based on isbn
-- ============================================================
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000001' WHERE isbn = '9780747532743' AND id != 'c0a00001-0000-0000-0000-000000000001';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000002' WHERE isbn = '9780747532699' AND id != 'c0a00001-0000-0000-0000-000000000002';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000003' WHERE isbn = '9780618640157' AND id != 'c0a00001-0000-0000-0000-000000000003';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000004' WHERE isbn = '9780451524935' AND id != 'c0a00001-0000-0000-0000-000000000004';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000005' WHERE isbn = '9780141439518' AND id != 'c0a00001-0000-0000-0000-000000000005';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000006' WHERE isbn = '9782070409228' AND id != 'c0a00001-0000-0000-0000-000000000006';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000007' WHERE isbn = '9782070409235' AND id != 'c0a00001-0000-0000-0000-000000000007';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000008' WHERE isbn = '9780307743657' AND id != 'c0a00001-0000-0000-0000-000000000008';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000009' WHERE isbn = '9781400079278' AND id != 'c0a00001-0000-0000-0000-000000000009';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000010' WHERE isbn = '9780062073488' AND id != 'c0a00001-0000-0000-0000-000000000010';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000011' WHERE isbn = '9780553293357' AND id != 'c0a00001-0000-0000-0000-000000000011';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000012' WHERE isbn = '9782081374230' AND id != 'c0a00001-0000-0000-0000-000000000012';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000013' WHERE isbn = '9782253000396' AND id != 'c0a00001-0000-0000-0000-000000000013';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000014' WHERE isbn = '9782070409341' AND id != 'c0a00001-0000-0000-0000-000000000014';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000015' WHERE isbn = '9780062572232' AND id != 'c0a00001-0000-0000-0000-000000000015';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000016' WHERE isbn = '9780345539434' AND id != 'c0a00001-0000-0000-0000-000000000016';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000017' WHERE isbn = '9780618640158' AND id != 'c0a00001-0000-0000-0000-000000000017';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000018' WHERE isbn = '9782253000397' AND id != 'c0a00001-0000-0000-0000-000000000018';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000019' WHERE isbn = '9782070409229' AND id != 'c0a00001-0000-0000-0000-000000000019';
UPDATE copy SET id = 'c0a00001-0000-0000-0000-000000000020' WHERE isbn = '9780451524936' AND id != 'c0a00001-0000-0000-0000-000000000020';

-- ============================================================
-- 3. Re-add FK constraints
-- ============================================================
ALTER TABLE orders ADD CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES client(id);
ALTER TABLE review ADD CONSTRAINT review_client_id_fkey FOREIGN KEY (client_id) REFERENCES client(id);
ALTER TABLE order_line ADD CONSTRAINT order_line_copy_id_fkey FOREIGN KEY (copy_id) REFERENCES copy(id);
ALTER TABLE stock ADD CONSTRAINT stock_copy_id_fkey FOREIGN KEY (copy_id) REFERENCES copy(id);
ALTER TABLE stock_movement ADD CONSTRAINT stock_movement_copy_id_fkey FOREIGN KEY (copy_id) REFERENCES copy(id);