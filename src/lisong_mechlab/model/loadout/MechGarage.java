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

/**
 * This class is a serialisable collection of {@link Loadout}s, known as a {@link MechGarage}.
 * 
 * @author Emily Björk
 */
public class MechGarage{
   /**
    * This class implements {@link UndoAction}s for the {@link MechGarage}.
    * 
    * @author Emily Björk
    */
   static class GarageUndoAction implements UndoAction{
      private enum Action{
         AddLoadout, RemoveLoadout
      }

      private final Action     action;
      private final Loadout    loadout;
      private final String     description;
      private final MechGarage garage;

      private GarageUndoAction(Action anAction, Loadout aLoadout, MechGarage aGarage){
         action = anAction;
         loadout = aLoadout;
         garage = aGarage;
         if( action == Action.AddLoadout )
            description = "Undo add " + aLoadout.getName() + " to garage.";
         else
            description = "Undo remove " + aLoadout.getName() + " from garage.";
      }

      @Override
      public String describe(){
         return description;
      }

      @Override
      public void undo(){
         if( action == Action.AddLoadout ){
            garage.remove(loadout, false);
         }
         else{
            garage.add(loadout, false);
         }
      }

      /**
       * Undo actions don't affect the loadout per say, they affect the garage.
       */
      @Override
      public boolean affects(Loadout aLoadout){
         return false;
      }
   }

   /**
    * This class implements {@link MessageXBar.Message}s for the {@link MechGarage} so that other components can react
    * to changes in the garage.
    * 
    * @author Emily Björk
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
   private transient UndoStack   undoStack;

   /**
    * Creates a new, empty {@link MechGarage}.
    * 
    * @param aXBar
    *           The {@link MessageXBar} to signal changes to this garage on.
    * @param anUndoStack
    *           The {@link UndoStack} to add {@link UndoAction}s for this garage to.
    */
   public MechGarage(MessageXBar aXBar, UndoStack anUndoStack){
      xBar = aXBar;
      xBar.post(new Message(Message.Type.NewGarage, this));
      undoStack = anUndoStack;
   }

   /**
    * Creates a new {@link MechGarage} from an XML file with existing garage contents.
    * 
    * @param aFile
    *           The {@link File} to read from.
    * @param aXBar
    *           The {@link MessageXBar} to signal changes to the garage on.
    * @param anUndoStack
    *           The {@link UndoStack} to add {@link UndoAction}s for the garage to.
    * @return A new {@link MechGarage} containing the {@link Loadout}s found in <code>aFile</code>.
    * @throws IOException
    *            Thrown if there was an error reading the garage file.
    */
   public static MechGarage open(File aFile, MessageXBar aXBar, UndoStack anUndoStack) throws IOException{
      if( aFile.isFile() && aFile.length() < 50 ){
         throw new IOException("The file is too small to be a garage file!");
      }

      FileInputStream fis = null;
      MechGarage mg = null;
      try{
         fis = new FileInputStream(aFile);
         mg = (MechGarage)garageXstream(aXBar, anUndoStack).fromXML(fis);
      }
      finally{
         if( null != fis )
            fis.close();
      }
      mg.file = aFile;
      mg.xBar = aXBar;
      mg.undoStack = anUndoStack;
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

      FileWriter fileWriter = null;
      try{
         fileWriter = new FileWriter(aFile);
         fileWriter.write(garageXstream(xBar, undoStack).toXML(this));
         file = aFile;
      }
      finally{
         if( fileWriter != null ){
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
    * Adds a new {@link Loadout} to this garage. This will submit an {@link UndoAction} to the {@link UndoStack} given
    * in the constructor so that the action can be undone.
    * 
    * @param aLoadout
    *           The {@link Loadout} to add.
    * @param isUndoable
    *           If <code>true</code>, is invocation will generate an {@link UndoAction} to the {@link UndoStack} given
    *           in the constructor.
    */
   public void add(Loadout aLoadout, boolean isUndoable){
      if( mechs.contains(aLoadout) ){
         throw new IllegalArgumentException("The loadout \"" + aLoadout.getName() + "\" is already saved to the garage!");
      }
      mechs.add(aLoadout);
      xBar.post(new Message(Message.Type.LoadoutAdded, this, aLoadout));
      if( isUndoable )
         undoStack.pushAction(new GarageUndoAction(GarageUndoAction.Action.AddLoadout, aLoadout, this));
   }

   /**
    * Removes the given {@link Loadout} from the garage. This will submit an {@link UndoAction} to the {@link UndoStack}
    * given in the constructor so that the action can be undone.
    * 
    * @param aLoadout
    *           The {@link Loadout} to be removed.
    * @param isUndoable
    *           If <code>true</code>, is invocation will generate an {@link UndoAction} to the {@link UndoStack} given
    *           in the constructor.
    */
   public void remove(Loadout aLoadout, boolean isUndoable){
      if( mechs.remove(aLoadout) ){
         xBar.post(new Message(Message.Type.LoadoutRemoved, this, aLoadout));
         if( isUndoable )
            undoStack.pushAction(new GarageUndoAction(GarageUndoAction.Action.RemoveLoadout, aLoadout, this));
      }
   }

   /**
    * Private helper method for the {@link XStream} serialization.
    * 
    * @param anUndoStack
    * @param crossBar
    *           The {@link MessageXBar} to use for any {@link Loadout}s loaded from files.
    * @return An {@link XStream} object usable for deserialization of garages.
    */
   private static XStream garageXstream(MessageXBar anXBar, UndoStack anUndoStack){
      XStream stream = Loadout.loadoutXstream(anXBar, anUndoStack);
      stream.alias("garage", MechGarage.class);
      stream.omitField(MechGarage.class, "file");
      return stream;
   }
}
