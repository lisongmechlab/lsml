/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//@formatter:on
package org.lisoft.lsml.view.models;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage.ChangeMsg;

/**
 * This model is used for 'Mech upgrades.
 * 
 * @author Li Song
 *
 */
public abstract class UpgradeModel extends BinaryAttributeModel {

    private LoadoutBase<?> loadout;
    private ChangeMsg      messageType;

    /**
     * @param aMessageReception
     * @param aLoadout
     * @param aMessageType
     */
    public UpgradeModel(MessageReception aMessageReception, LoadoutBase<?> aLoadout, ChangeMsg aMessageType) {
        super(aMessageReception);
        loadout = aLoadout;

        messageType = aMessageType;
    }


    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout)) {
            if (aMsg instanceof UpgradesMessage) {
                UpgradesMessage message = (UpgradesMessage) aMsg;
                if (message.msg == messageType) {
//                    fireStateChanged();
                }
            }
        }
    }

}
