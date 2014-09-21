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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.export.SmurfyImportExport;
import lisong_mechlab.util.SwingHelpers;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.mechlab.LoadoutFrame;

/**
 * This action will upload the given loadout to Smurfy's website.
 * 
 * @author Li Song
 */
public class ExportToSmurfyAction extends AbstractAction {
	private static final long	serialVersionUID	= -2600531408508841174L;
	private final LoadoutFrame	loadoutFrame;

	public ExportToSmurfyAction(LoadoutFrame aLoadoutFrame) {
		super("Export to smurfy...");
		loadoutFrame = aLoadoutFrame;
	}

	@Override
	public void actionPerformed(ActionEvent aArg0) {
		LoadoutBase<?> loadout = loadoutFrame.getLoadout();

		SmurfyImportExport export = new SmurfyImportExport(null, ProgramInit.lsml().loadoutCoder);

		try {
			String url = export.sendLoadout(loadout);

			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel("Your loadout is available at:"), BorderLayout.NORTH);
			JLabel link = new JLabel();
			panel.add(link);
			SwingHelpers.hypertextLink(link, url, url);

			JOptionPane.showMessageDialog(loadoutFrame, panel, "Export complete", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(loadoutFrame, "Error: " + e.getMessage(), "Unable to export to smurfy!",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
