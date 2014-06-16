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
package lisong_mechlab.view.mechlab;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JList;

import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.OpAddModule;
import lisong_mechlab.model.loadout.OpRemoveModule;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.mechlab.equipment.ModuleTransferHandler;
import lisong_mechlab.view.render.ItemRenderer;

/**
 * This class implements a JList for {@link PilotModule}s.
 * 
 * @author Emily Björk
 */
public class PilotModuleList extends JList<PilotModule>{
   private static final long    serialVersionUID = -3812414074800032146L;
   private final MessageXBar    xBar;
   private final LoadoutBase<?> loadout;
   private final OperationStack stack;

   /**
    * @param aXBar
    * @param aOperationStack
    * @param aLoadout
    */
   public PilotModuleList(MessageXBar aXBar, OperationStack aOperationStack, LoadoutBase<?> aLoadout){
      super(new PilotModuleModel(aLoadout, aXBar));
      xBar = aXBar;
      stack = aOperationStack;
      loadout = aLoadout;
      setVisible(true);
      setVisibleRowCount(4);
      setFixedCellWidth(ItemRenderer.getItemWidth());
      setFixedCellHeight(ItemRenderer.getItemHeight());
      setDragEnabled(true);
      setTransferHandler(new ModuleTransferHandler());

      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(java.awt.event.MouseEvent e){
            if( e.getClickCount() >= 2 ){
               takeCurrent();
            }
         }
      });

      addKeyListener(new KeyAdapter(){
         @Override
         public void keyReleased(KeyEvent aE){
            if( aE.getKeyCode() == KeyEvent.VK_DELETE ){
               takeCurrent();
            }
         }
      });
   }

   public LoadoutBase<?> getLoadout(){
      return loadout;
   }

   /**
    * @param aModule
    */
   public void putElement(PilotModule aModule){
      stack.pushAndApply(new OpAddModule(xBar, loadout, aModule));
   }
   
   public PilotModule takeCurrent(){
      PilotModule module = getSelectedValue();
      if( module != null ){
         stack.pushAndApply(new OpRemoveModule(xBar, loadout, module));
      }
      return module;
   }
}
