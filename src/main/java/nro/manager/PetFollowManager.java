package nro.manager;

import nro.jdbc.MongoDBConnection;
import nro.models.player.PetFollow;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

/**
 * @author outcast c-cute hột me 😳
 */

public class PetFollowManager extends AbsManager<PetFollow> {

    private static final PetFollowManager INSTANCE = new PetFollowManager();

    public static PetFollowManager gI() {
        return INSTANCE;
    }

    public void load() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("pet_follow");
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    int id = rs.getInteger("id_temp", 0);
                    int iconID = rs.getInteger("icon", 0);
                    int w = rs.getInteger("width", 0);
                    int h = rs.getInteger("height", 0);
                    byte nFrame = rs.getInteger("frame", 0).byteValue();
                    add(new PetFollow(id, iconID, w, h, nFrame));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PetFollow findByID(int id) {
        for (PetFollow pet : list) {
            if (pet.getId() == id) {
                return pet;
            }
        }
        return null;
    }
}
