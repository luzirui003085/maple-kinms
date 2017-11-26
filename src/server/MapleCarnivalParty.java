package server;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 * Note for this class : MapleCharacter reference must be removed immediately
 * after cpq or upon dc.
 *
 * @author Rob
 */
public class MapleCarnivalParty {

    private final List<Integer> members = new LinkedList<>();
    private final WeakReference<MapleCharacter> leader;
    private final byte team;
    private final int channel;
    private short availableCP = 0, totalCP = 0;
    private boolean winner = false;

    /**
     *
     * @param owner
     * @param members1
     * @param team1
     */
    public MapleCarnivalParty(final MapleCharacter owner, final List<MapleCharacter> members1, final byte team1) {
        leader = new WeakReference<>(owner);
        for (MapleCharacter mem : members1) {
            members.add(mem.getId());
            mem.setCarnivalParty(this);
        }
        team = team1;
        channel = owner.getClient().getChannel();
    }

    /**
     *
     * @return
     */
    public final MapleCharacter getLeader() {
        return leader.get();
    }

    /**
     *
     * @param player
     * @param ammount
     */
    public void addCP(MapleCharacter player, int ammount) {
        totalCP += ammount;
        availableCP += ammount;
        player.addCP(ammount);
    }

    /**
     *
     * @return
     */
    public int getTotalCP() {
        return totalCP;
    }

    /**
     *
     * @return
     */
    public int getAvailableCP() {
        return availableCP;
    }

    /**
     *
     * @param player
     * @param ammount
     */
    public void useCP(MapleCharacter player, int ammount) {
        availableCP -= ammount;
        player.useCP(ammount);
    }

    /**
     *
     * @return
     */
    public List<Integer> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     *
     * @return
     */
    public int getTeam() {
        return team;
    }

    /**
     *
     * @param map
     * @param portalname
     */
    public void warp(final MapleMap map, final String portalname) {
        for (int chr : members) {
            final MapleCharacter c = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(chr);
            if (c != null) {
                c.changeMap(map, map.getPortal(portalname));
            }
        }
    }

    /**
     *
     * @param map
     * @param portalid
     */
    public void warp(final MapleMap map, final int portalid) {
        for (int chr : members) {
            final MapleCharacter c = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(chr);
            if (c != null) {
                c.changeMap(map, map.getPortal(portalid));
            }
        }
    }

    /**
     *
     * @param map
     * @return
     */
    public boolean allInMap(MapleMap map) {
        for (int chr : members) {
            if (map.getCharacterById(chr) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param chr
     */
    public void removeMember(MapleCharacter chr) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i) == chr.getId()) {
                members.remove(i);
                chr.setCarnivalParty(null);
            }
        }

    }

    /**
     *
     * @return
     */
    public boolean isWinner() {
        return winner;
    }

    /**
     *
     * @param status
     */
    public void setWinner(boolean status) {
        winner = status;
    }

    /**
     *
     */
    public void displayMatchResult() {
        final String effect = winner ? "quest/carnival/win" : "quest/carnival/lose";
        final String sound = winner ? "MobCarnival/Win" : "MobCarnival/Lose";
        boolean done = false;
        for (int chr : members) {
            final MapleCharacter c = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(chr);
            if (c != null) {
                c.getClient().getSession().write(MaplePacketCreator.showEffect(effect));
                c.getClient().getSession().write(MaplePacketCreator.playSound(sound));
                if (!done) {
                    done = true;
                    c.getMap().killAllMonsters(true);
                    c.getMap().setSpawns(false); //resetFully will take care of this
                }
            }
        }

    }
}
