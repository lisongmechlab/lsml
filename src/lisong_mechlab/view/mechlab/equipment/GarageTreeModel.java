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

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.util.MessageXBar;

public class GarageTreeModel implements TreeModel, InternalFrameListener{
   private final List<TreeModelListener>                     listeners = new ArrayList<TreeModelListener>();
   private final DefaultTreeCathegory<AbstractTreeCathegory> root;

   public GarageTreeModel(MessageXBar xBar){
      root = new DefaultTreeCathegory<AbstractTreeCathegory>("MechLab", this);

      DefaultTreeCathegory<AbstractTreeCathegory> chassii = new DefaultTreeCathegory<AbstractTreeCathegory>("Chassii", root, this);
      GarageCathegory garage = new GarageCathegory("Garage", root, this, xBar);

      root.addChild(chassii);
      root.addChild(garage);

      // Chassii
      for(ChassiClass chassiClass : ChassiClass.values()){
         DefaultTreeCathegory<Chassi> chassiiSub = new DefaultTreeCathegory<Chassi>(chassiClass.toString(), chassii, this);
         for(Chassi chassi : ChassiDB.lookup(chassiClass)){
            chassiiSub.addChild(chassi);
         }
         chassiiSub.sort(new Comparator<Chassi>(){
            @Override
            public int compare(Chassi aO1, Chassi aO2){
               return aO1.getNameShort().compareTo(aO2.getNameShort());
            }
         });
         chassii.addChild(chassiiSub);
      }
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
