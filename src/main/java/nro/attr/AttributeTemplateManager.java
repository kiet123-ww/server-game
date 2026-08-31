/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.attr;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import nro.jdbc.MongoDBConnection;
import nro.utils.Log;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 */
public class AttributeTemplateManager {

    private static final AttributeTemplateManager instance = new AttributeTemplateManager();

    public static AttributeTemplateManager getInstance() {
        return instance;
    }

    private final List<AttributeTemplate> list = new ArrayList<>();

    public void load() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("attribute_template");
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                int id = doc.getInteger("id");
                String name = doc.getString("name");
                AttributeTemplate at = AttributeTemplate.builder()
                        .id(id)
                        .name(name)
                        .build();
                add(at);
            }
        } catch (Exception ex) {
            Log.error(AttributeTemplateManager.class, ex, "Load attribute template err");
        }
    }

    public void add(AttributeTemplate at) {
        list.add(at);
    }

    public void remove(AttributeTemplate at) {
        list.remove(at);
    }

    public AttributeTemplate find(int id) {
        for (AttributeTemplate at : list) {
            if (at.getId() == id) {
                return at;
            }
        }
        return null;
    }
}
