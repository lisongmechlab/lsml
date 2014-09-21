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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.loadout.LoadoutMessage.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This operation renames a loadout.
 * 
 * @author Emily Björk
 */
public class OpRename extends Operation {
	private String					oldName;
	private final String			newName;

	protected final MessageXBar		xBar;
	protected final LoadoutBase<?>	loadout;

	/**
	 * @param aLoadout
	 *            The {@link LoadoutStandard} to rename.
	 * @param anXBar
	 *            A {@link MessageXBar} to announce the change on.
	 * @param aName
	 *            The new name of the loadout.
	 */
	public OpRename(LoadoutBase<?> aLoadout, MessageXBar anXBar, String aName) {
		loadout = aLoadout;
		xBar = anXBar;
		newName = aName;
	}

	@Override
	public void undo() {
		if (oldName == loadout.getName())
			return;
		loadout.rename(oldName);
		xBar.post(new LoadoutMessage(loadout, Type.RENAME));
	}

	@Override
	public void apply() {
		oldName = loadout.getName();
		if (oldName == newName)
			return;
		loadout.rename(newName);
		if (xBar != null)
			xBar.post(new LoadoutMessage(loadout, Type.RENAME));
	}

	@Override
	public String describe() {
		return "rename loadout";
	}
}
