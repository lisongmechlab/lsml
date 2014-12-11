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
package lisong_mechlab.model.item;

import java.io.File;
import java.io.FileWriter;

public class Test {
    public static void main(String[] args) throws Exception {
        File file = new File("test.txt");

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("x");
        fileWriter.close();

        boolean exists = file.exists();
        boolean deleted = false;
        if (exists) {
            deleted = file.delete();
        }
        boolean stillexists = file.exists();

        System.out.println("Exists = " + exists);
        System.out.println("Deleted = " + deleted);
        System.out.println("Still Exists = " + stillexists);
    }
}
