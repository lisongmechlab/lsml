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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.model.datacache.gamedata.Localization;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsUpgradeType;
import org.lisoft.lsml.model.item.Faction;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Base class for all upgrades for 'mechs.
 * 
 * @author Emily Björk
 */
public abstract class Upgrade {
    @XStreamAsAttribute
    private final String  name;
    @XStreamAsAttribute
    private final int     mwoId;
    @XStreamAsAttribute
    private final Faction faction;
    private final String  description;

    protected Upgrade(String aName, String aDescription, int aMwoId, Faction aFaction) {
        name = aName;
        mwoId = aMwoId;
        description = aDescription;
        faction = aFaction;
    }

    protected Upgrade(ItemStatsUpgradeType aUpgradeType) {
        this(Localization.key2string(aUpgradeType.Loc.nameTag), Localization.key2string(aUpgradeType.Loc.descTag),
                Integer.parseInt(aUpgradeType.id), Faction.fromMwo(aUpgradeType.faction));
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return The localized name of the upgrade.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The MW:O ID for the upgrade.
     */
    public int getMwoId() {
        return mwoId;
    }

    /**
     * @return The MW:O description of the upgrade.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The faction that this upgrades is for.
     */
    public Faction getFaction() {
        return faction;

    }

    /**
     * @return The {@link UpgradeType} of this upgrade.
     */
    public abstract UpgradeType getType();
}
