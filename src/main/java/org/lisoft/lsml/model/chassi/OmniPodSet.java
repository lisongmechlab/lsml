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
package org.lisoft.lsml.model.chassi;

import java.util.Collection;
import java.util.Collections;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This class models a set of {@link OmniPod}s in order to provide the set bonus when all pods of a set are equipped.
 *
 * @author Emily Björk
 */
public class OmniPodSet {
    private final Collection<Modifier> modifiers;

    public OmniPodSet(Collection<Modifier> aModifiers) {
        modifiers = Collections.unmodifiableCollection(aModifiers);
    }

    public Collection<Modifier> getModifiers() {
        return modifiers;
    }
}
