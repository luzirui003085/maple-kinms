
package handling.channel.handler;

import client.MapleCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import server.maps.AnimatedMapleMapObject;
import server.maps.FakeCharacter;
import server.movement.*;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author zjj
 */
public class MovementParse {

    //1 = player, 2 = mob, 3 = pet, 4 = summon, 5 = dragon

    /**
     *
     * @param lea
     * @param kind
     * @param chr
     * @return
     */
    public static final List<LifeMovementFragment> parseMovement(final SeekableLittleEndianAccessor lea, int kind, MapleCharacter chr) {
        final List<LifeMovementFragment> res = new ArrayList<>();
        final byte numCommands = lea.readByte();
        String 类型 = "";
        switch (kind) {
            case 1:
                类型 = "角色移动";
                break;
            case 2:
                类型 = "怪物移动";
                break;
            case 3:
                类型 = "宠物移动";
                break;
            case 4:
                类型 = "召唤兽移动";
                break;
            case 5:
                类型 = "龙移动";
                break;
            default:
                break;
        }
        for (byte i = 0; i < numCommands; i++) {
            final byte command = lea.readByte();
            if (chr.hasFakeChar()) {
                for (final FakeCharacter ch : chr.getFakeChars()) {
                    if (ch.follow() && ch.getFakeChar().getMap() == chr.getMap() && command == 0) {
                        return null;
                    }
                }
            }
            switch (command) {
                case -1: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short unk = lea.readShort();
                    final short fh = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    final BounceMovement bm = new BounceMovement(command, new Point(xpos, ypos), duration, newstate);
                    bm.setFH(fh);
                    bm.setUnk(unk);
                    res.add(bm);
                    break;
                }
                case 0: // normal move
                case 5:
                case 17: // Float
                {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    final AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
		    // log.trace("Move to {},{} command {} wobble {},{} ? {} state {} duration {}", new Object[] { xpos,
                    // xpos, command, xwobble, ywobble, newstate, duration });
                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 6: // fj
                case 12:
                case 13: // Shot-jump-back thing
                case 16: { // Float
                    final short xmod = lea.readShort();
                    final short ymod = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    final RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);
                    res.add(rlm);
		    // log.trace("Relative move {},{} state {}, duration {}", new Object[] { xmod, ymod, newstate,
                    // duration });
                    break;
                }
                case 3:
                case 4: // tele... -.-
                case 7: // assaulter
                case 8: // assassinate
                case 9: // rush
                case 14:
                {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final byte newstate = lea.readByte();
                    final TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
                    tm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(tm);
                    break;
                }
                case 10: // change equip ???
                    res.add(new ChangeEquipSpecialAwesome(command, lea.readByte()));
                    break;
                case 11: // chair
                {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short unk = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    final ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setUnk(unk);
                    res.add(cm);
                    break;
                }
                case 15: { // Jump Down
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    final short fh = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    final JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
                    jdm.setUnk(unk);
                    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    jdm.setFH(fh);

                    res.add(jdm);
                    break;
                }
                case 20:
                case 21:
                case 22: {
                    int unk = lea.readShort();
                    int newstate = lea.readByte();
                   final AranMovement acm = new AranMovement(command, new Point(0, 0), unk, newstate);
                        res.add(acm);
                }
                case 18:
                case 19:
                /*case 19:
                case 20: // Aran Combat Step
                case 21: {
                    final short xmod = lea.readShort();
                    final short ymod = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    final AranMovement am = new AranMovement(command, new Point(xmod, ymod), duration, newstate);

                    res.add(am);
                    break;
                }*/
                default:
                  //  System.out.println("Kind movement: " + 类型 + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    return null;
            }
        }
        if (numCommands != res.size()) {
        //    System.out.println("error in movement");
            return null; // Probably hack
        }
        return res;
    }

    /**
     *
     * @param movement
     * @param target
     * @param yoffset
     */
    public static final void updatePosition(final List<LifeMovementFragment> movement, final AnimatedMapleMapObject target, final int yoffset) {
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
