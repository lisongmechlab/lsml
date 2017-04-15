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
package org.lisoft.lsml.util;

import static org.junit.Assert.fail;

import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.LoadoutCoderV1;
import org.lisoft.lsml.model.export.LoadoutCoderV2;
import org.lisoft.lsml.model.export.LoadoutCoderV3;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;

/**
 * This class contains various static helpers to make writing tests easier.
 *
 * @author Emily Björk
 */
public class TestHelpers {

    private static final ErrorReportingCallback errorCallback = (aLoadout, aErrors) -> {
        fail(aErrors.toString());
    };

    private static final Base64 base64 = new Base64();
    private static final LoadoutCoderV1 coderV1 = new LoadoutCoderV1();
    private static final LoadoutCoderV2 coderV2 = new LoadoutCoderV2();
    private static final LoadoutCoderV3 coderV3 = new LoadoutCoderV3(errorCallback);
    private static final Base64LoadoutCoder coder = new Base64LoadoutCoder(base64, coderV1, coderV2, coderV3);

    public static String encodeLSML(Loadout aLoadout) {
        return coder.encodeLSML(aLoadout);
    }

    public static Loadout parse(String aLsmlLink) throws Exception {
        return coder.parse(aLsmlLink);
    }
}
