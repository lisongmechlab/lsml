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
package org.lisoft.lsml.view.mechlab;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.mechlab.equipmentpanel.EquipmentPanel;
import org.lisoft.lsml.view.mechlab.equipmentpanel.ModuleSeletionList;
import org.lisoft.lsml.view.mechlab.garagetree.GarageTree;
import org.lisoft.lsml.view.mechlab.loadoutframe.LoadoutFrame;
import org.lisoft.lsml.view.preferences.Preferences;

/**
 * This class shows the 'mech lab pane in the main tabbed pane.
 * 
 * @author Emily Björk
 */
public class MechLabPane extends JSplitPane {
    private static final long serialVersionUID = 1079910953509846928L;
    private final DesktopPane desktop;
    private final MessageXBar xBar;

    public MechLabPane(MessageXBar anXBar, Preferences aPreferences) {
        super(JSplitPane.HORIZONTAL_SPLIT, true);
        xBar = anXBar;
        desktop = new DesktopPane(xBar);

        JTextField filterBar = new JTextField();
        JPanel filterPanel = new JPanel(new BorderLayout(5, 5));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        filterPanel.add(new JLabel("Filter:"), BorderLayout.LINE_START);
        filterPanel.add(filterBar, BorderLayout.CENTER);

        JPanel garagePanel = new JPanel(new BorderLayout());
        garagePanel.add(filterPanel, BorderLayout.PAGE_START);
        garagePanel.add(new JScrollPane(new GarageTree(desktop, xBar, filterBar, aPreferences)), BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Equipment", new EquipmentPanel(desktop, xBar));
        tabbedPane.addTab("Garage", garagePanel);

        JPanel modulesPanel = new ScrollablePanel();
        modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.PAGE_AXIS));

        for (ModuleSlot slotType : ModuleSlot.values()) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBorder(StyleManager.sectionBorder(slotType.toString()));
            panel.add(new ModuleSeletionList(desktop, anXBar, slotType), BorderLayout.CENTER);
            modulesPanel.add(panel);
        }

        tabbedPane.addTab("Modules", new JScrollPane(modulesPanel));

        setLeftComponent(tabbedPane);
        setRightComponent(desktop);
        setDividerLocation(getLeftComponent().getMinimumSize().width);
    }

    /**
     * Will open the given {@link Loadout} into the desktop pane by creating a new {@link LoadoutFrame}.
     * 
     * @param aLoadout
     *            The {@link Loadout} to create the frame for.
     * @param aDropShipMode
     *            If this loadout was opened from a drop ship.
     */
    public void openLoadout(Loadout<?> aLoadout, boolean aDropShipMode) {
        desktop.openLoadout(aLoadout, aDropShipMode);
    }

    public void openDropShip(DropShip aDropShip) {
        desktop.openDropShip(aDropShip);
    }

    /**
     * @return The currently selected loadout.
     */
    public Loadout<?> getCurrentLoadout() {
        if (null != getActiveLoadoutFrame())
            return getActiveLoadoutFrame().getLoadout();
        return null;
    }

    /**
     * @return The currently selected {@link LoadoutFrame}.
     */
    public LoadoutFrame getActiveLoadoutFrame() {
        return (LoadoutFrame) desktop.getSelectedFrame();
    }

    /**
     * Will open the given {@link LoadoutStandard} into the desktop pane by creating a new {@link LoadoutFrame}.
     * 
     * @param aLSMLUrl
     *            The LSML link to open.
     */
    public void openLoadout(String aLSMLUrl) {
        assert (SwingUtilities.isEventDispatchThread());
        try {
            openLoadout(ProgramInit.lsml().loadoutCoder.parse(aLSMLUrl), false);
        }
        catch (DecodingException e) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(),
                    "Unable to import loadout from \"" + aLSMLUrl + "\"!\n\nError:\n" + e);
        }
        catch (Throwable e) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(),
                    "Unable to decode: " + aLSMLUrl + "\n\n" + "The link is malformed.\nError:" + e);
        }
    }

    /**
     * @return <code>true</code> if the pane was closed, <code>false</code> otherwise.
     */
    public boolean close() {
        return desktop.closeAll();
    }

}
