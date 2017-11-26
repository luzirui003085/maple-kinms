package server.maps;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacket;
import tools.MaplePacketCreator;

/**
 *
 * @author zjj
 */
public class MapleLove extends AbstractMapleMapObject {

    private Point pos;
    private MapleCharacter owner;
    private String text;
    private int ft;
    private int itemid;

    /**
     *
     * @param owner
     * @param pos
     * @param ft
     * @param text
     * @param itemid
     */
    public MapleLove(MapleCharacter owner, Point pos, int ft, String text, int itemid) {
        this.owner = owner;
        this.pos = pos;
        this.text = text;
        this.ft = ft;
        this.itemid = itemid;
    }

    /**
     *
     * @return
     */
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.LOVE;
    }

    /**
     *
     * @return
     */
    @Override
    public Point getPosition() {
        return this.pos.getLocation();
    }

    /**
     *
     * @return
     */
    public MapleCharacter getOwner() {
        return this.owner;
    }

    /**
     *
     * @param position
     */
    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param client
     */
    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(makeDestroyData());
    }

    /**
     *
     * @param client
     */
    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(makeSpawnData());
    }

    /**
     *
     * @return
     */
    public MaplePacket makeSpawnData() {
        return MaplePacketCreator.spawnLove(getObjectId(), this.itemid, this.owner.getName(), this.text, this.pos, this.ft);
    }

    /**
     *
     * @return
     */
    public MaplePacket makeDestroyData() {
        return MaplePacketCreator.removeLove(getObjectId());
    }
}
