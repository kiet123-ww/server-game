package nro.jdbc.daos;

import nro.jdbc.MongoDBConnection;
import nro.models.item.ItemOption;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.shop.TabShop;
import nro.services.ItemService;
import nro.utils.Log;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class ShopDAO {

    public static List<Shop> getShops() {
        List<Shop> list = new ArrayList<>();
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("shop");
            try (MongoCursor<Document> cursor = collection.find().sort(Sorts.ascending("npc_id", "shop_order")).iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    Shop shop = new Shop();
                    shop.id = rs.getInteger("id", 0);
                    shop.npcId = (byte) (int) rs.getInteger("npc_id", 0);
                    shop.shopOrder = (byte) (int) rs.getInteger("shop_order", 0);
                    loadShopTab(shop);
                    list.add(shop);
                }
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
        return list;
    }

    private static void loadShopTab(Shop shop) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("tab_shop");
            try (MongoCursor<Document> cursor = collection.find(Filters.eq("shop_id", shop.id)).sort(Sorts.ascending("id")).iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    TabShop tab = new TabShop();
                    tab.shop = shop;
                    tab.id = rs.getInteger("id", 0);
                    tab.name = rs.getString("name") != null ? rs.getString("name").replaceAll("<>", "\n") : "";
                    loadItemShop(tab);
                    shop.tabShops.add(tab);
                }
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
    }

    private static void loadItemShop(TabShop tabShop) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("item_shop");
            try (MongoCursor<Document> cursor = collection.find(Filters.and(Filters.eq("is_sell", 1), Filters.eq("tab_id", tabShop.id)))
                    .sort(Sorts.descending("create_time")).iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    ItemShop itemShop = new ItemShop();
                    itemShop.tabShop = tabShop;
                    itemShop.id = rs.getInteger("id", 0);
                    itemShop.temp = ItemService.gI().getTemplate((short) (int) rs.getInteger("temp_id", 0));
                    itemShop.gold = rs.getInteger("gold", 0);
                    itemShop.gem = rs.getInteger("gem", 0);
                    itemShop.isNew = rs.getBoolean("is_new", false);
                    itemShop.itemExchange = rs.getInteger("item_exchange", -1);
                    if (itemShop.itemExchange != -1) {
                        itemShop.iconSpec = ItemService.gI().getTemplate(itemShop.itemExchange).iconID;
                        itemShop.costSpec = rs.getInteger("quantity_exchange", 0);
                    }
                    // thay đổi vật phẩm trong shop
                    loadItemShopOption(itemShop);
                    tabShop.itemShops.add(itemShop);
                }
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
    }

    private static void loadItemShopOption(ItemShop itemShop) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("item_shop_option");
            try (MongoCursor<Document> cursor = collection.find(Filters.eq("item_shop_id", itemShop.id)).iterator()) {
                while (cursor.hasNext()) {
                    Document rs = cursor.next();
                    itemShop.options.add(new ItemOption(rs.getInteger("option_id", 0), rs.getInteger("param", 0)));
                }
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
    }

}
