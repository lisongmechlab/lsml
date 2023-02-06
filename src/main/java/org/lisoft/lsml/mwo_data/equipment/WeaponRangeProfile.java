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
package org.lisoft.lsml.mwo_data.equipment;

import java.util.*;
import org.lisoft.lsml.math.probability.GaussianDistribution;
import org.lisoft.lsml.mwo_data.equipment.WeaponRangeProfile.RangeNode.InterpolationType;
import org.lisoft.lsml.mwo_data.modifiers.Attribute;
import org.lisoft.lsml.mwo_data.modifiers.Modifier;
import org.lisoft.lsml.mwo_data.modifiers.ModifierDescription;

/**
 * This class abstracts the range profiles for weapons.
 *
 * @author Li Song
 */
public class WeaponRangeProfile {

  public static class Range {
    public final double minimum;
    public final double maximum;

    public Range(double aMinimum, double aMaximum) {
      minimum = aMinimum;
      maximum = aMaximum;
    }
  }

  public static class RangeNode {
    private final double damageModifier;
    private final Double exponent;
    private final Attribute start;
    private final InterpolationType typeToNext;

    /**
     * Creates a new non-exponential range node.
     *
     * @param aStartRange The start range of the node.
     * @param aInterpolationType The {@link InterpolationType} of from this node to the next. Must
     *     not be {@link InterpolationType#EXPONENTIAL}.
     * @param aDamageModifier The damage multiplier this node has.
     */
    public RangeNode(
        Attribute aStartRange, InterpolationType aInterpolationType, double aDamageModifier) {
      this(aStartRange, aInterpolationType, aDamageModifier, null);
    }

    /**
     * Creates a new range node.
     *
     * @param aStartRange The start range of the node.
     * @param aInterpolationType The {@link InterpolationType} of from this node to the next.
     * @param aDamageModifier The damage multiplier this node has.
     * @param aExponent The exponent for {@link InterpolationType#EXPONENTIAL} must be <code>null
     *     </code> for other interpolation types.
     */
    public RangeNode(
        Attribute aStartRange,
        InterpolationType aInterpolationType,
        double aDamageModifier,
        Double aExponent) {
      if (aInterpolationType == InterpolationType.EXPONENTIAL && null == aExponent) {
        throw new IllegalArgumentException("Exponential range node must specify exponent!");
      } else if (aInterpolationType != InterpolationType.EXPONENTIAL && null != aExponent) {
        throw new IllegalArgumentException("Non exponential range node must not specify exponent!");
      }
      start = aStartRange;
      damageModifier = aDamageModifier;
      typeToNext = aInterpolationType;
      exponent = aExponent;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append('[').append(start).append(", ").append(typeToNext);
      if (null != exponent) {
        sb.append('^').append(exponent);
      }
      sb.append(", ").append(100.0 * damageModifier).append('%').append(']');
      return sb.toString();
    }

    public enum InterpolationType {
      STEP,
      LINEAR,
      EXPONENTIAL;

      /**
       * Converts from a string in the MWO data files to an enumeration value.
       *
       * @param aMwoString The string to convert.
       * @return A value in {@link InterpolationType}.
       */
      public static InterpolationType fromMwo(String aMwoString) {
        return switch (aMwoString.toLowerCase()) {
          case "step" -> STEP;
          case "linear" -> LINEAR;
          case "exponential" -> EXPONENTIAL;
          default -> throw new IllegalArgumentException(
              "Unknown interpolation type: " + aMwoString);
        };
      }
    }
  }

  private static final RangeNode SENTINEL_HEAD =
      new RangeNode(
          new Attribute(Double.NEGATIVE_INFINITY, ModifierDescription.SEL_ALL),
          InterpolationType.STEP,
          0.0);
  private static final RangeNode SENTINEL_TAIL =
      new RangeNode(
          new Attribute(Double.POSITIVE_INFINITY, ModifierDescription.SEL_ALL),
          InterpolationType.STEP,
          0.0);
  private final List<RangeNode> nodes;
  private final Attribute spread; // May be null if no spread

  public WeaponRangeProfile(Attribute aSpread, List<RangeNode> aRangeSpecification) {
    nodes = aRangeSpecification;
    spread = aSpread;

    // Remove repeated "zero" nodes at the tail.
    while (nodes.size() > 1
        && nodes.get(nodes.size() - 2).damageModifier == 0.0
        && nodes.get(nodes.size() - 1).damageModifier == 0.0) {
      nodes.remove(nodes.size() - 1);
    }
  }

  public WeaponRangeProfile(List<RangeNode> aRangeSpecification) {
    this(null, aRangeSpecification);
  }

  /**
   * @return The maximal effectiveness of the profile (some weapons have > 1.0 effectiveness!)
   */
  public double getMaxEffectiveness() {
    double modifier = Double.NEGATIVE_INFINITY;
    for (final RangeNode n : nodes) {
      modifier = Math.max(modifier, n.damageModifier);
    }
    return modifier;
  }

  /**
   * @param aModifiers A {@link Collection} of {@link Modifier}s that can affect the result.
   * @return The maximum range up-to which the profile has a non-zero damage multiplier.
   */
  public double getMaxRange(Collection<Modifier> aModifiers) {
    if (nodes.isEmpty()) {
      return 0.0;
    }

    return nodes.get(nodes.size() - 1).start.value(aModifiers);
  }

  /**
   * @param aModifiers A {@link Collection} of {@link Modifier}s that can affect the result.
   * @return A {@link Range} that shows the min/max range at which optimal weapon damage can be
   *     done.
   */
  public Range getOptimalRange(Collection<Modifier> aModifiers) {
    RangeNode first = nodes.get(0);
    RangeNode last = first;
    for (final RangeNode n : nodes) {
      if (n.damageModifier > first.damageModifier) {
        first = n;
        last = n;
      } else if (n.damageModifier == first.damageModifier || first == last) {
        last = n;
      }
    }
    return new Range(first.start.value(aModifiers), last.start.value(aModifiers));
  }

  /**
   * Gets the largest range where the weapon does a percentile of its maximal damage (taking damage
   * multipliers > 1.0 into account too) in steps of 10 meters.
   *
   * @param aPercentile The percentile of damage to get the range for.
   * @param aModifiers A {@link Collection} of {@link Modifier}s that could affect the result.
   * @return A {@link Range} that represents the minimum and maximum range at which the weapon does
   *     the percentile damage.
   */
  public Range getPercentileRange(double aPercentile, Collection<Modifier> aModifiers) {
    final List<Double> samplePoints = getPolygonTrainRanges(10, aModifiers);
    final List<Double> damagePoints =
        samplePoints.stream().map(x -> rangeEffectiveness(x, aModifiers)).toList();

    final Optional<Double> max = damagePoints.stream().max(Comparator.naturalOrder());
    if (max.isEmpty()) {
      return new Range(0.0, 0.0);
    }
    final double threshold = max.get() * aPercentile;

    Range longestRange = null;
    double longestLength = Double.NEGATIVE_INFINITY;
    double rangeStart = Double.NEGATIVE_INFINITY;

    double prevDamage = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < samplePoints.size(); ++i) {
      final double currDamage = damagePoints.get(i);

      if (prevDamage < threshold && currDamage >= threshold) {
        rangeStart = samplePoints.get(i);
      } else if (prevDamage >= threshold && currDamage < threshold) {
        final double rangeEnd = samplePoints.get(i - 1);
        final double rangeLength = rangeEnd - rangeStart;
        if (rangeLength > longestLength) {
          longestLength = rangeLength;
          longestRange = new Range(rangeStart, rangeEnd);
        }
      }
      prevDamage = currDamage;
    }
    return longestRange;
  }

  /**
   * Computes a list of range values that should be used when drawing the range profile as a polygon
   * train.
   *
   * @param aDx The step the use in smooth segments of the polygon train.
   * @param aModifiers A collection of {@link Modifier} that could affect the ranges.
   * @return A {@link List} of {@link Double}s with the ranges.
   */
  public List<Double> getPolygonTrainRanges(double aDx, Collection<Modifier> aModifiers) {
    final List<Double> ans = new ArrayList<>();

    if (!nodes.isEmpty()) {
      double nextStep = aDx;
      InterpolationType interpolationType = InterpolationType.LINEAR;
      for (final RangeNode rangeNode : nodes) {
        final double r = rangeNode.start.value(aModifiers);
        while (nextStep < r) {
          ans.add(nextStep);
          nextStep += aDx;
        }

        if (interpolationType == InterpolationType.STEP) {
          ans.add(Math.nextDown(r));
        }
        ans.add(r);
        interpolationType = rangeNode.typeToNext;
      }

      final RangeNode lastNode = nodes.get(nodes.size() - 1);
      if (lastNode.damageModifier != 0.0) {
        ans.add(Math.nextUp(lastNode.start.value(aModifiers)));
      }
    }

    return ans;
  }

  public Attribute getSpread() {
    return spread;
  }

  /**
   * Computes the effectiveness of the weapon at the given range.
   *
   * @param aRange The range to calculate for.
   * @param aModifiers A collection of {@link Modifier}s that might affect the results.
   * @return A scale value to apply to weapon damage at the range.
   */
  public double rangeEffectiveness(double aRange, Collection<Modifier> aModifiers) {
    RangeNode startNode = SENTINEL_HEAD;
    RangeNode endNode = SENTINEL_TAIL;
    for (final RangeNode node : nodes) {
      if (node.start.value(aModifiers) <= aRange) {
        startNode = node;
      } else {
        endNode = node;
        break;
      }
    }

    // Range completely outside the profile, make it zero.
    if (endNode == SENTINEL_TAIL) {
      if (startNode.start.value(aModifiers) == aRange) {
        return startNode.damageModifier * calcSpreadFactor(aRange, aModifiers);
      }
      return 0.0;
    }

    final double spreadFactor = calcSpreadFactor(aRange, aModifiers);
    final double low = startNode.start.value(aModifiers);
    final double high = endNode.start.value(aModifiers);
    final double damageRange = endNode.damageModifier - startNode.damageModifier;
    final double dT = (aRange - low) / (high - low);

    double damageFactor = startNode.damageModifier;
    switch (startNode.typeToNext) {
      case EXPONENTIAL:
        // Unsure how this should work when the damage ramp is negative.
        damageFactor += damageRange * Math.pow(dT, startNode.exponent);
        break;
      case LINEAR:
        damageFactor += damageRange * dT;
        break;
      case STEP:
        break;
      default:
        throw new RuntimeException("Missing interpolation type from switch!");
    }
    return spreadFactor * damageFactor;
  }

  private double calcSpreadFactor(double aRange, Collection<Modifier> aModifiers) {
    if (null != spread) {
      // Assumption:
      // The 'spread' value is the standard deviation of a zero-mean Gaussian distribution of
      // angles.
      final GaussianDistribution gaussianDistribution = new GaussianDistribution();

      final double targetRadius = 6; // [m]
      final double maxAngle = Math.atan2(targetRadius, aRange) * 180 / Math.PI; // [deg]

      // X ~= N(0, spread)
      // P_hit = P(-maxAngle <= X; X <= +maxAngle)
      // Xn = (X - 0) / spread ~ N(0,1)
      // P_hit = cdf(maxAngle / spread) - cdf(-maxAngle / spread) = 2*cdf(maxAngle / spread) - 1.0;
      return 2 * gaussianDistribution.cdf(maxAngle / spread.value(aModifiers)) - 1;
    }
    return 1.0;
  }
}
