package nro.jdbc.daos;

import com.google.gson.Gson;
import nro.consts.ConstMap;
import nro.jdbc.DBService;
import nro.manager.AchiveManager;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemTime;
import nro.models.player.*;
import nro.models.skill.Skill;
import nro.models.task.Achivement;
import nro.models.task.AchivementTemplate;
import nro.server.Manager;
import nro.services.MapService;
import nro.utils.Log;
import nro.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Connection;


import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;
import nro.services.InventoryService;

/**
 * @author đŸ’– YTB KhanhDTK đŸ’–
 * 
 */
public class PlayerDAO {

    public static boolean updateTimeLogout;

    public static void createNewPlayer(int userId, String name, byte gender, int hair) {
        
        try {
            JSONArray dataInventory = new JSONArray();

            dataInventory.add(2000);
            dataInventory.add(10000);
            dataInventory.add(0);
            String inventory = dataInventory.toJSONString();

            JSONArray dataLocation = new JSONArray();
            dataLocation.add(100);
            dataLocation.add(384);
            dataLocation.add(39 + gender);
            String location = dataLocation.toJSONString();

            JSONArray dataPoint = new JSONArray();
            dataPoint.add(0);// nang dong
            dataPoint.add(gender == 1 ? 200 : 100);// mp
            dataPoint.add(gender == 1 ? 200 : 100);// mpg
            dataPoint.add(0);// critg
            dataPoint.add(0);// limitpower
            dataPoint.add(1000);// stamina
            dataPoint.add(gender == 0 ? 200 : 100);// hp
            dataPoint.add(0);// defg
            dataPoint.add(2000);// tn
            dataPoint.add(1000);// maxsta
            dataPoint.add(gender == 2 ? 15 : 10);// damg
            dataPoint.add(2000);// pow
            dataPoint.add(gender == 0 ? 200 : 100);// hpg
            String point = dataPoint.toJSONString();

            JSONArray dataMagicTree = new JSONArray();
            dataMagicTree.add(0);// isupgr
            dataMagicTree.add(new Date().getTime());
            dataMagicTree.add(1);// LV
            dataMagicTree.add(new Date().getTime());
            dataMagicTree.add(5);// curr_pea
            String magicTree = dataMagicTree.toJSONString();

            int idAo = gender == 0 ? 0 : gender == 1 ? 1 : 2;
            int idQuan = gender == 0 ? 6 : gender == 1 ? 7 : 8;
            int def = gender == 2 ? 3 : 2;
            int hp = gender == 0 ? 30 : 20;

            JSONArray dataBody = new JSONArray();
            for (int i = 0; i < 12; i++) {
                JSONObject item = new JSONObject();
                JSONArray options = new JSONArray();
                JSONArray option = new JSONArray();
                if (i == 0) {
                    option.add(47);
                    option.add(def);
                    options.add(option);
                    item.put("temp_id", idAo);
                    item.put("create_time", System.currentTimeMillis());
                    item.put("quantity", 1);
                } else if (i == 1) {
                    option.add(6);
                    option.add(hp);
                    options.add(option);
                    item.put("temp_id", idQuan);
                    item.put("create_time", System.currentTimeMillis());
                    item.put("quantity", 1);
                } else {
                    item.put("temp_id", -1);
                    item.put("create_time", 0);
                    item.put("quantity", 1);
                }
                item.put("option", options);
                dataBody.add(item);
            }
            String itemsBody = dataBody.toJSONString();

            JSONArray dataBag = new JSONArray();
            for (int i = 0; i < 20; i++) {
                JSONObject item = new JSONObject();
                JSONArray options = new JSONArray();
                JSONArray option = new JSONArray();
                if (i == 0) {
                    option.add(73);
                    option.add(1);
                    options.add(option);
                    item.put("temp_id", 457);
                    item.put("create_time", System.currentTimeMillis());
                    item.put("quantity", 10);
                } else {
                    item.put("temp_id", -1);
                    item.put("create_time", 0);
                    item.put("quantity", 1);
                }
                item.put("option", options);
                dataBag.add(item);
            }
            String itemsBag = dataBag.toJSONString();

            JSONArray dataBox = new JSONArray();
            for (int i = 0; i < 20; i++) {
                JSONObject item = new JSONObject();
                JSONArray options = new JSONArray();
                JSONArray option = new JSONArray();
                if (i == 0) {
                    item.put("temp_id", 12);
                    option.add(14);
                    option.add(1);
                    options.add(option);
                    item.put("create_time", System.currentTimeMillis());
                } else {
                    item.put("temp_id", -1);
                    item.put("create_time", 0);
                }
                item.put("option", options);
                item.put("quantity", 1);
                dataBox.add(item);
            }
            String itemsBox = dataBox.toJSONString();

            JSONArray dataLuckyRound = new JSONArray();
            for (int i = 0; i < 110; i++) {
                JSONObject item = new JSONObject();
                JSONArray options = new JSONArray();
                item.put("temp_id", -1);
                item.put("option", options);
                item.put("create_time", 0);
                item.put("quantity", 1);
                dataLuckyRound.add(item);
            }
            String itemsBoxLuckyRound = dataLuckyRound.toJSONString();

            String friends = "[]";
            String enemies = "[]";

            JSONArray dataIntrinsic = new JSONArray();
            dataIntrinsic.add(0);
            dataIntrinsic.add(0);
            dataIntrinsic.add(0);
            dataIntrinsic.add(0);
            String intrinsic = dataIntrinsic.toJSONString();

            JSONArray dataItemTime = new JSONArray();
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            dataItemTime.add(0);
            String itemTime = dataItemTime.toJSONString();

            JSONArray dataTask = new JSONArray();
            dataTask.add(0);
            dataTask.add(20);
            dataTask.add(0);
            String task = dataTask.toJSONString();

            JSONArray dataAchive = new JSONArray();
            for (AchivementTemplate a : AchiveManager.getInstance().getList()) {
                JSONObject jobj = new JSONObject();
                jobj.put("id", a.getId());
                jobj.put("count", 0);
                jobj.put("finish", 0);
                jobj.put("receive", 0);
                dataAchive.add(jobj);
            }
            String achive = dataAchive.toJSONString();

            String mabuEgg = "{}";

            JSONArray dataCharms = new JSONArray();
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            dataCharms.add(0);
            String charms = dataCharms.toJSONString();

            int[] skillsArr = gender == 0 ? new int[] { 0, 1, 6, 9, 10, 20, 22, 19, 24, 27 }
                    : gender == 1 ? new int[] { 2, 3, 7, 11, 12, 17, 18, 19, 26, 27 }
                            : new int[] { 4, 5, 8, 13, 14, 21, 23, 19, 25, 27 };

            JSONArray dataSkills = new JSONArray();
            for (int i = 0; i < skillsArr.length; i++) {
                JSONArray skill = new JSONArray();
                skill.add(skillsArr[i]);
                skill.add(0);
                if (i == 0) {
                    skill.add(1);
                } else {
                    skill.add(0);
                }
                dataSkills.add(skill);
            }
            String skills = dataSkills.toJSONString();

            JSONArray dataSkillShortcut = new JSONArray();
            dataSkillShortcut.add(gender == 0 ? 0 : gender == 1 ? 2 : 4);
            for (int i = 0; i < 9; i++) {
                dataSkillShortcut.add(-1);
            }
            String skillsShortcut = dataSkillShortcut.toJSONString();

            String petInfo = "{}";
            String petPoint = "{}";
            String petBody = "[]";
            String petSkill = "[]";

            JSONArray dataBlackBall = new JSONArray();
            for (int i = 1; i <= 7; i++) {
                JSONArray arr = new JSONArray();
                arr.add(0);
                arr.add(0);
                dataBlackBall.add(arr);
            }
            String blackBall = dataBlackBall.toJSONString();

            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("player");
            int newPlayerId = AccountDAO.getNextSequenceValue("playerId");
            org.bson.Document doc = new org.bson.Document("id", newPlayerId)
                    .append("account_id", userId)
                    .append("name", name)
                    .append("head", hair)
                    .append("gender", gender)
                    .append("have_tennis_space_ship", false)
                    .append("clan_id_sv" + Manager.SERVER, -1)
                    .append("data_inventory", inventory)
                    .append("data_location", location)
                    .append("data_point", point)
                    .append("data_magic_tree", magicTree)
                    .append("items_body", itemsBody)
                    .append("items_bag", itemsBag)
                    .append("items_box", itemsBox)
                    .append("items_box_lucky_round", itemsBoxLuckyRound)
                    .append("friends", friends)
                    .append("enemies", enemies)
                    .append("data_intrinsic", intrinsic)
                    .append("data_item_time", itemTime)
                    .append("data_task", task)
                    .append("data_mabu_egg", mabuEgg)
                    .append("data_charm", charms)
                    .append("skills", skills)
                    .append("skills_shortcut", skillsShortcut)
                    .append("pet_info", petInfo)
                    .append("pet_point", petPoint)
                    .append("pet_body", petBody)
                    .append("pet_skill", petSkill)
                    .append("data_black_ball", blackBall)
                    .append("thoi_vang", 10)
                    .append("data_side_task", "{}")
                    .append("achivements", achive)
                    .append("lastimelogin", new java.util.Date())
                    .append("sk_tet", "[0,0,0,0,0]")
                    .append("dhtime", "[0,0,0,0,0,0]")
                    .append("dhtime2", "[0,0,0,0,0,0]")
                    .append("dhtime3", "[0,0,0,0,0,0]")
                    .append("dhtime4", "[0,0,0,0,0,0]")
                    .append("dhtime5", "[0,0,0,0,0,0]")
                    .append("killWhis", "[0,0,0]")
                    .append("MaBaoVe", "[0,0]")
                    .append("checkNhanQua", "[0,0]")
                    .append("data_card", "[]")
                    .append("challenge", "[0,0,0,0]")
                    .append("buy_limit", "[]")
                    .append("reward_limit", "[]")
                    .append("event_point", 0)
                    .append("moc_nap", 0)
                    .append("firstTimeLogin", new java.util.Date());
            
            collection.insertOne(doc);
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lá»—i táº¡o player má»›i");
        }

    }

    public static void updatePlayer(Player player) {
        updatePlayer(player, null);
    }

    public static void updatePlayer(Player player, Connection connection) {
        if (player.isDisposed() || player.isSaving()) {
            return;
        }
        player.setSaving(true);
        try {
            int n1s = 0;
            int n2s = 0;
            int n3s = 0;
            int tv = 0;
            if (player.loaded) {
                long st = System.currentTimeMillis();
                try {

                    JSONArray dataInventory = new JSONArray();
                    // data kim lÆ°á»£ng
                    dataInventory.add(player.inventory.gold);
                    dataInventory.add(player.inventory.gem);
                    dataInventory.add(player.inventory.ruby);
                    dataInventory.add(player.inventory.goldLimit);
                    String inventory = dataInventory.toJSONString();

                    int mapId = -1;
                    mapId = player.mapIdBeforeLogout;
                    int x = player.location.x;
                    int y = player.location.y;
                    int hp = player.nPoint.hp;
                    int mp = player.nPoint.mp;
                    if (player.isDie()) {
                        mapId = player.gender + 21;
                        x = 300;
                        y = 336;
                        hp = 1;
                        mp = 1;
                    } else {
                        if (MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBlackBallWar(mapId)
                                || mapId == 126 || mapId == ConstMap.CON_DUONG_RAN_DOC
                                || mapId == ConstMap.CON_DUONG_RAN_DOC_142 || mapId == ConstMap.CON_DUONG_RAN_DOC_143
                                || mapId == ConstMap.HOANG_MAC) {
                            mapId = player.gender + 21;
                            x = 300;
                            y = 336;
                        }
                    }

                    // data vá»‹ trĂ­
                    JSONArray dataLocation = new JSONArray();
                    dataLocation.add(x);
                    dataLocation.add(y);
                    dataLocation.add(mapId);
                    String location = dataLocation.toJSONString();
                    // data chá»‰ sá»‘
                    JSONArray dataPoint = new JSONArray();
                    dataPoint.add(0);
                    dataPoint.add(mp);
                    dataPoint.add(player.nPoint.mpg);
                    dataPoint.add(player.nPoint.critg);
                    dataPoint.add(player.nPoint.limitPower);
                    dataPoint.add(player.nPoint.stamina);
                    dataPoint.add(hp);
                    dataPoint.add(player.nPoint.defg);
                    dataPoint.add(player.nPoint.tiemNang);
                    dataPoint.add(player.nPoint.maxStamina);
                    dataPoint.add(player.nPoint.dameg);
                    dataPoint.add(player.nPoint.power);
                    dataPoint.add(player.nPoint.hpg);
                    String point = dataPoint.toJSONString();

                    // data Ä‘áº­u tháº§n
                    JSONArray dataMagicTree = new JSONArray();
                    dataMagicTree.add(player.magicTree.isUpgrade ? 1 : 0);
                    dataMagicTree.add(player.magicTree.lastTimeUpgrade);
                    dataMagicTree.add(player.magicTree.level);
                    dataMagicTree.add(player.magicTree.lastTimeHarvest);
                    dataMagicTree.add(player.magicTree.currPeas);
                    String magicTree = dataMagicTree.toJSONString();

                    // data body
                    JSONArray dataBody = new JSONArray();
                    for (Item item : player.inventory.itemsBody) {
                        JSONObject dataItem = new JSONObject();
                        if (item.isNotNullItem()) {
                            JSONArray options = new JSONArray();
                            dataItem.put("temp_id", item.template.id);
                            dataItem.put("quantity", item.quantity);
                            dataItem.put("create_time", item.createTime);
                            for (ItemOption io : item.itemOptions) {
                                JSONArray option = new JSONArray();
                                option.add(io.optionTemplate.id);
                                option.add(io.param);
                                options.add(option);
                            }
                            dataItem.put("option", options);
                        } else {
                            JSONArray options = new JSONArray();
                            dataItem.put("temp_id", -1);
                            dataItem.put("quantity", 0);
                            dataItem.put("create_time", 0);
                            dataItem.put("option", options);
                        }
                        dataBody.add(dataItem);
                    }

                    String itemsBody = dataBody.toJSONString();

                    // data bag
                    JSONArray dataBag = new JSONArray();
                    for (Item item : player.inventory.itemsBag) {
                        JSONObject dataItem = new JSONObject();
                        if (item.isNotNullItem()) {
                            JSONArray options = new JSONArray();
                            switch (item.template.id) {
                                case 14:
                                    n1s += item.quantity;
                                    break;
                                case 15:
                                    n2s += item.quantity;
                                    break;
                                case 16:
                                    n3s += item.quantity;
                                    break;
                                case 457:
                                    tv += item.quantity;
                                    break;
                            }
                            dataItem.put("temp_id", item.template.id);
                            dataItem.put("quantity", item.quantity);
                            dataItem.put("create_time", item.createTime);

                            for (ItemOption io : item.itemOptions) {
                                JSONArray option = new JSONArray();
                                option.add(io.optionTemplate.id);
                                option.add(io.param);
                                options.add(option);
                            }
                            dataItem.put("option", options);
                        } else {
                            JSONArray options = new JSONArray();
                            dataItem.put("temp_id", -1);
                            dataItem.put("quantity", 0);
                            dataItem.put("create_time", 0);
                            dataItem.put("option", options);
                        }
                        dataBag.add(dataItem);
                    }
                    String itemsBag = dataBag.toJSONString();

                    // data box
                    JSONArray dataBox = new JSONArray();
                    for (Item item : player.inventory.itemsBox) {
                        JSONObject dataItem = new JSONObject();
                        if (item.isNotNullItem()) {
                            JSONArray options = new JSONArray();
                            switch (item.template.id) {
                                case 14:
                                    n1s += item.quantity;
                                    break;
                                case 15:
                                    n2s += item.quantity;
                                    break;
                                case 16:
                                    n3s += item.quantity;
                                    break;
                                case 457:
                                    tv += item.quantity;
                                    break;
                            }
                            dataItem.put("temp_id", item.template.id);
                            dataItem.put("quantity", item.quantity);
                            dataItem.put("create_time", item.createTime);

                            for (ItemOption io : item.itemOptions) {
                                JSONArray option = new JSONArray();
                                option.add(io.optionTemplate.id);
                                option.add(io.param);
                                options.add(option);
                            }
                            dataItem.put("option", options);
                        } else {
                            JSONArray options = new JSONArray();
                            dataItem.put("temp_id", -1);
                            dataItem.put("quantity", 0);
                            dataItem.put("create_time", 0);
                            dataItem.put("option", options);
                        }
                        dataBox.add(dataItem);
                    }
                    String itemsBox = dataBox.toJSONString();

                    // data box crack ball
                    JSONArray dataCrackBall = new JSONArray();
                    for (Item item : player.inventory.itemsBoxCrackBall) {
                        JSONObject dataItem = new JSONObject();
                        if (item.isNotNullItem()) {
                            dataItem.put("temp_id", item.template.id);
                            dataItem.put("quantity", item.quantity);
                            dataItem.put("create_time", item.createTime);
                            JSONArray options = new JSONArray();
                            for (ItemOption io : item.itemOptions) {
                                JSONArray option = new JSONArray();
                                option.add(io.optionTemplate.id);
                                option.add(io.param);
                                options.add(option);
                            }
                            dataItem.put("option", options);
                        } else {
                            JSONArray options = new JSONArray();
                            dataItem.put("temp_id", -1);
                            dataItem.put("quantity", 0);
                            dataItem.put("create_time", 0);
                            dataItem.put("option", options);
                        }
                        dataCrackBall.add(dataItem);
                    }
                    String itemsBoxLuckyRound = dataCrackBall.toJSONString();

                    // data báº¡n bĂ¨
                    JSONArray dataFriends = new JSONArray();
                    for (Friend f : player.friends) {
                        JSONObject friend = new JSONObject();
                        friend.put("id", f.id);
                        friend.put("name", f.name);
                        friend.put("power", f.power);
                        friend.put("head", f.head);
                        friend.put("body", f.body);
                        friend.put("leg", f.leg);
                        friend.put("bag", f.bag);
                        dataFriends.add(friend);
                    }
                    String friend = dataFriends.toJSONString();

                    // data káº» thĂ¹
                    JSONArray dataEnemies = new JSONArray();
                    for (Friend e : player.enemies) {
                        JSONObject enemy = new JSONObject();
                        enemy.put("id", e.id);
                        enemy.put("name", e.name);
                        enemy.put("power", e.power);
                        enemy.put("head", e.head);
                        enemy.put("body", e.body);
                        enemy.put("leg", e.leg);
                        enemy.put("bag", e.bag);
                        dataEnemies.add(enemy);
                    }
                    String enemy = dataEnemies.toJSONString();

                    // data ná»™i táº¡i
                    JSONArray dataIntrinsic = new JSONArray();
                    dataIntrinsic.add(player.playerIntrinsic.intrinsic.id);
                    dataIntrinsic.add(player.playerIntrinsic.intrinsic.param1);
                    dataIntrinsic.add(player.playerIntrinsic.countOpen);
                    dataIntrinsic.add(player.playerIntrinsic.intrinsic.param2);
                    String intrinsic = dataIntrinsic.toJSONString();

                    // data item time
                    JSONArray dataItemTime = new JSONArray();
                    dataItemTime.add(player.itemTime.isUseBoKhi
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseAnDanh
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh))
                            : 0);
                    dataItemTime
                            .add(player.itemTime.isOpenPower
                                    ? (ItemTime.TIME_OPEN_POWER
                                            - (System.currentTimeMillis() - player.itemTime.lastTimeOpenPower))
                                    : 0);
                    dataItemTime.add(player.itemTime.isUseCuongNo
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseMayDo
                            ? (ItemTime.TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseBoHuyet
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet))
                            : 0);
                    dataItemTime.add(player.itemTime.iconMeal);
                    dataItemTime.add(player.itemTime.isEatMeal
                            ? (ItemTime.TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseGiapXen
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseBanhChung
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhChung))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseBanhTet
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTet))
                            : 0);

                    dataItemTime.add(player.itemTime.isUseBoKhi2
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi2))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseGiapXen2
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen2))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseCuongNo2
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo2))
                            : 0);
                    dataItemTime.add(player.itemTime.isUseBoHuyet2
                            ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet2))
                            : 0);

                    // BiNgo
                    dataItemTime.add(player.effectSkill.isBiNgo
                            ? (30_000 - (System.currentTimeMillis() - player.effectSkill.lastBiNgo))
                            : 0);
                    // BiNgo

                    String itemTime = dataItemTime.toJSONString();

                    // data nhiá»‡m vá»¥
                    JSONArray dataTask = new JSONArray();
                    dataTask.add(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
                    dataTask.add(player.playerTask.taskMain.id);
                    dataTask.add(player.playerTask.taskMain.index);
                    String task = dataTask.toJSONString();

                    // data nhiá»‡m vá»¥ hĂ ng ngĂ y
                    JSONArray dataSideTask = new JSONArray();
                    dataSideTask.add(player.playerTask.sideTask.level);
                    dataSideTask.add(player.playerTask.sideTask.count);
                    dataSideTask.add(player.playerTask.sideTask.leftTask);
                    dataSideTask.add(
                            player.playerTask.sideTask.template != null ? player.playerTask.sideTask.template.id : -1);
                    dataSideTask.add(player.playerTask.sideTask.receivedTime);
                    dataSideTask.add(player.playerTask.sideTask.maxCount);
                    String sideTask = dataSideTask.toJSONString();

                    JSONArray dataAchive = new JSONArray();
                    for (Achivement a : player.playerTask.achivements) {
                        JSONObject jobj = new JSONObject();
                        jobj.put("id", a.getId());
                        jobj.put("count", a.getCount());
                        jobj.put("finish", a.isFinish() ? 1 : 0);
                        jobj.put("receive", a.isReceive() ? 1 : 0);
                        dataAchive.add(jobj);
                    }
                    String achive = dataAchive.toJSONString();

                    // data trá»©ng bÆ°
                    JSONObject dataMaBu = new JSONObject();
                    if (player.mabuEgg != null) {
                        dataMaBu.put("create_time", player.mabuEgg.lastTimeCreate);
                        dataMaBu.put("time_done", player.mabuEgg.timeDone);
                    }
                    String mabuEgg = dataMaBu.toJSONString();

                    // DATA DANH HIá»†U BY LOUIS GOKU
                    JSONArray dataArray = new JSONArray();
                    dataArray.add(player.isTitleUse == true ? 1 : 0);
                    dataArray.add(player.lastTimeTitle1);
                    dataArray.add(player.IdDanhHieu_1);
                    dataArray.add(player.ChiSoHP_1);
                    dataArray.add(player.ChiSoKI_1);
                    dataArray.add(player.ChiSoSD_1);
                    String dhtime = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.isTitleUse2 == true ? 1 : 0);
                    dataArray.add(player.lastTimeTitle2);
                    dataArray.add(player.IdDanhHieu_2);
                    dataArray.add(player.ChiSoHP_2);
                    dataArray.add(player.ChiSoKI_2);
                    dataArray.add(player.ChiSoSD_2);
                    String dhtime2 = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.isTitleUse3 == true ? 1 : 0);
                    dataArray.add(player.lastTimeTitle3);
                    dataArray.add(player.IdDanhHieu_3);
                    dataArray.add(player.ChiSoHP_3);
                    dataArray.add(player.ChiSoKI_3);
                    dataArray.add(player.ChiSoSD_3);
                    String dhtime3 = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.isTitleUse4 == true ? 1 : 0);
                    dataArray.add(player.lastTimeTitle4);
                    dataArray.add(player.IdDanhHieu_4);
                    dataArray.add(player.ChiSoHP_4);
                    dataArray.add(player.ChiSoKI_4);
                    dataArray.add(player.ChiSoSD_4);
                    String dhtime4 = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.isTitleUse5 == true ? 1 : 0);
                    dataArray.add(player.lastTimeTitle5);
                    dataArray.add(player.IdDanhHieu_5);
                    dataArray.add(player.ChiSoHP_5);
                    dataArray.add(player.ChiSoKI_5);
                    dataArray.add(player.ChiSoSD_5);
                    String dhtime5 = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.isUseMaBaoVe == true ? 1 : 0);
                    dataArray.add(player.MaBaoVe);
                    String MaBaoVe = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.event.luotNhanNgocMienPhi);
                    dataArray.add(player.event.luotNhanBuaMienPhi);
                    String checkNhanQua = dataArray.toJSONString();
                    dataArray.clear();

                    JSONArray dataArray2 = new JSONArray();
                    dataArray2.add(player.lastTimeSwapWhis);
                    dataArray2.add(player.lastTimeKillWhis);
                    dataArray2.add(player.levelKillWhis);
                    String killWhis = dataArray2.toJSONString();
                    dataArray2.clear();

                    // data bĂ¹a
                    JSONArray dataCharms = new JSONArray();
                    dataCharms.add(player.charms.tdTriTue);
                    dataCharms.add(player.charms.tdManhMe);
                    dataCharms.add(player.charms.tdDaTrau);
                    dataCharms.add(player.charms.tdOaiHung);
                    dataCharms.add(player.charms.tdBatTu);
                    dataCharms.add(player.charms.tdDeoDai);
                    dataCharms.add(player.charms.tdThuHut);
                    dataCharms.add(player.charms.tdDeTu);
                    dataCharms.add(player.charms.tdTriTue3);
                    dataCharms.add(player.charms.tdTriTue4);
                    dataCharms.add(player.charms.tdDeTuMabu);
                    String charm = dataCharms.toJSONString();

                    // data skill
                    JSONArray dataSkills = new JSONArray();
                    for (Skill skill : player.playerSkill.skills) {
                        // if (skill.skillId != -1) {
                        JSONArray dataskill = new JSONArray();
                        dataskill.add(skill.template.id);
                        dataskill.add(skill.lastTimeUseThisSkill);
                        dataskill.add(skill.point);
                        // } else {
                        // dataObject.put("temp_id", -1);
                        // dataObject.put("point", 0);
                        // dataObject.put("last_time_use", 0);
                        // }
                        dataSkills.add(dataskill);
                    }
                    String skills = dataSkills.toJSONString();

                    JSONArray dataSkillShortcut = new JSONArray();
                    // data skill shortcut
                    for (int skillId : player.playerSkill.skillShortCut) {
                        dataSkillShortcut.add(skillId);
                    }
                    String skillShortcut = dataSkillShortcut.toJSONString();

                    JSONObject jPetInfo = new JSONObject();
                    JSONObject jPetPoint = new JSONObject();
                    JSONArray jPetBody = new JSONArray();
                    JSONArray jPetSkills = new JSONArray();
                    String petInfo = jPetInfo.toJSONString();
                    String petPoint = jPetPoint.toJSONString();
                    String petBody = jPetBody.toJSONString();
                    String petSkill = jPetSkills.toJSONString();

                    JSONArray dataChallenge = new JSONArray();
                    dataChallenge.add(player.goldChallenge);
                    dataChallenge.add(player.levelWoodChest);
                    dataChallenge.add(player.receivedWoodChest ? 1 : 0);
                    dataChallenge.add(player.gemChallenge);
                    String challenge = dataChallenge.toJSONString();

                    JSONArray dataSuKienTet = new JSONArray();
                    dataSuKienTet.add(player.event.getTimeCookTetCake());
                    dataSuKienTet.add(player.event.getTimeCookChungCake());
                    dataSuKienTet.add(player.event.isCookingTetCake() ? 1 : 0);
                    dataSuKienTet.add(player.event.isCookingChungCake() ? 1 : 0);
                    dataSuKienTet.add(player.event.isReceivedLuckyMoney() ? 1 : 0);
                    String skTet = dataSuKienTet.toJSONString();

                    JSONArray dataBuyLimit = new JSONArray();
                    for (int i = 0; i < player.buyLimit.length; i++) {
                        dataBuyLimit.add(player.buyLimit[i]);
                    }
                    String buyLimit = dataBuyLimit.toJSONString();

                    JSONArray dataRwLimit = new JSONArray();
                    for (int i = 0; i < player.getRewardLimit().length; i++) {
                        dataRwLimit.add(player.getRewardLimit()[i]);
                    }
                    String rwLimit = dataRwLimit.toJSONString();

                    // data pet
                    if (player.pet != null) {
                        jPetInfo.put("name", player.pet.name);
                        jPetInfo.put("gender", player.pet.gender);
                        jPetInfo.put("is_mabu", player.pet.isMabu ? 1 : 0);

                        jPetInfo.put("is_Bulo", player.pet.isBulo ? 1 : 0);
                        jPetInfo.put("is_CellBao", player.pet.isCellBao ? 1 : 0);
                        jPetInfo.put("is_BillNhi", player.pet.isBillNhi ? 1 : 0);
                        jPetInfo.put("is_FideTrau", player.pet.isFideTrau ? 1 : 0);
                        jPetInfo.put("is_SuperPicolo", player.pet.isSuperPicolo ? 1 : 0);

                        jPetInfo.put("status", player.pet.status);
                        jPetInfo.put("type_fusion", player.fusion.typeFusion);
                        int timeLeftFusion = (int) (Fusion.TIME_FUSION
                                - (System.currentTimeMillis() - player.fusion.lastTimeFusion));
                        jPetInfo.put("left_fusion", timeLeftFusion < 0 ? 0 : timeLeftFusion);
                        petInfo = jPetInfo.toJSONString();

                        jPetPoint.put("power", player.pet.nPoint.power);
                        jPetPoint.put("tiem_nang", player.pet.nPoint.tiemNang);
                        jPetPoint.put("stamina", player.pet.nPoint.stamina);
                        jPetPoint.put("max_stamina", player.pet.nPoint.maxStamina);
                        jPetPoint.put("hpg", player.pet.nPoint.hpg);
                        jPetPoint.put("mpg", player.pet.nPoint.mpg);
                        jPetPoint.put("damg", player.pet.nPoint.dameg);
                        jPetPoint.put("defg", player.pet.nPoint.defg);
                        jPetPoint.put("critg", player.pet.nPoint.critg);
                        jPetPoint.put("limit_power", player.pet.nPoint.limitPower);
                        jPetPoint.put("hp", player.pet.nPoint.hp);
                        jPetPoint.put("mp", player.pet.nPoint.mp);
                        petPoint = jPetPoint.toJSONString();

                        for (Item item : player.pet.inventory.itemsBody) {
                            JSONObject dataItem = new JSONObject();
                            if (item.isNotNullItem()) {
                                dataItem.put("temp_id", item.template.id);
                                dataItem.put("quantity", item.quantity);
                                dataItem.put("create_time", item.createTime);
                                JSONArray options = new JSONArray();
                                for (ItemOption io : item.itemOptions) {
                                    JSONArray option = new JSONArray();
                                    option.add(io.optionTemplate.id);
                                    option.add(io.param);
                                    options.add(option);
                                }
                                dataItem.put("option", options);
                            } else {
                                JSONArray options = new JSONArray();
                                dataItem.put("temp_id", -1);
                                dataItem.put("quantity", 0);
                                dataItem.put("create_time", 0);
                                dataItem.put("option", options);
                            }
                            jPetBody.add(dataItem);
                        }
                        petBody = jPetBody.toJSONString();

                        for (Skill s : player.pet.playerSkill.skills) {
                            JSONArray pskill = new JSONArray();
                            if (s.skillId != -1) {
                                pskill.add(s.template.id);
                                pskill.add(s.point);
                            } else {
                                pskill.add(-1);
                                pskill.add(0);
                            }
                            jPetSkills.add(pskill);
                        }
                        petSkill = jPetSkills.toJSONString();
                    }

                    JSONArray dataBlackBall = new JSONArray();
                    // data thÆ°á»Ÿng ngá»c rá»“ng Ä‘en
                    for (int i = 1; i <= 7; i++) {
                        JSONArray data = new JSONArray();
                        data.add(player.rewardBlackBall.timeOutOfDateReward[i - 1]);
                        data.add(player.rewardBlackBall.lastTimeGetReward[i - 1]);
                        dataBlackBall.add(data);
                    }
                    String blackBall = dataBlackBall.toJSONString();
                    try {
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("player");
                        org.bson.conversions.Bson updates = com.mongodb.client.model.Updates.combine(
                                com.mongodb.client.model.Updates.set("head", player.head),
                                com.mongodb.client.model.Updates.set("have_tennis_space_ship", player.haveTennisSpaceShip),
                                com.mongodb.client.model.Updates.set("clan_id_sv" + Manager.SERVER, (short) (player.clan != null ? player.clan.id : -1)),
                                com.mongodb.client.model.Updates.set("data_inventory", inventory),
                                com.mongodb.client.model.Updates.set("data_location", location),
                                com.mongodb.client.model.Updates.set("data_point", point),
                                com.mongodb.client.model.Updates.set("data_magic_tree", magicTree),
                                com.mongodb.client.model.Updates.set("items_body", itemsBody),
                                com.mongodb.client.model.Updates.set("items_bag", itemsBag),
                                com.mongodb.client.model.Updates.set("items_box", itemsBox),
                                com.mongodb.client.model.Updates.set("items_box_lucky_round", itemsBoxLuckyRound),
                                com.mongodb.client.model.Updates.set("friends", friend),
                                com.mongodb.client.model.Updates.set("enemies", enemy),
                                com.mongodb.client.model.Updates.set("data_intrinsic", intrinsic),
                                com.mongodb.client.model.Updates.set("data_item_time", itemTime),
                                com.mongodb.client.model.Updates.set("data_task", task),
                                com.mongodb.client.model.Updates.set("data_mabu_egg", mabuEgg),
                                com.mongodb.client.model.Updates.set("pet_info", petInfo),
                                com.mongodb.client.model.Updates.set("pet_point", petPoint),
                                com.mongodb.client.model.Updates.set("pet_body", petBody),
                                com.mongodb.client.model.Updates.set("pet_skill", petSkill),
                                com.mongodb.client.model.Updates.set("power", player.nPoint.power),
                                com.mongodb.client.model.Updates.set("pet_power", player.pet != null ? player.pet.nPoint.power : 0),
                                com.mongodb.client.model.Updates.set("data_black_ball", blackBall),
                                com.mongodb.client.model.Updates.set("data_side_task", sideTask),
                                com.mongodb.client.model.Updates.set("data_charm", charm),
                                com.mongodb.client.model.Updates.set("skills", skills),
                                com.mongodb.client.model.Updates.set("skills_shortcut", skillShortcut),
                                com.mongodb.client.model.Updates.set("thoi_vang", tv),
                                com.mongodb.client.model.Updates.set("1sao", n1s),
                                com.mongodb.client.model.Updates.set("2sao", n2s),
                                com.mongodb.client.model.Updates.set("3sao", n3s),
                                com.mongodb.client.model.Updates.set("collection_book", new Gson().toJson(player.getCollectionBook().getCards())),
                                com.mongodb.client.model.Updates.set("event_point", player.event.getEventPoint()),
                                com.mongodb.client.model.Updates.set("firstTimeLogin", Util.toDateString(player.firstTimeLogin)),
                                com.mongodb.client.model.Updates.set("challenge", challenge),
                                com.mongodb.client.model.Updates.set("sk_tet", skTet),
                                com.mongodb.client.model.Updates.set("buy_limit", buyLimit),
                                com.mongodb.client.model.Updates.set("moc_nap", player.event.getMocNapDaNhan()),
                                com.mongodb.client.model.Updates.set("achivements", achive),
                                com.mongodb.client.model.Updates.set("reward_limit", rwLimit),
                                com.mongodb.client.model.Updates.set("dhtime", dhtime),
                                com.mongodb.client.model.Updates.set("dhtime2", dhtime2),
                                com.mongodb.client.model.Updates.set("dhtime3", dhtime3),
                                com.mongodb.client.model.Updates.set("dhtime4", dhtime4),
                                com.mongodb.client.model.Updates.set("dhtime5", dhtime5),
                                com.mongodb.client.model.Updates.set("killWhis", killWhis),
                                com.mongodb.client.model.Updates.set("MaBaoVe", MaBaoVe),
                                com.mongodb.client.model.Updates.set("levelKillWhis", player.levelKillWhisDone),
                                com.mongodb.client.model.Updates.set("timeKillWhis", player.timeKillWhis),
                                com.mongodb.client.model.Updates.set("checkNhanQua", checkNhanQua)
                        );
                        
                        collection.updateOne(com.mongodb.client.model.Filters.eq("id", (int) player.id), updates);
                        if (updateTimeLogout) {
                            AccountDAO.updateAccoutLogout(player.getSession());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Log.error(PlayerDAO.class, e, "Lá»—i save player " + player.name);
                } finally {

                }
            }
        } finally {
            player.setSaving(false);
        }
    }

    public static void saveName(Player player) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("player");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", (int) player.id), com.mongodb.client.model.Updates.set("name", player.name));
        } catch (Exception e) {
        }
    }

    public static void saveMaBaoVe(Player player, int mabaove) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", player.getSession().userId), com.mongodb.client.model.Updates.set("MaBaoVe", mabaove));
        } catch (Exception e) {
        }
    }

    public static void Bat_Tat_MaBaoVe(Player player, int action) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", player.getSession().userId), com.mongodb.client.model.Updates.set("isUseMaBaoVe", action == 0 ? 1 : 0));
        } catch (Exception e) {
        }
    }

    public static boolean isExistName(String name) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("player");
            return collection.countDocuments(com.mongodb.client.model.Filters.eq("name", name)) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void subRuby(Player player, int userId, int ruby) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", userId), com.mongodb.client.model.Updates.inc("ruby", -ruby));
        } catch (Exception e) {
            nro.utils.Log.error(PlayerDAO.class, e, "L?i update ruby " + player.name);
        }
    }

    public static void subGoldBar(Player player, int num) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", player.getSession().userId), com.mongodb.client.model.Updates.inc("thoi_vang", -num));
        } catch (Exception e) {
            nro.utils.Log.error(PlayerDAO.class, e, "L?i update th?i vàng " + player.name);
        }
    }

    public static void moThanhVien(Player player) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", player.getSession().userId), com.mongodb.client.model.Updates.set("active", 1));
        } catch (Exception e) {
            nro.utils.Log.error(PlayerDAO.class, e, "L?i update m? thành viên " + player.name);
        }
    }

    public static void addGoldBar(Player player, int num) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", player.getSession().userId), com.mongodb.client.model.Updates.inc("thoi_vang", num));
        } catch (Exception e) {
            nro.utils.Log.error(PlayerDAO.class, e, "L?i update th?i vàng " + player.name);
        }
    }

    public static void subVndBar(Player player, int num) {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("account");
            collection.updateOne(com.mongodb.client.model.Filters.eq("id", player.getSession().userId), com.mongodb.client.model.Updates.inc("vnd", -num));
        } catch (Exception e) {
        }
    }

    public static void addHistoryReceiveGoldBar(Player player, int goldBefore, int goldAfter, int goldBagBefore, int goldBagAfter, int goldBoxBefore, int goldBoxAfter) {
    }

    public static void updateItemReward(Player player) {
    }

    public static void saveBag(Player player) {
    }
}
