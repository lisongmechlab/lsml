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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This helper class is used for dealing with operating system variants and versions.
 * 
 * @author Emily Björk
 */
public class OS {
    private final static Variant  OS_TYPE;
    private static WindowsVersion WINDOWS_VERSION;

    public enum Variant {
        MacOS, Windows, Unix
    }

    public enum WindowsVersion {
        None, WinOld, // 95, 98, ME, 2000, NT etc
        WinXP, // 2001
        WinServer2003, // 2003
        WinVista, // 2007
        WinServer2008, // 2008
        Win7, // 2009
        Win8, // 2012
        WinServer2012, // 2012
        Win81 // 2013
    }

    static public boolean isWindowsOrNewer(WindowsVersion aLeastVersion) {
        return OS_TYPE == Variant.Windows && WINDOWS_VERSION.ordinal() >= aLeastVersion.ordinal();
    }

    static {
        String os = System.getProperty("os.name");
        Pattern pattern = Pattern.compile("win\\D*?(server)?\\s*(\\d+\\.?\\d*|nt|ce|xp|vista).*",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(os);

        if (matcher.matches()) {
            OS_TYPE = Variant.Windows;
            String version = matcher.group(2).toLowerCase();
            if (matcher.group(1) != null && matcher.group(1).length() > 0) {
                if (version.equals("2003"))
                    WINDOWS_VERSION = WindowsVersion.WinServer2003;
                else if (version.equals("2008"))
                    WINDOWS_VERSION = WindowsVersion.WinServer2008;
                else if (version.equals("2012"))
                    WINDOWS_VERSION = WindowsVersion.WinServer2012;
                else {
                    try {
                        if (Integer.parseInt(version) > 2012) {
                            WINDOWS_VERSION = WindowsVersion.WinServer2012;
                        }
                        else
                            WINDOWS_VERSION = WindowsVersion.None; // Couldn't parse, safer to assume nothing
                    }
                    catch (Throwable t) {
                        WINDOWS_VERSION = WindowsVersion.None;
                    }
                }
            }
            else {
                if (version.equals("95") || version.equals("98") || version.equals("nt") || version.equals("ce")
                        || version.equals("2000"))
                    WINDOWS_VERSION = WindowsVersion.WinOld;
                else if (version.equals("xp"))
                    WINDOWS_VERSION = WindowsVersion.WinXP;
                else if (version.equals("vista"))
                    WINDOWS_VERSION = WindowsVersion.WinVista;
                else if (version.equals("7"))
                    WINDOWS_VERSION = WindowsVersion.Win7;
                else if (version.equals("8"))
                    WINDOWS_VERSION = WindowsVersion.Win8;
                else {
                    try {
                        double d = Double.parseDouble(version);
                        if (d > 8 && d < 90) {
                            WINDOWS_VERSION = WindowsVersion.Win8;
                        }
                        else
                            WINDOWS_VERSION = WindowsVersion.None; // Couldn't parse, safer to assume nothing
                    }
                    catch (Throwable t) {
                        WINDOWS_VERSION = WindowsVersion.None;
                    }
                }
            }
        }
        else if (os.toLowerCase().contains("mac")) {
            OS_TYPE = Variant.MacOS;
        }
        else {
            OS_TYPE = Variant.Unix;
        }
    }
}
