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
            list.clear();
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("pet_follow");
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    Object idObj = rs.get("id_temp");
                    int id = idObj != null ? Integer.parseInt(idObj.toString()) : 0;
                    Object iconObj = rs.get("icon");
                    int iconID = iconObj != null ? Integer.parseInt(iconObj.toString()) : 0;
                    Object wObj = rs.get("width");
                    int w = wObj != null ? Integer.parseInt(wObj.toString()) : 0;
                    Object hObj = rs.get("height");
                    int h = hObj != null ? Integer.parseInt(hObj.toString()) : 0;
                    Object frameObj = rs.get("frame");
                    byte nFrame = frameObj != null ? Byte.parseByte(frameObj.toString()) : 0;
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
