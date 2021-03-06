
package scripting;

import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import client.MapleClient;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import server.quest.MapleQuest;

/**
 *
 * @author zjj
 */
public class NPCScriptManager extends AbstractScriptManager {

    private final Map<MapleClient, NPCConversationManager> cms = new WeakHashMap<>();
    private static final NPCScriptManager instance = new NPCScriptManager();

    /**
     *
     * @return
     */
    public static final NPCScriptManager getInstance() {
        return instance;
    }

    /**
     *
     * @param c
     * @param npc
     */
    public void start(MapleClient c, int npc) {
        start(c, npc, 0);
    }

    /**
     *
     * @param c
     * @param npc
     * @param wh
     */
    public final void start(final MapleClient c, final int npc, int wh) {
        final Lock lock = c.getNPCLock();
        lock.lock();
        try {
            if (c.getPlayer().isGM()) {
                if (wh == 0) {
                    c.getPlayer().dropMessage("[系统提示]您已经建立与NPC:" + npc + "的对话。");
                } else {
                    c.getPlayer().dropMessage("[系统提示]您已经建立与NPC:" + npc + "_" + wh + "的对话。");
                }
            }
            if (!cms.containsKey(c)) {
                Invocable iv;// = getInvocable("npc/" + npc + ".js", c, true);

                if (wh == 0) {
                    iv = getInvocable("npc/" + npc + ".js", c, true);
                } else {
                    iv = getInvocable("npc/" + npc + "_" + wh + ".js", c, true);
                }
                /*if (iv == null) {

                    iv = getInvocable("npc/notcoded.js", c, true); //safe disposal

                    if (iv == null) {
                        dispose(c);
                        return;
                    }
                }*/
                final ScriptEngine scriptengine = (ScriptEngine) iv;
                final NPCConversationManager cm;
                if (wh == 0) {
                    cm = new NPCConversationManager(c, npc, -1, (byte) -1, iv, 0);
                } else {
                    cm = new NPCConversationManager(c, npc, -1, (byte) -1, iv, wh);
                }
                cms.put(c, cm);
                if ((iv == null) || (getInstance() == null)) {
                    if (wh == 0) {
                        cm.sendOk("欢迎来到#b冒险岛#k。我暂时没有功能。如果你有好的建议或者好的想法可以让我拥有功能。可以联系管理员。\r\n我的ID是: #r" + npc + "#k.");
                    } else {
                        cm.sendOk("欢迎来到#b冒险岛#k。我暂时没有功能。如果你有好的建议或者好的想法可以让我拥有功能。可以联系管理员。\r\n我的ID是: #r" + npc + "_" + wh + "#k.");

                    }
                    //   cm.sendOk("欢迎来到#b冒险岛#k。我暂时没有功能。如果你有好的建议或者好的想法可以让我拥有功能。可以联系管理员。\r\n我的ID是: #r" + npc + "#k.\r\n  ---- 技术QQ：3114195408");
                    cm.dispose();
                    return;
                }
                scriptengine.put("cm", cm);
                scriptengine.put("npcid", npc);
                c.getPlayer().setConversation(1);

                try {
                    iv.invokeFunction("start"); // Temporary until I've removed all of start
                } catch (NoSuchMethodException nsme) {
                    iv.invokeFunction("action", (byte) 1, (byte) 0, 0);
                    // FileoutputUtil.log(FileoutputUtil.ScriptEx_LogAAA, "Error executing NPC script, NPC ID : " + npc + " exp:" + nsme.getMessage());
                }
            } else {
                c.getPlayer().dropMessage(5, "你现在不能攻击或不能跟npc对话,请在点击拍卖边上的聊天来解除假死状态");
            }

        } catch (final Exception e) {
            System.err.println("NPC 腳本錯誤, 它ID為 : " + npc + "_" + wh + "." + e);
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系統提示] NPC " + npc + "_" + wh + "腳本錯誤 " + e + "");
            }
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing NPC script, NPC ID : " + npc + "_" + wh + "." + e);
            dispose(c);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param c
     * @param mode
     * @param type
     * @param selection
     */
    public void action(final MapleClient c, final byte mode, final byte type, final int selection) {
        action(c, (byte) mode, (byte) type, selection, 0);
    }

    /**
     *
     * @param c
     * @param mode
     * @param type
     * @param selection
     * @param wh
     */
    public final void action(final MapleClient c, final byte mode, final byte type, final int selection, int wh) {
        if (mode != -1) {
            final NPCConversationManager cm = cms.get(c);
            if (cm == null || cm.getLastMsg() > -1) {
                return;
            }
            final Lock lock = c.getNPCLock();
            lock.lock();
            try {

                if (cm.pendingDisposal) {
                    dispose(c);
                } else if (wh == 0) {
                    cm.getIv().invokeFunction("action", mode, type, selection);
                } else {
                    cm.getIv().invokeFunction("action", mode, type, selection, wh);
                }
            } catch (final Exception e) {
                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系統提示] NPC " + cm.getNpc() + "_" + wh + "腳本錯誤 " + e + "");
                }
                System.err.println("NPC 腳本錯誤. 它ID為 : " + cm.getNpc() + "_" + wh + ":" + e);
                dispose(c);
                // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing NPC script, NPC ID : " + cm.getNpc() + "_" + wh + "." + e);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     *
     * @param c
     * @param npc
     * @param quest
     */
    public final void startQuest(final MapleClient c, final int npc, final int quest) {
        if (!MapleQuest.getInstance(quest).canStart(c.getPlayer(), null)) {
            return;
        }
        final Lock lock = c.getNPCLock();
        lock.lock();
        try {
            if (!cms.containsKey(c)) {
                final Invocable iv = getInvocable("quest/" + quest + ".js", c, true);
                if (iv == null) {
                    dispose(c);
                    return;
                }
                final ScriptEngine scriptengine = (ScriptEngine) iv;
                final NPCConversationManager cm = new NPCConversationManager(c, npc, quest, (byte) 0, iv, 0);
                cms.put(c, cm);
                scriptengine.put("qm", cm);

                c.getPlayer().setConversation(1);
                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系統提示]您已經建立與任務腳本:" + quest + "的往來。");
                }
                //System.out.println("NPCID started: " + npc + " startquest " + quest);
                iv.invokeFunction("start", (byte) 1, (byte) 0, 0); // start it off as something
            } else {
                dispose(c);
                // c.getPlayer().dropMessage(5, "You already are talking to an NPC. Use @ea if this is not intended.");
            }
        } catch (final Exception e) {
            System.err.println("Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + e);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + e);
            dispose(c);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param c
     * @param mode
     * @param type
     * @param selection
     */
    public final void startQuest(final MapleClient c, final byte mode, final byte type, final int selection) {
        final Lock lock = c.getNPCLock();
        final NPCConversationManager cm = cms.get(c);
        if (cm == null || cm.getLastMsg() > -1) {
            return;
        }
        lock.lock();
        try {
            if (cm.pendingDisposal) {
                dispose(c);
            } else {
                cm.getIv().invokeFunction("start", mode, type, selection);
            }
        } catch (Exception e) {
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系統提示]任務腳本:" + cm.getQuest() + "錯誤...NPC: " + cm.getNpc() + ":" + e);
            }
            System.err.println("Error executing Quest script. (" + cm.getQuest() + ")...NPC: " + cm.getNpc() + ":" + e);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Quest script. (" + cm.getQuest() + ")..NPCID: " + cm.getNpc() + ":" + e);
            dispose(c);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param c
     * @param npc
     * @param quest
     * @param customEnd
     */
    public final void endQuest(final MapleClient c, final int npc, final int quest, final boolean customEnd) {
        if (!customEnd && !MapleQuest.getInstance(quest).canComplete(c.getPlayer(), null)) {
            return;
        }
        final Lock lock = c.getNPCLock();
        //final NPCConversationManager cm = cms.get(c);
        lock.lock();
        try {
            if (!cms.containsKey(c)) {
                final Invocable iv = getInvocable("quest/" + quest + ".js", c, true);
                if (iv == null) {
                    dispose(c);
                    return;
                }
                final ScriptEngine scriptengine = (ScriptEngine) iv;
                final NPCConversationManager cm = new NPCConversationManager(c, npc, quest, (byte) 1, iv, 0);
                cms.put(c, cm);
                scriptengine.put("qm", cm);

                c.getPlayer().setConversation(1);
                //System.out.println("NPCID started: " + npc + " endquest " + quest);
                iv.invokeFunction("end", (byte) 1, (byte) 0, 0); // start it off as something
            } else {
                // c.getPlayer().dropMessage(5, "You already are talking to an NPC. Use @ea if this is not intended.");
            }
        } catch (Exception e) {
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系統提示]任務腳本:" + quest + "錯誤...NPC: " + quest + ":" + e);
            }
            System.err.println("Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + e);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + e);
            dispose(c);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param c
     * @param mode
     * @param type
     * @param selection
     */
    public final void endQuest(final MapleClient c, final byte mode, final byte type, final int selection) {
        final Lock lock = c.getNPCLock();
        final NPCConversationManager cm = cms.get(c);
        if (cm == null || cm.getLastMsg() > -1) {
            return;
        }
        lock.lock();
        try {
            if (cm.pendingDisposal) {
                dispose(c);
            } else {
                cm.getIv().invokeFunction("end", mode, type, selection);
            }
        } catch (Exception e) {
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系統提示]任務腳本:" + cm.getQuest() + "錯誤...NPC: " + cm.getNpc() + ":" + e);
            }
            System.err.println("Error executing Quest script. (" + cm.getQuest() + ")...NPC: " + cm.getNpc() + ":" + e);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Quest script. (" + cm.getQuest() + ")..NPCID: " + cm.getNpc() + ":" + e);
            dispose(c);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param c
     */
    public final void dispose(final MapleClient c) {
        final NPCConversationManager npccm = cms.get(c);
        if (npccm != null) {
            cms.remove(c);
            if (npccm.getType() == -1) {
                if (npccm.getwh() == 0) {
                    c.removeScriptEngine("scripts/npc/" + npccm.getNpc() + ".js");
                } else {
                    c.removeScriptEngine("scripts/npc/" + npccm.getNpc() + "_" + npccm.getwh() + ".js");
                }
                //  c.removeScriptEngine("scripts/npc/" + npccm.getNpc() + ".js");
                c.removeScriptEngine("scripts/npc/notcoded.js");
            } else {
                c.removeScriptEngine("scripts/quest/" + npccm.getQuest() + ".js");
            }
        }
        if (c.getPlayer() != null && c.getPlayer().getConversation() == 1) {
            c.getPlayer().setConversation(0);
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public final NPCConversationManager getCM(final MapleClient c) {
        return cms.get(c);
    }
}
