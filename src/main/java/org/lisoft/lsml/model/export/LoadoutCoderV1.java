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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.command.CmdSetArmourType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.export.garage.CompatibilityHelper;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.util.Huffman1;

/**
 * The first version of {@link LoadoutCoder} for LSML.
 *
 * @author Li Song
 */
public class LoadoutCoderV1 implements LoadoutCoder {
    private static final int HEADER_MAGIC = 0xAC;
    private final Huffman1<Integer> huff;

    public LoadoutCoderV1() {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("coderstats.bin");
                ObjectInputStream in = new ObjectInputStream(is);) {
            @SuppressWarnings("unchecked")
            final Map<Integer, Integer> freqs = (Map<Integer, Integer>) in.readObject();
            huff = new Huffman1<>(freqs, null);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canDecode(byte[] aBitStream) {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
        return buffer.read() == HEADER_MAGIC;
    }

    @Override
    public LoadoutStandard decode(final byte[] aBitStream) throws DecodingException, Exception {
        final ByteArrayInputStream buffer = new ByteArrayInputStream(aBitStream);
        final LoadoutStandard loadout;
        final CommandStack stack = new CommandStack(0);

        // Read header
        {
            if (buffer.read() != HEADER_MAGIC) {
                throw new DecodingException("Wrong format!"); // Wrong format
            }

            final int upeff = buffer.read() & 0xFF; // 8 bits for efficiencies and
            // 16 bits contain chassi ID.
            final short chassiId = (short) ((buffer.read() & 0xFF) << 8 | buffer.read() & 0xFF); // Big endian,
                                                                                                 // respecting
            // RFC
            // 1700

            final ChassisStandard chassis = (ChassisStandard) ChassisDB.lookup(chassiId);
            loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassis);

            final boolean artemisIv = (upeff & 1 << 7) != 0;
            final boolean endoSteel = (upeff & 1 << 4) != 0;
            final boolean ferroFib = (upeff & 1 << 5) != 0;
            final boolean dhs = (upeff & 1 << 6) != 0;
            final GuidanceUpgrade guidance = artemisIv ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STD_GUIDANCE;
            final StructureUpgrade structure = endoSteel ? UpgradeDB.IS_ES_STRUCTURE : UpgradeDB.IS_STD_STRUCTURE;
            final ArmourUpgrade armour = ferroFib ? UpgradeDB.IS_FF_ARMOUR : UpgradeDB.IS_STD_ARMOUR;
            final HeatSinkUpgrade heatSinks = dhs ? UpgradeDB.IS_DHS : UpgradeDB.IS_SHS;

            stack.pushAndApply(new CmdSetGuidanceType(null, loadout, guidance));
            stack.pushAndApply(new CmdSetHeatSinkType(null, loadout, heatSinks));
            stack.pushAndApply(new CmdSetStructureType(null, loadout, structure));
            stack.pushAndApply(new CmdSetArmourType(null, loadout, armour));
            loadout.getEfficiencies().setEfficiency(MechEfficiencyType.COOL_RUN, (upeff & 1 << 3) != 0, null);
            loadout.getEfficiencies().setEfficiency(MechEfficiencyType.HEAT_CONTAINMENT, (upeff & 1 << 2) != 0, null);
            loadout.getEfficiencies().setEfficiency(MechEfficiencyType.SPEED_TWEAK, (upeff & 1 << 1) != 0, null);
            loadout.getEfficiencies().setDoubleBasics((upeff & 1 << 0) != 0, null);
        }

        // Armour values next, RA, RT, RL, HD, CT, LT, LL, LA
        // 1 byte per armour value (2 for RT,CT,LT front first)
        for (final Location location : Location.right2Left()) {
            final ConfiguredComponentStandard component = loadout.getComponent(location);
            for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                stack.pushAndApply(new CmdSetArmour(null, loadout, component, side, buffer.read(), true));
            }
        }

        // Items are encoded as a list of integers which record the item ID. Components are separated by -1.
        // The order is the same as for armour: RA, RT, RL, HD, CT, LT, LL, LA
        {
            final byte[] rest = new byte[buffer.available()];
            try {
                buffer.read(rest);
            }
            catch (final IOException e) {
                throw new DecodingException(e);
            }
            final List<Integer> ids = huff.decode(rest);
            for (final Location location : Location.right2Left()) {
                Integer v;
                final List<Item> later = new ArrayList<>();
                while (!ids.isEmpty() && -1 != (v = ids.remove(0))) {
                    final Item pItem = ItemDB.lookup(v);
                    final Item item = CompatibilityHelper.fixArtemis(pItem, loadout.getUpgrades().getGuidance());
                    if (item instanceof HeatSink) {
                        later.add(item); // Add heat sinks last after engine has been added
                        continue;
                    }
                    stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), item));
                }
                for (final Item i : later) {
                    stack.pushAndApply(new CmdAddItem(null, loadout, loadout.getComponent(location), i));
                }
            }
        }
        return loadout;
    }

    @Override
    public byte[] encode(final Loadout aLoadout) throws EncodingException {
        throw new EncodingException("Protocol version 1 encoding is no longer allowed.");
    }
}
