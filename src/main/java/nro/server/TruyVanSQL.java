package nro.server;

import nro.jdbc.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class TruyVanSQL {

    public static String getPlayerNameById(int playerId) {
        String playerName = null;
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("player");
            Document doc = collection.find(Filters.eq("id", playerId)).first();
            if (doc != null) {
                playerName = doc.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerName;
    }

    public static void main(String[] args) {
        int playerId = 1; 
        String playerName = getPlayerNameById(playerId);
        System.out.println("Player Name: " + playerName);
    }
}
