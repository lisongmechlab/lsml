/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.garage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.MXParserDriver;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import javax.inject.Inject;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.export.garage.*;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.mwo_data.equipment.Item;

/**
 * This class is used for loading and writing garage files from/to disk.
 *
 * @author Li Song
 */
public class GarageSerializer {
  private final LoadoutBuilder builder;
  private final ErrorReporter errorReporter;
  private final LoadoutFactory loadoutFactory;

  @Inject
  public GarageSerializer(
      ErrorReporter aErrorReporter, LoadoutFactory aLoadoutFactory, LoadoutBuilder aBuilder) {
    errorReporter = aErrorReporter;
    loadoutFactory = aLoadoutFactory;
    builder = aBuilder;
  }

  /**
   * Loads a garage from a stream.
   *
   * @param aInputStream A {@link InputStream} to load from.
   * @return A {@link Garage}.
   */
  public Garage load(InputStream aInputStream) {
    final XStream stream = makeStream();
    return (Garage) stream.fromXML(aInputStream);
  }

  public void save(OutputStream aOutputStream, Garage aGarage) {
    final XStream stream = makeStream();
    stream.toXML(Objects.requireNonNull(aGarage, "Save called with a null garage!"), aOutputStream);
  }

  private XStream makeStream() {
    final XStream stream = new XStream(new MXParserDriver());
    stream.autodetectAnnotations(true);
    stream.processAnnotations(Garage.class);
    stream.processAnnotations(LoadoutOmniMech.class);
    stream.processAnnotations(LoadoutStandard.class);
    stream.setMode(XStream.NO_REFERENCES);

    stream.registerConverter(new ItemConverter(builder));
    stream.registerConverter(new ModuleConverter(builder));
    stream.registerConverter(new ConfiguredComponentConverter(null, null));
    stream.registerConverter(new LoadoutConverter(errorReporter, loadoutFactory, builder));
    stream.registerConverter(new UpgradeConverter(builder));
    stream.registerConverter(new UpgradesConverter());
    stream.registerConverter(new EfficienciesConverter());
    stream.registerConverter(
        new GarageConverter(stream.getMapper(), stream.getReflectionProvider()));

    stream.allowTypeHierarchy(Loadout.class);
    stream.allowTypeHierarchy(DropShip.class);
    stream.allowTypeHierarchy(Garage.class);
    stream.allowTypeHierarchy(GarageDirectory.class);

    stream.addImmutableType(Item.class, true);
    stream.alias("component", ConfiguredComponentStandard.class);
    stream.ignoreUnknownElements(".*firingMode*");
    return stream;
  }
}
