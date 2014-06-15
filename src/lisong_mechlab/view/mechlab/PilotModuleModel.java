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
package lisong_mechlab.view.mechlab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.item.PilotModuleDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.model.loadout.LoadoutMessage.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;

/**
 * This class implements a {@link ComboBoxModel} for selecting {@link PilotModule}s for a {@link LoadoutBase}.
 * 
 * @author Emily Björk
 */
public class PilotModuleModel implements ListModel<PilotModule>, MessageXBar.Reader{
   private final LoadoutBase<?>        loadout;
   private final Set<ListDataListener> listeners = new HashSet<>();
   private final List<PilotModule>     modules   = new ArrayList<>();

   public PilotModuleModel(LoadoutBase<?> aLoadout, MessageXBar aXBar){
      loadout = aLoadout;
      aXBar.attach(this);
      updateModules();
   }

   private void fireListeners(){
      ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, modules.size());
      for(ListDataListener listener : listeners){

         listener.contentsChanged(e);
      }
   }

   private void updateModules(){
      List<PilotModule> mods = PilotModuleDB.lookup(PilotModule.class);
      modules.clear();
      for(PilotModule module : mods){
         if( loadout.canAddModule(module) ){
            modules.add(module);
         }
      }

      fireListeners();
   }

   @Override
   public int getSize(){
      return loadout.getModulesMax();
   }

   @Override
   public PilotModule getElementAt(int aIndex){
      if( aIndex >= loadout.getModules().size() )
         return null;
      return loadout.getModules().get(aIndex);
   }

   @Override
   public void addListDataListener(ListDataListener aL){
      listeners.add(aL);
   }

   @Override
   public void removeListDataListener(ListDataListener aL){
      listeners.remove(aL);
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) && aMsg instanceof LoadoutMessage ){
         LoadoutMessage msg = (LoadoutMessage)aMsg;
         if( msg.type == Type.MODULES_CHANGED ){
            updateModules();
         }
      }
   }
}
