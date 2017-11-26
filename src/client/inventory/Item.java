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

import constants.GameConstants;
import java.io.Serializable;

/**
 *
 * @author zjj
 */
public class Item implements IItem, Serializable {

    private final int id;
    private short position;
    private short quantity;
    private byte flag;
    private long expiration = -1;
    private MaplePet pet = null;
    private int uniqueid = -1;
    private String owner = "";
    private String GameMaster_log = null;
    private String giftFrom = "";

    /**
     *
     */
    protected MapleRing ring = null;
    private byte itemLevel;

    /**
     *
     * @param id
     * @param position
     * @param quantity
     * @param flag
     * @param uniqueid
     */
    public Item(final int id, final short position, final short quantity, final byte flag, final int uniqueid) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        this.uniqueid = uniqueid;
    }

    /**
     *
     * @param id
     * @param position
     * @param quantity
     * @param flag
     */
    public Item(final int id, final short position, final short quantity, final byte flag) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
    }

    /**
     *
     * @param id
     * @param position
     * @param quantity
     */
    public Item(int id, byte position, short quantity) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.itemLevel = 1;
    }

    /**
     *
     * @return
     */
    @Override
    public IItem copy() {
        final Item ret = new Item(id, position, quantity, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    /**
     *
     * @param position
     */
    @Override
    public final void setPosition(final short position) {
        this.position = position;

        if (pet != null) {
            pet.setInventoryPosition(position);
        }
    }

    /**
     *
     * @param quantity
     */
    @Override
    public void setQuantity(final short quantity) {
        this.quantity = quantity;
    }

    /**
     *
     * @return
     */
    @Override
    public final int getItemId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public final short getPosition() {
        return position;
    }

    /**
     *
     * @return
     */
    @Override
    public final byte getFlag() {
        return flag;
    }

    /**
     *
     * @return
     */
    @Override
    public final short getQuantity() {
        return quantity;
    }

    /**
     *
     * @return
     */
    @Override
    public byte getType() {
        return 2; // An Item
    }

    /**
     *
     * @return
     */
    @Override
    public final String getOwner() {
        return owner;
    }

    /**
     *
     * @param owner
     */
    @Override
    public final void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     *
     * @param flag
     */
    @Override
    public final void setFlag(final byte flag) {
        this.flag = flag;
    }

    /**
     *
     * @return
     */
    @Override
    public final long getExpiration() {
        return expiration;
    }

    /**
     *
     * @param expire
     */
    @Override
    public final void setExpiration(final long expire) {
        this.expiration = expire;
    }

    /**
     *
     * @return
     */
    @Override
    public final String getGMLog() {
        return GameMaster_log;
    }

    /**
     *
     * @param GameMaster_log
     */
    @Override
    public void setGMLog(final String GameMaster_log) {
        this.GameMaster_log = GameMaster_log;
    }

    /**
     *
     * @return
     */
    @Override
    public final int getUniqueId() {
        return uniqueid;
    }

    /**
     *
     * @param id
     */
    @Override
    public final void setUniqueId(final int id) {
        this.uniqueid = id;
    }

    /**
     *
     * @return
     */
    @Override
    public final MaplePet getPet() {
        return pet;
    }

    /**
     *
     * @param pet
     */
    public final void setPet(final MaplePet pet) {
        this.pet = pet;
    }

    /**
     *
     * @param gf
     */
    @Override
    public void setGiftFrom(String gf) {
        this.giftFrom = gf;
    }

    /**
     *
     * @return
     */
    @Override
    public String getGiftFrom() {
        return giftFrom;
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

    @Override
    public int compareTo(IItem other) {
        if (Math.abs(position) < Math.abs(other.getPosition())) {
            return -1;
        } else if (Math.abs(position) == Math.abs(other.getPosition())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IItem)) {
            return false;
        }
        final IItem ite = (IItem) obj;
        return uniqueid == ite.getUniqueId() && id == ite.getItemId() && quantity == ite.getQuantity() && Math.abs(position) == Math.abs(ite.getPosition());
    }

    @Override
    public String toString() {
        return "Item: " + id + " quantity: " + quantity;
    }

    /**
     *
     * @return
     */
    @Override
    public MapleRing getRing() {
        if (!GameConstants.isEffectRing(id) || getUniqueId() <= 0) {
            return null;
        }
        if (ring == null) {
            ring = MapleRing.loadFromDb(getUniqueId(), position < 0);
        }
        return ring;
    }

    /**
     *
     * @param ring
     */
    public void setRing(MapleRing ring) {
        this.ring = ring;
    }
}
