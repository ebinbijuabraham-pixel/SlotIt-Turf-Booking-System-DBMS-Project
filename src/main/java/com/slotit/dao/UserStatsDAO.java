package com.slotit.dao;

// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoCollection;
// import com.mongodb.client.MongoDatabase;
// import org.bson.Document;

/**
 * Data Access Object for MongoDB collections: player_stats and match_commentary.
 * Demonstrates NoSQL polyglot persistence for flexible, unstructured sports data.
 */
public class UserStatsDAO {

    // private MongoClient mongoClient;
    // private MongoDatabase database;

    public UserStatsDAO() {
        // Initialize MongoDB connection
        // mongoClient = MongoClients.create("mongodb://localhost:27017");
        // database = mongoClient.getDatabase("slotit_db");
    }

    /**
     * Retrieves the stats for a given user ID.
     */
    public String getPlayerStats(int userId) {
        // MongoCollection<Document> collection = database.getCollection("player_stats");
        // Document query = new Document("user_id", userId);
        // Document result = collection.find(query).first();
        // return result != null ? result.toJson() : "{}";

        return "{ \"user_id\": " + userId + ", \"matches_played\": 12, \"goals\": 8, \"motm\": 3, \"win_rate\": 67 }";
    }

    /**
     * Updates or inserts a stat document for a user.
     */
    public void updatePlayerStats(int userId, String statsJson) {
        // MongoCollection<Document> collection = database.getCollection("player_stats");
        // Document query = new Document("user_id", userId);
        // Document update = Document.parse(statsJson);
        // Document updateOperation = new Document("$set", update);
        // collection.updateOne(query, updateOperation, new com.mongodb.client.model.UpdateOptions().upsert(true));

        System.out.println("Updated stats for user " + userId);
    }

    /**
     * Retrieves match commentary for a specific match.
     */
    public String getMatchCommentary(int matchId) {
        // MongoCollection<Document> collection = database.getCollection("match_commentary");
        // Document query = new Document("match_id", matchId);
        // List<String> commentaries = new ArrayList<>();
        // for (Document doc : collection.find(query)) {
        //     commentaries.add(doc.toJson());
        // }
        // return commentaries.toString();

        return "[{ \"match_id\": " + matchId + ", \"minute\": 12, \"event\": \"Goal!\" }]";
    }

    /**
     * Adds a new commentary event to a match.
     */
    public void addMatchCommentary(int matchId, int minute, String event) {
        // MongoCollection<Document> collection = database.getCollection("match_commentary");
        // Document newEntry = new Document("match_id", matchId)
        //                         .append("minute", minute)
        //                         .append("event", event);
        // collection.insertOne(newEntry);

        System.out.println("Added commentary for match " + matchId);
    }
}
