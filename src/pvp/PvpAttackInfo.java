/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pvp;

import java.awt.Rectangle;

/**
 *
 * @author zjj
 */
public class PvpAttackInfo {

    /**
     *
     */
    public int skillId; // 技能id

    /**
     *
     */
    public int critRate; // 暴击

    /**
     *
     */
    public int ignoreDef; // 无视防御

    /**
     *
     */
    public int skillDamage; // 技能伤害

    /**
     *
     */
    public int mobCount; // 攻击人物数量

    /**
     *
     */
    public int attackCount; // 攻击次数

    /**
     *
     */
    public int pvpRange; // 攻击范围

    /**
     *
     */
    public boolean facingLeft; // 

    /**
     *
     */
    public double maxDamage; // 最大攻击

    /**
     *
     */
    public Rectangle box;
}
