package handling.channel.handler;

import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.PlayerStats;
import client.SkillFactory;
import client.anticheat.CheatTracker;
import client.anticheat.CheatingOffense;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pvp.MaplePvp;
import server.MapleStatEffect;
import server.Randomizer;
import server.Timer.MapTimer;
import server.life.Element;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;

/**
 *
 * @author zjj
 */
public class DamageParse {

    private final static int[] charges = {1211005, 1211006};

    /**
     *
     */
    public static MapleMonster pvpMob;

    /**
     *
     * @param attack
     * @param theSkill
     * @param player
     * @param attackCount
     * @param maxDamagePerMonster
     * @param effect
     * @param attack_type
     */
    public static void applyAttack(final AttackInfo attack, final ISkill theSkill, final MapleCharacter player, int attackCount, final double maxDamagePerMonster, final MapleStatEffect effect, final AttackType attack_type) {
        if (!player.isAlive()) {
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        if (attack.skill != 0) {
            if (effect == null) {
                player.getClient().getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (GameConstants.isMulungSkill(attack.skill)) {
                if (player.getMapId() / 10000 != 92502) {
                    return;
                } else {
                    player.mulung_EnergyModify(false);
                }
            }
            if (GameConstants.isPyramidSkill(attack.skill)) {
                if (player.getMapId() / 1000000 != 926) {
                    //AutobanManager.getInstance().autoban(player.getClient(), "Using Pyramid skill outside of pyramid maps.");
                    return;
                } else if (player.getPyramidSubway() == null || !player.getPyramidSubway().onSkillUse(player)) {
                    return;
                }
            }
            if (attack.targets > effect.getMobCount()) { // Must be done here, since NPE with normal atk
                player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
                return;
            }
        }
        if (attack.hits > attackCount) {
            if (attack.skill != 4211006) {
                player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
                return;
            }
        }
        if (attack.hits > 0 && attack.targets > 0) {
            // Don't ever do this. it's too expensive.
            if (!player.getStat().checkEquipDurabilitys(player, -1)) { //i guess this is how it works ?
                player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
                return;
            } //lol
        }
        int totDamage = 0;
        final MapleMap map = player.getMap();
        if (map.getId() == GameConstants.PVP_MAP && player.getClient().getChannel() == GameConstants.PVP_CHANEL) {
            MaplePvp.doPvP(player, map, attack, effect);
        }
        if (attack.skill == 4211006) { // meso explosion
            for (AttackPair oned : attack.allDamage) {
                if (oned.attack != null) {
                    continue;
                }
                final MapleMapObject mapobject = map.getMapObject(oned.objectid, MapleMapObjectType.ITEM);

                if (mapobject != null) {
                    final MapleMapItem mapitem = (MapleMapItem) mapobject;
                    mapitem.getLock().lock();
                    try {
                        if (mapitem.getMeso() > 0) {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            map.removeMapObject(mapitem);
                            map.broadcastMessage(MaplePacketCreator.explodeDrop(mapitem.getObjectId()));
                            mapitem.setPickedUp(true);
                        } else {
                            player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                            return;
                        }
                    } finally {
                        mapitem.getLock().unlock();
                    }
                } else {
                    player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                    return; // etc explosion, exploding nonexistant things, etc.
                }
            }
        }
        int fixeddmg, totDamageToOneMonster = 0;
        long hpMob = 0;
        final PlayerStats stats = player.getStat();

        int CriticalDamage = stats.passive_sharpeye_percent();
        byte ShdowPartnerAttackPercentage = 0;
        if (attack_type == AttackType.RANGED_WITH_SHADOWPARTNER || attack_type == AttackType.NON_RANGED_WITH_MIRROR) {
            final MapleStatEffect shadowPartnerEffect;
            if (attack_type == AttackType.NON_RANGED_WITH_MIRROR) {
                shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.MIRROR_IMAGE);
            } else {
                shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.SHADOWPARTNER);
            }
            if (shadowPartnerEffect != null) {
                if (attack.skill != 0 && attack_type != AttackType.NON_RANGED_WITH_MIRROR) {
                    ShdowPartnerAttackPercentage = (byte) shadowPartnerEffect.getY();
                } else {
                    ShdowPartnerAttackPercentage = (byte) shadowPartnerEffect.getX();
                }
            }
            attackCount /= 2; // hack xD
        }

        byte overallAttackCount; // Tracking of Shadow Partner additional damage.
        double maxDamagePerHit = 0;
        MapleMonster monster;
        MapleMonsterStats monsterstats;
        boolean Tempest;

        for (final AttackPair oned : attack.allDamage) {
            monster = map.getMonsterByOid(oned.objectid);

            if (monster != null) {
                totDamageToOneMonster = 0;
                hpMob = monster.getHp();
                monsterstats = monster.getStats();
                fixeddmg = monsterstats.getFixedDamage();
                Tempest = monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006;

                if (!Tempest && !player.isGM()) {
                    if (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY) && !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
                        maxDamagePerHit = CalculateMaxWeaponDamagePerHit(player, monster, attack, theSkill, effect, maxDamagePerMonster, CriticalDamage);
                    } else {
                        maxDamagePerHit = 1;
                    }
                }
                overallAttackCount = 0; // Tracking of Shadow Partner additional damage.
                Integer eachd;
                for (Pair<Integer, Boolean> eachde : oned.attack) {
                    eachd = eachde.left;
                    overallAttackCount++;

                    if (overallAttackCount - 1 == attackCount) { // Is a Shadow partner hit so let's divide it once
                        maxDamagePerHit = (maxDamagePerHit / 100) * ShdowPartnerAttackPercentage;
                    }
                    // System.out.println("Client damage : " + eachd + " Server : " + maxDamagePerHit);
                    if (fixeddmg != -1) {
                        if (monsterstats.getOnlyNoramlAttack()) {
                            eachd = attack.skill != 0 ? 0 : fixeddmg;
                        } else {
                            eachd = fixeddmg;
                        }
                    } else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = attack.skill != 0 ? 0 : Math.min(eachd, (int) maxDamagePerHit);  // Convert to server calculated damage
                    } else if (!player.isGM()) {
                        if (Tempest) { // Monster buffed with Tempest
                            if (eachd > monster.getMobMaxHp()) {
                                eachd = (int) Math.min(monster.getMobMaxHp(), Integer.MAX_VALUE);
                                player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE);
                            }
                        } else if (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY) && !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
                            if (eachd > maxDamagePerHit) {
                                player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE);
                                if (eachd > maxDamagePerHit * 2) {
                                    eachd = (int) (maxDamagePerHit * 2); // Convert to server calculated damage
                                    player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_2);
                                }
                            }
                            if ((eachd > maxDamagePerHit) && (maxDamagePerHit > 1000.0D)) {
                                player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE, new StringBuilder().append("[伤害: ").append(eachd).append(", 预计伤害: ").append(maxDamagePerHit).append(", 怪物ID: ").append(monster.getId()).append("] [职业: ").append(player.getJob()).append(", 等级: ").append(player.getLevel()).append(", 技能: ").append(attack.skill).append("]").toString());
                                if (attack.real) {
                                    player.getCheatTracker().checkSameDamage(eachd, maxDamagePerHit);
                                }
                                if (eachd > maxDamagePerHit * 3.0D) {
                                    eachd = (int) (maxDamagePerHit * 2.0D);
                                    if (eachd >= 199999) {
                                        return;
                                    }
                                }
                            }
                        } else if (eachd > maxDamagePerHit) {
                            eachd = (int) (maxDamagePerHit);
                        }
                    }
                    /*if (player == null) { // o_O
                        return;
                    }*/
                    if (player.getClient().getChannelServer().isAdminOnly()) {
                        player.dropMessage(5, "Damage: " + eachd);
                    }
                    totDamageToOneMonster += eachd;
                    //force the miss even if they dont miss. popular wz edit
                    if (monster.getId() == 9300021 && player.getPyramidSubway() != null) { //miss
                        player.getPyramidSubway().onMiss(player);
                    }
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                /*if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100) && (!GameConstants.isNoDelaySkill(attack.skill)) && (!GameConstants.is不检测范围(attack.skill)) && (!monster.getStats().isBoss()) && (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange))) {
                    World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, new StringBuilder().append("[GM Message] ").append(player.getName()).append(" (等级 ").append(player.getLevel()).append(") 攻击范围异常。 地图ID: ").append(player.getMapId()).append(" 职业: ").append(player.getJob()).toString()));
                    player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, new StringBuilder().append("[范围: ").append(player.getTruePosition().distanceSq(monster.getTruePosition())).append(", 预期范围: ").append(GameConstants.getAttackRange(effect, player.getStat().defRange)).append(" 职业: ").append(player.getJob()).append("]").toString());
                }*/
                if (player.getPosition().distanceSq(monster.getPosition()) > 700000.0) { // 815^2 <-- the most ranged attack in the game is Flame Wheel at 815 range
                    player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER); // , Double.toString(Math.sqrt(distance))
                }
                // pickpocket
                if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
                    switch (attack.skill) {
                        case 0:
                        case 4001334:
                        case 4201005:
                        case 4211002:
                        case 4211004:
                        case 4221003:
                        case 4221007:
                            handlePickPocket(player, monster, oned);
                            break;
                    }
                }
                final MapleStatEffect ds = player.getStatForBuff(MapleBuffStat.DARKSIGHT);
                if (ds != null && !player.isGM()) {
                    if (ds.getSourceId() != 4330001 || !ds.makeChanceResult()) {
                        player.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
                    }
                }

                if (totDamageToOneMonster > 0) {
                    if (attack.skill != 1221011) {// 圣域
                        monster.damage(player, totDamageToOneMonster, true, attack.skill);
                    } else {
                        monster.damage(player, (monster.getStats().isBoss() ? 199999 : (monster.getHp() - 1)), true, attack.skill);
                    }
                    if (monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) { //test
                        player.addHP(-(7000 + Randomizer.nextInt(8000))); //this is what it seems to be?
                    }
                    if (stats.hpRecoverProp > 0) {
                        if (Randomizer.nextInt(100) <= stats.hpRecoverProp) {//i think its out of 100, anyway
                            player.healHP(stats.hpRecover);
                        }
                    }
                    if (stats.mpRecoverProp > 0) {
                        if (Randomizer.nextInt(100) <= stats.mpRecoverProp) {//i think its out of 100, anyway
                            player.healMP(stats.mpRecover);
                        }
                    }
                    if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                        stats.setHp((stats.getHp() + ((int) Math.min(monster.getMobMaxHp(), Math.min(((int) ((double) totDamage * player.getStatForBuff(MapleBuffStat.COMBO_DRAIN).getX() / 100.0)), stats.getMaxHp() / 2)))), true);
                    }
                    // effects
                    switch (attack.skill) {
                        case 4101005: //drain
                        case 5111004: { // Energy Drain
                            stats.setHp((stats.getHp() + ((int) Math.min(monster.getMobMaxHp(), Math.min(((int) ((double) totDamage * theSkill.getEffect(player.getSkillLevel(theSkill)).getX() / 100.0)), stats.getMaxHp() / 2)))), true);
                            break;
                        }
                        case 5211006:
                        case 22151002: //killer wing
                        case 5220011: {//homing
                            player.setLinkMid(monster.getObjectId());
                            break;
                        }
                        case 1311005: { // Sacrifice
                            final int remainingHP = stats.getHp() - totDamage * effect.getX() / 100;
                            stats.setHp(remainingHP < 1 ? 1 : remainingHP);
                            break;
                        }
                        case 4301001:
                        case 4311002:
                        case 4311003:
                        case 4331000:
                        case 4331004:
                        case 4331005:
                        case 4341005:
                        case 4221007: // Boomerang Stab
                        case 4221001: // 暗杀
                        case 4211002: // Assulter
                        case 4201005: // Savage Blow
                        case 4001002: // Disorder
                        case 4001334: // Double Stab
                        case 4121007: // Triple Throw
                        case 4111005: // Avenger
                        case 4001344: { // Lucky Seven
                            // Venom
                            final ISkill skill = SkillFactory.getSkill(4120005);
                            final ISkill skill2 = SkillFactory.getSkill(4220005);
                            final ISkill skill3 = SkillFactory.getSkill(4340001);
                            if (player.getSkillLevel(skill) > 0) {
                                // 屏蔽武器用毒液
//                                final MapleStatEffect venomEffect = skill.getEffect(player.getSkillLevel(skill));
//                                MonsterStatusEffect monsterStatusEffect;
//
//                                for (int i = 0; i < attackCount; i++) {
//                                    if (venomEffect.makeChanceResult()) {
//                                        if (monster.getVenomMulti() < 3) {
//                                            monster.setVenomMulti((byte) (monster.getVenomMulti() + 1));
//                                            monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.POISON, 1, 4120005, null, false);
//                                            monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
//                                        }
//                                    }
//                                }
                            } else if (player.getSkillLevel(skill2) > 0) {
                                final MapleStatEffect venomEffect = skill2.getEffect(player.getSkillLevel(skill2));
                                MonsterStatusEffect monsterStatusEffect;

                                for (int i = 0; i < attackCount; i++) {
                                    if (venomEffect.makeChanceResult()) {
                                        if (monster.getVenomMulti() < 3) {
                                            monster.setVenomMulti((byte) (monster.getVenomMulti() + 1));
                                            monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.POISON, 1, 4220005, null, false);
                                            monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                        }
                                    }
                                }
                            } else if (player.getSkillLevel(skill3) > 0) {
                                final MapleStatEffect venomEffect = skill3.getEffect(player.getSkillLevel(skill3));
                                MonsterStatusEffect monsterStatusEffect;

                                for (int i = 0; i < attackCount; i++) {
                                    if (venomEffect.makeChanceResult()) {
                                        if (monster.getVenomMulti() < 3) {
                                            monster.setVenomMulti((byte) (monster.getVenomMulti() + 1));
                                            monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.POISON, 1, 4340001, null, false);
                                            monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case 4201004: { //steal
//                            monster.handleSteal(player);
                            break;
                        }
                        //case 21101003: // body pressure
                        case 21000002: // Double attack
                        case 21100001: // Triple Attack
                        case 21100002: // Pole Arm Push
                        case 21100004: // Pole Arm Smash
                        case 21110002: // Full Swing
                        case 21110003: // Pole Arm Toss
                        case 21110004: // Fenrir Phantom
                        case 21110006: // Whirlwind
                        case 21110007: // (hidden) Full Swing - Double Attack
                        case 21110008: // (hidden) Full Swing - Triple Attack
                        case 21120002: // Overswing
                        case 21120005: // Pole Arm finale
                        case 21120006: // Tempest
                        case 21120009: // (hidden) Overswing - Double Attack
                        case 21120010: { // (hidden) Overswing - Triple Attack
                            if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null && !monster.getStats().isBoss()) {
                                final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.WK_CHARGE);
                                if (eff != null && eff.getSourceId() == 21111005) {
                                    monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, eff.getX(), eff.getSourceId(), null, false), false, eff.getY() * 1000, false);
                                }
                            }
                            if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null && !monster.getStats().isBoss()) {
                                final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BODY_PRESSURE);

                                if (eff != null && eff.makeChanceResult() && !monster.isBuffed(MonsterStatus.NEUTRALISE)) {
                                    monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.NEUTRALISE, 1, eff.getSourceId(), null, false), false, eff.getX() * 1000, false);
                                }
                            }
                            break;
                        }
                        default: //passives attack bonuses
                            break;
                    }
                    if (totDamageToOneMonster > 0) {
                        IItem weapon_ = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                        if (weapon_ != null) {
                            MonsterStatus stat = GameConstants.getStatFromWeapon(weapon_.getItemId()); //10001 = acc/darkness. 10005 = speed/slow.
                            if (stat != null && Randomizer.nextInt(100) < GameConstants.getStatChance()) {
                                final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(stat, GameConstants.getXForStat(stat), GameConstants.getSkillForStat(stat), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, 10000, false, false);
                            }
                        }
                        if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                            final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BLIND);

                            if (eff.makeChanceResult()) {
                                final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.ACC, eff.getX(), eff.getSourceId(), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, false);
                            }

                        }
                        if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                            final ISkill skill = SkillFactory.getSkill(3121007);
                            final MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));

                            if (eff.makeChanceResult()) {
                                final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.SPEED, eff.getX(), 3121007, null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, false);
                            }
                        }
                        if (player.getJob() == 121) { // WHITEKNIGHT
                            for (int charge : charges) {
                                final ISkill skill = SkillFactory.getSkill(charge);
                                if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill)) {
                                    final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.FREEZE, 1, charge, null, false);
                                    monster.applyStatus(player, monsterStatusEffect, false, skill.getEffect(player.getSkillLevel(skill)).getY() * 2000, false);
                                    break;
                                }
                            }
                        }
                    }
                    if (effect != null && effect.getMonsterStati().size() > 0) {
                        if (effect.makeChanceResult()) {
                            for (Map.Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                                monster.applyStatus(player, new MonsterStatusEffect(z.getKey(), z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), false);
                            }
                        }
                    }
                }
            }
        }
        if (attack.skill == 4331003 && totDamageToOneMonster < hpMob) {
            return;
        }
        if (attack.skill != 0 && (attack.targets > 0 || (attack.skill != 4331003 && attack.skill != 4341002)) && attack.skill != 21101003 && attack.skill != 5110001 && attack.skill != 15100004 && attack.skill != 11101002 && attack.skill != 13101002) {
            effect.applyTo(player, attack.position);
        }
        if (totDamage > 1) {
            final CheatTracker tracker = player.getCheatTracker();

            tracker.setAttacksWithoutHit(true);
            if (tracker.getAttacksWithoutHit() > 1000) {
                tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
            }
        }
    }

    /**
     *
     * @param attack
     * @param theSkill
     * @param player
     * @param effect
     */
    public static final void applyAttackMagic(final AttackInfo attack, final ISkill theSkill, final MapleCharacter player, final MapleStatEffect effect) {
        if (!player.isAlive()) {
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            System.out.println("Return 7");
            return;
        }
        if (attack.real) {
            player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
        }
        if (attack.hits > effect.getAttackCount() || attack.targets > effect.getMobCount()) {
            player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
            return;
        }
        if (attack.hits > 0 && attack.targets > 0) {
            if (!player.getStat().checkEquipDurabilitys(player, -1)) { //i guess this is how it works ?
                player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
                return;
            } //lol
        }
        if (GameConstants.isMulungSkill(attack.skill)) {
            if (player.getMapId() / 10000 != 92502) {
                return;
            } else {
                player.mulung_EnergyModify(false);
            }
        }
        if (GameConstants.isPyramidSkill(attack.skill)) {
            if (player.getMapId() / 1000000 != 926) {
                return;
            } else if (player.getPyramidSubway() == null || !player.getPyramidSubway().onSkillUse(player)) {
                return;
            }
        }
        final PlayerStats stats = player.getStat();
        double maxDamagePerHit;
        if (attack.skill == 2301002) {
            maxDamagePerHit = 30000;
        } else if (attack.skill == 1000 || attack.skill == 10001000 || attack.skill == 20001000 || attack.skill == 20011000 || attack.skill == 30001000) {
            maxDamagePerHit = 40;
        } else if (GameConstants.isPyramidSkill(attack.skill)) {
            maxDamagePerHit = 1;
        } else {
            final double v75 = (effect.getMatk() * 0.058);
            maxDamagePerHit = stats.getTotalMagic() * (stats.getInt() * 0.5 + (v75 * v75) + effect.getMatk() * 3.3) / 100;
        }
        maxDamagePerHit *= 1.04; // Avoid any errors for now

        final Element element = player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null ? Element.NEUTRAL : theSkill.getElement();

        double MaxDamagePerHit = 0;
        int totDamageToOneMonster, totDamage = 0, fixeddmg;
        byte overallAttackCount;
        boolean Tempest;
        MapleMonsterStats monsterstats;
        int CriticalDamage = stats.passive_sharpeye_percent();
        final ISkill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
        final int eaterLevel = player.getSkillLevel(eaterSkill);

        final MapleMap map = player.getMap();

        if (map.getId() == GameConstants.PVP_MAP && player.getClient().getChannel() == GameConstants.PVP_CHANEL) {
            MaplePvp.doPvP(player, map, attack, effect);
        }

        for (final AttackPair oned : attack.allDamage) {
            final MapleMonster monster = map.getMonsterByOid(oned.objectid);

            if (monster != null) {
                Tempest = monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006 && !monster.getStats().isBoss();
                totDamageToOneMonster = 0;
                monsterstats = monster.getStats();
                fixeddmg = monsterstats.getFixedDamage();
                if (!Tempest && !player.isGM()) {
                    if (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) {
                        MaxDamagePerHit = CalculateMaxMagicDamagePerHit(player, theSkill, monster, monsterstats, stats, element, CriticalDamage, maxDamagePerHit);
                    } else {
                        MaxDamagePerHit = 1;
                    }
                }
                overallAttackCount = 0;
                Integer eachd;
                for (Pair<Integer, Boolean> eachde : oned.attack) {
                    eachd = eachde.left;
                    overallAttackCount++;
                    if (fixeddmg != -1) {
                        eachd = monsterstats.getOnlyNoramlAttack() ? 0 : fixeddmg; // Magic is always not a normal attack
                    } else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = 0; // Magic is always not a normal attack
                    } else if (!player.isGM()) {
                        if (Tempest) { // Buffed with Tempest
                            if (eachd > monster.getMobMaxHp()) {
                                eachd = (int) Math.min(monster.getMobMaxHp(), Integer.MAX_VALUE);
                                player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC);
                            }
                        } else if (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) {
                            if (eachd > maxDamagePerHit) {
                                player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC);
                                if (eachd > MaxDamagePerHit * 2) {
                                    eachd = (int) (MaxDamagePerHit * 2); // Convert to server calculated damage
                                    player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC_2);
                                }
                            }
                        } else if (eachd > maxDamagePerHit) {
                            eachd = (int) (maxDamagePerHit);
                        }
                    }
                    totDamageToOneMonster += eachd;
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);

                if (player.getPosition().distanceSq(monster.getPosition()) > 700000.0) { // 600^2, 550 is approximatly the range of ultis
                    player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER);
                }
                if (attack.skill == 2301002 && !monsterstats.getUndead()) {
                    player.getCheatTracker().registerOffense(CheatingOffense.HEAL_ATTACKING_UNDEAD);
                    return;
                }

                if (totDamageToOneMonster > 0) {
                    monster.damage(player, totDamageToOneMonster, true, attack.skill);
                    if (monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) { //test
                        player.addHP(-(7000 + Randomizer.nextInt(8000))); //this is what it seems to be?
                    }
                    // effects
                    switch (attack.skill) {
                        case 2221003:
                            monster.setTempEffectiveness(Element.FIRE, theSkill.getEffect(player.getSkillLevel(theSkill)).getDuration());
                            break;
                        case 2121003:
                            monster.setTempEffectiveness(Element.ICE, theSkill.getEffect(player.getSkillLevel(theSkill)).getDuration());
                            break;
                    }
                    if (effect != null && effect.getMonsterStati().size() > 0) {
                        if (effect.makeChanceResult()) {
                            for (Map.Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                                monster.applyStatus(player, new MonsterStatusEffect(z.getKey(), z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), false);
                            }
                        }
                    }
                    if (eaterLevel > 0) {
                        eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
                    }
                }
            }
        }
        if (attack.skill != 2301002) {
            effect.applyTo(player);
        }

        if (totDamage > 1) {
            final CheatTracker tracker = player.getCheatTracker();
            tracker.setAttacksWithoutHit(true);

            if (tracker.getAttacksWithoutHit() > 1000) {
                tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
            }
        }
    }

    private static double CalculateMaxMagicDamagePerHit(final MapleCharacter chr, final ISkill skill, final MapleMonster monster, final MapleMonsterStats mobstats, final PlayerStats stats, final Element elem, final Integer sharpEye, final double maxDamagePerMonster) {
        final int dLevel = Math.max(mobstats.getLevel() - chr.getLevel(), 0);
        final int Accuracy = (int) (Math.floor((stats.getTotalInt() / 10.0)) + Math.floor((stats.getTotalLuk() / 10.0)));
        final int MinAccuracy = mobstats.getEva() * (dLevel * 2 + 51) / 120;

        if (MinAccuracy > Accuracy && skill.getId() != 1000 && skill.getId() != 10001000 && skill.getId() != 20001000 && skill.getId() != 20011000 && skill.getId() != 30001000 && !GameConstants.isPyramidSkill(skill.getId())) { // miss :P or HACK :O
            return 0;
        }
        double elemMaxDamagePerMob;

        switch (monster.getEffectiveness(elem)) {
            case IMMUNE:
                elemMaxDamagePerMob = 1;
                break;
            case NORMAL:
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster, stats);
                break;
            case WEAK:
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * 1.5, stats);
                break;
            case STRONG:
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * 0.5, stats);
                break;
            default:
                throw new RuntimeException("Unknown enum constant");
        }
        elemMaxDamagePerMob -= mobstats.getMagicDefense() * 0.5;
        elemMaxDamagePerMob += (elemMaxDamagePerMob / 100) * sharpEye;
        elemMaxDamagePerMob += (elemMaxDamagePerMob * (mobstats.isBoss() ? stats.bossdam_r : stats.dam_r)) / 100;
        switch (skill.getId()) {
            case 1000:
            case 10001000:
            case 20001000:
            case 20011000:
            case 30001000:
                elemMaxDamagePerMob = 40;
                break;
            case 1020:
            case 10001020:
            case 20001020:
            case 20011020:
            case 30001020:
                elemMaxDamagePerMob = 1;
                break;
        }
        if (elemMaxDamagePerMob > 199999) {
            elemMaxDamagePerMob = 199999;
        } else if (elemMaxDamagePerMob < 0) {
            elemMaxDamagePerMob = 1;
        }
        return elemMaxDamagePerMob;
    }

    private static double ElementalStaffAttackBonus(final Element elem, double elemMaxDamagePerMob, final PlayerStats stats) {
        switch (elem) {
            case FIRE:
                return (elemMaxDamagePerMob / 100) * stats.element_fire;
            case ICE:
                return (elemMaxDamagePerMob / 100) * stats.element_ice;
            case LIGHTING:
                return (elemMaxDamagePerMob / 100) * stats.element_light;
            case POISON:
                return (elemMaxDamagePerMob / 100) * stats.element_psn;
            default:
                return (elemMaxDamagePerMob / 100) * stats.def;
        }
    }

    private static void handlePickPocket(final MapleCharacter player, final MapleMonster mob, AttackPair oned) {
        final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET);
        final ISkill skill = SkillFactory.getSkill(4211003);
        final MapleStatEffect s = skill.getEffect(player.getSkillLevel(skill));

        for (final Pair<Integer, Boolean> eachde : oned.attack) {
            final Integer eachd = eachde.left;
            if (s.makeChanceResult()) {

                MapTimer.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachd / 20000) * maxmeso, 1), maxmeso), new Point((int) (mob.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (mob.getPosition().getY())), mob, player, true, (byte) 0);
                    }
                }, 100);
            }
        }
    }

    private static double CalculateMaxWeaponDamagePerHit(final MapleCharacter player, final MapleMonster monster, final AttackInfo attack, final ISkill theSkill, final MapleStatEffect attackEffect, double maximumDamageToMonster, final Integer CriticalDamagePercent) {
        if (player.getMapId() / 1000000 == 914) { //aran
            return 199999;
        }
        List<Element> elements = new ArrayList<>();
        boolean defined = false; // 是否能超过 199999
        if (theSkill != null) {
            elements.add(theSkill.getElement());

            switch (theSkill.getId()) {
                case 3001004: // 断魂箭
                    defined = true; //can go past 199999
                    break;
                case 1000: // 蜗牛投掷术
                case 10001000: // 蜗牛投掷术
                case 20001000: // 蜗牛投掷术
                    maximumDamageToMonster = 40;
                    defined = true;
                    break;
                case 1020: // 法老王的愤怒
                case 10001020: // 法老王的愤怒
                case 20001020: // 法老王的愤怒
                    maximumDamageToMonster = 1;
                    defined = true;
                    break;
//                case 4331003: //Owl Spirit
//                    maximumDamageToMonster = (monster.getStats().isBoss() ? 199999 : monster.getHp());
//                    defined = true;
//                    break;
                case 3221007: // 一击要害箭
                    maximumDamageToMonster = (monster.getStats().isBoss() ? 199999 : monster.getMobMaxHp());
                    defined = true;
                    break;
                case 1221011:// 圣域
                    maximumDamageToMonster = (monster.getStats().isBoss() ? 199999 : monster.getHp() - 1);
                    defined = true;
                    break;
                case 4211006: // 金钱炸弹
                    maximumDamageToMonster = 750000;
                    defined = true;
                    break;
                case 1009: // 流星竹雨
                case 10001009: // 流星竹雨
                case 20001009: // 流星竹雨
                    defined = true;
                    maximumDamageToMonster = (monster.getStats().isBoss() ? monster.getMobMaxHp() / 30 * 100 : monster.getMobMaxHp());
                    break;
                case 3211006: // 箭扫射
                    if (monster.getStatusSourceID(MonsterStatus.FREEZE) == 3211003) { //blizzard in effect
                        defined = true;
                        maximumDamageToMonster = monster.getHp();
                    }
                    break;
            }
        }
        if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
            int chargeSkillId = player.getBuffSource(MapleBuffStat.WK_CHARGE);

            switch (chargeSkillId) {
                case 1211003: // 烈焰之剑
                case 1211004: // 烈焰钝器
                    elements.add(Element.FIRE);
                    break;
                case 1211005: // 寒冰之剑
                case 1211006: // 寒冰钝器
                case 21111005: // 冰雪矛
                    elements.add(Element.ICE);
                    break;
                case 1211007: // 雷电之击：剑
                case 1211008: // 雷电之击：钝器
                case 15101006: // 雷鸣
                    elements.add(Element.LIGHTING);
                    break;
                case 1221003: // 圣灵之剑
                case 1221004: // 圣灵之锤
                case 11111007: // 灵魂属性
                    elements.add(Element.HOLY);
                    break;
                case 12101005: // 自然力重置
                    elements.clear(); //neutral
                    break;
            }
        }
        if (player.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE) != null) {
            elements.add(Element.LIGHTING);
        }
        double elementalMaxDamagePerMonster = maximumDamageToMonster;
        if (elements.size() > 0) {
            double elementalEffect;

            switch (attack.skill) {
                case 3211003: // 寒冰箭
                case 3111003: // 烈火箭
                    elementalEffect = attackEffect.getX() / 200.0;
                    break;
                default:
                    elementalEffect = 0.5;
                    break;
            }
            for (Element element : elements) {
                switch (monster.getEffectiveness(element)) {
                    case IMMUNE:
                        elementalMaxDamagePerMonster = 1;
                        break;
                    case WEAK:
                        elementalMaxDamagePerMonster *= (1.0 + elementalEffect);
                        break;
                    case STRONG:
                        elementalMaxDamagePerMonster *= (1.0 - elementalEffect);
                        break;
                    default:
                        break; //normal nothing
                }
            }
        }
        final short moblevel = monster.getStats().getLevel();
        final short d = moblevel > player.getLevel() ? (short) (moblevel - player.getLevel()) : 0;
        elementalMaxDamagePerMonster = elementalMaxDamagePerMonster * (1 - 0.01 * d) - monster.getStats().getPhysicalDefense() * 0.5;

        elementalMaxDamagePerMonster += (elementalMaxDamagePerMonster / 100.0) * CriticalDamagePercent;

        if (theSkill != null && theSkill.isChargeSkill() && player.getKeyDownSkill_Time() == 0) {
            return 0;
        }
        final MapleStatEffect homing = player.getStatForBuff(MapleBuffStat.HOMING_BEACON);
        if (homing != null && player.getLinkMid() == monster.getObjectId() && homing.getSourceId() == 5220011) { //bullseye
            elementalMaxDamagePerMonster += (elementalMaxDamagePerMonster * homing.getX());
        }
        final PlayerStats stat = player.getStat();
        elementalMaxDamagePerMonster += (elementalMaxDamagePerMonster * (monster.getStats().isBoss() ? stat.bossdam_r : stat.dam_r)) / 100.0;

        if (elementalMaxDamagePerMonster > 199999) {
            if (!defined) {
                elementalMaxDamagePerMonster = 199999;
            }
        } else if (elementalMaxDamagePerMonster < 0) {
            elementalMaxDamagePerMonster = 1;
        }
        return elementalMaxDamagePerMonster;
    }

    /**
     *
     * @param attack
     * @param rate
     * @return
     */
    public static final AttackInfo DivideAttack(final AttackInfo attack, final int rate) {
        attack.real = false;
        if (rate <= 1) {
            return attack; //lol
        }
        for (AttackPair p : attack.allDamage) {
            if (p.attack != null) {
                for (Pair<Integer, Boolean> eachd : p.attack) {
                    eachd.left /= rate; //too ex.
                }
            }
        }
        return attack;
    }

    /**
     *
     * @param attack
     * @param chr
     * @param type
     * @return
     */
    public static final AttackInfo Modify_AttackCrit(final AttackInfo attack, final MapleCharacter chr, final int type) {
        final int CriticalRate = chr.getStat().passive_sharpeye_rate();
        final boolean shadow = (type == 2 && chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) || (type == 1 && chr.getBuffedValue(MapleBuffStat.MIRROR_IMAGE) != null);
        if (attack.skill != 4211006 && attack.skill != 3211003 && attack.skill != 4111004 && (CriticalRate > 0 || attack.skill == 4221001 || attack.skill == 3221007)) { //blizz + shadow meso + m.e no crits
            for (AttackPair p : attack.allDamage) {
                if (p.attack != null) {
                    int hit = 0;
                    final int mid_att = p.attack.size() / 2;
                    final List<Pair<Integer, Boolean>> eachd_copy = new ArrayList<>(p.attack);
                    for (Pair<Integer, Boolean> eachd : p.attack) {
                        hit++;
                        if (!eachd.right) {
                            if (attack.skill == 4221001) { // 暗杀 assassinate never crit first 3, always crit last
                                eachd.right = (hit == 4 && Randomizer.nextInt(100) < 90);
                            } else if (attack.skill == 3221007 || eachd.left > 199999) { // 这个技能总是暴击
                                eachd.right = true;
                            } else if (shadow && hit > mid_att) { //影分身复制主体的暴击
                                eachd.right = eachd_copy.get(hit - 1 - mid_att).right;
                            } else {
                                eachd.right = (Randomizer.nextInt(100)) < CriticalRate;
                            }
                            eachd_copy.get(hit - 1).right = eachd.right;
                        }
                    }
                }
            }
        }
        return attack;
    }

    /**
     *
     * @param lea
     * @param chr
     * @return
     */
    public static final AttackInfo parseDmgMa(final LittleEndianAccessor lea, final MapleCharacter chr) {
        final AttackInfo ret = new AttackInfo();

        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        lea.skip(8); //?
        ret.skill = lea.readInt();
        lea.skip(12); // ORDER [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        switch (ret.skill) {
            case 2121001: // 创世之破
            case 2221001: // 创世之破
            case 2321001: // 创世之破
                ret.charge = lea.readInt();
                break;
            default:
                ret.charge = -1;
                break;
        }
        lea.skip(1);
        ret.unk = 0;
        ret.display = lea.readByte(); // Always zero?
        ret.animation = lea.readByte();
        lea.skip(1); // Weapon subclass
        ret.speed = lea.readByte(); // Confirmed
        ret.lastAttackTickCount = lea.readInt(); // Ticks

        int oid, damage;
        List<Pair<Integer, Boolean>> allDamageNumbers;
        ret.allDamage = new ArrayList<>();

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(14); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack

            allDamageNumbers = new ArrayList<>();

            MapleMonster monster = chr.getMap().getMonsterByOid(oid);
            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                if (ret.skill > 0) {
                    damage = Damage_SkillPD(chr, damage, ret);
                } else {
                    damage = Damage_NoSkillPD(chr, damage);
                }
                damage = Damage_PG(chr, damage, ret);
                allDamageNumbers.add(new Pair<>(damage, false));
            }
            lea.skip(4); // CRC of monster [Wz Editing]
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();

        return ret;
    }

    /**
     *
     * @param lea
     * @param chr
     * @return
     */
    public static final AttackInfo parseDmgM(final LittleEndianAccessor lea, final MapleCharacter chr) {
        final AttackInfo ret = new AttackInfo();

        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12); // ORDER [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        switch (ret.skill) {
            case 5101004: // 贯骨击
            case 15101003: // 贯骨击
            case 5201002: // 投弹攻击
            case 14111006: // 毒炸弹
                ret.charge = lea.readInt();
                break;
            default:
                ret.charge = 0;
                break;
        }
        lea.skip(1);
        ret.unk = 0;
        ret.display = lea.readByte(); // Always zero?
        ret.animation = lea.readByte();
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte(); // Confirmed
        ret.lastAttackTickCount = lea.readInt(); // Ticks

        ret.allDamage = new ArrayList<>();

        if (ret.skill == 4211006) { // Meso Explosion
            return parseMesoExplosion(lea, ret, chr);
        }
        int oid, damage;
        List<Pair<Integer, Boolean>> allDamageNumbers;

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(14); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack

            allDamageNumbers = new ArrayList<>();

            MapleMonster monster = chr.getMap().getMonsterByOid(oid);
            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                if (ret.skill > 0) {
                    damage = Damage_SkillPD(chr, damage, ret);
                } else {
                    damage = Damage_NoSkillPD(chr, damage);
                }
                damage = Damage_PG(chr, damage, ret);
                allDamageNumbers.add(new Pair<>(damage, false));
            }
            lea.skip(4); // CRC of monster [Wz Editing]
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        return ret;
    }

    /**
     *
     * @param lea
     * @param chr
     * @return
     */
    public static final AttackInfo parseDmgR(final LittleEndianAccessor lea, final MapleCharacter chr) {
        final AttackInfo ret = new AttackInfo();

        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        lea.skip(8);
        ret.skill = lea.readInt();

        lea.skip(12); // ORDER [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82

        switch (ret.skill) {
            case 3121004: // 暴风箭雨
            case 3221001: // 穿透箭
            case 5221004: // 金属风暴
            case 13111002: // 暴风箭雨
                lea.skip(4); // extra 4 bytes
                break;
        }
        ret.charge = -1;
        lea.skip(1);
        ret.unk = 0;
        ret.display = lea.readByte(); // Always zero?
        ret.animation = lea.readByte();
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte(); // Confirmed
        ret.lastAttackTickCount = lea.readInt(); // Ticks
        ret.slot = (byte) lea.readShort();
        ret.csstar = (byte) lea.readShort();
        ret.AOE = lea.readByte(); // is AOE or not, TT/ Avenger = 41, Showdown = 0

        int damage, oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;
        ret.allDamage = new ArrayList<>();

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(14); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack

            MapleMonster monster = chr.getMap().getMonsterByOid(oid);
            allDamageNumbers = new ArrayList<>();
            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                if (ret.skill > 0) {
                    damage = Damage_SkillPD(chr, damage, ret);
                } else {
                    damage = Damage_NoSkillPD(chr, damage);
                }
                damage = Damage_PG(chr, damage, ret);
                allDamageNumbers.add(new Pair<>(damage, false));
            }
            lea.skip(4); // CRC of monster [Wz Editing]

            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        lea.skip(4);
        ret.position = lea.readPos();

        return ret;
    }

    /**
     *
     * @param lea
     * @param ret
     * @param chr
     * @return
     */
    public static AttackInfo parseMesoExplosion(final LittleEndianAccessor lea, final AttackInfo ret, final MapleCharacter chr) {
        byte bullets;
        if (ret.hits == 0) {
            lea.skip(4);
            bullets = lea.readByte();
            for (int j = 0; j < bullets; j++) {
                ret.allDamage.add(new AttackPair(lea.readInt(), null));
                lea.skip(1);
            }
            lea.skip(2); // 8F 02
            return ret;
        }

        int oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(12);
            bullets = lea.readByte();
            allDamageNumbers = new ArrayList<>();
            for (int j = 0; j < bullets; j++) {
                int damage = lea.readInt();
                damage = Damage_SkillPD(chr, damage, ret);
                damage = Damage_PG(chr, damage, ret);
                allDamageNumbers.add(new Pair<>(damage, false)); //m.e. never crits
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
            lea.skip(4); // C3 8F 41 94, 51 04 5B 01
        }
        lea.skip(4);
        bullets = lea.readByte();

        for (int j = 0; j < bullets; j++) {
            ret.allDamage.add(new AttackPair(lea.readInt(), null));
            lea.skip(1);
        }
        lea.skip(2); // 8F 02/ 63 02

        return ret;
    }

    /**
     *
     * @param c
     * @param monster
     * @param ret
     */
    public static void Damage_Mob_Level(MapleCharacter c, MapleMonster monster, AttackInfo ret) {
        try {
            //80 - 90 - 20 = 80 - 70 
            if (c.getLevel() < monster.getStats().getLevel() - 20 && ret.skill != 4211006 && c.getJob() > 422 && c.getJob() < 400) {
                String 越级打怪检测 = "职业：" + c.getJob()
                        + "\r\n" + "等级：" + c.getLevel()
                        + "\r\n" + "怪物等级：" + monster.getStats().getLevel() + "怪物名字：" + monster.getStats().getName()
                        + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                        + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                FileoutputUtil.packetLog("log\\越级打怪检测\\" + c.getName() + ".log", 越级打怪检测);
            } else if (c.getLevel() < monster.getStats().getLevel() - 30 && ret.skill != 4211006) {
                String 越级打怪检测 = "职业：" + c.getJob()
                        + "\r\n" + "等级：" + c.getLevel()
                        + "\r\n" + "怪物等级：" + monster.getStats().getLevel() + "怪物名字：" + monster.getStats().getName()
                        + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                        + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                FileoutputUtil.packetLog("log\\越级打怪检测\\" + c.getName() + ".log", 越级打怪检测);
            }

        } catch (Exception e) {
        }
    }

    /**
     *
     * @param c
     * @param monster
     * @param ret
     */
    public static void Damage_Position(MapleCharacter c, MapleMonster monster, AttackInfo ret) {
        try {
            if (!GameConstants.不检测技能(ret.skill)) {
                if (c.getJob() >= 1300 && c.getJob() <= 1311
                        || c.getJob() >= 1400 && c.getJob() <= 1411
                        || c.getJob() >= 400 && c.getJob() <= 422
                        || c.getJob() >= 300 && c.getJob() <= 322
                        || c.getJob() == 500
                        || c.getJob() >= 520 && c.getJob() <= 522) {

                    if (c.getPosition().y - monster.getPosition().y >= 800) {
                        String 全屏 = "等级A：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    } else if (c.getPosition().y - monster.getPosition().y <= -800) {
                        String 全屏 = "等级B：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    } else if (c.getPosition().x - monster.getPosition().x >= 800) {
                        String 全屏 = "等级C：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    } else if (c.getPosition().x - monster.getPosition().x <= -900) {
                        String 全屏 = "等级D：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    }
                } else if (c.getJob() >= 200 && c.getJob() < 300) {
                    if (c.getPosition().y - monster.getPosition().y >= 800) {
                        String 全屏 = "等级E：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    } else if (c.getPosition().y - monster.getPosition().y <= -800) {
                        String 全屏 = "等级F：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    } else if (c.getPosition().x - monster.getPosition().x >= 550) {
                        String 全屏 = "等级G：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    } else if (c.getPosition().x - monster.getPosition().x <= -550) {
                        String 全屏 = "等级H：" + c.getLevel()
                                + "\r\n" + "职业：" + c.getJob()
                                + "\r\n" + "地图：" + c.getMapId()
                                + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                                + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                                + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                                + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                    }
                } else if (c.getPosition().y - monster.getPosition().y >= 350) {
                    String 全屏 = "等级I：" + c.getLevel()
                            + "\r\n" + "职业：" + c.getJob()
                            + "\r\n" + "地图：" + c.getMapId()
                            + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                            + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                            + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                            + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                } else if (c.getPosition().y - monster.getPosition().y <= -350) {
                    String 全屏 = "等级J：" + c.getLevel()
                            + "\r\n" + "职业：" + c.getJob()
                            + "\r\n" + "地图：" + c.getMapId()
                            + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                            + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                            + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                            + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                } else if (c.getPosition().x - monster.getPosition().x >= 500) {
                    String 全屏 = "等级K：" + c.getLevel()
                            + "\r\n" + "职业：" + c.getJob()
                            + "\r\n" + "地图：" + c.getMapId()
                            + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                            + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                            + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                            + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                } else if (c.getPosition().x - monster.getPosition().x <= -500) {
                    String 全屏 = "等级L：" + c.getLevel()
                            + "\r\n" + "职业：" + c.getJob()
                            + "\r\n" + "地图：" + c.getMapId()
                            + "\r\n" + "人物坐标：X:" + c.getPosition().x + " Y:" + c.getPosition().y
                            + "\r\n" + "怪物坐标：" + monster.getPosition().x + " Y:" + monster.getPosition().y
                            + "\r\n" + "时间：" + FileoutputUtil.CurrentReadable_Time()
                            + "\r\n" + "IP：" + c.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    FileoutputUtil.packetLog("log\\全屏检测\\" + c.getName() + ".log", 全屏);

                }
            }

        } catch (Exception e) {
        }
    }

    /**
     *
     * @param c
     * @param damage
     * @param ret
     * @return
     */
    public static final int Damage_PG(MapleCharacter c, int damage, AttackInfo ret) {

        if (ret.skill != 14101006) { // 吸血
            if (damage >= 199999) {
                int sj = Randomizer.nextInt(80000);
                damage = 199999 + (c.getStat().getTotalLuk() + c.getStat().getTotalDex() + c.getStat().getTotalStr() + c.getStat().getTotalInt()) * 3 + (c.getStat().getTotalWatk() + c.getStat().getTotalMagic()) * 6;
                if (damage > sj) {
                    damage = damage + sj;
                }
                if (damage >= 19999999) {
                    damage = 19999999;
                }
                DamageParse.pvpMob = MapleLifeFactory.getMonster(9400711);
                c.getClient().getSession().write(MaplePacketCreator.damagePlayer(ret.skill, DamageParse.pvpMob.getId(), c.getId(), damage));
                c.getClient().getSession().write(MaplePacketCreator.sendHint("#r破功伤害#k:" + damage, 200, 5));
            }
        }
        return damage;
    }

    /**
     *
     * @param c
     * @param damage
     * @return
     */
    public static final int Damage_NoSkillPD(MapleCharacter c, int damage) {
        if (c.getJob() == 1000 || c.getJob() == 0 || c.getJob() == 2000) {
            if (damage >= 150) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 2100 || c.getJob() == 2110 || c.getJob() == 2111 || c.getJob() == 2112) {//战神技能
            if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 6.8)) {
                damage = 1;
            }
            return damage;
        } else if (c.getJob() == 100 || c.getJob() == 110 || c.getJob() == 111 || c.getJob() == 112
                || c.getJob() == 120 || c.getJob() == 121 || c.getJob() == 122
                || c.getJob() == 130 || c.getJob() == 131 || c.getJob() == 132) { //战士技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 6) {
                damage = 1;
            }
            return damage;
        } else if (c.getJob() == 200 || c.getJob() == 210 || c.getJob() == 211 || c.getJob() == 212
                || c.getJob() == 220 || c.getJob() == 221 || c.getJob() == 222
                || c.getJob() == 230 || c.getJob() == 231 || c.getJob() == 232) {//魔法师技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 6) {
                damage = 1;
            }
            return damage;
        } else if (c.getJob() == 300 || c.getJob() == 310 || c.getJob() == 311 || c.getJob() == 312
                || c.getJob() == 320 || c.getJob() == 321 || c.getJob() == 322) {//弓箭手技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 400 || c.getJob() == 410 || c.getJob() == 411 || c.getJob() == 412
                || c.getJob() == 420 || c.getJob() == 421 || c.getJob() == 422) {//飞侠技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 500 || c.getJob() == 510 || c.getJob() == 511 || c.getJob() == 512
                || c.getJob() == 520 || c.getJob() == 521 || c.getJob() == 522) {//海盗技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1000 || c.getJob() == 1100 || c.getJob() == 1110 || c.getJob() == 1111) {//魂骑士技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1200 || c.getJob() == 1210 || c.getJob() == 1211) {//炎术士技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1300 || c.getJob() == 1310 || c.getJob() == 1311) {//风灵使者技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1400 || c.getJob() == 1410 || c.getJob() == 1411) {//夜行者技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1500 || c.getJob() == 1510 || c.getJob() == 1511) {//奇袭者技能
            if (c.getStat().getCurrentMaxBaseDamage() <= damage / 7) {
                damage = 1;
                return damage;
            }

        }
        return damage;
    }

    /**
     *
     * @param c
     * @param damage
     * @param ret
     * @return
     */
    public static final int Damage_SkillPD(MapleCharacter c, int damage, final AttackInfo ret) {
        if (GameConstants.Novice_Skill(ret.skill)) {//新手蜗牛壳技能
            if (damage > 40) {
                c.dropMessage(1, "你以为你猴赛雷？");
                c.dropMessage(1, "丢内楼母");
                c.dropMessage(1, "QNMLGB");
                c.dropMessage(1, "fuck you");
                c.dropMessage(1, "吃翔吧你");
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 2100 || c.getJob() == 2110 || c.getJob() == 2111 || c.getJob() == 2112) {//战神技能
            if (GameConstants.Ares_Skill_350(ret.skill)) { //  终极投掷 战神之舞  三重重击  全力挥击
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 13)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Ares_Skill_140(ret.skill)) { // 战神突进 冰雪矛  旋风 双重重击
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 20)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Ares_Skill_1500(ret.skill)) { // 钻石星辰
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 21)) {
                    return damage;
                }
            } else if (GameConstants.Ares_Skill_800(ret.skill)) { // 巨熊咆哮 幻影狼牙 斗气爆裂
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 14)) {
                    damage = 1;
                    return damage;
                }
            }
        } else if (c.getJob() == 100 || c.getJob() == 110 || c.getJob() == 111 || c.getJob() == 112
                || c.getJob() == 120 || c.getJob() == 121 || c.getJob() == 122
                || c.getJob() == 130 || c.getJob() == 131 || c.getJob() == 132) { //战士技能
            if (GameConstants.Warrior_Skill_450(ret.skill)) { // 终极斧 强力攻击 群体攻击 终极剑 斗气集中 虎咆哮 突进 轻舞飞扬 英雄之斧 属性攻击 突进 枪连击 矛连击 无双枪 无双矛 龙之献祭 龙咆哮 突进
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 11)) {
                    damage = 1;
                    return damage;
                }
            } else if (ret.skill == 1221009) { // 连环环破
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 18)) {
//                    damage = 1;
//                    return damage;
                }
            } else if (ret.skill == 1221011) { // 圣域
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 12)) {
//                    damage = 1;
//                    return damage;
                }
            } else if (GameConstants.Warrior_Skill_2000(ret.skill)) { // 勇士气绝剑等等
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 24)) {
//                    damage = 1;
//                    return damage;
                }
            }
        } else if (c.getJob() == 200 || c.getJob() == 210 || c.getJob() == 211 || c.getJob() == 212
                || c.getJob() == 220 || c.getJob() == 221 || c.getJob() == 222
                || c.getJob() == 230 || c.getJob() == 231 || c.getJob() == 232) {//魔法师技能
            if (GameConstants.Magician_Skill_90(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 15)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Magician_Skill_180(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 18)) {
                    damage = 1;
                    return damage;
                }

            } else if (GameConstants.Magician_Skill_240(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 20)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Magician_Skill_670(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 36)) {
                    damage = 1;
                    return damage;
                }
            }

        } else if (c.getJob() == 300 || c.getJob() == 310 || c.getJob() == 311 || c.getJob() == 312 || c.getJob() == 320 || c.getJob() == 321 || c.getJob() == 322) {//弓箭手技能
            if (GameConstants.Bowman_Skill_180(ret.skill) && (c.getBuffedValue(MapleBuffStat.SHARP_EYES) != null) && (damage > 0)) { //判断火眼
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 13)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Bowman_Skill_260(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 9)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Bowman_Skill_850(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 12)) {
                    damage = 1;
                    return damage;
                }
            } else if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 8.5) && ret.skill == 0) {
                damage = 1;
                return damage;
            }
            if (GameConstants.Bowman_Skill_180(ret.skill) && (damage > 0)) { //没有火眼
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 6.5)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Bowman_Skill_260(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 6)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Bowman_Skill_850(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 8)) {
                    damage = 1;
                    return damage;
                }
            }
        } else if (c.getJob() == 400 || c.getJob() == 410 || c.getJob() == 411 || c.getJob() == 412 || c.getJob() == 420 || c.getJob() == 421 || c.getJob() == 422) {//飞侠技能
            if (GameConstants.Thief_Skill_180(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 11)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Thief_Skill_250(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 14)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Thief_Skill_500(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 18)) {
                    damage = 1;
                    return damage;
                }
            } else if (ret.skill == 4221001) { // 暗杀技能
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 25)) {
//                    damage = 1;
//                    return damage;
                }
            }
        } else if (c.getJob() == 500 || c.getJob() == 510 || c.getJob() == 511 || c.getJob() == 512 || c.getJob() == 520 || c.getJob() == 521 || c.getJob() == 522) {//海盗技能
            if (GameConstants.Pirate_Skill_290(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 8)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Pirate_Skill_420(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 9.3)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Pirate_Skill_700(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 13)) {
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Pirate_Skill_810(ret.skill)) { // 潜龙出渊和  重量炮击
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 13.2)) {
//                    damage = 1;
//                    return damage;
                }
            } else if (GameConstants.Pirate_Skill_1200(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 18)) {
                    damage = 1;
                    return damage;
                }
            }
        } else if (c.getJob() == 1000 || c.getJob() == 1100 || c.getJob() == 1110 || c.getJob() == 1111) {//魂骑士技能
            if (GameConstants.Ghost_Knight_Skill_320(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 8.5)) {
                    //   c.dropMessage(1, "[魂骑士技能攻击力检测+A]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 7) && ret.skill == 0) {
                //  c.dropMessage(1, "[魂骑士技能攻击力检测+D]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1200 || c.getJob() == 1210 || c.getJob() == 1211) {//炎术士技能
            if (GameConstants.Fire_Knight_Skill_140(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 13)) {
//                                c.dropMessage(1, "[炎术士技能攻击力检测+A]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Fire_Knight_Skill_500(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 8)) {
                    //   c.dropMessage(1, "[炎术士技能攻击力检测+B]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 7) && ret.skill == 0) {
                // c.dropMessage(1, "[炎术士技能攻击力检测+D]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1300 || c.getJob() == 1310 || c.getJob() == 1311) {//风灵使者技能
            if (GameConstants.Wind_Knight_Skill_160(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 8)) {
                    // c.dropMessage(1, "[风灵使者技能攻击力检测+A]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Wind_Knight_Skill_550(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 11)) {
                    //   c.dropMessage(1, "[风灵使者技能攻击力检测+B]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 7) && ret.skill == 0) {
                // c.dropMessage(1, "[风灵使者技能攻击力检测+D]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                damage = 1;
                return damage;
            }
        } else if (c.getJob() == 1400 || c.getJob() == 1410 || c.getJob() == 1411) {//夜行者技能
            if (GameConstants.Night_Knight_Skill_220(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 9)) {
                    //c.dropMessage(1, "[夜行者技能攻击力检测+A]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 7) && ret.skill == 0) {
                //  c.dropMessage(1, "[夜行者技能攻击力检测+D]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                damage = 1;
                return damage;
            }

        } else if (c.getJob() == 1500 || c.getJob() == 1510 || c.getJob() == 1511) {//奇袭者技能
            if (GameConstants.Thief_Skill_270(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 7.7)) {
                    //    player.dropMessage(1,"[奇袭者技能攻击力检测+A]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Thief_Skill_420(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 10.2)) {
                    //c.dropMessage(1, "[奇袭者技能攻击力检测+B]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if (GameConstants.Thief_Skill_650(ret.skill)) {
                if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 14)) {
                    // c.dropMessage(1, "[奇袭者技能攻击力检测+C]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                    damage = 1;
                    return damage;
                }
            } else if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 7) && ret.skill == 0) {
                // c.dropMessage(1, "[奇袭者技能攻击力检测+D]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                damage = 1;
                return damage;
            }

        } else if (ret.skill == 4211006) {
            if ((c.getStat().getCurrentMaxBaseDamage() <= damage / 13)) {
                //  c.dropMessage(1, "[技能攻击力检测+D]\r\n非法使用外挂或者修改WZ\r\n导致:攻击力过高.\r\n攻击力无效！\r\n请勿再次使用后果自负！");
                damage = 1;
                return damage;
            }
        }
        return damage;
    }
}
