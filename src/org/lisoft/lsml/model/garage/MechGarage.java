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
package org.lisoft.lsml.model.garage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;

import com.thoughtworks.xstream.XStream;

/**
 * This class is a serialisable collection of {@link LoadoutStandard}s, known as a {@link MechGarage}.
 * 
 * @author Emily Björk
 */
public class MechGarage {
    /**
     * This class implements {@link org.lisoft.lsml.util.message.Message}s for the {@link MechGarage} so that other
     * components can react to changes in the garage.
     * 
     * @author Emily Björk
     */
    public static class GarageMessage implements Message {
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((garage == null) ? 0 : garage.hashCode());
            result = prime * result + ((loadout == null) ? 0 : loadout.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GarageMessage) {
                GarageMessage that = (GarageMessage) obj;
                return this.garage == that.garage && this.type == that.type && this.loadout == that.loadout;
            }
            return false;
        }

        public enum Type {
            LoadoutAdded, LoadoutRemoved, NewGarage, Saved
        }

        public final Type            type;
        public final MechGarage      garage;
        private final LoadoutBase<?> loadout;

        public GarageMessage(Type aType, MechGarage aGarage, LoadoutBase<?> aLoadout) {
            type = aType;
            garage = aGarage;
            loadout = aLoadout;
        }

        public GarageMessage(Type aType, MechGarage aGarage) {
            this(aType, aGarage, null);
        }

        @Override
        public boolean isForMe(LoadoutBase<?> aLoadout) {
            return aLoadout == loadout;
        }

        @Override
        public boolean affectsHeatOrDamage() {
            return false;
        }
    }

    private final List<LoadoutBase<?>> mechs = new ArrayList<>();
    private File                       file;
    private transient MessageXBar      xBar;

    /**
     * Creates a new, empty {@link MechGarage}.
     * 
     * @param aXBar
     *            The {@link MessageXBar} to signal changes to this garage on.
     */
    public MechGarage(MessageXBar aXBar) {
        xBar = aXBar;
        xBar.post(new GarageMessage(GarageMessage.Type.NewGarage, this));
    }

    /**
     * Creates a new {@link MechGarage} from an XML file with existing garage contents. In the case of corrupt loadouts
     * as much as possible is loaded and errors are passed along as the second parameter to the pair.
     * 
     * @param aFile
     *            The {@link File} to read from.
     * @param aXBar
     *            The {@link MessageXBar} to signal changes to the garage on.
     * @return A new {@link MechGarage} containing the {@link LoadoutStandard}s found in <code>aFile</code>.
     * @throws IOException
     *             Thrown if there was an error reading the garage file.
     */
    public static MechGarage open(File aFile, MessageXBar aXBar) throws IOException {
        if (aFile.isFile() && aFile.length() < 50) {
            throw new IOException("The file is too small to be a garage file!");
        }

        MechGarage mg = null;
        try (FileInputStream fis = new FileInputStream(aFile)) {
            mg = (MechGarage) garageXstream().fromXML(fis);
        }
        mg.file = aFile;
        mg.xBar = aXBar;
        mg.xBar.post(new GarageMessage(GarageMessage.Type.NewGarage, mg));
        return mg;
    }

    /**
     * Saves this garage, overwriting the file it was previously saved to (or opened from).
     * 
     * @throws IOException
     *             Thrown if this garage has not been saveas:ed previously.
     */
    public final void save() throws IOException {
        saveas(file, true);
    }

    /**
     * Saves this garage to the given file without overwriting.
     * 
     * @param aFile
     *            The {@link File} to write to.
     * @throws IOException
     *             Thrown if the file already existed or could not be written to.
     */
    public final void saveas(File aFile) throws IOException {
        saveas(aFile, false);
    }

    /**
     * Saves this garage to the given file, optionally overwriting any previously existing file.
     * 
     * @param aFile
     *            The {@link File} to write to.
     * @param flagOverwrite
     *            If <code>true</code>, will overwrite any existing file with the same name.
     * @throws IOException
     *             Thrown if <code>flagOverwrite</code> is false and a file with the given name already exists or there
     *             was another error while writing the file.
     */
    public void saveas(File aFile, boolean flagOverwrite) throws IOException {
        if (aFile == null) {
            throw new IOException("No file given to save to!");
        }

        if (aFile.exists() && !flagOverwrite) {
            throw new IOException("File already exists!");
        }

        FileOutputStream fileWriter = null;
        OutputStreamWriter writer = null;
        try {
            fileWriter = new FileOutputStream(aFile);
            writer = new OutputStreamWriter(fileWriter, "UTF-8");
            writer.write(garageXstream().toXML(this));
            file = aFile;
        }
        finally {
            if (writer != null) {
                writer.close();
            }
            else if (fileWriter != null) {
                fileWriter.close();
            }
        }
        xBar.post(new GarageMessage(GarageMessage.Type.Saved, this));
    }

    /**
     * @return An unmodifiable list of all the {@link LoadoutStandard}s in this garage.
     */
    public List<LoadoutBase<?>> getMechs() {
        return Collections.unmodifiableList(mechs);
    }

    /**
     * @return The {@link File} this garage was opened from or last saved to.
     */
    public File getFile() {
        return file;
    }

    /**
     * Adds a new {@link LoadoutStandard} to this garage. This will submit an {@link Command} to the
     * {@link CommandStack} given in the constructor so that the action can be undone.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to add.
     */
    public void add(LoadoutBase<?> aLoadout) {
        mechs.add(aLoadout);
        xBar.post(new GarageMessage(GarageMessage.Type.LoadoutAdded, MechGarage.this, aLoadout));
    }

    /**
     * Removes the given {@link LoadoutStandard} from the garage. This will submit an {@link Command} to the
     * {@link CommandStack} given in the constructor so that the action can be undone.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to remove.
     */
    public void remove(LoadoutBase<?> aLoadout) {
        if (mechs.remove(aLoadout)) {
            xBar.post(new GarageMessage(GarageMessage.Type.LoadoutRemoved, MechGarage.this, aLoadout));
        }
    }

    /**
     * Private helper method for the {@link XStream} serialization.
     * 
     * @return An {@link XStream} object usable for deserialization of garages.
     */
    private static XStream garageXstream() {
        XStream stream = LoadoutBase.loadoutXstream();
        stream.alias("garage", MechGarage.class);
        stream.omitField(MechGarage.class, "file");
        stream.alias("loadout", LoadoutOmniMech.class);
        stream.alias("loadout", LoadoutStandard.class);
        return stream;
    }
}
