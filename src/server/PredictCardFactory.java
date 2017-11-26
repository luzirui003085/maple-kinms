package server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

/**
 *
 * @author zjj
 */
public class PredictCardFactory {

    private static final PredictCardFactory instance = new PredictCardFactory();

    /**
     *
     */
    protected MapleDataProvider etcData;

    /**
     *
     */
    protected Map<Integer, PredictCard> predictCard;

    /**
     *
     */
    protected Map<Integer, PredictCardComment> predictCardComment;

    /**
     *
     */
    public PredictCardFactory() {
        this.etcData = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));
        this.predictCard = new HashMap();
        this.predictCardComment = new HashMap();
    }

    /**
     *
     * @return
     */
    public static PredictCardFactory getInstance() {
        return instance;
    }

    /**
     *
     */
    public void initialize() {
        if ((!this.predictCard.isEmpty()) || (!this.predictCardComment.isEmpty())) {
            return;
        }
        MapleData infoData = this.etcData.getData("PredictCard.img");

        for (MapleData cardDat : infoData) {
            if (cardDat.getName().equals("comment")) {
                continue;
            }
            PredictCard card = new PredictCard();
            card.name = MapleDataTool.getString("name", cardDat, "");
            card.comment = MapleDataTool.getString("comment", cardDat, "");
            this.predictCard.put(Integer.parseInt(cardDat.getName()), card);
        }

        MapleData commentData = infoData.getChildByPath("comment");
        for (MapleData commentDat : commentData) {
            PredictCardComment comment = new PredictCardComment();
            comment.worldmsg0 = MapleDataTool.getString("0", commentDat, "");
            comment.worldmsg1 = MapleDataTool.getString("1", commentDat, "");
            comment.score = MapleDataTool.getIntConvert("score", commentDat, 0);
            comment.effectType = MapleDataTool.getIntConvert("effectType", commentDat, 0);
            this.predictCardComment.put(Integer.parseInt(commentDat.getName()), comment);
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public PredictCard getPredictCard(int id) {
        if (!this.predictCard.containsKey(id)) {
            return null;
        }
        return (PredictCard) this.predictCard.get(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public PredictCardComment getPredictCardComment(int id) {
        if (!this.predictCardComment.containsKey(id)) {
            return null;
        }
        return (PredictCardComment) this.predictCardComment.get(id);
    }

    /**
     *
     * @return
     */
    public PredictCardComment RandomCardComment() {
        return getPredictCardComment(Randomizer.nextInt(this.predictCardComment.size()));
    }

    /**
     *
     * @return
     */
    public int getCardCommentSize() {
        return this.predictCardComment.size();
    }

    /**
     *
     */
    public static class PredictCard {

        /**
         *
         */
        public String name;

        /**
         *
         */
        public String comment;
    }

    /**
     *
     */
    public static class PredictCardComment {

        /**
         *
         */
        public int score;

        /**
         *
         */
        public int effectType;

        /**
         *
         */
        public String worldmsg0;

        /**
         *
         */
        public String worldmsg1;
    }
}
