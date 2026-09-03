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
import com.mongodb.client.model.Sorts;

import java.util.ArrayList;
import java.util.List;

public class TopManager {

    @Getter
    private List<Player> list = new ArrayList<>();

    @Getter
    private List<Player> listTask = new ArrayList<>();

    @Getter
    private List<Player> listVnd = new ArrayList<>();
    
    @Getter
    private List<Player> listNangDong = new ArrayList<>();
    
    private static final TopManager INSTANCE = new TopManager();

    public static TopManager getInstance() {
        return INSTANCE;
    }

    public void load() {
        list.clear();
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("player");
            try (MongoCursor<Document> cursor = collection.find().sort(Sorts.descending("power")).limit(20).iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    JSONArray dataArray;
                    JSONObject dataObject = null;

                    Player player = new Player();

                    player.id = rs.getInteger("id", 0);
                    player.name = rs.getString("name");
                    player.head = (short) (int) rs.getInteger("head", 0);
                    player.gender = (byte) (int) rs.getInteger("gender", 0);

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("data_point") == null ? "[]" : rs.getString("data_point"));
                    if (dataArray != null && dataArray.size() >= 12) {
                        player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body") == null ? "[]" : rs.getString("items_body"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item;
                        dataObject = (JSONObject) JSONValue.parse(dataArray.get(i).toString());
                        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                            JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                        Integer.parseInt(String.valueOf(opt.get(1)))));
                            }
                            Object ct = dataObject.get("create_time");
                            item.createTime = ct != null ? Long.parseLong(String.valueOf(ct)) : 0L;
                            if (ItemService.gI().isOutOfDateTime(item)) {
                                item = ItemService.gI().createItemNull();
                            }
                        } else {
                            item = ItemService.gI().createItemNull();
                        }
                        player.inventory.itemsBody.add(item);
                    }
                    dataArray.clear();
                    if (dataObject != null) {
                        dataObject.clear();
                    }

                    list.add(player);
                }
            }
        } catch (Exception e) {
            Log.error(TopManager.class, e);
        }
    }

    public void loadTopNvu() {
        listTask.clear();
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("player");
            List<Document> allDocs = new ArrayList<>();
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) allDocs.add(cursor.next());
            }
            
            allDocs.sort((d1, d2) -> {
                String task1 = d1.getString("data_task");
                String task2 = d2.getString("data_task");
                int nv1 = 0, nv2 = 0;
                int nv3_1 = 0, nv3_2 = 0;
                try {
                    String[] parts1 = task1.split(",");
                    if (parts1.length > 1) nv1 = Integer.parseInt(parts1[1]);
                    if (parts1.length > 2) nv3_1 = Integer.parseInt(parts1[2]);
                } catch(Exception e){}
                try {
                    String[] parts2 = task2.split(",");
                    if (parts2.length > 1) nv2 = Integer.parseInt(parts2[1]);
                    if (parts2.length > 2) nv3_2 = Integer.parseInt(parts2[2]);
                } catch(Exception e){}
                
                if (nv1 != nv2) return Integer.compare(nv2, nv1);
                if (nv3_1 != nv3_2) return Integer.compare(nv3_2, nv3_1);
                
                long p1 = 0, p2 = 0;
                try {
                    JSONArray dataArray1 = (JSONArray) JSONValue.parse(d1.getString("data_point"));
                    p1 = Long.parseLong(dataArray1.get(11).toString());
                } catch(Exception e){}
                try {
                    JSONArray dataArray2 = (JSONArray) JSONValue.parse(d2.getString("data_point"));
                    p2 = Long.parseLong(dataArray2.get(11).toString());
                } catch(Exception e){}
                return Long.compare(p2, p1);
            });
            
            for (int k = 0; k < Math.min(20, allDocs.size()); k++) {
                Document rs = allDocs.get(k);
                JSONArray dataArray;
                JSONObject dataObject = null;

                Player player = new Player();

                player.id = rs.getInteger("id", 0);
                player.name = rs.getString("name");
                player.head = (short) (int) rs.getInteger("head", 0);
                player.gender = (byte) (int) rs.getInteger("gender", 0);
                
                String task = rs.getString("data_task");
                int nv = 0;
                try {
                    if (task != null) {
                        String[] parts = task.split(",");
                        if (parts.length > 1) nv = Integer.parseInt(parts[1]);
                    }
                } catch (Exception e) {}
                player.topTask = (byte) nv;

                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_point") == null ? "[]" : rs.getString("data_point"));
                if (dataArray != null && dataArray.size() >= 12) {
                    player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
                }
                if (dataArray != null) dataArray.clear();

                dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body") == null ? "[]" : rs.getString("items_body"));
                for (int i = 0; i < dataArray.size(); i++) {
                    Item item;
                    dataObject = (JSONObject) JSONValue.parse(dataArray.get(i).toString());
                    short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                        JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));
                        for (int j = 0; j < options.size(); j++) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                            item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                        Object ct = dataObject.get("create_time");
                        item.createTime = ct != null ? Long.parseLong(String.valueOf(ct)) : 0L;
                        if (ItemService.gI().isOutOfDateTime(item)) {
                            item = ItemService.gI().createItemNull();
                        }
                    } else {
                        item = ItemService.gI().createItemNull();
                    }
                    player.inventory.itemsBody.add(item);
                }
                dataArray.clear();
                if (dataObject != null) {
                    dataObject.clear();
                }

                listTask.add(player);
            }
        } catch (Exception e) {
            Log.error(TopManager.class, e);
        }
    }

    public void loadTopNangDong() {
        listNangDong.clear();
        try {
            MongoCollection<Document> accColl = MongoDBConnection.getDatabase().getCollection("account");
            MongoCollection<Document> playerColl = MongoDBConnection.getDatabase().getCollection("player");
            
            // Note: Since NangDong is stored as string/int, we can just sort natively or in memory.
            List<Document> allAccounts = new ArrayList<>();
            try (MongoCursor<Document> cursor = accColl.find().iterator()) {
                while (cursor.hasNext()) allAccounts.add(cursor.next());
            }
            
            allAccounts.sort((d1, d2) -> {
                Object nd1 = d1.get("NangDong");
                Object nd2 = d2.get("NangDong");
                int val1 = 0, val2 = 0;
                try { if (nd1 != null) val1 = Integer.parseInt(nd1.toString()); } catch(Exception e){}
                try { if (nd2 != null) val2 = Integer.parseInt(nd2.toString()); } catch(Exception e){}
                return Integer.compare(val2, val1);
            });
            
            int count = 0;
            for (Document acc : allAccounts) {
                if (count >= 20) break;
                int accId = acc.getInteger("id", 0);
                Document rs = playerColl.find(com.mongodb.client.model.Filters.eq("account_id", accId)).first();
                if (rs != null) {
                    JSONArray dataArray;
                    JSONObject dataObject = null;

                    Player player = new Player();

                    player.id = rs.getInteger("id", 0);
                    player.name = rs.getString("name");
                    player.head = (short) (int) rs.getInteger("head", 0);
                    player.gender = (byte) (int) rs.getInteger("gender", 0);
                    
                    Object nd = acc.get("NangDong");
                    player.topNangDong = nd != null ? Integer.parseInt(nd.toString()) : 0;

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("data_point") == null ? "[]" : rs.getString("data_point"));
                    player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
                    dataArray.clear();

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body") == null ? "[]" : rs.getString("items_body"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item;
                        dataObject = (JSONObject) JSONValue.parse(dataArray.get(i).toString());
                        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                            JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                        Integer.parseInt(String.valueOf(opt.get(1)))));
                            }
                            Object ct = dataObject.get("create_time");
                            item.createTime = ct != null ? Long.parseLong(String.valueOf(ct)) : 0L;
                            if (ItemService.gI().isOutOfDateTime(item)) {
                                item = ItemService.gI().createItemNull();
                            }
                        } else {
                            item = ItemService.gI().createItemNull();
                        }
                        player.inventory.itemsBody.add(item);
                    }
                    dataArray.clear();
                    if (dataObject != null) {
                        dataObject.clear();
                    }

                    listNangDong.add(player);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.error(TopManager.class, e);
        }
    }

    public void loadTopVnd() {
        listVnd.clear();
        try {
            MongoCollection<Document> accColl = MongoDBConnection.getDatabase().getCollection("account");
            MongoCollection<Document> playerColl = MongoDBConnection.getDatabase().getCollection("player");
            
            List<Document> allAccounts = new ArrayList<>();
            try (MongoCursor<Document> cursor = accColl.find().iterator()) {
                while (cursor.hasNext()) allAccounts.add(cursor.next());
            }
            
            allAccounts.sort((d1, d2) -> {
                Object nd1 = d1.get("tongnap");
                Object nd2 = d2.get("tongnap");
                int val1 = 0, val2 = 0;
                try { if (nd1 != null) val1 = Integer.parseInt(nd1.toString()); } catch(Exception e){}
                try { if (nd2 != null) val2 = Integer.parseInt(nd2.toString()); } catch(Exception e){}
                return Integer.compare(val2, val1);
            });
            
            int count = 0;
            for (Document acc : allAccounts) {
                if (count >= 20) break;
                int accId = acc.getInteger("id", 0);
                Document rs = playerColl.find(com.mongodb.client.model.Filters.eq("account_id", accId)).first();
                if (rs != null) {
                    JSONArray dataArray;
                    JSONObject dataObject = null;

                    Player player = new Player();

                    player.id = rs.getInteger("id", 0);
                    player.name = rs.getString("name");
                    player.head = (short) (int) rs.getInteger("head", 0);
                    player.gender = (byte) (int) rs.getInteger("gender", 0);
                    
                    Object tongnap = acc.get("tongnap");
                    player.topVnd = tongnap != null ? Integer.parseInt(tongnap.toString()) : 0;

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("data_point") == null ? "[]" : rs.getString("data_point"));
                    player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
                    dataArray.clear();

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body") == null ? "[]" : rs.getString("items_body"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item;
                        dataObject = (JSONObject) JSONValue.parse(dataArray.get(i).toString());
                        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                            JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                        Integer.parseInt(String.valueOf(opt.get(1)))));
                            }
                            Object ct = dataObject.get("create_time");
                            item.createTime = ct != null ? Long.parseLong(String.valueOf(ct)) : 0L;
                            if (ItemService.gI().isOutOfDateTime(item)) {
                                item = ItemService.gI().createItemNull();
                            }
                        } else {
                            item = ItemService.gI().createItemNull();
                        }
                        player.inventory.itemsBody.add(item);
                    }
                    dataArray.clear();
                    if (dataObject != null) {
                        dataObject.clear();
                    }

                    listVnd.add(player);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.error(TopManager.class, e);
        }
    }
}

