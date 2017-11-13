package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import client.inventory.IItem;
import client.inventory.Item;
import client.SkillFactory;
import constants.GameConstants;
import client.inventory.MapleInventoryIdentifier;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import database.DatabaseConnection;
import tools.MaplePacketCreator;

public class MapleShop {

    private static final Set<Integer> rechargeableItems = new LinkedHashSet<Integer>();
    private int id;
    private int npcId;
    private List<MapleShopItem> items;
 static {
        rechargeableItems.add(2070000); //海星镖
        rechargeableItems.add(2070001); //回旋镖
        rechargeableItems.add(2070002); //黑色利刃
        rechargeableItems.add(2070003); //雪花镖
        rechargeableItems.add(2070004); //黑色刺
        rechargeableItems.add(2070005); //金钱镖
        rechargeableItems.add(2070006); //齿轮镖
        rechargeableItems.add(2070007); //月牙镖
        rechargeableItems.add(2070008); //小雪球
        rechargeableItems.add(2070009); //木制陀螺
        rechargeableItems.add(2070010); //冰菱
        rechargeableItems.add(2070011); //枫叶镖
        rechargeableItems.add(2070012); //纸飞机
        rechargeableItems.add(2070013); //橘子
        rechargeableItems.add(2070015); //初学者标
        rechargeableItems.add(2070016); //雪球
        //rechargeableItems.add(2070017); //巨型雪球
        rechargeableItems.add(2070019); //高科技电光镖
        rechargeableItems.add(2070020); //鞭炮
        rechargeableItems.add(2070021); //蛋糕镖
        rechargeableItems.add(2070023); //火焰飞镖
        rechargeableItems.add(2070024); //无限飞镖
        rechargeableItems.add(2070025); //无限飞镖
        rechargeableItems.add(2070026); //无限飞镖

        rechargeableItems.add(2330000); //子弹
        rechargeableItems.add(2330001); //手枪弹
        rechargeableItems.add(2330002); //铜头子弹
        rechargeableItems.add(2330003); //银子弹
        rechargeableItems.add(2330004); //高爆弹
        rechargeableItems.add(2330005); //穿甲弹
        rechargeableItems.add(2330006); //新手专用弹
        rechargeableItems.add(2331000); //穿甲弹
        rechargeableItems.add(2332000); //新手专用弹
       // rechargeableItems.add(2330007); //高科技穿甲弹
       // rechargeableItems.add(2330008); //钢铁子弹
    }

   /* static {
         for (int i = 2070000; i <= 2070021; i++) {
       rechargeableItems.add(Integer.valueOf(i));
     }
     for (int i = 2070023; i <= 2070026; i++) {
       rechargeableItems.add(Integer.valueOf(i));
     }
     rechargeableItems.remove(Integer.valueOf(2070014));
     rechargeableItems.remove(Integer.valueOf(2070015));
     rechargeableItems.remove(Integer.valueOf(2070016));
     rechargeableItems.remove(Integer.valueOf(2070017));
     rechargeableItems.remove(Integer.valueOf(2070018));
     rechargeableItems.remove(Integer.valueOf(2070019));
     rechargeableItems.remove(Integer.valueOf(2070020));
     rechargeableItems.remove(Integer.valueOf(2070021));
     rechargeableItems.remove(Integer.valueOf(2070023));
     rechargeableItems.remove(Integer.valueOf(2070024));
     rechargeableItems.remove(Integer.valueOf(2070025));
     rechargeableItems.remove(Integer.valueOf(2070026));
 
     for (int i = 2330000; i <= 2330006; i++) {
       rechargeableItems.add(Integer.valueOf(i));
     }
     rechargeableItems.add(Integer.valueOf(2331000));
     rechargeableItems.add(Integer.valueOf(2332000));
        rechargeableItems.add(2070000);
        rechargeableItems.add(2070001);
        rechargeableItems.add(2070002);
        rechargeableItems.add(2070003);
        rechargeableItems.add(2070004);
        rechargeableItems.add(2070005);
        rechargeableItems.add(2070006);
        rechargeableItems.add(2070007);
        rechargeableItems.add(2070008);
        rechargeableItems.add(2070009);
        rechargeableItems.add(2070010);
        rechargeableItems.add(2070011);
        rechargeableItems.add(2070012);
        rechargeableItems.add(2070013);
	rechargeableItems.add(2070014); // Doesn't Exist [Devil Rain]
	rechargeableItems.add(2070015); // Beginner Star
        rechargeableItems.add(2070016);
	rechargeableItems.add(2070017); // Doesn't Exist
        rechargeableItems.add(2070018); // Balanced Fury
        rechargeableItems.add(2070019); // Magic Throwing Star
        rechargeableItems.add(2070020);
        rechargeableItems.add(2070021);
        rechargeableItems.add(2070023);
        rechargeableItems.add(2070024);
        rechargeableItems.add(2070025);
        rechargeableItems.add(2070026);

        rechargeableItems.add(2330000);
        rechargeableItems.add(2330001);
        rechargeableItems.add(2330002);
        rechargeableItems.add(2330003);
        rechargeableItems.add(2330004);
        rechargeableItems.add(2330005);
//	rechargeableItems.add(2330006); // Beginner Bullet
        rechargeableItems.add(2330007);

        rechargeableItems.add(2331000); // Capsules
        rechargeableItems.add(2332000); // Capsules
    }*/

    /**
     * Creates a new instance of MapleShop
     */
    private MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
        items = new LinkedList<MapleShopItem>();
    }

    public void addItem(MapleShopItem item) {
        items.add(item);
    }

    public void sendShop(MapleClient c) {
        c.getPlayer().setShop(this);
        c.getSession().write(MaplePacketCreator.getNPCShop(c, getNpcId(), items));
    }

    public void buy(MapleClient c, int itemId, short quantity) {
        if (quantity <= 0) {
            AutobanManager.getInstance().addPoints(c, 1000, 0, "购买道具数量 " + quantity + " 道具: " + itemId);
            return;
        }
        /*if (!GameConstants.isMountItemAvailable(itemId, c.getPlayer().getJob())) {
            c.getPlayer().dropMessage(1, "You may not buy this item.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }*/
        if (c.getPlayer().getMapId() != 809030000 &&  getId() == 9100109) {
            c.getPlayer().dropMessage(5, "无法正常操作A！"+c.getPlayer().getMapId()+"/"+getId());
        } else if (c.getPlayer().getMapId() == 809030000 && getId() == 9100109) {//豆豆机中奖次数
            MapleShopItem item = findById(itemId);
            if (item != null && item.getPrice() > 0) {
                final int price = GameConstants.isRechargable(itemId) ? item.getPrice() : (item.getPrice() * quantity);
                if (price >= 0 && c.getPlayer().getddj() >= price) {
                    if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                        c.getPlayer().gainddj(-price);
                        if (GameConstants.isPet(itemId)) {
                            MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, (byte) 0);
                        } else {
                            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                            if (GameConstants.isRechargable(itemId)) {
                                quantity = ii.getSlotMax(c, item.getItemId());
                            }
                            MapleInventoryManipulator.addById(c, itemId, quantity, (byte) 0);
                        }
                        c.getPlayer().dropMessage(1, "购买成功.\r\n消费："+price+"豆豆中奖次数！\r\n剩余："+c.getPlayer().getddj()+"豆豆中奖次数！");
                    } else {
                        c.getPlayer().dropMessage(1, "请留出足够的背包空间！");
                    }
                    c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0));
                } else {
                    c.getPlayer().dropMessage(1, "你的豆豆机中奖次数不足!\r\n请继续打豆豆中奖!\r\n中奖次数够了以后才能\r\n当前豆豆中奖次数："+c.getPlayer().getddj());
                }
            }
        }else if (c.getPlayer().getMapId() != 809030000 && getId() == 9120104) {
            c.getPlayer().dropMessage(5, "无法正常操作A！"+c.getPlayer().getMapId()+"/"+getId());
        } else if (c.getPlayer().getMapId() == 809030000 && getId() == 9120104) {//豆豆数量
            MapleShopItem item = findById(itemId);
            if (item != null && item.getPrice() > 0) {
                final int price = GameConstants.isRechargable(itemId) ? item.getPrice() : (item.getPrice() * quantity);
                if (price >= 0 && c.getPlayer().getBeans() >= price) {
                    if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                        c.getPlayer().gainBeans(-price);
                        if (GameConstants.isPet(itemId)) {
                            MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, (byte) 0);
                        } else {
                            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                            if (GameConstants.isRechargable(itemId)) {
                                quantity = ii.getSlotMax(c, item.getItemId());
                            }
                            MapleInventoryManipulator.addById(c, itemId, quantity, (byte) 0);
                        }
                        c.getPlayer().dropMessage(1, "购买成功.\r\n消费："+price+"豆豆！\r\n剩余："+c.getPlayer().getBeans()+"豆豆！");
                    } else {
                        c.getPlayer().dropMessage(1, "请留出足够的背包空间！");
                    }
                    c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0));
                } else {
                    c.getPlayer().dropMessage(1, "你的豆豆数量不足!\r\n请去商城购买!");
                }
            }
        } else {
            MapleShopItem item = findById(itemId);
            if (item != null && item.getPrice() > 0) {
                final int price = GameConstants.isRechargable(itemId) ? item.getPrice() : (item.getPrice() * quantity);
                if (price >= 0 && c.getPlayer().getMeso() >= price) {
                    if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                        c.getPlayer().gainMeso(-price, false);
                        if (GameConstants.isPet(itemId)) {
                            MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, (byte) 0);
                        } else {
                            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                            if (GameConstants.isRechargable(itemId)) {
                                quantity = ii.getSlotMax(c, item.getItemId());
                            }

                            MapleInventoryManipulator.addById(c, itemId, quantity, (byte) 0);
                        }
                    } else {
                        c.getPlayer().dropMessage(1, "Your Inventory is full");
                    }
                    c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0));
                }
            }
        }
        /*else if (item != null  && quantity == 1 && c.getPlayer().haveItem(item.getReqItem(), item.getReqItemQ(), false, true)) {
            if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(item.getReqItem()), item.getReqItem(), item.getReqItemQ(), false, false);
                if (GameConstants.isPet(itemId)) {
                    MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1);
                } else {
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                    if (GameConstants.isRechargable(itemId)) {
                        quantity = ii.getSlotMax(c, item.getItemId());
                    }
                    MapleInventoryManipulator.addById(c, itemId, quantity);
                }
            } else {
                c.getPlayer().dropMessage(1, "Your Inventory is full");
            }
            c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0));
        }*/
    }

    public void sell(MapleClient c, MapleInventoryType type, byte slot, short quantity) {
        if (quantity == 0xFFFF || quantity == 0) {
            quantity = 1;
        }
        IItem item = c.getPlayer().getInventory(type).getItem(slot);
        if (item == null) {
            return;
        }

        if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
            quantity = item.getQuantity();
        }
        if (quantity < 0) {
            AutobanManager.getInstance().addPoints(c, 1000, 0, "Selling " + quantity + " " + item.getItemId() + " (" + type.name() + "/" + slot + ")");
            return;
        }
        short iQuant = item.getQuantity();
        if (iQuant == 0xFFFF) {
            iQuant = 1;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.cantSell(item.getItemId())) {
            return;
        }
        if (quantity <= iQuant && iQuant > 0) {
            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            double price;
            if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
                price = ii.getWholePrice(item.getItemId()) / (double) ii.getSlotMax(c, item.getItemId());
            } else {
                price = ii.getPrice(item.getItemId());
            }
            final int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0);
            if (price != -1.0 && recvMesos > 0) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0x8));
        }
    }

    public void recharge(final MapleClient c, final byte slot) {
        final IItem item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

        if (item == null || (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId()))) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slotMax = ii.getSlotMax(c, item.getItemId());
        final int skill = GameConstants.getMasterySkill(c.getPlayer().getJob());

        if (skill != 0) {
            slotMax += c.getPlayer().getSkillLevel(SkillFactory.getSkill(skill)) * 10;
        }
        if (item.getQuantity() < slotMax) {
            final int price = (int) Math.round(ii.getPrice(item.getItemId()) * (slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                c.getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) item, false));
                c.getPlayer().gainMeso(-price, false, true, false);
                c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0x8));
            }
        }
    }

    protected MapleShopItem findById(int itemId) {
        for (MapleShopItem item : items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public static MapleShop createFromDB(int id, boolean isShopId) {
        MapleShop ret = null;
        int shopId;

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shopId = rs.getInt("shopid");
                ret = new MapleShop(shopId, rs.getInt("npcid"));
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                return null;
            }
            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            List<Integer> recharges = new ArrayList<Integer>(rechargeableItems);
            while (rs.next()) {
                if (GameConstants.isThrowingStar(rs.getInt("itemid")) || GameConstants.isBullet(rs.getInt("itemid"))) {
                    MapleShopItem starItem = new MapleShopItem((short) 1, rs.getInt("itemid"), rs.getInt("price"));
                    ret.addItem(starItem);
                    if (rechargeableItems.contains(starItem.getItemId())) {
                        recharges.remove(Integer.valueOf(starItem.getItemId()));
                    }
                } else {
                    ret.addItem(new MapleShopItem((short) 1000, rs.getInt("itemid"), rs.getInt("price")));
                }
            }
            for (Integer recharge : recharges) {
                ret.addItem(new MapleShopItem((short) 1000, recharge.intValue(), 0));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Could not load shop" + e);
        }
        return ret;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getId() {
        return id;
    }
}
