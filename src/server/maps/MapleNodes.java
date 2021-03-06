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
package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.Pair;

/**
 *
 * @author zjj
 */
public class MapleNodes {

    private Map<Integer, MapleNodeInfo> nodes; //used for HOB pq.
    private final List<Rectangle> areas;
    private List<MaplePlatform> platforms;
    private List<MonsterPoint> monsterPoints;
    private List<Integer> skillIds;
    private List<Pair<Integer, Integer>> mobsToSpawn;
    private List<Pair<Point, Integer>> guardiansToSpawn;
    private int nodeStart = -1, nodeEnd = -1, mapid;
    private boolean firstHighest = true;

    /**
     *
     * @param mapid
     */
    public MapleNodes(final int mapid) {
        nodes = new LinkedHashMap<>();
        areas = new ArrayList<>();
        platforms = new ArrayList<>();
        skillIds = new ArrayList<>();
        monsterPoints = new ArrayList<>();
        mobsToSpawn = new ArrayList<>();
        guardiansToSpawn = new ArrayList<>();
        this.mapid = mapid;
    }

    /**
     *
     * @param ns
     */
    public void setNodeStart(final int ns) {
        this.nodeStart = ns;
    }

    /**
     *
     * @param ns
     */
    public void setNodeEnd(final int ns) {
        this.nodeEnd = ns;
    }

    /**
     *
     */
    public static class MapleNodeInfo {

        public int node,

        /**
         *
         */
        key, 

        /**
         *
         */
        x, 

        /**
         *
         */
        y, 

        /**
         *
         */
        attr;

        /**
         *
         */
        public List<Integer> edge;

        /**
         *
         * @param node
         * @param key
         * @param x
         * @param y
         * @param attr
         * @param edge
         */
        public MapleNodeInfo(int node, int key, int x, int y, int attr, List<Integer> edge) {
            this.node = node;
            this.key = key;
            this.x = x;
            this.y = y;
            this.attr = attr;
            this.edge = edge;
        }
    }

    /**
     *
     * @param mni
     */
    public void addNode(final MapleNodeInfo mni) {
        this.nodes.put(mni.key, mni);
    }

    /**
     *
     * @return
     */
    public Collection<MapleNodeInfo> getNodes() {

        return new ArrayList<>(nodes.values());
    }

    /**
     *
     * @param index
     * @return
     */
    public MapleNodeInfo getNode(final int index) {
        int i = 1;
        for (MapleNodeInfo x : getNodes()) {
            if (i == index) {
                return x;
            }
            i++;
        }
        return null;
    }

    private int getNextNode(final MapleNodeInfo mni) {
        if (mni == null) {
            return -1;
        }
        addNode(mni);
        // output part
	/*StringBuilder b = new StringBuilder(mapid + " added key " + mni.key + ". edges: ");
         for (int i : mni.edge) {
         b.append(i + ", ");
         }
         System.out.println(b.toString());
         FileoutputUtil.log(FileoutputUtil.PacketEx_Log, b.toString());*/
        // output part end

        int ret = -1;
        for (int i : mni.edge) {
            if (nodes.get(i) == null) {
                if (ret != -1 && mapid / 100 == 9211204) {
                    if (!firstHighest) {
                        ret = Math.min(ret, i);
                    } else {
                        firstHighest = false;
                        ret = Math.max(ret, i);
                        //two ways for stage 5 to get to end, thats highest ->lowest, and lowest -> highest(doesn't work)
                        break;
                    }
                } else {
                    ret = i;
                }
            }
        }
        return ret;
    }

    /**
     *
     */
    public void sortNodes() {
        if (nodes.size() <= 0 || nodeStart < 0) {
            return;
        }
        Map<Integer, MapleNodeInfo> unsortedNodes = new HashMap<>(nodes);
        final int nodeSize = unsortedNodes.size();
        nodes.clear();
        int nextNode = getNextNode(unsortedNodes.get(nodeStart));
        while (nodes.size() != nodeSize && nextNode >= 0) {
            nextNode = getNextNode(unsortedNodes.get(nextNode));
        }
    }

    /**
     *
     * @param rec
     */
    public final void addMapleArea(final Rectangle rec) {
        areas.add(rec);
    }

    /**
     *
     * @return
     */
    public final List<Rectangle> getAreas() {
        return new ArrayList<>(areas);
    }

    /**
     *
     * @param index
     * @return
     */
    public final Rectangle getArea(final int index) {
        return getAreas().get(index);
    }

    /**
     *
     */
    public static class MaplePlatform {

        /**
         *
         */
        public String name;
        public int start,

        /**
         *
         */
        speed, 

        /**
         *
         */
        x1, 

        /**
         *
         */
        y1, 

        /**
         *
         */
        x2, 

        /**
         *
         */
        y2, 

        /**
         *
         */
        r;

        /**
         *
         */
        public List<Integer> SN;

        /**
         *
         * @param name
         * @param start
         * @param speed
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @param r
         * @param SN
         */
        public MaplePlatform(String name, int start, int speed, int x1, int y1, int x2, int y2, int r, List<Integer> SN) {
            this.name = name;
            this.start = start;
            this.speed = speed;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.r = r;
            this.SN = SN;
        }
    }

    /**
     *
     * @param mp
     */
    public final void addPlatform(final MaplePlatform mp) {
        this.platforms.add(mp);
    }

    /**
     *
     * @return
     */
    public final List<MaplePlatform> getPlatforms() {
        return new ArrayList<>(platforms);
    }

    /**
     *
     */
    public static class MonsterPoint {

        public int x,

        /**
         *
         */
        y, 

        /**
         *
         */
        fh, 

        /**
         *
         */
        cy, 

        /**
         *
         */
        team;

        /**
         *
         * @param x
         * @param y
         * @param fh
         * @param cy
         * @param team
         */
        public MonsterPoint(int x, int y, int fh, int cy, int team) {
            this.x = x;
            this.y = y;
            this.fh = fh;
            this.cy = cy;
            this.team = team;
        }
    }

    /**
     *
     * @return
     */
    public final List<MonsterPoint> getMonsterPoints() {
        return monsterPoints;
    }

    /**
     *
     * @param x
     * @param y
     * @param fh
     * @param cy
     * @param team
     */
    public final void addMonsterPoint(int x, int y, int fh, int cy, int team) {
        this.monsterPoints.add(new MonsterPoint(x, y, fh, cy, team));
    }

    /**
     *
     * @param mobId
     * @param spendCP
     */
    public final void addMobSpawn(int mobId, int spendCP) {
        this.mobsToSpawn.add(new Pair<>(mobId, spendCP));
    }

    /**
     *
     * @return
     */
    public final List<Pair<Integer, Integer>> getMobsToSpawn() {
        return mobsToSpawn;
    }

    /**
     *
     * @param guardian
     * @param team
     */
    public final void addGuardianSpawn(Point guardian, int team) {
        this.guardiansToSpawn.add(new Pair<>(guardian, team));
    }

    /**
     *
     * @return
     */
    public final List<Pair<Point, Integer>> getGuardians() {
        return guardiansToSpawn;
    }

    /**
     *
     * @return
     */
    public final List<Integer> getSkillIds() {
        return skillIds;
    }

    /**
     *
     * @param z
     */
    public final void addSkillId(int z) {
        this.skillIds.add(z);
    }
}
