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
package org.lisoft.lsml.view.mechlab.equipment;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.PilotModuleDB;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMessage;
import org.lisoft.lsml.model.loadout.LoadoutMessage.Type;
import org.lisoft.lsml.model.loadout.component.ComponentMessage;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.ModuleTransferHandler;
import org.lisoft.lsml.view.mechlab.LoadoutDesktop;
import org.lisoft.lsml.view.mechlab.LoadoutFrame;

/**
 * This {@link JPanel} shows all the available pilot modules on the equipment panel.
 * 
 * @author Emily Björk
 */
public class ModuleSeletionList extends JList<PilotModule>implements InternalFrameListener, Message.Recipient {
    private static final long                   serialVersionUID = -5162141596342256532L;
    private final DefaultListModel<PilotModule> model;
    private LoadoutBase<?>                      currentLoadout;
    private ModuleSlot                          slotType;

    public ModuleSeletionList(final LoadoutDesktop aDesktop, final MessageXBar aXBar, ModuleSlot aCathegory) {
        model = new DefaultListModel<>();
        slotType = aCathegory;
        changeLoadout(null);

        setModel(model);

        setCellRenderer(new ListCellRenderer<PilotModule>() {
            JLabel label = new JLabel();

            @Override
            public Component getListCellRendererComponent(JList<? extends PilotModule> aList, PilotModule aValue,
                    int aIndex, boolean aIsSelected, boolean aCellHasFocus) {
                if (currentLoadout != null && EquipResult.SUCCESS != currentLoadout.canAddModule(aValue)) {
                    label.setForeground(Color.RED);
                }
                else {
                    label.setForeground(Color.BLACK);
                }
                label.setText(aValue.getName());
                return label;
            }
        });
        setTransferHandler(new ModuleTransferHandler());
        setDragEnabled(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aE) {
                if (aE.getClickCount() >= 2 && currentLoadout != null) {
                    PilotModule module = getSelectedValue();
                    if (module != null && (EquipResult.SUCCESS == currentLoadout.canAddModule(module))) {
                        JInternalFrame frame = aDesktop.getSelectedFrame();
                        if (frame != null) {
                            LoadoutFrame loadoutFrame = (LoadoutFrame) frame;
                            try {
                                loadoutFrame.getOpStack().pushAndApply(new CmdAddModule(aXBar, currentLoadout, module));
                            }
                            catch (Exception e) {
                                JOptionPane.showMessageDialog(null, "Add module failed.\nError: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        });
        aXBar.attach(this);
        aDesktop.addInternalFrameListener(this);
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent aArg0) {
        LoadoutFrame frame = (LoadoutFrame) aArg0.getInternalFrame();
        changeLoadout(frame.getLoadout());
    }

    private void changeLoadout(LoadoutBase<?> aLoadout) {
        model.removeAllElements();
        List<PilotModule> modules = new ArrayList<>();

        for (PilotModule pilotModule : PilotModuleDB.lookup(slotType)) {
            if (aLoadout == null) {
                modules.add(pilotModule);
            }
            else {
                if (aLoadout.getChassis().getFaction().isCompatible(pilotModule.getFaction())) {
                    if (pilotModule instanceof WeaponModule) {
                        WeaponModule weaponModule = (WeaponModule) pilotModule;
                        boolean affectsAtLeastOne = false;
                        for (Weapon weapon : aLoadout.items(Weapon.class)) {
                            if (weaponModule.affectsWeapon(weapon)) {
                                affectsAtLeastOne = true;
                                break;
                            }
                        }

                        if (!affectsAtLeastOne) {
                            continue;
                        }
                    }

                    modules.add(pilotModule);
                }
            }
        }

        Collections.sort(modules, new Comparator<PilotModule>() {
            @Override
            public int compare(PilotModule aO1, PilotModule aO2) {
                return aO1.getName().compareTo(aO2.getName());
            }
        });

        for (PilotModule module : modules) {
            model.addElement(module);
        }

        currentLoadout = aLoadout;
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent aE) {
        changeLoadout(null);
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent aE) {
        changeLoadout(null);
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent aArg0) {
        LoadoutFrame frame = (LoadoutFrame) aArg0.getInternalFrame();
        changeLoadout(frame.getLoadout());
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage message = (LoadoutMessage) aMsg;
            if (message.type == Type.MODULES_CHANGED) {
                changeLoadout(currentLoadout);
            }
        }
        else if (aMsg instanceof ComponentMessage) {
            ComponentMessage msg = (ComponentMessage) aMsg;
            if (msg.isForMe(currentLoadout) && msg.isItemsChanged()) {
                changeLoadout(currentLoadout);
            }
        }
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent aE) {/* No-Op */
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent aE) {/* No-Op */
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent aE) {/* No-Op */
    }

}
