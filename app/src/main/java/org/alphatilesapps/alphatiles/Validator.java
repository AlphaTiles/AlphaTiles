package org.alphatilesapps.alphatiles;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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

public class Validator {

    private final Set<String> fatalErrors = new LinkedHashSet<>();
    private final Set<String> warnings = new LinkedHashSet<>();

    private Drive driveService;
    private final DriveFolder langPackDriveFolder;

    private Sheets sheetsService;
    private final GoogleSheet langPackGoogleSheet;

    private final HashMap<String, String> DESIRED_DATA_FROM_TABS = new HashMap<>(Map.of(
            "langinfo", "A1:B15",
            "gametiles", "A1:Q",
            "wordlist", "A1:F",
            "keyboard", "A1:B",
            "games", "A1:H",
            "syllables", "A1:G",
            "resources", "A1:C7",
            //TODO should this be a fixed number?
            "settings", "A1:B",
            "colors", "A1:C"));

    private final HashMap<String,String> DESIRED_FILETYPE_FROM_SUBFOLDERS = new HashMap<>(Map.of(
            "images_words", "image/",
            "audio_words" ,"audio/mpeg",
            "images_resources_optional", "image/",
            "images_words_low_res", "image/",
            "audio_tiles_optional", "audio/mpeg",
            "audio_instructions_optional", "audio/mpeg",
            "audio_syllables_optional", "audio/mpeg"));

    private final String GENERIC_WARNING = "one or more checks was not able to be run because of " +
            "unresolved fatal errors";

    public Validator(String driveFolderUrl) throws IOException, GeneralSecurityException, ValidatorException {

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));

        String driveFolderId = driveFolderUrl.substring(driveFolderUrl.indexOf("folders/") + 8);
        buildServices(driveFolderId);
        this.langPackDriveFolder = new DriveFolder(driveFolderId);
        this.langPackGoogleSheet = langPackDriveFolder.getOnlyGoogleSheet();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    //TODO will requiring a higher minimum api matter for when the validator class goes into an app if its not used?
    public void validate() {

        this.validateRequiredSheetTabs();

        boolean usesSyllables = decideIfSyllablesAttempted();
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

    private void validateRequiredSheetTabs(){

        // this first step is looks at the desired data from tabs field set at the top of the
        // code, searches for a tab that has the matching name, and shrinks the internal representation
        // of those tabs to only be what is specified

        for (Map.Entry<String, String> nameAndRange : DESIRED_DATA_FROM_TABS.entrySet()) {
            try {
                Tab desiredTab = langPackGoogleSheet.getTabFromName(nameAndRange.getKey());
                desiredTab.sizeTabUsingRange(nameAndRange.getValue());
            }
            catch (ValidatorException e){
                fatalErrors.add("the tab " + nameAndRange.getKey() + " does not appear in the" +
                        "language pack google sheet");
            }

        }

        //Each try catch block is essentially one check. Each check uses a catch block so that
        // that check will simply be passed over if a helper method throws a ValidatorException
        //(ie the helper method is unable to do what it is supposed to in a meaningful
        // way because of an existing fatal error (like missing a tab in the google sheet)).

        try {
            for (String cell : langPackGoogleSheet.getTabFromName("wordlist").getCol(0)) {
                if (!cell.matches("[a-z0-9_]+")) {
                    fatalErrors.add("In the first column of wordList, the word " + cell + " contains non-alphanumeric " +
                            "characters. " + "Please remove them. (The only allowed characters are a-z, 0-9, and _)");
                }
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        // Todo... make checks more readable...
        try {
            Tab keyboardTab = langPackGoogleSheet.getTabFromName("keyboard");

            ArrayList<String> themeColorCol = keyboardTab.getCol(1);
            themeColorCol.removeAll(Set.of("0", "1", "2", "3", "4"));
            if (themeColorCol.size() > 0){
                warnings.add("colum 2 of keyboard should only have numbers 0-4");
            }

            // the below section compares they keys in keyboard to the words in wordlist
            if (!keyboardTab.getCol(0).contains(".")) {
                keyboardTab.add(new ArrayList<>(List.of(".", "0")));
            }

            // this is a map of each key to how many times it is used in the wordslist
            Map<String, Integer> keyUsage = new HashMap<>();
            ArrayList<String> keysList = keyboardTab.getCol(0);
            for (String key : keysList) {
                keyUsage.put(key, 0);
            }
            for (String cell : langPackGoogleSheet.getTabFromName("wordlist").getCol(1)) {
                for (String letter : cell.split("")) {
                    if (!keyUsage.containsKey(letter)) {
                        warnings.add("In wordList, the word \"" + cell + "\" contains the letter \"" + letter +
                                "\" which is not in the keyboard.");
                    } else {
                        keyUsage.put(letter, keyUsage.get(letter) + 1);
                    }
                }
            }
            for (Map.Entry<String, Integer> entry : keyUsage.entrySet()) {
                if (entry.getValue() < 6) {
                    warnings.add("In wordList.txt, the letter \"" + entry.getKey() + "\" is only used" +
                            " in " + entry.getValue() + " words. It is recommended that each letter be" +
                            " used in at least 6 words.");
                }
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        //todo is checking headers still a necessary check for gameTiles or others?.. aslo non initial letters black box error?
        try {
            // the below section compares the tiles in gameTiles to the words in wordlist
            Map<String, Integer> tileUsage = new HashMap<>();
            int longWords = 0;
            tileUsage.put(".", 0);
            tileUsage.put("'", 0);
            for (String tile : langPackGoogleSheet.getTabFromName("gametiles").getCol(0)) {
                tileUsage.put(tile, 0);
            }
            for (String word : langPackGoogleSheet.getTabFromName("wordlist").getCol(1)) {
                // the below numTilesInWord method parses a word into the tiles in the tileUsage dictionary,
                // returning the number of tiles in the provided word.
                // it also updates the tileUsage dictionary so the dictionary counts how many times each tile appears
                int tileCounter = numTilesInWord(word, tileUsage);
                if (tileCounter == 0) {
                    warnings.add("no combination of tiles can be put together to create \"" + word + "\" in wordlist");
                }

                if (tileCounter >= 10) {
                    if (tileCounter > 15) {
                        fatalErrors.add("the word \"" + word + "\" in wordlist takes more than 15 tiles to build");
                    } else {
                        longWords += 1;
                    }
                }
            }
            if (longWords > 0) {
                warnings.add("the wordlist has " + longWords + " long words (10 to 15 game tiles);" +
                        " shorter words are preferable in an early literacy game. Consider removing longer words ");
            }
            for (Map.Entry<String, Integer> tile : tileUsage.entrySet()) {
                if (tile.getValue() < 3) {
                    warnings.add("the tile \"" + tile.getKey() + "\" in gametiles only appears in words " + tile.getValue()
                            + " times. It is recommended that each letter be used in at least 3 words");
                }
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }
        try {
            //todo.. the correct uppercase value?
            Tab gameTiles = langPackGoogleSheet.getTabFromName("gametiles");
            // below helper methods adds warnings for any duplicates in the provided column
            gameTiles.checkColForDuplicates(0);
            // the below portion makes sure each tile and their alternates are all unique
            for (ArrayList<String> row : gameTiles) {
                List<String> alternates = row.subList(0, 4);
                if (new HashSet<>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in gametiles has the same tile appearing in multiple places");
                }
            }
            // this check makes sure that colum 4 of gameTiles only has valid type specifiers
            ArrayList<String> gameTileTypes = gameTiles.getCol(4);
            HashSet<String> validTypes = new HashSet<>(Set.of("C","V","X", "AD", "AV", "BV", "FV", "LV", "T", "SAD"));
            for (int i = 0; i < gameTileTypes.size(); i++) {
                if (!validTypes.contains(gameTileTypes.get(i))) {
                    warnings.add("row " + i + 1 + " of gametiles does not specify a valid type in the types column. Valid" +
                            " types are " + validTypes);
                }
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }
        try {
            langPackGoogleSheet.getTabFromName("wordlist").checkColForDuplicates(0);
            langPackGoogleSheet.getTabFromName("wordlist").checkColForDuplicates(1);
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }
        try {
            langPackGoogleSheet.getTabFromName("settings").checkColForDuplicates(0);
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void validateResourceSubfolders(){
        //todo add secondary image functionality
        for (Map.Entry<String, String> nameToMimeType : this.DESIRED_FILETYPE_FROM_SUBFOLDERS.entrySet()){
            try {
                DriveFolder subFolder = langPackDriveFolder.getFolderFromName(nameToMimeType.getKey());
                subFolder.filterByMimeType(nameToMimeType.getValue());
            } catch (ValidatorException e) {
                fatalErrors.add(e.getMessage());
            }
        }
        // in the validateResourceSubfolders() methods these booleans are set to true if it is determined
        // that the app is trying to use these features
        boolean hasInstructionAudio = decideIfAudioAttempted("games", 4, "audio_instructions_optional");
        if (!hasInstructionAudio){
            DESIRED_FILETYPE_FROM_SUBFOLDERS.remove("audio_instructions_optional");
        }
        boolean hasSyllableAudio = decideIfAudioAttempted("syllables", 4, "audio_syllables_optional");
        if (!hasSyllableAudio){
            DESIRED_FILETYPE_FROM_SUBFOLDERS.remove("audio_syllables_optional");
        }
        boolean hasTileAudio = decideIfAudioAttempted("gametiles", 5, "audio_tiles_optional");
        if (!hasTileAudio){
            DESIRED_FILETYPE_FROM_SUBFOLDERS.remove("audio_tiles_optional");
        }

        try {
            DriveFolder resourceImages = langPackDriveFolder.getFolderFromName("images_resources_optional");
            ArrayList<String> resourceImageNames = langPackGoogleSheet.getTabFromName("resources").getCol(1);
            resourceImages.checkFileNameAgainstList(resourceImageNames);
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            DriveFolder wordImages = langPackDriveFolder.getFolderFromName("images_words");
            ArrayList<String> wordImageNames = langPackGoogleSheet.getTabFromName("wordlist").getCol(0);
            wordImages.checkFileNameAgainstList(wordImageNames);
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            DriveFolder wordAudio = langPackDriveFolder.getFolderFromName("audio_words");
            ArrayList<String> wordsInLWC = langPackGoogleSheet.getTabFromName("wordlist").getCol(0);
            wordAudio.checkFileNameAgainstList(wordsInLWC);
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            if (hasTileAudio) {
                DriveFolder tileAudio = langPackDriveFolder.getFolderFromName("audio_tiles_optional");
                ArrayList<String> tiles = langPackGoogleSheet.getTabFromName("gametiles").getCol(5);
                tileAudio.checkFileNameAgainstList(tiles);
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            if (hasSyllableAudio) {
                DriveFolder syllableAudio = langPackDriveFolder.getFolderFromName("audio_syllables_optional");
                ArrayList<String> syllables = langPackGoogleSheet.getTabFromName("syllables").getCol(4);
                syllableAudio.checkFileNameAgainstList(syllables);
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            if (hasInstructionAudio) {
                DriveFolder instructionAudio = langPackDriveFolder.getFolderFromName("audio_instructions_optional");
                ArrayList<String> gamesList = langPackGoogleSheet.getTabFromName("games").getCol(4);
                gamesList.removeAll(Collections.singleton("X"));
                instructionAudio.checkFileNameAgainstList(gamesList);
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }
    }

    private void validateSyllables(){

        try {
            Tab syllables = langPackGoogleSheet.getTabFromName("syllables");
            // below makes sure the first column in the syllables tab doesn't have duplicates
            syllables.checkColForDuplicates(0);
            // below makes sure each syllable and its alterantes are all unique
            for (ArrayList<String> row : syllables) {
                List<String> alternates = row.subList(0, 4);
                if (new HashSet<>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in syllables has the same tile appearing in multiple places");
                }
            }
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

        try {
            HashSet<String> providedSyllables = new HashSet<>(langPackGoogleSheet.getTabFromName("syllables").getCol(0));
            HashSet<String> parsedSyllables = new HashSet<>();
            for (String word : langPackGoogleSheet.getTabFromName("wordlist").getCol(1)) {
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
        } catch (ValidatorException e) {
            warnings.add(GENERIC_WARNING);
        }

    }

    public Set<String> getFatalErrors() {
        return this.fatalErrors;
    }

    public Set<String> getWarnings() {
        return this.warnings;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeValidatedFiles() throws IOException, ValidatorException {

        // top portion writes the desired data from tabs into the raw folder
        String pathToLangPack = "src/" + langPackGoogleSheet.getName();
        deleteDirectory(new java.io.File(pathToLangPack));

        String userDir = System.getProperty("user.dir");
        String pathToStudioProjects = userDir.substring(0, userDir.indexOf("/AlphaTiles/app"));
        String pathToTemplate = pathToStudioProjects + "/PublicLanguageAssets/templateTemplate";
        java.io.File templateFile = new java.io.File(pathToTemplate);

        Files.createDirectory(Paths.get(pathToLangPack));
        copyDirectory(templateFile, pathToLangPack);

        String pathToRaw = pathToLangPack + "/res/raw";
        for (String desiredTabName : DESIRED_DATA_FROM_TABS.keySet()){
            Tab desiredTab = langPackGoogleSheet.getTabFromName(desiredTabName);
            FileWriter writer = new FileWriter(pathToRaw + "/aa_" + desiredTab.getName() + ".txt");
            writer.write(desiredTab.toString());
            writer.close();

        }

        for (Map.Entry<String, String> subfolderSpecs : DESIRED_FILETYPE_FROM_SUBFOLDERS.entrySet()) {

            String subFolderName = subfolderSpecs.getKey();
            String subFolderFileTypes = subfolderSpecs.getValue();

            try {
                DriveFolder wordImagesFolder = langPackDriveFolder.getFolderFromName(subFolderName);
                ArrayList<GoogleDriveItem> folderContents = wordImagesFolder.getFolderContents();

                String outputFolderPath = pathToLangPack + "/res/raw/";
                if (subFolderFileTypes.contains("image")){
                    outputFolderPath = pathToLangPack + "/res/drawable/";
                }

                for (GoogleDriveItem driveResource : folderContents) {
                    String pathForResource = outputFolderPath + driveResource.getName();
                    java.io.File downloadedResource = new java.io.File(pathForResource);
                    OutputStream out = new FileOutputStream(downloadedResource);
                    driveService.files().get(driveResource.getId()).executeMediaAndDownloadTo(out);
                }
            } catch (ValidatorException e) {
                warnings.add(GENERIC_WARNING);
            }
        }



//        boolean reachedProductFlavors = false;
//        boolean passedProductFlavors = false;
//        String appName = "";
//        try{
//            appName = langPackGoogleSheet.getTabFromName("langinfo").get(7).get(1);
//        }
//        catch (Exception e){
//            throw new ValidatorException("can't find the game name in langinfo");
//        }
//        String sectionToRewrite =
//                "        productFlavors {\n" +
//                "        // Assigns this product flavor to the \"version\" flavor dimension.\n" +
//                "        // If you are using only one dimension, this property is optional,\n" +
//                "        // and the plugin automatically assigns all the module's flavors to\n" +
//                "        // that dimension.\n" +
//                "\n" +
//                "        //Alpha Tiles internal team developers can find active product flavor definitions here:\n" +
//                "        // https://docs.google.com/document/d/1a3satcmHFa5r6l7THrKLgxSWVCs-Mp2yOGyzk4oETsk/edit\n" +
//                "\n" +
//                "        " + langPackName + " {\n" +
//                "            dimension \"language\"\n" +
//                "            applicationIdSuffix \".blue." + langPackName+ "\"\n" +
//                "            resValue \"string\", \"app_name\", '" + appName + "'\n" +
//                "        }\n" +
//                "    }\n";
//        StringBuilder newBuildGradle = new StringBuilder();
//
//
//        String pathToBuildGradle = System.getProperty("user.dir") + "/build.gradle";
//        BufferedReader readBuildGradle = new BufferedReader(new FileReader(pathToBuildGradle));
//        String line;
//        while ((line = readBuildGradle.readLine()) != null) {
//            if (line.contains("productFlavors {")) {
//                reachedProductFlavors = true;
//                newBuildGradle.append(sectionToRewrite);
//            }
//            else if (reachedProductFlavors && line.contains("namespace 'org.alphatilesapps.alphatiles'")){
//                passedProductFlavors = true;
//            }
//            if (!reachedProductFlavors || passedProductFlavors){
//                newBuildGradle.append(line).append("\n");
//            }
//        }
//        readBuildGradle.close();
//
//        BufferedWriter writeBuildGradle = new BufferedWriter(new FileWriter(pathToBuildGradle));
//        writeBuildGradle.write(newBuildGradle.toString());
//        writeBuildGradle.close();
    }

    public class GoogleDriveItem{
        private String mimeType;
        private final String id;
        private String name;
        protected GoogleDriveItem(String inID, String inName, String inMimeType){
            this.id = inID;
            this.name = inName;
            this.mimeType = inMimeType;
        }

        protected String getName() {
            return this.name;
        }

        protected String getMimeType(){
            return this.mimeType;
        }

        protected String getId() {
            return this.id;
        }
    }

    private class DriveFolder extends GoogleDriveItem{

        private final ArrayList<GoogleDriveItem> folderContents = new ArrayList<>();

        protected DriveFolder(String driveFolderId) throws IOException {
            this(driveFolderId, (String) driveService.files().get(driveFolderId).get("name"));
        }
        protected DriveFolder(String driveFolderId, String inName) throws IOException {
            super(driveFolderId, inName, "application/vnd.google-apps.folder");
            String pageToken = null;
            do {
                FileList result = driveService.files().list()
                        .setQ("parents in '" + driveFolderId + "' and trashed = false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .setPageToken(pageToken)
                        .execute();

                for (File file : result.getFiles()){

                    if (file.getMimeType().equals("application/vnd.google-apps.spreadsheet")) {
                        folderContents.add(new GoogleSheet(file.getId(), file.getName()));
                    }
                    else if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                        folderContents.add(new DriveFolder(file.getId(), file.getName()));
                    }
                    else {
                        folderContents.add(new GoogleDriveItem(file.getId(), file.getName(), file.getMimeType()));
                    }
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }

        protected DriveFolder getFolderFromName(String inName) throws ValidatorException{
            for (DriveFolder item : this.getContentsOfType(DriveFolder.class)){
                if (item.getName().equals(inName)){
                    return item;
                }
            }
            throw new ValidatorException("was not able to find " + inName + " in the drive folder " + this.getName());
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        protected void checkFileNameAgainstList(ArrayList<String> toMatch){
            toMatch.sort((first, second) -> Integer.compare(second.length(),
                    first.length()));

            for (GoogleDriveItem item : new ArrayList<>(folderContents)) {
                boolean hasMatchingName = false;
                for (String candidate : new ArrayList<>(toMatch)) {
                    if (item.getName().startsWith(candidate)) {
                        toMatch.remove(candidate);
                        hasMatchingName = true;
                        break;
                    }
                }
                if (!hasMatchingName) {
                    warnings.add("the file " + item.getName() + " in " + this.getName() + " may be excess " +
                            "as the start of the filename does not appear to match to anything");
                    folderContents.remove(item);
                }
            }
            for (String shouldHaveMatched : toMatch){
                warnings.add(shouldHaveMatched + "does not have a corresponding file in " + this.getName() +
                        " of the correct file type");
            }
            //TODO should I have services.json?
        }
        protected ArrayList<GoogleDriveItem> getFolderContents(){
            return this.folderContents;
        }
        //TODO I struggled with this method for a while and ended up just using something from
        // stack overflow but I should think about it more
        protected <type extends GoogleDriveItem> ArrayList<type> getContentsOfType(Class<type> myClass) {
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
                    warnings.add(item.getName() + " will be ignored in " + this.getName() +
                            " as it was not of type " + mimeType);
                }
            }
        }

        protected GoogleSheet getOnlyGoogleSheet() throws ValidatorException{
            ArrayList<GoogleSheet> allSheets = getContentsOfType(GoogleSheet.class);
            if (allSheets.size() == 0) {
                throw new ValidatorException("No google sheet found in specified google drive folder");
            } else if (allSheets.size() > 1) {
                throw new ValidatorException("More than one google sheet found in specified google drive folder");
            } else {
                return allSheets.get(0);
            }
        }

        protected int size(){
            return folderContents.size();
        }


    }

    private class GoogleSheet extends GoogleDriveItem {
        private final ArrayList<Tab> tabList = new ArrayList<>();

        protected GoogleSheet(String spreadSheetId, String inName) throws IOException {
            super(spreadSheetId, inName, "application/vnd.google-apps.spreadsheet");
            Spreadsheet thisSpreadsheet = sheetsService.spreadsheets().get(spreadSheetId).execute();
            List<Sheet> sheetList = thisSpreadsheet.getSheets();
            for (Sheet sheet : sheetList){
                tabList.add(new Tab(spreadSheetId, sheet.getProperties().getTitle())) ;

            }
        }

        public Tab getTabFromName(String name) throws ValidatorException {
            for (Tab tab : tabList) {
                if (tab.getName().equals(name)) {
                    return tab;
                }
            }
            throw new ValidatorException("The " + name + " tab does not exist");
        }
    }
    private class Tab extends ArrayList<ArrayList<String>> {
        private final String name;
        private Integer rowLen;
        private Integer colLen;
        private final String spreadSheetId;

        protected Tab(String inSpreadsheetId, String inName) {
            super();
            this.name = inName;
            this.spreadSheetId = inSpreadsheetId;

            try {
                ValueRange response = sheetsService.spreadsheets().values()
                        .get(spreadSheetId, name + "!A1:Z")
                        .setValueRenderOption("UNFORMATTED_VALUE")
                        .execute();
                for (List row : response.getValues()) {
                    ArrayList<String> newRow = new ArrayList<>();
                    for (Object cell : row) {
                        // to allow a single white space cell
                        if (cell.toString().matches("\\u0020+")){
                            newRow.add(" ");
                        }
                        else{
                            newRow.add(cell.toString().strip());
                        }
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
                    fatalErrors.add("the tab " + this.name + " does not have enough rows. It should " +
                            "have " + this.colLen);
                }

                if (this.size() > this.colLen) {
                    this.subList(this.colLen, this.size()).clear();
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


        public ArrayList<String> getCol(int colNum) throws ValidatorException {
            try {
                ArrayList<String> toReturn = new ArrayList<>();
                for (ArrayList<String> row : this) {
                    toReturn.add(row.get(colNum));
                }
                toReturn.remove(0);
                return toReturn;
            }
            catch (IndexOutOfBoundsException e){
                fatalErrors.add(("the tab " + this.getName() + " is completely empty"));
                throw new ValidatorException("the tab " + this.getName() + " is completely empty");
            }
        }

        private void checkColForDuplicates(int colNum) throws ValidatorException {
            Set<String> colSet = new HashSet<>();
            for (String cell : this.getCol(colNum)) {
                if (!colSet.add(cell)) {
                    fatalErrors.add(cell + " appears more than once in column " + colNum + 1 +  " of " + this.getName());
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

        @NonNull
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

    private boolean decideIfAudioAttempted(String tabName, int colNum, String subFolderName) {

        boolean someAudioFiles = false;
        boolean someAudioNames = false;

        try {
            DriveFolder subFolder = langPackDriveFolder.getFolderFromName(subFolderName);
            if (subFolder.getName().equals(subFolderName) && subFolder.size() > 0) {
                someAudioFiles = true;
            }
        } catch (ValidatorException e) {
        }

        try {
            ArrayList<String> AudioNames = langPackGoogleSheet.getTabFromName(tabName).getCol(colNum);
            AudioNames.removeAll(Collections.singleton("X"));
            if (AudioNames.size() > 0) {
                someAudioNames = true;
            }
        } catch (ValidatorException e){
        }

        if (someAudioNames && someAudioFiles){
            return true;
        } else if (someAudioNames) {
            warnings.add("you list names of audio files in the column " + colNum + " of  the tab " + tabName
                    + " (ie you have text in the column that is not 'X') but the folder" + subFolderName + " is empty"
                    + " please add matching audio files to the folder" + subFolderName + " if you want to use this feature");
        } else if (someAudioFiles) {
            warnings.add("you have audio files in the folder " + subFolderName + " but column"
                    + " of the tabv" + tabName + " doesn't list any audio file names"
                    + " please add matching audio file names to  the tabv" + tabName + " if you want to use this feature");
        }
        return false;
    }

    private boolean decideIfSyllablesAttempted(){

        boolean numerousWordsSpliced = false;
        boolean syllTabNotEmpty = false;

        try {
            int wordsSpliced = 0;
            for (String word : langPackGoogleSheet.getTabFromName("wordlist").getCol(1)) {
                if (word.contains(".")) {
                    wordsSpliced += 1;
                }
            }
            if (wordsSpliced > 6) {
                numerousWordsSpliced = true;
            }
        }
        catch (ValidatorException e){
        }

        try {
            if (langPackGoogleSheet.getTabFromName("syllables").size() > 0) {
                syllTabNotEmpty = true;
            }
        }catch (ValidatorException e){
        }

        if (syllTabNotEmpty && numerousWordsSpliced) {
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

    // TODO clean this up a bit lol
    private int numTilesInWord(String toParse, Map<String, Integer> tileUsage) {
        Set<String> tileSet = tileUsage.keySet();
        for (int i = toParse.length(); i > 0; i--) {
            String longestPossibleTile = toParse.substring(0, i);
            if (tileSet.contains(longestPossibleTile)) {
                if (i == toParse.length()){
                    return 1;
                }
                else{
                    int numTilesIfThisOne = numTilesInWord(toParse.substring(i), tileUsage);
                    if (numTilesIfThisOne > 0){
                        tileUsage.put(longestPossibleTile, tileUsage.get(longestPossibleTile) + 1);
                        return  numTilesIfThisOne + 1;
                    }
                }
            }
        }
        return 0;
    }

    private void buildServices(String driveFolderId) throws GeneralSecurityException, IOException {

        // intially builds the drive and sheets service not knowing if the token is revoked/expired
        String APPLICATION_NAME = "Alpha Tiles Validator";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        sheetsService =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        driveService =
                new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        // tries to use the drive service to check if the token is revoked or expired
        try {
            driveService.files().get(driveFolderId).execute();
        }
        // if the token is revoked or expired, deletes the token and then rebuilds the services
        catch (TokenResponseException | GoogleJsonResponseException e){
            String errorDescription;
            if (e instanceof TokenResponseException){
                errorDescription = ((TokenResponseException) e).getDetails().get("error_description").toString();
            }
            else{
                errorDescription = ((GoogleJsonResponseException) e).getDetails().get("error_description").toString();
            }

            if (errorDescription.equals("Token has been expired or revoked.")) {
                deleteDirectory(new java.io.File("tokens"));
                sheetsService =
                        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
                driveService =
                        new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
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


        String credentialsJson =
                "{\"installed\":{\"client_id\":\"384994053794-tuci4d2mhf4caems7jalfmb4voi855b8.apps." +
                        "googleusercontent.com\",\"project_id\":\"enhanced-medium-387818\",\"auth_uri\":" +
                        "\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":" +
                        "\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":" +
                        "\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":" +
                        "\"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\",\"redirect_uris\":[\"http://localhost\"]}}";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        //Global instance of the scopes required by this quickstart.
        //If modifying these scopes, delete your previously saved tokens/ folder.

        List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_READONLY);
        //String CREDENTIALS_FILE_PATH = "/credentials.json";
        // Load client secrets.
        InputStream in = new ByteArrayInputStream(credentialsJson.getBytes());
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private void deleteDirectory(java.io.File directoryToBeDeleted) {
        java.io.File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (java.io.File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void copyDirectory(java.io.File directoryToBeCopied, String destinationPath) throws IOException {
        java.io.File[] allContents = directoryToBeCopied.listFiles();
        if (allContents != null) {
            for (java.io.File subContent : allContents) {
                Path oldAvatarPath = Paths.get(subContent.getPath());
                Path newAvatarPath = Paths.get(destinationPath + "/" + subContent.getName());
                Files.copy(oldAvatarPath, newAvatarPath);
                if (subContent.isDirectory()) {
                    copyDirectory(subContent, destinationPath + "/" + subContent.getName());
                }
            }
        }
    }

    public static class ValidatorException extends Exception {
        public ValidatorException(String errorMessage) {
            super(errorMessage);
        }
    }

}
