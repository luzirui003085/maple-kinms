
package handling.login;

import handling.MapleServerHandler;
import handling.mina.MapleCodecFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.ServerProperties;
import tools.Triple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author zjj
 */
public class LoginServer {

    /**
     *
     */
    public static int PORT = 8484;
    private static IoAcceptor acceptor;
    private static Map<Integer, Integer> load = new HashMap<>();
    private static String serverName, eventMessage;
    private static byte flag;
    private static int maxCharacters, userLimit, usersOn = 0;
    private static boolean finishedShutdown = true, adminOnly = false;
    private static final HashMap<Integer, Triple<String, String, Integer>> loginAuth = new HashMap<>();
    private static final HashSet<String> loginIPAuth = new HashSet<>();

    private static LoginServer instance = new LoginServer();

    /**
     * @return
     */
    public static LoginServer getInstance() {
        return instance;
    }

    /**
     * @param chrid
     * @param ip
     * @param tempIp
     * @param channel
     */
    public static void putLoginAuth(int chrid, String ip, String tempIp, int channel) {
        loginAuth.put(chrid, new Triple(ip, tempIp, channel));
        loginIPAuth.add(ip);
    }

    /**
     * @param chrid
     * @return
     */
    public static Triple<String, String, Integer> getLoginAuth(int chrid) {
        return (Triple) loginAuth.remove(chrid);
    }

    /**
     * @param ip
     * @return
     */
    public static boolean containsIPAuth(String ip) {
        return loginIPAuth.contains(ip);
    }

    /**
     * @param ip
     */
    public static void removeIPAuth(String ip) {
        loginIPAuth.remove(ip);
    }

    /**
     * @param ip
     */
    public static void addIPAuth(String ip) {
        loginIPAuth.add(ip);
    }

    /**
     * @param channel
     */
    public static final void addChannel(final int channel) {
        load.put(channel, 0);
    }

    /**
     * @param channel
     */
    public static final void removeChannel(final int channel) {
        load.remove(channel);
    }

    /**
     *
     */
    public static final void run_startup_configurations() {
        userLimit = Integer.parseInt(ServerProperties.getProperty("KinMS.UserLimit"));
        serverName = ServerProperties.getProperty("KinMS.ServerName");
        eventMessage = ServerProperties.getProperty("KinMS.EventMessage");
        flag = Byte.parseByte(ServerProperties.getProperty("KinMS.Flag"));
        PORT = Integer.parseInt(ServerProperties.getProperty("KinMS.LPort"));
        adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("KinMS.Admin", "false"));
        maxCharacters = Integer.parseInt(ServerProperties.getProperty("KinMS.MaxCharacters"));

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));

        acceptor.setHandler(new MapleServerHandler(-1, false));
        //acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);

        try {
            acceptor.bind(new InetSocketAddress(PORT));
            System.out.println("服务器   蓝蜗牛: 启动端口 " + PORT);
        } catch (IOException e) {
            System.err.println("Binding to port " + PORT + " failed" + e);
        }
    }

    /**
     *
     */
    public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("Shutting down login...");
        acceptor.unbind(new InetSocketAddress(PORT));
        finishedShutdown = true; //nothing. lol
    }

    /**
     * @return
     */
    public static final String getServerName() {
        return serverName;
    }

    /**
     * @return
     */
    public static final String getEventMessage() {
        return eventMessage;
    }

    /**
     * @return
     */
    public static final byte getFlag() {
        return flag;
    }

    /**
     * @return
     */
    public static final int getMaxCharacters() {
        return maxCharacters;
    }

    /**
     * @return
     */
    public static final Map<Integer, Integer> getLoad() {
        return load;
    }

    /**
     * @param load_
     * @param usersOn_
     */
    public static void setLoad(final Map<Integer, Integer> load_, final int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    /**
     * @param newMessage
     */
    public static final void setEventMessage(final String newMessage) {
        eventMessage = newMessage;
    }

    /**
     * @param newflag
     */
    public static final void setFlag(final byte newflag) {
        flag = newflag;
    }

    /**
     * @return
     */
    public static final int getUserLimit() {
        return userLimit;
    }

    /**
     * @return
     */
    public static final int getUsersOn() {
        return usersOn;
    }

    /**
     * @param newLimit
     */
    public static final void setUserLimit(final int newLimit) {
        userLimit = newLimit;
    }

    /**
     * @return
     */
    public static final int getNumberOfSessions() {
        return acceptor.getManagedSessions().size();
    }

    /**
     * @return
     */
    public static final boolean isAdminOnly() {
        return adminOnly;
    }

    /**
     * @return
     */
    public static final boolean isShutdown() {
        return finishedShutdown;
    }

    /**
     *
     */
    public static final void setOn() {
        finishedShutdown = false;
    }
}
