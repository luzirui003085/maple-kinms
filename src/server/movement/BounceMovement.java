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
import tools.data.output.LittleEndianWriter;

/**
 *
 * @author zjj
 */
public class BounceMovement extends AbstractLifeMovement {

    private int unk;
    private int fh;

    /**
     *
     * @param type
     * @param position
     * @param duration
     * @param newstate
     */
    public BounceMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    /**
     *
     * @return
     */
    public int getUnk() {
        return unk;
    }

    /**
     *
     * @param unk
     */
    public void setUnk(int unk) {
        this.unk = unk;
    }

    /**
     *
     * @return
     */
    public int getFH() {
        return fh;
    }

    /**
     *
     * @param fh
     */
    public void setFH(int fh) {
        this.fh = fh;
    }

    /**
     *
     * @param lew
     */
    @Override
    public void serialize(LittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writeShort(getUnk());
        lew.writeShort(getFH());
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
