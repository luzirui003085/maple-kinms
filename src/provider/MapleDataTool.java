/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

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
package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;
import provider.WzXML.MapleDataType;

/**
 *
 * @author zjj
 */
public class MapleDataTool {

    /**
     *
     * @param data
     * @return
     */
    public static String getString(MapleData data) {
        return ((String) data.getData());
    }

    /**
     *
     * @param data
     * @param def
     * @return
     */
    public static String getString(MapleData data, String def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return ((String) data.getData());
        }
    }

    /**
     *
     * @param path
     * @param data
     * @return
     */
    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    /**
     *
     * @param path
     * @param data
     * @param def
     * @return
     */
    public static String getString(String path, MapleData data, String def) {
        return getString(data.getChildByPath(path), def);
    }

    /**
     *
     * @param data
     * @return
     */
    public static double getDouble(MapleData data) {
        return ((Double) data.getData());
    }

    /**
     *
     * @param data
     * @return
     */
    public static float getFloat(MapleData data) {
        return ((Float) data.getData());
    }

    /**
     *
     * @param data
     * @param def
     * @return
     */
    public static float getFloat(MapleData data, float def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return ((Float) data.getData());
        }
    }

    /**
     *
     * @param data
     * @return
     */
    public static int getInt(MapleData data) {
        return ((Integer) data.getData());
    }

    /**
     *
     * @param data
     * @param def
     * @return
     */
    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else if (data.getType() == MapleDataType.SHORT) {
            return Integer.valueOf(((Short) data.getData()));
        } else {
            return ((Integer) data.getData());
        }
    }

    /**
     *
     * @param path
     * @param data
     * @return
     */
    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    /**
     *
     * @param data
     * @return
     */
    public static int getIntConvert(MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return getInt(data);
        }
    }

    /**
     *
     * @param path
     * @param data
     * @return
     */
    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        }
        return getInt(d);
    }

    /**
     *
     * @param path
     * @param data
     * @param def
     * @return
     */
    public static int getInt(String path, MapleData data, int def) {
        return getInt(data.getChildByPath(path), def);
    }

    /**
     *
     * @param path
     * @param data
     * @param def
     * @return
     */
    public static int getIntConvert(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            try {
                return Integer.parseInt(getString(d));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getInt(d, def);
        }
    }

    /**
     *
     * @param data
     * @return
     */
    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    /**
     *
     * @param data
     * @return
     */
    public static Point getPoint(MapleData data) {
        return ((Point) data.getData());
    }

    /**
     *
     * @param path
     * @param data
     * @return
     */
    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    /**
     *
     * @param path
     * @param data
     * @param def
     * @return
     */
    public static Point getPoint(String path, MapleData data, Point def) {
        final MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }

    /**
     *
     * @param data
     * @return
     */
    public static String getFullDataPath(MapleData data) {
        String path = "";
        MapleDataEntity myData = data;
        while (myData != null) {
            path = myData.getName() + "/" + path;
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }
}
