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
package lisong_mechlab.model.item;

import lisong_mechlab.model.mwo_parsing.helpers.MdfInternal;

/**
 * Internals are special items that do not exist in the ItemDB. Instead they are created and owned by the chassii.
 * 
 * @author Emily
 */
public class Internal extends Item{
   public Internal(MdfInternal aInternal){
      super(aInternal.Name, aInternal.Desc, aInternal.Slots, 0); // TODO: Check translation
   }

   public Internal(String aNameTag, String aDescTag, int aSlots){
      super(aNameTag, aDescTag, aSlots, 0);
   }
}
