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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * A metric that calculates how high the mech can jump.
 * 
 * @author Li Song
 */
public class JumpDistance implements Metric {
    private final Loadout loadout;

    public JumpDistance(final Loadout aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public double calculate() {
        JumpJet jj = null;

        for (JumpJet item : loadout.items(JumpJet.class)) {
            jj = item;
            break;
        }

        if (jj == null)
            return 0;
        return loadout.getJumpJetCount() * jj.getForce() * jj.getDuration() * jj.getDuration()
                / (2 * loadout.getChassis().getMassMax());
    }
}
