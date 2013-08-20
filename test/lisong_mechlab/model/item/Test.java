package lisong_mechlab.model.item;

import java.io.File;
import java.io.FileWriter;

public class Test{
   public static void main(String[] args) throws Exception{
      File file = new File("test.txt");

      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write("x");
      fileWriter.close();

      boolean exists = file.exists();
      boolean deleted = false;
      if( exists ){
         deleted = file.delete();
      }
      boolean stillexists = file.exists();

      System.out.println("Exists = " + exists);
      System.out.println("Deleted = " + deleted);
      System.out.println("Still Exists = " + stillexists);
   }
}
