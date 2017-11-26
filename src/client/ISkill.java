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
package client;

import server.MapleStatEffect;
import server.life.Element;

/**
 *
 * @author zjj
 */
public interface ISkill {

    /**
     *
     * @return
     */
    int getId();

    /**
     *
     * @param level
     * @return
     */
    MapleStatEffect getEffect(int level);

    /**
     *
     * @return
     */
    byte getMaxLevel();

    /**
     *
     * @return
     */
    int getAnimationTime();

    /**
     *
     * @param job
     * @return
     */
    public boolean canBeLearnedBy(int job);

    /**
     *
     * @return
     */
    public boolean isFourthJob();

    /**
     *
     * @return
     */
    public boolean getAction();

    /**
     *
     * @return
     */
    public boolean isTimeLimited();

    /**
     *
     * @return
     */
    public int getMasterLevel();

    /**
     *
     * @return
     */
    public Element getElement();

    /**
     *
     * @return
     */
    public boolean isBeginnerSkill();

    /**
     *
     * @return
     */
    public boolean hasRequiredSkill();

    /**
     *
     * @return
     */
    public boolean isInvisible();

    /**
     *
     * @return
     */
    public boolean isChargeSkill();

    /**
     *
     * @return
     */
    public int getRequiredSkillLevel();

    /**
     *
     * @return
     */
    public int getRequiredSkillId();

    /**
     *
     * @return
     */
    public String getName();
}
