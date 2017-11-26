
package handling.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author zjj
 */
public class MapleParty implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private MaplePartyCharacter leader;
    private List<MaplePartyCharacter> members = new LinkedList<>();
    private int id;

    /**
     *
     * @param id
     * @param chrfor
     */
    public MapleParty(int id, MaplePartyCharacter chrfor) {
        this.leader = chrfor;
        this.members.add(this.leader);
        this.id = id;
    }

    /**
     *
     * @param member
     * @return
     */
    public boolean containsMembers(MaplePartyCharacter member) {
        return members.contains(member);
    }

    /**
     *
     * @param member
     */
    public void addMember(MaplePartyCharacter member) {
        members.add(member);
    }

    /**
     *
     * @param member
     */
    public void removeMember(MaplePartyCharacter member) {
        members.remove(member);
    }

    /**
     *
     * @param member
     */
    public void updateMember(MaplePartyCharacter member) {
        for (int i = 0; i < members.size(); i++) {
            MaplePartyCharacter chr = members.get(i);
            if (chr.equals(member)) {
                members.set(i, member);
            }
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public MaplePartyCharacter getMemberById(int id) {
        for (MaplePartyCharacter chr : members) {
            if (chr.getId() == id) {
                return chr;
            }
        }
        return null;
    }

    /**
     *
     * @param index
     * @return
     */
    public MaplePartyCharacter getMemberByIndex(int index) {
        return members.get(index);
    }

    /**
     *
     * @return
     */
    public Collection<MaplePartyCharacter> getMembers() {
        return new LinkedList<>(members);
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public MaplePartyCharacter getLeader() {
        return leader;
    }

    /**
     *
     * @param nLeader
     */
    public void setLeader(MaplePartyCharacter nLeader) {
        leader = nLeader;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapleParty other = (MapleParty) obj;
        return id == other.id;
    }
}
