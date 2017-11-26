package server;

import client.MapleDisease;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.life.MobSkill;
import server.life.MobSkillFactory;

/**
 *
 * @author zjj
 */
public class MapleCarnivalFactory {

    private final static MapleCarnivalFactory instance = new MapleCarnivalFactory();
    private final Map<Integer, MCSkill> skills = new HashMap<>();
    private final Map<Integer, MCSkill> guardians = new HashMap<>();
    private final MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Skill.wz"));

    /**
     *
     */
    public MapleCarnivalFactory() {
        //whoosh
        initialize();
    }

    /**
     *
     * @return
     */
    public static final MapleCarnivalFactory getInstance() {
        return instance;
    }

    private void initialize() {
        if (!skills.isEmpty()) {
            return;
        }
        for (MapleData z : dataRoot.getData("MCSkill.img")) {
            skills.put(Integer.parseInt(z.getName()), new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), MapleDataTool.getInt("target", z, 1) > 1));
        }
        for (MapleData z : dataRoot.getData("MCGuardian.img")) {
            guardians.put(Integer.parseInt(z.getName()), new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), true));
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public MCSkill getSkill(final int id) {
        return skills.get(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public MCSkill getGuardian(final int id) {
        return guardians.get(id);
    }

    /**
     *
     */
    public static class MCSkill {

        public int cpLoss,

        /**
         *
         */
        skillid, 

        /**
         *
         */
        level;

        /**
         *
         */
        public boolean targetsAll;

        /**
         *
         * @param _cpLoss
         * @param _skillid
         * @param _level
         * @param _targetsAll
         */
        public MCSkill(int _cpLoss, int _skillid, int _level, boolean _targetsAll) {
            cpLoss = _cpLoss;
            skillid = _skillid;
            level = _level;
            targetsAll = _targetsAll;
        }

        /**
         *
         * @return
         */
        public MobSkill getSkill() {
            return MobSkillFactory.getMobSkill(skillid, 1); //level?
        }

        /**
         *
         * @return
         */
        public MapleDisease getDisease() {
            if (skillid <= 0) {
                return MapleDisease.getRandom();
            }
            return MapleDisease.getBySkill(skillid);
        }
    }
}
