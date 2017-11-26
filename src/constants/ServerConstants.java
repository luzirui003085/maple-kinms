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
package constants;

import server.ServerProperties;

/**
 *
 * @author zjj
 */
public class ServerConstants {

    // Start of Poll

    /**
     *
     */
    public static final boolean PollEnabled = false;

    /**
     *
     */
    public static final String Poll_Question = "Are you mudkiz?";

    /**
     *
     */
    public static final String[] Poll_Answers = {"test1", "test2", "test3"};
    // End of Poll
    
    /**
     *
     */
    public static final short MAPLE_VERSION = 0x4f;

    /**
     *
     */
    public static final String MAPLE_PATCH = "1";

    /**
     *
     */
    public static final boolean Use_Fixed_IV = false;

    /**
     *
     */
    public static final int MIN_MTS = 110;

    /**
     *
     */
    public static final int MTS_BASE = 100; //+1000 to everything in MSEA but cash is costly here

    /**
     *
     */
    public static final int MTS_TAX = 10; //+% to everything

    /**
     *
     */
    public static final int MTS_MESO = 5000; //mesos needed

    /**
     *
     */
    public static final int CHANNEL_COUNT = 200;

    /**
     *
     */
    public static String PACKET_ERROR = "";

    /**
     *
     */
    public static int Channel = 0;

    /**
     *
     */
    public static int removePlayerFromMap = 0;

    /**
     *
     */
    public static int getHello = 0;

    /**
     *
     */
    public static final boolean PACKET_ERROR_OFF = Boolean.parseBoolean(ServerProperties.getProperty("KinMS.记录38错误", "false"));
    //服务端输出操作

    /**
     *
     */
    public static boolean 封包显示 = Boolean.parseBoolean(ServerProperties.getProperty("KinMS.封包显示", "false"));

    /**
     *
     */
    public static boolean 调试输出封包 = Boolean.parseBoolean(ServerProperties.getProperty("KinMS.调试输出封包", "false"));

    /**
     *
     * @param ERROR
     */
    public void setPACKET_ERROR(String ERROR) {
        PACKET_ERROR = ERROR;
    }

    /**
     *
     * @return
     */
    public String getPACKET_ERROR() {
        return PACKET_ERROR;
    }

    /**
     *
     * @param ERROR
     */
    public void setChannel(int ERROR) {
        Channel = ERROR;
    }

    /**
     *
     * @return
     */
    public int getChannel() {
        return Channel;
    }

    /**
     *
     * @param ERROR
     */
    public void setRemovePlayerFromMap(int ERROR) {
        removePlayerFromMap = ERROR;
    }

    /**
     *
     * @return
     */
    public int getRemovePlayerFromMap() {
        return removePlayerFromMap;
    }

    /**
     *
     * @param ERROR
     */
    public void setHello(int ERROR) {
        getHello = ERROR;
    }

    /**
     *
     * @return
     */
    public int getHello() {
        return getHello;
    }

    /**
     *
     */
    public static final String CashShop_Key = "a;!%dfb_=*-a123d9{P~";

    /**
     *
     */
    public static final String Login_Key = "pWv]xq:SPTCtk^LGnU9F";

    /**
     *
     */
    public static final String[] Channel_Key = {"a56=-_dcSAgb",
        "y5(9=8@nV$;G",
        "yS5j943GzdUm",
        "G]R8Frg;kx6Y",
        "Z)?7fh*([N6S",
        "p4H8=*sknaEK",
        "A!Z7:mS.2?Kq",
        "M5:!rfv[?mdF",
        "Ee@3-7u5s6xy",
        "p]6L3eS(R;8A",
        "gZ,^k9.npy#F",
        "cG3M,*7%@zgt",
        "t+#@TV^3)hL9",
        "mw4:?sAU7[!6",
        "b6L]HF(2S,aE",
        "H@rAq]#^Y3+J",
        "o2A%wKCuqc7Txk5?#rNZ",
        "d4.Np*B89C6+]y2M^z-7",
        "oTL2jy9^zkH.84u(%b[d",
        "WCSJZj3tGX,[4hu;9s?g"
    };

    /*
     * Specifics which job gives an additional EXP to party
     * returns the percentage of EXP to increase
     */

    /**
     *
     * @param job
     * @return
     */

    public static final byte Class_Bonus_EXP(final int job) {
        switch (job) {
            case 3000: //whenever these arrive, they'll give bonus
            case 3200:
            case 3210:
            case 3211:
            case 3212:
            case 3300:
            case 3310:
            case 3311:
            case 3312:
            case 3500:
            case 3510:
            case 3511:
            case 3512:
                return 10;
        }
        return 0;
    }

    /**
     *
     */
    public static enum PlayerGMRank {

        /**
         *
         */
        NORMAL('@', 0),

        /**
         *
         */
        INTERN('!', 1),

        /**
         *
         */
        GM('!', 2),

        /**
         *
         */
        ADMIN('!', 3);
        //SUPERADMIN('!', 3);
        private char commandPrefix;
        private int level;

        private PlayerGMRank(char ch, int level) {
            commandPrefix = ch;
            this.level = level;
        }

        /**
         *
         * @return
         */
        public char getCommandPrefix() {
            return commandPrefix;
        }

        /**
         *
         * @return
         */
        public int getLevel() {
            return level;
        }
    }

    /**
     *
     */
    public static enum CommandType {

        /**
         *
         */
        NORMAL(0),

        /**
         *
         */
        TRADE(1);
        private int level;

        private CommandType(int level) {
            this.level = level;
        }

        /**
         *
         * @return
         */
        public int getType() {
            return level;
        }
    }
}
