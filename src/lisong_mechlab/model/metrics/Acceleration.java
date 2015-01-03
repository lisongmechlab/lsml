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
package lisong_mechlab.model.metrics;

import lisong_mechlab.model.chassi.MovementProfile;

/**
 * This Metric calculates the acceleration for a loadout.
 * @author Li Song
 *
 */
public class Acceleration implements Metric {

    @Override
    public double calculate() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /**
     * Determines the acceleration [m/s^2] for given parameters.
     * @param aSpeed The speed to get the acceleration for.
     * @param aMovementProfile The MovementProfile to use for calculating the acceleration.
     * @return The acceleration [m/s^2] for a given speed.
     */
    public double calculate(double aSpeed, MovementProfile aMovementProfile){
        return 0;
    }
    
    public double timeToTopSpeed(double aMaxSpeed, MovementProfile aMovementProfile){
        int time = 0;
        double speed = 0.0;
        final double dt = 0.01;
        
        while(speed < aMaxSpeed){
            speed += calculate(speed, aMovementProfile) * dt;
            time ++;
        }
        
        return time*dt;
    }

}
