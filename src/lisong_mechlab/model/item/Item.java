package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.Localization;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStats;

public class Item implements Comparable<Item>{
   private final String        locName;
   private final String        locDesc;
   private final String        mwoName;
   private final int           mwoIdx;

   private final int           slots;
   private final double        tons;
   private final HardpointType hardpointType;

   public Item(ItemStats anItemStats, HardpointType aHardpointType, int aNumSlots, double aNumTons){
      locName = Localization.key2string(anItemStats.Loc.nameTag);
      locDesc = Localization.key2string(anItemStats.Loc.descTag);
      mwoName = anItemStats.name;
      mwoIdx = anItemStats.id;

      slots = aNumSlots;
      tons = aNumTons;
      hardpointType = aHardpointType;
   }

   public Item(String aNameTag, String aDesc, int aSlots){
      locName = Localization.key2string(aNameTag);
      locDesc = Localization.key2string(aDesc);
      mwoName = "";
      mwoIdx = -1;

      slots = aSlots;
      tons = 0;
      hardpointType = HardpointType.NONE;
   }

   public String getKey(){
      return mwoName;
   }

   @Override
   final public String toString(){
      return getName();
   }

   final public String getName(){
      return locName;
   }

   public int getNumCriticalSlots(){
      return slots;
   }

   public int getNumCriticalSlots(Upgrades aUpgrades){
      return getNumCriticalSlots();
   }

   public HardpointType getHardpointType(){
      return hardpointType;
   }

   public double getMass(){
      return tons;
   }

   public double getMass(Upgrades aUpgrades){
      return getMass();
   }

   public int getMwoIdx(){
      return mwoIdx;
   }

   public String getDescription(){
      return locDesc;
   }

   /**
    * Determines if the given {@link Loadout} is able to equip the given item. Will consider the chassi and upgrades
    * only.
    * 
    * @param aLoadout
    * @return True if the {@link Loadout} is able to carry the weapon with current upgrades.
    */
   public boolean isEquippableOn(Loadout aLoadout){
      return true;
   }

   /*
    * (non-Javadoc) Defines sorting order for items. Default is lexicographical order.
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(Item rhs){
      return locName.compareTo(rhs.locName);
   }

   public String getName(Upgrades aUpgrades){
      return getName();
   }
}
