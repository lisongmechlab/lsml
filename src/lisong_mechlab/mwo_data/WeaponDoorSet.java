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
package lisong_mechlab.mwo_data;

import java.util.List;

import lisong_mechlab.mwo_data.helpers.HardPointWeaponSlot.Attachment;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents a WeaponDoorSet element in the data files.
 * 
 * @author Li Song
 */
public class WeaponDoorSet {
    @XStreamAsAttribute
    public int id;

    public class WeaponDoor {
        @XStreamAsAttribute
        double                  closedDamageFactor;

        @XStreamAsAttribute
        public String           AName;

        @XStreamAsAttribute
        double                  firingdelay;

        @XStreamImplicit(itemFieldName = "Attachment")
        public List<Attachment> attachments;
    }

    @XStreamImplicit(itemFieldName = "WeaponDoor")
    public List<WeaponDoor> weaponDoors;
}
