/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.graphs.DamageGraphModel;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.view.graphs.DamageGraphWindow;

/**
 * This a damage graph based on a {@link DamageGraphModel} in a separate window.
 * 
 * @author Emily Björk
 */
public class ShowDamageGraphAction extends AbstractAction {
    private static final long      serialVersionUID = -5939335331941199195L;
    private final Loadout<?>       loadout;
    private final MessageXBar      xBar;
    private final DamageGraphModel model;

    /**
     * Creates a new {@link ShowDamageGraphAction}.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to set armor for.
     * @param aXBar
     *            The {@link MessageXBar} to signal armor changes on.
     * @param aModel
     */
    public ShowDamageGraphAction(Loadout<?> aLoadout, MessageXBar aXBar, DamageGraphModel aModel) {
        super(aModel.getTitle());
        loadout = aLoadout;
        xBar = aXBar;
        model = aModel;
    }

    @SuppressWarnings("unused")
    @Override
    public void actionPerformed(ActionEvent aArg0) {
        new DamageGraphWindow(loadout, xBar, model);
    }
}
