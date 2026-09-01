package nro.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import nro.jdbc.MongoDBConnection;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.utils.Util;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class GiftService {

    private static GiftService i;

    private GiftService() {

    }

    public static GiftService gI() {
        if (i == null) {
            i = new GiftService();
        }
        return i;
    }

    public void use(Player player, String code) {
        int lent = code.length();
        if (code.equals("") || lent < 5 || lent > 30) {
            Service.getInstance().sendThongBaoOK(player, "Mã quà tặng có chiều dài từ 5 đến 30 ký tự.");
            return;
        }
        Pattern p = Pattern.compile("^[a-zA-Z0-9]+$");
        Matcher m1 = p.matcher(code);
        if (!m1.find()) {
            Service.getInstance().sendThongBaoOK(player, "Mã quà tặng chỉ gồm chữ và số.");
            return;
        }
        code = code.toLowerCase();
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("gift_codes");
            Document res = collection.find(Filters.eq("code", code)).first();
            
            if (res == null) {
                Service.getInstance().sendThongBaoOK(player, "Mã quà tặng không tồn tại hoặc đã hết hạn.");
                return;
            }

            Object expObj = res.get("expires_at");
            Date expiresAt = null;
            if (expObj instanceof Date) {
                expiresAt = (Date) expObj;
            } else if (expObj != null) {
                try {
                    expiresAt = java.sql.Timestamp.valueOf(expObj.toString());
                } catch (Exception e) {}
            }
            if (expiresAt != null && expiresAt.before(new Date())) {
                Service.getInstance().sendThongBaoOK(player, "Mã quà tặng không tồn tại hoặc đã hết hạn.");
                return;
            }

            int id = res.getInteger("id", res.getInteger("_id", 0));
            Number statusNum = (Number) res.get("status");
            byte status = statusNum != null ? statusNum.byteValue() : 0;
            
            Number typeNum = (Number) res.get("type");
            byte type = typeNum != null ? typeNum.byteValue() : 0;
            
            Object activeObj = res.get("active");
            boolean active = activeObj != null && (activeObj.equals(1) || activeObj.equals(true) || "1".equals(activeObj.toString()) || "true".equalsIgnoreCase(activeObj.toString()));

            if (status == 1) {
                Service.getInstance().sendThongBaoOK(player, "Mã quà tặng đã được sử dụng");
                return;
            } else if (type == 1 && isUsedGiftCode((int) player.id, id)) {
                Service.getInstance().sendThongBaoOK(player, "Mỗi người chỉ được sử dụng 1 lần.");
                return;
            } else if (active && !player.getSession().actived) {
                Service.getInstance().sendThongBao(player, "Cần kích hoạt tài khoản để nhận mã quà tặng này");
                return;
            }

            Number goldNum = (Number) res.get("gold");
            int gold = goldNum != null ? goldNum.intValue() : 0;
            
            Number gemNum = (Number) res.get("gem");
            int gem = gemNum != null ? gemNum.intValue() : 0;
            
            Number rubyNum = (Number) res.get("ruby");
            int ruby = rubyNum != null ? rubyNum.intValue() : 0;

            String itemsStr = res.getString("items");
            if (itemsStr == null) itemsStr = "[]";
            JSONArray arrItem = new JSONArray(itemsStr);
            int size = arrItem.length();

            if (size > InventoryService.gI().getCountEmptyBag(player)) {
                Service.getInstance().sendThongBaoOK(player, "Bạn không đủ chỗ trống trong hành trang.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("|7|Bạn nhận được").append("\b");
            for (int j = 0; j < size; j++) {
                JSONObject itemObj = (JSONObject) arrItem.get(j);
                int itemID = itemObj.getInt("id");
                int quantity = itemObj.getInt("quantity");
                JSONArray options = itemObj.getJSONArray("options");

                Item item = ItemService.gI().createNewItem((short) itemID, quantity);
                for (int k = 0; k < options.length(); k++) {
                    JSONObject obj = options.getJSONObject(k);
                    int optionID = obj.getInt("id");
                    int param = obj.getInt("param");
                    item.itemOptions.add(new ItemOption(optionID, param));
                }
                item.createTime = System.currentTimeMillis();
                InventoryService.gI().addItemBag(player, item, 0);
                sb.append(String.format("|2|%s %s", Util.numberToMoney(quantity), item.template.name)).append("\b");
            }

            if (gold > 0) {
                player.inventory.addGold(gold);
                sb.append(String.format("- %s vàng", Util.numberToMoney(gold))).append("\b");
            }

            if (gem > 0) {
                player.inventory.gem += gem;
                sb.append(String.format("- %s ngọc xanh", Util.numberToMoney(gem))).append("\b");
            }

            if (ruby > 0) {
                player.inventory.ruby += ruby;
                sb.append(String.format("- %s hồng ngọc", Util.numberToMoney(ruby))).append("\b");
            }
            Service.getInstance().sendMoney(player);
            InventoryService.gI().sendItemBags(player);
            String text = sb.toString();
            String[] arr = text.split("\\\b");
            StringBuilder sb2 = new StringBuilder();
            for (int j = 0; j < arr.length; j++) {
                sb2.append(arr[j]);
                if (j % 10 == 0 && j != 0 && j != arr.length - 1) {
                    sb2.append("\n");
                } else {
                    sb2.append("\b");
                }
            }
            NpcService.gI().createMenuConMeo(player, -1, -1, sb2.toString(), "OK");
            addUsedGiftCode((int) player.id, id, code);
            if (type == 0) {
                collection.updateOne(Filters.eq("code", code), Updates.combine(
                        Updates.set("status", (byte) 1),
                        Updates.set("updated_at", new Date())
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUsedGiftCode(int playerID, int giftCodeId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("gift_code_histories");
            Document res = collection.find(Filters.and(
                    Filters.eq("gift_code_id", giftCodeId),
                    Filters.eq("player_id", playerID)
            )).first();
            return res != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addUsedGiftCode(int playerID, int giftCodeId, String code) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("gift_code_histories");
            Document doc = new Document("player_id", playerID)
                    .append("gift_code_id", giftCodeId)
                    .append("code", code)
                    .append("created_at", new Date());
            collection.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

