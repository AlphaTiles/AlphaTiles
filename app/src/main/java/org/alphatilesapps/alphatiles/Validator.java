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
import java.util.Collections;
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
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


//todo... change exception structure to filter what gets caught
public class Validator {
    private Set<String> fatalErrors = new LinkedHashSet<>();
    private Set<String> warnings = new LinkedHashSet<>();

    private DriveFolder langPackDriveFolder;

    private GoogleSheet langPackGoogleSheet;

    //TODO double check the check size method
    private final HashMap<String, String> DESIRED_DATA_FROM_TABS = new HashMap<>(Map.of(
            "langinfo", "A1:B15",
            "gametiles", "A1:Q",
            "wordlist", "A1:F",
            "keyboard", "A1:B36",
            "games", "A1:H",
            "syllables", "A1:G",
            "resources", "A1:C7",
            "settings", "A1:B",
            "colors", "A1:C"));

    private final HashMap<String,String> DESIRED_FILETYPE_FROM_SUBFOLDERS = new HashMap<>(Map.of(
            "word_images", "images/",
            "word_audio" ,"audio/mpeg",
            "resource_images", "images/",
            "low_res_images", "image/",
            "(OPTIONAL)_tile_audio", "audio/mpeg",
            "(OPTIONAL)_instruction_audio", "audio/mpeg",
            "(OPTIONAL)_syllable_audio", "audio/mpeg"));

    private boolean hasInstructionAudio = false;
    private boolean hasTileAudio = false;
    private boolean hasSyllableAudio = false;
    private boolean usesSyllables = false;


    private final String GENERIC_WARNING = "one or more checks was not able to be run because of unresolved fatal errors";

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
        String driveFolderId = driveFolderUrl.substring(driveFolderUrl.indexOf("folders/") + 8);
        this.langPackDriveFolder = new DriveFolder(driveFolderId);
        this.langPackGoogleSheet = langPackDriveFolder.getOnlyGoogleSheet();
    }

    public void validate() throws Exception {

        this.validateRequiredSheetTabs();

        this.usesSyllables = decideIfSyllablesAttempted();
        if (usesSyllables) {
            this.validateSyllables();
        }

        this.validateResourceSubfolders();

        System.out.println("\nList of Fatal Errors\n********");
        for (String error : fatalErrors) {
            System.out.println(error);
        }
        System.out.println("\nList of Warnings\n********");
        for (String error : warnings) {
            System.out.println(error);
        }

    }

    public Set<String> getFatalErrors() {
        return this.fatalErrors;
    }

    public Set<String> getWarnings() {
        return this.warnings;
    }

    public void writeValidatedFiles(String path) throws IOException {
        for (Tab tab : langPackGoogleSheet.getTabList()){
            FileWriter writer = new FileWriter(path + "/aa_" + tab.getName() + ".txt");
            writer.write(tab.toString());
            writer.close();
        }
    }

    public abstract class GoogleDriveItem{
        private String id;
        private String name;
        protected GoogleDriveItem(String inID){
            this.id = inID;
        }

        protected GoogleDriveItem(String inID, String inName){
            this.id = inID;
            this.name = inName;
        }

        protected String getName() {
            return this.name;
        }

        protected abstract String getMimeType();

        protected String getId() {
            return this.id;
        }
    }

    private class DriveFolder extends GoogleDriveItem{

        private ArrayList<GoogleDriveItem> folderContents = new ArrayList<>();
        protected DriveFolder(String driveFolderId) throws IOException{
            super(driveFolderId);
            super.name = driveService.files().get(driveFolderId).execute().getName();
            String pageToken = null;
            do {
                FileList result = driveService.files().list()
                        .setQ("parents in '" + driveFolderId + "'")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .setPageToken(pageToken)
                        .execute();

                for (File file : result.getFiles()){

                    if (file.getMimeType().equals("application/vnd.google-apps.spreadsheet")) {
                        folderContents.add(new GoogleSheet(file.getId()));
                    }
                    else if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                        folderContents.add(new DriveFolder(file.getId()));
                    }
                    else {
                        folderContents.add(new DriveResource(file.getId(), file.getName(), file.getMimeType()));
                    }
                 }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }

        @Override
        protected String getMimeType(){
            return "application/vnd.google-apps.folder";
        }

        protected DriveFolder getFolderFromName(String inName){
            for (DriveFolder item : this.findAllOfType(DriveFolder.class)){
                if (item.getName().equals(inName)){
                    return item;
                }
            }
            warnings.add("was not able to find " + inName + " in the drive folder " + this.getName());
            return null;
        }

        protected void checkFileNameAgainstList(ArrayList<String> toMatch) throws Exception{
            PriorityQueue<String> LongestFirstToMatch = new PriorityQueue<>(toMatch);
            for (GoogleDriveItem item : folderContents) {
                boolean hasMatchingName = false;
                for (String candidate : new ArrayList<>(LongestFirstToMatch)) {
                    if (item.getName().startsWith(candidate)) {
                        LongestFirstToMatch.remove(candidate);
                        hasMatchingName = true;
                    }
                }
                if (!hasMatchingName) {
                    warnings.add("the file " + item.getName() + " in " + this.getName() + " may be excess" +
                            "as the start of the filename does not appear to match to anything");
                }
            }
            for (String shouldHaveMatched : LongestFirstToMatch){
                warnings.add(shouldHaveMatched + "does not have a corresponding file in " + this.getName());
            }
        }

        protected <type extends GoogleDriveItem> ArrayList<type> findAllOfType(Class<type> myClass) {
            ArrayList<type> filteredList = new ArrayList<>();
            for (GoogleDriveItem item : folderContents) {
                try {
                    filteredList.add((myClass.cast(item)));
                }
                catch (ClassCastException e){
                }
                }
            return filteredList;
        }

        protected void filterByMimeType(String mimeType) {
            for (GoogleDriveItem item : new ArrayList<>(folderContents)) {
                if (!item.getMimeType().contains(mimeType)) {
                    folderContents.remove(item);
                    warnings.add(item.getName() + " will be ignored in " + this.getName() + " as it was not of type " + mimeType);
                }
            }
        }

        protected GoogleSheet getOnlyGoogleSheet(){
            ArrayList<GoogleSheet> allSheets = findAllOfType(GoogleSheet.class);
            if (allSheets.size() == 0) {
                throw new RuntimeException("No google sheet found in specified google drive folder");
            } else if (allSheets.size() > 1) {
                throw new RuntimeException("More than one google sheet found in specified google drive folder");
            } else {
                return allSheets.get(0);
            }
        }

        protected int size(){
            return folderContents.size();
        }


    }
    private class DriveResource extends GoogleDriveItem{

        private String mimeType;
        protected DriveResource(String inParentId, String inName, String inMimeType){
            super(inParentId, inName);
            this.mimeType = inMimeType;
        }
        @Override
        protected String getMimeType(){
            return this.mimeType;
        }
    }
    private class GoogleSheet extends GoogleDriveItem {
        private ArrayList<Tab> tabList = new ArrayList<>();

        protected GoogleSheet(String spreadSheetId) throws IOException {
            super(spreadSheetId);
            Spreadsheet thisSpreadsheet = sheetsService.spreadsheets().get(spreadSheetId).execute();
            super.name = thisSpreadsheet.getProperties().getTitle();
            List<Sheet> sheetList = thisSpreadsheet.getSheets();
            for (Sheet sheet : sheetList){
                tabList.add(new Tab(spreadSheetId, sheet.getProperties().getTitle())) ;

            }
        }

        @Override
        protected String getMimeType(){
            return "application/vnd.google-apps.spreadsheet";
        }
        public ArrayList<Tab> getTabList(){
            return this.tabList;
        }

        public Tab getFromName(String name) throws Exception {
            for (Tab tab : tabList) {
                if (tab.getName().equals(name)) {
                    return tab;
                }
            }
            throw new Exception("The " + name + " file does not exist");
        }
    }
    private class Tab extends ArrayList<ArrayList<String>> {
        private String name;
        private Integer rowLen;
        private Integer colLen;
        private String spreadSheetId;

        protected Tab(String inSpreadsheetId, String inName) {
            super();
            this.name = inName;
            this.spreadSheetId = inSpreadsheetId;
            try {
                ValueRange response = sheetsService.spreadsheets().values()
                        .get(spreadSheetId, name + "!A1:Z")
                        .execute();
                for (List row : response.getValues()) {
                    ArrayList<String> newRow = new ArrayList<>();
                    for (Object cell : row) {
                        newRow.add(cell.toString().strip());
                    }
                    this.add(newRow);
                }
            } catch (Exception e) {
                fatalErrors.add("not able to find information in the tab " + this.name +
                        " or software was unable to access the sheet");
            }

        }

        private void sizeTabUsingRange(String inRange) {
            this.rowLen = rowLenFromRange(inRange);
            this.colLen = colLenFromRange(inRange);
            if (this.colLen != null){
                if (this.size() < this.colLen){
                    fatalErrors.add("the tab " + this.name + " does not have enough rows. It should" +
                            "have " + this.colLen);
                }

                for (int i = this.size() -1 ; i >= this.colLen; i--){
                    this.remove(i);
                }
            }
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).size() > this.rowLen){
                    this.set(i, new ArrayList<>(this.get(i).subList(0, this.rowLen)));
                }
                if (this.get(i).size() == 0 || new HashSet<>(this.get(i)).size() == 1 && this.get(i).get(0).equals("")){
                    this.remove(i);
                    i--;
                }

                else if (this.get(i).size() < this.rowLen || this.get(i).contains("")) {
                    fatalErrors.add("The row " + (i + 1) + " in " + this.name + " is missing information");
                }
            }



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

        private void checkColForDuplicates(int colNum) {
            Set<String> colSet = new HashSet<>();
            for (String cell : this.getCol(colNum)) {
                if (!colSet.add(cell)) {
                    warnings.add(cell + " appears more than once in column " + colNum + " of " + this.getName());
                }
            }
        }

        private Integer rowLenFromRange(String range) {
            try {
                int ascii1 = (int) range.charAt(0);
                int ascii2 = (int) range.charAt(range.indexOf(':') + 1);
                return ascii2 - ascii1 + 1;
            } catch (Exception e) {
                throw new RuntimeException("requested ranges are invalid");
            }
        }

        private Integer colLenFromRange(String range) {
            try {
                int ascii1 = Integer.parseInt(range.substring(1, range.indexOf(':')));
                int ascii2 = Integer.parseInt(range.substring(range.indexOf(':') + 2));
                return ascii2 - ascii1 + 1;
            } catch (Exception e) {
                return null;
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

    private void validateRequiredSheetTabs() throws Exception{


        for (Map.Entry<String, String> nameAndRange : DESIRED_DATA_FROM_TABS.entrySet()) {
            Tab desiredTab = langPackGoogleSheet.getFromName(nameAndRange.getKey());
            desiredTab.sizeTabUsingRange(nameAndRange.getValue());
        }

        try {
            for (String cell : langPackGoogleSheet.getFromName("wordlist").getCol(0)) {
                if (!cell.matches("[a-zA-Z0-9_]+")) {
                    fatalErrors.add("In the first column of wordList, the word " + cell + " contains non-alphanumeric characters. " +
                            "Please remove them. (The only allowed characters are a-z, A-Z, and 0-9.)");
                }
            }
        } catch (Exception e) {
            warnings.add(GENERIC_WARNING);
        }
        try {
            Map<String, Integer> keyUsage = new HashMap<>();
            keyUsage.put(".", 0);
            keyUsage.put("'", 0);
            for (String key : langPackGoogleSheet.getFromName("keyboard").getCol(0, true)) {
                keyUsage.put(key, 0);
            }
            for (String cell : langPackGoogleSheet.getFromName("wordlist").getCol(1, true)) {
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
            warnings.add(GENERIC_WARNING);
        }
        try {
            for (String tile : langPackGoogleSheet.getFromName("gametiles").getCol(0, true)) {
                boolean tileFound = false;
                int wordIndex = 0;
                ArrayList<String> wordArray = langPackGoogleSheet.getFromName("wordlist").getCol(1, true);
                while (!tileFound && wordIndex < wordArray.size()) {
                    if (langPackGoogleSheet.getFromName("wordlist").getCol(1).get(wordIndex).contains(tile)) {
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
            warnings.add(GENERIC_WARNING);
        }
        try {
            Map<String, Integer> tileUsage = new HashMap<>();
            int longWords = 0;
            tileUsage.put(".", 0);
            tileUsage.put("'", 0);
            for (String tile : langPackGoogleSheet.getFromName("gametiles").getCol(0, true)) {
                tileUsage.put(tile, 0);
            }
            for (String word : langPackGoogleSheet.getFromName("wordlist").getCol(1, true)) {
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
            warnings.add(GENERIC_WARNING);
        }
        try {
            Tab gameFiles = langPackGoogleSheet.getFromName("gametiles");
            gameFiles.checkColForDuplicates(0);
            for (ArrayList<String> row : gameFiles) {
                List<String> alternates = row.subList(1, 4);
                if (new HashSet<>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in gametiles has the same tile appearing in multiple places");
                }
            }
        } catch (Exception e) {
            warnings.add(GENERIC_WARNING);
        }
        try {
            langPackGoogleSheet.getFromName("wordlist").checkColForDuplicates(0);
            langPackGoogleSheet.getFromName("wordlist").checkColForDuplicates(1);
        } catch (Exception e) {
            warnings.add(GENERIC_WARNING);
        }
        try {
            langPackGoogleSheet.getFromName("settings").checkColForDuplicates(0);
            langPackGoogleSheet.getFromName("settings").checkColForDuplicates(1);
        } catch (Exception e) {
            warnings.add(GENERIC_WARNING);
        }
    }

    private void validateResourceSubfolders() throws Exception{

        for (Map.Entry<String, String> nameToMimeType : this.DESIRED_FILETYPE_FROM_SUBFOLDERS.entrySet()){
            try {
                DriveFolder subFolder = langPackDriveFolder.getFolderFromName(nameToMimeType.getKey());
                subFolder.filterByMimeType(nameToMimeType.getValue());
            } catch (Exception e) {
                warnings.add(GENERIC_WARNING);
            }
        }
        this.hasInstructionAudio = decideIfAudioAttempted("games", 4, "(OPTIONAL)_instruction_audio");
        this.hasSyllableAudio = decideIfAudioAttempted("syllables", 4, "(OPTIONAL)_syllable_audio");
        this.hasTileAudio = decideIfAudioAttempted("gametiles", 5, "(OPTIONAL)_tile_audio");

        try {
        DriveFolder resourceImages = langPackDriveFolder.getFolderFromName("resource_images");
        ArrayList<String> resourceImageNames = langPackGoogleSheet.getFromName("resources").getCol(1, true);
        resourceImages.checkFileNameAgainstList(resourceImageNames);
    } catch (Exception e) {
        warnings.add(GENERIC_WARNING);
    }
        try {
        DriveFolder wordImages = langPackDriveFolder.getFolderFromName("word_images");
        ArrayList<String> wordImageNames = langPackGoogleSheet.getFromName("wordlist").getCol(0, true);
        wordImages.checkFileNameAgainstList(wordImageNames);
} catch (Exception e) {
        warnings.add(GENERIC_WARNING);
        }
        try {
        DriveFolder wordAudio = langPackDriveFolder.getFolderFromName("word_audio");
        //TODO find right word for broader language
        ArrayList<String> wordsInBroaderLanguage = langPackGoogleSheet.getFromName("wordlist").getCol(0, true);
        wordAudio.checkFileNameAgainstList(wordsInBroaderLanguage);
        } catch (Exception e) {
        warnings.add(GENERIC_WARNING);
        }
        try {
        if (hasTileAudio) {
            DriveFolder tileAudio = langPackDriveFolder.getFolderFromName("(OPTIONAL)_tile_audio");
            ArrayList<String> tiles = langPackGoogleSheet.getFromName("gametiles").getCol(5, true);
            tileAudio.checkFileNameAgainstList(tiles);
        }
        } catch (Exception e) {
        warnings.add(GENERIC_WARNING);
        }
        try {
        if (hasSyllableAudio) {
            DriveFolder syllableAudio = langPackDriveFolder.getFolderFromName("(OPTIONAL)_syllable_audio");
            ArrayList<String> syllables = langPackGoogleSheet.getFromName("syllables").getCol(4, true);
            syllableAudio.checkFileNameAgainstList(syllables);
        }
        } catch (Exception e) {
        warnings.add(GENERIC_WARNING);
        }
        try {
        if (hasInstructionAudio) {
            DriveFolder instructionAudio = langPackDriveFolder.getFolderFromName("(OPTIONAL)_instruction_audio");
            ArrayList<String> gamesList = langPackGoogleSheet.getFromName("games").getCol(4, true);
            instructionAudio.checkFileNameAgainstList(gamesList);
        }
        } catch (Exception e) {
        warnings.add(GENERIC_WARNING);
        }
    }

    private void validateSyllables(){

        try {
            Tab syllables = langPackGoogleSheet.getFromName("syllables");
            syllables.checkColForDuplicates(0);
            for (ArrayList<String> row : syllables) {
                List<String> alternates = row.subList(1, 4);
                if (new HashSet<>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in syllables has the same tile appearing in multiple places");
                }
            }
        } catch (Exception e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            HashSet<String> providedSyllables = new HashSet<>(langPackGoogleSheet.getFromName("syllables").getCol(0, true));
            HashSet<String> parsedSyllables = new HashSet<>();
            for (String word : langPackGoogleSheet.getFromName("wordlist").getCol(1, true)) {
                String[] syllablesInWord = word.split("\\.");
                parsedSyllables.addAll(Arrays.asList(syllablesInWord));
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
            warnings.add(GENERIC_WARNING);
        }

    }

    private boolean decideIfAudioAttempted(String tabName, int colNum, String subFolderName) throws Exception{

        boolean someAudioFiles = false;
        boolean someAudioNames = false;

        try {
            DriveFolder subFolder = langPackDriveFolder.getFolderFromName(subFolderName);
            if (subFolder.getName().equals(subFolderName) && subFolder.size() > 0) {
                someAudioFiles = true;
            }
        }
        catch (Exception e){
            warnings.add(GENERIC_WARNING);
        }

        ArrayList<String> AudioNames = langPackGoogleSheet.getFromName(tabName).getCol(colNum, true);
        AudioNames.removeAll(Collections.singleton("X"));
        if (AudioNames.size() > 0){
            someAudioNames = true;
        }

        if (someAudioNames && someAudioFiles){
            return true;
        } else if (someAudioNames) {
            warnings.add("you list names of audio files in the column " + colNum + " of " + tabName
            + " (ie you have text in the column that is not 'X') but " + subFolderName + " is empty"
            + " please add matching audio files to " + subFolderName + " if you want to use this feature");
        } else if (someAudioFiles) {
            warnings.add("you have audio files in the folder " + subFolderName + " but column"
                    + " of " + tabName + " doesn't list any audio file names"
                    + " please add matching audio file names to " + tabName + " if you want to use this feature");
        }
        return false;
    }

    private boolean decideIfSyllablesAttempted() throws Exception{

        boolean numerousWordsSpliced = false;
        boolean syllTabNotEmpty = false;

        int wordsSpliced = 0;
        for (String word : langPackGoogleSheet.getFromName("wordlist").getCol(1, true)) {
            if (word.contains(".")) {
                wordsSpliced += 1;
            }
        }
        if (wordsSpliced > 6) {
            numerousWordsSpliced = true;
        }
        if (langPackGoogleSheet.getFromName("syllables").size() > 0){
            syllTabNotEmpty = true;
        }

        if (syllTabNotEmpty && numerousWordsSpliced){
            return true;
        } else if (syllTabNotEmpty) {
            warnings.add("you have more than 6 words in wordlist that are spliced with periods"
                    + " but the syllables tab is empty."
                    + " Please add syllables to the syllables tab if you want to use syllable games");
        } else if (numerousWordsSpliced) {
            warnings.add("your syllables tab is not empty but your words in wordlist aren't " +
                    "spliced with periods. Please sylables with a period in wordlist" +
                    " if you want to use syllable games");
        }
        return false;
    }

    private int numTilesInWord(String toParse, Map<String, Integer> tileUsage, int numTilesSoFar) throws Exception {
        Set<String> tileSet = tileUsage.keySet();
        for (int i = toParse.length(); i > 0; i--) {
            String tileToCheck = toParse.substring(0, i);
            if (tileSet.contains(tileToCheck) && (i == toParse.length() || (numTilesInWord(toParse.substring(i), tileUsage, numTilesSoFar + 1)) != 0)) {
                tileUsage.put(tileToCheck, tileUsage.get(tileToCheck) + 1);
                return numTilesSoFar + 1;
            }
        }
        return 0;
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
}
