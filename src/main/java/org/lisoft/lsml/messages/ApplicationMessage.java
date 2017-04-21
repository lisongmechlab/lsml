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
package org.lisoft.lsml.messages;

import org.lisoft.lsml.model.loadout.Loadout;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * @author Li Song
 */
public class ApplicationMessage implements Message {

	public static enum Type {
		OPEN_LOADOUT, SHARE_LSML, SHARE_SMURFY, CLOSE_OVERLAY
	}

	private final Loadout loadout;
	private final Type type;
	private final Node origin;

	public ApplicationMessage(Loadout aLoadout, Type aType, Node aOrigin) {
		loadout = aLoadout;
		type = aType;
		origin = aOrigin;
	}

	/**
	 * @param closeOverlay
	 * @param root
	 */
	public ApplicationMessage(Type aType, Region aOrigin) {
		this(null, aType, aOrigin);
	}

	@Override
	public boolean affectsHeatOrDamage() {
		return false;
	}

	/**
	 * @return the loadout
	 */
	public Loadout getLoadout() {
		return loadout;
	}

	/**
	 * @return the origin
	 */
	public Node getOrigin() {
		return origin;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	@Override
	public boolean isForMe(Loadout aLoadout) {
		return false;
	}

}
