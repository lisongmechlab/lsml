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
package org.lisoft.lsml.application;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import javax.inject.Inject;

/**
 * This class integrates with the OS. For now it provides "sticky" on Windows 7 and above where the application can be
 * pinned to the task bar.
 *
 * @author Li Song
 */
public class DefaultOSIntegration implements OSIntegration {

    @Inject
    public DefaultOSIntegration() {
        /* NOP */
    }

    @Override
    public void setup() {
        if (OS.isWindowsOrNewer(OS.WindowsVersion.WIN_7)) {
            try {
                // Setup AppUserModelID if windows 7 or later.
                Native.register("shell32");
                setCurrentProcessExplicitAppUserModelID(LiSongMechLab.class.getName());
                Native.unregister();
            } catch (final Throwable t) {
                System.out.println("Couldn't call into shell32.dll!");
                System.out.println(t.getMessage());
            }
        }
    }

    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

    private static void setCurrentProcessExplicitAppUserModelID(final String appID) {
        if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0) {
            throw new RuntimeException("Unable to set current process explicit AppUserModelID to: " + appID);
        }
    }

}
