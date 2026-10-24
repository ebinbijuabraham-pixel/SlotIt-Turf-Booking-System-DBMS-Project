DELIMITER //

DROP TRIGGER IF EXISTS trg_update_streak //
DROP TRIGGER IF EXISTS trg_slot_drop_notify //

-- Trigger 1: trg_update_streak
-- After a user makes a booking, increment their streak_count in the users table.
CREATE TRIGGER trg_update_streak
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    UPDATE users
    SET streak_count = IFNULL(streak_count, 0) + 1
    WHERE user_id = NEW.user_id;
END //

-- Trigger 2: trg_slot_drop_notify
-- After a slot status changes from 'booked' to 'available', notify users in the waitlist.
CREATE TRIGGER trg_slot_drop_notify
AFTER UPDATE ON time_slots
FOR EACH ROW
BEGIN
    -- Check if the status actually changed from 'booked' to 'available'
    IF OLD.status = 'booked' AND NEW.status = 'available' THEN
        -- Insert a notification for each user in the waitlist for this slot
        INSERT INTO notifications (user_id, message, is_read, created_at)
        SELECT 
            w.user_id, 
            CONCAT('The slot you waitlisted for on ', NEW.slot_date, ' is now available!'), 
            0,
            NOW()
        FROM waitlist w
        WHERE w.slot_id = NEW.slot_id;
    END IF;
END //

DELIMITER ;
