package client.messages;

import tools.StringUtil;

/**
 *
 * @author Emilyx3
 */
public class CommandProcessorUtil {

    /**
     *
     * @param splitted
     * @param str
     * @return
     */
    public static String joinAfterString(String splitted[], String str) {
        for (int i = 1; i < splitted.length; i++) {
            if (splitted[i].equalsIgnoreCase(str) && i + 1 < splitted.length) {
                return StringUtil.joinStringFrom(splitted, i + 1);
            }
        }
        return null;
    }

    /**
     *
     * @param splitted
     * @param position
     * @param def
     * @return
     */
    public static int getOptionalIntArg(String splitted[], int position, int def) {
        if (splitted.length > position) {
            try {
                return Integer.parseInt(splitted[position]);
            } catch (NumberFormatException nfe) {
                return def;
            }
        }
        return def;
    }

    /**
     *
     * @param splitted
     * @param startpos
     * @param name
     * @return
     */
    public static String getNamedArg(String splitted[], int startpos, String name) {
        for (int i = startpos; i < splitted.length; i++) {
            if (splitted[i].equalsIgnoreCase(name) && i + 1 < splitted.length) {
                return splitted[i + 1];
            }
        }
        return null;
    }

    /**
     *
     * @param splitted
     * @param startpos
     * @param name
     * @return
     */
    public static Long getNamedLongArg(String splitted[], int startpos, String name) {
        String arg = getNamedArg(splitted, startpos, name);
        if (arg != null) {
            try {
                return Long.parseLong(arg);
            } catch (NumberFormatException nfe) {
                // swallow - we don't really care
            }
        }
        return null;
    }

    /**
     *
     * @param splitted
     * @param startpos
     * @param name
     * @return
     */
    public static Integer getNamedIntArg(String splitted[], int startpos, String name) {
        String arg = getNamedArg(splitted, startpos, name);
        if (arg != null) {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException nfe) {
                // swallow - we don't really care
            }
        }
        return null;
    }

    /**
     *
     * @param splitted
     * @param startpos
     * @param name
     * @param def
     * @return
     */
    public static int getNamedIntArg(String splitted[], int startpos, String name, int def) {
        Integer ret = getNamedIntArg(splitted, startpos, name);
        if (ret == null) {
            return def;
        }
        return ret;
    }

    /**
     *
     * @param splitted
     * @param startpos
     * @param name
     * @return
     */
    public static Double getNamedDoubleArg(String splitted[], int startpos, String name) {
        String arg = getNamedArg(splitted, startpos, name);
        if (arg != null) {
            try {
                return Double.parseDouble(arg);
            } catch (NumberFormatException nfe) {
                // swallow - we don't really care
            }
        }
        return null;
    }
}
