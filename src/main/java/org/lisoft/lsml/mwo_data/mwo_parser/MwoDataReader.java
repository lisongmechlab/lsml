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
package org.lisoft.lsml.mwo_data.mwo_parser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.mwo_data.Database;
import org.lisoft.lsml.mwo_data.mwo_parser.GameVFS.GameFile;

/**
 * This is the main entry point for the parsing of the gamefiles. Most of the heavy lifting is done
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
  private final ErrorReporter errorReporter;
  private final String runningVersion;

  @Inject
  public MwoDataReader(@Named("version") String aRunningVersion, ErrorReporter aErrorReporter) {
    runningVersion = aRunningVersion;
    errorReporter = aErrorReporter;
  }

  /**
   * Reads the latest data from the game files and creates a new database.
   *
   * @param aGameDirectory A directory that contains a game installation.
   * @return An {@link Optional} {@link Database} if the parsing succeeds without fatal errors.
   */
  public Optional<Database> parseGameFiles(File aGameDirectory) {
    try {
      final GameVFS gameVFS = new GameVFS(aGameDirectory);
      final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
      final PartialDatabase partialDatabase =
          new PartialDatabase(new Localisation(gameVFS), gameFiles);
      return Optional.of(partialDatabase.generateDatabase(runningVersion, gameVFS));
    } catch (final Throwable t) {
      errorReporter.error(
          "Parse error",
          "This usually happens when PGI has changed the structure of the data files "
              + "in a patch. Please look for an updated version of LSML at www.li-soft.org."
              + " In the meanwhile LSML will continue to function with the data from the last"
              + " successfully parsed patch.",
          t);
      return Optional.empty();
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
  public boolean shouldUpdate(Database aDatabase, File aGameDirectory) {
    try {
      final GameVFS gameVFS = new GameVFS(aGameDirectory);
      final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
      final Map<String, Long> checkSums = aDatabase.getChecksums();
      if (gameFiles.size() != checkSums.size()) {
        return true;
      }

      for (final GameFile gameFile : gameFiles) {
        if (gameFile.crc32 != checkSums.get(gameFile.path)) {
          return true;
        }
      }
    } catch (final IOException e) {
      errorReporter.error(
          "Error reading data files", "LSML couldn't open game data files for reading.", e);
    }
    return false;
  }
}
