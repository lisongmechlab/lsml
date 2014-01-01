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
package lisong_mechlab.converter;

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

import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.preferences.PreferenceStore;

/**
 * This class handles finding data files in the game folder.
 * 
 * @author Emily Björk
 */
public class GameDataFile{
   public static final File      ITEM_STATS_XML = new File("Game/Libs/Items/ItemStats.xml");
   public static final File      MDF_ROOT       = new File("Game/Objects/mechs/");

   private final Map<File, File> entryCache     = new HashMap<File, File>();
   private static Path           gamePath;

   public GameDataFile() throws IOException{
      ProgramInit.setProcessText("Searching for game files:");
      String gameDir = PreferenceStore.getString(PreferenceStore.GAMEDIRECTORY_KEY);
      if( isValidGameDirectory(new File(gameDir).toPath()) ){
         gamePath = new File(gameDir).toPath();
      }
      else{
         Path p = findGameDirectory();
         if( null != p ){
            gamePath = p;
         }
         else
            throw new FileNotFoundException("Couldn't find the game directory!");
      }
      PreferenceStore.setString(PreferenceStore.GAMEDIRECTORY_KEY, gamePath.toString());
      ProgramInit.setProcessText("Parsing game files...");
   }

   /**
    * Will list the files in the given path under the game root.
    * <p>
    * NOTE: This currently only works with paths that are not inside of archives
    * </p>
    * 
    * @param aPath
    *           The path to list in.
    * @return An array of {@link File} objects or null if no files were found.
    */
   public File[] listGameDir(File aPath){
      File target = gamePath.resolve(aPath.toPath()).toFile();

      File files[] = target.listFiles();
      if( files != null ){
         for(int i = 0; i < files.length; ++i){
            files[i] = gamePath.relativize(files[i].toPath()).toFile();
         }
      }
      return files;
   }

   /**
    * Will open an input stream to the given game data file.
    * 
    * @param aPath
    *           The path to the file to open, with archive file names expanded. For example
    *           "Game/Objects/mechs/spider/sdr-5k.mdf"
    * @return An {@link InputStream} to the requested file.
    * @throws IOException
    * @throws ZipException
    */
   public InputStream openGameFile(File aPath) throws ZipException, IOException{
      // Cause of issue #118, we don't need this functionality atm so it is disabled until needed.
      // // Try finding a raw file
      // {
      // File file = new File(gamePath.toFile(), aPath.toString());
      // if( file.exists() ){
      // return new FileInputStream(file);
      // }
      // }

      // Try looking in archive cache
      File sourceArchive = null;
      synchronized( entryCache ){
         sourceArchive = entryCache.get(aPath);

         if( null == sourceArchive ){
            // Cache miss! Update cache
            search(aPath, new File(gamePath.toFile(), "Game"));

            // Try again
            sourceArchive = entryCache.get(aPath);
            if( sourceArchive == null ){
               throw new RuntimeException("Failed to find sought for file (" + aPath + ") in the game files, this is most likely a bug!");
            }
         }
      }

      ZipFile zipFile = null;
      byte[] buffer = null;
      try{
         zipFile = new ZipFile(sourceArchive);

         String archivePath = gamePath.relativize(sourceArchive.getParentFile().toPath()).relativize(aPath.toPath()).toString();
         // Fix windows...
         archivePath = archivePath.replaceAll("\\\\", "/");

         ZipEntry entry = zipFile.getEntry(archivePath);
         if( null == entry ){
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while( entries.hasMoreElements() ){
               System.err.println(entries.nextElement());
            }
            throw new RuntimeException("Failed to find sought for file (" + aPath + ") in the game files, this is most likely a bug!");
         }
         int size = (int)entry.getSize();
         buffer = new byte[size];
         InputStream is = zipFile.getInputStream(entry);
         int bytesRead = 0;
         while( bytesRead < size ){
            int res = is.read(buffer, bytesRead, size - bytesRead);
            if( -1 == res ){
               throw new IOException("Couldn't read entire file!");
            }
            bytesRead += res;
         }
      }
      finally{
         if( null != zipFile )
            zipFile.close();
      }
      return new ByteArrayInputStream(buffer);
   }

   private void search(File aLocalPath, File aSearchRoot) throws IOException{
      synchronized( entryCache ){

         Collection<File> visitedArchives = entryCache.values();
         Path relativePath = gamePath.relativize(aSearchRoot.toPath());

         for(File file : aSearchRoot.listFiles()){
            if( file.isDirectory() ){
               search(aLocalPath, file);
            }
            else{
               if( visitedArchives.contains(file) ){
                  continue;
               }
               if( file.getName().toLowerCase().endsWith(".pak") && !file.getName().toLowerCase().contains("french") ){
                  ZipFile zipFile = null;
                  try{
                     zipFile = new ZipFile(file);
                     Enumeration<? extends ZipEntry> entries = zipFile.entries();
                     while( entries.hasMoreElements() ){
                        File key = new File(relativePath.toFile(), entries.nextElement().toString());
                        entryCache.put(key, file);
                     }
                  }
                  catch( IOException exception ){
                     System.err.println(exception);
                  }
                  finally{
                     if( null != zipFile )
                        zipFile.close();
                  }
                  if( entryCache.containsKey(aLocalPath) ){
                     break; // We have put the sought for key into the cache.
                  }
               }
            }
         }
      }
   }

   /**
    * Determine if the given {@link Path} points to the root of a valid game install.
    * 
    * @param aPath
    *           The path to check.
    * @return <code>true</code> if <code>aPath</code> points to a valid game install, false otherwise.
    */
   private boolean isValidGameDirectory(Path aPath){
      return (new File(aPath.toFile(), "Game/Objects.pak")).exists() && (new File(aPath.toFile(), "Bin32/MechWarriorOnline.exe")).exists();
   }

   private Path findGameDirectory() throws IOException{
      class GameFinder extends SimpleFileVisitor<Path>{
         public Path gameRoot = null;

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
            ProgramInit.setSubText(file.toString());
            if( file.endsWith("Game/Objects.pak") ){
               int answer = JOptionPane.showConfirmDialog(null, "Found the game files at: " + file.getParent().getParent().toString()
                                                                + "\nIs this your primary game install?", "Confirm game directory",
                                                          JOptionPane.YES_NO_OPTION);
               if( JOptionPane.YES_OPTION == answer ){
                  gameRoot = file.getParent().getParent();
                  return TERMINATE;
               }
            }
            return CONTINUE;
         }

         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
            if( dir.getFileName() != null ){
               if( System.getProperty("os.name").toLowerCase().contains("win") ){
                  // On windows we can skip some folders
                  Set<String> skipList = new HashSet<>();
                  skipList.add("windows");
                  skipList.add("users");
                  if( skipList.contains(dir.getFileName().toString().toLowerCase()) )
                     return SKIP_SUBTREE;
               }
               else{
                  // Assume Linux, skip some folders
                  Set<String> skipList = new HashSet<>();
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

                  if( skipList.contains(dir.toAbsolutePath().toString()) )
                     return SKIP_SUBTREE;
               }
            }
            return CONTINUE;
         }

         @Override
         public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException{
            if( file.toFile().isDirectory() )
               return SKIP_SUBTREE;
            return CONTINUE;
         }
      }

      // Look for a quick exit in the default install directories.
      for(Path path : getDefaultGameFileLocations()){
         if( isValidGameDirectory(path) ){
            return path;
         }
      }

      int answer = JOptionPane.showOptionDialog(null, "The game was not installed in any of the default locations.\nHow would you like to proceed?",
                                                "Determining game install...", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                new String[] {"Automatic search", "Manual browse"}, null);
      if( answer == 0 ){
         // Walk all the file roots, or drives in windows
         GameFinder finder = new GameFinder();
         File[] roots = File.listRoots();
         for(File root : roots){
            // But only if they have enough space to hold the game install and enough space to be a usable disk (5 Mb)
            if( root.getTotalSpace() > 1024 * 1024 * 1500 && root.getFreeSpace() > 1024 * 1024 * 5 ){
               Files.walkFileTree(root.toPath(), finder);
               if( null != finder.gameRoot ){
                  return finder.gameRoot;
               }
            }
         }
      }
      else if( answer == 1 ){
         JFileChooser fc = new JFileChooser();
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         while( JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null) ){
            Path selectedPath = fc.getSelectedFile().toPath();
            if( isValidGameDirectory(selectedPath) ){
               return selectedPath;
            }
            int tryagain = JOptionPane.showConfirmDialog(null,
                                                         "The selected folder doesn't contain a valid game install.\nWould you like to try again?",
                                                         "Ooops!", JOptionPane.YES_NO_OPTION);
            if( tryagain != JOptionPane.YES_OPTION )
               break;
         }
      }
      return null;
   }

   /**
    * @return A {@link List} of {@link Path}s that are likely to contain the game.
    */
   private List<Path> getDefaultGameFileLocations(){
      List<Path> ans = new ArrayList<>();
      // Uses two variations one for x64 and one for x86
      ans.add(FileSystems.getDefault().getPath("C:\\Program Files (x86)\\Piranha Games\\MechWarrior Online"));
      ans.add(FileSystems.getDefault().getPath("C:\\Program Files\\Piranha Games\\MechWarrior Online"));
      ans.add(FileSystems.getDefault().getPath("C:\\Games\\Piranha Games\\MechWarrior Online"));
      return ans;
   }
}
