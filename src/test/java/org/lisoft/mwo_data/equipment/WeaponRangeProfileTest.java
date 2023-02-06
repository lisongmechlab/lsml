/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.mwo_data.equipment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.*;
import org.junit.Test;
import org.lisoft.lsml.math.probability.BinomialDistribution;
import org.lisoft.lsml.util.TestHelpers;
import org.lisoft.mwo_data.equipment.WeaponRangeProfile.RangeNode;
import org.lisoft.mwo_data.equipment.WeaponRangeProfile.RangeNode.InterpolationType;
import org.lisoft.mwo_data.modifiers.*;

/**
 * Test suite for {@link WeaponRangeProfile}.
 *
 * @author Li Song
 */
public class WeaponRangeProfileTest {
  private static final double TOLERANCE = 1E-9;
  final Collection<Modifier> modifiersRange10Pct =
      List.of(
          new Modifier(
              new ModifierDescription(
                  "",
                  "",
                  Operation.MUL,
                  ModifierDescription.SEL_ALL,
                  ModifierDescription.SPEC_WEAPON_RANGE,
                  ModifierType.POSITIVE_GOOD),
              0.1));

  @Test
  public void testGetMaxEffectiveness() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    assertEquals(1.5, cut.getMaxEffectiveness(), 0.0);
  }

  @Test
  public void testGetMaxRange() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    assertEquals(300.0, cut.getMaxRange(null), 0.0);
  }

  @Test
  public void testGetMaxRangeEmptyRange() {
    final List<RangeNode> profile = new ArrayList<>();
    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);
    assertEquals(0.0, cut.getMaxRange(null), 0.0);
  }

  @Test
  public void testGetMaxRangeZeroNodeAtTail() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.LINEAR, 0.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    assertEquals(100.0, cut.getMaxRange(null), 0.0);
  }

  @Test
  public void testGetOptimalRange() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(
        new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.EXPONENTIAL, 0.5, 2.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(400.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final WeaponRangeProfile.Range rng = cut.getOptimalRange(null);

    assertEquals(200, rng.minimum, 0.0);
    assertEquals(300, rng.maximum, 0.0);
  }

  @Test
  public void testGetOptimalRangeAtStart() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final WeaponRangeProfile.Range rng = cut.getOptimalRange(null);

    assertEquals(0, rng.minimum, 0.0);
    assertEquals(100, rng.maximum, 0.0);
  }

  @Test
  public void testGetOptimalRangeSteps() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.STEP, 0.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.STEP, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final WeaponRangeProfile.Range rng = cut.getOptimalRange(null);

    assertEquals(100, rng.minimum, 0.0);
    assertEquals(200, rng.maximum, 0.0);
  }

  @Test
  public void testGetPercentileRange() {
    final double spread = 1.0;
    final Attribute attrSpread =
        new Attribute(spread, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_SPREAD);

    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.LINEAR, 0.25));
    profile.add(new RangeNode(TestHelpers.rangeNode(400.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(700.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);

    final WeaponRangeProfile.Range rng = cut.getPercentileRange(0.9, null);

    assertEquals(220, rng.minimum, 10.0);
    assertEquals(420, rng.maximum, 10.0);
  }

  @Test
  public void testGetPercentileRangeCLRM() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.EXPONENTIAL, 0.0, 2.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(180.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(900.0), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final WeaponRangeProfile.Range rng = cut.getPercentileRange(0.9, null);

    assertEquals(Math.sqrt(0.9) * 180, rng.minimum, 10.0);
    assertEquals(900, rng.maximum, 10.0);
  }

  @Test
  public void testGetPercentileRangeEmptyRange() {
    final double spread = 1.0;
    final Attribute attrSpread =
        new Attribute(spread, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_SPREAD);

    final List<RangeNode> profile = new ArrayList<>();

    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);

    final WeaponRangeProfile.Range rng = cut.getPercentileRange(0.9, null);

    assertEquals(0.0, rng.minimum, 0.0);
    assertEquals(0.0, rng.maximum, 0.0);
  }

  @Test
  public void testGetPercentileRangeNoSpread() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.LINEAR, 0.25));
    profile.add(new RangeNode(TestHelpers.rangeNode(400.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(700.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final WeaponRangeProfile.Range rng = cut.getPercentileRange(0.9, null);

    assertEquals(352, rng.minimum, 10.0);
    assertEquals(430, rng.maximum, 10.0);
  }

  /**
   * {@link WeaponRangeProfile#getPolygonTrainRanges} returns a series of range positions that can
   * be used for drawing the range profile as a polygon train.
   */
  @Test
  public void testGetPolygonTrainRanges() {
    final double dx = 10.0;
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(
        new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.EXPONENTIAL, 0.5, 2.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final List<Double> expected = new ArrayList<>();
    expected.add(0.0);
    expected.add(Math.nextDown(100.0));
    for (double x = 100.0; x <= 200; x += dx) {
      expected.add(x);
    }
    expected.add(300.0);
    expected.add(Math.nextUp(300.0));

    assertShapeEquals(expected, cut.getPolygonTrainRanges(dx, null));
  }

  /** If the last {@link RangeNode} ends at 0.0 multiplier, don't add a dummy node after. */
  @Test
  public void testGetPolygonTrainRangesNoExtraTrailingSamples() {
    final double dx = 10.0;
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final List<Double> expected = new ArrayList<>();
    expected.add(0.0);
    expected.add(300.0);
    assertShapeEquals(expected, cut.getPolygonTrainRanges(dx, null));
  }

  /**
   * {@link WeaponRangeProfile#getPolygonTrainRanges} returns a series of range positions that can
   * be used for drawing the range profile as a polygon train.
   */
  @Test
  public void testGetPolygonTrainRangesNoRangeNodes() {
    final double dx = 10.0;
    final List<RangeNode> profile = new ArrayList<>();
    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);
    final List<Double> expected = new ArrayList<>();
    assertShapeEquals(expected, cut.getPolygonTrainRanges(dx, null));
  }

  /**
   * If the {@link WeaponRangeProfile} contains a spread, the entire range should be sampled with
   * dT.
   */
  @Test
  public void testGetPolygonTrainRangesSpread() {
    final double dx = 10.0;
    final double spread = 1.0;
    final Attribute attrSpread =
        new Attribute(spread, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_SPREAD);

    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(
        new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.EXPONENTIAL, 0.5, 2.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);
    final List<Double> expected = new ArrayList<>();
    for (double x = 0; x <= 300; x += dx) {
      expected.add(x);
    }
    expected.add(Math.nextUp(300.0));

    assertShapeEquals(expected, cut.getPolygonTrainRanges(dx, null));
  }

  /**
   * {@link WeaponRangeProfile#getPolygonTrainRanges} returns a series of range positions that can
   * be used for drawing the range profile as a polygon train.
   */
  @Test
  public void testGetPolygonTrainRangesSpreadNoRangeNodes() {
    final double dx = 10.0;
    final double spread = 1.0;
    final Attribute attrSpread =
        new Attribute(spread, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_SPREAD);
    final List<RangeNode> profile = new ArrayList<>();
    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);
    final List<Double> expected = new ArrayList<>();
    assertShapeEquals(expected, cut.getPolygonTrainRanges(dx, null));
  }

  @Test
  public void testRangeEffectiveness() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(
        new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.EXPONENTIAL, 0.5, 2.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(400.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    // Always 0 effectiveness below 0m
    assertEquals(0.0, cut.rangeEffectiveness(-Math.ulp(0.0), null), 0.0);

    // Step of .25 until 100m
    assertEquals(0.25, cut.rangeEffectiveness(0.0, null), 0.0);
    assertEquals(0.25, cut.rangeEffectiveness(50.0, null), 0.0);
    assertEquals(0.25, cut.rangeEffectiveness(Math.nextDown(100.0), null), 0.0);

    // Exponential from 0.5 to 1.5 until 200m with power 2
    assertEquals(0.5, cut.rangeEffectiveness(100.0, null), 0.0);
    assertEquals(0.75, cut.rangeEffectiveness(150.0, null), 0.0);
    assertEquals(1.5, cut.rangeEffectiveness(200.0, null), 0.0);

    // Linear from 1.5 to 1.0 until 300m
    assertEquals(1.5, cut.rangeEffectiveness(200.0, null), 0.0);
    assertEquals(1.25, cut.rangeEffectiveness(250.0, null), 0.0);
    assertEquals(1.0, cut.rangeEffectiveness(300.0, null), 0.0);

    // Linear from 1.0 to 0.0 until 400m
    assertEquals(1.0, cut.rangeEffectiveness(300.0, null), 0.0);
    assertEquals(0.5, cut.rangeEffectiveness(350.0, null), 0.0);
    assertEquals(0.0, cut.rangeEffectiveness(400.0, null), 0.0);

    // Extrapolate step from last node.
    assertEquals(0.0, cut.rangeEffectiveness(500.0, null), 0.0);
  }

  @Test
  public void testRangeEffectivenessBeyondMaxDrop() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    // Extrapolate step from last node.
    assertEquals(1.0, cut.rangeEffectiveness(300.0, null), 0.0);
    assertEquals(0.0, cut.rangeEffectiveness(Math.nextUp(300.0), null), 0.0);
  }

  @Test
  public void testRangeEffectivenessModifiers() {
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 0.25));
    profile.add(
        new RangeNode(TestHelpers.rangeNode(100.0), InterpolationType.EXPONENTIAL, 0.5, 2.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(200.0), InterpolationType.LINEAR, 1.5));
    profile.add(new RangeNode(TestHelpers.rangeNode(300.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(400.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(profile);

    final double scale = 1.1;

    // Always 0 effectiveness below 0m
    assertEquals(
        0.0, cut.rangeEffectiveness(-Math.ulp(scale * 0.0), modifiersRange10Pct), TOLERANCE);

    // Step of .25 until 100m
    assertEquals(0.25, cut.rangeEffectiveness(scale * 0.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(0.25, cut.rangeEffectiveness(scale * 50.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(
        0.25, cut.rangeEffectiveness(Math.nextDown(scale * 100.0), modifiersRange10Pct), TOLERANCE);

    // Exponential from 0.5 to 1.5 until 200m with power 2
    assertEquals(0.5, cut.rangeEffectiveness(scale * 100.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(0.75, cut.rangeEffectiveness(scale * 150.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(1.5, cut.rangeEffectiveness(scale * 200.0, modifiersRange10Pct), TOLERANCE);

    // Linear from 1.5 to 1.0 until 300m
    assertEquals(1.5, cut.rangeEffectiveness(scale * 200.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(1.25, cut.rangeEffectiveness(scale * 250.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(1.0, cut.rangeEffectiveness(scale * 300.0, modifiersRange10Pct), TOLERANCE);

    // Linear from 1.0 to 0.0 until 400m
    assertEquals(1.0, cut.rangeEffectiveness(scale * 300.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(0.5, cut.rangeEffectiveness(scale * 350.0, modifiersRange10Pct), TOLERANCE);
    assertEquals(0.0, cut.rangeEffectiveness(scale * 400.0, modifiersRange10Pct), TOLERANCE);

    // Extrapolate step from last node.
    assertEquals(0.0, cut.rangeEffectiveness(scale * 500.0, modifiersRange10Pct), TOLERANCE);
  }

  @Test
  public void testRangeEffectivenessSpreadLotsOfProjectiles() {
    // We don't want range affecting the result.
    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(4000.0), InterpolationType.LINEAR, 0.0));

    // Compute the required range to have "angle" field of view on "radius" sized target.
    final double angleRad = Math.PI / 6.0; // 30 degrees
    final double radius = 6.0;
    final double range = radius / Math.tan(angleRad); // tan = y / x -> range = radius / tan

    // 1 STD dev = the angle of attack. Means each trial has expected 68.27% chance to hit the
    // target at the given range.
    final Attribute attrSpread =
        new Attribute(
            Math.toDegrees(angleRad),
            ModifierDescription.SEL_ALL,
            ModifierDescription.SPEC_WEAPON_SPREAD);
    final double P_hit = 0.6827;

    // Use a different way of computing the result
    final int projectilesPerRound = 20;
    final BinomialDistribution bin = new BinomialDistribution(P_hit, projectilesPerRound);
    double expectedHits = 0;
    for (int i = 1; i <= projectilesPerRound; ++i) {
      expectedHits += i * bin.pdf(i);
    }

    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);
    final double ans = cut.rangeEffectiveness(range, null);
    assertEquals(expectedHits / projectilesPerRound, ans, 0.0002);
  }

  @Test
  public void testRangeEffectivenessWeaponSpread() {
    final double spread = 1.0;
    final Attribute attrSpread =
        new Attribute(spread, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_SPREAD);

    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.STEP, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(4000.0), InterpolationType.LINEAR, 0.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);

    assertEquals(1.0, cut.rangeEffectiveness(0, modifiersRange10Pct), 0.0);
    assertEquals(0.61, cut.rangeEffectiveness(400, modifiersRange10Pct), 0.01);
  }

  @Test
  public void testRangeEffectivenessWeaponSpreadMaxRange() {
    final double spread = 10.0;
    final double maxRange = 4000.0;
    final Attribute attrSpread =
        new Attribute(spread, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_SPREAD);

    final List<RangeNode> profile = new ArrayList<>();
    profile.add(new RangeNode(TestHelpers.rangeNode(0.0), InterpolationType.LINEAR, 1.0));
    profile.add(new RangeNode(TestHelpers.rangeNode(maxRange), InterpolationType.LINEAR, 1.0));

    final WeaponRangeProfile cut = new WeaponRangeProfile(attrSpread, profile);

    assertEquals(0.0, cut.rangeEffectiveness(maxRange, null), 0.01);
  }

  private void assertShapeEquals(List<Double> expected, List<Double> actual) {
    if (expected.size() < 2) {
      assertEquals(expected.toString(), actual.toString());
      return;
    }

    if (actual.size() < expected.size()) {
      fail("Actual array too short!");
    }

    final Iterator<Double> expectedIt = expected.iterator();
    double prevExpected = expectedIt.next();
    assertEquals(prevExpected, actual.get(0), 0.0);
    double nextExpected = expectedIt.next();
    final double prevActual = Double.NEGATIVE_INFINITY;
    boolean atEnd = false;
    for (final double nextActual : actual) {
      if (atEnd) {
        assertEquals(
            "Went outside of domain of expected values: " + nextActual,
            expected.toString(),
            actual.toString());
      } else if (nextActual == nextExpected) {
        if (!expectedIt.hasNext()) {
          atEnd = true;
        } else {
          nextExpected = expectedIt.next();
        }
      } else if (nextActual > nextExpected) {
        fail("Expected a value less than or equal to: " + nextExpected);
      } else if (nextActual <= prevActual) {
        fail("Must be larger than previous value: " + prevActual);
      }
    }

    if (expectedIt.hasNext()) {
      fail("Not entire range of expected values was covered!");
    }
  }
}
