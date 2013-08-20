package lisong_mechlab.model.chassi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.mwo_parsing.HardpointsXml;
import lisong_mechlab.model.mwo_parsing.helpers.MdfComponent;
import lisong_mechlab.model.mwo_parsing.helpers.MdfComponent.Hardpoint;
import lisong_mechlab.model.mwo_parsing.helpers.MdfInternal;

public class InternalPart{
   private final int                         criticalslots;
   private final Part                        type;
   private final int                         maxarmor;
   private final double                      hitpoints;
   private final List<Item>                  internals;
   private final Map<HardpointType, Integer> hardpoints;

   public InternalPart(MdfComponent aComponent, Part aPart, Chassi aChassi, HardpointsXml aHardpoints){
      criticalslots = aComponent.Slots;
      type = aPart;
      hitpoints = aComponent.HP;
      maxarmor = (int)(hitpoints*2);

      internals = new ArrayList<Item>();
      if( null != aComponent.internals ){
         for(MdfInternal internal : aComponent.internals){
            internals.add(new Internal(internal));
         }
      }

      hardpoints = new HashMap<HardpointType, Integer>();
      if( null != aComponent.hardpoints ){
         for(Hardpoint hardpoint : aComponent.hardpoints){
            final HardpointType hardpointType = HardpointType.fromMwoType(hardpoint.Type);
            if( !hardpoints.containsKey(hardpointType) ){
               hardpoints.put(hardpointType, Integer.valueOf(0));
            }
            hardpoints.put(hardpointType, hardpoints.get(hardpointType) + aHardpoints.slotsForId(hardpoint.ID));
         }
      }
      
      // Stupid PGI making hacks to put ECM on a hardpoint... now I have to change my code...
      if (aComponent.CanEquipECM == 1)
         hardpoints.put(HardpointType.ECM, 1);
   }
   
   @Override
   public String toString(){
      return getType().toString();
   }

   public Part getType(){
      return type;
   }

   public int getArmorMax(){
      return maxarmor;
   }

   public int getNumCriticalslots(){
      return criticalslots;
   }

   public int getNumHardpoints(HardpointType aHardpointType){
      if( hardpoints.containsKey(aHardpointType) ){
         return hardpoints.get(aHardpointType);
      }
      return 0;
   }

   public List<Item> getInternalItems(){
      return Collections.unmodifiableList(internals);
   }

   public double getHitpoints(){
      return hitpoints;
   }
}
