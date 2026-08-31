package nro.jdbc.daos;

import nro.jdbc.MongoDBConnection;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.utils.Log;
import nro.utils.TimeUtil;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import java.util.Date;
import java.util.List;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class HistoryTransactionDAO {

    public static void insert(Player player1, Player player2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After,
            List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("history_transaction");
            String itemsTrade1 = "Gold: " + goldP1;
            String itemsTrade2 = "Gold: " + goldP2;
            for (Item item : itemP1) {
                itemsTrade1 += ", " + item.template.name + " (x" + item.quantity + ")";
            }
            for (Item item : itemP2) {
                itemsTrade2 += ", " + item.template.name + " (x" + item.quantity + ")";
            }

            String itemsBefore1 = "Gold: " + gold1Before + ", ";
            String itemsBefore2 = "Gold: " + gold2Before + ", ";
            for (Item item : bag1After) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsBefore1 += info;
                }
            }
            for (Item item : bag2Before) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsBefore2 += info;
                }
            }

            String itemsAfter1 = "Gold: " + gold1Before + ", ";
            String itemsAfter2 = "Gold: " + gold2Before + ", ";
            for (Item item : bag1After) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsAfter1 += info;
                }
            }
            for (Item item : bag2After) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsAfter2 += info;
                }
            }

            Document doc = new Document("player1", player1.name + " (" + player1.id + ")")
                    .append("player2", player2.name + " (" + player2.id + ")")
                    .append("itemsTrade1", itemsTrade1)
                    .append("itemsTrade2", itemsTrade2)
                    .append("itemsBefore1", itemsBefore1)
                    .append("itemsBefore2", itemsBefore2)
                    .append("itemsAfter1", itemsAfter1)
                    .append("itemsAfter2", itemsAfter2)
                    .append("time_tran", new Date());

            collection.insertOne(doc);
        } catch (Exception e) {
            Log.error(HistoryTransactionDAO.class, e);
        }
    }

    public static void deleteHistory() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("history_transaction");
            Date threshold = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(TimeUtil.getTimeBeforeCurrent(3 * 24 * 60 * 60 * 1000, "yyyy-MM-dd"));
            collection.deleteMany(Filters.lt("time_tran", threshold));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
