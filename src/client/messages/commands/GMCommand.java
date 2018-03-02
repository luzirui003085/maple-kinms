package client.messages.commands;

import client.*;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import client.messages.CommandProcessorUtil;
import com.mysql.jdbc.Connection;
import constants.GameConstants;
import constants.ServerConstants.PlayerGMRank;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.CheaterData;
import handling.world.World;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import scripting.EventManager;
import scripting.PortalScriptManager;
import scripting.ReactorScriptManager;
import server.*;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleShopFactory;
import server.ShutdownServer;
import server.Timer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.OverrideMonsterStats;
import server.maps.FakeCharacter;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import tools.ArrayMap;
import tools.HexTool;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @author Emilyx3
 */
public class GMCommand {

    /**
     * @return
     */
    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.GM;
    }

    public static class 超级拉所有玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cs.getPlayerStorage().getAllCharacters()) {
                    if (chr.getMapId() != c.getPlayer().getMapId()) {
                        chr.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
                    }
                    if (chr.getClient().getChannel() != c.getChannel()) {
                        chr.changeChannel(c.getChannel());
                    }
                }
            }
            return 0;
        }
    }

    public static class 拉 extends WarpHere {
    }

    public static class WarpHere extends CommandExecute {

        public int execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
            } else {
                int ch = World.Find.findChannel(splitted[1]);
                if (ch < 0) {
                    c.getPlayer().dropMessage(5, "角色不在线");
                    return 1;
                } else {
                    victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                    c.getPlayer().dropMessage(5, "正在传送玩家到身边");
                    victim.dropMessage(5, "GM正在传送你");
                    if (victim.getMapId() != c.getPlayer().getMapId()) {
                        final MapleMap mapp = victim.getClient().getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId());
                        victim.changeMap(mapp, mapp.getPortal(0));
                    }
                    victim.changeChannel(c.getChannel());
                }
            }
            return 1;
        }

    }

    /**
     *
     */
    public static class Ban extends CommandExecute {

        /**
         *
         */
        protected boolean hellban = false;

        private String getCommand() {
            return "Ban";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(5, "[Syntax] !" + getCommand() + " <玩家> <原因>");
                return 0;
            }
            StringBuilder sb = new StringBuilder(c.getPlayer().getName());
            sb.append(" banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (target != null) {
                if (c.getPlayer().getGMLevel() > target.getGMLevel() || c.getPlayer().isAdmin()) {
                    sb.append(" (IP: ").append(target.getClient().getSessionIPAddress()).append(")");
                    if (target.ban(sb.toString(), c.getPlayer().isAdmin(), false, hellban)) {
                        c.getPlayer().dropMessage(6, "[" + getCommand() + "] 成功封鎖 " + splitted[1] + ".");
                        return 1;
                    } else {
                        c.getPlayer().dropMessage(6, "[" + getCommand() + "] 封鎖失敗.");
                        return 0;
                    }
                } else {
                    c.getPlayer().dropMessage(6, "[" + getCommand() + "] May not ban GMs...");
                    return 1;
                }
            } else {
                if (MapleCharacter.ban(splitted[1], sb.toString(), false, c.getPlayer().isAdmin() ? 250 : c.getPlayer().getGMLevel(), splitted[0].equals("!hellban"))) {
                    c.getPlayer().dropMessage(6, "[" + getCommand() + "] 成功離線封鎖 " + splitted[1] + ".");
                    return 1;
                } else {
                    c.getPlayer().dropMessage(6, "[" + getCommand() + "] 封鎖失敗 " + splitted[1]);
                    return 0;
                }
            }
        }
    }

    /**
     *
     */
    public static class UnBan extends CommandExecute {

        /**
         *
         */
        protected boolean hellban = false;

        private String getCommand() {
            return "UnBan";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "[Syntax] !" + getCommand() + " <原因>");
                return 0;
            }
            byte ret;
            if (hellban) {
                ret = MapleClient.unHellban(splitted[1]);
            } else {
                ret = MapleClient.unban(splitted[1]);
            }
            if (ret == -2) {
                c.getPlayer().dropMessage(6, "[" + getCommand() + "] SQL error.");
                return 0;
            } else if (ret == -1) {
                c.getPlayer().dropMessage(6, "[" + getCommand() + "] The character does not exist.");
                return 0;
            } else {
                c.getPlayer().dropMessage(6, "[" + getCommand() + "] Successfully unbanned!");

            }
            byte ret_ = MapleClient.unbanIPMacs(splitted[1]);
            if (ret_ == -2) {
                c.getPlayer().dropMessage(6, "[UnbanIP] SQL error.");
            } else if (ret_ == -1) {
                c.getPlayer().dropMessage(6, "[UnbanIP] The character does not exist.");
            } else if (ret_ == 0) {
                c.getPlayer().dropMessage(6, "[UnbanIP] No IP or Mac with that character exists!");
            } else if (ret_ == 1) {
                c.getPlayer().dropMessage(6, "[UnbanIP] IP/Mac -- one of them was found and unbanned.");
            } else if (ret_ == 2) {
                c.getPlayer().dropMessage(6, "[UnbanIP] Both IP and Macs were unbanned.");
            }
            return ret_ > 0 ? 1 : 0;
        }
    }

    /**
     *
     */
    public static class 双倍经验 extends CommandExecute {

        private int change = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            this.change = Integer.parseInt(splitted[1]);
            if ((this.change == 0) || (this.change == 1)) {
                c.getPlayer().dropMessage(5, "以前 - 经验: " + c.getChannelServer().getExpRate() + " 金币: " + c.getChannelServer().getMesoRate() + " 爆率: " + c.getChannelServer().getDropRate());
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.setDoubleExp(this.change);
                }
                c.getPlayer().dropMessage(5, "现在 - 经验: " + c.getChannelServer().getExpRate() + " 金币: " + c.getChannelServer().getMesoRate() + " 爆率: " + c.getChannelServer().getDropRate());
                if (this.change == 0) {
                    for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                        for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员关闭【双倍经验】活动！快感谢管理员吧！", 5120000);
                        }
                    }
                } else if (this.change == 1) {
                    for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                        for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员开启【双倍经验】活动！快感谢管理员吧！", 5120000);
                        }
                    }
                }
                return 1;
            }
            c.getPlayer().dropMessage(5, "输入的数字无效，0为关闭活动经验，1为开启活动经验。当前输入为: " + this.change);
            return 0;
        }
    }

    /**
     *
     */
    public static class 双倍经验time extends CommandExecute {

        private int time = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            this.time = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                cserv.setDoubleExp(1);
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr == null) {
                        continue;
                    }
                    chr.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员开启【双倍经验】活动！", 5120000);
                }
            }
            Timer.WorldTimer.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setDoubleExp(0);
                        for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            chr.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员关闭【双倍经验】活动！期待下次活动！", 5120000);
                        }
                    }
                }
            }, 60000 * time);
            return 0;
        }
    }

    /**
     *
     */
    public static class 双倍爆率 extends CommandExecute {

        private int change = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            this.change = Integer.parseInt(splitted[1]);
            if ((this.change == 1) || (this.change == 2)) {
                c.getPlayer().dropMessage(5, "以前 - 经验: " + c.getChannelServer().getExpRate() + " 金币: " + c.getChannelServer().getMesoRate() + " 爆率: " + c.getChannelServer().getDropRate());
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.setDoubleDrop(this.change);
                }
                c.getPlayer().dropMessage(5, "现在 - 经验: " + c.getChannelServer().getExpRate() + " 金币: " + c.getChannelServer().getMesoRate() + " 爆率: " + c.getChannelServer().getDropRate());
                if (this.change == 1) {
                    for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                        for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员关闭【双倍爆率】活动！快感谢管理员吧！", 5121009);
                        }
                    }
                } else if (this.change == 2) {
                    for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                        for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员开启【双倍爆率】活动！快感谢管理员吧！", 5121009);
                        }
                    }
                }
                return 1;
            }
            c.getPlayer().dropMessage(5, "输入的数字无效，1为关闭活动经验，2为开启活动经验。当前输入为: " + this.change);
            return 0;
        }
    }

    /**
     *
     */
    public static class 双倍金币 extends CommandExecute {

        private int change = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            this.change = Integer.parseInt(splitted[1]);
            if ((this.change == 1) || (this.change == 2)) {
                c.getPlayer().dropMessage(5, "以前 - 经验: " + c.getChannelServer().getExpRate() + " 金币: " + c.getChannelServer().getMesoRate() + " 爆率: " + c.getChannelServer().getDropRate());
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.setDoubleMeso(this.change);
                }
                c.getPlayer().dropMessage(5, "现在 - 经验: " + c.getChannelServer().getExpRate() + " 金币: " + c.getChannelServer().getMesoRate() + " 爆率: " + c.getChannelServer().getDropRate());
                if (this.change == 1) {
                    for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                        for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员关闭【双倍冒险币】活动！快感谢管理员吧！", 5121020);

                        }
                    }
                } else if (this.change == 2) {
                    for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                        for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {

                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员开启【双倍冒险币】活动！快感谢管理员吧！", 5121020);

                        }
                    }
                }
                return 1;
            }
            c.getPlayer().dropMessage(5, "输入的数字无效，1为关闭活动经验，2为开启活动经验。当前输入为: " + this.change);
            return 0;
        }
    }

    /**
     *
     */
    public static class 给所有人点卷 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !给所有人点卷 [点卷类型1：点卷 - 2：抵用] [点卷数量]");
                return 0;
            }
            int type = Integer.parseInt(splitted[1]);
            int quantity = Integer.parseInt(splitted[2]);
            if ((type <= 0) || (type > 2)) {
                type = 2;
            }
            if (quantity > 9000) {
                quantity = 9000;
            }
            int ret = 0;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.modifyCSPoints(type, quantity, false);
                    mch.dropMessage(-11, new StringBuilder().append("[系统提示] 恭喜您获得管理员赠送给您的").append(type == 1 ? "点券 " : " 抵用券 ").append(quantity).append(" 点.").toString());
                    ret++;
                }
            }
            if (type == 1) {
                for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "点卷给在线的所有玩家！快感谢管理员吧！", 5120004);

                    }
                }
            } else if (type == 2) {
                for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "抵用卷给在线的所有玩家！快感谢管理员吧！", 5120004);

                    }
                }
            }
            c.getPlayer().dropMessage(6, new StringBuilder().append("命令使用成功，当前共有: ").append(ret).append(" 个玩家获得: ").append(quantity).append(" 点的").append(type == 1 ? "点券 " : " 抵用券 ").append(" 总计: ").append(ret * quantity).toString());
            return 1;
        }
    }

    /**
     *
     */
    public static class 给所有人经验 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int quantity = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainExp(quantity, true, false, true);
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "经验给在线的所有玩家！快感谢管理员吧！", 5121020);

                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 给所有人冒险币 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int quantity = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainMeso(quantity, true);
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "冒险币给在线的所有玩家！快感谢管理员吧！", 5121020);

                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 给所有人物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !给所有人物品 [物品ID] [数量]");
                return 0;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int item = Integer.parseInt(splitted[1]);
            int quantity = Integer.parseInt(splitted[2]);
            String mz = ii.getName(item);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    MapleInventoryManipulator.addById(mch.getClient(), item, (short) quantity, (byte) 0);
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (quantity <= 1) {
                        mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放【" + mz + "】物品给在线的所有玩家！快感谢管理员吧！", 5120000);

                    } else {
                        mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放【" + mz + "】物品【" + quantity + "】个给在线的所有玩家！快感谢管理员吧！", 5120000);

                    }
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 给当前地图点卷 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !给当前地图点卷 [点卷类型1：点卷 - 2：抵用] [点卷数量]");
                return 0;
            }
            int type = Integer.parseInt(splitted[1]);
            int quantity = Integer.parseInt(splitted[2]);
            if ((type <= 0) || (type > 2)) {
                type = 2;
            }
            if (quantity > 9000) {
                quantity = 9000;
            }
            int ret = 0;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (c.getPlayer().getMapId() == mch.getMapId()) {
                        mch.modifyCSPoints(type, quantity, false);
                        mch.dropMessage(-11, new StringBuilder().append("[系统提示] 恭喜您获得管理员赠送给您的").append(type == 1 ? "点券 " : " 抵用券 ").append(quantity).append(" 点.").toString());
                        ret++;
                    }
                }
            }
            if (type == 1) {
                for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        if (c.getPlayer().getMapId() == mch.getMapId()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "点卷给当前地图在线的所有玩家！快感谢管理员吧！", 5120004);
                        }
                    }
                }
            } else if (type == 2) {
                for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        if (c.getPlayer().getMapId() == mch.getMapId()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "抵用卷给当前地图在线的所有玩家！快感谢管理员吧！", 5120004);
                        }
                    }
                }
            }
            c.getPlayer().dropMessage(6, new StringBuilder().append("命令使用成功，当前共有: ").append(ret).append(" 个玩家获得: ").append(quantity).append(" 点的").append(type == 1 ? "点券 " : " 抵用券 ").append(" 总计: ").append(ret * quantity).toString());
            return 1;
        }
    }

    /**
     *
     */
    public static class 给当前地图经验 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int quantity = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (c.getPlayer().getMapId() == mch.getMapId()) {
                        mch.gainExp(quantity, true, false, true);
                    }
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (c.getPlayer().getMapId() == mch.getMapId()) {
                        mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "经验给当前地图在线的所有玩家！快感谢管理员吧！", 5121020);
                    }
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 给当前地图冒险币 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int quantity = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (c.getPlayer().getMapId() == mch.getMapId()) {
                        mch.gainMeso(quantity, true);
                    }
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (c.getPlayer().getMapId() == mch.getMapId()) {
                        mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放" + quantity + "冒险币给当前地图在线的所有玩家！快感谢管理员吧！", 5121020);
                    }

                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 给当前地图物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !给当前地图物品 [物品ID] [数量]");
                return 0;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int item = Integer.parseInt(splitted[1]);
            int quantity = Integer.parseInt(splitted[2]);
            String mz = ii.getName(item);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (c.getPlayer().getMapId() == mch.getMapId()) {
                        MapleInventoryManipulator.addById(mch.getClient(), item, (short) quantity, (byte) 0);
                    }
                }
            }
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (quantity <= 1) {
                        if (c.getPlayer().getMapId() == mch.getMapId()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放【" + mz + "】物品给当前地图在线的所有玩家！快感谢管理员吧！", 5120000);
                        }

                    } else {
                        if (c.getPlayer().getMapId() == mch.getMapId()) {
                            mch.startMapEffect(ServerProperties.getProperty("KinMS.CommandMessage") + "管理员发放【" + mz + "】物品【" + quantity + "】个给当前地图在线的所有玩家！快感谢管理员吧！", 5120000);
                        }

                    }
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 给玩家物品 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "用法: !给玩家物品 [角色名字][物品ID] [数量]");
                return 0;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            String name = splitted[1];
            int item = Integer.parseInt(splitted[2]);
            int quantity = Integer.parseInt(splitted[3]);
            String mz = ii.getName(item);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (mch.getName().equals(name)) {
                        MapleInventoryManipulator.addById(mch.getClient(), item, (short) quantity, (byte) 0);
                        c.getPlayer().dropMessage(6, "给予成功！");
                    }
                }
            }
            /* for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
             for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
             if (quantity <= 1) {
             mch.startMapEffect("天成管理员发放【" + mz + "】物品给在线的所以玩家！快感谢管理员吧！", 5120000);
             } else {
             mch.startMapEffect("天成管理员发放【" + mz + "】物品【" + quantity + "】个给在线的所以玩家！快感谢管理员吧！", 5120000);
             }
             }
             }*/
            return 1;
        }
    }

    /**
     *
     */
    public static class DCAll extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int range = -1;
            switch (splitted[1]) {
                case "m":
                    range = 0;
                    break;
                case "c":
                    range = 1;
                    break;
                case "w":
                    range = 2;
                    break;
                default:
                    break;
            }
            if (range == -1) {
                range = 1;
            }
            switch (range) {
                case 0:
                    c.getPlayer().getMap().disconnectAll();
                    c.getPlayer().dropMessage(5, "已成功断开当前地图所有玩家的连接.");
                    break;
                case 1:
                    c.getChannelServer().getPlayerStorage().disconnectAll(true);
                    c.getPlayer().dropMessage(5, "已成功断开当前频道所有玩家的连接.");
                    break;
                case 2:
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.getPlayerStorage().disconnectAll(true);
                    }
                    c.getPlayer().dropMessage(5, "已成功断开当前游戏所有玩家的连接.");
                    break;
                default:
                    break;
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class DC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int level = 0;
            MapleCharacter victim;
            if (splitted[1].charAt(0) == '-') {
                level = StringUtil.countCharacters(splitted[1], 'f');
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
            } else {
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            }
            if (level < 2 && victim != null) {
                victim.getClient().getSession().close();
                if (level >= 1) {
                    victim.getClient().disconnect(true, false);
                }
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "Please use dc -f instead, or the victim does not exist.");
                return 0;
            }
        }
    }

    /**
     *
     */
    public static class 删除道具 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "需要输入 <角色名字> <道具ID>");
                return 0;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "输入的角色不存在或者角色不在线或者不在这个频道.");
                return 0;
            }
            chr.removeAll(Integer.parseInt(splitted[2]), false, false);
            c.getPlayer().dropMessage(6, "已经成功的将ID为: " + splitted[2] + " 的所有道具从角色: " + splitted[1] + " 的背包中删除.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 人气 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !人气 <玩家名字> <要加人气的数量>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int fame = 0;
            try {
                fame = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(6, "输入的数字无效...");
                return 0;
            }
            if ((victim != null) && (player.allowedToTarget(victim))) {
                victim.addFame(fame);
                victim.updateSingleStat(MapleStat.FAME, victim.getFame());
                // GMCommand.log.info("[命令] 管理员 " + player.getName() + " 给玩家 " + victim.getName() + " 加人气 " + fame + " 点.");
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 查看当前地图信息 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().dropMessage(6, "当前地图信息: ID " + c.getPlayer().getMapId() + " 名字 " + c.getPlayer().getMap().getMapName() + " 当前坐标: X " + c.getPlayer().getPosition().x + " Y " + c.getPlayer().getPosition().y);
            return 1;
        }
    }

    /**
     *
     */
    public static class KillAll extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleMap map = c.getPlayer().getMap();
            double range = (1.0D / 0.0D);
            if (splitted.length > 1) {
                int irange = Integer.parseInt(splitted[1]);
                if (splitted.length <= 2) {
                    range = irange * irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
                }
            }
            if (map == null) {
                c.getPlayer().dropMessage(6, "输入的地图不存在.");
                return 0;
            }

            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.MONSTER}))) {
                MapleMonster mob = (MapleMonster) monstermo;
                if ((!mob.getStats().isBoss()) || (c.getPlayer().isGM())) {
                    map.killMonster(mob, c.getPlayer(), false, false, (byte) 1);
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class WarpMap extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            ;
            try {
                MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                if (target == null) {
                    c.getPlayer().dropMessage(6, "输入的地图不存在.");
                    return 0;
                }
                MapleMap from = c.getPlayer().getMap();
                for (MapleCharacter chr : from.getCharactersThreadsafe()) {
                    chr.changeMap(target, target.getPortal(0));
                }
            } catch (Exception e) {

                c.getPlayer().dropMessage(5, "错误: " + e.getMessage());
                return 0;
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 杀死玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !杀死玩家 <list player names>");
                return 0;
            }
            MapleCharacter victim = null;
            for (int i = 1; i < splitted.length; i++) {
                try {
                    victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[i]);
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "没有找到名字为: " + splitted[i] + " 的玩家.");
                }
                if ((player.allowedToTarget(victim)) && (player.getGMLevel() >= victim.getGMLevel())) {
                    victim.getStat().setHp(0);
                    victim.getStat().setMp(0);
                    victim.updateSingleStat(MapleStat.HP, 0);
                    victim.updateSingleStat(MapleStat.MP, 0);
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 断开玩家连接 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[(splitted.length - 1)]);
            if ((victim != null) && (c.getPlayer().getGMLevel() >= victim.getGMLevel())) {
                victim.getClient().getSession().close();
                victim.getClient().disconnect(true, false);
                c.getPlayer().dropMessage(6, "已经成功断开 " + victim.getName() + " 的连接.");
                return 1;
            }
            c.getPlayer().dropMessage(6, "使用的对象不存在或者角色名字错误或者对放的GM权限比你高.");
            return 0;
        }
    }

    /**
     *
     */
    public static class 频道在线 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().dropMessage(6, "频道在线: " + Integer.parseInt(splitted[1]) + ":");
            c.getPlayer().dropMessage(6, ChannelServer.getInstance(Integer.parseInt(splitted[1])).getPlayerStorage().getOnlinePlayers(true));
            return 1;
        }
    }

    /**
     *
     */
    public static class 在线 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().dropMessage(6, "频道在线 " + c.getChannel() + ":");
            c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
            return 1;
        }
    }

    /**
     *
     */
    public static class 隐身模式 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            SkillFactory.getSkill(9001004).getEffect(1).applyTo(c.getPlayer());
            c.getPlayer().dropMessage(6, "隐身模式已开启.");
            return 0;
        }
    }

    /**
     *
     */
    public static class 解除隐身 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().dispelSkill(9001004);
            return 0;
        }
    }

    /**
     *
     */
    public static class 拉玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            ChannelServer cserv = c.getChannelServer();
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
            return 0;
        }
    }

    /**
     *
     */
    public static class 拉玩家id extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            ChannelServer cserv = c.getChannelServer();
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(Integer.parseInt(splitted[1]));
            //  MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
            return 0;
        }
    }

    /**
     *
     */
    public static class 拉所有玩家 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            ChannelServer cserv = c.getChannelServer();
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != c.getPlayer().getMapId()) {
                    mch.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
                }
            }
            return 0;
        }
    }

    /**
     *
     */
    public static class 刷新地图 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            boolean custMap = splitted.length >= 2;
            MapleCharacter player = c.getPlayer();
            int mapid = custMap ? Integer.parseInt(splitted[1]) : player.getMapId();
            MapleMap map = custMap ? player.getClient().getChannelServer().getMapFactory().getMap(mapid) : player.getMap();
            if (player.getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
                MapleMap newMap = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
                MaplePortal newPor = newMap.getPortal(0);
                LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<>(map.getCharacters()); // do NOT remove, fixing ConcurrentModificationEx.
                outerLoop:
                for (MapleCharacter m : mcs) {
                    for (int x = 0; x < 5; x++) {
                        try {
                            m.changeMap(newMap, newPor);
                            continue outerLoop;
                        } catch (Throwable t) {
                        }
                    }
                    //    mc.dropMessage("Failed warping " + m.getName() + " to the new map. Skipping...");
                }
                //  mc.dropMessage("地图刷新完毕，如还出现NPC不见请使用此命令.");
                //    return;
            }
            //   mc.dropMessage("Unsuccessful reset!");
            return 0;
        }
    }

    /**
     *
     */
    public static class 检查玩家物品信息 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if ((splitted.length < 3) || (splitted[1] == null) || (splitted[1].isEmpty()) || (splitted[2] == null) || (splitted[2].isEmpty())) {
                c.getPlayer().dropMessage(6, "用法: !检查玩家物品信息 <玩家名字> <道具ID>");
                return 0;
            }
            int item = Integer.parseInt(splitted[2]);
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int itemamount = chr.getItemQuantity(item, true);
            if (itemamount > 0) {
                c.getPlayer().dropMessage(6, chr.getName() + " 拥有 " + itemamount + " (" + item + ").");
            } else {
                c.getPlayer().dropMessage(6, chr.getName() + " 没有ID为 (" + item + ") 的道具.");
            }

            return 1;
        }
    }

    /**
     *
     */
    public static class 检测作弊 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            List cheaters = World.getCheaters();
            for (int x = cheaters.size() - 1; x >= 0; x--) {
                CheaterData cheater = (CheaterData) cheaters.get(x);
                c.getPlayer().dropMessage(6, cheater.getInfo());
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 找玩家位置 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(5, "没有找到 " + splitted[1] + " 玩家.");
                return 0;
            }
            victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 2), victim.isGM(), 0));

            return 1;
        }
    }

    /**
     *
     */
    public static class 谁在这个地图 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            StringBuilder builder = new StringBuilder("当前地图玩家: ").append(c.getPlayer().getMap().getCharactersThreadsafe().size()).append(" 人. ");
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (builder.length() > 150) {
                    builder.setLength(builder.length() - 2);
                    c.getPlayer().dropMessage(6, builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            c.getPlayer().dropMessage(6, builder.toString());
            return 1;
        }
    }

    /**
     *
     */
    public static class 全部存档 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int p = 0;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    chr.saveToDB(true, true);
                    p++;
                }
            }
            c.getPlayer().dropMessage(5, "[全部存档] 保存" + p + "个玩家成功。");
            return 1;
        }
    }

    /**
     *
     */
    public static class 雇佣存档 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
         /*   int p = 0;
            for (handling.channel.ChannelServer cserv : handling.channel.ChannelServer.getAllInstances()) { 
                try {
                    Thread.sleep(2000L);//2ms
                } catch (InterruptedException ex) {//捕获该线程异常
                    System.out.println("线程锁开启失败：" + ex);
                }
                cserv.closeAllMerchant();
                p++;
            }
            c.getPlayer().dropMessage(5, "[保存雇佣商人系统] 雇佣商人保存" + p + "个频道成功。");
            */

            int p = 0;
            for (handling.channel.ChannelServer cserv : handling.channel.ChannelServer.getAllInstances()) {
                try {
                    Thread.sleep(2000L);//2ms
                } catch (InterruptedException ex) {//捕获该线程异常
                    System.out.println("线程锁开启失败：" + ex);
                }
                cserv.closeAllMerchants();
                p++;
            }
            c.getPlayer().dropMessage(5, "[保存雇佣商人系统] 雇佣商人保存" + p + "个频道成功。");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重置怪物 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().getMap().killAllMonsters(false);
            return 1;
        }
    }

    /**
     *
     */
    public static class 满技能 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().maxAllSkills();
            return 1;
        }
    }

    /**
     *
     */
    public static class Job extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().changeJob(Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    /**
     *
     */
    public static class GainMeso extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().gainMeso(Integer.MAX_VALUE - c.getPlayer().getMeso(), true);
            return 1;
        }
    }

    /**
     *
     */
    public static class Item extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            final int itemId = Integer.parseInt(splitted[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);

            if (!c.getPlayer().isAdmin()) {
                for (int i : GameConstants.itemBlock) {
                    if (itemId == i) {
                        c.getPlayer().dropMessage(5, "Sorry but this item is blocked for your GM level.");
                        return 0;
                    }
                }
                return 0;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (GameConstants.isPet(itemId)) {
                c.getPlayer().dropMessage(5, "Please purchase a pet from the cash shop instead.");
            } else if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 為無效的物品");
            } else {
                IItem item;
                byte flag = 0;
                flag |= ItemFlag.LOCK.getValue();

                if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                    //item.setFlag(flag);

                } else {
                    item = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
                    if (GameConstants.getInventoryType(itemId) != MapleInventoryType.USE) {
                        // item.setFlag(flag);
                    }
                }

                if (item.getType() != MapleInventoryType.USE.getType()) {
                    //  item.setOwner(c.getPlayer().getName());
                }
                item.setGMLog(c.getPlayer().getName());

                MapleInventoryManipulator.addbyItem(c, item);
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class Drop extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            final int itemId = Integer.parseInt(splitted[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);

            if (!c.getPlayer().isAdmin()) {
                for (int i : GameConstants.itemBlock) {
                    if (itemId == i) {
                        c.getPlayer().dropMessage(5, "Sorry but this item is blocked for your GM level.");
                        return 0;
                    }
                }
                return 0;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (GameConstants.isPet(itemId)) {
                c.getPlayer().dropMessage(5, "Please purchase a pet from the cash shop instead.");
            } else if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 為無效的物品");
            } else {
                IItem item;
                byte flag = 0;
                flag |= ItemFlag.LOCK.getValue();

                if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                    //item.setFlag(flag);

                } else {
                    item = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
                    if (GameConstants.getInventoryType(itemId) != MapleInventoryType.USE) {
                        // item.setFlag(flag);
                    }
                }

                if (item.getType() != MapleInventoryType.USE.getType()) {
                    //  item.setOwner(c.getPlayer().getName());
                }
                item.setGMLog(c.getPlayer().getName());

                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, c.getPlayer().getPosition(), true, true);
                //   MapleInventoryManipulator.addbyItem(c, item);
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class Level extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().setLevel(Short.parseShort(splitted[1]));
            c.getPlayer().levelUp();
            if (c.getPlayer().getExp() < 0) {
                c.getPlayer().gainExp(-c.getPlayer().getExp(), false, false, true);
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class spy extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "使用規則: !spy <玩家名字>");
            } else {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim.getGMLevel() > 3) {
                    c.getPlayer().dropMessage(5, "你不能查看比你高權限的人!");
                    return 0;
                }
                if (victim != null) {
                    c.getPlayer().dropMessage(5, "此玩家狀態:");
                    c.getPlayer().dropMessage(5, "等級: " + victim.getLevel() + "职业: " + victim.getJob() + "人气: " + victim.getFame());
                    c.getPlayer().dropMessage(5, "地圖: " + victim.getMapId() + " - " + victim.getMap().getMapName());
                    c.getPlayer().dropMessage(5, "力量: " + victim.getStat().getStr() + "  ||  敏捷: " + victim.getStat().getDex() + "  ||  智力: " + victim.getStat().getInt() + "  ||  运气: " + victim.getStat().getLuk());
                    c.getPlayer().dropMessage(5, "拥有 " + victim.getMeso() + " 冒险币.");
                    // victim.dropMessage(5, c.getPlayer().getName() + " GM在觀察您..");
                } else {
                    c.getPlayer().dropMessage(5, "找不到此玩家.");
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class online1 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().dropMessage(6, "上线的角色 频道-" + c.getChannel() + ":");
            c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
            return 1;
        }
    }

    /**
     *
     */
    public static class Spawn extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int mid = Integer.parseInt(splitted[1]);
            int num = Math.min(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), 500);
            Long hp = CommandProcessorUtil.getNamedLongArg(splitted, 1, "hp");
            Integer exp = CommandProcessorUtil.getNamedIntArg(splitted, 1, "exp");
            Double php = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "php");
            Double pexp = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "pexp");
            MapleMonster onemob;
            try {
                onemob = MapleLifeFactory.getMonster(mid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "错误: " + e.getMessage());
                return 0;
            }
            if (onemob == null) {
                c.getPlayer().dropMessage(5, "输入的怪物不存在.");
                return 0;
            }
            long newhp = 0L;
            int newexp = 0;
            if (hp != null) {
                newhp = hp;
            } else if (php != null) {
                newhp = (long) (onemob.getMobMaxHp() * (php / 100.0D));
            } else {
                newhp = onemob.getMobMaxHp();
            }
            if (exp != null) {
                newexp = exp;
            } else if (pexp != null) {
                newexp = (int) (onemob.getMobExp() * (pexp / 100.0D));
            } else {
                newexp = onemob.getMobExp();
            }
            if (newhp < 1L) {
                newhp = 1L;
            }

            OverrideMonsterStats overrideStats = new OverrideMonsterStats(newhp, onemob.getMobMaxMp(), newexp, false);
            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                mob.setHp(newhp);
                mob.setOverrideStats(overrideStats);
                c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 总在线人数 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            Map connected = World.getConnected();
            StringBuilder conStr = new StringBuilder("连接数量: ");
            boolean first = true;
            for (Iterator i$ = connected.keySet().iterator(); i$.hasNext(); ) {
                int i = ((Integer) i$.next());
                if (!first) {
                    conStr.append(", ");
                } else {
                    first = false;
                }
                if (i == 0) {
                    conStr.append("总计: ");
                    conStr.append(connected.get(i));
                } else {
                    conStr.append("频道 ");
                    conStr.append(i);
                    conStr.append(": ");
                    conStr.append(connected.get(i));
                }
            }
            c.getPlayer().dropMessage(conStr.toString());
            return 1;
        }
    }

    /**
     *
     */
    public static class onlines extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            int p = 0;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr != null && c.getPlayer().getGMLevel() >= chr.getGMLevel()) {
                        StringBuilder ret = new StringBuilder();
                        ret.append(" 频道: ");
                        ret.append(chr.getClient().getChannel());
                        ret.append(" 角色名字 ");
                        ret.append(StringUtil.getRightPaddedStr(chr.getName(), ' ', 13));
                        ret.append(" ID: ");
                        ret.append(chr.getId());
                        ret.append(" 等级: ");
                        ret.append(StringUtil.getRightPaddedStr(String.valueOf(chr.getLevel()), ' ', 3));
                        ret.append(" 职业: ");
                        ret.append(chr.getJob());
                        if (chr.getMap() != null) {
                            ret.append(" 地图: ");
                            ret.append(chr.getMapId()).append(" - ").append(chr.getMap().getMapName());
                            c.getPlayer().dropMessage(6, ret.toString());
                        }
                        p++;
                    }
                }
            }
            c.getPlayer().dropMessage(6, new StringBuilder().append("当前服务器总人数: ").append(p).toString());
            c.getPlayer().dropMessage(6, "-------------------------------------------------------------------------------------");
            return 1;
        }
    }

    /**
     *
     */
    public static class Warp extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted.length == 2) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    victim.changeMap(target, target.getPortal(0));
                }
            } else {
                try {
                    victim = c.getPlayer();
                    int ch = World.Find.findChannel(splitted[1]);
                    if (ch < 0) {
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        c.getPlayer().changeMap(target, target.getPortal(0));
                    } else {
                        victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                        c.getPlayer().dropMessage(6, "Cross changing channel. Please wait.");
                        if (victim.getMapId() != c.getPlayer().getMapId()) {
                            final MapleMap mapp = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                            c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                        }
                        c.getPlayer().changeChannel(ch);
                    }
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "Something went wrong " + e.getMessage());
                    return 0;
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class Warpid extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterById(Integer.parseInt(splitted[1]));
            if (victim != null) {
                if (splitted.length == 2) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    victim.changeMap(target, target.getPortal(0));
                }
            } else {
                try {
                    victim = c.getPlayer();
                    int ch = World.Find.findChannel(Integer.parseInt(splitted[1]));
                    if (ch < 0) {
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        c.getPlayer().changeMap(target, target.getPortal(0));
                    } else {
                        victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                        c.getPlayer().dropMessage(6, "Cross changing channel. Please wait.");
                        if (victim.getMapId() != c.getPlayer().getMapId()) {
                            final MapleMap mapp = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                            c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                        }
                        c.getPlayer().changeChannel(ch);
                    }
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "Something went wrong " + e.getMessage());
                    return 0;
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class CnGM extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }

            World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(5, "<GM聊天視窗>" + "頻道" + c.getPlayer().getClient().getChannel() + " [" + c.getPlayer().getName() + "] : " + StringUtil.joinStringFrom(splitted, 1)).getBytes());

            return 1;
        }
    }

    /**
     *
     */
    public static class 刷抵用卷 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "请输入数量.");
                return 0;
            }
            c.getPlayer().modifyCSPoints(2, Integer.parseInt(splitted[1]), true);
            return 1;
        }
    }

    /**
     *
     */
    public static class 刷点卷 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "请输入数量.");
                return 0;
            }
            c.getPlayer().modifyCSPoints(1, Integer.parseInt(splitted[1]), true);
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载任务 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleQuest.clearQuests();
            c.getPlayer().dropMessage(5, "重新加载任务脚本完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载商城 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            CashItemFactory.getInstance().clearCashShop();
            c.getPlayer().dropMessage(5, "重新加载商城完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载包头 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            SendPacketOpcode.reloadValues();
            RecvPacketOpcode.reloadValues();
            c.getPlayer().dropMessage(5, "重新获取包头完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载副本 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            for (ChannelServer instance : ChannelServer.getAllInstances()) {
                instance.reloadEvents();
            }
            c.getPlayer().dropMessage(5, "重新加载活动脚本完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载商店 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleShopFactory.getInstance().clear();
            c.getPlayer().dropMessage(5, "重新加载商店贩卖道具完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载传送 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            PortalScriptManager.getInstance().clearScripts();
            c.getPlayer().dropMessage(5, "重新加载传送点脚本完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载爆率 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            MapleMonsterInformationProvider.getInstance().clearDrops();
            ReactorScriptManager.getInstance().clearDrops();
            c.getPlayer().dropMessage(5, "重新加载爆率完成.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 重启服务端 extends ShutdownTime {
    }

    /**
     *
     */
    public static class ShutdownTime extends GMCommand.Shutdown {

        private static ScheduledFuture<?> ts = null;
        private int minutesLeft = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            this.minutesLeft = Integer.parseInt(splitted[1]);
            c.getPlayer().dropMessage(6, "游戏将在 " + this.minutesLeft + " 分钟之后关闭...");
            if ((ts == null) && ((t == null) || (!t.isAlive()))) {
                t = new Thread(ShutdownServer.getInstance());
                ts = Timer.EventTimer.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        if (GMCommand.ShutdownTime.this.minutesLeft == 0) {
                            ShutdownServer.getInstance().shutdown();
                            GMCommand.Shutdown.t.start();
                            GMCommand.ShutdownTime.ts.cancel(false);
                            return;
                        }
                        /* if(GMCommand.ShutdownTime.this.minutesLeft == 1){
                         try {
                         for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                         for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                         chr.saveToDB(true, true);
                         }
                         }
                         System.out.println("服务端关闭保存雇佣事件  开始...");
                         for (handling.channel.ChannelServer cserv : handling.channel.ChannelServer.getAllInstances()) {
                         try {
                         Thread.sleep(2000L);//2ms
                         } catch (InterruptedException ex) {//捕获该线程异常
                         System.out.println("线程锁开启失败：" + ex);
                         }
                         cserv.closeAllMerchants();
                         }
                         System.out.println("服务端关闭保存雇佣事件  已完成...");
                         } catch (Exception e) {
                         System.out.println("关闭服务端保存雇佣错误 - " + e);
                         }
                         }*/
                        //   World.Broadcast.broadcastMessage(UIPacket.clearMidMsg());
                        // World.Broadcast.broadcastMessage(UIPacket.getMidMsg("游戏将于 " + GMCommand.ShutdownTime.this.minutesLeft + " 分钟之后关闭维护.请玩家安全下线.", true, 0));
                        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, " 游戏将于 " + GMCommand.ShutdownTime.this.minutesLeft + " 分钟之后关闭维护.请玩家安全下线."));
                        //AdminCommand.ShutdownTime.access$010(AdminCommand.ShutdownTime.this);
                        minutesLeft--;
                    }
                }, 60000);
            } else {
                c.getPlayer().dropMessage(6, "已经使用过一次这个命令，暂时无法使用.");
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class Shutdown extends CommandExecute {

        /**
         *
         */
        protected static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            c.getPlayer().dropMessage(6, "游戏即将关闭...");
            if ((t == null) || (!t.isAlive())) {
                t = new Thread(ShutdownServer.getInstance());
                ShutdownServer.getInstance().shutdown();
                t.start();
            } else {
                c.getPlayer().dropMessage(6, "已经使用过一次这个命令，暂时无法使用.");
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class GetSkill extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            ISkill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
            byte level = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
            byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1);

            if (level > skill.getMaxLevel()) {
                level = (byte) skill.getMaxLevel();
            }
            if (masterlevel > skill.getMaxLevel()) {
                masterlevel = (byte) skill.getMaxLevel();
            }
            c.getPlayer().changeSkillLevel(skill, level, masterlevel);
            return 1;
        }
    }

    /**
     *
     */
    public static class 封包调试 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.StartWindow();
            return 1;
        }
    }

    /**
     *
     */
    public static class 删除克隆 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (FakeCharacter fc : c.getPlayer().getFakeChars()) {
                if (fc.getFakeChar().getMap() == c.getPlayer().getMap()) {
                    c.getChannelServer().getAllClones().remove(fc);
                    c.getPlayer().getMap().removePlayer(fc.getFakeChar());
                }
            }
            c.getPlayer().getFakeChars().clear();
            c.getPlayer().dropMessage("你已经删除了魔宠.");
            return 1;
        }
    }

    /**
     *
     */
    public static class 克隆测试 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "最好别乱搞！.");
                return 0;
            }
            //   int 数量 = Integer.parseInt(splitted[1]);
            int 类型 = Integer.parseInt(splitted[1]);
            if (c.getPlayer().getFakeChars().size() > 1) {
                c.getPlayer().dropMessage("元神数量最大为1个.");
            } else {
                for (int i = 0; i < 1 && i + c.getPlayer().getFakeChars().size() < 1; i++) {
                    FakeCharacter fc = new FakeCharacter(c.getPlayer(), c.getPlayer().getId() + c.getPlayer().getFakeChars().size() + 1 + i, 类型); //FakeCharacter fc =
                    new FakeCharacter(c.getPlayer(), c.getPlayer().getId(), 类型);
                    c.getPlayer().getFakeChars().add(fc);
                    c.getChannelServer().addClone(fc);
                }
                c.getPlayer().dropMessage("你的元神数量 " + c.getPlayer().getFakeChars().size() + "个分身.");
            }

            return 1;
        }
    }

    /**
     *
     */
    public static class 时间调试 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int year = Calendar.getInstance().get(Calendar.YEAR);//年
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;//月
            int date = Calendar.getInstance().get(Calendar.DATE);//日
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);//小时
            int minute = Calendar.getInstance().get(Calendar.MINUTE);//分钟
            int second = Calendar.getInstance().get(Calendar.SECOND); //毫秒
            c.getPlayer().dropMessage(6, year + ":年 " + month + ":月 " + date + ":日 " + hour + ":小时 " + minute + ":分钟 " + second + ":毫秒");
            System.out.println(year + ":年 " + month + ":月 " + date + ":日 " + hour + ":小时 " + minute + ":分钟 " + second + ":毫秒");
            return 1;
        }
    }

    /**
     *
     */
    public static class ItemCheck extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3 || splitted[1] == null || splitted[1].isEmpty() || splitted[2] == null || splitted[2].isEmpty()) {
                c.getPlayer().dropMessage(6, "!itemcheck <playername> <itemid>");
                return 0;
            } else {
                int item = Integer.parseInt(splitted[2]);
                MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                int itemamount = chr.getItemQuantity(item, true);
                if (itemamount > 0) {
                    c.getPlayer().dropMessage(6, chr.getName() + " has " + itemamount + " (" + item + ").");
                } else {
                    c.getPlayer().dropMessage(6, chr.getName() + " doesn't have (" + item + ")");
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class Marry extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "指令規則 <name> <itemid>");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[2]);
            if (!GameConstants.isEffectRing(itemId)) {
                c.getPlayer().dropMessage(6, "錯誤的物品ID.");
            } else {
                MapleCharacter fff = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (fff == null) {
                    c.getPlayer().dropMessage(6, "玩家必須上線");
                } else {
                    int[] ringID = {MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};
                    try {
                        MapleCharacter[] chrz = {fff, c.getPlayer()};
                        for (int i = 0; i < chrz.length; i++) {
                            Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId);
                            if (eq == null) {
                                c.getPlayer().dropMessage(6, "錯誤的物品ID.");
                                return 0;
                            }
                            eq.setUniqueId(ringID[i]);
                            MapleInventoryManipulator.addbyItem(chrz[i].getClient(), eq.copy());
                            chrz[i].dropMessage(6, "成功與  " + chrz[i == 0 ? 1 : 0].getName() + " 結婚");
                        }
                        MapleRing.addToDB(itemId, c.getPlayer(), fff.getName(), fff.getId(), ringID);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class CloneMe extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().cloneLook();
            return 1;
        }
    }

    /**
     *
     */
    public static class DisposeClones extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, c.getPlayer().getCloneSize() + " clones disposed.");
            c.getPlayer().disposeClones();
            return 1;
        }
    }

    /**
     *
     */
    public static class 重载跑商 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().resetGamePointsPS();
            return 1;
        }
    }

    /**
     *
     */
    public static class 包头 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            int packetheader = Integer.parseInt(splitted[1]);
            String packet_in = " 00 00 00 00 00 00 00 00 00 ";
            if (splitted.length > 2) {
                packet_in = StringUtil.joinStringFrom(splitted, 2);
            }

            mplew.writeShort(packetheader);
            mplew.write(HexTool.getByteArrayFromHexString(packet_in));
            mplew.writeZeroBytes(20);
            c.getSession().write(mplew.getPacket());
            c.getPlayer().dropMessage(packetheader + "已传送封包[" + mplew.getPacket().getBytes().length + "] : " + mplew.toString());
            return 1;
        }
    }

    /**
     *
     */
    public static class 包 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            int packetheader = Integer.parseInt(splitted[1]);
            //String packet_in = " 00 00 00 00 00 00 00 00 00 ";
            String packet_in = splitted[2];
            if (splitted.length > 2) {
                packet_in = StringUtil.joinStringFrom(splitted, 2);
            }

            mplew.writeShort(packetheader);
            //mplew.write(HexTool.getByteArrayFromHexString("01 03"));
            mplew.write(HexTool.getByteArrayFromHexString(packet_in));
            //  mplew.writeZeroBytes(20);
            c.getSession().write(mplew.getPacket());
            c.getPlayer().dropMessage(packetheader + "已传送封包[" + mplew.getPacket().getBytes().length + "] : " + mplew.toString());
            return 1;
        }
    }

    /**
     *
     */
    public static class 开启活动副本 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {

            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: !开启活动副本 <副本频道> <类型1-6>");
                return 0;
            }
            int channel = Integer.parseInt(splitted[1]);
            int lx = Integer.parseInt(splitted[2]);
            final EventManager em = c.getChannelServer().getEventSM().getEventManager("AutomatedEvent");
            if (em != null) {
                em.scheduleRandomEventInChannel(c.getPlayer(), channel, lx);
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 公告 extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            String note = splitted[1];
            int item = Integer.parseInt(splitted[2]);
            for (ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (item > 0) {
                        mch.startMapEffect(note, item);
                    }
                    mch.dropMessage(note);
                    mch.dropMessage(-1, note);
                }
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 临时NPC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(c.getPlayer().getPosition().y);
                npc.setRx0(c.getPlayer().getPosition().x + 50);
                npc.setRx1(c.getPlayer().getPosition().x - 50);
                npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                npc.setCustom(true);
                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
            } else {
                c.getPlayer().dropMessage(6, "You have entered an invalid NPC ID");
                return 0;
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class 永久NPC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(6, "!pnpc [npcid]");
                return 0;
            }
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                final int xpos = c.getPlayer().getPosition().x;
                final int ypos = c.getPlayer().getPosition().y;
                final int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos);
                npc.setRx1(xpos);
                npc.setFh(fh);
                npc.setCustom(true);
                try {
                    Connection con = (Connection) DatabaseConnection.getConnection();
                    try (PreparedStatement ps = (PreparedStatement) con.prepareStatement("INSERT INTO spawns (idd, f, hide, fh, cy, rx0, rx1, type, x, y, mid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, npcId);
                        ps.setInt(2, 0); // 1 = right , 0 = left
                        ps.setInt(3, 0); // 1 = hide, 0 = show
                        ps.setInt(4, fh);
                        ps.setInt(5, ypos);
                        ps.setInt(6, xpos);
                        ps.setInt(7, xpos);
                        ps.setString(8, "n");
                        ps.setInt(9, xpos);
                        ps.setInt(10, ypos);
                        ps.setInt(11, c.getPlayer().getMapId());
                        ps.executeUpdate();
                    }
                } catch (SQLException e) {
                    c.getPlayer().dropMessage(6, "Failed to save NPC to the database");
                }
                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
                c.getPlayer().dropMessage(6, "Please do not reload this map or else the NPC will disappear till the next restart.");
            } else {
                c.getPlayer().dropMessage(6, "You have entered an invalid Npc-Id");
                return 0;
            }
            return 1;
        }
    }

    /**
     *
     */
    public static class ClearInv extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isAdmin()) {
                return 0;
            }
            java.util.Map<Pair<Short, Short>, MapleInventoryType> eqs = new ArrayMap<>();
            switch (splitted[1]) {
                case "全部":
                    for (MapleInventoryType type : MapleInventoryType.values()) {
                        for (IItem item : c.getPlayer().getInventory(type)) {
                            eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), type);
                        }
                    }
                    break;
                case "已裝備道具":
                    for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIPPED);
                    }
                    break;
                case "武器":
                    for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
                    }
                    break;
                case "消耗":
                    for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
                    }
                    break;
                case "裝飾":
                    for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
                    }
                    break;
                case "其他":
                    for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
                    }
                    break;
                case "特殊":
                    for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
                    }
                    break;
                default:
                    c.getPlayer().dropMessage(6, "[全部/已裝備道具/武器/消耗/裝飾/其他/特殊]");
                    break;
            }
            for (Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
                MapleInventoryManipulator.removeFromSlot(c, eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
            }
            return 1;
        }
    }
}
