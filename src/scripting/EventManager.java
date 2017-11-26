
package scripting;

import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import javax.script.Invocable;
import javax.script.ScriptException;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MapleSquad;
import server.Randomizer;
import server.Timer.EventTimer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapFactory;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import database.DatabaseConnection;

/**
 *
 * @author zjj
 */
public class EventManager {

    private static int[] eventChannel = new int[2];
    private Invocable iv;
    private int channel;
    private Map<String, EventInstanceManager> instances = new WeakHashMap<>();
    private Properties props = new Properties();
    private String name;

    /**
     *
     * @param cserv
     * @param iv
     * @param name
     */
    public EventManager(ChannelServer cserv, Invocable iv, String name) {
        this.iv = iv;
        this.channel = cserv.getChannel();
        this.name = name;
    }

    /**
     *
     */
    public void cancel() {
        try {
            iv.invokeFunction("cancelSchedule", (Object) null);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
        }
    }

    /**
     *
     * @param methodName
     * @param delay
     * @return
     */
    public ScheduledFuture<?> schedule(final String methodName, long delay) {
        return EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, (Object) null);
                } catch (Exception ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                    // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }

    /**
     *
     * @param methodName
     * @param delay
     * @param eim
     * @return
     */
    public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
        return EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (Exception ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                    // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }

    /**
     *
     * @param methodName
     * @param eim
     * @param delay
     * @return
     */
    public ScheduledFuture<?> schedule(final String methodName, final EventInstanceManager eim, long delay) {
        return EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (Exception ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                    // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }

    /**
     *
     * @param methodName
     * @param timestamp
     * @return
     */
    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {

            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, (Object) null);
                } catch (ScriptException ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                } catch (NoSuchMethodException ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, timestamp);
    }

    /**
     *
     * @return
     */
    public int getChannel() {
        return channel;
    }

    /**
     *
     * @return
     */
    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(channel);
    }

    /**
     *
     * @param name
     * @return
     */
    public EventInstanceManager getInstance(String name) {
        return instances.get(name);
    }

    /**
     *
     * @return
     */
    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    /**
     *
     * @param name
     * @return
     */
    public EventInstanceManager newInstance(String name) {
        EventInstanceManager ret = new EventInstanceManager(this, name, channel);
        instances.put(name, ret);
        return ret;
    }

    /**
     *
     * @param name
     */
    public void disposeInstance(String name) {
        instances.remove(name);
        if (getProperty("state") != null && instances.isEmpty()) {
            setProperty("state", "0");
        }
        if (getProperty("leader") != null && instances.isEmpty() && getProperty("leader").equals("false")) {
            setProperty("leader", "true");
        }
        if (this.name.equals("CWKPQ")) { //hard code it because i said so
            final MapleSquad squad = ChannelServer.getInstance(channel).getMapleSquad("CWKPQ");//so fkin hacky
            if (squad != null) {
                squad.clear();
            }
        }
    }

    /**
     *
     * @return
     */
    public Invocable getIv() {
        return iv;
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     *
     * @return
     */
    public final Properties getProperties() {
        return props;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public void startInstance() {
        try {
            iv.invokeFunction("setup", (Object) null);
        } catch (Exception ex) {
            ex.printStackTrace();
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    /**
     *
     * @param mapid
     * @param chr
     */
    public void startInstance(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
            eim.registerCarnivalParty(chr, chr.getMap(), (byte) 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    /**
     *
     * @param mapid
     * @param chr
     */
    public void startInstance_Party(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
            eim.registerParty(chr.getParty(), chr.getMap());
        } catch (Exception ex) {
            ex.printStackTrace();
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    //GPQ

    /**
     *
     * @param character
     * @param leader
     */
    public void startInstance(MapleCharacter character, String leader) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerPlayer(character);
            eim.setProperty("leader", leader);
            eim.setProperty("guildid", String.valueOf(character.getGuildId()));
            setProperty("guildid", String.valueOf(character.getGuildId()));
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-Guild:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-Guild:\n" + ex);
        }
    }

    /**
     *
     * @param character
     */
    public void startInstance_CharID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", character.getId()));
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-CharID:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-CharID:\n" + ex);
        }
    }

    /**
     *
     * @param character
     */
    public void startInstance(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
        }
    }

    //PQ method: starts a PQ

    /**
     *
     * @param party
     * @param map
     */
    public void startInstance(MapleParty party, MapleMap map) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", party.getId()));
            eim.registerParty(party, map);
        } catch (ScriptException ex) {
            System.out.println("Event name : " + name + ", method Name : setup-partyid:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-partyid:\n" + ex);
        } catch (Exception ex) {
            //ignore
            startInstance_NoID(party, map, ex);
        }
    }

    /**
     *
     * @param party
     * @param map
     */
    public void startInstance_NoID(MapleParty party, MapleMap map) {
        startInstance_NoID(party, map, null);
    }

    /**
     *
     * @param party
     * @param map
     * @param old
     */
    public void startInstance_NoID(MapleParty party, MapleMap map, final Exception old) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerParty(party, map);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-party:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-party:\n" + ex + "\n" + (old == null ? "no old exception" : old));
        }
    }

    //non-PQ method for starting instance

    /**
     *
     * @param eim
     * @param leader
     */
    public void startInstance(EventInstanceManager eim, String leader) {
        try {
            iv.invokeFunction("setup", eim);
            eim.setProperty("leader", leader);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-leader:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-leader:\n" + ex);
        }
    }

    /**
     *
     * @param squad
     * @param map
     */
    public void startInstance(MapleSquad squad, MapleMap map) {
        startInstance(squad, map, -1);
    }

    /**
     *
     * @param squad
     * @param map
     * @param questID
     */
    public void startInstance(MapleSquad squad, MapleMap map, int questID) {
        if (squad.getStatus() == 0) {
            return; //we dont like cleared squads
        }
        if (!squad.getLeader().isGM()) {
            if (squad.getMembers().size() < squad.getType().i) { //less than 3
                squad.getLeader().dropMessage(5, "這個遠征隊至少要有 " + squad.getType().i + " 人以上才可以開戰.");
                return;
            }
            if (name.equals("CWKPQ") && squad.getJobs().size() < 5) {
                squad.getLeader().dropMessage(5, "The squad requires members from every type of job.");
                return;
            }
        }
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", squad.getLeaderName()));
            eim.registerSquad(squad, map, questID);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-squad:\n" + ex);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-squad:\n" + ex);
        }
    }

    /**
     *
     * @param squad
     * @param map
     * @param bossid
     */
    public void startInstance(MapleSquad squad, MapleMap map, String bossid) {
        if (squad.getStatus() == 0) {
            return;
        }
        if (!squad.getLeader().isGM()) {
            int mapid = map.getId();
            int chrSize = 0;
            for (String chr : squad.getMembers()) {
                MapleCharacter player = squad.getChar(chr);
                if ((player != null) && (player.getMapId() == mapid)) {
                    chrSize++;
                }
            }
            if (chrSize < squad.getType().i) {
                squad.getLeader().dropMessage(5, new StringBuilder().append("远征队中人员少于 ").append(squad.getType().i).append(" 人，无法开始远征任务。注意必须队伍中的角色在线且在同一地图。当前人数: ").append(chrSize).toString());
                return;
            }
            if ((this.name.equals("CWKPQ")) && (squad.getJobs().size() < 5)) {
                squad.getLeader().dropMessage(5, "远征队中成员职业的类型小于5种，无法开始远征任务。");
                return;
            }
        }
        try {
            EventInstanceManager eim = (EventInstanceManager) (EventInstanceManager) this.iv.invokeFunction("setup", squad.getLeaderName());
            eim.registerSquad(squad, map, Integer.parseInt(bossid));
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\n").append(ex).toString());
            FileoutputUtil.log("log\\Script_Except.log", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\n").append(ex).toString());
        }
    }

    /**
     *
     * @param from
     * @param to
     */
    public void warpAllPlayer(int from, int to) {
        final MapleMap tomap = getMapFactory().getMap(to);
        final MapleMap frommap = getMapFactory().getMap(from);
        List<MapleCharacter> list = frommap.getCharactersThreadsafe();
        if (tomap != null && frommap != null && list != null && frommap.getCharactersSize() > 0) {
            for (MapleMapObject mmo : list) {
                ((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
            }
        }
    }
    
    /**
     *
     * @return
     */
    public int online() {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        ResultSet re;
        int count = 0;
        try {
            ps = con.prepareStatement("SELECT count(*) as cc FROM accounts WHERE loggedin = 2");
            re = ps.executeQuery();
            while (re.next()) {
                count = re.getInt("cc");
            }
        } catch (SQLException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }    

    /**
     *
     * @return
     */
    public MapleMapFactory getMapFactory() {
        return getChannelServer().getMapFactory();
    }

    /**
     *
     * @return
     */
    public OverrideMonsterStats newMonsterStats() {
        return new OverrideMonsterStats();
    }

    /**
     *
     * @return
     */
    public List<MapleCharacter> newCharList() {
        return new ArrayList<>();
    }

    /**
     *
     * @param id
     * @return
     */
    public MapleMonster getMonster(final int id) {
        return MapleLifeFactory.getMonster(id);
    }

    /**
     *
     * @param mapid
     * @param effect
     */
    public void broadcastShip(final int mapid, final int effect) {
        getMapFactory().getMap(mapid).broadcastMessage(MaplePacketCreator.boatPacket(effect));
    }

    /**
     *
     * @param mapid
     */
    public void broadcastChangeMusic(final int mapid) {
        getMapFactory().getMap(mapid).broadcastMessage(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
    }

    /**
     *
     * @param msg
     */
    public void broadcastYellowMsg(final String msg) {
        getChannelServer().broadcastPacket(MaplePacketCreator.yellowChat(msg));
    }

    /**
     *
     * @param type
     * @param msg
     * @param weather
     */
    public void broadcastServerMsg(final int type, final String msg, final boolean weather) {
        if (!weather) {
            getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(type, msg));
        } else {
            for (MapleMap load : getMapFactory().getAllMaps()) {
                if (load.getCharactersSize() > 0) {
                    load.startMapEffect(msg, type);
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean scheduleRandomEvent() {
        boolean omg = false;
        for (int i = 0; i < eventChannel.length; i++) {
            omg |= scheduleRandomEventInChannel(eventChannel[i]);
        }
        return omg;
    }

    /**
     *
     * @param chz
     * @return
     */
    public boolean scheduleRandomEventInChannel(int chz) {
        final ChannelServer cs = ChannelServer.getInstance(chz);
        if (cs == null || cs.getEvent() > -1) {
            return false;
        }
        MapleEventType t = null;
        while (t == null) {
            for (MapleEventType x : MapleEventType.values()) {
                //if (Randomizer.nextInt(MapleEventType.values().length) == 0 && x != MapleEventType.OxQuiz/*選邊站*/) {
                if (Randomizer.nextInt(MapleEventType.values().length) == 0) {
                     t = x;
                    break;
                }
            }
        }
        final String msg = MapleEvent.scheduleEvent(t, cs);
        if (msg.length() > 0) {
            broadcastYellowMsg(msg);
            return false;
        }
        EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (cs.getEvent() >= 0) {
                    MapleEvent.setEvent(cs, true);
                }
            }
        }, 180000);
        return true;
    }

    /**
     *
     * @param chr
     * @param chz
     * @param A
     * @return
     */
    public boolean scheduleRandomEventInChannel(MapleCharacter chr, int chz, int A) {
        final ChannelServer cs = ChannelServer.getInstance(chz);
        if (cs == null || cs.getEvent() > -1) {
            return false;
        }
        MapleEventType t = null;
        OUTER:
        while (t == null) {
            //  for (MapleEventType x : MapleEventType.values()) {
            switch (A) {
                case 1:
                    t = MapleEventType.Coconut;
                    break OUTER;
            /*if (Randomizer.nextInt(MapleEventType.values().length) == 0 && x != MapleEventType.OxQuiz) {
            t = x;
            break;
            }*/
            //  }
                case 2:
                    t = MapleEventType.CokePlay;
                    break OUTER;
                case 3:
                    t = MapleEventType.Fitness;
                    break OUTER;
                case 4:
                    t = MapleEventType.OlaOla;
                    break OUTER;
                case 5:
                    t = MapleEventType.OxQuiz;
                    break OUTER;
                case 6:
                    t = MapleEventType.Snowball;
                    break OUTER;
                default:
                    chr.dropMessage(6, "输入指令错误！");
                    return false;
            }
        }
        final String msg = MapleEvent.scheduleEvent(t, cs);
        if (msg.length() > 0) {
            broadcastYellowMsg(msg);
            return false;
        }
        EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (cs.getEvent() >= 0) {
                    MapleEvent.setEvent(cs, true);
                }
            }
        }, 180000);
        return true;
    }
    
    /**
     *
     */
    public void setWorldEvent() {
        for (int i = 0; i < eventChannel.length; i++) {
//            eventChannel[i] = 1; //2-8
            eventChannel[i] = Randomizer.nextInt(ChannelServer.getAllInstances().size()) + i; //2-13
        }
    }
}
