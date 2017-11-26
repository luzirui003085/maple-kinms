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
package client.status;

import java.io.Serializable;

/**
 *
 * @author zjj
 */
public enum MonsterStatus implements Serializable {

    
    /*WATK(0x1),
    WDEF(0x2),
    MATK(0x4),
    MDEF(0x8),
    ACC(0x10),
    AVOID(0x20),
    SPEED(0x40),
    STUN(0x80), //this is possibly only the bowman stun
    FREEZE(0x100),
    POISON(0x200),
    SEAL(0x400),
    TAUNT(0x800),
    WEAPON_ATTACK_UP(0x1000),
    WEAPON_DEFENSE_UP(0x2000),
    MAGIC_ATTACK_UP(0x4000),
    MAGIC_DEFENSE_UP(0x8000),
    DOOM(0x10000),
    SHADOW_WEB(0x20000),
    WEAPON_IMMUNITY(0x40000),
    MAGIC_IMMUNITY(0x80000),
    NINJA_AMBUSH(0x400000),
    HYPNOTIZED(0x10000000),
    VENOMOUS_WEAPON(0x1000000L),
    DARKNESS(0x2000000L),
    EMPTY(0x8000000L),
    HYPNOTIZE(0x10000000L),
    WEAPON_DAMAGE_REFLECT(0x20000000L),
    MAGIC_DAMAGE_REFLECT(0x40000000L),
    SUMMON(0x80000000L) //all summon bag mobs have.*/

    /**
     *
     */

    NEUTRALISE(0x02), // first int on v.87 or else it won't work.

    /**
     *
     */

    WATK(0x100000000L),

    /**
     *
     */
    WDEF(0x200000000L),

    /**
     *
     */
    MATK(0x400000000L),

    /**
     *
     */
    MDEF(0x800000000L),

    /**
     *
     */
    ACC(0x1000000000L),

    /**
     *
     */
    AVOID(0x2000000000L),

    /**
     *
     */
    SPEED(0x4000000000L),

    /**
     *
     */
    STUN(0x8000000000L),

    /**
     *
     */
    FREEZE(0x10000000000L), // 凍結

    /**
     *
     */
    POISON(0x20000000000L),

    /**
     *
     */
    SEAL(0x40000000000L),

    /**
     *
     */
    SHOWDOWN(0x80000000000L),

    /**
     *
     */
    WEAPON_ATTACK_UP(0x100000000000L),

    /**
     *
     */
    WEAPON_DEFENSE_UP(0x200000000000L),

    /**
     *
     */
    MAGIC_ATTACK_UP(0x400000000000L),

    /**
     *
     */
    MAGIC_DEFENSE_UP(0x800000000000L),

    /**
     *
     */
    DOOM(0x1000000000000L),

    /**
     *
     */
    SHADOW_WEB(0x2000000000000L),

    /**
     *
     */
    WEAPON_IMMUNITY(0x4000000000000L),

    /**
     *
     */
    MAGIC_IMMUNITY(0x8000000000000L),

    /**
     *
     */
    DAMAGE_IMMUNITY(0x20000000000000L),

    /**
     *
     */
    NINJA_AMBUSH(0x40000000000000L),

    /**
     *
     */
    VENOMOUS_WEAPON(0x100000000000000L),

    /**
     *
     */
    DARKNESS(0x200000000000000L),

    /**
     *
     */
    EMPTY(0x800000000000000L),

    /**
     *
     */
    HYPNOTIZE(0x1000000000000000L),

    /**
     *
     */
    WEAPON_DAMAGE_REFLECT(0x2000000000000000L),

    /**
     *
     */
    MAGIC_DAMAGE_REFLECT(0x4000000000000000L),

    /**
     *
     */
    SUMMON(0x8000000000000000L) //all summon bag mobs have.*/
    ;
    static final long serialVersionUID = 0L;
    private final long i;
    private final boolean first;

    private MonsterStatus(long i) {
        this.i = i;
        first = false;
    }

    private MonsterStatus(int i, boolean first) {
        this.i = i;
        this.first = first;
    }

    /**
     *
     * @return
     */
    public boolean isFirst() {
        return first;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return this == SUMMON || this == EMPTY;
    }

    /**
     *
     * @return
     */
    public long getValue() {
        return i;
    }
}
