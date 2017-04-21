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

import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;

import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.LoadoutCoderV1;
import org.lisoft.lsml.model.export.LoadoutCoderV2;
import org.lisoft.lsml.model.export.LoadoutCoderV3;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.view_fx.ErrorReporter;

import javafx.stage.Window;

/**
 * This class contains various static helpers to make writing tests easier.
 *
 * @author Emily Björk
 */
public class TestHelpers {

    private static final ErrorReporter errorCallback = new ErrorReporter() {
        @Override
        public void error(Window aOwner, Loadout aLoadout, List<Throwable> aErrors) {
            fail(Arrays.deepToString(aErrors.toArray()));
        }

        @Override
        public void error(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
            fail(aMessage);
        }

        @Override
        public void fatal(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
            fail(aMessage);
        }
    };

    private final static Encoder base64Encoder = Base64.getEncoder();
    private final static Decoder base64Decoder = Base64.getDecoder();

    private static final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    private static final LoadoutCoderV1 coderV1 = new LoadoutCoderV1(loadoutFactory);
    private static final LoadoutCoderV2 coderV2 = new LoadoutCoderV2(loadoutFactory);
    private static final LoadoutCoderV3 coderV3 = new LoadoutCoderV3(errorCallback, loadoutFactory);
    private static final Base64LoadoutCoder coder = new Base64LoadoutCoder(base64Encoder, base64Decoder, coderV1,
            coderV2, coderV3);

    public static String encodeLSML(Loadout aLoadout) {
        return coder.encodeLSML(aLoadout);
    }

    public static Loadout parse(String aLsmlLink) throws Exception {
        return coder.parse(aLsmlLink);
    }
}
