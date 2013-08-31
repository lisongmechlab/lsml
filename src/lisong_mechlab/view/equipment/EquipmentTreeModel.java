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

import lisong_mechlab.model.MessageXBar;
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
import lisong_mechlab.view.LSML;

public class EquipmentTreeModel implements TreeModel, InternalFrameListener{
   private final List<TreeModelListener>                     listeners = new ArrayList<TreeModelListener>();
   private final DefaultTreeCathegory<AbstractTreeCathegory> root;

   public EquipmentTreeModel(LSML aLSML, MessageXBar xBar) throws Exception{
      root = new DefaultTreeCathegory<AbstractTreeCathegory>("MechLab", this);

      List<Item> items = ItemDB.lookup(Item.class);

      DefaultTreeCathegory<AbstractTreeCathegory> chassii = new DefaultTreeCathegory<AbstractTreeCathegory>("Chassii", root, this);
      GarageCathegory garage = new GarageCathegory("Garage", root, this, aLSML.getXBar());

      // Process the items list
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
            energy.add((EnergyWeapon)item);
         else if( item instanceof BallisticWeapon ){
            Ammunition ammo = ((AmmoWeapon)item).getAmmoType();
            ballistic.add(item);
            ballistic.add(ammo);
         }
         else if( item instanceof MissileWeapon ){
            Ammunition ammo = ((AmmoWeapon)item).getAmmoType();
            missile.add(item);
            missile.add(ammo);
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
               misc.add(((AmmoWeapon)item).getAmmoType());
            misc.add(item);
         }
      }

      root.addChild(chassii);
      root.addChild(garage);
      root.addChild(new EquippableItemsCathegory(energy, "Energy", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(ballistic, "Ballistic", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(missile, "Missile", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(engineStd, "Engine - STD", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(engineXl, "Engine - XL", root, this, xBar));
      root.addChild(new EquippableItemsCathegory(misc, "Misc", root, this, xBar));

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
