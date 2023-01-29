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
package org.lisoft.lsml.model.database.mwo_parser;

import static java.nio.file.FileVisitResult.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javafx.util.Callback;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.util.OS.WindowsVersion;

/**
 * This class is a Virtual File System for finding data files in the game folder.
 *
 * @author Li Song
 */
public class GameVFS {
  /**
   * This structure contains information about a game file, its CRC, path and a stream for reading
   * from it.
   *
   * @author Li Song
   */
  static class GameFile implements AutoCloseable {
    final long crc32;
    final String path;
    final InputStream stream;

    GameFile(ZipFile aZipFile, ZipEntry aEntry, String aPath) throws IOException {
      final int size = (int) aEntry.getSize();
      final byte[] buffer = new byte[size];

      // Inflate file to memory
      try (InputStream is = aZipFile.getInputStream(aEntry)) {
        int bytesRead = 0;
        while (bytesRead < size) {
          final int res = is.read(buffer, bytesRead, size - bytesRead);
          if (-1 == res) {
            throw new IOException("Couldn't read entire file!");
          }
          bytesRead += res;
        }
      }

      crc32 = aEntry.getCrc();
      path = aPath;
      stream = new ByteArrayInputStream(buffer);
    }

    @Override
    public void close() throws Exception {
      stream.close();
    }

    @Override
    public String toString() {
      return path + " [" + crc32 + "]";
    }
  }

  private static class GameFinder extends SimpleFileVisitor<Path> {
    private final Callback<Path, Boolean> confirmGameInstallCallback;
    private final Callback<Path, Void> fileVisitCallback;
    private final Set<String> skipList = new HashSet<>();
    private Path gameRoot = null;

    private GameFinder(
        Callback<Path, Void> aFileVisitCallback,
        Callback<Path, Boolean> aConfirmGameInstallCallback) {
      fileVisitCallback = aFileVisitCallback;
      confirmGameInstallCallback = aConfirmGameInstallCallback;

      if (OS.isWindowsOrNewer(WindowsVersion.WIN_OLD)) {
        skipList.add("windows");
        skipList.add("users");
        skipList.add("$recycle.bin");
      } else {
        // Assuming Unix based
        skipList.add("/bin");
        skipList.add("/boot");
        skipList.add("/dev");
        skipList.add("/etc");
        skipList.add("/lib");
        skipList.add("/lib64");
        skipList.add("/proc");
        skipList.add("/sys");
        skipList.add("/run");
        skipList.add("/sbin");
        skipList.add("/tmp");
      }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
      final Path fileName = dir.getFileName();
      if (fileName != null) {
        if (OS.isWindowsOrNewer(WindowsVersion.WIN_OLD)) {
          // On Windows we can skip some folders
          if (skipList.contains(fileName.toString().toLowerCase())) {
            return SKIP_SUBTREE;
          }
        } else {
          if (skipList.contains(dir.toAbsolutePath().toString())) {
            return SKIP_SUBTREE;
          }
        }

        fileVisitCallback.call(dir);

        if (isValidGameDirectory(dir.toFile()) && confirmGameInstallCallback.call(dir)) {
          gameRoot = dir;
          return TERMINATE;
        }
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      return SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      if (file.toFile().isDirectory()) {
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }
  }

  private final Map<File, File> file2archive = new HashMap<>();
  private final Path gamePath;

  /**
   * Creates a new virtual file system for game files in the given directory which must be a valid
   * game install. See {@link GameVFS#isValidGameDirectory(File)}.
   *
   * @param gameDir The {@link File} where the game directory is.
   * @throws IOException Throw in an error was encountered wile initialising the VFS.
   */
  public GameVFS(File gameDir) throws IOException {
    if (isValidGameDirectory(gameDir)) {
      gamePath = gameDir.toPath();
    } else {
      throw new FileNotFoundException("Not a valid game directory!");
    }
  }

  /**
   * Attempts a (smart) search of all file system roots to automatically detect a MWO installation.
   *
   * @param aFileVisitCallback A {@link Callback} that will be called repeatedly with the currently
   *     searched directory to provide feedback.
   * @param aConfirmationCallback Called with a {@link Path} when a candidate location is found, the
   *     callback should confirm with the user if this is indeed the correct installation (in the
   *     case of multiple MWO installations such as PTS).
   * @return An {@link Optional} with a {@link Path} to the game installation root if found, empty
   *     otherwise. installation.
   */
  public static Optional<Path> autoDetectGameInstall(
      Callback<Path, Void> aFileVisitCallback, Callback<Path, Boolean> aConfirmationCallback) {

    final GameFinder finder = new GameFinder(aFileVisitCallback, aConfirmationCallback);
    for (final File root : File.listRoots()) {
      try {
        final long mwoInstallSize = 20L * 1024 * 1024 * 1024;
        final long minFreeSpace = 5L * 1024 * 1024; // Must have free space (rules out RO media)
        if (root.getTotalSpace() > mwoInstallSize && root.getFreeSpace() > minFreeSpace) {
          Files.walkFileTree(root.toPath(), finder);
          if (null != finder.gameRoot) {
            return Optional.of(finder.gameRoot);
          }
        }
      } catch (final IOException e) {
        // Ignore and continue search.
      }
    }
    return Optional.empty();
  }

  /**
   * @return A {@link List} of {@link Path}s that are likely to contain the game.
   */
  public static List<Path> getDefaultGameFileLocations() {
    final List<Path> ans = new ArrayList<>();
    // Uses two variations one for x64 and one for x86
    ans.add(
        FileSystems.getDefault()
            .getPath("C:\\Program Files (x86)\\Piranha Games\\MechWarrior Online"));
    ans.add(
        FileSystems.getDefault().getPath("C:\\Program Files\\Piranha Games\\MechWarrior Online"));
    ans.add(
        FileSystems.getDefault()
            .getPath("C:\\Program Files (x86)\\steam\\steamapps\\common\\MechWarrior Online"));
    ans.add(
        FileSystems.getDefault()
            .getPath("C:\\Program Files\\steam\\steamapps\\common\\MechWarrior Online"));
    ans.add(FileSystems.getDefault().getPath("C:\\Games\\Piranha Games\\MechWarrior Online"));
    return ans;
  }

  /**
   * Determine if the given {@link Path} points to the root of a valid game install.
   *
   * @param aFile The path to check.
   * @return <code>true</code> if <code>aPath</code> points to a valid game install, false
   *     otherwise.
   */
  public static boolean isValidGameDirectory(File aFile) {
    final boolean hasObjectsPak = new File(aFile, "Game/Objects.pak").exists();
    final boolean hasBinary =
        new File(aFile, "Bin32/MWOClient.exe").exists()
            || new File(aFile, "Bin64/MWOClient.exe").exists();
    return hasObjectsPak && hasBinary;
  }

  /**
   * Will list the files in the given path under the game root.
   *
   * <p>NOTE: This currently only works with paths that are not inside of archives
   *
   * @param aPath The path to list in.
   * @return An array of {@link File} objects or null if no files were found.
   */
  public File[] listGameDir(File aPath) {
    final File target = gamePath.resolve(aPath.toPath()).toFile();

    final File[] files = target.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; ++i) {
        files[i] = gamePath.relativize(files[i].toPath()).toFile();
      }
    }
    return files;
  }

  /**
   * Will open an input stream to the given game data file.
   *
   * @param aGameLocalPath The path to the file to open, with archive file names expanded. For
   *     example "Game/Objects/mechs/spider/sdr-5k.mdf"
   * @return An {@link InputStream} to the requested file.
   * @throws IOException if the game file couldn't be read.
   * @throws ZipException if the game file couldn't be extracted from the pak file.
   */
  public GameFile openGameFile(File aGameLocalPath) throws ZipException, IOException {
    final Optional<File> sourceArchive = findArchiveForFile(aGameLocalPath, gamePath.toFile());
    if (sourceArchive.isEmpty()) {
      throw new IOException(
          "Failed to find sought for file (" + aGameLocalPath + ") in the game files!");
    }

    try (ZipFile zipFile = new ZipFile(sourceArchive.get())) {

      String archivePath =
          gamePath
              .relativize(sourceArchive.get().getParentFile().toPath())
              .relativize(aGameLocalPath.toPath())
              .toString();

      // Canonise to Unix file system separator.
      archivePath = archivePath.replaceAll("\\\\", "/");

      ZipEntry entry = zipFile.getEntry(archivePath);
      if (null == entry) {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        // Apparently PGI is still as lousy as ever at being consistent
        // with case so, manually check if we can
        // find a case-insensitive match before giving up
        while (entries.hasMoreElements()) {
          final ZipEntry nextEntry = entries.nextElement();
          final String next = nextEntry.getName();
          if (archivePath.equalsIgnoreCase(next)) {
            entry = nextEntry;
            break;
          }
        }
        if (null == entry) {
          throw new IOException("Unable to find previously found file!?!?!");
        }
      }
      return new GameFile(zipFile, entry, aGameLocalPath.toString());
    }
  }

  public Collection<GameFile> openGameFiles(Collection<File> aFiles) throws IOException {
    final List<GameFile> ans = new ArrayList<>(aFiles.size());
    for (final File file : aFiles) {
      ans.add(openGameFile(file));
    }
    return ans;
  }

  private static boolean isArchive(File aFile) {
    final String name = aFile.getName().toLowerCase();
    return aFile.isFile() && name.endsWith(".pak") && !name.contains("french");
  }

  private void cacheArchive(File aFileInArchive, File aArchive) {
    file2archive.put(canonicalizePath(aFileInArchive), aArchive);
  }

  private void cacheContentsOfArchive(File aArchive, File aRelativeBasePath) throws IOException {
    try (ZipFile zipFile = new ZipFile(aArchive)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final File fileInArchive = new File(aRelativeBasePath, entries.nextElement().toString());
        cacheArchive(fileInArchive, aArchive);
      }
    }
  }

  // java.io.File has different equality semantics depending on the host operating system.
  // This leads to annoyance when we run on Linux, pointed at an installation of MWO on a
  // Windows partition.
  private File canonicalizePath(File aFile) {
    // Explicitly specify an English locale, so that Turkish users don't run into problems with
    // Iceferret.pak turning into ıceferret.pak.
    return new File(aFile.toString().toLowerCase(Locale.US));
  }

  private Optional<File> findArchiveForFile(File aGameLocalPath, File aSearchRoot)
      throws IOException {
    final File sourceArchive = getCachedArchive(aGameLocalPath);
    if (null != sourceArchive) {
      return Optional.of(sourceArchive);
    }

    final Collection<File> visitedArchives = file2archive.values();
    final Path relativePath = gamePath.relativize(aSearchRoot.toPath());

    final File[] listFiles = aSearchRoot.listFiles();
    if (null == listFiles) {
      return Optional.empty();
    }

    for (final File fileOnDisk : listFiles) {
      if (fileOnDisk.isDirectory()) {
        final Optional<File> file = findArchiveForFile(aGameLocalPath, fileOnDisk);
        if (file.isPresent()) {
          return file;
        }
      } else {
        if (isArchive(fileOnDisk) && !visitedArchives.contains(fileOnDisk)) {
          cacheContentsOfArchive(fileOnDisk, relativePath.toFile());
          if (null != getCachedArchive(aGameLocalPath)) {
            return Optional.of(fileOnDisk);
          }
        }
      }
    }
    return Optional.empty();
  }

  private File getCachedArchive(File aFileInArchive) {
    return file2archive.get(canonicalizePath(aFileInArchive));
  }
}
