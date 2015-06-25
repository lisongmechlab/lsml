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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.lisoft.lsml.command.OpAddModule;
import org.lisoft.lsml.command.OpRemoveModule;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.PilotModuleDB;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.OperationStack;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.ModuleTransferHandler;
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
    private final OperationStack stack;
    private final ModuleSlot     moduleSlot;

    public PilotModuleList(MessageXBar aXBar, OperationStack aOperationStack, LoadoutBase<?> aLoadout,
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
                    takeCurrent();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent aE) {
                if (aE.getKeyCode() == KeyEvent.VK_DELETE) {
                    takeCurrent();
                }
            }
        });
    }

    public LoadoutBase<?> getLoadout() {
        return loadout;
    }

    public void putElement(PilotModule aModule) {
        stack.pushAndApply(new OpAddModule(xBar, loadout, aModule));
    }

    public PilotModule takeCurrent() {
        String sel = getSelectedValue();
        if (sel.equals(PilotModuleModel.EMPTY))
            return null;

        PilotModule module = PilotModuleDB.lookup(getSelectedValue());
        if (module != null) {
            stack.pushAndApply(new OpRemoveModule(xBar, loadout, module));
        }
        return module;
    }
}