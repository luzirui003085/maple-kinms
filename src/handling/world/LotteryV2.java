package handling.world;

import database.DatabaseConnection;
import server.Timer;
import tools.MaplePacketCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LotteryV2 {
//    private final static Logger log = Logger.getLogger(LotteryV2.class.getName());
//    private final static int MAX = 10; // 总共多少个数
//    private final static int NUM = 5; // 选几个数
//    private final static float RATING = 0.5f; // 从奖池拿出来分的钱比例
//    private final static int MONEY = 100; // 彩票单价(w)
//    private final static SimpleDateFormat FMT = new SimpleDateFormat("MMddHHmm"); // 时间格式化
//    private static AtomicInteger poolMoney = new AtomicInteger(0); // 目前为止的累计奖池
//
//    public static void init() {
//        initPool(); // 初始化奖金池
//        initTimer(); // 初始化定时器
//    }
//
//    private static void initPool() {
//        int date = Integer.parseInt(FMT.format(currentStage().getTime()));
//        int lastPoolMoney = 0;
//        try {
//            // 上一期剩下多少钱
//            Connection con = DatabaseConnection.getConnection();
//            try (PreparedStatement ps = con.prepareStatement("SELECT money FROM lotteries WHERE date < ? ORDER BY date DESC LIMIT 1")) {
//                ps.setInt(1, date);
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        lastPoolMoney = rs.getInt(1);
//                    }
//                }
//            }
//            // 当前期已经买了多少钱
//            try (PreparedStatement ps = con.prepareStatement("SELECT sum(rate) FROM lottery WHERE date = ?")) {
//                ps.setInt(1, date);
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        poolMoney.set(lastPoolMoney + MONEY * rs.getInt(1));
//                    }
//                }
//            }
//            System.out.println("[彩票]当前期号:" + date);
//            System.out.println("[彩票]上一期为止累计奖金池" + lastPoolMoney);
//            System.out.println("[彩票]当前累计奖金池" + poolMoney);
//        } catch (Exception e) {
//            System.out.println("[彩票]加载奖金池错误" + e.getMessage());
//        }
//    }
//
//    private static void initTimer() {
//        // 先算当前这小时的最后一小时距离现在的毫秒差
//        long now = Calendar.getInstance().getTimeInMillis();
//        Calendar currentStage = currentStage();
//        long current = currentStage.getTimeInMillis();
//        long next = nextStage(currentStage).getTimeInMillis();
//        // 开奖计时器
//        System.out.println("[彩票]下次开奖在" + (next - now) / 1000 + "秒之后");
//        Timer.EtcTimer.getInstance().register(() -> {
//            int c = Integer.parseInt(FMT.format(currentStage().getTime()));
//            String n = number();
//            checkLottery(c, n); // 检查有谁中奖了
//        }, next - current, next - now); // 1小时
//    }
//
//    private static void checkLottery(int date, String num) {
//        System.out.println("[彩票]当前期号：[" + date + "]，当前期开奖号码：[" + num + "]");
//        // 然后查找lottery中的中奖的比例人数  人数*他们对应的倍率
//        int lotteryNum = 0;
//        Connection con = DatabaseConnection.getConnection();
//        try {
//            try (PreparedStatement ps = con.prepareStatement("SELECT sum(rate) FROM lottery WHERE date = ? AND number = ? AND money = 0")) {
//                ps.setInt(1, date);
//                ps.setString(2, num);
//                ResultSet rs = ps.executeQuery();
//                if (rs.next()) {
//                    lotteryNum = rs.getInt(1);
//                }
//            }
//        } catch (SQLException ex) {
//            System.out.println("[彩票]计算中奖人数错误");
//            log.log(Level.SEVERE, null, ex);
//        }
//        System.out.println("[彩票]中奖比例人数：" + lotteryNum);
//        int lotterySalaryPerChar = 0, lotterySalary = 0;
//        if (lotteryNum > 0) {
//            lotterySalary = Math.round(poolMoney.get() * RATING); // 需要拿出来分的钱
//            lotterySalaryPerChar = Math.round(lotterySalary / lotteryNum); // 单位人获取到的奖金
//            System.out.println("[彩票]每个单位人获取金额：" + lotterySalaryPerChar);
//            // 更新彩票的兑奖金额
//            try {
//                try (PreparedStatement ps = con.prepareStatement("UPDATE lottery SET money = ? * rate WHERE date = ? AND number = ? AND money = 0")) {
//                    ps.setInt(1, lotterySalaryPerChar);
//                    ps.setInt(2, date);
//                    ps.setString(3, num);
//                    ps.executeUpdate();
//                }
//                poolMoney.addAndGet(-lotterySalary);
//            } catch (SQLException ex) {
//                System.out.println("[彩票]更新彩票兑奖金额失败：" + ex.getMessage());
//                log.log(Level.SEVERE, null, ex);
//            }
//        } else {
//            System.out.println("[彩票]没人中奖");
//        }
//        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "本期彩票开奖号码是:[" + num + "] 共 " + lotteryNum + " 人次中奖，他们将平分 " + lotterySalary * 10000 + " 冒险币").getBytes());
//        // 然后将 date number pool写入到数据库中
//        try {
//            try (PreparedStatement ps = con.prepareStatement("INSERT INTO lotteries (date, number, money, salary) VALUES (?,?,?,?)")) {
//                ps.setInt(1, date);
//                ps.setString(2, num);
//                ps.setInt(3, poolMoney.get());
//                ps.setInt(4, lotterySalaryPerChar);
//                ps.execute();
//            }
//        } catch (SQLException ex) {
//            System.out.println("[彩票]创建彩票记录失败" + ex.getMessage());
//            log.log(Level.SEVERE, null, ex);
//        }
//    }
//
//    // 当前期：yymmdd+当前小时
//    private static Calendar currentStage() {
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.MINUTE, 0);
//        c.set(Calendar.SECOND, 0);
//        return c;
//    }
//
//    // 下一期
//    private static Calendar nextStage(Calendar c) {
//        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
//        c.set(Calendar.MINUTE, 0);
//        c.set(Calendar.SECOND, 0);
//        return c;
//    }
//
//    // 随机生成彩票号码
//    public static String number() {
//        int[] arr = new int[MAX];
//        int i;
//        //初始的有序数组
//        for (i = 0; i < MAX; i++) {
//            arr[i] = i + 1;
//        }
//        //费雪耶兹置乱算法
//        for (i = arr.length - 1; i > MAX - NUM - 1; i--) {
//            // 随机数生成器，范围[0, i]
//            int rand = (new Random()).nextInt(i + 1);
//            int temp = arr[i];
//            arr[i] = arr[rand];
//            arr[rand] = temp;
//        }
//        List<Integer> l = new ArrayList<>();
//        for (i = arr.length - 1; i > MAX - NUM - 1; --i) {
//            l.add(arr[i]);
//        }
//        return formatNumber(l);
//    }
//
//    public static String formatNumber(String number) {
//        String[] l3 = number.split(" ");
//        List<Integer> l2 = new ArrayList<>();
//        for (String s : l3) {
//            int i = Integer.parseInt(s);
//            l2.add(i);
//        }
//        return formatNumber(l2);
//    }
//
//    private static String formatNumber(List<Integer> arr) {
//        Collections.sort(arr);
//        StringBuilder sb = new StringBuilder();
//        for (Object o : arr) {
//            sb.append(o).append(" ");
//        }
//        return sb.toString().trim();
//    }
//
//
//    public static boolean createLottery(int charid, String number, int rate) {
//        int date = Integer.parseInt(FMT.format(currentStage().getTime()));
//
//        return createLottery(charid, number, rate, date);
//    }
//
//    public static boolean createLottery(int charid, String number, int rate, int date) {
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            try {
//                try (PreparedStatement ps = con.prepareStatement("INSERT INTO lottery (character_id, number, rate, date) VALUES (?,?,?,?)")) {
//                    ps.setInt(1, charid);
//                    ps.setString(2, number);
//                    ps.setInt(3, rate);
//                    ps.setInt(4, date);
//                    ps.execute();
//                }
//                poolMoney.addAndGet(rate * MONEY);
//            } catch (SQLException ex) {
//                System.out.println("[彩票]插入彩票失败");
//                log.log(Level.SEVERE, null, ex);
//                return false;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//        return true;
//    }
//
//    public static boolean updateLottery(int id) {
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            try (PreparedStatement ps = con.prepareStatement("UPDATE lottery SET money = -money WHERE id = ?")) {
//                ps.setInt(1, id);
//                ps.executeUpdate();
//                return true;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public static ArrayList<Map<String, Object>> getLotteries(int charid, int limit) {
//        ArrayList<Map<String, Object>> lst = new ArrayList<>();
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            try (PreparedStatement ps = con.prepareStatement("SELECT id,number,date,rate,money FROM lottery WHERE character_id = ? ORDER BY id DESC LIMIT ?")) {
//                ps.setInt(1, charid);
//                ps.setInt(2, limit);
//                ResultSet rs = ps.executeQuery();
//
//                while (rs.next()) {
//                    Map<String, Object> m = new HashMap<>();
//                    m.put("id", rs.getInt("id"));
//                    m.put("number", rs.getString("number"));
//                    m.put("date", rs.getInt("date"));
//                    m.put("rate", rs.getInt("rate"));
//                    m.put("money", rs.getInt("money"));
//                    lst.add(m);
//                }
//            }
//        } catch (Exception ignored) {
//        }
//        return lst;
//    }
//
//    public static Map<String, Object> getLottery(int id) {
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            try (PreparedStatement ps = con.prepareStatement("SELECT id,number,date,rate,money FROM lottery WHERE id = ? LIMIT 1")) {
//                ps.setInt(1, id);
//                ResultSet rs = ps.executeQuery();
//
//                if (rs.next()) {
//                    Map<String, Object> m = new HashMap<>();
//                    m.put("id", rs.getInt("id"));
//                    m.put("number", rs.getString("number"));
//                    m.put("date", rs.getInt("date"));
//                    m.put("rate", rs.getInt("rate"));
//                    m.put("money", rs.getInt("money"));
//                    return m;
//                }
//            }
//        } catch (Exception ignored) {
//        }
//        return null;
//    }
}
