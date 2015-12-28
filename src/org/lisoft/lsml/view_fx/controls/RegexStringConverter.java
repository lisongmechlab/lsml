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
package org.lisoft.lsml.view_fx.controls;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

/**
 * A {@link StringConverter} that uses a regular expression to parse the contents. Compatible with
 * {@link BetterTextFormatter}.
 * 
 * @author Emily Björk
 */
public class RegexStringConverter extends StringConverter<Double> {
    private final DecimalFormat df;
    private final Pattern       pattern;

    /**
     * Creates a new {@link RegexStringConverter}. The pattern and format must be compatible.
     * 
     * @param aPattern
     *            The {@link Pattern} to use when parsing.
     * @param aDecimalFormat
     *            A {@link DecimalFormat} to use when formatting.
     */
    public RegexStringConverter(Pattern aPattern, DecimalFormat aDecimalFormat) {
        pattern = aPattern;
        df = aDecimalFormat;
    }

    @Override
    public Double fromString(String aString) {
        Matcher m = pattern.matcher(aString);
        if (m.matches()) {
            return Double.parseDouble(m.group(1));
        }
        return null;
    }

    @Override
    public String toString(Double aObject) {
        return df.format(aObject.doubleValue());
    }
}
