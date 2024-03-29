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
package org.lisoft.lsml.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This helper class is used for dealing with operating system variants and versions.
 *
 * @author Li Song
 */
public class OS {
  private static final Variant OS_TYPE;
  private static WindowsVersion WINDOWS_VERSION;

  static {
    String os = System.getProperty("os.name");
    Pattern pattern =
        Pattern.compile(
            "win\\D*?(server)?\\s*(\\d+\\.?\\d*|nt|ce|xp|vista).*", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(os);

    if (matcher.matches()) {
      OS_TYPE = Variant.WINDOWS;
      String version = matcher.group(2).toLowerCase();
      if (matcher.group(1) != null && matcher.group(1).length() > 0) {
        switch (version) {
          case "2003":
            WINDOWS_VERSION = WindowsVersion.WIN_SERVER_2003;
            break;
          case "2008":
            WINDOWS_VERSION = WindowsVersion.WIN_SERVER_2008;
            break;
          case "2012":
            WINDOWS_VERSION = WindowsVersion.WIN_SERVER_2012;
            break;
          default:
            try {
              if (Integer.parseInt(version) > 2012) {
                WINDOWS_VERSION = WindowsVersion.WIN_SERVER_2012;
              } else {
                WINDOWS_VERSION = WindowsVersion.NONE; // Couldn't parse, safer to assume nothing
              }
            } catch (Throwable t) {
              WINDOWS_VERSION = WindowsVersion.NONE;
            }
            break;
        }
      } else {
        switch (version) {
          case "95":
          case "98":
          case "nt":
          case "ce":
          case "2000":
            WINDOWS_VERSION = WindowsVersion.WIN_OLD;
            break;
          case "xp":
            WINDOWS_VERSION = WindowsVersion.WIN_XP;
            break;
          case "vista":
            WINDOWS_VERSION = WindowsVersion.WIN_VISTA;
            break;
          case "7":
            WINDOWS_VERSION = WindowsVersion.WIN_7;
            break;
          case "8":
            WINDOWS_VERSION = WindowsVersion.WIN_8;
            break;
          default:
            try {
              double d = Double.parseDouble(version);
              if (d > 8 && d < 90) {
                WINDOWS_VERSION = WindowsVersion.WIN_8;
              } else {
                WINDOWS_VERSION = WindowsVersion.NONE; // Couldn't parse, safer to assume nothing
              }
            } catch (Throwable t) {
              WINDOWS_VERSION = WindowsVersion.NONE;
            }
            break;
        }
      }
    } else if (os.toLowerCase().contains("mac")) {
      OS_TYPE = Variant.MAC_OS;
    } else {
      OS_TYPE = Variant.UNIX;
    }
  }

  public static boolean isWindowsOrNewer(WindowsVersion aLeastVersion) {
    return OS_TYPE == Variant.WINDOWS && WINDOWS_VERSION.ordinal() >= aLeastVersion.ordinal();
  }

  public enum Variant {
    MAC_OS,
    WINDOWS,
    UNIX
  }

  public enum WindowsVersion {
    NONE,
    WIN_OLD, // 95, 98, ME, 2000, NT etc
    WIN_XP, // 2001
    WIN_SERVER_2003, // 2003
    WIN_VISTA, // 2007
    WIN_SERVER_2008, // 2008
    WIN_7, // 2009
    WIN_8, // 2012
    WIN_SERVER_2012, // 2012
    WIN_8_1 // 2013
  }
}
