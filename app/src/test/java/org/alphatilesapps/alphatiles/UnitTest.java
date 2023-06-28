package org.alphatilesapps.alphatiles;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {


    @Test
    public void validateLangPack() throws Validator.ValidatorException, GeneralSecurityException, IOException {

        // TODO !!! replace the url below with the link to the language pack google drive folder
        String url = "https://drive.google.com/drive/u/0/folders/1Pr1rwpzfhpUmw0U3FLLxpPM9Zy68I5gM";

        // TODO !!! change "false" to "true" if you want the app to download/use the language pack from google drive
        boolean overWriteResFolder = false;

        Validator myValidator = new Validator(url);
        myValidator.validate();


        if (overWriteResFolder) {
            myValidator.writeValidatedFiles();
        }

    }
}