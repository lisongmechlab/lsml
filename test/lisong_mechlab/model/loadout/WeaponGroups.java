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
package lisong_mechlab.model.loadout;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.Weapon;

/**
 * This class abstracts weapon groups on mech loadouts.
 * 
 * @author Emily Björk
 */
public class WeaponGroups {
    private final static int MAX_GROUPS = 6;
    private final static int MAX_WEAPONS = 16;
    private FiringMode  firingMode;
    
    BitSet bs;
    
    enum FiringMode{
        AlphaStrike, ChainFire, Optimal
    }
    
    /**
     * 
     */
    public WeaponGroups() {
        bs.
    }

    public List<Weapon> getWeaponOrder(){
        return null;
    }
    
    public void setGroup(int aGroup, int aWeapon){
        
    }
    
    public boolean isInGroup(int aGroup, int aWeapon){
        
    }
    
    public Collection<Weapon> getWeapons(int aGroup){
        List<Weapon> ans = new ArrayList<>();
        List<Weapon> weapons = getWeaponOrder();
        for(int i = 0; i < weapons.size(); ++i){
            if(isInGroup(aGroup, i)){
                ans.add(weapons.get(i));
            }
        }
        return ans;
    }
    
    public FiringMode getFiringMode(int aGroup){
        return firingMode;
    }
   
    public void setFiringMode(int aGroup, FiringMode aFiringMode){
        firingMode = aFiringMode;
    }
    
}
