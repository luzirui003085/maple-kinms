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
package server.maps;

import java.awt.Point;

/**
 *
 * @author zjj
 */
public abstract class AbstractMapleMapObject implements MapleMapObject {

    private Point position = new Point();
    private int objectId;

    /**
     *
     * @return
     */
    @Override
    public abstract MapleMapObjectType getType();

    /**
     *
     * @return
     */
    public Point getTruePosition() {
        return this.position;
    }

    /**
     *
     * @return
     */
    @Override
    public Point getPosition() {
        return new Point(position);
    }

    /**
     *
     * @param position
     */
    @Override
    public void setPosition(Point position) {
        this.position.x = position.x;
        this.position.y = position.y;
    }

    /**
     *
     * @return
     */
    @Override
    public int getObjectId() {
        return objectId;
    }

    /**
     *
     * @param id
     */
    @Override
    public void setObjectId(int id) {
        this.objectId = id;
    }
}
