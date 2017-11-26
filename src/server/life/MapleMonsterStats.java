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
package server.life;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.Pair;

/**
 *
 * @author zjj
 */
public class MapleMonsterStats {

    private byte cp, selfDestruction_action, tagColor, tagBgColor, rareItemDropLevel, HPDisplayType;
    private short level, PhysicalDefense, MagicDefense, eva;
    private long hp;
    private int exp, mp, removeAfter, buffToGive, fixedDamage, selfDestruction_hp, dropItemPeriod, point;
    private boolean boss, undead, ffaLoot, firstAttack, isExplosiveReward, mobile, fly, onlyNormalAttack, friendly, noDoom;
    private String name;
    private Map<Element, ElementalEffectiveness> resistance = new EnumMap<Element, ElementalEffectiveness>(Element.class);
    private List<Integer> revives = new ArrayList<>();
    private List<Pair<Integer, Integer>> skills = new ArrayList<>();
    private BanishInfo banish;

    /**
     *
     * @return
     */
    public int getExp() {
        return exp;
    }

    /**
     *
     * @param exp
     */
    public void setExp(int exp) {
        this.exp = exp;
    }

    /**
     *
     * @return
     */
    public long getHp() {
        return hp;
    }

    /**
     *
     * @param hp
     */
    public void setHp(long hp) {
        this.hp = hp;//(hp * 3L / 2L);
    }

    /**
     *
     * @return
     */
    public int getMp() {
        return mp;
    }

    /**
     *
     * @param mp
     */
    public void setMp(int mp) {
        this.mp = mp;
    }

    /**
     *
     * @return
     */
    public short getLevel() {
        return level;
    }

    /**
     *
     * @param level
     */
    public void setLevel(short level) {
        this.level = level;
    }

    /**
     *
     * @param selfDestruction_action
     */
    public void setSelfD(byte selfDestruction_action) {
        this.selfDestruction_action = selfDestruction_action;
    }

    /**
     *
     * @return
     */
    public byte getSelfD() {
        return selfDestruction_action;
    }

    /**
     *
     * @param selfDestruction_hp
     */
    public void setSelfDHP(int selfDestruction_hp) {
        this.selfDestruction_hp = selfDestruction_hp;
    }

    /**
     *
     * @return
     */
    public int getSelfDHp() {
        return selfDestruction_hp;
    }

    /**
     *
     * @param damage
     */
    public void setFixedDamage(int damage) {
        this.fixedDamage = damage;
    }

    /**
     *
     * @return
     */
    public int getFixedDamage() {
        return fixedDamage;
    }

    /**
     *
     * @param PhysicalDefense
     */
    public void setPhysicalDefense(final short PhysicalDefense) {
        this.PhysicalDefense = PhysicalDefense;
    }

    /**
     *
     * @return
     */
    public short getPhysicalDefense() {
        return PhysicalDefense;
    }

    /**
     *
     * @param MagicDefense
     */
    public final void setMagicDefense(final short MagicDefense) {
        this.MagicDefense = MagicDefense;
    }

    /**
     *
     * @return
     */
    public final short getMagicDefense() {
        return MagicDefense;
    }

    /**
     *
     * @param eva
     */
    public final void setEva(final short eva) {
        this.eva = eva;
    }

    /**
     *
     * @return
     */
    public final short getEva() {
        return eva;
    }

    /**
     *
     * @param onlyNormalAttack
     */
    public void setOnlyNormalAttack(boolean onlyNormalAttack) {
        this.onlyNormalAttack = onlyNormalAttack;
    }

    /**
     *
     * @return
     */
    public boolean getOnlyNoramlAttack() {
        return onlyNormalAttack;
    }

    /**
     *
     * @return
     */
    public BanishInfo getBanishInfo() {
        return banish;
    }

    /**
     *
     * @param banish
     */
    public void setBanishInfo(BanishInfo banish) {
        this.banish = banish;
    }

    /**
     *
     * @return
     */
    public int getRemoveAfter() {
        return removeAfter;
    }

    /**
     *
     * @param removeAfter
     */
    public void setRemoveAfter(int removeAfter) {
        this.removeAfter = removeAfter;
    }

    /**
     *
     * @return
     */
    public byte getrareItemDropLevel() {
        return rareItemDropLevel;
    }

    /**
     *
     * @param rareItemDropLevel
     */
    public void setrareItemDropLevel(byte rareItemDropLevel) {
        this.rareItemDropLevel = rareItemDropLevel;
    }

    /**
     *
     * @param boss
     */
    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    /**
     *
     * @return
     */
    public boolean isBoss() {
        return boss;
    }

    /**
     *
     * @param ffaLoot
     */
    public void setFfaLoot(boolean ffaLoot) {
        this.ffaLoot = ffaLoot;
    }

    /**
     *
     * @return
     */
    public boolean isFfaLoot() {
        return ffaLoot;
    }

    /**
     *
     * @param isExplosiveReward
     */
    public void setExplosiveReward(boolean isExplosiveReward) {
        this.isExplosiveReward = isExplosiveReward;
    }

    /**
     *
     * @return
     */
    public boolean isExplosiveReward() {
        return isExplosiveReward;
    }

    /**
     *
     * @param mobile
     */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    /**
     *
     * @return
     */
    public boolean getMobile() {
        return mobile;
    }

    /**
     *
     * @param fly
     */
    public void setFly(boolean fly) {
        this.fly = fly;
    }

    /**
     *
     * @return
     */
    public boolean getFly() {
        return fly;
    }

    /**
     *
     * @return
     */
    public List<Integer> getRevives() {
        return revives;
    }

    /**
     *
     * @param revives
     */
    public void setRevives(List<Integer> revives) {
        this.revives = revives;
    }

    /**
     *
     * @param undead
     */
    public void setUndead(boolean undead) {
        this.undead = undead;
    }

    /**
     *
     * @return
     */
    public boolean getUndead() {
        return undead;
    }

    /**
     *
     * @param e
     * @param ee
     */
    public void setEffectiveness(Element e, ElementalEffectiveness ee) {
        resistance.put(e, ee);
    }

    /**
     *
     * @param e
     */
    public void removeEffectiveness(Element e) {
        resistance.remove(e);
    }

    /**
     *
     * @param e
     * @return
     */
    public ElementalEffectiveness getEffectiveness(Element e) {
        ElementalEffectiveness elementalEffectiveness = resistance.get(e);
        if (elementalEffectiveness == null) {
            return ElementalEffectiveness.NORMAL;
        } else {
            return elementalEffectiveness;
        }
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
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public byte getTagColor() {
        return tagColor;
    }

    /**
     *
     * @param tagColor
     */
    public void setTagColor(int tagColor) {
        this.tagColor = (byte) tagColor;
    }

    /**
     *
     * @return
     */
    public byte getTagBgColor() {
        return tagBgColor;
    }

    /**
     *
     * @param tagBgColor
     */
    public void setTagBgColor(int tagBgColor) {
        this.tagBgColor = (byte) tagBgColor;
    }

    /**
     *
     * @param skill_
     */
    public void setSkills(List<Pair<Integer, Integer>> skill_) {
        for (Pair<Integer, Integer> skill : skill_) {
            skills.add(skill);
        }
    }

    /**
     *
     * @return
     */
    public List<Pair<Integer, Integer>> getSkills() {
        return Collections.unmodifiableList(this.skills);
    }

    /**
     *
     * @return
     */
    public byte getNoSkills() {
        return (byte) skills.size();
    }

    /**
     *
     * @param skillId
     * @param level
     * @return
     */
    public boolean hasSkill(int skillId, int level) {
        for (Pair<Integer, Integer> skill : skills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param firstAttack
     */
    public void setFirstAttack(boolean firstAttack) {
        this.firstAttack = firstAttack;
    }

    /**
     *
     * @return
     */
    public boolean isFirstAttack() {
        return firstAttack;
    }

    /**
     *
     * @param cp
     */
    public void setCP(byte cp) {
        this.cp = cp;
    }

    /**
     *
     * @return
     */
    public byte getCP() {
        return cp;
    }

    /**
     *
     * @param cp
     */
    public void setPoint(int cp) {
        this.point = cp;
    }

    /**
     *
     * @return
     */
    public int getPoint() {
        return point;
    }

    /**
     *
     * @param friendly
     */
    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    /**
     *
     * @return
     */
    public boolean isFriendly() {
        return friendly;
    }

    /**
     *
     * @param doom
     */
    public void setNoDoom(boolean doom) {
        this.noDoom = doom;
    }

    /**
     *
     * @return
     */
    public boolean isNoDoom() {
        return noDoom;
    }

    /**
     *
     * @param buff
     */
    public void setBuffToGive(int buff) {
        this.buffToGive = buff;
    }

    /**
     *
     * @return
     */
    public int getBuffToGive() {
        return buffToGive;
    }

    /**
     *
     * @return
     */
    public byte getHPDisplayType() {
        return HPDisplayType;
    }

    /**
     *
     * @param HPDisplayType
     */
    public void setHPDisplayType(byte HPDisplayType) {
        this.HPDisplayType = HPDisplayType;
    }

    /**
     *
     * @return
     */
    public int getDropItemPeriod() {
        return dropItemPeriod;
    }

    /**
     *
     * @param d
     */
    public void setDropItemPeriod(int d) {
        this.dropItemPeriod = d;
    }
}
