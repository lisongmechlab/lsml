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
package lisong_mechlab.view.preferences;

/**
 * This class implements preferences for Smurfy interaction.
 * 
 * @author Emily Björk
 */
public class SmurfyPreferences {
	private static final String SMURFY_REMEMBER_KEY = "smurfyRememberKey";
	private static final String SMURFY_KEY = "smurfyKey";

	public boolean shouldRememberAPIKey() {
		return Boolean.parseBoolean(PreferenceStore.getString(SMURFY_REMEMBER_KEY, "false"));
	}

	public void remeberAPIKey(String aAPIKey) {
		if (null != aAPIKey) {
			PreferenceStore.setString(SMURFY_REMEMBER_KEY, "true");
			PreferenceStore.setString(SMURFY_KEY, aAPIKey);
		} else {
			PreferenceStore.setString(SMURFY_REMEMBER_KEY, "false");
			PreferenceStore.setString(SMURFY_KEY, "");
		}
	}

	/**
	 * @return The stored API key for smurfy.
	 */
	public String getApiKey() {
		return PreferenceStore.getString(SMURFY_KEY);
	}
}
