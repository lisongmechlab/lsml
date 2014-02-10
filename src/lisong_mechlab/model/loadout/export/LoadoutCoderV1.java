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
import lisong_mechlab.model.loadout.LoadStockOperation;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatsinkUpgrade;
import lisong_mechlab.model.upgrades.SetArmorTypeOperation;
import lisong_mechlab.model.upgrades.SetDHSOperation;
import lisong_mechlab.model.upgrades.SetEndoSteelOperation;
import lisong_mechlab.model.upgrades.SetGuidanceOperation;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.EncodingException;
import lisong_mechlab.util.Huffman1;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

/**
 * The first version of {@link LoadoutCoder} for LSML.
 * 
 * @author Emily Björk
 */
public class LoadoutCoderV1 implements LoadoutCoder{
   private static final int        HEADER_MAGIC = 0xAC;
   private final Huffman1<Integer> huff;
   private final MessageXBar       xBar;
   private final Part[]            partOrder    = new Part[] {Part.RightArm, Part.RightTorso, Part.RightLeg, Part.Head, Part.CenterTorso,
         Part.LeftTorso, Part.LeftLeg, Part.LeftArm};

   public LoadoutCoderV1(MessageXBar anXBar){
      xBar = anXBar;
      ObjectInputStream in = null;
      try{
         InputStream is = LoadoutCoderV1.class.getResourceAsStream("/resources/coderstats.bin");
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
   public byte[] encode(final Loadout aLoadout) throws EncodingException{
      // FIXME
      throw new EncodingException("Protocol version 1 encoding is no longer allowed. Please update to 1.3.3 when it is out");
      //@formatter:off
      /*final ByteArrayOutputStream buffer = new ByteArrayOutputStream(100);

      // Write header (32 bits)
      {
         buffer.write(HEADER_MAGIC); // 8 bits for version number

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
         byte[] data = huff.encode(ids);
         try{
            buffer.write(data);
         }
         catch( IOException e ){
            throw new EncodingException(e);
         }
      }
      return buffer.toByteArray();
      */
      //@formatter:on
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
         // 16 bits contain chassi ID.
         short chassiId = (short)(((buffer.read() & 0xFF) << 8) | (buffer.read() & 0xFF)); // Big endian, respecting RFC
                                                                                           // 1700

         Chassi chassi = ChassiDB.lookup(chassiId);
         loadout = new Loadout(chassi, xBar);

         boolean artemisIv = (upeff & (1 << 7)) != 0;
         boolean endoSteel = (upeff & (1 << 4)) != 0;
         boolean ferroFib = (upeff & (1 << 5)) != 0;
         boolean dhs = (upeff & (1 << 6)) != 0;
         GuidanceUpgrade guidance = artemisIv ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STANDARD_GUIDANCE;
         StructureUpgrade structure = endoSteel ? UpgradeDB.ENDO_STEEL_STRUCTURE : UpgradeDB.STANDARD_STRUCTURE;
         ArmorUpgrade armor = ferroFib ? UpgradeDB.FERRO_FIBROUS_ARMOR : UpgradeDB.STANDARD_ARMOR;
         HeatsinkUpgrade heatSinks = dhs ? UpgradeDB.DOUBLE_HEATSINKS : UpgradeDB.STANDARD_HEATSINKS;

         stack.pushAndApply(new SetGuidanceOperation(xBar, loadout, guidance));
         stack.pushAndApply(new SetDHSOperation(xBar, loadout, heatSinks));
         stack.pushAndApply(new SetEndoSteelOperation(xBar, loadout, structure));
         stack.pushAndApply(new SetArmorTypeOperation(xBar, loadout, armor));
         loadout.getEfficiencies().setCoolRun((upeff & (1 << 3)) != 0);
         loadout.getEfficiencies().setHeatContainment((upeff & (1 << 2)) != 0);
         loadout.getEfficiencies().setSpeedTweak((upeff & (1 << 1)) != 0);
         loadout.getEfficiencies().setDoubleBasics((upeff & (1 << 0)) != 0);
      }

      // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
      // 1 byte per armor value (2 for RT,CT,LT front first)
      for(Part part : partOrder){
         if( part.isTwoSided() ){
            stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(part), ArmorSide.FRONT, buffer.read()));
            stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(part), ArmorSide.BACK, buffer.read()));
         }
         else{
            stack.pushAndApply(new SetArmorOperation(xBar, loadout.getPart(part), ArmorSide.ONLY, buffer.read()));
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
         for(Part part : partOrder){
            Integer v;
            while( !ids.isEmpty() && -1 != (v = ids.remove(0)) ){
               Item pItem = ItemDB.lookup(v);
               Item item = CompatibilityHelper.fixArtemis(pItem, loadout.getUpgrades().getGuidance());
               stack.pushAndApply(new AddItemOperation(xBar, loadout.getPart(part), item));
            }
         }
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
      // generateStatsFromStock();
   }

   @SuppressWarnings("unused")
   private static void generateAllLoadouts() throws Exception{
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));
      MessageXBar xBar = new MessageXBar();
      OperationStack stack = new OperationStack(0);

      Base64LoadoutCoder coder = new Base64LoadoutCoder(xBar);

      for(Chassi chassi : chassii){
         Loadout loadout = new Loadout(chassi.getName(), xBar);
         stack.pushAndApply(new LoadStockOperation(loadout, xBar));

         for(LoadoutPart part : loadout.getPartLoadOuts()){
            for(Item item : new ArrayList<>(part.getItems())){
               if( item.getName().toLowerCase().contains("artemis") ){
                  stack.pushAndApply(new RemoveItemOperation(xBar, part, item));
                  stack.pushAndApply(new AddItemOperation(xBar, part,
                                                          ItemDB.lookup(item.getName().substring(0, item.getName().indexOf(" + ARTEMIS")))));
               }
            }
         }

         System.out.println("[" + chassi.getName() + "]=" + coder.encodeLSML(loadout));
      }
   }

   @SuppressWarnings("unused")
   private static void generateStatsFromStock() throws Exception{
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));

      Map<Integer, Integer> freqs = new TreeMap<>();
      MessageXBar anXBar = new MessageXBar();
      for(Chassi chassi : chassii){
         Loadout loadout = new Loadout(chassi.getName(), anXBar);

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
