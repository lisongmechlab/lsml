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
package org.lisoft.lsml.view.mechlab;

import java.util.List;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This strategy will render quirks to HTML but only include the quirks that matches a given selector.
 * 
 * @author Emily Björk
 */
public class FilteredHtmlQuirksRenderingStrategy extends HtmlQuirkRenderingStrategy {

    private final List<String> selectors;

    public FilteredHtmlQuirksRenderingStrategy(List<String> aSelectors, boolean aShowHeaders) {
        super(aShowHeaders);
        selectors = aSelectors;
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
