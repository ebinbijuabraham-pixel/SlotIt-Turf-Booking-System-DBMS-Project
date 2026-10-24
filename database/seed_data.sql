USE slotit_db;

-- All seeded users have password: password123
-- SHA-256('SlotItSecureSalt2024!' + 'password123') = b06000902f81c12d4e16a722c391c01c3312e091f99625322e5f747db43898e7
INSERT INTO users (name, email, phone, password_hash, role, wallet_balance, streak_count, total_bookings, badge) VALUES
('Ebin Biju Abraham', 'player1@example.com', '9876543210', 'b06000902f81c12d4e16a722c391c01c3312e091f99625322e5f747db43898e7', 'player', 2500.00, 5, 10, 'gold'),
('Tharun Bhaskaran', 'player2@example.com', '9876543211', 'b06000902f81c12d4e16a722c391c01c3312e091f99625322e5f747db43898e7', 'player', 1800.00, 3, 6, 'silver'),
('Afroz Shanavas', 'player3@example.com', '9876543212', 'b06000902f81c12d4e16a722c391c01c3312e091f99625322e5f747db43898e7', 'player', 3000.00, 10, 25, 'platinum'),
('Niveditha C M', 'owner1@example.com', '9876543213', 'b06000902f81c12d4e16a722c391c01c3312e091f99625322e5f747db43898e7', 'owner', 5000.00, 0, 0, 'none'),
('Kollam Turf Admin', 'owner2@example.com', '9876543214', 'b06000902f81c12d4e16a722c391c01c3312e091f99625322e5f747db43898e7', 'owner', 4000.00, 0, 0, 'none');

-- Seed 17 Venues across Kollam, Kochi, Trivandrum, and Calicut
INSERT INTO venues (owner_id, name, address, city, latitude, longitude, sport_type, description, rating, total_ratings, is_outdoor) VALUES
-- Kollam (IDs 1..5)
(4, 'GreenPark Football Arena', 'Kavanad, Kollam', 'Kollam', 8.900123, 76.551234, 'football', 'Premium FIFA-grade 7-a-side artificial turf with LED floodlights, changing rooms, and ample parking.', 4.8, 120, TRUE),
(4, 'SmashPoint Badminton Hub', 'Chinnakada, Kollam', 'Kollam', 8.885678, 76.589012, 'badminton', 'BWF-approved indoor wooden courts with synthetic mats and pro shop.', 4.9, 85, FALSE),
(5, 'CricketZone Nets', 'Kadappakada, Kollam', 'Kollam', 8.882345, 76.601234, 'cricket', 'Full-length box cricket turf and automated bowling machine nets.', 4.5, 45, TRUE),
(5, 'CourtKing Basketball Arena', 'Asramam, Kollam', 'Kollam', 8.891234, 76.582345, 'basketball', 'FIBA standard acrylic outdoor basketball court with night lighting.', 4.6, 60, TRUE),
(4, 'NetBlaze Tennis Club', 'Tangasseri, Kollam', 'Kollam', 8.875678, 76.567890, 'tennis', 'Championship clay and synthetic hard tennis courts by the coast.', 4.4, 30, TRUE),
-- Kochi (IDs 6..9)
(4, 'Kaloor Stadium Turf', 'Kaloor, Kochi', 'Kochi', 9.994100, 76.301800, 'football', 'High-density 7s and 5s FIFA turf right next to JLN International Stadium, Kaloor.', 4.9, 210, TRUE),
(5, 'Kakkanad Shuttle Arena', 'Infopark Road, Kochi', 'Kochi', 10.015900, 76.341900, 'badminton', 'Air-conditioned 6-court BWF synthetic badminton complex near Infopark.', 4.8, 164, FALSE),
(4, 'Panampilly Box Cricket', 'Panampilly Nagar, Kochi', 'Kochi', 9.961500, 76.299000, 'cricket', '360-degree netted rooftop & ground box cricket arena with digital scoreboard.', 4.7, 98, TRUE),
(5, 'Edappally Ace Tennis Club', 'Edappally Toll, Kochi', 'Kochi', 10.026100, 76.312500, 'tennis', 'Floodlit hard-court tennis academy and match arena.', 4.6, 72, FALSE),
-- Trivandrum (IDs 10..13)
(4, 'Technopark Kickoff Arena', 'Kazhakkoottam, Trivandrum', 'Trivandrum', 8.558100, 76.881600, 'football', 'Premier 7-a-side football turf popular with Technopark teams.', 4.8, 185, TRUE),
(5, 'Kowdiar Smash Court', 'Kowdiar Palace Rd, Trivandrum', 'Trivandrum', 8.524100, 76.961200, 'badminton', ' Cushion-mounted wooden indoor badminton courts with lounge.', 4.9, 140, FALSE),
(4, 'Vellayambalam Cricket Hub', 'Vellayambalam, Trivandrum', 'Trivandrum', 8.513000, 76.963400, 'cricket', 'Professional AstroTurf cricket pitches and speed-gun bowling lanes.', 4.6, 88, TRUE),
(5, 'Sasthamangalam Slam Dunk', 'Sasthamangalam, Trivandrum', 'Trivandrum', 8.511200, 76.971500, 'basketball', 'Indoor maple-wood basketball court with glass backboards.', 4.7, 64, FALSE),
-- Calicut (IDs 14..17)
(4, 'Malabar United Football Turf', 'Mavoor Road, Calicut', 'Calicut', 11.258800, 75.780400, 'football', 'Calicut''s flagship 7s football stadium turf with gallery seating.', 4.9, 230, TRUE),
(5, 'Beach Road Badminton Club', 'Vellayil, Calicut', 'Calicut', 11.262100, 75.767200, 'badminton', '4 indoor Yonex-mat badminton courts with sea-breeze ventilation.', 4.7, 95, FALSE),
(4, 'Thondayad Strikers Cricket', 'Thondayad Bypass, Calicut', 'Calicut', 11.268500, 75.805100, 'cricket', 'All-weather covered box cricket arena for 6s and 8s tournaments.', 4.6, 78, TRUE),
(5, 'Nadakkavu Hoops Arena', 'Nadakkavu, Calicut', 'Calicut', 11.274200, 75.776500, 'basketball', 'Pro-grade floodlit basketball court and training hub.', 4.5, 52, TRUE);

-- Seed Courts (2 per venue)
INSERT INTO courts (venue_id, court_name, hourly_rate, peak_multiplier, is_active) VALUES
(1, 'Turf A (7-a-side)', 800.00, 1.5, TRUE),
(1, 'Turf B (5-a-side)', 600.00, 1.5, TRUE),
(2, 'Wooden Court 1', 300.00, 1.25, TRUE),
(2, 'Synthetic Court 2', 300.00, 1.25, TRUE),
(3, 'Box Cricket Turf', 1000.00, 1.5, TRUE),
(3, 'Bowling Machine Net', 500.00, 1.5, TRUE),
(4, 'Main Arena Court', 600.00, 1.5, TRUE),
(4, 'Half Court Practice', 400.00, 1.5, TRUE),
(5, 'Center Clay Court', 700.00, 1.3, TRUE),
(5, 'Synthetic Court B', 800.00, 1.3, TRUE),
(6, 'Kaloor 7s Arena', 1000.00, 1.5, TRUE),
(6, 'Kaloor 5s Cage', 800.00, 1.5, TRUE),
(7, 'Infopark Court 1', 400.00, 1.25, TRUE),
(7, 'Infopark Court 2', 400.00, 1.25, TRUE),
(8, 'Panampilly Turf A', 900.00, 1.5, TRUE),
(8, 'Panampilly Net B', 600.00, 1.5, TRUE),
(9, 'Hard Court 1', 750.00, 1.3, TRUE),
(9, 'Hard Court 2', 750.00, 1.3, TRUE),
(10, 'Techno 7s Turf', 900.00, 1.5, TRUE),
(10, 'Techno 5s Turf', 700.00, 1.5, TRUE),
(11, 'Kowdiar Court A', 350.00, 1.25, TRUE),
(11, 'Kowdiar Court B', 350.00, 1.25, TRUE),
(12, 'Vellayambalam Pitch 1', 850.00, 1.5, TRUE),
(12, 'Vellayambalam Pitch 2', 650.00, 1.5, TRUE),
(13, 'Indoor Maple Court', 500.00, 1.4, TRUE),
(13, 'Practice Half Court', 350.00, 1.4, TRUE),
(14, 'Malabar Main 7s', 850.00, 1.5, TRUE),
(14, 'Malabar 5s West', 650.00, 1.5, TRUE),
(15, 'Yonex Mat Court 1', 300.00, 1.25, TRUE),
(15, 'Yonex Mat Court 2', 300.00, 1.25, TRUE),
(16, 'Bypass Box Turf', 800.00, 1.5, TRUE),
(16, 'Practice Net 2', 500.00, 1.5, TRUE),
(17, 'Nadakkavu Full Court', 450.00, 1.4, TRUE),
(17, 'Nadakkavu 3x3 Court', 350.00, 1.4, TRUE);

-- Seed initial booked/available Time Slots on today's date (slots 1..5)
INSERT INTO time_slots (court_id, slot_date, start_time, end_time, status, is_peak) VALUES
(1, CURRENT_DATE, '18:00:00', '19:00:00', 'booked', TRUE),
(1, CURRENT_DATE, '19:00:00', '20:00:00', 'available', TRUE),
(1, CURRENT_DATE, '08:00:00', '09:00:00', 'available', FALSE),
(3, CURRENT_DATE, '18:00:00', '19:00:00', 'booked', TRUE),
(5, CURRENT_DATE, '19:00:00', '20:00:00', 'booked', TRUE);

-- Generate hourly slots for the next 7 days across all 10 courts (6 AM - 9 PM)
INSERT IGNORE INTO time_slots (court_id, slot_date, start_time, end_time, status, is_peak)
WITH RECURSIVE
  days AS (
    SELECT 0 AS d
    UNION ALL
    SELECT d + 1 FROM days WHERE d < 6
  ),
  hours AS (
    SELECT 6 AS h
    UNION ALL
    SELECT h + 1 FROM hours WHERE h < 21
  )
SELECT
  c.court_id,
  DATE_ADD(CURRENT_DATE, INTERVAL days.d DAY) AS slot_date,
  MAKETIME(hours.h, 0, 0) AS start_time,
  MAKETIME(hours.h + 1, 0, 0) AS end_time,
  'available' AS status,
  CASE WHEN hours.h BETWEEN 18 AND 20 THEN TRUE ELSE FALSE END AS is_peak
FROM courts c
CROSS JOIN days
CROSS JOIN hours
WHERE hours.h IN (6, 7, 8, 9, 10, 16, 17, 18, 19, 20, 21);

-- Seed Bookings (referencing slots 1, 4, 5)
INSERT INTO bookings (user_id, slot_id, amount_paid, payment_status) VALUES
(1, 1, 1200.00, 'completed'),
(2, 4, 375.00, 'completed'),
(3, 5, 1500.00, 'completed');

-- Seed Tournaments
INSERT INTO tournaments (name, venue_id, sport, start_date, end_date, format, max_teams, status, created_by) VALUES
('Kollam Super 7s Cup', 1, 'Football', DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), 'knockout', 8, 'upcoming', 4),
('Chinnakada Smash Open', 2, 'Badminton', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), 'knockout', 16, 'upcoming', 4),
('TKMCE Box Cricket League', 3, 'Cricket', DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 16 DAY), 'league', 8, 'upcoming', 5);

-- Seed Teams for Tournament 1
INSERT INTO teams (name, tournament_id, captain_id, player_count) VALUES
('TKM Strikers', 1, 1, 7),
('Kollam Kings', 1, 2, 7),
('Asramam Avengers', 1, 3, 7),
('Tangasseri Titans', 1, 1, 7);

-- Seed Open Squad Requests
INSERT INTO squad_requests (user_id, slot_id, sport, players_needed, current_count, message, status) VALUES
(1, 2, 'Football', 7, 5, 'Need 2 more players for 7-a-side at GreenPark tonight! Intermediate level.', 'open'),
(2, 3, 'Badminton', 4, 3, 'Need 1 partner for doubles match. Yonex Mavis 350 shuttles ready!', 'open'),
(3, 5, 'Cricket', 11, 8, 'Looking for 3 all-rounders for weekend box cricket clash.', 'open');

INSERT IGNORE INTO squad_members (request_id, user_id) VALUES
(1, 1), (1, 2),
(2, 2),
(3, 3);
