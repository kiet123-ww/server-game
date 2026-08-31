package nro.manager;

import nro.jdbc.MongoDBConnection;
import nro.models.kigui.KiGuiItem;
import nro.models.kigui.KiGuiShop;
import nro.models.item.ItemOption;
import nro.services.ItemService;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class KiGuiManager {

    private static final KiGuiManager INSTANCE = new KiGuiManager();

    public static KiGuiManager getInstance() {
        return INSTANCE;
    }

    public void load() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("consignment_shop");
            JSONArray jsonArray = null;
            JSONValue jsonValue = new JSONValue();
            KiGuiShop consignmentShop = KiGuiShop.getInstance();
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    short itemID = (short) (int) rs.getInteger("item_id", 0);
                    int quantity = rs.getInteger("quantity", 0);
                    KiGuiItem item = ItemService.gI().createNewConsignmentItem(itemID, quantity);
                    Long consignorId = rs.getLong("consignor_id");
                    item.setConsignorID(consignorId != null ? consignorId : 0L);
                    item.setTab((byte) (int) rs.getInteger("tab", 0));
                    item.setPriceGold(rs.getInteger("gold", 0));
                    item.setPriceGem(rs.getInteger("gem", 0));
                    item.setUpTop(rs.getBoolean("up_top", false));
                    item.setSold(rs.getInteger("sold", 0));
                    Long timeConsign = rs.getLong("time_consign");
                    item.createTime = timeConsign != null ? timeConsign : 0L;
                    item.setConsignName(rs.getString("consignor_name"));
                    item.setSuKien(rs.getBoolean("su_kien", false));
                    jsonArray = (JSONArray) jsonValue.parse(rs.getString("item_options"));

                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONArray opt = (JSONArray) jsonValue.parse(String.valueOf(jsonArray.get(j)));
                        item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    consignmentShop.addItem(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("consignment_shop");
            collection.drop();
            
            int id = 0;
            List<KiGuiItem> list = KiGuiShop.getInstance().getList();
            List<Document> documents = new ArrayList<>();
            for (KiGuiItem it : list) {
                if (it != null) {
                    JSONArray options = new JSONArray();
                    for (ItemOption io : it.itemOptions) {
                        JSONArray option = new JSONArray();
                        option.add(io.optionTemplate.id);
                        option.add(io.param);
                        options.add(option);
                    }
                    Document doc = new Document();
                    doc.put("id", id++);
                    doc.put("consignor_id", it.getConsignorID());
                    doc.put("tab", it.getTab());
                    doc.put("item_id", it.template.id);
                    doc.put("gold", it.getPriceGold());
                    doc.put("gem", it.getPriceGem());
                    doc.put("quantity", it.quantity);
                    doc.put("item_options", options.toJSONString());
                    doc.put("up_top", it.isUpTop());
                    doc.put("sold", it.getSold());
                    doc.put("time_consign", it.createTime);
                    doc.put("consignor_name", it.getConsignName());
                    doc.put("su_kien", it.isSuKien());
                    documents.add(doc);
                }
            }
            if (!documents.isEmpty()) {
                collection.insertMany(documents);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
