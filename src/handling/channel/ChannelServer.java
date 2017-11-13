/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package handling.channel;

import client.MapleCharacter;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.ByteArrayMaplePacket;
import handling.MaplePacket;
import handling.MapleServerHandler;
import handling.login.LoginServer;
import handling.mina.MapleCodecFactory;
import handling.world.CheaterData;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import provider.MapleDataProviderFactory;
import scripting.EventScriptManager;
import server.MapleSquad;
import server.MapleSquad.MapleSquadType;
import server.ServerProperties;
import server.Timer;
import server.events.MapleCoconut;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MapleOxQuiz;
import server.events.MapleSnowball;
import server.events.MapleSurvival;
import server.life.PlayerNPC;
import server.maps.FakeCharacter;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.shops.HiredMerchant;
import server.shops.HiredMerchantSave;
import server.shops.IMaplePlayerShop;
import tools.CollectionUtil;
import tools.ConcurrentEnumMap;
import tools.MaplePacketCreator;
import tools.packet.UIPacket;

public class ChannelServer implements Serializable {

    public static long serverStartTime;
    private static final long serialVersionUID = 1L;
    private int expRate, mesoRate, dropRate, cashRate;
    private int doubleExp = 0;
    private int doubleMeso = 1;
    private int doubleDrop = 1;
    private int zidongExp = 1;
    private int zidongDrop = 1;
    private short port = 7574;
    private static final short DEFAULT_PORT = 7574;
    private final int channel;
    private int running_MerchantID = 0, flags = 0;
    private String serverMessage, key, ip, serverName;
    private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false, adminOnly = false;
    private PlayerStorage players;
    private MapleServerHandler serverHandler;
    private IoAcceptor acceptor;
    private final MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private static final Map<Integer, ChannelServer> instances = new HashMap<Integer, ChannelServer>();
    private final Map<MapleSquadType, MapleSquad> mapleSquads = new ConcurrentEnumMap<MapleSquadType, MapleSquad>(MapleSquadType.class);
    private final Map<Integer, HiredMerchant> merchants = new HashMap<Integer, HiredMerchant>();
    private final Map<Integer, PlayerNPC> playerNPCs = new HashMap<Integer, PlayerNPC>();
    private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock(); //merchant
    private final ReentrantReadWriteLock squadLock = new ReentrantReadWriteLock(); //squad
    private int eventmap = -1;
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<MapleEventType, MapleEvent>(MapleEventType.class);
    private final boolean debugMode = false;
    private int instanceId = 0;
    private int statLimit;
    private Collection<FakeCharacter> clones = new LinkedList<FakeCharacter>();

//    private ChannelServer(final String key, final int channel) {
//        this.key = key;
//        this.channel = channel;
//        mapFactory = new MapleMapFactory();
//        mapFactory.setChannel(channel);
//    }
    private ChannelServer(final int channel) {
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(channel);
        /* this.channel = channel;
        mapFactory = new MapleMapFactory();
        mapFactory.setChannel(channel);*/
    }

    public static Set<Integer> getAllInstance() {
        return new HashSet<Integer>(instances.keySet());
    }

    public final void loadEvents() {
        if (!events.isEmpty()) {
            return;
        }
        events.put(MapleEventType.CokePlay, new MapleCoconut(channel, MapleEventType.CokePlay.mapids));
        events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut.mapids));
        events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness.mapids));
        events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla.mapids));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz.mapids));
        events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball.mapids));
        //  events.put(MapleEventType.Survival, new MapleSurvival(channel, MapleEventType.Survival.mapids));
    }

    public final void run_startup_configurations() {
        setChannel(this.channel); //instances.put
        try {
            //  expRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Exp"));
            // mesoRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Meso"));
            //  dropRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Drop"));
            expRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Exp"));
            mesoRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Meso"));
            dropRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Drop"));
            cashRate = Integer.parseInt(ServerProperties.getProperty("KinMS.Cash"));
            serverMessage = ServerProperties.getProperty("KinMS.ServerMessage");
            statLimit = Integer.parseInt(ServerProperties.getProperty("KinMS.statLimit", "999"));
            serverName = ServerProperties.getProperty("KinMS.ServerName");
            flags = Integer.parseInt(ServerProperties.getProperty("KinMS.WFlags", "0"));
            adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("KinMS.Admin", "false"));
            eventSM = new EventScriptManager(this, ServerProperties.getProperty("KinMS.Events").split(","));
            port = Short.parseShort(ServerProperties.getProperty("KinMS.Port" + this.channel, String.valueOf(DEFAULT_PORT + this.channel)));
            //他不会去 启动 KinMS.Port  而是启动的 DEFAULT_PORT的yto
            //   port = Short.parseShort(ServerProperties.getProperty("KinMS.Port" + channel));
            // port = Integer.parseInt(this.props.getProperty("net.sf.cherry.channel.net.port"));

        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        ip = "127.0.0.1" + ":" + port;

        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        acceptor = new SocketAcceptor();
        final SocketAcceptorConfig acceptor_config = new SocketAcceptorConfig();
        acceptor_config.getSessionConfig().setTcpNoDelay(true);
        acceptor_config.setDisconnectOnUnbind(true);
        acceptor_config.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        players = new PlayerStorage(this.channel);
        loadEvents();
        try {
            this.serverHandler = new MapleServerHandler(this.channel, false);
            acceptor.bind(new InetSocketAddress(port), serverHandler, acceptor_config);
            System.out.println("频道 " + this.channel + ": 启动端口 " + port + ": 服务器IP " + ip + "");
            eventSM.init();
        } catch (IOException e) {
            System.out.println("Binding to port " + port + " failed (ch: " + getChannel() + ")" + e);
        }
    }

    public final void shutdown(Object threadToNotify) {
        if (finishedShutdown) {
            return;
        }
        broadcastPacket(MaplePacketCreator.serverNotice(0, "這個頻道正在關閉中."));
        // dc all clients by hand so we get sessionClosed...
        shutdown = true;

        System.out.println("Channel " + channel + ", Saving hired merchants...");

        closeAllMerchants();

        System.out.println("Channel " + channel + ", Saving characters...");

        getPlayerStorage().disconnectAll();

        System.out.println("Channel " + channel + ", Unbinding...");

        acceptor.unbindAll();
        acceptor = null;

        //temporary while we dont have !addchannel
        instances.remove(channel);
        LoginServer.removeChannel(channel);
        setFinishShutdown();
//        if (threadToNotify != null) {
//            synchronized (threadToNotify) {
//                threadToNotify.notify();
//            }
//        }
    }

    public final void unbind() {
        acceptor.unbindAll();
    }

    public final boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public final MapleMapFactory getMapFactory() {
        return mapFactory;
    }

//    public static final ChannelServer newInstance(final String key, final int channel) {
//        return new ChannelServer(key, channel);
//    }
    public static final ChannelServer newInstance(final int channel) {
        return new ChannelServer(channel);
    }

    public static final ChannelServer getInstance(final int channel) {
        return instances.get(channel);
    }

    public final void addPlayer(final MapleCharacter chr) {
        getPlayerStorage().registerPlayer(chr);
        chr.getClient().getSession().write(MaplePacketCreator.serverMessage(serverMessage));
    }

    public final PlayerStorage getPlayerStorage() {
        if (players == null) { //wth
            players = new PlayerStorage(channel); //wthhhh
        }
        return players;
    }

    public final void removePlayer(final MapleCharacter chr) {
        getPlayerStorage().deregisterPlayer(chr);

    }

    public final void removePlayer(final int idz, final String namez) {
        getPlayerStorage().deregisterPlayer(idz, namez);

    }

    public final String getServerMessage() {
        return serverMessage;
    }

    public final void setServerMessage(final String newMessage) {
        serverMessage = newMessage;
        broadcastPacket(MaplePacketCreator.serverMessage(serverMessage));
    }

    public final void broadcastPacket(final MaplePacket data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public final void broadcastSmegaPacket(final MaplePacket data) {
        getPlayerStorage().broadcastSmegaPacket(data);
    }

    public final void broadcastGMPacket(final MaplePacket data) {
        getPlayerStorage().broadcastGMPacket(data);
    }

    public final int getExpRate() {
        return expRate + doubleExp * zidongExp;
    }

    public final void setExpRate(final int expRate) {
        this.expRate = expRate;
    }

    public final int getCashRate() {
        return cashRate;
    }

    public final void setCashRate(final int cashRate) {
        this.cashRate = cashRate;
    }

    public final int getChannel() {
        return channel;
    }

    public final void setChannel(final int channel) {
        instances.put(channel, this);
        LoginServer.addChannel(channel);
    }

    public static final Collection<ChannelServer> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public final String getIP() {
        return ip;
    }

    public String getIPA() {
        return ip;
    }

    public final boolean isShutdown() {
        return shutdown;
    }

    public final int getLoadedMaps() {
        return mapFactory.getLoadedMaps();
    }

    public final EventScriptManager getEventSM() {
        return eventSM;
    }

    public final void reloadEvents() {
        eventSM.cancel();
        eventSM = new EventScriptManager(this, ServerProperties.getProperty("KinMS.Events").split(","));
        eventSM.init();
    }

    public final int getMesoRate() {
        return mesoRate * doubleMeso;
    }

    public final void setMesoRate(final int mesoRate) {
        this.mesoRate = mesoRate;
    }

    public final int getDropRate() {
        return dropRate * doubleDrop * zidongDrop;
    }

    public final void setDropRate(final int dropRate) {
        this.dropRate = dropRate;
    }

    public int getDoubleExp() {
        if ((this.doubleExp < 0) || (this.doubleExp > 2)) {
            return 0;
        }
        return this.doubleExp;
    }

    public void setDoubleExp(int doubleExp) {
        if ((doubleExp < 0) || (doubleExp > 2)) {
            this.doubleExp = 0;
        } else {
            this.doubleExp = doubleExp;
        }
    }

    public int getZiDongExp() {
        if ((this.zidongExp < 0) || (this.zidongExp > 2)) {
            return 0;
        }
        return this.zidongExp;
    }

    public void setZiDongExp(int zidongExp) {
        if ((zidongExp < 0) || (zidongExp > 2)) {
            this.zidongExp = 0;
        } else {
            this.zidongExp = zidongExp;
        }
    }

    public int getDoubleMeso() {
        if ((this.doubleMeso < 0) || (this.doubleMeso > 2)) {
            return 1;
        }
        return this.doubleMeso;
    }

    public void setDoubleMeso(int doubleMeso) {
        if ((doubleMeso < 0) || (doubleMeso > 2)) {
            this.doubleMeso = 1;
        } else {
            this.doubleMeso = doubleMeso;
        }
    }

    public int getDoubleDrop() {
        if ((this.doubleDrop < 0) || (this.doubleDrop > 2)) {
            return 1;
        }
        return this.doubleDrop;
    }

    public void setDoubleDrop(int doubleDrop) {
        if ((doubleDrop < 0) || (doubleDrop > 2)) {
            this.doubleDrop = 1;
        } else {
            this.doubleDrop = doubleDrop;
        }
    }

    public int getZiDongDrop() {
        if ((this.zidongDrop < 0) || (this.zidongDrop > 2)) {
            return 1;
        }
        return this.zidongDrop;
    }

    public void setZiDongDrop(int zidongDrop) {
        if ((zidongDrop < 0) || (zidongDrop > 2)) {
            this.zidongDrop = 1;
        } else {
            this.zidongDrop = zidongDrop;
        }
    }

    /* public static final void startChannel_Main() {
        serverStartTime = System.currentTimeMillis();

        for (int i = 0; i < Integer.parseInt(ServerProperties.getProperty("KinMS.Count", "0")); i++) {
            //newInstance(ServerConstants.Channel_Key[i], i + 1).run_startup_configurations();
            newInstance(i + 1).run_startup_configurations();
        }
    }*/
    public int getStatLimit() {
        return this.statLimit;
    }

    public void setStatLimit(int limit) {
        this.statLimit = limit;
    }

    public static void startChannel_Main() {
        serverStartTime = System.currentTimeMillis();
        int ch = Integer.parseInt(ServerProperties.getProperty("KinMS.Count", "0"));
        if (ch > 10) {
            ch = 10;
        }
        for (int i = 0; i < ch; i++) {
            newInstance(i + 1).run_startup_configurations();
        }
    }

    public static final void startChannel(final int channel) {
        serverStartTime = System.currentTimeMillis();
        for (int i = 0; i < Integer.parseInt(ServerProperties.getProperty("KinMS.Count", "0")); i++) {
            if (channel == i + 1) {

                //newInstance(ServerConstants.Channel_Key[i], i + 1).run_startup_configurations();
                newInstance(i + 1).run_startup_configurations();
                break;
            }
        }
    }

    public Map<MapleSquadType, MapleSquad> getAllSquads() {
        return Collections.unmodifiableMap(mapleSquads);
    }

    public final MapleSquad getMapleSquad(final String type) {
        return getMapleSquad(MapleSquadType.valueOf(type.toLowerCase()));
    }

    public final MapleSquad getMapleSquad(final MapleSquadType type) {
        return mapleSquads.get(type);
    }

    public final boolean addMapleSquad(final MapleSquad squad, final String type) {
        final MapleSquadType types = MapleSquadType.valueOf(type.toLowerCase());
        if (types != null && !mapleSquads.containsKey(types)) {
            mapleSquads.put(types, squad);
            squad.scheduleRemoval();
            return true;
        }
        return false;
    }

    public boolean removeMapleSquad(MapleSquad squad, MapleSquadType type) {
        if (type != null && mapleSquads.containsKey(type)) {
            if (mapleSquads.get(type) == squad) {
                mapleSquads.remove(type);
                return true;
            }
        }
        return false;
    }

    public final boolean removeMapleSquad(final MapleSquadType types) {
        if (types != null && mapleSquads.containsKey(types)) {
            mapleSquads.remove(types);
            return true;
        }
        return false;
    }

    public int closeAllMerchant() {
        int ret = 0;
        merchLock.writeLock().lock();
        try {
            final Iterator<HiredMerchant> merchants_ = merchants.values().iterator();
            // Iterator merchants_ = this.merchants.entrySet().iterator();
            while (merchants_.hasNext()) {
                HiredMerchant hm = (HiredMerchant) ((Map.Entry) merchants_.next()).getValue();
                HiredMerchantSave.QueueShopForSave(hm);
                hm.getMap().removeMapObject(hm);
                merchants_.remove();
                ret++;
            }
        } finally {
            merchLock.writeLock().unlock();
        }
        for (int i = 910000001; i <= 910000022; i++) {
            for (MapleMapObject mmo : this.mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                HiredMerchantSave.QueueShopForSave((HiredMerchant) mmo);
                ret++;
            }
        }
        return ret;
    }

    public void closeAllMerchants() {
        int ret = 0;
        long Start = System.currentTimeMillis();
        merchLock.writeLock().lock();
        try {
            Iterator hmit = this.merchants.entrySet().iterator();
            // final Iterator<HiredMerchant> merchants_ = merchants.values().iterator();
            while (hmit.hasNext()) {
                ((HiredMerchant) ((Map.Entry) hmit.next()).getValue()).closeShop(true, false);
                hmit.remove();
                ret++;
            }
        } catch (Exception e) {
            System.out.println("关闭雇佣商店出现错误..." + e);
        } finally {
            merchLock.writeLock().unlock();
        }
        System.out.println("频道 " + this.channel + " 共保存雇佣商店: " + ret + " | 耗时: " + (System.currentTimeMillis() - Start) + " 毫秒.");
    }

    public void closeAllMerchantsc() {
        merchLock.writeLock().lock();
        try {
            final Iterator<HiredMerchant> merchants_ = merchants.values().iterator();
            while (merchants_.hasNext()) {
                merchants_.next().closeShop(true, true);
                merchants_.remove();
            }
        } catch (Exception e) {
            System.out.println("关闭雇佣商店出现错误..." + e);
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public final int addMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();

        int runningmer = 0;
        try {
            runningmer = running_MerchantID;
            merchants.put(running_MerchantID, hMerchant);
            running_MerchantID++;
        } finally {
            merchLock.writeLock().unlock();
        }
        return runningmer;
    }

    public final void removeMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();

        try {
            merchants.remove(hMerchant.getStoreId());
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public final boolean containsMerchant(final int accid) {
        boolean contains = false;

        merchLock.readLock().lock();
        try {
            final Iterator itr = merchants.values().iterator();

            while (itr.hasNext()) {
                if (((IMaplePlayerShop) itr.next()).getOwnerAccId() == accid) {
                    contains = true;
                    break;
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return contains;
    }

    public final List<HiredMerchant> searchMerchant(final int itemSearch) {
        final List<HiredMerchant> list = new LinkedList<HiredMerchant>();
        merchLock.readLock().lock();
        try {
            final Iterator itr = merchants.values().iterator();

            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return list;
    }

    public final void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = !this.MegaphoneMuteState;
    }

    public final boolean getMegaphoneMuteState() {
        return MegaphoneMuteState;
    }

    public int getEvent() {
        return eventmap;
    }

    public final void setEvent(final int ze) {
        this.eventmap = ze;
    }

    public MapleEvent getEvent(final MapleEventType t) {
        return events.get(t);
    }

    public final Collection<PlayerNPC> getAllPlayerNPC() {
        return playerNPCs.values();
    }

    public final PlayerNPC getPlayerNPC(final int id) {
        return playerNPCs.get(id);
    }

    public final void addPlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.containsKey(npc.getId())) {
            removePlayerNPC(npc);
        }
        playerNPCs.put(npc.getId(), npc);
        getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
    }

    public final void removePlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.containsKey(npc.getId())) {
            playerNPCs.remove(npc.getId());
            getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
        }
    }

    public final String getServerName() {
        return serverName;
    }

    public final void setServerName(final String sn) {
        this.serverName = sn;
    }

    public final int getPort() {
        return port;
    }

    public static final Set<Integer> getChannelServer() {
        return new HashSet<Integer>(instances.keySet());
    }

    public final void setShutdown() {
        this.shutdown = true;
        System.out.println("Channel " + channel + " has set to shutdown.");
    }

    public final void setFinishShutdown() {
        this.finishedShutdown = true;
        System.out.println("频道 " + channel + " 已关闭完成.");
    }

    public final boolean isAdminOnly() {
        return adminOnly;
    }

    public final static int getChannelCount() {
        return instances.size();
    }

    public final MapleServerHandler getServerHandler() {
        return serverHandler;
    }

    public final int getTempFlag() {
        return flags;
    }

    public static Map<Integer, Integer> getChannelLoad() {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (ChannelServer cs : instances.values()) {
            ret.put(cs.getChannel(), cs.getConnectedClients());
        }
        return ret;
    }

    public int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> cheaters = getPlayerStorage().getCheaters();

        Collections.sort(cheaters);
        return CollectionUtil.copyFirst(cheaters, 20);
    }

    public void broadcastMessage(byte[] message) {
        broadcastPacket(new ByteArrayMaplePacket(message));
    }

    public void broadcastMessage(MaplePacket message) {
        broadcastPacket(message);
    }

    public void broadcastSmega(byte[] message) {
        broadcastSmegaPacket(new ByteArrayMaplePacket(message));
    }

    public void broadcastGMMessage(byte[] message) {
        broadcastGMPacket(new ByteArrayMaplePacket(message));
    }

    public void saveAll() {
        int ppl = 0;
        for (MapleCharacter chr : this.players.getAllCharacters()) {
            if (chr != null) {
                ppl++;
                chr.saveToDB(false, false);
            } else {
                continue;
            }
        }
        System.out.println("[自动存档] 已经将频道 " + this.channel + " 的 " + ppl + " 个玩家保存到数据中.");
    }

    public void AutoNx(int dy) {
        mapFactory.getMap(Integer.parseInt(ServerProperties.getProperty("KinMS.PDMap"))).AutoNx(dy);
    }

    public void AutoBoss(int channel, int map, int Hour, int time, int moid, int x, int y, int hp) {
        mapFactory.getMap(map).spawnMonsterOnGroundBelow(moid, x, y, hp, channel, map, time, Hour);
    }

    public void AutoTime(int dy) {
        for (ChannelServer chan : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                if (chr != null) {
                    chr.gainGamePoints(1);
                    if (chr.getGamePoints() < 5) {
                        chr.resetGamePointsPD();
                    }
                }
            }
        }
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void addInstanceId() {
        instanceId++;
    }

    public void shutdown() {

        if (this.finishedShutdown) {
            return;
        }
        broadcastPacket(MaplePacketCreator.serverNotice(0, "游戏即将关闭维护..."));

        this.shutdown = true;
        System.out.println("频道 " + this.channel + " 正在清理活动脚本...");

        this.eventSM.cancel();

        System.out.println("频道 " + this.channel + " 正在保存所有角色数据...");

        getPlayerStorage().disconnectAll();

        System.out.println("频道 " + this.channel + " 解除绑定端口...");

        this.acceptor.unbindAll();
        this.acceptor = null;

        instances.remove(this.channel);
        setFinishShutdown();
    }

    public void addClone(FakeCharacter fc) {
        clones.add(fc);
    }

    public void removeClone(FakeCharacter fc) {
        clones.remove(fc);
    }

    public Collection<FakeCharacter> getAllClones() {
        return Collections.unmodifiableCollection(clones);
    }

    public void Startqmdb() throws InterruptedException {

        Calendar cc = Calendar.getInstance();//可以对每个时间域单独修改
        int hour = cc.get(Calendar.HOUR_OF_DAY);
        int minute = cc.get(Calendar.MINUTE);
        int second = cc.get(Calendar.SECOND);
        int number = cc.get(Calendar.DAY_OF_WEEK);//得出系统时间
        if (number == 6) {//如果是星期5(因为该方法为一周8天所以会多1)
            if (hour == 20) {
                try {
                    qqq();
                } catch (SQLException ex) {
//                    Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("开启全民夺宝错误。请检查" + ex);
                }
            }
        }
    }

    private void qqq() throws SQLException, InterruptedException {
        for (int ii = 0; ii <= 20; ii++) {
            Thread.sleep(700);
            //每次循环随机一次数字
            int 总数 = 获取全民夺宝总数();
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
                        mch.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[全民夺宝活动]恭喜玩家" + mch.全民夺宝2(A) + "成为了本期夺宝的幸运玩家!!!"));
                        mch.getClient().getSession().write(UIPacket.getTopMsg("[全民夺宝活动]恭喜玩家" + mch.全民夺宝2(A) + "成为了本期夺宝的幸运玩家!!!"));
                        mch.玩家获得物品(mch.全民夺宝3(A), mch.全民夺宝2(A));
                        mch.getClient().getSession().write(MaplePacketCreator.enableActions());
                    }
                }
                break;
                //   }
            }
        }

    }
//获取全民夺宝总数

    public int 获取全民夺宝总数() throws SQLException {
        java.sql.Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT count(*) from qmdbplayer";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = -1;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        return count;
    }

}
