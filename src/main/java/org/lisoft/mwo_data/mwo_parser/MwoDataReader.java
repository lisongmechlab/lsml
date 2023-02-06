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
package org.lisoft.mwo_data.mwo_parser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.mwo_data.Database;

/**
 * This is the main entry point for the parsing of the game files. Most of the heavy lifting is done
 * by the {@link PartialDatabase} class that contains the state of parsing as it progresses and
 * finally packages that into a {@link Database} object.
 *
 * <p>The continued existence of this class is an open question. It's existence doesn't really feel
 * motivated after most of the code moved into {@link PartialDatabase} as part of a cleanup aimed at
 * getting rid of many cast from Object and simplifying function signatures.
 *
 * @author Li Song
 */
public class MwoDataReader {
  private static final List<File> FILES_TO_PARSE =
      Arrays.asList(
          new File("Game/Libs/Items/Weapons/Weapons.xml"),
          new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml"),
          new File("Game/Libs/Items/Modules/Ammo.xml"),
          new File("Game/Libs/Items/Modules/Engines.xml"),
          new File("Game/Libs/Items/Modules/Equipment.xml"),
          new File("Game/Libs/Items/Modules/JumpJets.xml"),
          new File("Game/Libs/Items/Modules/Internals.xml"),
          new File("Game/Libs/Items/Modules/PilotModules.xml"),
          new File("Game/Libs/Items/Modules/WeaponMods.xml"),
          new File("Game/Libs/Items/Modules/Consumables.xml"),
          new File("Game/Libs/Items/Modules/MASC.xml"),
          new File("Game/Libs/Items/Mechs/Mechs.xml"),
          new File("Game/Libs/Items/OmniPods.xml"));
  private final String runningVersion;

  @Inject
  public MwoDataReader(@Named("version") String aRunningVersion) {
    runningVersion = aRunningVersion;
  }

  /**
   * Reads the latest data from the game files and creates a new database.
   *
   * @param aGameDirectory A directory that contains a game installation.
   * @return A {@link Database} if the parsing succeeds without fatal errors.
   */
  public Database parseGameFiles(File aGameDirectory) throws ParseErrorException {
    try {
      final GameVFS gameVFS = new GameVFS(aGameDirectory);
      final Collection<GameVFS.GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
      final PartialDatabase partialDatabase =
          new PartialDatabase(new Localisation(gameVFS), gameFiles);
      return partialDatabase.generateDatabase(runningVersion, gameVFS);
    } catch (final Throwable t) {
      throw new ParseErrorException("Unable to parse game files!", t);
    }
  }

  /**
   * Compares the database to the game files and determines if there is any reason to attempt a
   * further parse.
   *
   * @param aDatabase The {@link Database} to compare to.
   * @param aGameDirectory The directory to read game files to compare to.
   * @return <code>true</code> if the game files have newer data than what's in the database.
   */
  public boolean shouldUpdate(Database aDatabase, File aGameDirectory) throws ParseErrorException {
    try {
      final GameVFS gameVFS = new GameVFS(aGameDirectory);
      final Collection<GameVFS.GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
      final Map<String, Long> checkSums = aDatabase.getChecksums();
      if (gameFiles.size() != checkSums.size()) {
        return true;
      }

      for (final GameVFS.GameFile gameFile : gameFiles) {
        if (gameFile.crc32 != checkSums.get(gameFile.path)) {
          return true;
        }
      }
    } catch (final IOException e) {
      throw new ParseErrorException("Error when opening game files for reading!", e);
    }
    return false;
  }
}
