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
package lisong_mechlab.view;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * This class is used as a TableColumn for a JTable that displays a derived attribute from some data in a cell of the
 * table model.
 * 
 * @author Emily Björk
 */
abstract public class AttributeTableColumn extends TableColumn{
   private static final long serialVersionUID = 7314642485571311021L;
   private final JLabel      renderer         = new JLabel();

   public AttributeTableColumn(Object aHeader, int aModelIndex){
      this(aHeader, aModelIndex, null);
   }

   public AttributeTableColumn(Object aHeader, int aModelIndex, final String aTooltip){
      super(aModelIndex);
      setHeaderValue(aHeader);
      renderer.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));

      if( aTooltip != null ){
         setHeaderRenderer(new TableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn){
               final TableCellRenderer tableRenderer = aTable.getTableHeader().getDefaultRenderer();
               final JComponent component = (JComponent)tableRenderer.getTableCellRendererComponent(aTable, aValue, aIsSelected, aHasFocus, aRow,
                                                                                                    aColumn);
               component.setToolTipText(aTooltip);
               return component;
            }
         });
      }
   }

   /**
    * Gets the derived value of this column for the given model value.
    * 
    * @param aSourceRowObject
    *           The matching cell from the model that this column is rendering. I.e. model[current_row][aModelIndex].
    * @return The text string to display
    */
   public abstract String valueOf(Object aSourceRowObject);

   /**
    * This method gets called so that the user may style the renderer
    * 
    * @param aJLabel
    *           The label that is used for rendering the string.
    * @param aSourceRowObject
    *           The object that is being rendered.
    * @param isSelected
    *           True if the cell is selected.
    * @param hasFocus
    *           True if the cell has focus.
    */
   @SuppressWarnings("unused")
   public void styleLabel(JLabel aJLabel, Object aSourceRowObject, boolean isSelected, boolean hasFocus){/* NO-OP */}

   @Override
   public final TableCellRenderer getCellRenderer(){
      return new TableCellRenderer(){
         @Override
         public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn){
            styleLabel(renderer, aValue, aIsSelected, aHasFocus);
            renderer.setText(valueOf(aValue));
            return renderer;
         }
      };
   }
}
