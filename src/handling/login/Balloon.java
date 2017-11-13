/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handling.login;

/**
 *
 * @author Administrator
 */
public class Balloon {

    public int nX;
    public int nY;
    public String sMessage;

    public Balloon(String sMessage, int nX, int nY) {
        this.sMessage = sMessage;
        this.nX = nX;
        this.nY = nY;
    }
}
