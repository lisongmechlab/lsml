package lisong_mechlab.model.mwo_parsing.helpers;

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
