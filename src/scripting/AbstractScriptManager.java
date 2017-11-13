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
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import client.MapleClient;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.script.ScriptException;
import tools.FileoutputUtil;

/**
 *
 * @author Matze
 */
public abstract class AbstractScriptManager {

    private static final ScriptEngineManager sem = new ScriptEngineManager();

    protected Invocable getInvocable(String path, MapleClient c) {
        return getInvocable(path, c, false);
    }

    protected Invocable getInvocable(String path, MapleClient c, boolean npc) {
        InputStreamReader fr = null;
        BufferedReader bf = null;
        try {

            path = "scripts/" + path;
            ScriptEngine engine = null;

            if (c != null) {
                engine = c.getScriptEngine(path);
            }
            if (engine == null) {
                File scriptFile = new File(path);
                if (!scriptFile.exists()) {
                    return null;
                }
                String javaver = System.getProperty("java.version").substring(0, 3);
                if (javaver.equals("1.8")) {
                    engine = sem.getEngineByName("nashorn");
                } else {
                    engine = sem.getEngineByName("javascript");
                }

                if (c != null) {
                    c.setScriptEngine(path, engine);
                }
                fr = new InputStreamReader(new FileInputStream(scriptFile), "gbk");
                if (javaver.equals("1.8")) {
                    bf = new BufferedReader(fr);
                    StringBuilder builder = new StringBuilder();
                    builder.append("load('nashorn:mozilla_compat.js');").append(System.lineSeparator());
                    String line = null;
                    while ((line = bf.readLine()) != null) {
                        builder.append(line).append(System.lineSeparator());
                    }
                    engine.eval(builder.toString());
                } else {
                    engine.eval(fr);
                }
            } else if (c != null && npc) {
                c.getPlayer().dropMessage(5, "你现在不能攻击或不能跟npc对话,请在点击拍卖边上的聊天来解除假死状态");
            }
            return (Invocable) engine;
        } catch (IOException | ScriptException e) {
            System.err.println("Error executing script. Path: " + path + "\nException " + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing script. Path: " + path + "\nException " + e);
            return null;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
}
