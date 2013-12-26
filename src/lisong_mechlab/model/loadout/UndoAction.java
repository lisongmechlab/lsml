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
package lisong_mechlab.model.loadout;

/**
 * The {@link UndoAction} class represents an action that can be undone. Undoing the action will restore the state of
 * affected object to that before the {@link UndoAction} was created.
 * 
 * @author Emily Björk
 */
public interface UndoAction{

   /**
    * @return A {@link String} containing a (short) human readable description of this action.
    */
   public String describe();

   /**
    * Will undo this action.
    */
   public void undo();

   /**
    * Determines if this action would affect the given {@link Loadout} if undone.
    * 
    * @param aLoadout
    *           The loadout to check if this action affects.
    * @return <code>true</code> if this action affects the given {@link Loadout}.
    */
   public boolean affects(Loadout aLoadout);
}
