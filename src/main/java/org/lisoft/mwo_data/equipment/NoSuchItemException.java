/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.mwo_data.equipment;

/**
 * An exception throw by the various database objects when a lookup fails.
 *
 * @author Li Song
 */
public class NoSuchItemException extends Exception {

  public NoSuchItemException() {
    // NOP
  }

  public NoSuchItemException(String aMessage) {
    super(aMessage);
    // NOP
  }

  public NoSuchItemException(String aMessage, Throwable aCause) {
    super(aMessage, aCause);
    // NOP
  }

  public NoSuchItemException(
      String aMessage, Throwable aCause, boolean aEnableSuppression, boolean aWritableStackTrace) {
    super(aMessage, aCause, aEnableSuppression, aWritableStackTrace);
    // NOP
  }

  public NoSuchItemException(Throwable aCause) {
    super(aCause);
    // NOP
  }
}
