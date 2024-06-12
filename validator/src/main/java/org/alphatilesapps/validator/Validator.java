package org.alphatilesapps.validator;

import static javax.swing.JOptionPane.YES_NO_OPTION;

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
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Validator {

    //<editor-fold desc="Customizations for app builders and main method">
    /**
     * For app builders to customize. Validator will add notify if is a key is used less than this
     * number of times.
     */
    private static final int NUM_TIMES_KEYS_WANTED_IN_WORDS = 5;

    /**
     * For app builders to customize. Validator will add notify if is a key is used less than this
     * number of times.
     */
    private static final int NUM_TIMES_TILES_WANTED_IN_WORDS = 5;

    /**
     * List of Tile objects from gametiles
     */
    public static TileList tileList;

    /**
     * List of Word objects from wordlist
     */
    public static ArrayList<Word> wordList;

    /**
     * List of Key objects from keyboard
     */
    public static ArrayList<Key> keyList;

    /**
     * List of color strings from the colors tab
     */
    public static ArrayList<String> colorList;

    /**
     * Hashmap of Tile texts to Tile objects from tileList
     */
    public static TileHashMap tileHashMap;

    /**
     * Smaller lists of tiles based on type
     */
    public static TileList SILENT_PRELIMINARY_TILES = new TileList();
    public static TileList SILENT_PLACEHOLDER_CONSONANTS = new TileList();
    public static ArrayList<String> MULTITYPE_TILES = new ArrayList<>();


    /**
     * Could be Roman, Thai, Lao, or Khmer. Read from langinfo
     */
    public static String scriptType;
    /**
     * Could be "◌", "×", etc. Read from langinfo
     */
    public static String placeholderCharacter;


    /**
     * main method for running the validator. Prompts user for the URL of the google drive folder.
     * Constructs a Validator object using the URL. Calls the validate method.  Prints out
     * a list fatal errors, warnings, project notes and recommendations. Prompts the to decide whether to download
     * the language pack into android studio, and if desired, calls the writeValidatedFiles method.
     */
    public static void main(String[] args) throws ValidatorException, GeneralSecurityException, IOException {

        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setUndecorated(true);
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);

        try {

            Path confirmedPath;
            Path userDir = Paths.get(System.getProperty("user.dir")).getParent();
            try {
                confirmedPath = Paths.get(Files.readString(userDir.resolve("pathForValidator.txt"), StandardCharsets.UTF_8));
            } catch (Exception ignored) {
                JPanel panel2 = new JPanel();
                panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
                JTextField path = new JTextField(userDir.toString());
                String message2 = "Please check that this is the correct path to the AlphaTiles folder";
                panel2.add(new JLabel(message2));
                panel2.add(path);
                JCheckBox remember = new JCheckBox("Remember this path");
                panel2.add(remember);
                JOptionPane.showConfirmDialog(jf, panel2, "AlphaTiles", JOptionPane.DEFAULT_OPTION);
                confirmedPath = Paths.get(path.getText());
                if (remember.isSelected()) {
                    Files.writeString(userDir.resolve("pathForValidator.txt"), confirmedPath.toString(), StandardCharsets.UTF_8);
                }
            }
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JTextField urlInput = new JTextField();
            String message = "Enter the URL for the Google Drive folder of your " +
                    "language pack";
            panel.add(new JLabel(message));
            panel.add(urlInput);
            // Might need to pass this to the validator itself once more checks are added, see Checks class at the bottom of the file
            Checks checks = new Checks(panel);
            int ignored = JOptionPane.showConfirmDialog(jf, panel, "AlphaTiles", JOptionPane.DEFAULT_OPTION);
            String url = urlInput.getText();
            if (!url.isEmpty()) {
                Validator myValidator = new Validator(url, confirmedPath);
                myValidator.validate();
                System.out.println("\n\nList of Fatal Errors\n********");
                int n = 0;
                for (String error : myValidator.getFatalErrors()) {
                    n++;
                    System.out.println(n + ". " + error);
                }
                n = 0;
                System.out.println("\nList of Warnings\n********");
                for (String warning : myValidator.getWarnings()) {
                    n++;
                    System.out.println(n + ". " + warning);
                }
                n = 0;
                System.out.println("\nProject Notes\n********");
                for (String note : myValidator.getNotes()) {
                    n++;
                    System.out.println(n + ". " + note);
                }
                n = 0;
                if (checks.showRecommendations) {
                    System.out.println("\nList of Recommendations\n********");
                    for (String recommendation : myValidator.getRecommendations()) {
                        n++;
                        System.out.println(n + ". " + recommendation);
                    }
                }

                jf.setVisible(true);
                int wantsToDownload = JOptionPane.showOptionDialog(jf, "After reviewing errors and warnings, " +
                                "are you ready to download the data from this language pack into android studio", "AlphaTiles",
                        YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

                if (wantsToDownload == 0) {
                    jf.setVisible(true);
                    int isSure = JOptionPane.showOptionDialog(jf,
                            "Are you sure? This will replace any existing language pack of the same name in android studio",
                            "AlphaTiles", YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

                    if (isSure == 0) {
                        myValidator.writeValidatedFiles(confirmedPath.resolve("app"));
                    }
                }
            } else {
                System.out.println("\n> Validator closed. No Drive folder url was entered.");
            }

        } finally {
            jf.dispose();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Validator fields">

    private final Path rootPath;
    /**
     * A LinkedHashSet of fatal errors found by the validator (is Set to avoid duplicate messages). Printed by main.
     */
    private final Set<String> fatalErrors = new LinkedHashSet<>();

    /**
     * A LinkedHashSet of warnings found by the validator (is Set to avoid duplicate messages). Printed by main.
     */
    private final Set<String> warnings = new LinkedHashSet<>();

    /**
     * A LinkedHashSet of project notes found by the validator (is Set to avoid duplicate messages). Printed by main.
     */
    private final Set<String> project_notes = new LinkedHashSet<>();

    /**
     * A LinkedHashSet of recommendations found by the validator (is Set to avoid duplicate messages). Printed by main.
     */
    private final Set<String> recommendations = new LinkedHashSet<>();

    /**
     * A com.google.api.services.drive.Drive object used to populate the Validator's google drive data structures.
     * Avoid using driveService directly, use the validator custom objects and methods instead.
     */
    private Drive driveService;

    /**
     * A GoogleDriveFolder object representing the entire language pack folder. Contains all the files and folders in the
     * language pack. They are accessible through methods provided in the GoogleDriveFolder class
     */
    private final GoogleDriveFolder langPackDriveFolder;

    /**
     * A com.google.api.services.sheets.v4.Sheets object used to populate the Validator's google drive data structures.
     * Avoid using driveService directly, use the validator custom objects and methods instead.
     */
    private Sheets sheetsService;

    /**
     * A GoogleSheet object representing the entire language pack spreadsheet. Contains all the tabs and cells in the
     * language pack. They are accessible through methods provided in the GoogleSheet class.
     */
    private final GoogleSheet langPackGoogleSheet;

    /**
     * A Map of the names of the tabs needed for validation to the ranges of cells needed from each tab
     * (in A1 notation). The validator automatically checks for these tabs in the langPackGoogleSheet,
     * checks for empty cells in these ranges, checks for enough rows (if applicable) and
     * shrinks down the tabs to only contain the ranges specified in this map. Any tabs specified here are automatically
     * added to the raw folder as aa_tabName.txt if the language pack is downloaded.
     */
    private static final HashMap<String, String> DESIRED_RANGE_FROM_TABS = new HashMap<>(Map.ofEntries(
            Map.entry("langinfo", "A1:B15"),
            Map.entry("gametiles", "A1:Q"),
            Map.entry("wordlist", "A1:F"),
            Map.entry("keyboard", "A1:B"),
            Map.entry("games", "A1:H"),
            Map.entry("syllables", "A1:G"),
            Map.entry("resources", "A1:C"),
            Map.entry("settings", "A1:B"),
            Map.entry("colors", "A1:C"),
            Map.entry("names", "A1:B"),
            Map.entry("notes", "A1:B")));

    /**
     * A Map of the names of the folders needed for validation to the file types needed in each folder (in MIME type, comma delimited).
     * The validator automatically checks for these folders in the langPackDriveFolder,
     * and checks that each file in the folder is of the correct type (warning and removing if not).
     * Any folder specified here are will automatically have its contents added to drawable if it is an image
     * and raw otherwise if the language pack is downloaded to android studio.
     */
    private static final HashMap<String, String> DESIRED_FILETYPE_FROM_SUBFOLDERS = new HashMap<>(Map.of(
            "images_words", "image/",
            "audio_words", "audio/mpeg",
            "images_resources_optional", "image/",
            "images_words_low_res", "image/",
            "audio_tiles_optional", "audio/mpeg",
            "audio_instructions_optional", "audio/mpeg",
            "audio_syllables_optional", "audio/mpeg",
            "font", "application/x-font-ttf, font/ttf, text/xml"
    ));

    /**
     * A String used as a prefix to the warning given when catching a ValidatorException. Use command F to make sure
     * your use is consistent with previous catches of the same ValidatorException.
     */
    private final String FAILED_CHECK_WARNING = "one or more checks was not able to be run because of " +
            "unresolved fatal errors in the checking of ";
    //</editor-fold>

    //<editor-fold desc="Validator constructor and getters">

    /**
     * A constructor for a Validator object, creates the langPackDriveFolder field from the URL and searches for one
     * (and only one) google sheet in the folder to set as the langPackGoogleSheet field.
     *
     * @param driveFolderUrl a String representing the URL of the language pack folder in google drive
     */
    public Validator(String driveFolderUrl, Path rootPath) throws IOException, GeneralSecurityException, ValidatorException {
        this.rootPath = rootPath;
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        String driveFolderId = driveFolderUrl.substring(driveFolderUrl.indexOf("folders/") + 8);
        buildServices(driveFolderId);
        this.langPackDriveFolder = new GoogleDriveFolder(driveFolderId);
        if (langPackDriveFolder.size() == 0) {
            throw new ValidatorException("Cannot find find any files in a folder with the given URL");
        }
        this.langPackGoogleSheet = langPackDriveFolder.getOnlyGoogleSheet();
    }

    public Set<String> getFatalErrors() {
        return this.fatalErrors;
    }

    public Set<String> getWarnings() {
        return this.warnings;
    }

    public Set<String> getNotes() {
        return this.project_notes;
    }

    public Set<String> getRecommendations() {
        return this.recommendations;
    }
    //</editor-fold>

    //<editor-fold desc="validation methods">

    /**
     * Executes all validation, delegating to validateGoogleSheet, validateSyllables (
     * if it appears syllables are attempted) and validateResourceSubfolders.
     * Populates fatalErrors, warnings, project notes and recommendations.
     */
    public void validate() {

        this.validateGoogleSheet();

        //runs syllable checks only if 6 or more words contain periods (for syllable splicing)
        boolean usesSyllables = decideIfSyllablesAttempted();
        if (usesSyllables) {
            this.validateSyllablesTab();
        }

        this.validateResourceSubfolders();

    }

    /**
     * Executes checks langPackGoogleSheet, including default checks based on DESIRED_RANGE_FROM_TABS.
     * Checks are wrapped in try catch blocks so that if one check fails, the rest of the checks can still be run.
     * Populates fatalErrors, warnings, project notes and recommendations.
     */
    private void validateGoogleSheet() {

        System.out.println();
        System.out.println();
        System.out.println("This report is for " + langPackGoogleSheet.getName() + ".");

        // this first step is looks at the desired data from tabs field set at the top of the
        // code, searches for a tab that has the matching name, and shrinks the internal representation
        // of those tabs to only be what is specified
        try {
            String appName = langPackGoogleSheet
                    .getTabFromName("langinfo")
                    .getRowFromFirstCell("Game Name (In Local Lang)")
                    .get(1)
                    .replace("'", "ꞌ");
            if (appName.length() > 30) {
                warnings.add("App name '" + appName + "' is too long, should be less than 30 characters to be compatible with Play Store limits.");
            }
        } catch (Exception ignored) {
        }
        for (Map.Entry<String, String> nameAndRange : DESIRED_RANGE_FROM_TABS.entrySet()) {
            try {
                Tab desiredTab = langPackGoogleSheet.getTabFromName(nameAndRange.getKey());
                desiredTab.sizeTabUsingRange(nameAndRange.getValue());
            } catch (ValidatorException e) {
                fatalErrors.add("the tab " + nameAndRange.getKey() + " does not appear in the language pack Google sheet.");
            }
        }

        // Make a temp folder containing the tabs as UTF-8 encoded .txt files while they are being checked
        Path pathToValidator = rootPath.resolve("validator");
        Path pathToTempFolder = pathToValidator.resolve("temp");
        for (String desiredTabName : DESIRED_RANGE_FROM_TABS.keySet()) {
            try {
                Tab desiredTab = langPackGoogleSheet.getTabFromName(desiredTabName);
                Files.createDirectories(pathToTempFolder);
                java.io.File tempFile = pathToTempFolder.resolve(desiredTab.getName() + ".txt").toFile();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
                writer.write(desiredTab.toString());
                writer.close();
            } catch (ValidatorException | IOException e) {
                fatalErrors.add("FAILED TO DOWNLOAD data from tab \"" + desiredTabName + "\"");
            }
        }

        // Put data from these text files into lists for checking
        try {
            buildWordList();
        } catch (IOException e) {
            fatalErrors.add("FAILED TO DOWNLOAD OR READ \"wordlist\"");
        }
        try {
            buildColorList();
        } catch (IOException e) {
            fatalErrors.add("FAILED TO DOWNLOAD OR READ \"colors\"");
        }
        try {
            buildKeyList();
        } catch (IOException e) {
            fatalErrors.add("FAILED TO DOWNLOAD OR READ \"keyboard\"");
        }
        try {
            buildTileList();
            for (int t = 0; t < tileList.size(); t++) {
                Tile thisTile = tileList.get(t);
                if (!thisTile.tileTypeB.equals("none")) {
                    MULTITYPE_TILES.add(thisTile.text);
                }
            }
        } catch (IOException e) {
            fatalErrors.add("FAILED TO DOWNLOAD OR READ \"gametiles\"");
        }


        //Each try catch block is essentially one check. Each check uses a catch block so that
        // that check will simply be passed over if a helper method throws a ValidatorException
        //(ie the helper method is unable to do what it is supposed to in a meaningful
        // way because of an existing fatal error (like missing a tab in the google sheet)).

        try {
            for (String cell : langPackGoogleSheet.getTabFromName("wordlist").getCol(0)) {
                if (!cell.matches("[a-z0-9_]+")) {
                    fatalErrors.add("In the first column of wordList, the word \"" + cell + "\" contains non-alphanumeric " +
                            "characters. " + "Please remove them. (The only allowed characters are a-z, 0-9, and _)");
                }
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the wordlist tab");
        }

        try {
            // Make sure all colors exist
            ArrayList<String> available = colorList;
            for (Key key : keyList) {
                if (!available.contains(key.color)) {
                    fatalErrors.add("Keyboard uses color id " + key.color + ", which is not defined in the colors tab");
                }
            }

            // Compare the keys in keyboard to the words in wordlist
            Map<String, Integer> keyUsage = getKeyUsage();

            float numWordsWithSpaces = 0;
            for (Word word : wordList) {
                if (word.wordInLOP.contains(" ")) {
                    numWordsWithSpaces++;
                }

                String LOPwordString = word.wordInLOP.replace(".", "");
                for (int i = 0; i < LOPwordString.length(); i++) {
                    if (!(keyUsage.containsKey(String.valueOf(LOPwordString.charAt(i))))) { // Flag chars that aren't in the keyboard

                        boolean charIsPartOfLongerKeyString = false;
                        for (String keyString : keyUsage.keySet()) {
                            if (keyString.contains(String.valueOf(LOPwordString.charAt(i)))) {
                                charIsPartOfLongerKeyString = true;
                                break;
                            }
                        }
                        if (!charIsPartOfLongerKeyString) {
                            String unicodeString = "(Unicode) " + (int) LOPwordString.charAt(i);
                            fatalErrors.add("In wordList, the word \"" + LOPwordString + "\" contains the character \"" + LOPwordString.charAt(i) +
                                    "\" which is not in the keyboard. " + unicodeString);
                        }
                    }
                }
            }
            if (numWordsWithSpaces / wordList.size() < 0.05f && numWordsWithSpaces != 0) {
                recommendations.add("Less than 5% of words contain spaces; consider removing those with spaces");
            }

            for (Map.Entry<String, Integer> entry : keyUsage.entrySet()) {
                if (entry.getValue() < NUM_TIMES_KEYS_WANTED_IN_WORDS) {
                    String unicodeString = "";
                    String key = entry.getKey();
                    if (!key.isEmpty()) {
                        unicodeString = " (Unicode " + (int) key.charAt(0) + ")";
                    }
                    recommendations.add("In wordList.txt, the key \"" + entry.getKey() + "\"" + unicodeString +
                            " is only used in " + entry.getValue() + " words. It is recommended that each key be" +
                            " used in at least " + NUM_TIMES_KEYS_WANTED_IN_WORDS + " times");
                }
            }
        } catch (NullPointerException e) {
            warnings.add(FAILED_CHECK_WARNING + "the keyboard tab");
        }
        // China game requires words with three tiles or four tiles
        int fourTileWords = 0;
        int threeTileWords = 0;
        try {
            // Compare the tiles in gameTiles to the words in wordlist
            Map<String, Integer> tileUsage = new HashMap<>();
            int longWords = 0;

            for (Tile tile : tileList) {
                tileUsage.put(tile.text, 0);
            }
            for (Word word : wordList) {
                // The tileUsage dictionary counts how many times each tile occurs in the wordlist

                try {
                    Tab langInfo = langPackGoogleSheet.getTabFromName("langinfo");
                    scriptType = langInfo.getRowFromFirstCell("Script type").get(1); // sets global variable used to determine simple/complex parse
                } catch (ValidatorException e) {
                    fatalErrors.add("In langinfo \"Script type\" must be either \"Arabic,\" \"Devanagari,\" \"Khmer,\" \"Lao,\" \"Roman,\"or \"Thai\". Please add a valid script type.");
                }
                try {
                    Tab settings = langPackGoogleSheet.getTabFromName("settings");
                    placeholderCharacter = settings.getRowFromFirstCell("Stand-in base for combining tiles").get(1); // sets global variable for complex parses
                    boolean placeholderCharacterFoundInGametiles = false;
                    for (Tile tile : tileList) {
                        if (tile.text.contains(placeholderCharacter)) {
                            placeholderCharacterFoundInGametiles = true;
                        }
                    }
                    if (!placeholderCharacterFoundInGametiles) {
                        fatalErrors.add("The stand-in base for combining characters in \"Settings\" is " + placeholderCharacter + " but that character is not in any of the gametiles. "
                                + "Please add the placeholder character you are using to the settings tab.");
                    }
                } catch (ValidatorException e) {
                    if (!(scriptType == null)) {
                        if (scriptType.matches("(Thai|Lao)")) {
                            warnings.add("Since the script type is Thai or Lao, you have the option to specify the \"Stand-in base for combining tiles\" in settings. By default, it will be \"◌\".");
                        }
                    }
                }
                ArrayList<Tile> tilesInWord;
                try {
                    tilesInWord = tileList.parseWordIntoTilesPreliminary(word); // Complex tiles are broken into pieces for the China game
                    int numTiles = tilesInWord.size();
                    if (numTiles == 3) {
                        threeTileWords += 1;
                    } else if (numTiles == 4) {
                        fourTileWords += 1;
                    }
                    if (numTiles >= 9) {
                        if (numTiles > 15) {
                            fatalErrors.add("the word \"" + word.wordInLOP + "\" in wordlist takes more than 15 tiles to build");
                        } else {
                            longWords += 1;
                        }
                    }
                }
                // go to the next word if this one cannot be parsed
                catch (NullPointerException e) {
                    fatalErrors.add("Cannot parse word \"" + word.wordInLOP + "\" in wordlist into tiles from gametiles.");
                }

            }
            if (longWords > 0) {
                recommendations.add("the wordlist has " + longWords + " long words (10 to 15 game tiles);" +
                        " shorter words are preferable in an early literacy game. Consider removing longer words ");
            }
            for (Word word : wordList) {
                ArrayList<Tile> tilesInWord = tileList.parseWordIntoTiles(word);
                for (Tile tile : tileList) {
                    for (Tile tileInWord : tilesInWord) {
                        if (tileInWord == null) {
                            ArrayList<String> tileStringsInWord = new ArrayList<String>();
                            for (Tile t : tilesInWord) {
                                if (!(t == null)) {
                                    tileStringsInWord.add(t.text);
                                }
                            }
                            fatalErrors.add("The word " + word.wordInLOP + " could not be parsed. The tiles parsed were " + tileStringsInWord);
                            break;
                        }
                        if (tileInWord.text.equals(tile.text)) {
                            tileUsage.put(tile.text, tileUsage.get(tile.text) + 1);
                        }
                    }
                }
            }
            for (Map.Entry<String, Integer> tile : tileUsage.entrySet()) {
                if (tile.getValue() < NUM_TIMES_TILES_WANTED_IN_WORDS) {
                    recommendations.add("the tile \"" + tile.getKey() + "\" in gametiles only appears in words " + tile.getValue()
                            + " times. It is recommended that each tile be used in at least " + NUM_TIMES_TILES_WANTED_IN_WORDS + " times");
                }
            }

        } catch (NullPointerException e) {
            warnings.add(FAILED_CHECK_WARNING + "the gametiles tab, the wordlist tab, or the langinfo setting \"Script type\"");
        }
        try {
            Tab gameTiles = langPackGoogleSheet.getTabFromName("gametiles");
            // add warnings for any duplicates in the provided column
            gameTiles.checkColForDuplicates(0);
            // make sure each tile and their alternates are all unique
            for (ArrayList<String> row : gameTiles) {
                List<String> alternates = row.subList(0, 4);
                if (new HashSet<>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in gametiles has the same tile appearing in multiple places");
                }
            }

            // make sure that the distractors are subsets of the language pack's game tiles
            List<String> validGameTiles = gameTiles.getCol(0);
            List<String> firstAlternates = gameTiles.getCol(1);
            List<String> secondAlternates = gameTiles.getCol(2);
            List<String> thirdAlternates = gameTiles.getCol(3);
            for (int i = 0; i < validGameTiles.size(); i++) {
                if (!validGameTiles.contains(firstAlternates.get(i))) {
                    fatalErrors.add("row " + (i + 2) + " of gametiles contains an invalid tile as an alternate: " + firstAlternates.get(i)
                            + ". Please add this alternate to the tile list if it is missing or replace it with a valid tile from the list.");
                }
                if (!validGameTiles.contains(secondAlternates.get(i))) {
                    fatalErrors.add("row " + (i + 2) + " of gametiles contains an invalid tile as an alternate: " + secondAlternates.get(i)
                            + ". Please add this alternate to the tile list if it is missing or replace it with a valid tile from the list.");
                }
                if (!validGameTiles.contains(thirdAlternates.get(i))) {
                    fatalErrors.add("row " + (i + 2) + " of gametiles contains an invalid tile as an alternate: " + thirdAlternates.get(i)
                            + ". Please add this alternate to the tile list if it is missing or replace it with a valid tile from the list.");
                }
            }

            // make sure that colum 4 of gameTiles only has valid type specifiers
            ArrayList<String> gameTileTypes = gameTiles.getCol(4);
            HashSet<String> validTypes = new HashSet<>(Set.of("C", "PC", "V", "X", "D", "AD", "AV", "BV", "FV", "LV", "T", "SAD"));
            for (int i = 0; i < gameTileTypes.size(); i++) {
                if (!validTypes.contains(gameTileTypes.get(i))) {
                    fatalErrors.add("row " + (i + 2) + " of gametiles does not specify a valid type in the types column. Valid" +
                            " types are " + validTypes);
                }
            }

            // check if the uppercase tiles are all full upper case or proper case and warn accordingly

            ArrayList<String> multiPossibleUpperCase = gameTiles.getCol(6);
            for (int i = multiPossibleUpperCase.size() - 1; i >= 0; i--) {
                int numPossibleUpperCase = 0;
                for (char c : multiPossibleUpperCase.get(i).toCharArray()) {
                    if (Character.toUpperCase(c) != Character.toLowerCase(c)) {
                        numPossibleUpperCase++;
                    }
                }
                if (numPossibleUpperCase <= 1) {
                    multiPossibleUpperCase.remove(i);
                }
            }

            ArrayList<String> properCaseOnly = new ArrayList<>();
            ArrayList<String> fullUpperCaseOnly = new ArrayList<>();
            ArrayList<String> other = new ArrayList<>();
            for (String upperCaseTile : multiPossibleUpperCase) {
                String fullUpper = upperCaseTile.toUpperCase();
                //building proper case string may not work if tile starts with char that cannot be uppercase
                String properCase = upperCaseTile.toLowerCase();
                String firstChar = properCase.substring(0, 1);
                properCase = properCase.replaceFirst(firstChar, firstChar.toUpperCase());

                if (upperCaseTile.equals(fullUpper)) {
                    fullUpperCaseOnly.add(upperCaseTile);
                } else if (upperCaseTile.equals(properCase)) {
                    properCaseOnly.add(upperCaseTile);
                } else {
                    other.add(upperCaseTile);
                }
            }
            if (Math.max(properCaseOnly.size(), fullUpperCaseOnly.size()) != multiPossibleUpperCase.size()) {

                int numExamplesFullUpper = Math.min(fullUpperCaseOnly.size(), 5);
                int numExamplesProper = Math.min(properCaseOnly.size(), 5);
                int numExamplesOther = Math.min(other.size(), 5);
                warnings.add("The column Upper in the gametiles tab doesn't appear to consistently stick with proper case " +
                        "(the first key being upper case) or full upper case (the whole tile is upper case) " +
                        "\n\tExamples of tiles that seem to use full uppercase are " + fullUpperCaseOnly.subList(0, numExamplesFullUpper) +
                        "\n\tExamples of tiles that seem to use proper case are " + properCaseOnly.subList(0, numExamplesProper) +
                        "\n\tExamples of tiles that appear to be neither are " + other.subList(0, numExamplesOther));
            }

            if (!fullUpperCaseOnly.isEmpty()) {
                warnings.add("You use full upper case in the Upper column in the gametiles tab. This may lead to unintended" +
                        " formatting. For example if you had a tile \"ch\" with the uppercase value \"CH\", users could" +
                        " see the word CHildren");
            }

        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the gametiles tab");
        }
        try {
            langPackGoogleSheet.getTabFromName("wordlist").checkColForDuplicates(0);
            langPackGoogleSheet.getTabFromName("wordlist").checkColForDuplicates(1);
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the wordlist tab");
        }
        try {
            langPackGoogleSheet.getTabFromName("settings").checkColForDuplicates(0);
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the settings tab");
        }
        try {
            Tab langInfo = langPackGoogleSheet.getTabFromName("langinfo");
            if (!langInfo.getRowFromFirstCell("Script direction (LTR or RTL)").get(1).matches("(LTR|RTL)")) {
                fatalErrors.add("In langinfo \"script direction\" must be either \"LTR\" or \"RTL\"");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the langinfo tab");
        }
        try {
            Tab langInfo = langPackGoogleSheet.getTabFromName("langinfo");
            if (!langInfo.getRowFromFirstCell("Script type").get(1).matches("(Arabic|Devanagari|Khmer|Lao|Roman|Thai|)")) {
                fatalErrors.add("In langinfo \"Script type\" must be either \"Arabic,\" \"Devanagari,\" \"Khmer,\" \"Lao,\" \"Roman,\"or \"Thai\"");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the langinfo tab");
        }
        try {
            Tab notesTab = langPackGoogleSheet.getTabFromName("notes");
            ArrayList<String> notesCol = notesTab.getCol(1);
            project_notes.addAll(notesCol);
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the notes tab");
        }
        try {
            Tab settings = langPackGoogleSheet.getTabFromName("settings");
            if (!settings.getRowFromFirstCell("Has tile audio").get(1).matches("(TRUE|FALSE)")) {
                fatalErrors.add("In settings \"Has tile audio\" must be either \"TRUE\" or \"FALSE\"");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the settings tab");
        }
        try {
            Tab settings = langPackGoogleSheet.getTabFromName("settings");
            if (!settings.getRowFromFirstCell("Has syllable audio").get(1).matches("(TRUE|FALSE)")) {
                fatalErrors.add("In settings \"Has syllable audio\" must be either \"TRUE\" or \"FALSE\"");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the settings tab");
        }
        try {
            Tab settings = langPackGoogleSheet.getTabFromName("settings");
            if (!settings.getRowFromFirstCell("Differentiates types of multitype symbols").get(1).matches("(TRUE|FALSE)")) {
                fatalErrors.add("In settings \"Differentiates types of multitype symbols\" must be either \"TRUE\" or \"FALSE\"");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the settings tab");
        }
        try {
            Tab settings = langPackGoogleSheet.getTabFromName("settings");
            if (!settings.getRowFromFirstCell("First letter stage correspondence").get(1).matches("(TRUE|FALSE)")) {
                fatalErrors.add("In settings \"First letter stage correspondence\" must be either \"TRUE\" or \"FALSE\")");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the settings tab");
        }
        try {
            Tab wordlist = langPackGoogleSheet.getTabFromName("wordlist");
            ArrayList<String> gamesList = langPackGoogleSheet.getTabFromName("games").getCol(1);
            if (!gamesList.contains("Italy")) {
                recommendations.add("It is recommended that you include the Italy game");
            } else if (wordlist.size() < 55) {
                fatalErrors.add("the Italy game requires at least 54 words, you only provide " + wordlist.size());
            }

            if (gamesList.size() < 7) {
                recommendations.add("it is recommended that you have more than 6 games");
            }
            if (wordlist.size() < 21) {
                recommendations.add("it is recommended that you have 20 or more words");
            }
            if (!gamesList.contains("China")) {
                recommendations.add("it is recommended that you include the China game");
            }
            if ((fourTileWords < 3 || threeTileWords < 1) && gamesList.contains("China")) {
                fatalErrors.add("the China game requires at least 3 four tile words and 1 three tile word, you only " +
                        "provide " + fourTileWords + " four tile words and " + threeTileWords + " three tile words");
            }
            HashSet<String> mexicoLevels = new HashSet<>();
            for (ArrayList<String> row : langPackGoogleSheet.getTabFromName("games")) {
                if (row.get(1).equals("Mexico")) {
                    mexicoLevels.add(row.get(2));
                }
            }
            if (mexicoLevels.size() < 5) {
                warnings.add("It is recommended that you have the game Mexico with 5 levels");
            }
            HashSet<String> myanmarLevels = new HashSet<>();
            for (ArrayList<String> row : langPackGoogleSheet.getTabFromName("games")) {
                if (row.get(1).equals("Myanmar")) {
                    myanmarLevels.add(row.get(2));
                }
            }
            if (myanmarLevels.size() < 3) {
                warnings.add("It is recommended you have the game Myanmar with 3 levels");
            }
            for (ArrayList<String> row : langPackGoogleSheet.getTabFromName("games")) {
                if ((row.get(1).equals("Sudan") || row.get(1).equals("Romania")) && !row.get(3).equals("5")) {
                    warnings.add("Games like Romania and Sudan (no right or wrong answers) should use " +
                            "code color 5 (yellow). Check game door " + row.get(0) + " in the games tab");
                }
            }
            int i = 0;
            for (String row : langPackGoogleSheet.getTabFromName("games").getCol(0)) {
                i++;
                try {
                    int n = Integer.parseInt(row);
                    if (n != i) {
                        fatalErrors.add("Cell in row " + (i + 1) + ", column A of the games tab must be " + i + ", was " + row);
                    }
                } catch (NumberFormatException e) {
                    fatalErrors.add("Cell in row " + (i + 1) + ", column A of the games tab must be " + i + ", was " + row);
                }
            }

        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the wordlist tab or the games tab");
        }
        try {
            for (Word word : wordList) {
                try {
                    parseTypeSpecification(word);
                } catch (ValidatorException ignored) {
                }
            }
        } catch (NullPointerException e) {
            warnings.add(FAILED_CHECK_WARNING + "the gametiles tab, the wordlist tab, or the langinfo setting \"Script type\"");
        }

        // Delete temp files used for securing the right encodings before testing
        try {
            Files.walk(pathToTempFolder).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);  // delete each temp file
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Map<String, Integer> getKeyUsage() {
        Map<String, Integer> keyUsage = new HashMap<>(); // A map of each key to how many times it is used in the wordlist
        for (Key key : keyList) {
            keyUsage.put(key.text, 0);
        }
        for (Key key : keyList) {
            for (Word word : wordList) { // Find how many words contain each key
                String LOPwordString = word.wordInLOP.replace(".", "");
                if (LOPwordString.contains(key.text)) {
                    keyUsage.put(key.text, keyUsage.get(key.text) + 1);
                }
            }
        }
        return keyUsage;
    }

    Set<String> SPECIAL_AUDIO_INSTRUCTIONS = Set.of(
            "zzz_earth.mp3",
            "zzz_about.mp3",
            "zzz_choose_player.mp3",
            "zzz_resources.mp3",
            "zzz_set_player_name.mp3"
    );

    /**
     * Executes checks on the resource folders langPackGoogleDrive.
     * Includes default checks based on DESIRED_FILETYPE_FROM_SUBFOLDERS.
     * Checks are wrapped in try catch blocks so that if one check fails, the rest of the checks can still be run.
     * Populates fatalErrors, warnings, project notes and recommendations.
     */
    private void validateResourceSubfolders() {

        for (Map.Entry<String, String> nameToMimeType : DESIRED_FILETYPE_FROM_SUBFOLDERS.entrySet()) {
            try {
                GoogleDriveFolder subFolder = langPackDriveFolder.getFolderFromName(nameToMimeType.getKey());
                subFolder.filterByMimeTypes(nameToMimeType.getValue().split(","));
                for (GoogleDriveItem itemInFolder : langPackDriveFolder.getFolderFromName(nameToMimeType.getKey()).folderContents) {
                    // make sure the file names use valid
                    if (!itemInFolder.getName().matches("[a-z0-9_]+\\.+[a-z0-9_]+")) {
                        fatalErrors.add("In " + nameToMimeType.getKey() + ", the file \"" + itemInFolder.getName() +
                                "\" must be in the format name.type, where both name and type only use characters" +
                                " a-z, 0-9, and _");
                    }
                }
            } catch (ValidatorException e) {
                fatalErrors.add(e.getMessage());
            }
        }
        // in the validateResourceSubfolders() methods these booleans are set to true if it is determined
        // that the the given column lists file names (anything other than X or naWhileMPOnly)
        // and the referenced drive folder contains files
        decideIfFontAttempted();
        boolean hasInstructionAudio = decideIfAudioAttempted("games", 4, "audio_instructions_optional");
        //tile and syllable audio have the extra step of checking against settings to see if the checks should be run
        boolean syllableAudioAttempted = decideIfAudioAttempted("syllables", 4, "audio_syllables_optional");
        boolean syllableAudioSetting = false;
        try {
            if (langPackGoogleSheet.getTabFromName("settings").getRowFromFirstCell("Has syllable audio").get(1).equals("TRUE")) {
                syllableAudioSetting = true;
            }
        } catch (Exception ignored) {
        }
        boolean hasSyllableAudio = syllableAudioAttempted && syllableAudioSetting;
        if (!hasSyllableAudio) {
            if (syllableAudioAttempted) {
                warnings.add("Although it appears you set up your language pack for syllable audio, the \"Has syllable audio\" " +
                        "row in the settings tab is not set to \"TRUE\"");
            }
            if (syllableAudioSetting) {
                warnings.add("Although you entered \"TRUE\" for \"has syllable audio\" in the settings tab, " +
                        "column E of syllables and/or folder \"audio_syllables_optional\" are empty");
            }
        }

        boolean tileAudioAttempted = decideIfAudioAttempted("gametiles", 5, "audio_tiles_optional");
        boolean tileAudioSetting = false;
        try {
            if (langPackGoogleSheet.getTabFromName("settings").getRowFromFirstCell("Has tile audio").get(1).equals("TRUE")) {
                tileAudioSetting = true;
            }
        } catch (Exception e) {
            warnings.add(FAILED_CHECK_WARNING + "the settings tab");
        }

        boolean hasTileAudio = tileAudioSetting && tileAudioAttempted;
        if (!hasTileAudio) {
            if (tileAudioAttempted) {
                warnings.add("Although it appears you set up your language pack for tile audio, \"has tile audio\" " +
                        " in the settings tab is not set to \"TRUE\"");
            }
            if (tileAudioSetting) {
                warnings.add("Although you entered \"TRUE\" for \"has tile audio\" in the settings tab, " +
                        "column F of gameTiles and/or folder \"audio_tiles_optional\" are empty");
            }
        }

        try {
            GoogleDriveFolder resourceImages = langPackDriveFolder.getFolderFromName("images_resources_optional");
            ArrayList<String> resourceImageNames = langPackGoogleSheet.getTabFromName("resources").getCol(2);
            resourceImages.checkItemNamesAgainstList(resourceImageNames);
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the images_resources_optional folder or the resources tab");
        }

        try {
            GoogleDriveFolder wordImages = langPackDriveFolder.getFolderFromName("images_words");
            ArrayList<String> wordsInLWC = langPackGoogleSheet.getTabFromName("wordlist").getCol(0);
            wordImages.checkItemNamesAgainstList(wordsInLWC);
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the images_words folder or the wordlist tab");
        }
        try {
            GoogleDriveFolder lowResWordImages = langPackDriveFolder.getFolderFromName("images_words_low_res");
            // if lowResWordImages is not empty, we assume the user is trying to provide their own low res images
            //otherwise we will generate these in writeImageAndAudioFiles
            if (lowResWordImages.size() > 0) {
                ArrayList<String> TwoAppendedWordsInLWC = langPackGoogleSheet.getTabFromName("wordlist").getCol(0);
                TwoAppendedWordsInLWC.replaceAll(s -> s + "2");
                lowResWordImages.checkItemNamesAgainstList(TwoAppendedWordsInLWC);
            } else {
                warnings.add("Since the folder images_words_low_res is empty, the validator will automatically generate " +
                        "smaller versions of all images if asked to download language data from google drive.");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the images_words_low_res folder or the wordlist tab");
        }

        try {
            GoogleDriveFolder wordAudio = langPackDriveFolder.getFolderFromName("audio_words");
            ArrayList<String> wordsInLWC = langPackGoogleSheet.getTabFromName("wordlist").getCol(0);
            wordAudio.checkItemNamesAgainstList(wordsInLWC);
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the audio_words folder or the wordlist tab");
        }

        try {
            if (hasTileAudio) {
                GoogleDriveFolder tileAudio = langPackDriveFolder.getFolderFromName("audio_tiles_optional");
                ArrayList<String> tiles = langPackGoogleSheet.getTabFromName("gametiles").getCol(5);
                ArrayList<String> tilesType2 = langPackGoogleSheet.getTabFromName("gametiles").getCol(8);
                for (String tileB : tilesType2) {
                    if (!tileB.equals("X")) {
                        tiles.add(tileB);
                    }
                }
                ArrayList<String> tilesType3 = langPackGoogleSheet.getTabFromName("gametiles").getCol(10);
                for (String tileC : tilesType3) {
                    if (!tileC.equals("X")) {
                        tiles.add(tileC);
                    }
                }
                tileAudio.checkItemNamesAgainstList(tiles);
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the audio_tiles_optional folder or the gametiles tab");
        }
        try {
            Tab gamesTab = langPackGoogleSheet.getTabFromName("games");
            boolean hasSudanForTiles = false;
            for (int i = 0; i < gamesTab.size(); i++) {
                if (gamesTab.get(i).get(1).equals("Sudan") && gamesTab.get(i).get(6).equals("T")) {
                    hasSudanForTiles = true;
                    break;
                }
            }

            if (hasTileAudio && !hasSudanForTiles) {
                recommendations.add("It is recommended you add Sudan for tiles to the games tab if you have tile audio");
            } else if (!hasTileAudio && hasSudanForTiles) {
                fatalErrors.add("You cannot have Sudan for tiles in the games tab if you do not have tile audio");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the games tab");
        }
        try {
            if (hasSyllableAudio) {
                GoogleDriveFolder syllableAudio = langPackDriveFolder.getFolderFromName("audio_syllables_optional");
                ArrayList<String> syllables = langPackGoogleSheet.getTabFromName("syllables").getCol(4);
                syllableAudio.checkItemNamesAgainstList(syllables);
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the audio_syllables_optional folder or the syllables tab");
        }
        try {
            Tab gamesTab = langPackGoogleSheet.getTabFromName("games");
            boolean hasSudanForSyllables = false;
            for (int i = 1; i < gamesTab.size(); i++) {
                if (gamesTab.get(i).get(1).equals("Sudan") && gamesTab.get(i).get(6).equals("S")) {
                    hasSudanForSyllables = true;
                    break;
                }
            }

            if (hasSyllableAudio && !hasSudanForSyllables) {
                recommendations.add("It is recommended you add Sudan for syllables to the games tab if you have syllable audio");
            } else if (!hasSyllableAudio && hasSudanForSyllables) {
                fatalErrors.add("You cannot have Sudan for syllables in the games tab if you do not have syllable audio");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the games tab");
        }

        try {
            if (hasInstructionAudio) {
                GoogleDriveFolder instructionAudio = langPackDriveFolder.getFolderFromName("audio_instructions_optional");
                ArrayList<String> instructions = langPackGoogleSheet.getTabFromName("games").getCol(4);
                ArrayList<String> names = langPackGoogleSheet.getTabFromName("games").getCol(1);

                for (int idx = 0; idx < names.size(); ) {
                    String instruction = instructions.get(idx);
                    if (instruction.equals("X") || instruction.equals("naWhileMPOnly")) {
                        instructions.remove(idx);
                        names.remove(idx);
                    } else {
                        idx++;
                    }
                }
                instructionAudio.checkItemNamesAgainstList(instructions, true, SPECIAL_AUDIO_INSTRUCTIONS);
                HashMap<String, String> map = new HashMap<>();
                for (int idx = 0; idx < names.size(); idx++) {
                    if (!map.containsKey(instructions.get(idx))) {
                        map.put(instructions.get(idx), names.get(idx));
                    } else if (!map.get(instructions.get(idx)).equals(names.get(idx))) {
                        warnings.add("Instruction audio " + instructions.get(idx) + " is used in more than one game.");
                    }
                }
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the audio_instructions_optional folder or the games tab");
        }
    }

    /**
     * Executes checks on the syllable tab in langPackGoogleSheet.
     * Checks are wrapped in try catch blocks so that if one check fails, the rest of the checks can still be run.
     * Populates fatalErrors, warnings, project notes and recommendations.
     */
    private void validateSyllablesTab() {

        try {
            Tab syllables = langPackGoogleSheet.getTabFromName("syllables");
            // make sure the first column in the syllables tab doesn't have duplicates
            syllables.checkColForDuplicates(0);
            // make sure each syllable and its alternates are all unique
            for (ArrayList<String> row : syllables) {
                List<String> alternates = row.subList(0, 4);
                if (new HashSet<>(alternates).size() < alternates.size()) {
                    warnings.add("the row " + row + " in syllables has the same cell appearing in multiple places");
                }
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the syllables tab");
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
                fatalErrors.add("Syllable " + notInParsed + " is never used in a word in wordlist");
            }
            for (String notInProvided : parsedSyllables) {
                fatalErrors.add("Syllable " + notInProvided + " is used in wordlist but not in the syllables tab");
            }
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the syllables tab or the wordlist tab");
        }

    }
    //</editor-fold>

    //<editor-fold desc="writing-app-resources methods">

    /**
     * Writes an android language pack to be used in the AlphaTiles app, including adjustments to build.gradle.
     * Bases language pack template on local device in PublicLanguageAssets repo that should be sister to AlphaTiles.
     * Delegates to writeNewBuildGradle, writeRawTxtFiles,and writeImageAndAudioFiles.
     * Also copies the google_services.json file from the old language pack if it exists.
     *
     * @param pathToApp a Path object that leads to the app folder in the AlphaTiles repo
     */
    public void writeValidatedFiles(Path pathToApp) throws IOException, ValidatorException {

        String langPackNameNoSpaces = langPackGoogleSheet.getName().replaceAll("\\s+", "");
        Path pathToLangPack = pathToApp.resolve("src").resolve(langPackNameNoSpaces);

        //checks for a google_services.json file and copies it to a temporary location before deleting
        //old language pack
        Path pathToServices = pathToLangPack.resolve("google-services.json");
        Path pathToTempServices = Paths.get(String.valueOf(pathToApp.resolve("src")), "TEMP_google_services.json");
        if (Files.exists(pathToServices)) {
            Files.copy(pathToServices, pathToTempServices);
        }
        deleteDirectory(pathToLangPack);
        Files.createDirectories(pathToLangPack);

        // copies contents of templateTemplate into fresh language pack directory
        Path pathToValidator = rootPath.resolve("validator");
        Path pathToTemplate = Paths.get(String.valueOf(pathToValidator.resolve("templateTemplate")));
        copyDirectory(pathToTemplate, pathToLangPack);

        // If a temporary services.json file was created, moves it into the new language pack.
        if (Files.exists(pathToTempServices)) {
            Files.move(pathToTempServices, pathToLangPack.resolve("google-services.json"));
        }

        writeNewBuildGradle(pathToApp);
        writeRawTxtFiles(pathToLangPack);
        writeImageAndAudioFiles(pathToLangPack);


    }

    /**
     * Copies the contents of the old build.gradle file into three StringBuilders,
     * one for the beginning of the file up to the first language pack, one for the language packs (commented out)
     * except for the one being overwritten, and one for the rest of the file. Writes a new build.gradle with the
     * beginning, the new language pack, the commented out old language packs and the end of the build.gradle file.
     *
     * @param pathToApp a Path object that leads to the app folder in the AlphaTiles repo
     */
    private void writeNewBuildGradle(Path pathToApp) throws IOException, ValidatorException {

        String appName;
        String langPackName = langPackGoogleSheet.getName().replaceAll("\\s+", "");
        try {
            appName = langPackGoogleSheet.getTabFromName("langinfo").getRowFromFirstCell("Game Name (In Local Lang)").get(1);
            appName = appName.replace("'", "ꞌ");
        } catch (Exception e) {
            throw new ValidatorException("can't find the game name in langinfo");
        }
        String newLangPack =
                "        " + langPackName + " {\n" +
                        "            dimension \"language\"\n" +
                        "            applicationIdSuffix \".blue." + langPackName + "\"\n" +
                        "            resValue \"string\", \"app_name\", '" + appName + "'\n" +
                        "        }\n";

        StringBuilder beforeLangPacks = new StringBuilder();
        StringBuilder otherLangPacks = new StringBuilder();
        StringBuilder afterLangPacks = new StringBuilder();
        BufferedReader readBuildGradle = new BufferedReader(new FileReader(pathToApp.resolve("build.gradle").toFile(), StandardCharsets.UTF_8));

        boolean reachedProductFlavors = false;
        boolean reachedFirstLangPack = false;
        String line = "";
        while (!reachedFirstLangPack) {
            beforeLangPacks.append(line).append("\n");
            line = readBuildGradle.readLine();
            if (line.matches("\\s*productFlavors\\s*\\{.*")) {
                reachedProductFlavors = true;
            } else if (reachedProductFlavors && line.contains("{")) {
                reachedFirstLangPack = true;
            }
        }
        //delete the first \n
        beforeLangPacks.delete(0, 1);

        boolean onTargetLangPack = false;
        boolean finishedLangPacks = false;
        int bracketCounter = 1;
        while (!finishedLangPacks && line != null) {

            if (line.contains("{")) {
                bracketCounter += 1;
            } else if (line.contains("}")) {
                bracketCounter -= 1;
            }
            if (bracketCounter == 0) {
                finishedLangPacks = true;
            }

            if (line.matches("\\s*" + langPackName + "\\s*\\{.*")) {
                onTargetLangPack = true;
            } else if (onTargetLangPack && line.contains("}")) {
                onTargetLangPack = false;
            } else if (!onTargetLangPack) {
                otherLangPacks.append(line).append("\n");
            }
            line = readBuildGradle.readLine();
        }

        while (line != null) {
            afterLangPacks.append(line).append("\n");
            line = readBuildGradle.readLine();
        }
        //delete last \n
        if (afterLangPacks.length() > 0) {
            afterLangPacks.setLength(afterLangPacks.length() - 1);
        }

        readBuildGradle.close();

        BufferedWriter writeBuildGradle = new BufferedWriter(new FileWriter(pathToApp.resolve("build.gradle").toFile(), StandardCharsets.UTF_8));
        writeBuildGradle.write(beforeLangPacks + newLangPack + otherLangPacks + afterLangPacks);
        writeBuildGradle.close();
    }

    /**
     * Writes each tab in DESIRED_RANGE_FROM_TABS to a raw txt file in the downloaded language pack.
     *
     * @param pathToLangPack a Path object that leads to the app/src/name-of-langPack folder in the AlphaTiles repo
     */
    private void writeRawTxtFiles(Path pathToLangPack) throws IOException {
        System.out.println("\n\ndownloading language pack spreadsheet from google drive into language pack ... ");
        Path pathToRaw = pathToLangPack.resolve("res").resolve("raw");
        for (String desiredTabName : DESIRED_RANGE_FROM_TABS.keySet()) {
            try {
                Tab desiredTab = langPackGoogleSheet.getTabFromName(desiredTabName);
                Files.createDirectories(pathToRaw);
                java.io.File rawFile = pathToRaw.resolve("aa_" + desiredTab.getName() + ".txt").toFile();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(rawFile), StandardCharsets.UTF_8);
                writer.write(desiredTab.toString());
                writer.close();
            } catch (ValidatorException e) {
                System.out.println("FAILED TO DOWNLOAD data from tab \"" + desiredTabName + "\"");
            }
        }
        System.out.println("finished downloading language pack spreadsheet from google drive into language pack");
    }

    /**
     * Writes each folder in DESIRED_FILETYPE_FROM_SUBFOLDERS to the appropriate folder in the downloaded language pack.
     * (drawable for images and raw for everything else)
     *
     * @param pathToLangPack a Path object that leads to the app/src/name-of-langPack folder in the AlphaTiles repo
     */
    private void writeImageAndAudioFiles(Path pathToLangPack) throws IOException {
        boolean missingLowResImages = false;
        try {
            if (langPackDriveFolder.getFolderFromName("images_words_low_res").size() == 0) {
                missingLowResImages = true;
            }
        } catch (ValidatorException e) {
            missingLowResImages = true;
        }
        for (Map.Entry<String, String> subfolderSpecs : DESIRED_FILETYPE_FROM_SUBFOLDERS.entrySet()) {

            try {
                String subFolderName = subfolderSpecs.getKey();
                String subFolderFileTypes = subfolderSpecs.getValue();

                GoogleDriveFolder wordImagesFolder = langPackDriveFolder.getFolderFromName(subFolderName);
                ArrayList<GoogleDriveItem> folderContents = wordImagesFolder.getFolderContents();

                System.out.println("downloading " + subFolderName + " from google drive into language pack ... ");
                Path outputFolderPath = pathToLangPack.resolve("res").resolve("raw");
                if (subFolderFileTypes.contains("image")) {
                    outputFolderPath = pathToLangPack.resolve("res").resolve("drawable");
                    Files.createDirectories(outputFolderPath);
                }
                if (subFolderName.equals("font")) {
                    outputFolderPath = pathToLangPack.resolve("res").resolve("font");
                    Files.createDirectories(outputFolderPath);
                }

                for (GoogleDriveItem driveResource : folderContents) {
                    Path pathForResource = outputFolderPath.resolve(driveResource.getName());
                    java.io.File downloadedResource = pathForResource.toFile();
                    OutputStream out = new FileOutputStream(downloadedResource);
                    driveService.files().get(driveResource.getId()).executeMediaAndDownloadTo(out);

                    // if the user did not provide low res images, generate them and put them in drawable
                    if (missingLowResImages && subFolderFileTypes.contains("image")) {

                        BufferedImage needsLowRes = ImageIO.read(downloadedResource);
                        Path pathForLowRes = outputFolderPath.resolve(driveResource.getName().replace(".", "2."));

                        int currentWidth = needsLowRes.getWidth();
                        int currentHeight = needsLowRes.getHeight();
                        double scaleFactor = Math.min(524.0 / currentWidth, 524.0 / currentHeight);

                        String informalTypeName = driveResource.mimeType.substring(driveResource.mimeType.indexOf("/") + 1);

                        if (scaleFactor < 1 && scaleFactor > 0) {
                            Image lowResImage = needsLowRes.getScaledInstance((int) (currentWidth * scaleFactor),
                                    (int) (currentHeight * scaleFactor), Image.SCALE_DEFAULT);
                            BufferedImage lowResBuffered = new BufferedImage((int) (currentWidth * scaleFactor),
                                    (int) (currentHeight * scaleFactor), BufferedImage.TYPE_INT_ARGB);
                            lowResBuffered.createGraphics().drawImage(lowResImage, 0, 0, null);
                            boolean wroteSuccessfully = ImageIO.write(lowResBuffered, informalTypeName, pathForLowRes.toFile());

                            if (!wroteSuccessfully) {
                                // if downloading fails, try with a different buffered image type (works for JPEGs but
                                // not transparent png images
                                lowResBuffered = new BufferedImage((int) (currentWidth * scaleFactor),
                                        (int) (currentHeight * scaleFactor), BufferedImage.TYPE_INT_RGB);
                                lowResBuffered.createGraphics().drawImage(lowResImage, 0, 0, null);
                                wroteSuccessfully = ImageIO.write(lowResBuffered, informalTypeName, pathForLowRes.toFile());
                            }

                            if (!wroteSuccessfully) {
                                System.out.println("FAILED TO DOWNLOAD low res version of " + driveResource.getName());
                            }

                        } else {
                            ImageIO.write(needsLowRes, informalTypeName, pathForLowRes.toFile());
                        }
                    }
                }
                System.out.println("finished downloading " + subFolderName + " from google drive into language pack.");
            } catch (ValidatorException e) {
                System.out.println("FAILED TO DOWNLOAD " + subfolderSpecs.getKey() + " from google drive into language pack.");
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="GoogleDriveItem class structure">

    /**
     * Represents any item in google drive. Extended by GoogleDriveFolder and GoogleSheet,
     * and directly instantiated for files that are not folders or spreadsheets (example images and audio files).
     */
    private static class GoogleDriveItem {

        /**
         * the mimeType of any GoogleDriveItem instance.
         */
        private final String mimeType;

        /**
         * the google id of any GoogleDriveItem instance.
         * (used for driveService or sheetsService operations)
         */
        private final String id;

        /**
         * the file/folder/sheet name of any GoogleDriveItem instance.
         */
        private final String name;

        /**
         * Constructor for GoogleDriveItem.
         *
         * @param inID       a String that is the google id of the item
         * @param inName     a String that is the name of the item
         * @param inMimeType a String that is the mimeType of the item
         */
        protected GoogleDriveItem(String inID, String inName, String inMimeType) {
            this.id = inID;
            this.name = inName;
            this.mimeType = inMimeType;
        }

        protected String getName() {
            return this.name;
        }

        protected String getMimeType() {
            return this.mimeType;
        }

        protected String getId() {
            return this.id;
        }
    }

    /**
     * Represents a google drive folder, extended from GoogleDriveItem. On construction automatically
     * recursively populates the folderContents field with all the contents of the folder
     * (constructing GoogleDriveFolder, GoogleSheet, and GoogleDriveItem objects as appropriate).
     */
    private class GoogleDriveFolder extends GoogleDriveItem {

        /**
         * ArrayList of all the GoogleDriveFolder, GoogleSheet, and GoogleDriveItem objects in the folder.
         * Automatically populated on construction.
         */
        private final ArrayList<GoogleDriveItem> folderContents = new ArrayList<>();

        protected ArrayList<GoogleDriveItem> getFolderContents() {
            return this.folderContents;
        }

        /**
         * Constructor for GoogleDriveFolder. Recursively populates the folderContents field with all the contents of the folder
         * (constructing GoogleDriveFolder, GoogleSheet, and GoogleDriveItem objects as appropriate).
         *
         * @param driveFolderId a String that is the google id of this folder
         */
        protected GoogleDriveFolder(String driveFolderId) throws IOException {
            this(driveFolderId, driveService.files().get(driveFolderId).setFields("name").execute().getName());
        }

        /**
         * Constructor for GoogleDriveFolder. Recursively populates the folderContents field with all the contents of the folder
         * (constructing GoogleDriveFolder, GoogleSheet, and GoogleDriveItem objects as appropriate).
         *
         * @param driveFolderId a String that is the google id of this folder
         * @param inName        a String that is the name of this folder
         */
        protected GoogleDriveFolder(String driveFolderId, String inName) throws IOException {
            super(driveFolderId, inName, "application/vnd.google-apps.folder");
            String pageToken = null;
            do {
                FileList result = driveService.files().list()
                        .setQ("parents in '" + driveFolderId + "' and trashed = false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .setPageToken(pageToken)
                        .execute();

                for (File file : result.getFiles()) {

                    if (file.getMimeType().equals("application/vnd.google-apps.spreadsheet")) {
                        folderContents.add(new GoogleSheet(file.getId(), file.getName()));
                    } else if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                        folderContents.add(new GoogleDriveFolder(file.getId(), file.getName()));
                    } else {
                        folderContents.add(new GoogleDriveItem(file.getId(), file.getName(), file.getMimeType()));
                    }
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }

        /**
         * Searches for a GoogleDriveItem object in folderContents with the given name before the period (if there is a
         * period)
         *
         * @param inName a String that is the name of the item to search for
         * @return a Google Drive Item in this.folderContents who name has inName before the period (if there is a
         * period). Null otherwise.
         */
        protected GoogleDriveItem getItemWithName(String inName) {
            for (GoogleDriveItem item : this.folderContents) {
                String itemName = item.getName();
                int endIndex = (!itemName.contains(".") ? itemName.length() : itemName.indexOf("."));
                if (itemName.substring(0, endIndex).equals(inName)) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Searches for a GoogleDriveFolder object in the folderContents field with the given name.
         *
         * @param inName a String that is the name of the folder to search for
         * @return a GoogleDriveFolder object with the given name if found, throws exception otherwise
         * @throws ValidatorException if no GoogleDriveFolder object with the given name is found
         */
        protected GoogleDriveFolder getFolderFromName(String inName) throws ValidatorException {
            for (GoogleDriveFolder item : this.<GoogleDriveFolder>getAllOfMimeType("vnd.google-apps.folder")) {
                if (item.getName().equals(inName)) {
                    return item;
                }
            }
            throw new ValidatorException("was not able to find the " + inName + " folder in the drive folder \"" + this.getName() + "\"");
        }


        //todo this method can be made cleaner with the getItemFromName

        /**
         * Takes a list of names of the items that should be in the folder. Removes any items from folderContents
         * that do not match one of the names in the list (adding a warning as it does). Also adds a warning for any
         * name that does not match to an item.
         *
         * @param namesList an ArrayList of Strings which are the names to be compared against folderContents
         */
        protected void checkItemNamesAgainstList(ArrayList<String> namesList) {
            checkItemNamesAgainstList(namesList, false, Set.of());
        }

        protected void checkItemNamesAgainstList(ArrayList<String> namesList, boolean allowRepeats, Set<String> optional) {

            ArrayList<String> namesNotYetFound = new ArrayList<>(namesList);
            namesNotYetFound.addAll(optional);
            ArrayList<GoogleDriveItem> filesNotYetMatched = new ArrayList<>(this.folderContents);

            for (String name : namesList) {
                GoogleDriveItem itemWithName = this.getItemWithName(name);
                if (itemWithName != null) {
                    namesNotYetFound.remove(name);
                    filesNotYetMatched.remove(itemWithName);
                    if (namesNotYetFound.contains(name) && !allowRepeats) {
                        warnings.add("The file name " + name + " in " + this.getName() + " is asked for in multiple places ");
                    }
                }
            }

            for (String shouldHaveFound : namesNotYetFound) {
                if (optional.contains(shouldHaveFound)) continue;
                fatalErrors.add(shouldHaveFound + " does not have a corresponding file in " + this.getName() +
                        " of the correct file type");
            }

            for (GoogleDriveItem shouldHaveMatched : filesNotYetMatched) {
                if (optional.contains(shouldHaveMatched.name)) continue;
                warnings.add("the file " + shouldHaveMatched.getName() + " in " + this.getName() + " may be excess " +
                        "or duplicate and will be ignored");
                folderContents.remove(shouldHaveMatched);
            }
        }

        /**
         * returns a list of all items in the folderContents field that are of the given type.
         * WARNING the mimeType parameter must ensure items can be cast to the type parameter type
         *
         * @param mimeType a String that is the mimeType desired in the returned list
         * @param <type>   a generic type that items should be cast to in the returned list
         * @return an ArrayList of all items in the folderContents field that are of the given type
         */
        @SuppressWarnings("unchecked")
        protected <type extends GoogleDriveItem> ArrayList<type> getAllOfMimeType(String mimeType) {
            ArrayList<type> filteredList = new ArrayList<>();
            for (GoogleDriveItem item : folderContents) {
                if (item.getMimeType().contains(mimeType)) {
                    filteredList.add((type) item);
                }
            }
            return filteredList;
        }

        /**
         * Removes any items from the folderContents field that are not of the given mimeType, adding a warning every
         * time it does so.
         *
         * @param mimeTypes an array of Strings that are the mimeTypes to filter by
         */
        protected void filterByMimeTypes(String[] mimeTypes) {
            for (GoogleDriveItem item : new ArrayList<>(folderContents)) {
                boolean success = false;
                for (String mimeType : mimeTypes) {
                    success |= item.getMimeType().contains(mimeType.trim());
                }
                if (!success) {
                    folderContents.remove(item);
                    warnings.add(item.getName() + " will be ignored in " + this.getName() +
                            " as it was not of any of these types: " + Arrays.toString(mimeTypes) + ", it was of type " + item.getMimeType());
                }
            }
        }

        /**
         * Searches for one and only google sheet in folderContents, returns it if found, throws an exception if not
         * or if multiple found
         *
         * @return the one and only google sheet in folderContents
         */
        protected GoogleSheet getOnlyGoogleSheet() throws ValidatorException {
            ArrayList<GoogleSheet> allSheets = getAllOfMimeType("google-apps.spreadsheet");
            if (allSheets.isEmpty()) {
                throw new ValidatorException("No google sheet found in specified google drive folder");
            } else if (allSheets.size() > 1) {
                throw new ValidatorException("More than one google sheet found in specified google drive folder");
            } else {
                return allSheets.get(0);
            }
        }

        /**
         * returns the number of items in the folderContents field
         *
         * @return the number of items in the folderContents field
         */
        protected int size() {
            return folderContents.size();
        }


    }

    /**
     * Represents a google sheet, extended from GoogleDriveItem. On construction automatically
     * populates the tabList field with Tab objects representing all the tabs in the actual google sheet.
     */
    private class GoogleSheet extends GoogleDriveItem {

        /**
         * An ArrayList of Tab objects representing all the tabs in the actual google sheet.
         */
        private final ArrayList<Tab> tabList = new ArrayList<>();

        /**
         * Constructor for a google sheet object. Automatically populates the tabList field with Tab objects.
         *
         * @param spreadSheetId a String that is the id of the google sheet
         * @param inName        a String that is the name of the google sheet
         */
        protected GoogleSheet(String spreadSheetId, String inName) throws IOException {
            super(spreadSheetId, inName, "application/vnd.google-apps.spreadsheet");
            Spreadsheet thisSpreadsheet = sheetsService.spreadsheets().get(spreadSheetId).execute();
            List<Sheet> sheetList = thisSpreadsheet.getSheets();
            for (Sheet sheet : sheetList) {
                tabList.add(new Tab(spreadSheetId, sheet.getProperties().getTitle()));
            }
        }

        /**
         * searches for a Tab object in the tabList field with the given name. Returns it if found, throws a
         * ValidatorException if not.
         *
         * @param inName a String that is the name of the tab to be searched for
         * @return a Tab object in the tabList field with the given name
         * @throws ValidatorException if no Tab object in the tabList field has the given name
         */
        protected Tab getTabFromName(String inName) throws ValidatorException {
            for (Tab tab : tabList) {
                if (tab.getName().equals(inName)) {
                    return tab;
                }
            }
            throw new ValidatorException("The " + inName + " tab does not exist");
        }
    }

    /**
     * Represents a tab in a google sheet. On construction uses the google sheets api to populate itself
     * with the cells in the tab. Extended from an a two dimensional String ArrayList.
     */
    private class Tab extends ArrayList<ArrayList<String>> {

        /**
         * The name of this tab.
         */
        private final String name;

        /**
         * Constructor for a tab object. Uses sheetsService to populate itself with the cells in the actual google
         * sheets tab. Automatically strips all leading and trailing white space from the cells.
         *
         * @param inSpreadsheetId a String that is the id of the google sheet the tab is in
         * @param inName          a String that is the name of the tab
         */
        protected Tab(String inSpreadsheetId, String inName) {
            super();
            this.name = inName;

            try {
                ValueRange response = sheetsService.spreadsheets().values()
                        .get(inSpreadsheetId, name + "!A1:Z")
                        .setValueRenderOption("FORMATTED_VALUE")
                        .execute();
                for (List<Object> row : response.getValues()) {
                    ArrayList<String> newRow = new ArrayList<>();
                    for (Object cell : row) {
                        // to allow a single white space cell
                        if (cell.toString().matches("\\u0020+")) {
                            newRow.add(" ");
                        } else {
                            // otherwise strip cells and decompose diacritics from them
                            newRow.add(Normalizer.normalize(cell.toString().strip(), Normalizer.Form.NFD));
                        }
                    }
                    this.add(newRow);
                }
            } catch (Exception e) {
                fatalErrors.add("not able to find information in the tab \"" + this.name +
                        "\" or software was unable to access the sheet");
            }

        }

        protected String getName() {
            return this.name;
        }

        /**
         * compares the tab against a provided range (A1 format). Adds fatal errors if there are not enough rows in the
         * tab, removes rows/columns outside the range if they are present. Adds fatal errors for too short rows,
         * empty cells, and cells that are multiple lines.
         *
         * @param inRange a String that is the range to compare the tab against (in A1 format)
         */
        private void sizeTabUsingRange(String inRange) {

            int rowLen = rowLenFromRange(inRange);
            Integer colLen = colLenFromRange(inRange);

            ArrayList<Integer> toRemove = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).size() > rowLen) {
                    this.set(i, new ArrayList<>(this.get(i).subList(0, rowLen)));
                }
                if (this.get(i).isEmpty() || new HashSet<>(this.get(i)).size() == 1 && this.get(i).get(0).isEmpty()) {
                    toRemove.add(i);
                } else if (this.get(i).size() < rowLen) {
                    for (int j = this.get(i).size(); j < rowLen; j++) {

                        if (defaultValInTemplateTxt(this.name, j) != null) {
                            this.get(i).add(defaultValInTemplateTxt(this.name, j));
                            warnings.add("The tab \"" + this.getName() + "\" is missing cells/columns which could be replaced " +
                                    "by default values found in the latest language pack template. " +
                                    "Validation and downloading will proceed as if missing information was filled " +
                                    "in with default values.");
                        } else {
                            this.get(i).add("");
                            fatalErrors.add("The row " + (i + 1) + " in " + this.name + " is too short. It should have " +
                                    rowLen + " cells.");
                        }
                    }
                } else {
                    for (int j = 0; j < this.get(i).size(); j++) {
                        if (this.get(i).get(j).contains("\n")) {
                            fatalErrors.add("The cell at row " + (i + 1) + " column " + (j + 1) + " in " + this.name +
                                    " contains multiple lines. Please delete the 'enter' character ");
                        } else if (this.get(i).get(j).isEmpty()) {
                            if (defaultValInTemplateTxt(this.name, j) != null) {
                                this.get(i).set(j, defaultValInTemplateTxt(this.name, j));
                                warnings.add("The tab \"" + this.getName() + "\" is missing cells/columns which could be replaced " +
                                        "by default values found in the latest language pack template. " +
                                        "Validation and downloading will proceed as if missing information was filled " +
                                        "in with default values.");

                            } else {
                                fatalErrors.add("The cell at row " + (i + 1) + " column " + (j + 1) + " in " + this.name +
                                        " is empty. Please add info to this cell.");
                            }
                        }
                    }
                }
            }
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                if (!(toRemove.contains(toRemove.get(i) + 1) || toRemove.get(i) + 1 == this.size())) {
                    warnings.add("The row " + (toRemove.get(i) + 2) + " in " + this.name + " appears to have empty rows above it." +
                            " The validator will behave as if these empty row(s) were deleted");
                }
                this.remove((int) toRemove.get(i));
            }

            if (colLen != null) {
                if (this.size() < colLen) {
                    fatalErrors.add("the tab " + this.name + " does not have enough rows. It should " +
                            "have " + colLen);
                    for (int i = this.size(); i < colLen; i++) {
                        ArrayList<String> newRow = new ArrayList<>();
                        for (int j = 0; j < rowLen; j++) {
                            newRow.add("");
                        }
                        this.add(newRow);
                    }
                }

                if (this.size() > colLen) {
                    this.subList(colLen, this.size()).clear();
                }
            }
        }

        /**
         * returns an ArrayList of Strings representing the column at the given column number. Removes the
         * first row of the tab (the header row) before returning the column. Adds a fatal error
         * and throws a ValidatorException if the tab is empty.
         *
         * @param colNum the column number to return (0 indexed)
         * @return an ArrayList of Strings representing the given column number in the tab (excluding header)
         * @throws ValidatorException if the tab is empty
         */
        protected ArrayList<String> getCol(int colNum) throws ValidatorException {
            try {
                ArrayList<String> toReturn = new ArrayList<>();
                for (ArrayList<String> row : this) {
                    toReturn.add(row.get(colNum));
                }
                toReturn.remove(0);
                return toReturn;
            } catch (IndexOutOfBoundsException e) {
                fatalErrors.add(("the tab " + this.getName() + " is completely empty"));
                throw new ValidatorException("the tab " + this.getName() + " is completely empty");
            }
        }

        /**
         * returns an ArrayList of Strings representing a row that starts with the given first cell. Adds
         * a fatal error and throws a ValidatorException if the tab does not contain a row that starts with the given
         * first cell.
         *
         * @param firstCell a String that is the first cell of the row to return
         * @return an ArrayList of Strings representing a row in the tab that starts with the given first cell
         * @throws ValidatorException if the tab does not contain a row that starts with the given first cell
         */
        protected ArrayList<String> getRowFromFirstCell(String firstCell) throws ValidatorException {
            return getRowWithCellInCol(firstCell, 0);
        }

        /**
         * returns an ArrayList of Strings representing a row that the given cell in the given column number. Adds
         * a fatal error and throws a ValidatorException if the tab does not contain a row satisfies these conditions
         *
         * @param cell a String that is the desired cell to be looked for in a specific column
         * @param col  the column to look for the given cell in
         * @return an ArrayList of Strings representing a row in the tab that contains the given cell at the given column
         * @throws ValidatorException if the tab does not contain a row that satisfies these conditions
         */
        protected ArrayList<String> getRowWithCellInCol(String cell, int col) throws ValidatorException {

            for (ArrayList<String> row : this) {
                // allows for a number followed by a period followed by any number of spaces before the String cell
                //(which is treated as a string literal)
                if (row.get(col).matches("([0-9]+\\.)?\\s*" + Pattern.quote(cell))) {
                    return row;
                }
            }
            fatalErrors.add("cannot find a row in " + this.getName() + " that contains \"" + cell + "\" in column " + col);
            throw new ValidatorException("cannot find a row in " + this.getName() + " that contains \"" + cell + "\" in column " + col);
        }

        /**
         * checks if the column at the given column number contains duplicates. Adds a fatal error if it does.
         *
         * @param colNum the column number to check (zero indexed)
         */
        private void checkColForDuplicates(int colNum) throws ValidatorException {
            Set<String> colSet = new HashSet<>();
            for (String cell : this.getCol(colNum)) {
                if (!colSet.add(cell)) {
                    fatalErrors.add("\"" + cell + "\"" + " appears more than once in column " + (colNum + 1) + " of " + this.getName());
                }
            }
        }

        /**
         * Private helper function in Tab to interpret the provided range (A1 format) and return the number of rows
         *
         * @param range a String that is the range to interpret (in A1 format)
         * @return the number of rows specified by the range
         */
        private Integer rowLenFromRange(String range) {
            try {
                int ascii1 = range.charAt(0);
                int ascii2 = range.charAt(range.indexOf(':') + 1);
                return ascii2 - ascii1 + 1;
            } catch (Exception e) {
                throw new RuntimeException("requested ranges are invalid");
            }
        }

        /**
         * Private helper function in Tab to interpret the provided range (A1 format) and return the number of columns
         *
         * @param range a String that is the range to interpret (in A1 format)
         * @return the number of columns specified by the range, or null if none are specified
         */
        private Integer colLenFromRange(String range) {
            try {
                int ascii1 = Integer.parseInt(range.substring(1, range.indexOf(':')));
                int ascii2 = Integer.parseInt(range.substring(range.indexOf(':') + 2));
                return ascii2 - ascii1 + 1;
            } catch (Exception e) {
                return null;
            }

        }

        /**
         * Overrides the toString method to return a tab separated string representation of the tab
         *
         * @return a tab separated string representation of the tab
         */
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
    //</editor-fold>

    //<editor-fold desc="helper methods">
    private boolean decideIfFontAttempted() {
        try {
            GoogleDriveFolder fontFolder = langPackDriveFolder.getFolderFromName("font");
            boolean hasXml = false;
            boolean allCorrect = true;
            int nFonts = 0;
            for (GoogleDriveItem item : fontFolder.folderContents) {
                if (item.getMimeType().equals("text/xml")) {
                    hasXml = true;
                    continue;
                }
                allCorrect &= DESIRED_FILETYPE_FROM_SUBFOLDERS.get("font").contains(item.getMimeType());
                if (allCorrect) {
                    nFonts += 1;
                }
            }
            boolean success = true;
            if (!hasXml) {
                fatalErrors.add("Missing font xml in font folder");
                success = false;
            }
            if (nFonts < 2) {
                fatalErrors.add("Missing one or both required fonts (regular and bold)");
                success = false;
            }
            return success;
        } catch (ValidatorException e) {
            DESIRED_FILETYPE_FROM_SUBFOLDERS.remove("font");
            return false;
        }
    }

    /**
     * Private helper function to evaluate if an optional audio feature is being attempted. Returns true
     * if the tab contains any audio names in the given colum AND the subfolder with the given name
     * is not empty. Returns false otherwise, removing the subfolder from DESIRED_FILETYPE_FROM_SUBFOLDER
     * and adding a warning if one of the two conditions is met.
     * (skips cells containing "X" or "naWhileMPOnly" when evaluating a tab).
     *
     * @param tabName       a String that is the name of the tab with the audio names
     * @param colNum        the column number of the tab with the audio names
     * @param subFolderName a String that is the name of the subfolder in the drive folder with the audio files
     * @return True if the tab contains any audio names in the given colum AND the subfolder with the given name
     * is not empty. False otherwise.
     */
    private boolean decideIfAudioAttempted(String tabName, int colNum, String subFolderName) {

        boolean someAudioFiles = false;
        boolean someAudioNames = false;

        try {
            GoogleDriveFolder subFolder = langPackDriveFolder.getFolderFromName(subFolderName);
            // Don't include special instructions in this check.
            // (will need to change this if the special instructions stop being hardcoded
            for (GoogleDriveItem item : subFolder.folderContents) {
                if (SPECIAL_AUDIO_INSTRUCTIONS.contains(item.name)) {
                    someAudioNames = true;
                    break; // Override name check
                }
            }
            if (subFolder.getName().equals(subFolderName) && subFolder.size() > 0) {
                someAudioFiles = true;
            }
        } catch (ValidatorException ignored) {
        }

        try {
            ArrayList<String> AudioNames = langPackGoogleSheet.getTabFromName(tabName).getCol(colNum);
            AudioNames.removeAll(Set.of("X", "naWhileMPOnly"));
            if (!AudioNames.isEmpty()) {
                someAudioNames = true;
            }
        } catch (ValidatorException ignored) {
        }

        if (someAudioNames && someAudioFiles) {
            return true;
        } else if (someAudioNames) {
            warnings.add("you list names of audio files in the column " + (char) (colNum + 65) + " of  the tab " + tabName
                    + " (ie you have text in the column that is not 'X') but the folder " + subFolderName + " is empty. "
                    + "Please add matching audio files to the folder " + subFolderName + " if you want to use this feature");
        } else if (someAudioFiles) {
            warnings.add("you have audio files in the folder " + subFolderName + " but column"
                    + " of the tab " + tabName + " doesn't list any audio file names"
                    + " please add matching audio file names to the tab " + tabName + " if you want to use this feature");
        }
        DESIRED_FILETYPE_FROM_SUBFOLDERS.remove(subFolderName);
        return false;
    }

    /**
     * Private helper function to evaluate if an optional syllables feature is being attempted. Returns true
     * if the tab contains more than six words parsed into syllables (they contain periods) AND the syllables tab is not
     * empty. Returns false otherwise, adding a warning if one of the two conditions is met.
     *
     * @return True if the syllables tab contains more than six words parsed into syllables (they contain periods)
     * AND the syllables tab is not empty. False otherwise.
     */
    private boolean decideIfSyllablesAttempted() {

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
        } catch (ValidatorException e) {
            warnings.add(FAILED_CHECK_WARNING + "the wordlist tab");
        }

        try {
            if (langPackGoogleSheet.getTabFromName("syllables").size() > 1) {
                syllTabNotEmpty = true;
            }
        } catch (ValidatorException ignored) {
        }

        if (syllTabNotEmpty && numerousWordsSpliced) {
            return true;
        } else if (numerousWordsSpliced) {
            warnings.add("you have more than 6 words in wordlist that are spliced with periods"
                    + " but the syllables tab is empty."
                    + " Please add syllables to the syllables tab if you want to use syllable games");
        } else if (syllTabNotEmpty) {
            warnings.add("your syllables tab is not empty but your words in wordlist aren't " +
                    "spliced with periods. Please splice words into syllables with a period in wordlist" +
                    " if you want to use syllable games");
        }
        return false;
    }

    /**
     * Private helper function to compare a word against its type specification string, adding errors appropriately.
     * Compares the possible type specifications for the tiles in the word against what is specified in the type
     * specification string.
     *
     * @param word a Word object loaded from wordlist
     * @return An ArrayList of Strings, where the ith string represents the type specification of the ith tile in the word
     */
    private ArrayList<String> parseTypeSpecification(Word word) throws ValidatorException {
        ArrayList<Tile> wordAsSimpleTileList;
        if (scriptType.matches("(Thai|Lao|Khmer)")) { // Type specifications must map to all simple, contiguous tile pieces, and not to the final parsing, which contains any complex tiles
            wordAsSimpleTileList = tileList.parseWordIntoTilesPreliminary(word);
        } else {
            wordAsSimpleTileList = tileList.parseWordIntoTiles(word);
        }

        if (wordAsSimpleTileList == null) {
            throw new ValidatorException("Cannot parse word \"" + word.wordInLOP + "\" in wordlist into tiles from gametiles.");
        }

        String typeSpecifications = word.mixedDefs;
        //compare the type specifications to the possible types of the tiles in the word
        ArrayList<String> toReturn = new ArrayList<>();
        ArrayList<String> typeSpecsList = new ArrayList<>();
        //workaround so 12 gets split into 1 and 2 the first time but not the second time
        if (typeSpecifications.startsWith("1")) {
            typeSpecifications = typeSpecifications.replaceFirst("1", "");
            typeSpecsList.add("1");
        }
        // split the type specifications into a list of strings. Cannot use split with lookahead because it doesn't work with variable length lookaheads
        Pattern oneSpec = Pattern.compile("1?[0-9]|[CVXT]|AD|AV|BV|FV|LV|SAD|PC|.");
        Matcher specMatcher = oneSpec.matcher(typeSpecifications);
        while (specMatcher.find()) {
            typeSpecsList.add(specMatcher.group());
        }

        if (typeSpecsList.size() == 1 && !typeSpecifications.equals("-")) {
            return parseAbbreviatedTypeSpecification(word);
        } else if (typeSpecifications.equals("-")) {  // No multi types info included. Build the full type specification out of index numbers
            typeSpecsList.clear();
            for (int i = 0; i < wordAsSimpleTileList.size(); i++) {
                typeSpecsList.add(wordAsSimpleTileList.get(i).tileType);
            }
        } else if (typeSpecsList.size() != wordAsSimpleTileList.size()) {
            ArrayList<String> wordAsSimpleTileStringList = new ArrayList<>();
            for (Tile tile : wordAsSimpleTileList) {
                wordAsSimpleTileStringList.add(tile.text);
            }
            fatalErrors.add("In wordlist, the word " + word.wordInLOP + " has " + wordAsSimpleTileList.size() +
                    " tiles, but the mixed types cell has " + typeSpecsList.size() + " specifications (its tiles are " + wordAsSimpleTileStringList + ")");
            throw new ValidatorException("In wordlist, the word " + word.wordInLOP + " has " + wordAsSimpleTileList.size() +
                    " tiles, but the mixed types cell has " + typeSpecsList.size() + " specifications (its tiles are " + wordAsSimpleTileStringList + ")");
        }

        for (int i = 0; i < typeSpecsList.size(); i++) {
            Tile currentTile = wordAsSimpleTileList.get(i);
            String currentSpecification = typeSpecsList.get(i);

            if (currentSpecification.matches("1?[0-9]")) {
                if ((!currentTile.tileTypeB.equals("none") || !currentTile.tileTypeC.equals("none"))) {
                    fatalErrors.add("In wordlist, the word " + word.wordInLOP + " has no type specification" +
                            " for tile " + currentTile.text + " but that tile has multiple types");
                }
                toReturn.add(currentTile.tileType);
            } else if (currentSpecification.equals(currentTile.tileType) || currentSpecification.equals(currentTile.tileTypeB) || currentSpecification.equals(currentTile.tileTypeC)) {
                toReturn.add(currentSpecification);
            } else {
                fatalErrors.add("In wordlist, the word \"" + word.wordInLOP + "\" has type specification \"" + currentSpecification +
                        "\" for tile \"" + currentTile.text + "\" but that tile does not have that type specification");
                throw new ValidatorException("In wordlist, the word \"" + word.wordInLOP + "\" has type specification \"" + currentSpecification +
                        "\" for tile " + currentTile.text + "\" but that tile does not have that type specification");
            }
        }
        return toReturn;
    }

    /**
     * Private helper function for parseTypeSpecification. Compares a word to a typeSpecification if the user has chosen
     * to use an abbreviated form of type specification.
     *
     * @param word a Word object loaded from wordList
     * @return An ArrayList of Strings, where the ith string represents the type specification of the ith tile in the word
     */
    private ArrayList<String> parseAbbreviatedTypeSpecification(Word word) throws ValidatorException {
        Tab gameTilesTab = langPackGoogleSheet.getTabFromName("gametiles");
        ArrayList<Tile> wordAsSimpleTileList;
        if (scriptType.matches("(Thai|Lao|Khmer)")) { // Type specifications must be given for all contiguous tile pieces (not mapped to the final parsing, which will get complex tiles)
            wordAsSimpleTileList = tileList.parseWordIntoTilesPreliminary(word);
        } else {
            wordAsSimpleTileList = tileList.parseWordIntoTiles(word);
        }

        if (wordAsSimpleTileList == null) {
            throw new ValidatorException("Cannot parse word \"" + word.wordInLOP + "\" in wordlist into tiles from gametiles.");
        }

        String typeSpecifications = word.mixedDefs;
        // Go through each tile in the word, checking if the tile is multi-type and if the single multi-type
        // tile has been found already and adding to the tile specifications list accordingly.
        ArrayList<String> toReturn = new ArrayList<>();
        boolean foundMultiTypeTile = false;
        for (int i = 0; i < wordAsSimpleTileList.size(); i++) {
            ArrayList<String> tileRow = gameTilesTab.getRowFromFirstCell(wordAsSimpleTileList.get(i).text);
            boolean isMultiType = !tileRow.get(7).equals("none") || !tileRow.get(9).equals("none");
            if (isMultiType) {
                foundMultiTypeTile = true;
                if (
                        tileRow.get(7).equals(typeSpecifications) ||
                                tileRow.get(9).equals(typeSpecifications) ||
                                tileRow.get(4).equals(typeSpecifications)
                ) {
                    toReturn.add(typeSpecifications);
                } else {
                    fatalErrors.add("In wordlist, the word \"" + word.wordInLOP + "\" specifies only ONE multi-type tile (with the " +
                            "type specification \"" + typeSpecifications +
                            "\") but the tile with row " + tileRow + " is a multi-type tile without a match to this specification");
                    throw new ValidatorException("In wordlist, the word \"" + word.wordInLOP + "\" specifies only ONE multi-type tile (with the " +
                            "type specification \"" + typeSpecifications + "\") but the tile with row " + tileRow + " is a multi-type tile without a match to this specification");
                }
            } else {
                toReturn.add(tileRow.get(4));
            }
        }
        if (!foundMultiTypeTile) {
            ArrayList<String> wordAsSimpleTileStringList = new ArrayList<>();
            for (Tile tile : wordAsSimpleTileList) {
                wordAsSimpleTileStringList.add(tile.text);
            }
            fatalErrors.add("In wordlist, the word \"" + word.wordInLOP + "\" specifies ONE multi-type tile with the " +
                    "type specification \"" + typeSpecifications +
                    "\" but none of its tiles have multiple types (its simple tiles are " + wordAsSimpleTileStringList + ")");
            throw new ValidatorException("In wordlist, the word \"" + word.wordInLOP + "\" specifies ONE multi-type tile with the " +
                    "type specification \"" + typeSpecifications +
                    "\" but none tiles have multiple types (its simple tiles are " + wordAsSimpleTileStringList + ")");
        }
        return toReturn;
    }


    /**
     * Private helper function to build the driveService and sheetsService objects. Replaces the refresh token
     * if it expired or revoked.
     *
     * @param driveFolderId a String that is the a google drive ID used to check if the token is revoked or expired
     */
    private void buildServices(String driveFolderId) throws GeneralSecurityException, IOException {

        // initially builds the drive and sheets service not knowing if the token is revoked/expired
        String APPLICATION_NAME = "Alpha Tiles Validator";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        sheetsService =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCrdntls(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        driveService =
                new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCrdntls(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        // tries to use the drive service to check if the token is revoked or expired
        try {
            driveService.files().get(driveFolderId).execute();
        }
        // if the token is revoked or expired, deletes the token and then rebuilds the services
        catch (TokenResponseException | GoogleJsonResponseException e) {
            boolean badToken = false;
            if (e instanceof TokenResponseException) {
                badToken = true;
            } else {
                String errorDescription = ((GoogleJsonResponseException) e).getDetails().get("message").toString();
                if (errorDescription.contains("Request had invalid authentication credentials. Expected OAuth 2 access token")) {
                    badToken = true;
                }
            }

            if (badToken) {
                deleteDirectory(Paths.get("tokens"));
                sheetsService =
                        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCrdntls(HTTP_TRANSPORT))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
                driveService =
                        new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCrdntls(HTTP_TRANSPORT))
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
    private Credential getCrdntls(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {


        String crdntls =
                "{\"installed\":{\"client_id\":" +
                        "\"408067119694-iun4c6kd4mti4s5lcmg9vrqdmn99p486.apps.googleusercontent.com\"," +
                        "\"project_id\":\"alpha-tiles-validator\"," +
                        "\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\"," +
                        "\"token_uri\":\"https://oauth2.googleapis.com/token\"," +
                        "\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\"," +
                        "\"client_secret\":\"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\"," +
                        "\"redirect_uris\":[\"http://localhost\"]}}";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        //Global instance of the scopes required by this quickstart.
        //If modifying these scopes, delete your previously saved tokens/ folder.

        List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_READONLY);
        //String CREDENTIALS_FILE_PATH = "/credentials.json";
        // Load client secrets.
        InputStream in = new ByteArrayInputStream(crdntls.getBytes());
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

    /**
     * Deletes the entire directory at the given path in the computer's file system.
     *
     * @param directoryToBeDeleted the Path object to the directory to be deleted
     */
    private static void deleteDirectory(Path directoryToBeDeleted) {
        java.io.File[] allContents = directoryToBeDeleted.toFile().listFiles();
        if (allContents != null) {
            for (java.io.File file : allContents) {
                deleteDirectory(file.toPath());
            }
        }
        directoryToBeDeleted.toFile().delete();
    }

    /**
     * Provides the default value for a given colum in a given txt file in templateTemplate/res/raw
     *
     * @param rawFileName the name of the raw file to be looked at (without the "aa_" or ".txt")
     * @param col         the column number to look at the default value of
     * @return a string which is the default value of the given raw txt file at the given value, or null if there is none
     */
    private static String defaultValInTemplateTxt(String rawFileName, int col) {

        Path pathToRawFile = Paths.get("templateTemplate")
                .resolve("res").resolve("raw").resolve("aa_" + rawFileName + ".txt");
        try {
            BufferedReader txtReader = new BufferedReader(new FileReader(pathToRawFile.toFile()));
            String line;
            ArrayList<ArrayList<String>> contents = new ArrayList<>();
            while ((line = txtReader.readLine()) != null) {
                contents.add(new ArrayList<>(List.of(line.split("\t"))));
            }
            if ((contents.size() == 2) && (contents.get(1).size() > col)) {
                if (!contents.get(1).get(col).isEmpty()) {
                    return contents.get(1).get(col);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Copies an entire directory from one path to another. (recursively)
     *
     * @param directoryToBeCopied the Path object to the directory to be copied
     * @param destinationPath     the Path object to the destination directory
     */
    public static void copyDirectory(Path directoryToBeCopied, Path destinationPath) throws IOException {
        java.io.File[] allContents = directoryToBeCopied.toFile().listFiles();
        if (allContents != null) {
            for (java.io.File subContent : allContents) {
                Path oldPath = subContent.toPath();
                Path newPath = destinationPath.resolve(subContent.getName());
                Files.copy(oldPath, newPath);
                if (subContent.isDirectory()) {
                    copyDirectory(oldPath, newPath);
                }
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="ValidatorException">
    public static class ValidatorException extends Exception {
        public ValidatorException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class Checks {
        public boolean showRecommendations = true;

        public Checks(JPanel dialog) {
            addCheck(dialog, "Show recommendations", (ActionEvent e) -> showRecommendations = !showRecommendations);
        }

        private void addCheck(JPanel dialog, String message, ActionListener listener) {
            JCheckBox check = new JCheckBox(message);
            check.setSelected(true);
            check.addActionListener(listener);
            dialog.add(check);
        }

    }
    //</editor-fold>

    /****************************************************************************************************************************************
     Objects with helpful fields and methods accessed in parsing checks
     **************************************************************************************************************************************/

    /**
     * Word objects load from the wordlist tab are used in parsing methods for easier access
     */
    public static class Word {
        public String wordInLWC;
        public String wordInLOP;
        public int duration;
        public String mixedDefs;
        public String adjustment;
        public String stageOfFirstAppearance;

        public Word(String wordInLWC, String wordInLOP, int duration, String mixedDefs, String adjustment, String stageOfFirstAppearance) {
            this.wordInLWC = wordInLWC;
            this.wordInLOP = wordInLOP;
            this.duration = duration;
            this.mixedDefs = mixedDefs;
            this.adjustment = adjustment;
            this.stageOfFirstAppearance = stageOfFirstAppearance;
        }

        public boolean hasNull() {
            return wordInLWC == null || wordInLOP == null || mixedDefs == null || adjustment == null;
        }
    }

    /**
     * Extended by Tile and Syllable
     */
    public static class WordPiece {
        public String text;
        public ArrayList<String> distractors;
        public String audioName;

        public WordPiece(String textOfThisWordPiece) {
            this.text = textOfThisWordPiece;
        }

        public WordPiece(WordPiece anotherWordPiece) {
            this.text = anotherWordPiece.text;
            this.distractors = anotherWordPiece.distractors;
            this.audioName = anotherWordPiece.audioName;
        }

    }

    /**
     * Tile objects are specific instances of the tiles in gametiles
     */
    public static class Tile extends WordPiece {
        public String tileType;
        public String upper;
        public String tileTypeB;
        public String audioNameB;
        public String tileTypeC;
        public String audioNameC;
        public int tileDuration1;
        public int tileDuration2;
        public int tileDuration3;
        public int stageOfFirstAppearance;
        public int stageOfFirstAppearanceB;
        public int stageOfFirstAppearanceC;
        public String typeOfThisTileInstance;
        public int stageOfFirstAppearanceForThisTileType;
        public String audioForThisTileType;

        public Tile(String text, ArrayList<String> distractors, String tileType, String audioName, String upper, String tileTypeB, String audioNameB, String tileTypeC, String audioNameC, int tileDuration1, int tileDuration2, int tileDuration3, int stageOfFirstAppearance, int stageOfFirstAppearanceB, int stageOfFirstAppearanceC, String typeOfThisTileInstance, int stageOfFirstAppearanceForThisTileType, String audioForThisTileType) {
            super(text);
            this.distractors = distractors;
            this.tileType = tileType;
            this.audioName = audioName;
            this.upper = upper;
            this.tileTypeB = tileTypeB;
            this.audioNameB = audioNameB;
            this.tileTypeC = tileTypeC;
            this.audioNameC = audioNameC;
            this.tileDuration1 = tileDuration1;
            this.tileDuration2 = tileDuration2;
            this.tileDuration3 = tileDuration3;
            this.stageOfFirstAppearance = stageOfFirstAppearance;
            this.stageOfFirstAppearanceB = stageOfFirstAppearanceB;
            this.stageOfFirstAppearanceC = stageOfFirstAppearanceC;
            this.typeOfThisTileInstance = typeOfThisTileInstance;
            this.stageOfFirstAppearanceForThisTileType = stageOfFirstAppearanceForThisTileType;
            this.audioForThisTileType = audioForThisTileType;
        }

        public Tile(Tile anotherTile) {
            super(anotherTile);
            this.distractors = anotherTile.distractors;
            this.tileType = anotherTile.tileType;
            this.audioName = anotherTile.audioName;
            this.upper = anotherTile.upper;
            this.tileTypeB = anotherTile.tileTypeB;
            this.audioNameB = anotherTile.audioNameB;
            this.tileTypeC = anotherTile.tileTypeC;
            this.audioNameC = anotherTile.audioNameC;
            this.tileDuration1 = anotherTile.tileDuration1;
            this.tileDuration2 = anotherTile.tileDuration2;
            this.tileDuration3 = anotherTile.tileDuration3;
            this.stageOfFirstAppearance = anotherTile.stageOfFirstAppearance;
            this.stageOfFirstAppearanceB = anotherTile.stageOfFirstAppearanceB;
            this.stageOfFirstAppearanceC = anotherTile.stageOfFirstAppearanceC;
            this.typeOfThisTileInstance = anotherTile.typeOfThisTileInstance;
            this.stageOfFirstAppearanceForThisTileType = anotherTile.stageOfFirstAppearanceForThisTileType;
            this.audioForThisTileType = anotherTile.audioForThisTileType;
        }

        public boolean hasNull() {
            if (text == null || tileType == null || audioName == null || upper == null || tileTypeB == null || audioNameB == null || tileTypeC == null || audioNameC == null)
                return true;
            if (distractors.isEmpty())
                return true;
            return false;
        }
    }

    public class Key {
        public String text;
        public String color;

        public Key(String text, String color) {
            this.text = text;
            this.color = color;
        }

        public boolean hasNull() {
            return text == null || color == null;
        }
    }

    /**
     * Class containing methods for tile parsing
     */
    public static class TileList extends ArrayList<Tile> {
        public boolean contains(Tile tile) {
            for (int t = 0; t < size(); t++) {
                if (get(t).text.equals(tile.text) && get(t).typeOfThisTileInstance.equals(tile.typeOfThisTileInstance)) {
                    return true;
                }
            }
            return false;
        }

        public ArrayList<Tile> parseWordIntoTiles(Word wordListWord) {

            ArrayList<Tile> parsedWordArrayPreliminary = parseWordIntoTilesPreliminary(wordListWord);
            if (!scriptType.matches("(Thai|Lao|Khmer)")) {
                return parsedWordArrayPreliminary;
            }

            ArrayList<Tile> parsedWordTileArray = new ArrayList<>();

            int consonantScanIndex = 0;
            int currentConsonantIndex = 0;
            int previousConsonantIndex = -1;
            int nextConsonantIndex = parsedWordArrayPreliminary.size();
            boolean foundNextConsonant = false;

            while (consonantScanIndex < parsedWordArrayPreliminary.size()) {

                Tile currentConsonant = null;
                foundNextConsonant = false;
                Tile currentTile;
                String currentTileString;
                String currentTileType;
                // Scan for the next unchecked consonant tile
                while (!foundNextConsonant && consonantScanIndex < parsedWordArrayPreliminary.size()) {
                    currentTile = parsedWordArrayPreliminary.get(consonantScanIndex);
                    currentTileType = currentTile.typeOfThisTileInstance;
                    if (currentTileType.matches("(C|PC)")) {
                        currentConsonant = currentTile;
                        currentConsonantIndex = consonantScanIndex;
                        foundNextConsonant = true;
                    }
                    consonantScanIndex++;
                }
                if (!foundNextConsonant) {
                    currentConsonantIndex = parsedWordArrayPreliminary.size();
                }

                foundNextConsonant = false;
                // Scan for the unchecked consonant tile after that one, if it exists. Just save its index; it won't be added to the array on this iteration.
                while (!foundNextConsonant && consonantScanIndex < parsedWordArrayPreliminary.size()) {
                    currentTile = parsedWordArrayPreliminary.get(consonantScanIndex);
                    currentTileType = currentTile.typeOfThisTileInstance;
                    if (currentTileType.matches("(C|PC)")) {
                        nextConsonantIndex = consonantScanIndex;
                        foundNextConsonant = true;
                    }
                    consonantScanIndex++;
                }
                if (!foundNextConsonant) {
                    nextConsonantIndex = parsedWordArrayPreliminary.size();
                }

                // Combine vowel symbols into complex vowels that occur around the current consonant, as applicable. Find diacritics, spaces, and dashes that occur on/after this syllable.

                // Find vowel symbols that occur between previous and current consonants
                Tile vowelTile = null;
                ArrayList<Tile> SADTiles = new ArrayList<>();
                Tile diacriticTile;
                String vowelStringSoFar = "";
                String vowelTypeSoFar = "";
                String diacriticStringSoFar = "";
                Tile nonCombiningVowelFromPreviousSyllable = null;
                for (int b = previousConsonantIndex + 1; b < currentConsonantIndex; b++) {
                    currentTile = parsedWordArrayPreliminary.get(b);
                    currentTileString = currentTile.text;
                    currentTileType = currentTile.typeOfThisTileInstance;
                    if (currentTileType.equals("LV")) {
                        vowelStringSoFar += currentTileString;
                        if (vowelStringSoFar.equals(currentTileString)) { // the vowel so far is a preliminary tile
                            vowelTypeSoFar = currentTileType;
                        } else if (tileHashMap.containsKey(vowelStringSoFar)) {
                            vowelTypeSoFar = tileHashMap.find(vowelStringSoFar).tileType; // complex tiles do not get multityping
                        }
                    } else if (currentTileType.equals("V")) {
                        nonCombiningVowelFromPreviousSyllable = currentTile;
                    }
                }

                // Find vowel, diacritic, space, and dash symbols that occur between current and next consonants
                Tile nonComplexV = null;
                for (int a = currentConsonantIndex + 1; a < nextConsonantIndex; a++) {
                    currentTile = parsedWordArrayPreliminary.get(a);
                    currentTileString = currentTile.text;
                    currentTileType = currentTile.typeOfThisTileInstance;
                    if (currentTileType.matches("(AV|BV|FV)")) { // Prepare to add current AV/BV/FV to vowel-so-far
                        if (tileHashMap.containsKey(vowelStringSoFar)) { // Vowel composite so far is parsable as one tile in the tile list
                            if (vowelTypeSoFar.equals("LV")) {
                                if (!vowelStringSoFar.endsWith(placeholderCharacter)) {
                                    vowelStringSoFar += placeholderCharacter;
                                }
                            } else if (vowelTypeSoFar.matches("(AV|BV|FV)") && !vowelStringSoFar.startsWith(placeholderCharacter)) {
                                vowelStringSoFar = placeholderCharacter + vowelStringSoFar; // Put the placeholder before the previous AV/BV/FV before adding current AV/BV/FV to it
                            }
                        }
                        if (vowelStringSoFar.contains(placeholderCharacter) && currentTileString.contains(placeholderCharacter)) {
                            currentTileString = currentTileString.replace(placeholderCharacter, ""); // Just want one placeholder
                        }
                        vowelStringSoFar += currentTileString;
                        if (vowelStringSoFar.equals(currentTileString)) { // the vowel so far is a preliminary tile
                            vowelTypeSoFar = currentTileType;
                        } else if (tileHashMap.containsKey(vowelStringSoFar)) { // complex tiles do not get multityping
                            vowelTypeSoFar = tileHashMap.find(vowelStringSoFar).tileType;
                        }
                    } else if (currentTileType.matches("(AD|D)")) { // Save any AD (Above/After Diacritics) or other Diacritics between consonants
                        if (!diacriticStringSoFar.isEmpty() && !diacriticStringSoFar.contains(placeholderCharacter)) {
                            diacriticStringSoFar = placeholderCharacter + diacriticStringSoFar; // For complex diacritics
                        }
                        if (diacriticStringSoFar.contains(placeholderCharacter) && currentTileString.contains(placeholderCharacter)) { // Just want one placeholder
                            currentTileString = currentTileString.replace(placeholderCharacter, "");
                        }
                        diacriticStringSoFar += currentTileString;
                    } else if (currentTileType.equals("SAD")) { // Save any Space-And-Dash chars that comes between syllables.
                        SADTiles.add(currentTile);
                    } else if (!foundNextConsonant && currentTileType.equals("V")) { // There is a V (not LV/FV/AV/BV) on the end of the word
                        nonComplexV = currentTile;
                    }
                }


                // Add saved items to the tile array
                if (!(nonCombiningVowelFromPreviousSyllable == null)) {
                    parsedWordTileArray.add(nonCombiningVowelFromPreviousSyllable);
                }
                if (!(currentConsonant == null)) {
                    // Combine diacritics with consonant if that combination is in the tileList. Ex:บ๋
                    if (!diacriticStringSoFar.isEmpty() && tileHashMap.containsKey(currentConsonant.text + diacriticStringSoFar.replace(placeholderCharacter, ""))) {
                        currentConsonant = tileHashMap.find(currentConsonant.text + diacriticStringSoFar.replace(placeholderCharacter, ""));
                        diacriticStringSoFar = "";
                    }

                    if (!vowelStringSoFar.isEmpty()) {
                        // Add tiles in different orders based on the vowel's position
                        switch (vowelTypeSoFar) {
                            case "LV":
                                vowelTile = tileHashMap.find(vowelStringSoFar);
                                parsedWordTileArray.add(vowelTile);
                                parsedWordTileArray.add(currentConsonant);
                                if (!diacriticStringSoFar.isEmpty()) {
                                    diacriticTile = tileHashMap.find(diacriticStringSoFar);
                                    parsedWordTileArray.add(diacriticTile);
                                }
                                break;
                            case "AV":
                            case "BV":
                            case "V":
                                vowelTile = tileHashMap.find(vowelStringSoFar);
                                parsedWordTileArray.add(currentConsonant);
                                parsedWordTileArray.add(vowelTile);
                                if (!diacriticStringSoFar.isEmpty()) {
                                    diacriticTile = tileHashMap.find(diacriticStringSoFar);
                                    parsedWordTileArray.add(diacriticTile);
                                }
                                break;
                            case "FV":
                                parsedWordTileArray.add(currentConsonant);
                                if (!diacriticStringSoFar.isEmpty()) {
                                    diacriticTile = tileHashMap.find(diacriticStringSoFar);
                                    parsedWordTileArray.add(diacriticTile);
                                }
                                vowelTile = tileHashMap.find(vowelStringSoFar);
                                parsedWordTileArray.add(vowelTile);
                                break;
                        }
                    } else { // No vowel left to add after this consonant and before adding ADs
                        parsedWordTileArray.add(currentConsonant);
                        if (!diacriticStringSoFar.isEmpty()) {
                            diacriticTile = tileHashMap.find(diacriticStringSoFar);
                            if (diacriticTile.tileType.equals("AD")) {
                                parsedWordTileArray.add(diacriticTile);
                            }
                        }
                    }

                    // If a V is found before the (next) consonant, add it. It is syllable-initial or -mid.
                    if (!(nonComplexV == null)) {
                        parsedWordTileArray.add(nonComplexV);
                    }

                    // Add any other diacritics after any Vs.
                    if (!diacriticStringSoFar.isEmpty()) {
                        diacriticTile = tileHashMap.find(diacriticStringSoFar);
                        if (diacriticTile.tileType.equals("D")) {
                            parsedWordTileArray.add(diacriticTile);
                        }
                    }

                    // Add any spaces or dashes that come between syllables
                    if (!(SADTiles.isEmpty())) {
                        parsedWordTileArray.addAll(SADTiles);
                    }

                    previousConsonantIndex = currentConsonantIndex;
                }
                consonantScanIndex = nextConsonantIndex;
            }
            return parsedWordTileArray;

        }

        /**
         * Returns type of a preliminary tile that has multiple types
         *
         * @param index                  index of the multitype, preliminary tile within the tilesInWordPreliminary list
         * @param tilesInWordPreliminary preliminary (non-complex) parsed tiles from the word
         * @param wordListWord           a Word from the wordList
         * @return the type of the multitype tile specified
         */

        public String getInstanceTypeForMixedTilePreliminary(int index, ArrayList<Tile> tilesInWordPreliminary, Word wordListWord) throws NullPointerException {
            // if mixedDefinitionInfo is not C or V or X or dash, then we assume it has two elements
            // to disambiguate, e.g. niwan', where...
            // first n is a C and second n is a X (nasality indicator), and we would code as C234X6

            // JP: these types come from the gameTiles
            // In the wordlist, "-" means "no multifunction symbols in this word"
            // The types in the wordlist come from the same set of types as found in gametiles

            String mixedDefinitionInfoString = wordListWord.mixedDefs;
            String instanceType = null;
            ArrayList<String> types = new ArrayList<String>(Arrays.asList("C", "PC", "V", "X", "T", "-", "SAD", "LV", "AV", "BV", "FV", "D", "AD"));

            if (!types.contains(mixedDefinitionInfoString)) { // if it's not just one mixed-type definition,...

                // Store the mixed types information (e.g. C234X6, 1FV3C5) from the wordlist in an array.
                // one designation per tile. Either just tile index number (1-indexed), or a type specification.
                int numTilesInWord = tilesInWordPreliminary.size();
                String[] mixedDefinitionInfoArray = new String[numTilesInWord];

                // For numbers in the info string, store the numbers in the array
                for (int i = 0; i < numTilesInWord; i++) {
                    String number = String.valueOf(i + 1);
                    if (mixedDefinitionInfoString.contains(number)) {
                        mixedDefinitionInfoArray[i] = number;
                    }
                }
                // Now store what's in between the numbers (the type info parts)
                int previousNumberEndIndex = 0;
                int nextNumberStartIndex;
                for (int i = 0; i < numTilesInWord; i++) {
                    String nextNumber = String.valueOf(i + 2); // The number after this one, 1-indexed
                    int tilesInBetween = 1;

                    if (mixedDefinitionInfoArray[i] == null) { // A number wasn't filled in here, there must be type info for this tile
                        nextNumberStartIndex = mixedDefinitionInfoString.indexOf(nextNumber);
                        int nextNumberInt = Integer.parseInt(nextNumber);
                        while (nextNumberStartIndex == -1 && nextNumberInt <= numTilesInWord) { // Maybe the number after this one is not in the array, either. Find the next one that is.
                            nextNumberInt++;
                            nextNumber = String.valueOf(nextNumberInt);
                            nextNumberStartIndex = mixedDefinitionInfoString.indexOf(nextNumber);
                            tilesInBetween++;
                        }
                        if (nextNumberStartIndex == -1 || nextNumberInt > numTilesInWord) { // It checked to the end and didn't find any more numbers
                            nextNumberStartIndex = mixedDefinitionInfoString.length();
                        }

                        String infoBetweenPreviousAndNextNumbers = mixedDefinitionInfoString.substring(previousNumberEndIndex, nextNumberStartIndex);
                        if (tilesInBetween == 1) { // The substring is the type of the ith preliminary tile
                            mixedDefinitionInfoArray[i] = mixedDefinitionInfoString.substring(previousNumberEndIndex, nextNumberStartIndex);
                        } else { // The first type in substring is the type of the ith preliminary tile
                            String type = "";
                            for (int c = 1; c < infoBetweenPreviousAndNextNumbers.length(); c++) {
                                String firstC = infoBetweenPreviousAndNextNumbers.substring(0, c);
                                if (types.contains(firstC)) {
                                    type = firstC;
                                }
                            }
                            mixedDefinitionInfoArray[i] = type;
                        }
                    }
                    previousNumberEndIndex = previousNumberEndIndex + mixedDefinitionInfoArray[i].length(); // Next time, look starting after this info
                }
                instanceType = mixedDefinitionInfoArray[index];
            } else {
                instanceType = mixedDefinitionInfoString; // When mixedDefs is just "C", "V", etc. by itself
            }
            return instanceType;
        }


        public ArrayList<Tile> parseWordIntoTilesPreliminary(Word wordListWord) {
            // Updates by KP, Oct 2020
            // AH, Nov 2020, extended to check up to four characters in a game tile
            ArrayList<Tile> wordPreliminaryTileArray = new ArrayList<>();
            ArrayList<Tile> wordPreliminaryTileArrayFinal = new ArrayList<>();

            // Parse the reference word first
            String LOPwordString = wordListWord.wordInLOP;
            int charBlockLength;
            String next1Chars;
            String next2Chars;
            String next3Chars;
            String next4Chars;
            int tileIndex = 0;

            for (int i = 0; i < LOPwordString.length(); i++) {

                // Create blocks of the next one, two, three and four Unicode characters for analysis
                next1Chars = LOPwordString.substring(i, i + 1);

                if (i < LOPwordString.length() - 1) {
                    next2Chars = LOPwordString.substring(i, i + 2);
                } else {
                    next2Chars = "XYZXYZ";
                }

                if (i < LOPwordString.length() - 2) {
                    next3Chars = LOPwordString.substring(i, i + 3);
                } else {
                    next3Chars = "XYZXYZ";
                }

                if (i < LOPwordString.length() - 3) {
                    next4Chars = LOPwordString.substring(i, i + 4);
                } else {
                    next4Chars = "XYZXYZ";
                }

                // See if the blocks of length one, two, three or four Unicode characters matches game tiles
                // Choose the longest block that matches a game tile and add that as the next segment in the parsed word array
                charBlockLength = 0;
                if (tileHashMap.containsKey(next1Chars) || tileHashMap.containsKey(placeholderCharacter + next1Chars) || tileHashMap.containsKey(next1Chars + placeholderCharacter)) {
                    // If charBlockLength is already assigned 2 or 3 or 4, it should not overwrite with 1
                    charBlockLength = 1;
                }
                if (tileHashMap.containsKey(next2Chars)) {
                    // The value 2 can overwrite 1 but it can't overwrite 3 or 4
                    charBlockLength = 2;
                }
                if (tileHashMap.containsKey(next3Chars)) {
                    // The value 3 can overwrite 1 or 2 but it can't overwrite 4
                    charBlockLength = 3;
                }
                if (tileHashMap.containsKey(next4Chars)) {
                    // The value 4 can overwrite 1 or 2 or 3
                    charBlockLength = 4;
                }


                // Add the selected game tile (the longest selected from the previous loop) to the parsed word array
                String tileString = "";
                switch (charBlockLength) {
                    case 1:
                        if (tileHashMap.containsKey(next1Chars)) {
                            tileString = next1Chars;
                        } else if (tileHashMap.containsKey(placeholderCharacter + next1Chars)) { // For AV/BV/FV/AD/D stored with placeholder
                            tileString = placeholderCharacter + next1Chars;
                        } else if (tileHashMap.containsKey(next1Chars + placeholderCharacter)) { // For LV stored with placeholder
                            tileString = next1Chars + placeholderCharacter;
                        }
                        break;
                    case 2:
                        tileString = next2Chars;
                        i++;
                        break;
                    case 3:
                        tileString = next3Chars;
                        i += 2;
                        break;
                    case 4:
                        tileString = next4Chars;
                        i += 3;
                        break;
                    default:
                        break;
                }
                if (!tileString.isEmpty()) {
                    Tile nextTile = tileHashMap.find(tileString);
                    wordPreliminaryTileArray.add(nextTile);
                }
            }
            for (Tile tile : wordPreliminaryTileArray) { // Set instance-specific fields
                Tile nextTile = new Tile(tile);
                if (MULTITYPE_TILES.contains(nextTile.text)) {
                    nextTile.typeOfThisTileInstance = getInstanceTypeForMixedTilePreliminary(tileIndex, wordPreliminaryTileArray, wordListWord);
                    if (nextTile.typeOfThisTileInstance.equals(nextTile.tileTypeB)) {
                        nextTile.stageOfFirstAppearanceForThisTileType = nextTile.stageOfFirstAppearanceB;
                        nextTile.audioForThisTileType = nextTile.audioNameB;
                    } else if (nextTile.typeOfThisTileInstance.equals(nextTile.tileTypeC)) {
                        nextTile.stageOfFirstAppearanceForThisTileType = nextTile.stageOfFirstAppearanceC;
                        nextTile.audioForThisTileType = nextTile.audioNameC;
                    } else {
                        nextTile.stageOfFirstAppearanceForThisTileType = nextTile.stageOfFirstAppearance;
                        nextTile.audioForThisTileType = nextTile.audioName;
                    }
                    wordPreliminaryTileArrayFinal.add(nextTile);
                } else {
                    nextTile.typeOfThisTileInstance = nextTile.tileType;
                    nextTile.stageOfFirstAppearanceForThisTileType = nextTile.stageOfFirstAppearance;
                    nextTile.audioForThisTileType = nextTile.audioName;
                    wordPreliminaryTileArrayFinal.add(nextTile);
                }
                tileIndex++;
            }

            return wordPreliminaryTileArrayFinal;
        }

    }

    /**
     * Class mapping tile texts to Tile objects
     */
    public class TileHashMap extends HashMap<String, Tile> {
        // Getting a tile from the tile hashmap using the baseTile String will return
        // a Tile object with that baseTile string. It is essentially type-ignorant.
        // It doesn't return based on the typeOfThisTileInstance Tile property.
        public Tile find(String key) {
            for (String k : keySet()) {
                if (k.equals(key)) {
                    return (new Tile(get(k)));
                }
            }
            return null;
        }

    }


    /**
     * Method to build a list of Word objects for easier access during parsing
     *
     * @throws IOException if the wordlist tab was not downloaded into temp/wordlist.txt
     */
    public void buildWordList() throws IOException {
        // KP, Oct 2020 (updated by AH to allow for spaces in fields (some common nouns in some languages have spaces)

        Path pathToValidator = rootPath.resolve("validator");
        Path pathToTempFolder = pathToValidator.resolve("temp");
        BufferedReader wordlistFileReader = new BufferedReader(new FileReader(pathToTempFolder.resolve("wordlist.txt").toFile(), StandardCharsets.UTF_8));
        boolean header = true;
        wordList = new ArrayList<Word>();
        String thisLine = wordlistFileReader.readLine();
        while (thisLine != null) {
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                header = false;
            } else {
                try {
                    Word word = new Word(thisLineArray[0], thisLineArray[1], Integer.parseInt(thisLineArray[2]), thisLineArray[3], "", thisLineArray[5]);
                    wordList.add(word);
                } catch (IndexOutOfBoundsException e) {
                    // this row in wordlist.txt is empty at some columns and cannot be assessed
                }
            }
            thisLine = wordlistFileReader.readLine();
        }
        wordlistFileReader.close();
    }

    /**
     * Method to build a list of Key objects for easier access during parsing
     *
     * @throws IOException if the keyboard tab was not downloaded into temp/keyboard.txt
     */
    public void buildKeyList() throws IOException {
        // KP, Oct 2020
        // AH, Nov 2020, updates to add second column (color theme)
        // AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces

        Path pathToValidator = rootPath.resolve("validator");
        Path pathToTempFolder = pathToValidator.resolve("temp");
        BufferedReader keyboardFileReader = new BufferedReader(new FileReader(pathToTempFolder.resolve("keyboard.txt").toFile(), StandardCharsets.UTF_8));
        boolean header = true;
        keyList = new ArrayList<Key>();
        String thisLine = keyboardFileReader.readLine();
        while (thisLine != null) {
            String[] thisLineArray = thisLine.split("\t");
            if (header) { // skip header row
                header = false;
            } else {
                try {
                    Key key = new Key(thisLineArray[0], thisLineArray[1]);
                    keyList.add(key);
                } catch (IndexOutOfBoundsException e) {
                    // This row is missing values in at least one column
                }
            }
            thisLine = keyboardFileReader.readLine();
        }
        keyboardFileReader.close();
    }

    /**
     * Method to build a list of color strings to check against assigned key colors and game door colors
     *
     * @throws IOException if the colors tab was not downloaded into temp/colors.txt
     */
    public void buildColorList() throws IOException {
        Path pathToValidator = rootPath.resolve("validator");
        Path pathToTempFolder = pathToValidator.resolve("temp");
        BufferedReader colorsFileReader = new BufferedReader(new FileReader(pathToTempFolder.resolve("colors.txt").toFile(), StandardCharsets.UTF_8));
        boolean header = true;
        colorList = new ArrayList<String>();
        String thisLine = colorsFileReader.readLine();
        while (thisLine != null) {
            String[] thisLineArray = thisLine.split("\t", 3);
            if (header) {
                header = false;
            } else {
                if (!thisLineArray[0].isEmpty()) {
                    colorList.add(thisLineArray[0]);
                }
            }
            thisLine = colorsFileReader.readLine();
        }
        colorsFileReader.close();
    }

    /**
     * Load a full list of Tile objects, with their various specified types, from gametiles
     *
     * @throws IOException if the gametiles tab was not downloaded to temp/gametiles.txt
     */
    public void buildTileList() throws IOException {
        // KP, Oct 2020
        // AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces
        // AH Mar 2021, add new column for audio tile and for upper case tile

        Path pathToValidator = rootPath.resolve("validator");
        Path pathToTempFolder = pathToValidator.resolve("temp");
        BufferedReader gametilesFileReader = new BufferedReader(new FileReader(pathToTempFolder.resolve("gametiles.txt").toFile(), StandardCharsets.UTF_8));
        String thisLine = gametilesFileReader.readLine();
        boolean header = true;
        tileList = new TileList();

        while (thisLine != null) {
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                header = false; // skips the header line
            } else {
                // Sort information for staged introduction, including among potential second or third types of a tile
                int stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3;
                if (!thisLineArray[14].matches("[0-9]+")) { // Add all first types of tiles to "stage 1" if stages aren't being used
                    stageOfFirstAppearance = 1;
                } else {
                    stageOfFirstAppearance = Integer.parseInt(thisLineArray[14]);
                    if (!(stageOfFirstAppearance >= 1 && stageOfFirstAppearance <= 7)) {
                        stageOfFirstAppearance = 1;
                    }
                }
                if (!thisLineArray[15].matches("[0-9]+")) {
                    stageOfFirstAppearanceType2 = 1;
                } else {
                    stageOfFirstAppearanceType2 = Integer.parseInt(thisLineArray[15]);
                    if (!(stageOfFirstAppearanceType2 >= 1 && stageOfFirstAppearanceType2 <= 7)) {
                        stageOfFirstAppearance = 1;
                    }
                }
                if (!thisLineArray[16].matches("[0-9]+")) {
                    stageOfFirstAppearanceType3 = 1;
                } else {
                    stageOfFirstAppearanceType3 = Integer.parseInt(thisLineArray[16]);
                    if (!(stageOfFirstAppearanceType3 >= 1 && stageOfFirstAppearanceType3 <= 7)) {
                        stageOfFirstAppearance = 1;
                    }
                }
                // Create tile(s) and add to list; may add up to three tiles from the same line if it has multiple types
                try {
                    ArrayList<String> distractors = new ArrayList<>();
                    distractors.add(thisLineArray[1]);
                    distractors.add(thisLineArray[2]);
                    distractors.add(thisLineArray[3]);
                    Tile tile = new Tile(thisLineArray[0], distractors, thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3, thisLineArray[4], stageOfFirstAppearance, thisLineArray[5]);

                    tileList.add(tile);

                    if (!tile.tileTypeB.equals("none")) {
                        tile = new Tile(thisLineArray[0], distractors, thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3, thisLineArray[7], stageOfFirstAppearanceType2, thisLineArray[8]);
                        if (!tile.hasNull()) {
                            tileList.add(tile);
                        }
                    }
                    if (!tile.tileTypeC.equals("none")) {
                        tile = new Tile(thisLineArray[0], distractors, thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3, thisLineArray[9], stageOfFirstAppearanceType3, thisLineArray[10]);
                        if (!tile.hasNull()) {
                            tileList.add(tile);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    // This gametiles row is missing one or more values; it will be flagged earlier and will not be added
                }
            }
            thisLine = gametilesFileReader.readLine();
        }
        gametilesFileReader.close();
        for (Tile thisTile : tileList) {
            if (thisTile.audioForThisTileType.equals("zz_no_audio_needed") && thisTile.typeOfThisTileInstance.equals("PC")) {
                SILENT_PLACEHOLDER_CONSONANTS.add(thisTile);
            }
            if (thisTile.audioForThisTileType.equals("zz_no_audio_needed") && !thisTile.typeOfThisTileInstance.equals("PC")) {
                SILENT_PRELIMINARY_TILES.add(thisTile);
            }
        }
        buildTileHashMap();
    }

    /**
     * helper method making the HashMap of tile texts to tiles from gametiles
     */
    public void buildTileHashMap() {
        tileHashMap = new TileHashMap();
        for (int i = 0; i < tileList.size(); i++) {
            tileHashMap.put(tileList.get(i).text, tileList.get(i));
        }
    }

}

