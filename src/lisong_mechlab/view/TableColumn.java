package lisong_mechlab.view;

import javax.swing.table.TableCellRenderer;

import lisong_mechlab.model.chassi.Chassi;

public abstract class TableColumn<T> {
   private final Class<T> type;
   private final String   header;

   public TableColumn(String aHeader, Class<T> aType){
      type = aType;
      header = aHeader;
   }

   final public Class<?> getColumnClass(){
      return type;
   }

   public String header(){
      return header;
   }

   public abstract T value(Chassi aChassi);

   public TableCellRenderer getRenderer(){
      return null;
   }
}
