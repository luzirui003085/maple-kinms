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
package scripting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import client.MapleClient;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import server.MaplePortal;

public class PortalScriptManager {

    private static final PortalScriptManager instance = new PortalScriptManager();
    private final Map<String, PortalScript> scripts = new HashMap<>();
    private final static ScriptEngineFactory sef;

    static {
        String javaver = System.getProperty("java.version").substring(0, 3);
        sef = new ScriptEngineManager().getEngineByName(javaver.equals("1.8") ? "nashorn" : "javascript").getFactory();
    }

    public final static PortalScriptManager getInstance() {
        return instance;
    }

    private PortalScript getPortalScript(MapleClient c, final String scriptName) {
        if (scripts.containsKey(scriptName)) {
            return scripts.get(scriptName);
        }

        final File scriptFile = new File("scripts/portal/" + scriptName + ".js");
        if (c.getPlayer().isGM()) {
            c.getPlayer().dropMessage("[系统提示]您已经建立与PortalScript:[" + scriptName + ".js]的对话。");
        }
        if (!scriptFile.exists()) {
            scripts.put(scriptName, null);
            return null;
        }

        InputStreamReader fr = null;
        final ScriptEngine portal = sef.getScriptEngine();
        try {
            fr = new InputStreamReader(new FileInputStream(scriptFile), "gbk");
            CompiledScript compiled = ((Compilable) portal).compile(fr);
            compiled.eval();
        } catch (final Exception e) {
            System.err.println("Error executing Portalscript: " + scriptName + ":" + e);
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Portal script. (" + scriptName + ") " + e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (final IOException e) {
                    System.err.println("ERROR CLOSING" + e);
                }
            }
        }
        final PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
        scripts.put(scriptName, script);
        return script;
    }

    public final void executePortalScript(final MaplePortal portal, final MapleClient c) {
        final PortalScript script = getPortalScript(c, portal.getScriptName());

        if (script != null) {
            try {
                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示]您已经建立与PortalScriptA:[" + portal.getScriptName() + ".js]的对话。");
                }
                script.enter(new PortalPlayerInteraction(c, portal));
            } catch (Exception e) {
                System.err.println("Error entering Portalscript: " + portal.getScriptName() + ":" + e);
            }
        } else {
            System.out.println("Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
        }
    }

    public final void clearScripts() {
        scripts.clear();
    }
}
