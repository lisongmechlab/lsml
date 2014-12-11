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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import lisong_mechlab.view.preferences.PreferencesDialog;

/**
 * This action will open up the settings dialog.
 * 
 * @author Li Song
 */
public class OpenPreferences extends AbstractAction {
    private static final long serialVersionUID = -2615543435137494754L;

    public OpenPreferences(String aString, KeyStroke aKeyStroke) {
        super(aString);
        putValue(Action.ACCELERATOR_KEY, aKeyStroke);
    }

    @SuppressWarnings("unused")
    // Constructor has desired side effects
    @Override
    public void actionPerformed(ActionEvent aArg0) {
        new PreferencesDialog();
    }
}
