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

import static javafx.beans.binding.Bindings.equal;
import static javafx.beans.binding.Bindings.when;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.model.item.Faction;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;

/**
 * @author Li Song
 *
 */
public class FxBindingUtils {

    /**
     * Creates an {@link ObjectBinding} of {@link Faction} type from two boolean expressions for either
     * {@link Faction#CLAN} or {@link Faction#INNERSPHERE}.
     * 
     * @param filterClan
     *            A {@link BooleanExpression} that is true if clan should be included.
     * @param filterInnerSphere
     *            A {@link BooleanExpression} that is true if inner sphere should be included.
     * @return A new {@link ObjectBinding} of {@link Faction}.
     */
    public static ObjectBinding<Faction> createFactionBinding(BooleanExpression filterClan,
            BooleanExpression filterInnerSphere) {

        return when(equal(filterClan, filterInnerSphere)).then(Faction.ANY)
                .otherwise(when(filterClan).then(Faction.CLAN).otherwise(Faction.INNERSPHERE));
    }

    /**
     * Creates a {@link StringBinding} that will convert the given {@link NumberExpression} to a formatted string. NaN
     * and <code>null</code> values will be converted to a hyphen ('-').
     * 
     * @param aFormat
     *            A format to convert the number to. See {@link DecimalFormat}.
     * @param aZeroAsHyphen
     *            If <code>true</code> a value of zero will be shown as a hyphen ('-').
     * @param aValue
     *            The value to convert.
     * @return A {@link StringBinding} that converts the given {@link NumberExpression}.
     */
    public static StringBinding formatValue(String aFormat, boolean aZeroAsHyphen, double aValue) {
        return new StringBinding() {
            private final DecimalFormat df = new DecimalFormat(aFormat);

            @Override
            protected String computeValue() {
                if ((aZeroAsHyphen && aValue == 0.0) || Double.isNaN(aValue)) {
                    return "-";
                }
                return df.format(aValue);
            }
        };
    }

    /**
     * Creates a {@link StringBinding} that will convert the given {@link NumberExpression} to a formatted string. NaN
     * and <code>null</code> values will be converted to a hyphen ('-').
     * 
     * @param aFormat
     *            A format to convert the number to. See {@link DecimalFormat}.
     * @param aZeroAsHyphen
     *            If <code>true</code> a value of zero will be shown as a hyphen ('-').
     * @param aValue
     *            The value to convert.
     * @return A {@link StringBinding} that converts the given {@link NumberExpression}.
     */
    public static StringBinding formatValue(String aFormat, boolean aZeroAsHyphen, NumberExpression aValue) {
        return new StringBinding() {
            private final DecimalFormat df = new DecimalFormat(aFormat);
            {
                bind(aValue);
            }

            @Override
            protected String computeValue() {
                Number value = aValue.getValue();
                if (value == null || (aZeroAsHyphen && value.doubleValue() == 0.0)
                        || Double.isNaN(value.doubleValue())) {
                    return "-";
                }
                return df.format(value.doubleValue());
            }
        };
    }

    /**
     * Formats a {@link StringBinding} to contain a number of {@link NumberExpression}s.
     * 
     * The format of the format string is a simplified version of what {@link String#format(String, Object...)}
     * supports.
     * 
     * The format is:
     * 
     * %[.n][p][h]
     * 
     * where '.n' is optional and signifies the maximal number of decimal digits to show. The optional 'p' means to
     * format the value as a percentage by multiplying by 100 and adding a percent symbol suffix (literal '%'). The
     * optional 'h' means to format a value of zero (0.0) as a hyphen ('-') to symbolise "not applicable".
     * 
     * A double percent string will output a single percent literal, '%%'.
     * 
     * @param aFmt
     *            A format string as described above.
     * @param aNumbers
     *            A variable number of expressions that should be formatted. Must match the number of format specifiers
     *            in the format string.
     * @return A {@link StringBinding} with the given numbers formatted.
     */
    public static StringBinding format(String aFmt, NumberExpression... aNumbers) {
        return new StringBinding() {
            private final static String DEFAULT_FORMAT = "#.##";
            protected static final String DEFAULT_FORMAT_PCT = "#.## %";
            private final List<String> parts;
            private final List<StringBinding> values;

            {
                parts = new ArrayList<>();
                values = new ArrayList<>();
                StringBuilder sb = new StringBuilder(aFmt.length());

                int formatIdx = 0;
                int pen = 0;
                while (pen < aFmt.length()) {
                    final char penChar = aFmt.charAt(pen);
                    final boolean isFormat = penChar == '%';
                    final boolean isLiteralPct = isFormat && (pen + 1 < aFmt.length()) && aFmt.charAt(pen + 1) == '%';

                    if (!isFormat) {
                        sb.append(penChar);
                        pen += 1;
                    }
                    else if (isLiteralPct) {
                        sb.append('%');
                        pen += 2;
                    }
                    else { // It is a format string and it is not a literal percent symbol.
                        parts.add(sb.toString());
                        sb = new StringBuilder(aFmt.length() - pen);

                        boolean optPercent = false;
                        boolean optHyphen = false;
                        int optPrecision = -1;

                        boolean parsingFmt = true;
                        do {
                            pen += 1;
                            if (pen >= aFmt.length())
                                break;
                            char nextChar = aFmt.charAt(pen);
                            switch (nextChar) {
                                case 'p':
                                    optPercent = true;
                                    break;
                                case 'h':
                                    optHyphen = true;
                                    break;
                                case '.':
                                    int precisionPen = pen + 1;
                                    while (precisionPen < aFmt.length()
                                            && Character.isDigit(aFmt.charAt(precisionPen))) {
                                        precisionPen++;
                                    }
                                    optPrecision = Integer.parseInt(aFmt.substring(pen + 1, precisionPen));
                                    pen = precisionPen - 1;
                                    break;
                                default:
                                    parsingFmt = false;
                                    break;
                            }
                        } while (parsingFmt);

                        final String numberFormat;
                        if (optPrecision < 0) {
                            if (optPercent) {
                                numberFormat = DEFAULT_FORMAT_PCT;
                            }
                            else {
                                numberFormat = DEFAULT_FORMAT;
                            }
                        }
                        else {
                            StringBuilder formatBuilder = new StringBuilder();
                            formatBuilder.append('#');
                            if (optPrecision > 0) {
                                formatBuilder.append('.');
                                for (int i = 0; i < optPrecision; ++i) {
                                    formatBuilder.append('#');
                                }
                            }
                            if (optPercent) {
                                formatBuilder.append(" %");
                            }
                            numberFormat = formatBuilder.toString();
                        }
                        StringBinding value = formatValue(numberFormat, optHyphen, aNumbers[formatIdx]);
                        bind(value);
                        values.add(value);
                        formatIdx++;
                    }
                }

                if (sb.length() > 0) {
                    parts.add(sb.toString());
                }

                if (formatIdx != aNumbers.length) {
                    throw new IllegalArgumentException(
                            "The number of format specifiers in the fmt string didn't match the number of expressions");
                }
            }

            @Override
            protected String computeValue() {
                StringBuilder sb = new StringBuilder();
                int nextValue = 0;
                for (String part : parts) {
                    sb.append(part);
                    if (nextValue < values.size()) {
                        sb.append(values.get(nextValue).get());
                        nextValue++;
                    }
                }
                return sb.toString();
            }
        };
    }

}
