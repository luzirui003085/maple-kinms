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

/**
 *
 * @author zjj
 */
public interface IEquip extends IItem {

    /**
     *
     */
    public static enum ScrollResult {

        /**
         *
         */
        SUCCESS, 

        /**
         *
         */
        FAIL, 

        /**
         *
         */
        CURSE
    }

    /**
     *
     */
    public static final int ARMOR_RATIO = 350000;

    /**
     *
     */
    public static final int WEAPON_RATIO = 700000;

    /**
     *
     * @return
     */
    byte getUpgradeSlots();

    /**
     *
     * @return
     */
    byte getLevel();

    /**
     *
     * @return
     */
    public byte getViciousHammer();

    /**
     *
     * @return
     */
    public int getItemEXP();

    /**
     *
     * @return
     */
    public int getExpPercentage();

    /**
     *
     * @return
     */
    @Override
    public byte getEquipLevel();

    /**
     *
     * @return
     */
    public int getEquipLevels();
    
    /**
     *
     * @return
     */
    public int getEquipExp();

    /**
     *
     * @return
     */
    public int getEquipExpForLevel();

    /**
     *
     * @return
     */
    public int getBaseLevel();

    /**
     *
     * @return
     */
    public short getStr();

    /**
     *
     * @return
     */
    public short getDex();

    /**
     *
     * @return
     */
    public short getInt();

    /**
     *
     * @return
     */
    public short getLuk();

    /**
     *
     * @return
     */
    public short getHp();

    /**
     *
     * @return
     */
    public short getMp();

    /**
     *
     * @return
     */
    public short getWatk();

    /**
     *
     * @return
     */
    public short getMatk();

    /**
     *
     * @return
     */
    public short getWdef();

    /**
     *
     * @return
     */
    public short getMdef();

    /**
     *
     * @return
     */
    public short getAcc();

    /**
     *
     * @return
     */
    public short getAvoid();

    /**
     *
     * @return
     */
    public short getHands();

    /**
     *
     * @return
     */
    public short getSpeed();

    /**
     *
     * @return
     */
    public short getJump();

    /**
     *
     * @return
     */
    public int getDurability();

    /**
     *
     * @return
     */
    public byte getEnhance();

    /**
     *
     * @return
     */
    public byte getState();

    /**
     *
     * @return
     */
    public short getPotential1();

    /**
     *
     * @return
     */
    public short getPotential2();

    /**
     *
     * @return
     */
    public short getPotential3();

    /**
     *
     * @return
     */
    public short getHpR();

    /**
     *
     * @return
     */
    public short getMpR();
}
