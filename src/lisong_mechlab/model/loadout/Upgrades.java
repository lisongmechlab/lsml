package lisong_mechlab.model.loadout;

import lisong_mechlab.model.MessageXBar;

public class Upgrades{
   private boolean                     artemis;
   private boolean                     ferroFibrous;
   private boolean                     endoSteel;
   private boolean                     dhs;

   private transient final MessageXBar xBar;

   public static class Message implements MessageXBar.Message{
      public final ChangeMsg msg;
      public final Upgrades  source;

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

   public enum ChangeMsg{
      GUIDANCE, STRUCTURE, ARMOR, HEATSINKS
   }

   public Upgrades(MessageXBar anXBar){
      xBar = anXBar;
   }

   public boolean hasArtemis(){
      return artemis;
   }

   public boolean hasDoubleHeatSinks(){
      return dhs;
   }

   public boolean hasEndoSteel(){
      return endoSteel;
   }

   public boolean hasFerroFibrous(){
      return ferroFibrous;
   }

   public void setArtemis(boolean anArtemis){
      if( anArtemis != artemis ){
         artemis = anArtemis;
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.GUIDANCE, this));
      }
   }

   public void setDoubleHeatSinks(boolean aDHS){
      if( aDHS != dhs ){
         dhs = aDHS;
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.HEATSINKS, this));
      }
   }

   public void setEndoSteel(boolean anEndoSteel){
      if( anEndoSteel != endoSteel ){
         endoSteel = anEndoSteel;
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.STRUCTURE, this));
      }
   }

   public void setFerroFibrous(boolean aFerroFibrous){
      if( aFerroFibrous != ferroFibrous ){
         ferroFibrous = aFerroFibrous;
         if( xBar != null )
            xBar.post(new Message(ChangeMsg.ARMOR, this));
      }
   }
}
