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
package org.lisoft.lsml.converter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.lisoft.lsml.parsing.mwo_gamedata.GameVFS;
import org.lisoft.lsml.view.preferences.CorePreferences;

public class GameDataFileTest {

    @Test
    public void test() throws IOException {
        File gameDir = new File(CorePreferences.getGameDirectory());
        GameVFS dataFile = new GameVFS(gameDir);

        InputStream inputStream = dataFile.openGameFile(new File("Game/mechs/Objects/mechs/spider/sdr-5k.mdf")).stream;

        assertTrue(inputStream.available() > 6000);
    }

}
