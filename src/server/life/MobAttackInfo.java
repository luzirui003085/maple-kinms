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
public class MobAttackInfo {

    private boolean isDeadlyAttack;
    private int mpBurn, mpCon;
    private int diseaseSkill, diseaseLevel;

    /**
     *
     */
    public MobAttackInfo() {
    }

    /**
     *
     * @param isDeadlyAttack
     */
    public void setDeadlyAttack(boolean isDeadlyAttack) {
        this.isDeadlyAttack = isDeadlyAttack;
    }

    /**
     *
     * @return
     */
    public boolean isDeadlyAttack() {
        return isDeadlyAttack;
    }

    /**
     *
     * @param mpBurn
     */
    public void setMpBurn(int mpBurn) {
        this.mpBurn = mpBurn;
    }

    /**
     *
     * @return
     */
    public int getMpBurn() {
        return mpBurn;
    }

    /**
     *
     * @param diseaseSkill
     */
    public void setDiseaseSkill(int diseaseSkill) {
        this.diseaseSkill = diseaseSkill;
    }

    /**
     *
     * @return
     */
    public int getDiseaseSkill() {
        return diseaseSkill;
    }

    /**
     *
     * @param diseaseLevel
     */
    public void setDiseaseLevel(int diseaseLevel) {
        this.diseaseLevel = diseaseLevel;
    }

    /**
     *
     * @return
     */
    public int getDiseaseLevel() {
        return diseaseLevel;
    }

    /**
     *
     * @param mpCon
     */
    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    /**
     *
     * @return
     */
    public int getMpCon() {
        return mpCon;
    }
}
