package nro.manager;

import lombok.Getter;
import nro.jdbc.MongoDBConnection;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.services.ItemService;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class TopKillWhisManager {

    @Getter
    private List<Player> list = new ArrayList<>();
    private static final TopKillWhisManager INSTANCE = new TopKillWhisManager();

    public static TopKillWhisManager getInstance() {
        return INSTANCE;
    }

    public void load() {
    list.clear();

    try {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("player");
        try (MongoCursor<Document> cursor = collection.find(Filters.gt("levelKillWhis", 0))
                .sort(Sorts.orderBy(Sorts.descending("levelKillWhis"), Sorts.ascending("timeKillWhis")))
                .limit(100).iterator()) {
            while (cursor.hasNext()) {
                try {
                    Document rs = cursor.next();
                    Player player = extractPlayerFromDocument(rs);
                    if (player != null) {
                        list.add(player);
                    }
                } catch (Exception ex) {
                }
            }
        }
    } catch (Exception e) {
        Log.error(TopKillWhisManager.class, e);
    }
}


    private Player extractPlayerFromDocument(Document rs) {
        Player player = new Player();

        player.id = rs.getInteger("id", 0);
        player.name = rs.getString("name");
        player.head = (short) (int) rs.getInteger("head", 0);
        player.gender = (byte) (int) rs.getInteger("gender", 0);

        player.levelKillWhisDone = rs.getInteger("levelKillWhis", 0);
        Object tkw = rs.get("timeKillWhis");
        player.timeKillWhis = tkw != null ? Long.parseLong(tkw.toString()) : 0L;
        
        Object ltl = rs.get("lastimelogin");
        if (ltl instanceof Date) {
            player.lastimelogin = new java.sql.Timestamp(((Date) ltl).getTime());
        } else if (ltl != null) {
            try {
                player.lastimelogin = java.sql.Timestamp.valueOf(ltl.toString());
            } catch (Exception e) {}
        }
        
        extractDataPoint(rs.getString("data_point"), player);
        extractItemsBody(rs.getString("items_body"), player);

        return player;
    }

    private void extractDataPoint(String dataPoint, Player player) {
        if (dataPoint == null) return;
        try {
            JSONValue jv = new JSONValue();
            JSONArray dataArray = (JSONArray) jv.parse(dataPoint);
            if (dataArray != null && dataArray.size() >= 12) {
                player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
            }
            if (dataArray != null) dataArray.clear();
        } catch (Exception e) {
        }
    }

    private void extractItemsBody(String itemsBody, Player player) {
        if (itemsBody == null) return;
        try {
            JSONValue jv = new JSONValue();
            JSONArray dataArray = (JSONArray) jv.parse(itemsBody);
            if (dataArray != null) {
                for (Object itemDataObject : dataArray) {
                    Item item = createItemFromDataObject(itemDataObject.toString());
                    player.inventory.itemsBody.add(item);
                }
                dataArray.clear();
            }
        } catch (Exception e) {
        }
    }

    private Item createItemFromDataObject(String itemData) {
        JSONValue jv = new JSONValue();
        JSONObject dataObject = (JSONObject) jv.parse(itemData);
        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
        Item item;

        if (tempId != -1) {
            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
            JSONArray options = (JSONArray) jv.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));

            for (Object option : options) {
                JSONArray opt = (JSONArray) jv.parse(String.valueOf(option));
                item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                        Integer.parseInt(String.valueOf(opt.get(1)))));
            }

            item.createTime = Long.parseLong(String.valueOf(dataObject.get("create_time")));

            if (ItemService.gI().isOutOfDateTime(item)) {
                item = ItemService.gI().createItemNull();
            }
        } else {
            item = ItemService.gI().createItemNull();
        }

        return item;
    }
}
