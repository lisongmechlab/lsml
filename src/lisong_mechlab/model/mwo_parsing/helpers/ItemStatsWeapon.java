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
package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsWeapon extends ItemStats{

   public static class WeaponStatsTag{
      @XStreamAsAttribute
      public double speed;
      @XStreamAsAttribute
      public double volleydelay;
      @XStreamAsAttribute
      public double duration;
      @XStreamAsAttribute
      public double tons;
      @XStreamAsAttribute
      public double maxRange;
      @XStreamAsAttribute
      public double longRange;
      @XStreamAsAttribute
      public double minRange;
      @XStreamAsAttribute
      public int    ammoPerShot;
      @XStreamAsAttribute
      public String ammoType;
      @XStreamAsAttribute
      public double cooldown;
      @XStreamAsAttribute
      public double heat;
      @XStreamAsAttribute
      public double impulse;
      @XStreamAsAttribute
      public double heatdamage;
      @XStreamAsAttribute
      public double damage;
      @XStreamAsAttribute
      public int    numFiring;
      @XStreamAsAttribute
      public String projectileclass;
      @XStreamAsAttribute
      public String type;
      @XStreamAsAttribute
      public int    slots;
      @XStreamAsAttribute
      public int    Health;
      @XStreamAsAttribute
      public String artemisAmmoType;
      @XStreamAsAttribute
      public int    numPerShot;
      @XStreamAsAttribute
      public int    minheatpenaltylevel;
      @XStreamAsAttribute
      public double heatpenalty;
      @XStreamAsAttribute
      public int    heatPenaltyID;
      @XStreamAsAttribute
      public double rof;
      @XStreamAsAttribute
      public double spread;
      @XStreamAsAttribute
      public double JammingChance;
      @XStreamAsAttribute
      public double JammedTime;
      @XStreamAsAttribute
      public int ShotsDuringCooldown;
   }

   public WeaponStatsTag WeaponStats;
}
