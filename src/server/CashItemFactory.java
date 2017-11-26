package server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import database.DatabaseConnection;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.CashItemInfo.CashModInfo;

/**
 *
 * @author zjj
 */
public class CashItemFactory {

    private final static CashItemFactory instance = new CashItemFactory();
    private final static int[] bestItems = new int[]{50100010, 50100010, 50100010, 50100010, 50100010};
    private boolean initialized = false;
    private final Map<Integer, CashItemInfo> itemStats = new HashMap<>();
    private final Map<Integer, List<CashItemInfo>> itemPackage = new HashMap<>();
    private final Map<Integer, CashModInfo> itemMods = new HashMap<>();
    private final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));
    //是这个目录把，嗯
    private final MapleDataProvider itemStringInfo = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz"));
    private Map<Integer, Integer> idLookup = new HashMap();

    /**
     *
     * @return
     */
    public static final CashItemFactory getInstance() {
        return instance;
    }

    /**
     *
     */
    protected CashItemFactory() {
    }

    /**
     *
     */
    public void initialize() {
        System.out.println("Loading CashItemFactory :::");
        final List<Integer> itemids = new ArrayList<>();
        for (MapleData field : data.getData("Commodity.img").getChildren()) {
            final int SN = MapleDataTool.getIntConvert("SN", field, 0);
            final int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);
            final CashItemInfo stats = new CashItemInfo(itemId,
                    MapleDataTool.getIntConvert("Count", field, 1),
                    MapleDataTool.getIntConvert("Price", field, 0), SN,
                    MapleDataTool.getIntConvert("Period", field, 0),
                    MapleDataTool.getIntConvert("Gender", field, 2),
                    MapleDataTool.getIntConvert("OnSale", field, 0) > 0);
            if (SN > 0) {
                itemStats.put(SN, stats);
                idLookup.put(itemId, SN);
            }

            if (itemId > 0) {
                itemids.add(itemId);
            }
        }
        for (int i : itemids) {
            getPackageItems(i);
        }
        for (int i : itemStats.keySet()) {
            getModInfo(i);
            getItem(i); //init the modinfo's citem
        }
        initialized = true;
    }

    /**
     *
     * @param sn
     * @return
     */
    public final CashItemInfo getItem(int sn) {
        final CashItemInfo stats = itemStats.get(sn);
        // final CashItemInfo stats = itemStats.get(Integer.valueOf(sn));
        final CashModInfo z = getModInfo(sn);
        if (z != null && z.showUp) {
            return z.toCItem(stats); //null doesnt matter
        }
        if (stats == null || !stats.onSale()) {
            return null;
        }
        //hmm
        return stats;
    }

    /**
     *
     * @param itemId
     * @return
     */
    public final List<CashItemInfo> getPackageItems(int itemId) {
        if (itemPackage.get(itemId) != null) {
            return itemPackage.get(itemId);
        }
        final List<CashItemInfo> packageItems = new ArrayList<>();

        final MapleData b = data.getData("CashPackage.img");

        /*for (MapleData c : b.getChildren()) {
            if (c.getChildByPath("SN") == null) {
                continue;
            }
            for (MapleData d : c.getChildByPath("SN").getChildren()) {
                packageItems.add(itemStats.get(Integer.valueOf(MapleDataTool.getIntConvert(d))));
            }
            itemPackage.put(itemId, packageItems);
        }*/
        if (b == null || b.getChildByPath(itemId + "/SN") == null) {
            return null;
        }
        for (MapleData d : b.getChildByPath(itemId + "/SN").getChildren()) {
            packageItems.add(itemStats.get(MapleDataTool.getIntConvert(d)));
        }
        itemPackage.put(itemId, packageItems);
        return packageItems;
    }

    /**
     *
     * @param sn
     * @return
     */
    public final CashModInfo getModInfo(int sn) {
        CashModInfo ret = itemMods.get(sn);
        if (ret == null) {
            if (initialized) {
                return null;
            }
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_modified_items WHERE serial = ?");
                ps.setInt(1, sn);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ret = new CashModInfo(sn, rs.getInt("discount_price"), rs.getInt("mark"), rs.getInt("showup") > 0, rs.getInt("itemid"), rs.getInt("priority"), rs.getInt("package") > 0, rs.getInt("period"), rs.getInt("gender"), rs.getInt("count"), rs.getInt("meso"), rs.getInt("unk_1"), rs.getInt("unk_2"), rs.getInt("unk_3"), rs.getInt("extra_flags"));
                    itemMods.put(sn, ret);
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     *
     * @param itemid
     * @return
     */
    public final int getItemSN(int itemid) {
        for (Entry<Integer, CashItemInfo> ci : itemStats.entrySet()) {
            if (ci.getValue().getId() == itemid) {
                return ci.getValue().getSN();
            }
        }
        return 0;
    }

    /**
     *
     * @return
     */
    public final Collection<CashModInfo> getAllModInfo() {
        if (!initialized) {
            initialize();
        }
        return itemMods.values();
    }

    /**
     *
     * @return
     */
    public final int[] getBestItems() {
        return bestItems;
    }

    /**
     *
     * @param itemId
     * @return
     */
    public int getSnFromId(int itemId) {
        return idLookup.get(itemId);
    }

    /**
     *
     */
    public final void clearCashShop() {
        itemStats.clear();
        itemPackage.clear();
        itemMods.clear();
        idLookup.clear();
        initialized = false;
        initialize();
    }
}
