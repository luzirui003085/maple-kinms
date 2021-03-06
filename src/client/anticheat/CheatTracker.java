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
package client.anticheat;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import constants.GameConstants;
import handling.world.World;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.AutobanManager;
import server.Timer.CheatTimer;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.StringUtil;

/**
 *
 * @author zjj
 */
public class CheatTracker {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rL = lock.readLock(), wL = lock.writeLock();
    private final Map<CheatingOffense, CheatingOffenseEntry> offenses = new EnumMap<CheatingOffense, CheatingOffenseEntry>(CheatingOffense.class);
    private final WeakReference<MapleCharacter> chr;
    // For keeping track of speed attack hack.
    private int lastAttackTickCount = 0;
    private byte Attack_tickResetCount = 0;
    private long Server_ClientAtkTickDiff = 0;
    private long lastDamage = 0;
    private long takingDamageSince;
    private int numSequentialDamage = 0;
    private long lastDamageTakenTime = 0;
    private byte numZeroDamageTaken = 0;
    private int numSequentialSummonAttack = 0;
    private long summonSummonTime = 0;
    private int numSameDamage = 0;
    private Point lastMonsterMove;
    private int monsterMoveCount;
    private int attacksWithoutHit = 0;
    private byte dropsPerSecond = 0;
    private long lastDropTime = 0;
    private byte msgsPerSecond = 0;
    private long lastMsgTime = 0;
    private ScheduledFuture<?> invalidationTask;
    private int gm_message = 50;
    private int lastTickCount = 0, tickSame = 0;
    private long lastASmegaTime = 0;
    private boolean saveToDB = false;
    private long lastSaveTime = 0L;
    private long[] lastTime = new long[6];

    /**
     *
     * @param chr
     */
    public CheatTracker(final MapleCharacter chr) {
        this.chr = new WeakReference<>(chr);
        invalidationTask = CheatTimer.getInstance().register(new InvalidationTask(), 60000);
        takingDamageSince = System.currentTimeMillis();
    }

    /**
     *
     * @param skillId
     * @param tickcount
     */
    public final void checkAttack(final int skillId, final int tickcount) {
        final short AtkDelay = GameConstants.getAttackDelay(skillId);
        if ((tickcount - lastAttackTickCount) < AtkDelay) {
            registerOffense(CheatingOffense.FASTATTACK);
        }
        final long STime_TC = System.currentTimeMillis() - tickcount; // hack = - more
        if (Server_ClientAtkTickDiff - STime_TC > 250) { // 250 is the ping, TODO
            registerOffense(CheatingOffense.FASTATTACK2);
        }
        // if speed hack, client tickcount values will be running at a faster pace
        // For lagging, it isn't an issue since TIME is running simotaniously, client
        // will be sending values of older time

//	System.out.println("Delay [" + skillId + "] = " + (tickcount - lastAttackTickCount) + ", " + (Server_ClientAtkTickDiff - STime_TC));
        Attack_tickResetCount++; // Without this, the difference will always be at 100
        if (Attack_tickResetCount >= (AtkDelay <= 200 ? 2 : 4)) {
            Attack_tickResetCount = 0;
            Server_ClientAtkTickDiff = STime_TC;
        }
        chr.get().updateTick(tickcount);
        lastAttackTickCount = tickcount;
    }

    /**
     *
     * @param damage
     */
    public final void checkTakeDamage(final int damage) {
        numSequentialDamage++;
        lastDamageTakenTime = System.currentTimeMillis();

        // System.out.println("tb" + timeBetweenDamage);
        // System.out.println("ns" + numSequentialDamage);
        // System.out.println(timeBetweenDamage / 1500 + "(" + timeBetweenDamage / numSequentialDamage + ")");
        if (lastDamageTakenTime - takingDamageSince / 500 < numSequentialDamage) {
            registerOffense(CheatingOffense.FAST_TAKE_DAMAGE);
        }
        if (lastDamageTakenTime - takingDamageSince > 4500) {
            takingDamageSince = lastDamageTakenTime;
            numSequentialDamage = 0;
        }
        /*	(non-thieves)
         Min Miss Rate: 2%
         Max Miss Rate: 80%
         (thieves)
         Min Miss Rate: 5%
         Max Miss Rate: 95%*/
        if (damage == 0) {
            numZeroDamageTaken++;
            if (numZeroDamageTaken >= 35) { // Num count MSEA a/b players
                numZeroDamageTaken = 0;
                registerOffense(CheatingOffense.HIGH_AVOID);
            }
        } else if (damage != -1) {
            numZeroDamageTaken = 0;
        }
    }

    /**
     *
     * @return
     */
    public boolean canSaveDB() {
        if (!this.saveToDB) {
            this.saveToDB = true;
            return false;
        }
        if ((this.lastSaveTime + 60000L > System.currentTimeMillis()) && (this.chr.get() != null)) {
            return false;
        }
        this.lastSaveTime = System.currentTimeMillis();
        return true;
    }

    /**
     *
     * @param dmg
     */
    public final void checkSameDamage(final int dmg) {
        if (dmg > 2000 && lastDamage == dmg) {
            numSameDamage++;

            if (numSameDamage > 5) {
                numSameDamage = 0;
                registerOffense(CheatingOffense.SAME_DAMAGE, numSameDamage + " times: " + dmg);
            }
        } else {
            lastDamage = dmg;
            numSameDamage = 0;
        }
    }

    /**
     *
     * @param pos
     * @param chr
     */
    public final void checkMoveMonster(final Point pos, MapleCharacter chr) {

        //double dis = Math.abs(pos.distance(lastMonsterMove));
        if (pos.equals(this.lastMonsterMove)) {
            monsterMoveCount++;
            if (monsterMoveCount > 50) {
             //   registerOffense(CheatingOffense.MOVE_MONSTERS);
                monsterMoveCount = 0;
               World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[管理員訊息] 开挂玩家[" + MapleCharacterUtil.makeMapleReadable(chr.getName()) +"] 地图ID["+ chr.getMapId() +"] 怀疑使用吸怪! ").getBytes());
                     String note = "时间：" + FileoutputUtil.CurrentReadable_Time() + " "
                        + "|| 玩家名字：" + chr.getName() + ""
                        + "|| 玩家地图：" + chr.getMapId() + "\r\n";
                FileoutputUtil.packetLog("log\\吸怪检测\\" + chr.getName() + ".log", note);
            }
            /*
             * } else if ( dis > 1500 ) { monsterMoveCount++; if
             * (monsterMoveCount > 15) {
             * registerOffense(CheatingOffense.MOVE_MONSTERS);
            }
             */
        } else {
            lastMonsterMove = pos;
            monsterMoveCount = 1;
        }
    }

    /**
     *
     * @param dmg
     * @param expected
     */
    public void checkSameDamage(int dmg, double expected) {
        if ((dmg > 2000) && (this.lastDamage == dmg) && (this.chr.get() != null) && ((this.chr.get().getLevel() < 180) || (dmg > expected * 2.0D))) {
            this.numSameDamage += 1;
            if (this.numSameDamage > 5) {
                registerOffense(CheatingOffense.SAME_DAMAGE, new StringBuilder().append(this.numSameDamage).append(" times, 攻击伤害 ").append(dmg).append(", 预期伤害 ").append(expected).append(" [等级: ").append(this.chr.get().getLevel()).append(", 职业: ").append(this.chr.get().getJob()).append("]").toString());
                this.numSameDamage = 0;
            }
        } else {
            this.lastDamage = dmg;
            this.numSameDamage = 0;
        }
    }

    /**
     *
     */
    public final void resetSummonAttack() {
        summonSummonTime = System.currentTimeMillis();
        numSequentialSummonAttack = 0;
    }

    /**
     *
     * @return
     */
    public final boolean checkSummonAttack() {
        numSequentialSummonAttack++;
        //estimated
        // System.out.println(numMPRegens + "/" + allowedRegens);
        if ((System.currentTimeMillis() - summonSummonTime) / (2000 + 1) < numSequentialSummonAttack) {
            registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
            return false;
        }
        return true;
    }

    /**
     *
     */
    public final void checkDrop() {
        checkDrop(false);
    }

    /**
     *
     * @param dc
     */
    public final void checkDrop(final boolean dc) {
        if ((System.currentTimeMillis() - lastDropTime) < 1000) {
            dropsPerSecond++;
            if (dropsPerSecond >= (dc ? 32 : 16) && chr.get() != null) {
//                if (dc) {
//                    chr.get().getClient().getSession().close();
//                } else {
                chr.get().getClient().setMonitored(true);
//                }
            }
        } else {
            dropsPerSecond = 0;
        }
        lastDropTime = System.currentTimeMillis();
    }

    /**
     *
     * @return
     */
    public boolean canAvatarSmega2() {
	if (lastASmegaTime + 10000 > System.currentTimeMillis() && chr.get() != null && !chr.get().isGM()) {
	    return false;
	}
	lastASmegaTime = System.currentTimeMillis();
	return true;
    }

    /**
     *
     * @param limit
     * @param type
     * @return
     */
    public synchronized boolean GMSpam(int limit, int type) {
        if (type < 0 || lastTime.length < type) {
            type = 1; // default xD
        }
        if (System.currentTimeMillis() < limit + lastTime[type]) {
            return true;
        }
        lastTime[type] = System.currentTimeMillis();
        return false;
    }

    /**
     *
     */
    public final void checkMsg() { //ALL types of msg. caution with number of  msgsPerSecond
        if ((System.currentTimeMillis() - lastMsgTime) < 1000) { //luckily maplestory has auto-check for too much msging
            msgsPerSecond++;
            /*            if (msgsPerSecond > 10 && chr.get() != null) {
             chr.get().getClient().getSession().close();
             }*/
        } else {
            msgsPerSecond = 0;
        }
        lastMsgTime = System.currentTimeMillis();
    }

    /**
     *
     * @return
     */
    public final int getAttacksWithoutHit() {
        return attacksWithoutHit;
    }

    /**
     *
     * @param increase
     */
    public final void setAttacksWithoutHit(final boolean increase) {
        if (increase) {
            this.attacksWithoutHit++;
        } else {
            this.attacksWithoutHit = 0;
        }
    }

    /**
     *
     * @param offense
     */
    public final void registerOffense(final CheatingOffense offense) {
        registerOffense(offense, null);
    }

    /**
     *
     * @param offense
     * @param param
     */
    public final void registerOffense(final CheatingOffense offense, final String param) {
        final MapleCharacter chrhardref = chr.get();
        if (chrhardref == null || !offense.isEnabled() || chrhardref.isClone() || chrhardref.isGM()) {
            return;
        }
        CheatingOffenseEntry entry = null;
        rL.lock();
        try {
            entry = offenses.get(offense);
        } finally {
            rL.unlock();
        }
        if (entry != null && entry.isExpired()) {
            expireEntry(entry);
            entry = null;
        }
        if (entry == null) {
            entry = new CheatingOffenseEntry(offense, chrhardref.getId());
        }
        if (param != null) {
            entry.setParam(param);
        }
        entry.incrementCount();
        if (offense.shouldAutoban(entry.getCount())) {
            final byte type = offense.getBanType();
            if (type == 1) {
                AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
            } else if (type == 2) {
                //chrhardref.getClient().getSession().close();
            }
            gm_message = 50;
            return;
        }
        wL.lock();
        try {
            offenses.put(offense, entry);
        } finally {
            wL.unlock();
        }
        switch (offense) {
            // case HIGH_DAMAGE_MAGIC_2:
            // case MOVE_MONSTERS:
            // case HIGH_DAMAGE_2:
            // case ATTACK_FARAWAY_MONSTER:
            // case ATTACK_FARAWAY_MONSTER_SUMMON:
            case SAME_DAMAGE:
            // case FASTATTACK:
            // case FASTATTACK2:
                gm_message--;
                if (gm_message == 0) {
                    System.out.println(MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) + "疑似使用外掛");
                   World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[管理員訊息] 开挂玩家[" + MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) +"] 地图ID["+ chrhardref.getMapId() +"] suspected of hacking! " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param))).getBytes());
                     String note = "时间：" + FileoutputUtil.CurrentReadable_Time() + " "
                            + "|| 玩家名字：" + chrhardref.getName() + ""
                            + "|| 玩家地图：" + chrhardref.getMapId() + ""
                            + "|| 外挂类型：" + offense.name() + "\r\n";
                    FileoutputUtil.packetLog("log\\外挂检测\\" + chrhardref.getName() + ".log", note);
                    //                    AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
                    gm_message = 50;
                }
                break;
        }
        CheatingOffensePersister.getInstance().persistEntry(entry);
    }

    /**
     *
     * @param newTick
     */
    public void updateTick(int newTick) {
        if (newTick == lastTickCount) { //definitely packet spamming
/*	    if (tickSame >= 5) {
             chr.get().getClient().getSession().close(); //i could also add a check for less than, but i'm not too worried at the moment :)
             } else {*/
            tickSame++;
//	    }
        } else {
            tickSame = 0;
        }
        lastTickCount = newTick;
    }

    /**
     *
     * @param coe
     */
    public final void expireEntry(final CheatingOffenseEntry coe) {
        wL.lock();
        try {
            offenses.remove(coe.getOffense());
        } finally {
            wL.unlock();
        }
    }

    /**
     *
     * @return
     */
    public final int getPoints() {
        int ret = 0;
        CheatingOffenseEntry[] offenses_copy;
        rL.lock();
        try {
            offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
        } finally {
            rL.unlock();
        }
        for (final CheatingOffenseEntry entry : offenses_copy) {
            if (entry.isExpired()) {
                expireEntry(entry);
            } else {
                ret += entry.getPoints();
            }
        }
        return ret;
    }

    /**
     *
     * @return
     */
    public final Map<CheatingOffense, CheatingOffenseEntry> getOffenses() {
        return Collections.unmodifiableMap(offenses);
    }

    /**
     *
     * @return
     */
    public final String getSummary() {
        final StringBuilder ret = new StringBuilder();
        final List<CheatingOffenseEntry> offenseList = new ArrayList<>();
        rL.lock();
        try {
            for (final CheatingOffenseEntry entry : offenses.values()) {
                if (!entry.isExpired()) {
                    offenseList.add(entry);
                }
            }
        } finally {
            rL.unlock();
        }
        Collections.sort(offenseList, new Comparator<CheatingOffenseEntry>() {

            @Override
            public final int compare(final CheatingOffenseEntry o1, final CheatingOffenseEntry o2) {
                final int thisVal = o1.getPoints();
                final int anotherVal = o2.getPoints();
                return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
            }
        });
        final int to = Math.min(offenseList.size(), 4);
        for (int x = 0; x < to; x++) {
            ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).getOffense().name()));
            ret.append(": ");
            ret.append(offenseList.get(x).getCount());
            if (x != to - 1) {
                ret.append(" ");
            }
        }
        return ret.toString();
    }

    /**
     *
     */
    public final void dispose() {
        if (invalidationTask != null) {
            invalidationTask.cancel(false);
        }
        invalidationTask = null;
    }

    private final class InvalidationTask implements Runnable {

        @Override
        public final void run() {
            CheatingOffenseEntry[] offenses_copy;
            rL.lock();
            try {
                offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
            } finally {
                rL.unlock();
            }
            for (CheatingOffenseEntry offense : offenses_copy) {
                if (offense.isExpired()) {
                    expireEntry(offense);
                }
            }
            if (chr.get() == null) {
                dispose();
            }
        }
    }
}
