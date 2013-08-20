package lisong_mechlab.view.equipment;

import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.TreePath;

abstract class AbstractTreeCathegory implements TreeCathegory{
   private final String             name;
   private final TreePath           path;
   private final EquipmentTreeModel model;
   
   public AbstractTreeCathegory(String aName, EquipmentTreeModel aModel){
      name = aName;
      path = new TreePath(this);
      model = aModel;
   }
   
   public AbstractTreeCathegory(String aName, TreeCathegory aParent, EquipmentTreeModel aModel){
      name = aName;
      path = aParent.getPath().pathByAddingChild(this);
      model = aModel;
   }
   
   @Override
   public String toString(){
      return name;
   }  

   @Override
   public void internalFrameActivated(InternalFrameEvent aE){
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aE){
   }

   @Override
   public void internalFrameClosing(InternalFrameEvent aE){
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aE){
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
   }

   @Override
   public void internalFrameOpened(InternalFrameEvent aE){
   }

   @Override
   public TreePath getPath(){
      return path;
   }

   @Override
   public EquipmentTreeModel getModel(){
      return model;
   }
}