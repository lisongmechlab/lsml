package lisong_mechlab.model.loadout;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;

/**
 * A first simple implementation of a {@link LoadoutCoder}.
 * 
 * @author Li Song
 */
public class LoadoutCoderV1 implements LoadoutCoder{

   /**
    * Will process the stock builds and generate statistics
    * 
    * @param arg
    */
   public static void main(String[] arg){
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));
   }

   @Override
   public byte[] encode(Loadout aLoadout){
      ByteBuffer buffer = ByteBuffer.allocate(100);

      // Write header
      {
         // 32 bits to encode coder version, upgrades and efficiencies
         int header = 0xAC << 24; // 8 bits for version number

         int upeff = 0; // 8 bits for efficiencies
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasArtemis() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasDoubleHeatSinks() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasFerroFibrous() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getUpgrades().hasEndoSteel() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasCoolRun() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasHeatContainment() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasSpeedTweak() ? 1 : 0);
         upeff = (upeff << 1) | (aLoadout.getEfficiencies().hasDoubleBasics() ? 1 : 0);
         header = header | (upeff << 16);

         // 16 low bits unused
         buffer.putInt(header);
      }
      
      // Armor values next, RA, RT, RL, HD, CT, LT, LL, LA
      // 1 byte per armor value (2 for RT,CT,LT front first)
      {
         buffer.put((byte)aLoadout.getPart(Part.RightArm).getArmor(ArmorSide.ONLY));
         buffer.put((byte)aLoadout.getPart(Part.RightTorso).getArmor(ArmorSide.FRONT));
         buffer.put((byte)aLoadout.getPart(Part.RightTorso).getArmor(ArmorSide.BACK));
         buffer.put((byte)aLoadout.getPart(Part.RightLeg).getArmor(ArmorSide.ONLY));
         
         buffer.put((byte)aLoadout.getPart(Part.Head).getArmor(ArmorSide.ONLY));
         buffer.put((byte)aLoadout.getPart(Part.CenterTorso).getArmor(ArmorSide.FRONT));
         buffer.put((byte)aLoadout.getPart(Part.CenterTorso).getArmor(ArmorSide.BACK));
         
         buffer.put((byte)aLoadout.getPart(Part.LeftTorso).getArmor(ArmorSide.FRONT));
         buffer.put((byte)aLoadout.getPart(Part.LeftTorso).getArmor(ArmorSide.BACK));
         buffer.put((byte)aLoadout.getPart(Part.LeftLeg).getArmor(ArmorSide.ONLY));
         buffer.put((byte)aLoadout.getPart(Part.LeftArm).getArmor(ArmorSide.ONLY));         
      }
      
      return null;
   }

   @Override
   public Loadout decode(byte[] aBitStream) throws IllegalArgumentException{
      // TODO Auto-generated method stub
      return null;
   }

}
