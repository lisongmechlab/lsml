package lisong_mechlab.view.equipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

class DefaultTreeCathegory <T> extends AbstractTreeCathegory{
   final protected List<T> children = new ArrayList<>();

   public DefaultTreeCathegory(String aName, EquipmentTreeModel aModel){
      super(aName, aModel);
   }

   public DefaultTreeCathegory(String aName, TreeCathegory aParent, EquipmentTreeModel aModel){
      super(aName, aParent, aModel);
   }

   public void addChild(T anObject){
      children.add(anObject);
   }

   public void sort(Comparator<T> comparator){
      Collections.sort(children, comparator);
   }

   @Override
   public int getChildCount(){
      return children.size();
   }

   @Override
   public int getIndex(Object aChild){
      return children.indexOf(aChild);
   }

   @Override
   public Object getChild(int anIndex){
      return children.get(anIndex);
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameActivated(aE);
         }
      }
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameClosed(aE);
         }
      }
   }

   @Override
   public void internalFrameClosing(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameClosing(aE);
         }
      }
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameDeactivated(aE);
         }
      }
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameDeiconified(aE);
         }
      }
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameIconified(aE);
         }
      }
   }

   @Override
   public void internalFrameOpened(InternalFrameEvent aE){
      for(Object object : children){
         if( object instanceof InternalFrameListener ){
            ((InternalFrameListener)object).internalFrameOpened(aE);
         }
      }
   }
}
