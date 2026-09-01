/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nro.power;

import nro.jdbc.DBService;
import nro.models.player.Player;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author Kitak
 */
public class CaptionManager {

    private static final CaptionManager instance = new CaptionManager();

    public static CaptionManager getInstance() {
        return instance;
    }

    @Getter
    private List<Caption> captions;

    public CaptionManager() {
        captions = new ArrayList<>();
    }

    public void load() {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("caption");
            com.mongodb.client.MongoCursor<org.bson.Document> rs = collection.find().iterator();
            while (rs.hasNext()) {
                org.bson.Document doc = rs.next();
                int id = (doc.getInteger("id") != null ? (short) (int) doc.getInteger("id") : 0);
                String earth = doc.getString("earth");
                String saiya = doc.getString("saiya");
                String namek = doc.getString("namek");
                Number powerNum = (Number) doc.get("power");
                long power = powerNum != null ? powerNum.longValue() : 0L;
                Caption caption = Caption.builder()
                        .id(id)
                        .earth(earth)
                        .saiya(saiya)
                        .namek(namek)
                        .power(power)
                        .build();
                add(caption);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(Caption caption) {
        captions.add(caption);
    }

    public void remove(Caption caption) {
        captions.remove(caption);
    }

    public Caption find(int id) {
        for (Caption caption : captions) {
            if (caption.getId() == id) {
                return caption;
            }
        }
        return null;
    }

    public int getLevel(Player player) {
        try {
            long power = player.nPoint.power;
            int size = captions.size();
            int level = 0;
            for (int i = size - 1; i >= 0; i--) {
                long p = captions.get(i).getPower();
                if (power >= p) {
                    level = i;
                    break;
                }
            }
            return level;
        } catch (Exception e) {

        }
        return 0;
    }
}
