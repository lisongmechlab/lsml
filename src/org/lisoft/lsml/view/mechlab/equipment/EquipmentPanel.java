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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ComponentMessage;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.mechlab.ItemInfoPanel;
import org.lisoft.lsml.view.mechlab.ItemLabel;
import org.lisoft.lsml.view.mechlab.LoadoutDesktop;
import org.lisoft.lsml.view.mechlab.LoadoutFrame;
import org.lisoft.lsml.view.render.ModifiedFlowLayout;
import org.lisoft.lsml.view.render.ScrollablePanel;

/**
 * This class renders the equipment panel that contains all the equippable items on the selected loadout.
 * 
 * @author Emily Björk
 */
public class EquipmentPanel extends JPanel implements Message.Recipient, InternalFrameListener {
    private static final long   serialVersionUID = -8126726006921797207L;
    private final ItemInfoPanel infoPanel        = new ItemInfoPanel();
    private final JPanel        energyItems      = new JPanel(new ModifiedFlowLayout());
    private final JPanel        ballisticItems   = new JPanel(new ModifiedFlowLayout());
    private final JPanel        missileItems     = new JPanel(new ModifiedFlowLayout());
    private LoadoutBase<?>      currentLoadout;
    private JPanel              miscItems        = new JPanel(new ModifiedFlowLayout());
    private JPanel              engineItems      = new JPanel(new ModifiedFlowLayout());
    private JPanel              engineXlItems    = new JPanel(new ModifiedFlowLayout());

    public EquipmentPanel(LoadoutDesktop aDesktop, MessageXBar aXBar) {
        aXBar.attach(this);
        aDesktop.addInternalFrameListener(this);

        setLayout(new BorderLayout());
        List<Item> items = ItemDB.lookup(Item.class);
        Collections.sort(items);

        energyItems.setBorder(BorderFactory.createTitledBorder("Energy"));
        ballisticItems.setBorder(BorderFactory.createTitledBorder("Ballistic"));
        missileItems.setBorder(BorderFactory.createTitledBorder("Missile"));
        miscItems.setBorder(BorderFactory.createTitledBorder("Misc"));
        engineItems.setBorder(BorderFactory.createTitledBorder("Engine - STD"));
        engineXlItems.setBorder(BorderFactory.createTitledBorder("Engine - XL"));
        for (Item item : items) {
            if (item instanceof Internal)
                continue;

            ItemLabel itemLabel = new ItemLabel(item, this, infoPanel, aXBar);
            if (item instanceof Ammunition) {
                Ammunition ammunition = (Ammunition) item;
                switch (ammunition.getWeaponHardpointType()) {
                    case BALLISTIC:
                        ballisticItems.add(itemLabel);
                        break;
                    case ENERGY:
                        energyItems.add(itemLabel);
                        break;
                    case MISSILE:
                        missileItems.add(itemLabel);
                        break;
                    case AMS: // Fall-through
                    case ECM: // Fall-through
                    case NONE: // Fall-through
                    default:
                        miscItems.add(itemLabel);
                        break;
                }
            }
            else if (item instanceof EnergyWeapon) {
                energyItems.add(itemLabel);
            }
            else if (item instanceof BallisticWeapon) {
                ballisticItems.add(itemLabel);
            }
            else if (item instanceof MissileWeapon) {
                missileItems.add(itemLabel);
            }
            else if (item instanceof Engine) {
                if (((Engine) item).getType() == EngineType.XL) {
                    engineXlItems.add(itemLabel);
                }
                else {
                    engineItems.add(itemLabel);
                }
            }
            else {
                miscItems.add(itemLabel);
            }
        }

        JPanel itemFlowPanel = new ScrollablePanel();
        itemFlowPanel.setLayout(new BoxLayout(itemFlowPanel, BoxLayout.PAGE_AXIS));
        itemFlowPanel.add(energyItems);
        itemFlowPanel.add(ballisticItems);
        itemFlowPanel.add(missileItems);
        itemFlowPanel.add(miscItems);
        itemFlowPanel.add(engineItems);
        itemFlowPanel.add(engineXlItems);
        JScrollPane itemFlowScrollPanel = new JScrollPane(itemFlowPanel);
        itemFlowScrollPanel.setAlignmentX(LEFT_ALIGNMENT);

        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(itemFlowScrollPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        changeLoadout(null, true);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent aArg0) {/* NO-OP */
        if (currentLoadout == null) // Was this the last open loadout?
            changeLoadout(null, true);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent aArg0) {/* NO-OP */
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent aArg0) {/* NO-OP */
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent aArg0) {
        LoadoutFrame frame = (LoadoutFrame) aArg0.getInternalFrame();
        changeLoadout(frame.getLoadout(), true);
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent aE) {
        currentLoadout = null;
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent aE) {
        if (currentLoadout == null) // Was this the last visible loadout?
            changeLoadout(null, true);
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent aArg0) {
        LoadoutFrame frame = (LoadoutFrame) aArg0.getInternalFrame();
        changeLoadout(frame.getLoadout(), true);
    }

    private void changeLoadout(LoadoutBase<?> aLoadout, boolean aShouldUpdateVisibility) {

        if (aLoadout != null) {
            energyItems.setVisible(aLoadout.getHardpointsCount(HardPointType.ENERGY) > 0);
            missileItems.setVisible(aLoadout.getHardpointsCount(HardPointType.MISSILE) > 0);
            ballisticItems.setVisible(aLoadout.getHardpointsCount(HardPointType.BALLISTIC) > 0);

            if (aLoadout instanceof LoadoutOmniMech) {
                engineItems.setVisible(false);
                engineXlItems.setVisible(false);
            }
        }
        else {
            energyItems.setVisible(true);
            missileItems.setVisible(true);
            ballisticItems.setVisible(true);
            engineItems.setVisible(true);
            engineXlItems.setVisible(true);
        }

        updateCategory(ballisticItems, aLoadout, aShouldUpdateVisibility);
        updateCategory(energyItems, aLoadout, aShouldUpdateVisibility);
        updateCategory(missileItems, aLoadout, aShouldUpdateVisibility);
        updateCategory(miscItems, aLoadout, aShouldUpdateVisibility);
        updateCategory(engineXlItems, aLoadout, aShouldUpdateVisibility);
        updateCategory(engineItems, aLoadout, aShouldUpdateVisibility);

        currentLoadout = aLoadout;
    }

    private void updateCategory(JPanel aPanel, LoadoutBase<?> aLoadout, boolean aShouldUpdateVisibility) {

        if (aPanel.isVisible()) {
            for (Component c : aPanel.getComponents()) {
                ItemLabel l = (ItemLabel) c;
                if (aShouldUpdateVisibility)
                    l.updateVisibility(aLoadout);
                l.updateDisplay(aLoadout);
            }
        }
    }
    
    @Override
    public void receive(Message aMsg) {
        if (currentLoadout == null || aMsg.isForMe(currentLoadout)) {
            boolean shouldUpdateVisibility = (aMsg == null ? true : aMsg instanceof UpgradesMessage);
            if (!shouldUpdateVisibility) {
                if (aMsg instanceof ComponentMessage) {
                    ComponentMessage msg = (ComponentMessage) aMsg;
                    if (msg.isItemsChanged()) {
                        shouldUpdateVisibility = true;
                    }
                }
            }
            
            changeLoadout(currentLoadout, shouldUpdateVisibility);

            //revalidate();
//            repaint();
        }
    }

    public LoadoutBase<?> getCurrentLoadout() {
        return currentLoadout;
    }
}
