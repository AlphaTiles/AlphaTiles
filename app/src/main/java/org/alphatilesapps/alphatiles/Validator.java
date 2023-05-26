/*
This class is used to validate the existence and format of the txt files that are in the language
pack folder that is a sister to main.


 */
package org.alphatilesapps.alphatiles;


import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Validator {


    private final android.content.res.Resources res;
    private final String packageName;
    private final Context context;
    private ArrayList<String> fatalErrors = new ArrayList<>();
    private ArrayList<String> warnings = new ArrayList<>();
    private final RawFilesArray rawFiles = new RawFilesArray();

    private final ArrayList<String> expectedRawFilesList = new ArrayList(Arrays.asList("aa_games",
            "aa_gametiles", "aa_keyboard", "aa_langinfo", "aa_names", "aa_resources", "aa_settings",
            "aa_syllables", "aa_wordlist"));


    private final ArrayList<String> rawFilesTemplatesList = new ArrayList(Arrays.asList("aa_games_template",
            "aa_gametiles_template", "aa_keyboard_template", "aa_langinfo_template", "aa_names_template",
            "aa_resources_template", "aa_settings_template", "aa_syllables_template", "aa_wordlist_template"));

    public Validator(Context context) {
        this.context = context;
        this.packageName = context.getPackageName();
        this.res = context.getResources();
        for (String name : rawFilesTemplatesList) {
            rawFiles.add(new RawFile(name));
        }


    }

    public void validate() {

        for (String name : expectedRawFilesList) {
            RawFile templateEquivalent = rawFiles.getFromName(name + "_template");
            rawFiles.add(new RawFile(name, templateEquivalent.get(templateEquivalent.size() - 1).size()));
            rawFiles.matchingRows(name, name + "_template", 0, 0, fatalErrors);
        }
        rawFiles.matchingCols("aa_langInfo", "aa_langInfo_template", 1, 1, fatalErrors);
        for (String cell : rawFiles.getFromName("aa_langinfo").getCol(1)) {
            if (cell.contains("Ꞌ") || cell.contains("ꞌ")) {
                warnings.add("In aa_langinfo.txt, the \"saltillo\" [ < ꞌ >,  glottal stop, " +
                        "Excel.UNICODE()=42892, VBA.AscW()=-22644] will not display correctly in " +
                        "devices Android 4.4 and older. It could be replaced with the apostrophe" +
                        " [< ' >, UNICODE()=39] or prime [< ′ >, UNICODE()=8242].");
            }

        }
        for (String cell : rawFiles.getFromName("aa_wordList").getCol(0)) {
            if (!cell.matches("[a-zA-Z0-9]+")) {
                fatalErrors.add("In aa_wordList, the word " + cell + " contains non-alphanumeric characters. " +
                        "Please remove them. (The only allowed characters are a-z, A-Z, and 0-9.)");
            }
        }
        ArrayList<String> keysList = rawFiles.getFromName("aa_keyboard").getCol(0);
        if (!keysList.contains(".")){
            warnings.add("In aa_keyboard.txt, the letter . (period) is not included. A period may be" +
                    "necessary to split syllables");
        }
        Map<String, Integer> keyCount= new HashMap<>();
            for (String key : keysList){
                keyCount.put(key, 0);
            }
        for (String cell : rawFiles.getFromName("aa_wordList").getCol(1)) {
            for (String letter : cell.split("")) {
                if (!keysList.contains(letter)) {
                    warnings.add("In aa_wordList, the word " + cell + " contains the letter " + letter +
                            " which is not in the keyboard.");
                }
                else{
                    keyCount.put(letter, keyCount.get(letter)+1);
                }
            }
        }
        for (Map.Entry<String, Integer> entry : keyCount.entrySet()){
            if (entry.getValue()<6){
                warnings.add("In aa_wordList.txt, the letter " + entry.getKey() + " is only used" +
                        " in " + entry.getValue() + " words. It is recommended that each letter be" +
                        " used in at least 6 words.");
            }
        }
        for (String tile : rawFiles.getFromName("aa_gametiles").getCol(0)) {
            boolean tileFound = false;
            for (String word : rawFiles.getFromName("aa_wordList").getCol(0)) {
                if (word.contains(tile)) {
                    tileFound = true;
                    break;
                }
            }
            if (!tileFound){
                warnings.add("In aa_gametiles.txt, the tile " + tile + " is not used in any words.");
            }
        }

    }

    public ArrayList<String> getFatalErrors() {
        return fatalErrors;
    }

    public ArrayList<String> getWarnings() {
        return warnings;
    }


    private class ArrayFromTxt extends ArrayList<ArrayList<String>> {
        private Integer rowLen;
        private Integer colLen;
        private String name;


    }


    private class RawFile extends ArrayList<ArrayList<String>> {
        private String name;
        private Integer rowLen;


        public RawFile(String name, Integer rowLen) {
            super();
            this.name = name;
            this.rowLen = rowLen;

            this.checkAndBuildFile();
            this.checkArraySize();
        }


        public RawFile(String name) {
            super();
            this.name = name;
            this.checkAndBuildFile();
            this.checkArraySize();
        }

        public String getName() {
            return this.name;
        }

        public int getRowLen() {
            return this.rowLen;
        }

        public void checkAndBuildFile() {
            if (res.getIdentifier(this.name, "raw", packageName) == 0) {
                fatalErrors.add("The " + this.name + " folder does not exist.");
            } else {
                this.buildArray();
            }
        }

        public ArrayList<String> getCol(int colNum) {
            ArrayList<String> col = new ArrayList<>();
            for (ArrayList<String> row : this) {
                col.add(row.get(colNum));
            }
            return col;
        }

        private void buildArray() {
            int id = res.getIdentifier(this.name, "raw", packageName);
            try {
                Scanner scanner = new Scanner(res.openRawResource(id));
                while (scanner.hasNextLine()) {
                    this.add(new ArrayList(Arrays.asList(scanner.nextLine().split("\t"))));
                }
                scanner.close();
            } catch (Exception e) {
                fatalErrors.add("The " + name + " file is not readable");
            }
            this.checkArraySize();
        }

        private void checkArraySize() {
            if (this.rowLen != null) {
                for (ArrayList<String> row : this) {
                    if (row.size() != this.rowLen) {
                        fatalErrors.add("The row " + row.toString() + " in " + name + " is missing information");
                    }
                }
            }

        }
    }

    private class RawFilesArray extends ArrayList<RawFile> {
        public RawFilesArray() {
            super();
        }

        public void matchingCols(String name1, String name2, int colNum1, int colNum2, ArrayList<String> errorList) {
            RawFile file1 = this.getFromName(name1);
            RawFile file2 = this.getFromName(name2);

            if (file1 == null || file2 == null) {
                fatalErrors.add("The " + name1 + " or " + name2 + " file does not exist");
                return;
            }
            for (int i = 0; i < Math.max(file1.size(), file2.size()); i++) {
                if (i >= file1.size()) {
                    errorList.add("The " + name1 + " file is missing row " + i);
                } else if (i >= file2.size()) {
                    errorList.add("The " + name2 + " file is missing row " + i);
                } else if (!file1.get(i).get(colNum1).equals(file2.get(i).get(colNum2))) {
                    errorList.add("Column " + colNum1 + " of " + name1 + " and Column "
                            + colNum2 + " of " + name2 + " files do not match at row " + i);
                }
            }
        }

        public void matchingRows(String name1, String name2, int rowNum1, int rowNum2, ArrayList<String> errorList) {
            RawFile file1 = this.getFromName(name1);
            RawFile file2 = this.getFromName(name2);
            if (file1 == null || file2 == null) {
                fatalErrors.add("The " + name1 + " or " + name2 + " file does not exist");
                return;
            }
            for (int i = 0; i < Math.max(file1.get(rowNum1).size(), file2.get(rowNum2).size()); i++) {
                if (i >= file1.get(rowNum1).size()) {
                    errorList.add("The " + name1 + " file is missing column " + i);
                } else if (i >= file2.get(rowNum2).size()) {
                    errorList.add("The " + name2 + " file is missing column " + i);
                } else if (!file1.get(rowNum1).get(i).equals(file2.get(rowNum2).get(i))) {
                    errorList.add("Row " + rowNum1 + " of " + name1 + " and Row "
                            + rowNum2 + " of " + name2 + " files do not match at column " + i);
                }
            }
        }

        public RawFile getFromName(String name) {
            for (RawFile file : this) {
                if (file.getName().equals(name)) {
                    return file;
                }
            }
            fatalErrors.add("The " + name + " file does not exist");
            return null;
        }
    }
}

