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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents quirks in the form of movement, health and weapon stats.
 * 
 * @author Emily Björk
 */
public class Quirks {
    public static enum QuirkBenefit {
        POSITIVE_GOOD, NEGATIVE_GOOD, INDETERMINATE;

        public String getColor(double aValue) {
            switch (this) {
                case INDETERMINATE:
                    return "black";
                case NEGATIVE_GOOD:
                    return (aValue < 0) ? "green" : "red";
                case POSITIVE_GOOD:
                    return (aValue > 0) ? "green" : "red";
                default:
                    throw new IllegalArgumentException("Unknown quirkmode!");
            }
        }
    }

    private final List<Quirk> quirks;

    /**
     * Creates a mew {@link Quirks} object.
     * 
     * @param aQuirks
     *            A {@link List} of {@link Quirk}s that make up this {@link Quirks}.
     */
    public Quirks(List<Quirk> aQuirks) {
        quirks = aQuirks;
    }

    public String describeAsHtml() {
        if (quirks.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");
        sb.append("<p>Quirks:</p>");
        describeAsHtmlWithoutHeaders(sb);
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Will describe the contents of this quirks object without emitting HTML head and body tags.
     * 
     * @param aOutput
     *            The {@link StringBuilder} to put the output in.
     */
    public void describeAsHtmlWithoutHeaders(StringBuilder aOutput) {
        for (Quirk quirk : quirks) {
            quirk.describeToHtml(aOutput);
        }
    }

    public <T> List<T> getQuirksByType(Class<T> aClass) {
        List<T> ans = new ArrayList<>();
        for (Quirk quirk : quirks) {
            if (aClass.isInstance(quirk)) {
                ans.add(aClass.cast(quirk));
            }
        }
        return ans;
    }
}
