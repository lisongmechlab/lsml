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
package lisong_mechlab.view.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.garage.MechGarage;
import lisong_mechlab.model.garage.OpRemoveFromGarage;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.mechlab.LoadoutFrame;

public class DeleteLoadoutAction extends AbstractAction implements Reader {
	private static final long serialVersionUID = -4813215864397617783L;
	private static final String SHORTCUT_STROKE = "control D";
	private final LoadoutBase<?> loadout;
	private final MechGarage garage;
	private final LoadoutFrame loadoutFrame;

	public DeleteLoadoutAction(MessageXBar anXBar, MechGarage aGarage, LoadoutFrame aLoadoutFrame) {
		this(anXBar, aGarage, aLoadoutFrame, aLoadoutFrame.getLoadout());
	}

	public DeleteLoadoutAction(MessageXBar anXBar, MechGarage aGarage, LoadoutBase<?> aLoadout) {
		this(anXBar, aGarage, null, aLoadout);
	}

	private DeleteLoadoutAction(MessageXBar anXBar, MechGarage aGarage, LoadoutFrame aLoadoutFrame,
			LoadoutBase<?> aLoadout) {
		super("Delete loadout");
		loadoutFrame = aLoadoutFrame;
		loadout = aLoadout;
		garage = aGarage;
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
		setEnabled(garage.getMechs().contains(loadout));
		anXBar.attach(this);
	}

	@Override
	public void actionPerformed(ActionEvent aE) {
		if (garage.getMechs().contains(loadout)) {
			Component source = loadoutFrame == null ? ProgramInit.lsml() : loadoutFrame;

			int result = JOptionPane.showConfirmDialog(source, "Are you certain you want to delete the loadout: "
					+ loadout.getName() + "?", "Confirm operation", JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION == result) {
				try {
					ProgramInit.lsml().garageOperationStack.pushAndApply(new OpRemoveFromGarage(garage, loadout));
				} catch (RuntimeException e) {
					JOptionPane
							.showMessageDialog(
									source,
									"An error occured!\n"
											+ "Please report an issue at https://github.com/lisongmechlab/lsml/issues and copy paste the following this message:\n"
											+ e.getMessage() + "\nStack trace:\n" + e.getStackTrace());
				}
			}
		}
	}

	@Override
	public void receive(Message aMsg) {
		if (aMsg instanceof MechGarage.Message) {
			MechGarage.Message msg = (MechGarage.Message) aMsg;
			if (msg.isForMe(loadout)) {
				setEnabled(garage.getMechs().contains(loadout));
			}
		}
	}
}
