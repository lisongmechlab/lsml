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
package org.lisoft.lsml.view_fx.util;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

/**
 * This is a {@link TextFormatter} that reuses the string converter to double as a filter.
 *
 * @author Li Song
 * @param <T>
 *            The type to format.
 */
public class BetterTextFormatter<T> extends TextFormatter<T> {

    private static class BetterChange<T> implements UnaryOperator<TextFormatter.Change> {
        private final StringConverter<T> converter;

        private BetterChange(StringConverter<T> aConverter) {
            converter = aConverter;
        }

        @Override
        public Change apply(Change aArg0) {
            if (null != converter.fromString(aArg0.getControlNewText())) {
                return aArg0;
            }
            return null;
        }
    }

    /**
     * Creates a new {@link BetterTextFormatter}
     *
     * @param aConverter
     *            A {@link StringConverter} that returns <code>null</code> for invalid data.
     *
     * @param aDefaultValue
     *            The default value if all else fails.
     *
     */
    public BetterTextFormatter(StringConverter<T> aConverter, T aDefaultValue) {
        super(aConverter, aDefaultValue, new BetterChange<>(aConverter));
    }
}
