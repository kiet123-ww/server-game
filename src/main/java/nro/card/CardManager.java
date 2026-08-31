/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.card;

import nro.jdbc.MongoDBConnection;
import nro.models.item.ItemOption;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 */
public class CardManager {

    private static final CardManager instance = new CardManager();

    public static CardManager getInstance() {
        return instance;
    }

    @Getter
    private final List<CardTemplate> cardTemplates = new ArrayList<>();

    public void load() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("collection_book");
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    int id = (short) (int) rs.getInteger("id", 0);
                    int itemID = (short) (int) rs.getInteger("item_id", 0);
                    String name = rs.getString("name");
                    String info = rs.getString("info");
                    byte maxAmount = (byte) (int) rs.getInteger("max_amount", 0);
                    short icon = (short) (int) rs.getInteger("icon", 0);
                    byte rank = (byte) (int) rs.getInteger("rank", 0);
                    byte type = (byte) (int) rs.getInteger("type", 0);
                    short mobID = (short) (int) rs.getInteger("mob_id", 0);
                    short head = (short) (int) rs.getInteger("head", 0);
                    short body = (short) (int) rs.getInteger("body", 0);
                    short leg = (short) (int) rs.getInteger("leg", 0);
                    short bag = (short) (int) rs.getInteger("bag", 0);
                    short aura = (short) (int) rs.getInteger("aura", 0);
                    ArrayList<ItemOption> options = new ArrayList<>();
                    JSONArray jArr = new JSONArray(rs.getString("options"));
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject obj = jArr.getJSONObject(i);
                        int oID = obj.getInt("id");
                        int oParam = obj.getInt("param");
                        int active_card = obj.getInt("active_card");
                        ItemOption itemOption = new ItemOption(oID, oParam);
                        itemOption.activeCard = (byte) active_card;
                        options.add(itemOption);
                    }
                    CardTemplate card = CardTemplate.builder()
                            .id(id)
                            .name(name)
                            .itemID(itemID)
                            .info(info)
                            .maxAmount(maxAmount)
                            .icon(icon)
                            .rank(rank)
                            .type(type)
                            .mobID(mobID)
                            .head(head)
                            .body(body)
                            .leg(leg)
                            .bag(bag)
                            .aura(aura)
                            .options(options)
                            .build();
                    add(card);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(CardTemplate cardTemplate) {
        cardTemplates.add(cardTemplate);
    }

    public void remove(CardTemplate cardTemplate) {
        cardTemplates.add(cardTemplate);
    }

    public CardTemplate find(int id) {
        for (CardTemplate card : cardTemplates) {
            if (card.getId() == id) {
                return card;
            }
        }
        return null;
    }
}
