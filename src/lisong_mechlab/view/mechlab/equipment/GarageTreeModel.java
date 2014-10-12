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
package lisong_mechlab.view.mechlab.equipment;

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

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.preferences.Preferences;

public class GarageTreeModel implements TreeModel, InternalFrameListener {
    private final List<TreeModelListener>                     listeners = new ArrayList<TreeModelListener>();
    private final DefaultTreeCathegory<AbstractTreeCathegory> root;
    private final Preferences                                 preferences;

    class ChassisFilterTreeCathegory extends FilterTreeCathegory<ChassisBase> {
        public ChassisFilterTreeCathegory(MessageXBar aXBar, Object chassiClass, TreeCathegory chassisIS,
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
        root = new DefaultTreeCathegory<AbstractTreeCathegory>("MechLab", this);
        preferences = aPreferences;

        DefaultTreeCathegory<AbstractTreeCathegory> chassisIS = new DefaultTreeCathegory<AbstractTreeCathegory>(
                "Inner Sphere", root, this);
        DefaultTreeCathegory<AbstractTreeCathegory> chassisClan = new DefaultTreeCathegory<AbstractTreeCathegory>(
                "Clan", root, this);
        for (final ChassisClass chassiClass : ChassisClass.values()) {
            DefaultTreeCathegory<ChassisBase> classIS = new ChassisFilterTreeCathegory(aXBar, chassiClass.toString(),
                    chassisIS, aFilterBar, aGarageTree);
            DefaultTreeCathegory<ChassisBase> classClan = new ChassisFilterTreeCathegory(aXBar, chassiClass.toString(),
                    chassisClan, aFilterBar, aGarageTree);

            for (ChassisBase chassi : ChassisDB.lookup(chassiClass)) {
                if (chassi.getFaction() == Faction.InnerSphere)
                    classIS.addChild(chassi);
                else if (chassi.getFaction() == Faction.Clan)
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

        DefaultTreeCathegory<GarageCathegory> garageIs = new DefaultTreeCathegory<>("Garage - IS", root, this);
        for (ChassisClass chassiClass : ChassisClass.values()) {
            GarageCathegory clazz = new GarageCathegory(chassiClass.toString(), garageIs, this, aXBar, chassiClass,
                    aFilterBar, aGarageTree, Faction.InnerSphere);
            garageIs.addChild(clazz);
        }

        DefaultTreeCathegory<GarageCathegory> garageClan = new DefaultTreeCathegory<>("Garage - Clan", root, this);
        for (ChassisClass chassiClass : ChassisClass.values()) {
            GarageCathegory clazz = new GarageCathegory(chassiClass.toString(), garageClan, this, aXBar, chassiClass,
                    aFilterBar, aGarageTree, Faction.Clan);
            garageClan.addChild(clazz);
        }

        root.addChild(garageIs);
        root.addChild(garageClan);
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
        return ((TreeCathegory) aParent).getChild(anIndex);
    }

    @Override
    public int getChildCount(Object aParent) {
        return ((TreeCathegory) aParent).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object aParent, Object aChild) {
        return ((TreeCathegory) aParent).getIndex(aChild);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object aNode) {
        return !(aNode instanceof TreeCathegory);
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
