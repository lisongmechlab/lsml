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
package lisong_mechlab.util.message;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;

/**
 * A base interface for all messages sent on the {@link MessageXBar}.
 * 
 * @author Li Song
 */
public interface Message {
    /**
     * Classes that need to be able to listen for messages have to implement this interface. And find a
     * {@link MessageReception} object to attach to.
     * 
     * @author Li Song
     */
    public static interface Recipient {
        void receive(Message aMsg);
    }

    /**
     * Checks if this message is related to a specific {@link LoadoutStandard}.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to check.
     * @return <code>true</code> if this message affects the given {@link LoadoutStandard}.
     */
    public boolean isForMe(LoadoutBase<?> aLoadout);

    /**
     * @return <code>true</code> if this message can affect the damage or heat output of the related
     *         {@link LoadoutStandard}.
     */
    public boolean affectsHeatOrDamage();
}