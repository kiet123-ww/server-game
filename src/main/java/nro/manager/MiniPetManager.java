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
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("mini_pet");
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    int id = rs.getInteger("id_temp", 0);
                    short head = rs.getInteger("head", 0).shortValue();
                    short body = rs.getInteger("body", 0).shortValue();
                    short leg = rs.getInteger("leg", 0).shortValue();
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
