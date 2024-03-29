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
package org.lisoft.mwo_data.mwo_parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
class XMLHardpoints {
  @XStreamImplicit(itemFieldName = "Hardpoint")
  List<HardPointInfo> hardpoints;

  @XStreamImplicit(itemFieldName = "WeaponDoorSet")
  List<WeaponDoorSet> weapondoors;

  static XMLHardpoints fromXml(InputStream is) {
    XStream xstream = GameVFS.makeMwoSuitableXStream();
    xstream.alias("Hardpoints", XMLHardpoints.class);
    xstream.alias("HardPoint", HardPointInfo.class);
    xstream.alias("WeaponSlot", HardPointWeaponSlot.class);
    return (XMLHardpoints) xstream.fromXML(is);
  }

  int slotsForId(int aID) {
    for (HardPointInfo hardPointInfo : hardpoints) {
      if (hardPointInfo.id == aID) {
        return hardPointInfo.weaponslots.size();
      }
    }
    throw new RuntimeException("Problem reading hardpoint info!");
  }

  List<Integer> tubesForId(int aID) {
    Pattern pattern =
        Pattern.compile(".*(?:(?:[ls]rm)|(?:missile))(\\d+).*", Pattern.CASE_INSENSITIVE);

    for (HardPointInfo hardPointInfo : hardpoints) {
      if (hardPointInfo.id == aID) {
        List<Integer> tubes = new ArrayList<>();
        for (HardPointWeaponSlot weaponslot : hardPointInfo.weaponslots) {
          int maxTubes = 0;
          for (HardPointWeaponSlot.Attachment attachment : weaponslot.attachments) {
            Matcher matcher = pattern.matcher(attachment.AName);
            if (matcher.matches() && matcher.groupCount() == 1) {
              maxTubes = Math.max(maxTubes, Integer.parseInt(matcher.group(1)));
            } else if (attachment.AName.toLowerCase().contains("narc")) {
              maxTubes = Math.max(1, maxTubes);
            }
          }
          tubes.add(maxTubes);
        }
        return tubes;
      }
    }
    throw new RuntimeException("Problem reading hardpoint couldn't parse missile tube counts!");
  }
}
