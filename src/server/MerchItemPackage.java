package server;

import client.inventory.IItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zjj
 */
public class MerchItemPackage {

    private long sentTime;
    private int mesos = 0, packageid;
    private List<IItem> items = new ArrayList<>();

    /**
     *
     * @param items
     */
    public void setItems(List<IItem> items) {
        this.items = items;
    }

    /**
     *
     * @return
     */
    public List<IItem> getItems() {
        return items;
    }

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
    public int getPackageid() {
        return packageid;
    }

    /**
     *
     * @param packageid
     */
    public void setPackageid(int packageid) {
        this.packageid = packageid;
    }
}
