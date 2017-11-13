/*
 WNMS 召唤兽/魔宠系统
 生成地图实例 创建召唤兽
 */
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import tools.MockIOSession;

/**
 *
 * @author sqdzyk
 */

/**
 *
 * @author Patrick/PurpleMadness
 */
public class FakeCharacter {

    private MapleCharacter ch;
    private MapleCharacter owner;
    private boolean follow = true;

    public FakeCharacter(MapleCharacter player, int id, int lx) {
        String pz = null;

        /*int 品质 = player.读取pz();
        if (品质 == 1) {
            pz = "散仙";
        } else if (品质 == 2) {
            pz = "★真仙★";
        } else if (品质 == 3) {
            pz = "★玄仙★";
        } else if (品质 == 4) {
            pz = "★金仙★";
        } else if (品质 == 5) {
            pz = "◇准圣◇";
        } else if (品质 == 6) {
            pz = "◇圣人◆";
        } else if (品质 == 7) {
            pz = "◆鸿钧◆";
        }*/
        MapleCharacter clone = new MapleCharacter(true);
        clone.setFake();
        clone.setHair(player.getHair());
        clone.setFace(player.getFace());
        clone.setSkinColor(player.getSkinColor());
        clone.setName("〖" + player.getName() + "-" + pz + "〗", false);
        clone.setID(id + 1000000);
        clone.setLevel(player.getLevel());
        clone.setJob(player.getJob());
        clone.setMap(player.getMap());
        clone.setPosition(player.getPosition());
        MapleInventory equip;
        equip = clone.getInventory(MapleInventoryType.EQUIPPED );
        IItem weapon_item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
        Equip 帽子 = new Equip(1003766, (byte) -101);//真．戴碧斯魂
        Equip 勋章 = new Equip(1142443, (byte) -26);//勋章

        Equip 套装 = new Equip(1052553, (byte) -105);//压什么的套装
        Equip 披风 = new Equip(1102632, (byte) -109);//赎罪者
        Equip 鞋子 = new Equip(1072888, (byte) -107);//特效的 //1112115
        Equip RING1 = new Equip(1112586, (byte) -112);//特效的 //1112115  1112586
        clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(weapon_item);
        
        clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(帽子);
        clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(披风);
        clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(鞋子);
        clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(套装);
        clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(勋章);
         clone.getInventory(MapleInventoryType.EQUIPPED).addFromDB(RING1);
        player.getMap().addBotPlayer(clone, lx);
        clone.setClient(new MapleClient(null, null, new MockIOSession()) {

        });

        ch = clone;
        owner = player;
    }

    public MapleCharacter getFakeChar() {
        return ch;
    }

    public boolean follow() {
        return follow;
    }

    public void setFollow(boolean set) {
        follow = set;
    }

    public MapleCharacter getOwner() {
        return owner;
    }
}
