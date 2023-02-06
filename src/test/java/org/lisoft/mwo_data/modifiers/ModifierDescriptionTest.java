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
package org.lisoft.mwo_data.modifiers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.lisoft.mwo_data.ChassisDB;
import org.lisoft.mwo_data.OmniPodDB;
import org.lisoft.mwo_data.mechs.ChassisStandard;

/**
 * Test suite for {@link ModifierDescription}.
 *
 * @author Li Song
 */
public class ModifierDescriptionTest {
  private final String key = "key";
  private final Operation op = Operation.MUL;
  private final Collection<String> sel = Arrays.asList("foo", "bar");
  private final String spec = "faz";
  private final ModifierType type = ModifierType.INDETERMINATE;
  private final String ui = "name";

  public static void main(String[] args) {
    dumpAllKnownSelectors();
  }

  /**
   * The {@link ModifierDescription#SPEC_ALL} specifier shall apply to all attributes with at least
   * one selector matching, regardless of the specifier.
   */
  @Test
  public void testAffectsAllSpecifier() {
    final Attribute attrFail = makeAttribute(List.of("fail"), "faz");
    final Attribute attr = makeAttribute(List.of("foo"), "faz");
    final Attribute attrNullSpec = makeAttribute(List.of("foo"), null);

    assertFalse(
        new ModifierDescription(ui, key, op, sel, ModifierDescription.SPEC_ALL, type)
            .affects(attrFail));
    assertTrue(
        new ModifierDescription(ui, key, op, sel, ModifierDescription.SPEC_ALL, type)
            .affects(attrNullSpec));
    assertTrue(
        new ModifierDescription(ui, key, op, sel, ModifierDescription.SPEC_ALL, type)
            .affects(attr));
  }

  /**
   * The {@link ModifierDescription} shall affect an {@link Attribute} if both attribute and
   * description have null specifier or if they are equal.
   */
  @Test
  public void testAffectsNullNonNullSpecifier() {
    final Attribute attrNullSpec = makeAttribute(List.of("bar"), null);
    final Attribute attr = makeAttribute(List.of("bar"), "n");

    assertTrue(new ModifierDescription(ui, key, op, sel, null, type).affects(attrNullSpec));
    assertFalse(new ModifierDescription(ui, key, op, sel, spec, type).affects(attrNullSpec));
    assertFalse(new ModifierDescription(ui, key, op, sel, null, type).affects(attr));
    assertFalse(new ModifierDescription(ui, key, op, sel, spec, type).affects(attr));
  }

  @Test
  public void testAffectsNullSpecifier() {
    final Attribute atribute = makeAttribute(List.of("foo"), "faz");

    assertFalse(new ModifierDescription(ui, key, op, sel, null, type).affects(atribute));
    assertTrue(new ModifierDescription(ui, key, op, sel, spec, type).affects(atribute));
  }

  /**
   * The {@link ModifierDescription#affects(Attribute)} shall return true if the {@link Attribute}
   * has at least one of the selectors of the description. Or the description has the {@link
   * ModifierDescription#SEL_ALL} selector.
   */
  @Test
  public void testAffectsSelectors() {
    final Attribute attrFail = makeAttribute(List.of("fail"), "faz");
    final Attribute attr = makeAttribute(List.of("foo"), "faz");

    assertTrue(
        new ModifierDescription(ui, key, op, ModifierDescription.SEL_ALL, spec, type)
            .affects(attrFail));
    assertTrue(
        new ModifierDescription(ui, key, op, ModifierDescription.SEL_ALL, spec, type)
            .affects(attr));

    assertTrue(new ModifierDescription(ui, key, op, sel, spec, type).affects(attr));
    assertFalse(new ModifierDescription(ui, key, op, sel, spec, type).affects(attrFail));
  }

  @Test
  public void testConstruction() {
    final ModifierDescription cut = new ModifierDescription(ui, key, op, sel, spec, type);
    assertEquals(key, cut.getKey());
    assertEquals(type, cut.getModifierType());
    assertEquals(ui, cut.getUiName());
    assertEquals(ui, cut.toString());
    assertEquals(op, cut.getOperation());
    assertEquals(spec, cut.getSpecifier());
    assertTrue(cut.getSelectors().containsAll(sel));
    assertEquals(cut.getSelectors().size(), sel.size());
  }

  @Test
  public void testEquals() {
    final ModifierDescription cut = new ModifierDescription(ui, key, op, sel, spec, type);
    assertEquals(
        cut, new ModifierDescription("name", "key", op, Arrays.asList("bar", "foo"), "faz", type));

    final ModifierDescription nullSpec = new ModifierDescription(ui, key, op, sel, null, type);
    assertEquals(nullSpec, new ModifierDescription(ui, key, op, sel, null, type));
  }

  @Test
  public void testEqualsDiff() {
    final ModifierDescription cut = new ModifierDescription(ui, key, op, sel, spec, type);
    assertNotEquals(cut, new ModifierDescription("x", key, op, sel, spec, type));
    assertNotEquals(cut, new ModifierDescription(ui, "x", op, sel, spec, type));
    assertNotEquals(cut, new ModifierDescription(ui, key, Operation.ADD, sel, spec, type));
    assertNotEquals(cut, new ModifierDescription(ui, key, op, List.of("x"), spec, type));
    assertNotEquals(
        cut, new ModifierDescription(ui, key, op, Arrays.asList("foo", "bar", "x"), spec, type));
    assertNotEquals(cut, new ModifierDescription(ui, key, op, List.of("foo"), spec, type));
    assertNotEquals(cut, new ModifierDescription(ui, key, op, sel, "x", type));
    assertNotEquals(cut, new ModifierDescription(ui, key, op, sel, null, type));
    assertNotEquals(
        cut, new ModifierDescription(ui, key, op, sel, spec, ModifierType.POSITIVE_GOOD));

    final ModifierDescription nullSpec = new ModifierDescription(ui, key, op, sel, null, type);
    assertNotEquals(nullSpec, new ModifierDescription(ui, key, op, sel, spec, type));
  }

  @Test
  public void testEqualsNull() {
    final ModifierDescription cut = new ModifierDescription(ui, key, op, sel, spec, type);
    assertNotEquals(null, cut);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSelectorsIsUnmodifiable() {
    final ModifierDescription cut = new ModifierDescription(ui, key, op, sel, spec, type);
    cut.getSelectors().add("failure");
  }

  private static void dumpAllKnownSelectors() {
    Set<String> a =
        ChassisDB.lookupAll().stream()
            .filter(c -> c instanceof ChassisStandard)
            .map(c -> (ChassisStandard) c)
            .flatMap(c -> c.getQuirks().stream())
            .flatMap(m -> m.getDescription().getSelectors().stream())
            .collect(Collectors.toSet());

    Set<String> b =
        OmniPodDB.all().stream()
            .flatMap(o -> o.getQuirks().stream())
            .flatMap(m -> m.getDescription().getSelectors().stream())
            .collect(Collectors.toSet());

    /*
    ChassisDB.lookupAll().stream()
            .filter(c -> c instanceof ChassisStandard)
            .map(c -> (ChassisStandard) c)
            .filter(c -> c.getQuirks().stream().anyMatch(modifier -> modifier.getDescription().getSelectors().contains("all")))
                    .forEachOrdered(System.out::println);*/

    a.addAll(b);
    a.stream().sorted().forEach(System.out::println);
    /*
            2021-08-26
    ac
    accellerp
    all
    armorresist
    atm
    ballistic
    captureaccelerator
    clanantimissilesystem
    clanerlaser
    clanerppc
    clangaussrifle
    clanheavymediumlaser
    clanlargepulselsr
    clanlaser
    clanlbxautocannon
    clanlbxautocannon10
    clanmachinegun
    clannarcbeacon
    clanppc
    clanultraautocannon5
    critchance
    decellerp
    energy
    erlaser
    erppc
    externalheat
    gaussrifle
    heatdissipation
    heavylaser
    internalresist
    isantimissilesystem
    isautocannon10
    isautocannon2
    isautocannon20
    isautocannon5
    iserlargelaser
    isermediumlaser
    iserppc
    isflamer
    isgaussrifle
    islargelaser
    islargepulselaser
    islbxautocannon
    islbxautocannon10
    islightppc
    islrm
    islrm10
    islrm15
    islrm20
    islrm5
    ismachinegun
    ismediumlaser
    ismediumpulselaser
    isnarcbeacon
    issrm
    issrm4
    isstdlaser
    isstreaksrm
    isstreaksrm2
    isultraautocannon5
    jumpjets
    laser
    lbxautocannon
    lrm
    mediumpulselaser
    missile
    narcbeacon
    nonpulselaser
    overheatdamage
    ppcfamily
    pulselaser
    reversespeed
    rocketlauncher
    rocketlauncher15
    rotaryautocannon
    sensorrange
    srm
    stealtharmorcooldown
    streaksrm
    targetdecayduration
    torso
    turnlerp
    turnrate
    ultraautocannon
    ultraautocannon20
             */
  }

  private Attribute makeAttribute(Collection<String> aSelectors, String aSpecifier) {
    final Attribute atribute = mock(Attribute.class);
    when(atribute.getSpecifier()).thenReturn(aSpecifier);
    when(atribute.getSelectors()).thenReturn(aSelectors);
    return atribute;
  }
}
