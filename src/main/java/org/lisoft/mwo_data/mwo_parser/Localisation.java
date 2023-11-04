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
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.lisoft.mwo_data.Database;
import org.lisoft.mwo_data.mwo_parser.GameVFS.GameFile;

/**
 * This class will provide localization (and implicitly all naming) of items through the MWO data
 * files.
 *
 * <p>Caution: This class will only be initialized if the {@link Database} performs a database
 * update.
 *
 * @author Li Song
 */
class Localisation {
  private static final String LOCALISATION_XML_FILE =
      "Game/Localized/Localization/English/TheRealLoc.xml";
  private final Map<String, String> key2string = new HashMap<>();

  public Localisation(GameVFS aGameVFS) throws Exception {
    final File[] files = new File[] {new File(LOCALISATION_XML_FILE)};

    final XStream xstream = GameVFS.makeMwoSuitableXStream();
    xstream.alias("Workbook", Workbook.class);
    for (final File filePath : files) {
      try (GameFile file = aGameVFS.openGameFile(filePath)) {
        final Workbook workbook = (Workbook) xstream.fromXML(file.stream);
        for (final Workbook.Worksheet.Table.Row row : workbook.Worksheet.Table.rows) {
          if (row.cells == null || row.cells.size() < 1) {
            // Skip past junk
            continue;
          }
          if (row.cells.get(0).Data == null) {
            continue;
          }
          if (row.cells.size() >= 2) {
            final String key = row.cells.get(0).Data;
            final String data = row.cells.get(1).Data;
            key2string.put(canonize(key), data);
          }
        }
      }
    }

    // PGI messed up again, add a patch *sigh*. Issue #804.
    key2string.putIfAbsent("@fnr-j", "JAILBIRD");
  }

  public String key2string(String aKey) {
    final String canon = canonize(aKey);
    if (!key2string.containsKey(canon)) {
      if (canon.contains("_desc") || canon.endsWith("desc")) {
        return "Empty Description";
      }
      throw new IllegalArgumentException("No such key found!: " + canon);
    }
    return key2string.get(canon);
  }

  private static String canonize(String aKey) {
    String canonized = aKey;
    // We need to normalize MKII -> MK2 etc
    canonized = canonized.toLowerCase();
    if (canonized.contains("_mk")) {
      // The order here is important, we cannot replace substrings of longer matches before the
      // longer
      // matches are tried.
      canonized = canonized.replaceAll("_mkiii", "_mk3");
      canonized = canonized.replaceAll("_mkii", "_mk2");
      canonized = canonized.replaceAll("_mkiv", "_mk4");
      canonized = canonized.replaceAll("_mki", "_mk1");
      canonized = canonized.replaceAll("_mkvi", "_mk6");
      canonized = canonized.replaceAll("_mkv", "_mk5");
      canonized =
          canonized.replaceAll("_mkl", "_mk1"); // They've mistaken an l (ell) for an 1 (one)
    }
    if (canonized.endsWith("_ad")) {
      // Really PGI?, really?
      canonized = canonized + "d";
    }

    canonized = canonized.replaceAll("_multiplier", "_mult");
    canonized = canonized.replaceAll("_additive", "_add");
    canonized = canonized.replaceAll("_longrange_", "_range_");
    canonized = canonized.replaceAll("_maxrange_", "_range_");

    if (!canonized.startsWith("@")) {
      canonized = "@" + canonized;
    }
    return canonized;
  }
}
