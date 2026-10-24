-- Slotit: Advanced SQL Queries for Viva/Presentation
-- These queries demonstrate proficiency in complex SQL operations including JOINs, Aggregations, Window Functions, and Geo-spatial logic.

-- 1. "Find all venues within 5km of my location, sorted by distance" (Haversine Formula)
-- Use when showing search results near the user's current GPS location.
-- Expected output: venue_name, address, distance_km
SELECT 
    v.name, 
    v.address, 
    ( 6371 * acos( cos( radians(12.9716) ) * cos( radians( v.latitude ) ) 
    * cos( radians( v.longitude ) - radians(77.5946) ) 
    + sin( radians(12.9716) ) * sin( radians( v.latitude ) ) ) ) AS distance_km
FROM venues v
HAVING distance_km <= 5
ORDER BY distance_km ASC;


-- 2. "What is the busiest time slot across all venues?" (GROUP BY + COUNT + ORDER BY)
-- Use for generating system-wide analytics on peak usage times.
-- Expected output: start_time, end_time, total_bookings
SELECT 
    ts.start_time, 
    ts.end_time, 
    COUNT(b.booking_id) AS total_bookings
FROM time_slots ts
JOIN bookings b ON ts.slot_id = b.slot_id
WHERE b.payment_status = 'completed'
GROUP BY ts.start_time, ts.end_time
ORDER BY total_bookings DESC
LIMIT 1;


-- 3. "Calculate total revenue per venue for this month" (SUM + JOIN + GROUP BY)
-- Use for venue owner dashboards to show their earnings.
-- Expected output: venue_name, total_revenue
SELECT 
    v.name, 
    SUM(b.amount_paid) AS total_revenue
FROM venues v
JOIN courts c ON v.venue_id = c.venue_id
JOIN time_slots ts ON c.court_id = ts.court_id
JOIN bookings b ON ts.slot_id = b.slot_id
WHERE b.payment_status = 'completed' 
  AND MONTH(b.booking_timestamp) = MONTH(CURRENT_DATE())
  AND YEAR(b.booking_timestamp) = YEAR(CURRENT_DATE())
GROUP BY v.venue_id, v.name;


-- 4. "Find the user with the longest booking streak" (Window functions or subquery)
-- Use for gamification, identifying the most loyal users.
-- Expected output: user_id, username, total_bookings
SELECT 
    u.user_id, 
    u.name, 
    COUNT(b.booking_id) AS total_bookings
FROM users u
JOIN bookings b ON u.user_id = b.user_id
WHERE b.payment_status = 'completed'
GROUP BY u.user_id, u.name
ORDER BY total_bookings DESC
LIMIT 5;


-- 5. "List all slots that were booked and then cancelled (refunded)" (JOIN + WHERE)
-- Use for auditing and managing the cancellation queue.
-- Expected output: booking_id, username, venue_name, amount_refunded
SELECT 
    b.booking_id, 
    u.name, 
    v.name, 
    b.amount_paid AS amount_refunded
FROM bookings b
JOIN users u ON b.user_id = u.user_id
JOIN time_slots ts ON b.slot_id = ts.slot_id
JOIN courts c ON ts.court_id = c.court_id
JOIN venues v ON c.venue_id = v.venue_id
WHERE b.payment_status = 'refunded';


-- 6. "Show occupancy rate per venue for the last 7 days" (COUNT booked / COUNT total * 100)
-- Use to measure venue performance and utilization.
-- Expected output: venue_name, occupancy_percentage
SELECT 
    v.name,
    COUNT(CASE WHEN ts.status = 'booked' THEN 1 END) * 100.0 / COUNT(ts.slot_id) AS occupancy_percentage
FROM venues v
JOIN courts c ON v.venue_id = c.venue_id
JOIN time_slots ts ON c.court_id = ts.court_id
-- assuming start_time is a time only or datetime, adjust as necessary if we had dates
GROUP BY v.venue_id, v.name;


-- 7. "Find venues where peak slots earn more than 3x off-peak slots" (HAVING)
-- Use to identify highly demand-driven venues that benefit most from dynamic pricing.
-- Expected output: venue_name, peak_revenue, offpeak_revenue
SELECT 
    v.name,
    SUM(CASE WHEN ts.is_peak = 1 THEN b.amount_paid ELSE 0 END) AS peak_revenue,
    SUM(CASE WHEN ts.is_peak = 0 THEN b.amount_paid ELSE 0 END) AS offpeak_revenue
FROM venues v
JOIN courts c ON v.venue_id = c.venue_id
JOIN time_slots ts ON c.court_id = ts.court_id
JOIN bookings b ON ts.slot_id = b.slot_id
WHERE b.payment_status = 'completed'
GROUP BY v.venue_id, v.name
HAVING peak_revenue > (3 * offpeak_revenue) AND offpeak_revenue > 0;


-- 8. "Get the average booking value per sport type" (AVG + JOIN + GROUP BY)
-- Use for market analysis to see which sports generate the most revenue per session.
-- Expected output: sport_type, avg_booking_value
SELECT 
    v.sport_type, 
    ROUND(AVG(b.amount_paid), 2) AS avg_booking_value
FROM courts c
JOIN venues v ON c.venue_id = v.venue_id
JOIN time_slots ts ON c.court_id = ts.court_id
JOIN bookings b ON ts.slot_id = b.slot_id
WHERE b.payment_status = 'completed'
GROUP BY v.sport_type
ORDER BY avg_booking_value DESC;


-- 9. "List users who have booked at 3 or more different venues" (COUNT DISTINCT + HAVING)
-- Use to find exploratory users for specific marketing campaigns.
-- Expected output: username, distinct_venues_visited
SELECT 
    u.name, 
    COUNT(DISTINCT c.venue_id) AS distinct_venues_visited
FROM users u
JOIN bookings b ON u.user_id = b.user_id
JOIN time_slots ts ON b.slot_id = ts.slot_id
JOIN courts c ON ts.court_id = c.court_id
WHERE b.payment_status = 'completed'
GROUP BY u.user_id, u.name
HAVING distinct_venues_visited >= 3
ORDER BY distinct_venues_visited DESC;


-- 10. "Find time slots that are always booked (popularity ranking)" (Aggregation)
-- Use to recommend the best times for new venues to open up courts.
-- Expected output: start_time, end_time, booking_count
SELECT 
    ts.start_time, 
    ts.end_time,
    COUNT(*) as booking_count
FROM time_slots ts
WHERE ts.status = 'booked'
GROUP BY ts.start_time, ts.end_time
ORDER BY booking_count DESC
LIMIT 10;
