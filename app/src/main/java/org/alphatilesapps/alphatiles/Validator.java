package org.alphatilesapps.alphatiles;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Validator {
    private static final String credentialsJson = "{\"installed\":{\"client_id\":\"384994053794-tuci4d2mhf4caems7jalfmb4voi855b8.apps.googleusercontent.com\",\"project_id\":\"enhanced-medium-387818\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"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\",\"redirect_uris\":[\"http://localhost\"]}}";
    private final String url;
    private Set<String> fatalErrors = new HashSet<>();
    private Set<String> warnings = new HashSet<>();
    private static final String APPLICATION_NAME = "Alpha Tiles Validator";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private ArrayList<String> ranges = new ArrayList<>(List.of("langinfo!A1:B", "gametiles!A1:Q", "wordlist!A1:f",
            "keyboard!A1:B36", "games!A1:H", "syllables!A1:G", "resources!A1:C7", "settings!A1:B", "settings!A1:B",
            "colors!A1:C"));
    private final String templateSheetId = "1HPi_N6AoHiG6DkY16oKG9LrFr_a8PBzeYUaM6oP-g8w";
    private String toValidateSheetId;
    private TabArray template;
    private TabArray toValidate;

    private final String genericWarning = "one or more checks was not able to be run because of unresolved fatal errors";
    private int longWords = 0;
    private int tileCounter = 0;

    public Validator(String url) {
        this.url = url;
    }
    public Set<String> getFatalErrors() {
        return fatalErrors;
    }

    public Set<String> getWarnings() {
        return warnings;
    }

    public void writeValidatedFiles(String path) throws IOException{
        for (Tab tab : toValidate){
            FileWriter writer = new FileWriter(path + "/aa_" + tab.getName() + ".txt");
            writer.write(tab.toString());
            writer.close();
        }
    }
    public void validate() throws IOException, GeneralSecurityException {
        //template represents the entire template google sheet
        template = new TabArray(templateSheetId, ranges, true);
        toValidateSheetId = sheetIdFromUrl();
        //toValidate represents the entire google sheet that is being validated
        toValidate = new TabArray(toValidateSheetId, ranges, false);

        for (Tab templateTab : template) {

            try {
                Tab tabToVal = toValidate.getFromName(templateTab.getName());
                // the rowRen field in each tab to be validated is set to the number of columns in the tab's template
                tabToVal.setRowLen(templateTab.getRowLen());
                // each row in that tab is checked to make sure it has the correct length
                tabToVal.checkTabDataRectangular();
                // each tab is checked to make sure its header (the first row) matches the tab's template
                if (!tabToVal.get(0).equals(templateTab.get(0)) && !templateTab.getName().equals("wordlist")) {
                    warnings.add("heading of " + templateTab.getName() + " is different from template");
                }
            } catch (Exception e) {
                warnings.add(genericWarning);
            }
        }
        try {
            if (!template.getFromName("langinfo").getCol(0).equals(toValidate.getFromName("langinfo").getCol(0))) {
                warnings.add("column 1 of langinfo tab does not match template");
            }
            for (String cell : toValidate.getFromName("langinfo").getCol(1)) {
                if (cell.contains("Ꞌ") || cell.contains("ꞌ")) {
                    warnings.add("In langinfo.txt, the \"saltillo\" [ < ꞌ >,  glottal stop, " +
                            "Excel.UNICODE()=42892, VBA.AscW()=-22644] will not display correctly in " +
                            "devices Android 4.4 and older. It could be replaced with the apostrophe" +
                            " [< ' >, UNICODE()=39] or prime [< ′ >, UNICODE()=8242].");
                }

            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            if (!template.getFromName("settings").getCol(0).equals(toValidate.getFromName("settings").getCol(0))) {
                warnings.add("column 1 of settings tab does not match template");
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            for (String cell : toValidate.getFromName("wordlist").getCol(0)) {
                if (!cell.matches("[a-zA-Z0-9_]+")) {
                    fatalErrors.add("In the first column of wordList, the word " + cell + " contains non-alphanumeric characters. " +
                            "Please remove them. (The only allowed characters are a-z, A-Z, and 0-9.)");
                }
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            Map<String, Integer> keyUsage = new HashMap<>();
            keyUsage.put(".", 0);
            keyUsage.put("'", 0);
            for (String key : toValidate.getFromName("keyboard").getCol(0, true)) {
                keyUsage.put(key, 0);
            }
            for (String cell : toValidate.getFromName("wordlist").getCol(1)) {
                for (String letter : cell.split("")) {
                    if (!keyUsage.containsKey(letter)) {
                        warnings.add("In wordList, the word " + cell + " contains the letter " + letter +
                                " which is not in the keyboard.");
                    } else {
                        keyUsage.put(letter, keyUsage.get(letter) + 1);
                    }
                }
            }
            for (Map.Entry<String, Integer> entry : keyUsage.entrySet()) {
                if (entry.getValue() < 6) {
                    warnings.add("In wordList.txt, the letter " + entry.getKey() + " is only used" +
                            " in " + entry.getValue() + " words. It is recommended that each letter be" +
                            " used in at least 6 words.");
                }
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            for (String tile : toValidate.getFromName("gametiles").getCol(0, true)) {
                boolean tileFound = false;
                int wordIndex = 0;
                ArrayList<String> wordArray = toValidate.getFromName("wordlist").getCol(1, true);
                while (!tileFound && wordIndex < wordArray.size()) {
                    if (toValidate.getFromName("wordlist").getCol(1).get(wordIndex).contains(tile)) {
                        tileFound = true;
                    } else {
                        wordIndex += 1;
                        if (wordIndex == wordArray.size()) {
                            warnings.add("tile " + tile + " is never used in a word");
                        }
                    }
                }
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }

        try {
            Map<String, Integer> tileUsage = new HashMap<>();
            tileUsage.put(".", 0);
            tileUsage.put("'", 0);
            for (String tile : toValidate.getFromName("gametiles").getCol(0, true)) {
                tileUsage.put(tile, 0);
            }
            for (String word : toValidate.getFromName("wordlist").getCol(1, true)) {

                if (!parse(word, tileUsage)) {
                    warnings.add("no combination of tiles can be put together to create " + word + "in wordlist");
                }

                if (tileCounter >= 10) {
                    if (tileCounter >= 15) {
                        fatalErrors.add("the word " + " in wordlist takes more than 15 tiles to build");
                    } else {
                        longWords += 1;
                    }
                }
                tileCounter = 0;
            }

            for (Map.Entry<String, Integer> tile : tileUsage.entrySet()) {
                if (tile.getValue() < 6) {
                    warnings.add("the tile " + tile.getKey() + " in gametiles only appears in words " + tile.getValue()
                            + " times. It is recommended that each letter be used in at least 6 words");
                }
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            Tab gameFiles = toValidate.getFromName("gametiles");
            gameFiles.checkColForDuplicates(0);
            for (ArrayList<String> row : gameFiles) {
                List<String> alternates = row.subList(1, 4);
                if (new HashSet<String>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in gametiles has the same tile appearing in multiple places");
                }
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            toValidate.getFromName("wordlist").checkColForDuplicates(0);
            toValidate.getFromName("wordlist").checkColForDuplicates(1);
        } catch (Exception e) {
            warnings.add(genericWarning);
        }

        try {
            toValidate.getFromName("settings").checkColForDuplicates(0);
            toValidate.getFromName("settings").checkColForDuplicates(1);
        } catch (Exception e) {
            warnings.add(genericWarning);
        }
        try {
            Tab syllables = toValidate.getFromName("syllables");
            syllables.checkColForDuplicates(0);
            for (ArrayList<String> row : syllables) {
                List<String> alternates = row.subList(1, 4);
                if (new HashSet<String>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in syllables has the same tile appearing in multiple places");
                }
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }

        try {
            HashSet<String> providedSyllables = new HashSet<>(toValidate.getFromName("syllables").getCol(0, true));
            HashSet<String> parsedSyllables = new HashSet<>();
            for (String word : toValidate.getFromName("wordlist").getCol(1, true)) {
                String[] syllablesInWord = word.split("\\.");
                for (String syllable : syllablesInWord) {
                    parsedSyllables.add(syllable);
                }
            }
            HashSet<String> providedSyllCopy = new HashSet<>(providedSyllables);
            providedSyllCopy.removeAll(parsedSyllables);
            parsedSyllables.removeAll(providedSyllables);
            for (String notInParsed : providedSyllCopy) {
                warnings.add("Syllable " + notInParsed + " is never used in a word in wordlist");
            }
            for (String notInProvided : parsedSyllables) {
                warnings.add("Syllable " + notInProvided + " is used in wordlist but not in the syllables tab");
            }
        } catch (Exception e) {
            warnings.add(genericWarning);
        }


        System.out.println("\nList of Fatal Errors\n********");
        for (String error : fatalErrors) {
            System.out.println(error);
        }
        System.out.println("\nList of Warnings\n********");
        for (String error : warnings) {
            System.out.println(error);
        }
    }


    private Boolean parse(String toParse, Map<String, Integer> tileUsage) throws Exception {
        Set<String> tileSet = tileUsage.keySet();
        Set<String> wordSet = new HashSet<>(toValidate.getFromName("wordlist").getCol(1, true));
        for (int i = toParse.length(); i > 0; i--) {
            String tileToCheck = toParse.substring(0, i);
            if (tileSet.contains(tileToCheck) && (i == toParse.length() || parse(toParse.substring(i), tileUsage))) {
                tileUsage.put(tileToCheck, tileUsage.get(tileToCheck) + 1);
                tileCounter += 1;
                return true;
            }
        }
        return false;
    }

    private String sheetIdFromUrl() throws IOException {

        //System.out.println("please enter URL of google sheet to validate ");
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //String url = br.readLine();
        //br.close();

        try {
            int substringStartIndex = url.indexOf("/d/") + 3;
            int substringEndIndex = url.indexOf("/", substringStartIndex);
            return url.substring(substringStartIndex, substringEndIndex);
        } catch (Exception e) {
            throw new RuntimeException("provided sheet url invalid");
        }
    }

    private class Tab extends ArrayList<ArrayList<String>> {
        private String name;
        private Integer rowLen;


        public Tab(List<List<Object>> inData, String name, Integer rowLen) {
            this(inData, name);
            this.rowLen = rowLen;
        }

        public Tab(List<List<Object>> inData, String name) {
            super();
            this.name = name;
            for (List row : inData) {
                ArrayList<String> newRow = new ArrayList<>();
                for (Object cell : row) {
                    newRow.add(cell.toString().strip());
                }
                this.add(newRow);
            }
        }

        public String getName() {
            return this.name;
        }

        public int getRowLen() {
            return this.rowLen;
        }

        public void setRowLen(int rowLen) {
            this.rowLen = rowLen;
        }

        public ArrayList<String> getCol(int colNum) {
            ArrayList<String> col = new ArrayList<>();
            for (ArrayList<String> row : this) {
                col.add(row.get(colNum));
            }
            return col;
        }

        public ArrayList<String> getCol(int colNum, boolean excludeHeader) {
            ArrayList<String> toReturn = new ArrayList<>(this.getCol(colNum));
            toReturn.remove(0);
            return toReturn;
        }

        private void checkTabDataRectangular() {
            if (this.rowLen != null) {
                for (int i = 0; i < this.size(); i++) {
                    if (this.get(i).size() != this.rowLen || this.get(i).contains("")) {
                        fatalErrors.add("The row " + (i + 1) + " in " + name + " is missing information");
                    }
                }
            }

        }

        private void checkColForDuplicates(int colNum) {
            Set<String> colSet = new HashSet<>();
            for (String cell : this.getCol(colNum)) {
                if (!colSet.add(cell)) {
                    warnings.add(cell + " appears more than once in column " + colNum + " of " + this.getName());
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder toReturn = new StringBuilder();
            for (int i = 0; i < this.size(); i++) {
                for (int o = 0; o < this.get(i).size(); o++) {
                    toReturn.append(this.get(i).get(o));
                    if (o < (this.get(i).size() - 1))
                        toReturn.append("\t");
                    else
                        toReturn.append("\n");
                }
            }
            return toReturn.toString();
        }

    }


    private class TabArray extends ArrayList<Tab> {
        private String spreadsheetId = "";
        private ArrayList<String> ranges = new ArrayList<>();

        public TabArray(String spreadSheetID, ArrayList<String> ranges, boolean hasCorrectRowLen) throws IOException, GeneralSecurityException {
            super();
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.spreadsheetId = spreadSheetID;
            this.ranges = ranges;
            Sheets service =
                    new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
            for (String range : ranges) {
                try {
                    ValueRange response = service.spreadsheets().values()
                            .get(spreadsheetId, range)
                            .execute();
                    List<List<Object>> values = response.getValues();
                    if (hasCorrectRowLen) {
                        this.add(new Tab(values, range.substring(0, range.indexOf('!')), values.get(0).size()));
                    } else {
                        this.add(new Tab(values, range.substring(0, range.indexOf('!'))));
                    }
                } catch (Exception e) {
                    fatalErrors.add("not able to find information in the tab " + range.substring(0, range.indexOf('!')) +
                            " or software was unable to access the sheet");
                }
            }
        }

        public Tab getFromName(String name) throws Exception {
            for (Tab file : this) {
                if (file.getName().equals(name)) {
                    return file;
                }
            }
            throw new Exception("The " + name + " file does not exist");
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {

        String TOKENS_DIRECTORY_PATH = "tokens";

        /**
         * Global instance of the scopes required by this quickstart.
         * If modifying these scopes, delete your previously saved tokens/ folder.
         */
        List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
        //String CREDENTIALS_FILE_PATH = "/credentials.json";
        // Load client secrets.
        InputStream in = new ByteArrayInputStream(credentialsJson.getBytes());
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + "credentials.json");
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}