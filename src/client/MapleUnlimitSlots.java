/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.Pair;

/**
 *
 * @author zjj
 */
public class MapleUnlimitSlots {

    protected int character_id;

    MapleUnlimitSlots(int character_id) {
        this.character_id = character_id;
    }

    public void addSlots() {
        addSlots(1);
    }

    public void addSlots(int n) {
        // update characters set unlimit_slots = unlimit_slots + {n} where id = {id};
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;

            ps = con.prepareStatement("update characters set unlimit_slots = unlimit_slots + ? where id = ?");
            ps.setInt(1, n);
            ps.setInt(2, character_id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSlots(int slots) {
        // update characters set unlimit_slots = {slots} where id = {id}
        try {
            // update characters set unlimit_slots = unlimit_slots + {n} where id = {id};
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;

            ps = con.prepareStatement("update characters set unlimit_slots = ? where id = ?");
            ps.setInt(1, slots);
            ps.setInt(2, character_id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getSlots() {
        // select unlimit_slots from characters where id = {id}
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("select unlimit_slots from characters where id = ?");
            ps.setInt(1, character_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                return z;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public int getUsedSlots() {
        // select count(1) as used_unlimit_slots from unlimit_slots_items where character_id = {id}
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("select count(1) as used_unlimit_slots from unlimit_slots_items where character_id = ?");
            ps.setInt(1, character_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                return z;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    // id => [item, count]
    public Map<Integer, Pair<Integer, Integer>> getItems() {
        Map<Integer, Pair<Integer, Integer>> res = new HashMap<>();
        try {
            // select * from unlimit_slots_items where character_id = {id}
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("select * from unlimit_slots_items where character_id = ?");
            ps.setInt(1, character_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id"),
                        iid = rs.getInt("item_id"),
                        c = rs.getInt("count");
                res.put(id, new Pair<>(iid, c));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public void addItem(int item_id, int count) {
        // insert into unlimit_slots_items (character_id, item_id, count) VALUES ({id}, {item_id}, {count})
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("insert into unlimit_slots_items (character_id, item_id, count) VALUES (?,?,?)");
            ps.setInt(1, character_id);
            ps.setInt(2, item_id);
            ps.setInt(3, count);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteItem(int id) {
        // delete from unlimit_slots_items where id = {id}
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("delete from unlimit_slots_items where id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Pair<Integer, Integer> getItemById(int id) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("select * from unlimit_slots_items where id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cid = rs.getInt("character_id"),
                        iid = rs.getInt("item_id"),
                        c = rs.getInt("count");
                if (cid == character_id) {
                    return new Pair<>(iid, c);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MapleUnlimitSlots.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
