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
package lisong_mechlab.model.loadout.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.OpLoadStock;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.EncodingException;
import lisong_mechlab.util.Huffman1;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

/**
 * The Second version of {@link LoadoutCoder} for LSML.
 * 
 * @author Emily Björk
 */
public class LoadoutCoderV2 implements LoadoutCoder{
   private static final int        HEADER_MAGIC = 0xAC + 1;
   private final Huffman1<Integer> huff;
   private final MessageXBar       xBar;
   private final Location[]        partOrder    = new Location[] {Location.RightArm, Location.RightTorso, Location.RightLeg, Location.Head,
         Location.CenterTorso, Location.LeftTorso, Location.LeftLeg, Location.LeftArm};

   public LoadoutCoderV2(MessageXBar anXBar){
      xBar = anXBar;
      ObjectInputStream in = null;
      try{
         InputStream is = LoadoutCoderV2.class.getResourceAsStream("/resources/coderstats_v2.bin");
         in = new ObjectInputStream(is);
         @SuppressWarnings("unchecked")
         Map<Integer, Integer> freqs = (Map<Integer, Integer>)in.readObject();
         huff = new Huffman1<Integer>(freqs, null);

         // for(Map.Entry<Integer, Integer> e : freqs.entrySet())
         // System.out.println("["+e.getKey() + "] = " + e.getValue());
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
   public byte[] encode(final Loadout aLoadout) throws EncodingException{

      final ByteArrayOutputStream buffer = new ByteArrayOutputStream(100);

      // Write header (32 bits)
      {
         buffer.write(HEADER_MAGIC); // 8 bits for version number

         int upeff = 0; // 8 bits for efficiencies
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasCoolRun() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasHeatContainment() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasSpeedTweak() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasDoubleBasics() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasFastFire() ? 1 : 0);
         buffer.write(upeff);

         // 16 bits contain chassis ID.
         short chassiId = (short)aLoadout.getChassi().getMwoId();
         if( chassiId != aLoadout.getChassi().getMwoId() )
            throw new RuntimeException("Chassi ID was larger than 16 bits!");

         buffer.write((chassiId & 0xFF00) >> 8); // Big endian, respecting RFC 1700
         buffer.write((chassiId & 0xFF));
      }

      // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
      // 1 byte per armor value (2 for RT,CT,LT front first)
      for(Location part : partOrder){
         if( part.isTwoSided() ){
            buffer.write((byte)aLoadout.getPart(part).getArmor(ArmorSide.FRONT));
            buffer.write((byte)aLoadout.getPart(part).getArmor(ArmorSide.BACK));
         }
         else{
            buffer.write((byte)aLoadout.getPart(part).getArmor(ArmorSide.ONLY));
         }
      }

      // Items, upgrades and pilot modules are encoded as a list of integers which record the ItemID found in
      // ItemStats.xml.
      // Upgrades first, followed by -1, then components separated by -1 then pilot modules
      // The order of components is the same as for armor: RA, RT, RL, HD, CT, LT, LL, LA
      {
         List<Integer> ids = new ArrayList<>();

         ids.add(aLoadout.getUpgrades().getArmor().getMwoId());
         ids.add(aLoadout.getUpgrades().getStructure().getMwoId());
         ids.add(aLoadout.getUpgrades().getHeatSink().getMwoId());
         ids.add(aLoadout.getUpgrades().getGuidance().getMwoId());

         ids.add(-1);

         for(Location part : partOrder){
            List<Item> items = aLoadout.getPart(part).getItems();
            for(Item item : items){
               if( !(item instanceof Internal) ){
                  ids.add(item.getMwoId());
               }
            }
            ids.add(-1);
         }

         // TODO: add pilot modules

         // Encode the list with huffman
         byte[] data = huff.encode(ids);
         try{
            buffer.write(data);
         }
         catch( IOException e ){
            throw new EncodingException(e);
         }
      }
      return buffer.toByteArray();
   }

   @Override
   public Loadout decode(final byte[] aBitStream) throws DecodingException{
      final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
      final Loadout loadout;
      final OperationStack stack = new OperationStack(0);

      // Read header
      {
         if( buffer.read() != HEADER_MAGIC ){
            throw new DecodingException("Wrong format!"); // Wrong format
         }

         int upeff = buffer.read() & 0xFF; // 8 bits for efficiencies and
         // 16 bits contain chassis ID (Big endian, respecting RFC 1700)
         short chassiId = (short)(((buffer.read() & 0xFF) << 8) | (buffer.read() & 0xFF));

         Chassis chassi = ChassisDB.lookup(chassiId);
         loadout = new Loadout(chassi, xBar);
         loadout.getEfficiencies().setCoolRun((upeff & (1 << 4)) != 0);
         loadout.getEfficiencies().setHeatContainment((upeff & (1 << 3)) != 0);
         loadout.getEfficiencies().setSpeedTweak((upeff & (1 << 2)) != 0);
         loadout.getEfficiencies().setDoubleBasics((upeff & (1 << 1)) != 0);
         loadout.getEfficiencies().setFastFire((upeff & (1 << 0)) != 0);
      }

      // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
      // 1 byte per armor value (2 for RT,CT,LT front first)
      for(Location part : partOrder){
         if( part.isTwoSided() ){
            stack.pushAndApply(new OpSetArmor(xBar, loadout.getPart(part), ArmorSide.FRONT, buffer.read(), true));
            stack.pushAndApply(new OpSetArmor(xBar, loadout.getPart(part), ArmorSide.BACK, buffer.read(), true));
         }
         else{
            stack.pushAndApply(new OpSetArmor(xBar, loadout.getPart(part), ArmorSide.ONLY, buffer.read(), true));
         }
      }

      // Items are encoded as a list of integers which record the item ID. Components are separated by -1.
      // The order is the same as for armor: RA, RT, RL, HD, CT, LT, LL, LA
      {
         byte[] rest = new byte[buffer.available()];
         try{
            buffer.read(rest);
         }
         catch( IOException e ){
            throw new DecodingException(e);
         }
         List<Integer> ids = huff.decode(rest);
         stack.pushAndApply(new OpSetArmorType(xBar, loadout, (ArmorUpgrade)UpgradeDB.lookup(ids.get(0))));
         stack.pushAndApply(new OpSetStructureType(xBar, loadout, (StructureUpgrade)UpgradeDB.lookup(ids.get(1))));
         stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, (HeatSinkUpgrade)UpgradeDB.lookup(ids.get(2))));
         stack.pushAndApply(new OpSetGuidanceType(xBar, loadout, (GuidanceUpgrade)UpgradeDB.lookup(ids.get(3))));

         if( -1 != ids.get(4) ){
            throw new DecodingException("Broken LSML link, expected separator got: " + ids.get(4));
         }
         ids.remove(4);
         ids.remove(3);
         ids.remove(2);
         ids.remove(1);
         ids.remove(0);

         for(Location part : partOrder){
            Integer v;
            List<Item> later = new ArrayList<>();
            while( !ids.isEmpty() && -1 != (v = ids.remove(0)) ){
               Item item = ItemDB.lookup(v);
               if( item instanceof HeatSink ){
                  later.add(item); // Add heat sinks last after engine has been added
                  continue;
               }
               stack.pushAndApply(new OpAddItem(xBar, loadout.getPart(part), ItemDB.lookup(v)));
            }
            for(Item i : later){
               stack.pushAndApply(new OpAddItem(xBar, loadout.getPart(part), i));
            }

         }

         // TODO: read pilot modules
      }
      return loadout;
   }

   @Override
   public boolean canDecode(byte[] aBitStream){
      final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
      return buffer.read() == HEADER_MAGIC;
   }

   /**
    * Will process the stock builds and generate statistics and dump it to a file.
    * 
    * @param arg
    * @throws Exception
    */
   public static void main(String[] arg) throws Exception{
      // generateAllLoadouts();
      // generateStatsFromStdin();
   }

   @SuppressWarnings("unused")
   private static void generateAllLoadouts() throws Exception{
      List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
      chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
      chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
      chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
      MessageXBar xBar = new MessageXBar();
      Base64LoadoutCoder coder = new Base64LoadoutCoder(xBar);
      OperationStack stack = new OperationStack(0);
      for(Chassis chassis : chassii){
         Loadout loadout = new Loadout(chassis, xBar);
         stack.pushAndApply(new OpLoadStock(chassis, loadout, xBar));
         System.out.println("[" + chassis.getName() + "]=" + coder.encodeLSML(loadout));
      }
   }

   @SuppressWarnings("unused")
   private static void generateStatsFromStdin() throws Exception{
      Scanner sc = new Scanner(System.in);

      int numLoadouts = Integer.parseInt(sc.nextLine());

      Map<Integer, Integer> freqs = new TreeMap<>();
      String line = sc.nextLine();
      do{
         String[] s = line.split(" ");
         int id = Integer.parseInt(s[0]);
         int freq = Integer.parseInt(s[1]);
         freqs.put(id, freq);
         line = sc.nextLine();
      }
      while( !line.contains("q") );

      // Make sure all items are in the statistics even if they have a very low probability
      for(Item item : ItemDB.lookup(Item.class)){
         int id = item.getMwoId();
         if( !freqs.containsKey(id) )
            freqs.put(id, 1);
      }

      freqs.put(-1, numLoadouts * 9); // 9 separators per loadout
      freqs.put(UpgradeDB.STANDARD_ARMOR.getMwoId(), numLoadouts * 7 / 10); // Standard armor
      freqs.put(UpgradeDB.FERRO_FIBROUS_ARMOR.getMwoId(), numLoadouts * 3 / 10); // Ferro Fibrous Armor
      freqs.put(UpgradeDB.STANDARD_STRUCTURE.getMwoId(), numLoadouts * 3 / 10); // Standard structure
      freqs.put(UpgradeDB.ENDO_STEEL_STRUCTURE.getMwoId(), numLoadouts * 7 / 10); // Endo-Steel
      freqs.put(UpgradeDB.STANDARD_HEATSINKS.getMwoId(), numLoadouts * 1 / 20); // SHS
      freqs.put(UpgradeDB.DOUBLE_HEATSINKS.getMwoId(), numLoadouts * 19 / 20); // DHS
      freqs.put(UpgradeDB.STANDARD_GUIDANCE.getMwoId(), numLoadouts * 7 / 10); // No Artemis
      freqs.put(UpgradeDB.ARTEMIS_IV.getMwoId(), numLoadouts * 3 / 10); // Artemis IV

      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("resources/resources/coderstats_v2.bin"));
      out.writeObject(freqs);
      out.close();
      sc.close();
   }
}
