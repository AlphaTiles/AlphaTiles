package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class Start extends AppCompatActivity
{
    Context context;

    public static final int ALT_COUNT = 3;  // KP

    public static String localAppName; // KP add "public"

    public static TileList tileList; // KP // from aa_gametiles.txt

    public static TileListWithMultipleTypes tileListWithMultipleTypes;

    public static WordList wordList;     // KP  // from aa_wordlist.txt

    public static SyllableList syllableList; // JP // from aa_syllables.txt

    public static KeyList keyList; // KP // from aa_keyboard.txt

    public static GameList gameList; // from aa_games.text

    public static LangInfoList langInfoList; // KP / from aa_langinfo.txt

    public static SettingsList settingsList; // KP // from aa_settings.txt

    public static AvatarNameList nameList; // KP / from aa_names.txt

    // LM / allows us to find() a Tile object using its name
    public static TileHashMap tileHashMap;

    public static WordHashMap wordHashMap;

    public static SyllableHashMap syllableHashMap; //JP

    public static List<String> MULTIFUNCTIONS = new ArrayList<>();

    public static ArrayList<Integer> avatarIdList;
    public static ArrayList<Drawable> avatarJpgList;
    public static SoundPool gameSounds;
    public static int correctSoundID;
    public static int incorrectSoundID;
    public static int correctFinalSoundID;
    public static HashMap<String, Integer> wordAudioIDs;
    public static HashMap<String, Integer> tileAudioIDs;
    public static HashMap<String, Integer> syllableAudioIDs; //JP
//    public static HashMap<String, Integer> instructionAudioIDs;
    public static int correctSoundDuration;
    public static int incorrectSoundDuration;
    public static int correctFinalSoundDuration;
    public static HashMap<String, Integer> wordDurations;
    public static HashMap<String, Integer> tileDurations;
    public static HashMap<String, Integer> syllableDurations;
    public static final ArrayList<String> COLORS = new ArrayList<>();
    public static int totalAudio; //JP: the total number of audio files to be loaded into the soundpool
//    public static HashMap<String, Integer> instructionDurations;

    private static final Logger LOGGER = Logger.getLogger( Start.class.getName() );

    public static Boolean hasTileAudio;
    public static Boolean hasSyllableAudio;
    public static Boolean hasSyllableGames = false;
    public static int after12checkedTrackers;
    Boolean differentiateTypes;

    public static int numberOfAvatars = 12; //default

    public static List<String> CONSONANTS = new ArrayList<>();
    public static List<String> VOWELS = new ArrayList<>();
    public static List<String> CorV = new ArrayList<>();
    public static List<String> TONES = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        totalAudio = 3; // JP: how many total audio files to load
        // will be used in LoadingScreen.java to determine when all audio files have loaded -> advance to ChoosePlayer
        // initialize to 3 for correct, incorrect, and correctFinal sounds

        LOGGER.info("Remember: pre-completed buildLangInfoArray()");
        buildLangInfoArray();
        LOGGER.info("Remember: completed buildLangInfoArray() and buildNamesArray()");

        buildKeysArray();
        LOGGER.info("Remember: completed buildKeysArray()");

        buildSettingsArray();
        LOGGER.info("Remember: completed buildSettingsArray()");

        buildColorsArray();
        LOGGER.info("Remember: completed buildColorsArray()");

        String hasAudioSetting = settingsList.find("Has tile audio");
        if(hasAudioSetting.compareTo("")!=0){
            hasTileAudio = Boolean.parseBoolean(hasAudioSetting);
        }
        else{
            hasTileAudio = false;
        }

        String differentiateTypesSetting = settingsList.find("Differentiates types of multitype symbols");
        if(differentiateTypesSetting.compareTo("") != 0){
            differentiateTypes = Boolean.parseBoolean(differentiateTypesSetting);
        }
        else{
            differentiateTypes = false;
        }

        String after12checkedTrackersSetting = settingsList.find("After 12 checked trackers");
        if (after12checkedTrackersSetting.compareTo("") != 0) {
            after12checkedTrackers = Integer.valueOf(after12checkedTrackersSetting);
        } else {
            after12checkedTrackers = 3;
        }

        //to make syllable audio optional
        String hasSyllableAudioSetting = settingsList.find("Has syllable audio");
        if(hasSyllableAudioSetting.compareTo("")!=0){
            hasSyllableAudio = Boolean.parseBoolean(hasSyllableAudioSetting);
        }
        else{
            hasSyllableAudio = false;
        }

        String customNumOfAvatars = settingsList.find("Number of avatars");
        if (customNumOfAvatars.compareTo("")!=0){
            numberOfAvatars = Integer.parseInt(customNumOfAvatars);
        }
        // otherwise keep 12 default

        LOGGER.info("Remember: completed hasTileAudio & differentiateTypes & hasSyllableAudio");

        // JP: the old constructor is deprecated after API 21, so account for both scenarios
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            gameSounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
        }else{
            gameSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        buildTilesArray();
        for (int d = 0; d < Start.tileList.size(); d++) {
            if (Start.tileList.get(d).tileType.equals("C")) {
                CONSONANTS.add(Start.tileList.get(d).baseTile);
                CorV.add(Start.tileList.get(d).baseTile);
            }
            else if (Start.tileList.get(d).tileType.equals("V")) {
                VOWELS.add(Start.tileList.get(d).baseTile);
                CorV.add(Start.tileList.get(d).baseTile);
            }
            else if (Start.tileList.get(d).tileType.equals("T")) {
                TONES.add(Start.tileList.get(d).baseTile);
            }
        }
        Collections.shuffle(CONSONANTS);
        Collections.shuffle(VOWELS);
        Collections.shuffle(CorV);
        Collections.shuffle(TONES);
        if (hasTileAudio){
            totalAudio = totalAudio + tileList.size();
        }
        LOGGER.info("Remember: completed buildTilesArray()");

        buildGamesArray();
        LOGGER.info("Remember: completed buildGamesArray()");

        buildWordsArray();
        totalAudio = totalAudio + wordList.size();
        populateWordDurations(); /* JP separated from the loop where we populate wordAudioIDs for
        the purpose of making sure durations hashmap will be done even if loading the audio isn't;
        makes null checking simpler
        */
        LOGGER.info("Remember: completed buildWordsArray()");

        if (hasSyllableGames){
            buildSyllablesArray();
            LOGGER.info("Remember: completed buildSyllablesArray()");
        }

        if(hasSyllableAudio){
            totalAudio = totalAudio + syllableList.size();
        }

        if(differentiateTypes){

            if (MULTIFUNCTIONS.isEmpty()) {  //makes sure MULTIFUNCTIONS is populated only once when the app is running
                for (int d = 0; d < Start.tileList.size(); d++) {
                    if (!Start.tileList.get(d).tileTypeB.equals("none")) {
                        MULTIFUNCTIONS.add(Start.tileList.get(d).baseTile);
                    }
                }
            }
        }

        Intent intent = new Intent(this, LoadingScreen.class);

        startActivity(intent);

    }

    private void buildColorsArray() {
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_colors));

        boolean header = true;

        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t",3);
            if (header) {
                header = false;
            } else {
                COLORS.add(thisLineArray[2]);
            }
        }
    }

    //memory leak fix
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameSounds.release();
        gameSounds = null;
    }




    public void populateWordDurations(){
        wordDurations = new HashMap();
        for (Word word: wordList)
        {
            wordDurations.put(word.nationalWord, word.duration + 100);
        }
    }

    public void buildTilesArray() {
        // KP, Oct 2020
        // AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces
        // AH Mar 2021, add new column for audio tile and for upper case tile

        Scanner scanner = new 	Scanner(getResources().openRawResource(R.raw.aa_gametiles));
        boolean header = true;
        tileList = new TileList();

        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t",14);
            if (header) {
                tileList.baseTitle = thisLineArray[0];
                tileList.alt1Title = thisLineArray[1];
                tileList.alt2Title = thisLineArray[2];
                tileList.alt3Title = thisLineArray[3];
                tileList.tileTypeTitle = thisLineArray[4];
                tileList.audioForTileTitle = thisLineArray[5];
                tileList.upperTileTitle = thisLineArray[6];
                tileList.tileTypeBTitle = thisLineArray[7];
                tileList.audioForTileBTitle = thisLineArray[8];
                tileList.tileTypeCTitle = thisLineArray[9];
                tileList.audioForTileCTitle = thisLineArray[10];
                tileList.tileDuration1 = thisLineArray[11];
                tileList.tileDuration2 = thisLineArray[12];
                tileList.tileDuration3 = thisLineArray[13];
                header = false;
            } else {
                Tile tile = new Tile(thisLineArray[0], thisLineArray[1], thisLineArray[2], thisLineArray[3], thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], Integer.parseInt(thisLineArray[11]), Integer.parseInt(thisLineArray[12]), Integer.parseInt(thisLineArray[13]));
                if (!tile.hasNull()) {
                    tileList.add(tile);
                }
            }
        }

        if(differentiateTypes) {

            tileListWithMultipleTypes = new TileListWithMultipleTypes();

            for (Tile tile : tileList) {
                tileListWithMultipleTypes.add(tile.baseTile);

                if (tile.tileTypeB.compareTo("none") != 0) {
                    tileListWithMultipleTypes.add(tile.baseTile + "B");
                }
                if (tile.tileTypeC.compareTo("none") != 0) {
                    tileListWithMultipleTypes.add(tile.baseTile + "C");
                }
            }
        }

        buildTileHashMap();
    }

    public void buildSyllablesArray() {
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_syllables));

        boolean header = true;
        syllableList = new SyllableList();

        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t", 7);
            if (header) {
                syllableList.syllableTitle = thisLineArray[0];
                syllableList.distractorsTitles = new String[]{thisLineArray[1], thisLineArray[2], thisLineArray[3]};
                syllableList.syllableAudioNameTitle = thisLineArray[4];
                syllableList.syllableDurationTitle = thisLineArray[5];
                syllableList.colorTitle = thisLineArray[6];
                header = false;
            } else {
                String[] distractors = {thisLineArray[1], thisLineArray[2], thisLineArray[3]};
                Syllable syllable = new Syllable(thisLineArray[0], distractors, thisLineArray[4], Integer.parseInt(thisLineArray[5]), thisLineArray[6]);
                if (!syllable.hasNull()) {
                    syllableList.add(syllable);
                }
            }
        }

        buildSyllableHashMap();
    }

    private void buildSyllableHashMap() {
        syllableHashMap = new SyllableHashMap();
        for(int i = 0; i < syllableList.size(); i++){
            syllableHashMap.put(syllableList.get(i).syllable, syllableList.get(i));
        }
    }

    public void buildWordsArray() {
        // KP, Oct 2020 (updated by AH to allow for spaces in fields (some common nouns in some languages have spaces)

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_wordlist));
        boolean header = true;
        wordList = new WordList();
        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                wordList.nationalTitle = thisLineArray[0];
                wordList.localTitle = thisLineArray[1];
                wordList.durationTitle = thisLineArray[2];
                wordList.mixedDefsTitle = thisLineArray[3];
                wordList.adjustment = thisLineArray[4];
                header = false;
            } else {
                Word word = new Word(thisLineArray[0], thisLineArray[1], Integer.parseInt(thisLineArray[2]), thisLineArray[3], thisLineArray[4]);
                if (!word.hasNull()) {
                    wordList.add(word);
                }
            }
        }

        buildWordHashMap();
    }

    public void buildKeysArray() {
        // KP, Oct 2020
        // AH, Nov 2020, updates to add second column (color theme)
        // AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_keyboard)); // prep scan of aa_keyboard.txt
        boolean header = true;
        keyList = new KeyList();
        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                keyList.keysTitle = thisLineArray[0];
                keyList.colorTitle = thisLineArray[1];
                header = false;
            } else {
                Key key = new Key(thisLineArray[0], thisLineArray[1]);
                if (!key.hasNull()) {
                    keyList.add(key);
                }
            }
        }
    }

    public void buildGamesArray() {

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_games)); // prep scan of aa_games.txt
        boolean header = true;
        gameList = new GameList();
        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                gameList.gameNumber = thisLineArray[0];
                gameList.gameCountry = thisLineArray[1];
                gameList.gameLevel = thisLineArray[2];
                gameList.gameColor = thisLineArray[3];
                gameList.gameInstrLabel = thisLineArray[4];
                gameList.gameInstrDuration = thisLineArray[5];
                gameList.gameMode = thisLineArray[6];
                header = false;
            } else {
                Game game = new Game(thisLineArray[0], thisLineArray[1],thisLineArray[2], thisLineArray[3],thisLineArray[4], thisLineArray[5], thisLineArray[6]);
                if (!game.hasNull()) {
                    gameList.add(game);
                }
                if (thisLineArray[6].equals("S")){ //JP
                    hasSyllableGames = true;
                }
            }
        }
    }

    public void buildSettingsArray() {

        boolean header = true;
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_settings)); // prep scan of aa_settings.txt

        settingsList = new SettingsList();
        while (scanner.hasNext()) {
            if (scanner.hasNextLine()) {
                if (header) {
                    settingsList.title = scanner.nextLine();
                    header = false;
                } else {
                    String thisLine = scanner.nextLine();
                    String[] thisLineArray = thisLine.split("\t");
                    settingsList.put(thisLineArray[0], thisLineArray[1]);
                }
            }
        }

    }

    public void buildLangInfoArray() {

        boolean header = true;
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_langinfo)); // prep scan of aa_langinfo.txt

        langInfoList = new LangInfoList();
        while (scanner.hasNext())
        {
            if (scanner.hasNextLine())
            {
                if (header)
                {
                    langInfoList.title = scanner.nextLine();
                    header = false;
                }
                else
                {
                    String thisLine = scanner.nextLine();
                    String[] thisLineArray = thisLine.split("\t");
                    langInfoList.put(thisLineArray[0], thisLineArray[1]);
                }
            }
        }

        localAppName = langInfoList.find("Game Name");

        String localWordForName = langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            buildNamesArray();
        }

    }

    public void buildNamesArray() {

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_names)); // prep scan of aa_names.txt

        boolean header = true;

        nameList = new AvatarNameList();
        while (scanner.hasNext())
        {
            if (scanner.hasNextLine())
            {
                if (header)
                {
                    nameList.title = scanner.nextLine();
                    header = false;
                }
                else
                {
                    String thisLine = scanner.nextLine();
                    String[] thisLineArray = thisLine.split("\t");
                    nameList.add(thisLineArray[1]);
                }
            }
        }

        localAppName = langInfoList.find("Game Name");

    }

    public void buildTileHashMap(){
        tileHashMap = new TileHashMap();
        for(int i = 0; i < tileList.size(); i++){
            tileHashMap.put(tileList.get(i).baseTile, tileList.get(i));
        }
    }

    public void buildWordHashMap(){
        wordHashMap = new WordHashMap();
        for(int i = 0; i < wordList.size(); i++){
            wordHashMap.put(wordList.get(i).nationalWord, wordList.get(i));
        }
    }

    public class Word {
        public String nationalWord;
        public String localWord;
        public int duration;
        public String mixedDefs;
        public String adjustment;

        public Word(String nationalWord, String localWord, int duration, String mixedDefs, String adjustment) {
            this.nationalWord = nationalWord;
            this.localWord = localWord;
            this.duration = duration;
            this.mixedDefs = mixedDefs;
            this.adjustment = adjustment;
        }

        public boolean hasNull() {
            return nationalWord == null || localWord == null || mixedDefs == null || adjustment == null;
        }
    }

    public class Tile {
        public String baseTile;
        public String[] altTiles;
        public String tileType;
        public String audioForTile;
        public String upperTile;
        public String tileTypeB;
        public String audioForTileB;
        public String tileTypeC;
        public String audioForTileC;
        public int tileDuration1;
        public int tileDuration2;
        public int tileDuration3;
        public Tile(String baseTile, String alt1Tile, String alt2Tile, String alt3Tile, String tileType, String audioForTile, String upperTile, String tileTypeB, String audioForTileB, String tileTypeC, String audioForTileC, int tileDuration1, int tileDuration2, int tileDuration3) {
            this.baseTile = baseTile;
            altTiles = new String[ALT_COUNT];
            altTiles[0] = alt1Tile;
            altTiles[1] = alt2Tile;
            altTiles[2] = alt3Tile;
            this.tileType = tileType;
            this.audioForTile = audioForTile;
            this.upperTile = upperTile;
            this.tileTypeB = tileTypeB;
            this.audioForTileB = audioForTileB;
            this.tileTypeC = tileTypeC;
            this.audioForTileC = audioForTileC;
            this.tileDuration1 = tileDuration1;
            this.tileDuration2 = tileDuration2;
            this.tileDuration3 = tileDuration3;
        }

        public boolean hasNull() {
            if (baseTile == null || tileType == null || audioForTile == null || upperTile == null || tileTypeB == null || audioForTileB == null || tileTypeC == null || audioForTileC == null)
                return true;
            for (String tile : altTiles)
                if (tile == null)
                    return true;
            return false;
        }
    }

    public class Key {
        public String baseKey;
        public String keyColor;

        public Key(String baseKey, String keyColor) {
            this.baseKey = baseKey;
            this.keyColor = keyColor;
        }

        public boolean hasNull() {
            return baseKey == null || keyColor == null;
        }
    }

    public class Game {
        public String gameNumber;
        public String gameCountry;
        public String gameLevel;
        public String gameColor;
        public String gameInstrLabel;
        public String gameInstrDuration;
        public String gameMode; //JP : for syllable or tile mode

        public Game(String gameNumber, String gameCountry, String gameLevel, String gameColor, String gameInstrLabel, String gameInstrDuration, String gameMode) {
            this.gameNumber = gameNumber;
            this.gameCountry = gameCountry;
            this.gameLevel = gameLevel;
            this.gameColor = gameColor;
            this.gameInstrLabel = gameInstrLabel;
            this.gameInstrDuration = gameInstrDuration;
            this.gameMode = gameMode;
        }

        public boolean hasNull() {
            return gameNumber == null || gameCountry == null|| gameLevel == null|| gameColor == null || gameInstrLabel == null|| gameInstrDuration == null || gameMode == null;
        }
    }

    public class WordList extends ArrayList<Word> {
        public String nationalTitle;	// e.g. languages like English or Spanish (LWCs = Languages of Wider Communication)
        public String localTitle;	// e.g. LOPS (language of play) like Me'phaa, Kayan or Romani Gabor
        public String durationTitle;	// the length of the clip in ms, relevant only if set to use SoundPool
        public String mixedDefsTitle;	// for languages with multi-function symbols (e.g. in the word <niwan'>, the first |n| is a consontant and the second |n| is a nasality indicator
        public String adjustment;	// a font-specific reduction in size for words with longer pixel width

        public int returnGroupOneCount(String someGameTile) {
            // Group One = words that START with the active tile

            ArrayList<String> parsedWordArrayFinal;
            String wordInitialTile;
            String wordInitialTileType;
            String someGameTileType;
            String someGameTileWithoutSuffix;

            someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
            if (someGameTileType.compareTo("B") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
            } else if (someGameTileType.compareTo("C") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
            } else {
                someGameTileWithoutSuffix = someGameTile;
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
            }

            int tilesCount = 0;

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);

                wordInitialTile = parsedWordArrayFinal.get(0);

                if (wordInitialTile != null) {

                    if(differentiateTypes){//checking if both tile and type match
                        if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                            wordInitialTileType = Start.tileList.getInstanceTypeForMixedTile(0, get(i).nationalWord);
                        }
                        else{//not dealing with a multifunction symbol
                            wordInitialTileType = tileHashMap.find(wordInitialTile).tileType;
                        }

                        if(wordInitialTile.equals(someGameTileWithoutSuffix) && someGameTileType.equals(wordInitialTileType)){
                            tilesCount++;
                        }

                    }
                    else {//Not differentiating types, only matching tile to tile
                        if (parsedWordArrayFinal.get(0).equals(someGameTile)) {
                            tilesCount++;
                        }
                    }

                }
            }

            return tilesCount;

        }

        public String[][] returnGroupOneWords(String someGameTile, int tilesCount) {
            // Group One = words that START with the active tile

            ArrayList<String> parsedWordArrayFinal;
            int hitsCounter = 0;

            String[][] wordsWithNonInitialTiles = new String [tilesCount][2];

            String wordInitialTile;
            String wordInitialTileType;
            String someGameTileType;
            String someGameTileWithoutSuffix;


            someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
            if (someGameTileType.compareTo("B") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
            } else if (someGameTileType.compareTo("C") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
            } else {
                someGameTileWithoutSuffix = someGameTile;
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
            }

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);

                wordInitialTile = parsedWordArrayFinal.get(0);

                if (wordInitialTile != null) {

                    if(differentiateTypes){//checking if both tile and type match
                        if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                            wordInitialTileType = Start.tileList.getInstanceTypeForMixedTile(0, get(i).localWord);
                        }
                        else{//not dealing with a multifunction symbol
                            wordInitialTileType = tileHashMap.find(wordInitialTile).tileType;
                        }

                        if(wordInitialTile.equals(someGameTileWithoutSuffix) && someGameTileType.equals(wordInitialTileType)){
                            wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
                            wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
                            hitsCounter++;
                        }

                    }
                    else {//Not differentiating types, only matching tile to tile
                        if (parsedWordArrayFinal.get(0).equals(someGameTile)) {
                            wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
                            wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
                            hitsCounter++;
                        }

                    }
                }
            }

            return wordsWithNonInitialTiles;

        }

        public int returnGroupTwoCount(String someGameTile) {
            // Group Two = words that contain the active tile non-initially (but excluding initially)

            ArrayList<String> parsedWordArrayFinal;
            String tileInFocus;
            String tileInFocusType;
            String someGameTileType;
            String someGameTileWithoutSuffix;

            someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
            if (someGameTileType.compareTo("B") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
            } else if (someGameTileType.compareTo("C") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
            } else {
                someGameTileWithoutSuffix = someGameTile;
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
            }

            int tilesCount = 0;

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);

                for (int k = 1; k < parsedWordArrayFinal.size(); k++) {
                    tileInFocus = parsedWordArrayFinal.get(k);

                    if (tileInFocus != null) {

                        if(differentiateTypes){//checking if both tile and type match
                            if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                                tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, get(i).nationalWord);
                            }
                            else{//not dealing with a multifunction symbol
                                tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                            }

                            if(tileInFocus.equals(someGameTileWithoutSuffix) && someGameTileType.equals(tileInFocusType)){
                                tilesCount++;
                            }

                        }
                        else {//Not differentiating types, only matching tile to tile
                            if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                                tilesCount++;
                            }
                        }
                    }
                }
            }

            return tilesCount;

        }

        public String[][] returnGroupTwoWords(String someGameTile, int tilesCount) {
            // Group Two = words that contain the active tile non-initially (but excluding initially)

            ArrayList<String> parsedWordArrayFinal;
            int hitsCounter = 0;

            String[][] wordsWithNonInitialTiles = new String [tilesCount][2];

            String tileInFocus;
            String tileInFocusType;
            String someGameTileType;
            String someGameTileWithoutSuffix;


            someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
            if (someGameTileType.compareTo("B") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
            } else if (someGameTileType.compareTo("C") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
            } else {
                someGameTileWithoutSuffix = someGameTile;
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
            }

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);

                for (int k = 1; k < parsedWordArrayFinal.size(); k++) {
                    tileInFocus = parsedWordArrayFinal.get(k);

                    if (tileInFocus != null) {

                        if(differentiateTypes){//checking if both tile and type match
                            if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                                tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, get(i).localWord);
                            }
                            else{//not dealing with a multifunction symbol
                                tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                            }

                            if(tileInFocus.equals(someGameTileWithoutSuffix) && someGameTileType.equals(tileInFocusType)){
                                wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
                                wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
                                hitsCounter++;
                            }

                        }
                        else {//Not differentiating types, only matching tile to tile
                            if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                                wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
                                wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
                                hitsCounter++;
                            }
                        }
                    }
                }
            }

            return wordsWithNonInitialTiles;

        }

        public int returnGroupThreeCount(String someGameTile) {
            // Group Three = words containing the active tile anywhere (initial and/or non-initial)

            ArrayList<String> parsedWordArrayFinal;
            String tileInFocus;
            String tileInFocusType;
            String someGameTileType;
            String someGameTileWithoutSuffix;

            someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
            if (someGameTileType.compareTo("B") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
            } else if (someGameTileType.compareTo("C") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
            } else {
                someGameTileWithoutSuffix = someGameTile;
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
            }

            int tilesCount = 0;

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);

                for (int k = 0; k < parsedWordArrayFinal.size(); k++) {
                    tileInFocus = parsedWordArrayFinal.get(k);

                    if (tileInFocus != null) {

                        if(differentiateTypes){//checking if both tile and type match
                            if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                                tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, get(i).nationalWord);
                            }
                            else{//not dealing with a multifunction symbol
                                tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                            }

                            if(tileInFocus.equals(someGameTileWithoutSuffix) && someGameTileType.equals(tileInFocusType)){
                                tilesCount++;
                            }

                        }
                        else {//Not differentiating types, only matching tile to tile
                            if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                                tilesCount++;
                            }
                        }
                    }
                }
            }

            return tilesCount;

        }

        public String[][] returnGroupThreeWords(String someGameTile, int tilesCount) {
            // Group Three = words containing the active tile anywhere (initial and/or non-initial)

            ArrayList<String> parsedWordArrayFinal;
            int hitsCounter = 0;

            String[][] wordsContainingSomeGameTile = new String [tilesCount][2];

            String tileInFocus;
            String tileInFocusType;
            String someGameTileType;
            String someGameTileWithoutSuffix;


            someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
            if (someGameTileType.compareTo("B") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
            } else if (someGameTileType.compareTo("C") == 0) {
                someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
            } else {
                someGameTileWithoutSuffix = someGameTile;
                someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
            }

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);

                for (int k = 0; k < parsedWordArrayFinal.size(); k++) {
                    tileInFocus = parsedWordArrayFinal.get(k);

                    if (tileInFocus != null) {

                        if(differentiateTypes){//checking if both tile and type match
                            if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                                tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, get(i).nationalWord);
                            }
                            else{//not dealing with a multifunction symbol
                                tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                            }

                            if(tileInFocus.equals(someGameTileWithoutSuffix) && someGameTileType.equals(tileInFocusType)){
                                wordsContainingSomeGameTile[hitsCounter][0] = get(i).nationalWord;
                                wordsContainingSomeGameTile[hitsCounter][1] = get(i).localWord;
                                hitsCounter++;
                            }

                        }
                        else {//Not differentiating types, only matching tile to tile
                            if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                                wordsContainingSomeGameTile[hitsCounter][0] = get(i).nationalWord;
                                wordsContainingSomeGameTile[hitsCounter][1] = get(i).localWord;
                                hitsCounter++;
                            }
                        }
                    }
                }
            }

            return wordsContainingSomeGameTile;


        }

        public String stripInstructionCharacters(String localWord) {
            // The period instructs the parseWord method to force a tile break
            String newString = localWord.replaceAll("[.]", "");
            return newString;
        }

        public int returnPositionInWordList(String someLWCWord) {

            int wordPosition = 0;
            for (int i = 0; i < size(); i++) {

                if (get(i).nationalWord.equals(someLWCWord)) {
                    wordPosition = i;
                }
            }

            return wordPosition;

        }

        public ArrayList<String[]> returnFourWords(String wordInLOP, String wordInLWC, String refTile, int challengeLevel, String refType, String choiceType){

        //}, float adjustmentCutoff) {

            ArrayList<String[]> fourChoices = new ArrayList();
            ArrayList<String[]> easyWords = new ArrayList();        // words that do not begin with same tile or with distractor tile
            ArrayList<String[]> moderateWords = new ArrayList();    // words that begin with distractor tiles
            ArrayList<String[]> hardWords = new ArrayList();        // words that begin with the same tile (but excluding wordInLOP
            ArrayList<String> parsedWordArrayFinal;

            // Note that the following are four non-overlapping groups: easyWords, moderateWords, hardWords, wordInLOP

            int correctRow = returnPositionInWordList(wordInLOP);
            String partA = wordInLWC;
            String partB = wordInLOP;
            String[] wordEntry = new String [] {partA, partB};
            fourChoices.add(wordEntry);

//            LOGGER.info("Remember refTile = " + refTile);
//            LOGGER.info("Remember wordInLOP / wordInLWC = " + wordInLOP + " : " + wordInLWC);
//            LOGGER.info("Remember challengeLevel = " + challengeLevel);
//            LOGGER.info("Remember choiceType = " + choiceType);

            String alt1lower = Start.tileList.get(Start.tileList.returnPositionInAlphabet(refTile)).altTiles[0];
            String alt2lower = Start.tileList.get(Start.tileList.returnPositionInAlphabet(refTile)).altTiles[1];
            String alt3lower = Start.tileList.get(Start.tileList.returnPositionInAlphabet(refTile)).altTiles[2];

            String alt1;
            String alt2;
            String alt3;

            if (refType.equals("TILE_UPPER")) {

                alt1 = Start.tileList.get(Start.tileList.returnPositionInAlphabet(alt1lower)).upperTile;
                alt2 = Start.tileList.get(Start.tileList.returnPositionInAlphabet(alt2lower)).upperTile;
                alt3 = Start.tileList.get(Start.tileList.returnPositionInAlphabet(alt3lower)).upperTile;

            } else {

                alt1 = alt1lower;
                alt2 = alt2lower;
                alt3 = alt3lower;

            }

//            LOGGER.info("Remember alt1 / alt2 / alt3 = " + alt1 + " : " + alt2 + " : " + alt3);

            for (int i = 0; i < wordList.size(); i++) {

                String activeWord = Start.wordList.get(i).localWord;
                parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(activeWord);
                String activeTileLower = parsedWordArrayFinal.get(0);
                String activeTile;

                if (refType.equals("TILE_UPPER")) {

                    activeTile = Start.tileList.get(Start.tileList.returnPositionInAlphabet(activeTileLower)).upperTile;

                } else {

                    activeTile = activeTileLower;

                }

                if (!activeTile.equals(refTile) && !activeTile.equals(alt1) && !activeTile.equals(alt2) && !activeTile.equals(alt3)) {
                    partA = Start.wordList.get(i).nationalWord;
                    partB = Start.wordList.get(i).localWord;
                    if (!wordInLOP.equals(partB)) {
                        wordEntry = new String[]{partA, partB};
                        easyWords.add(wordEntry);
                    }
                }

                if (activeTile.equals(alt1) || activeTile.equals(alt2) || activeTile.equals(alt3)) {
                    partA = Start.wordList.get(i).nationalWord;
                    partB = Start.wordList.get(i).localWord;
                    if (!wordInLOP.equals(partB)) {
                        wordEntry = new String [] {partA, partB};
                        moderateWords.add(wordEntry);
                    }
                }

                if (activeTile.equals(refTile) && !activeWord.equals(wordInLOP)) {
                    partA = Start.wordList.get(i).nationalWord;
                    partB = Start.wordList.get(i).localWord;
                    if (!wordInLOP.equals(partB)) {
                        wordEntry = new String [] {partA, partB};
                        hardWords.add(wordEntry);
                    }
                }

            }

//            LOGGER.info("Remember easyWords.size() = " + easyWords.size());
//            LOGGER.info("Remember moderateWords.size() = " + moderateWords.size());
//            LOGGER.info("Remember hardWords.size() = " + hardWords.size());

            Collections.shuffle(easyWords);
            Collections.shuffle(moderateWords);
            Collections.shuffle(hardWords);

            if (challengeLevel == 1) {
                // use easy words
                // ASSUMING that there will always be three words that do not start with refTile or distractor tiles
                // since problematic tiles may not be included in distractor tiles for certain languages, always need to check using while loop

                for (int i = 0; i < 3; i++) {
                    //JP edits to fix c vs ch issue:
                    if (refType.equals("TILE_UPPER") || refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) { //conditions where c vs ch conflicts can occur
                        String[] possibleWordArr;
                        String possibleWord;
                        String firstTile;

                        possibleWordArr = easyWords.get(i);

                        possibleWord = possibleWordArr[1]; //should be LOP word
                        parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                        firstTile = parsedWordArrayFinal.get(0);

                        while ((Character.toLowerCase(firstTile.charAt(0)) == Character.toLowerCase(refTile.charAt(0))) && (firstTile.length() > refTile.length())
                                || fourChoices.contains(possibleWordArr)) { //loops continues until a non-conflicting tile is chosen
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());

                            possibleWordArr = easyWords.get(rand1);
                            possibleWord = possibleWordArr[1];
                            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                            firstTile = parsedWordArrayFinal.get(0);
                        }

                        fourChoices.add(possibleWordArr);
                    } else{
                        fourChoices.add(easyWords.get(i));
                    }
                }

            }

            if (challengeLevel == 2) {
                // use moderate words and if the supply runs out use easy words

                for (int i = 0; i < 3; i++) {
                    //JP: edits to try to fix c vs ch issue;
                    if (refType.equals("TILE_UPPER") || refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) { //conditions where c vs ch conflicts can occur
                        String[] possibleWordArr;
                        String possibleWord;
                        String firstTile;
                        if (moderateWords.size() > i) {
                            //first try to simply get a moderate word if there are enough moderate wordds
                            possibleWordArr = moderateWords.get(i);
                        }else {
                            //if there are not enough moderate words go straight to trying a random easy word
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());

                            possibleWordArr = easyWords.get(rand1);
                        }
                        possibleWord = possibleWordArr[1]; //should be LOP word
                        parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                        firstTile = parsedWordArrayFinal.get(0); //should be tile

                        //then test whether this possible word is problematic, and if so, replace it with a (different) random easy word.
                        //the random easy word also needs to be tested, since some languages may have instances where tiles "c" and "ch" both exist
                        //but one is not listed as a distractor tile of the other
                        while ((Character.toLowerCase(firstTile.charAt(0)) == Character.toLowerCase(refTile.charAt(0))) && (firstTile.length() > refTile.length())) {
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());

                            possibleWordArr = easyWords.get(rand1);
                            possibleWord = possibleWordArr[1];
                            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                            firstTile = parsedWordArrayFinal.get(0);
                        }

                        //after those tests, the possible word has been validated and can be added to the answer choices
                        fourChoices.add(possibleWordArr);
                    } //inner if
                    else { //when ref is a word or picture
                        if (moderateWords.size() > i) {
                            fourChoices.add(moderateWords.get(i));
                        } else {
                            fourChoices.add(easyWords.get(i - moderateWords.size()));
                        }
                    }
                } //for loop
            } //level

            if (challengeLevel == 3) {
                // use hard words and if the supply runs out use moderate words and if the supply runs out use easy words

                for (int i = 0; i < 3; i++) {
                    if (hardWords.size() > i) {

                        fourChoices.add(hardWords.get(i));

                    } else {
                        if (moderateWords.size() > (i - hardWords.size())) {

                            fourChoices.add(moderateWords.get(i - hardWords.size()));

                        } else {

                            fourChoices.add(easyWords.get(i - hardWords.size() - moderateWords.size()));

                        }
                    }
                }

            }
//            LOGGER.info("Remember fourChoices.get(0)[1] = " + fourChoices.get(0)[1]);
//            LOGGER.info("Remember fourChoices.get(1)[1] = " + fourChoices.get(1)[1]);
//            LOGGER.info("Remember fourChoices.get(2)[1] = " + fourChoices.get(2)[1]);
//            LOGGER.info("Remember fourChoices.get(3)[1] = " + fourChoices.get(3)[1]);

            Collections.shuffle(fourChoices);
            return fourChoices;

        }

    }

    public class Syllable{
        public String syllable;
        public String[] distractors;
        public String syllableAudioName;
        public int syllableDuration;
        public String color;


        public Syllable(String syllable, String[] distractors, String syllableAudioName, int syllableDuration, String color) {
            this.syllable = syllable;
            this.distractors = distractors;
            this.syllableAudioName = syllableAudioName;
            this.syllableDuration = syllableDuration;
            this.color = color;
        }

        public boolean hasNull() {
            return syllable == null || distractors[0] == null || distractors[1] == null || distractors[2] == null || syllableAudioName == null || color == null;
        }
    }

    public class SyllableList extends ArrayList<Syllable>{
        public String syllableTitle;
        public String[] distractorsTitles;
        public String syllableAudioNameTitle;
        public String syllableDurationTitle;
        public String colorTitle;

        public ArrayList<String> parseWordIntoSyllables(String parseMe) {
            String[] parsedWordArrayTemp = parseMe.split("[\\s.-]"); //period, dash, space
            return new ArrayList<String>(Arrays.asList(parsedWordArrayTemp));
        }

        public String returnRandomCorrespondingSyllable(String correctSyll) {

            String wrongTile = "";
            Random rand = new Random();

            for (int i = 0; i < size(); i++) {
                if (get(i).syllable.equals(correctSyll)) {
                    int randomNum = rand.nextInt(get(i).distractors.length);
                    wrongTile = get(i).distractors[randomNum];
                    break;
                }
            }

            return wrongTile;

        }

        public ArrayList<String[]> returnFourWords(String refTile, int chall){
            ArrayList<String> potentialWordParsed;
            String potentialWord;
            String natWord;
            ArrayList<String[]> fourWords = new ArrayList<>();
            Set<String> trackWords = new HashSet<>(); //used to prevent repeats
            Syllable refSyllable = syllableHashMap.find(refTile);
            Random rand = new Random();
            boolean correctRep = false;

            // get a word that starts with the refTile (syllable)
            while (!correctRep){
                int randomNum = rand.nextInt(wordList.size());
                potentialWord = wordList.get(randomNum).localWord;
                natWord = wordList.get(randomNum).nationalWord;
                potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                if (potentialWordParsed.get(0).equals(refTile)){
                    fourWords.add(new String[]{natWord, potentialWord});
                    trackWords.add(potentialWord);
                    correctRep = true;
                }
            }


            if (chall == 1){ //easy words = not same initial syllable and no distractor syllables word-initially
                while (fourWords.size() < 4){
                    int randomNum = rand.nextInt(wordList.size());
                    potentialWord = wordList.get(randomNum).localWord;
                    natWord = wordList.get(randomNum).nationalWord;
                    potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                    if (!potentialWordParsed.get(0).equals(refTile) && !potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[0]) && !potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[1]) && !potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[2])){
                        String[] tileEntry = new String[]{natWord, potentialWord};
                        if (!trackWords.contains(potentialWord)) {
                            trackWords.add(potentialWord);
                            fourWords.add(tileEntry);
                        }
                    }
                }
            }else if (chall == 2){ // medium words = start w/distractor syllables
                int count = 0;
                while (fourWords.size() < 4 && count < wordList.size()){
                    int randomNum = rand.nextInt(wordList.size());
                    potentialWord = wordList.get(randomNum).localWord;
                    natWord = wordList.get(randomNum).nationalWord;
                    potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                    if (!potentialWordParsed.get(0).equals(refTile) && (potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[0]) || potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[1]) || potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[2]))){
                        String[] tileEntry = new String[]{natWord, potentialWord};
                        if (!trackWords.contains(potentialWord)) {
                            trackWords.add(potentialWord);
                            fourWords.add(tileEntry);
                        }
                    }
                    count++;
                }
                while (fourWords.size() < 4){ // maybe this is an infinite loop - change to allow
                    // any word that doesn't begin with correct syll
                    int randomNum = rand.nextInt(wordList.size());
                    potentialWord = wordList.get(randomNum).localWord;
                    natWord = wordList.get(randomNum).nationalWord;
                    potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                    // if (!potentialWordParsed.get(0).equals(refTile) && (potentialWord.charAt(0)
                    //                            == refTile.charAt(0)))
                    if (!potentialWordParsed.get(0).equals(refTile)){
                        String[] tileEntry = new String[]{natWord, potentialWord};
                        if (!trackWords.contains(potentialWord)) {
                            trackWords.add(potentialWord);
                            fourWords.add(tileEntry);
                        }
                    }
                }
            }

            return fourWords;
        }

        public ArrayList<String[]> returnFourSylls(String refTile, int chall){
            ArrayList<String[]> fourSylls = new ArrayList<>();
            Syllable refSyllable = syllableHashMap.find(refTile);
            Set<String> trackSylls = new HashSet<>(); //used to prevent repeats
            String potentialSyll;
            String potentialSyllAud;
            Random rand = new Random();
            fourSylls.add(new String[] {refSyllable.syllableAudioName, refSyllable.syllable}); // correct
            trackSylls.add(refSyllable.syllable);
            if (chall == 1){ //random wrong syllables
                while (fourSylls.size() < 4){
                    int randomNum = rand.nextInt(syllableList.size());
                    potentialSyll = syllableList.get(randomNum).syllable;
                    potentialSyllAud = syllableList.get(randomNum).syllableAudioName;
                    if (!potentialSyll.equals(refTile) && !potentialSyll
                            .equals(refSyllable.distractors[0]) && !potentialSyll
                            .equals(refSyllable.distractors[1]) && !potentialSyll
                            .equals(refSyllable.distractors[2]) && !trackSylls.contains(potentialSyll)){
                        trackSylls.add(potentialSyll);
                        fourSylls.add(new String[]{potentialSyllAud, potentialSyll});
                    }
                }
            }else if (chall == 2){ //distractor syllables
                if (!trackSylls.contains(refSyllable.distractors[0])){
                    trackSylls.add(refSyllable.distractors[0]);
                    fourSylls.add(new String [] {refSyllable.syllableAudioName, refSyllable.distractors[0]});
                }
                if (!trackSylls.contains(refSyllable.distractors[1])){
                    trackSylls.add(refSyllable.distractors[1]);
                    fourSylls.add(new String[] {refSyllable.syllableAudioName, refSyllable.distractors[1]});
                }
                if (!trackSylls.contains(refSyllable.distractors[2])){
                    trackSylls.add(refSyllable.distractors[2]);
                    fourSylls.add(new String[] {refSyllable.syllableAudioName, refSyllable.distractors[2]});
                }
                while (fourSylls.size() < 4){
                    int randomNum = rand.nextInt(syllableList.size());
                    potentialSyll = syllableList.get(randomNum).syllable;
                    potentialSyllAud = syllableList.get(randomNum).syllableAudioName;
                    if (!potentialSyll.equals(refTile) && !trackSylls.contains(potentialSyll)){
                        trackSylls.add(potentialSyll);
                        fourSylls.add(new String[]{potentialSyllAud, potentialSyll});
                    }
                }
            }
            return (ArrayList<String[]>) fourSylls;
        }

        public int returnPositionInSyllList(String someGameTile) {

            int alphabetPosition = 0;
            for (int i = 0; i < size(); i++) {

                if (get(i).syllable.equals(someGameTile)) {
                    alphabetPosition = i;
                }
            }

            return alphabetPosition;

        }

    }

    public class TileList extends ArrayList<Tile> {
        public String baseTitle;
        public String alt1Title;
        public String alt2Title;
        public String alt3Title;
        public String tileTypeTitle;
        public String audioForTileTitle;
        public String upperTileTitle;
        public String tileTypeBTitle;
        public String audioForTileBTitle;
        public String tileTypeCTitle;
        public String audioForTileCTitle;
        public String tileDuration1;
        public String tileDuration2;
        public String tileDuration3;

        public ArrayList<String> parseWordIntoTiles(String parseMe) {
            // Updates by KP, Oct 2020
            // AH, Nov 2020, extended to check up to four characters in a game tile

            ArrayList<String> parsedWordArrayTemp = new ArrayList();

            int charBlock;
            String next1; // the next one character from the string
            String next2; // the next two characters from the string
            String next3; // the next three characters from the string
            String next4; // // the next four characters from the string

            int i; // counter to iterate through the characters of the analyzed word
            int k; // counter to scroll through all game tiles for hits on the analyzed character(s) of the word string

            for (i = 0; i < parseMe.length(); i++) {

                // Create blocks of the next one, two, three and four Unicode characters for analysis
                next1 = parseMe.substring(i, i + 1);

                if (i < parseMe.length() - 1) {
                    next2 = parseMe.substring(i, i + 2);
                } else {
                    next2 = "XYZXYZ";
                }

                if (i < parseMe.length() - 2) {
                    next3 = parseMe.substring(i, i + 3);
                } else {
                    next3 = "XYZXYZ";
                }

                if (i < parseMe.length() - 3) {
                    next4 = parseMe.substring(i, i + 4);
                } else {
                    next4 = "XYZXYZ";
                }

                // See if the blocks of length one, two, three or four Unicode characters matches game tiles
                // Choose the longest block that matches a game tile and add that as the next segment in the parsed word array
                charBlock = 0;
                for (k = 0; k < size(); k++) {

//                    LOGGER.info("Remember: tileList.get(" + k + ").baseTile = " +  tileList.get(k).baseTile);

                    if (next1.equals(tileList.get(k).baseTile) && charBlock == 0) {
                        // If charBlock is already assigned 2 or 3 or 4, it should not overwrite with 1
//                        LOGGER.info("Remember: next1 = " + next1);
                        charBlock = 1;
                    }
                    if (next2.equals(tileList.get(k).baseTile) && charBlock != 3 && charBlock != 4) {
                        // The value 2 can overwrite 1 but it can't overwrite 3 or 4
//                        LOGGER.info("Remember: next2 = " + next2);
                        charBlock = 2;
                    }
                    if (next3.equals(tileList.get(k).baseTile) && charBlock != 4) {
                        // The value 3 can overwrite 1 or 2 but it can't overwrite 4
//                        LOGGER.info("Remember: next3 = " + next3);
                        charBlock = 3;
                    }
                    if (next4.equals(tileList.get(k).baseTile)) {
                        // The value 4 can overwrite 1 or 2 or 3
                        charBlock = 4;
                    }
                    if ((tileList.get(k).baseTile == null && k > 0)) {
                        k = tileList.size();
                    }
                }

                // Add the selected game tile (the longest selected from the previous loop) to the parsed word array
                switch (charBlock) {
                    case 1:
                        parsedWordArrayTemp.add(next1);
                        break;
                    case 2:
                        parsedWordArrayTemp.add(next2);
                        i++;
                        break;
                    case 3:
                        parsedWordArrayTemp.add(next3);
                        i += 2;
                        break;
                    case 4:
                        parsedWordArrayTemp.add(next4);
                        i += 3;
                        break;
                    default:
                        break;
                }

            }

//            for (int q =0; q < parsedWordArrayTemp.size(); q++) {
//                LOGGER.info("Remember parsedWordArrayTemp.get(" + q + ") = " + parsedWordArrayTemp.get(q));
//            }

            return parsedWordArrayTemp;
        }

        public String returnNextAlphabetTile(String oldTile) {

            String nextTile = "";
            for (int i = 0; i < size(); i++) {
                if (get(i).baseTile.equals(oldTile)) {
                    if (i < (size() - 1)) {
                        nextTile = get(i + 1).baseTile;
                    } else// if (i == size() - 1) {
                        nextTile = get(0).baseTile;
                }
            }

            return nextTile;

        }

        public String returnPreviousAlphabetTile(String oldTile) {

            String previousTile = "";
            for (int i = size() - 1; i >= 0; i--) {

                if (get(i).baseTile.equals(oldTile)) {
                    if (i > 0) {
                        previousTile = get(i - 1).baseTile;
                    } else// if (i == 0) {
                        previousTile = get(size() - 1).baseTile;
                }
            }

            return previousTile;

        }

        public int returnPositionInAlphabet(String someGameTile) {

            int alphabetPosition = 0;
            for (int i = 0; i < size(); i++) {

                if (get(i).baseTile.equals(someGameTile)) {
                    alphabetPosition = i;
                }
                if (get(i).upperTile.equals(someGameTile)) {
                    alphabetPosition = i;
                }
            }

            return alphabetPosition;

        }

        public String returnRandomCorrespondingTile(String correctTile) {

            String wrongTile = "";
            Random rand = new Random();

            for (int i = 0; i < size(); i++) {
                if (get(i).baseTile.equals(correctTile)) {
                    int randomNum = rand.nextInt(get(i).altTiles.length);
                    wrongTile = get(i).altTiles[randomNum];
                    break;
                }
            }

            return wrongTile;

        }

        public ArrayList<String[]> returnFourTiles(String correctTile, int challengeLevelX, String choiceType) {

//            LOGGER.info("Remember: M");
            ArrayList<String[]> fourChoices = new ArrayList();

//            LOGGER.info("Remember: M2: correctTile = " + correctTile);
            int correctRow = returnPositionInAlphabet(correctTile);

//            LOGGER.info("Remember: N");
            String partA = Start.tileList.get(correctRow).audioForTile;
            String partB = null;
            if (choiceType.equals("TILE_LOWER")) {
                partB = Start.tileList.get(correctRow).baseTile;
            }
            if (choiceType.equals("TILE_UPPER")) {
                partB = Start.tileList.get(correctRow).upperTile;
            }
//            LOGGER.info("Remember: N2: partB = " + partB);
            String[] tileEntry = new String [] {partA, partB};
//            LOGGER.info("Remember: N3:  = " + Arrays.toString(tileEntry));
            fourChoices.add(tileEntry);

//            LOGGER.info("Remember: O");
//            LOGGER.info("Remember: challengeLevelX = " + challengeLevelX);
            if (challengeLevelX == 1) {
                // use random tiles
                //JP TO-DO: NEED TO CHECK THAT RANDOM TILE IS NOT AN ISSUE

                Random rand = new Random();
                int rand1 = 0; // forces into while loop
                int rand2 = 0; // forces into while loop
                int rand3 = 0; // forces into while loop
                String altTile = "";

                while (rand1 == 0) {
                    rand1 = rand.nextInt(tileList.size());
                    altTile = Start.tileList.get(rand1).baseTile;
                    if (correctRow == rand1 || Character.toLowerCase(correctTile.charAt(0)) == Character.toLowerCase(altTile.charAt(0))) {
                        //think through why this is always false
                        //and fix condition to deal with upper and lower (convert test to lower)
                        rand1 = 0;
                    } else {
                        altTile = Start.tileList.get(rand1).baseTile;
                    }
                }
                if (choiceType.equals("TILE_LOWER")) {
                    partB = altTile;
                }
                if (choiceType.equals("TILE_UPPER")) {
                    partB = Start.tileList.get(rand1).upperTile;
                }
                partA = Start.tileList.get(returnPositionInAlphabet(altTile)).audioForTile;
                tileEntry = new String [] {partA, partB};
//                LOGGER.info("Remember: O2: tileEntry = " + Arrays.toString(tileEntry));
                fourChoices.add(tileEntry);

                while (rand2 == 0) {
                    rand2 = rand.nextInt(tileList.size());
                    altTile = Start.tileList.get(rand2).baseTile;
                    if (correctRow == rand2 || rand1 == rand2 || Character.toLowerCase(correctTile.charAt(0)) == Character.toLowerCase(altTile.charAt(0))) {
                        rand2 = 0;
                    } else {
                        altTile = Start.tileList.get(rand2).baseTile;
                    }
                }
                if (choiceType.equals("TILE_LOWER")) {
                    partB = altTile;
                }
                if (choiceType.equals("TILE_UPPER")) {
                    partB = Start.tileList.get(rand2).upperTile;
                }
                partA = Start.tileList.get(returnPositionInAlphabet(altTile)).audioForTile;
                tileEntry = new String [] {partA, partB};
//                LOGGER.info("Remember: O3: tileEntry = " + Arrays.toString(tileEntry));
                fourChoices.add(tileEntry);

                while (rand3 == 0) {
                    rand3 = rand.nextInt(tileList.size());
                    altTile = Start.tileList.get(rand3).baseTile;
                    if (correctRow == rand3 || rand1 == rand2 || rand1 == rand3 || rand2 == rand3
                            || Character.toLowerCase(correctTile.charAt(0)) == Character.toLowerCase(altTile.charAt(0))) {
                        rand3 = 0;
                    } else {
                        altTile = Start.tileList.get(rand3).baseTile;
                    }
                }
                if (choiceType.equals("TILE_LOWER")) {
                    partB = altTile;
                }
                if (choiceType.equals("TILE_UPPER")) {
                    partB = Start.tileList.get(rand3).upperTile;
                }
                partA = Start.tileList.get(returnPositionInAlphabet(altTile)).audioForTile;
                tileEntry = new String [] {partA, partB};
//                LOGGER.info("Remember: O4: tileEntry = " + Arrays.toString(tileEntry));
                fourChoices.add(tileEntry);

            }


            if (challengeLevelX == 2) {
                // use distractor tiles

                for (int i = 1; i < 4; i++) {

//                    LOGGER.info("Remember: P2");
                    if (choiceType.equals("TILE_LOWER")) {
                        partB = Start.tileList.get(correctRow).altTiles[i - 1];

                    }
//                    LOGGER.info("Remember: P3");
                    if (choiceType.equals("TILE_UPPER")) {
                        partB = Start.tileList.get(returnPositionInAlphabet(Start.tileList.get(correctRow).altTiles[i - 1])).upperTile;
                    }

                    //JP approach 2:

                    //while (replace) {

                        if (partB.charAt(0) == correctTile.charAt(0)) {
                            if (partB.length() <= correctTile.length()) {
                                Random rand = new Random();
                                int rand5 = rand.nextInt(tileList.size());
                                if (choiceType.equals("TILE_UPPER")) {
                                    partB = Start.tileList.get(rand5).upperTile;
                                    while ((Character.toLowerCase(partB.charAt(0)) == Character.toLowerCase(correctTile.charAt(0))) ||
                                            partB.equals(Start.tileList.get(returnPositionInAlphabet(Start.tileList.get(correctRow).altTiles[0])).upperTile) ||
                                            partB.equals(Start.tileList.get(returnPositionInAlphabet(Start.tileList.get(correctRow).altTiles[0])).upperTile) ||
                                            partB.equals(Start.tileList.get(returnPositionInAlphabet(Start.tileList.get(correctRow).altTiles[0])).upperTile)) {
                                        rand5 = rand.nextInt(tileList.size());
                                        partB = Start.tileList.get(rand5).upperTile;
                                    }
                                } else if (choiceType.equals("TILE_LOWER")) {
                                    partB = Start.tileList.get(rand5).baseTile;
                                    while ((Character.toLowerCase(partB.charAt(0)) == Character.toLowerCase(correctTile.charAt(0))) ||
                                        partB.equals(Start.tileList.get(correctRow).altTiles[0]) ||
                                        partB.equals(Start.tileList.get(correctRow).altTiles[1]) ||
                                        partB.equals(Start.tileList.get(correctRow).altTiles[2])) {
                                            rand5 = rand.nextInt(tileList.size());
                                            partB = Start.tileList.get(rand5).baseTile;
                                    }
                                }

                            }
                        }
                    //}
                    //
//                    LOGGER.info("Remember: P4");
                    partA = Start.tileList.get(returnPositionInAlphabet(partB)).audioForTile;
//                    LOGGER.info("Remember: P5");
                    tileEntry = new String [] {partA, partB};
//                    LOGGER.info("Remember: P6");
                    fourChoices.add(tileEntry);
//                    LOGGER.info("Remember: P7");
                }
            }

//            LOGGER.info("Remember: R");
            Collections.shuffle(fourChoices);

            return fourChoices;

        }

        public String getInstanceTypeForMixedTile(int index, String wordInLWC) {

            // need to rethink this function for tone, SAD,

            String instanceType = null;

            String mixedDefinitionInfo = Start.wordHashMap.find(wordInLWC).mixedDefs;
            // wordInLWC: "a ceu "
            // find, finds the key and returns the value

            // if mixedDefinitionInfo is not C or V or X or dash, then we assume it has two elements
            // to disambiguate, e.g. niwan', where...
            // first n is a C and second n is a X (nasality indicator), and we would code as C234X6

            // JP: these types come from the wordlist
            // in the wordlist, "-" does not mean "dash", it means "no multifunction symbols in this word"
            // but the types in the wordlist come from the same set of choices as from the gametiles
            if (!mixedDefinitionInfo.equals("C") && !mixedDefinitionInfo.equals("V")
                    && !mixedDefinitionInfo.equals("X") && !mixedDefinitionInfo.equals("T")
                    && !mixedDefinitionInfo.equals("-") && !mixedDefinitionInfo.equals("SAD")) {
                instanceType = String.valueOf(mixedDefinitionInfo.charAt(index));
            } else {
                instanceType = mixedDefinitionInfo;
            }

            return instanceType;

        }

    }

    public class TileHashMap extends HashMap<String, Tile>{

        public Tile find(String key) {
            for (String k : keySet()) {
                if (k.compareTo(key) == 0) {
                    return (get(k));
                }
            }
        return null;
        }

    }

    public class SyllableHashMap extends HashMap<String, Syllable>{

        public Syllable find(String key) {
            for (String k : keySet()) {
                if (k.compareTo(key) == 0) {
                    return (get(k));
                }
            }
            return null;
        }

    }

    public class WordHashMap extends HashMap<String, Word>{

        public Word find(String key) {
            for (String k : keySet()) {
                if (k.compareTo(key) == 0) {
                    return (get(k));
                }
            }
            return null;
        }

    }

    public class KeyList extends ArrayList<Key> {

        public String keysTitle;
        public String colorTitle;

    }

    public class GameList extends ArrayList<Game> {

        public String gameNumber;
        public String gameCountry;
        public String gameLevel;
        public String gameColor;
        public String gameInstrLabel;
        public String gameInstrDuration;
        public String gameMode;

    }

    public class LangInfoList extends HashMap<String, String> {

        public String title;

        public String find(String keyContains) {
            for (String k : keySet()) {
                if (k.contains(keyContains)) {
                    return (get(k));
                }
            }
            return "";
        }
    }

    public class SettingsList extends HashMap<String, String> {

        public String title;

        public String find(String keyContains) {
            for (String k : keySet()) {
                if (k.contains(keyContains)) {
                    return (get(k));
                }
            }
            return "";
        }
    }

    public class AvatarNameList extends ArrayList<String> {

        public String title;

    }

    public class TileListWithMultipleTypes extends ArrayList<String> {

        public String returnNextAlphabetTileDifferentiateTypes(String oldTile) {

            String nextTile = "";
            for (int i = 0; i < tileListWithMultipleTypes.size(); i++) {
                if (tileListWithMultipleTypes.get(i).equals(oldTile)) {
                    if (i < (tileListWithMultipleTypes.size() - 1)) {
                        nextTile = tileListWithMultipleTypes.get(i + 1);
                    } else// if (i == size() - 1) {
                        nextTile = tileListWithMultipleTypes.get(0);
                }
            }

            return nextTile;

        }

        public String returnPreviousAlphabetTileDifferentiateTypes(String oldTile) {

            String previousTile = "";
            for (int i = tileListWithMultipleTypes.size() - 1; i >= 0; i--) {

                if (tileListWithMultipleTypes.get(i).equals(oldTile)) {
                    if (i > 0) {
                        previousTile = tileListWithMultipleTypes.get(i - 1);
                    } else// if (i == 0) {
                        previousTile = tileListWithMultipleTypes.get(tileListWithMultipleTypes.size() - 1);
                }
            }

            return previousTile;

        }
    }

}
