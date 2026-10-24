// Connect to or create the database
use slotit_db;

// 1. player_stats collection
// Demonstrates flexible schema for different sports
db.player_stats.insertMany([
  {
    user_id: 1,
    sport: "football",
    stats: {
      goals_scored: 15,
      assists: 8,
      matches_played: 20,
      position: "forward"
    },
    achievements: ["Top Scorer 2025", "Hat-trick Hero"],
    last_updated: new Date()
  },
  {
    user_id: 2,
    sport: "cricket",
    stats: {
      runs_scored: 540,
      wickets_taken: 12,
      batting_average: 45.0,
      bowling_economy: 7.2
    },
    achievements: ["Century Maker"],
    last_updated: new Date()
  }
]);

// 2. match_commentary collection
// Array of timestamped text entries
db.match_commentary.insertMany([
  {
    match_id: 1,
    tournament_id: 1,
    commentary: [
      { time: "0'", event: "Match started", text: "The referee blows the whistle." },
      { time: "15'", event: "Goal", text: "Amazing strike from outside the box by Kollam Kings!" },
      { time: "45'", event: "Half time", text: "Kollam Kings lead 1-0." }
    ],
    created_at: new Date()
  }
]);

// 3. turf_photos collection
// Photo metadata
db.turf_photos.insertMany([
  {
    venue_id: 1,
    user_id: 4, // owner
    url: "https://example.com/photos/turf_a_1.jpg",
    caption: "New artificial turf installed",
    rating: null,
    uploaded_at: new Date()
  },
  {
    venue_id: 1,
    user_id: 1, // player reviewing
    url: "https://example.com/photos/turf_a_review1.jpg",
    caption: "Great playing conditions under the floodlights",
    rating: 5,
    uploaded_at: new Date()
  }
]);

// Create indexes
db.player_stats.createIndex({ user_id: 1, sport: 1 });
db.match_commentary.createIndex({ match_id: 1 });
db.turf_photos.createIndex({ venue_id: 1 });
