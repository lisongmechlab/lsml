package lisong_mechlab.view.equipment;

import javax.swing.tree.TreePath;

abstract class AbstractTreeCathegory extends TreeCathegory{
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
   public TreePath getPath(){
      return path;
   }

   @Override
   public EquipmentTreeModel getModel(){
      return model;
   }
}