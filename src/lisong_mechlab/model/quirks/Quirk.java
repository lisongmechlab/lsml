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
package lisong_mechlab.model.quirks;

import java.text.DecimalFormat;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class Quirk {
    protected final static DecimalFormat FORMAT = new DecimalFormat("###.#");

    @XStreamAsAttribute
    public final String                  name;
    @XStreamAsAttribute
    public final double                  value;

    public Quirk(String aName, double aValue) {
        name = aName;
        value = aValue;
    }

    /**
     * @return <code>true</code> if a positive value on this quirk is beneficial for the pilot.
     */
    abstract public Quirks.QuirkBenefit isPositiveGood();

    /**
     * Outputs HTML to describe this quirk to an existing document. I.e. body and header tags are not emitted.
     * 
     * @param aSB
     *            The {@link StringBuilder} to send the output to.
     */
    public void describeToHtml(StringBuilder aSB) {
        aSB.append("<div>");
        aSB.append("<span style=\"color:").append(isPositiveGood().getColor(value)).append(";\">");

        aSB.append(name).append(": ");
        writeValue(aSB);

        aSB.append("</span>");
        aSB.append("</div>");
    }

    /**
     * Formats the quirk into a human readable description.
     * 
     * @param aSB
     *            The string builder to send the result to.
     */
    protected void writeValue(StringBuilder aSB) {
        if (value > 0) {
            aSB.append("+");
        }
        if (value < 1.0) {
            aSB.append(FORMAT.format(value * 100)).append("%");
        }
        else {
            aSB.append(FORMAT.format(value));
        }
    }
}