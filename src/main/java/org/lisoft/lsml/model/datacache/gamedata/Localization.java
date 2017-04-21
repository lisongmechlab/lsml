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
package org.lisoft.lsml.model.datacache.gamedata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.gamedata.helpers.Workbook;

import com.thoughtworks.xstream.XStream;

/**
 * This class will provide localization (and implicitly all naming) of items through the MWO data files.
 * <p>
 * Caution: This class will only be initialized if the {@link DataCache} performs a cache update.
 *
 * FIXME: Replace with non-singleton
 *
 * @author Emily
 */
public class Localization {
    private static Map<String, String> key2string = null;

    public static void initialize(GameVFS aGameVFS) throws IOException {
        key2string = new HashMap<>();

        final File[] files = new File[] { new File("Game/Localized/Languages/TheRealLoc.xml") };
        /*
         * , new File("Game/Localized/Languages/ui_Mech_Loc.xml"), new File("Game/Localized/Languages/General.xml"), new
         * File("Game/Localized/Languages/Mechlab.xml"), new File("Game/Localized/Languages/text_ui_menus.xml")};
         */
        /*
         * for(File file : files){ try{ XmlReader reader = new XmlReader(dataFile.openGameFile(file)); for(Element row :
         * reader.getElementsByTagName("Row")){ List<Element> cells = reader.getElementsByTagName("Cell", row); if(
         * cells.size() < 3 || !cells.get(0).getAttribute("ss:Index").equals("2") ){ continue; } List<Element> data0 =
         * reader.getElementsByTagName("Data", cells.get(0)); List<Element> data2 = reader.getElementsByTagName("Data",
         * cells.get(2)); if( data0.size() != 1 || data2.size() != 1 ){ continue; } String tag0 =
         * canonize(reader.getTagValue("Data", cells.get(0))); String tag2 = reader.getTagValue("Data", cells.get(2));
         * key2string.put(tag0, tag2); System.out.println(file + " ## " + tag0 + " = " + tag2); } } catch( Exception e
         * ){ throw new RuntimeException(e); } }
         */

        final XStream xstream = DataCache.makeMwoSuitableXStream();
        xstream.alias("Workbook", Workbook.class);
        for (final File file : files) {
            final Workbook workbook = (Workbook) xstream.fromXML(aGameVFS.openGameFile(file).stream);
            for (final Workbook.Worksheet.Table.Row row : workbook.Worksheet.Table.rows) { // Skip past junk
                if (row.cells == null || row.cells.size() < 1) {
                    // debugprintrow(row);
                    continue;
                }
                if (row.cells.get(0).Data == null) {
                    // debugprintrow(row);
                    continue;
                }
                if (row.cells.size() >= 2) {
                    final String key = row.cells.get(0).Data;
                    final String data = row.cells.get(1).Data;
                    if (data == null || data.length() < 2) {
                        debugprintrow(row);
                    }
                    final String canonname = canonize(key);
                    key2string.put(canonname, data);
                }
                else {
                    debugprintrow(row); // Debug Breakpoint
                }
            }
        }

    }

    public static String key2string(String aKey) {
        final String canon = canonize(aKey);
        if (!key2string.containsKey(canon)) {
            if (aKey.contains("_desc")) {
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
        if (aKey.contains("_mk")) {
            canonized = aKey.replaceAll("_mkvi", "_mk6");
            canonized = aKey.replaceAll("_mkv", "_mk5");
            canonized = aKey.replaceAll("_mkiv", "_mk4");
            canonized = aKey.replaceAll("_mkiii", "_mk3");
            canonized = aKey.replaceAll("_mkii", "_mk2");
            canonized = aKey.replaceAll("_mki", "_mk1");
            canonized = aKey.replaceAll("_mkl", "_mk1"); // They've mistaken an l (ell) for an 1 (one)
        }

        // canonized = canonized.replace("gaussrifle", "gauss");
        // canonized = canonized.replace("largelaser", "ll");
        // canonized = canonized.replace("largepulselaser", "lpl");
        // canonized = canonized.replace("mediumlaser", "ml");
        // canonized = canonized.replace("mediumpulselaser", "mpl");
        // canonized = canonized.replace("smalllaser", "sl");
        // canonized = canonized.replace("smallpulselaser", "spl");
        // canonized = canonized.replace("autocannon", "ac");
        // canonized = canonized.replace("multiplier", "mult");
        // canonized = canonized.replace("additive", "add");

        if (!canonized.startsWith("@")) {
            canonized = "@" + canonized;
        }

        return canonized;
    }

    @SuppressWarnings("unused")
    static private void debugprintrow(Workbook.Worksheet.Table.Row row) {
        // if( row.cells != null ){
        // System.out.print("{");
        // for(Workbook.Worksheet.Table.Row.Cell cell : row.cells){
        // System.out.print(cell.Data + "##");
        // }
        // System.out.println("}");
        // }
    }
}
