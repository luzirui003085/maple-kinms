
package client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataFileEntry;
import provider.MapleDataProviderFactory;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataTool;
import tools.StringUtil;

/**
 *
 * @author zjj
 */
public class SkillFactory {

    private static final Map<Integer, ISkill> skills = new HashMap<>();
    private static final Map<Integer, List<Integer>> skillsByJob = new HashMap<>();
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap<>();
    private final static MapleData stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz")).getData("Skill.img");
    private static MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Skill.wz"));
    private static MapleDataProvider Data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Skill.wz"));

    /**
     *
     * @param jobid
     * @param skill
     * @param skilllevel
     * @return
     */
    public static int getSkilldamage(int jobid, int skill, int skilllevel) {
        return MapleDataTool.getInt(Data.getData("" + jobid + ".img").getChildByPath("skill").getChildByPath("" + skill + "").getChildByPath("level").getChildByPath("" + skilllevel + "").getChildByPath("damage"));
    }

    /**
     *
     * @param jobid
     * @param skill
     * @param skilllevel
     * @return
     */
    public static int getSkillmad(int jobid, int skill, int skilllevel) {
        return MapleDataTool.getInt(Data.getData("" + jobid + ".img").getChildByPath("skill").getChildByPath("" + skill + "").getChildByPath("level").getChildByPath("" + skilllevel + "").getChildByPath("mad"));
    }
    
    /**
     *
     * @param id
     * @return
     */
    public static final ISkill getSkill(final int id) {
        if (!skills.isEmpty()) {
            return skills.get(id);
        }
        System.out.println("加载 技能完成 :::");
        final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Skill.wz"));
        final MapleDataDirectoryEntry root = datasource.getRoot();

        int skillid;
        MapleData summon_data;
        SummonSkillEntry sse;

        for (MapleDataFileEntry topDir : root.getFiles()) { // Loop thru jobs
            if (topDir.getName().length() <= 8) {
                for (MapleData data : datasource.getData(topDir.getName())) { // Loop thru each jobs
                    if (data.getName().equals("skill")) {
                        for (MapleData data2 : data) { // Loop thru each jobs
                            if (data2 != null) {
                                skillid = Integer.parseInt(data2.getName());

                                Skill skil = Skill.loadFromData(skillid, data2);
                                List<Integer> job = skillsByJob.get(skillid / 10000);
                                if (job == null) {
                                    job = new ArrayList<>();
                                    skillsByJob.put(skillid / 10000, job);
                                }
                                job.add(skillid);
                                skil.setName(getName(skillid));

                                skills.put(skillid, skil);

                                summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data != null) {
                                    sse = new SummonSkillEntry();
                                    sse.attackAfter = (short) MapleDataTool.getInt("attackAfter", summon_data, 999999);
                                    sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
                                    sse.mobCount = (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
                                    SummonSkillInformation.put(skillid, sse);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public static ISkill getSkill1(int id) {
        ISkill ret = (ISkill) skills.get(id);
        if (ret != null) {
            return ret;
        }
        synchronized (skills) {
            ret = (ISkill) skills.get(id);
            if (ret == null) {
                int job = id / 10000;
                MapleData skillroot = datasource.getData(StringUtil.getLeftPaddedStr(String.valueOf(job), '0', 3) + ".img");
                MapleData skillData = skillroot.getChildByPath("skill/" + StringUtil.getLeftPaddedStr(String.valueOf(id), '0', 7));
                if (skillData != null) {
                    ret = Skill.loadFromData(id, skillData);
                }
                skills.put(id, ret);
            }
            return ret;
        }
    }

    /**
     *
     * @param jobId
     * @return
     */
    public static final List<Integer> getSkillsByJob(final int jobId) {
        return skillsByJob.get(jobId);
    }

    /**
     *
     * @param id
     * @return
     */
    public static final String getSkillName(final int id) {
        ISkill skil = getSkill(id);
        if (skil != null) {
            return skil.getName();
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public static final String getName(final int id) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }
        return null;
    }

    /**
     *
     * @param skillid
     * @return
     */
    public static final SummonSkillEntry getSummonData(final int skillid) {
        return SummonSkillInformation.get(skillid);
    }

    /**
     *
     * @return
     */
    public static final Collection<ISkill> getAllSkills() {
        return skills.values();
    }
}
