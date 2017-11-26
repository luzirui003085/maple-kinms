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
package server.life;

/**
 *
 * @author zjj
 */
public class OverrideMonsterStats {

    /**
     *
     */
    public long hp;
    public int exp,

    /**
     *
     */
    mp;

    /**
     *
     */
    public OverrideMonsterStats() {
        hp = 0;
        exp = 0;
        mp = 0;
    }

    /**
     *
     * @param hp
     * @param mp
     * @param exp
     * @param change
     */
    public OverrideMonsterStats(long hp, int mp, int exp, boolean change) {
        this.hp = /*change ? (hp * 3L / 2L) : */ hp;
        this.mp = mp;
        this.exp = exp;
    }

    /**
     *
     * @param hp
     * @param mp
     * @param exp
     */
    public OverrideMonsterStats(long hp, int mp, int exp) {
        this(hp, mp, exp, true);
    }

    /**
     *
     * @return
     */
    public int getExp() {
        return exp;
    }

    /**
     *
     * @param exp
     */
    public void setOExp(int exp) {
        this.exp = exp;
    }

    /**
     *
     * @return
     */
    public long getHp() {
        return hp;
    }

    /**
     *
     * @param hp
     */
    public void setOHp(long hp) {
        this.hp = hp;
    }

    /**
     *
     * @return
     */
    public int getMp() {
        return mp;
    }

    /**
     *
     * @param mp
     */
    public void setOMp(int mp) {
        this.mp = mp;
    }
}
