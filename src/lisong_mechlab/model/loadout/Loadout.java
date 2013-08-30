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

import lisong_mechlab.XmlReader;
import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.MessageXBar;
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
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Loadout.Message.Type;
import lisong_mechlab.model.loadout.converters.ChassiConverter;
import lisong_mechlab.model.loadout.converters.ItemConverter;
import lisong_mechlab.model.loadout.converters.LoadoutConverter;
import lisong_mechlab.model.loadout.converters.LoadoutPartConverter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Loadout implements MessageXBar.Reader{
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

      public final Loadout loadout;
      public final Type    type;

      public Message(Loadout aLoadout, Type aType){
         loadout = aLoadout;
         type = aType;
      }

      public enum Type{
         RENAME, CREATE, UPGRADES
      }

   }

   private String                       name;
   private final Chassi                 chassi;
   private final Map<Part, LoadoutPart> parts = new TreeMap<Part, LoadoutPart>();
   private final Upgrades               upgrades;
   private final Efficiencies           efficiencies;
   private final MessageXBar            xBar;

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
      upgrades = new Upgrades(anXBar);
      for(InternalPart part : chassi.getInternalParts()){
         LoadoutPart confPart = new LoadoutPart(this, part, anXBar);
         parts.put(part.getType(), confPart);
      }

      xBar = anXBar;
      xBar.attach(this);
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
      loadStock();
   }

   @Override
   public String toString(){
      if( getName().contains(chassi.getNameShort()) )
         return getName();
      else
         return getName() + " (" + chassi.getNameShort() + ")";
   }

   public static Loadout load(File aFile, MessageXBar crossBar){
      XStream stream = loadoutXstream(crossBar);
      return (Loadout)stream.fromXML(aFile);
   }

   public void loadStock() throws Exception{
      strip();

      File loadoutXml = new File("Game/Libs/MechLoadout/" + chassi.getMwoName().toLowerCase() + ".xml");
      GameDataFile dataFile = new GameDataFile();
      XmlReader reader = new XmlReader(dataFile.openGameFile(loadoutXml));

      List<Element> maybeUpgrades = reader.getElementsByTagName("Upgrades");
      if( maybeUpgrades.size() == 1 ){
         Element upgrades = maybeUpgrades.get(0);
         getUpgrades().setDoubleHeatSinks(reader.getElementByTagName("HeatSinks", upgrades).getAttribute("Type").equals("Double"));
         getUpgrades().setFerroFibrous(reader.getElementByTagName("Armor", upgrades).getAttribute("ItemID").equals("2801"));
         getUpgrades().setEndoSteel(reader.getElementByTagName("Structure", upgrades).getAttribute("ItemID").equals("3101"));
         getUpgrades().setArtemis(reader.getElementByTagName("Artemis", upgrades).getAttribute("Equipped").equals("1"));
      }

      for(Element component : reader.getElementsByTagName("component")){
         String name = component.getAttribute("Name");
         int armor = Integer.parseInt(component.getAttribute("Armor"));

         Part partType = Part.fromMwoName(name);

         LoadoutPart part = getPart(partType);
         if( partType.isTwoSided() ){
            if( Part.isRear(name) )
               part.setArmor(ArmorSide.BACK, armor);
            else
               part.setArmor(ArmorSide.FRONT, armor);
         }
         else
            part.setArmor(ArmorSide.ONLY, armor);

         Node child = component.getFirstChild();
         while( null != child ){
            if( child.getNodeType() == Node.ELEMENT_NODE ){
               Item item = ItemDB.lookup(Integer.parseInt(((Element)child).getAttribute("ItemID")));
               part.addItem(item);
            }
            child = child.getNextSibling();
         }
      }
   }

   public void strip(){
      stripArmor();
      for(LoadoutPart loadoutPart : parts.values()){
         loadoutPart.removeAllItems();
      }
      upgrades.setArtemis(false);
      upgrades.setEndoSteel(false);
      upgrades.setFerroFibrous(false);
      upgrades.setDoubleHeatSinks(false);
   }

   public double getMass(){
      double ans = chassi.getInternalMass();
      if( getUpgrades().hasEndoSteel() ){
         ans *= 0.5;
      }
      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getItemMass();
      }

      ans += getArmor() / LoadoutPart.ARMOR_PER_TON * (1.0 - (getUpgrades().hasFerroFibrous() ? 0.12 : 0));
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
         ans += 14;
      }
      if( getUpgrades().hasEndoSteel() ){
         ans += 14;
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

   static XStream loadoutXstream(MessageXBar anXBar){
      XStream stream = new XStream(new StaxDriver());
      stream.setMode(XStream.NO_REFERENCES);
      stream.registerConverter(new ChassiConverter());
      stream.registerConverter(new ItemConverter());
      stream.registerConverter(new LoadoutPartConverter(null));
      stream.registerConverter(new LoadoutConverter(anXBar));
      stream.omitField(Observable.class, "changed");
      stream.omitField(Observable.class, "obs");
      stream.addImmutableType(Item.class);
      stream.alias("component", LoadoutPart.class);
      stream.alias("loadout", Loadout.class);
      stream.addImplicitMap(Loadout.class, "parts", LoadoutPart.class, "internalpart");
      return stream;
   }

   @Override
   public void receive(MessageXBar.Message aMsg){
      if( aMsg instanceof Upgrades.Message ){
         Upgrades.Message msg = (Upgrades.Message)aMsg;
         if( msg.source != upgrades ){
            return;
         }
         switch( msg.msg ){
            case ARMOR:
               if( getNumCriticalSlotsFree() < 0 ){
                  upgrades.setFerroFibrous(false);
                  throw new IllegalArgumentException("Not enough free slots!");
               }
               break;
            case GUIDANCE:
               checkArtemisAdditionLegal();
               
               break;
            case HEATSINKS:
               break;
            case STRUCTURE:
               if( getNumCriticalSlotsFree() < 0 ){
                  upgrades.setEndoSteel(false);
                  throw new IllegalArgumentException("Not enough free slots!");
               }
               break;
            default:
               break;

         }
      }
   }

   private void checkArtemisAdditionLegal() throws IllegalArgumentException{
      if(upgrades.hasArtemis()){
         int extraMassCounter = 0;
         int extraCritSlotsCounter = 0;
         for(LoadoutPart part : parts.values()){
            for(Item item : part.getItems()){
               if(item instanceof MissileWeapon){
                  extraCritSlotsCounter++;
                  extraMassCounter++;
               }
            }
         }
         
         if(!(extraMassCounter <= getFreeMass()) ){
            getUpgrades().setArtemis(false);
            throw new IllegalArgumentException("Not enough free mass!");
            
         }
         if(!(extraCritSlotsCounter <= getNumCriticalSlotsFree())){
            getUpgrades().setArtemis(false);
            throw new IllegalArgumentException("Not enough free crit slots!");
         }
      }
   }

   private double getFreeMass(){
      double freeMass = chassi.getMassMax() - getMass();
      return freeMass;
   }

   public void setMaxArmor(double aRatio){
      stripArmor();
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
            part.setArmor(ArmorSide.FRONT, front);
            part.setArmor(ArmorSide.BACK, back);
         }
         else{
            part.setArmor(ArmorSide.ONLY, max);
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
         if(part.canAddItem(anItem))
            return true;
      }
      return false;
   }

   public void stripArmor(){
      for(LoadoutPart loadoutPart : parts.values()){
         if( loadoutPart.getInternalPart().getType().isTwoSided() ){
            loadoutPart.setArmor(ArmorSide.FRONT, 0);
            loadoutPart.setArmor(ArmorSide.BACK, 0);
         }
         else{
            loadoutPart.setArmor(ArmorSide.ONLY, 0);
         }
      }
   }
}
