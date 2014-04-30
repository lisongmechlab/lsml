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
package lisong_mechlab.mwo_data.helpers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class Workbook{
   static public class Worksheet{
      static public class Table{
         static public class Row{
            static public class Cell{
               public String Data;
            }

            @XStreamImplicit(itemFieldName = "Cell")
            public List<Cell> cells;
         }

         @XStreamImplicit(itemFieldName = "Row")
         public List<Row> rows;
      }

      public Table Table;
   }

   public Worksheet Worksheet;
}
