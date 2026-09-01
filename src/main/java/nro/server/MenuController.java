package nro.server;

import java.io.IOException;
import nro.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.npc.NpcManager;
import nro.server.io.Session;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.func.TransactionService;

public class MenuController {

    private static MenuController instance;

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new MenuController();
        }
        return instance;
    }

    public void openMenuNPC(Session session, int idnpc, Player player) {
        System.out.println("[NPC MENU] openMenuNPC: " + idnpc + " Player: " + (player != null ? player.name : "null"));
        try {
            TransactionService.gI().cancelTrade(player);
            Npc npc = null;

            if (idnpc == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                npc = NpcManager.getNpc(ConstNpc.CALICK);
            } else if (idnpc == 54) {
                npc = NpcManager.getNpc((byte) 54);
            } else {
                npc = player.zone.map.getNpc(player, idnpc);
            }
            if (npc != null) {
                npc.openBaseMenu(player);
            } else {
                Service.getInstance().hideWaitDialog(player);
            }
        } catch (Exception e) {
            System.err.println("[ERROR in openMenuNPC " + idnpc + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void doSelectMenu(Player player, int npcId, int select) throws IOException {
        System.out.println("[NPC MENU] doSelectMenu: npcId=" + npcId + ", select=" + select + ", Player: " + (player != null ? player.name : "null"));
        TransactionService.gI().cancelTrade(player);
        try {
            switch (npcId) {
                case ConstNpc.RONG_THIENG:
                case ConstNpc.CON_MEO:
                    NpcManager.getNpc((byte) npcId).confirmMenu(player, select);
                    break;

                default:
                    Npc npc = null;
                    if (npcId == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                        npc = NpcManager.getNpc(ConstNpc.CALICK);
                    } else {
                        npc = player.zone.map.getNpc(player, npcId);
                    }
                    if (npc != null) {
                        npc.confirmMenu(player, select);
                    } else {
                        if (npcId == 54) {
                            npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
                            npc.confirmMenu(player, select);
                        } else {
                            Service.getInstance().hideWaitDialog(player);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("[ERROR in doSelectMenu npc=" + npcId + ", select=" + select + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
