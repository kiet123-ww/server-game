package nro.jdbc.daos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.InterfaceAddress;
import nro.card.Card;
import nro.card.CollectionBook;
import nro.consts.ConstAchive;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.jdbc.DBService;
import nro.manager.AchiveManager;
import nro.manager.PetFollowManager;
import nro.models.player.PetFollow;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemTime;
import nro.models.npc.specialnpc.MabuEgg;
import nro.models.npc.specialnpc.MagicTree;
import nro.models.player.*;
import nro.models.skill.Skill;
import nro.models.task.Achivement;
import nro.models.task.AchivementTemplate;
import nro.models.task.TaskMain;
import nro.server.Client;
import nro.server.Manager;
import nro.server.io.Session;
import nro.server.model.AntiLogin;
import nro.services.*;
import nro.utils.SkillUtil;
import nro.utils.TimeUtil;
import nro.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nro.jdbc.MongoDBConnection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ❤Girlkun75❤
 * @copyright ❤YTB KhanhDTK❤
 */
public class GodGK {
    // ttest da sua

    public static boolean login(Session session, AntiLogin al) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("account");
            Document rs = collection.find(Filters.and(Filters.eq("username", session.uu), Filters.eq("password", session.pp))).first();
            if (rs != null) {
                session.userId = rs.getInteger("id", 0); // Note: Assuming "id" instead of "account.id"
                Session plInGame = Client.gI().getSession(session);
                if (plInGame != null) {
                    Service.getInstance().sendThongBaoOK(plInGame, "Máy chủ tắt hoặc mất sóng!");
                    Client.gI().kickSession(plInGame);
                    Service.getInstance().sendThongBaoOK(session, "Máy chủ tắt hoặc mất sóng!");
                    return false;
                }

                session.isAdmin = (rs.get("is_admin") != null && (rs.get("is_admin").equals(1) || rs.get("is_admin").equals(true)));
                session.lastTimeLogout = (rs.get("last_time_logout") instanceof java.util.Date ? ((java.util.Date) rs.get("last_time_logout")).getTime() : 0L);
                session.actived = (rs.get("active") != null && (rs.get("active").equals(1) || rs.get("active").equals(true)));
                session.goldBar = rs.getInteger("thoi_vang", 0);
                session.vndBar = rs.getInteger("vnd", 0);
                session.tongnap = rs.getInteger("tongnap", 0);
                session.dataReward = rs.getString("reward");
                if ((rs.get("last_time_login") instanceof java.util.Date ? ((java.util.Date) rs.get("last_time_login")).getTime() : 0L) > session.lastTimeLogout) {
                    Service.getInstance().sendThongBaoOK(session, "Tài khoản đang đăng nhập máy chủ khác!");
                    return false;
                }
                if ((rs.get("ban") != null && (rs.get("ban").equals(1) || rs.get("ban").equals(true)))) {
                    Service.getInstance().sendThongBaoOK(session, "Tài khoản đã bị khóa do vi phạm điều khoản!");
                } else {
                    long lastTimeLogout = (rs.get("last_time_logout") instanceof java.util.Date ? ((java.util.Date) rs.get("last_time_logout")).getTime() : 0L);
                    int secondsPass = (int) ((System.currentTimeMillis() - lastTimeLogout) / 1000);
                    if (secondsPass < Manager.SECOND_WAIT_LOGIN && !session.isAdmin) {
                        Service.getInstance().sendThongBaoOK(session, "Vui lòng chờ "
                                + (Manager.SECOND_WAIT_LOGIN - secondsPass) + " giây để đăng nhập lại.");
                    }
                }
                al.reset();
                return true;
            } else {
                Service.getInstance().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không chính xác");
                al.wrong();
                // Anti login
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Player loadPlayer(Session session) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("player");
            Document rs = collection.find(Filters.eq("account_id", session.userId)).first();
            try {
                if (rs != null) {
                    int plHp = 200000000;
                    int plMp = 200000000;
                    JSONValue jv = new JSONValue();
                    JSONArray dataArray = null;
                    JSONObject dataObject = null;

                    Player player = new Player();

                    // base info
                    player.id = rs.getInteger("id", 0);
                    player.name = rs.getString("name");
                    player.head = (short) (int) rs.getInteger("head", 0);
                    player.gender = (byte) (int) rs.getInteger("gender", 0);
                    player.haveTennisSpaceShip = (rs.get("have_tennis_space_ship") != null && (rs.get("have_tennis_space_ship").equals(1) || rs.get("have_tennis_space_ship").equals(true)));

                    int clanId = rs.getInteger("clan_id_sv" + Manager.SERVER, -1);
                    if (clanId != -1) {
                        Clan clan = ClanService.gI().getClanById(clanId);
                        if (clan != null) {
                            for (ClanMember cm : clan.getMembers()) {
                                if (cm.id == player.id) {
                                    clan.addMemberOnline(player);
                                    player.clan = clan;
                                    player.clanMember = cm;
                                    player.setBuff(clan.getBuff());
                                    break;
                                }
                            }
                        }
                    }
                    // diem su kien
                    int evPoint = rs.getInteger("event_point", 0);
                    player.event.setEventPoint(evPoint);

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("sk_tet") == null ? "[]" : rs.getString("sk_tet"));
                    if (dataArray != null && dataArray.size() >= 5) {
                        int timeBanhTet = Integer.parseInt(dataArray.get(0).toString());
                        int timeBanhChung = Integer.parseInt(dataArray.get(1).toString());
                        boolean isNauBanhTet = Integer.parseInt(dataArray.get(2).toString()) == 1;
                        boolean isNauBanhChung = Integer.parseInt(dataArray.get(3).toString()) == 1;
                        boolean receivedLuckMoney = Integer.parseInt(dataArray.get(4).toString()) == 1;

                        player.event.setTimeCookTetCake(timeBanhTet);
                        player.event.setTimeCookChungCake(timeBanhChung);
                        player.event.setCookingTetCake(isNauBanhTet);
                        player.event.setCookingChungCake(isNauBanhChung);
                        player.event.setReceivedLuckyMoney(receivedLuckMoney);
                    }
                    if (dataArray != null) dataArray.clear();

                    // data kim lượng
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("data_inventory") == null ? "[]" : rs.getString("data_inventory"));
                    if (dataArray != null && dataArray.size() >= 3) {
                        player.inventory.gold = Long.parseLong(dataArray.get(0).toString());
                        player.inventory.gem = Integer.parseInt(dataArray.get(1).toString());
                        player.inventory.ruby = Integer.parseInt(dataArray.get(2).toString());
                        if (dataArray.size() >= 4) {
                            player.inventory.goldLimit = Long.parseLong(dataArray.get(3).toString());
                        }
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("dhtime") == null ? "[]" : rs.getString("dhtime"));
                    if (dataArray != null && dataArray.size() >= 6) {
                        player.isTitleUse = Integer.parseInt(String.valueOf(dataArray.get(0))) == 1 ? true : false;
                        player.lastTimeTitle1 = Long.parseLong(String.valueOf(dataArray.get(1)));
                        player.IdDanhHieu_1 = Integer.parseInt(String.valueOf(dataArray.get(2)));
                        player.ChiSoHP_1 = Integer.parseInt(String.valueOf(dataArray.get(3)));
                        player.ChiSoKI_1 = Integer.parseInt(String.valueOf(dataArray.get(4)));
                        player.ChiSoSD_1 = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("dhtime2") == null ? "[]" : rs.getString("dhtime2"));
                    if (dataArray != null && dataArray.size() >= 6) {
                        player.isTitleUse2 = Integer.parseInt(String.valueOf(dataArray.get(0))) == 1 ? true : false;
                        player.lastTimeTitle2 = Long.parseLong(String.valueOf(dataArray.get(1)));
                        player.IdDanhHieu_2 = Integer.parseInt(String.valueOf(dataArray.get(2)));
                        player.ChiSoHP_2 = Integer.parseInt(String.valueOf(dataArray.get(3)));
                        player.ChiSoKI_2 = Integer.parseInt(String.valueOf(dataArray.get(4)));
                        player.ChiSoSD_2 = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("dhtime3") == null ? "[]" : rs.getString("dhtime3"));
                    if (dataArray != null && dataArray.size() >= 6) {
                        player.isTitleUse3 = Integer.parseInt(String.valueOf(dataArray.get(0))) == 1 ? true : false;
                        player.lastTimeTitle3 = Long.parseLong(String.valueOf(dataArray.get(1)));
                        player.IdDanhHieu_3 = Integer.parseInt(String.valueOf(dataArray.get(2)));
                        player.ChiSoHP_3 = Integer.parseInt(String.valueOf(dataArray.get(3)));
                        player.ChiSoKI_3 = Integer.parseInt(String.valueOf(dataArray.get(4)));
                        player.ChiSoSD_3 = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("dhtime4") == null ? "[]" : rs.getString("dhtime4"));
                    if (dataArray != null && dataArray.size() >= 6) {
                        player.isTitleUse4 = Integer.parseInt(String.valueOf(dataArray.get(0))) == 1 ? true : false;
                        player.lastTimeTitle4 = Long.parseLong(String.valueOf(dataArray.get(1)));
                        player.IdDanhHieu_4 = Integer.parseInt(String.valueOf(dataArray.get(2)));
                        player.ChiSoHP_4 = Integer.parseInt(String.valueOf(dataArray.get(3)));
                        player.ChiSoKI_4 = Integer.parseInt(String.valueOf(dataArray.get(4)));
                        player.ChiSoSD_4 = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("dhtime5") == null ? "[]" : rs.getString("dhtime5"));
                    if (dataArray != null && dataArray.size() >= 6) {
                        player.isTitleUse5 = Integer.parseInt(String.valueOf(dataArray.get(0))) == 1 ? true : false;
                        player.lastTimeTitle5 = Long.parseLong(String.valueOf(dataArray.get(1)));
                        player.IdDanhHieu_5 = Integer.parseInt(String.valueOf(dataArray.get(2)));
                        player.ChiSoHP_5 = Integer.parseInt(String.valueOf(dataArray.get(3)));
                        player.ChiSoKI_5 = Integer.parseInt(String.valueOf(dataArray.get(4)));
                        player.ChiSoSD_5 = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("killWhis") == null ? "[]" : rs.getString("killWhis"));
                    if (dataArray != null && dataArray.size() >= 3) {
                        player.lastTimeSwapWhis = Long.parseLong(String.valueOf(dataArray.get(0)));
                        player.lastTimeKillWhis = Long.parseLong(String.valueOf(dataArray.get(1)));
                        player.levelKillWhis = Integer.parseInt(String.valueOf(dataArray.get(2)));
                    }
                    if (dataArray != null) dataArray.clear();

                    dataArray = (JSONArray) jv.parse(rs.getString("MaBaoVe") == null ? "[]" : rs.getString("MaBaoVe"));
                    if (dataArray != null && dataArray.size() >= 2) {
                        player.isUseMaBaoVe = Integer.parseInt(String.valueOf(dataArray.get(0))) == 1 ? true : false;
                        player.MaBaoVe = Integer.parseInt(String.valueOf(dataArray.get(1)));
                    }
                    if (dataArray != null) dataArray.clear();

                    Object lkw = rs.get("levelKillWhis");
                    player.levelKillWhisDone = lkw != null ? Integer.parseInt(lkw.toString()) : 0;

                    Object tkw = rs.get("timeKillWhis");
                    player.timeKillWhis = tkw != null ? Long.parseLong(tkw.toString()) : 0L;

                    player.event.setDiemTichLuy(session.diemTichNap);

                    dataArray = (JSONArray) jv.parse(rs.getString("checkNhanQua") == null ? "[]" : rs.getString("checkNhanQua"));
                    if (dataArray != null && dataArray.size() >= 2) {
                        player.event.luotNhanNgocMienPhi = Integer.parseInt(String.valueOf(dataArray.get(0)));
                        player.event.luotNhanBuaMienPhi = Integer.parseInt(String.valueOf(dataArray.get(1)));
                    }
                    if (dataArray != null) dataArray.clear();

                    player.event.setMocNapDaNhan(rs.getInteger("moc_nap", 0));

                    player.server = session.server;
                    // data tọa độ
                    try {
                        dataArray = (JSONArray) jv.parse(rs.getString("data_location") == null ? "[]" : rs.getString("data_location"));
                        if (dataArray != null && dataArray.size() >= 3) {
                            player.location.x = Integer.parseInt(dataArray.get(0).toString());
                            player.location.y = Integer.parseInt(dataArray.get(1).toString());
                            int mapId = Integer.parseInt(dataArray.get(2).toString());
                            if (MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBlackBallWar(mapId)
                                    || MapService.gI().isMapBanDoKhoBau(mapId) || mapId == 126
                                    || mapId == ConstMap.CON_DUONG_RAN_DOC
                                    || mapId == ConstMap.CON_DUONG_RAN_DOC_142 || mapId == ConstMap.CON_DUONG_RAN_DOC_143
                                    || mapId == ConstMap.HOANG_MAC) {
                                mapId = player.gender + 21;
                                player.location.x = 300;
                                player.location.y = 336;
                            } else if (MapService.gI().isMapKhiGas(mapId)) {
                                mapId = 5;
                                player.location.x = 106;
                                player.location.y = 228;
                            }
                            player.zone = MapService.gI().getMapCanJoin(player, mapId);
                        } else {
                            player.location.x = 300;
                            player.location.y = 336;
                            player.zone = MapService.gI().getMapCanJoin(player, player.gender + 21);
                        }
                    } catch (Exception e) {
                        player.location.x = 300;
                        player.location.y = 336;
                        player.zone = MapService.gI().getMapCanJoin(player, player.gender + 21);
                        e.printStackTrace();
                    }
                    if (dataArray != null) dataArray.clear();

                    // data chỉ số
                    dataArray = (JSONArray) jv.parse(rs.getString("data_point") == null ? "[]" : rs.getString("data_point"));
                    if (dataArray != null && dataArray.size() >= 13) {
                        plMp = Integer.parseInt(dataArray.get(1).toString());
                        player.nPoint.mpg = Integer.parseInt(dataArray.get(2).toString());
                        player.nPoint.critg = Byte.parseByte(dataArray.get(3).toString());
                        player.nPoint.limitPower = Byte.parseByte(dataArray.get(4).toString());
                        player.nPoint.stamina = Short.parseShort(dataArray.get(5).toString());
                        plHp = Integer.parseInt(dataArray.get(6).toString());
                        player.nPoint.defg = Integer.parseInt(dataArray.get(7).toString());
                        player.nPoint.tiemNang = Long.parseLong(dataArray.get(8).toString());
                        player.nPoint.maxStamina = Short.parseShort(dataArray.get(9).toString());
                        player.nPoint.dameg = Integer.parseInt(dataArray.get(10).toString());
                        player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
                        player.nPoint.hpg = Integer.parseInt(dataArray.get(12).toString());
                    }
                    if (dataArray != null) dataArray.clear();

                    // data đậu thần
                    dataArray = (JSONArray) jv.parse(rs.getString("data_magic_tree") == null ? "[]" : rs.getString("data_magic_tree"));
                    if (dataArray != null && dataArray.size() >= 5) {
                        boolean isUpgrade = Byte.parseByte(dataArray.get(0).toString()) == 1;
                        long lastTimeUpgrade = Long.parseLong(dataArray.get(1).toString());
                        byte level = Byte.parseByte(dataArray.get(2).toString());
                        long lastTimeHarvest = Long.parseLong(dataArray.get(3).toString());
                        byte currPea = Byte.parseByte(dataArray.get(4).toString());
                        player.magicTree = new MagicTree(player, level, currPea, lastTimeHarvest, isUpgrade,
                                lastTimeUpgrade);
                    } else {
                        player.magicTree = new MagicTree(player, (byte) 1, (byte) 5, 0, false, 0);
                    }
                    if (dataArray != null) dataArray.clear();

                    // data phần thưởng sao đen
                    dataArray = (JSONArray) jv.parse(rs.getString("data_black_ball") == null ? "[]" : rs.getString("data_black_ball"));
                    if (dataArray != null) {
                        for (int i = 0; i < dataArray.size() && i < player.rewardBlackBall.timeOutOfDateReward.length; i++) {
                            JSONArray reward = (JSONArray) jv.parse(String.valueOf(dataArray.get(i)));
                            if (reward != null && reward.size() >= 2) {
                                player.rewardBlackBall.timeOutOfDateReward[i] = Long.parseLong(reward.get(0).toString());
                                player.rewardBlackBall.lastTimeGetReward[i] = Long.parseLong(reward.get(1).toString());
                            }
                            if (reward != null) reward.clear();
                        }
                        dataArray.clear();
                    }

                    // data body
                    dataArray = (JSONArray) jv.parse(rs.getString("items_body") == null ? "[]" : rs.getString("items_body"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item = null;
                        dataObject = (JSONObject) dataArray.get(i);
                        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId,
                                    Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                            JSONArray options = (JSONArray) dataObject.get("option");
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) options.get(j);
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
                        player.inventory.itemsBody.add(item);
                    }
                    while (player.inventory.itemsBody.size() < 13) {
                        player.inventory.itemsBody.add(ItemService.gI().createItemNull());
                    }
                    dataArray.clear();
                    dataObject.clear();

                    try {
                        dataArray = (JSONArray) jv.parse(rs.getString("items_bag") == null ? "[]" : rs.getString("items_bag"));
                        for (int i = 0; i < dataArray.size(); i++) {
                            Item item = null;
                            dataObject = (JSONObject) dataArray.get(i);
                            short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                            if (tempId != -1) {
                                item = ItemService.gI().createNewItem(tempId,
                                        Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                                JSONArray options = (JSONArray) dataObject.get("option");
                                for (int j = 0; j < options.size(); j++) {
                                    JSONArray opt = (JSONArray) options.get(j);
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
                            player.inventory.itemsBag.add(item);
                        }
                        dataArray.clear();
                        dataObject.clear();
                    } catch (Exception e) {
                        System.out.println("Lỗi hành trang hành trang người chơi");
                        e.printStackTrace();
                    }

                    // data box
                    dataArray = (JSONArray) jv.parse(rs.getString("items_box") == null ? "[]" : rs.getString("items_box"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item = null;
                        dataObject = (JSONObject) dataArray.get(i);
                        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId,
                                    Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                            JSONArray options = (JSONArray) dataObject.get("option");
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) options.get(j);
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

                        player.inventory.itemsBox.add(item);
                    }
                    dataArray.clear();
                    dataObject.clear();

                    // data box lucky round
                    dataArray = (JSONArray) jv.parse(rs.getString("items_box_lucky_round") == null ? "[]" : rs.getString("items_box_lucky_round"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item = null;
                        dataObject = (JSONObject) dataArray.get(i);
                        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId,
                                    Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                            JSONArray options = (JSONArray) dataObject.get("option");
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) options.get(j);
                                item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                        Integer.parseInt(String.valueOf(opt.get(1)))));
                            }
                        } else {
                            item = ItemService.gI().createItemNull();
                        }
                        player.inventory.itemsBoxCrackBall.add(item);
                    }
                    dataArray.clear();
                    dataObject.clear();

                    // data friends
                    dataArray = (JSONArray) jv.parse(rs.getString("friends") == null ? "[]" : rs.getString("friends"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        dataObject = (JSONObject) dataArray.get(i);
                        Friend friend = new Friend();
                        friend.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                        friend.name = String.valueOf(dataObject.get("name"));
                        friend.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                        friend.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                        friend.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                        friend.bag = Byte.parseByte(String.valueOf(dataObject.get("bag")));
                        friend.power = Long.parseLong(String.valueOf(dataObject.get("power")));
                        player.friends.add(friend);
                        dataObject.clear();
                    }
                    dataArray.clear();

                    // data enemies
                    dataArray = (JSONArray) jv.parse(rs.getString("enemies") == null ? "[]" : rs.getString("enemies"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        dataObject = (JSONObject) dataArray.get(i);
                        Enemy enemy = new Enemy();
                        enemy.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                        enemy.name = String.valueOf(dataObject.get("name"));
                        enemy.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                        enemy.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                        enemy.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                        enemy.bag = Byte.parseByte(String.valueOf(dataObject.get("bag")));
                        enemy.power = Long.parseLong(String.valueOf(dataObject.get("power")));
                        player.enemies.add(enemy);
                        dataObject.clear();
                    }
                    dataArray.clear();

                    // data nội tại
                    dataArray = (JSONArray) jv.parse(rs.getString("data_intrinsic") == null ? "[]" : rs.getString("data_intrinsic"));
                    if (dataArray != null && dataArray.size() >= 4) {
                        byte intrinsicId = Byte.parseByte(dataArray.get(0).toString());
                        player.playerIntrinsic.intrinsic = IntrinsicService.gI().getIntrinsicById(intrinsicId);
                        player.playerIntrinsic.intrinsic.param1 = Short.parseShort(dataArray.get(1).toString());
                        player.playerIntrinsic.countOpen = Byte.parseByte(dataArray.get(2).toString());
                        player.playerIntrinsic.intrinsic.param2 = Short.parseShort(dataArray.get(3).toString());
                    } else {
                        player.playerIntrinsic.intrinsic = IntrinsicService.gI().getIntrinsicById((byte) 0);
                    }
                    if (dataArray != null) dataArray.clear();

                    // data item time
                    dataArray = (JSONArray) jv.parse(rs.getString("data_item_time") == null ? "[]" : rs.getString("data_item_time"));
                    int timeBoKhi = 0, timeAnDanh = 0, timeOpenPower = 0, timeCuongNo = 0, timeBoHuyet = 0, timeGiapXen = 0;
                    int timeMayDo = 0, timeMeal = 0, iconMeal = 0;
                    int timeBanhChung1 = 0, timeBanhTet1 = 0, timeBoKhi2 = 0, timeGiapXen2 = 0, timeCuongNo2 = 0, timeBoHuyet2 = 0, timeBiNgo = 0;
                    if (dataArray != null) {
                        try {
                            if (dataArray.size() > 0) timeBoKhi = Integer.parseInt(dataArray.get(0).toString());
                            if (dataArray.size() > 1) timeAnDanh = Integer.parseInt(dataArray.get(1).toString());
                            if (dataArray.size() > 2) timeOpenPower = Integer.parseInt(dataArray.get(2).toString());
                            if (dataArray.size() > 3) timeCuongNo = Integer.parseInt(dataArray.get(3).toString());
                            if (dataArray.size() > 4) timeMayDo = Integer.parseInt(dataArray.get(4).toString());
                            if (dataArray.size() > 5) timeBoHuyet = Integer.parseInt(dataArray.get(5).toString());
                            if (dataArray.size() > 6) iconMeal = Integer.parseInt(dataArray.get(6).toString());
                            if (dataArray.size() > 7) timeMeal = Integer.parseInt(dataArray.get(7).toString());
                            if (dataArray.size() > 8) timeGiapXen = Integer.parseInt(dataArray.get(8).toString());
                            if (dataArray.size() >= 15) {
                                timeBanhChung1 = Integer.parseInt(dataArray.get(9).toString());
                                timeBanhTet1 = Integer.parseInt(dataArray.get(10).toString());
                                timeBoKhi2 = Integer.parseInt(dataArray.get(11).toString());
                                timeGiapXen2 = Integer.parseInt(dataArray.get(12).toString());
                                timeCuongNo2 = Integer.parseInt(dataArray.get(13).toString());
                                timeBoHuyet2 = Integer.parseInt(dataArray.get(14).toString());
                            }
                            if (dataArray.size() >= 16) {
                                timeBiNgo = Integer.parseInt(dataArray.get(15).toString());
                            }
                        } catch (Exception e) {
                        }
                        dataArray.clear();
                    }
                    
                    player.itemTime.lastTimeBoHuyet = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet);
                    player.itemTime.lastTimeBoKhi = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoKhi);
                    player.itemTime.lastTimeGiapXen = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeGiapXen);
                    player.itemTime.lastTimeCuongNo = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeCuongNo);
                    player.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet2);
                    player.itemTime.lastTimeBoKhi2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoKhi2);
                    player.itemTime.lastTimeGiapXen2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeGiapXen2);
                    player.itemTime.lastTimeCuongNo2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeCuongNo2);
                    player.itemTime.lastTimeAnDanh = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeAnDanh);
                    player.itemTime.lastTimeOpenPower = System.currentTimeMillis()
                            - (ItemTime.TIME_OPEN_POWER - timeOpenPower);
                    player.itemTime.lastTimeUseMayDo = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO - timeMayDo);
                    player.itemTime.lastTimeEatMeal = System.currentTimeMillis() - (ItemTime.TIME_EAT_MEAL - timeMeal);
                    player.itemTime.lastTimeBanhChung = System.currentTimeMillis()
                            - (ItemTime.TIME_EAT_MEAL - timeBanhChung1);
                    player.itemTime.lastTimeBanhTet = System.currentTimeMillis()
                            - (ItemTime.TIME_EAT_MEAL - timeBanhTet1);
                    player.itemTime.iconMeal = iconMeal;
                    player.itemTime.isUseBoHuyet = timeBoHuyet != 0;
                    player.itemTime.isUseBoKhi = timeBoKhi != 0;
                    player.itemTime.isUseGiapXen = timeGiapXen != 0;
                    player.itemTime.isUseCuongNo = timeCuongNo != 0;
                    player.itemTime.isUseBoHuyet2 = timeBoHuyet2 != 0;
                    player.itemTime.isUseBoKhi2 = timeBoKhi2 != 0;
                    player.itemTime.isUseGiapXen2 = timeGiapXen2 != 0;
                    player.itemTime.isUseCuongNo2 = timeCuongNo2 != 0;
                    player.itemTime.isUseAnDanh = timeAnDanh != 0;
                    player.itemTime.isOpenPower = timeOpenPower != 0;
                    player.itemTime.isUseMayDo = timeMayDo != 0;
                    player.itemTime.isEatMeal = timeMeal != 0;
                    player.itemTime.isUseBanhChung = timeBanhChung1 != 0;
                    player.itemTime.isUseBanhTet = timeBanhTet1 != 0;
                    
                    // BiNgo
                    player.effectSkill.isBiNgo = timeBiNgo != 0;
                    player.effectSkill.lastBiNgo = System.currentTimeMillis() - (30_000 - timeBiNgo);
                    // BiNgo
                    
                    // data nhiệm vụ
                    dataArray = (JSONArray) jv.parse(rs.getString("data_task") == null ? "[]" : rs.getString("data_task"));
                    if (dataArray != null && dataArray.size() >= 3) {
                        TaskMain taskMain = TaskService.gI().getTaskMainById(player,
                                Byte.parseByte(dataArray.get(1).toString()));
                        if (taskMain != null) {
                            int subTaskIdx = Integer.parseInt(dataArray.get(2).toString());
                            if (subTaskIdx >= 0 && subTaskIdx < taskMain.subTasks.size()) {
                                taskMain.subTasks.get(subTaskIdx).count = Short
                                        .parseShort(dataArray.get(0).toString());
                            }
                            taskMain.index = (byte) subTaskIdx;
                            player.playerTask.taskMain = taskMain;
                        }
                    }
                    if (dataArray != null) dataArray.clear();

                    // data nhiệm vụ hàng ngày
                    try {
                        dataArray = (JSONArray) jv.parse(rs.getString("data_side_task") == null ? "[]" : rs.getString("data_side_task"));
                        if (dataArray != null && dataArray.size() >= 6) {
                            String format = "dd-MM-yyyy";
                            long receivedTime = Long.parseLong(String.valueOf(dataArray.get(4)));
                            Date date = new Date(receivedTime);
                            if (TimeUtil.formatTime(date, format).equals(TimeUtil.formatTime(new Date(), format))) {
                                player.playerTask.sideTask.level = Integer
                                        .parseInt(String.valueOf(dataArray.get(0).toString()));
                                player.playerTask.sideTask.count = Integer.parseInt(dataArray.get(1).toString());
                                player.playerTask.sideTask.leftTask = Integer
                                        .parseInt(String.valueOf(dataArray.get(2).toString()));
                                player.playerTask.sideTask.template = TaskService.gI()
                                        .getSideTaskTemplateById(Integer.parseInt(dataArray.get(3).toString()));
                                player.playerTask.sideTask.maxCount = Integer
                                        .parseInt(String.valueOf(dataArray.get(5).toString()));
                                player.playerTask.sideTask.receivedTime = receivedTime;
                            }
                        }
                    } catch (Exception e) {
                    }

                    dataArray = (JSONArray) jv.parse(rs.getString("achivements") == null ? "[]" : rs.getString("achivements"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        dataObject = (JSONObject) jv.parse(String.valueOf(dataArray.get(i)));
                        Achivement achivement = new Achivement();
                        achivement.setId(Integer.parseInt(dataObject.get("id").toString()));
                        achivement.setCount(Integer.parseInt(dataObject.get("count").toString()));
                        achivement.setFinish(Integer.parseInt(dataObject.get("finish").toString()) == 1);
                        achivement.setReceive(Integer.parseInt(dataObject.get("receive").toString()) == 1);
                        AchivementTemplate a = AchiveManager.getInstance().findByID(achivement.getId());
                        if (a != null) {
                            achivement.setName(a.getName());
                            achivement.setDetail(a.getDetail());
                            achivement.setMaxCount(a.getMaxCount());
                            achivement.setMoney(a.getMoney());
                            player.playerTask.achivements.add(achivement);
                        }
                    }

                    List<AchivementTemplate> listAchivements = AchiveManager.getInstance().getList();
                    if (dataArray.size() < listAchivements.size()) { // add thêm nhiệm vụ khi có nhiệm vụ mới
                        for (int i = dataArray.size(); i < listAchivements.size(); i++) {
                            AchivementTemplate a = AchiveManager.getInstance().findByID(i);
                            Achivement achivement = new Achivement();
                            if (a != null) {
                                achivement.setId(a.getId());
                                achivement.setCount(0);
                                achivement.setFinish(false);
                                achivement.setReceive(false);
                                achivement.setName(a.getName());
                                achivement.setDetail(a.getDetail());
                                achivement.setMaxCount(a.getMaxCount());
                                achivement.setMoney(a.getMoney());
                                player.playerTask.achivements.add(achivement);
                            }
                        }
                    }
                    dataArray.clear();

                    // data trứng bư
                    dataObject = (JSONObject) jv.parse(rs.getString("data_mabu_egg") == null ? "{}" : rs.getString("data_mabu_egg"));
                    Object createTime = dataObject.get("create_time");
                    if (createTime != null) {
                        player.mabuEgg = new MabuEgg(player, Long.parseLong(String.valueOf(createTime)),
                                Long.parseLong(String.valueOf(dataObject.get("time_done"))));
                    }
                    dataObject.clear();

                    // data bùa
                    dataArray = (JSONArray) jv.parse(rs.getString("data_charm") == null ? "[]" : rs.getString("data_charm"));
                    if (dataArray != null && dataArray.size() >= 10) {
                        player.charms.tdTriTue = Long.parseLong(dataArray.get(0).toString());
                        player.charms.tdManhMe = Long.parseLong(dataArray.get(1).toString());
                        player.charms.tdDaTrau = Long.parseLong(dataArray.get(2).toString());
                        player.charms.tdOaiHung = Long.parseLong(dataArray.get(3).toString());
                        player.charms.tdBatTu = Long.parseLong(dataArray.get(4).toString());
                        player.charms.tdDeoDai = Long.parseLong(dataArray.get(5).toString());
                        player.charms.tdThuHut = Long.parseLong(dataArray.get(6).toString());
                        player.charms.tdDeTu = Long.parseLong(dataArray.get(7).toString());
                        player.charms.tdTriTue3 = Long.parseLong(dataArray.get(8).toString());
                        player.charms.tdTriTue4 = Long.parseLong(dataArray.get(9).toString());
                        if (dataArray.size() >= 11) {
                            player.charms.tdDeTuMabu = Long.parseLong(dataArray.get(10).toString());
                        }
                    }
                    if (dataArray != null) dataArray.clear();

                    // data skill
                    dataArray = (JSONArray) jv.parse(rs.getString("skills") == null ? "[]" : rs.getString("skills"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray skillTemp = (JSONArray) jv.parse(String.valueOf(dataArray.get(i)));
                        int tempId = Integer.parseInt(skillTemp.get(0).toString());
                        byte point = Byte.parseByte(skillTemp.get(2).toString());
                        Skill skill = null;
                        if (point != 0) {
                            skill = SkillUtil.createSkill(tempId, point);
                        } else {
                            skill = SkillUtil.createSkillLevel0(tempId);
                        }
                        skill.lastTimeUseThisSkill = Long.parseLong(skillTemp.get(1).toString());
                        player.playerSkill.skills.add(skill);
                        skillTemp.clear();
                    }
                    dataArray.clear();

                    // data skill shortcut
                    dataArray = (JSONArray) jv.parse(rs.getString("skills_shortcut") == null ? "[]" : rs.getString("skills_shortcut"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        player.playerSkill.skillShortCut[i] = Byte.parseByte(String.valueOf(dataArray.get(i)));
                    }
                    for (int i : player.playerSkill.skillShortCut) {
                        if (player.playerSkill.getSkillbyId(i) != null
                                && player.playerSkill.getSkillbyId(i).damage > 0) {
                            player.playerSkill.skillSelect = player.playerSkill.getSkillbyId(i);
                            break;
                        }
                    }
                    if (player.playerSkill.skillSelect == null) {
                        player.playerSkill.skillSelect = player.playerSkill
                                .getSkillbyId(player.gender == ConstPlayer.TRAI_DAT
                                        ? Skill.DRAGON
                                        : (player.gender == ConstPlayer.NAMEC ? Skill.DEMON : Skill.GALICK));
                    }
                    dataArray.clear();

                    Gson gson = new Gson();
                    List<Card> cards = gson.fromJson(rs.getString("collection_book"), new TypeToken<List<Card>>() {
                    }.getType());

                    CollectionBook book = new CollectionBook(player);
                    if (cards != null) {
                        book.setCards(cards);
                    } else {
                        book.setCards(new ArrayList<>());
                    }
                    book.init();
                    player.setCollectionBook(book);
                    List<Item> itemsBody = player.inventory.itemsBody;

                    if (itemsBody.size() > 11 && itemsBody.get(11).isNotNullItem()) {
                        MiniPet.callMiniPet(player, (player.inventory.itemsBody.get(11).template.id));
                    }

                    if (itemsBody.size() > 10 && itemsBody.get(10).isNotNullItem()) {
                        PetFollow pet = PetFollowManager.gI().findByID(itemsBody.get(10).getId());
                        if (pet != null) {
                            player.setPetFollow(pet);
                            PlayerService.gI().sendPetFollow(player);
                        }
                    }

                    Object ftlObj = rs.get("firstTimeLogin");
                    if (ftlObj instanceof java.util.Date) {
                        player.firstTimeLogin = (java.util.Date) ftlObj;
                    } else if (ftlObj != null) {
                        Date d = Util.getDate(ftlObj.toString());
                        if (d != null) {
                            player.firstTimeLogin = d;
                        } else {
                            try {
                                player.firstTimeLogin = java.sql.Timestamp.valueOf(ftlObj.toString());
                            } catch (Exception e) {
                                player.firstTimeLogin = new java.util.Date();
                            }
                        }
                    } else {
                        player.firstTimeLogin = new java.util.Date();
                    }

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("buy_limit") == null ? "[]" : rs.getString("buy_limit"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        player.buyLimit[i] = Byte.parseByte(dataArray.get(i).toString());
                    }

                    dataArray = (JSONArray) JSONValue.parse(rs.getString("reward_limit") == null ? "[]" : rs.getString("reward_limit"));

                    player.rewardLimit = new byte[dataArray.size()];
                    for (int i = 0; i < dataArray.size(); i++) {
                        player.rewardLimit[i] = Byte.parseByte(dataArray.get(i).toString());
                    }

                    // dhvt23
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("challenge") == null ? "[]" : rs.getString("challenge"));
                    if (dataArray != null && dataArray.size() >= 4) {
                        player.goldChallenge = Integer.parseInt(dataArray.get(0).toString());
                        player.levelWoodChest = Integer.parseInt(dataArray.get(1).toString());
                        player.receivedWoodChest = Integer.parseInt(dataArray.get(2).toString()) == 1;
                        player.gemChallenge = Integer.parseInt(dataArray.get(3).toString());
                    }
                    if (dataArray != null) dataArray.clear();

                    PlayerService.gI().dailyLogin(player);

                    // data pet
                    dataObject = (JSONObject) jv.parse(rs.getString("pet_info") == null ? "{}" : rs.getString("pet_info"));
                    if (dataObject != null && !dataObject.isEmpty() && dataObject.containsKey("gender")) {
                        Pet pet = new Pet(player);
                        pet.id = -player.id;
                        pet.gender = Byte.parseByte(String.valueOf(dataObject.get("gender")));
                        pet.isMabu = dataObject.get("is_mabu") != null && Byte.parseByte(String.valueOf(dataObject.get("is_mabu"))) == 1;
                        pet.isBulo = dataObject.get("is_Bulo") != null && Byte.parseByte(String.valueOf(dataObject.get("is_Bulo"))) == 1;
                        pet.isCellBao = dataObject.get("is_CellBao") != null && Byte.parseByte(String.valueOf(dataObject.get("is_CellBao"))) == 1;
                        pet.isBillNhi = dataObject.get("is_BillNhi") != null && Byte.parseByte(String.valueOf(dataObject.get("is_BillNhi"))) == 1;
                        pet.isFideTrau = dataObject.get("is_FideTrau") != null && Byte.parseByte(String.valueOf(dataObject.get("is_FideTrau"))) == 1;
                        pet.isSuperPicolo = dataObject.get("is_SuperPicolo") != null && Byte.parseByte(String.valueOf(dataObject.get("is_SuperPicolo"))) == 1;
                        pet.name = String.valueOf(dataObject.get("name"));
                        player.fusion.typeFusion = dataObject.get("type_fusion") != null ? Byte.parseByte(String.valueOf(dataObject.get("type_fusion"))) : 0;
                        int leftFusion = dataObject.get("left_fusion") != null ? Integer.parseInt(String.valueOf(dataObject.get("left_fusion"))) : 0;
                        player.fusion.lastTimeFusion = System.currentTimeMillis() - (Fusion.TIME_FUSION - leftFusion);
                        pet.status = dataObject.get("status") != null ? Byte.parseByte(String.valueOf(dataObject.get("status"))) : 0;

                        // data chỉ số
                        JSONObject petPointObj = (JSONObject) jv.parse(rs.getString("pet_point") == null ? "{}" : rs.getString("pet_point"));
                        if (petPointObj != null && !petPointObj.isEmpty() && petPointObj.containsKey("hpg")) {
                            pet.nPoint.stamina = petPointObj.get("stamina") != null ? Short.parseShort(String.valueOf(petPointObj.get("stamina"))) : 1000;
                            pet.nPoint.maxStamina = petPointObj.get("max_stamina") != null ? Short.parseShort(String.valueOf(petPointObj.get("max_stamina"))) : 1000;
                            pet.nPoint.hpg = petPointObj.get("hpg") != null ? Integer.parseInt(String.valueOf(petPointObj.get("hpg"))) : 2000;
                            pet.nPoint.mpg = petPointObj.get("mpg") != null ? Integer.parseInt(String.valueOf(petPointObj.get("mpg"))) : 2000;
                            pet.nPoint.dameg = petPointObj.get("damg") != null ? Integer.parseInt(String.valueOf(petPointObj.get("damg"))) : 100;
                            pet.nPoint.defg = petPointObj.get("defg") != null ? Integer.parseInt(String.valueOf(petPointObj.get("defg"))) : 0;
                            pet.nPoint.critg = petPointObj.get("critg") != null ? Integer.parseInt(String.valueOf(petPointObj.get("critg"))) : 0;
                            pet.nPoint.power = petPointObj.get("power") != null ? Long.parseLong(String.valueOf(petPointObj.get("power"))) : 2000;
                            pet.nPoint.tiemNang = petPointObj.get("tiem_nang") != null ? Long.parseLong(String.valueOf(petPointObj.get("tiem_nang"))) : 2000;
                            pet.nPoint.limitPower = petPointObj.get("limit_power") != null ? Byte.parseByte(String.valueOf(petPointObj.get("limit_power"))) : 0;
                            int hp = petPointObj.get("hp") != null ? Integer.parseInt(String.valueOf(petPointObj.get("hp"))) : pet.nPoint.hpg;
                            int mp = petPointObj.get("mp") != null ? Integer.parseInt(String.valueOf(petPointObj.get("mp"))) : pet.nPoint.mpg;
                            pet.nPoint.hp = hp;
                            pet.nPoint.mp = mp;
                        }

                        // data body
                        dataArray = (JSONArray) jv.parse(rs.getString("pet_body") == null ? "[]" : rs.getString("pet_body"));
                        if (dataArray != null) {
                            for (int i = 0; i < dataArray.size(); i++) {
                                Item item = null;
                                dataObject = (JSONObject) dataArray.get(i);
                                short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                                if (tempId != -1) {
                                    item = ItemService.gI().createNewItem(tempId,
                                            Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                                    JSONArray options = (JSONArray) dataObject.get("option");
                                    for (int j = 0; j < options.size(); j++) {
                                        JSONArray opt = (JSONArray) options.get(j);
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
                                pet.inventory.itemsBody.add(item);
                            }
                            dataArray.clear();
                        }

                        // data skills
                        dataArray = (JSONArray) jv.parse(rs.getString("pet_skill") == null ? "[]" : rs.getString("pet_skill"));
                        if (dataArray != null) {
                            for (int i = 0; i < dataArray.size(); i++) {
                                JSONArray skillTemp = (JSONArray) dataArray.get(i);
                                int tempId = Integer.parseInt(String.valueOf(skillTemp.get(0)));
                                byte point = Byte.parseByte(String.valueOf(skillTemp.get(1)));
                                Skill skill = null;
                                if (point != 0) {
                                    skill = SkillUtil.createSkill(tempId, point);
                                } else {
                                    skill = SkillUtil.createSkillLevel0(tempId);
                                }
                                switch (skill.template.id) {
                                    case Skill.KAMEJOKO:
                                    case Skill.MASENKO:
                                    case Skill.ANTOMIC:
                                        skill.coolDown = 1000;
                                        break;
                                }
                                pet.playerSkill.skills.add(skill);
                            }
                            dataArray.clear();
                        }
                        player.pet = pet;
                    }
                    if (session.ruby > 0) {
                        player.inventory.ruby += session.ruby;
                        if (player.playerTask.achivements.size() > ConstAchive.LAN_DAU_NAP_NGOC) {
                            player.playerTask.achivements.get(ConstAchive.LAN_DAU_NAP_NGOC).count += session.ruby;
                        }
                        PlayerDAO.subRuby(player, session.userId, session.ruby);
                    }
                    player.nPoint.hp = plHp;
                    player.nPoint.mp = plMp;
                    session.player = player;

                    MongoCollection<Document> accColl = MongoDBConnection.getDatabase().getCollection("account");
                    accColl.updateOne(Filters.eq("id", session.userId), com.mongodb.client.model.Updates.combine(
                            com.mongodb.client.model.Updates.set("last_time_login", new Date()),
                            com.mongodb.client.model.Updates.set("ip_address", session.ipAddress)
                    ));

                    MongoCollection<Document> pColl = MongoDBConnection.getDatabase().getCollection("player");
                    pColl.updateOne(Filters.eq("account_id", session.userId), com.mongodb.client.model.Updates.combine(
                            com.mongodb.client.model.Updates.set("lastimelogin", new Date()),
                            com.mongodb.client.model.Updates.set("tongnap", session.tongnap)
                    ));

                    return player;
                }
            } finally {
                // connection handled by mongo driver automatically
            }
        } catch (Exception ex) {
            System.err.println("[ERROR in GodGK.loadPlayer for userId=" + session.userId + "]: " + ex.getMessage());
            ex.printStackTrace();
            session.dataLoadFailed = true;
        }
        return null;
    }
}




