
package server.events;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import server.Timer.EventTimer;
import tools.MaplePacketCreator;

/**
 *
 * @author zjj
 */
public class MapleCoconut extends MapleEvent {

    private List<MapleCoconuts> coconuts = new LinkedList<>();
    private int[] coconutscore = new int[2];
    private int countBombing = 0;
    private int countFalling = 0;
    private int countStopped = 0;

    /**
     *
     * @param channel
     * @param mapid
     */
    public MapleCoconut(final int channel, final int[] mapid) {
        super(channel, mapid);
    }

    /**
     *
     */
    @Override
    public void reset() {
        super.reset();
        resetCoconutScore();
    }

    /**
     *
     */
    @Override
    public void unreset() {
        super.unreset();
        resetCoconutScore();
        setHittable(false);
    }

    /**
     *
     * @param chr
     */
    @Override
    public void onMapLoad(MapleCharacter chr) {
        chr.getClient().getSession().write(MaplePacketCreator.coconutScore(getCoconutScore()));
    }

    /**
     *
     * @param id
     * @return
     */
    public MapleCoconuts getCoconut(int id) {
        return coconuts.get(id);
    }

    /**
     *
     * @return
     */
    public List<MapleCoconuts> getAllCoconuts() {
        return coconuts;
    }

    /**
     *
     * @param hittable
     */
    public void setHittable(boolean hittable) {
        for (MapleCoconuts nut : coconuts) {
            nut.setHittable(hittable);
        }
    }

    /**
     *
     * @return
     */
    public int getBombings() {
        return countBombing;
    }

    /**
     *
     */
    public void bombCoconut() {
        countBombing--;
    }

    /**
     *
     * @return
     */
    public int getFalling() {
        return countFalling;
    }

    /**
     *
     */
    public void fallCoconut() {
        countFalling--;
    }

    /**
     *
     * @return
     */
    public int getStopped() {
        return countStopped;
    }

    /**
     *
     */
    public void stopCoconut() {
        countStopped--;
    }

    /**
     *
     * @return
     */
    public int[] getCoconutScore() { // coconut event
        return coconutscore;
    }

    /**
     *
     * @return
     */
    public int getMapleScore() { // Team Maple, coconut event
        return coconutscore[0];
    }

    /**
     *
     * @return
     */
    public int getStoryScore() { // Team Story, coconut event
        return coconutscore[1];
    }

    /**
     *
     */
    public void addMapleScore() { // Team Maple, coconut event
        coconutscore[0]++;
    }

    /**
     *
     */
    public void addStoryScore() { // Team Story, coconut event
        coconutscore[1]++;
    }

    /**
     *
     */
    public void resetCoconutScore() {
        coconutscore[0] = 0;
        coconutscore[1] = 0;
        countBombing = 80;
        countFalling = 1001;
        countStopped = 20;
        coconuts.clear();
        for (int i = 0; i < 506; i++) {
            coconuts.add(new MapleCoconuts());
        }
    }

    /**
     *
     */
    @Override
    public void startEvent() {
        reset();
        setHittable(true);
        getMap(0).broadcastMessage(MaplePacketCreator.serverNotice(5, "活動開始!!"));
        getMap(0).broadcastMessage(MaplePacketCreator.hitCoconut(true, 0, 0));
        getMap(0).broadcastMessage(MaplePacketCreator.getClock(360));

        EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (getMapleScore() == getStoryScore()) {
                    bonusTime();
                } else if (getMapleScore() > getStoryScore()) {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        if (chr.getCoconutTeam() == 0) {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                } else {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        if (chr.getCoconutTeam() == 1) {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                }
            }
        }, 360000);
    }

    /**
     *
     */
    public void bonusTime() {
        getMap(0).broadcastMessage(MaplePacketCreator.getClock(120));
        EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (getMapleScore() == getStoryScore()) {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                        chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                    }
                    warpOut();
                } else if (getMapleScore() > getStoryScore()) {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        if (chr.getCoconutTeam() == 0) {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                } else {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        if (chr.getCoconutTeam() == 1) {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                }
            }
        }, 120000);

    }

    /**
     *
     */
    public void warpOut() {
        setHittable(false);
        EventTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                    if ((getMapleScore() > getStoryScore() && chr.getCoconutTeam() == 0) || (getStoryScore() > getMapleScore() && chr.getCoconutTeam() == 1)) {
                        givePrize(chr);
                    }
                    warpBack(chr);
                }
                unreset();
            }
        }, 12000);
    }

    /**
     *
     */
    public static class MapleCoconuts {

        private int hits = 0;
        private boolean hittable = false;
        private boolean stopped = false;
        private long hittime = System.currentTimeMillis();

        /**
         *
         */
        public void hit() {
            this.hittime = System.currentTimeMillis() + 1000; // test
            hits++;
        }

        /**
         *
         * @return
         */
        public int getHits() {
            return hits;
        }

        /**
         *
         */
        public void resetHits() {
            hits = 0;
        }

        /**
         *
         * @return
         */
        public boolean isHittable() {
            return hittable;
        }

        /**
         *
         * @param hittable
         */
        public void setHittable(boolean hittable) {
            this.hittable = hittable;
        }

        /**
         *
         * @return
         */
        public boolean isStopped() {
            return stopped;
        }

        /**
         *
         * @param stopped
         */
        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

        /**
         *
         * @return
         */
        public long getHitTime() {
            return hittime;
        }
    }
}
