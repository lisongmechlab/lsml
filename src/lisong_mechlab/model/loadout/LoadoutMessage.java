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
package lisong_mechlab.model.loadout;

import lisong_mechlab.util.MessageXBar;

/**
 * This message carries information about change to a {@link LoadoutBase} object.
 * 
 * @author Li Song
 */
public class LoadoutMessage implements MessageXBar.Message {
	public enum Type {
		RENAME, CREATE, UPDATE, MODULES_CHANGED
	}

	private final LoadoutBase<?> loadout;

	public final Type type;

	public LoadoutMessage(LoadoutBase<?> aLoadout, Type aType) {
		loadout = aLoadout;
		type = aType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoadoutMessage other = (LoadoutMessage) obj;
		if (loadout == null) {
			if (other.loadout != null)
				return false;
		} else if (!loadout.equals(other.loadout))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loadout == null) ? 0 : loadout.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean affectsHeatOrDamage() {
		return type == Type.UPDATE || type == Type.MODULES_CHANGED;
	}

	@Override
	public boolean isForMe(LoadoutBase<?> aLoadout) {
		return loadout == aLoadout;
	}
}
