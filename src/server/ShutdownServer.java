package server;

import java.sql.SQLException;

import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.World.Alliance;
import handling.world.World.Broadcast;
import handling.world.World.Family;
import handling.world.World.Guild;
import org.apache.log4j.Logger;
import server.Timer.*;
import tools.MaplePacketCreator;

public class ShutdownServer implements Runnable {

    private static final ShutdownServer instance = new ShutdownServer();
    public static boolean running = false;
    private static final Logger log = Logger.getLogger(ShutdownServer.class);
     public int mode = 0;

    public static ShutdownServer getInstance() {
        return instance;
    }

   /* @Override
    public void run() {
        synchronized (this) {
            if (running) { //Run once!
                return;
            }
            running = true;
        }
        World.isShutDown = true;
        try {
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.setShutdown();
            }
            LoginServer.shutdown();
            Integer[] chs = ChannelServer.getAllInstance().toArray(new Integer[0]);

            for (int i : chs) {
                try {
                    ChannelServer cs = ChannelServer.getInstance(i);
                    synchronized (this) {
                        cs.shutdown(this);
//                        try {
//                            this.wait();
//                        } catch (InterruptedException ex) {
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            CashShopServer.shutdown();
            World.Guild.save();
            World.Alliance.save();
            World.Family.save();
            DatabaseConnection.closeAll();
        } catch (SQLException e) {
            System.err.println("THROW" + e);
        }
        WorldTimer.getInstance().stop();
        MapTimer.getInstance().stop();
        MobTimer.getInstance().stop();
        BuffTimer.getInstance().stop();
        CloneTimer.getInstance().stop();
        EventTimer.getInstance().stop();
        EtcTimer.getInstance().stop();

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            //shutdown
        }
        System.exit(0); //not sure if this is really needed for ChannelServer
    }*/

    public void shutdown() {
        run();
    }

    @Override
    public void run() {
        if (this.mode == 0) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, " 游戏服务器将关闭维护，请玩家安全下线..."));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.setShutdown();
                cs.setServerMessage("游戏服务器将关闭维护，请玩家安全下线...");
                cs.closeAllMerchants();
            }
            Guild.save();
            Alliance.save();
            Family.save();
            System.out.println("服务端关闭事件 1 已完成.");
            this.mode += 1;
        } else if (this.mode == 1) {
            this.mode += 1;
            System.out.println("服务端关闭事件 2 开始...");
            try {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, " 游戏服务器将关闭维护，请玩家安全下线..."));
                Integer[] chs = (Integer[]) ChannelServer.getAllInstance().toArray(new Integer[0]);
                for (int i:chs) {
                    try {
                        ChannelServer cs = ChannelServer.getInstance(i);
                        synchronized (this) {
                            cs.shutdown();
                        }
                    } catch (Exception e) {
                        log.error("关闭服务端错误 - 3" + e);
                    }
                }
                LoginServer.shutdown();
                CashShopServer.shutdown();
                DatabaseConnection.closeAll();
            } catch (SQLException e) {
                log.error("关闭服务端错误 - 4" + e);
            }
            Timer.WorldTimer.getInstance().stop();
            Timer.MapTimer.getInstance().stop();
            Timer.BuffTimer.getInstance().stop();
            Timer.CloneTimer.getInstance().stop();
            Timer.EventTimer.getInstance().stop();
            Timer.EtcTimer.getInstance().stop();
            //Timer.PingTimer.getInstance().stop();
            System.out.println("服务端关闭事件 2 已完成.");
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                        log.error("关闭服务端错误 - 2" + e);
            }
            System.exit(0);
        }
    }
}
