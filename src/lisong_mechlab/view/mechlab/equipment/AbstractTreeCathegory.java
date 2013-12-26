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

import javax.swing.tree.TreePath;

abstract class AbstractTreeCathegory extends TreeCathegory{
   private final String          name;
   private final TreePath        path;
   private final GarageTreeModel model;

   public AbstractTreeCathegory(String aName, GarageTreeModel aModel){
      name = aName;
      path = new TreePath(this);
      model = aModel;
   }

   public AbstractTreeCathegory(String aName, TreeCathegory aParent, GarageTreeModel aModel){
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
   public GarageTreeModel getModel(){
      return model;
   }
}
