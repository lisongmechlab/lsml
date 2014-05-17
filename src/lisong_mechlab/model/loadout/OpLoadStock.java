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
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.util.MessageXBar;

/**
 * This operation loads a 'mechs stock {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpLoadStock extends OpLoadoutBase{
   public OpLoadStock(ChassisBase aChassiVariation, LoadoutBase<?, ?> aLoadout, MessageXBar anXBar){
      super(aLoadout, anXBar, "load stock");

      StockLoadout stockLoadout = StockLoadoutDB.lookup(aChassiVariation);

      addOp(new OpStripLoadout(loadout, xBar));
      addOp(new OpSetStructureType(xBar, loadout, stockLoadout.getStructureType()));
      addOp(new OpSetGuidanceType(xBar, loadout, stockLoadout.getGuidanceType()));
      addOp(new OpSetArmorType(xBar, loadout, stockLoadout.getArmorType()));
      addOp(new OpSetHeatSinkType(xBar, loadout, stockLoadout.getHeatSinkType()));

      for(StockLoadout.StockComponent stockComponent : stockLoadout.getComponents()){
         Location location = stockComponent.getPart();
         ConfiguredComponent configured = aLoadout.getComponent(location);

         if( location.isTwoSided() ){
            addOp(new OpSetArmor(xBar, aLoadout, configured, ArmorSide.BACK, stockComponent.getArmorBack(), true));
            addOp(new OpSetArmor(xBar, aLoadout, configured, ArmorSide.FRONT, stockComponent.getArmorFront(), true));
         }
         else{
            addOp(new OpSetArmor(xBar, aLoadout, configured, ArmorSide.ONLY, stockComponent.getArmorFront(), true));
         }

         for(Integer item : stockComponent.getItems()){
            addOp(new OpAddItem(xBar, aLoadout, configured, ItemDB.lookup(item)));
         }
      }
   }
}
