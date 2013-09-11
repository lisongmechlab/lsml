package lisong_mechlab.model.loadout.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.Huffman1;
import lisong_mechlab.util.MessageXBar;

/**
 * A first simple implementation of a {@link LoadoutCoder}.
 * 
 * @author Li Song
 */
public class LoadoutCoderV1 implements LoadoutCoder{

   private final Huffman1<Integer> huff;
   private final MessageXBar       xBar;
   private final Part[]            partOrder = new Part[] {Part.RightArm, Part.RightTorso, Part.RightLeg, Part.Head, Part.CenterTorso,
         Part.LeftTorso, Part.LeftLeg, Part.LeftArm};

   public LoadoutCoderV1(MessageXBar anXBar){
      xBar = anXBar;
      ObjectInputStream in = null;
      try{
         InputStream is;
         // = LoadoutCoderV1.class.getResourceAsStream("/resources/coderstats.bin");
         // if( is == null ){
         is = new FileInputStream("resources/resources/coderstats.bin");
         // }
         in = new ObjectInputStream(is);
         Map<Integer, Integer> freqs = (Map<Integer, Integer>)in.readObject();
         huff = new Huffman1<Integer>(freqs, null);
      }
      catch( Exception e ){
         throw new RuntimeException(e);
      }
      finally{
         if( in != null ){
            try{
               in.close();
            }
            catch( IOException e ){
               e.printStackTrace();
            }
         }
      }
   }

   @Override
   public byte[] encode(final Loadout aLoadout) throws IOException{
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream(100);

      // Write header (32 bits)
      {
         buffer.write(0xAC); // 8 bits for version number

         int upeff = 0; // 8 bits for efficiencies
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasArtemis() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasDoubleHeatSinks() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasFerroFibrous() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasEndoSteel() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasCoolRun() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasHeatContainment() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasSpeedTweak() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasDoubleBasics() ? 1 : 0);
         buffer.write(upeff);

         // 16 bits contain chassi ID.
         short chassiId = (short)aLoadout.getChassi().getMwoId();
         if( chassiId != aLoadout.getChassi().getMwoId() )
            throw new RuntimeException("Chassi ID was larger than 16 bits!");

         buffer.write((chassiId & 0xFF00) >> 8); // Big endian, respecting RFC 1700
         buffer.write((chassiId & 0xFF));
      }

      // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
      // 1 byte per armor value (2 for RT,CT,LT front first)
      for(Part part : partOrder){
         if( part.isTwoSided() ){
            buffer.write((byte)aLoadout.getPart(part).getArmor(ArmorSide.FRONT));
            buffer.write((byte)aLoadout.getPart(part).getArmor(ArmorSide.BACK));
         }
         else{
            buffer.write((byte)aLoadout.getPart(part).getArmor(ArmorSide.ONLY));
         }
      }

      // Items are encoded as a list of integers which record the item ID. Components are separated by -1.
      // The order is the same as for armor: RA, RT, RL, HD, CT, LT, LL, LA
      {
         List<Integer> ids = new ArrayList<>();
         for(Part part : partOrder){
            List<Item> items = aLoadout.getPart(part).getItems();
            for(Item item : items){
               if( !(item instanceof Internal) ){
                  ids.add(item.getMwoIdx());
               }
            }
            ids.add(-1);
         }
         ids.remove(ids.size() - 1); // Remove the last separator

         // Encode the list with huffman
         buffer.write(huff.encode(ids));
      }
      return buffer.toByteArray();
   }

   @Override
   public Loadout decode(final byte[] aBitStream) throws IOException{
      final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
      final Loadout loadout;

      // Read header
      {
         if( buffer.read() != 0xAC ){
            throw new DecodingException(); // Wrong format
         }

         int upeff = buffer.read() & 0xFF; // 8 bits for efficiencies and
         // 16 bits contain chassi ID.
         short chassiId = (short)(((buffer.read() & 0xFF) << 8) | (buffer.read() & 0xFF)); // Big endian, respecting RFC
                                                                                           // 1700

         Chassi chassi = ChassiDB.lookup(chassiId);
         loadout = new Loadout(chassi, xBar);

         loadout.getUpgrades().setArtemis((upeff & (1 << 7)) != 0);
         loadout.getUpgrades().setDoubleHeatSinks((upeff & (1 << 6)) != 0);
         loadout.getUpgrades().setFerroFibrous((upeff & (1 << 5)) != 0);
         loadout.getUpgrades().setEndoSteel((upeff & (1 << 4)) != 0);
         loadout.getEfficiencies().setCoolRun((upeff & (1 << 3)) != 0);
         loadout.getEfficiencies().setHeatContainment((upeff & (1 << 2)) != 0);
         loadout.getEfficiencies().setSpeedTweak((upeff & (1 << 1)) != 0);
         loadout.getEfficiencies().setDoubleBasics((upeff & (1 << 0)) != 0);
      }

      // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
      // 1 byte per armor value (2 for RT,CT,LT front first)
      for(Part part : partOrder){
         if( part.isTwoSided() ){
            loadout.getPart(part).setArmor(ArmorSide.FRONT, buffer.read());
            loadout.getPart(part).setArmor(ArmorSide.BACK, buffer.read());
         }
         else{
            loadout.getPart(part).setArmor(ArmorSide.ONLY, buffer.read());
         }
      }

      // Items are encoded as a list of integers which record the item ID. Components are separated by -1.
      // The order is the same as for armor: RA, RT, RL, HD, CT, LT, LL, LA
      {
         byte[] rest = new byte[buffer.available()];
         buffer.read(rest);
         List<Integer> ids = huff.decode(rest);
         for(Part part : partOrder){
            Integer v;
            while( !ids.isEmpty() && -1 != (v = ids.remove(0)) ){
               loadout.getPart(part).addItem(ItemDB.lookup(v));
            }
         }
      }
      return loadout;
   }

   /**
    * Will process the stock builds and generate statistics
    * 
    * @param arg
    * @throws Exception
    */
   public static void main(String[] arg) throws Exception{
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));
   
      Map<Integer, Integer> freqs = new TreeMap<>();
      MessageXBar anXBar = new MessageXBar();
      for(Chassi chassi : chassii){
         Loadout loadout = new Loadout(chassi, anXBar);
         loadout.loadStock();
   
         for(Item item : loadout.getAllItems()){
            if( item == null ){
               throw new RuntimeException("FFAIL");
            }
   
            if( !(item instanceof Internal) ){
               int id = item.getMwoIdx();
               if( freqs.containsKey(id) ){
                  freqs.put(id, freqs.get(id) + 1);
               }
               else{
                  freqs.put(id, 1);
               }
            }
         }
      }
   
      // Make sure all items are in the statistics even if they have a very low probability
      for(Item item : ItemDB.lookup(Item.class)){
         int id = item.getMwoIdx();
         if( !freqs.containsKey(id) )
            freqs.put(id, 1);
      }
   
      freqs.put(-1, chassii.size() * 7); // 7 separators per chassi
   
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("resources/resources/coderstats.bin"));
      out.writeObject(freqs);
      out.close();
   }
}
