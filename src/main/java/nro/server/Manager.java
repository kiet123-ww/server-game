package nro.server;

import lombok.Getter;
import nro.attr.Attribute;
import nro.attr.AttributeManager;
import nro.attr.AttributeTemplateManager;
import nro.card.CardManager;
import nro.consts.ConstItem;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.data.DataGame;
import nro.event.Event;
import nro.jdbc.daos.AccountDAO;
import nro.jdbc.daos.ShopDAO;
import nro.lib.RandomCollection;
import nro.manager.*;
import nro.models.*;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.intrinsic.Intrinsic;
import nro.models.item.*;
import nro.models.map.*;
import nro.models.mob.MobReward;
import nro.models.mob.MobTemplate;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.npc.NpcTemplate;
import nro.models.player.Referee;
import nro.models.shop.Shop;
import nro.models.skill.NClass;
import nro.models.skill.Skill;
import nro.models.skill.SkillTemplate;
import nro.models.task.SideTaskTemplate;
import nro.models.task.SubTaskMain;
import nro.models.task.TaskMain;
import nro.noti.NotiManager;
import nro.power.CaptionManager;
import nro.power.PowerLimitManager;
import nro.services.ItemService;
import nro.services.MapService;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import nro.jdbc.MongoDBConnection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import nro.consts.ConstNpc;
import nro.models.boss.mapoffline.NPC_ToSuKaio;

/**
 * @author 💖 YTB KhanhDTK 💖
 *
 */
public class Manager {

    private static Manager i;

    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 40;
    public static byte MAX_PER_IP = 5;
    public static int MAX_PLAYER = 1000;
    public static byte RATE_EXP_SERVER = 1;
    public static int EVENT_SEVER = 0;
    public static String DOMAIN = "";

    public static int danhquairoingoc;

    public static Timestamp timeSuKienDuaTop = Timestamp.valueOf("2024-03-20 23:59:59");

    public static String timeStartDuaTop = "10h ngày 17/3/2024";

    public static String timeEndDuaTop = "23h59 ngày 24/3/2024";

    public static String timeEndNhanGiai = "28/3/2024";

    public static String SERVER_NAME = "";

    public static int EVENT_COUNT_THAN_HUY_DIET = 0;
    public static int EVENT_COUNT_QUY_LAO_KAME = 0;
    public static int EVENT_COUNT_THAN_MEO = 0;
    public static int EVENT_COUNT_THUONG_DE = 0;
    public static int EVENT_COUNT_THAN_VU_TRU = 0;

    public static String loginHost;
    public static int loginPort = 3105;
    public static int apiPort = 8080;
    public static int bossGroup = 5;
    public static int workerGroup = 10;
    public static String apiKey = "abcdef";
    public static String executeCommand;
    public static boolean debug;

    public static String KEY_SERVER = "";

    public static String KEY_SERVER_2 = "";

    public static boolean activeKey;

    public static int TIME_CON_SO_MAY_MAN = 5;

    public static int TIME_START_CON_SO_MAY_MAN = 8;

    public static int TIME_END_CON_SO_MAY_MAN = 21;

    public static final List<String> TOP_PLAYERS = new ArrayList<>();

    public static MapTemplate[] MAP_TEMPLATES;
    public static final List<nro.models.map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final List<MobReward> MOB_REWARDS = new ArrayList<>();
    public static final RandomCollection<ItemLuckyRound> LUCKY_ROUND_REWARDS = new RandomCollection<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<String> CAPTIONS = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<CaiTrang> CAI_TRANGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final ByteArrayOutputStream[] cache = new ByteArrayOutputStream[4];
    public static final RandomCollection<Integer> HONG_DAO_CHIN = new RandomCollection<>();
    public static final RandomCollection<Integer> HOP_QUA_TET = new RandomCollection<>();

    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<String, Byte>();
    @Getter
    public GameConfig gameConfig;

    public static Manager gI() {
        if (i == null) {
            i = new Manager();
        }
        return i;
    }

    private Manager() {
        try {
            loadProperties();
            gameConfig = new GameConfig();
        } catch (IOException ex) {
            Log.error(Manager.class, ex, "Lỗi load properites");
            System.exit(0);
        }
        loadDatabase();
        NpcFactory.createNpcConMeo();
        NpcFactory.createNpcRongThieng();
        Event.initEvent(gameConfig.getEvent());
        if (Event.isEvent()) {
            Event.getInstance().init();
        }
        initRandomItem();
        NamekBallManager.gI().initBall();
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

    public static String hienThiTimeSuKien() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        String formattedTime = dateFormat.format(timeSuKienDuaTop);
        return formattedTime;
    }

    public static String demTimeSuKien() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime eventTime = timeSuKienDuaTop.toLocalDateTime();

        long daysRemaining = ChronoUnit.DAYS.between(currentTime, eventTime);
        if (daysRemaining > 0) {
            return "(" + daysRemaining + " ngày nữa)";
        } else {
            return "(Đã kết thúc)";
        }
    }

    public static long demTimeSuKien2() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime eventTime = timeSuKienDuaTop.toLocalDateTime();

        long daysRemaining = ChronoUnit.DAYS.between(currentTime, eventTime);
        if (daysRemaining > 0) {
            return daysRemaining;
        } else {
            return 0;
        }
    }

    private void initRandomItem() {
        HONG_DAO_CHIN.add(50, ConstItem.CHU_GIAI);
        HONG_DAO_CHIN.add(50, ConstItem.HONG_NGOC);

        HOP_QUA_TET.add(10, ConstItem.DIEU_RONG);
        HOP_QUA_TET.add(10, ConstItem.DAO_RANG_CUA);
        HOP_QUA_TET.add(10, ConstItem.QUAT_BA_TIEU);
        HOP_QUA_TET.add(10, ConstItem.BUA_MJOLNIR);
        HOP_QUA_TET.add(10, ConstItem.BUA_STORMBREAKER);
        HOP_QUA_TET.add(10, ConstItem.DINH_BA_SATAN);
        HOP_QUA_TET.add(10, ConstItem.CHOI_PHU_THUY);
        HOP_QUA_TET.add(10, ConstItem.MANH_AO);
        HOP_QUA_TET.add(10, ConstItem.MANH_QUAN);
        HOP_QUA_TET.add(10, ConstItem.MANH_GIAY);
        HOP_QUA_TET.add(10, ConstItem.MANH_NHAN);
        HOP_QUA_TET.add(10, ConstItem.MANH_GANG_TAY);
        HOP_QUA_TET.add(8, ConstItem.PHUONG_HOANG_LUA);
        // HOP_QUA_TET.add(7, ConstItem.CAI_TRANG_SSJ_3_WHITE);
        HOP_QUA_TET.add(7, ConstItem.NOEL_2022_GOKU);
        HOP_QUA_TET.add(7, ConstItem.NOEL_2022_CADIC);
        HOP_QUA_TET.add(7, ConstItem.NOEL_2022_POCOLO);
        HOP_QUA_TET.add(20, ConstItem.CUONG_NO_2);
        HOP_QUA_TET.add(20, ConstItem.BO_HUYET_2);
        HOP_QUA_TET.add(20, ConstItem.BO_KHI_2);
    }

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            nro.models.map.Map map = null;
            if (mapTemp.id == 126) {
                map = new nro.models.map.SantaCity(mapTemp.id,
                        mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                        mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                        mapTemp.zones, mapTemp.isMapOffline(),
                        mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);
                if (map != null) {
                    SantaCity santaCity = (SantaCity) map;
                    // 3600000 = 1H
                    santaCity.timer(21, 0, 0, 7200000);
                }
            } else {
                map = new nro.models.map.Map(mapTemp.id,
                        mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                        mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                        mapTemp.zones, mapTemp.isMapOffline(),
                        mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);
            }
            if (map != null) {
                MAPS.add(map);
                map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
                map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY, mapTemp.npcAvatar);
                new Thread(map, "Update map " + map.mapName).start();
            }
        }
        Referee r = new Referee();
        r.initReferee();

        Log.success("Init map thành công!");
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONValue jv = new JSONValue();
        JSONArray dataArray = null;
        JSONObject dataObject = null;
        try {
            com.mongodb.client.MongoDatabase db = nro.jdbc.MongoDBConnection.getDatabase();
            // load part
            PartManager.getInstance().load();

            // load map template
            long countRow = db.getCollection("map_template").countDocuments();
            if (countRow > 0) {
                MAP_TEMPLATES = new MapTemplate[(int)countRow];
                com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("map_template").find().iterator();
                short i = 0;
                while (rs.hasNext()) { org.bson.Document doc = rs.next();
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                    String mapName = doc.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    // load data
                    dataArray = (JSONArray) jv.parse(doc.getString("data"));
                    mapTemplate.type = Byte.parseByte(String.valueOf(dataArray.get(0)));
                    mapTemplate.planetId = Byte.parseByte(String.valueOf(dataArray.get(1)));
                    mapTemplate.bgType = Byte.parseByte(String.valueOf(dataArray.get(2)));
                    mapTemplate.tileId = Byte.parseByte(String.valueOf(dataArray.get(3)));
                    mapTemplate.bgId = Byte.parseByte(String.valueOf(dataArray.get(4)));
                    dataArray.clear();
                    mapTemplate.zones = (doc.getInteger("zones") != null ? doc.getInteger("zones").byteValue() : 0);
                    mapTemplate.maxPlayerPerZone = (doc.getInteger("max_player") != null ? doc.getInteger("max_player").byteValue() : 0);
                    // load waypoints
                    dataArray = (JSONArray) jv.parse(doc.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ","));
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    // load mobs
                    dataArray = (JSONArray) jv.parse(doc.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new int[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Integer.parseInt(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    // load npc
                    dataArray = (JSONArray) jv.parse(doc.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    mapTemplate.npcAvatar = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        mapTemplate.npcAvatar[j] = Short.parseShort(String.valueOf(dtn.get(3)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    // load eff map
                    dataArray = (JSONArray) jv.parse(doc.getString("effect"));
                    for (int j = 0; j < dataArray.size(); j++) {
                        EffectMap em = new EffectMap();
                        dataObject = (JSONObject) jv.parse(dataArray.get(j).toString());
                        if (dataObject != null) {
                            em.setKey(String.valueOf(dataObject.get("key")));
                            em.setValue(String.valueOf(dataObject.get("value")));
                            mapTemplate.effectMaps.add(em);
                        }
                    }

                    dataArray.clear();

                    if (dataObject != null) {
                        dataObject.clear();
                    }

                    MAP_TEMPLATES[i++] = mapTemplate;
                }
                Log.success("Load map template thành công (" + MAP_TEMPLATES.length + ")");
            }

            // load skill
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("skill_template").find().iterator();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                byte id = (doc.getInteger("nclass_id") != null ? doc.getInteger("nclass_id").byteValue() : 0);
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = (doc.getInteger("id") != null ? doc.getInteger("id").byteValue() : 0);
                skillTemplate.name = doc.getString("name");
                skillTemplate.maxPoint = (doc.getInteger("max_point") != null ? doc.getInteger("max_point").byteValue() : 0);
                skillTemplate.manaUseType = (doc.getInteger("mana_use_type") != null ? doc.getInteger("mana_use_type").byteValue() : 0);
                skillTemplate.type = (doc.getInteger("type") != null ? doc.getInteger("type").byteValue() : 0);
                skillTemplate.iconId = (doc.getInteger("icon_id") != null ? doc.getInteger("icon_id").shortValue() : 0);
                skillTemplate.damInfo = doc.getString("dam_info");
                skillTemplate.description = doc.getString("desc");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        doc.getString("skills"));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) jv.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
            
            
            Log.success("Load skill thành công (" + NCLASS.size() + ")");

            // load head avatar
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("head_avatar").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                HeadAvatar headAvatar = new HeadAvatar((doc.getInteger("head_id") != null ? doc.getInteger("head_id") : 0), (doc.getInteger("avatar_id") != null ? doc.getInteger("avatar_id") : 0));
                HEAD_AVATARS.add(headAvatar);
            }
            
            
            Log.success("Load head avatar thành công (" + HEAD_AVATARS.size() + ")");

            // load flag bag
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("flag_bag").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                FlagBag flagBag = new FlagBag();
                flagBag.id = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                flagBag.name = doc.getString("name");
                flagBag.gold = (doc.getInteger("gold") != null ? doc.getInteger("gold") : 0);
                flagBag.gem = (doc.getInteger("gem") != null ? doc.getInteger("gem") : 0);
                flagBag.iconId = (doc.getInteger("icon_id") != null ? doc.getInteger("icon_id").shortValue() : 0);
                String[] iconData = doc.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            
            
            Log.success("Load flag bag thành công (" + FLAGS_BAGS.size() + ")");

            // load cải trang
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("cai_trang").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                CaiTrang caiTrang = new CaiTrang((doc.getInteger("id_temp") != null ? doc.getInteger("id_temp") : 0),
                        (doc.getInteger("head") != null ? doc.getInteger("head") : 0), (doc.getInteger("body") != null ? doc.getInteger("body") : 0), (doc.getInteger("leg") != null ? doc.getInteger("leg") : 0), (doc.getInteger("bag") != null ? doc.getInteger("bag") : 0));
                CAI_TRANGS.add(caiTrang);
            }
            
            
            Log.success("Load cải trang thành công (" + CAI_TRANGS.size() + ")");

            // load intrinsic
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("intrinsic").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = (doc.getInteger("id") != null ? doc.getInteger("id").byteValue() : 0);
                intrinsic.name = doc.getString("name");
                intrinsic.paramFrom1 = (doc.getInteger("param_from_1") != null ? doc.getInteger("param_from_1").shortValue() : 0);
                intrinsic.paramTo1 = (doc.getInteger("param_to_1") != null ? doc.getInteger("param_to_1").shortValue() : 0);
                intrinsic.paramFrom2 = (doc.getInteger("param_from_2") != null ? doc.getInteger("param_from_2").shortValue() : 0);
                intrinsic.paramTo2 = (doc.getInteger("param_to_2") != null ? doc.getInteger("param_to_2").shortValue() : 0);
                intrinsic.icon = (doc.getInteger("icon") != null ? doc.getInteger("icon").shortValue() : 0);
                intrinsic.gender = (doc.getInteger("gender") != null ? doc.getInteger("gender").byteValue() : 0);
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT:
                        INTRINSIC_TD.add(intrinsic);
                        break;
                    case ConstPlayer.NAMEC:
                        INTRINSIC_NM.add(intrinsic);
                        break;
                    case ConstPlayer.XAYDA:
                        INTRINSIC_XD.add(intrinsic);
                        break;
                    default:
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                }
                INTRINSICS.add(intrinsic);
            }
            
            
            Log.success("Load intrinsic thành công (" + INTRINSICS.size() + ")");

            // load task
            ps = con.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                int id = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = doc.getString("name");
                    task.detail = doc.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = doc.getString("sub_name");
                subTask.maxCount = (doc.getInteger("max_count") != null ? doc.getInteger("max_count").shortValue() : 0);
                subTask.notify = doc.getString("notify");
                subTask.npcId = (doc.getInteger("npc_id") != null ? doc.getInteger("npc_id").byteValue() : 0);
                subTask.mapId = (doc.getInteger("map") != null ? doc.getInteger("map").shortValue() : 0);
                task.subTasks.add(subTask);
            }
            
            
            Log.success("Load task thành công (" + TASKS.size() + ")");

            // load side task
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("side_task_template").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                sideTask.name = doc.getString("name");
                String[] mc1 = doc.getString("max_count_lv1").split("-");
                String[] mc2 = doc.getString("max_count_lv2").split("-");
                String[] mc3 = doc.getString("max_count_lv3").split("-");
                String[] mc4 = doc.getString("max_count_lv4").split("-");
                String[] mc5 = doc.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            
            
            Log.success("Load side task thành công (" + SIDE_TASKS_TEMPLATE.size() + ")");

            // load item template
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("item_template").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = (doc.getInteger("id") != null ? doc.getInteger("id").shortValue() : 0);
                itemTemp.type = (doc.getInteger("type") != null ? doc.getInteger("type").byteValue() : 0);
                itemTemp.gender = (doc.getInteger("gender") != null ? doc.getInteger("gender").byteValue() : 0);
                itemTemp.name = doc.getString("name");
                itemTemp.description = doc.getString("description");
                itemTemp.iconID = (doc.getInteger("icon_id") != null ? doc.getInteger("icon_id").shortValue() : 0);
                itemTemp.part = (doc.getInteger("part") != null ? doc.getInteger("part").shortValue() : 0);
                itemTemp.isUpToUp = (doc.getBoolean("is_up_to_up") != null ? doc.getBoolean("is_up_to_up") : false);
                itemTemp.strRequire = (doc.getInteger("power_require") != null ? doc.getInteger("power_require") : 0);
                ITEM_TEMPLATES.add(itemTemp);
            }
            
            
            Log.success("Load map item template thành công (" + ITEM_TEMPLATES.size() + ")");

            // load item option template
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("item_option_template").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                optionTemp.name = doc.getString("name");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            
            
            Log.success("Load map item option template thành công (" + ITEM_OPTION_TEMPLATES.size() + ")");

            // load shop
            SHOPS = ShopDAO.getShops(con);
            Log.success("Load shop thành công (" + SHOPS.size() + ")");

            // load reward lucky round
            File folder = new File("resources/khanhdtk/data/data_lucky_round_reward");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    String line = Files.readAllLines(fileEntry.toPath()).get(0);
                    JSONArray jdata = (JSONArray) JSONValue.parse(line);
                    double sum = 0;
                    for (int i = 0; i < jdata.size(); i++) {
                        JSONObject obj = (JSONObject) jdata.get(i);
                        int id = ((Long) obj.get("id")).intValue();
                        double percent = ((Double) obj.get("percent"));
                        JSONArray jOptions = (JSONArray) obj.get("options");
                        ItemLuckyRound item = new ItemLuckyRound();
                        item.temp = ItemService.gI().getTemplate(id);
                        item.percent = percent;
                        sum += percent;
                        for (int j = 0; j < jOptions.size(); j++) {
                            JSONObject jOption = (JSONObject) jOptions.get(j);
                            int oID = ((Long) jOption.get("id")).intValue();
                            String strParam = (String) jOption.get("param");
                            ItemOptionLuckyRound io = new ItemOptionLuckyRound();
                            ItemOption itemOption = new ItemOption(oID, 0);
                            io.itemOption = itemOption;
                            String[] param = strParam.split("-");
                            io.param1 = Integer.parseInt(param[0]);
                            if (param.length == 2) {
                                io.param2 = Integer.parseInt(param[1]);
                            }
                            item.itemOptions.add(io);
                        }
                        LUCKY_ROUND_REWARDS.add(percent, item);
                    }
                    LUCKY_ROUND_REWARDS.add(((double) 100) - sum, null);
                    Log.success("Load reward lucky round thành công! " + sum);
                }
            }

            // load reward mob
            folder = new File("resources/khanhdtk/data/data_mob_reward");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        line = line.replaceAll("[{}\\[\\]]", "");
                        String[] arrSub = line.split("\\|");
                        int tempId = Integer.parseInt(arrSub[0]);
                        boolean haveMobReward = false;
                        MobReward mobReward = null;
                        for (MobReward m : MOB_REWARDS) {
                            if (m.tempId == tempId) {
                                mobReward = m;
                                haveMobReward = true;
                                break;
                            }
                        }
                        if (!haveMobReward) {
                            mobReward = new MobReward();
                            mobReward.tempId = tempId;
                            MOB_REWARDS.add(mobReward);
                        }
                        for (int i = 1; i < arrSub.length; i++) {
                            String[] dataItem = arrSub[i].split(",");
                            String[] mapsId = dataItem[0].split(";");

                            String[] itemId = dataItem[1].split(";");
                            for (int j = 0; j < itemId.length; j++) {
                                ItemReward itemReward = new ItemReward();
                                itemReward.mapId = new int[mapsId.length];
                                for (int k = 0; k < mapsId.length; k++) {
                                    itemReward.mapId[k] = Integer.parseInt(mapsId[k]);
                                }
                                itemReward.tempId = Integer.parseInt(itemId[j]);
                                itemReward.ratio = Integer.parseInt(dataItem[2]);
                                itemReward.typeRatio = Integer.parseInt(dataItem[3]);
                                itemReward.forAllGender = Integer.parseInt(dataItem[4]) == 1;
                                if (itemReward.tempId == 76
                                        || itemReward.tempId == 188
                                        || itemReward.tempId == 189
                                        || itemReward.tempId == 190) {
                                    mobReward.goldRewards.add(itemReward);
                                } else if (itemReward.tempId == 380) {
                                    mobReward.capsuleKyBi.add(itemReward);
                                } else if (itemReward.tempId >= 663 && itemReward.tempId <= 667) {
                                    mobReward.foods.add(itemReward);
                                } else if (itemReward.tempId == 590) {
                                    mobReward.biKieps.add(itemReward);
                                } else {
                                    mobReward.itemRewards.add(itemReward);
                                }
                            }
                        }
                    }
                }
            }
            Log.success("Load reward lucky round thành công (" + MOB_REWARDS.size() + ")");
            // load mob template
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("mob_template").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = (doc.getInteger("id") != null ? doc.getInteger("id").byteValue() : 0);
                mobTemp.type = (doc.getInteger("type") != null ? doc.getInteger("type").byteValue() : 0);
                mobTemp.name = doc.getString("name");
                mobTemp.hp = (doc.getInteger("hp") != null ? doc.getInteger("hp") : 0);
                mobTemp.rangeMove = (doc.getInteger("range_move") != null ? doc.getInteger("range_move").byteValue() : 0);
                mobTemp.speed = (doc.getInteger("speed") != null ? doc.getInteger("speed").byteValue() : 0);
                mobTemp.dartType = (doc.getInteger("dart_type") != null ? doc.getInteger("dart_type").byteValue() : 0);
                mobTemp.percentDame = (doc.getInteger("percent_dame") != null ? doc.getInteger("percent_dame").byteValue() : 0);
                mobTemp.percentTiemNang = (doc.getInteger("percent_tiem_nang") != null ? doc.getInteger("percent_tiem_nang").byteValue() : 0);
                MOB_TEMPLATES.add(mobTemp);
            }
            
            
            Log.success("Load mob template thành công (" + MOB_TEMPLATES.size() + ")");

            // load npc template
            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("npc_template").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = (doc.getInteger("id") != null ? doc.getInteger("id").byteValue() : 0);
                npcTemp.name = doc.getString("name");
                npcTemp.head = (doc.getInteger("head") != null ? doc.getInteger("head").shortValue() : 0);
                npcTemp.body = (doc.getInteger("body") != null ? doc.getInteger("body").shortValue() : 0);
                npcTemp.leg = (doc.getInteger("leg") != null ? doc.getInteger("leg").shortValue() : 0);
                NPC_TEMPLATES.add(npcTemp);
            }
            
            
            Log.success("Load npc template thành công (" + NPC_TEMPLATES.size() + ")");

            initMap();

            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("img_by_name").find().iterator();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                IMAGES_BY_NAME.put(doc.getString("name"), (doc.getInteger("n_frame") != null ? doc.getInteger("n_frame").byteValue() : 0));
            }
            Log.success("Thông báo tải dữ liệu images by name thành công (" + IMAGES_BY_NAME.size() + ")");

            // load clan
            ps = con.prepareStatement("select * from clan_sv" + SERVER);
            rs = ps.executeQuery();
            while (rs.hasNext()) { org.bson.Document doc = rs.next();
                Clan clan = new Clan();
                clan.id = (doc.getInteger("id") != null ? doc.getInteger("id") : 0);
                clan.name = doc.getString("name");
                clan.slogan = doc.getString("slogan");
                clan.imgId = (doc.getInteger("img_id") != null ? doc.getInteger("img_id").byteValue() : 0);
                clan.powerPoint = (doc.getLong("power_point") != null ? doc.getLong("power_point") : 0L);
                clan.maxMember = (doc.getInteger("max_member") != null ? doc.getInteger("max_member").byteValue() : 0);
                clan.clanPoint = (doc.getInteger("clan_point") != null ? doc.getInteger("clan_point") : 0);
                clan.level = (doc.getInteger("level") != null ? doc.getInteger("level").byteValue() : 0);
                clan.createTime = (int) (new java.sql.Timestamp(doc.getDate("create_time").getTime()).getTime() / 1000);

                dataArray = (JSONArray) jv.parse(doc.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) jv.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (Exception e) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray = (JSONArray) JSONValue.parse(doc.getString("thanhTichBDKB"));
                if (!dataArray.isEmpty()) {
                    clan.levelDoneBanDoKhoBau = Integer.parseInt(String.valueOf(dataArray.get(0)));
                    clan.thoiGianHoanThanhBDKB = Long.parseLong(String.valueOf(dataArray.get(1)));
                }
                CLANS.add(clan);
                dataArray.clear();
                dataObject.clear();
            }
            
            

            com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("clan_sv" + SERVER).find().sort(com.mongodb.client.model.Sorts.descending("id")).limit(1).iterator();
            if (rs.next()) {
                Clan.NEXT_ID = (doc.getInteger("id") != null ? doc.getInteger("id") : 0) + 1;
            }

            
            

            Log.success("Load clan thành công (" + CLANS.size() + "), clan next id: " + Clan.NEXT_ID);

            try {
                if (rs != null) {
                    
                }
                if (ps != null) {
                    
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
            CardManager.getInstance().load();
            PowerLimitManager.getInstance().load();
            CaptionManager.getInstance().load();
            AttributeTemplateManager.getInstance().load();
            loadAttributeServer();
            loadEventCount();
            EffectEventManager.gI().load();
            NotiManager.getInstance().load();
            KiGuiManager.getInstance().load();
            AchiveManager.getInstance().load();
            MiniPetManager.gI().load();
            PetFollowManager.gI().load();
        } catch (Exception e) {
            Log.error(Manager.class, e, "Lỗi load database");
            System.exit(0);
        }

        Log.log(
                "Tổng thời gian load database: " + (System.currentTimeMillis() - st) + "(ms)");
    }

    public static MapTemplate getMapTemplate(int mapID) {
        for (MapTemplate map : MAP_TEMPLATES) {
            if (map.id == mapID) {
                return map;
            }
        }
        return null;
    }

    public static void loadEventCount() {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("event");
            org.bson.Document rs = collection.find(com.mongodb.client.model.Filters.eq("server", SERVER)).first();
            if (rs != null) {
                EVENT_COUNT_QUY_LAO_KAME = rs.getInteger("kame", 0);
                EVENT_COUNT_THAN_HUY_DIET = rs.getInteger("bill", 0);
                EVENT_COUNT_THAN_MEO = rs.getInteger("karin", 0);
                EVENT_COUNT_THUONG_DE = rs.getInteger("thuongde", 0);
                EVENT_COUNT_THAN_VU_TRU = rs.getInteger("thanvutru", 0);
            }
        } catch (Exception e) {
        }
    }

    public void updateEventCount() {
        try {
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("event");
            org.bson.conversions.Bson updates = com.mongodb.client.model.Updates.combine(
                com.mongodb.client.model.Updates.set("kame", EVENT_COUNT_QUY_LAO_KAME),
                com.mongodb.client.model.Updates.set("bill", EVENT_COUNT_THAN_HUY_DIET),
                com.mongodb.client.model.Updates.set("karin", EVENT_COUNT_THAN_MEO),
                com.mongodb.client.model.Updates.set("thuongde", EVENT_COUNT_THUONG_DE),
                com.mongodb.client.model.Updates.set("thanvutru", EVENT_COUNT_THAN_VU_TRU)
            );
            collection.updateOne(com.mongodb.client.model.Filters.eq("server", SERVER), updates);
        } catch (Exception ex) {
        }
    }

    public void loadAttributeServer() {
        try {
            nro.attr.AttributeManager am = new nro.attr.AttributeManager();
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("attribute_server");
            com.mongodb.client.MongoCursor<org.bson.Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                org.bson.Document rs = cursor.next();
                int id = rs.getInteger("id", 0);
                int templateID = rs.getInteger("attribute_template_id", 0);
                int value = rs.getInteger("value", 0);
                int time = rs.getInteger("time", 0);
                nro.attr.Attribute at = nro.attr.Attribute.builder()
                        .id(id)
                        .templateID(templateID)
                        .value(value)
                        .time(time)
                        .build();
                am.add(at);
            }
            ServerManager.gI().setAttributeManager(am);
        } catch (Exception ex) {
        }
    }

    public void updateAttributeServer() {
        try {
            nro.attr.AttributeManager am = ServerManager.gI().getAttributeManager();
            java.util.List<nro.attr.Attribute> attributes = am.getAttributes();
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("attribute_server");
            synchronized (attributes) {
                for (nro.attr.Attribute at : attributes) {
                    try {
                        if (at.isChanged()) {
                            org.bson.conversions.Bson updates = com.mongodb.client.model.Updates.combine(
                                com.mongodb.client.model.Updates.set("attribute_template_id", at.getTemplate().getId()),
                                com.mongodb.client.model.Updates.set("value", at.getValue()),
                                com.mongodb.client.model.Updates.set("time", at.getTime())
                            );
                            collection.updateOne(com.mongodb.client.model.Filters.eq("id", at.getId()), updates);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("resources/config/server.properties"));
        Object value = null;
        // ###Config db
        if ((value = properties.get("server.db.driver")) != null) {
            DBService.DRIVER = String.valueOf(value);
        }
        if ((value = properties.get("server.db.ip")) != null) {
            DBService.DB_HOST = String.valueOf(value);
        }
        if ((value = properties.get("server.db.port")) != null) {
            DBService.DB_PORT = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.db.name")) != null) {
            DBService.DB_NAME = String.valueOf(value);
        }
        if ((value = properties.get("server.db.us")) != null) {
            DBService.DB_USER = String.valueOf(value);
        }
        if ((value = properties.get("server.db.pw")) != null) {
            DBService.DB_PASSWORD = String.valueOf(value);
        }
        if ((value = properties.get("server.db.max")) != null) {
            DBService.MAX_CONN = Integer.parseInt(String.valueOf(value));
        }
        if (properties.containsKey("login.host")) {
            loginHost = properties.getProperty("login.host");
        } else {
            loginHost = "127.0.0.1";
        }
        if (properties.containsKey("update.timelogin")) {
            ServerManager.updateTimeLogin = Boolean.parseBoolean(properties.getProperty("update.timelogin"));
        }

        if (properties.containsKey("execute.command")) {
            executeCommand = properties.getProperty("execute.command");
        }

        // ###Config sv
        if ((value = properties.get("server.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.name")) != null) {
            ServerManager.NAME = String.valueOf(value);
        }
        if ((value = properties.get("server.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        if (properties.containsKey("server.debug")) {
            debug = Boolean.parseBoolean(properties.getProperty("server.debug"));
        } else {
            debug = false;
        }
        if ((value = properties.get("api.key")) != null) {
            Manager.apiKey = String.valueOf(value);
        }
        if ((value = properties.get("api.port")) != null) {
            Manager.apiPort = Integer.parseInt(String.valueOf(value));
        }
        String linkServer = "";
        for (int i = 1; i <= 10; i++) {
            value = properties.get("server.sv" + i);
            if (value != null) {
                linkServer += String.valueOf(value) + ":0,";
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.waitlogin")) != null) {
            SECOND_WAIT_LOGIN = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxperip")) != null) {
            MAX_PER_IP = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.expserver")) != null) {
            RATE_EXP_SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.event")) != null) {
            EVENT_SEVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.name")) != null) {
            SERVER_NAME = String.valueOf(value);
        }
        if ((value = properties.get("server.domain")) != null) {
            DOMAIN = String.valueOf(value);
        }
        if ((value = properties.get("server.key")) != null) {
            KEY_SERVER = String.valueOf(value);
        }
        if ((value = properties.get("server.key2")) != null) {
            KEY_SERVER_2 = String.valueOf(value);
        }
        if (properties.containsKey("server.activeKey")) {
            activeKey = Boolean.parseBoolean(properties.getProperty("server.activeKey"));
        } else {
            activeKey = false;
        }
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("resources/khanhdtk/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(MapService.class,
                    e);
        }
        return tileIndexTileType;
    }

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("resources/map/" + mapId));
            int w = dis.readByte();
            int h = dis.readByte();
            tileMap = new int[h][w];
            for (int i = 0; i < tileMap.length; i++) {
                for (int j = 0; j < tileMap[i].length; j++) {
                    tileMap[i][j] = dis.readByte();
                }
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tileMap;
    }

    // service*******************************************************************
    public static Clan getClanById(int id) throws Exception {
        for (Clan clan : CLANS) {
            if (clan.id == id) {
                return clan;
            }
        }
        throw new Exception("Không tìm thấy clan id: " + id);
    }

    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static CaiTrang getCaiTrangByItemId(int itemId) {
        for (CaiTrang caiTrang : CAI_TRANGS) {
            if (caiTrang.tempId == itemId) {
                return caiTrang;
            }
        }
        return null;
    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

}
