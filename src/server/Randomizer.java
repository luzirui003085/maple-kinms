package server;

import java.util.Random;

/**
 *
 * @author zjj
 */
public class Randomizer {

    private final static Random rand = new Random();

    /**
     *
     * @return
     */
    public static final int nextInt() {
        return rand.nextInt();
    }

    /**
     *
     * @param arg0
     * @return
     */
    public static final int nextInt(final int arg0) {
        return rand.nextInt(arg0);
    }

    /**
     *
     * @param bytes
     */
    public static final void nextBytes(final byte[] bytes) {
        rand.nextBytes(bytes);
    }

    /**
     *
     * @return
     */
    public static final boolean nextBoolean() {
        return rand.nextBoolean();
    }

    /**
     *
     * @return
     */
    public static final double nextDouble() {
        return rand.nextDouble();
    }

    /**
     *
     * @return
     */
    public static final float nextFloat() {
        return rand.nextFloat();
    }

    /**
     *
     * @return
     */
    public static final long nextLong() {
        return rand.nextLong();
    }

    /**
     *
     * @param lbound
     * @param ubound
     * @return
     */
    public static final int rand(final int lbound, final int ubound) {
        return (int) ((rand.nextDouble() * (ubound - lbound + 1)) + lbound);
    }
}
