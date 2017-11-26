
package tools;

import java.awt.Point;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import client.inventory.MapleMount;
import client.BuddylistEntry;
import client.inventory.IItem;
import constants.GameConstants;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.MapleKeyLayout;
import client.inventory.MaplePet;
import client.MapleQuestStatus;
import client.MapleStat;
import client.inventory.IEquip.ScrollResult;
import client.MapleDisease;
import client.inventory.MapleRing;
import client.SkillMacro;
import client.inventory.*;
import handling.ByteArrayMaplePacket;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import constants.ServerConstants;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.channel.MapleGuildRanking.GuildRankingInfo;
import handling.channel.handler.Beans;
import handling.channel.handler.InventoryHandler;
import handling.world.World;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleGuildAlliance;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import server.MapleItemInformationProvider;
import server.MapleShopItem;
import server.MapleStatEffect;
import server.MapleTrade;
import server.MapleDueyActions;
import server.Randomizer;
import server.ServerProperties;
import server.life.SummonAttackEntry;
import server.maps.MapleSummon;
import server.life.MapleNPC;
import server.life.PlayerNPC;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleMist;
import server.maps.MapleMapItem;
import server.events.MapleSnowball.MapleSnowballs;
import server.life.MapleMonster;
import server.maps.MapleDragon;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import server.movement.LifeMovementFragment;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.PacketHelper;

/**
 *
 * @author zjj
 */
public class MaplePacketCreator {

    /**
     *
     */
    public final static List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

    private static final byte[] CHAR_INFO_MAGIC = {-1, -55, -102, 59};

    ServerConstants ERROR = new ServerConstants();

    /**
     *
     * @param port
     * @param clientId
     * @return
     */
    public static final MaplePacket getServerIP(final int port, final int clientId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getServerIP--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        try {
            mplew.write(InetAddress.getByName(ServerProperties.getProperty("KinMS.IP")).getAddress());
            // System.out.println(InetAddress.getByName(ServerProperties.getProperty("KinMS.IP")).getAddress());
            //mplew.write(InetAddress.getByName(ServerConstants.Gateway_IP_String).getAddress());
        } catch (UnknownHostException e) {
            //  System.out.println("登录服务器IP："+e);
            //mplew.write(ServerConstants.Gateway_IP);
        }
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[]{1, 0, 0, 0, 0});

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getServerIP-124" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param port
     * @return
     */
    public static final MaplePacket getChannelChange(final int port) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getChannelChange--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        try {
            mplew.write(InetAddress.getByName(ServerProperties.getProperty("KinMS.IP")).getAddress());
            //   System.out.println(InetAddress.getByName(ServerProperties.getProperty("KinMS.IP")).getAddress());
            //mplew.write(InetAddress.getByName(ServerConstants.Gateway_IP_String).getAddress());
        } catch (UnknownHostException e) {
            //  System.out.println("频道服务器IP："+e);
            //mplew.write(ServerConstants.Gateway_IP);
        }
        mplew.writeShort(port);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setChannel(1);
            //ERROR.setPACKET_ERROR("getChannelChange-149" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @return
     */
    public static final MaplePacket getCharInfo(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getCharInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);

        chr.CRand().connectData(mplew); // Random number generator

        PacketHelper.addCharacterInfo(mplew, chr);

        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getCharInfo-175" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static final MaplePacket enableActions() {
        if (ServerConstants.调试输出封包) {
            System.out.println("enableActions--------------------");
        }
        return updatePlayerStats(EMPTY_STATUPDATE, true, 0);
    }

    /**
     *
     * @param stats
     * @param evan
     * @return
     */
    public static final MaplePacket updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final int evan) {
        if (ServerConstants.调试输出封包) {
            System.out.println("updatePlayerStatsA--------------------");
        }
        return updatePlayerStats(stats, false, evan);
    }

    /**
     *
     * @param stats
     * @param itemReaction
     * @param evan
     * @return
     */
    public static final MaplePacket updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final boolean itemReaction, final int evan) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updatePlayerStats--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (final Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {

                @Override
                public int compare(final Pair<MapleStat, Integer> o1, final Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);

        for (final Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    mplew.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x80) {
                    mplew.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() < 0x40000) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate.getRight());
                }
            }
        }
        mplew.writeShort(0); //增加
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updatePlayerStats-238" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket blockedPortal() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("blockedPortal--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(1);
        mplew.writeInt(0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("blockedPortal-253" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket weirdStatUpdate() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("weirdStatUpdate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0);
        mplew.write(56);
        mplew.writeShort(0);
        mplew.writeLong(0L);
        mplew.writeLong(0L);
        mplew.writeLong(0L);
        mplew.write(0);
        mplew.write(1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("weirdStatUpdate-276" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param itemReaction
     * @return
     */
    public static final MaplePacket updateSp(MapleCharacter chr, final boolean itemReaction) { //this will do..
        if (ServerConstants.调试输出封包) {
            System.out.println("updateSpA--------------------");
        }
        return updateSp(chr, itemReaction, false);
    }

    /**
     *
     * @param chr
     * @param itemReaction
     * @param overrideJob
     * @return
     */
    public static final MaplePacket updateSp(MapleCharacter chr, final boolean itemReaction, final boolean overrideJob) { //this will do..
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("updateSp--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        mplew.writeInt(0x20000);
        /*  if (overrideJob || GameConstants.isEvan(chr.getJob()) || GameConstants.isResist(chr.getJob())) {
         mplew.write(chr.getRemainingSpSize());
         for (int i = 0; i < chr.getRemainingSps().length; i++) {
         if (chr.getRemainingSp(i) > 0) {
         mplew.write(i + 1);
         mplew.write(chr.getRemainingSp(i));
         }
         }
         } else {*/
        mplew.writeShort(chr.getRemainingSp());
        mplew.writeShort(0); //增加
        //   }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateSp-310" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();

    }

    /**
     *
     * @param to
     * @param spawnPoint
     * @param chr
     * @return
     */
    public static final MaplePacket getWarpToMap(final MapleMap to, final int spawnPoint, final MapleCharacter chr) {

        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("getWarpToMap--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(3);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getStat().getHp());
        //  mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getWarpToMap-336" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param portal
     * @return
     */
    public static final MaplePacket instantMapWarp(final byte portal) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("instantMapWarp--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        mplew.write(0);
        mplew.write(portal); // 6

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("instantMapWarp-353" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param townId
     * @param targetId
     * @param skillId
     * @param pos
     * @return
     */
    public static final MaplePacket spawnPortal(final int townId, final int targetId, final int skillId, final Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnPortal--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
//        mplew.writeInt(skillId);
        if (pos != null) {
            mplew.writePos(pos);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnPortal-374" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param pos
     * @param town
     * @return
     */
    public static final MaplePacket spawnDoor(final int oid, final Point pos, final boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnDoor--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(town ? 1 : 0);
        mplew.writeInt(oid);
        mplew.writePos(pos);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnDoor-392" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param town
     * @return
     */
    public static MaplePacket removeDoor(int oid, boolean town) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeDoor--------------------");
        }
        if (town) {
            mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
        } else {
            mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
            mplew.write(/*town ? 1 : */0);
            mplew.writeInt(oid);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeDoor-415" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param summon
     * @param animated
     * @return
     */
    public static MaplePacket spawnSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnSummon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        //  mplew.write(114);
        mplew.write(summon.getOwnerLevel());
        mplew.write(summon.getSkillLevel()); //idk but nexon sends 1 for octo, so we'll leave it
        mplew.writeShort(summon.getPosition().x);
        mplew.writeInt(summon.getPosition().y);
        //1mplew.writePos(summon.getPosition());
        //   mplew.write(4); //reaper = 5?
        //  mplew.writeShort(0/*summon.getFh()*/);
        //  mplew.write(31);
        mplew.write(0);
        mplew.write(summon.getMovementType().getValue());
        mplew.write(summon.getSummonType()); // 0 = Summon can't attack - but puppets don't attack with 1 either ^.-
        //  mplew.write(0/*animated ? 0 : 1*/);
        mplew.write(animated ? 0 : 1);
        //final MapleCharacter chr = summon.getOwner();
        /*        mplew.write(summon.getSkill() == 4341006 && chr != null ? 1 : 0); //mirror target
         if (summon.getSkill() == 4341006 && chr != null) {
         PacketHelper.addCharLook(mplew, chr, true);
         }*/

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnSummon-452" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param summon
     * @param animated
     * @return
     */
    public static MaplePacket removeSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeSummon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeSummon-470" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket getRelogResponse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        if (ServerConstants.调试输出封包) {
            System.out.println("getRelogResponse--------------------");
        }
        mplew.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
        mplew.write(1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getRelogResponse-486" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     * Possible values for <code>type</code>:<br>
     * 1: You cannot move that channel. Please try again later.<br>
     * 2: You cannot go into the cash shop. Please try again later.<br>
     * 3: The Item-Trading shop is currently unavailable, please try again
     * later.<br>
     * 4: You cannot go into the trade shop, due to the limitation of user
     * count.<br>
     * 5: You do not meet the minimum level requirement to access the Trade
     * Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static MaplePacket serverBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("serverBlocked--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
        mplew.write(type);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("serverBlocked-516" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param message
     * @return
     */
    public static MaplePacket serverMessage(String message) {
        if (ServerConstants.调试输出封包) {
            System.out.println("serverMessageA--------------------");
        }
        return serverMessage(4, 0, message, false);
    }

    /**
     *
     * @param type
     * @param message
     * @return
     */
    public static MaplePacket serverNotice(int type, String message) {
        if (ServerConstants.调试输出封包) {
            System.out.println("serverNoticeA--------------------");
        }
        return serverMessage(type, 0, message, false);
    }

    /**
     *
     * @param type
     * @param channel
     * @param message
     * @return
     */
    public static MaplePacket serverNotice(int type, int channel, String message) {
        if (ServerConstants.调试输出封包) {
            System.out.println("serverNoticeB--------------------");
        }
        return serverMessage(type, channel, message, false);
    }

    /**
     *
     * @param type
     * @param channel
     * @param message
     * @param smegaEar
     * @return
     */
    public static MaplePacket serverNotice(int type, int channel, String message, boolean smegaEar) {
        if (ServerConstants.调试输出封包) {
            System.out.println("serverNoticeC--------------------");
        }
        return serverMessage(type, channel, message, smegaEar);
    }

    private static MaplePacket serverMessage(int type, int channel, String message, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("serverMessage--------------------");
        }

        /*0：[公告]
         1：弹出
         2：超级电话
         3：超级大电话
         4：在顶部滚动消息
         5：粉红的全文
         6：蓝色光芒的全文：
         8：项大型电话：
         9：心大电话
         10：颅骨的超级电话
         11：绿色超级手机的消息吗？
         12：三大全文的电话线
         13：端部角=。=“
         14：绿色gachapon盒
         15：为了芦笋盒
         18：蓝色的公告（一次）*/
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);

        switch (type) {
            case 3:
            case 9:
            case 10:
            case 11:
            case 12:
                mplew.write(channel - 1); // channel
                mplew.write(megaEar ? 1 : 0);
                break;
            case 6:
            case 18:
                mplew.writeInt(channel >= 1000000 && channel < 6000000 ? channel : 0); //cash itemID, displayed in yellow by the {name}
                //E.G. All new EXP coupon {Ruby EXP Coupon} is now available in the Cash Shop!
                //with Ruby Exp Coupon being in yellow and with item info
                break;
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("serverMessage-596" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param name
     * @param message
     * @param item
     * @param rareness
     * @param channel
     * @return
     */
    public static MaplePacket getGachaponMega(final String name, final String message, final IItem item, final byte rareness, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getGachaponMega--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(/*rareness == 2 ? 15 : */14);
        mplew.writeMapleAsciiString(name + message);
        mplew.writeInt(channel - 1); // 0~3 i think
        //mplew.writeMapleAsciiString(name);
        PacketHelper.addItemInfo(mplew, item, true, true);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getGachaponMega-616" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param message
     * @param ear
     * @param channel
     * @return
     */
    public static MaplePacket tripleSmega(List<String> message, boolean ear, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("tripleSmega--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(10);

        if (message.get(0) != null) {
            mplew.writeMapleAsciiString(message.get(0));
        }
        mplew.write(message.size());
        for (int i = 1; i < message.size(); i++) {
            if (message.get(i) != null) {
                mplew.writeMapleAsciiString(message.get(i));
            }
        }
        mplew.write(channel - 1);
        mplew.write(ear ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("tripleSmega-644" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param channel
     * @param itemId
     * @param message
     * @param ear
     * @return
     */
    public static MaplePacket getAvatarMega(MapleCharacter chr, int channel, int itemId, String message, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getAvatarMega--------------------");
        }
        mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(channel - 1); // channel
        mplew.write(ear ? 1 : 0);
        PacketHelper.addCharLook(mplew, chr, true);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getAvatarMega-665" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @param whisper
     * @param channel
     * @param item
     * @return
     */
    public static MaplePacket itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("itemMegaphone--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);

        if (item == null) {
            mplew.write(0);
        } else {
            PacketHelper.addItemInfo(mplew, item, false, false, true);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("itemMegaphone-689" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param life
     * @param show
     * @return
     */
    public static MaplePacket spawnNPC(MapleNPC life, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnNPC--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(show ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnNPC-713" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param objectid
     * @return
     */
    public static MaplePacket removeNPC(final int objectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeNPC--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
        mplew.writeInt(objectid);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeNPC-729" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param life
     * @param MiniMap
     * @return
     */
    public static MaplePacket spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnNPCRequestController--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(MiniMap ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnNPCRequestController-754" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @return
     */
    public static MaplePacket spawnPlayerNPC(PlayerNPC npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("spawnPlayerNPC--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        mplew.write(npc.getF() == 1 ? 0 : 1);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(npc.getGender());
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<>();
        for (Entry<Byte, Integer> position : equip.entrySet()) {
            byte pos = (byte) (position.getKey() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, position.getValue());
            } else if ((pos > 100 || pos == -128) && pos != 111) { // don't ask. o.o
                pos = (byte) (pos == -128 ? 28 : pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, position.getValue());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, position.getValue());
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(npc.getPet(i));
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnPlayerNPC-812" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cidfrom
     * @param text
     * @param whiteBG
     * @param show
     * @return
     */
    public static MaplePacket getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getChatText--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getChatText-831" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param value
     * @return
     */
    public static MaplePacket GameMaster_Func(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("GameMaster_Func--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZeroBytes(17);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("GameMaster_Func-848" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param value
     * @return
     */
    public static MaplePacket testCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("testCombo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(value);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("testCombo-864" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param hex
     * @return
     */
    public static MaplePacket getPacketFromHexString(String hex) {
        if (ServerConstants.调试输出封包) {
            System.out.println("getPacketFromHexString--------------------");
        }
        return new ByteArrayMaplePacket(HexTool.getByteArrayFromHexString(hex));
    }

    /**
     *
     * @param gain
     * @param white
     * @param partyinc
     * @param Class_Bonus_EXP
     * @param Equipment_Bonus_EXP
     * @param Premium_Bonus_EXP
     * @param 结婚经验
     * @return
     */
    public static final MaplePacket GainEXP_Monster(final int gain, final boolean white, final int partyinc, final int Class_Bonus_EXP, final int Equipment_Bonus_EXP, final int Premium_Bonus_EXP, final int 结婚经验) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("GainEXP_Monster--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.write(white ? 1 : 0);//1是开 0是关
        mplew.writeInt(gain);
        mplew.write(0); // Not in chat
        // mplew.writeInt(inChat ? 活动经验 : 活动经验);
        mplew.writeInt(0); // Event Bonus
        mplew.writeShort(0);

        // mplew.writeInt(0); //wedding bonus
        mplew.writeInt(结婚经验); //party ring bonus结婚经验
        mplew.write(0);
        mplew.writeInt(partyinc); // Party size组队经验
        mplew.writeInt(Equipment_Bonus_EXP); //Equipment Bonus EXP道具佩戴附加经验
        mplew.writeInt(Premium_Bonus_EXP); // Premium bonus EXP网吧经验
//        mplew.writeInt(0); //Rainbow Week Bonus EXP
        // mplew.writeInt(Class_Bonus_EXP); // Class bonus EXP
//        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("GainEXP_Monster-903" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gain
     * @param inChat
     * @param white
     * @return
     */
    public static final MaplePacket GainEXP_Others(final int gain, final boolean inChat, final boolean white) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("GainEXP_Others--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(0);
        mplew.writeInt(inChat ? 1 : 0); // monster book bonus
        //  mplew.write(0); // Party percentage
        mplew.writeShort(0); // Party bouns
        mplew.writeZeroBytes(4);

        if (inChat) {
            mplew.writeZeroBytes(13); // some ring bonus/ party exp ??
        } else { // some ring bonus/ party exp
            mplew.writeZeroBytes(13); // Item equip bonus EXP
        }
        //mplew.writeZeroBytes(4); // Class bonus EXP
//        mplew.writeInt(0);
//        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("GainEXP_Others-935" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gain
     * @return
     */
    public static final MaplePacket getShowFameGain(final int gain) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getShowFameGain--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getShowFameGain-952" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gain
     * @param inChat
     * @return
     */
    public static final MaplePacket showMesoGain(final int gain, final boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showMesoGain--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.write(1);
            mplew.write(0);
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showMesoGain-976" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemId
     * @param quantity
     * @return
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity) {
        if (ServerConstants.调试输出封包) {
            System.out.println("getShowItemGainA--------------------");
        }
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     *
     * @param itemId
     * @param quantity
     * @param inChat
     * @return
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getShowItemGain--------------------");
        }
        if (inChat) {
            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1); // item count
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            /*	    for (int i = 0; i < count; i++) { // if ItemCount is handled.
             mplew.writeInt(itemId);
             mplew.writeInt(quantity);
             }*/
        } else {
            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getShowItemGain-1014" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemId
     * @param effect
     * @return
     */
    public static MaplePacket showRewardItemAnimation(int itemId, String effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showRewardItemAnimationA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x0B);
        mplew.writeInt(itemId);
        mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showRewardItemAnimationA-1035" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemId
     * @param effect
     * @param from_playerid
     * @return
     */
    public static MaplePacket showRewardItemAnimation(int itemId, String effect, int from_playerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showRewardItemAnimationB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(0x0B);
        mplew.writeInt(itemId);
        mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showRewardItemAnimationB-1057" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param drop
     * @param dropfrom
     * @param dropto
     * @param mod
     * @return
     */
    public static MaplePacket dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("dropItemFromMapObject--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod); // 1 animation, 2 no animation, 3 spawn disappearing item [Fade], 4 spawn disappearing item
        mplew.writeInt(drop.getObjectId()); // item owner id
        mplew.write(drop.getMeso() > 0 ? 1 : 0); // 1 mesos, 0 item, 2 and above all item meso bag,
        mplew.writeInt(drop.getItemId()); // drop object ID
        mplew.writeInt(drop.getOwner()); // owner charid
        mplew.write(drop.getDropType()); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
        mplew.writePos(dropto);
        mplew.writeInt(0);

        if (mod != 2) {
            mplew.writePos(dropfrom);
            //  mplew.writeShort(0);
        }
        mplew.write(0);
        if (mod != 2) {
            mplew.write(0);
            mplew.write(1);
        }
        //if (drop.getMeso() == 0) {
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        //  mplew.writeShort(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("dropItemFromMapObject-1095" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @return
     */
    public static MaplePacket spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnPlayerMapobject--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeMapleAsciiString(chr.getName());

        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("");
            mplew.write(new byte[6]);
        } else {
            final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeMapleAsciiString("");
                mplew.write(new byte[6]);
            }
        }
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0xE0);
        mplew.write(0x1F);
        mplew.write(0);
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            mplew.writeInt(2);
        } else {
            mplew.writeInt(0);
        }
        long buffmask = 0L;
        Integer buffvalue = null;
        if ((chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) && (!chr.isHidden())) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffvalue = chr.getBuffedValue(MapleBuffStat.COMBO);
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            buffvalue = chr.getBuffedValue(MapleBuffStat.MORPH);
        }
        if (chr.getBuffedValue(MapleBuffStat.能量) != null) {
            buffmask |= MapleBuffStat.能量.getValue();
            buffvalue = chr.getBuffedValue(MapleBuffStat.能量);
        }//AREN'T THESE 
        mplew.writeInt((int) (buffmask >> 32 & 0xFFFFFFFF));
        if (buffvalue != null) {
            if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
                mplew.writeShort(buffvalue);
            } else {
                mplew.write(buffvalue.byteValue());
            }
        }
        final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        mplew.writeInt((int) (buffmask & 0xFFFFFFFF));
        mplew.write(new byte[6]);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0); //start of Monster Riding
        mplew.write(0);
        IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
        if ((chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) && (mount != null)) {
            mplew.writeInt(mount.getItemId());
            mplew.writeInt(1004);
            mplew.writeInt(0x01261F00);
            mplew.write(0);
        } else {
            mplew.writeInt(CHAR_MAGIC_SPAWN);
            mplew.writeLong(0L);
            mplew.write(0);
        }
        mplew.writeLong(0L);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.write(1);
        mplew.write(0x41);
        mplew.write(0x9A);
        mplew.write(0x70);
        mplew.write(7);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.writeShort(chr.getJob());
        PacketHelper.addCharLook(mplew, chr, false);
        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); //max is like 100. but w/e
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(0); // 只有台陸版有
        mplew.writeInt(-1); // 只有台陸版有
        mplew.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
        mplew.writePos(chr.getPosition());
        mplew.write(chr.getStance());
        mplew.writeShort(0); // FH
        mplew.write(0);
        mplew.writeInt(chr.getMount().getLevel()); // mount lvl
        mplew.writeInt(chr.getMount().getExp()); // exp
        mplew.writeInt(chr.getMount().getFatigue()); // tiredness
        PacketHelper.addAnnounceBox(mplew, chr);
        mplew.write(chr.getChalkboard() != null && chr.getChalkboard().length() > 0 ? 1 : 0);
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
// 戒指开始
//        mplew.write(0);
//        mplew.writeInt(0);
//        mplew.write(0);
//        mplew.writeInt(0);
//        mplew.write((byte) 0);
//        mplew.writeShort(0);
        Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        List<MapleRing> allrings = rings.getLeft();
        allrings.addAll(rings.getRight());
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        addMarriageRingLook(mplew, chr);
// 戒指结束        
        mplew.writeShort(0);// mplew.write(0); // 台版自己加的
        if (chr.getCarnivalParty() != null) {
            mplew.write(chr.getCarnivalParty().getTeam());
        } else if (chr.getMapId() == 109080000 || chr.getMapId() == 109080010) {
            mplew.write(chr.getCoconutTeam()); //is it 0/1 or is it 1/2?
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setRemovePlayerFromMap(1);
            // ERROR.setPACKET_ERROR("spawnPlayerMapobject-1356" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param 类型
     * @return
     */
    public static MaplePacket KspawnPlayerMapobject(MapleCharacter chr, int 类型) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        String 魔宠名字 = null;

        if (类型 == 0) {
            魔宠名字 = "≤克隆≥";
        } else if (类型 == 1) {
            魔宠名字 = "≤花蘑菇≥";
        } else if (类型 == 2) {
            魔宠名字 = "≤漂漂猪≥";
        } else if (类型 == 3) {
            魔宠名字 = "≤白外星人≥";
        } else if (类型 == 4) {
            魔宠名字 = "≤龙族≥";
        } else if (类型 == 5) {
            魔宠名字 = "≤提干≥";
        } else if (类型 == 6) {
            魔宠名字 = "≤彩色蜗牛≥";
        } else if (类型 == 7) {
            魔宠名字 = "≤幽灵≥";
        } else if (类型 == 8) {
            魔宠名字 = "≤苏菲莉亚丢弃的人偶≥";
        } else if (类型 == 9) {
            魔宠名字 = "≤超人Ⅰ ≥";
        } else if (类型 == 10) {
            魔宠名字 = "≤超人Ⅱ≥";
        } else if (类型 == 11) {
            魔宠名字 = "≤老鼠变身≥";
        } else if (类型 == 12) {
            魔宠名字 = "≤屈原≥";
        } else if (类型 == 13) {
            魔宠名字 = "≤白色兔子≥";
        } else if (类型 == 14) {
            魔宠名字 = "≤粉红兔子≥";
        } else if (类型 == 15) {
            魔宠名字 = "≤佩托≥";
        } else if (类型 == 16) {
            魔宠名字 = "≤宝贝龙≥";
        } else if (类型 == 17) {
            魔宠名字 = "≤电子狗≥";
        } else if (类型 == 18) {
            魔宠名字 = "≤银色雪犬≥";
        } else if (类型 == 19) {
            魔宠名字 = "≤箱子≥";
        } else if (类型 == 20) {
            魔宠名字 = "≤圣诞雪人≥";
        } else if (类型 == 21) {
            魔宠名字 = "≤月牙牛魔王≥";
        } else if (类型 == 22) {
            魔宠名字 = "≤蓝枪牛魔王≥";
        } else if (类型 == 23) {
            魔宠名字 = "≤温顺的牛≥";
        } else if (类型 == 24) {
            魔宠名字 = "≤凶恶的牛≥";
        } else if (类型 == 25) {
            魔宠名字 = "≤黄金猪猪≥";
        } else if (类型 == 26) {
            魔宠名字 = "≤匹诺曹的爷爷≥";
        } else if (类型 == 27) {
            魔宠名字 = "≤匹诺曹≥";
        } else if (类型 == 28) {
            魔宠名字 = "≤外星人≥";
        } else if (类型 == 29) {
            魔宠名字 = "≤战神地图NPCA≥";
        } else if (类型 == 30) {
            魔宠名字 = "≤战神地图NPCB≥";
        } else if (类型 == 31) {
            魔宠名字 = "≤战神地图NPCC≥";
        } else if (类型 == 32) {
            魔宠名字 = "≤战神地图NPCD≥";
        } else if (类型 == 33) {
            魔宠名字 = "≤战神地图NPCE≥";
        } else if (类型 == 34) {
            魔宠名字 = "≤圣诞雪人≥";
        } else if (类型 == 35) {
            魔宠名字 = "≤农民≥";
        } else if (类型 == 36) {
            魔宠名字 = "≤保洁阿姨≥";
        } else if (类型 == 37) {
            魔宠名字 = "≤熊孩子≥";
        } else if (类型 == 38) {
            魔宠名字 = "≤老绅士≥";
        } else if (类型 == 39) {
            魔宠名字 = "≤机械装甲车≥";
        } else if (类型 == 40) {
            魔宠名字 = "≤小蜗牛≥";
        } else if (类型 == 41) {
            魔宠名字 = "≤小雪娃娃≥";
        } else if (类型 == 42) {
            魔宠名字 = "≤郁闷的绿蘑菇≥";
        } else if (类型 == 43) {
            魔宠名字 = "≤蝙蝠≥";
        } else if (类型 == 44) {
            魔宠名字 = "≤警用飞艇≥";
        } else if (类型 == 45) {
            魔宠名字 = "≤大头蛇≥";
        } else if (类型 == 46) {
            魔宠名字 = "≤猴子≥";
        } else if (类型 == 47) {
            魔宠名字 = "≤蜗牛王≥";
        } else if (类型 == 48) {
            魔宠名字 = "≤蓝色玩具老鼠≥";
        } else if (类型 == 48) {
            魔宠名字 = "≤紫色闹钟怪≥";
        } else if (类型 == 50) {
            魔宠名字 = "≤方块人≥";
        } else if (类型 == 51) {
            魔宠名字 = "≤小雪怪≥";
        } else if (类型 == 52) {
            魔宠名字 = "≤老仙人掌≥";
        } else if (类型 == 53) {
            魔宠名字 = "≤滑雪企鹅≥";
        } else if (类型 == 54) {
            魔宠名字 = "≤绿龙≥";
        } else if (类型 == 55) {
            魔宠名字 = "≤恶魔猫≥";
        } else if (类型 == 56) {
            魔宠名字 = "≤小老虎≥";
        } else if (类型 == 57) {
            魔宠名字 = "≤海螺王≥";
        } else if (类型 == 58) {
            魔宠名字 = "≤狐狸≥";
        } else if (类型 == 59) {
            魔宠名字 = "≤石头人≥";
        } else if (类型 == 60) {
            魔宠名字 = "≤仙猫≥";
        } else if (类型 == 61) {
            魔宠名字 = "≤工地熊≥";
        } else if (类型 == 62) {
            魔宠名字 = "≤大力外星人≥";
        } else if (类型 == 63) {
            魔宠名字 = "≤刺猬≥";
        } else if (类型 == 64) {
            魔宠名字 = "≤三眼木妖怪≥";
        } else if (类型 == 65) {
            魔宠名字 = "≤工地松鼠≥";
        } else if (类型 == 66) {
            魔宠名字 = "≤小鹿≥";
        } else if (类型 == 67) {
            魔宠名字 = "≤恶魔猫王≥";
        } else if (类型 == 68) {
            魔宠名字 = "≤人参≥";
        } else if (类型 == 69) {
            魔宠名字 = "≤时间恶魔≥";
        } else if (类型 == 70) {
            魔宠名字 = "≤毛球怪≥";
        } else if (类型 == 71) {
            魔宠名字 = "≤海盗船员≥";
        } else if (类型 == 72) {
            魔宠名字 = "≤蝙蝠魔≥";
        } else if (类型 == 73) {
            魔宠名字 = "≤时间小丑≥";
        } else if (类型 == 74) {
            魔宠名字 = "≤时间船长≥";
        } else if (类型 == 75) {
            魔宠名字 = "≤雪人怪≥";
        } else if (类型 == 76) {
            魔宠名字 = "≤未来美女战士≥";
        } else if (类型 == 77) {
            魔宠名字 = "≤天鹰≥";
        } else if (类型 == 78) {
            魔宠名字 = "≤哭泣的蓝蘑菇≥";
        } else if (类型 == 79) {
            魔宠名字 = "≤猥琐的独眼木妖≥";
        } else if (类型 == 80) {
            魔宠名字 = "≤生气的僵尸蘑菇≥";
        } else if (类型 == 81) {
            魔宠名字 = "≤忧郁的野猪≥";
        } else if (类型 == 82) {
            魔宠名字 = "≤狮子王≥";
        } else if (类型 == 83) {
            魔宠名字 = "≤闹钟≥";
        } else if (类型 == 84) {
            魔宠名字 = "≤蓝兔子≥";
        } else if (类型 == 85) {
            魔宠名字 = "≤红兔子≥";
        } else if (类型 == 86) {
            魔宠名字 = "≤哭泣的小冰骑士≥";
        } else if (类型 == 87) {
            魔宠名字 = "≤小神龙≥";
        } else if (类型 == 88) {
            魔宠名字 = "≤黑蛇≥";
        } else if (类型 == 89) {
            魔宠名字 = "≤黄金龙≥";
        } else if (类型 == 90) {
            魔宠名字 = "≤小神龙≥";
        } else if (类型 == 91) {
            魔宠名字 = "≤石化了≥";
        } else if (类型 == 92) {
            魔宠名字 = "≤小神龙≥";
        } else if (类型 == 93) {
            魔宠名字 = "≤黑蛇≥";
        } else if (类型 == 94) {
            魔宠名字 = "≤品克缤≥";
        } else if (类型 == 95) {
            魔宠名字 = "≤红发刺客≥";
        } else if (类型 == 96) {
            魔宠名字 = "≤银枪战士≥";
        } else if (类型 == 97) {
            魔宠名字 = "≤红发刺客≥";
        } else if (类型 == 98) {
            魔宠名字 = "≤白老鼠≥";
        } else if (类型 == 99) {
            魔宠名字 = "≤褐色老鼠≥";
        } else if (类型 == 100) {
            魔宠名字 = "≤褐色老鼠≥";
        } else if (类型 == 101) {
            魔宠名字 = "≤黄金老鼠≥";
        } else if (类型 == 102) {
            魔宠名字 = "≤绿透石头人≥";
        } else if (类型 == 103) {
            魔宠名字 = "≤装甲车≥";
        } else if (类型 == 104) {
            魔宠名字 = "≤彩头蛇≥";
        } else if (类型 == 105) {
            魔宠名字 = "≤红花蛇≥";
        } else if (类型 == 106) {
            魔宠名字 = "≤萌黑蛇≥";
        } else if (类型 == 107) {
            魔宠名字 = "≤萌白蛇≥";
        } else if (类型 == 108) {
            魔宠名字 = "≤萌龙战士≥";
        } else if (类型 == 109) {
            魔宠名字 = "≤萌狮王≥";
        } else if (类型 == 110) {
            魔宠名字 = "≤萌黑暗法师≥";
        } else if (类型 == 111) {
            魔宠名字 = "≤萌女神≥";
        } else if (类型 == 112) {
            魔宠名字 = "≤萌白发妹子≥";
        } else if (类型 == 113) {
            魔宠名字 = "≤萌扎昆≥";
        } else if (类型 == 114) {
            魔宠名字 = "≤萌黑龙王≥";
        } else if (类型 == 115) {
            魔宠名字 = "≤萌红发妹子≥";
        } else if (类型 == 116) {
            魔宠名字 = "≤萌光法师≥";
        } else if (类型 == 117) {
            魔宠名字 = "≤萌夜法师≥";
        } else if (类型 == 118) {
            魔宠名字 = "≤萌夜光法师≥";
        } else if (类型 == 119) {
            魔宠名字 = "≤萌幻影≥";
        } else if (类型 == 120) {
            魔宠名字 = "≤萌双弩≥";
        } else if (类型 == 121) {
            魔宠名字 = "≤红发美女≥";
        } else if (类型 == 122) {
            魔宠名字 = "≤狂龙战士≥";
        } else if (类型 == 123) {
            魔宠名字 = "≤黑暗魔法师≥";
        } else if (类型 == 124) {
            魔宠名字 = "≤黑暗骷髅战士≥";
        } else if (类型 == 125) {
            魔宠名字 = "≤黑暗骷髅法师≥";
        } else if (类型 == 126) {
            魔宠名字 = "≤黑暗骷髅射手≥";
        } else if (类型 == 127) {
            魔宠名字 = "≤黑暗骷髅刺客≥";
        } else if (类型 == 128) {
            魔宠名字 = "≤黑暗骷髅海盗≥";
        } else if (类型 == 129) {
            魔宠名字 = "≤骷髅盗贼≥";
        } else if (类型 == 130) {
            魔宠名字 = "≤滑雪企鹅≥";
        } else if (类型 == 131) {
            魔宠名字 = "≤帅哥学霸≥";
        } else if (类型 == 132) {
            魔宠名字 = "≤奇怪的章鱼≥";
        } else if (类型 == 133) {
            魔宠名字 = "≤可疑的跟班≥";
        } else if (类型 == 134) {
            魔宠名字 = "≤神秘的箱子≥";
        } else if (类型 == 135) {
            魔宠名字 = "≤萌白羊≥";
        } else if (类型 == 136) {
            魔宠名字 = "≤萌蓝羊≥";
        } else if (类型 == 137) {
            魔宠名字 = "≤恋爱中的褐色熊≥";
        } else if (类型 == 138) {
            魔宠名字 = "≤恋爱中的黑熊≥";
        } else if (类型 == 139) {
            魔宠名字 = "≤基友蘑菇≥";
        }
        if (ServerConstants.调试输出封包) {
            System.out.println("spawnPlayerMapobject--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeMapleAsciiString(魔宠名字);
        mplew.write(new byte[6]);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0xE0);
        mplew.write(0x1F);
        mplew.write(0);
        if (类型 == 0) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 00"));//变身花蘑菇的封包数据
        } else if (类型 == 1) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 01"));//变身花蘑菇的封包数据
        } else if (类型 == 2) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 02"));//变身漂漂猪的封包数据
        } else if (类型 == 3) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 03"));//变身白外星人的封包数据
        } else if (类型 == 4) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 04"));//变身龙族的封包数据
        } else if (类型 == 5) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 06"));//变身提干的封包数据
        } else if (类型 == 6) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 07"));//变身彩色蜗牛的封包数据
        } else if (类型 == 7) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 08"));//变身幽灵的封包数据
        } else if (类型 == 8) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 09"));//变身苏菲莉亚丢弃的人偶的封包数据
        } else if (类型 == 9) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 0A"));//变身超人变形A的封包数据
        } else if (类型 == 10) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 0B"));//变身超人变形B的封包数据
        } else if (类型 == 11) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 0C"));//变身老鼠变身的封包数据
        } else if (类型 == 12) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 0F"));//变身屈原的封包数据
        } else if (类型 == 13) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 13"));//变身白色兔子的封包数据
        } else if (类型 == 14) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 14"));//变身粉红兔子的封包数据
        } else if (类型 == 15) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 16"));//变身佩托的封包数据
        } else if (类型 == 16) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 1A"));//变身宝贝龙的封包数据
        } else if (类型 == 17) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 1B"));//变身罗博（电子狗）的封包数据
        } else if (类型 == 18) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 1C"));//变身雪犬(灰色)的封包数据
        } else if (类型 == 19) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 1D"));//变身箱子的封包数据
        } else if (类型 == 20) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 1E"));//变身圣诞雪人的封包数据
        } else if (类型 == 21) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 1F"));//变身牛魔王(黄)的封包数据
        } else if (类型 == 22) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 20"));//变身牛魔王(蓝)的封包数据
        } else if (类型 == 23) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 21"));//变身温顺的牛的封包数据
        } else if (类型 == 24) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 22"));//变身凶恶的牛的封包数据
        } else if (类型 == 25) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 23"));//变身黄金猪猪的封包数据
        } else if (类型 == 26) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 23"));//变身匹诺曹的爷爷的封包数据
        } else if (类型 == 27) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 25"));//变身匹诺曹的封包数据37
        } else if (类型 == 28) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 28"));//变身外星人的封包数据40
        } else if (类型 == 29) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 29"));//变身战神地图NPCA的封包数据41
        } else if (类型 == 30) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 2A"));//变身战神地图NPCB的封包数据42
        } else if (类型 == 31) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 2B"));//变身战神地图NPCC的封包数据43
        } else if (类型 == 32) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 2C"));//变身战神地图NPCD的封包数据44
        } else if (类型 == 33) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 2D"));//变身战神地图NPCE的封包数据45
        } else if (类型 == 34) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 32"));//变身圣诞雪人的封包数据
        } else if (类型 == 35) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 33"));//变身农民的封包数据
        } else if (类型 == 36) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 34"));//变身保洁阿姨的封包数据
        } else if (类型 == 37) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 35"));//变身熊孩子的封包数据
        } else if (类型 == 38) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 3F"));//变身老绅士的封包数据
        } else if (类型 == 39) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 43"));//变身机械装甲车的封包数据
        } else if (类型 == 40) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 44"));//变身小蜗牛的封包数据
        } else if (类型 == 41) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 45"));//变身小雪娃娃的封包数据
        } else if (类型 == 42) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 46"));//变身郁闷的绿蘑菇的封包数据
        } else if (类型 == 43) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 47"));//变身蝙蝠的封包数据
        } else if (类型 == 44) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 48"));//变身警用飞艇的封包数据
        } else if (类型 == 45) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 49"));//变身大头蛇的封包数据
        } else if (类型 == 46) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 4A"));//变身猴子的封包数据
        } else if (类型 == 47) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 4B"));//变身蜗牛王的封包数据
        } else if (类型 == 48) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 4C"));//变身蓝色玩具老鼠的封包数据
        } else if (类型 == 48) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 4D"));//变身紫色闹钟怪的封包数据
        } else if (类型 == 50) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 4E"));//变身方块人的封包数据
        } else if (类型 == 51) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 4F"));//变身小雪怪的封包数据
        } else if (类型 == 52) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 50"));//变身老仙人掌的封包数据
        } else if (类型 == 53) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 51"));//变身滑雪企鹅的封包数据
        } else if (类型 == 54) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 52"));//变身绿龙的封包数据
        } else if (类型 == 55) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 53"));//变身恶魔猫的封包数据
        } else if (类型 == 56) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 54"));//变身小老虎的封包数据
        } else if (类型 == 57) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 55"));//变身海螺王的封包数据
        } else if (类型 == 58) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 56"));//变身狐狸的封包数据
        } else if (类型 == 59) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 57"));//变身石头人的封包数据
        } else if (类型 == 60) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 58"));//变身仙猫的封包数据
        } else if (类型 == 61) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 59"));//变身工地熊的封包数据
        } else if (类型 == 62) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 5A"));//变身大力外星人的封包数据
        } else if (类型 == 63) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 5B"));//变身刺猬的封包数据
        } else if (类型 == 64) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 5C"));//变身三眼木妖怪的封包数据
        } else if (类型 == 65) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 5D"));//变身工地松鼠的封包数据
        } else if (类型 == 66) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 5E"));//变身小鹿的封包数据
        } else if (类型 == 67) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 5F"));//变身恶魔猫王的封包数据
        } else if (类型 == 68) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 60"));//变身人参的封包数据
        } else if (类型 == 69) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 61"));//变身时间恶魔的封包数据
        } else if (类型 == 70) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 62"));//变身毛球怪的封包数据
        } else if (类型 == 71) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 63"));//变身海盗船员的封包数据
        } else if (类型 == 72) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 64"));//变身蝙蝠魔(飞)的封包数据
        } else if (类型 == 73) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 65"));//变身时间小丑的封包数据
        } else if (类型 == 74) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 66"));//变身时间船长的封包数据
        } else if (类型 == 75) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 67"));//变身雪人怪的封包数据
        } else if (类型 == 76) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 68"));//变身未来美女战士的封包数据
        } else if (类型 == 77) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 69"));//变身天鹰的封包数据
        } else if (类型 == 78) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 6A"));//变身哭泣的蓝蘑菇的封包数据
        } else if (类型 == 79) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 6B"));//变身猥琐的独眼木妖的封包数据
        } else if (类型 == 80) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 6C"));//变身生气的僵尸蘑菇的封包数据
        } else if (类型 == 81) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 6D"));//变身忧郁的野猪的封包数据
        } else if (类型 == 82) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 6E"));//变身狮子王的封包数据
        } else if (类型 == 83) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 6F"));//变身闹钟的封包数据
        } else if (类型 == 84) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 70"));//变身蓝兔子的封包数据
        } else if (类型 == 85) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 71"));//变身红兔子的封包数据
        } else if (类型 == 86) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 72"));//变身哭泣的小冰骑士的封包数据
        } else if (类型 == 87) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 73"));//变身小神龙的封包数据
        } else if (类型 == 88) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 74"));//变身黑蛇的封包数据
        } else if (类型 == 89) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 7D"));//变身黄金龙的封包数据
        } else if (类型 == 90) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 7E"));//变身小神龙的封包数据
        } else if (类型 == 91) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 80"));//变身石化了的封包数据
        } else if (类型 == 92) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 81"));//变身小神龙的封包数据
        } else if (类型 == 93) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 82"));//变身黑蛇的封包数据
        } else if (类型 == 94) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 83"));//变身品克缤的封包数据
        } else if (类型 == 95) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 84"));//变身红发刺客的封包数据
        } else if (类型 == 96) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 85"));//变身银枪战士的封包数据
        } else if (类型 == 97) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 86"));//变身红发刺客的封包数据
        } else if (类型 == 98) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 87"));//变身白老鼠的封包数据
        } else if (类型 == 99) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 88"));//变身褐色老鼠的封包数据
        } else if (类型 == 100) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 89"));//变身褐色老鼠的封包数据
        } else if (类型 == 101) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 8A"));//变身黄金老鼠的封包数据
        } else if (类型 == 102) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 8B"));//变身绿透石头人的封包数据
        } else if (类型 == 103) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 8C"));//变身装甲车的封包数据
        } else if (类型 == 104) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 8D"));//变身彩头蛇的封包数据
        } else if (类型 == 105) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 8E"));//变身红花蛇的封包数据
        } else if (类型 == 106) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 8F"));//变身萌黑蛇的封包数据
        } else if (类型 == 107) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 90"));//变身萌白蛇的封包数据
        } else if (类型 == 108) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 91"));//变身萌龙战士的封包数据
        } else if (类型 == 109) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 92"));//变身萌狮王的封包数据
        } else if (类型 == 110) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 93"));//变身萌黑暗法师的封包数据
        } else if (类型 == 111) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 94"));//变身萌女神的封包数据
        } else if (类型 == 112) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 95"));//变身萌白发妹子的封包数据
        } else if (类型 == 113) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 96"));//变身萌扎昆的封包数据
        } else if (类型 == 114) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 97"));//变身萌龙王的封包数据
        } else if (类型 == 115) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 98"));//变身萌红发妹子的封包数据
        } else if (类型 == 116) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 99"));//变身萌光法师的封包数据
        } else if (类型 == 117) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 9A"));//变身萌夜法师的封包数据
        } else if (类型 == 118) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 9B"));//变身萌夜光法师的封包数据
        } else if (类型 == 119) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 9C"));//变身萌幻影的封包数据
        } else if (类型 == 120) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 9D"));//变身萌双弩的封包数据
        } else if (类型 == 121) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A0"));//变身红发美女的封包数据
        } else if (类型 == 122) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A1"));//变身狂龙战士的封包数据
        } else if (类型 == 123) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A2"));//变身黑暗魔法师的封包数据
        } else if (类型 == 124) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A3"));//变身黑暗骷髅战士的封包数据
        } else if (类型 == 125) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A4"));//变身黑暗骷髅法师的封包数据
        } else if (类型 == 126) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A5"));//变身黑暗骷髅射手的封包数据
        } else if (类型 == 127) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A6"));//变身黑暗骷髅刺客的封包数据
        } else if (类型 == 128) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A7"));//变身黑暗骷髅海盗的封包数据
        } else if (类型 == 129) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A8"));//变身骷髅盗贼的封包数据
        } else if (类型 == 130) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 A9"));//变身滑雪企鹅的封包数据
        } else if (类型 == 131) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 AA"));//变身帅哥学霸的封包数据
        } else if (类型 == 132) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 AB"));//变身奇怪的章鱼的封包数据
        } else if (类型 == 133) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 AC"));//变身可疑的跟班的封包数据
        } else if (类型 == 134) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 AD"));//变身神秘的箱子的封包数据
        } else if (类型 == 135) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 AE"));//变身萌白羊的封包数据
        } else if (类型 == 136) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 AF"));//变身萌蓝羊的封包数据
        } else if (类型 == 137) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 B0"));//变身恋爱中的褐色熊的封包数据
        } else if (类型 == 138) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 B1"));//变身恋爱中的黑熊的封包数据
        } else if (类型 == 139) {
            mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 00 00 00 00 B2"));//变身基友蘑菇的封包数据
        }
        /*if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
         mplew.writeInt(2);
         } else {
         mplew.writeInt(0);
         }
         long buffmask = 0L;
         Integer buffvalue = null;
         if ((chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) && (!chr.isHidden())) {
         buffmask |= MapleBuffStat.DARKSIGHT.getValue();
         }
         if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
         buffmask |= MapleBuffStat.COMBO.getValue();
         buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
         }
         if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
         buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
         }
         if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
         buffmask |= MapleBuffStat.SOULARROW.getValue();
         }
         if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
         buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
         }
         if (chr.getBuffedValue(MapleBuffStat.能量) != null) {
         buffmask |= MapleBuffStat.能量.getValue();
         buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.能量).intValue());
         }//AREN'T THESE 
         mplew.writeInt((int) (buffmask >> 32 & 0xFFFFFFFF));
         if (buffvalue != null) {
         if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
         mplew.writeShort(buffvalue.intValue());
         } else {
         mplew.write(buffvalue.byteValue());
         }
         }
         final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
         mplew.writeInt((int) (buffmask & 0xFFFFFFFF));*/
        final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(new byte[6]);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0); //start of Monster Riding
        mplew.write(0);
        IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
        if ((chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) && (mount != null)) {
            mplew.writeInt(mount.getItemId());
            mplew.writeInt(1004);
            mplew.writeInt(0x01261F00);
            mplew.write(0);
        } else {
            mplew.writeInt(CHAR_MAGIC_SPAWN);
            mplew.writeLong(0L);
            mplew.write(0);
        }
        mplew.writeLong(0L);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.write(1);
        mplew.write(0x41);
        mplew.write(0x9A);
        mplew.write(0x70);
        mplew.write(7);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.writeShort(chr.getJob());
        PacketHelper.addCharLook(mplew, chr, false);
        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); //max is like 100. but w/e
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(0); // 只有台陸版有
        mplew.writeInt(-1); // 只有台陸版有
        mplew.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
        //mplew.writePos(chr.getPosition());
        mplew.writeShort(chr.getPosition().x + 100);//横
        mplew.writeShort(chr.getPosition().y);//竖
        mplew.write(chr.getStance());
        mplew.writeShort(0); // FH
        mplew.write(0);
        mplew.writeInt(chr.getMount().getLevel()); // mount lvl
        mplew.writeInt(chr.getMount().getExp()); // exp
        mplew.writeInt(chr.getMount().getFatigue()); // tiredness
        PacketHelper.addAnnounceBox(mplew, chr);
        mplew.write(chr.getChalkboard() != null && chr.getChalkboard().length() > 0 ? 1 : 0);
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(mplew, rings.getLeft());
        addRingInfo(mplew, rings.getRight());
        mplew.writeShort(0);
        mplew.write(0); // 台版自己加的
        if (chr.getCarnivalParty() != null) {
            mplew.write(chr.getCarnivalParty().getTeam());
        } else if (chr.getMapId() == 109080000 || chr.getMapId() == 109080010) {
            mplew.write(chr.getCoconutTeam()); //is it 0/1 or is it 1/2?
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnPlayerMapobject-2057" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param chr
     * @return
     */
    public static MaplePacket removePlayerFromMap(int cid, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removePlayerFromMap--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);

        ServerConstants ERROR = new ServerConstants();
        /*if (ServerConstants.PACKET_ERROR_OFF && ERROR.getChannel() == 1) {
            ERROR.setRemovePlayerFromMap(1);
            //ERROR.setPACKET_ERROR("removePlayerFromMap-2073" + "：\r\n" + ERROR.getPACKET_ERROR() + "\r\n\r\n");
        }else */
        if (ServerConstants.PACKET_ERROR_OFF && ERROR.getChannel() != 1 || ERROR.getRemovePlayerFromMap() != 1) {
            // ERROR.setPACKET_ERROR("38错误" + "：\r\n" + ERROR.getPACKET_ERROR() + "\r\n\r\n");
            String note = "时间：" + FileoutputUtil.CurrentReadable_Time() + " "
                    + "|| 玩家名字：" + chr.getName() + ""
                    + "|| 玩家地图：" + chr.getMapId() + "\r\n"
                    + "38错误：" + ERROR.getPACKET_ERROR() + "\r\n\r\n";
            FileoutputUtil.packetLog("logs\\38掉线\\" + chr.getName() + ".log", note);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @param expression
     * @return
     */
    public static MaplePacket facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("facialExpression--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        /*        mplew.writeInt(-1); //itemid of expression use
         mplew.write(0);*/

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("facialExpression-2092" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param moves
     * @param startPos
     * @return
     */
    public static MaplePacket movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("movePlayer--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
//        mplew.writePos(startPos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("movePlayer-2111" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param oid
     * @param startPos
     * @param moves
     * @return
     */
    public static MaplePacket moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("moveSummon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writePos(startPos);
//        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("moveSummon-2131" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param summonSkillId
     * @param animation
     * @param allDamage
     * @return
     */
    public static MaplePacket summonAttack(final int cid, final int summonSkillId, final byte animation, final List<SummonAttackEntry> allDamage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("summonAttack--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        /*   mplew.writeInt(cid);
         mplew.writeInt(summonSkillId);
         //  mplew.write(level - 1); //? guess
         mplew.write(animation);
         mplew.write(allDamage.size());

         for (final SummonAttackEntry attackEntry : allDamage) {
         mplew.writeInt(attackEntry.getMonster().getObjectId()); // oid
         mplew.write(1);
         mplew.write(6);
         // mplew.write(0x07); // who knows
         mplew.writeInt(attackEntry.getDamage()); // damage
         }*/
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("summonAttack-2158" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param tbyte
     * @param skill
     * @param level
     * @param display
     * @param animation
     * @param speed
     * @param damage
     * @param energy
     * @param lvl
     * @param mastery
     * @param unk
     * @param charge
     * @return
     */
    public static MaplePacket closeRangeAttack(int cid, int tbyte, int skill, int level, byte display, byte animation, byte speed, List<AttackPair> damage, final boolean energy, int lvl, byte mastery, byte unk, int charge) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("closeRangeAttack--------------------");
        }
        mplew.writeShort(energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl); //?
        if (skill > 0) {
            mplew.write(level);
            mplew.writeInt(skill);
        } else {
            mplew.write(0);
        }
        mplew.write(unk); // Added on v.82
        mplew.write(display);
        mplew.write(animation);
        mplew.write(speed);
        mplew.write(mastery); // Mastery
        mplew.writeInt(0);  // E9 03 BE FC

        if (skill == 4211006) {
            for (AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(0x07);
                    mplew.write(oned.attack.size());
                    for (Pair<Integer, Boolean> eachd : oned.attack) {
                        // highest bit set = crit
                        mplew.writeInt(eachd.left); //m.e. is never crit
                    }
                }
            }
        } else {
            for (AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(0x07);
                    for (Pair<Integer, Boolean> eachd : oned.attack) {
                        // highest bit set = crit
                        if (eachd.right) {
                            mplew.writeInt(eachd.left + 0x80000000);
                        } else {
                            mplew.writeInt(eachd.left);
                        }
                    }
                }
            }
        }
        //if (charge > 0) {
        //	mplew.writeInt(charge); //is it supposed to be here
        //}
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("closeRangeAttack-2219" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param tbyte
     * @param skill
     * @param level
     * @param display
     * @param animation
     * @param speed
     * @param itemid
     * @param damage
     * @param pos
     * @param lvl
     * @param mastery
     * @param unk
     * @return
     */
    public static MaplePacket rangedAttack(int cid, byte tbyte, int skill, int level, byte display, byte animation, byte speed, int itemid, List<AttackPair> damage, final Point pos, int lvl, byte mastery, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("rangedAttack--------------------");
        }
        mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl); //?
        if (skill > 0) {
            mplew.write(level);
            mplew.writeInt(skill);
        } else {
            mplew.write(0);
        }
        mplew.write(unk); // Added on v.82
        mplew.write(display);
        mplew.write(animation);
        mplew.write(speed);
        mplew.write(mastery); // Mastery level, who cares
        mplew.writeInt(itemid);

        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(0x07);
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    // highest bit set = crit
                    if (eachd.right) {
                        mplew.writeInt(eachd.left + 0x80000000);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        mplew.writePos(pos); // Position

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("rangedAttack-2265" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param tbyte
     * @param skill
     * @param level
     * @param display
     * @param animation
     * @param speed
     * @param damage
     * @param charge
     * @param lvl
     * @param unk
     * @return
     */
    public static MaplePacket magicAttack(int cid, int tbyte, int skill, int level, byte display, byte animation, byte speed, List<AttackPair> damage, int charge, int lvl, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("magicAttack--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl); //?
        mplew.write(level);
        mplew.writeInt(skill);

        mplew.write(unk); // Added on v.82
        mplew.write(display);
        mplew.write(animation);
        mplew.write(speed);
        mplew.write(0); // Mastery byte is always 0 because spells don't have a swoosh
        mplew.writeInt(0);

        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(-1/*0x07*/);
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    // highest bit set = crit
                    if (eachd.right) {
                        mplew.writeInt(eachd.left + 0x80000000);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        if (charge > 0) {
            mplew.writeInt(charge);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("magicAttack-2309" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param sid
     * @param items
     * @return
     */
    public static MaplePacket getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        if (ServerConstants.调试输出封包) {
            System.out.println("getNPCShop--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size()); // item count
        for (MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
//            mplew.writeInt(item.getReqItem());
//            mplew.writeInt(item.getReqItemQ());
//            mplew.writeLong(0);
            if (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o
                mplew.writeShort(item.getBuyable());
            } else {
                mplew.writeZeroBytes(6);
                mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
                //上面按理说是一个long 不知道为什么要分成 6 和 2
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getNPCShop-2341" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param code
     * @return
     */
    public static MaplePacket confirmShopTransaction(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("confirmShopTransaction--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code); // 8 = sell, 0 = buy, 0x20 = due to an error

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("confirmShopTransaction-2357" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param item
     * @return
     */
    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item) {
        if (ServerConstants.调试输出封包) {
            System.out.println("addInventorySlotA--------------------");
        }
        return addInventorySlot(type, item, false);
    }

    /**
     *
     * @param type
     * @param item
     * @param fromDrop
     * @return
     */
    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("addInventorySlot--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.writeShort(1); // add mode
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        PacketHelper.addItemInfo(mplew, item, true, false);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("addInventorySlot-2384" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /*
     * 0 = 获得道具
     * 1 = 更新道具数量
     * 2 = 移动道具
     * 3 = 删除道具
     * 5 = 移动道具小背包到背包
     * 6 = 小背包更新道具
     * 7 = 小背包删除道具
     * 8 = 移动位置小背包里面的道具
     * 9 = 小背包获得道具
     */

    /**
     *
     * @param type
     * @param item
     * @param fromDrop
     * @return
     */

    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateInventorySlot--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
//	mplew.write((slot2 > 0 ? 1 : 0) + 1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(type.getType()); // iv type
        mplew.writeShort(item.getPosition()); // slot id
        mplew.writeShort(item.getQuantity());
        /*	if (slot2 > 0) {
         mplew.write(1);
         mplew.write(type.getType());
         mplew.writeShort(slot2);
         mplew.writeShort(amt2);
         }*/
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateInventorySlot-2411" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param src
     * @param dst
     * @return
     */
    public static MaplePacket moveInventoryItem(MapleInventoryType type, short src, short dst) {
        if (ServerConstants.调试输出封包) {
            System.out.println("moveInventoryItemA--------------------");
        }
        return moveInventoryItem(type, src, dst, (byte) -1);
    }

    /**
     *
     * @param type
     * @param src
     * @param dst
     * @param equipIndicator
     * @return
     */
    public static MaplePacket moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("moveInventoryItemB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 02"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.write(equipIndicator);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("moveInventoryItemB-2439" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param src
     * @param dst
     * @param total
     * @return
     */
    public static MaplePacket moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("moveAndMergeInventoryItem--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.write(1); // merge mode?
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(total);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("moveAndMergeInventoryItem-2461" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param src
     * @param dst
     * @param srcQ
     * @param dstQ
     * @return
     */
    public static MaplePacket moveAndMergeWithRestInventoryItem(MapleInventoryType type, short src, short dst, short srcQ, short dstQ) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("moveAndMergeWithRestInventoryItem--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 01"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(srcQ);
        mplew.write(HexTool.getByteArrayFromHexString("01"));
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(dstQ);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("moveAndMergeWithRestInventoryItem-2484" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param slot
     * @param fromDrop
     * @return
     */
    public static MaplePacket clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("clearInventoryItem--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(HexTool.getByteArrayFromHexString("01 03"));
        mplew.write(type.getType());
        mplew.writeShort(slot);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("clearInventoryItem-2503" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param item
     * @param invType
     * @return
     */
    public static MaplePacket updateSpecialItemUse(IItem item, byte invType) {
        if (ServerConstants.调试输出封包) {
            System.out.println("updateSpecialItemUseA--------------------");
        }
        return updateSpecialItemUse(item, invType, item.getPosition());
    }

    /**
     *
     * @param item
     * @param invType
     * @param pos
     * @return
     */
    public static MaplePacket updateSpecialItemUse(IItem item, byte invType, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateSpecialItemUseB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(2); // always 2
        mplew.write(3); // quantity > 0 (?)
        mplew.write(invType); // Inventory type
        mplew.writeShort(pos); // item slot
        mplew.write(0);
        mplew.write(invType);
        if (item.getType() == 1) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
        PacketHelper.addItemInfo(mplew, item, true, true);
        if (item.getPosition() < 0) {
            mplew.write(2); //?
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateSpecialItemUseB-2541" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param item
     * @param invType
     * @return
     */
    public static MaplePacket updateSpecialItemUse_(IItem item, byte invType) {
        if (ServerConstants.调试输出封包) {
            System.out.println("updateSpecialItemUse_A--------------------");
        }
        return updateSpecialItemUse_(item, invType, item.getPosition());
    }

    /**
     *
     * @param item
     * @param invType
     * @param pos
     * @return
     */
    public static MaplePacket updateSpecialItemUse_(IItem item, byte invType, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateSpecialItemUse_B--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(1); // always 2
        mplew.write(0); // quantity > 0 (?)
        mplew.write(invType); // Inventory type
        if (item.getType() == 1) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
        PacketHelper.addItemInfo(mplew, item, true, true);
        if (item.getPosition() < 0) {
            mplew.write(1); //?
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateSpecialItemUse_B-2576" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param scroll
     * @param item
     * @param destroyed
     * @param potential
     * @return
     */
    public static MaplePacket scrolledItem(IItem scroll, IItem item, boolean destroyed, boolean potential) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("scrolledItem--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1); // fromdrop always true
        mplew.write(destroyed ? 2 : 3);
        mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
        mplew.write(GameConstants.getInventoryType(scroll.getItemId()).getType()); //can be cash
        mplew.writeShort(scroll.getPosition());

        if (scroll.getQuantity() > 0) {
            mplew.writeShort(scroll.getQuantity());
        }
        mplew.write(3);
        if (!destroyed) {
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            mplew.write(0);
        }
        mplew.write(MapleInventoryType.EQUIP.getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (!potential) {
            mplew.write(1);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("scrolledItem-2614" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param scrollSuccess
     * @param legendarySpirit
     * @return
     */
    public static MaplePacket getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getScrollEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);

        switch (scrollSuccess) {
            case SUCCESS:
                mplew.writeShort(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case FAIL:
                mplew.writeShort(0);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case CURSE:
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
        }
        mplew.write(0); //? pam's song?
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getScrollEffect-2646" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    //miracle cube?

    /**
     *
     * @param chr
     * @param itemid
     * @return
     */
    public static MaplePacket getPotentialEffect(final int chr, final int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPotentialEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_EFFECT.getValue());
        mplew.writeInt(chr);
        mplew.writeInt(itemid);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPotentialEffect-2663" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    //magnify glass

    /**
     *
     * @param chr
     * @param pos
     * @return
     */
    public static MaplePacket getPotentialReset(final int chr, final short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPotentialReset--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
        mplew.writeInt(chr);
        mplew.writeShort(pos);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPotentialReset-2680" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static final MaplePacket ItemMaker_Success() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//D6 00 00 00 00 00 01 00 00 00 00 DC DD 40 00 01 00 00 00 01 00 00 00 8A 1C 3D 00 01 00 00 00 00 00 00 00 00 B0 AD 01 00
        if (ServerConstants.调试输出封包) {
            System.out.println("ItemMaker_Success--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x11);
        mplew.writeZeroBytes(4);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("ItemMaker_Success-2697" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from_playerid
     * @return
     */
    public static final MaplePacket ItemMaker_Success_3rdParty(final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("ItemMaker_Success_3rdParty--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(0x11);
        mplew.writeZeroBytes(4);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("ItemMaker_Success_3rdParty-2715" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @return
     */
    public static MaplePacket explodeDrop(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("explodeDrop--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(4); // 4 = Explode
        mplew.writeInt(oid);
        mplew.writeShort(655);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("explodeDrop-2733" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param animation
     * @param cid
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid) {
        if (ServerConstants.调试输出封包) {
            System.out.println("removeItemFromMapA--------------------");
        }
        return removeItemFromMap(oid, animation, cid, 0);
    }

    /**
     *
     * @param oid
     * @param animation
     * @param cid
     * @param slot
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeItemFromMapB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // 0 = Expire, 1 = without animation, 2 = pickup, 4 = explode, 5 = pet pickup
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (animation == 5) { // allow pet pickup?
                mplew.write(slot);
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeItemFromMapB-2762" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @return
     */
    public static MaplePacket updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateCharLook--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketHelper.addCharLook(mplew, chr, false);
        // 戒指开始
        Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        List<MapleRing> allrings = rings.getLeft();
        allrings.addAll(rings.getRight());
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        // 戒指结束 
        return mplew.getPacket();
    }

    private static void addMarriageRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(chr.getMarriageRing(false) != null ? (byte) 1 : (byte) 0);
        if (chr.getMarriageRing(false) != null) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing(false).getPartnerChrId());
            mplew.writeInt(chr.getMarriageRing(false).getRingId());
        }
    }

    /**
     *
     * @param mplew
     * @param rings
     */
    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
        if (ServerConstants.调试输出封包) {
            System.out.println("addRingInfo--------------------");
        }
        mplew.write(rings.size() > 0 ? 1 : 0);
        mplew.writeInt(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
    }

    //丢下道具[扔东西扔全部]

    /**
     *
     * @param type
     * @param src
     * @return
     */
    public static MaplePacket dropInventoryItem(MapleInventoryType type, short src) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("dropInventoryItem--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        if (src < 0) {
            mplew.write(1);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("dropInventoryItem-2818" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    //更新掉落装备[扔东西扔一个]/*  if ((item.getItemId() >= 1112800)

    /**
     *
     * @param type
     * @param item
     * @return
     */
    public static MaplePacket dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("dropInventoryItemUpdate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01"));
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("dropInventoryItemUpdate-2838" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param skill
     * @param monsteridfrom
     * @param cid
     * @param damage
     * @return
     */
    public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage) {
        return damagePlayer(skill, monsteridfrom, cid, damage, 0, (byte) 0, 0, false, 0, 0, 0);
    }

    /**
     *
     * @param skill
     * @param monsteridfrom
     * @param cid
     * @param damage
     * @param fake
     * @param direction
     * @param reflect
     * @param is_pg
     * @param oid
     * @param pos_x
     * @param pos_y
     * @return
     */
    public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, byte direction, int reflect, boolean is_pg, int oid, int pos_x, int pos_y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("damagePlayer--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(direction);

        if (reflect > 0) {
            mplew.write(reflect);
            mplew.write(is_pg ? 1 : 0);
            mplew.writeInt(oid);
            mplew.write(6);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
            mplew.write(0);
        } else {
            mplew.writeShort(0);
        }
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("damagePlayer-2873" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param quest
     * @return
     */
    public static final MaplePacket updateQuest(final MapleQuestStatus quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateQuest--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest.getQuest().getId());
        mplew.write(quest.getStatus());
        switch (quest.getStatus()) {
            case 0:
                mplew.writeZeroBytes(10);
                break;
            case 1:
                mplew.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
                break;
            case 2:
                mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
                break;
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateQuest-2902" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param quest
     * @param data
     * @return
     */
    public static final MaplePacket updateInfoQuest(final int quest, final String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateInfoQuest--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x0A);
        mplew.writeShort(quest);
        mplew.writeMapleAsciiString(data);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateInfoQuest-2920" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @param npc
     * @param progress
     * @return
     */
    public static MaplePacket updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateQuestInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateQuestInfo-2939" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param quest
     * @param npc
     * @param nextquest
     * @return
     */
    public static MaplePacket updateQuestFinish(int quest, int npc, int nextquest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("updateQuestFinish--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(nextquest);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateQuestFinish-2956" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param isSelf
     * @return
     */
    public static final MaplePacket charInfo(final MapleCharacter chr, final boolean isSelf) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("charInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        mplew.writeShort(chr.getFame());
        mplew.write(chr.getMarriageId() > 0 ? 1 : 0); // heart red or gray
        String guildName = "-";
        String allianceName = "-";
        final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
        if (chr.getGuildId() > 0 && gs != null) {
            guildName = gs.getName();
            if (gs.getAllianceId() > 0) {
                final MapleGuildAlliance allianceNameA = World.Alliance.getAlliance(gs.getAllianceId());
                if (allianceNameA != null) {
                    allianceName = allianceNameA.getName();
                }
            }
        }
        mplew.writeMapleAsciiString(guildName);
        mplew.writeMapleAsciiString(allianceName); // Alliance
//        mplew.write(isSelf ? 1 : 0);
        /*  mplew.writeMapleAsciiString(chr.getcharmessage()); // 角色訊息
         mplew.write(chr.getexpression());// 表情
         mplew.write(chr.getconstellation());// 星座
         mplew.write(chr.getblood());// 血型
         mplew.write(chr.getmonth());// 月
         mplew.write(chr.getday());// 日*/

        final IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
        final int peteqid = inv != null ? inv.getItemId() : 0; //宠物装备1
        final IItem inv1 = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -122);
        final int peteqid1 = inv1 != null ? inv1.getItemId() : 0; //宠物装备2
        final IItem inv2 = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -124);
        final int peteqid2 = inv2 != null ? inv2.getItemId() : 0; //宠物装备2

        for (final MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                mplew.write(pet.getUniqueId()); //o-o byte ?
                mplew.writeInt(pet.getPetItemId()); // petid
                mplew.writeMapleAsciiString(pet.getName());
                mplew.write(pet.getLevel()); // pet level
                mplew.writeShort(pet.getCloseness()); // pet closeness
                mplew.write(pet.getFullness()); // pet fullness
                mplew.writeShort(pet.getFlags());
                mplew.writeInt(peteqid);
            }
        }
        mplew.write(0); // End of pet

        if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
            final MapleMount mount = chr.getMount();
            final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
            mplew.write(canwear ? 1 : 0);
            //   if (canwear) {
            mplew.writeInt(mount.getLevel());
            mplew.writeInt(mount.getExp());
            mplew.writeInt(mount.getFatigue());
            //   }
        } else {
            mplew.write(0);
        }

        /*        final int wishlistSize = chr.getWishlistSize();
         mplew.write(wishlistSize);
         if (wishlistSize > 0) {
         final int[] wishlist = chr.getWishlist();
         for (int x = 0; x < wishlistSize; x++) {
         mplew.writeInt(wishlist[x]);
         }
         }*/
        mplew.write(0);
        chr.getMonsterBook().addCharInfoPacket(chr.getMonsterBookCover(), mplew);

        IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
        mplew.writeInt(medal == null ? 0 : medal.getItemId());
        List<Integer> medalQuests = new ArrayList<>();
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if (q.getQuest().getMedalItem() > 0 && GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP) { //chair kind medal viewmedal is weird
                medalQuests.add(q.getQuest().getId());
            }
        }
        mplew.writeShort(medalQuests.size());
        for (int x : medalQuests) {
            mplew.writeShort(x);
        }

        MapleInventory iv = chr.getInventory(MapleInventoryType.SETUP);
        List<Item> chairItems = new ArrayList();
        for (IItem item : iv.list()) {
            if ((item.getItemId() >= 3010000) && (item.getItemId() <= 3020001)) {
                chairItems.add((Item) item);
            }
        }
        mplew.writeInt(chairItems.size());
        for (IItem item : chairItems) {
            mplew.writeInt(item.getItemId());
        }

        MapleInventory 勋章列表 = chr.getInventory(MapleInventoryType.EQUIP);
        List<Item> 勋章列表Items = new ArrayList();
        for (IItem item : 勋章列表.list()) {
            if ((item.getItemId() >= 1142000) && (item.getItemId() <= 1142999)) {
                勋章列表Items.add((Item) item);
            }
        }
        mplew.writeInt(勋章列表Items.size());
        for (IItem item : 勋章列表Items) {
            mplew.writeInt(item.getItemId());
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("charInfo-3080" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void writeLongMask(MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
        if (ServerConstants.调试输出封包) {
            System.out.println("writeLongMask--------------------");
        }
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    // List<Pair<MapleDisease, Integer>>
    private static void writeLongDiseaseMask(MaplePacketLittleEndianWriter mplew, List<Pair<MapleDisease, Integer>> statups) {
        if (ServerConstants.调试输出封包) {
            System.out.println("writeLongDiseaseMask--------------------");
        }
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<MapleDisease, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    private static void writeLongMaskFromListM(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
        if (ServerConstants.调试输出封包) {
            System.out.println("writeLongMaskFromList--------------------");
        }
        long firstmask = 0;
        long secondmask = 0;
        mplew.write(0);
        for (MapleBuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeInt(0);
        mplew.writeZeroBytes(3);
    }

    private static void writeLongMaskFromList(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
        if (ServerConstants.调试输出封包) {
            System.out.println("writeLongMaskFromList--------------------");
        }
        long firstmask = 0;
        long secondmask = 0;
        for (MapleBuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    /**
     *
     * @param c
     * @param buffid
     * @param skillid
     * @param statups
     * @return
     */
    public static MaplePacket giveMount(MapleCharacter c, int buffid, int skillid, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveMount--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

        mplew.write(0);
        writeLongMask(mplew, statups);

        //mplew.writeShort(0);
        //  mplew.writeInt(buffid); // 1902000 saddle
        //  mplew.writeInt(skillid); // skillid
        for (Pair statup : statups) {
            if ((((Integer) statup.getRight()).shortValue() >= 1000) && (((Integer) statup.getRight()).shortValue() != 1002)) {
                mplew.writeShort(((Integer) statup.getRight()).shortValue() + c.getGender() * 100);
            } else {
                mplew.write(0);
            }
            mplew.writeInt(buffid);
            mplew.writeInt(skillid);
            //log.info("第一段的MASK："+mask+" ");
        }
        mplew.writeInt(0); // Server tick value
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(2); // Total buffed times
        int a = giveBuff(c, buffid);
        if (a > 0) {
            mplew.write(a);
            // //////System.out.println("giveBuff在之后发了一个字节的a包 a的值："+a);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveMount-3191" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param buffid
     * @return
     */
    public static int giveBuff(MapleCharacter c, int buffid) {

        int a = 0;
        switch (buffid) {
            case 1121000:
            case 1221000:
            case 1321000:
            case 2121000:
            case 2221000:
            case 2321000:
            case 3121000:
            case 3221000:
            case 4121000:
            case 4221000:
            case 5121000:
            case 5221000:
            case 5321005:
            case 23121005:
            case 31121004:
            case 21121000:
            case 4341000:
            case 22171000:
            case 32121007:
            case 33121007:
            case 35121007:
            case 1002:
            case 10001002:
            case 20001002:
            case 9001001:
            case 14101003:
            case 8000:
            case 10008000:
            case 20008000:
            case 20018000:
            case 30008000:
            case 4101004:
            case 4201003:
            case 5101007:
                ///  case 双刀.暗影轻功:
                // case 风灵使者.风影漫步:
                a = 5;
                break;
            case 32101003: //黄色灵气
                // case 战法.进阶黄色灵气:
                a = 0x1D;
                break;
            case 33121006: //暴走状态
            case -2022458: //神兽的祝福
                a = 6;
                break;
            case 5111005: //超人变形
            case 5121003: //超级变身
            case 13111005:// 风灵使者 - 信天翁
            case 15111002:// 奇袭者 - 超级变身
                a = 7;
                break;
            //  case MapleBuffStat.冰雪矛:
            //    a = 2;
            //  break;
            case 5301003:
                a = 3;
                break;
        }
        return a;
    }

    /**
     *
     * @param statups
     * @param duration
     * @param skillid
     * @return
     */
    public static MaplePacket givePirate(List<Pair<MapleBuffStat, Integer>> statups, int duration, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("givePirate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
//        writeLongMask(mplew, statups);
//
//        mplew.writeShort(0);
//        for (Pair<MapleBuffStat, Integer> stat : statups) {
//            if (infusion) {
//                mplew.writeInt(0);
//            } else {
//                mplew.writeInt(stat.getRight().intValue());
//            }
//            mplew.writeLong(skillid);
//            mplew.writeZeroBytes(infusion ? 6 : 1);
//            mplew.writeShort(duration);
//        }
//        mplew.writeShort(infusion ? 600 : 0);
//        if (!infusion) {
//            mplew.write(1); //does this only come in dash?
//        }
//        if (ServerConstants.PACKET_ERROR_OFF) {
//            ServerConstants ERROR = new ServerConstants();
//            ERROR.setPACKET_ERROR("givePirate-3285" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
//        }
//        return mplew.getPacket();

        mplew.writeLong(0L);
        mplew.writeLong(MapleBuffStat.MORPH.getValue());
        mplew.writeShort(0);

        mplew.writeInt(skillid);
        mplew.writeZeroBytes(1);
        mplew.writeInt(duration);
        mplew.writeZeroBytes(6);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param statups
     * @param duration
     * @param cid
     * @param skillid
     * @return
     */
    public static MaplePacket giveForeignPirate(List<Pair<MapleBuffStat, Integer>> statups, int duration, int cid, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveForeignPirate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            if (infusion) {
                mplew.writeInt(0);
            } else {
                mplew.writeInt(stat.getRight());
            }
            mplew.writeLong(skillid);
            mplew.writeZeroBytes(infusion ? 7 : 1);
            mplew.writeShort(duration);//duration... seconds
        }
        mplew.writeShort(infusion ? 600 : 0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveForeignPirate-3310" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param skillid
     * @param mobid
     * @return
     */
    public static MaplePacket giveHoming(int skillid, int mobid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveHoming--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
        mplew.writeLong(0);

        mplew.writeShort(0);
        mplew.writeInt(1);
        mplew.writeLong(skillid);
        mplew.write(0);
        mplew.writeInt(mobid);
        mplew.writeShort(0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveHoming-3333" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param barammount
     * @return
     */
    public static MaplePacket giveForeignEnergyCharge(int cid, int barammount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        mplew.writeShort(0);
        mplew.writeShort(barammount);
        mplew.writeShort(0);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param bar
     * @param bufflength
     * @return
     */
    public static MaplePacket giveEnergyChargeTest(int bar, int bufflength) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveEnergyChargeTestA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        mplew.writeLong(0);
        /*        mplew.writeShort(0);
         mplew.writeInt(0);
         mplew.writeInt(1555445060); //?*/
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        mplew.writeLong(0); //skillid, but its 0 here
        mplew.write(0);
        mplew.writeInt(bar >= 10000 ? bufflength : 0);//short - bufflength...50
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveEnergyChargeTestA-3357" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param buffid
     * @param bufflength
     * @param statups
     * @return
     */
    public static MaplePacket givePirateBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("givePirateBuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        //  long mask = getLongMask(statups);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        mplew.writeLong(0L);
        mplew.writeShort(0);
        for (Pair statup : statups) {
            mplew.writeShort(((Integer) statup.getRight()).shortValue());
            mplew.writeShort(0);
            mplew.writeInt(buffid);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(bufflength);
        }
        mplew.writeShort(0);
        mplew.write(0);
        // ////System.out.println("A-------------2A");
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("givePirateBuff-3385" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param statups
     * @param duration
     * @return
     */
    public static MaplePacket 能量条(List<Pair<MapleBuffStat, Integer>> statups, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // long mask = getLongMask(statups);
        if (ServerConstants.调试输出封包) {
            System.out.println("能量条--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.write(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        mplew.writeLong(0L);
        mplew.write(0);
        for (Pair stat : statups) {
            mplew.writeInt(((Integer) stat.getRight()).shortValue());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(duration);
        }
        mplew.writeShort(0);
        mplew.write(2);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("能量条-3413" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param bar
     * @param bufflength
     * @return
     */
    public static MaplePacket giveEnergyChargeTest(int cid, int bar, int bufflength) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveEnergyChargeTestB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        //   mplew.writeLong(0);
        /*        mplew.writeShort(0);
         mplew.writeInt(0);
         mplew.writeInt(1555445060); //?*/
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        mplew.writeLong(0); //skillid, but its 0 here
        //mplew.write(0);
        mplew.writeInt(bar >= 10000 ? bufflength : 0);//short - bufflength...50
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveEnergyChargeTestB-3439" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param buffid
     * @param bufflength
     * @param statups
     * @param effect
     * @return
     */
    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveBuff--1------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        // 17 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 07 00 AE E1 3E 00 68 B9 01 00 00 00 00 00

        //lhc patch adds an extra int here
        writeLongMask(mplew, statups);

        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        mplew.writeShort(0); // delay,  wk charges have 600 here o.o
        mplew.writeShort(0); // combo 600, too
        if (effect == null || (!effect.isCombo() && !effect.isFinalAttack())) {
            mplew.write(0); // Test
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveBuff-3469" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param statups
     * @param skillid
     * @param level
     * @param duration
     * @return
     */
    public static MaplePacket giveDebuff(final List<Pair<MapleDisease, Integer>> statups, int skillid, int level, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveDebuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

        writeLongDiseaseMask(mplew, statups);

        for (Pair<MapleDisease, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(skillid);
            mplew.writeShort(level);
            mplew.writeInt(duration);
        }
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.writeShort(900); //Delay
        mplew.write(2);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveDebuff-3496" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param statups
     * @param skillid
     * @param level
     * @return
     */
    public static MaplePacket giveForeignDebuff(int cid, final List<Pair<MapleDisease, Integer>> statups, int skillid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveForeignDebuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        writeLongDiseaseMask(mplew, statups);

        /*if (skillid == 125) {
         mplew.writeShort(0);
         }*/
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(900); //Delay
        mplew.write(3);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveForeignDebuff-3523" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param mask
     * @param first
     * @return
     */
    public static MaplePacket cancelForeignDebuff(int cid, long mask, boolean first) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelForeignDebuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(first ? mask : 0);
        mplew.writeLong(first ? 0 : mask);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelForeignDebuff-3541" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param statups
     * @param itemId
     * @param skillId
     * @return
     */
    public static MaplePacket showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, int itemId, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showMonsterRiding--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        mplew.write(0);
        writeLongMask(mplew, statups);

        mplew.write(0);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showMonsterRiding-3567" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param cid
     * @param statups
     * @param effect
     * @return
     */
    public static MaplePacket giveForeignBuff(MapleCharacter c, int cid, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveForeignBuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        writeLongMask(mplew, statups);

        /*for (Pair<MapleBuffStat, Integer> statup : statups) {
         mplew.writeShort(statup.getRight().shortValue());
         } */
        for (Pair statup : statups) {
            if ((effect.isMorph()) && (((Integer) statup.getRight()) <= 255)) {
                mplew.write(((Integer) statup.getRight()).byteValue());
            } else if (effect.isPirateMorph()) {
                mplew.writeShort(((Integer) statup.getRight()).shortValue() + c.getGender() * 100);
            } else {
                mplew.writeShort(((Integer) statup.getRight()).shortValue());
            }
        }
        mplew.writeShort(0); // same as give_buff
        if ((effect.isMorph()) && (!effect.isPirateMorph())) {
            //  if (effect.isMorph()) {
            mplew.writeShort(0);
        }
        mplew.write(0);
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveForeignBuff-3605" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param statups
     * @return
     */
    public static MaplePacket cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelForeignBuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        writeLongMaskFromList(mplew, statups);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelForeignBuff-3623" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param statups
     * @return
     */
    public static MaplePacket cancelForeignBuffMONSTER(int cid, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelForeignBuffA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        writeLongMaskFromListM(mplew, statups);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelForeignBuffA-3641" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param statups
     * @return
     */
    public static MaplePacket cancelForeignBuffMONSTERS(int cid, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelForeignBuffA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);

        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 01 00"));
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00"));
        mplew.write(3);
        // writeLongMaskFromListM(mplew, statups);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelForeignBuffA-3662" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param statups
     * @return
     */
    public static MaplePacket cancelBuffMONSTER(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelBuffMONSTER--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        //if (statups != null) {
        writeLongMaskFromListM(mplew, statups);
        mplew.write(3);
        /*} else {
         mplew.writeLong(0);
         mplew.writeInt(0x40);
         mplew.writeInt(0x1000);
         }*/

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelBuffMONSTER-3686" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param statups
     * @return
     */
    public static MaplePacket cancelBuffMONSTERS(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelBuffMONSTERS--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        //if (statups != null) {
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 01 00"));
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00"));
        mplew.write(3);
        /*} else {
         mplew.writeLong(0);
         mplew.writeInt(0x40);
         mplew.writeInt(0x1000);
         }*/

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelBuffMONSTERS-3711" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param statups
     * @return
     */
    public static MaplePacket cancelBuff(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelBuffA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        //if (statups != null) {
        writeLongMaskFromList(mplew, statups);
        mplew.write(3);
        /*} else {
         mplew.writeLong(0);
         mplew.writeInt(0x40);
         mplew.writeInt(0x1000);
         }*/

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelBuffA-3735" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket cancelHoming() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelHoming--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

        mplew.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
        mplew.writeLong(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelHoming-3753" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mask
     * @param first
     * @return
     */
    public static MaplePacket cancelDebuff(long mask, boolean first) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelDebuff--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong(first ? mask : 0);
        mplew.writeLong(first ? 0 : mask);
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelDebuff-3771" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param levelup
     * @return
     */
    public static MaplePacket updateMount(MapleCharacter chr, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateMount--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        mplew.write(levelup ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateMount-3791" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @return
     */
    public static MaplePacket mountInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("mountInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("mountInfo-3811" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param slot
     * @return
     */
    public static MaplePacket getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPlayerShopNewVisitor--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
//        mplew.writeShort(c.getJob());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPlayerShopNewVisitor-3830" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param slot
     * @return
     */
    public static MaplePacket getPlayerShopRemoveVisitor(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPlayerShopRemoveVisitor--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 0" + slot));

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPlayerShopRemoveVisitor-3846" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @return
     */
    public static MaplePacket getTradePartnerAdd(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradePartnerAdd--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(4);
        mplew.write(1);
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
//        mplew.writeShort(c.getJob());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradePartnerAdd-3866" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param 现金交易
     * @return
     */
    public static MaplePacket getTradeInvite(MapleCharacter c, boolean 现金交易) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradeInvite--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(2);
        mplew.write(现金交易 ? 6 : 3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(0); // Trade ID

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradeInvite-3885" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param number
     * @param meso
     * @return
     */
    public static MaplePacket getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradeMesoSet--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xF);
        mplew.write(number);
        mplew.writeInt(meso);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradeMesoSet-3903" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param number
     * @param item
     * @return
     */
    public static MaplePacket getTradeItemAdd(byte number, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradeItemAdd--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xE);
        mplew.write(number);
        PacketHelper.addItemInfo(mplew, item, false, false, true);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradeItemAdd-3921" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param trade
     * @param number
     * @param 现金交易
     * @return
     */
    public static MaplePacket getTradeStart(MapleClient c, MapleTrade trade, byte number, boolean 现金交易) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradeStart--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(现金交易 ? 6 : 3);
        mplew.write(2);
        mplew.write(number);

        if (number == 1) {
            mplew.write(0);
            PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
//            mplew.writeShort(trade.getPartner().getChr().getJob());
        }
        mplew.write(number);
        PacketHelper.addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
//        mplew.writeShort(c.getPlayer().getJob());
        mplew.write(0xFF);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradeStart-3952" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradeConfirmation--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x10); //or 7? what

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradeConfirmation-3968" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param UserSlot
     * @param message
     * @return
     */
    public static MaplePacket TradeMessage(final byte UserSlot, final byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("TradeMessage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(UserSlot);
        mplew.write(message);
        //0x02 = cancelled
        //0x07 = success [tax is automated]
        //0x08 = unsuccessful
        //0x09 = "You cannot make the trade because there are some items which you cannot carry more than one."
        //0x0A = "You cannot make the trade because the other person's on a different map."

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("TradeMessage-3991" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param UserSlot
     * @param unsuccessful
     * @return
     */
    public static MaplePacket getTradeCancel(final byte UserSlot, final int unsuccessful) { //0 = canceled 1 = invent space 2 = pickuprestricted
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getTradeCancel--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(UserSlot);
        mplew.write(unsuccessful == 0 ? 2 : (unsuccessful == 1 ? 9 : 10));

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getTradeCancel-4009" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param msgType
     * @param talk
     * @param endBytes
     * @param type
     * @return
     */
    public static MaplePacket getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getNPCTalk--------------------");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.write(type); // 1 = No ESC, 3 = show character + no sec
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getNPCTalk-4030" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param sel
     * @return
     */
    public static final MaplePacket getMapSelection(final int npcid, final String sel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getMapSelection--------------------");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npcid);
        mplew.writeShort(0xD);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeMapleAsciiString(sel);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getMapSelection-4051" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param talk
     * @param card
     * @param args
     * @return
     */
    public static MaplePacket getNPCTalkStyle(int npc, String talk, int card, int[] args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getNPCTalkStyle--------------------");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(7);
        mplew.writeMapleAsciiString(talk);
        mplew.write(args.length);

        for (int i = 0; i < args.length; i++) {
            mplew.writeInt(args[i]);
        }
        mplew.writeInt(card);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getNPCTalkStyle-4075" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param talk
     * @param def
     * @param min
     * @param max
     * @return
     */
    public static MaplePacket getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getNPCTalkNum--------------------");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(3);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        mplew.writeInt(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getNPCTalkNum-4098" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param talk
     * @return
     */
    public static MaplePacket getNPCTalkText(int npc, String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getNPCTalkText--------------------");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(2);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(0);
        mplew.writeInt(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getNPCTalkText-4119" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param effect
     * @return
     */
    public static MaplePacket showForeignEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showForeignEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect); // 0 = Level up, 8 = job change

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showForeignEffect-4136" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param skillid
     * @param effectid
     * @return
     */
    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid) {
        if (ServerConstants.调试输出封包) {
            System.out.println("showBuffeffect--------------------");
        }
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    /**
     *
     * @param cid
     * @param skillid
     * @param effectid
     * @param direction
     * @return
     */
    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showBuffeffectA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid); //ehh?
        mplew.writeInt(skillid);
        mplew.write(2); //skill level = 1 for the lulz
        mplew.write(1); //actually skill level ? 0 = dosnt show
        if (direction != (byte) 3) {
            mplew.write(direction);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showBuffeffectA-4165" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param skillid
     * @param effectid
     * @return
     */
    public static MaplePacket showOwnBuffEffect(int skillid, int effectid) {
        if (ServerConstants.调试输出封包) {
            System.out.println("showOwnBuffEffectA--------------------");
        }
        return showOwnBuffEffect(skillid, effectid, (byte) 3);
    }

    /**
     *
     * @param skillid
     * @param effectid
     * @param direction
     * @return
     */
    public static MaplePacket showOwnBuffEffect(int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showOwnBuffEffectB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(0xA9); //skill level = 1 for the lulz
        mplew.write(1); //0 = doesnt show? or is this even here
        if (direction != (byte) 3) {
            mplew.write(direction);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showOwnBuffEffectB-4194" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket showItemLevelupEffect() {
        if (ServerConstants.调试输出封包) {
            System.out.println("showItemLevelupEffect--------------------");
        }
        return showSpecialEffect(17);
    }

    /**
     *
     * @return
     */
    public static MaplePacket showMonsterBookPickup() {
        return showSpecialEffect(14);
    }

    /**
     *
     * @return
     */
    public static MaplePacket showEquipmentLevelUp() {
        return showSpecialEffect(17);
    }

    /**
     *
     * @return
     */
    public static MaplePacket showItemLevelup() {
        return showSpecialEffect(17);
    }

    /**
     *
     * @param cid
     * @return
     */
    public static MaplePacket showForeignItemLevelupEffect(int cid) {
        if (ServerConstants.调试输出封包) {
            System.out.println("showForeignItemLevelupEffect--------------------");
        }
        return showSpecialEffect(cid, 17);
    }

    /**
     *
     * @param effect
     * @return
     */
    public static MaplePacket showSpecialEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showSpecialEffectA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showSpecialEffectA-4236" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param effect
     * @return
     */
    public static MaplePacket showSpecialEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showSpecialEffectB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showSpecialEffectB-4253" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param skillid
     * @param level
     * @param masterlevel
     * @param expiration
     * @return
     */
    public static MaplePacket updateSkill(int skillid, int level, int masterlevel, long expiration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateSkill--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        //   PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateSkill-4275" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param status
     * @return
     */
    public static final MaplePacket updateQuestMobKills(final MapleQuestStatus status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateQuestMobKills--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(status.getQuest().getId());
        mplew.write(1);

        final StringBuilder sb = new StringBuilder();
        for (final int kills : status.getMobKills().values()) {
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
        }
        mplew.writeMapleAsciiString(sb.toString());
        mplew.writeZeroBytes(8);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateQuestMobKills-4300" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param status
     * @return
     */
    public static MaplePacket 游戏屏幕中间黄色字体(String status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("游戏屏幕中间黄色字体--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(4761);
        mplew.write(1);

        /*final StringBuilder sb = new StringBuilder();
         for (final int kills : status.getMobKills().values()) {
         sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
         }*/
        mplew.writeMapleAsciiString(status);
        mplew.writeZeroBytes(8);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("游戏屏幕中间黄色字体-4325" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param status
     * @param id
     * @return
     */
    public static MaplePacket 游戏屏幕中间黄色字体(String status, int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("游戏屏幕中间黄色字体--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(id);
        mplew.write(1);

        /*final StringBuilder sb = new StringBuilder();
         for (final int kills : status.getMobKills().values()) {
         sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
         }*/
        mplew.writeMapleAsciiString(status);
        mplew.writeZeroBytes(8);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("游戏屏幕中间黄色字体-4350" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param id
     * @return
     */
    public static MaplePacket getShowQuestCompletion(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getShowQuestCompletion--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getShowQuestCompletion-4366" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param layout
     * @return
     */
    public static MaplePacket getKeymap(MapleKeyLayout layout) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("getKeymap--------------------");
        }
        mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
        //mplew.write(0);

        layout.writeData(mplew);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getKeymap4384" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param sender
     * @param channel
     * @param text
     * @return
     */
    public static MaplePacket getWhisper(String sender, int channel, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getWhisper--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1);
        mplew.writeMapleAsciiString(text);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getWhisper-4403" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param reply
     * @return
     */
    public static MaplePacket getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getWhisperReply--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);//  0x0 = cannot find char, 0x1 = success

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getWhisperReply-4421" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param mapid
     * @param buddy
     * @return
     */
    public static MaplePacket getFindReplyWithMap(String target, int mapid, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getFindReplyWithMap--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.writeZeroBytes(8); // ?? official doesn't send zeros here but whatever

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getFindReplyWithMap-4441" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param channel
     * @param buddy
     * @return
     */
    public static MaplePacket getFindReply(String target, int channel, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getFindReply--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getFindReply-4460" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket getInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getInventoryFull--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getInventoryFull-4477" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket getShowInventoryFull() {
        if (ServerConstants.调试输出封包) {
            System.out.println("getShowInventoryFull--------------------");
        }
        return getShowInventoryStatus(255);
    }

    /**
     *
     * @return
     */
    public static MaplePacket showItemUnavailable() {
        if (ServerConstants.调试输出封包) {
            System.out.println("showItemUnavailable--------------------");
        }
        return getShowInventoryStatus(254);
    }

    /**
     *
     * @param mode
     * @return
     */
    public static MaplePacket getShowInventoryStatus(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getShowInventoryStatus--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getShowInventoryStatus-4510" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcId
     * @param slots
     * @param items
     * @param meso
     * @return
     */
    public static MaplePacket getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getStorage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x16);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(0x7E);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        // mplew.writeShort(0);
        mplew.write((byte) items.size());
        for (IItem item : items) {
            if (GameConstants.is豆豆装备(item.getItemId())) {
                PacketHelper.addDDItemInfo(mplew, item, true, true, false);
            } else {
                PacketHelper.addItemInfo(mplew, item, true, true);
            }
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        //  mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getStorage-4544" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket getStorageFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getStorageFull--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x11);
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getStorageFull-4561" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param slots
     * @param meso
     * @return
     */
    public static MaplePacket mesoStorage(byte slots, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("mesoStorage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x13);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("mesoStorage-4582" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param slots
     * @param type
     * @param items
     * @return
     */
    public static MaplePacket storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("storeStorage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x0D);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("storeStorage-4605" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param slots
     * @param type
     * @param items
     * @return
     */
    public static MaplePacket takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("takeOutStorage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("takeOutStorage-4628" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param percent
     * @return
     */
    public static MaplePacket fairyPendantMessage(int type, int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("fairyPendantMessage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FAIRY_PEND_MSG.getValue());
        mplew.writeShort(21); // 0x15
        mplew.writeInt(0); // idk
        mplew.writeShort(0); // idk
        mplew.writeShort(percent); // percent
        mplew.writeShort(0); // idk

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("fairyPendantMessage-4648" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mode
     * @param charname
     * @param newfame
     * @return
     */
    public static MaplePacket giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveFameResponse--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveFameResponse-4668" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param status
     * @return
     */
    public static MaplePacket giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("giveFameErrorResponse--------------------");
        }
        /*  0：好的，用的是给名回应
         1：用户名是不直接进入
         2：15岁以下的用户是无法切换的名声。
         3：今天不能提高或下降的名气了。
         4：这一个月的这个角色不能提高或下降。
         5：获得名声，使用获得名声
         6：由于一个意想不到的错误，一个不被提高，也没有下降的程度*/
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("giveFameErrorResponse-4691" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mode
     * @param charnameFrom
     * @return
     */
    public static MaplePacket receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("receiveFame--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("receiveFame-4709" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param partyid
     * @return
     */
    public static MaplePacket partyCreated(int partyid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("partyCreated--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeInt(partyid);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.writeInt(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("partyCreated-4729" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @return
     */
    public static MaplePacket partyInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("partyInvite--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
//        mplew.writeInt(from.getLevel());
//        mplew.writeInt(from.getJob());
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("partyInvite-4750" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param message
     * @return
     */
    public static MaplePacket partyStatusMessage(int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("partyStatusMessageA--------------------");
        }
        /*	* 10: A beginner can't create a party.
         * 1/11/14/19: Your request for a party didn't work due to an unexpected error.
         * 13: You have yet to join a party.
         * 16: Already have joined a party.
         * 17: The party you're trying to join is already in full capacity.
         * 19: Unable to find the requested character in this channel.*/
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("partyStatusMessageA-4772" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param message
     * @param charname
     * @return
     */
    public static MaplePacket partyStatusMessage(int message, String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("partyStatusMessageB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message); // 23: 'Char' have denied request to the party.
        mplew.writeMapleAsciiString(charname);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("partyStatusMessageB-4789" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
        if (ServerConstants.调试输出封包) {
            System.out.println("addPartyStatus--------------------");
        }
        List<MaplePartyCharacter> partymembers = new ArrayList<>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(StringUtil.getRightPaddedStr(partychar.getName(), '\000', 13));
            // lew.writeAsciiString(partychar.getName(), 15);
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-2);
            }
        }
        lew.writeInt(party.getLeader().getId());
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapid());
            } else {
                lew.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                //    lew.writeInt(partychar.getDoorSkill());
                lew.writeInt(partychar.getDoorPosition().x);
                lew.writeInt(partychar.getDoorPosition().y);
            } else {
                lew.writeInt(0);
                lew.writeInt(0);
                lew.writeInt(0);
                lew.writeInt(0);
                //  lew.writeInt(leaving ? 999999999 : 0);
                //  lew.writeInt(leaving ? 999999999 : 0);
                // lew.writeLong(leaving ? -1 : 0);
            }
        }
    }

    /**
     *
     * @param forChannel
     * @param party
     * @param op
     * @param target
     * @return
     */
    public static MaplePacket updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateParty--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                mplew.write(0xC);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                mplew.write(op == PartyOperation.DISBAND ? 0 : 1);
                if (op == PartyOperation.DISBAND) {
                    mplew.writeInt(target.getId());
                } else {
                    mplew.write(op == PartyOperation.EXPEL ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, false);
                    // addPartyStatus(forChannel, party, mplew, op == PartyOperation.LEAVE);
                }
                break;
            case JOIN:
                mplew.write(0xF);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                mplew.write(0x7);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, false);
                //addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                break;
            case CHANGE_LEADER:
                //  case CHANGE_LEADER_DC:
                mplew.write(0x1A); //test
                mplew.writeInt(target.getId());
                mplew.write(0);
                // mplew.write(op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0);
                break;
            //1D = expel function not available in this map.
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateParty-4897" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param townId
     * @param targetId
     * @param skillId
     * @param position
     * @return
     */
    public static MaplePacket partyPortal(int townId, int targetId, int skillId, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("partyPortal--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x23);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
//        mplew.writeInt(skillId);
        mplew.writePos(position);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("partyPortal-4917" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param curhp
     * @param maxhp
     * @return
     */
    public static MaplePacket updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updatePartyMemberHP--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updatePartyMemberHP-4935" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static MaplePacket multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("multiChat--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode); //  0 buddychat; 1 partychat; 2 guildchat
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("multiChat-4953" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param time
     * @return
     */
    public static MaplePacket getClock(int time) { // time in seconds
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getClock--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getClock-4970" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param hour
     * @param min
     * @param sec
     * @return
     */
    public static MaplePacket getClockTime(int hour, int min, int sec) { // Current Time
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getClockTime--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getClockTime-4989" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mist
     * @return
     */
    public static MaplePacket spawnMist(final MapleMist mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnMist--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.writeInt(mist.isMobMist() ? 0 : (mist.isPoisonMist() != 0 ? 1 : 2)); //2 = invincible, so put 1 for recovery aura
        mplew.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            mplew.writeInt(mist.getSourceSkill().getId());
        } else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnMist-5019" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param eruption
     * @return
     */
    public static MaplePacket removeMist(final int oid, boolean eruption) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeMist--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        //mplew.write(eruption ? 1 : 0); // 117

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeMist-5036" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param summonSkillId
     * @param damage
     * @param unkByte
     * @param monsterIdFrom
     * @return
     */
    public static MaplePacket damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("damageSummon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("damageSummon-5057" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param message
     * @return
     */
    public static MaplePacket buddylistMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("buddylistMessage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("buddylistMessage-5073" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param buddylist
     * @return
     */
    public static MaplePacket updateBuddylist(Collection<BuddylistEntry> buddylist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateBuddylist--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());

        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId());
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\000', 13));
                // mplew.writeAsciiString(buddy.getName(), 15);
                mplew.write(0);
                mplew.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel() - 1);
                // mplew.writeAsciiString(buddy.getGroup(), 17);
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getGroup(), '\000', 17));
            }
        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateBuddylist-5104" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cidFrom
     * @param nameFrom
     * @param levelFrom
     * @param jobFrom
     * @return
     */
    public static MaplePacket requestBuddylistAdd(int cidFrom, String nameFrom, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("requestBuddylistAdd--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\000', 13));
        mplew.write(1);
        mplew.write(5);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeAsciiString(StringUtil.getRightPaddedStr("群未定", '\000', 17));
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("requestBuddylistAdd-5130" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param characterid
     * @param channel
     * @return
     */
    public static MaplePacket updateBuddyChannel(int characterid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateBuddyChannel--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateBuddyChannel-5149" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param characterid
     * @param itemid
     * @return
     */
    public static MaplePacket itemEffect(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("itemEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("itemEffect-5166" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param characterid
     * @param itemid
     * @return
     */
    public static MaplePacket itemEffects(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("itemEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("itemEffect-5183" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param capacity
     * @return
     */
    public static MaplePacket updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateBuddyCapacity--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateBuddyCapacity-5200" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param characterid
     * @param itemid
     * @return
     */
    public static MaplePacket showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showChair--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showChair-5217" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param id
     * @return
     */
    public static MaplePacket cancelChair(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("cancelChair--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("cancelChair-5237" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param reactor
     * @return
     */
    public static MaplePacket spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("spawnReactor--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.write(reactor.getFacingDirection()); // stance
        //mplew.writeMapleAsciiString(reactor.getName());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnReactor-5258" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param reactor
     * @param stance
     * @return
     */
    public static MaplePacket triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("triggerReactor--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.writeInt(stance);
        //mplew.write(0);
        // mplew.write(5); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("triggerReactor-5279" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param reactor
     * @return
     */
    public static MaplePacket destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("destroyReactor--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("destroyReactor-5297" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param song
     * @return
     */
    public static MaplePacket musicChange(String song) {
        if (ServerConstants.调试输出封包) {
            System.out.println("musicChange--------------------");
        }
        return environmentChange(song, 6);
    }

    /**
     *
     * @param effect
     * @return
     */
    public static MaplePacket showEffect(String effect) {
        if (ServerConstants.调试输出封包) {
            System.out.println("showEffect--------------------");
        }
        return environmentChange(effect, 3);
    }

    /**
     *
     * @param sound
     * @return
     */
    public static MaplePacket playSound(String sound) {
        if (ServerConstants.调试输出封包) {
            System.out.println("playSound--------------------");
        }
        return environmentChange(sound, 4);
    }

    /**
     *
     * @param env
     * @param mode
     * @return
     */
    public static MaplePacket environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("environmentChange--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("environmentChange-5335" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param env
     * @param mode
     * @return
     */
    public static MaplePacket environmentMove(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("environmentMove--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
        mplew.writeMapleAsciiString(env);
        mplew.writeInt(mode);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("environmentMove-5352" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @param itemid
     * @param active
     * @return
     */
    public static MaplePacket startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("startMapEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);

        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("startMapEffect-5372" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket removeMapEffect() {
        if (ServerConstants.调试输出封包) {
            System.out.println("removeMapEffect--------------------");
        }
        return startMapEffect(null, 0, false);
    }

    /**
     *
     * @param c
     * @return
     */
    public static MaplePacket fuckGuildInfo(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("fuckGuildInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x1A); //signature for showing guild info

        String Prefix = "";
        if (c.getPrefix() == 1) {
            Prefix = "[技術團隊成員]";
        }
        if (c.getPrefix() == 2) {
            Prefix = "[遊戲管理成員]";
        }
        if (c.getPrefix() == 3) {
            Prefix = "[活動辦理成員]";
        }

        mplew.write(1); //bInGuild
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(Prefix);
        //mplew.writeMapleAsciiString("");
        mplew.write(0);//members.size()
        mplew.writeInt(0);//mgc.getId()
        //mplew.writeAsciiString("");//mgc.getName(), 15
        mplew.writeInt(0);//mgc.getJobId()
        mplew.writeInt(0);//mgc.getLevel()
        mplew.writeInt(0);//mgc.getGuildRank()
        mplew.writeInt(0);//mgc.isOnline() ? 1 : 0
        mplew.writeInt(0);//signature
        mplew.writeInt(0);//mgc.getAllianceRank();
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.write(0);
        //mplew.writeMapleAsciiString("");
        mplew.writeInt(0);
        mplew.writeInt(0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("fuckGuildInfo-5427" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @return
     */
    public static MaplePacket showGuildInfo(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showGuildInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x1A); //signature for showing guild info

        if (c == null || c.getMGC() == null) { //show empty guild (used for leaving, expelled)
            mplew.write(0);
            if (ServerConstants.PACKET_ERROR_OFF) {
                ServerConstants ERROR = new ServerConstants();
                ERROR.setPACKET_ERROR("showGuildInfo-5445" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
            }
            return mplew.getPacket();
        }
        MapleGuild g = World.Guild.getGuild(c.getGuildId());
        if (g == null) { //failed to read from DB - don't show a guild
            mplew.write(0);
            if (ServerConstants.PACKET_ERROR_OFF) {
                ServerConstants ERROR = new ServerConstants();
                ERROR.setPACKET_ERROR("showGuildInfo-5454" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
            }
            return mplew.getPacket();
        } else {
            MapleGuildCharacter mgc = g.getMGC(c.getId());
            c.setGuildRank(mgc.getGuildRank());
        }
        mplew.write(1); //bInGuild
        getGuildInfo(mplew, g);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showGuildInfo-5465" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {

        if (ServerConstants.调试输出封包) {
            System.out.println("getGuildInfo--------------------");
        }
        //System.out.println("writegetGuildInfo");
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
    }

    private static void getGuildInfo2(MaplePacketLittleEndianWriter mplew, MapleGuild guild, MapleCharacter chr) {

        if (ServerConstants.调试输出封包) {
            System.out.println("getGuildInfo2--------------------");
        }
        //System.out.println("writegetGuildInfo2");
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
    }

    /**
     *
     * @param gid
     * @param cid
     * @param bOnline
     * @return
     */
    public static MaplePacket guildMemberOnline(int gid, int cid, boolean bOnline) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildMemberOnline--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(61);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildMemberOnline-5528" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @param charName
     * @param levelFrom
     * @param jobFrom
     * @return
     */
    public static MaplePacket guildInvite(int gid, String charName, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildInvite--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);
//        mplew.writeInt(levelFrom);
//        mplew.writeInt(jobFrom);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildInvite-5548" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param charname
     * @return
     */
    public static MaplePacket denyGuildInvitation(String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("denyGuildInvitation--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x37);
        mplew.writeMapleAsciiString(charname);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("denyGuildInvitation-5565" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param code
     * @return
     */
    public static MaplePacket genericGuildMessage(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("genericGuildMessage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("genericGuildMessage-5581" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mgc
     * @return
     */
    public static MaplePacket newGuildMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("newGuildMember--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\000', 13));
        // mplew.writeAsciiString(mgc.getName(),15);
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
        mplew.writeInt(mgc.getAllianceRank()); //should always 3

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("newGuildMember-5607" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    //someone leaving, mode == 0x2c for leaving, 0x2f for expelled

    /**
     *
     * @param mgc
     * @param bExpelled
     * @return
     */
    public static MaplePacket memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("memberLeft--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 0x2f : 0x2c);

        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("memberLeft-5628" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mgc
     * @return
     */
    public static MaplePacket changeRank(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("changeRank--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x40);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("changeRank-5647" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @param notice
     * @return
     */
    public static MaplePacket guildNotice(int gid, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildNotice--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x44);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildNotice-5665" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mgc
     * @return
     */
    public static MaplePacket guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildMemberLevelJobUpdate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3C);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildMemberLevelJobUpdate-5685" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @param ranks
     * @return
     */
    public static MaplePacket rankTitleChange(int gid, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("rankTitleChange--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(62);
        mplew.writeInt(gid);

        for (String r : ranks) {
            mplew.writeMapleAsciiString(r);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("rankTitleChange-5705" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @return
     */
    public static MaplePacket guildDisband(int gid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildDisband--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(50);
        mplew.writeInt(gid);
        mplew.write(1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildDisband-5723" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @param bg
     * @param bgcolor
     * @param logo
     * @param logocolor
     * @return
     */
    public static MaplePacket guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildEmblemChange--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x42);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildEmblemChange-5744" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @param capacity
     * @return
     */
    public static MaplePacket guildCapacityChange(int gid, int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("guildCapacityChange--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3a);
        mplew.writeInt(gid);
        mplew.write(capacity);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("guildCapacityChange-5762" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @param expelledGuild
     * @param expelled
     * @return
     */
    public static MaplePacket removeGuildFromAlliance(MapleGuildAlliance alliance, MapleGuild expelledGuild, boolean expelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeGuildFromAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x10);
        addAllianceInfo(mplew, alliance);
        getGuildInfo(mplew, expelledGuild);
        mplew.write(expelled ? 1 : 0); //1 = expelled, 0 = left
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeGuildFromAlliance-5780" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @param in
     * @return
     */
    public static MaplePacket changeAlliance(MapleGuildAlliance alliance, final boolean in) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("changeAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x01);
        mplew.write(in ? 1 : 0);
        mplew.writeInt(in ? alliance.getId() : 0);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < noGuilds; i++) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.write(noGuilds);
        for (int i = 0; i < noGuilds; i++) {
            mplew.writeInt(g[i].getId());
            //must be world
            Collection<MapleGuildCharacter> members = g[i].getMembers();
            mplew.writeInt(members.size());
            for (MapleGuildCharacter mgc : members) {
                mplew.writeInt(mgc.getId());
                mplew.write(in ? mgc.getAllianceRank() : 0);
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("changeAlliance-5816" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param allianceid
     * @param newLeader
     * @param oldLeader
     * @return
     */
    public static MaplePacket changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("changeAllianceLeaderA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x02);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("changeAllianceLeaderA-5834" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param allianceid
     * @param newLeader
     * @param oldLeader
     * @return
     */
    public static MaplePacket updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateAllianceLeaderB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x19);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateAllianceLeaderB-5852" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param allianceName
     * @param inviter
     * @return
     */
    public static MaplePacket sendAllianceInvite(String allianceName, MapleCharacter inviter) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendAllianceInvite--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x03);
        mplew.writeInt(inviter.getGuildId());
        mplew.writeMapleAsciiString(inviter.getName());
        //alliance invite did NOT change
        mplew.writeMapleAsciiString(allianceName);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendAllianceInvite-5871" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @param guild
     * @param add
     * @return
     */
    public static MaplePacket changeGuildInAlliance(MapleGuildAlliance alliance, MapleGuild guild, final boolean add) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("changeGuildInAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x04);
        mplew.writeInt(add ? alliance.getId() : 0);
        mplew.writeInt(guild.getId());
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.writeInt(members.size());
        for (MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
            mplew.write(add ? mgc.getAllianceRank() : 0);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("changeGuildInAlliance-5894" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param allianceid
     * @param player
     * @return
     */
    public static MaplePacket changeAllianceRank(int allianceid, MapleGuildCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("changeAllianceRank--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(allianceid);
        mplew.writeInt(player.getId());
        mplew.writeInt(player.getAllianceRank());
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("changeAllianceRank-5912" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @return
     */
    public static MaplePacket createGuildAlliance(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("createGuildAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0F);
        addAllianceInfo(mplew, alliance);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        for (MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("createGuildAlliance-5939" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @return
     */
    public static MaplePacket getAllianceInfo(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getAllianceInfo--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0C);
        mplew.write(alliance == null ? 0 : 1); //in an alliance
        if (alliance != null) {
            addAllianceInfo(mplew, alliance);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getAllianceInfo-5958" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @return
     */
    public static MaplePacket getAllianceUpdate(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getAllianceUpdate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x17);
        addAllianceInfo(mplew, alliance);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getAllianceUpdate-5973" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @return
     */
    public static MaplePacket getGuildAlliance(MapleGuildAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getGuildAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0D);
        if (alliance == null) {
            mplew.writeInt(0);
            if (ServerConstants.PACKET_ERROR_OFF) {
                ServerConstants ERROR = new ServerConstants();
                ERROR.setPACKET_ERROR("getGuildAlliance-5991" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
            }
            return mplew.getPacket();
        }
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.writeInt(noGuilds);
        for (MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getGuildAlliance-6009" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @param newGuild
     * @return
     */
    public static MaplePacket addGuildToAlliance(MapleGuildAlliance alliance, MapleGuild newGuild) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("addGuildToAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x12);
        addAllianceInfo(mplew, alliance);
        mplew.writeInt(newGuild.getId()); //???
        getGuildInfo(mplew, newGuild);
        mplew.write(0); //???
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("addGuildToAlliance-6028" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addAllianceInfo(MaplePacketLittleEndianWriter mplew, MapleGuildAlliance alliance) {
        if (ServerConstants.调试输出封包) {
            System.out.println("addAllianceInfo--------------------");
        }
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRank(i));
        }
        mplew.write(alliance.getNoGuilds());
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            mplew.writeInt(alliance.getGuildId(i));
        }
        mplew.writeInt(alliance.getCapacity()); // ????
        mplew.writeMapleAsciiString(alliance.getNotice());
    }

    /**
     *
     * @param alliance
     * @param gid
     * @param id
     * @param online
     * @return
     */
    public static MaplePacket allianceMemberOnline(int alliance, int gid, int id, boolean online) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("allianceMemberOnline--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0E);
        mplew.writeInt(alliance);
        mplew.writeInt(gid);
        mplew.writeInt(id);
        mplew.write(online ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("allianceMemberOnline-6065" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mgc
     * @param allianceid
     * @return
     */
    public static MaplePacket updateAlliance(MapleGuildCharacter mgc, int allianceid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x18);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateAlliance-6086" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param allianceid
     * @param mgc
     * @return
     */
    public static MaplePacket updateAllianceRank(int allianceid, MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateAllianceRank--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1B);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getAllianceRank());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateAllianceRank-6105" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param alliance
     * @return
     */
    public static MaplePacket disbandAlliance(int alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("disbandAlliance--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1D);
        mplew.writeInt(alliance);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("disbandAlliance-6122" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param bbs
     * @param start
     * @return
     */
    public static MaplePacket BBSThreadList(final List<MapleBBSThread> bbs, int start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("BBSThreadList--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(6);

        if (bbs == null) {
            mplew.write(0);
            mplew.writeLong(0);
            if (ServerConstants.PACKET_ERROR_OFF) {
                ServerConstants ERROR = new ServerConstants();
                ERROR.setPACKET_ERROR("BBSThreadList-6141" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
            }
            return mplew.getPacket();
        }
        int threadCount = bbs.size();
        MapleBBSThread notice = null;
        for (MapleBBSThread b : bbs) {
            if (b.isNotice()) { //notice
                notice = b;
                break;
            }
        }
        final int ret = (notice == null ? 0 : 1);
        mplew.write(ret);
        if (notice != null) { //has a notice
            addThread(mplew, notice);
            threadCount--; //one thread didn't count (because it's a notice)
        }
        if (threadCount < start) { //seek to the thread before where we start
            //uh, we're trying to start at a place past possible
            start = 0;
        }
        //each page has 10 threads, start = page # in packet but not here
        mplew.writeInt(threadCount);
        final int pages = Math.min(10, threadCount - start);
        mplew.writeInt(pages);

        for (int i = 0; i < pages; i++) {
            addThread(mplew, bbs.get(start + i + ret)); //because 0 = notice
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("BBSThreadList-6173" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addThread(MaplePacketLittleEndianWriter mplew, MapleBBSThread rs) {
        if (ServerConstants.调试输出封包) {
            System.out.println("addThread--------------------");
        }
        mplew.writeInt(rs.localthreadID);
        mplew.writeInt(rs.ownerID);
        mplew.writeMapleAsciiString(rs.name);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
        mplew.writeInt(rs.icon);
        mplew.writeInt(rs.getReplyCount());
    }

    /**
     *
     * @param thread
     * @return
     */
    public static MaplePacket showThread(MapleBBSThread thread) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showThread--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(7);

        mplew.writeInt(thread.localthreadID);
        mplew.writeInt(thread.ownerID);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
        mplew.writeMapleAsciiString(thread.name);
        mplew.writeMapleAsciiString(thread.text);
        mplew.writeInt(thread.icon);
        mplew.writeInt(thread.getReplyCount());
        for (MapleBBSReply reply : thread.replies.values()) {
            mplew.writeInt(reply.replyid);
            mplew.writeInt(reply.ownerID);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
            mplew.writeMapleAsciiString(reply.content);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showThread-6214" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param all
     * @return
     */
    public static MaplePacket showGuildRanks(int npcid, List<GuildRankingInfo> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showGuildRanks--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());

        for (GuildRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getGP());
            mplew.writeInt(info.getLogo());
            mplew.writeInt(info.getLogoColor());
            mplew.writeInt(info.getLogoBg());
            mplew.writeInt(info.getLogoBgColor());
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showGuildRanks-6241" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param gid
     * @param GP
     * @return
     */
    public static MaplePacket updateGP(int gid, int GP) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateGP--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(GP);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateGP-6259" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @param skillId
     * @param level
     * @param flags
     * @param speed
     * @param unk
     * @return
     */
    public static MaplePacket skillEffect(MapleCharacter from, int skillId, byte level, byte flags, byte speed, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("skillEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);
        mplew.write(unk); // Direction ??

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("skillEffect-6280" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @param skillId
     * @return
     */
    public static MaplePacket skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("skillCancel--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("skillCancel-6297" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mobid
     * @param success
     * @return
     */
    public static MaplePacket showMagnet(int mobid, byte success) { // Monster Magnet
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showMagnet--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showMagnet-6314" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param hint
     * @param width
     * @param height
     * @return
     */
    public static MaplePacket sendHint(String hint, int width, int height) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendHint--------------------");
        }
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendHint-6342" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @param messengerid
     * @return
     */
    public static MaplePacket messengerInvite(String from, int messengerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("messengerInvite--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0x05);
        mplew.writeInt(messengerid);
        mplew.write(0x00);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("messengerInvite-6362" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @param chr
     * @param position
     * @param channel
     * @return
     */
    public static MaplePacket addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("addMessengerPlayer--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("addMessengerPlayer-6382" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param position
     * @return
     */
    public static MaplePacket removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeMessengerPlayer--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeMessengerPlayer-6399" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param from
     * @param chr
     * @param position
     * @param channel
     * @return
     */
    public static MaplePacket updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateMessengerPlayer--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x07);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateMessengerPlayer-6419" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param position
     * @return
     */
    public static MaplePacket joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("joinMessenger--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("joinMessenger-6436" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param text
     * @return
     */
    public static MaplePacket messengerChat(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("messengerChat--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("messengerChat-6453" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param text
     * @param mode
     * @param mode2
     * @return
     */
    public static MaplePacket messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("messengerNote--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("messengerNote-6471" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param buddy
     * @return
     */
    public static MaplePacket getFindReplyWithCS(String target, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getFindReplyWithCS--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getFindReplyWithCS-6490" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param buddy
     * @return
     */
    public static MaplePacket getFindReplyWithMTS(String target, final boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getFindReplyWithMTS--------------------");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(0);
        mplew.writeInt(-1);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getFindReplyWithMTS-6509" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket showEquipEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showEquipEffectA--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showEquipEffectA-6524" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param team
     * @return
     */
    public static MaplePacket showEquipEffect(int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showEquipEffectB--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showEquipEffectB-6539" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param summonSkillId
     * @param newStance
     * @return
     */
    public static MaplePacket summonSkill(int cid, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("summonSkill--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("summonSkill-6557" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param sid
     * @param time
     * @return
     */
    public static MaplePacket skillCooldown(int sid, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("skillCooldown--------------------");
        }
        mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(time);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("skillCooldown-6574" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param skillid
     * @param maxlevel
     * @param canuse
     * @param success
     * @return
     */
    public static MaplePacket useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("useSkillBook--------------------");
        }
        mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
//        mplew.write(0); //?
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("useSkillBook-6596" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param macros
     * @return
     */
    public static MaplePacket getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getMacros--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count); // number of macros
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getMacros-6627" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param name
     * @param score
     * @param empty
     * @return
     */
    public static MaplePacket updateAriantPQRanking(String name, int score, boolean empty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("updateAriantPQRanking--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ARIANT_PQ_START.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateAriantPQRanking-6646" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mobid
     * @param itemid
     * @param success
     * @return
     */
    public static MaplePacket catchMob(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // !packet 87 01 
        // mplew.writeShort(SendPacketOpcode.CATCH_MOB.getValue());
        mplew.writeShort(0x50);
        mplew.write(success);
        mplew.writeInt(itemid);
        mplew.writeInt(mobid);

        return mplew.getPacket();
    }

    /**
     *
     * @param mobid
     * @param itemid
     * @param success
     * @return
     */
    public static MaplePacket catchMonster(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("catchMonster--------------------");
        }
        if (itemid == 2270002) {
            mplew.writeShort(SendPacketOpcode.CATCH_ARIANT.getValue());
        } else {
            mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
        }
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("catchMonster6668" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket showAriantScoreBoard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showAriantScoreBoard--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());
        /*mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write(0);
         mplew.writeInt(0);
         mplew.writeInt(0);*/
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showAriantScoreBoard-6687" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @return
     */
    public static MaplePacket boatPacket(boolean type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("boatPacket--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(type ? 1 : 2);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("boatPacket-6702" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param effect
     * @return
     */
    public static MaplePacket boatPacket(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("boatPacket--------------------");
        }
        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had 3: boat leaves

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("boatPacket-6720" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param effect
     * @return
     */
    public static MaplePacket boatEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("boatEffect--------------------");
        }
        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(SendPacketOpcode.BOAT_EFF.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had the other ones o.o

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("boatEffect-6738" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param remove
     * @param Package
     * @return
     */
    public static MaplePacket removeItemFromDuey(boolean remove, int Package) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("removeItemFromDuey--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(0x17);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeItemFromDuey-6756" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param operation
     * @param packages
     * @return
     */
    public static MaplePacket sendDuey(byte operation, List<MapleDueyActions> packages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendDuey--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(operation);

        switch (operation) {
            case 8: { // Request 13 Digit AS
                mplew.write(1);
                // 0xFF = error
                break;
            }
            case 9: { // Open duey
                mplew.write(0);
                mplew.write(packages.size());

                for (MapleDueyActions dp : packages) {
                    mplew.writeInt(dp.getPackageId());
                    mplew.writeAsciiString(dp.getSender(), 15);
                    mplew.writeInt(dp.getMesos());
                    mplew.writeLong(KoreanDateUtil.getFileTimestamp(dp.getSentTime(), false));
                    mplew.writeZeroBytes(205);

                    if (dp.getItem() != null) {
                        mplew.write(1);
                        PacketHelper.addItemInfo(mplew, dp.getItem(), true, true);
                    } else {
                        mplew.write(0);
                    }
                    //System.out.println("Package has been sent in packet: " + dp.getPackageId());
                }
                mplew.write(0);
                break;
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendDuey-6081" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket Mulung_DojoUp2() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("Mulung_DojoUp2--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x07);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("Mulung_DojoUp2-6817" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket dojoWarpUp() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("dojoWarpUp--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DOJO_WARP_UP.getValue());
        mplew.write(0);
        mplew.write(6);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("dojoWarpUp-6832" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @return
     */
    public static MaplePacket showQuestMsg(final String msg) {//月秒
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showQuestMsg--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(msg);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showQuestMsg-6847" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param m
     * @return
     */
    public static MaplePacket HSText(String m) {
        if (ServerConstants.调试输出封包) {
            System.out.println("Mulung_Pts--------------------");
        }
        return showQuestMsg(m);
    }

    /**
     *
     * @param recv
     * @param total
     * @return
     */
    public static MaplePacket Mulung_Pts(int recv, int total) {
        if (ServerConstants.调试输出封包) {
            System.out.println("Mulung_Pts--------------------");
        }
        return showQuestMsg("你獲得 " + recv + " 修煉點數, 目前累積了 " + total + " 點修煉點數");
    }

    /**
     *
     * @param questionSet
     * @param questionId
     * @param askQuestion
     * @return
     */
    public static MaplePacket showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showOXQuiz--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showOXQuiz-6877" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket leftKnockBack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("leftKnockBack--------------------");
        }
        mplew.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("leftKnockBack-6890" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param ball1
     * @param ball2
     * @return
     */
    public static MaplePacket rollSnowball(int type, MapleSnowballs ball1, MapleSnowballs ball2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("rollSnowball--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
        mplew.write(type); // 0 = normal, 1 = rolls from start to end, 2 = down disappear, 3 = up disappear, 4 = move
        mplew.writeInt(ball1 == null ? 0 : (ball1.getSnowmanHP() / 75));
        mplew.writeInt(ball2 == null ? 0 : (ball2.getSnowmanHP() / 75));
        mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
        mplew.writeZeroBytes(11);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("rollSnowball-6910" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket enterSnowBall() {
        if (ServerConstants.调试输出封包) {
            System.out.println("enterSnowBall--------------------");
        }
        return rollSnowball(0, null, null);
    }

    /**
     *
     * @param team
     * @param damage
     * @param distance
     * @param delay
     * @return
     */
    public static MaplePacket hitSnowBall(int team, int damage, int distance, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("hitSnowBall--------------------");
        }
        mplew.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("hitSnowBall-6934" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param team
     * @param message
     * @return
     */
    public static MaplePacket snowballMessage(int team, int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("snowballMessage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeInt(message);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("snowballMessage6949" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @return
     */
    public static MaplePacket finishedSort(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("finishedSort--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
        mplew.write(1);
        mplew.write(type);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("finishedSort-6964" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    // 00 01 00 00 00 00

    /**
     *
     * @param coconutscore
     * @return
     */
    public static MaplePacket coconutScore(int[] coconutscore) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("coconutScore--------------------");
        }
        mplew.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(coconutscore[0]);
        mplew.writeShort(coconutscore[1]);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("coconutScore-6980" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param spawn
     * @param id
     * @param type
     * @return
     */
    public static MaplePacket hitCoconut(boolean spawn, int id, int type) {
        // FF 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("hitCoconut--------------------");
        }
        mplew.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
        if (spawn) {
            mplew.write(0);
            mplew.writeInt(0x80);
        } else {
            mplew.writeInt(id);
            mplew.write(type); // What action to do for the coconut.
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("hitCoconut-7001" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @return
     */
    public static MaplePacket finishedGather(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("finishedGather--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FINISH_GATHER.getValue());
        mplew.write(1);
        mplew.write(type);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("finishedGather-7016" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @return
     */
    public static MaplePacket yellowChat(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("yellowChat--------------------");
        }
        mplew.writeShort(SendPacketOpcode.YELLOW_CHAT.getValue());
        mplew.write(-1); //could be something like mob displaying message.
        mplew.writeMapleAsciiString(msg);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("yellowChat-7031" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemId
     * @param quantity
     * @param itemId2
     * @param quantity2
     * @return
     */
    public static MaplePacket getPeanutResult(int itemId, short quantity, int itemId2, short quantity2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPeanutResult--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PIGMI_REWARD.getValue());
        mplew.writeInt(itemId);
        mplew.writeShort(quantity);
        mplew.writeInt(5060003);
        mplew.writeInt(itemId2);
        mplew.writeInt(quantity2);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPeanutResult-7051" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param family
     * @param level
     * @param name
     * @return
     */
    public static MaplePacket sendLevelup(boolean family, int level, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendLevelup--------------------");
        }
        mplew.writeShort(SendPacketOpcode.LEVEL_UPDATE.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendLevelup-7069" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param family
     * @param name
     * @return
     */
    public static MaplePacket sendMarriage(boolean family, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendMarriage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeMapleAsciiString(name);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendMarriage-7086" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param family
     * @param jobid
     * @param name
     * @return
     */
    public static MaplePacket sendJobup(boolean family, int jobid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendJobup--------------------");
        }
        mplew.writeShort(SendPacketOpcode.JOB_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeInt(jobid); //or is this a short
        mplew.writeMapleAsciiString(name);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendJobup-7104" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param spawned
     * @param time
     * @return
     */
    public static MaplePacket showZakumShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showZakumShrine--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showZakumShrine-7119" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param spawned
     * @param time
     * @return
     */
    public static MaplePacket showHorntailShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showHorntailShrine--------------------");
        }
        mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showHorntailShrine-7134" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param spawned
     * @param time
     * @return
     */
    public static MaplePacket showChaosZakumShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showChaosZakumShrine--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showChaosZakumShrine-7149" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param spawned
     * @param time
     * @return
     */
    public static MaplePacket showChaosHorntailShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showChaosHorntailShrine--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showChaosHorntailShrine-7164" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket stopClock() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("stopClock--------------------");
        }
        mplew.writeShort(SendPacketOpcode.STOP_CLOCK.getValue());
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("stopClock-7177" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param d
     * @return
     */
    public static MaplePacket spawnDragon(MapleDragon d) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("spawnDragon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writeInt(d.getPosition().x);
        mplew.writeInt(d.getPosition().y);
        mplew.write(d.getStance()); //stance?
        mplew.writeShort(0);
        mplew.writeShort(d.getJobId());
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnDragon-7196" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chrid
     * @return
     */
    public static MaplePacket removeDragon(int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("removeDragon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
        mplew.writeInt(chrid);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeDragon-7210" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param d
     * @param startPos
     * @param moves
     * @return
     */
    public static MaplePacket moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("moveDragon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue()); //not sure
        mplew.writeInt(d.getOwner());
        mplew.writePos(startPos);
        mplew.writeInt(0);

        PacketHelper.serializeMovementList(mplew, moves);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("moveDragon-7230" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket addTutorialStats() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(0);
        if (ServerConstants.调试输出封包) {
            System.out.println("addTutorialStats--------------------");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        mplew.writeInt(3871);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.writeShort(255);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.write(120);
        mplew.write(140);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("addTutorialStats-7253" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static final MaplePacket temporaryStats_Aran() {
        if (ServerConstants.调试输出封包) {
            System.out.println("temporaryStats_Aran--------------------");
        }
        final List<Pair<MapleStat.Temp, Integer>> stats = new ArrayList<>();
        stats.add(new Pair<>(MapleStat.Temp.STR, 999));
        stats.add(new Pair<>(MapleStat.Temp.DEX, 999));
        stats.add(new Pair<>(MapleStat.Temp.INT, 999));
        stats.add(new Pair<>(MapleStat.Temp.LUK, 999));
        stats.add(new Pair<>(MapleStat.Temp.WATK, 255));
        stats.add(new Pair<>(MapleStat.Temp.ACC, 999));
        stats.add(new Pair<>(MapleStat.Temp.AVOID, 999));
        stats.add(new Pair<>(MapleStat.Temp.SPEED, 140));
        stats.add(new Pair<>(MapleStat.Temp.JUMP, 120));
        return temporaryStats(stats);
    }

    /**
     *
     * @param chr
     * @return
     */
    public static final MaplePacket temporaryStats_Balrog(final MapleCharacter chr) {
        if (ServerConstants.调试输出封包) {
            System.out.println("temporaryStats_Balrog--------------------");
        }
        final List<Pair<MapleStat.Temp, Integer>> stats = new ArrayList<>();
        int offset = 1 + (chr.getLevel() - 90) / 20;
        //every 20 levels above 90, +1
        stats.add(new Pair<>(MapleStat.Temp.STR, chr.getStat().getTotalStr() / offset));
        stats.add(new Pair<>(MapleStat.Temp.DEX, chr.getStat().getTotalDex() / offset));
        stats.add(new Pair<>(MapleStat.Temp.INT, chr.getStat().getTotalInt() / offset));
        stats.add(new Pair<>(MapleStat.Temp.LUK, chr.getStat().getTotalLuk() / offset));
        stats.add(new Pair<>(MapleStat.Temp.WATK, chr.getStat().getTotalWatk() / offset));
        stats.add(new Pair<>(MapleStat.Temp.MATK, chr.getStat().getTotalMagic() / offset));
        return temporaryStats(stats);
    }

    /**
     *
     * @param stats
     * @return
     */
    public static final MaplePacket temporaryStats(final List<Pair<MapleStat.Temp, Integer>> stats) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("temporaryStats--------------------");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        //str 0x1, dex 0x2, int 0x4, luk 0x8
        //level 0x10 = 255
        //0x100 = 999
        //0x200 = 999
        //0x400 = 120
        //0x800 = 140
        int updateMask = 0;
        for (final Pair<MapleStat.Temp, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat.Temp, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat.Temp, Integer>>() {

                @Override
                public int compare(final Pair<MapleStat.Temp, Integer> o1, final Pair<MapleStat.Temp, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);
        Integer value;

        for (final Pair<MapleStat.Temp, Integer> statupdate : mystats) {
            value = statupdate.getLeft().getValue();

            if (value >= 1) {
                if (value <= 0x200) { //level 0x10 - is this really short or some other? (FF 00)
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.write(statupdate.getRight().byteValue());
                }
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("temporaryStats-7335" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static final MaplePacket temporaryStats_Reset() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("temporaryStats_Reset--------------------");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("temporaryStats_Reset-7348" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    //its likely that durability items use this

    /**
     *
     * @param cid
     * @param amount
     * @return
     */
    public static final MaplePacket showHpHealed(final int cid, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showHpHealed--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(0x06); //Type
        mplew.writeInt(amount);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showHpHealed-7365" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param amount
     * @return
     */
    public static final MaplePacket showOwnHpHealed(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showOwnHpHealed--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x06); //Type
        mplew.writeInt(amount);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showOwnHpHealed-7380" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @return
     */
    public static final MaplePacket sendRepairWindow(int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendRepairWindow--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REPAIR_WINDOW.getValue());
        mplew.writeInt(0x22); //sending 0x21 here opens evan skill window o.o
        mplew.writeInt(npc);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendRepairWindow-7395" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param amount
     * @return
     */
    public static final MaplePacket sendPyramidUpdate(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendPyramidUpdate--------------------");
        }

        int v2 = 0;
        int v3 = 0;
        mplew.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount); //1-132 ?
        if (amount > 0) {
            mplew.writeInt(v2);
            mplew.write(v3);
            if (v3 == v2) {
            } else {
                mplew.writeMapleAsciiString("");
                mplew.writeInt(v2);
                mplew.writeInt(v2);
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendPyramidUpdate-7409" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param rank
     * @param amount
     * @return
     */
    public static final MaplePacket sendPyramidResult(final byte rank, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendPyramidResult--------------------");
        }
        mplew.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
        mplew.write(rank);
        mplew.writeInt(amount); //1-132 ?
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendPyramidResult-7424" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    //show_status_info - 01 53 1E 01
    //10/08/14/19/11
    //update_quest_info - 08 53 1E 00 00 00 00 00 00 00 00
    //show_status_info - 01 51 1E 01 01 00 30
    //update_quest_info - 08 51 1E 00 00 00 00 00 00 00 00

    /**
     *
     * @param type
     * @param amount
     * @return
     */
    public static final MaplePacket sendPyramidEnergy(final String type, final String amount) {
        if (ServerConstants.调试输出封包) {
            System.out.println("sendPyramidEnergy--------------------");
        }
        return sendString(1, type, amount);
    }

    /**
     *
     * @param type
     * @param object
     * @param amount
     * @return
     */
    public static final MaplePacket sendString(final int type, final String object, final String amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendString--------------------");
        }
        switch (type) {//switch循环 相当于 if 但是比if性能好
            case 1:
                mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
                break;
            case 2:
                mplew.writeShort(SendPacketOpcode.GHOST_POINT.getValue());
                break;
            case 3:
                mplew.writeShort(SendPacketOpcode.GHOST_STATUS.getValue());
                break;
        }
        mplew.writeMapleAsciiString(object); //massacre_hit, massacre_cool, massacre_miss, massacre_party, massacre_laststage, massacre_skill
        mplew.writeMapleAsciiString(amount);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendString-7461" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @param amount
     * @return
     */
    public static final MaplePacket sendGhostPoint(final String type, final String amount) {
        if (ServerConstants.调试输出封包) {
            System.out.println("sendGhostPoint--------------------");
        }
        return sendString(2, type, amount); //PRaid_Point (0-1500???)
    }

    /**
     *
     * @param type
     * @param amount
     * @return
     */
    public static final MaplePacket sendGhostStatus(final String type, final String amount) {
        if (ServerConstants.调试输出封包) {
            System.out.println("sendGhostStatus--------------------");
        }
        return sendString(3, type, amount); //Red_Stage(1-5), Blue_Stage, blueTeamDamage, redTeamDamage
    }

    /**
     *
     * @param energy
     * @return
     */
    public static MaplePacket MulungEnergy(int energy) {
        if (ServerConstants.调试输出封包) {
            System.out.println("MulungEnergy--------------------");
        }
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    /**
     *
     * @return
     */
    public static MaplePacket getPollQuestion() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPollQuestion--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GAME_POLL_QUESTION.getValue());
        mplew.writeInt(1);
        mplew.writeInt(14);
        mplew.writeMapleAsciiString(ServerConstants.Poll_Question);
        mplew.writeInt(ServerConstants.Poll_Answers.length); // pollcount
        for (byte i = 0; i < ServerConstants.Poll_Answers.length; i++) {
            mplew.writeMapleAsciiString(ServerConstants.Poll_Answers[i]);
        }

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPollQuestion-7504" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param message
     * @return
     */
    public static MaplePacket getPollReply(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getPollReply--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GAME_POLL_REPLY.getValue());
        mplew.writeMapleAsciiString(message);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getPollReply-7520" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param data
     * @return
     */
    public static MaplePacket getEvanTutorial(String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getEvanTutorial--------------------");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());

        mplew.writeInt(8);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeMapleAsciiString(data);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getEvanTutorial-7542" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket showEventInstructions() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showEventInstructions--------------------");
        }
        mplew.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showEventInstructions-7556" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket getOwlOpen() { //best items! hardcoded
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("getOwlOpen--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(7);
        mplew.write(GameConstants.owlItems.length);
        for (int i : GameConstants.owlItems) {
            mplew.writeInt(i);
        } //these are the most searched items. too lazy to actually make
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getOwlOpen-7574" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemSearch
     * @param hms
     * @return
     */
    public static MaplePacket getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("getOwlSearched--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(itemSearch);
        int size = 0;

        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        mplew.writeInt(size);
        for (HiredMerchant hm : hms) {
            final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwnerName());
                mplew.writeInt(hm.getMap().getId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.item.getQuantity()); //I THINK.
                mplew.writeInt(item.bundles); //I THINK.
                mplew.writeInt(item.price);
                switch (InventoryHandler.OWL_ID) {
                    case 0:
                        mplew.writeInt(hm.getOwnerId()); //store ID
                        break;
                    case 1:
                        mplew.writeInt(hm.getStoreId());
                        break;
                    default:
                        mplew.writeInt(hm.getObjectId());
                        break;
                }
                mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
                mplew.write(GameConstants.getInventoryType(itemSearch).getType()); //position?
                if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                    PacketHelper.addItemInfo(mplew, item.item, true, true);
                }
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getOwlSearched-7623" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param mode
     * @param mesos
     * @param selection
     * @param answer
     * @return
     */
    public static MaplePacket getRPSMode(byte mode, int mesos, int selection, int answer) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getRPSMode--------------------");
        }
        mplew.writeShort(SendPacketOpcode.RPS_GAME.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6: { //not enough mesos
                if (mesos != -1) {
                    mplew.writeInt(mesos);
                }
                break;
            }
            case 8: { //open (npc)
                mplew.writeInt(9000019);
                break;
            }
            case 11: { //selection vs answer
                mplew.write(selection);
                mplew.write(answer); // FF = lose, or if selection = answer then lose ???
                break;
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getRPSMode-7655" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param invType
     * @param newSlots
     * @return
     */
    public static final MaplePacket getSlotUpdate(byte invType, byte newSlots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getSlotUpdate--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_INVENTORY_SLOT.getValue());
        mplew.write(invType);
        mplew.write(newSlots);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getSlotUpdate-7671" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chrid
     * @return
     */
    public static MaplePacket followRequest(int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("followRequest--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FOLLOW_REQUEST.getValue());
        mplew.writeInt(chrid);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("followRequest-7685" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param initiator
     * @param replier
     * @param toMap
     * @return
     */
    public static MaplePacket followEffect(int initiator, int replier, Point toMap) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("followEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
        mplew.writeInt(initiator);
        mplew.writeInt(replier);
        if (replier == 0) { //cancel
            mplew.write(toMap == null ? 0 : 1); //1 -> x (int) y (int) to change map
            if (toMap != null) {
                mplew.writeInt(toMap.x);
                mplew.writeInt(toMap.y);
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("followEffect-7707" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param opcode
     * @return
     */
    public static MaplePacket getFollowMsg(int opcode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("getFollowMsg--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
        mplew.writeLong(opcode); //5 = canceled request.
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getFollowMsg-7721" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param otherStart
     * @param myStart
     * @param otherEnd
     * @param moves
     * @return
     */
    public static MaplePacket moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("moveFollow--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
        mplew.writePos(otherStart);
        mplew.writePos(myStart);
        PacketHelper.serializeMovementList(mplew, moves);
        mplew.write(0x11); //what? could relate to movePlayer
        for (int i = 0; i < 8; i++) {
            mplew.write(0x88); //?? sometimes 44
        }
        mplew.write(8); //?
        mplew.writePos(otherEnd);
        mplew.writePos(otherStart);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("moveFollow-7746" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @return
     */
    public static final MaplePacket getFollowMessage(final String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getFollowMessage--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FOLLOW_MESSAGE.getValue());
        mplew.writeShort(0x0B); //?
        mplew.writeMapleAsciiString(msg); //white in gms, but msea just makes it pink.. waste
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getFollowMessage-7762" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param objectid
     * @param map
     * @return
     */
    public static final MaplePacket getNodeProperties(final MapleMonster objectid, final MapleMap map) {
        //idk.
        if (ServerConstants.调试输出封包) {
            System.out.println("getNodeProperties--------------------");
        }
        if (objectid.getNodePacket() != null) {
            return objectid.getNodePacket();
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
        mplew.writeInt(objectid.getObjectId()); //?
        mplew.writeInt(map.getNodes().size());
        mplew.writeInt(objectid.getPosition().x);
        mplew.writeInt(objectid.getPosition().y);
        for (MapleNodeInfo mni : map.getNodes()) {
            mplew.writeInt(mni.x);
            mplew.writeInt(mni.y);
            mplew.writeInt(mni.attr);
            if (mni.attr == 2) { //msg
                mplew.writeInt(500); //? talkMonster
            }
        }
        mplew.writeZeroBytes(6);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getNodeProperties-7793" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        objectid.setNodePacket(mplew.getPacket());
        return objectid.getNodePacket();
    }

    /**
     *
     * @param map
     * @return
     */
    public static final MaplePacket getMovingPlatforms(final MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getMovingPlatforms--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); x++) {
                mplew.writeInt(mp.SN.get(x));
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);//?
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getMovingPlatforms-7826" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param map
     * @return
     */
    public static final MaplePacket getUpdateEnvironment(final MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("getUpdateEnvironment--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
        mplew.writeInt(map.getEnvironment().size());
        for (Entry<String, Integer> mp : map.getEnvironment().entrySet()) {
            mplew.writeMapleAsciiString(mp.getKey());
            mplew.writeInt(mp.getValue());
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("getUpdateEnvironment-7845" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param name
     * @param cid
     * @return
     */
    public static MaplePacket sendEngagementRequest(String name, int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendEngagementRequest--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ENGAGE_REQUEST.getValue());
        mplew.write(0); //mode, 0 = engage, 1 = cancel, 2 = answer.. etc
        mplew.writeMapleAsciiString(name); // name
        mplew.writeInt(cid); // playerid
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendEngagementRequest-7861" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static MaplePacket trembleEffect(int type, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("trembleEffect--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("trembleEffect-7883" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @param item
     * @param male
     * @param female
     * @return
     */
    public static MaplePacket sendEngagement(final byte msg, final int item, final MapleCharacter male, final MapleCharacter female) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendEngagement--------------------");
        }
        // 0B = Engagement has been concluded.
        // 0D = The engagement is cancelled.
        // 0E = The divorce is concluded.
        // 10 = The marriage reservation has been successsfully made.
        // 12 = Wrong character name
        // 13 = The party in not in the same map.
        // 14 = Your inventory is full. Please empty your E.T.C window.
        // 15 = The person's inventory is full.
        // 16 = The person cannot be of the same gender.
        // 17 = You are already engaged.
        // 18 = The person is already engaged.
        // 19 = You are already married.
        // 1A = The person is already married.
        // 1B = You are not allowed to propose.
        // 1C = The person is not allowed to be proposed to.
        // 1D = Unfortunately, the one who proposed to you has cancelled his proprosal.
        // 1E = The person had declined the proposal with thanks.
        // 1F = The reservation has been cancelled. Try again later.
        // 20 = You cannot cancel the wedding after reservation.
        // 22 = The invitation card is ineffective.
        mplew.writeShort(SendPacketOpcode.ENGAGE_RESULT.getValue());
        mplew.write(msg); // 1103 custom quest
        switch (msg) {
            case 11: {
                mplew.writeInt(0); // ringid or uniqueid
                mplew.writeInt(male.getId());
                mplew.writeInt(female.getId());
                mplew.writeShort(1); //always
                mplew.writeInt(item);
                mplew.writeInt(item); // wtf?repeat?
                mplew.writeAsciiString(male.getName(), 15);
                mplew.writeAsciiString(female.getName(), 15);
                break;
            }
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendEngagement-7930" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param msg
     * @return
     */
    public static MaplePacket englishQuizMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("englishQuizMsg--------------------");
        }
        mplew.writeShort(SendPacketOpcode.ENGLISH_QUIZ.getValue());
        mplew.writeInt(20); //?
        mplew.writeMapleAsciiString(msg);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("englishQuizMsg-7947" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param beansCount
     * @param type
     * @return
     */
    public static MaplePacket openBeans(int beansCount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("openBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME1.getValue());
        mplew.writeInt(beansCount);
        mplew.write(type);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("openBeans-7962" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param beansCount
     * @return
     */
    public static MaplePacket updateBeans(int cid, int beansCount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("updateBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_BEANS.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(beansCount);
        mplew.writeInt(0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("updateBeans-7978" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param b
     * @param a
     * @return
     */
    public static MaplePacket BeansZJgeidd(int b, int a) {//豆豆进洞后奖励的
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        mplew.write(b);//类型 05   08  都是加豆豆···
        mplew.writeInt(a);//奖励豆豆的数量
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket BeansHJG() {//黄金狗
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        mplew.write(7);//类型
        mplew.writeInt(1);//改变模式
        return mplew.getPacket();
    }

    /**
     *
     * @param a
     * @return
     */
    public static MaplePacket BeansJDCS(int a) {//进洞次数 最多有7个
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        mplew.write(4);//类型
        mplew.writeInt(a);//1 = 1次
        mplew.writeInt(0);//
        return mplew.getPacket();
    }

    /**
     *
     * @param a
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @return
     */
    public static MaplePacket BeansJDXZ(int a, int a1, int a2, int a3, int a4, int a5, int a6) {//进洞后开始旋转图片
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        mplew.write(4);//类型
        mplew.write(a);//个数
        mplew.write(0);//第一排
        mplew.write(0);//第三排
        mplew.write(0);//第二排
        mplew.writeInt(a1);//未知数据···
        mplew.writeInt(a2);//未知数据···
        mplew.writeInt(a3);//未知数据···
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket BeansQR() {//(null)请拿到凯？琳处确认
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        mplew.write(7);//类型
        mplew.writeInt(1);//改变模式
        return mplew.getPacket();
    }

    /**
     *
     * @param beansInfo
     * @return
     */
    public static MaplePacket showBeans(List<Beans> beansInfo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showBeans--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        //  mplew.write(0);
        /*mplew.writeShort(力度);
        mplew.write(size);
        mplew.writeShort(Pos);
        // mplew.write(Type);
        mplew.writeInt(Type);*/

        mplew.write(0);
        mplew.write(beansInfo.size());
        for (Beans bean : beansInfo) {
            mplew.writeShort(bean.getPos());
            mplew.write(bean.getType());
            mplew.writeInt(bean.getNumber());
            ////System.out.println("发包时豆豆序号"+bean.getNumber());
        }
        ////System.out.println("豆豆发包"+mplew.getPacket());
        //mplew.write(HexTool.getByteArrayFromHexString("
        /* DA 01 
        00 
        05 
        
        BD 0F 
        01 
        60 00 00 00 
        
        FF 0E 
        01 
        61 00 00 00 
        
        69 0E 
        01 
        62 00 00 00 
        
        05 0F 
        01 
        63 00 00 00 
        
        C6 0F 
        01 
        64 00 00 00
        "));*/
 /*  5D 01 
        00
        05 
        
        61 00 
        00 
        01 00 00 00 
        
        32 00 
        00 
        02 00 00 00 
        
        9D FF 
        00 
        03 00 00 00 
        
        BC FF 
        00 
        04 00 00 00 
        
        44 00 
        00
        05 00 00 00*/

 /* mplew.write(size);
         mplew.writeShort(Pos);
         mplew.write(Type);
         mplew.writeInt(1);*/
        //System.out.println("���鞊��"+bean.getNumber());
        //System.out.println("鞊����"+mplew.getPacket());
        //mplew.write(HexTool.getByteArrayFromHexString("DA 01 00 05 BD 0F 01 60 00 00 00 FF 0E 01 61 00 00 00 69 0E 01 62 00 00 00 05 0F 01 63 00 00 00 C6 0F 01 64 00 00 00"));
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showBeans-8004" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @return
     */
    public static MaplePacket showCharCash(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("showCharCash--------------------");
        }
        mplew.writeShort(SendPacketOpcode.CHAR_CASH.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getCSPoints(2));

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showCharCash-8021" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param itemid
     * @param name
     * @param msg
     * @param pos
     * @param ft
     * @return
     */
    public static MaplePacket spawnLove(int oid, int itemid, String name, String msg, Point pos, int ft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("spawnLove--------------------");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_LOVE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(name);
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y + ft);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnLove-8040" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @return
     */
    public static MaplePacket removeLove(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("removeLove--------------------");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_LOVE.getValue());
        mplew.writeInt(oid);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("removeLove-8054" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket licenseRequest() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("licenseRequest--------------------");
        }
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(22);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("licenseRequest-8069" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket licenseResult() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("licenseResult--------------------");
        }
        mplew.writeShort(SendPacketOpcode.LICENSE_RESULT.getValue());
        mplew.write(1);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("licenseResult-8084" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket showForcedEquip() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("showForcedEquip--------------------");
        }
        mplew.writeShort(SendPacketOpcode.FORCED_MAP_EQUIP.getValue());
        mplew.writeInt(0);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("showForcedEquip-8098" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @return
     */
    public static MaplePacket removeTutorialStats() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("removeTutorialStats--------------------");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            //ERROR.setPACKET_ERROR("removeTutorialStats-8111" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param type
     * @return
     */
    public static MaplePacket spawnTutorialSummon(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("spawnTutorialSummon--------------------");
        }
        mplew.writeShort(SendPacketOpcode.TUTORIAL_SUMMON.getValue());
        mplew.write(type);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("spawnTutorialSummon-8125" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cidFrom
     * @param nameFrom
     * @return
     */
    public static MaplePacket requestBuddylistAdd(int cidFrom, String nameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // System.out.println("删除好友");
        if (ServerConstants.调试输出封包) {
            System.out.println("requestBuddylistAdd--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\000', 13));
        mplew.write(1);
        mplew.write(5);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeAsciiString(StringUtil.getRightPaddedStr("群未定", '\000', 17));
        mplew.write(0);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("requestBuddylistAdd-8151" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemId
     * @return
     */
    public static MaplePacket sendAutoHpPot(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendAutoHpPot--------------------");
        }
        mplew.writeShort(SendPacketOpcode.AUTO_HP_POT.getValue());
        mplew.writeInt(itemId);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendAutoHpPot-8167" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param itemId
     * @return
     */
    public static MaplePacket sendAutoMpPot(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (ServerConstants.调试输出封包) {
            System.out.println("sendAutoMpPot--------------------");
        }
        mplew.writeShort(SendPacketOpcode.AUTO_MP_POT.getValue());
        mplew.writeInt(itemId);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendAutoMpPot-8183" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param testmsg
     * @return
     */
    public static MaplePacket testPacket(byte[] testmsg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("testPacket--------------------");
        }
        mplew.write(testmsg);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("testPacket-8196" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param x
     * @param laba
     * @return
     */
    public static MaplePacket BeansGameMessage(int cid, int x, String laba) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("testPacket--------------------");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME_MESSAGE.getValue());
        mplew.writeInt(cid);
        mplew.write(x);
        mplew.writeMapleAsciiString(laba);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("testPacket-8196" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param lx
     * @return
     */
    public static MaplePacket sendEventWindow(int npc, int lx) {

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("sendEventWindow--------------------");
        }
        mplew.writeShort(SendPacketOpcode.EVENT_WINDOW.getValue());
        if (lx <= 0) {
            mplew.writeInt(55);
        } else {
            mplew.writeInt(lx);
        }
        if (npc > 0) {
            mplew.writeInt(npc);
        }
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("sendEventWindow-8218" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param web
     * @return
     */
    public static MaplePacket openWeb(String web) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("openWeb--------------------");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_WEB.getValue());
        mplew.writeMapleAsciiString(web);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("openWeb-8232" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param barammount
     * @return
     */
    public static MaplePacket giveEnergyCharge(int barammount) {

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        //////System.out.println("第1个输出。伤害为 " + barammount + "");
        mplew.writeInt(0);
        //mplew.writeLong(mask);
        //mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        //////System.out.println("第2个输出。伤害为 " + barammount + "");
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeShort(barammount);
        mplew.writeShort(0);
        mplew.writeLong(0);
        mplew.write(0);
        mplew.writeInt(50);
        //////System.out.println("第3个输出。伤害为 " + barammount + "");
        return mplew.getPacket();

    }

    /**
     *
     * @param i
     * @return
     */
    public static MaplePacket shenlong(int i) {
        //if(show)//System.out.println("调用的函数："+new Throwable().getStackTrace()[0]); //显示调用的类 函数名 函数所在行
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(i); //274/275 暗/亮   124/125 龙珠9个齐全,开始召唤神龙
        //mplew.write(HexTool.getByteArrayFromHexString("00 BC 06 00 00"));
        mplew.write(HexTool.getByteArrayFromHexString("DC 05 00 00 90 5F 01 00 DC 05 00 00 9B 00 00 00"));
        return mplew.getPacket();
    }

    /**
     *
     * @param i
     * @return
     */
    public static MaplePacket shenlong2(int i) {
        //if(show)//System.out.println("调用的函数："+new Throwable().getStackTrace()[0]); //显示调用的类 函数名 函数所在行
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(i); //274/275 暗/亮   124/125 龙珠9个齐全,开始召唤神龙
        mplew.write(HexTool.getByteArrayFromHexString("02 CB 06 00 00 FB 44 00 00"));
        return mplew.getPacket();
    }

    /**
     *
     * @param i
     * @param Zhaohuan
     * @return
     */
    public static MaplePacket DragonBall1(int i, boolean Zhaohuan) {
        //if(show)//System.out.println("调用的函数："+new Throwable().getStackTrace()[0]); //显示调用的类 函数名 函数所在行
        //打开谜之蛋 137
        //龙珠 141
        //95 00 00 00 00 00 01 00 00 01 01 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //mplew.writeShort(SendPacketOpcode.DragonBall1.getValue());
        mplew.writeInt(0);
        mplew.write(1);
        if (!Zhaohuan) { //不能召唤
            mplew.writeShort(0);
            mplew.writeShort(i); //512的倍数都是满龙珠 不用Long的原因是512的倍数出现的龙珠是蓝色的 漂亮
            mplew.writeShort(0);
        } else { //可以召唤
            mplew.writeLong(512); //512的倍数都是满龙珠 不是则出不来
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param talk
     * @param type
     * @return
     */
    public static MaplePacket getCY1(int npc, String talk, byte type) {
        //if(show)//System.out.println("调用的函数："+new Throwable().getStackTrace()[0]); //显示调用的类 函数名 函数所在行
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(0x0D);//次元之镜
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(type);//由#type#开始 填1 打开Npc就先显示#1#的图案    填2 打开Npc就先显示#2#的图案
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        return mplew.getPacket();
    }

    /**
     *
     * @param npc
     * @param talk
     * @param type
     * @return
     */
    public static MaplePacket getCY2(int npc, String talk, byte type) {
        //if(show)//System.out.println("调用的函数："+new Throwable().getStackTrace()[0]); //显示调用的类 函数名 函数所在行
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(0x10);//龙舟赛
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(type);//由#type#开始 填1 打开Npc就先显示#1#的图案    填2 打开Npc就先显示#2#的图案
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showRQRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("fame"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showJXRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeLong(rs.getInt("jx"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            // mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showXRRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("pvpkills"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showBSRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("pvpdeaths"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showVipRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showLevelRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showMesoRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }
    //renwu 2

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showRenwu2Ranks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("renwu2"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    /**
     *
     * @param npcid
     * @param rs
     * @return
     * @throws SQLException
     */
    public static MaplePacket showGuildRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("GP"));
            mplew.writeInt(rs.getInt("logo"));
            mplew.writeInt(rs.getInt("logoColor"));
            mplew.writeInt(rs.getInt("logoBG"));
            mplew.writeInt(rs.getInt("logoBGColor"));
        }

        return mplew.getPacket();
    }

    /**
     *
     * @param v1
     * @return
     */
    public static MaplePacket sub_93F0BE(int v1) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //mplew.writeShort(SendPacketOpcode.OPEN_WEB.getValue());

        mplew.writeShort(0xEB);
        mplew.writeInt(v1);
        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param n
     * @param value
     * @return
     */
    public static MaplePacket rechargeCombo(String n, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        //mplew.writeShort(SendPacketOpcode.ARAN_COMBO_RECHARGE.getValue());
        mplew.writeShort(0xE8);
        mplew.writeMapleAsciiString(n);
        mplew.writeInt(value);

        if (ServerConstants.PACKET_ERROR_OFF) {
            ServerConstants ERROR = new ServerConstants();
            ERROR.setPACKET_ERROR("" + "：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param item
     * @return
     */
    public static MaplePacket updateEquipSlot(IItem item) {//道具取名
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerConstants.调试输出封包) {
            System.out.println("updateEquipSlot--------------------");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(HexTool.getByteArrayFromHexString("02 03 01"));
        mplew.writeShort(item.getPosition());
        mplew.write(0);
        mplew.write(item.getType());
        mplew.writeShort(item.getPosition());
        PacketHelper.addItemInfo(mplew, item, true, true);
        mplew.writeMapleAsciiString("wat");

        return mplew.getPacket();
    }

    /**
     *
     * @param chrId
     * @param type
     * @param monsteridfrom
     * @param damage
     * @return
     */
    public static MaplePacket PVPdamagePlayer(int chrId, int type, int monsteridfrom, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(chrId);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(damage);
        return mplew.getPacket();
    }
}
