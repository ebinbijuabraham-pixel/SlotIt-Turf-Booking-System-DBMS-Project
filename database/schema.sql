CREATE DATABASE IF NOT EXISTS slotit_db;
USE slotit_db;

-- Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('player','owner','admin') NOT NULL,
    wallet_balance DECIMAL(10,2) DEFAULT 0,
    streak_count INT DEFAULT 0,
    total_bookings INT DEFAULT 0,
    badge ENUM('none','silver','gold','platinum') DEFAULT 'none',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Venues Table
CREATE TABLE venues (
    venue_id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    sport_type ENUM('football','cricket','badminton','tennis','basketball') NOT NULL,
    description TEXT,
    rating DECIMAL(3,2) DEFAULT 0,
    total_ratings INT DEFAULT 0,
    image_url VARCHAR(500),
    is_outdoor BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

-- Courts Table
CREATE TABLE courts (
    court_id INT AUTO_INCREMENT PRIMARY KEY,
    venue_id INT NOT NULL,
    court_name VARCHAR(100) NOT NULL,
    hourly_rate DECIMAL(8,2) NOT NULL,
    peak_multiplier DECIMAL(3,2) DEFAULT 1.50,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id) ON DELETE CASCADE
);

-- Time Slots Table
CREATE TABLE time_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    court_id INT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('available','booked','maintenance') DEFAULT 'available',
    is_peak BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (court_id) REFERENCES courts(court_id) ON DELETE CASCADE,
    UNIQUE KEY unique_slot (court_id, slot_date, start_time)
);

-- Bookings Table
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    slot_id INT NOT NULL,
    booking_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount_paid DECIMAL(10,2) NOT NULL,
    payment_status ENUM('pending','completed','refunded') DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id) ON DELETE CASCADE
);

-- Tournaments Table
CREATE TABLE tournaments (
    tournament_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    venue_id INT NOT NULL,
    sport VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    format ENUM('knockout','league') NOT NULL,
    max_teams INT DEFAULT 8,
    status ENUM('upcoming','ongoing','completed') DEFAULT 'upcoming',
    created_by INT NOT NULL,
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Teams Table
CREATE TABLE teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    tournament_id INT NOT NULL,
    captain_id INT NOT NULL,
    player_count INT DEFAULT 0,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id) ON DELETE CASCADE,
    FOREIGN KEY (captain_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Matches Table
CREATE TABLE matches (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    tournament_id INT NOT NULL,
    team1_id INT NOT NULL,
    team2_id INT NOT NULL,
    court_id INT NOT NULL,
    scheduled_at DATETIME NOT NULL,
    match_status ENUM('scheduled','live','completed') DEFAULT 'scheduled',
    score_team1 INT DEFAULT 0,
    score_team2 INT DEFAULT 0,
    winner_id INT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id) ON DELETE CASCADE,
    FOREIGN KEY (team1_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (team2_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (court_id) REFERENCES courts(court_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES teams(team_id) ON DELETE SET NULL
);

-- Waitlist Table
CREATE TABLE waitlist (
    waitlist_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    slot_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id) ON DELETE CASCADE
);

-- Squad Requests Table
CREATE TABLE squad_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    slot_id INT NOT NULL,
    sport VARCHAR(50) NOT NULL,
    players_needed INT NOT NULL,
    current_count INT DEFAULT 1,
    message TEXT,
    status ENUM('open','full','cancelled') DEFAULT 'open',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_venue_location ON venues(latitude, longitude);
CREATE INDEX idx_slot_date_status ON time_slots(slot_date, status);
CREATE INDEX idx_booking_user ON bookings(user_id);
CREATE INDEX idx_tournament_venue ON tournaments(venue_id);

CREATE TABLE IF NOT EXISTS squad_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES squad_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_squad_member (request_id, user_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
