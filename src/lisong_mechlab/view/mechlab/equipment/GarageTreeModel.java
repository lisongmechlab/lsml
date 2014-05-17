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
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.view.preferences.Preferences;

public class GarageTreeModel implements TreeModel, InternalFrameListener{
   private final List<TreeModelListener>                     listeners = new ArrayList<TreeModelListener>();
   private final DefaultTreeCathegory<AbstractTreeCathegory> root;
   private final Preferences                                 preferences;

   public GarageTreeModel(MessageXBar aXBar, JTextField aFilterBar, GarageTree aGarageTree, Preferences aPreferences){
      root = new DefaultTreeCathegory<AbstractTreeCathegory>("MechLab", this);
      preferences = aPreferences;

      DefaultTreeCathegory<AbstractTreeCathegory> chassisStandard = new DefaultTreeCathegory<AbstractTreeCathegory>("BattleMechs", root, this);
      DefaultTreeCathegory<AbstractTreeCathegory> chassisOmniMech = new DefaultTreeCathegory<AbstractTreeCathegory>("OmniMechs", root, this);
      for(final ChassisClass chassiClass : ChassisClass.values()){
         DefaultTreeCathegory<ChassisStandard> classStandard = new FilterTreeCathegory<ChassisStandard>(aXBar, chassiClass.toString(),
                                                                                                        chassisStandard, this, aFilterBar,
                                                                                                        aGarageTree){
            @Override
            protected boolean filter(ChassisStandard c){
               if( preferences.uiPreferences.getHideSpecialMechs() && c.getVariantType().isVariation() )
                  return false;
               return c.getName().toLowerCase().contains(getFilterString());
            }
         };
         DefaultTreeCathegory<ChassisOmniMech> classOmniMech = new FilterTreeCathegory<ChassisOmniMech>(aXBar, chassiClass.toString(),
                                                                                                        chassisOmniMech, this, aFilterBar,
                                                                                                        aGarageTree){
            @Override
            protected boolean filter(ChassisOmniMech c){
               if( preferences.uiPreferences.getHideSpecialMechs() && c.getVariantType().isVariation() )
                  return false;
               return c.getName().toLowerCase().contains(getFilterString());
            }
         };

         for(ChassisBase chassi : ChassisDB.lookup(chassiClass)){
            if( chassi instanceof ChassisStandard )
               classStandard.addChild((ChassisStandard)chassi);
            else if( chassi instanceof ChassisOmniMech )
               classOmniMech.addChild((ChassisOmniMech)chassi);
            else
               throw new RuntimeException("Unexpected chassis type when generating garage tree.");
         }
         classStandard.sort(new Comparator<ChassisStandard>(){
            @Override
            public int compare(ChassisStandard aO1, ChassisStandard aO2){
               return aO1.getNameShort().compareTo(aO2.getNameShort());
            }
         });
         classOmniMech.sort(new Comparator<ChassisOmniMech>(){
            @Override
            public int compare(ChassisOmniMech aO1, ChassisOmniMech aO2){
               return aO1.getNameShort().compareTo(aO2.getNameShort());
            }
         });
         chassisStandard.addChild(classStandard);
         chassisOmniMech.addChild(classOmniMech);
      }
      root.addChild(chassisStandard);
      root.addChild(chassisOmniMech);

      DefaultTreeCathegory<GarageCathegory> garage = new DefaultTreeCathegory<>("Garage", root, this);
      for(ChassisClass chassiClass : ChassisClass.values()){
         GarageCathegory clazz = new GarageCathegory(chassiClass.toString(), garage, this, aXBar, chassiClass, aFilterBar, aGarageTree);
         garage.addChild(clazz);
      }

      root.addChild(garage);
   }

   public void notifyTreeChange(TreeModelEvent e){
      for(TreeModelListener listener : listeners){
         listener.treeStructureChanged(e);
      }
   }

   @Override
   public void addTreeModelListener(TreeModelListener aListener){
      listeners.add(aListener);
   }

   @Override
   public Object getChild(Object aParent, int anIndex){
      return ((TreeCathegory)aParent).getChild(anIndex);
   }

   @Override
   public int getChildCount(Object aParent){
      return ((TreeCathegory)aParent).getChildCount();
   }

   @Override
   public int getIndexOfChild(Object aParent, Object aChild){
      return ((TreeCathegory)aParent).getIndex(aChild);
   }

   @Override
   public Object getRoot(){
      return root;
   }

   @Override
   public boolean isLeaf(Object aNode){
      return !(aNode instanceof TreeCathegory);
   }

   @Override
   public void removeTreeModelListener(TreeModelListener aListener){
      listeners.remove(aListener);
   }

   @Override
   public void valueForPathChanged(TreePath aPath, Object aNewValue){
      // No-Op
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aE){
      root.internalFrameActivated(aE);
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aE){
      root.internalFrameClosed(aE);
   }

   @Override
   public void internalFrameClosing(InternalFrameEvent aE){
      root.internalFrameClosing(aE);
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
      root.internalFrameDeactivated(aE);
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aE){
      root.internalFrameDeiconified(aE);
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
      root.internalFrameIconified(aE);
   }

   @Override
   public void internalFrameOpened(InternalFrameEvent aE){
      root.internalFrameOpened(aE);
   }
}
