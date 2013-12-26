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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

class DefaultTreeCathegory<T> extends AbstractTreeCathegory{
   final protected List<T> children = new ArrayList<>();

   public DefaultTreeCathegory(String aName, GarageTreeModel aModel){
      super(aName, aModel);
   }

   public DefaultTreeCathegory(String aName, TreeCathegory aParent, GarageTreeModel aModel){
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
