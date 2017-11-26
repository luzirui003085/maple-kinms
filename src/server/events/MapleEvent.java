
package server.events;

import client.MapleCharacter;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.world.World;
import server.RandomRewards;
import server.Timer.EventTimer;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import tools.MaplePacketCreator;

/**
 *
 * @author zjj
 */
public abstract class MapleEvent {

    /**
     *
     */
    protected int[] mapid;

    /**
     *
     */
    protected int channel;
    private boolean isRunning = false;

    /**
     *
     * @param channel
     * @param mapid
     */
    public MapleEvent(final int channel, final int[] mapid) {
        this.channel = channel;
        this.mapid = mapid;
    }

    /**
     *
     * @return
     */
    public boolean isRunning() {
        return isIsRunning();
    }

    /**
     *
     * @param i
     * @return
     */
    public MapleMap getMap(final int i) {
        return getChannelServer().getMapFactory().getMap(mapid[i]);
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
     * @param packet
     */
    public void broadcast(final MaplePacket packet) {
        for (int i = 0; i < mapid.length; i++) {
            getMap(i).broadcastMessage(packet);
        }
    }

    /**
     *
     * @param chr
     */
    public void givePrize(final MapleCharacter chr) {
        final int reward = RandomRewards.getInstance().getEventReward();
        switch (reward) {
            case 0:
                chr.gainMeso(10000, true, false, false);
                chr.dropMessage(5, "你获得 10000 冒险币");
                break;
            case 1:
                chr.gainMeso(10000, true, false, false);
                chr.dropMessage(5, "你获得 10000 冒险币");
                break;
            case 2:
                chr.modifyCSPoints(1, 200, false);
                chr.dropMessage(5, "你获得 200 抵用");
                break;
            case 3:
                chr.addFame(2);
                chr.dropMessage(5, "你获得 2 人气");
                break;
            default:
                break;
        }

    }

    /**
     *
     * @param chr
     */
    public void finished(MapleCharacter chr) { //most dont do shit here
    }

    /**
     *
     * @param chr
     */
    public void onMapLoad(MapleCharacter chr) { //most dont do shit here
    }

    /**
     *
     */
    public void startEvent() {
    }

    /**
     *
     * @param chr
     */
    public void warpBack(MapleCharacter chr) {
        int map = chr.getSavedLocation(SavedLocationType.EVENT);
        if (map <= -1) {
            map = 104000000;
        }
        final MapleMap mapp = chr.getClient().getChannelServer().getMapFactory().getMap(map);
        chr.changeMap(mapp, mapp.getPortal(0));
    }

    /**
     *
     */
    public void reset() {
        setIsRunning(true);
    }

    /**
     *
     */
    public void unreset() {
        setIsRunning(false);
    }

    /**
     *
     * @param cserv
     * @param auto
     */
    public static final void setEvent(final ChannelServer cserv, final boolean auto) {
        if (auto) {
            for (MapleEventType t : MapleEventType.values()) {
                final MapleEvent e = cserv.getEvent(t);
                if (e.isIsRunning()) {
                    for (int i : e.mapid) {
                        if (cserv.getEvent() == i) {
                            e.broadcast(MaplePacketCreator.serverNotice(0, "距離活動開始只剩一分鐘!"));
                            e.broadcast(MaplePacketCreator.getClock(60));
                            EventTimer.getInstance().schedule(new Runnable() {

                                @Override
                                public void run() {
                                    e.startEvent();
                                }
                            }, 60000);
                            break;
                        }
                    }
                }
            }
        }
        cserv.setEvent(-1);
    }

    /**
     *
     * @param chr
     * @param channel
     */
    public static final void mapLoad(final MapleCharacter chr, final int channel) {
        if (chr == null) {
            return;
        } //o_o
        for (MapleEventType t : MapleEventType.values()) {
            try {
                final MapleEvent e = ChannelServer.getInstance(channel).getEvent(t);
                if (e.isIsRunning()) {
                    if (chr.getMapId() == 109050000) { //finished map
                        e.finished(chr);
                    }
                    for (int i : e.mapid) {
                        if (chr.getMapId() == i) {
                            e.onMapLoad(chr);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     *
     * @param chr
     */
    public static final void onStartEvent(final MapleCharacter chr) {
        for (MapleEventType t : MapleEventType.values()) {
            final MapleEvent e = chr.getClient().getChannelServer().getEvent(t);
            if (e.isIsRunning()) {
                for (int i : e.mapid) {
                    if (chr.getMapId() == i) {
                        e.startEvent();
                        chr.dropMessage(5, String.valueOf(t) + " 活動開始");
                    }
                }
            }
        }
    }

    /**
     *
     * @param event
     * @param cserv
     * @return
     */
    public static final String scheduleEvent(final MapleEventType event, final ChannelServer cserv) {
        if (cserv.getEvent() != -1 || cserv.getEvent(event) == null) {
            return "該活動已經被禁止安排了.";
        }
        for (int i : cserv.getEvent(event).mapid) {
            if (cserv.getMapFactory().getMap(i).getCharactersSize() > 0) {
                return "該活動已經在執行中.";
            }
        }
        cserv.setEvent(cserv.getEvent(event).mapid[0]);
        cserv.getEvent(event).reset();
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "活动 " + String.valueOf(event) + " 即将在频道 " + cserv.getChannel() + " 举行 , 要参加的玩家请到频道 " + cserv.getChannel() + ".3分钟内点击拍卖-城镇活动传送-系统活动传送进入！").getBytes());
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "活动 " + String.valueOf(event) + " 即将在频道 " + cserv.getChannel() + " 举行 , 要参加的玩家请到频道 " + cserv.getChannel() + ".3分钟内点击拍卖-城镇活动传送-系统活动传送进入！").getBytes());
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "活动 " + String.valueOf(event) + " 即将在频道 " + cserv.getChannel() + " 举行 , 要参加的玩家请到频道 " + cserv.getChannel() + ".3分钟内点击拍卖-城镇活动传送-系统活动传送进入！").getBytes());
        return "";
    }

    /**
     * @return the isRunning
     */
    public boolean isIsRunning() {
        return isRunning;
    }

    /**
     * @param isRunning the isRunning to set
     */
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
