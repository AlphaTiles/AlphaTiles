package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

public class Start extends AppCompatActivity
{
    Context context;

    public static final int ALT_COUNT = 3;  // KP

    public static String localAppName; // KP add "public"

    public static TileList tileList; // KP // from aa_gametiles.txt

    public static WordList wordList;     // KP  // from aa_wordlist.txt

    public static KeyList keyList; // KP // from aa_keyboard.txt

    public static GameList gameList; // from aa_games.text

    public static LangInfoList langInfoList; // KP / from aa_langinfo.txt

    public static SettingsList settingsList; // KP // from aa_settings.txt

    public static AvatarNameList nameList; // KP / from aa_names.txt

    public static ArrayList<Integer> avatarIdList;
    public static ArrayList<Drawable> avatarJpgList;
    public static SoundPool gameSounds;
    public static int correctSoundID;
    public static int incorrectSoundID;
    public static int correctFinalSoundID;
    public static HashMap<String, Integer> speechIDs;
    public static HashMap<String, Integer> tileAudioIDs;
    public static int correctSoundDuration;
    public static int incorrectSoundDuration;
    public static int correctFinalSoundDuration;
    public static HashMap<String, Integer> speechDurations;

    private static final Logger LOGGER = Logger.getLogger( Start.class.getName() );

    ConstraintLayout startCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        buildLangInfoArray();
        LOGGER.info("Remember: completed buildLangInfoArray() and buildNamesArray()");

        buildKeysArray();
        LOGGER.info("Remember: completed buildKeysArray()");

        buildSettingsArray();
        LOGGER.info("Remember: completed buildSettingsArray()");

        buildGamesArray();
        LOGGER.info("Remember: completed buildGamesArray()");

        buildWordAndTileArrays();
        LOGGER.info("Remember: completed buildWordAndTileArrays()");

        Intent intent = new Intent(this, ChoosePlayer.class);

        startActivity(intent);

        finish();

    }

    private int getAssetDuration(int assetID)
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(assetID);
        mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public void buildWordAndTileArrays()    {
        LOGGER.info("Remember: entered buildAllArrays() method");
//        Util.logMemory();
        buildTilesArray();
        LOGGER.info("Remember: completed buildTilesArray()");
//        Util.logMemory();
        buildWordsArray();
        LOGGER.info("Remember: completed buildWordsArray()");
//        Util.logMemory();

        // load music sounds
        gameSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        correctSoundID = gameSounds.load(context, R.raw.zz_correct, 3);
        incorrectSoundID = gameSounds.load(context, R.raw.zz_incorrect, 3);
        correctFinalSoundID = gameSounds.load(context, R.raw.zz_correct_final, 1);

        correctSoundDuration = getAssetDuration(R.raw.zz_correct) + 200;
//		incorrectSoundDuration = getAssetDuration(R.raw.zz_incorrect);	// not needed atm
//		correctFinalSoundDuration = getAssetDuration(R.raw.zz_correct_final);	// not needed atm

        // load speech sounds
        Resources res = context.getResources();
        speechIDs = new HashMap();
        speechDurations = new HashMap();
        for (Word word : wordList)
        {
            int resId = res.getIdentifier(word.nationalWord, "raw", context.getPackageName());
            speechIDs.put(word.nationalWord, gameSounds.load(context, resId, 2));
            speechDurations.put(word.nationalWord, word.duration + 100);
//			speechDurations.put(word.nationalWord, getAssetDuration(resId) + 200);
        }

        tileAudioIDs = new HashMap(0);

        for(Tile tile : tileList){

            int resId = res.getIdentifier(tile.audioForTile, "raw", context.getPackageName());
            tileAudioIDs.put(tile.baseTile, gameSounds.load(context, resId, 2));
        }


    }
    public void buildTilesArray() {
        // KP, Oct 2020
        // AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces
        // AH Mar 2021, add new column for audio tile and for upper case tile

        Scanner scanner = new 	Scanner(getResources().openRawResource(R.raw.aa_gametiles));
        /*
        JP reminder: this is scanning/parsing the aa_gametiles.txt file to obtain the tiles
        each line is a new tile with 11 attributes
        */
        boolean header = true;
        tileList = new TileList();

        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t",11);
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
                header = false;
            } else {
                //JP reminder: each tile has 11 attributes
                //are the Or1, Or2, and Or3 distractor tiles?
                Tile tile = new Tile(thisLineArray[0], thisLineArray[1], thisLineArray[2], thisLineArray[3], thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10]);
                if (!tile.hasNull()) {
                    tileList.add(tile);
                }
            }
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
                header = false;
            } else {
                Game game = new Game(thisLineArray[0], thisLineArray[1],thisLineArray[2], thisLineArray[3]);
                if (!game.hasNull()) {
                    gameList.add(game);
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
        public Tile(String baseTile, String alt1Tile, String alt2Tile, String alt3Tile, String tileType, String audioForTile, String upperTile, String tileTypeB, String audioForTileB, String tileTypeC, String audioForTileC) {
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

        public Game(String gameNumber, String gameCountry, String gameLevel, String gameColor) {
            this.gameNumber = gameNumber;
            this.gameCountry = gameCountry;
            this.gameLevel = gameLevel;
            this.gameColor = gameColor;
        }

        public boolean hasNull() {
            return gameNumber == null || gameCountry == null|| gameLevel == null|| gameColor == null;
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

            int tilesCount = 0;

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
                if (parsedWordArrayFinal.get(0).equals(someGameTile)) {
                    tilesCount++;
                }
            }

            return tilesCount;

        }

        public String[][] returnGroupOneWords(String someGameTile, int tilesCount) {
            // Group One = words that START with the active tile

            ArrayList<String> parsedWordArrayFinal;
            int hitsCounter = 0;

            String[][] wordsStartingWithTileArray = new String [tilesCount][2];

            for (int i = 0; i < Start.wordList.size(); i++) {
                parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
                if (parsedWordArrayFinal.get(0).equals(someGameTile)) {
                    wordsStartingWithTileArray[hitsCounter][0] = get(i).nationalWord;
                    wordsStartingWithTileArray[hitsCounter][1] = get(i).localWord;
                    hitsCounter++;
                }
            }

            return wordsStartingWithTileArray;

        }

        public int returnGroupTwoCount(String someGameTile) {
            // Group Two = words that contain the active tile non-initially (but excluding initially)

            ArrayList<String> parsedWordArrayFinal;

            int tilesCount = 0;

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
                for (int k = 1; k < parsedWordArrayFinal.size(); k++) {
                    // k = 1, not 0, because you're looking for non-initial tiles
                    if (parsedWordArrayFinal.get(k) != null) {
                        if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                            tilesCount++;
                            break;
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

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
                for (int k = 1; k < parsedWordArrayFinal.size(); k++) {
                    // k = 1, not 0, because you're looking for non-initial tiles
                    if (parsedWordArrayFinal.get(k) != null) {
                        if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                            wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
                            wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
                            hitsCounter++;
                            break;
                        }
                    }
                }
            }

            return wordsWithNonInitialTiles;

        }

        public int returnGroupThreeCount(String someGameTile) {
            // Group Three = words containing the active tile anywhere (initial and/or non-initial)

            ArrayList<String> parsedWordArrayFinal;

            int tilesCount = 0;

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
                for (int k = 0; k < parsedWordArrayFinal.size(); k++) {
                    if (parsedWordArrayFinal.get(k) != null) {
                        if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                            tilesCount++;
                            break;
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

            String[][] wordsWithNonInitialTiles = new String [tilesCount][2];

            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
                for (int k = 0; k < parsedWordArrayFinal.size(); k++) {
                    if (parsedWordArrayFinal.get(k) != null) {
                        if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                            wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
                            wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
                            hitsCounter++;
                            break;
                        }
                    }
                }
            }

            return wordsWithNonInitialTiles;

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

        public ArrayList<String[]> returnFourWords(String wordInLOP, String wordInLWC, String refTile, int challengeLevel, String refType, String choiceType) {

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
                parsedWordArrayFinal = Start.tileList.parseWord(activeWord);
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

                for (int i = 0; i < 3; i++) {

                    fourChoices.add(easyWords.get(i));

                }

            }

            if (challengeLevel == 2) {
                // use moderate words and if the supply runs out use easy words

                for (int i = 0; i < 3; i++) {
                    if (moderateWords.size() > i) {

                        fourChoices.add(moderateWords.get(i));

                    } else {

                        fourChoices.add(easyWords.get(i - moderateWords.size()));

                    }
                }

            }

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

        public ArrayList<String> parseWord(String parseMe) {
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

                Random rand = new Random();
                int rand1 = 0; // forces into while loop
                int rand2 = 0; // forces into while loop
                int rand3 = 0; // forces into while loop
                String altTile = null;

                while (rand1 == 0) {
                    rand1 = rand.nextInt(tileList.size());
                    if (correctRow == rand1) {
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
                    if (correctRow == rand1 || correctRow == rand2 || rand1 == rand2) {
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
                    if (correctRow == rand1 || correctRow == rand2 || correctRow == rand3 || rand1 == rand2 || rand1 == rand3 || rand2 == rand3) {
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

            } else {

//                LOGGER.info("Remember: O5: skipped challengeLevel 1");

            }

//            LOGGER.info("Remember: P");
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

            String instanceType = null;

            String mixedDefinitionInfo = Start.wordList.get(wordList.returnPositionInWordList(wordInLWC)).mixedDefs;

            // if mixedDefinitionInfo is not C or V or X or dash, then we assume it has two elements to disambiguate, e.g. niwan', where...
            // first n is a C and second n is a X (nasality indicator), and we would code as C234X6
            if (!mixedDefinitionInfo.equals("C") && !mixedDefinitionInfo.equals("V") && !mixedDefinitionInfo.equals("X") && !mixedDefinitionInfo.equals("-")) {
                instanceType = String.valueOf(mixedDefinitionInfo.charAt(index));
            } else {
                instanceType = mixedDefinitionInfo;
            }

            return instanceType;

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

}
