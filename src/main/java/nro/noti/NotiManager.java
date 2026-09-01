package nro.noti;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import nro.jdbc.MongoDBConnection;

import nro.consts.Cmd;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class NotiManager {

    private static final NotiManager INSTANCE = new NotiManager();

    public static NotiManager getInstance() {
        return INSTANCE;
    }

    private static List<Notification> notifications = new ArrayList<Notification>();
    private static Alert alert;

    public void load() {
        loadNoti();
        loadAlert();
    }

    public void loadNoti() {
        try {
            notifications.clear();
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("notifications");
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Notification notification = new Notification();
                Object idObj = doc.get("id");
                notification.setId(idObj != null ? Integer.parseInt(idObj.toString()) : 0);
                notification.setContent(doc.getString("content"));
                notification.setTitle(doc.getString("title"));
                addNoti(notification);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadAlert() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("alert");
            Document doc = collection.find().first();
            if (doc != null) {
                Alert a = new Alert();
                a.content = doc.getString("content");
                this.alert = a;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addNoti(Notification noti) {
        notifications.add(noti);
    }

    public void sendAlert(Player player) {
        Service.getInstance().sendThongBaoFromAdmin(player, alert.content);
    }

    public void sendDanhQuaiNhanNgoc(Player player) {
        if (player.event.luotNhanNgocMienPhi == 1) {
            Service.getInstance().sendThongBao(player, "Hôm nay bạn sẽ nhận được từ 1 đến 2 viên ngọc khi tiêu diệt 1 con quái");
        }
    }

    public void sendNoti(Player player) {
        Message m = new Message(Cmd.GAME_INFO);
        try {
            DataOutputStream ds = m.writer();
            ds.writeByte(notifications.size());
            for (Notification notification : notifications) {
                ds.writeShort(notification.getId());
                ds.writeUTF(notification.getTitle());
                ds.writeUTF(notification.getContent());
            }
            ds.flush();
            player.sendMessage(m);
            m.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
