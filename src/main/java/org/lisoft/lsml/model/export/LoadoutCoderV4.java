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
package org.lisoft.lsml.model.export;

import javax.inject.Inject;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.LoadoutFactory;

/**
 * The fourth version of {@link LoadoutCoder} for LSML. Differs from the third
 * only by magic number and frequency table used for Huffman encoding.
 *
 * Frequency table at the base of the huffman coder is here:
 * https://gist.github.com/EmilyBjoerk/31f79d045772c874743049028ff01956
 *
 * @author Emily Björk
 */
public class LoadoutCoderV4 extends LoadoutCoderV3 {
	@SuppressWarnings("hiding")
	public static final int HEADER_MAGIC = LoadoutCoderV3.HEADER_MAGIC + 1;

	@Inject
	public LoadoutCoderV4(ErrorReporter aErrorReporter, LoadoutFactory aLoadoutFactory) {
		super(aErrorReporter, aLoadoutFactory, "coderstats_v4.bin", HEADER_MAGIC);
	}
}
