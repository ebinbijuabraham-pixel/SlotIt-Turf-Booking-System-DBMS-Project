-- Concurrency Test Script for Slotit
-- This script demonstrates how we handle double-booking scenarios using database transactions and row-level locks.
-- Run these steps sequentially in two different database client sessions to see the locking mechanism in action.

-- 1. Initial State
-- Show that the target slot is currently available.
SELECT slot_id, court_id, start_time, end_time, status 
FROM time_slots 
WHERE slot_id = 1; 

-- Output should show status = 'available'

-- ==========================================
-- USER A SESSION (Transaction 1)
-- ==========================================

-- 2. User A begins checkout process
START TRANSACTION;

-- User A selects the slot and locks the row to prevent others from modifying it.
-- The FOR UPDATE clause is critical here.
SELECT * FROM time_slots 
WHERE slot_id = 1 
FOR UPDATE;

-- Output shows status = 'available' to User A.

-- ==========================================
-- USER B SESSION (Transaction 2)
-- ==========================================

-- 3. Explain to the examiner/audience:
-- "At this point, if User B tries to SELECT FOR UPDATE on the same row, 
-- they will be BLOCKED until User A commits or rolls back."

-- Try running this in a separate query tab/session:
-- START TRANSACTION;
-- SELECT * FROM time_slots WHERE slot_id = 1 FOR UPDATE;
-- (Notice how the query hangs, waiting for User A's transaction to finish)

-- ==========================================
-- USER A SESSION (Transaction 1)
-- ==========================================

-- 4. Complete User A's booking
-- Deduct balance
UPDATE users 
SET wallet_balance = wallet_balance - 800 
WHERE user_id = 1;

-- Mark slot as booked
UPDATE time_slots 
SET status = 'booked' 
WHERE slot_id = 1;

-- Create the booking record
INSERT INTO bookings (user_id, slot_id, amount_paid, payment_status) 
VALUES (1, 1, 800, 'completed');

-- Commit to release the lock!
COMMIT;

-- ==========================================
-- USER B SESSION (Transaction 2)
-- ==========================================

-- 5. Now Transaction 2 (User B) unblocks and gets the lock!
-- It reads the latest committed data.
-- SELECT * FROM time_slots WHERE slot_id = 1 FOR UPDATE; (This finally returns)

-- Output shows status = 'booked' to User B now.

-- User B's application logic sees it's booked and aborts the booking process.
ROLLBACK; 

-- ==========================================
-- 6. Verify Final State
-- ==========================================
SELECT b.booking_id, u.name, ts.start_time, ts.status 
FROM bookings b
JOIN users u ON b.user_id = u.user_id
JOIN time_slots ts ON b.slot_id = ts.slot_id
WHERE ts.slot_id = 1;
-- Output should show ONLY User A's booking.


-- ==========================================
-- 7. ROLLBACK Scenario (Insufficient Balance)
-- ==========================================

-- Reset the slot for this demo
UPDATE time_slots SET status = 'available' WHERE slot_id = 2;

START TRANSACTION;

-- User C locks the row
SELECT * FROM time_slots WHERE slot_id = 2 FOR UPDATE;

-- Check balance (Application logic determines it's too low)
-- E.g., User C needs 800 but only has 200.

-- Application aborts the transaction
ROLLBACK;

-- The slot remains available for others
SELECT slot_id, status FROM time_slots WHERE slot_id = 2;
-- Output shows status = 'available'
