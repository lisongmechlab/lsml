package lisong_mechlab.converter;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import lisong_mechlab.view.LsmlPreferences;

public class GameDataFile{
   public static final File             ITEM_STATS_XML = new File("Game/Libs/Items/ItemStats.xml");

   private final Path                   gamePath;
   private static final Map<File, File> entryCache     = new HashMap<File, File>();

   public static final File             MDF_ROOT       = new File("Game/Objects/mechs/");

   public GameDataFile() throws IOException{
      String gameDir = LsmlPreferences.getString("gameDir");
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
   }

   public GameDataFile(File aGameRoot) throws FileNotFoundException{
      if( isValidGameDirectory(aGameRoot.toPath()) ){
         gamePath = aGameRoot.toPath();
      }
      else{
         throw new FileNotFoundException("The given path doesn't contain the MWO client!");
      }
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
      // Try finding a raw file
      {
         File file = new File(gamePath.toFile(), aPath.toString());
         if( file.exists() ){
            return new FileInputStream(file);
         }
      }

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
               if( file.getName().toLowerCase().endsWith(".pak")  && !file.getName().toLowerCase().contains("french")){
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

   private boolean isValidGameDirectory(Path aPath){
      File file = new File(aPath.toFile(), "Game/Objects.pak");
      return file.exists();
   }

   private Path findGameDirectory() throws IOException{
      class GameFinder implements FileVisitor<Path>{
         public Path gameRoot = null;

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
            if( file.endsWith("Game/Objects.pak") ){
               gameRoot = file.getParent().getParent();
               return TERMINATE;
            }
            return CONTINUE;
         }

         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
            if( dir.getFileName() != null && dir.getFileName().toString().toLowerCase().equals("windows") && dir.getFileName().toString().toLowerCase().equals("users") )
               // Skip windows folder, it's big and slow and we don't expect to find the game there.
               return SKIP_SUBTREE;
            return CONTINUE;
         }

         @Override
         public FileVisitResult visitFileFailed(Path file, IOException exc){
            // System.err.println(exc);
            return CONTINUE;
         }

         @Override
         public FileVisitResult postVisitDirectory(Path aArg0, IOException aArg1) throws IOException{
            return CONTINUE;
         }

      }
      if( getDefaultGameFileLocation().toFile().exists() ){
         return getDefaultGameFileLocation();
      }
      GameFinder finder = new GameFinder();

      File[] roots = File.listRoots();
      for(File root : roots){
         if( root.getTotalSpace() > 1024 * 1024 * 1500 && root.getFreeSpace() > 1024 * 1024 * 50 ){
            Files.walkFileTree(root.toPath(), finder);
            if( null != finder.gameRoot ){
               return finder.gameRoot;
            }
         }
      }

      return null;
   }

   private Path getDefaultGameFileLocation(){
      Path defaultGameFileLocation = FileSystems.getDefault().getPath("C:\\Program Files (x86)\\Piranha Games\\MechWarrior Online"); //Uses two variations one for x64 and one for x86
      if( !defaultGameFileLocation.toFile().exists() ){
         defaultGameFileLocation = FileSystems.getDefault().getPath("C:\\Program Files\\Piranha Games\\MechWarrior Online");
      }
      if( !defaultGameFileLocation.toFile().exists() ){
         defaultGameFileLocation = FileSystems.getDefault().getPath("C:\\Games\\Piranha Games\\MechWarrior Online");
      }
      return defaultGameFileLocation;

   }
}
