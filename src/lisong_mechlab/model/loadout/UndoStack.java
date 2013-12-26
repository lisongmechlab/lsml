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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lisong_mechlab.model.loadout.MechGarage.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;

/**
 * This class models an undo stack. You can push actions onto this stack that can be undone. It will automatically reset
 * the stack if a new garage is loaded.
 * 
 * @author Emily Björk
 */
public class UndoStack implements MessageXBar.Reader{
   private final List<UndoAction> actions = new LinkedList<>();
   private final int              depth;

   /**
    * Creates a new {@link UndoStack} that listens on the given {@link MessageXBar} for garage resets and has the given
    * undo depth.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to listen to.
    * @param anUndoDepth
    *           The number of undo levels allowed.
    */
   public UndoStack(MessageXBar anXBar, int anUndoDepth){
      anXBar.attach(this);
      depth = anUndoDepth;
   }

   /**
    * Adds a new action that can be undone to the stack.
    * 
    * @param anAction
    *           The action that is to be added.
    */
   public void pushAction(UndoAction anAction){
      actions.add(0, anAction);
      while( actions.size() > depth ){
         actions.remove(actions.size() - 1);
      }
   }

   /**
    * Get the latest action that affected the given loadout.
    * 
    * @param aLoadout
    *           The loadout that must have been affected by the action that will be undone.
    * @return The {@link UndoAction} sought for or <code>null</code> if no {@link UndoAction} that matches the criteria
    *         exists.
    */
   public UndoAction latestLoadout(Loadout aLoadout){
      for(UndoAction action : actions){
         if( action.affects(aLoadout) ){
            return action;
         }
      }
      return null;
   }

   /**
    * Get the latest action without conditions.
    * 
    * @return The {@link UndoAction} sought for or <code>null</code> if no {@link UndoAction} that matches the criteria
    *         exists.
    */
   public UndoAction latestGlobal(){
      if( actions.isEmpty() )
         return null;
      return actions.get(0);
   }

   /**
    * Get the latest action that affected the contents of the garage (add/remove mech).
    * 
    * @return The {@link UndoAction} sought for or <code>null</code> if no {@link UndoAction} that matches the criteria
    *         exists.
    */
   public UndoAction latestGarage(){
      for(UndoAction action : actions){
         if( action instanceof MechGarage.GarageUndoAction ){
            return action;
         }
      }
      return null;
   }

   /**
    * This will undo the given {@link UndoAction} and remove it from the undo stack.
    * 
    * @param anUndoAction
    *           The action to undo.
    */
   public void undoAction(UndoAction anUndoAction){
      if( actions.remove(anUndoAction) ){
         anUndoAction.undo();
      }
   }

   /*
    * TODO: I'm not really happy about this as it doesn't follow the same pattern as clearLoadout. Leaving it for now
    * though.
    */
   @Override
   public void receive(Message aMsg){
      if( aMsg instanceof MechGarage.Message ){
         MechGarage.Message msg = (MechGarage.Message)aMsg;
         if( msg.type == Type.NewGarage )
            actions.clear();
      }
   }

   /**
    * Removes all {@link UndoAction}s pushed to this stack that affect the given loadout.
    * 
    * @param aLoadout
    *           The loadout to clear all related actions for.
    */
   public void clearLoadout(Loadout aLoadout){
      Iterator<UndoAction> it = actions.iterator();
      while( it.hasNext() ){
         UndoAction action = it.next();
         if( action.affects(aLoadout) )
            it.remove();
      }
   }
}
