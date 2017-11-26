package server;

/**
 *
 * @author zjj
 */
public class MapleShopItem {

    private short buyable;
    private int itemId;
    private int price;

    /**
     *
     * @param buyable
     * @param itemId
     * @param price
     */
    public MapleShopItem(short buyable, int itemId, int price) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
    }

    /**
     *
     * @return
     */
    public short getBuyable() {
        return buyable;
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
    public int getPrice() {
        return price;
    }

}
