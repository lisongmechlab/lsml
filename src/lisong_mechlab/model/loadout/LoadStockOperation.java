/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.StockLoadout;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.upgrades.SetArmorTypeOperation;
import lisong_mechlab.model.upgrades.SetGuidanceTypeOperation;
import lisong_mechlab.model.upgrades.SetHeatSinkTypeOperation;
import lisong_mechlab.model.upgrades.SetStructureTypeOperation;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation loads a 'mechs stock {@link Loadout}.
 * 
 * @author Li Song
 */
public class LoadStockOperation extends LoadoutOperation{
   public LoadStockOperation(Chassis aChassiVariation, Loadout aLoadout, MessageXBar anXBar){
      super(aLoadout, anXBar, "load stock");

      StockLoadout stockLoadout = StockLoadoutDB.lookup(aChassiVariation);

      addOp(new StripOperation(loadout, xBar));
      addOp(new SetStructureTypeOperation(xBar, loadout, stockLoadout.getStructureType()));
      addOp(new SetGuidanceTypeOperation(xBar, loadout, stockLoadout.getGuidanceType()));
      addOp(new SetArmorTypeOperation(xBar, loadout, stockLoadout.getArmorType()));
      addOp(new SetHeatSinkTypeOperation(xBar, loadout, stockLoadout.getHeatSinkType()));

      for(StockLoadout.StockComponent component : stockLoadout.getComponents()){
         Part part = component.getPart();
         LoadoutPart loadoutPart = aLoadout.getPart(part);

         if( part.isTwoSided() ){
            addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.BACK, component.getArmorBack(), true));
            addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.FRONT, component.getArmorFront(), true));
         }
         else{
            addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.ONLY, component.getArmorFront(), true));
         }

         for(Integer item : component.getItems()){
            addOp(new AddItemOperation(xBar, loadoutPart, ItemDB.lookup(item)));
         }
      }
   }
}
