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
package org.lisoft.lsml.model.garage;

import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.export.garage.ChassiConverter;
import org.lisoft.lsml.model.export.garage.ConfiguredComponentConverter;
import org.lisoft.lsml.model.export.garage.EfficienciesConverter;
import org.lisoft.lsml.model.export.garage.GarageConverter;
import org.lisoft.lsml.model.export.garage.ItemConverter;
import org.lisoft.lsml.model.export.garage.LoadoutConverter;
import org.lisoft.lsml.model.export.garage.ModuleConverter;
import org.lisoft.lsml.model.export.garage.UpgradeConverter;
import org.lisoft.lsml.model.export.garage.UpgradesConverter;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponentStandard;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

import com.thoughtworks.xstream.XStream;

/**
 * This class is used for loading and writing garage files from/to disk.
 *
 * @author Li Song
 */
public class GarageSerialiser {

    private static XStream garageXstream(ErrorReporter aErrorReporter, LoadoutFactory aLoadoutFactory) {
        final XStream stream = new XStream();
        stream.autodetectAnnotations(true);
        stream.processAnnotations(Garage.class);
        stream.processAnnotations(LoadoutOmniMech.class);
        stream.processAnnotations(LoadoutStandard.class);
        stream.setMode(XStream.NO_REFERENCES);
        stream.registerConverter(new ChassiConverter());
        stream.registerConverter(new ItemConverter());
        stream.registerConverter(new ModuleConverter());
        stream.registerConverter(new ConfiguredComponentConverter(null, null));
        stream.registerConverter(new LoadoutConverter(aErrorReporter, aLoadoutFactory));
        stream.registerConverter(new UpgradeConverter());
        stream.registerConverter(new UpgradesConverter());
        stream.registerConverter(new EfficienciesConverter());
        stream.registerConverter(new GarageConverter(stream.getMapper(), stream.getReflectionProvider()));
        stream.addImmutableType(Item.class, true);
        stream.alias("component", ConfiguredComponentStandard.class);
        stream.ignoreUnknownElements(".*firingMode*");
        return stream;
    }

    private final ErrorReporter errorReporter;
    private final LoadoutFactory loadoutFactory;

    @Inject
    public GarageSerialiser(ErrorReporter aErrorReporter, LoadoutFactory aLoadoutFactory) {
        errorReporter = aErrorReporter;
        loadoutFactory = aLoadoutFactory;
    }

    /**
     * Loads a garage from a stream.
     *
     * @param aInputStream
     *            A {@link InputStream} to load from.
     * @return A {@link Garage}.
     */
    public Garage load(InputStream aInputStream) {
        final XStream stream = garageXstream(errorReporter, loadoutFactory);
        return (Garage) stream.fromXML(aInputStream);
    }

    public void save(OutputStream aOutputStream, Garage aGarage) {
        final XStream stream = garageXstream(errorReporter, loadoutFactory);
        stream.toXML(aGarage, aOutputStream);
    }
}
