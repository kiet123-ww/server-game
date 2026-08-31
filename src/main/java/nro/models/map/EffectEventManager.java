package nro.models.map;

import nro.jdbc.DBService;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;




import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class EffectEventManager {
    private static final EffectEventManager i = new EffectEventManager();

    public static EffectEventManager gI() {
        return i;
    }

    @Getter
    private final List<EffectEventTemplate> list = new ArrayList<>();

    public void load() {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("map_template");
            com.mongodb.client.MongoCursor<org.bson.Document> rs = collection.find().iterator();
            while (rs.hasNext()) {
                org.bson.Document doc = rs.next();
                int mapID = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                if (doc.getString("eff_event") == null) continue;
                JSONArray jar = new JSONArray(doc.getString("eff_event"));
                int len = jar.length();
                for (int j = 0; j < len; j++) {
                    JSONObject jobj = jar.getJSONObject(j);
                    int evID = jobj.getInt("event_id");
                    int effID = jobj.getInt("eff_id");
                    int layer = jobj.getInt("layer");
                    int x = jobj.getInt("x");
                    int y = jobj.getInt("y");
                    int loop = jobj.getInt("loop");
                    int delay = jobj.getInt("delay");

                    EffectEventTemplate ee = EffectEventTemplate.builder()
                            .mapId(mapID)
                            .eventId(evID)
                            .effId(effID)
                            .layer(layer)
                            .x(x)
                            .y(y)
                            .loop(loop)
                            .delay(delay)
                            .build();
                    add(ee);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(EffectEventTemplate ee) {
        list.add(ee);
    }
}
