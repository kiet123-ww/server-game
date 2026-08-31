/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nro.power;

import nro.jdbc.DBService;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author Kitak
 */
public class PowerLimitManager {

    private static final PowerLimitManager instance = new PowerLimitManager();

    public static PowerLimitManager getInstance() {
        return instance;
    }

    @Getter
    private List<PowerLimit> powers;

    public PowerLimitManager() {
        powers = new ArrayList<>();
    }

    public void load() {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("power_limit");
            com.mongodb.client.MongoCursor<org.bson.Document> rs = collection.find().iterator();
            while (rs.hasNext()) {
                org.bson.Document doc = rs.next();
                int id = (doc.getInteger("id") != null ? (short) (int) doc.getInteger("id") : 0);
                long power = (doc.getLong("power") != null ? doc.getLong("power") : 0L);
                int hp = (doc.getInteger("hp") != null ? doc.getInteger("hp") : 0);
                int mp = (doc.getInteger("mp") != null ? doc.getInteger("mp") : 0);
                int damage = (doc.getInteger("damage") != null ? doc.getInteger("damage") : 0);
                int defense = (doc.getInteger("defense") != null ? doc.getInteger("defense") : 0);
                int critical = (doc.getInteger("critical") != null ? doc.getInteger("critical") : 0);
                PowerLimit powerLimit = PowerLimit.builder()
                        .id(id)
                        .power(power)
                        .hp(hp)
                        .mp(mp)
                        .damage(damage)
                        .defense(defense)
                        .critical(critical)
                        .build();
                add(powerLimit);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(PowerLimit powerLimit) {
        powers.add(powerLimit);
    }

    public void remove(PowerLimit powerLimit) {
        powers.remove(powerLimit);
    }

    public PowerLimit get(int index) {
        if (index < 0 || index >= powers.size()) {
            return null;
        }
        return powers.get(index);
    }
}
