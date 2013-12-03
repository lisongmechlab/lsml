/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.view.equipment;

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
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.util.MessageXBar;

public class EquipmentTreeModel implements TreeModel, InternalFrameListener{
   private final List<TreeModelListener>                     listeners = new ArrayList<TreeModelListener>();
   private final DefaultTreeCathegory<AbstractTreeCathegory> root;

   public EquipmentTreeModel(MessageXBar xBar){
      root = new DefaultTreeCathegory<AbstractTreeCathegory>("MechLab", this);

      List<Item> items = ItemDB.lookup(Item.class);

      DefaultTreeCathegory<AbstractTreeCathegory> chassii = new DefaultTreeCathegory<AbstractTreeCathegory>("Chassii", root, this);
      GarageCathegory garage = new GarageCathegory("Garage", root, this, xBar);

      // Process the items list
      List<Item> weapons = new ArrayList<>();
      List<Item> energy = new ArrayList<>();
      List<Item> ballistic = new ArrayList<>();
      List<Item> missile = new ArrayList<>();
      List<Item> engineStd = new ArrayList<>();
      List<Item> engineXl = new ArrayList<>();
      List<Item> misc = new ArrayList<>();
      for(Item item : items){
         if( item instanceof Ammunition ){
            continue;
         }
         else if( item instanceof EnergyWeapon )
            energy.add(item);
         else if( item instanceof BallisticWeapon ){
            Ammunition ammo = ((AmmoWeapon)item).getAmmoType(null);
            ballistic.add(item);
            ballistic.add(ammo);
         }
         else if( item instanceof MissileWeapon ){
            missile.add(item);
            Upgrades upgrades = new Upgrades(null);
            upgrades.setArtemis(true);
            missile.add(((AmmoWeapon)item).getAmmoType(upgrades));
            upgrades.setArtemis(false);
            missile.add(((AmmoWeapon)item).getAmmoType(upgrades));
         }
         else if( item instanceof Engine ){
            Engine engine = (Engine)item;
            if( engine.getType() == EngineType.STD )
               engineStd.add(engine);
            else
               engineXl.add(engine);
         }
         else{
            if( item instanceof AmmoWeapon )
               misc.add(((AmmoWeapon)item).getAmmoType(null));
            misc.add(item);
         }
      }

      weapons.addAll(energy);
      weapons.addAll(ballistic);
      weapons.addAll(missile);

      root.addChild(chassii);
      root.addChild(garage);
      root.addChild(new EquippableItemsCathegory(misc, "Misc", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(weapons, "Weapons", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(engineStd, "Engine - STD", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(engineXl, "Engine - XL", root, this, xBar));

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
