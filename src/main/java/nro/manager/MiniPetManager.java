package nro.manager;

import nro.jdbc.MongoDBConnection;
import nro.models.item.MinipetTemplate;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */

public class MiniPetManager implements IManager<MinipetTemplate> {

    private static final MiniPetManager INSTANCE = new MiniPetManager();

    private List<MinipetTemplate> list = new ArrayList<>();

    public static MiniPetManager gI() {
        return INSTANCE;
    }

    public void load() {
        try {
            list.clear();
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("mini_pet");
            try (MongoCursor<Document> cursor = collection.find().sort(com.mongodb.client.model.Sorts.ascending("id_temp")).iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    Object idObj = rs.get("id_temp");
                    int id = idObj != null ? Integer.parseInt(idObj.toString()) : 0;
                    Object headObj = rs.get("head");
                    short head = headObj != null ? Short.parseShort(headObj.toString()) : 0;
                    Object bodyObj = rs.get("body");
                    short body = bodyObj != null ? Short.parseShort(bodyObj.toString()) : 0;
                    Object legObj = rs.get("leg");
                    short leg = legObj != null ? Short.parseShort(legObj.toString()) : 0;
                    add(new MinipetTemplate(id, head, body, leg));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(MinipetTemplate minipetTemplate) {
        list.add(minipetTemplate);
    }

    @Override
    public void remove(MinipetTemplate minipetTemplate) {
        list.remove(minipetTemplate);
    }

    @Override
    public MinipetTemplate findByID(int id) {
        for (MinipetTemplate temp : list) {
            if (temp.getId() == id) {
                return temp;
            }
        }
        return null;
    }
}
