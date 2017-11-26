
package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.world.World;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import server.maps.AbstractMapleMapObject;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.PlayerShopPacket;

/**
 *
 * @author zjj
 */
public abstract class AbstractPlayerStore extends AbstractMapleMapObject implements IMaplePlayerShop {

    protected boolean open = false, 

    /**
     *
     */
    available = false;
    protected String ownerName, 

    /**
     *
     */
    des, 

    /**
     *
     */
    pass;
    protected int ownerId, 

    /**
     *
     */
    owneraccount, 

    /**
     *
     */
    itemId, 

    /**
     *
     */
    channel, 

    /**
     *
     */
    map;

    /**
     *
     */
    protected AtomicInteger meso = new AtomicInteger(0);

    /**
     *
     */
    protected WeakReference<MapleCharacter> chrs[];

    /**
     *
     */
    protected List<String> visitors = new LinkedList<>();

    /**
     *
     */
    protected List<BoughtItem> bought = new LinkedList<>();

    /**
     *
     */
    protected List<MaplePlayerShopItem> items = new LinkedList<>();
   // private static final Logger log = Logger.getLogger(AbstractPlayerStore.class);

    /**
     *
     * @param owner
     * @param itemId
     * @param desc
     * @param pass
     * @param slots
     */
    public AbstractPlayerStore(MapleCharacter owner, int itemId, String desc, String pass, int slots) {
        this.setPosition(owner.getPosition());
        this.ownerName = owner.getName();
        this.ownerId = owner.getId();
        this.owneraccount = owner.getAccountID();
        this.itemId = itemId;
        this.des = desc;
        this.pass = pass;
        this.map = owner.getMapId();
        this.channel = owner.getClient().getChannel();
        chrs = new WeakReference[slots];
        for (int i = 0; i < chrs.length; i++) {
            chrs[i] = new WeakReference<>(null);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxSize() {
        return chrs.length + 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getSize() {
        return getFreeSlot() == -1 ? getMaxSize() : getFreeSlot();
    }

    /**
     *
     * @param packet
     */
    @Override
    public void broadcastToVisitors(MaplePacket packet) {
        broadcastToVisitors(packet, true);
    }

    /**
     *
     * @param packet
     * @param owner
     */
    public void broadcastToVisitors(MaplePacket packet, boolean owner) {
        for (WeakReference<MapleCharacter> chr : chrs) {
            if (chr != null && chr.get() != null) {
                chr.get().getClient().getSession().write(packet);
            }
        }
        if (getShopType() != IMaplePlayerShop.HIRED_MERCHANT && owner && getMCOwner() != null) {
            getMCOwner().getClient().getSession().write(packet);
        }
    }

    /**
     *
     * @param packet
     * @param exception
     */
    public void broadcastToVisitors(MaplePacket packet, int exception) {
        for (WeakReference<MapleCharacter> chr : chrs) {
            if (chr != null && chr.get() != null && getVisitorSlot(chr.get()) != exception) {
                chr.get().getClient().getSession().write(packet);
            }
        }
        if (getShopType() != IMaplePlayerShop.HIRED_MERCHANT && getMCOwner() != null && exception != ownerId) {
            getMCOwner().getClient().getSession().write(packet);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int getMeso() {
        return meso.get();
    }

    /**
     *
     * @param meso
     */
    @Override
    public void setMeso(int meso) {
        this.meso.set(meso);
    }

    /**
     *
     * @param open
     */
    @Override
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isOpen() {
        return open;
    }

    /**
     *
     * @return
     */
    public boolean saveItems() {
        if (getShopType() != IMaplePlayerShop.HIRED_MERCHANT) { //hired merch only
            return false;
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM hiredmerch WHERE accountid = ? OR characterid = ?");
            ps.setInt(1, owneraccount);
            ps.setInt(2, ownerId);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, map, channel, time) VALUES (?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
            ps.setInt(1, ownerId);
            ps.setInt(2, owneraccount);
            ps.setInt(3, meso.get());
            ps.setInt(4, map);
            ps.setInt(5, channel);
            ps.setLong(6, System.currentTimeMillis());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                rs.close();
                ps.close();
                System.out.println("[SaveItems] 保存雇佣商店信息出错 - 1");
                String note = "时间：" + FileoutputUtil.CurrentReadable_Time() + " "
                            + "|| 玩家名字：" + getOwnerName() + "\r\n";
                    FileoutputUtil.packetLog("log\\雇佣出错-1\\" + getOwnerName() + ".log", note);
              //  log.error("[SaveItems] 保存雇佣商店信息出错 - 1");
                throw new RuntimeException("保存雇佣商店信息出错.");
            }
            final int packageid = rs.getInt(1);
            rs.close();
            ps.close();
            List<Pair<IItem, MapleInventoryType>> iters = new ArrayList<>();
            for (MaplePlayerShopItem pItems : items) {
                if ((pItems.item == null) || (pItems.bundles <= 0) || ((pItems.item.getQuantity() <= 0) && (!GameConstants.isRechargable(pItems.item.getItemId())))) {
                    continue;
                }
                IItem item = pItems.item.copy();
                item.setQuantity((short) (item.getQuantity() * pItems.bundles));
                item.setFlag((byte) (pItems.flag));
                iters.add(new Pair<>(item, GameConstants.getInventoryType(item.getItemId())));
            }
           // ItemLoader.HIRED_MERCHANT.saveItems(iters, this.ownerId);
            ItemLoader.HIRED_MERCHANT.saveItems(iters, packageid, owneraccount, ownerId);
            return true;
        } catch (SQLException se) {
                System.out.println("[SaveItems] 保存雇佣商店信息出错 - 2 " + se);
                String note = "时间：" + FileoutputUtil.CurrentReadable_Time() + " "
                            + "|| 玩家名字：" + getOwnerName() + "\r\n";
                    FileoutputUtil.packetLog("log\\雇佣出错-2\\" + getOwnerName() + ".log", note + "\r\n"+ se);
          //  log.error("[SaveItems] 保存雇佣商店信息出错 - 2 " + se);
        }
        return false;
    }

    /**
     *
     * @param num
     * @return
     */
    public MapleCharacter getVisitor(int num) {
        return chrs[num].get();
    }

    /**
     *
     */
    @Override
    public void update() {
        if (isAvailable()) {
            if (getShopType() == IMaplePlayerShop.HIRED_MERCHANT) {
                getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant) this));
            } else if (getMCOwner() != null) {
                getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(getMCOwner()));
            }
        }
    }

    /**
     *
     * @param visitor
     */
    @Override
    public void addVisitor(MapleCharacter visitor) {
        int i = getFreeSlot();
        if (i > 0) {
            if (getShopType() >= 3) {
                broadcastToVisitors(PlayerShopPacket.getMiniGameNewVisitor(visitor, i, (MapleMiniGame) this));
            } else {
                broadcastToVisitors(PlayerShopPacket.shopVisitorAdd(visitor, i));
            }
            chrs[i - 1] = new WeakReference<>(visitor);
            if (!isOwner(visitor)) {
                visitors.add(visitor.getName());
            }
            if (i == 3) {
                update();
            }
        }
    }

    /**
     *
     * @param visitor
     */
    @Override
    public void removeVisitor(MapleCharacter visitor) {
        final byte slot = getVisitorSlot(visitor);
        boolean shouldUpdate = getFreeSlot() == -1;
        if (slot > 0) {
            broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot), slot);
            chrs[slot - 1] = new WeakReference<>(null);
            if (shouldUpdate) {
                update();
            }
        }
    }

    /**
     *
     * @param visitor
     * @return
     */
    @Override
    public byte getVisitorSlot(MapleCharacter visitor) {
        for (byte i = 0; i < chrs.length; i++) {
            if (chrs[i] != null && chrs[i].get() != null && chrs[i].get().getId() == visitor.getId()) {
                return (byte) (i + 1);
            }
        }
        if (visitor.getId() == ownerId) { //can visit own store in merch, otherwise not.
            return 0;
        }
        return -1;
    }

    /**
     *
     * @param error
     * @param type
     */
    @Override
    public void removeAllVisitors(int error, int type) {
        for (int i = 0; i < chrs.length; i++) {
            MapleCharacter visitor = getVisitor(i);
            if (visitor != null) {
                if (type != -1) {
                    visitor.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(error, type));
                }
                broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(getVisitorSlot(visitor)), getVisitorSlot(visitor));
                visitor.setPlayerShop(null);
                chrs[i] = new WeakReference<>(null);
            }
        }
        update();
    }

    /**
     *
     * @return
     */
    @Override
    public String getOwnerName() {
        return ownerName;
    }

    /**
     *
     * @return
     */
    @Override
    public int getOwnerId() {
        return ownerId;
    }

    /**
     *
     * @return
     */
    @Override
    public int getOwnerAccId() {
        return owneraccount;
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        if (des == null) {
            return "";
        }
        return des;
    }

    /**
     *
     * @return
     */
    @Override
    public List<Pair<Byte, MapleCharacter>> getVisitors() {
        List<Pair<Byte, MapleCharacter>> chrz = new LinkedList<>();
        for (byte i = 0; i < chrs.length; i++) { //include owner or no
            if (chrs[i] != null && chrs[i].get() != null) {
                chrz.add(new Pair<>((byte) (i + 1), chrs[i].get()));
            }
        }
        return chrz;
    }

    /**
     *
     * @return
     */
    @Override
    public List<MaplePlayerShopItem> getItems() {
        return items;
    }

    /**
     *
     * @param item
     */
    @Override
    public void addItem(MaplePlayerShopItem item) {
        //System.out.println("Adding item ... 2");
        items.add(item);
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean removeItem(int item) {
        return false;
    }

    /**
     *
     * @param slot
     */
    @Override
    public void removeFromSlot(int slot) {
        items.remove(slot);
    }

    /**
     *
     * @return
     */
    @Override
    public byte getFreeSlot() {
        for (byte i = 0; i < chrs.length; i++) {
            if (chrs[i] == null || chrs[i].get() == null) {
                return (byte) (i + 1);
            }
        }
        return -1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemId() {
        return itemId;
    }

    /**
     *
     * @param chr
     * @return
     */
    @Override
    public boolean isOwner(MapleCharacter chr) {
        return chr.getId() == ownerId && chr.getName().equals(ownerName);
    }

    /**
     *
     * @return
     */
    @Override
    public String getPassword() {
        if (pass == null) {
            return "";
        }
        return pass;
    }

    /**
     *
     * @param client
     */
    @Override
    public void sendDestroyData(MapleClient client) {
    }

    /**
     *
     * @param client
     */
    @Override
    public void sendSpawnData(MapleClient client) {
    }

    /**
     *
     * @return
     */
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }

    /**
     *
     * @return
     */
    public MapleCharacter getMCOwner() {
        return getMap().getCharacterById(ownerId);
    }

    /**
     *
     * @return
     */
    public MapleCharacter getMCOwnerWorld() {
        int ourChannel = World.Find.findChannel(ownerId);
        if (ourChannel <= 0) {
            return null;
        }
        return ChannelServer.getInstance(ourChannel).getPlayerStorage().getCharacterById(ownerId);
    }

    /**
     *
     * @return
     */
    public MapleMap getMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(map);
    }

    /**
     *
     * @return
     */
    @Override
    public int getGameType() {
        if (getShopType() == IMaplePlayerShop.HIRED_MERCHANT) { //hiredmerch
            return 5;
        } else if (getShopType() == IMaplePlayerShop.PLAYER_SHOP) { //shop lol
            return 4;
        } else if (getShopType() == IMaplePlayerShop.OMOK) { //omok
            return 1;
        } else if (getShopType() == IMaplePlayerShop.MATCH_CARD) { //matchcard
            return 2;
        }
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     *
     * @param b
     */
    @Override
    public void setAvailable(boolean b) {
        this.available = b;
    }

    /**
     *
     * @return
     */
    @Override
    public List<BoughtItem> getBoughtItems() {
        return bought;
    }

    /**
     *
     */
    public static final class BoughtItem {

        /**
         *
         */
        public int id;

        /**
         *
         */
        public int quantity;

        /**
         *
         */
        public int totalPrice;

        /**
         *
         */
        public String buyer;

        /**
         *
         * @param id
         * @param quantity
         * @param totalPrice
         * @param buyer
         */
        public BoughtItem(final int id, final int quantity, final int totalPrice, final String buyer) {
            this.id = id;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.buyer = buyer;
        }
    }
}
