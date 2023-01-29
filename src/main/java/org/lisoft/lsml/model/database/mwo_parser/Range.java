/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.lsml.model.database.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Note that the <code>&ltRange&gt</code> tag appears in different contexts with different
 * attributes.
 *
 * <p>I can't find a way to instruct XStream to use different classes for different context so w
 *
 * @author Li Song
 */
@XStreamAlias("Range")
class Range {
  @XStreamAsAttribute double damageModifier;
  @XStreamAsAttribute Double exponent;
  @XStreamAsAttribute String interpolationToNextRange;
  // The following attributes are valid when read in the context of a <RANGE> on a
  // <TARGETINCOMPUTER>
  @XStreamAsAttribute double multiplier;
  // The following attributes are valid when read in the context of a <RANGE> tag on a <RANGES> list
  // in a <WEAPON>
  @XStreamAsAttribute double start;
}
