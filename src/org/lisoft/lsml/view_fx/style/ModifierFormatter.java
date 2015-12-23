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
package org.lisoft.lsml.view_fx.style;

import java.util.Collection;

import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * This class will format {@link Modifier}s to a {@link Label}s or containers.
 * 
 * @author Li Song
 */
public class ModifierFormatter {

    public Label format(Modifier aModifier) {
        Label label = new Label(aModifier.toString());
        double value = aModifier.getValue();
        ModifierType type = aModifier.getDescription().getModifierType();

        final String color;
        switch (type) {
            case INDETERMINATE:
                color = StyleManager.CSS_COLOUR_QUIRK_NEUTRAL;
                break;
            case NEGATIVE_GOOD:
                if (value < 0)
                    color = StyleManager.CSS_COLOUR_QUIRK_GOOD;
                else
                    color = StyleManager.CSS_COLOUR_QUIRK_BAD;
                break;
            case POSITIVE_GOOD:
                if (value < 0)
                    color = StyleManager.CSS_COLOUR_QUIRK_BAD;
                else
                    color = StyleManager.CSS_COLOUR_QUIRK_GOOD;
                break;
            default:
                throw new RuntimeException("Unknown modifier type!");
        }

        label.setStyle("-fx-text-fill:" + color);
        return label;
    }

    public void format(Collection<Modifier> aModifiers, ObservableList<Node> aTarget) {
        for (Modifier modifier : aModifiers) {
            // TODO: Collate modifiers
            aTarget.add(format(modifier));
        }
    }
}
