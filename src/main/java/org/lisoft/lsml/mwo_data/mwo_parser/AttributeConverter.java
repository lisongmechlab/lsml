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
package org.lisoft.lsml.mwo_data.mwo_parser;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.Arrays;
import java.util.List;
import org.lisoft.lsml.mwo_data.modifiers.Attribute;

public class AttributeConverter implements Converter {
  private static final String SELECTORS = "selectors";
  private static final String SPECIFIER = "specifier";
  private static final String VALUE = "value";

  @Override
  public boolean canConvert(Class aType) {
    return Attribute.class.isAssignableFrom(aType);
  }

  @Override
  public void marshal(
      Object aSource, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
    final Attribute attribute = (Attribute) aSource;
    if (attribute.getSpecifier() != null) {
      aWriter.addAttribute(SPECIFIER, attribute.getSpecifier());
    }

    aWriter.addAttribute(VALUE, Double.toString(attribute.getBaseValue()));

    final String selectors = String.join(",", attribute.getSelectors());
    aWriter.addAttribute(SELECTORS, selectors);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {

    final String specifier = aReader.getAttribute(SPECIFIER);
    final double value = Double.parseDouble(aReader.getAttribute(VALUE));
    final List<String> selectors = Arrays.asList(aReader.getAttribute(SELECTORS).split(","));

    return new Attribute(value, selectors, specifier);
  }
}
