package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

/**
 *
 * @author zjj
 */
public class ItemMakerFactory {

    private final static ItemMakerFactory instance = new ItemMakerFactory();

    /**
     *
     */
    protected Map<Integer, ItemMakerCreateEntry> createCache = new HashMap<>();

    /**
     *
     */
    protected Map<Integer, GemCreateEntry> gemCache = new HashMap<>();

    /**
     *
     * @return
     */
    public static ItemMakerFactory getInstance() {
        // DO ItemMakerFactory.getInstance() on ChannelServer startup.
        return instance;
    }

    /**
     *
     */
    protected ItemMakerFactory() {
        System.out.println("Loading ItemMakerFactory :::");
        // 0 = Item upgrade crystals
        // 1 / 2/ 4/ 8 = Item creation

        final MapleData info = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz")).getData("ItemMake.img");

        byte totalupgrades, reqMakerLevel;
        int reqLevel, cost, quantity, stimulator;
        GemCreateEntry ret;
        ItemMakerCreateEntry imt;

        for (MapleData dataType : info.getChildren()) {
            int type = Integer.parseInt(dataType.getName());
            switch (type) {
                case 0: { // Caching of gem
                    for (MapleData itemFolder : dataType.getChildren()) {
                        reqLevel = MapleDataTool.getInt("reqLevel", itemFolder, 0);
                        reqMakerLevel = (byte) MapleDataTool.getInt("reqSkillLevel", itemFolder, 0);
                        cost = MapleDataTool.getInt("meso", itemFolder, 0);
                        quantity = MapleDataTool.getInt("itemNum", itemFolder, 0);
//			totalupgrades = MapleDataTool.getInt("tuc", itemFolder, 0); // Gem is always 0

                        ret = new GemCreateEntry(cost, reqLevel, reqMakerLevel, quantity);

                        for (MapleData rewardNRecipe : itemFolder.getChildren()) {
                            for (MapleData ind : rewardNRecipe.getChildren()) {
                                if (rewardNRecipe.getName().equals("randomReward")) {
                                    ret.addRandomReward(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("prob", ind, 0));
// MapleDataTool.getInt("itemNum", ind, 0)
                                } else if (rewardNRecipe.getName().equals("recipe")) {
                                    ret.addReqRecipe(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("count", ind, 0));
                                }
                            }
                        }
                        gemCache.put(Integer.parseInt(itemFolder.getName()), ret);
                    }
                    break;
                }
                case 1: // Warrior
                case 2: // Magician
                case 4: // Bowman
                case 8: // Thief
                case 16: { // Pirate
                    for (MapleData itemFolder : dataType.getChildren()) {
                        reqLevel = MapleDataTool.getInt("reqLevel", itemFolder, 0);
                        reqMakerLevel = (byte) MapleDataTool.getInt("reqSkillLevel", itemFolder, 0);
                        cost = MapleDataTool.getInt("meso", itemFolder, 0);
                        quantity = MapleDataTool.getInt("itemNum", itemFolder, 0);
                        totalupgrades = (byte) MapleDataTool.getInt("tuc", itemFolder, 0);
                        stimulator = MapleDataTool.getInt("catalyst", itemFolder, 0);

                        imt = new ItemMakerCreateEntry(cost, reqLevel, reqMakerLevel, quantity, totalupgrades, stimulator);

                        for (MapleData Recipe : itemFolder.getChildren()) {
                            for (MapleData ind : Recipe.getChildren()) {
                                if (Recipe.getName().equals("recipe")) {
                                    imt.addReqItem(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("count", ind, 0));
                                }
                            }
                        }
                        createCache.put(Integer.parseInt(itemFolder.getName()), imt);
                    }
                    break;
                }
            }
        }
    }

    /**
     *
     * @param itemid
     * @return
     */
    public GemCreateEntry getGemInfo(int itemid) {
        return gemCache.get(itemid);
    }

    /**
     *
     * @param itemid
     * @return
     */
    public ItemMakerCreateEntry getCreateInfo(int itemid) {
        return createCache.get(itemid);
    }

    /**
     *
     */
    public static class GemCreateEntry {

        private int reqLevel, reqMakerLevel;
        private int cost, quantity;
        private List<Pair<Integer, Integer>> randomReward = new ArrayList<>();
        private List<Pair<Integer, Integer>> reqRecipe = new ArrayList<>();

        /**
         *
         * @param cost
         * @param reqLevel
         * @param reqMakerLevel
         * @param quantity
         */
        public GemCreateEntry(int cost, int reqLevel, int reqMakerLevel, int quantity) {
            this.cost = cost;
            this.reqLevel = reqLevel;
            this.reqMakerLevel = reqMakerLevel;
            this.quantity = quantity;
        }

        /**
         *
         * @return
         */
        public int getRewardAmount() {
            return quantity;
        }

        /**
         *
         * @return
         */
        public List<Pair<Integer, Integer>> getRandomReward() {
            return randomReward;
        }

        /**
         *
         * @return
         */
        public List<Pair<Integer, Integer>> getReqRecipes() {
            return reqRecipe;
        }

        /**
         *
         * @return
         */
        public int getReqLevel() {
            return reqLevel;
        }

        /**
         *
         * @return
         */
        public int getReqSkillLevel() {
            return reqMakerLevel;
        }

        /**
         *
         * @return
         */
        public int getCost() {
            return cost;
        }

        /**
         *
         * @param itemId
         * @param prob
         */
        protected void addRandomReward(int itemId, int prob) {
            randomReward.add(new Pair<>(itemId, prob));
        }

        /**
         *
         * @param itemId
         * @param count
         */
        protected void addReqRecipe(int itemId, int count) {
            reqRecipe.add(new Pair<>(itemId, count));
        }
    }

    /**
     *
     */
    public static class ItemMakerCreateEntry {

        private int reqLevel;
        private int cost, quantity, stimulator;
        private byte tuc, reqMakerLevel;
        private List<Pair<Integer, Integer>> reqItems = new ArrayList<>(); // itemId / amount
        private List<Integer> reqEquips = new ArrayList<>();

        /**
         *
         * @param cost
         * @param reqLevel
         * @param reqMakerLevel
         * @param quantity
         * @param tuc
         * @param stimulator
         */
        public ItemMakerCreateEntry(int cost, int reqLevel, byte reqMakerLevel, int quantity, byte tuc, int stimulator) {
            this.cost = cost;
            this.tuc = tuc;
            this.reqLevel = reqLevel;
            this.reqMakerLevel = reqMakerLevel;
            this.quantity = quantity;
            this.stimulator = stimulator;
        }

        /**
         *
         * @return
         */
        public byte getTUC() {
            return tuc;
        }

        /**
         *
         * @return
         */
        public int getRewardAmount() {
            return quantity;
        }

        /**
         *
         * @return
         */
        public List<Pair<Integer, Integer>> getReqItems() {
            return reqItems;
        }

        /**
         *
         * @return
         */
        public List<Integer> getReqEquips() {
            return reqEquips;
        }

        /**
         *
         * @return
         */
        public int getReqLevel() {
            return reqLevel;
        }

        /**
         *
         * @return
         */
        public byte getReqSkillLevel() {
            return reqMakerLevel;
        }

        /**
         *
         * @return
         */
        public int getCost() {
            return cost;
        }

        /**
         *
         * @return
         */
        public int getStimulator() {
            return stimulator;
        }

        /**
         *
         * @param itemId
         * @param amount
         */
        protected void addReqItem(int itemId, int amount) {
            reqItems.add(new Pair<>(itemId, amount));
        }
    }
}
