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
package client.inventory;

import client.MapleClient;
import constants.GameConstants;
import java.io.Serializable;
import java.util.List;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author zjj
 */
public class Equip extends Item implements IEquip, Serializable {

    private byte upgradeSlots = 0, level = 0, vicioushammer = 0, enhance = 0;
    private short str = 0, dex = 0, _int = 0, luk = 0, hp = 0, mp = 0, watk = 0, matk = 0, wdef = 0, mdef = 0, acc = 0, avoid = 0, hands = 0, speed = 0, jump = 0, potential1 = 0, potential2 = 0, potential3 = 0, hpR = 0, mpR = 0;
    private int itemEXP = 0, durability = -1;
    private byte itemLevel;

    /**
     *
     * @param id
     * @param position
     */
    public Equip(int id, byte position) {
        super(id, position, (short) 1);
      //  this.ring = false;
    }

    /**
     *
     * @param id
     * @param position
     * @param flag
     */
    public Equip(int id, short position, byte flag) {
        super(id, position, (short) 1, flag);
    }

    /**
     *
     * @param id
     * @param position
     * @param uniqueid
     * @param flag
     */
    public Equip(int id, short position, int uniqueid, byte flag) {
        super(id, position, (short) 1, flag, uniqueid);
    }

    /**
     *
     * @return
     */
    @Override
    public IItem copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUniqueId(), getFlag());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.enhance = enhance;
        ret.upgradeSlots = upgradeSlots;
        ret.level = level;
        ret.itemEXP = itemEXP;
        ret.durability = durability;
        ret.vicioushammer = vicioushammer;
        ret.potential1 = potential1;
        ret.potential2 = potential2;
        ret.potential3 = potential3;
        ret.hpR = hpR;
        ret.mpR = mpR;
        ret.itemLevel = this.itemLevel;
        ret.setGiftFrom(getGiftFrom());
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        return ret;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getType() {
        return 1;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    /**
     *
     * @return
     */
    @Override
    public short getStr() {
        return str;
    }

    /**
     *
     * @return
     */
    @Override
    public short getDex() {
        return dex;
    }

    /**
     *
     * @return
     */
    @Override
    public short getInt() {
        return _int;
    }

    /**
     *
     * @return
     */
    @Override
    public short getLuk() {
        return luk;
    }

    /**
     *
     * @return
     */
    @Override
    public short getHp() {
        return hp;
    }

    /**
     *
     * @return
     */
    @Override
    public short getMp() {
        return mp;
    }

    /**
     *
     * @return
     */
    @Override
    public short getWatk() {
        return watk;
    }

    /**
     *
     * @return
     */
    @Override
    public short getMatk() {
        return matk;
    }

    /**
     *
     * @return
     */
    @Override
    public short getWdef() {
        return wdef;
    }

    /**
     *
     * @return
     */
    @Override
    public short getMdef() {
        return mdef;
    }

    /**
     *
     * @return
     */
    @Override
    public short getAcc() {
        return acc;
    }

    /**
     *
     * @return
     */
    @Override
    public short getAvoid() {
        return avoid;
    }

    /**
     *
     * @return
     */
    @Override
    public short getHands() {
        return hands;
    }

    /**
     *
     * @return
     */
    @Override
    public short getSpeed() {
        return speed;
    }

    /**
     *
     * @return
     */
    @Override
    public short getJump() {
        return jump;
    }

    /**
     *
     * @param str
     */
    public void setStr(short str) {
        if (str < 0) {
            str = 0;
        }
        this.str = str;
    }

    /**
     *
     * @param dex
     */
    public void setDex(short dex) {
        if (dex < 0) {
            dex = 0;
        }
        this.dex = dex;
    }

    /**
     *
     * @param _int
     */
    public void setInt(short _int) {
        if (_int < 0) {
            _int = 0;
        }
        this._int = _int;
    }

    /**
     *
     * @param luk
     */
    public void setLuk(short luk) {
        if (luk < 0) {
            luk = 0;
        }
        this.luk = luk;
    }

    /**
     *
     * @param hp
     */
    public void setHp(short hp) {
        if (hp < 0) {
            hp = 0;
        }
        this.hp = hp;
    }

    /**
     *
     * @param mp
     */
    public void setMp(short mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    /**
     *
     * @param watk
     */
    public void setWatk(short watk) {
        if (watk < 0) {
            watk = 0;
        }
        this.watk = watk;
    }

    /**
     *
     * @param matk
     */
    public void setMatk(short matk) {
        if (matk < 0) {
            matk = 0;
        }
        this.matk = matk;
    }

    /**
     *
     * @param wdef
     */
    public void setWdef(short wdef) {
        if (wdef < 0) {
            wdef = 0;
        }
        this.wdef = wdef;
    }

    /**
     *
     * @param mdef
     */
    public void setMdef(short mdef) {
        if (mdef < 0) {
            mdef = 0;
        }
        this.mdef = mdef;
    }

    /**
     *
     * @param acc
     */
    public void setAcc(short acc) {
        if (acc < 0) {
            acc = 0;
        }
        this.acc = acc;
    }

    /**
     *
     * @param avoid
     */
    public void setAvoid(short avoid) {
        if (avoid < 0) {
            avoid = 0;
        }
        this.avoid = avoid;
    }

    /**
     *
     * @param hands
     */
    public void setHands(short hands) {
        if (hands < 0) {
            hands = 0;
        }
        this.hands = hands;
    }

    /**
     *
     * @param speed
     */
    public void setSpeed(short speed) {
        if (speed < 0) {
            speed = 0;
        }
        this.speed = speed;
    }

    /**
     *
     * @param jump
     */
    public void setJump(short jump) {
        if (jump < 0) {
            jump = 0;
        }
        this.jump = jump;
    }

    /**
     *
     * @param upgradeSlots
     */
    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getLevel() {
        return level;
    }

    /**
     *
     * @param level
     */
    public void setLevel(byte level) {
        this.level = level;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getViciousHammer() {
        return vicioushammer;
    }

    /**
     *
     * @param ham
     */
    public void setViciousHammer(byte ham) {
        vicioushammer = ham;
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemEXP() {
        return itemEXP;
    }

    /**
     *
     * @param itemEXP
     */
    public void setItemEXP(int itemEXP) {
        if (itemEXP < 0) {
            itemEXP = 0;
        }
        this.itemEXP = itemEXP;
    }

    /**
     *
     * @return
     */
    @Override
    public int getEquipExp() {
        if (itemEXP <= 0) {
            return 0;
        }
        //aproximate value
        if (GameConstants.isWeapon(getItemId())) {
            return itemEXP / IEquip.WEAPON_RATIO;
        } else {
            return itemEXP / IEquip.ARMOR_RATIO;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int getEquipExpForLevel() {
        if (getEquipExp() <= 0) {
            return 0;
        }
        int expz = getEquipExp();
        for (int i = getBaseLevel(); i <= GameConstants.getMaxLevel(getItemId()); i++) {
            if (expz >= GameConstants.getExpForLevel(i, getItemId())) {
                expz -= GameConstants.getExpForLevel(i, getItemId());
            } else { //for 0, dont continue;
                break;
            }
        }
        return expz;
    }
    
    /**
     *
     * @return
     */
    @Override
    public int getExpPercentage() {
        return this.itemEXP;
    }
    /*@Override
    public int getExpPercentage() {
        if (getEquipLevels() < getBaseLevel() || getEquipLevels() > GameConstants.getMaxLevel(getItemId()) || GameConstants.getExpForLevel(getEquipLevels(), getItemId()) <= 0) {
            return 0;
        }
        return getEquipExpForLevel() * 100 / GameConstants.getExpForLevel(getEquipLevels(), getItemId());
    }*/

    /**
     *
     * @return
     */
    @Override
    public int getEquipLevels() {
        if (GameConstants.getMaxLevel(getItemId()) <= 0) {
            return 0;
        } else if (getEquipExp() <= 0) {
            return getBaseLevel();
        }
        int levelz = getBaseLevel();
        int expz = getEquipExp();
        for (int i = levelz; (GameConstants.getStatFromWeapon(getItemId()) == null ? (i <= GameConstants.getMaxLevel(getItemId())) : (i < GameConstants.getMaxLevel(getItemId()))); i++) {
            if (expz >= GameConstants.getExpForLevel(i, getItemId())) {
                levelz++;
                expz -= GameConstants.getExpForLevel(i, getItemId());
            } else { //for 0, dont continue;
                break;
            }
        }
        return levelz;
    }

    /**
     *
     * @return
     */
    @Override
    public int getBaseLevel() {
        return (GameConstants.getStatFromWeapon(getItemId()) == null ? 1 : 0);
    }

    /**
     *
     * @param quantity
     */
    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    /**
     *
     * @return
     */
    @Override
    public int getDurability() {
        return durability;
    }

    /**
     *
     * @param dur
     */
    public void setDurability(final int dur) {
        this.durability = dur;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getEnhance() {
        return enhance;
    }

    /**
     *
     * @param en
     */
    public void setEnhance(final byte en) {
        this.enhance = en;
    }

    /**
     *
     * @return
     */
    @Override
    public short getPotential1() {
        return potential1;
    }

    /**
     *
     * @param en
     */
    public void setPotential1(final short en) {
        this.potential1 = en;
    }

    /**
     *
     * @return
     */
    @Override
    public short getPotential2() {
        return potential2;
    }

    /**
     *
     * @param en
     */
    public void setPotential2(final short en) {
        this.potential2 = en;
    }

    /**
     *
     * @return
     */
    @Override
    public short getPotential3() {
        return potential3;
    }

    /**
     *
     * @param en
     */
    public void setPotential3(final short en) {
        this.potential3 = en;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getState() {
        final int pots = potential1 + potential2 + potential3;
        if (potential1 >= 30000 || potential2 >= 30000 || potential3 >= 30000) {
            return 7;
        } else if (potential1 >= 20000 || potential2 >= 20000 || potential3 >= 20000) {
            return 6;
        } else if (pots >= 1) {
            return 5;
        } else if (pots < 0) {
            return 1;
        }
        return 0;
    }

    /**
     *
     */
    public void resetPotential() { //equip first receive
        //0.04% chance unique, 4% chance epic, else rare
        final int rank = Randomizer.nextInt(100) < 4 ? (Randomizer.nextInt(100) < 4 ? -7 : -6) : -5;
        setPotential1((short) rank);
        setPotential2((short) (Randomizer.nextInt(10) == 1 ? rank : 0)); //1/10 chance of 3 line
        setPotential3((short) 0); //just set it theoretically
    }

    /**
     *
     */
    public void renewPotential() {
        //4% chance upgrade
        final int rank = Randomizer.nextInt(100) < 4 && getState() != 7 ? -(getState() + 1) : -(getState());
        setPotential1((short) rank);
        setPotential2((short) (getPotential3() > 0 ? rank : 0)); //1/10 chance of 3 line
        setPotential3((short) 0); //just set it theoretically
    }

    /**
     *
     * @return
     */
    @Override
    public short getHpR() {
        return hpR;
    }

    /**
     *
     * @param hp
     */
    public void setHpR(final short hp) {
        this.hpR = hp;
    }

    /**
     *
     * @return
     */
    @Override
    public short getMpR() {
        return mpR;
    }

    /**
     *
     * @param mp
     */
    public void setMpR(final short mp) {
        this.mpR = mp;
    }

    /**
     *
     */
    public void gainItemLevel() {
        this.itemLevel = (byte)(this.itemLevel + 1);
    }

    /**
     *
     * @param c
     * @param gain
     * @param timeless
     */
    public void gainItemExp(MapleClient c, int gain, boolean timeless) {
        this.itemEXP += gain;
        int expNeeded = 0;
        if (timeless) {
            expNeeded = ExpTable.getTimelessItemExpNeededForLevel(this.itemLevel + 1);
        } else {
            expNeeded = ExpTable.getReverseItemExpNeededForLevel(this.itemLevel + 1);
        }
        if (this.itemEXP >= expNeeded) {
         // gainItemLevel();
            gainItemLevel(c, timeless);
            //gainLevel();
            c.getSession().write(MaplePacketCreator.showItemLevelup());
        }
    }

    /**
     *
     * @param c
     * @param timeless
     */
    public void gainItemLevel(MapleClient c, boolean timeless) {
        List<Pair<String, Integer>> stats = MapleItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel, timeless);
        for (Pair<String, Integer> stat : stats) {
            switch (stat.getLeft()) {
                case "incDEX":
                    dex += stat.getRight();
                    break;
                case "incSTR":
                    str += stat.getRight();
                    break;
                case "incINT":
                    _int += stat.getRight();
                    break;
                case "incLUK":
                    luk += stat.getRight();
                    break;
                case "incMHP":
                    hp += stat.getRight();
                    break;
                case "incMMP":
                    mp += stat.getRight();
                    break;
                case "incPAD":
                    watk += stat.getRight();
                    break;
                case "incMAD":
                    matk += stat.getRight();
                    break;
                case "incPDD":
                    wdef += stat.getRight();
                    break;
                case "incMDD":
                    mdef += stat.getRight();
                    break;
                case "incEVA":
                    avoid += stat.getRight();
                    break;
                case "incACC":
                    acc += stat.getRight();
                    break;
                case "incSpeed":
                    speed += stat.getRight();
                    break;
                case "incJump":
                    jump += stat.getRight();
                    break;
                default:
                    break;
            }
        }
        this.itemLevel++;
        c.getPlayer().getClient().getSession().write(MaplePacketCreator.showEquipmentLevelUp());
        c.getPlayer().getClient().getSession().write(MaplePacketCreator.updateSpecialItemUse(this, getType()));
              c.getPlayer().getClient().getSession().write(MaplePacketCreator.getCharInfo(c.getPlayer()));
          /*  c.getPlayer().getMap().removePlayer(c.getPlayer());
            c.getPlayer().getMap().addPlayer(c.getPlayer());*/
        //c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeignEffect(c.getPlayer().getId(), 17));
       // c.getPlayer().forceUpdateItem(MapleInventoryType.EQUIPPED, this);
    }

    /**
     *
     * @param gf
     */
    @Override
    public void setEquipLevel(byte gf) {
        this.itemLevel = gf;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getEquipLevel() {
        return itemLevel;
    }
}
