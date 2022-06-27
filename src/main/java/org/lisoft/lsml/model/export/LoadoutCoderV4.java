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
package org.lisoft.lsml.model.export;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.LoadoutFactory;

import javax.inject.Inject;

/**
 * The fourth version of {@link LoadoutCoder} for LSML. Differs from the third only by magic number and frequency table
 * used for Huffman encoding.
 * <p>
 * Frequency table at the base of the huffman coder is here:
 * https://gist.github.com/LiSong-Mechlab/b931398eb65cd482e36a7d47949a9b4b
 *
 * @author Li Song
 */
public class LoadoutCoderV4 extends LoadoutCoderV3 {
    @SuppressWarnings("hiding")
    public static final int HEADER_MAGIC = LoadoutCoderV3.HEADER_MAGIC + 1;

    @Inject
    public LoadoutCoderV4(ErrorReporter aErrorReporter, LoadoutFactory aLoadoutFactory) {
        super(aErrorReporter, aLoadoutFactory, "coderstats_v4.bin", HEADER_MAGIC);
    }
}
