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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.LoadoutCoderV1;
import org.lisoft.lsml.model.export.LoadoutCoderV2;
import org.lisoft.lsml.model.export.LoadoutCoderV3;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.item.WeaponRangeProfile;
import org.lisoft.lsml.model.item.WeaponRangeProfile.RangeNode;
import org.lisoft.lsml.model.item.WeaponRangeProfile.RangeNode.InterpolationType;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

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

    public static Weapon makeWeapon(final double zeroRange, final double minRange, final double longRange,
            final double maxRange, final boolean isOffensive, double dps, String aName,
            Collection<Modifier> aModifiers) {
        return makeWeapon(zeroRange, minRange, longRange, maxRange, 0.0, 1.0, 1.0, 0.0, isOffensive, dps, aName,
                aModifiers);
    }

    public static Weapon makeWeapon(final double zeroRange, final double minRange, final double longRange,
            final double maxRange, final double zeroRangeEff, final double minRangeEff, final double longRangeEff,
            final double maxRangeEff, final boolean isOffensive, double dps, String aName,
            Collection<Modifier> aModifiers) {
        final Weapon weapon = mock(Weapon.class);
        when(weapon.getName()).thenReturn(aName);
        when(weapon.isOffensive()).thenReturn(isOffensive);

        final List<RangeNode> nodes = new ArrayList<>();
        nodes.add(new RangeNode(rangeNode(zeroRange), InterpolationType.STEP, zeroRangeEff));
        nodes.add(new RangeNode(rangeNode(minRange), InterpolationType.LINEAR, minRangeEff));
        nodes.add(new RangeNode(rangeNode(longRange), InterpolationType.LINEAR, longRangeEff));
        nodes.add(new RangeNode(rangeNode(maxRange), InterpolationType.LINEAR, maxRangeEff));

        final WeaponRangeProfile rangeProfile = new WeaponRangeProfile(nodes);

        when(weapon.getRangeProfile()).thenReturn(rangeProfile);
        when(weapon.getRangeMax(aModifiers)).thenReturn(maxRange);
        when(weapon.getStat("d/s", aModifiers)).thenReturn(dps);
        return weapon;
    }

    public static Loadout parse(String aLsmlLink) throws Exception {
        return coder.parse(aLsmlLink);
    }

    public final static Attribute rangeNode(double aRange) {
        return new Attribute(aRange, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_RANGE);
    }
}
