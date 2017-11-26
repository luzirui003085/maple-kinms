/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package handling.channel.handler;

/**
 *
 * @author Administrator
 */
public class Beans {
    private int number;
    private int type;
    private int pos;

    /**
     *
     * @param pos
     * @param type
     * @param number
     */
    public Beans(int pos, int type, int number) {
        this.pos = pos;
        this.number = number;
        this.type = type;
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
    public int getNumber() {
        return number;
    }

    /**
     *
     * @return
     */
    public int getPos() {
        return pos;
    }
    
}
