package org.alphatilesapps.alphatiles;

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.Scanner;
import java.util.HashSet;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {


    @Test
    public void validateLangPack() throws Exception{

        // TODO !!! replace the url below with the link to the language pack google drive folder
        String url = "https://drive.google.com/drive/u/0/folders/1ykEWRmvVPhO-Y55bqt8QOu4WFGuLwLaH";

        // TODO !!! change "false" to "true" if you want the app to download/use the language pack from google drive
        boolean overWriteResFolder = false;

        Validator myValidator = new Validator(url);
        myValidator.validate();


        if (overWriteResFolder) {
            myValidator.writeValidatedFiles();
        }


        assertEquals((myValidator.getFatalErrors().equals(new HashSet<String>())
                && myValidator.getWarnings().equals(new HashSet<String>())), true);

    }
}