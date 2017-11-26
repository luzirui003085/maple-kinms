package server;

import client.inventory.IItem;

/**
 *
 * @author zjj
 */
public class MapleDueyActions {

    private String sender = null;
    private IItem item = null;
    private int mesos = 0;
    private int quantity = 1;
    private long sentTime;
    private int packageId = 0;

    /**
     *
     * @param pId
     * @param item
     */
    public MapleDueyActions(int pId, IItem item) {
        this.item = item;
        this.quantity = item.getQuantity();
        packageId = pId;
    }

    /**
     *
     * @param pId
     */
    public MapleDueyActions(int pId) { // meso only package
        this.packageId = pId;
    }

    /**
     *
     * @return
     */
    public String getSender() {
        return sender;
    }

    /**
     *
     * @param name
     */
    public void setSender(String name) {
        sender = name;
    }

    /**
     *
     * @return
     */
    public IItem getItem() {
        return item;
    }

    /**
     *
     * @return
     */
    public int getMesos() {
        return mesos;
    }

    /**
     *
     * @param set
     */
    public void setMesos(int set) {
        mesos = set;
    }

    /**
     *
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     *
     * @return
     */
    public int getPackageId() {
        return packageId;
    }

    /*    public boolean isExpired() {
     Calendar cal1 = Calendar.getInstance();
     cal1.set(year, month - 1, day);
     long diff = System.currentTimeMillis() - cal1.getTimeInMillis();
     int diffDays = (int) Math.abs(diff / (24 * 60 * 60 * 1000));
     return diffDays > 30;
     }

     public long sentTimeInMilliseconds() {
     Calendar cal = Calendar.getInstance();
     cal.set(year, month, day);
     return cal.getTimeInMillis();
     }*/

    /**
     *
     * @param sentTime
     */

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    /**
     *
     * @return
     */
    public long getSentTime() {
        return sentTime;
    }
}
