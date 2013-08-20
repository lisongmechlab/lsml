package lisong_mechlab.converter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class GameDataFileTest{

   @Test
   public void test() throws IOException{
      GameDataFile dataFile = new GameDataFile();

      InputStream inputStream = dataFile.openGameFile(new File("Game/Objects/mechs/spider/sdr-5k.mdf"));
      
      assertTrue(inputStream.available() > 6000);
   }

}
