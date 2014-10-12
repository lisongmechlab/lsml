package lisong_mechlab.model.loadout.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.OpRename;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;

public class SmurfyXMLTest {

    private void referenceTest(String aLSMLLink, String aResource) throws Exception {
        Base64LoadoutCoder coder = new Base64LoadoutCoder();
        LoadoutBase<?> loadout = coder.parse(aLSMLLink);
        (new OperationStack(0)).pushAndApply(new OpRename(loadout, null, "stock"));

        String xml = SmurfyXML.toXml(loadout);
        String lines[] = xml.split("\n");

        try (InputStream is = SmurfyXMLTest.class.getResourceAsStream(aResource);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)) {

            for (String line : lines) {
                String expected = br.readLine();
                if (expected == null) {
                    fail("Unexpected end of file!");
                    return; // Make eclipse understand that this is the end of this function.
                }

                String lineTrim = line.replaceAll("^\\s*", "").replaceAll("\\s*$", "");
                String expectedTrim = expected.replaceAll("^\\s*", "").replaceAll("\\s*$", "");

                assertEquals(expectedTrim, lineTrim);
            }
        }
    }

    @Test
    public final void testToXmlIS() throws Exception {
        referenceTest("lsml://rgARREAUUhJeHEAUUkTne6/ep3rMhjZ5PGcsMNEXVOQww3HhhS2RYYbDGw==",
                "/resources/smurfy_as7ddcstock.xml");
    }

    @Test
    public final void testToXmlClan() throws Exception {
        referenceTest("lsml://rgCwDhAIEBISCBAIEA4P6zHOZYy8rm2ZmaFXPVy9rmWaudrmGZmyxno2",
                "/resources/smurfy_kfxdstock.xml");
    }

}
