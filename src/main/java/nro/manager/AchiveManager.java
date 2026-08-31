package nro.manager;

import lombok.Getter;
import nro.jdbc.DBService;
import nro.models.task.AchivementTemplate;




import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class AchiveManager implements IManager<AchivementTemplate> {

    private static final AchiveManager INSTANCE = new AchiveManager();

    public static AchiveManager getInstance() {
        return INSTANCE;
    }

    @Getter
    private List<AchivementTemplate> list = new ArrayList<>();

    public void load() {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("achivements");
            com.mongodb.client.MongoCursor<org.bson.Document> rs = collection.find().iterator();
            while (rs.hasNext()) {
                org.bson.Document doc = rs.next();
                int id = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                String name = doc.getString("name");
                String detail = doc.getString("detail");
                int money = (doc.getInteger("money") != null ? doc.getInteger("money") : 0);
                int maxCount = (doc.getInteger("max_count") != null ? doc.getInteger("max_count") : 0);
                list.add(new AchivementTemplate(id,name,detail,money,maxCount));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public AchivementTemplate findByID(int id) {
        for (AchivementTemplate template : list) {
            if (template.getId() == id) {
                return template;
            }
        }
        return null;
    }

    @Override
    public void add(AchivementTemplate achivementTemplate) {

    }

    @Override
    public void remove(AchivementTemplate achivementTemplate) {

    }
}