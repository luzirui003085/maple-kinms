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
package server.movement;

import java.awt.Point;

/**
 *
 * @author zjj
 */
public abstract class AbstractLifeMovement implements LifeMovement {

    private Point position;
    private int duration;
    private int newstate;
    private int type;

    /**
     *
     * @param type
     * @param position
     * @param duration
     * @param newstate
     */
    public AbstractLifeMovement(int type, Point position, int duration, int newstate) {
        super();
        this.type = type;
        this.position = position;
        this.duration = duration;
        this.newstate = newstate;
    }

    /**
     *
     * @return
     */
    @Override
    public int getType() {
        return this.type;
    }

    /**
     *
     * @return
     */
    @Override
    public int getDuration() {
        return duration;
    }

    /**
     *
     * @return
     */
    @Override
    public int getNewstate() {
        return newstate;
    }

    /**
     *
     * @return
     */
    @Override
    public Point getPosition() {
        return position;
    }
}
