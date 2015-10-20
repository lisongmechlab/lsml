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
package org.lisoft.lsml.model.datacache.gamedata;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.util.OS.WindowsVersion;
import org.lisoft.lsml.view.preferences.CorePreferences;

/**
 * This class is a Virtual File System for finding data files in the game folder.
 * 
 * @author Li Song
 */
public class GameVFS {
    public static final File ITEM_STATS_XML  = new File("Game/Libs/Items/ItemStats.xml");
    public static final File MECH_ID_MAP_XML = new File("Game/Libs/Items/MechIDMap.xml");
    public static final File MDF_ROOT        = new File("Game/mechs/Objects/mechs/");

    private final Map<File, File> file2archive = new HashMap<File, File>();
    private static Path           gamePath;

    /**
     * This structure contains information about a game file, it's CRC, path and a stream for reading from it.
     * 
     * @author Li Song
     */
    public static class GameFile {
        public final InputStream stream;
        public final long        crc32;
        public final String      path;

        GameFile(ZipFile aZipFile, ZipEntry aEntry, String aPath) throws IOException {
            int size = (int) aEntry.getSize();
            byte[] buffer = new byte[size];

            try (InputStream is = aZipFile.getInputStream(aEntry);) {
                int bytesRead = 0;
                while (bytesRead < size) {
                    int res = is.read(buffer, bytesRead, size - bytesRead);
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

        GameFile(InputStream aStream, long aCrc32, String aPath) {
            stream = aStream;
            crc32 = aCrc32;
            path = aPath;
        }

        @Override
        public String toString() {
            return path + " [" + crc32 + "]";
        }
    }

    /**
     * Creates a new virtual file system for game files in the given directory which must be a valid game install. See
     * {@link GameVFS#checkGameFilesInstalled()}.
     * 
     * @param gameDir
     *            The {@link File} where the game directory is.
     * @throws IOException
     *             Throw in an error was encountered wile initializing the VFS.
     */
    public GameVFS(File gameDir) throws IOException {
        if (isValidGameDirectory(gameDir)) {
            gamePath = gameDir.toPath();
        }
        else {
            throw new FileNotFoundException("Not a valid game directory!");
        }
    }

    /**
     * Will list the files in the given path under the game root.
     * <p>
     * NOTE: This currently only works with paths that are not inside of archives
     * </p>
     * 
     * @param aPath
     *            The path to list in.
     * @return An array of {@link File} objects or null if no files were found.
     */
    public File[] listGameDir(File aPath) {
        File target = gamePath.resolve(aPath.toPath()).toFile();

        File files[] = target.listFiles();
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
     * @param aGameLocalPath
     *            The path to the file to open, with archive file names expanded. For example
     *            "Game/Objects/mechs/spider/sdr-5k.mdf"
     * @return An {@link InputStream} to the requested file.
     * @throws IOException
     * @throws ZipException
     */
    public GameFile openGameFile(File aGameLocalPath) throws ZipException, IOException {
        File sourceArchive = findArchiveForFile(aGameLocalPath, gamePath.toFile());
        if (null == sourceArchive) {
            throw new IOException("Failed to find sought for file (" + aGameLocalPath + ") in the game files!");
        }

        try (ZipFile zipFile = new ZipFile(sourceArchive)) {

            String archivePath = gamePath.relativize(sourceArchive.getParentFile().toPath())
                    .relativize(aGameLocalPath.toPath()).toString();
            archivePath = archivePath.replaceAll("\\\\", "/"); // Canonicalize to Unix file system separator.

            ZipEntry entry = zipFile.getEntry(archivePath);
            if (null == entry) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                // Apparently PGI is still as lousy as ever at being consistent with case so, manually check if we can
                // find a case-insensitive match before giving up
                while (entries.hasMoreElements()) {
                    ZipEntry nextEntry = entries.nextElement();
                    String next = nextEntry.getName();
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

    private File findArchiveForFile(File aGameLocalPath, File aSearchRoot) throws IOException {
        File sourceArchive = file2archive.get(aGameLocalPath);
        if (null != sourceArchive) {
            return sourceArchive;
        }

        Collection<File> visitedArchives = file2archive.values();
        Path relativePath = gamePath.relativize(aSearchRoot.toPath());

        for (File fileOnDisk : aSearchRoot.listFiles()) {
            if (fileOnDisk.isDirectory()) {
                File file = findArchiveForFile(aGameLocalPath, fileOnDisk);
                if (null != file) {
                    return file;
                }
            }
            else {
                if (isArchive(fileOnDisk) && !visitedArchives.contains(fileOnDisk)) {
                    cacheContentsOfArchive(fileOnDisk, relativePath.toFile());
                    if (file2archive.containsKey(aGameLocalPath)) {
                        return fileOnDisk;
                    }
                }
            }
        }
        return null;
    }

    private void cacheContentsOfArchive(File aArchive, File aRelativeBasePath) throws ZipException, IOException {
        try (ZipFile zipFile = new ZipFile(aArchive)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                File fileInArchive = new File(aRelativeBasePath, entries.nextElement().toString());
                file2archive.put(fileInArchive, aArchive);
            }
        }
    }

    private static boolean isArchive(File aFile) {
        String name = aFile.getName().toLowerCase();
        return aFile.isFile() && name.endsWith(".pak") && !name.contains("french");
    }

    /**
     * Determine if the given {@link Path} points to the root of a valid game install.
     * 
     * @param aPath
     *            The path to check.
     * @return <code>true</code> if <code>aPath</code> points to a valid game install, false otherwise.
     */
    private static boolean isValidGameDirectory(File aFile) {
        return (new File(aFile, "Game/Objects.pak")).exists()
                && (new File(aFile, "Bin32/MechWarriorOnline.exe")).exists();
    }

    static private class GameFinder extends SimpleFileVisitor<Path> {
        public Path gameRoot = null;
        Set<String> skipList = new HashSet<>();

        GameFinder() {

            if (OS.isWindowsOrNewer(WindowsVersion.WinOld)) {
                skipList.add("windows");
                skipList.add("users");
                skipList.add("$recycle.bin");
            }
            else {
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
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            return SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (dir.getFileName() != null) {
                if (OS.isWindowsOrNewer(WindowsVersion.WinOld)) {
                    // On windows we can skip some folders
                    if (skipList.contains(dir.getFileName().toString().toLowerCase()))
                        return SKIP_SUBTREE;
                }
                else {
                    if (skipList.contains(dir.toAbsolutePath().toString()))
                        return SKIP_SUBTREE;
                }

                if (isValidGameDirectory(dir.toFile())) {
                    int answer = JOptionPane.showConfirmDialog(null,
                            "Found the game files at: " + dir.toString() + "\nIs this your primary game install?",
                            "Confirm game directory", JOptionPane.YES_NO_OPTION);
                    if (JOptionPane.YES_OPTION == answer) {
                        gameRoot = dir;
                        return TERMINATE;
                    }
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (file.toFile().isDirectory())
                return SKIP_SUBTREE;
            return CONTINUE;
        }
    }

    public static boolean browseForGameInstall() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        while (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)) {
            Path selectedPath = fc.getSelectedFile().toPath();
            if (isValidGameDirectory(selectedPath.toFile())) {
                CorePreferences.setGameDirectory(selectedPath.toAbsolutePath().toString());
                return true;
            }
            int tryagain = JOptionPane.showConfirmDialog(null,
                    "The selected folder doesn't contain a valid game install.\nWould you like to try again?", "Ooops!",
                    JOptionPane.YES_NO_OPTION);
            if (tryagain != JOptionPane.YES_OPTION)
                break;
        }
        return false;
    }

    public static void checkGameFilesInstalled() {
        File storedGameDir = new File(CorePreferences.getGameDirectory());
        if (storedGameDir.isDirectory() && isValidGameDirectory(storedGameDir))
            return;

        // Look for a quick exit in the default install directories.
        for (Path path : getDefaultGameFileLocations()) {
            if (isValidGameDirectory(path.toFile())) {
                CorePreferences.setGameDirectory(path.toAbsolutePath().toString());
                return;
            }
        }

        // Check bundled status only after looking for the easy locations.
        if (CorePreferences.getUseBundledData()) {
            return;
        }

        while (true) {
            int answer = JOptionPane.showOptionDialog(null,
                    "The game was not installed in any of the default locations.\n"
                            + "If you don't have a game install, LSML can use bundled data.\n"
                            + "Be aware, the bundled data may be inaccurate if this is an old release.\n\n"
                            + "How would you like to proceed?",
                    "Determining game install...", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[] { "Automatic search", "Manual browse", "I don't have a game install",
                            "Close program" },
                    null);
            if (answer == 0) {
                // Walk all the file roots, or drives in windows
                GameFinder finder = new GameFinder();
                File[] roots = File.listRoots();

                for (File root : roots) {
                    try {
                        // But only if there's enough space for a game install and enough space to be a usable disk (5
                        // Mb)
                        if (root.getTotalSpace() > 1024 * 1024 * 1500 && root.getFreeSpace() > 1024 * 1024 * 5) {
                            Files.walkFileTree(root.toPath(), finder);
                            if (null != finder.gameRoot) {
                                CorePreferences.setGameDirectory(finder.gameRoot.toAbsolutePath().toString());
                                return;
                            }
                        }
                    }
                    catch (IOException e) {
                        // Ignore and continue search.
                    }
                }
                JOptionPane.showMessageDialog(null,
                        "Automatic search failed to find a game install, please use manual browse.");
            }
            else if (answer == 1) {
                browseForGameInstall();
            }
            else if (answer == 2) {
                CorePreferences.setUseBundledData(true);
                return;
            }
            else {
                System.exit(1);
                return;
            }
        }
    }

    /**
     * @return A {@link List} of {@link Path}s that are likely to contain the game.
     */
    private static List<Path> getDefaultGameFileLocations() {
        List<Path> ans = new ArrayList<>();
        // Uses two variations one for x64 and one for x86
        ans.add(FileSystems.getDefault().getPath("C:\\Program Files (x86)\\Piranha Games\\MechWarrior Online"));
        ans.add(FileSystems.getDefault().getPath("C:\\Program Files\\Piranha Games\\MechWarrior Online"));
        ans.add(FileSystems.getDefault().getPath("C:\\Games\\Piranha Games\\MechWarrior Online"));
        return ans;
    }
}
