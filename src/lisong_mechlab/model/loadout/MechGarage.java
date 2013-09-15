package lisong_mechlab.model.loadout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.util.MessageXBar;

import com.thoughtworks.xstream.XStream;

public class MechGarage{
   public static class Message implements MessageXBar.Message{
      @Override
      public int hashCode(){
         final int prime = 31;
         int result = 1;
         result = prime * result + ((garage == null) ? 0 : garage.hashCode());
         result = prime * result + ((loadout == null) ? 0 : loadout.hashCode());
         result = prime * result + ((type == null) ? 0 : type.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message that = (Message)obj;
            return this.garage == that.garage && this.type == that.type && this.loadout == that.loadout;
         }
         return false;
      }

      public enum Type{
         LoadoutAdded, LoadoutRemoved, NewGarage, Saved
      }

      public final Type       type;
      public final MechGarage garage;
      private final Loadout   loadout;

      public Message(Type aType, MechGarage aGarage, Loadout aLoadout){
         type = aType;
         garage = aGarage;
         loadout = aLoadout;
      }

      public Message(Type aType, MechGarage aGarage){
         this(aType, aGarage, null);
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout == loadout;
      }
   }

   private final List<Loadout>   mechs = new ArrayList<Loadout>();
   private File                  file  = null;
   private transient MessageXBar xBar  = null;

   public MechGarage(MessageXBar aXBar){
      xBar = aXBar;
      xBar.post(new Message(Message.Type.NewGarage, this));
   }

   public static MechGarage open(File aFile, MessageXBar aXBar) throws IOException{
      if( aFile.isFile() && aFile.length() < 50 ){
         throw new IOException("The file is too small to be a garage file!");
      }

      FileInputStream fis = null;
      MechGarage mg = null;
      try{
         fis = new FileInputStream(aFile);
         mg = (MechGarage)garageXstream(aXBar).fromXML(fis);
      }
      finally{
         if( null != fis )
            fis.close();
      }
      mg.file = aFile;
      mg.xBar = aXBar;
      mg.xBar.post(new Message(Message.Type.NewGarage, mg));
      return mg;
   }

   public final void save() throws IOException{
      saveas(file, true);
   }

   public final void saveas(File aFile) throws IOException{
      saveas(aFile, false);
   }

   public void saveas(File aFile, boolean flagOverwrite) throws IOException{
      if( aFile == null ){
         throw new IOException("No file given to save to!");
      }

      if( aFile.exists() && !flagOverwrite ){
         throw new IOException("File already exists!");
      }

      FileWriter fileWriter = null;
      try{
         fileWriter = new FileWriter(aFile);
         fileWriter.write(garageXstream(xBar).toXML(this));
         file = aFile;
      }
      finally{
         if( fileWriter != null ){
            fileWriter.close();
         }
      }
      xBar.post(new Message(Message.Type.Saved, this));
   }

   public List<Loadout> getMechs(){
      return Collections.unmodifiableList(mechs);
   }

   public File getFile(){
      return file;
   }

   public void add(Loadout aLoadout){
      if( mechs.contains(aLoadout) ){
         throw new IllegalArgumentException("The loadout \"" + aLoadout.getName() + "\" is already saved to the garage!");
      }
      mechs.add(aLoadout);
      xBar.post(new Message(Message.Type.LoadoutAdded, this, aLoadout));
   }

   public void remove(Loadout aLoadout){
      mechs.remove(aLoadout);
      xBar.post(new Message(Message.Type.LoadoutRemoved, this, aLoadout));
   }

   static XStream garageXstream(MessageXBar crossBar){
      XStream stream = Loadout.loadoutXstream(crossBar);
      stream.alias("garage", MechGarage.class);
      stream.omitField(MechGarage.class, "file");
      return stream;
   }
}
