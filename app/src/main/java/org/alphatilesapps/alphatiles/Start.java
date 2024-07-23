package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
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

public class Start extends AppCompatActivity {
    Context context;
    public static final int ALT_COUNT = 3;  // KP

    public static String localAppName; // KP add "public"

    public static TileList tileList; // KP // from aa_gametiles.txt
    public static ArrayList<ArrayList<Tile>> tileStagesLists; // LM // For staged introduction of tiles
    public static TileList tileListNoSAD; // JP // from aa_gametiles.txt minus SAD types

    public static WordList wordList;     // KP  // from aa_wordlist.txt
    public static ArrayList<WordList> wordStagesLists; // LM // For staged introduction of tiles/words

    public static SyllableList syllableList; // JP // from aa_syllables.txt
    public static KeyList keyList; // KP // from aa_keyboard.txt

    public static GameList gameList; // from aa_games.text
    public static LangInfoList langInfoList; // KP / from aa_langinfo.txt
    public static SettingsList settingsList; // KP // from aa_settings.txt
    public static AvatarNameList nameList; // KP / from aa_names.txt

    // LM / allows us to find() a Tile object using its name
    public static TileHashMap tileHashMap;
    public static TileHashMap tileHashMapNoSAD;
    public static WordHashMap lwcWordHashMap;
    public static WordHashMap lopWordHashMap;

    public static SyllableHashMap syllableHashMap; //JP

    public static SoundPool gameSounds;
    public static int correctSoundID;
    public static int incorrectSoundID;
    public static int correctFinalSoundID;
    public static HashMap<String, Integer> wordAudioIDs;
    public static HashMap<String, Integer> tileAudioIDs;
    public static HashMap<String, Integer> syllableAudioIDs; //JP
    public static int correctSoundDuration;
    public static HashMap<String, Integer> tileDurations;
    public static final ArrayList<String> colorList = new ArrayList<>();
    public static int totalAudio; //JP: the total number of audio files to be loaded into the soundpool

    public static Boolean hasTileAudio;
    public static Boolean hasSyllableAudio;
    public static Boolean hasSyllableGames = false;
    public static int after12checkedTrackers;
    public static Boolean differentiatesTileTypes;
    public static Boolean hasSAD = false;
    public static double stageCorrespondenceRatio;
    public static int numberOfAvatars = 12;
    public static String scriptType; // LM Can be "Thai", "Lao", or "Khmer" for special tile parsing. If nothing specified, tile parsing defaults to unidirectional.

    public static TileList CONSONANTS = new TileList();
    public static TileList PLACEHOLDER_CONSONANTS = new TileList();
    public static TileList SILENT_PRELIMINARY_TILES = new TileList();
    public static TileList SILENT_PLACEHOLDER_CONSONANTS = new TileList();
    public static TileList VOWELS = new TileList();
    public static TileList CorV = new TileList();
    public static TileList TONES = new TileList();

    public static TileList ADs = new TileList();

    public static TileList Ds = new TileList();

    public static TileList SAD = new TileList();
    public static List<String> SYLLABLES = new ArrayList<>();
    public static List<String> SAD_STRINGS = new ArrayList<>();
    public static ArrayList<String> MULTITYPE_TILES = new ArrayList<>();


    private static final Logger LOGGER = Logger.getLogger( Start.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        totalAudio = 3; // JP: how many total audio files to load
        // will be used in LoadingScreen.java to determine when all audio files have loaded -> advance to ChoosePlayer
        // initialize to 3 for correct, incorrect, and correctFinal sounds

        buildLangInfoList();
        LOGGER.info("LoadProgress: completed buildLangInfoList()");
        buildKeyList();
        LOGGER.info("LoadProgress: completed buildKeyList()");
        buildSettingsList();
        LOGGER.info("LoadProgress: completed buildSettingsList()");
        buildColorList();
        LOGGER.info("LoadProgress: completed buildColorsList()");

        String hasAudioSetting = settingsList.find("Has tile audio");
        if (!hasAudioSetting.equals("")) {
            hasTileAudio = Boolean.parseBoolean(hasAudioSetting);
        } else {
            hasTileAudio = false;
        }

        String differentiatesTileTypesSetting = settingsList.find("Differentiates types of multitype symbols");
        if (!differentiatesTileTypesSetting.equals("")) {
            differentiatesTileTypes = Boolean.parseBoolean(differentiatesTileTypesSetting);
        } else {
            differentiatesTileTypes = false;
        }

        String after12checkedTrackersSetting = settingsList.find("After 12 checked trackers");
        if (!after12checkedTrackersSetting.equals("")) {
            after12checkedTrackers = Integer.valueOf(after12checkedTrackersSetting);
        } else {
            after12checkedTrackers = 3;
        }

        //to make syllable audio optional
        String hasSyllableAudioSetting = settingsList.find("Has syllable audio");
        if (!hasSyllableAudioSetting.equals("")) {
            hasSyllableAudio = Boolean.parseBoolean(hasSyllableAudioSetting);
        } else {
            hasSyllableAudio = false;
        }

        String customNumOfAvatars = settingsList.find("Number of avatars"); // Default is 12
        if (!customNumOfAvatars.equals("")) {
            numberOfAvatars = Integer.parseInt(customNumOfAvatars);
        }

        String stageCorrespondenceRatioSetting = settingsList.find("Stage correspondence ratio");
        if (!stageCorrespondenceRatioSetting.equals("")) {
            stageCorrespondenceRatio = Double.parseDouble(stageCorrespondenceRatioSetting);
        } else {
            stageCorrespondenceRatio = 0.5;
        }

        // JP: the old constructor is deprecated after API 21, so account for both scenarios
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            gameSounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
        } else {
            gameSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        buildTileList();
        for (int d = 0; d < tileList.size(); d++) {
            Tile thisTile = tileList.get(d);
            String thisTileType = thisTile.typeOfThisTileInstance;
            if (!thisTile.tileTypeB.equals("none")) {
                MULTITYPE_TILES.add(thisTile.text);
            } else {
                if (thisTileType.equals("C")) {
                    CONSONANTS.add(thisTile);
                    CorV.add(thisTile);
                }
                else if (thisTileType.equals("PC")) {
                    PLACEHOLDER_CONSONANTS.add(thisTile);
                }
                else if (thisTileType.matches("(LV|BV|AV|FV|V)")) {
                    VOWELS.add(thisTile);
                    CorV.add(thisTile);
                }
                else if (thisTileType.equals("T")) {
                    TONES.add(thisTile);
                } else if (thisTileType.equals("D")) {
                    Ds.add(thisTile);
                } else if (thisTileType.equals("AD")) {
                    ADs.add(thisTile);
                }
                else if (thisTileType.equals("SAD")) {
                    hasSAD = true;
                    SAD.add(thisTile);
                    SAD_STRINGS.add(thisTile.text);
                }
            }
        }
        LOGGER.info("LoadProgress: completed buildTileList()");

        Collections.shuffle(CONSONANTS);
        Collections.shuffle(VOWELS);
        Collections.shuffle(CorV);
        Collections.shuffle(TONES);
        Collections.shuffle(ADs);
        Collections.shuffle(SYLLABLES);
        Collections.shuffle(MULTITYPE_TILES);

        if (hasTileAudio) {
            totalAudio = totalAudio + tileList.size();
        }

        buildWordList();
        LOGGER.info("LoadProgress: completed buildWordList()");
        buildTileStagesLists();
        LOGGER.info("LoadProgress: completed buildTileStagesLists()");
        buildWordStagesLists();
        LOGGER.info("LoadProgress: completed buildWordStagesLists()");
        buildGameList();
        LOGGER.info("LoadProgress: completed buildGameList()");
        totalAudio = totalAudio + wordList.size();

        if (hasSyllableGames) {
            buildSyllableList();
            for (int d = 0; d < syllableList.size(); d++) {
                SYLLABLES.add(syllableList.get(d).toString());
            }
            Collections.shuffle(SYLLABLES);
        }
        LOGGER.info("LoadProgress: completed buildSyllablesArray()");

        if (hasSyllableAudio) {
            totalAudio = totalAudio + syllableList.size();
        }

        if (differentiatesTileTypes && MULTITYPE_TILES.isEmpty()) {
            for (int d = 0; d < Start.tileList.size(); d++) {
                if (!Start.tileList.get(d).tileTypeB.equals("none")) {
                    MULTITYPE_TILES.add(Start.tileList.get(d).text);
                }
            }
        }
        Chile.data = Chile.chilePreProcess();

        Intent intent = new Intent(this, LoadingScreen.class);
        startActivity(intent);

    }

    private void buildColorList() {
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_colors));

        boolean header = true;

        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t", 3);
            if (header) {
                header = false;
            } else {
                colorList.add(thisLineArray[2]);
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
    public void buildTileList() {
        // KP, Oct 2020
        // AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces
        // AH Mar 2021, add new column for audio tile and for upper case tile

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_gametiles));
        boolean header = true;
        tileList = new TileList();
        tileListNoSAD = new TileList();

        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
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
                tileList.tileDuration1Title = "";
                tileList.tileDuration2Title = "";
                tileList.tileDuration3Title = "";
                tileList.stageOfFirstAppearanceTitle = thisLineArray[14];
                tileList.stageOfFirstAppearanceTitleType2 = thisLineArray[15];
                tileList.stageOfFirstAppearanceTitleType3 = thisLineArray[16];

                header = false;
            } else {
                // Sort information for staged introduction, including among potential second or third types of a tile
                int stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3;
                if(!thisLineArray[14].matches("[0-9]+")) { // Add all first types of tiles to "stage 1" if stages aren't being used
                    stageOfFirstAppearance = 1;
                } else {
                    stageOfFirstAppearance = Integer.parseInt(thisLineArray[14]);
                    if (!(stageOfFirstAppearance >= 1 && stageOfFirstAppearance <= 7)) {
                        stageOfFirstAppearance = 1;
                    }
                }
                if(!thisLineArray[15].matches("[0-9]+")) {
                    stageOfFirstAppearanceType2 = 1;
                } else {
                    stageOfFirstAppearanceType2 = Integer.parseInt(thisLineArray[15]);
                    if (!(stageOfFirstAppearanceType2 >= 1 && stageOfFirstAppearanceType2 <= 7)) {
                        stageOfFirstAppearance = 1;
                    }
                }
                if(!thisLineArray[16].matches("[0-9]+")) {
                    stageOfFirstAppearanceType3 = 1;
                } else {
                    stageOfFirstAppearanceType3 = Integer.parseInt(thisLineArray[16]);
                    if (!(stageOfFirstAppearanceType3 >= 1 && stageOfFirstAppearanceType3 <= 7)) {
                        stageOfFirstAppearance = 1;
                    }
                }
                // Create tile(s) and add to list; may add up to three tiles from the same line if it has multiple types
                ArrayList<String> distractors = new ArrayList<>();
                distractors.add(thisLineArray[1]);
                distractors.add(thisLineArray[2]);
                distractors.add(thisLineArray[3]);
                Tile tile = new Tile(thisLineArray[0], distractors, thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3, thisLineArray[4], stageOfFirstAppearance, thisLineArray[5]);
                if (!tile.hasNull()) {
                    tileList.add(tile);
                    if (!tile.typeOfThisTileInstance.equals("SAD") && !(tile.audioForThisTileType.equals("zz_no_audio_needed") && !tile.typeOfThisTileInstance.equals("PC"))) {
                        tileListNoSAD.add(tile); // placeholder consonants may be added even if they don't have audio
                    }
                }
                if(!tile.tileTypeB.equals("none")){
                    tile = new Tile(thisLineArray[0], distractors, thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3, thisLineArray[7], stageOfFirstAppearanceType2, thisLineArray[8]);
                    if (!tile.hasNull()) {
                        tileList.add(tile);
                        if (!tile.typeOfThisTileInstance.equals("SAD") && !(tile.audioForThisTileType.equals("zz_no_audio_needed") && !tile.typeOfThisTileInstance.equals("PC"))) {
                            tileListNoSAD.add(tile); // placeholder consonants may be added even if they don't have audio
                        }
                    }
                }
                if(!tile.tileTypeC.equals("none")){
                    tile = new Tile(thisLineArray[0], distractors, thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3, thisLineArray[9], stageOfFirstAppearanceType3, thisLineArray[10]);
                    if (!tile.hasNull()) {
                        tileList.add(tile);
                        if (!tile.typeOfThisTileInstance.equals("SAD") && !(tile.audioForThisTileType.equals("zz_no_audio_needed") && !tile.typeOfThisTileInstance.equals("PC"))) {
                            tileListNoSAD.add(tile); // placeholder consonants may be added even if they don't have audio
                        }
                    }
                }
            }
        }
        for (Tile thisTile : tileList) {
            if(thisTile.audioForThisTileType.equals("zz_no_audio_needed") && thisTile.typeOfThisTileInstance.equals("PC")) {
                SILENT_PLACEHOLDER_CONSONANTS.add(thisTile);
            }
            if(thisTile.audioForThisTileType.equals("zz_no_audio_needed") && !thisTile.typeOfThisTileInstance.equals("PC")) {
                SILENT_PRELIMINARY_TILES.add(thisTile);
            }
        }
        buildTileHashMap();
    }


    public void buildTileStagesLists(){
        // LM, Apr 2023
        // Tile stages lists do NOT include SAD characters
        // Tile stages list tiles DO have the typeOfThisTileInstance field set
        tileStagesLists = new ArrayList<ArrayList<Tile>>();
        ArrayList<Tile> tileListStage1 = new TileList();
        tileStagesLists.add(tileListStage1);
        ArrayList<Tile> tileListStage2 =  new TileList();
        tileStagesLists.add(tileListStage2);
        ArrayList<Tile> tileListStage3 =  new TileList();
        tileStagesLists.add(tileListStage3);
        ArrayList<Tile> tileListStage4 =  new TileList();
        tileStagesLists.add(tileListStage4);
        ArrayList<Tile> tileListStage5 =  new TileList();
        tileStagesLists.add(tileListStage5);
        ArrayList<Tile> tileListStage6 =  new TileList();
        tileStagesLists.add(tileListStage6);
        ArrayList<Tile> tileListStage7 =  new TileList();
        tileStagesLists.add(tileListStage7);

        for(int i=0; i<7; i++){
            for (int t=0; t<tileList.size(); t++) {
                Tile tile = tileList.get(t);
                if (tile.stageOfFirstAppearanceForThisTileType==(i+1) && !tile.typeOfThisTileInstance.equals("SAD")) {
                    tileStagesLists.get(i).add(tile);
                }
            }

        }
    }

    public void buildSyllableList() {
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
                ArrayList<String> distractors = new ArrayList<>();
                distractors.add(thisLineArray[1]);
                distractors.add(thisLineArray[2]);
                distractors.add(thisLineArray[3]);
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
        for (int i = 0; i < syllableList.size(); i++) {
            syllableHashMap.put(syllableList.get(i).text, syllableList.get(i));
        }
    }

    public void buildWordList() {
        // KP, Oct 2020 (updated by AH to allow for spaces in fields (some common nouns in some languages have spaces)

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_wordlist));
        boolean header = true;
        wordList = new WordList();
        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                wordList.wordInLWCTitle = thisLineArray[0];
                wordList.wordInLOPTitle = thisLineArray[1];
                wordList.durationTitle = thisLineArray[2];
                wordList.mixedDefsTitle = thisLineArray[3];
                wordList.adjustmentTitle = ""; //set during LoadingScreen activity
                wordList.stageOfFirstAppearanceTitle = thisLineArray[5];
                header = false;
            } else {
                Word word = new Word(thisLineArray[0], thisLineArray[1], Integer.parseInt(thisLineArray[2]), thisLineArray[3], "", thisLineArray[5]);
                if (!word.hasNull()) {
                    wordList.add(word);
                }
            }
        }

        buildWordHashMap();
    }

    public void buildWordStagesLists() {
        // LM, Apr 2023

        wordStagesLists = new ArrayList<WordList>();
        HashMap<Word, Integer> stagesOfFirstAppearance = new HashMap<Word, Integer>();
        WordList wordListStage1 = new WordList();
        wordStagesLists.add(wordListStage1);
        WordList wordListStage2 = new WordList();
        wordStagesLists.add(wordListStage2);
        WordList wordListStage3 = new WordList();
        wordStagesLists.add(wordListStage3);
        WordList wordListStage4 = new WordList();
        wordStagesLists.add(wordListStage4);
        WordList wordListStage5 = new WordList();
        wordStagesLists.add(wordListStage5);
        WordList wordListStage6 = new WordList();
        wordStagesLists.add(wordListStage6);
        WordList wordListStage7 = new WordList();
        wordStagesLists.add(wordListStage7);

        boolean firstLetterStageCorrespondence = false;
        int stage1and2MaxWordLength = Integer.MAX_VALUE;
        if(!settingsList.find("First letter stage correspondence").equals("")){
           firstLetterStageCorrespondence = Boolean.parseBoolean(settingsList.find("First letter stage correspondence"));
        }
        if(!settingsList.find("Stage 1-2 max word length").equals("")){
            stage1and2MaxWordLength = Integer.parseInt(settingsList.find("Stage 1-2 max word length"));
        }

        // Find default first stage correspondences
        // Start all off at the last possible stage to introduce the word
        int lastStage = 0;
        for(Tile tile : tileList){
            if (tile.stageOfFirstAppearance > lastStage){
                lastStage = tile.stageOfFirstAppearance;
            }
            if (tile.stageOfFirstAppearanceB > lastStage){
                lastStage = tile.stageOfFirstAppearanceB;
            }
            if (tile.stageOfFirstAppearanceC > lastStage){
                lastStage = tile.stageOfFirstAppearanceC;
            }
        }
        for(Word word : wordList){
            stagesOfFirstAppearance.put(word, lastStage);
        }
        // Keep trying to find an earlier stage that it corresponds with until knowing the earliest stage that it corresponds with.
        for(int i=5;i>-1; i--){
            ArrayList<Tile> cumulativeCorrespondingTiles = new ArrayList<Tile>();
            for(int s=0; s<=i; s++){
                cumulativeCorrespondingTiles.addAll(tileStagesLists.get(s));
            }
            for(Word word: wordList) {
                ArrayList<Tile> tilesInThisWord = tileList.parseWordIntoTiles(word.wordInLOP, word);
                int correspondingTiles = 0;
                for(int t=0; t<tilesInThisWord.size(); t++){
                    Tile aTileInThisWord = tilesInThisWord.get(t);
                    for(int a=0; a<cumulativeCorrespondingTiles.size(); a++) {
                        Tile aTileInTheStage = cumulativeCorrespondingTiles.get(a);
                        if(aTileInThisWord==null || aTileInThisWord.hasNull()){
                            LOGGER.info("problematic word: " + word.wordInLOP + "; index of problematic tile is " + a);
                        }
                        if(aTileInThisWord.text.equals(aTileInTheStage.text)){
                            if(differentiatesTileTypes){
                                if(aTileInTheStage.typeOfThisTileInstance.equals(aTileInThisWord.typeOfThisTileInstance)){
                                    correspondingTiles++;
                                    break;
                                }
                            } else {
                                correspondingTiles++;
                                break;
                            }
                        }
                    }
                }
                if((double)correspondingTiles/tilesInThisWord.size() > stageCorrespondenceRatio){
                    if ((i==0 || i==1) && (tilesInThisWord.size()>stage1and2MaxWordLength)){
                        stagesOfFirstAppearance.put(word, i+2); // Bump words that are too long for stage 1 or 2 to the next stage
                    } else {
                        stagesOfFirstAppearance.put(word, i+1);
                    }
                }
            }
        }

        // Then override for first letter correspondence, if set, to bring words to an earlier stage based on corresponding in first tile
        if(firstLetterStageCorrespondence){
            for (Word word : wordList) {
                ArrayList<Tile> tilesInThisWord = tileList.parseWordIntoTiles(word.wordInLOP, word);
                Tile firstTile = tilesInThisWord.get(0);
                String firstTileType = "";
                int stageFirstTileBelongsTo = firstTile.stageOfFirstAppearance;
                if(MULTITYPE_TILES.contains(firstTile.text)) { // Check if we need to get stageOfFirstAppearance2 or stageOfFirstAppearance3 instead
                    firstTileType = firstTile.typeOfThisTileInstance;
                    if(firstTile.tileTypeB.equals(firstTileType)){
                        stageFirstTileBelongsTo = firstTile.stageOfFirstAppearanceB;
                    } else if (firstTile.tileTypeC.equals(firstTileType)) {
                        stageFirstTileBelongsTo = firstTile.stageOfFirstAppearanceC;
                    }
                }

                if (stageFirstTileBelongsTo < stagesOfFirstAppearance.get(word)){ // Bump words to an earlier stage if their first tile matches a tile that's introduced in that stage
                    stagesOfFirstAppearance.put(word, stageFirstTileBelongsTo);
                }
            }
        }


        // Then override for any words that have explicit first stage info in the wordlist
        for (Word word : wordList){
            if(word.stageOfFirstAppearance.matches("[0-9]+")){
                int stage = Integer.parseInt(word.stageOfFirstAppearance);
                if (stage >=1 && stage <= 7) {
                    stagesOfFirstAppearance.put(word, stage);
                }
            }
        }

        // Then use the stage info found to make the sub-wordlists
        for(Word word : wordList){
            int stageOfFirstAppearance = stagesOfFirstAppearance.get(word);
            wordStagesLists.get(stageOfFirstAppearance-1).add(word);
        }

    }

    public void buildKeyList() {
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

    public void buildGameList() {

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_games)); // prep scan of aa_games.txt
        boolean header = true;
        gameList = new GameList();
        while (scanner.hasNext()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            if (header) {
                gameList.gameNumberTitle = thisLineArray[0];
                gameList.gameCountryTitle = thisLineArray[1];
                gameList.gameLevelTitle = thisLineArray[2];
                gameList.gameColorTitle = thisLineArray[3];
                gameList.gameInstrLabelTitle = thisLineArray[4];
                gameList.gameInstrDurationTitle = thisLineArray[5];
                gameList.gameModeTitle = thisLineArray[6];
                gameList.gameStageTitle = thisLineArray[7];
                header = false;
            } else {
                Game game = new Game(thisLineArray[0], thisLineArray[1], thisLineArray[2], thisLineArray[3], thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7]);
                if (!game.hasNull()) {
                    gameList.add(game);
                }
                if (thisLineArray[6].equals("S")) { //JP
                    hasSyllableGames = true;
                }
            }
        }
    }

    public void buildSettingsList() {

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

    public void buildLangInfoList() {

        boolean header = true;
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_langinfo)); // prep scan of aa_langinfo.txt

        langInfoList = new LangInfoList();
        while (scanner.hasNext()) {
            if (scanner.hasNextLine()) {
                if (header) {
                    langInfoList.title = scanner.nextLine();
                    header = false;
                } else {
                    String thisLine = scanner.nextLine();
                    String[] thisLineArray = thisLine.split("\t");
                    langInfoList.put(thisLineArray[0], thisLineArray[1]);
                }
            }
        }

        localAppName = langInfoList.find("Game Name");
        scriptType = langInfoList.find("Script type"); // If "Thai", "Lao", or "Khmer", special tile parsing occurs

        String localWordForName = langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            buildAvatarNameList();
        }

    }

    public void buildAvatarNameList() {

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_names)); // prep scan of aa_names.txt

        boolean header = true;

        nameList = new AvatarNameList();
        while (scanner.hasNext()) {
            if (scanner.hasNextLine()) {
                if (header) {
                    nameList.title = scanner.nextLine();
                    header = false;
                } else {
                    String thisLine = scanner.nextLine();
                    String[] thisLineArray = thisLine.split("\t");
                    nameList.add(thisLineArray[1]);
                }
            }
        }

        localAppName = langInfoList.find("Game Name");

    }

    public void buildTileHashMap() {
        tileHashMap = new TileHashMap();
        tileHashMapNoSAD = new TileHashMap();
        for (int i = 0; i < tileList.size(); i++) {
            tileHashMap.put(tileList.get(i).text, tileList.get(i));
            if (!tileList.get(i).tileType.equals("SAD")) {
                tileHashMapNoSAD.put(tileList.get(i).text, tileList.get(i));
            }
        }
    }

    public void buildWordHashMap() {
        lwcWordHashMap = new WordHashMap();
        for (int i = 0; i < wordList.size(); i++) {
            lwcWordHashMap.put(wordList.get(i).wordInLWC, wordList.get(i));
        }

        lopWordHashMap = new WordHashMap();
        for (int i = 0; i < wordList.size(); i++) {
            lopWordHashMap.put(wordList.get(i).wordInLOP, wordList.get(i));
        }
    }

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

        public Tile (Tile anotherTile) {
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

        /**
         * NOTE: This method may or may not actually work as intended.
         * It needs to be tested and revised.
         */
        public String getAudioNameAccountingForMultitypeSymbols() {
            if(!Start.differentiatesTileTypes) {
                return this.audioName;
            }

            return this.audioForThisTileType;
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

    public class Game {
        public String number;
        public String country;
        public String level;
        public String color;
        public String instructionAudioName;
        public String instructionDuration;
        public String mode; //JP : Syllable or Tile mode (S or T)
        public String stage; // LM The game will include tiles/words from all the stages up to and including the stage indicated in the row of aa_games.txt

        public Game(String gameNumber, String gameCountry, String gameLevel, String gameColor, String gameInstrLabel, String gameInstrDuration, String gameMode, String stage) {
            this.number = gameNumber;
            this.country = gameCountry;
            this.level = gameLevel;
            this.color = gameColor;
            this.instructionAudioName = gameInstrLabel;
            this.instructionDuration = gameInstrDuration;
            this.mode = gameMode;
            this.stage = stage;
        }

        public boolean hasNull() {
            return number == null || country == null || level == null || color == null || instructionAudioName == null || instructionDuration == null || mode == null;
        }
    }

    public static class WordList extends ArrayList<Word> {
        public String wordInLWCTitle;    // e.g. languages like English or Spanish (LWCs = Languages of Wider Communication)
        public String wordInLOPTitle;    // e.g. LOPS (language of play) like Me'phaa, Kayan or Romani Gabor
        public String durationTitle;    // the length of the clip in ms, relevant only if set to use SoundPool
        public String mixedDefsTitle;    // for languages with multi-function symbols (e.g. in the word <niwan'>, the first |n| is a consontant and the second |n| is a nasality indicator
        public String adjustmentTitle;    // a font-specific reduction in size for words with longer pixel width
        public String stageOfFirstAppearanceTitle; // an option indicator to override the default tile-based staging and assign this word to first appear in a certain stage

        public int numberOfWordsForActiveTile(Tile activeTile, int scanSetting) {
            // Scan setting 1: Words that start with the active tile
            // Scan setting 2: Part of getting word groups in scan setting 2 is getting words that contain the active tile, but not in starting position
            // Scan setting 3: Words that contain the active tile anywhere

            String activeTileType = activeTile.typeOfThisTileInstance;

            if(scriptType.matches("(Thai|Lao|Khmer)") && activeTileType.matches("LV") && scanSetting==1){
                return 0;
            }

            int wordCount = 0;
            for (int i = 0; i < size(); i++) {
                ArrayList<Tile> parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).wordInLOP, get(i));
                int t = 0;
                if(scriptType.matches("(Thai|Lao|Khmer)") && scanSetting==1) { // Find first sound tile (not LV, which is pronounced after the consonant it precedes)
                    Tile initialTile;
                    String initialTileType = "LV";
                    t = -1;
                    while (initialTileType.equals("LV") && t < parsedWordArrayFinal.size()) {
                        t++;
                        initialTile = parsedWordArrayFinal.get(t);
                        initialTileType = initialTile.typeOfThisTileInstance;
                    }
                }

                int startingScanIndex;
                int endingScanIndex;
                switch (scanSetting) {
                    case 1:
                        startingScanIndex = t; // t is the index of the first non-LV-type tile (So, C, V, etc; no leading vowels).
                        endingScanIndex = t+1;
                        break;
                    case 2:
                        startingScanIndex = 1;
                        endingScanIndex = parsedWordArrayFinal.size();
                        break;
                    default:
                        startingScanIndex = 0;
                        endingScanIndex = parsedWordArrayFinal.size();
                }

                for (int k = startingScanIndex; k < endingScanIndex; k++) {
                    Tile tileInFocus = parsedWordArrayFinal.get(k);
                    String tileInFocusType = tileInFocus.typeOfThisTileInstance;
                    if (tileInFocus.text.equals(activeTile.text)) {
                        if(differentiatesTileTypes){
                            if (tileInFocusType.equals(activeTileType)) {
                                wordCount++;
                                break; // Add each word only once, even if it contains the active tile more than once
                            }
                        } else { // Don't differentiate types; simply match tile to tile
                            wordCount++;
                            break; // Add each word only once, even if it contains the active tile more than once
                        }
                    }
                }
            }
            return wordCount;
        }

        public Word[] wordsForActiveTile(Tile activeTile, int wordCount, int scanSetting) {
            // Scan setting 1: Words that start with the active tile
            // Scan setting 2: Part of getting word groups in scan setting 2 is getting words that contain the active tile, but not in starting position
            // Scan setting 3: Words that contain the active tile anywhere
            Word[] wordsForActiveTile = new Word[wordCount];

            ArrayList<Tile> parsedWordArrayFinal;
            Tile tileInFocus;

            int hitsCounter = 0;
            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).wordInLOP, get(i));
                int t = 0;
                if(scriptType.matches("(Thai|Lao|Khmer)") && scanSetting==1) { // Find first sound tile (not LV, which is pronounced after the consonant it precedes)
                    Tile initialTile;
                    String initialTileType = "LV";
                    t = -1;
                    while (initialTileType.equals("LV") && t < parsedWordArrayFinal.size()) {
                        t++;
                        initialTile = parsedWordArrayFinal.get(t);
                        initialTileType = initialTile.typeOfThisTileInstance;
                    }
                }

                int startingScanIndex;
                int endingScanIndex;
                switch (scanSetting) {
                    case 1:
                        startingScanIndex = t; // t is the index of the first non-LV-type tile (So, C, V, etc; no leading vowels).
                        endingScanIndex = t+1;
                        break;
                    case 2:
                        startingScanIndex = 1;
                        endingScanIndex = parsedWordArrayFinal.size();
                        break;
                    default:
                        startingScanIndex = 0;
                        endingScanIndex = parsedWordArrayFinal.size();
                }

                for (int k = startingScanIndex; k < endingScanIndex; k++) {
                    tileInFocus = parsedWordArrayFinal.get(k);
                    if (tileInFocus.text.equals(activeTile.text)) {
                        if (differentiatesTileTypes) { // Check if both tile and type match
                            if (tileInFocus.typeOfThisTileInstance.equals(activeTile.typeOfThisTileInstance)) {
                                wordsForActiveTile[hitsCounter] = get(i);
                                hitsCounter++;
                                break; // Add each word only once, even if it contains the active tile more than once
                            }
                        } else { // Don't differentiate types; simply match tile to tile
                            wordsForActiveTile[hitsCounter] = get(i);
                            hitsCounter++;
                            break; // Add each word only once, even if it contains the active tile more than once
                        }
                    }

                }
            }
            return wordsForActiveTile;
        }

        public String stripInstructionCharacters(String wordInLOP) {
            // The period instructs the parseWord method to force a tile break
            String newString = wordInLOP.replaceAll("[.]", "");
            return newString;
        }


        public int returnPositionInWordList(String someLWCWord) {
            int wordPosition = 0;
            for (int i = 0; i < size(); i++) {
                if (get(i).wordInLWC.equals(someLWCWord)) {
                    wordPosition = i;
                }
            }
            return wordPosition;
        }


        public ArrayList<Word> returnFourWords(Word refWord, Tile refTile, int challengeLevel, String refType) {

            ArrayList<Word> fourWordChoices = new ArrayList<>();
            // Note that the following are four non-overlapping groups: easyWords, moderateWords, hardWords, wordInLOP
            ArrayList<Word> easyWords = new ArrayList<>();        // words that do not begin with same tile or with distractor tile
            ArrayList<Word> moderateWords = new ArrayList<>();    // words that begin with distractor tiles
            ArrayList<Word> hardWords = new ArrayList<>();        // words that begin with the same tile (but excluding wordInLOP)
            ArrayList<Tile> aWordParsedIntoTiles;

            fourWordChoices.add(refWord);

            String alt1lower = refTile.distractors.get(0);
            String alt2lower = refTile.distractors.get(1);
            String alt3lower = refTile.distractors.get(2);

            String alt1;
            String alt2;
            String alt3;

            if (refType.equals("TILE_UPPER")) {
                alt1 = tileHashMap.find(alt1lower).upper;
                alt2 = tileHashMap.find(alt2lower).upper;
                alt3 = tileHashMap.find(alt3lower).upper;

            } else {
                alt1 = alt1lower;
                alt2 = alt2lower;
                alt3 = alt3lower;
            }

            for (int i = 0; i < wordList.size(); i++) {
                Word aWord = wordList.get(i);
                aWordParsedIntoTiles = tileList.parseWordIntoTiles(aWord.wordInLOP, aWord);
                String firstTileString = aWordParsedIntoTiles.get(0).text;

                if (!firstTileString.equals(refTile.text) && !firstTileString.equals(alt1) && !firstTileString.equals(alt2) && !firstTileString.equals(alt3)) {
                    if (!refWord.wordInLOP.equals(aWord.wordInLOP)) {
                        easyWords.add(aWord);
                    }
                }

                if (firstTileString.equals(alt1) || firstTileString.equals(alt2) || firstTileString.equals(alt3)) {
                    if (!refWord.wordInLOP.equals(aWord.wordInLOP)) {
                        moderateWords.add(aWord);
                    }
                }

                if (firstTileString.equals(refTile.text) && !refWord.wordInLOP.equals(aWord.wordInLOP)) {
                        hardWords.add(aWord);
                }

            }
            Collections.shuffle(easyWords);
            Collections.shuffle(moderateWords);
            Collections.shuffle(hardWords);

            if (challengeLevel == 1) {
                // Use easy words
                // ASSUMING that there will always be three words that do not start with refTile or distractor tiles
                // Since problematic tiles may not be included in distractor tiles for certain languages, always need to check using while loop

                for (int i = 0; i < 3; i++) {
                    //JP edits to fix c vs ch issue:
                    if (refType.equals("TILE_UPPER") || refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) { //conditions where c vs ch conflicts can occur
                        Word aWordChoice = easyWords.get(i);
                        aWordParsedIntoTiles = Start.tileList.parseWordIntoTiles(aWordChoice.wordInLOP, aWordChoice);
                        Tile firstTile = aWordParsedIntoTiles.get(0);

                        while ((Character.toLowerCase(firstTile.text.charAt(0)) == Character.toLowerCase(refTile.text.charAt(0))) && (firstTile.text.length() > refTile.text.length())
                                || fourWordChoices.contains(aWordChoice)) { // Loops continues until a non-conflicting tile is chosen
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());
                            aWordChoice = easyWords.get(rand1);
                            aWordParsedIntoTiles = Start.tileList.parseWordIntoTiles(aWordChoice.wordInLOP, aWordChoice);
                            firstTile = aWordParsedIntoTiles.get(0);
                        }
                        fourWordChoices.add(aWordChoice);
                    } else {
                        fourWordChoices.add(easyWords.get(i));
                    }
                }

            } else if (challengeLevel == 2) {
                // use moderate words and if the supply runs out use easy words

                for (int i = 0; i < 3; i++) {
                    //JP: edits to try to fix c vs ch issue;
                    if (refType.equals("TILE_UPPER") || refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) { //conditions where c vs ch conflicts can occur
                        Word aWordChoice;
                        Tile firstTile;
                        if (moderateWords.size() > i) {
                            // First try to simply get a moderate word if there are enough moderate words
                            aWordChoice = moderateWords.get(i);
                        } else {
                            // If there are not enough moderate words go straight to trying a random easy word
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());
                            aWordChoice = easyWords.get(rand1);
                        }
                        aWordParsedIntoTiles = Start.tileList.parseWordIntoTiles(aWordChoice.wordInLOP, aWordChoice);
                        firstTile = aWordParsedIntoTiles.get(0); //should be tile

                        // Then test whether this possible word is problematic, and if so, replace it with a (different) random easy word.
                        // The random easy word also needs to be tested, since some languages may have instances where tiles "c" and "ch" both exist
                        // But one is not listed as a distractor tile of the other
                        while ((Character.toLowerCase(firstTile.text.charAt(0)) == Character.toLowerCase(refTile.text.charAt(0))) && (firstTile.text.length() > refTile.text.length())) {
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());
                            aWordChoice = easyWords.get(rand1);
                            aWordParsedIntoTiles = Start.tileList.parseWordIntoTiles(aWordChoice.wordInLOP, aWordChoice);
                            firstTile = aWordParsedIntoTiles.get(0);
                        }

                        // After those tests, the possible word has been validated and can be added to the answer choices
                        fourWordChoices.add(aWordChoice);
                    } else { // Ref is a word or picture
                        if (moderateWords.size() > i) {
                            fourWordChoices.add(moderateWords.get(i));
                        } else {
                            fourWordChoices.add(easyWords.get(i - moderateWords.size()));
                        }
                    }
                }
            } else if (challengeLevel == 3) {
                // Use hard words and if the supply runs out use moderate words and if the supply runs out use easy words
                for (int i = 0; i < 3; i++) {
                    if (hardWords.size() > i) {
                        fourWordChoices.add(hardWords.get(i));
                    } else {
                        if (moderateWords.size() > (i - hardWords.size())) {
                            fourWordChoices.add(moderateWords.get(i - hardWords.size()));
                        } else {
                            fourWordChoices.add(easyWords.get(i - hardWords.size() - moderateWords.size()));
                        }
                    }
                }

            }
            Collections.shuffle(fourWordChoices);
            return fourWordChoices;
        }
    }

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
    public static class Syllable extends WordPiece {
        public int duration;
        public String color;


        public Syllable(String text, ArrayList<String> distractors, String audioName, int duration, String color) {
            super(text);
            this.text = text;
            this.distractors = distractors;
            this.audioName = audioName;
            this.duration = duration;
            this.color = color;
        }


        public boolean hasNull() {
            return text == null || distractors.isEmpty() || audioName == null || color == null;
        }
    }

    public static class SyllableList extends ArrayList<Syllable> {
        public String syllableTitle;
        public String[] distractorsTitles;
        public String syllableAudioNameTitle;
        public String syllableDurationTitle;
        public String colorTitle;

        public ArrayList<Syllable> parseWordIntoSyllables(Word refWord) {
            ArrayList<Syllable> parsedWordArrayTemp = new ArrayList();
            StringTokenizer st = new StringTokenizer(refWord.wordInLOP, ".");
            while (st.hasMoreTokens()) {
                parsedWordArrayTemp.add(syllableHashMap.find(st.nextToken()));
            }
            return parsedWordArrayTemp;
        }

        public Syllable returnRandomDistractorSyllable(Syllable correctSyllable) {

            Random rand = new Random();
            int randomNum = rand.nextInt(3);
            return syllableHashMap.find(correctSyllable.distractors.get(randomNum));
        }

        public ArrayList<Word> returnFourWordChoices(String refSyllableString, int challengeLevel) {
            ArrayList<Syllable> wordChoiceParsedIntoSyllables;
            Word aWordChoice;
            ArrayList<Word> fourWordChoices = new ArrayList<>();
            Set<String> wordsAddedStrings = new HashSet<>(); // Prevents repeats
            Syllable refSyllable = syllableHashMap.find(refSyllableString);
            Random rand = new Random();
            boolean foundCorrespondingWord = false;

            // Get a word that starts with the refSyllableString
            while (!foundCorrespondingWord) {
                int randomNum = rand.nextInt(wordList.size());
                aWordChoice = wordList.get(randomNum);
                wordChoiceParsedIntoSyllables = syllableList.parseWordIntoSyllables(aWordChoice);
                if (wordChoiceParsedIntoSyllables.get(0).text.equals(refSyllableString)) {
                    fourWordChoices.add(aWordChoice);
                    wordsAddedStrings.add(aWordChoice.wordInLOP);
                    foundCorrespondingWord = true;
                }
            }

            if (challengeLevel == 1) { // Add three other words without same initial syllable or distractor syllables word-initially
                while (fourWordChoices.size() < 4) {
                    int randomNum = rand.nextInt(wordList.size());
                    aWordChoice = wordList.get(randomNum);
                    wordChoiceParsedIntoSyllables = syllableList.parseWordIntoSyllables(aWordChoice);
                    if (!wordChoiceParsedIntoSyllables.get(0).text.equals(refSyllableString) && !refSyllable.distractors.contains(wordChoiceParsedIntoSyllables.get(0).text)) {
                        if (!wordsAddedStrings.contains(aWordChoice.wordInLOP)) {
                            fourWordChoices.add(aWordChoice);
                            wordsAddedStrings.add(aWordChoice.wordInLOP);
                        }
                    }
                }
            } else if (challengeLevel == 2) { // Add three medium-challenge words that start with the distractor syllables
                int count = 0;
                while (fourWordChoices.size() < 4 && count < wordList.size()) {
                    int randomNum = rand.nextInt(wordList.size());
                    aWordChoice = wordList.get(randomNum);
                    wordChoiceParsedIntoSyllables = syllableList.parseWordIntoSyllables(aWordChoice);
                    if (!wordChoiceParsedIntoSyllables.get(0).text.equals(refSyllableString) && refSyllable.distractors.contains(wordChoiceParsedIntoSyllables.get(0).text)) {
                        if (!wordsAddedStrings.contains(aWordChoice.wordInLOP)) {
                            fourWordChoices.add(aWordChoice);
                            wordsAddedStrings.add(aWordChoice.wordInLOP);
                        }
                    }
                    count++;
                }
                while (fourWordChoices.size() < 4) {
                    int randomNum = rand.nextInt(wordList.size());
                    aWordChoice = wordList.get(randomNum);
                    wordChoiceParsedIntoSyllables = syllableList.parseWordIntoSyllables(aWordChoice);
                    if (!wordChoiceParsedIntoSyllables.get(0).text.equals(refSyllableString)) {
                        if (!wordsAddedStrings.contains(aWordChoice.wordInLOP)) {
                            fourWordChoices.add(aWordChoice);
                            wordsAddedStrings.add(aWordChoice.wordInLOP);
                        }
                    }
                }
            }
            Collections.shuffle(fourWordChoices);
            return fourWordChoices;
        }

        public ArrayList<Syllable> returnFourSyllableChoices(String refSyllableString, int challengeLevel) {
            ArrayList<Syllable> fourSyllableChoices = new ArrayList<>();
            Syllable refSyllable = syllableHashMap.find(refSyllableString);
            Set<String> syllablesAddedStrings = new HashSet<>(); // Prevents repeats
            Syllable aSyllableChoice;
            Random rand = new Random();
            fourSyllableChoices.add(refSyllable);
            syllablesAddedStrings.add(refSyllable.text);
            if (challengeLevel == 1) { // Random wrong syllables
                while (fourSyllableChoices.size() < 4) {
                    int randomNum = rand.nextInt(syllableList.size());
                    aSyllableChoice = syllableList.get(randomNum);
                    if (!aSyllableChoice.text.equals(refSyllableString) && !refSyllable.distractors.contains(aSyllableChoice.text)) {
                        syllablesAddedStrings.add(aSyllableChoice.text);
                        fourSyllableChoices.add(aSyllableChoice);
                    }
                }
            } else if (challengeLevel == 2) { // Distractor syllables
                if (!syllablesAddedStrings.contains(refSyllable.distractors.get(0))) {
                    syllablesAddedStrings.add(refSyllable.distractors.get(0));
                    fourSyllableChoices.add(syllableHashMap.find(refSyllable.distractors.get(0)));
                }
                if (!syllablesAddedStrings.contains(refSyllable.distractors.get(1))) {
                    syllablesAddedStrings.add(refSyllable.distractors.get(1));
                    fourSyllableChoices.add(syllableHashMap.find(refSyllable.distractors.get(1)));
                }
                if (!syllablesAddedStrings.contains(refSyllable.distractors.get(2))) {
                    syllablesAddedStrings.add(refSyllable.distractors.get(2));
                    fourSyllableChoices.add(syllableHashMap.find(refSyllable.distractors.get(2)));
                }
                while (fourSyllableChoices.size() < 4) {
                    int randomNum = rand.nextInt(syllableList.size());
                    aSyllableChoice = syllableList.get(randomNum);
                    if (!aSyllableChoice.text.equals(refSyllableString) && !syllablesAddedStrings.contains(aSyllableChoice.text)) {
                        syllablesAddedStrings.add(aSyllableChoice.text);
                        fourSyllableChoices.add(aSyllableChoice);
                    }
                }
            }
            Collections.shuffle(fourSyllableChoices);
            return fourSyllableChoices;
        }

        public int returnPositionInSyllableList(String someSyllable) {
            int alphabetPosition = 0;
            for (int i = 0; i < size(); i++) {

                if (get(i).text.equals(someSyllable)) {
                    alphabetPosition = i;
                }
            }
            return alphabetPosition;
        }

    }

    public static class TileList extends ArrayList<Tile> {
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
        public String tileDuration1Title;
        public String tileDuration2Title;
        public String tileDuration3Title;

        public String stageOfFirstAppearanceTitle;
        public String stageOfFirstAppearanceTitleType2;
        public String stageOfFirstAppearanceTitleType3;

        public boolean contains(Tile tile) {
            for(int t=0; t<size(); t++) {
                if(get(t).text.equals(tile.text) && get(t).typeOfThisTileInstance.equals(tile.typeOfThisTileInstance)) {
                    return true;
                }
            }
            return false;
        }

        public ArrayList<Tile> parseWordIntoTiles (String stringToParse, Word referenceWord) {
            ArrayList<Tile> parsedWordArrayPreliminary = parseWordIntoTilesPreliminary(stringToParse, referenceWord);
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
                        if(tileHashMap.containsKey(vowelStringSoFar)){ // Vowel composite so far is parsable as one tile in the tile list
                            if(vowelTypeSoFar.equals("LV")){
                                if(!vowelStringSoFar.endsWith("◌")){
                                    vowelStringSoFar += "◌";
                                }
                            } else if (vowelTypeSoFar.matches("(AV|BV|FV)") && !vowelStringSoFar.startsWith("◌")){
                                vowelStringSoFar = "◌" + vowelStringSoFar; // Put the placeholder before the previous AV/BV/FV before adding current AV/BV/FV to it
                            }
                        }
                        if (vowelStringSoFar.contains("◌") && currentTileString.contains("◌")) {
                            currentTileString = currentTileString.replace("◌", ""); // Just want one ◌
                        }
                        vowelStringSoFar += currentTileString;
                        if (vowelStringSoFar.equals(currentTileString)) { // the vowel so far is a preliminary tile
                            vowelTypeSoFar = currentTileType;
                        } else if (tileHashMap.containsKey(vowelStringSoFar)) { // complex tiles do not get multityping
                            vowelTypeSoFar = tileHashMap.find(vowelStringSoFar).tileType;
                        }
                    } else if (currentTileType.matches("(AD|D)")) { // Save any AD (Above/After Diacritics) or other Diacritics between consonants
                        if(!diacriticStringSoFar.isEmpty() && !diacriticStringSoFar.contains("◌")){
                            diacriticStringSoFar = "◌" + diacriticStringSoFar; // For complex diacritics
                        }
                        if(diacriticStringSoFar.contains("◌") && currentTileString.contains("◌")) { // Just want one ◌
                            currentTileString = currentTileString.replace("◌", "");
                        }
                        diacriticStringSoFar+=currentTileString;
                    } else if (currentTileType.equals("SAD")) { // Save any Space-And-Dash chars that comes between syllables.
                        SADTiles.add(currentTile);
                    } else if (!foundNextConsonant && currentTileType.equals("V")){ // There is a V (not LV/FV/AV/BV) on the end of the word
                        nonComplexV = currentTile;
                    }
                }


                // Add saved items to the tile array
                if(!(nonCombiningVowelFromPreviousSyllable==null)){
                    parsedWordTileArray.add(nonCombiningVowelFromPreviousSyllable);
                }
                if (!(currentConsonant==null)) {
                    // Combine diacritics with consonant if that combination is in the tileList. Ex:บ๋
                    if (!diacriticStringSoFar.isEmpty() && tileHashMap.containsKey(currentConsonant.text + diacriticStringSoFar.replace("◌", ""))) {
                        currentConsonant = tileHashMap.find(currentConsonant.text + diacriticStringSoFar.replace("◌", ""));
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
        public ArrayList<Tile> parseWordIntoTilesPreliminary (String stringToParse, Word referenceWord) {
            // Updates by KP, Oct 2020
            // AH, Nov 2020, extended to check up to four characters in a game tile
            ArrayList<Tile> referenceWordStringPreliminaryTileArray = new ArrayList<>();
            ArrayList<Tile> referenceWordStringPreliminaryTileArrayFinal = new ArrayList<>();

            // Parse the reference word first
            String refString = referenceWord.wordInLOP;
            int charBlockLength;
            String next1Chars;
            String next2Chars;
            String next3Chars;
            String next4Chars;
            int tileIndex = 0;

            for (int i = 0; i < refString.length(); i++) {

                // Create blocks of the next one, two, three and four Unicode characters for analysis
                next1Chars = refString.substring(i, i + 1);

                if (i < refString.length() - 1) {
                    next2Chars = refString.substring(i, i + 2);
                } else {
                    next2Chars = "XYZXYZ";
                }

                if (i < refString.length() - 2) {
                    next3Chars = refString.substring(i, i + 3);
                } else {
                    next3Chars = "XYZXYZ";
                }

                if (i < refString.length() - 3) {
                    next4Chars = refString.substring(i, i + 4);
                } else {
                    next4Chars = "XYZXYZ";
                }

                // See if the blocks of length one, two, three or four Unicode characters matches game tiles
                // Choose the longest block that matches a game tile and add that as the next segment in the parsed word array
                charBlockLength = 0;
                if (tileHashMap.containsKey(next1Chars) || tileHashMap.containsKey("◌" + next1Chars) || tileHashMap.containsKey(next1Chars + "◌")) {
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
                        if (tileHashMap.containsKey(next1Chars)){
                            tileString = next1Chars;
                        } else if (tileHashMap.containsKey("◌" + next1Chars)) { // For AV/BV/FV/AD/D stored with ◌
                            tileString = "◌" + next1Chars;
                        } else if (tileHashMap.containsKey(next1Chars + "◌")) { // For LV stored with ◌
                            tileString = next1Chars + "◌";
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
                if(!tileString.isEmpty()) {
                    Tile nextTile = tileHashMap.find(tileString);
                    referenceWordStringPreliminaryTileArray.add(nextTile);
                }
            }
            for (Tile tile : referenceWordStringPreliminaryTileArray) { // Set instance-specific fields
                Tile nextTile = new Tile(tile);
                if (MULTITYPE_TILES.contains(nextTile.text)) {
                    nextTile.typeOfThisTileInstance = getInstanceTypeForMixedTilePreliminary(tileIndex, referenceWordStringPreliminaryTileArray, referenceWord);
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
                    referenceWordStringPreliminaryTileArrayFinal.add(nextTile);
                } else {
                    nextTile.typeOfThisTileInstance = nextTile.tileType;
                    nextTile.stageOfFirstAppearanceForThisTileType = nextTile.stageOfFirstAppearance;
                    nextTile.audioForThisTileType = nextTile.audioName;
                    referenceWordStringPreliminaryTileArrayFinal.add(nextTile);
                }
                tileIndex++;
            }

            if (stringToParse.equals(refString)) { // DONE! Return the parsed correct word array.
                return referenceWordStringPreliminaryTileArrayFinal;
            } else { // Parse the stringToParse. Compare it with the correct parsing and roughly set the tiles' types.
                ArrayList<Tile> stringToParsePreliminaryTileArray = new ArrayList<>();

                for (int i = 0; i < stringToParse.length(); i++) {

                    // Create blocks of the next one, two, three and four Unicode characters for analysis
                    next1Chars = stringToParse.substring(i, i + 1);

                    if (i < stringToParse.length() - 1) {
                        next2Chars = stringToParse.substring(i, i + 2);
                    } else {
                        next2Chars = "XYZXYZ";
                    }

                    if (i < stringToParse.length() - 2) {
                        next3Chars = stringToParse.substring(i, i + 3);
                    } else {
                        next3Chars = "XYZXYZ";
                    }

                    if (i < stringToParse.length() - 3) {
                        next4Chars = stringToParse.substring(i, i + 4);
                    } else {
                        next4Chars = "XYZXYZ";
                    }

                    // See if the blocks of length one, two, three or four Unicode characters matches game tiles
                    // Choose the longest block that matches a game tile and add that as the next segment in the parsed word array
                    charBlockLength = 0;
                    if (tileHashMap.containsKey(next1Chars) || tileHashMap.containsKey("◌" + next1Chars) || tileHashMap.containsKey(next1Chars + "◌")) {
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
                            if (tileHashMap.containsKey(next1Chars)){
                                tileString = next1Chars;
                            } else if (tileHashMap.containsKey("◌" + next1Chars)) { // For AV/BV/FV/AD/D stored with ◌
                                tileString = "◌" + next1Chars;
                            } else if (tileHashMap.containsKey(next1Chars + "◌")) { // For LV stored with ◌
                                tileString = next1Chars + "◌";
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
                    Tile nextTile = tileHashMap.find(tileString);
                    stringToParsePreliminaryTileArray.add(new Tile(nextTile));
                }

                // Compare parsed tile arrays of stringToParse and referenceWord at the same indexes

                for(int parsedTileIndex = 0; parsedTileIndex<stringToParsePreliminaryTileArray.size(); parsedTileIndex++) { // Set instance-specific fields
                    Start.Tile thisParsedTile = stringToParsePreliminaryTileArray.get(parsedTileIndex);
                    if (parsedTileIndex < referenceWordStringPreliminaryTileArray.size()
                            && thisParsedTile.text.equals(referenceWordStringPreliminaryTileArray.get(parsedTileIndex).text)) {
                        thisParsedTile.typeOfThisTileInstance = referenceWordStringPreliminaryTileArray.get(parsedTileIndex).typeOfThisTileInstance;
                        thisParsedTile.audioForThisTileType = referenceWordStringPreliminaryTileArray.get(parsedTileIndex).audioForThisTileType;
                        thisParsedTile.stageOfFirstAppearanceForThisTileType = referenceWordStringPreliminaryTileArray.get(parsedTileIndex).stageOfFirstAppearance;
                    } else { // Use defaults, even for multitype symbols. There's no indication of type when tiles are used distinctly from reference.
                        thisParsedTile.typeOfThisTileInstance = thisParsedTile.tileType;
                        thisParsedTile.audioForThisTileType = thisParsedTile.audioName;
                        thisParsedTile.stageOfFirstAppearanceForThisTileType = thisParsedTile.stageOfFirstAppearance;
                    }
                }
                return stringToParsePreliminaryTileArray;
            }
        }

        public Tile returnNextTile(Tile oldTile) {

            Tile nextTile = null;
            int i = 0;
            while (!(get(i).text.equals(oldTile.text) && get(i).typeOfThisTileInstance.equals(oldTile.typeOfThisTileInstance))) {
                i++;
            }
            if(i == size()-1){
                nextTile = get(0);
            } else {
                nextTile = get(i + 1);
            }
            return nextTile;
        }

        public Tile returnPreviousTile(Tile oldTile) {
            Tile previousTile = null;
            int i = size()-1;
            while (!(get(i).text.equals(oldTile.text) && get(i).typeOfThisTileInstance.equals(oldTile.typeOfThisTileInstance))) {
                i--;
            }
            if (i > 0){
                previousTile = get(i-1);
            } else {
                previousTile = get(size()-1);
            }
            return previousTile;
        }

        public int returnPositionInAlphabet(Tile someGameTile) {

            int alphabetPosition = 0;
            for (int i = 0; i < size(); i++) {

                if (get(i).text.equals(someGameTile.text)) {
                    alphabetPosition = i;
                }
                if (get(i).upper.equals(someGameTile.text)) {
                    alphabetPosition = i;
                }
            }

            return alphabetPosition;

        }

        public Tile returnRandomDistractorTile(Tile correctTile) {

            Random rand = new Random();
            int randomNum = rand.nextInt(ALT_COUNT);
            return tileHashMap.find(correctTile.distractors.get(randomNum));
        }

        public ArrayList<Tile> returnFourTileChoices(Tile correctTile, int challengeLevel, String refTileType) {

            ArrayList<Tile> fourTileChoices = new ArrayList();
            ArrayList<String> alreadyStoredAnswerChoices = new ArrayList<>();

            Tile aTile = correctTile;
            alreadyStoredAnswerChoices.add(aTile.text);
            fourTileChoices.add(aTile); // Add the correct tile

            if (challengeLevel == 1) { // Use random tiles for the other choices
                Random rand = new Random();

                for(int i=0; i<3; i++){
                    boolean firstIteration = true;
                    while (firstIteration
                            || alreadyStoredAnswerChoices.contains(aTile.text)
                            || Character.toLowerCase(correctTile.text.charAt(0)) == Character.toLowerCase(aTile.text.charAt(0))
                            || (!aTile.typeOfThisTileInstance.matches("(C|PC)") && !aTile.typeOfThisTileInstance.equals("V")
                            && (refTileType.matches(("(C|PC)")) || refTileType.equals("V")))) {
                        int randomTileIndex = rand.nextInt(tileListNoSAD.size());
                        aTile = Start.tileListNoSAD.get(randomTileIndex);
                        firstIteration = false;
                    }
                    alreadyStoredAnswerChoices.add(aTile.text);
                    fourTileChoices.add(aTile);
                }

            } else if (challengeLevel == 2) { // Use distractor tiles
                for (int i = 0; i < 3; i++) {
                    aTile = tileHashMap.find(correctTile.distractors.get(i));
                    // If a distractor alternative does not meet specifications, replace with random alternative
                    if(((Character.toLowerCase(aTile.text.charAt(0)) == Character.toLowerCase(correctTile.text.charAt(0))) && aTile.text.length() < correctTile.text.length())
                            || ((refTileType.matches("(C|PC)") || refTileType.equals("V")) && (!aTile.typeOfThisTileInstance.matches("(C|PC)") && !aTile.typeOfThisTileInstance.equals("V")))) {
                        while ((Character.toLowerCase(aTile.text.charAt(0)) == Character.toLowerCase(correctTile.text.charAt(0)))
                                || correctTile.distractors.contains(aTile.text)
                                || ((refTileType.matches("(C|PC)") || refTileType.equals("V")) && (!aTile.typeOfThisTileInstance.matches("(C|PC)") && !aTile.typeOfThisTileInstance.equals("V")))) {
                            Random rand = new Random();
                            int rand5 = rand.nextInt(tileListNoSAD.size());
                            aTile = tileListNoSAD.get(rand5);
                        }
                    }
                    fourTileChoices.add(aTile);
                }
            }
            Collections.shuffle(fourTileChoices);
            return fourTileChoices;

        }

        public String getInstanceTypeForMixedTilePreliminary(int index, ArrayList<Tile> tilesInWordPreliminary, Word wordListWord) {
            // if mixedDefinitionInfo is not C or V or X or dash, then we assume it has two elements
            // to disambiguate, e.g. niwan', where...
            // first n is a C and second n is a X (nasality indicator), and we would code as C234X6

            // JP: these types come from the gametiles
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
                for(int i=0; i<numTilesInWord; i++){
                    String number = String.valueOf(i+1);
                    if(mixedDefinitionInfoString.contains(number)){
                        mixedDefinitionInfoArray[i] = number;
                    }
                }
                // Now store what's in between the numbers (the type info parts)
                int previousNumberEndIndex = 0;
                int nextNumberStartIndex;
                for(int i=0; i<numTilesInWord; i++){
                    String nextNumber = String.valueOf(i+2); // The number after this one, 1-indexed
                    int tilesInBetween = 1;

                    if(mixedDefinitionInfoArray[i] == null){ // A number wasn't filled in here, there must be type info for this tile
                        nextNumberStartIndex = mixedDefinitionInfoString.indexOf(nextNumber);
                        int nextNumberInt = Integer.parseInt(nextNumber);
                        while(nextNumberStartIndex==-1 && nextNumberInt<=numTilesInWord){ // Maybe the number after this one is not in the array, either. Find the next one that is.
                            nextNumberInt++;
                            nextNumber = String.valueOf(nextNumberInt);
                            nextNumberStartIndex = mixedDefinitionInfoString.indexOf(nextNumber);
                            tilesInBetween++;
                        }
                        if (nextNumberStartIndex==-1 || nextNumberInt>numTilesInWord){ // It checked to the end and didn't find any more numbers
                            nextNumberStartIndex = mixedDefinitionInfoString.length();
                        }

                        String infoBetweenPreviousAndNextNumbers = mixedDefinitionInfoString.substring(previousNumberEndIndex, nextNumberStartIndex);
                        if(tilesInBetween==1){ // The substring is the type of the ith preliminary tile
                            mixedDefinitionInfoArray[i] = mixedDefinitionInfoString.substring(previousNumberEndIndex, nextNumberStartIndex);
                        } else { // The first type in substring is the type of the ith preliminary tile
                            String type = "";
                            for(int c=1; c<infoBetweenPreviousAndNextNumbers.length(); c++){
                                String firstC = infoBetweenPreviousAndNextNumbers.substring(0, c);
                                if(types.contains(firstC)){
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

    }

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

    public class SyllableHashMap extends HashMap<String, Syllable> {

        public Syllable find(String key) {
            for (String k : keySet()) {
                if (k.equals(key)) {
                    return (get(k));
                }
            }
            return null;
        }

    }

    public class WordHashMap extends HashMap<String, Word> {

        public Word find(String key) {
            for (String k : keySet()) {
                if (k.equals(key)) {
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

        public String gameNumberTitle;
        public String gameCountryTitle;
        public String gameLevelTitle;
        public String gameColorTitle;
        public String gameInstrLabelTitle;
        public String gameInstrDurationTitle;
        public String gameModeTitle;
        public String gameStageTitle;

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
