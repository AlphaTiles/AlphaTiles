package org.alphatilesapps.alphatiles;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
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
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class Validator {
    private Set<String> fatalErrors = new LinkedHashSet<>();
    private Set<String> warnings = new LinkedHashSet<>();

    private final HashMap<String, String> ranges = new HashMap<>(Map.of("langinfo", "A1:B", "gametiles", "A1:Q", "wordlist", "A1:F",
            "keyboard", "A1:B36", "games", "A1:H", "syllables", "A1:G", "resources", "A1:C7", "settings", "A1:B",
            "colors", "A1:C"));
    private final String driveFolderUrl;
    private LangPackSpreadSheet toValidate;

    private TypeFilteredFolder images;

    private TypeFilteredFolder audio;


    private static final String credentialsJson = "{\"installed\":{\"client_id\":\"384994053794-tuci4d2mhf4caems7jalfmb4voi855b8.apps.googleusercontent.com\",\"project_id\":\"enhanced-medium-387818\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"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\",\"redirect_uris\":[\"http://localhost\"]}}";
    private static final String APPLICATION_NAME = "Alpha Tiles Validator";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final Sheets sheetsService =
            new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
    private final Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();


    public Validator(String driveFolderUrl) throws IOException, GeneralSecurityException {
        this.driveFolderUrl = driveFolderUrl;
    }

    public Set<String> getFatalErrors() {
        return fatalErrors;
    }

    public Set<String> getWarnings() {
        return warnings;
    }

    public void writeValidatedFiles(String path) throws IOException {
        for (Tab tab : toValidate) {
            FileWriter writer = new FileWriter(path + "/aa_" + tab.getName() + ".txt");
            writer.write(tab.toString());
            writer.close();
        }
    }

    public void validate() throws IOException, GeneralSecurityException {
        String driveFolderId = driveFolderUrl.substring(driveFolderUrl.indexOf("folders/") + 8);
        String toValidateSheetId = getSheetIdFromDriveFolder(driveFolderId);
        toValidate = new LangPackSpreadSheet(toValidateSheetId);

        String genericWarning = "one or more checks was not able to be run because of unresolved fatal errors";

        try {
            if (toValidate.getFromName("langinfo").size() != colLenFromRange(ranges.get("langinfo"))) {
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
            if (toValidate.getFromName("settings").size() != colLenFromRange(ranges.get("settings"))) {
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
            for (String cell : toValidate.getFromName("wordlist").getCol(1, true)) {
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
            int longWords = 0;
            tileUsage.put(".", 0);
            tileUsage.put("'", 0);
            for (String tile : toValidate.getFromName("gametiles").getCol(0, true)) {
                tileUsage.put(tile, 0);
            }
            for (String word : toValidate.getFromName("wordlist").getCol(1, true)) {
                int tileCounter = numTilesInWord(word, tileUsage, 0);
                if (tileCounter == 0) {
                    warnings.add("no combination of tiles can be put together to create " + word + "in wordlist");
                }

                if (tileCounter >= 10) {
                    if (tileCounter > 15) {
                        fatalErrors.add("the word " + " in wordlist takes more than 15 tiles to build");
                    } else {
                        longWords += 1;
                    }
                }
            }
            if (longWords > 0) {
                warnings.add("the wordlist has " + longWords + " long words (10 to 15 game tiles); shorter words are preferable in an early literacy game. Consider removing longer words ");
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
                if (new HashSet<>(alternates).size() < alternates.size()) {
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
                if (new HashSet<>(alternates).size() < alternates.size()) {
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

        images = new TypeFilteredFolder(driveFolderId, "images/png", "images");
        try {
            images.checkNamesAgainstWordList();
        }
        catch (Exception e){
            fatalErrors.add(genericWarning);
        }

        audio = new TypeFilteredFolder(driveFolderId, "audio/mpeg", "audio");
        try {
            audio.checkNamesAgainstWordList();
        }
        catch (Exception e){
            fatalErrors.add(genericWarning);
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


    private int numTilesInWord(String toParse, Map<String, Integer> tileUsage, int numTilesSoFar) throws Exception {
        Set<String> tileSet = tileUsage.keySet();
        Set<String> wordSet = new HashSet<>(toValidate.getFromName("wordlist").getCol(1, true));
        for (int i = toParse.length(); i > 0; i--) {
            String tileToCheck = toParse.substring(0, i);
            if (tileSet.contains(tileToCheck) && (i == toParse.length() || (numTilesInWord(toParse.substring(i), tileUsage, numTilesSoFar + 1)) != 0)) {
                tileUsage.put(tileToCheck, tileUsage.get(tileToCheck) + 1);
                return numTilesSoFar + 1;
            }
        }
        return 0;
    }

    private class LangPackSpreadSheet extends ArrayList<Tab> {
        private final String spreadsheetId;

        private final String name;


        public LangPackSpreadSheet(String spreadSheetID) throws IOException {
            super();
            this.spreadsheetId = spreadSheetID;
            this.name = sheetsService.spreadsheets().get(spreadsheetId).execute().getProperties().getTitle();
            for (Map.Entry<String, String> nameAndRange : ranges.entrySet()) {
                this.add(new Tab(nameAndRange.getKey(), nameAndRange.getValue(), spreadSheetID));
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

    private class Tab extends ArrayList<ArrayList<String>> {
        private String name;
        private Integer rowLen;


        public Tab(String inName, String range, String spreadsheetId) throws IOException {
            super();
            try {
                this.name = inName;
                this.rowLen = rowLenFromRange(range);
            } catch (Exception e) {
                throw new RuntimeException("The range " + range + " is not valid");
            }
            try {
                ValueRange response = sheetsService.spreadsheets().values()
                        .get(spreadsheetId, name + "!" + range)
                        .execute();
                for (List row : response.getValues()) {
                    ArrayList<String> newRow = new ArrayList<>();
                    for (Object cell : row) {
                        newRow.add(cell.toString().strip());
                    }
                    this.add(newRow);
                }
            } catch (Exception e) {
                fatalErrors.add("not able to find information in the tab " + range.substring(0, range.indexOf('!')) +
                        " or software was unable to access the sheet");
            }

            this.checkTabDataRectangular();
        }

        public String getName() {
            return this.name;
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

    private class TypeFilteredFolder extends ArrayList<File>{
        String driveFolderId;
        String name;

        protected TypeFilteredFolder(String driveFolderId, String fileType) throws IOException{
            String pageToken = null;
            this.driveFolderId = driveFolderId;
            this.name = driveService.files().get(driveFolderId).execute().getName();
            do {
                FileList result = driveService.files().list()
                        .setQ("mimeType='" + fileType + "' and parents in '" + driveFolderId + "'")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                this.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }

        protected TypeFilteredFolder (String driveFolderId, String fileType, String subFolderName) throws IOException {
            TypeFilteredFolder subFolder = new TypeFilteredFolder(driveFolderId,"application/vnd.google-apps.folder");
            boolean folderFound = false;
            for (File folder: subFolder){
                if (folder.getName().equals(subFolderName)){
                    this.driveFolderId = folder.getId();
                    this.name = folder.getName();
                    this.addAll(new TypeFilteredFolder(folder.getId(), fileType));
                    folderFound = true;
                }
            }
            if (!folderFound){
                fatalErrors.add("could not find folder with name " + subFolderName);}
        }

        protected void checkNamesAgainstWordList() throws Exception{
            PriorityQueue<String> words = new PriorityQueue<>(toValidate.getFromName("wordlist").getCol(0, true));
            for (File file : this) {
                boolean hasMatchingWord = false;
                for (String word : new ArrayList<>(words)) {
                    if (file.getName().startsWith(word)) {
                        words.remove(word);
                        hasMatchingWord = true;
                    }
                }
                if (!hasMatchingWord) {
                    warnings.add("the file " + file.getName() + " in " + this.name + " may be excess" +
                            "as the start of the filename does not match anything in wordlist");
                }
            }
            for (String word : words){
                warnings.add("the word " + word + "does not have a corresponding file in " + this.name);
            }
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
        List<String> SCOPES = Arrays.asList(new String[]{SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_METADATA_READONLY});
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
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private int rowLenFromRange(String range) {
        try {
            int ascii1 = (int) range.charAt(0);
            int ascii2 = (int) range.charAt(range.indexOf(':') + 1);
            return ascii2 - ascii1 + 1;
        } catch (Exception e) {
            throw new RuntimeException("requested ranges are invalid");
        }
    }

    private int colLenFromRange(String range) {
        try {
            int ascii1 = Integer.parseInt(String.valueOf(range.charAt(1)));
            int ascii2 = Integer.parseInt(String.valueOf(range.charAt(-1)));
            return ascii2 - ascii1 + 1;
        } catch (Exception e) {
            throw new RuntimeException("requested ranges are invalid");
        }

    }

    public String getSheetIdFromDriveFolder(String driveFolderId) throws IOException {
        TypeFilteredFolder allSheets = new TypeFilteredFolder(driveFolderId, "application/vnd.google-apps.spreadsheet");
        if (allSheets.size() == 0) {
            throw new RuntimeException("No google sheet found in specified google drive folder");
        } else if (allSheets.size() > 1) {
            throw new RuntimeException("More than one google sheet found in specified google drive folder");
        } else {
            return allSheets.get(0).getId();
        }
    }
}