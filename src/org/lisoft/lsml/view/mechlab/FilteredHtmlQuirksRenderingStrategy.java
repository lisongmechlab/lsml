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
package org.lisoft.lsml.view.mechlab;

import java.util.Collection;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This strategy will render quirks to HTML but only include the quirks that matches a given selector.
 * 
 * @author Li Song
 */
public class FilteredHtmlQuirksRenderingStrategy extends HtmlQuirkRenderingStrategy {

    private final Collection<String> selectors;

    public FilteredHtmlQuirksRenderingStrategy(Collection<String> aCollection, boolean aShowHeaders) {
        super(aShowHeaders);
        selectors = aCollection;
    }

    @Override
    protected void appendModifier(StringBuilder aSb, Modifier aModifier) {
        for (String selector : selectors) {
            if (aModifier.getDescription().getSelectors().contains(selector)) {
                super.appendModifier(aSb, aModifier);
                return;
            }
        }
    }
}
