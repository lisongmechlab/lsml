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
package lisong_mechlab.util;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * This class holds some static helper functions for dealing with SWING.
 * 
 * @author Emily Björk
 */
public class SwingHelpers {
	public static void bindAction(JComponent aComponent, String aCommand, Action anAction) {
		Object o = anAction.getValue(Action.ACCELERATOR_KEY);
		if (o instanceof KeyStroke)
			bindAction(aComponent, aCommand, anAction, (KeyStroke) o);
		else
			throw new IllegalArgumentException("Can not bind action, it has no keystroke assigned.");
	}

	public static void bindAction(JComponent aComponent, String aCommand, Action anAction, KeyStroke aKeyStroke) {
		aComponent.getInputMap(JComponent.WHEN_FOCUSED).put(aKeyStroke, aCommand);
		aComponent.getActionMap().put(aCommand, anAction);
	}

	public static void hypertextLink(final JLabel aLabel, final String aUrl, String aText) {
		aLabel.setText("<html><a href=\"" + aUrl + "\">" + aText + "</a></html>");
		aLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		aLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(aUrl));
				} catch (URISyntaxException | IOException ex) {
					JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(aLabel), "Unable to open link!");
				}
			}
		});
	}
}
