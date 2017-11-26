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

import java.io.Serializable;

/**
 *
 * @author zjj
 */
public class SkillMacro implements Serializable {

    private static final long serialVersionUID = -63413738569L;
    private int macroId;
    private int skill1;
    private int skill2;
    private int skill3;
    private String name;
    private int shout;
    private int position;

    /**
     *
     * @param skill1
     * @param skill2
     * @param skill3
     * @param name
     * @param shout
     * @param position
     */
    public SkillMacro(int skill1, int skill2, int skill3, String name, int shout, int position) {
        this.skill1 = skill1;
        this.skill2 = skill2;
        this.skill3 = skill3;
        this.name = name;
        this.shout = shout;
        this.position = position;
    }

    /**
     *
     * @return
     */
    public int getMacroId() {
        return macroId;
    }

    /**
     *
     * @return
     */
    public int getSkill1() {
        return skill1;
    }

    /**
     *
     * @return
     */
    public int getSkill2() {
        return skill2;
    }

    /**
     *
     * @return
     */
    public int getSkill3() {
        return skill3;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public int getShout() {
        return shout;
    }

    /**
     *
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     *
     * @param macroId
     */
    public void setMacroId(int macroId) {
        this.macroId = macroId;
    }

    /**
     *
     * @param skill1
     */
    public void setSkill1(int skill1) {
        this.skill1 = skill1;
    }

    /**
     *
     * @param skill2
     */
    public void setSkill2(int skill2) {
        this.skill2 = skill2;
    }

    /**
     *
     * @param skill3
     */
    public void setSkill3(int skill3) {
        this.skill3 = skill3;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param shout
     */
    public void setShout(int shout) {
        this.shout = shout;
    }

    /**
     *
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }
}
