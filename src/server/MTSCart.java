/*
 This file is part of the ZeroFusion MapleStory Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

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
package server;

import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tools.Pair;

/**
 *
 * @author zjj
 */
public class MTSCart implements Serializable {

    private static final long serialVersionUID = 231541893513373578L;
    private int characterId, tab = 1, type = 0, page = 0;
    //tab; 1 = buy now, 2 = wanted, 3 = auction, 4 = cart
    //type = inventorytype; 0 = anything
    //page = whatever
    private List<IItem> transfer = new ArrayList<>();
    private List<Integer> cart = new ArrayList<>();
    private List<Integer> notYetSold = new ArrayList<>(10);
    private int owedNX = 0;

    /**
     *
     * @param characterId
     * @throws SQLException
     */
    public MTSCart(int characterId) throws SQLException {
        this.characterId = characterId;
        for (Pair<IItem, MapleInventoryType> item : ItemLoader.MTS_TRANSFER.loadItems(false, characterId).values()) {
            transfer.add(item.getLeft());
        }
        loadCart();
        loadNotYetSold();
    }

    /**
     *
     * @return
     */
    public List<IItem> getInventory() {
        return transfer;
    }

    /**
     *
     * @param item
     */
    public void addToInventory(IItem item) {
        transfer.add(item);
    }

    /**
     *
     * @param item
     */
    public void removeFromInventory(IItem item) {
        transfer.remove(item);
    }

    /**
     *
     * @return
     */
    public List<Integer> getCart() {
        return cart;
    }

    /**
     *
     * @param car
     * @return
     */
    public boolean addToCart(final int car) {
        if (!cart.contains(car)) {
            cart.add(car);
            return true;
        }
        return false;
    }

    /**
     *
     * @param car
     */
    public void removeFromCart(final int car) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i) == car) {
                cart.remove(i);
            }
        }
    }

    /**
     *
     * @return
     */
    public List<Integer> getNotYetSold() {
        return notYetSold;
    }

    /**
     *
     * @param car
     */
    public void addToNotYetSold(final int car) {
        notYetSold.add(car);
    }

    /**
     *
     * @param car
     */
    public void removeFromNotYetSold(final int car) {
        for (int i = 0; i < notYetSold.size(); i++) {
            if (notYetSold.get(i) == car) {
                notYetSold.remove(i);
            }
        }
    }

    /**
     *
     * @return
     */
    public final int getSetOwedNX() {
        final int on = owedNX;
        owedNX = 0;
        return on;
    }

    /**
     *
     * @param newNX
     */
    public void increaseOwedNX(final int newNX) {
        owedNX += newNX;
    }

    /**
     *
     * @throws SQLException
     */
    public void save() throws SQLException {
        List<Pair<IItem, MapleInventoryType>> itemsWithType = new ArrayList<>();

        for (IItem item : getInventory()) {
            itemsWithType.add(new Pair<>(item, GameConstants.getInventoryType(item.getItemId())));
        }

        ItemLoader.MTS_TRANSFER.saveItems(itemsWithType, characterId);
        final Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE characterid = ?");
        ps.setInt(1, characterId);
        ps.execute();
        ps.close();
        ps = con.prepareStatement("INSERT INTO mts_cart VALUES(DEFAULT, ?, ?)");
        ps.setInt(1, characterId);
        for (int i : cart) {
            ps.setInt(2, i);
            ps.executeUpdate();
        }
        if (owedNX > 0) {
            ps.setInt(2, -owedNX);
            ps.executeUpdate();
        }
        ps.close();
        //notYetSold shouldnt be saved here
    }

    /**
     *
     * @throws SQLException
     */
    public void loadCart() throws SQLException {
        final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_cart WHERE characterid = ?");
        ps.setInt(1, characterId);
        final ResultSet rs = ps.executeQuery();
        int iId;
        while (rs.next()) {
            iId = rs.getInt("itemid");
            if (iId < 0) {
                owedNX -= iId;
            } else if (MTSStorage.getInstance().check(iId)) {
                cart.add(iId);
            }
        }
        rs.close();
        ps.close();
    }

    /**
     *
     * @throws SQLException
     */
    public void loadNotYetSold() throws SQLException {
        final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_items WHERE characterid = ?");
        ps.setInt(1, characterId);
        final ResultSet rs = ps.executeQuery();
        int pId;
        while (rs.next()) {
            pId = rs.getInt("id");
            if (MTSStorage.getInstance().check(pId)) {
                notYetSold.add(pId);
            }
        }
        rs.close();
        ps.close();
    }

    /**
     *
     * @param tab
     * @param type
     * @param page
     */
    public void changeInfo(final int tab, final int type, final int page) {
        this.tab = tab;
        this.type = type;
        this.page = page;
    }

    /**
     *
     * @return
     */
    public int getTab() {
        return tab;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public int getPage() {
        return page;
    }
}
