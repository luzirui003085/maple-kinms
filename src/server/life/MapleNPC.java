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
package server.life;

import client.MapleClient;
import server.MapleShopFactory;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

/**
 *
 * @author zjj
 */
public class MapleNPC extends AbstractLoadedMapleLife {

    private String name = "MISSINGNO";
    private boolean custom = false;

    /**
     *
     * @param id
     * @param name
     */
    public MapleNPC(final int id, final String name) {
        super(id);
        this.name = name;
    }

    /**
     *
     * @return
     */
    public final boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    /**
     *
     * @param c
     */
    public final void sendShop(final MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    /**
     *
     * @param client
     */
    @Override
    public void sendSpawnData(final MapleClient client) {
        if (getId() >= 9901000) {
        } else {
            client.getSession().write(MaplePacketCreator.spawnNPC(this, true));
            client.getSession().write(MaplePacketCreator.spawnNPCRequestController(this, true));
        }
    }

    /**
     *
     * @param client
     */
    @Override
    public final void sendDestroyData(final MapleClient client) {
        client.getSession().write(MaplePacketCreator.removeNPC(getObjectId()));
    }

    /**
     *
     * @return
     */
    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    /**
     *
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     *
     * @param n
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     *
     * @return
     */
    public final boolean isCustom() {
        return custom;
    }

    /**
     *
     * @param custom
     */
    public final void setCustom(final boolean custom) {
        this.custom = custom;
    }
}
