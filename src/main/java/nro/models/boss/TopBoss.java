package nro.models.boss;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nro.jdbc.daos.PlayerDAO;
import nro.models.player.Player;
import nro.services.Service;
import nro.utils.Log;

/**
 *
 * @author YTB KhanhDTK
 */
public class TopBoss {

    public static void update(Player pl) {
        //Luồng
        ExecutorService executor = Executors.newFixedThreadPool(1);
        //Cộng điểm
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", pl.getSession().userId), com.mongodb.client.model.Updates.inc("TopBoss", 1));
            Service.getInstance().sendThongBao(pl, "Bạn nhận được 1 điểm giết boss ");
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lỗi update top boss cho người chơi " + pl.name);
        }
         
    }
    public static void updatediemnoel(Player pl) {
        //Luồng
        ExecutorService executor = Executors.newFixedThreadPool(1);
        //Cộng điểm
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", pl.getSession().userId), com.mongodb.client.model.Updates.inc("TopNoel", 1));
            Service.getInstance().sendThongBao(pl, "Bạn nhận được 1 điểm sự kiện noel ");
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lỗi update top boss cho người chơi " + pl.name);
        }
    }
}
