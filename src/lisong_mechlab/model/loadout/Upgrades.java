package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.LoadoutPart.Message;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;
import lisong_mechlab.model.loadout.Upgrades.Message.ChangeMsg;
import lisong_mechlab.model.upgrade.ArmorUpgrade;
import lisong_mechlab.model.upgrade.GuidanceUpgrade;
import lisong_mechlab.model.upgrade.HeatsinkUpgrade;
import lisong_mechlab.model.upgrade.StructureUpgrade;
import lisong_mechlab.model.upgrade.UpgradeDB;
import lisong_mechlab.util.MessageXBar;

/**
 * This class is a simple container that manages upgrades for an loadout.
 * 
 * @author Li Song
 */
public class Upgrades{
   private ArmorUpgrade                armorType     = UpgradeDB.STANDARD_ARMOR;
   private StructureUpgrade            structureType = UpgradeDB.STANDARD_STRUCTURE;
   private GuidanceUpgrade             guidanceType  = UpgradeDB.STANDARD_GUIDANCE;
   private HeatsinkUpgrade             heatsinkType  = UpgradeDB.STANDARD_HEATSINNKS;

   private transient final MessageXBar xBar;

   public static class Message implements MessageXBar.Message{
      public final ChangeMsg msg;
      public final Upgrades  source;

      public enum ChangeMsg{
         GUIDANCE, STRUCTURE, ARMOR, HEATSINKS
      }

      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message other = (Message)obj;
            return msg == other.msg && source == other.source;
         }
         return false;
      }

      Message(ChangeMsg aChangeMsg, Upgrades anUpgrades){
         msg = aChangeMsg;
         source = anUpgrades;
      }
   }

   public Upgrades(MessageXBar anXBar){
      xBar = anXBar;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof Upgrades) )
         return false;
      Upgrades that = (Upgrades)obj;
      if( this.guidanceType != that.guidanceType )
         return false;
      if( this.heatsinkType != that.heatsinkType )
         return false;
      if( this.structureType != that.structureType )
         return false;
      if( this.armorType != that.armorType )
         return false;
      return true;
   }

   public GuidanceUpgrade getGuidance(){
      return guidanceType;
   }

   public HeatsinkUpgrade getHeatSink(){
      return heatsinkType;
   }

   public StructureUpgrade getStructure(){
      return structureType;
   }

   public ArmorUpgrade getArmor(){
      return armorType;
   }

   public void setGuidance(GuidanceUpgrade aGuidanceUpgrade){
      if( guidanceType != aGuidanceUpgrade ){
         // TODO: Check that the new upgrade is viable
         guidanceType = aGuidanceUpgrade;
         // TODO: Change any items on the loadout that are affected
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.GUIDANCE, this));
      }
   }
   
//   private Loadout loadout;
//   private Chassi  chassi;
//   private int     additionalCritSlots;
//   private int     additionalMass;
//
//   public ArtemisHandler(Loadout loadoutUnderTest){
//      loadout = loadoutUnderTest;
//      chassi = loadout.getChassi();
//      additionalCritSlots = 0;
//      additionalMass = 0;
//   }
//
//   public void checkLoadoutStillValid(){
//      if( !loadout.getUpgrades().hasArtemis() ){
//         for(LoadoutPart part : loadout.getPartLoadOuts()){
//            additionalCritSlots = 0;
//            additionalMass = 0;
//            if( !checkPartCanHoldArtemis(part) ){
//               throw new IllegalArgumentException("Not enough free crit slots!");
//            }
//         }
//      }
//
//   }
//
//   private boolean checkPartCanHoldArtemis(LoadoutPart aPart){
//
//      for(Item item : aPart.getItems()){
//         if( (item instanceof MissileWeapon) ){
//            additionalCritSlots++;
//            additionalMass++;
//         }
//
//      }
//      if( ((aPart.getNumCriticalSlotsFree()) < additionalCritSlots) && additionalCritSlots != 0 ){
//
//         return false;
//      }
//      return true;
//
//   }
//
//   public void checkArtemisAdditionLegal() throws IllegalArgumentException{
//      additionalCritSlots = 0;
//      additionalMass = 0;
//      for(Item item : loadout.getAllItems()){
//         if( (item instanceof MissileWeapon) ){
//            additionalCritSlots++;
//            additionalMass++;
//         }
//
//      }
//      if( !loadout.getUpgrades().hasArtemis() ){
//         if( (loadout.getMass() + additionalMass) > chassi.getMassMax() ){
//            throw new IllegalArgumentException("Not enough free mass!");
//         }
//         if( (loadout.getNumCriticalSlotsFree() - additionalCritSlots) < 0 ){
//            throw new IllegalArgumentException("Not enough free crit slots!");
//         }
//      }
//
//   }
   
//   
//   if( msg.source != loadout.getUpgrades() ){
//      return;
//   }
//
//   if( msg.msg == Upgrades.Message.ChangeMsg.HEATSINKS ){
//      if( msg.source.hasDoubleHeatSinks() )
//         while( items.remove(ItemDB.SHS) ){/* No-Op */}
//      else
//         while( items.remove(ItemDB.DHS) ){/* No-Op */}
//   }
//   else if( msg.msg == Upgrades.Message.ChangeMsg.GUIDANCE ){
//      boolean changed = false;
//
//      for(AmmoWeapon weapon : ItemDB.lookup(AmmoWeapon.class)){
//         Upgrades oldUpgrades = new Upgrades(null);
//         oldUpgrades.setArtemis(!msg.source.hasArtemis());
//         Ammunition oldAmmoType = weapon.getAmmoType(oldUpgrades);
//         Ammunition newAmmoType = weapon.getAmmoType(msg.source);
//         if( oldAmmoType == newAmmoType )
//            continue;
//
//         while( items.remove(oldAmmoType) ){
//            items.add(newAmmoType);
//            changed = true;
//         }
//      }
//      if( changed )
//         xBar.post(new Message(this, Type.ItemsChanged));
//      // loadout.getUpgrades().setArtemis(false);
//   }

   public void setHeatSink(HeatsinkUpgrade aHeatsinkUpgrade){
      if( heatsinkType != aHeatsinkUpgrade ){
         // TODO: Check that the new upgrade is viable
         heatsinkType = aHeatsinkUpgrade;
         // TODO: Change any items on the loadout that are affected
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.HEATSINKS, this));
      }
   }

   public void setStructure(StructureUpgrade aStructureUpgrade){
      if( structureType != aStructureUpgrade ){
         // TODO: Check that the new upgrade is viable
         structureType = aStructureUpgrade;
         // TODO: Change any items on the loadout that are affected
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.STRUCTURE, this));
      }
   }

   public void setArmor(ArmorUpgrade anArmorUpgrade){
      if( armorType != anArmorUpgrade ){
         // TODO: Check that the new upgrade is viable
         armorType = anArmorUpgrade;
         // TODO: Change any items on the loadout that are affected
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.ARMOR, this));
      }
   }
}
