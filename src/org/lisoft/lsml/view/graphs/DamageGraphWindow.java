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
package org.lisoft.lsml.view.graphs;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.lisoft.lsml.model.graphs.DamageGraphModel;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.ProgramInit;

/**
 * <p>
 * Presents a graph of damage over range for a given load out.
 * <p>
 * TODO: The calculation part should be extracted and unit tested!
 * 
 * @author Li Song
 */
public class DamageGraphWindow extends JFrame {
    /**
     * Creates and displays the {@link DamageGraphWindow}.
     * 
     * @param aLoadout
     *            The loadout to show the graph for.
     * @param aXbar
     *            A {@link MessageXBar} to listen to changes to the loadout on.
     * @param aModel
     *            A {@link DamageGraphModel} to display.
     */
    public DamageGraphWindow(LoadoutBase<?> aLoadout, MessageXBar aXbar, DamageGraphModel aModel) {
        super(aModel.getTitle() + " for " + aLoadout.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        DamageGraphPanel panel = new DamageGraphPanel(aLoadout, aXbar, aModel);
        setContentPane(panel);

        // chartPanel.setLayout(new OverlayLayout(chartPanel));
        // JButton button = new JButton(new OpenHelp("What is this?", "Max-sustained-dps-graph",
        // KeyStroke.getKeyStroke('w')));
        // button.setMargin(new Insets(5, 5, 5, 5));
        // button.setFocusable(false);
        // button.setAlignmentX(Component.RIGHT_ALIGNMENT);
        // button.setAlignmentY(Component.TOP_ALIGNMENT);
        // chartPanel.add(button);

        setIconImage(ProgramInit.programIcon);
        setSize(800, 600);
        setVisible(true);
    }

}
