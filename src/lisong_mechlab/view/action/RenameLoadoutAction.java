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
package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.OpRename;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.mechlab.LoadoutFrame;

public class RenameLoadoutAction extends AbstractAction {
	private static final String		SHORTCUT_STROKE		= "control R";
	private static final long		serialVersionUID	= -673375419929455179L;
	private final LoadoutFrame		loadoutFrame;
	private final LoadoutBase<?>	loadout;
	private final MessageXBar		xBar;
	private final OperationStack	stack;

	public RenameLoadoutAction(LoadoutBase<?> aLoadout, MessageXBar aXBar, OperationStack aStack) {
		super("Rename loadout...");
		loadout = aLoadout;
		loadoutFrame = null;
		xBar = aXBar;
		if (aStack == null)
			stack = new OperationStack(0); // Not undoable
		else
			stack = aStack;
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
	}

	public RenameLoadoutAction(LoadoutFrame aLoadoutFrame, MessageXBar aXBar) {
		super("Rename loadout...");
		loadout = aLoadoutFrame.getLoadout();
		loadoutFrame = aLoadoutFrame;
		xBar = aXBar;
		stack = loadoutFrame.getOpStack();
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
	}

	@Override
	public void actionPerformed(ActionEvent aE) {
		String name = JOptionPane.showInputDialog(loadoutFrame, "Give a name", loadout.getName());
		if (name == null || name.isEmpty()) {
			JOptionPane.showMessageDialog(loadoutFrame, "No name given!");
			return;
		}
		stack.pushAndApply(new OpRename(loadout, xBar, name));
	}
}
