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
package org.lisoft.lsml.view.mechlab.garagetree;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view.LoadoutTransferHandler;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.action.CloneLoadoutAction;
import org.lisoft.lsml.view.action.DeleteLoadoutAction;
import org.lisoft.lsml.view.action.RenameLoadoutAction;
import org.lisoft.lsml.view.mechlab.DesktopPane;
import org.lisoft.lsml.view.preferences.Preferences;

public class GarageTree extends JTree {
    private static final long serialVersionUID = -8856874024057864775L;
    GarageTreeModel           model            = null;
    private final MessageXBar xBar;

    public GarageTree(final DesktopPane aLoadoutDesktop, MessageXBar anXBar, JTextField aFilterBar,
            Preferences aPreferences) {
        model = new GarageTreeModel(anXBar, aFilterBar, this, aPreferences);
        xBar = anXBar;

        ToolTipManager.sharedInstance().registerComponent(this);
        setModel(model);
        setDragEnabled(true);
        setRootVisible(false);
        setShowsRootHandles(true);
        aLoadoutDesktop.addInternalFrameListener(model);
        setTransferHandler(new LoadoutTransferHandler());

        for (int i = 0; i < model.getChildCount(model.getRoot()); ++i) {
            TreePath path = new TreePath(new Object[] { model.getRoot(), model.getChild(model.getRoot(), i) });
            expandPath(path);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Object clicked = getClickedObject(e);
                    if (clicked instanceof Loadout<?>) {
                        GarageTree.this.setSelectionPath(getClosestPathForLocation(e.getX(), e.getY()));

                        Loadout<?> clickedLoadout = (Loadout<?>) clicked;
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem label = new JMenuItem(clickedLoadout.getName());
                        label.setEnabled(false);
                        menu.add(label);
                        menu.add(new JMenuItem(new RenameLoadoutAction(clickedLoadout, xBar, null)));
                        menu.add(new JMenuItem(
                                new DeleteLoadoutAction(xBar, ProgramInit.lsml().getGarage(), clickedLoadout)));
                        menu.add(new JMenuItem(
                                new CloneLoadoutAction("Clone", clickedLoadout, KeyStroke.getKeyStroke("C"))));
                        menu.show(GarageTree.this, e.getX(), e.getY());
                    }
                    else if (clicked instanceof DropShipContainerNode) {
                        final Faction faction = ((DropShipContainerNode) clicked).getFaction();

                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem label = new JMenuItem(
                                new AbstractAction("New " + faction.getUiName() + " drop ship...") {

                            @Override
                            public void actionPerformed(ActionEvent aArg0) {
                                aLoadoutDesktop.openDropShip(new DropShip(faction));
                            }
                        });
                        menu.add(label);
                        menu.show(GarageTree.this, e.getX(), e.getY());
                    }
                }
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
                    Object clicked = getClickedObject(e);
                    if (clicked instanceof Chassis) {
                        Loadout<?> loadout = DefaultLoadoutFactory.instance.produceEmpty((Chassis) clicked);
                        aLoadoutDesktop.openLoadout(loadout, false);
                    }
                    else if (clicked instanceof Loadout<?>) {
                        aLoadoutDesktop.openLoadout((Loadout<?>) clicked, false);
                    }
                    else if (clicked instanceof DropShip) {
                        aLoadoutDesktop.openDropShip((DropShip) clicked);
                    }
                }
            }
        });
    }

    private Object getClickedObject(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path == null)
            return null;
        return path.getLastPathComponent();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        TreePath mouseover = getPathForLocation(event.getX(), event.getY());
        if (mouseover != null) {
            Object leaf = mouseover.getLastPathComponent();
            if (leaf instanceof ChassisStandard) {
                StringBuilder sb = new StringBuilder(100);
                ChassisStandard chassi = (ChassisStandard) leaf;
                sb.append("<html>");
                sb.append("Max Tons: ").append(chassi.getMassMax()).append(" Engine: ").append(chassi.getEngineMin())
                        .append(" - ").append(chassi.getEngineMax()).append("<br>");
                sb.append("Max Jump Jets: ").append(chassi.getJumpJetsMax()).append(" ECM: ")
                        .append(chassi.getHardPointsCount(HardPointType.ECM) > 0 ? "Yes" : "No").append("<br>");
                sb.append("Ballistics: ").append(chassi.getHardPointsCount(HardPointType.BALLISTIC)).append(" Energy: ")
                        .append(chassi.getHardPointsCount(HardPointType.ENERGY)).append(" Missile: ")
                        .append(chassi.getHardPointsCount(HardPointType.MISSILE)).append(" AMS: ")
                        .append(chassi.getHardPointsCount(HardPointType.AMS)).append("<br>");
                sb.append("</html>");
                return sb.toString();
            }
        }
        return null;
    }
}
