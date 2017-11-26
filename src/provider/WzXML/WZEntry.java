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
package provider.WzXML;

import provider.MapleDataEntity;
import provider.MapleDataEntry;

/**
 *
 * @author zjj
 */
public class WZEntry implements MapleDataEntry {

    private String name;
    private int size;
    private int checksum;
    private int offset;
    private MapleDataEntity parent;

    /**
     *
     * @param name
     * @param size
     * @param checksum
     * @param parent
     */
    public WZEntry(String name, int size, int checksum, MapleDataEntity parent) {
        super();
        this.name = name;
        this.size = size;
        this.checksum = checksum;
        this.parent = parent;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    @Override
    public int getSize() {
        return size;
    }

    /**
     *
     * @return
     */
    @Override
    public int getChecksum() {
        return checksum;
    }

    /**
     *
     * @return
     */
    @Override
    public int getOffset() {
        return offset;
    }

    /**
     *
     * @return
     */
    @Override
    public MapleDataEntity getParent() {
        return parent;
    }
}
