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
package org.lisoft.lsml.view.mechlab.loadoutframe;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdRemoveModule;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.ModuleTransferHandler;
import org.lisoft.lsml.view.models.PilotModuleModel;
import org.lisoft.lsml.view.render.ItemRenderer;

/**
 * This class implements a JList for {@link PilotModule}s equipped on a {@link LoadoutBase}.
 * <p>
 * TODO: Make it adapt to changes in pilot modules from omnipods when they add pilot modules as quirks.
 * 
 * @author Emily Björk
 */
public class PilotModuleList extends JList<String> {
    private static final long    serialVersionUID = -3812414074800032146L;
    private final MessageXBar    xBar;
    private final LoadoutBase<?> loadout;
    private final CommandStack   stack;
    private final ModuleSlot     moduleSlot;

    public PilotModuleList(MessageXBar aXBar, CommandStack aOperationStack, LoadoutBase<?> aLoadout,
            ModuleSlot aModuleSlot) {
        super(new PilotModuleModel(aLoadout, aXBar, aModuleSlot));
        xBar = aXBar;
        stack = aOperationStack;
        loadout = aLoadout;
        moduleSlot = aModuleSlot;
        setVisible(true);
        setFocusable(false);
        setVisibleRowCount(aLoadout.getModulesMax(aModuleSlot));
        setFixedCellWidth(ItemRenderer.getItemWidth());
        setFixedCellHeight(ItemRenderer.getItemHeight());
        setDragEnabled(true);
        setTransferHandler(new ModuleTransferHandler());
        setCellRenderer(new ListCellRenderer<String>() {
            private final JLabel label = new JLabel();

            @Override
            public Component getListCellRendererComponent(JList<? extends String> aList, String aValue, int aIndex,
                    boolean aIsSelected, boolean aCellHasFocus) {
                label.setText(aValue);

                if (moduleSlot == ModuleSlot.HYBRID) {
                    label.setBackground(new Color(0xb8aa81));
                    label.setOpaque(true);
                }
                return label;
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    try {
                        takeCurrent();
                    }
                    catch (Exception e1) {
                        JOptionPane.showMessageDialog(null, e);
                    }
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent aE) {
                if (aE.getKeyCode() == KeyEvent.VK_DELETE) {
                    try {
                        takeCurrent();
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e);
                    }
                }
            }
        });
    }

    public LoadoutBase<?> getLoadout() {
        return loadout;
    }

    public void putElement(PilotModule aModule) throws Exception {
        stack.pushAndApply(new CmdAddModule(xBar, loadout, aModule));
    }

    public PilotModule takeCurrent() throws Exception {
        String sel = getSelectedValue();
        if (sel.equals(PilotModuleModel.EMPTY))
            return null;

        PilotModule module = PilotModuleDB.lookup(getSelectedValue());
        if (module != null) {
            stack.pushAndApply(new CmdRemoveModule(xBar, loadout, module));
        }
        return module;
    }
}
