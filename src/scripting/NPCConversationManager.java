
package scripting;

import client.*;
import client.inventory.*;

import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.world.*;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.script.Invocable;

import server.*;
import server.MapleCarnivalChallenge;
import server.MapleCarnivalParty;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.MapleSquad;
import server.Randomizer;
import server.Timer.CloneTimer;
import server.Timer.MapTimer;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.maps.AramiaFireWorks;
import server.maps.Event_DojoAgent;
import server.maps.Event_PyramidSubway;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SpeedRunType;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.packet.PlayerShopPacket;

/**
 * @author zjj
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private MapleClient c;
    private int npc, questid;
    private String getText;
    private byte type; // -1 = NPC, 0 = start quest, 1 = end quest
    private byte lastMsg = -1;

    /**
     *
     */
    public boolean pendingDisposal = false;
    private Invocable iv;
    private int wh = 0;
    public int hyt = 0,

    /**
     *
     */
    dxs = 0;


    /*
     * public NPCConversationManager(MapleClient c, int npc, int questid, byte
     * type, Invocable iv) { super(c); this.c = c; this.npc = npc; this.questid
     * = questid; this.type = type; this.iv = iv; }
     */

    /**
     * @param c
     * @param npc
     * @param questid
     * @param type
     * @param iv
     * @param wh
     */

    public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv, int wh) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.questid = questid;
        this.type = type;
        this.iv = iv;
        this.wh = wh;
    }

    /**
     * @return
     */
    public int getwh() {
        return this.wh;
    }

    /**
     * @return
     */
    public Invocable getIv() {
        return iv;
    }

    /**
     * @return
     */
    public String serverName() {
        return c.getChannelServer().getServerName();
    }

    /**
     * @return
     */
    public int getNpc() {
        return npc;
    }

    /**
     * @return
     */
    public int getQuest() {
        return questid;
    }

    /**
     * @return
     */
    public byte getType() {
        return type;
    }

    /**
     *
     */
    public void safeDispose() {
        pendingDisposal = true;
    }

    /**
     *
     */
    public void dispose() {
        NPCScriptManager.getInstance().dispose(c);
    }

    /**
     * @param sel
     */
    public void askMapSelection(final String sel) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(MaplePacketCreator.getMapSelection(npc, sel));
        lastMsg = 0xD;
    }

    /**
     * @param text
     */
    public void sendNext(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { //sendNext will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
        lastMsg = 0;
    }

    /**
     * @param text
     * @param type
     */
    public void sendNextS(String text, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", type));
        lastMsg = 0;
    }

    /**
     * @param text
     */
    public void sendPrev(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
        lastMsg = 0;
    }

    /**
     * @param text
     * @param type
     */
    public void sendPrevS(String text, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", type));
        lastMsg = 0;
    }

    /**
     * @param text
     */
    public void sendNextPrev(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
        lastMsg = 0;
    }

    /**
     * @param text
     */
    public void PlayerToNpc(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    /**
     * @param text
     */
    public void sendNextPrevS(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    /**
     * @param text
     * @param type
     */
    public void sendNextPrevS(String text, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", type));
        lastMsg = 0;
    }

    /**
     * @param text
     */
    public void sendOk(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
        lastMsg = 0;
    }

    /**
     * @param text
     * @param type
     */
    public void sendOkS(String text, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", type));
        lastMsg = 0;
    }

    /**
     * @param text
     */
    public void sendYesNo(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", (byte) 0));
        lastMsg = 1;
    }

    /**
     * @param text
     * @param type
     */
    public void sendYesNoS(String text, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", type));
        lastMsg = 1;
    }

    /**
     * @param text
     */
    public void sendAcceptDecline(String text) {
        askAcceptDecline(text);
    }

    /**
     * @param text
     */
    public void sendAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text);
    }

    /**
     * @param text
     */
    public void askAcceptDecline(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0B, text, "", (byte) 0));
        lastMsg = 0xB;
    }

    /**
     * @param text
     */
    public void askAcceptDeclineNoESC(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
        lastMsg = 0xC;
    }

    /**
     * @param text
     * @param card
     * @param args
     */
    public void askAvatar(String text, int card, int... args) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, card, args));
        lastMsg = 7;
    }

    /**
     * @param text
     */
    public void sendSimple(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) { //sendSimple will dc otherwise!
            sendNext(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", (byte) 0));
        lastMsg = 4;
    }

    /**
     * @param text
     * @param type
     */
    public void sendSimpleS(String text, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) { //sendSimple will dc otherwise!
            sendNextS(text, type);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", (byte) type));
        lastMsg = 4;
    }

    /**
     * @param text
     * @param caid
     * @param styles
     */
    public void sendStyle(String text, int caid, int styles[]) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, caid, styles));
        lastMsg = 7;
    }

    /**
     * @param text
     * @param def
     * @param min
     * @param max
     */
    public void sendGetNumber(String text, int def, int min, int max) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
        lastMsg = 3;
    }

    /**
     * @param slot
     * @param lock
     * @return
     */
    public IItem lockitem(int slot, boolean lock) {
        byte set = 0;
        byte eqslot = (byte) slot;
        Equip nEquip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(eqslot);
        if (nEquip != null) {
            if (lock) {
                set = 1;
                c.getPlayer().dropMessage("[系统信息] 物品位置 " + slot + " 锁定成功");
            } else {
                c.getPlayer().dropMessage("[系统信息] 物品位置 " + slot + " 解除锁定成功");
            }
            nEquip.setFlag(set);
            c.getSession().write(MaplePacketCreator.getCharInfo(c.getPlayer()));
            getMap().removePlayer(c.getPlayer());
            getMap().addPlayer(c.getPlayer());
        } else {
            c.getPlayer().dropMessage("[系统信息] 物品位置 " + slot + " 装备为空.");
        }
        return nEquip;
    }

    /**
     * @param slot
     * @param lock
     * @return
     */
    public IItem itemqh(int slot, byte lock) {
        byte eqslot = (byte) slot;
        Equip nEquip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(eqslot);
        if (nEquip != null && nEquip.getLevel() + nEquip.getUpgradeSlots() + lock <= 127) {
            nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() + lock));
            c.getPlayer().dropMessage("[系统信息] 物品位置 " + slot + " 强化次数成功");
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("剩余强化次数：" + (127 - (nEquip.getLevel() + nEquip.getUpgradeSlots())));
            }
            c.getSession().write(MaplePacketCreator.getCharInfo(c.getPlayer()));
            getMap().removePlayer(c.getPlayer());
            getMap().addPlayer(c.getPlayer());
        } else if (nEquip.getLevel() + nEquip.getUpgradeSlots() + lock > 127) {
            c.getPlayer().dropMessage("[系统信息] 物品位置 " + slot + " 装备的强化次数已经满了.");
        } else {
            c.getPlayer().dropMessage("[系统信息] 物品位置 " + slot + " 装备为空.");
        }
        return nEquip;
    }

    /**
     * @param text
     */
    public void sendGetText(String text) {
        if (lastMsg > -1) {
            return;
        }
        if (text.length() > 13) {
            c.getPlayer().dropMessage(1, "中文6字以下或者英文12字以下\r\n否则无效！");
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(MaplePacketCreator.getNPCTalkText(npc, text));
        lastMsg = 2;
    }

    /**
     * @param text
     * @param type
     */
    public void sendCY1(String text, byte type) {
        getClient().getSession().write(MaplePacketCreator.getCY1(npc, text, type));
    }

    /**
     * @param text
     * @param type
     */
    public void sendCY2(String text, byte type) {
        getClient().getSession().write(MaplePacketCreator.getCY2(npc, text, type));
    }

    /**
     * @param text
     */
    public void setGetText(String text) {
        this.getText = text;
    }

    /**
     * @return
     */
    public String getText() {
        return getText;
    }

    /**
     * @param hair
     */
    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    /**
     * @param face
     */
    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    /**
     * @param color
     */
    public void setSkin(int color) {
        getPlayer().setSkinColor((byte) color);
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    /**
     * @param ticket
     * @param args_all
     * @return
     */
    public int setRandomAvatar(int ticket, int[] args_all) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        int args = args_all[Randomizer.nextInt(args_all.length)];
        if (args < 100) {
            c.getPlayer().setSkinColor((byte) args);
            c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            c.getPlayer().setFace(args);
            c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            c.getPlayer().setHair(args);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    /**
     * @param ticket
     * @param args
     * @return
     */
    public int setAvatar(int ticket, int args) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        if (args < 100) {
            c.getPlayer().setSkinColor((byte) args);
            c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            c.getPlayer().setFace(args);
            c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            c.getPlayer().setHair(args);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    /**
     *
     */
    public void sendStorage() {
        c.getPlayer().setConversation(4);
        c.getPlayer().getStorage().sendStorage(c, npc);
    }

    /**
     * @param id
     */
    public void openShop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(c);
    }

    /**
     * @param id
     * @param quantity
     * @return
     */
    public int gainGachaponItem(int id, int quantity) {
        return gainGachaponItem(id, quantity, c.getPlayer().getMap().getStreetName() + " - " + c.getPlayer().getMap().getMapName());
    }

    /**
     * @param id
     * @param quantity
     * @param msg
     * @return
     */
    public int gainGachaponItem(int id, int quantity, final String msg) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity, 0);

            if (item == null) {
                return -1;
            }
            final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
            if (rareness > 0) {
                World.Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[" + msg + "] " + c.getPlayer().getName(), " : Lucky winner of Gachapon!", item, rareness, getPlayer().getClient().getChannel()).getBytes());
            }
            return item.getItemId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param id
     * @param quantity
     * @param msg
     * @param 概率
     * @param 时间
     * @return
     */
    public int gainGachaponItem(int id, int quantity, final String msg, int 概率, long 时间) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                getPlayer().dropMessage(5, "gainGachaponItem itemExists(id) == -1");
                return -1;
            }
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity, 时间);

            if (item == null) {
                getPlayer().dropMessage(5, "gainGachaponItem item == -1");
                return -1;
            }
            if (概率 > 0) {
                World.Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[" + msg + "] " + c.getPlayer().getName(), " : Lucky winner of Gachapon!", item, (byte) 0, getPlayer().getClient().getChannel()).getBytes());
            }
            return item.getItemId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param id
     * @param quantity
     * @param msg
     * @param 概率
     * @return
     */
    public int gainGachaponItem(int id, int quantity, final String msg, int 概率) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                getPlayer().dropMessage(5, "gainGachaponItem itemExists(id) == -1");
                return -1;
            }
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity, 0);

            if (item == null) {
                getPlayer().dropMessage(5, "gainGachaponItem item == -1");
                return -1;
            }
            if (概率 > 0) {
                World.Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[" + msg + "] " + c.getPlayer().getName(), " : Lucky winner of Gachapon!", item, (byte) 0, getPlayer().getClient().getChannel()).getBytes());
            }
            return item.getItemId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param job
     */
    public void changeJob(int job) {
        c.getPlayer().changeJob(job);
    }

    /**
     * @param id
     */
    public void startQuest(int id) {
        MapleQuest.getInstance(id).start(getPlayer(), npc);
    }

    /**
     * @param id
     */
    @Override
    public void completeQuest(int id) {
        MapleQuest.getInstance(id).complete(getPlayer(), npc);
    }

    /**
     * @param id
     */
    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    /**
     *
     */
    public void forceStartQuest() {
        MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), null);
    }

    /**
     * @param id
     */
    @Override
    public void forceStartQuest(int id) {
        MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc(), null);
    }

    /**
     * @param customData
     */
    public void forceStartQuest(String customData) {
        MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), customData);
    }

    /**
     *
     */
    public void forceCompleteQuest() {
        MapleQuest.getInstance(questid).forceComplete(getPlayer(), getNpc());
    }

    /**
     * @param id
     */
    @Override
    public void forceCompleteQuest(final int id) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), getNpc());
    }

    /**
     * @return
     */
    public String getQuestCustomData() {
        return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).getCustomData();
    }

    /**
     * @param customData
     */
    public void setQuestCustomData(String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(customData);
    }

    /**
     * @return
     */
    public int getLevel() {
        return getPlayer().getLevel();
    }

    /**
     * @return
     */
    public int getMeso() {
        return getPlayer().getMeso();
    }

    /**
     * @param amount
     */
    public void gainAp(final int amount) {
        c.getPlayer().gainAp((short) amount);
    }

    /**
     * @param type
     * @param amt
     */
    public void expandInventory(byte type, int amt) {
        c.getPlayer().expandInventory(type, amt);
    }

    /**
     *
     */
    public void unequipEverything() {
        MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Short> ids = new LinkedList<>();
        for (IItem item : equipped.list()) {
            ids.add(item.getPosition());
        }
        for (short id : ids) {
            MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
        }
    }

    /**
     *
     */
    public final void clearSkills() {
        Map<ISkill, SkillEntry> skills = getPlayer().getSkills();
        for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
            getPlayer().changeSkillLevel(skill.getKey(), (byte) 0, (byte) 0);
        }
    }

    /**
     * @param skillid
     * @return
     */
    public boolean hasSkill(int skillid) {
        ISkill theSkill = SkillFactory.getSkill(skillid);
        if (theSkill != null) {
            return c.getPlayer().getSkillLevel(theSkill) > 0;
        }
        return false;
    }

    /**
     * @param broadcast
     * @param effect
     */
    public void showEffect(boolean broadcast, String effect) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
        } else {
            c.getSession().write(MaplePacketCreator.showEffect(effect));
        }
    }

    /**
     * @param broadcast
     * @param sound
     */
    public void playSound(boolean broadcast, String sound) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
        } else {
            c.getSession().write(MaplePacketCreator.playSound(sound));
        }
    }

    /**
     * @param broadcast
     * @param env
     */
    public void environmentChange(boolean broadcast, String env) {//环境变化
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, 2));
        } else {
            c.getSession().write(MaplePacketCreator.environmentChange(env, 2));
        }
    }

    /**
     * @param capacity
     */
    public void updateBuddyCapacity(int capacity) {
        c.getPlayer().setBuddyCapacity((byte) capacity);
    }

    /**
     * @return
     */
    public int getBuddyCapacity() {
        return c.getPlayer().getBuddyCapacity();
    }

    /**
     * @return
     */
    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2 : getPlayer().getMap().getCharactersThreadsafe()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    /**
     * @return
     */
    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<>(); // creates an empty array full of shit..
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                if (ch != null) { // double check <3
                    chars.add(ch);
                }
            }
        }
        return chars;
    }

    /**
     * @param mapId
     * @param exp
     */
    public void warpPartyWithExp(int mapId, int exp) {
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
            }
        }
    }

    /**
     * @param mapId
     * @param exp
     * @param meso
     */
    public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
                curChar.gainMeso(meso, true);
            }
        }
    }

    /**
     * @param type
     * @return
     */
    public MapleSquad getSquad(String type) {
        return c.getChannelServer().getMapleSquad(type);
    }

    /**
     * @param type
     * @return
     */
    public int getSquadAvailability(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        return squad.getStatus();
    }

    /**
     * @param type
     * @param minutes
     * @param startText
     * @return
     */
    public boolean registerSquad(String type, int minutes, String startText) {
        if (c.getChannelServer().getMapleSquad(type) == null) {
            final MapleSquad squad = new MapleSquad(c.getChannel(), type, c.getPlayer(), minutes * 60 * 1000, startText);
            final boolean ret = c.getChannelServer().addMapleSquad(squad, type);
            if (ret) {
                final MapleMap map = c.getPlayer().getMap();

                map.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
                map.broadcastMessage(MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + startText));
            } else {
                squad.clear();
            }
            return ret;
        }
        return false;
    }

    /**
     * @param type
     * @param type_
     * @return
     */
    public boolean getSquadList(String type, byte type_) {
//        try {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return false;
        }
        switch (type_) {
            case 0:
            case 3:
                // Normal viewing
                sendNext(squad.getSquadMemberString(type_));
                break;
            case 1:
                // Squad Leader banning, Check out banned participant
                sendSimple(squad.getSquadMemberString(type_));
                break;
            case 2:
                if (squad.getBannedMemberSize() > 0) {
                    sendSimple(squad.getSquadMemberString(type_));
                } else {
                    sendNext(squad.getSquadMemberString(type_));
                }
                break;
            default:
                break;
        }
        return true;
        /*
         * } catch (NullPointerException ex) {
         * FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
         * return false; }
         */
    }

    /**
     * @param type
     * @return
     */
    public byte isSquadLeader(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        } else if (squad.getLeader() != null && squad.getLeader().getId() == c.getPlayer().getId()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * @param eim
     * @param squad
     * @return
     */
    public boolean reAdd(String eim, String squad) {
        EventInstanceManager eimz = getDisconnected(eim);
        MapleSquad squadz = getSquad(squad);
        if (eimz != null && squadz != null) {
            squadz.reAddMember(getPlayer());
            eimz.registerPlayer(getPlayer());
            return true;
        }
        return false;
    }

    /**
     * @param type
     * @param pos
     */
    public void banMember(String type, int pos) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(pos);
        }
    }

    /**
     * @param type
     * @param pos
     */
    public void acceptMember(String type, int pos) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.acceptMember(pos);
        }
    }

    /**
     * @param startMillis
     * @param endMillis
     * @return
     */
    public String getReadableMillis(long startMillis, long endMillis) {
        return StringUtil.getReadableMillis(startMillis, endMillis);
    }

    /**
     * @param type
     * @param join
     * @return
     */
    public int addMember(String type, boolean join) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.addMember(c.getPlayer(), join);
        }
        return -1;
    }

    /**
     * @param type
     * @return
     */
    public byte isSquadMember(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        } else if (squad.getMembers().contains(c.getPlayer())) {
            return 1;
        } else if (squad.isBanned(c.getPlayer())) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     *
     */
    public void resetReactors() {
        getPlayer().getMap().resetReactors();
    }

    /**
     * @param code
     */
    public void genericGuildMessage(int code) {
        c.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    /**
     *
     */
    public void disbandGuild() {
        final int gid = c.getPlayer().getGuildId();
        if (gid <= 0 || c.getPlayer().getGuildRank() != 1) {
            return;
        }
        World.Guild.disbandGuild(gid);
    }

    /**
     *
     */
    public void increaseGuildCapacity() {
        if (c.getPlayer().getMeso() < 2500000) {
            c.getSession().write(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
            return;
        }
        final int gid = c.getPlayer().getGuildId();
        if (gid <= 0) {
            return;
        }
        World.Guild.increaseGuildCapacity(gid);
        c.getPlayer().gainMeso(-2500000, true, false, true);
    }

    /**
     *
     */
    public void displayGuildRanks() {
        c.getSession().write(MaplePacketCreator.showGuildRanks(npc, MapleGuildRanking.getInstance().getRank()));
    }

    /**
     * @return
     */
    public boolean removePlayerFromInstance() {
        if (c.getPlayer().getEventInstance() != null) {
            c.getPlayer().getEventInstance().removePlayer(c.getPlayer());
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    public boolean isPlayerInstance() {
        return c.getPlayer().getEventInstance() != null;
    }

    /**
     * @param slot
     * @param type
     * @param amount
     */
    public void changeStat(byte slot, int type, short amount) {
        Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        switch (type) {
            case 0:
                sel.setStr(amount);
                break;
            case 1:
                sel.setDex(amount);
                break;
            case 2:
                sel.setInt(amount);
                break;
            case 3:
                sel.setLuk(amount);
                break;
            case 4:
                sel.setHp(amount);
                break;
            case 5:
                sel.setMp(amount);
                break;
            case 6:
                sel.setWatk(amount);
                break;
            case 7:
                sel.setMatk(amount);
                break;
            case 8:
                sel.setWdef(amount);
                break;
            case 9:
                sel.setMdef(amount);
                break;
            case 10:
                sel.setAcc(amount);
                break;
            case 11:
                sel.setAvoid(amount);
                break;
            case 12:
                sel.setHands(amount);
                break;
            case 13:
                sel.setSpeed(amount);
                break;
            case 14:
                sel.setJump(amount);
                break;
            case 15:
                sel.setUpgradeSlots((byte) amount);
                break;
            case 16:
                sel.setViciousHammer((byte) amount);
                break;
            case 17:
                sel.setLevel((byte) amount);
                break;
            case 18:
                sel.setEnhance((byte) amount);
                break;
            case 19:
                sel.setPotential1(amount);
                break;
            case 20:
                sel.setPotential2(amount);
                break;
            case 21:
                sel.setPotential3(amount);
                break;
            case 22:
                sel.setOwner(getText());
                break;
            default:
                break;
        }
        c.getPlayer().equipChanged();
    }

    /**
     *
     */
    public void killAllMonsters() {
        MapleMap map = c.getPlayer().getMap();
        double range = Double.POSITIVE_INFINITY;
        MapleMonster mob;
        for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
            mob = (MapleMonster) monstermo;
            if (mob.getStats().isBoss()) {
                map.killMonster(mob, c.getPlayer(), false, false, (byte) 1);
            }
        }
        /*
         * int mapid = c.getPlayer().getMapId(); MapleMap map =
         * c.getChannelServer().getMapFactory().getMap(mapid);
         * map.killAllMonsters(true); // No drop.
         */
    }

    /**
     *
     */
    public void giveMerchantMesos() {
        long mesos = 0;
        try {
            Connection con = (Connection) DatabaseConnection.getConnection();
            PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
            } else {
                mesos = rs.getLong("mesos");
            }
            rs.close();
            ps.close();

            ps = (PreparedStatement) con.prepareStatement("UPDATE hiredmerchants SET mesos = 0 WHERE merchantid = ?");
            ps.setInt(1, getPlayer().getId());
            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            System.err.println("Error gaining mesos in hired merchant" + ex);
        }
        c.getPlayer().gainMeso((int) mesos, true);
    }

    /**
     *
     */
    public void dc() {
        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(c.getPlayer().getName());
        victim.getClient().getSession().close();
        victim.getClient().disconnect(true, false);

    }

    /**
     * @return
     */
    public long getMerchantMesos() {
        long mesos = 0;
        try {
            Connection con = (Connection) DatabaseConnection.getConnection();
            PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
            } else {
                mesos = rs.getLong("mesos");
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("Error gaining mesos in hired merchant" + ex);
        }
        return mesos;
    }

    /**
     *
     */
    public void openDuey() {
        c.getPlayer().setConversation(2);
        c.getSession().write(MaplePacketCreator.sendDuey((byte) 9, null));
    }

    /**
     *
     */
    public void openMerchantItemStore() {
        c.getPlayer().setConversation(3);
        c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x22));
    }

    /**
     *
     */
    public void openMerchantItemStore1() {
        final MerchItemPackage pack = loadItemFrom_Database(c.getPlayer().getId(), c.getPlayer().getAccountID());
        //c.getPlayer().setConversation(3);
        c.getSession().write(PlayerShopPacket.merchItemStore_ItemData(pack));
    }

    private static final MerchItemPackage loadItemFrom_Database(final int charid, final int accountid) {
        final Connection con = DatabaseConnection.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ? OR accountid = ?");
            ps.setInt(1, charid);
            ps.setInt(2, accountid);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                ps.close();
                rs.close();
                return null;
            }
            final int packageid = rs.getInt("PackageId");

            final MerchItemPackage pack = new MerchItemPackage();
            pack.setPackageid(packageid);
            pack.setMesos(rs.getInt("Mesos"));
            pack.setSentTime(rs.getLong("time"));

            ps.close();
            rs.close();

            Map<Integer, Pair<IItem, MapleInventoryType>> items = ItemLoader.HIRED_MERCHANT.loadItems(false, charid);
            if (items != null) {
                List<IItem> iters = new ArrayList<>();
                for (Pair<IItem, MapleInventoryType> z : items.values()) {
                    iters.add(z.left);
                }
                pack.setItems(iters);
            }

            return pack;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     */
    public void sendRepairWindow() {
        c.getSession().write(MaplePacketCreator.sendRepairWindow(npc));
    }

    /**
     * @return
     */
    public final int getDojoPoints() {
        return c.getPlayer().getDojo();
    }

    /**
     * @return
     */
    public final int getDojoRecord() {
        return c.getPlayer().getDojoRecord();
    }

    /**
     * @param reset
     */
    public void setDojoRecord(final boolean reset) {
        c.getPlayer().setDojoRecord(reset);
    }

    /**
     * @param dojo
     * @param party
     * @return
     */
    public boolean start_DojoAgent(final boolean dojo, final boolean party) {
        if (dojo) {
            return Event_DojoAgent.warpStartDojo(c.getPlayer(), party);
        }
        return Event_DojoAgent.warpStartAgent(c.getPlayer(), party);
    }

    /**
     * @param pyramid
     * @return
     */
    public boolean start_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpStartPyramid(c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpStartSubway(c.getPlayer());
    }

    /**
     * @param pyramid
     * @return
     */
    public boolean bonus_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpBonusPyramid(c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpBonusSubway(c.getPlayer());
    }

    /**
     * @return
     */
    public final short getKegs() {
        return AramiaFireWorks.getInstance().getKegsPercentage();
    }

    /**
     * @param kegs
     */
    public void giveKegs(final int kegs) {//未知副本
        AramiaFireWorks.getInstance().giveKegs(c.getPlayer(), kegs);
    }

    /**
     * @return
     */
    public final short getSunshines() {
        return AramiaFireWorks.getInstance().getSunsPercentage();
    }

    /**
     * @param kegs
     */
    public void addSunshines(final int kegs) {
        AramiaFireWorks.getInstance().giveSuns(c.getPlayer(), kegs);
    }

    /**
     * @return
     */
    public final short getDecorations() {
        return AramiaFireWorks.getInstance().getDecsPercentage();
    }

    /**
     * @param kegs
     */
    public void addDecorations(final int kegs) {
        try {
            AramiaFireWorks.getInstance().giveDecs(c.getPlayer(), kegs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param type
     * @return
     */
    public final MapleInventory getInventory(int type) {
        return c.getPlayer().getInventory(MapleInventoryType.getByType((byte) type));
    }

    /**
     * @return
     */
    public final MapleCarnivalParty getCarnivalParty() {
        return c.getPlayer().getCarnivalParty();
    }

    /**
     * @return
     */
    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return c.getPlayer().getNextCarnivalRequest();
    }

    /**
     * @param chr
     * @return
     */
    public final MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
        return new MapleCarnivalChallenge(chr);
    }

    /**
     * @param hp
     */
    public void setHP(short hp) {
        c.getPlayer().getStat().setHp(hp);
    }

    /**
     *
     */
    public void maxStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(2);
        c.getPlayer().getStat().setStr((short) 32767);
        c.getPlayer().getStat().setDex((short) 32767);
        c.getPlayer().getStat().setInt((short) 32767);
        c.getPlayer().getStat().setLuk((short) 32767);

        c.getPlayer().getStat().setMaxHp((short) 30000);
        c.getPlayer().getStat().setMaxMp((short) 30000);
        c.getPlayer().getStat().setHp((short) 30000);
        c.getPlayer().getStat().setMp((short) 30000);

        statup.add(new Pair<>(MapleStat.STR, Integer.valueOf(32767)));
        statup.add(new Pair<>(MapleStat.DEX, Integer.valueOf(32767)));
        statup.add(new Pair<>(MapleStat.LUK, Integer.valueOf(32767)));
        statup.add(new Pair<>(MapleStat.INT, Integer.valueOf(32767)));
        statup.add(new Pair<>(MapleStat.HP, Integer.valueOf(30000)));
        statup.add(new Pair<>(MapleStat.MAXHP, Integer.valueOf(30000)));
        statup.add(new Pair<>(MapleStat.MP, Integer.valueOf(30000)));
        statup.add(new Pair<>(MapleStat.MAXMP, Integer.valueOf(30000)));

        c.getSession().write(MaplePacketCreator.updatePlayerStats(statup, c.getPlayer().getJob()));
    }

    /**
     * @param typ
     * @return
     */
    public Pair<String, Map<Integer, String>> getSpeedRun(String typ) {
        final SpeedRunType type = SpeedRunType.valueOf(typ);
        if (SpeedRunner.getInstance().getSpeedRunData(type) != null) {
            return SpeedRunner.getInstance().getSpeedRunData(type);
        }
        return new Pair<String, Map<Integer, String>>("", new HashMap<Integer, String>());
    }

    /**
     * @param ma
     * @param sel
     * @return
     */
    public boolean getSR(Pair<String, Map<Integer, String>> ma, int sel) {
        if (ma.getRight().get(sel) == null || ma.getRight().get(sel).length() <= 0) {
            dispose();
            return false;
        }
        sendOk(ma.getRight().get(sel));
        return true;
    }

    /**
     * @param itemid
     * @return
     */
    public Equip getEquip(int itemid) {
        return (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemid);
    }

    /**
     * @param statsSel
     * @param expire
     */
    public void setExpiration(Object statsSel, long expire) {
        if (statsSel instanceof Equip) {
            ((Equip) statsSel).setExpiration(System.currentTimeMillis() + (expire * 24 * 60 * 60 * 1000));
        }
    }

    /**
     * @param statsSel
     */
    public void setLock(Object statsSel) {
        if (statsSel instanceof Equip) {
            Equip eq = (Equip) statsSel;
            if (eq.getExpiration() == -1) {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
            } else {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
        }
    }

    /**
     * @param statsSel
     * @return
     */
    public boolean addFromDrop(Object statsSel) {
        if (statsSel instanceof IItem) {
            final IItem it = (IItem) statsSel;
            return MapleInventoryManipulator.checkSpace(getClient(), it.getItemId(), it.getQuantity(), it.getOwner()) && MapleInventoryManipulator.addFromDrop(getClient(), it, false);
        }
        return false;
    }

    /**
     * @param slot
     * @param invType
     * @param statsSel
     * @param offset
     * @param type
     * @return
     */
    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
        return replaceItem(slot, invType, statsSel, offset, type, false);
    }

    /**
     * @param slot
     * @param invType
     * @param statsSel
     * @param offset
     * @param type
     * @param takeSlot
     * @return
     */
    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        IItem item = getPlayer().getInventory(inv).getItem((byte) slot);
        if (item == null || statsSel instanceof IItem) {
            item = (IItem) statsSel;
        }
        if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
                return false;
            }
            Equip eq = (Equip) item;
            if (takeSlot) {
                if (eq.getUpgradeSlots() < 1) {
                    return false;
                } else {
                    eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() - 1));
                }
            }
            if (type.equalsIgnoreCase("Slots")) {
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + offset));
            } else if (type.equalsIgnoreCase("Level")) {
                eq.setLevel((byte) (eq.getLevel() + offset));
            } else if (type.equalsIgnoreCase("Hammer")) {
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("STR")) {
                eq.setStr((short) (eq.getStr() + offset));
            } else if (type.equalsIgnoreCase("DEX")) {
                eq.setDex((short) (eq.getDex() + offset));
            } else if (type.equalsIgnoreCase("INT")) {
                eq.setInt((short) (eq.getInt() + offset));
            } else if (type.equalsIgnoreCase("LUK")) {
                eq.setLuk((short) (eq.getLuk() + offset));
            } else if (type.equalsIgnoreCase("HP")) {
                eq.setHp((short) (eq.getHp() + offset));
            } else if (type.equalsIgnoreCase("MP")) {
                eq.setMp((short) (eq.getMp() + offset));
            } else if (type.equalsIgnoreCase("WATK")) {
                eq.setWatk((short) (eq.getWatk() + offset));
            } else if (type.equalsIgnoreCase("MATK")) {
                eq.setMatk((short) (eq.getMatk() + offset));
            } else if (type.equalsIgnoreCase("WDEF")) {
                eq.setWdef((short) (eq.getWdef() + offset));
            } else if (type.equalsIgnoreCase("MDEF")) {
                eq.setMdef((short) (eq.getMdef() + offset));
            } else if (type.equalsIgnoreCase("ACC")) {
                eq.setAcc((short) (eq.getAcc() + offset));
            } else if (type.equalsIgnoreCase("Avoid")) {
                eq.setAvoid((short) (eq.getAvoid() + offset));
            } else if (type.equalsIgnoreCase("Hands")) {
                eq.setHands((short) (eq.getHands() + offset));
            } else if (type.equalsIgnoreCase("Speed")) {
                eq.setSpeed((short) (eq.getSpeed() + offset));
            } else if (type.equalsIgnoreCase("Jump")) {
                eq.setJump((short) (eq.getJump() + offset));
            } else if (type.equalsIgnoreCase("ItemEXP")) {
                eq.setItemEXP(eq.getItemEXP() + offset);
            } else if (type.equalsIgnoreCase("Expiration")) {
                eq.setExpiration((long) (eq.getExpiration() + offset));
            } else if (type.equalsIgnoreCase("Flag")) {
                eq.setFlag((byte) (eq.getFlag() + offset));
            }
            if (eq.getExpiration() == -1) {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
            } else {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
            item = eq.copy();
        }
        MapleInventoryManipulator.removeFromSlot(getClient(), inv, (short) slot, item.getQuantity(), false);
        return MapleInventoryManipulator.addFromDrop(getClient(), item, false);
    }

    /**
     * @param slot
     * @param invType
     * @param statsSel
     * @param upgradeSlots
     * @return
     */
    public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
        return replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
    }

    /**
     * @param itemId
     * @return
     */
    public boolean isCash(final int itemId) {
        return MapleItemInformationProvider.getInstance().isCash(itemId);
    }

    /**
     * @param buff
     * @param duration
     * @param msg
     */
    public void buffGuild(final int buff, final int duration, final String msg) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getItemEffect(buff) != null && getPlayer().getGuildId() > 0) {
            final MapleStatEffect mse = ii.getItemEffect(buff);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr.getGuildId() == getPlayer().getGuildId()) {
                        mse.applyTo(chr, chr, true, null, duration);
                        chr.dropMessage(5, "Your guild has gotten a " + msg + " buff.");
                    }
                }
            }
        }
    }

    /**
     * @param alliancename
     * @return
     */
    public boolean createAlliance(String alliancename) {
        MapleParty pt = c.getPlayer().getParty();
        MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
        if (otherChar == null || otherChar.getId() == c.getPlayer().getId()) {
            return false;
        }
        try {
            return World.Alliance.createAlliance(alliancename, c.getPlayer().getId(), otherChar.getId(), c.getPlayer().getGuildId(), otherChar.getGuildId());
        } catch (Exception re) {
            re.printStackTrace();
            return false;
        }
    }

    /**
     * @return
     */
    public boolean addCapacityToAlliance() {
        try {
            final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
                if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && World.Alliance.changeAllianceCapacity(gs.getAllianceId())) {
                    gainMeso(-MapleGuildAlliance.CHANGE_CAPACITY_COST);
                    return true;
                }
            }
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    /**
     * @return
     */
    public boolean disbandAlliance() {
        try {
            final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
                if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && World.Alliance.disbandAlliance(gs.getAllianceId())) {
                    return true;
                }
            }
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    /**
     * @return
     */
    public byte getLastMsg() {
        return lastMsg;
    }

    /**
     * @param last
     */
    public final void setLastMsg(final byte last) {
        this.lastMsg = last;
    }

    /**
     * @param bossid
     * @return
     */
    public int getBossLog(String bossid) {
        return getPlayer().getBossLog(bossid);
    }

    /**
     * @param bossid
     */
    public void setBossLog(String bossid) {
        getPlayer().setBossLog(bossid);
    }

    /**
     * @param bossid
     */
    public final void givePartyBossLog(String bossid) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            setBossLog(bossid);
            return;
        }
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.setBossLog(bossid);
            }
        }
    }

    /**
     * @param bossid
     * @param b
     * @return
     */
    public final boolean getPartyBossLog(String bossid, int b) {
        int a = 0;
        int c = 0;
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1 && getBossLog(bossid) < b) {
            return true;
        }
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getMap().getCharacterById(chr.getId());
            c++;
            if (curChar != null && curChar.getBossLog(bossid) < b) {
                a++;
            }
        }
        return a == c;
    }

    /**
     *
     */
    public final void maxAllSkills() {
        for (ISkill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isApplicableSkill(skil.getId())) { //no db/additionals/resistance skills
                teachSkill(skil.getId(), skil.getMaxLevel(), skil.getMaxLevel());
            }
        }
    }

    /**
     * @param str
     * @param dex
     * @param z
     * @param luk
     */
    public final void resetStats(int str, int dex, int z, int luk) {
        c.getPlayer().resetStats(str, dex, z, luk);
    }

    /**
     * @param slot
     * @param invType
     * @param quantity
     * @return
     */
    public final boolean dropItem(int slot, int invType, int quantity) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        return MapleInventoryManipulator.drop(c, inv, (short) slot, (short) quantity, true);
    }

    /**
     * @return
     */
    public final List<Integer> getAllPotentialInfo() {
        return new ArrayList<>(MapleItemInformationProvider.getInstance().getAllPotentialInfo().keySet());
    }

    /**
     * @param id
     * @return
     */
    public final String getPotentialInfo(final int id) {
        final List<StructPotentialItem> potInfo = MapleItemInformationProvider.getInstance().getPotentialInfo(id);
        final StringBuilder builder = new StringBuilder("#b#ePOTENTIAL INFO FOR ID: ");
        builder.append(id);
        builder.append("#n#k\r\n\r\n");
        int minLevel = 1, maxLevel = 10;
        for (StructPotentialItem item : potInfo) {
            builder.append("#eLevels ");
            builder.append(minLevel);
            builder.append("~");
            builder.append(maxLevel);
            builder.append(": #n");
            builder.append(item.toString());
            minLevel += 10;
            maxLevel += 10;
            builder.append("\r\n");
        }
        return builder.toString();
    }

    /**
     *
     */
    public final void sendRPS() {
        c.getSession().write(MaplePacketCreator.getRPSMode((byte) 8, -1, -1, -1));
    }

    /**
     * @param ch
     * @param questid
     * @param data
     */
    public final void setQuestRecord(Object ch, final int questid, final String data) {
        ((MapleCharacter) ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
    }

    /**
     * @param ch
     */
    public final void doWeddingEffect(final Object ch) {
        final MapleCharacter chr = (MapleCharacter) ch;
        getMap().broadcastMessage(MaplePacketCreator.yellowChat(getPlayer().getName() + ", do you take " + chr.getName() + " as your wife and promise to stay beside her through all downtimes, crashes, and lags?"));
        CloneTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (chr == null || getPlayer() == null) {
                    warpMap(680000500, 0);
                } else {
                    getMap().broadcastMessage(MaplePacketCreator.yellowChat(chr.getName() + ", do you take " + getPlayer().getName() + " as your husband and promise to stay beside him through all downtimes, crashes, and lags?"));
                }
            }
        }, 10000);
        CloneTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (chr == null || getPlayer() == null) {
                    if (getPlayer() != null) {
                        setQuestRecord(getPlayer(), 160001, "3");
                        setQuestRecord(getPlayer(), 160002, "0");
                    } else if (chr != null) {
                        setQuestRecord(chr, 160001, "3");
                        setQuestRecord(chr, 160002, "0");
                    }
                    warpMap(680000500, 0);
                } else {
                    setQuestRecord(getPlayer(), 160001, "2");
                    setQuestRecord(chr, 160001, "2");
                    sendNPCText(getPlayer().getName() + " and " + chr.getName() + ", I wish you two all the best on your AsteriaSEA journey together!", 9201002);
                    getMap().startExtendedMapEffect("You may now kiss the bride, " + getPlayer().getName() + "!", 5120006);
                    if (chr.getGuildId() > 0) {
                        World.Guild.guildPacket(chr.getGuildId(), MaplePacketCreator.sendMarriage(false, chr.getName()));
                    }
                    if (chr.getFamilyId() > 0) {
                        World.Family.familyPacket(chr.getFamilyId(), MaplePacketCreator.sendMarriage(true, chr.getName()), chr.getId());
                    }
                    if (getPlayer().getGuildId() > 0) {
                        World.Guild.guildPacket(getPlayer().getGuildId(), MaplePacketCreator.sendMarriage(false, getPlayer().getName()));
                    }
                    if (getPlayer().getFamilyId() > 0) {
                        World.Family.familyPacket(getPlayer().getFamilyId(), MaplePacketCreator.sendMarriage(true, chr.getName()), getPlayer().getId());
                    }
                }
            }
        }, 20000); //10 sec 10 sec
    }

    /**
     * @param type
     */
    public void openDD(int type) {
        c.getSession().write(MaplePacketCreator.openBeans(getPlayer().getBeans(), type));
    }

    /**
     * @param text
     */
    public void worldMessage(String text) {
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, text).getBytes());
    }

    /**
     * @return
     */
    public int getBeans() {
        return getClient().getPlayer().getBeans();
    }

    /**
     * @param s
     */
    public void gainBeans(int s) {
        getPlayer().gainBeans(s);
        c.getSession().write(MaplePacketCreator.updateBeans(c.getPlayer().getId(), s));
    }

    /**
     * @param type
     * @return
     */
    public int getHyPay(int type) {
        return getPlayer().getHyPay(type);
    }

    /**
     * @param ss
     */
    public void szhs(String ss) {
        c.getSession().write(MaplePacketCreator.游戏屏幕中间黄色字体(ss));
    }

    /**
     * @param ss
     * @param id
     */
    public void szhs(String ss, int id) {
        c.getSession().write(MaplePacketCreator.游戏屏幕中间黄色字体(ss, id));
    }

    /**
     * @param hypay
     * @return
     */
    public int gainHyPay(int hypay) {
        return getPlayer().gainHyPay(hypay);
    }

    /**
     * @param hypay
     * @return
     */
    public int addHyPay(int hypay) {
        return getPlayer().addHyPay(hypay);
    }

    /**
     * @param pay
     * @return
     */
    public int delPayReward(int pay) {
        return getPlayer().delPayReward(pay);
    }

    /**
     * @param id
     * @return
     */
    public int getItemLevel(int id) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        return ii.getReqLevel(id);
    }

    /**
     *
     */
    public void alatPQ() {
        //   c.getSession().write(MaplePacketCreator.updateAriantPQRanking(getText, npc, pendingDisposal))
    }

    /**
     * @param days
     */
    public void xlkc(long days) {
        MapleQuestStatus marr = getPlayer().getQuestNoAdd(MapleQuest.getInstance(122700));
        if ((marr != null) && (marr.getCustomData() != null) && (Long.parseLong(marr.getCustomData()) >= System.currentTimeMillis())) {
            getPlayer().dropMessage(1, "项链扩充失败，您已经进行过项链扩充。");
        } else {
            String customData = String.valueOf(System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L);
            getPlayer().getQuestNAdd(MapleQuest.getInstance(122700)).setCustomData(customData);
            getPlayer().dropMessage(1, "项链" + days + "扩充扩充成功！");
        }
    }

    /**
     * 查询怪物掉落
     *
     * @param mobId
     * @return
     */
    public String checkDrop(int mobId) {
        List ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
        if ((ranks != null) && (ranks.size() > 0)) {
            int num = 0;
            int itemId = 0;
            int ch = 0;

            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            StringBuilder name = new StringBuilder();
            for (int i = 0; i < ranks.size(); i++) {
                MonsterDropEntry de = (MonsterDropEntry) ranks.get(i);
                if ((de.chance > 0) && ((de.questid <= 0) || ((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0)))) {
                    itemId = de.itemId;
                    if (!ii.itemExists(itemId)) {
                        continue;
                    }
                    if (num == 0) {
                        name.append("当前怪物 #o").append(mobId).append("# 的爆率为:\r\n");
                        name.append("--------------------------------------\r\n");
                    }
                    String namez = new StringBuilder().append("#z").append(itemId).append("#").toString();
                    if (itemId == 0) {
                        itemId = 4031041;
                        namez = new StringBuilder().append(de.Minimum * getClient().getChannelServer().getMesoRate()).append(" - ").append(de.Maximum * getClient().getChannelServer().getMesoRate()).append(" 的金币").toString();
                    }
                    ch = de.chance * getClient().getChannelServer().getDropRate();
                    if (getPlayer().isAdmin()) {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append(" - ").append(Integer.valueOf(ch >= 999999 ? 1000000 : ch).doubleValue() / 10000.0D).append("%的爆率. ").append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    } else {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    }
                    num++;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }
        return "没有找到这个怪物的爆率数据。";
    }

    /**
     * @return
     */
    public String checkMapDrop() {
        List ranks = new ArrayList(MapleMonsterInformationProvider.getInstance().getGlobalDrop());
        int mapid = this.c.getPlayer().getMap().getId();
        int cashServerRate = getClient().getChannelServer().getCashRate();
        int globalServerRate = 1;
        if ((ranks != null) && (ranks.size() > 0)) {
            int num = 0;

            StringBuilder name = new StringBuilder();
            for (int i = 0; i < ranks.size(); i++) {
                MonsterGlobalDropEntry de = (MonsterGlobalDropEntry) ranks.get(i);
                if ((de.continent < 0) || ((de.continent < 10) && (mapid / 100000000 == de.continent)) || ((de.continent < 100) && (mapid / 10000000 == de.continent)) || ((de.continent < 1000) && (mapid / 1000000 == de.continent))) {
                    int itemId = de.itemId;
                    if (num == 0) {
                        name.append("当前地图 #r").append(mapid).append("#k - #m").append(mapid).append("# 的全局爆率为:");
                        name.append("\r\n--------------------------------------\r\n");
                    }
                    String names = new StringBuilder().append("#z").append(itemId).append("#").toString();
                    if ((itemId == 0) && (cashServerRate != 0)) {
                        itemId = 4031041;
                        names = new StringBuilder().append(de.Minimum * cashServerRate).append(" - ").append(de.Maximum * cashServerRate).append(" 的抵用卷").toString();
                    }
                    int chance = de.chance * globalServerRate;
                    if (getPlayer().isAdmin()) {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(names).append(" - ").append(Integer.valueOf(chance >= 999999 ? 1000000 : chance).doubleValue() / 10000.0D).append("%的爆率. ").append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    } else {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(names).append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    }
                    num++;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }
        return "当前地图没有设置全局爆率。";
    }

    /**
     * @param lx
     */
    public void sendEventWindow(int lx) {
        this.c.getSession().write(MaplePacketCreator.sendEventWindow(0, lx));
    }

    /**
     * @param aa
     * @return
     */
    public int tylxfk(String aa) {
        int aaa = Integer.parseInt(aa);
        return aaa;
    }

    /**
     * @param name
     */
    public void petName(String name) {
        getPlayer().petName(name);
    }

    /**
     * @return
     */
    public boolean petgm() {
        MaplePet pet = getPlayer().getPet(0);
        if (pet == null) {
            getClient().getSession().write(MaplePacketCreator.serverNotice(1, "请召唤一只宠物出来！"));
            getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        return true;
    }

    /**
     * @param str
     * @return
     */
    public static int calculatePlaces(String str) {
        int m = 0;
        char arr[] = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];
            if ((c >= 0x0391 && c <= 0xFFE5)) //中文字符  
            {
                m = m + 2;
            } else if ((c >= 0x0000 && c <= 0x00FF)) //英文字符  
            {
                m = m + 1;
            }
        }
        return m;
    }

    /**
     * @param str
     * @return
     */
    public static boolean calculate(String str) {
        boolean result = str.matches("[0-9]+");
        if (result == true) {
            System.out.println("该字符串是纯数字");
            return true;
        } else {
            System.out.println("该字符串不是纯数字");
            return false;
        }
    }

    /**
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param name
     */
    public void setgrname(int name) {
        getPlayer().setgrname(name);
    }

    /**
     * @param name
     */
    public void setjzname(int name) {
        getPlayer().setjzname(name);
    }

    /**
     * @return
     */
    public int getgrname() {
        return getPlayer().getgrname();
    }

    /**
     * @return
     */
    public int getjzname() {
        return getPlayer().getjzname();
    }

    /**
     * @param name
     */
    public void setName(String name) {
        getPlayer().setName(name);
        getClient().getSession().write(MaplePacketCreator.serverNotice(1, "请换下频道就能正常显示名字了！"));
        getClient().getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * @return
     */
    public boolean Gzz() {
        return getPlayer().getGuild().getLeaderId() == getPlayer().getId();
    }

    /**
     * @param name
     */
    public void setGName(String name) {
        getPlayer().getGuild().setName(name);
        getClient().getSession().write(MaplePacketCreator.serverNotice(1, "请换下频道就能正常显示名字了！"));
        getClient().getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * @return
     */
    public int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * @return
     */
    public int getMin() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    /**
     * @return
     */
    public int getSec() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    /**
     * @return
     */
    public int gethour() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    /**
     * @return
     */
    public int getmin() {
        Calendar cal = Calendar.getInstance();
        int min = cal.get(Calendar.MINUTE);
        return min;
    }

    /**
     * @return
     */
    public int getsec() {
        Calendar cal = Calendar.getInstance();
        int sec = cal.get(Calendar.SECOND);
        return sec;
    }

    /**
     * @param s
     * @return
     */
    public int getMount(int s) {
        return GameConstants.getMountS(s);
    }

    /**
     * @return
     */
    public int gethyt() {
        return this.hyt;
    }

    /**
     * @param a
     */
    public void sethyt(int a) {
        this.hyt = a;
    }

    /**
     * @param a
     */
    public void gainhyt(int a) {
        this.hyt += a;
        if (gethyt() >= 5000 && getdxs() >= 5000) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                if (cserv.getZiDongDrop() > 1 && cserv.getZiDongExp() > 1) {
                    return;
                }
                cserv.setZiDongDrop(3);
                cserv.setZiDongExp(3);
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr == null) {
                        continue;
                    }
                    chr.startMapEffect("放烟火开启3倍经验3倍爆率活动！", 5120000);
                }
            }
            hyt = 0;
            dxs = 0;
            CloneTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setZiDongDrop(1);
                        cserv.setZiDongExp(1);
                        for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            chr.startMapEffect("放烟火关闭3倍经验3倍爆率活动！期待下次活动！", 5120000);
                        }
                    }
                    // System.out.println("[自动存档] 已经将 " + ppl + " 个玩家保存到数据中.");

                }
            }, 60000 * 120);
        }
    }

    /**
     * @return
     */
    public int getdxs() {
        return this.dxs;
    }

    /**
     * @param a
     */
    public void setdxs(int a) {
        this.dxs = a;
    }

    /**
     * @param a
     */
    public void gaindxs(int a) {
        this.dxs += a;
        if (gethyt() >= 5000 && getdxs() >= 5000) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                if (cserv.getZiDongDrop() > 1 && cserv.getZiDongExp() > 1) {
                    return;
                }
                cserv.setZiDongDrop(3);
                cserv.setZiDongExp(3);
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr == null) {
                        continue;
                    }
                    chr.startMapEffect("放烟火开启3倍经验3倍爆率活动！", 5120000);
                }
            }
            hyt = 0;
            dxs = 0;
            CloneTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setZiDongDrop(1);
                        cserv.setZiDongExp(1);
                        for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            chr.startMapEffect("放烟火关闭3倍经验3倍爆率活动！期待下次活动！", 5120000);
                        }
                    }
                    // System.out.println("[自动存档] 已经将 " + ppl + " 个玩家保存到数据中.");

                }
            }, 60000 * 120);
        }
    }

    /**
     * @param lx
     * @param msg
     * @throws RemoteException
     */
    public void 喇叭(int lx, String msg) throws RemoteException {
        switch (lx) {
            case 1:
                World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11, c.getChannel(), new StringBuilder().append("全服喇叭]").append(c.getPlayer().getName()).append(" : ").append(msg).toString()).getBytes());
                // World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11, this.c.getChannel(), msg).getBytes());
                break;
            case 2:
                World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, c.getChannel(), new StringBuilder().append("全服喇叭]").append(c.getPlayer().getName()).append(" : ").append(msg).toString()).getBytes());
                // World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, this.c.getChannel(), msg).getBytes());
                break;
            case 3:
                World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, c.getChannel(), new StringBuilder().append("全服喇叭]").append(c.getPlayer().getName()).append(" : ").append(msg).toString()).getBytes());
                //  World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, this.c.getChannel(), msg).getBytes());
        }
    }

    /**
     *
     */
    public void displayGuildRankss() {
        MapleGuild.displayGuildRanks(getClient(), this.npc);
    }

    /**
     *
     */
    public void displayRewnu2Ranks() {
        MapleGuild.displayRenwu2Ranks(getClient(), this.npc);
    }

    /**
     *
     */
    public void displayLevelRanks() {
        MapleGuild.displayLevelRanks(getClient(), this.npc);
    }

    /**
     *
     */
    public void 金币排行() {
        MapleGuild.meso(getClient(), this.npc);
    }

    /**
     *
     */
    public void 人气排行榜() {
        MapleGuild.人气排行(getClient(), this.npc);
    }

    /**
     * @throws InterruptedException
     * @throws SQLException
     */
    public void Startqmdb() throws InterruptedException, SQLException {
        for (int ii = 0; ii <= 20; ii++) {
            Thread.sleep(700);
            //每次循环随机一次数字
            int 总数 = getPlayer().获取全民夺宝总数();
            double a = (Math.random() * 总数) + 1;
            //实例化 转换为int
            int A = (int) new Double(a).intValue();
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
//                    mch.getClient().getSession().write(MaplePacketCreator.sendHint("#e#r擅木宝箱---打开中\r\n#n正在获取物品……#n#k\r\n\r\n#e#z" + arr[A] + "#", 200, 200));
                    mch.getClient().getSession().write(MaplePacketCreator.sendHint("#b===========全民冒险岛==========#k\r\n==============================#r\r\n#b========全民夺宝活动开始=======#k\r\n==============================#r\r\n#b===========随机抽取中==========#k\r\n◆正在随机抽选中奖的幸运玩家◆\r\n#b===========幸运玩家===========#r\r\n" + mch.全民夺宝2(A), 200, 200));
                    if (ii == 20) {
                        mch.getClient().getSession().write(MaplePacketCreator.sendHint("#e#r★★★★★全民夺宝★★★★★\r\n中奖玩家：" + mch.全民夺宝2(A), 200, 200));
                        mch.startMapEffect("★恭喜玩家:" + mch.全民夺宝2(A) + " 赢得了 [全民夺宝] !!★", 5120025);
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                }
                break;
                //   }
            }
        }
    }

    /**
     *
     */
    public void 谁是卧底() {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() < 6) {
            //人数不达标无法执行
            return;
        }
        final int cMap = getPlayer().getMapId();
        int 随机给予卧底值 = Randomizer.nextInt(6);
        int 随机给予卧底值2 = Randomizer.nextInt(6);
        int 人数 = getPlayer().getParty().getMembers().size();
        int 确定人数 = 0;
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && curChar.getMapId() == cMap) {
                确定人数++;
                if (随机给予卧底值 == 随机给予卧底值2) {
                    // curChar.谁是卧底(1);//0 = 普通   1 = 卧底
                } else {
                    // curChar.谁是卧底(0);//0 = 普通   1 = 卧底
                }
                if (人数 == 确定人数) {
                    //这里写入一个开始的公告即可
                    final MapTimer tMan = MapTimer.getInstance();
                    tMan.schedule(new Runnable() {
                        @Override
                        public void run() {
                            //这里写入已开始公告
                            //系统分配卧底
                            //头上或者聊天窗显示自己的名称和序号  和别人的序号
                        }
                    }, 1000 * 60);//1分钟
                }
            }
        }
    }

//    public boolean createLottery(String number, int rate) {
//        return LotteryV2.createLottery(getPlayer().getId(), LotteryV2.formatNumber(number), rate);
//    }
//
//    public ArrayList<Map<String, Object>> getLotteries() {
//        return LotteryV2.getLotteries(getPlayer().getId(), 100);
//    }
//
//    public Map<String, Object> getLottery(int id) {
//        return LotteryV2.getLottery(id);
//    }
//
//    public boolean updateLottery(int id) {
//        return LotteryV2.updateLottery(id);
//    }
}
