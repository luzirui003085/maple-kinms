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
 * @author Matze
 */
public enum MapleInventoryType {

    /**
     *
     */
    UNDEFINED(0),

    /**
     *
     */
    EQUIP(1),

    /**
     *
     */
    USE(2),

    /**
     *
     */
    SETUP(3),

    /**
     *
     */
    ETC(4),

    /**
     *
     */
    CASH(5),

    /**
     *
     */
    EQUIPPED(-1);
    final byte type;

    private MapleInventoryType(int type) {
        this.type = (byte) type;
    }

    /**
     *
     * @return
     */
    public byte getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public short getBitfieldEncoding() {
        return (short) (2 << type);
    }

    /**
     *
     * @param type
     * @return
     */
    public static MapleInventoryType getByType(byte type) {
        for (MapleInventoryType l : MapleInventoryType.values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    /**
     *
     * @param name
     * @return
     */
    public static MapleInventoryType getByWZName(String name) {
        switch (name) {
            case "Install":
                return SETUP;
            case "Consume":
                return USE;
            case "Etc":
                return ETC;
            case "Cash":
                return CASH;
            case "Pet":
                return CASH;
            default:
                break;
        }
        return UNDEFINED;
    }
}
