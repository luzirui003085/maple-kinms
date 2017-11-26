
package handling.world.family;

import client.MapleCharacter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zjj
 */
public class MapleFamilyCharacter implements java.io.Serializable {

    /**
     *
     */
    public static final long serialVersionUID = 2058609046116597760L;
    private int level, id, channel = -1, jobid, familyid, seniorid, currentrep, totalrep, junior1, junior2;
    private boolean online;
    private String name;
    private List<Integer> pedigree = new ArrayList<>(); //recalculate
    private int descendants = 0;

	// either read from active character...
    // if it's online

    /**
     *
     * @param c
     * @param fid
     * @param sid
     * @param j1
     * @param j2
     */
    public MapleFamilyCharacter(MapleCharacter c, int fid, int sid, int j1, int j2) {
        name = c.getName();
        level = c.getLevel();
        id = c.getId();
        channel = c.getClient().getChannel();
        jobid = c.getJob();
        familyid = fid;
        junior1 = j1;
        junior2 = j2;
        seniorid = sid;
        currentrep = c.getCurrentRep();
        totalrep = c.getTotalRep();
        online = true;
    }

    // or we could just read from the database

    /**
     *
     * @param _id
     * @param _lv
     * @param _name
     * @param _channel
     * @param _job
     * @param _fid
     * @param _sid
     * @param _jr1
     * @param _jr2
     * @param _crep
     * @param _trep
     * @param _on
     */
    public MapleFamilyCharacter(int _id, int _lv, String _name, int _channel, int _job, int _fid, int _sid, int _jr1, int _jr2, int _crep, int _trep, boolean _on) {
        level = _lv;
        id = _id;
        name = _name;
        if (_on) {
            channel = _channel;
        }
        jobid = _job;
        online = _on;
        familyid = _fid;
        seniorid = _sid;
        currentrep = _crep;
        totalrep = _trep;
        junior1 = _jr1;
        junior2 = _jr2;
    }

    /**
     *
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     *
     * @param l
     */
    public void setLevel(int l) {
        level = l;
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
     * @param ch
     */
    public void setChannel(int ch) {
        channel = ch;
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
    public int getJobId() {
        return jobid;
    }

    /**
     *
     * @param job
     */
    public void setJobId(int job) {
        jobid = job;
    }

    /**
     *
     * @return
     */
    public int getCurrentRep() {
        return currentrep;
    }

    /**
     *
     * @param cr
     */
    public void setCurrentRep(int cr) {
        this.currentrep = cr;
    }

    /**
     *
     * @return
     */
    public int getTotalRep() {
        return totalrep;
    }

    /**
     *
     * @param tr
     */
    public void setTotalRep(int tr) {
        this.totalrep = tr;
    }

    /**
     *
     * @return
     */
    public int getJunior1() {
        return junior1;
    }

    /**
     *
     * @return
     */
    public int getJunior2() {
        return junior2;
    }

    /**
     *
     * @param trs
     */
    public void setJunior1(int trs) {
        junior1 = trs;
    }

    /**
     *
     * @param trs
     */
    public void setJunior2(int trs) {
        junior2 = trs;
    }

    /**
     *
     * @return
     */
    public int getSeniorId() {
        return seniorid;
    }

    /**
     *
     * @param si
     */
    public void setSeniorId(int si) {
        this.seniorid = si;
    }

    /**
     *
     * @return
     */
    public int getFamilyId() {
        return familyid;
    }

    /**
     *
     * @param fi
     */
    public void setFamilyId(int fi) {
        this.familyid = fi;
    }

    /**
     *
     * @return
     */
    public boolean isOnline() {
        return online;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MapleFamilyCharacter)) {
            return false;
        }

        MapleFamilyCharacter o = (MapleFamilyCharacter) other;
        return (o.getId() == id && o.getName().equals(name));
    }

    /**
     *
     * @param f
     */
    public void setOnline(boolean f) {
        online = f;
    }

    /**
     *
     * @param fam
     * @return
     */
    public List<MapleFamilyCharacter> getAllJuniors(MapleFamily fam) { //to be used scarcely
        List<MapleFamilyCharacter> ret = new ArrayList<>();
        ret.add(this);
        if (junior1 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior1);
            if (chr != null) {
                ret.addAll(chr.getAllJuniors(fam));
			//} else {
                //	junior1 = 0;
            }
        }
        if (junior2 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior2);
            if (chr != null) {
                ret.addAll(chr.getAllJuniors(fam));
			//} else {
                //	junior2 = 0;
            }
        }
        return ret;
    }

    /**
     *
     * @param fam
     * @return
     */
    public List<MapleFamilyCharacter> getOnlineJuniors(MapleFamily fam) { //to be used scarcely
        List<MapleFamilyCharacter> ret = new ArrayList<>();
        ret.add(this);
        if (junior1 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior1);
            if (chr != null) {
                if (chr.isOnline()) {
                    ret.add(chr);
                }
                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter chr2 = fam.getMFC(chr.getJunior1());
                    if (chr2 != null && chr2.isOnline()) {
                        ret.add(chr2);
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter chr2 = fam.getMFC(chr.getJunior2());
                    if (chr2 != null && chr2.isOnline()) {
                        ret.add(chr2);
                    }
                }
			//} else {
                //	junior1 = 0;
            }
        }
        if (junior2 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior2);
            if (chr != null) {
                if (chr.isOnline()) {
                    ret.add(chr);
                }
                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter chr2 = fam.getMFC(chr.getJunior1());
                    if (chr2 != null && chr2.isOnline()) {
                        ret.add(chr2);
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter chr2 = fam.getMFC(chr.getJunior2());
                    if (chr2 != null && chr2.isOnline()) {
                        ret.add(chr2);
                    }
                }
			//} else {
                //	junior2 = 0;
            }
        }
        return ret;
    }

    /**
     *
     * @return
     */
    public List<Integer> getPedigree() {
        return pedigree;
    }

    /**
     *
     * @param fam
     */
    public void resetPedigree(MapleFamily fam) { //not in order
        pedigree = new ArrayList<>();
        pedigree.add(id); //lol
        if (seniorid > 0) {
            MapleFamilyCharacter chr = fam.getMFC(seniorid);
            if (chr != null) {
                pedigree.add(seniorid);
                if (chr.getSeniorId() > 0) {
                    pedigree.add(chr.getSeniorId());
                }
                if (chr.getJunior1() > 0 && chr.getJunior1() != id) {
                    pedigree.add(chr.getJunior1());
                } else if (chr.getJunior2() > 0 && chr.getJunior2() != id) {
                    pedigree.add(chr.getJunior2());
                }
			//} else {
                //	seniorid = 0;
            }
        }
        if (junior1 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior1);
            if (chr != null) {
                pedigree.add(junior1);
                if (chr.getJunior1() > 0) {
                    pedigree.add(chr.getJunior1());
                }
                if (chr.getJunior2() > 0) {
                    pedigree.add(chr.getJunior2());
                }
			//} else {
                //	junior1 = 0;
            }
        }
        if (junior2 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior2);
            if (chr != null) {
                pedigree.add(junior2);
                if (chr.getJunior1() > 0) {
                    pedigree.add(chr.getJunior1());
                }
                if (chr.getJunior2() > 0) {
                    pedigree.add(chr.getJunior2());
                }
			//} else {
                //	junior2 = 0;
            }
        }

    }

    /**
     *
     * @return
     */
    public int getDescendants() {
        return descendants;
    }

    /**
     *
     * @param fam
     * @return
     */
    public int resetDescendants(MapleFamily fam) { //advisable to only start this with leader. resets EVERYONE
        //recursion.
        descendants = 0;
        if (junior1 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior1);
            if (chr != null) {
                descendants += 1 + chr.resetDescendants(fam);
            }
        }
        if (junior2 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior2);
            if (chr != null) {
                descendants += 1 + chr.resetDescendants(fam);
            }
        }
        return descendants;
    }

    /**
     *
     * @param fam
     * @return
     */
    public int resetGenerations(MapleFamily fam) { //advisable to only start this with leader. resets EVERYONE
        //recursion. this field is NOT stored so please be advised
        int descendants1 = 0, descendants2 = 0;
        if (junior1 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior1);
            if (chr != null) {
                descendants1 = chr.resetGenerations(fam);
            }
        }
        if (junior2 > 0) {
            MapleFamilyCharacter chr = fam.getMFC(junior2);
            if (chr != null) {
                descendants2 = chr.resetGenerations(fam);
            }
        }
        int ret = Math.max(descendants1, descendants2);
        return ret + (ret > 0 ? 1 : 0);
    }

    /**
     *
     * @return
     */
    public int getNoJuniors() {
        int ret = 0;
        if (junior1 > 0) {
            ret++;
        }
        if (junior2 > 0) {
            ret++;
        }
        return ret;
    }
}
