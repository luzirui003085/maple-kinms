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
public interface IItem extends Comparable<IItem> {

    /**
     *
     * @return
     */
    byte getType();

    /**
     *
     * @return
     */
    short getPosition();

    /**
     *
     * @return
     */
    byte getFlag();

    /**
     *
     * @return
     */
    short getQuantity();

    /**
     *
     * @return
     */
    String getOwner();

    /**
     *
     * @return
     */
    String getGMLog();

    /**
     *
     * @return
     */
    int getItemId();

    /**
     *
     * @return
     */
    MaplePet getPet();

    /**
     *
     * @return
     */
    int getUniqueId();

    /**
     *
     * @return
     */
    IItem copy();

    /**
     *
     * @return
     */
    long getExpiration();

    /**
     *
     * @param flag
     */
    void setFlag(byte flag);

    /**
     *
     * @param id
     */
    void setUniqueId(int id);

    /**
     *
     * @param position
     */
    void setPosition(short position);

    /**
     *
     * @param expire
     */
    void setExpiration(long expire);

    /**
     *
     * @param owner
     */
    void setOwner(String owner);

    /**
     *
     * @param GameMaster_log
     */
    void setGMLog(String GameMaster_log);

    /**
     *
     * @param quantity
     */
    void setQuantity(short quantity);

    /**
     *
     * @param gf
     */
    void setGiftFrom(String gf);
    
    /**
     *
     * @param j
     */
    void setEquipLevel(byte j);

    /**
     *
     * @return
     */
    byte getEquipLevel();
    
    /**
     *
     * @return
     */
    String getGiftFrom();

    /**
     *
     * @return
     */
    MapleRing getRing();
}
