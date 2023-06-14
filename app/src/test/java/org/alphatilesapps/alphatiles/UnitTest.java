package org.alphatilesapps.alphatiles;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {


    @Test
    public void validateLangPack() throws Exception{

        //Note to Aaron: this is not something users will normally have to do... but since you last ran the program
        //I changed what google drive permissions the code uses (so it can download files from google drive). Because
        //you might have the old permissions stored locally, the very first time you run the program it may not download
        //files from drive (so you might have to run it twice).

        // TODO !!! replace the url below with the link to the language pack google drive folder
        String url = "https://drive.google.com/drive/u/0/folders/1ykEWRmvVPhO-Y55bqt8QOu4WFGuLwLaH";

        // TODO !!! change "false" to "true" if you want the app to download/use the language pack from google drive
        // Note to Aaron: In my email I had said this was not finished, but I have since written the code for this
        // thus, you can go head and test leaving this true if you would like.
        boolean overWriteResFolder = true;

        Validator myValidator = new Validator(url);
        myValidator.validate();


        if (overWriteResFolder) {
            myValidator.writeValidatedFiles();
        }


        assertTrue((myValidator.getFatalErrors().equals(new LinkedHashSet<>())
                && myValidator.getWarnings().equals(new LinkedHashSet<>())));

    }
}