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
package org.lisoft.lsml.view_fx.style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.modifiers.Modifier;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * This class will format {@link Modifier}s to a {@link Label}s or containers.
 *
 * @author Emily Björk
 */
public class PredicatedModifierFormatter extends ModifierFormatter {
    private final ObjectProperty<Predicate<Modifier>> predicate;

    public PredicatedModifierFormatter(Predicate<Modifier> aPredicate) {
        predicate = new SimpleObjectProperty<>(aPredicate);
    }

    @Override
    public void format(Collection<Modifier> aModifiers, ObservableList<Node> aTarget) {
        super.format(aModifiers.stream().filter(predicate.get()).collect(Collectors.toCollection(ArrayList::new)),
                aTarget);
    }

    @Override
    public Label format(Modifier aModifier) {
        if (predicate.get().test(aModifier)) {
            return super.format(aModifier);
        }
        return new Label();
    }

    public ObjectProperty<Predicate<Modifier>> predicateProperty() {
        return predicate;
    }
}
