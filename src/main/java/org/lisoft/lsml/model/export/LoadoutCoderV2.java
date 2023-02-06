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
package org.lisoft.lsml.model.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.lisoft.lsml.command.*;
import org.lisoft.lsml.model.ConsumableDB;
import org.lisoft.lsml.model.loadout.ConfiguredComponentStandard;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.util.Huffman1;
import org.lisoft.mwo_data.ChassisDB;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.equipment.ArmourUpgrade;
import org.lisoft.mwo_data.equipment.GuidanceUpgrade;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.equipment.HeatSinkUpgrade;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.StructureUpgrade;
import org.lisoft.mwo_data.equipment.UpgradeDB;
import org.lisoft.mwo_data.mechs.ArmourSide;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.ChassisStandard;
import org.lisoft.mwo_data.mechs.Location;

/**
 * The Second version of {@link LoadoutCoder} for LSML.
 *
 * @author Li Song
 */
public class LoadoutCoderV2 implements LoadoutCoder {
  private static final int HEADER_MAGIC = 0xAC + 1;
  private final Huffman1<Integer> huff;
  private final LoadoutFactory loadoutFactory;

  @Inject
  public LoadoutCoderV2(LoadoutFactory aLoadoutFactory) {
    loadoutFactory = aLoadoutFactory;
    try (InputStream is = getClass().getResourceAsStream("/coderstats_v2.bin");
        ObjectInputStream in = new ObjectInputStream(is)) {
      @SuppressWarnings("unchecked")
      final Map<Integer, Integer> freqs = (Map<Integer, Integer>) in.readObject();
      huff = new Huffman1<>(freqs, null);

      // for(Map.Entry<Integer, Integer> e : freqs.entrySet())
      // System.out.println("["+e.getKey() + "] = " + e.getValue());
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean canDecode(byte[] aBitStream) {
    final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
    return buffer.read() == HEADER_MAGIC;
  }

  @Override
  public LoadoutStandard decode(final byte[] aBitStream) throws Exception {
    final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
    final LoadoutStandard loadout;
    final CommandStack stack = new CommandStack(0);

    // Read header
    {
      if (buffer.read() != HEADER_MAGIC) {
        throw new DecodingException("Wrong format!"); // Wrong format
      }

      // We have 8 bits that contains the old efficiencies prior
      // to the skill-tree update. Read past it.
      buffer.read();

      // 16 bits contain chassis ID (Big endian, respecting RFC 1700)
      final short chassiId = (short) ((buffer.read() & 0xFF) << 8 | buffer.read() & 0xFF);

      final Chassis chassis = ChassisDB.lookup(chassiId);
      if (!(chassis instanceof ChassisStandard)) {
        throw new DecodingException("LSML link format v2 does not support omni mechs.");
      }
      loadout = (LoadoutStandard) loadoutFactory.produceEmpty(chassis);
    }

    // Armour values next, RA, RT, RL, HD, CT, LT, LL, LA
    // 1 byte per armour value (2 for RT,CT,LT front first)
    for (final Location location : Location.RIGHT_TO_LEFT) {
      final ConfiguredComponentStandard component = loadout.getComponent(location);
      for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
        stack.pushAndApply(new CmdSetArmour(null, loadout, component, side, buffer.read(), true));
      }
    }

    // Items are encoded as a list of integers which record the item ID.
    // Components are separated by -1.
    // The order is the same as for armour: RA, RT, RL, HD, CT, LT, LL, LA
    {
      final byte[] rest = new byte[buffer.available()];
      try {
        buffer.read(rest);
      } catch (final IOException e) {
        throw new DecodingException(e);
      }
      final List<Integer> ids = huff.decode(rest);
      stack.pushAndApply(
          new CmdSetArmourType(null, loadout, (ArmourUpgrade) UpgradeDB.lookup(ids.get(0))));
      stack.pushAndApply(
          new CmdSetStructureType(null, loadout, (StructureUpgrade) UpgradeDB.lookup(ids.get(1))));
      stack.pushAndApply(
          new CmdSetHeatSinkType(null, loadout, (HeatSinkUpgrade) UpgradeDB.lookup(ids.get(2))));
      stack.pushAndApply(
          new CmdSetGuidanceType(null, loadout, (GuidanceUpgrade) UpgradeDB.lookup(ids.get(3))));

      if (-1 != ids.get(4)) {
        throw new DecodingException("Broken LSML link, expected separator got: " + ids.get(4));
      }
      ids.remove(4);
      ids.remove(3);
      ids.remove(2);
      ids.remove(1);
      ids.remove(0);

      for (final Location location : Location.RIGHT_TO_LEFT) {
        Integer v;
        final List<Item> later = new ArrayList<>();
        while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
          final Item item = ItemDB.lookup(v);
          if (item instanceof HeatSink) {
            later.add(item); // Add heat sinks last after engine has
            // been added
            continue;
          }
          stack.pushAndApply(
              new CmdAddItem(null, loadout, loadout.getComponent(location), ItemDB.lookup(v)));
        }
        for (final Item i : later) {
          stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), i));
        }
      }

      Integer v;
      while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
        stack.pushAndApply(new CmdAddModule(null, loadout, ConsumableDB.lookup(v)));
      }
    }
    return loadout;
  }

  @Override
  public byte[] encode(final Loadout aLoadout) throws EncodingException {
    throw new EncodingException("Protocol version 2 encoding is no longer allowed.");
  }
}
