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
package org.lisoft.lsml.view.mechlab.garagetree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.view.preferences.Preferences;

public class GarageTreeModel implements TreeModel, InternalFrameListener {
    private final List<TreeModelListener>           listeners = new ArrayList<TreeModelListener>();
    private final DefaultTreeNode<AbstractTreeNode> root;
    private final Preferences                       preferences;

    class ChassisFilterTreeCathegory extends FilterTreeNode<ChassisBase> {
        public ChassisFilterTreeCathegory(MessageXBar aXBar, Object chassiClass, TreeNode chassisIS,
                JTextField aFilterBar, GarageTree aGarageTree) {
            super(aXBar, chassiClass.toString(), chassisIS, GarageTreeModel.this, aFilterBar, aGarageTree);
        }

        @Override
        protected boolean filter(ChassisBase c) {
            if (preferences.uiPreferences.getHideSpecialMechs() && c.getVariantType().isVariation())
                return false;
            return c.getName().toLowerCase().contains(getFilterString());
        }
    }

    class ChassisByName implements Comparator<ChassisBase> {
        @Override
        public int compare(ChassisBase aO1, ChassisBase aO2) {
            return aO1.getNameShort().compareTo(aO2.getNameShort());
        }
    }

    public GarageTreeModel(MessageXBar aXBar, JTextField aFilterBar, GarageTree aGarageTree, Preferences aPreferences) {
        root = new DefaultTreeNode<AbstractTreeNode>("MechLab", this);
        preferences = aPreferences;

        addChassisFolders(aXBar, aFilterBar, aGarageTree);
        addDropShipFolders(aXBar, aFilterBar, aGarageTree);
        addGarageLoadoutFolders(aXBar, aFilterBar, aGarageTree);
    }

    private void addDropShipFolders(MessageXBar aXBar, JTextField aFilterBar, GarageTree aGarageTree) {

        DropShipContainerNode is = new DropShipContainerNode("Drop Ships - IS", root, this, aXBar, aFilterBar,
                aGarageTree, Faction.INNERSPHERE);

        DropShipContainerNode clan = new DropShipContainerNode("Drop Ships - Clan", root, this, aXBar, aFilterBar,
                aGarageTree, Faction.CLAN);

        root.addChild(is);
        root.addChild(clan);
    }

    private void addGarageLoadoutFolders(MessageXBar aXBar, JTextField aFilterBar, GarageTree aGarageTree) {
        DefaultTreeNode<LoadoutContainerNode> garageIs = new DefaultTreeNode<>("Garage - IS", root, this);
        for (ChassisClass chassiClass : ChassisClass.values()) {
            if (ChassisClass.COLOSSAL == chassiClass)
                continue;
            LoadoutContainerNode clazz = new LoadoutContainerNode(chassiClass.toString(), garageIs, this, aXBar,
                    chassiClass, aFilterBar, aGarageTree, Faction.INNERSPHERE);
            garageIs.addChild(clazz);
        }

        DefaultTreeNode<LoadoutContainerNode> garageClan = new DefaultTreeNode<>("Garage - Clan", root, this);
        for (ChassisClass chassiClass : ChassisClass.values()) {
            if (ChassisClass.COLOSSAL == chassiClass)
                continue;
            LoadoutContainerNode clazz = new LoadoutContainerNode(chassiClass.toString(), garageClan, this, aXBar,
                    chassiClass, aFilterBar, aGarageTree, Faction.CLAN);
            garageClan.addChild(clazz);
        }

        root.addChild(garageIs);
        root.addChild(garageClan);
    }

    private void addChassisFolders(MessageXBar aXBar, JTextField aFilterBar, GarageTree aGarageTree) {
        DefaultTreeNode<AbstractTreeNode> chassisIS = new DefaultTreeNode<AbstractTreeNode>("Inner Sphere", root, this);
        DefaultTreeNode<AbstractTreeNode> chassisClan = new DefaultTreeNode<AbstractTreeNode>("Clan", root, this);
        for (final ChassisClass chassiClass : ChassisClass.values()) {
            if (ChassisClass.COLOSSAL == chassiClass)
                continue;

            DefaultTreeNode<ChassisBase> classIS = new ChassisFilterTreeCathegory(aXBar, chassiClass.toString(),
                    chassisIS, aFilterBar, aGarageTree);
            DefaultTreeNode<ChassisBase> classClan = new ChassisFilterTreeCathegory(aXBar, chassiClass.toString(),
                    chassisClan, aFilterBar, aGarageTree);

            for (ChassisBase chassi : ChassisDB.lookup(chassiClass)) {
                if (chassi.getFaction() == Faction.INNERSPHERE)
                    classIS.addChild(chassi);
                else if (chassi.getFaction() == Faction.CLAN)
                    classClan.addChild(chassi);
                else
                    throw new RuntimeException("Unexpected chassis faction when generating garage tree.");
            }
            classIS.sort(new ChassisByName());
            classClan.sort(new ChassisByName());
            chassisIS.addChild(classIS);
            chassisClan.addChild(classClan);
        }
        root.addChild(chassisIS);
        root.addChild(chassisClan);
    }

    public void notifyTreeChange(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(e);
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener aListener) {
        listeners.add(aListener);
    }

    @Override
    public Object getChild(Object aParent, int anIndex) {
        return ((TreeNode) aParent).getChild(anIndex);
    }

    @Override
    public int getChildCount(Object aParent) {
        return ((TreeNode) aParent).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object aParent, Object aChild) {
        return ((TreeNode) aParent).getIndex(aChild);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object aNode) {
        return !(aNode instanceof TreeNode);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener aListener) {
        listeners.remove(aListener);
    }

    @Override
    public void valueForPathChanged(TreePath aPath, Object aNewValue) {
        // No-Op
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent aE) {
        root.internalFrameActivated(aE);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent aE) {
        root.internalFrameClosed(aE);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent aE) {
        root.internalFrameClosing(aE);
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent aE) {
        root.internalFrameDeactivated(aE);
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent aE) {
        root.internalFrameDeiconified(aE);
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent aE) {
        root.internalFrameIconified(aE);
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent aE) {
        root.internalFrameOpened(aE);
    }
}
