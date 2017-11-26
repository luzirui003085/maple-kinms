package server;

import client.inventory.MapleInventoryType;

/**
 *
 * @author zjj
 */
public class CashItemInfoA {

    private int SN;
    private int itemId;
    private int count;
    private int price;
    private int period;
    private int gender;
    private boolean onSale;

    /**
     *
     * @param SN
     * @param itemId
     * @param count
     * @param price
     * @param period
     * @param gender
     * @param onSale
     */
    public CashItemInfoA(int SN, int itemId, int count, int price, int period, int gender, boolean onSale) {
        this.SN = SN;
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.period = period;
        this.gender = gender;
        this.onSale = onSale;
    }

    /**
     *
     * @return
     */
    public int getSN() {
        return SN;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return itemId;
    }

    /**
     *
     * @param g
     * @return
     */
    public boolean genderEquals(int g) {
        return g == this.gender || this.gender == 2;
    }

    /**
     *
     * @return
     */
    public int getItemId() {
        return itemId;
    }

    /**
     *
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     *
     * @return
     */
    public int getPrice() {
        return price;
    }

    /**
     *
     * @return
     */
    public int getPeriod() {
        return period;
    }

    /**
     *
     * @return
     */
    public int getGender() {
        return gender;
    }

    /**
     *
     * @return
     */
    public boolean onSale() {
        return onSale;
    }

    /**
     *
     * @param itemId
     * @return
     */
    public static MapleInventoryType getInventoryType(int itemId) {
        byte type = (byte) (itemId / 1000000);
        if ((type < 1) || (type > 5)) {
            return MapleInventoryType.UNDEFINED;
        }
        return MapleInventoryType.getByType(type);
    }

    /**
     *
     * @param i
     * @return
     */
    public int getItemId(int i) {
        return itemId;
    }
}