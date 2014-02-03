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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.Loadout.Message.Type;
import lisong_mechlab.model.loadout.OperationStack.CompositeOperation;
import lisong_mechlab.model.loadout.converters.ChassiConverter;
import lisong_mechlab.model.loadout.converters.ItemConverter;
import lisong_mechlab.model.loadout.converters.LoadoutConverter;
import lisong_mechlab.model.loadout.converters.LoadoutPartConverter;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.loadout.part.StripPartOperation;
import lisong_mechlab.model.upgrades.SetArtemisOperation;
import lisong_mechlab.model.upgrades.SetDHSOperation;
import lisong_mechlab.model.upgrades.SetEndoSteelOperation;
import lisong_mechlab.model.upgrades.SetFerroFibrousOperation;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.XmlReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class represents the complete state of a 'mechs configuration.
 * <p>
 * TODO: Strip some logic from this class.
 * 
 * @author Li Song
 */
public class Loadout{
   public static class Message implements MessageXBar.Message{
      @Override
      public int hashCode(){
         final int prime = 31;
         int result = 1;
         result = prime * result + ((loadout == null) ? 0 : loadout.hashCode());
         result = prime * result + ((type == null) ? 0 : type.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj){
         if( this == obj )
            return true;
         if( obj == null )
            return false;
         if( getClass() != obj.getClass() )
            return false;
         Message other = (Message)obj;
         if( loadout == null ){
            if( other.loadout != null )
               return false;
         }
         else if( !loadout.equals(other.loadout) )
            return false;
         if( type != other.type )
            return false;
         return true;
      }

      private final Loadout loadout;
      public final Type     type;

      public Message(Loadout aLoadout, Type aType){
         loadout = aLoadout;
         type = aType;
      }

      public enum Type{
         RENAME, CREATE
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return loadout == aLoadout;
      }
   }

   private String                       name;
   private final Chassi                 chassi;
   private final Map<Part, LoadoutPart> parts    = new TreeMap<Part, LoadoutPart>();
   private final Upgrades               upgrades = new Upgrades();
   private final Efficiencies           efficiencies;
   private final transient MessageXBar  xBar;

   /**
    * Will create a new, empty load out based on the given chassi.
    * 
    * @param aChassi
    *           The chassi to base the load out on.
    * @param anXBar
    *           The {@link MessageXBar} to signal changes to this loadout on.
    */
   public Loadout(Chassi aChassi, MessageXBar anXBar){
      name = aChassi.getNameShort();
      chassi = aChassi;
      for(InternalPart part : chassi.getInternalParts()){
         LoadoutPart confPart = new LoadoutPart(this, part);
         parts.put(part.getType(), confPart);
      }

      xBar = anXBar;
      xBar.post(new Message(this, Type.CREATE));

      efficiencies = new Efficiencies(xBar);
   }

   /**
    * Will load a stock load out for the given variation name.
    * 
    * @param aString
    *           The name of the stock variation to load.
    * @param anXBar
    * @throws Exception
    */
   public Loadout(String aString, MessageXBar anXBar) throws Exception{
      this(ChassiDB.lookup(aString), anXBar);
      OperationStack operationStack = new OperationStack(0);
      operationStack.pushAndApply(new LoadStockOperation());
   }

   @Override
   public String toString(){
      if( getName().contains(chassi.getNameShort()) )
         return getName();
      return getName() + " (" + chassi.getNameShort() + ")";
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((chassi == null) ? 0 : chassi.hashCode());
      result = prime * result + ((efficiencies == null) ? 0 : efficiencies.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((parts == null) ? 0 : parts.hashCode());
      result = prime * result + ((upgrades == null) ? 0 : upgrades.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !(obj instanceof Loadout) )
         return false;
      Loadout that = (Loadout)obj;
      if( !chassi.equals(that.chassi) )
         return false;
      if( !efficiencies.equals(that.efficiencies) )
         return false;
      if( !name.equals(that.name) )
         return false;
      if( !parts.equals(that.parts) )
         return false;
      if( !upgrades.equals(that.upgrades) )
         return false;
      return true;
   }

   public static Loadout load(File aFile, MessageXBar anXBar){
      XStream stream = loadoutXstream(anXBar);
      return (Loadout)stream.fromXML(aFile);
   }

   public class LoadStockOperation extends CompositeOperation{

      public LoadStockOperation() throws Exception{
         super("load stock");
         addOp(new StripOperation());

         File loadoutXml = new File("Game/Libs/MechLoadout/" + chassi.getMwoName().toLowerCase() + ".xml");
         GameDataFile dataFile = new GameDataFile();
         XmlReader reader = new XmlReader(dataFile.openGameFile(loadoutXml));

         List<Element> maybeUpgrades = reader.getElementsByTagName("Upgrades");
         if( maybeUpgrades.size() == 1 ){
            Element stockUpgrades = maybeUpgrades.get(0);
            // TODO: We really should fix issue #75 to get rid of these hard coded constants.
            boolean stockDhs = reader.getElementByTagName("HeatSinks", stockUpgrades).getAttribute("Type").equals("Double");
            boolean stockFerro = reader.getElementByTagName("Armor", stockUpgrades).getAttribute("ItemID").equals("2801");
            boolean stockEndo = reader.getElementByTagName("Structure", stockUpgrades).getAttribute("ItemID").equals("3101");
            boolean stockArtemis = reader.getElementByTagName("Artemis", stockUpgrades).getAttribute("Equipped").equals("1");
            addOp(new SetEndoSteelOperation(xBar, Loadout.this, stockEndo));
            addOp(new SetArtemisOperation(xBar, Loadout.this, stockArtemis));
            addOp(new SetFerroFibrousOperation(xBar, Loadout.this, stockFerro));
            addOp(new SetDHSOperation(xBar, Loadout.this, stockDhs));

            // FIXME: Revisit this fix! The game files are broken.
            if( chassi.getNameShort().equals("KTO-19") ){
               addOp(new SetFerroFibrousOperation(xBar, Loadout.this, true));
            }
         }
         else{
            addOp(new SetEndoSteelOperation(xBar, Loadout.this, false));
            addOp(new SetArtemisOperation(xBar, Loadout.this, false));
            addOp(new SetFerroFibrousOperation(xBar, Loadout.this, false));
            addOp(new SetDHSOperation(xBar, Loadout.this, false));
         }

         for(Element component : reader.getElementsByTagName("component")){
            String componentName = component.getAttribute("Name");
            int componentArmor = Integer.parseInt(component.getAttribute("Armor"));

            Part partType = Part.fromMwoName(componentName);

            LoadoutPart part = getPart(partType);
            if( partType.isTwoSided() ){
               if( Part.isRear(componentName) )
                  addOp(new SetArmorOperation(xBar, part, ArmorSide.BACK, componentArmor));
               else
                  addOp(new SetArmorOperation(xBar, part, ArmorSide.FRONT, componentArmor));
            }
            else
               addOp(new SetArmorOperation(xBar, part, ArmorSide.ONLY, componentArmor));

            Node child = component.getFirstChild();
            while( null != child ){
               if( child.getNodeType() == Node.ELEMENT_NODE ){
                  Item item = ItemDB.lookup(Integer.parseInt(((Element)child).getAttribute("ItemID")));
                  addOp(new AddItemOperation(xBar, part, item));
               }
               child = child.getNextSibling();
            }
         }
      }
   }

   public class StripOperation extends CompositeOperation{
      public StripOperation(){
         super("strip mech");
         for(LoadoutPart loadoutPart : parts.values()){
            addOp(new StripPartOperation(xBar, loadoutPart));
         }
         addOp(new SetEndoSteelOperation(xBar, Loadout.this, false));
         addOp(new SetArtemisOperation(xBar, Loadout.this, false));
         addOp(new SetFerroFibrousOperation(xBar, Loadout.this, false));
         addOp(new SetDHSOperation(xBar, Loadout.this, false));
      }
   }

   public double getMass(){
      double ans = chassi.getInternalMass();
      if( getUpgrades().hasEndoSteel() ){
         ans *= 0.5;
         ans += (chassi.getMassMax() % 10) * 0.05;
      }
      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getItemMass();
      }

      ans += getArmor() / (LoadoutPart.ARMOR_PER_TON * (getUpgrades().hasFerroFibrous() ? 1.12 : 1));
      return ans;
   }

   public String getName(){
      return name;
   }

   public Chassi getChassi(){
      return chassi;
   }

   public LoadoutPart getPart(Part aPartType){
      return parts.get(aPartType);
   }

   public int getArmor(){
      int ans = 0;
      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getArmorTotal();
      }
      return ans;
   }

   public int getNumCriticalSlotsFree(){
      return 12 * 5 + 6 * 3 - getNumCriticalSlotsUsed();
   }

   public int getNumCriticalSlotsUsed(){
      int ans = 0;

      if( getUpgrades().hasFerroFibrous() ){
         ans += 14; // TODO: We need to prepare to handle Clan FF
      }
      if( getUpgrades().hasEndoSteel() ){
         ans += 14; // TODO: We need to prepare to handle Clan ES
      }

      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getNumCriticalSlotsUsed();
      }
      return ans;
   }

   public Upgrades getUpgrades(){
      return upgrades;
   }

   public Engine getEngine(){
      LoadoutPart part = getPart(Part.CenterTorso);
      for(Item item : part.getItems()){
         if( item instanceof Engine ){
            return (Engine)item;
         }
      }
      return null;
   }

   public Efficiencies getEfficiencies(){
      return efficiencies;
   }

   public int getHeatsinksCount(){
      int ans = 0;
      for(Item item : getAllItems()){
         if( item instanceof HeatSink ){
            ans++;
         }
         else if( item instanceof Engine ){
            ans += ((Engine)item).getNumInternalHeatsinks();
         }
      }
      return ans;
   }

   public Collection<LoadoutPart> getPartLoadOuts(){
      return parts.values();
   }

   public void save(File aFile) throws IOException{
      FileWriter fileWriter = null;
      try{
         fileWriter = new FileWriter(aFile);
         fileWriter.write(loadoutXstream(xBar).toXML(this));
      }
      finally{
         if( fileWriter != null ){
            fileWriter.close();
         }
      }
   }

   public void rename(String aName){
      name = aName;
      xBar.post(new Message(this, Type.RENAME));
   }

   public static XStream loadoutXstream(MessageXBar anXBar){
      XStream stream = new XStream(new StaxDriver());
      stream.setMode(XStream.NO_REFERENCES);
      stream.registerConverter(new ChassiConverter());
      stream.registerConverter(new ItemConverter());
      stream.registerConverter(new LoadoutPartConverter(anXBar, null));
      stream.registerConverter(new LoadoutConverter(anXBar));
      stream.omitField(Observable.class, "changed");
      stream.omitField(Observable.class, "obs");
      stream.addImmutableType(Item.class);
      stream.alias("component", LoadoutPart.class);
      stream.alias("loadout", Loadout.class);
      stream.addImplicitMap(Loadout.class, "parts", LoadoutPart.class, "internalpart");
      return stream;
   }

   public double getFreeMass(){
      double freeMass = chassi.getMassMax() - getMass();
      return freeMass;
   }

   public class SetMaxArmorOperation extends CompositeOperation{
      public SetMaxArmorOperation(double aRatio){
         super("set max armor");
         for(LoadoutPart part : parts.values()){
            final int max = part.getInternalPart().getArmorMax();
            if( part.getInternalPart().getType().isTwoSided() ){
               // 1) front + back = max
               // 2) front / back = ratio
               // front = back * ratio
               // front = max - back
               // = > back * ratio = max - back
               int back = (int)(max / (aRatio + 1));
               int front = max - back;

               addOp(new SetArmorOperation(xBar, part, ArmorSide.BACK, 0));
               addOp(new SetArmorOperation(xBar, part, ArmorSide.FRONT, front));
               addOp(new SetArmorOperation(xBar, part, ArmorSide.BACK, back));
            }
            else{
               addOp(new SetArmorOperation(xBar, part, ArmorSide.ONLY, max));
            }
         }
      }
   }

   public int getJumpJetCount(){
      int ans = 0;
      for(Item item : getAllItems()){
         if( item instanceof JumpJet )
            ans++;
      }
      return ans;
   }

   public JumpJet getJumpJetType(){
      for(Item item : getAllItems()){
         if( item instanceof JumpJet ){
            return (JumpJet)item;
         }
      }
      return null;
   }

   public Collection<Item> getAllItems(){
      List<Item> items = new ArrayList<>();
      for(LoadoutPart part : parts.values()){
         items.addAll(part.getItems());
      }
      return items;
   }

   public boolean isEquippable(Item anItem){
      for(LoadoutPart part : parts.values()){
         if( part.canAddItem(anItem) )
            return true;
      }
      return false;
   }

   public class StripArmorOperation extends CompositeOperation{
      public StripArmorOperation(){
         super("strip armor");
         for(LoadoutPart loadoutPart : parts.values()){
            if( loadoutPart.getInternalPart().getType().isTwoSided() ){
               addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.FRONT, 0));
               addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.BACK, 0));
            }
            else{
               addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.ONLY, 0));
            }
         }
      }
   }

   public class AutoAddItemOperation extends CompositeOperation{
      public AutoAddItemOperation(Item anItem){
         super("auto place item");
         LoadoutPart ct = parts.get(Part.CenterTorso);
         if( anItem instanceof HeatSink && ct.getNumEngineHeatsinks() < ct.getNumEngineHeatsinksMax() && ct.canAddItem(anItem) ){
            addOp(new AddItemOperation(xBar, ct, anItem));
            return;
         }

         Part[] partOrder = new Part[] {Part.RightArm, Part.RightTorso, Part.RightLeg, Part.Head, Part.CenterTorso, Part.LeftTorso, Part.LeftLeg,
               Part.LeftArm};

         for(Part part : partOrder){
            LoadoutPart loadoutPart = parts.get(part);
            if( loadoutPart.canAddItem(anItem) ){
               addOp(new AddItemOperation(xBar, loadoutPart, anItem));
               return;
            }
         }
      }
   }
}
