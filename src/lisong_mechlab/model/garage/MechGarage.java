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
package lisong_mechlab.model.garage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

import com.thoughtworks.xstream.XStream;

/**
 * This class is a serialisable collection of {@link Loadout}s, known as a {@link MechGarage}.
 * 
 * @author Li Song
 */
public class MechGarage{
   /**
    * This class implements {@link lisong_mechlab.util.MessageXBar.Message}s for the {@link MechGarage} so that other
    * components can react to changes in the garage.
    * 
    * @author Li Song
    */
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
   private File                  file;
   private transient MessageXBar xBar;

   /**
    * Creates a new, empty {@link MechGarage}.
    * 
    * @param aXBar
    *           The {@link MessageXBar} to signal changes to this garage on.
    */
   public MechGarage(MessageXBar aXBar){
      xBar = aXBar;
      xBar.post(new Message(Message.Type.NewGarage, this));
   }

   /**
    * Creates a new {@link MechGarage} from an XML file with existing garage contents.
    * 
    * @param aFile
    *           The {@link File} to read from.
    * @param aXBar
    *           The {@link MessageXBar} to signal changes to the garage on.
    * @return A new {@link MechGarage} containing the {@link Loadout}s found in <code>aFile</code>.
    * @throws IOException
    *            Thrown if there was an error reading the garage file.
    */
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

   /**
    * Saves this garage, overwriting the file it was previously saved to (or opened from).
    * 
    * @throws IOException
    *            Thrown if this garage has not been saveas:ed previously.
    */
   public final void save() throws IOException{
      saveas(file, true);
   }

   /**
    * Saves this garage to the given file without overwriting.
    * 
    * @param aFile
    *           The {@link File} to write to.
    * @throws IOException
    *            Thrown if the file already existed or could not be written to.
    */
   public final void saveas(File aFile) throws IOException{
      saveas(aFile, false);
   }

   /**
    * Saves this garage to the given file, optionally overwriting any previously existing file.
    * 
    * @param aFile
    *           The {@link File} to write to.
    * @param flagOverwrite
    *           If <code>true</code>, will overwrite any existing file with the same name.
    * @throws IOException
    *            Thrown if <code>flagOverwrite</code> is false and a file with the given name already exists or there
    *            was another error while writing the file.
    */
   public void saveas(File aFile, boolean flagOverwrite) throws IOException{
      if( aFile == null ){
         throw new IOException("No file given to save to!");
      }

      if( aFile.exists() && !flagOverwrite ){
         throw new IOException("File already exists!");
      }

      FileOutputStream fileWriter = null;
      OutputStreamWriter writer = null;
      try{
         fileWriter = new FileOutputStream(aFile);
         writer = new OutputStreamWriter(fileWriter, "UTF-8");
         writer.write(garageXstream(xBar).toXML(this));
         file = aFile;
      }
      finally{
         if( writer != null ){
            writer.close();
         }
         else if( fileWriter != null ){
            fileWriter.close();
         }
      }
      xBar.post(new Message(Message.Type.Saved, this));
   }

   /**
    * @return An unmodifiable list of all the {@link Loadout}s in this garage.
    */
   public List<Loadout> getMechs(){
      return Collections.unmodifiableList(mechs);
   }

   /**
    * @return The {@link File} this garage was opened from or last saved to.
    */
   public File getFile(){
      return file;
   }

   /**
    * Adds a new {@link Loadout} to this garage. This will submit an {@link Operation} to the {@link OperationStack}
    * given in the constructor so that the action can be undone.
    * 
    * @param aLoadout
    *           The {@link Loadout} to add.
    */
   void add(Loadout aLoadout){
      mechs.add(aLoadout);
      xBar.post(new Message(Message.Type.LoadoutAdded, MechGarage.this, aLoadout));
   }

   /**
    * Removes the given {@link Loadout} from the garage. This will submit an {@link Operation} to the
    * {@link OperationStack} given in the constructor so that the action can be undone.
    * 
    * @param aLoadout
    *           The {@link Loadout} to remove.
    */
   void remove(Loadout aLoadout){
      if( mechs.remove(aLoadout) ){
         xBar.post(new Message(Message.Type.LoadoutRemoved, MechGarage.this, aLoadout));
      }
   }

   /**
    * Private helper method for the {@link XStream} serialization.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to use for any {@link Loadout}s loaded from files.
    * @return An {@link XStream} object usable for deserialization of garages.
    */
   private static XStream garageXstream(MessageXBar anXBar){
      XStream stream = Loadout.loadoutXstream(anXBar);
      stream.alias("garage", MechGarage.class);
      stream.omitField(MechGarage.class, "file");
      return stream;
   }
}
