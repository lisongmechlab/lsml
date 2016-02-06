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

import java.awt.Component;

import javax.swing.JLabel;

import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.item.ModifierEquipment;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This strategy will render quirks to HTML inside of a JLabel.
 * 
 * @author Li Song
 */
public class HtmlQuirkRenderingStrategy implements QuirksRenderingStrategy {

    private final boolean showHeaders;

    /**
     * @param aShowHeaders
     *            <code>true</code> if the headers for each section should be shown.
     */
    public HtmlQuirkRenderingStrategy(boolean aShowHeaders) {
        showHeaders = aShowHeaders;
    }

    protected void appendModifier(StringBuilder aSb, Modifier aModifier) {
        aModifier.describeToHtml(aSb);
    }

    private String renderText(Loadout<?> aLoadout) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");

        if (aLoadout instanceof LoadoutOmniMech) {
            if (showHeaders) {
                sb.append("<p>Omnipod Quirks:</p>");
            }
            for (ConfiguredComponentOmniMech component : ((LoadoutOmniMech) aLoadout).getComponents()) {
                for (Modifier modifier : component.getOmniPod().getQuirks()) {
                    appendModifier(sb, modifier);
                }
            }
        }
        else if (aLoadout instanceof LoadoutStandard) {
            if (showHeaders) {
                sb.append("<p>Chassis Quirks:</p>");
            }
            for (Modifier modifier : ((ChassisStandard) aLoadout.getChassis()).getQuirks()) {
                appendModifier(sb, modifier);
            }
        }
        else {
            throw new RuntimeException("Unknown loadout type!");
        }

        if (aLoadout.items(ModifierEquipment.class).iterator().hasNext()) {
            if (showHeaders) {
                sb.append("<p>Equipment Bonuses:</p>");
            }
            for (ModifierEquipment me : aLoadout.items(ModifierEquipment.class)) {
                for (Modifier modifier : me.getModifiers()) {
                    appendModifier(sb, modifier);
                }
            }
        }

        if (!aLoadout.getModules().isEmpty()) {
            if (showHeaders) {
                sb.append("<p>Module Bonuses:</p>");
            }
            for (PilotModule me : aLoadout.getModules()) {
                if (me instanceof ModifierEquipment) {
                    for (Modifier modifier : ((ModifierEquipment) me).getModifiers()) {
                        appendModifier(sb, modifier);
                    }
                }
            }
        }

        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public Component render(Loadout<?> aLoadout) {
        return new JLabel(renderText(aLoadout));
    }
}
