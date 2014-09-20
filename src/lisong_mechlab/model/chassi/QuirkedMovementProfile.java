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
package lisong_mechlab.model.chassi;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link MovementProfile} gives the sum of all added {@link MovementProfile}s. One profile has to be chosen as
 * main profile that gives base attributes.
 * 
 * @author Emily Björk
 */
public class QuirkedMovementProfile extends ModifiedProfileBase {

	private List<MovementModifier> terms = new ArrayList<>();
	MovementProfile mainProfile;

	@Override
	protected double calc(String aMethodName) {
		try {
			double base = (double) mainProfile.getClass().getMethod(aMethodName).invoke(mainProfile);
			double ans = base;

			for (MovementModifier profile : terms) {
				ans += (double) profile.getClass().getMethod(aMethodName.replace("get", "extra"), double.class)
						.invoke(profile, base);
			}
			return ans;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public QuirkedMovementProfile(MovementProfile aMainProfile) {
		mainProfile = aMainProfile;
	}

	public void addMovementModifier(MovementModifier aMovementProfile) {
		terms.add(aMovementProfile);
	}

	public void removeMovementModifier(MovementModifier aMovementProfile) {
		terms.remove(aMovementProfile);
	}

	@Override
	public MovementArchetype getMovementArchetype() {
		return mainProfile.getMovementArchetype();
	}

	@Override
	public double getMaxMovementSpeed() {
		return mainProfile.getMaxMovementSpeed();
	}

	@Override
	public double getReverseSpeedMultiplier() {
		return mainProfile.getReverseSpeedMultiplier();
	}
}
