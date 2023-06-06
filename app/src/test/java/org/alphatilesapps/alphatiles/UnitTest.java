package org.alphatilesapps.alphatiles;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashSet;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {


    @Test
    public void validateLangPack() {
        String url = "https://drive.google.com/drive/u/0/folders/1ykEWRmvVPhO-Y55bqt8QOu4WFGuLwLaH";
        boolean overWriteResFolder = false;
        String langPackNameForOverWriting = "tpxTeocuitlapa";
        try {
            Validator myValidator = new Validator(url);
            myValidator.validate();
            if (overWriteResFolder) {
                String path = System.getProperty("user.dir") + "/src/" + langPackNameForOverWriting + "/res";
                myValidator.writeValidatedFiles(path);
            }
            assertEquals((myValidator.getFatalErrors().equals(new HashSet<String>())
                    && myValidator.getWarnings().equals(new HashSet<String>())), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}