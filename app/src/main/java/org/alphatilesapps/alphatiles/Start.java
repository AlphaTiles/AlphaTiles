package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
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
    public static ArrayList<ArrayList<String>> tileStagesLists; // LM // For staged introduction of tiles

    public static TileList tileListNoSAD; // JP // from aa_gametiles.txt minus SAD types

    public static TileListWithMultipleTypes tileListWithMultipleTypes;
    public static TileListWithMultipleTypes tileListWithMultipleTypesNoSAD;

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

    public static TileTypeHashMapWithMultipleTypes tileTypeHashMapWithMultipleTypes;

    public static TileTypeHashMapWithMultipleTypes tileTypeHashMapWithMultipleTypesNoSAD;

    public static WordHashMap wordHashMap;

    public static SyllableHashMap syllableHashMap; //JP

    public static SoundPool gameSounds;
    public static int correctSoundID;
    public static int incorrectSoundID;
    public static int correctFinalSoundID;
    public static HashMap<String, Integer> wordAudioIDs;
    public static HashMap<String, Integer> tileAudioIDs;
    public static HashMap<String, Integer> syllableAudioIDs; //JP
    public static int correctSoundDuration;

    public static HashMap<String, Integer> wordDurations;
    public static HashMap<String, Integer> tileDurations;
    public static HashMap<String, Integer> syllableDurations;
    public static final ArrayList<String> COLORS = new ArrayList<>();
    public static int totalAudio; //JP: the total number of audio files to be loaded into the soundpool

    public static Boolean hasTileAudio;
    public static Boolean hasSyllableAudio;
    public static Boolean hasSyllableGames = false;
    public static int after12checkedTrackers;
    public static Boolean differentiateTypes;
    public static Boolean hasSAD = false;
    public static double stageCorrespondenceRatio;

    public static int numberOfAvatars = 12;

    public static List<String> CONSONANTS = new ArrayList<>();
    public static List<String> VOWELS = new ArrayList<>();
    public static List<String> CorV = new ArrayList<>();
    public static List<String> TONES = new ArrayList<>();
    public static List<String> SAD = new ArrayList<>();
    public static List<String> SYLLABLES = new ArrayList<>();
    public static List<String> MULTIFUNCTIONS = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger( Start.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        totalAudio = 3; // JP: how many total audio files to load
        // will be used in LoadingScreen.java to determine when all audio files have loaded -> advance to ChoosePlayer
        // initialize to 3 for correct, incorrect, and correctFinal sounds

        buildLangInfoArray();
        LOGGER.info("LoadProgress: completed buildLangInfoArray()");
        buildKeysArray();
        LOGGER.info("LoadProgress: completed buildKeysArray()");
        buildSettingsArray();
        LOGGER.info("LoadProgress: completed buildSettingsArray()");
        buildColorsArray();
        LOGGER.info("LoadProgress: completed buildColorsArray()");

        String hasAudioSetting = settingsList.find("Has tile audio");
        if (!hasAudioSetting.equals("")) {
            hasTileAudio = Boolean.parseBoolean(hasAudioSetting);
        } else {
            hasTileAudio = false;
        }

        String differentiateTypesSetting = settingsList.find("Differentiates types of multitype symbols");
        if (!differentiateTypesSetting.equals("")) {
            differentiateTypes = Boolean.parseBoolean(differentiateTypesSetting);
        } else {
            differentiateTypes = false;
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

        buildTilesArray();
        for (int d = 0; d < tileList.size(); d++) {
            if (tileList.get(d).tileType.equals("C") || tileList.get(d).tileTypeB.equals("C") || tileList.get(d).tileTypeC.equals("C")) {
                CONSONANTS.add(tileList.get(d).baseTile);
                CorV.add(tileList.get(d).baseTile);
            }
            if (tileList.get(d).tileType.contains("V") || tileList.get(d).tileTypeB.contains("V") || tileList.get(d).tileTypeC.contains("V")) {
                VOWELS.add(tileList.get(d).baseTile);
                if(!CorV.contains(tileList.get(d).baseTile)) {
                    CorV.add(tileList.get(d).baseTile);
                }
            }
            if (tileList.get(d).tileType.equals("T") || tileList.get(d).tileTypeB.equals("T") || tileList.get(d).tileTypeC.equals("T")) {
                TONES.add(tileList.get(d).baseTile);
            }
            if (tileList.get(d).tileType.equals("SAD")) {
                hasSAD = true;
                SAD.add(tileList.get(d).baseTile);
            }
            if (!tileList.get(d).tileTypeB.equals("none")) {
                MULTIFUNCTIONS.add(tileList.get(d).baseTile);
            }
        }
        LOGGER.info("LoadProgress: completed buildTilesArray()");

        Collections.shuffle(CONSONANTS);
        Collections.shuffle(VOWELS);
        Collections.shuffle(CorV);
        Collections.shuffle(TONES);
        Collections.shuffle(SYLLABLES);
        Collections.shuffle(MULTIFUNCTIONS);

        if (hasTileAudio) {
            totalAudio = totalAudio + tileList.size();
        }

        buildWordsArray();
        LOGGER.info("LoadProgress: completed buildWordsArray()");
        buildTileStagesLists();
        LOGGER.info("LoadProgress: completed buildTileStagesLists()");
        buildWordStagesLists();
        LOGGER.info("LoadProgress: completed buildWordStagesLists()");
        buildGamesArray();
        LOGGER.info("LoadProgress: completed buildGamesArray()");
        totalAudio = totalAudio + wordList.size();

        if (hasSyllableGames) {
            buildSyllablesArray();
            for (int d = 0; d < syllableList.size(); d++) {
                SYLLABLES.add(syllableList.get(d).toString());
            }
            Collections.shuffle(SYLLABLES);
        }
        LOGGER.info("LoadProgress: completed buildSyllablesArray()");

        if (hasSyllableAudio) {
            totalAudio = totalAudio + syllableList.size();
        }

        if (differentiateTypes && MULTIFUNCTIONS.isEmpty()) {
            for (int d = 0; d < Start.tileList.size(); d++) {
                if (!Start.tileList.get(d).tileTypeB.equals("none")) {
                    MULTIFUNCTIONS.add(Start.tileList.get(d).baseTile);
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
            String[] thisLineArray = thisLine.split("\t", 3);
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

    private int getAssetDuration(int assetID) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(assetID);
        mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public void buildTilesArray() {
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
                if(thisLineArray[14].equals("-")) { // Add all first types of tiles to "stage 1" if stages aren't being used
                    stageOfFirstAppearance = 1;
                } else {
                    stageOfFirstAppearance = Integer.parseInt(thisLineArray[14]);
                }
                if(thisLineArray[15].equals("-")) {
                    stageOfFirstAppearanceType2 = 1;
                } else {
                    stageOfFirstAppearanceType2 = Integer.parseInt(thisLineArray[15]);
                }
                if(thisLineArray[16].equals("-")) {
                    stageOfFirstAppearanceType3 = 1;
                } else {
                    stageOfFirstAppearanceType3 = Integer.parseInt(thisLineArray[16]);
                }
                // Create tile and add to list
                Tile tile = new Tile(thisLineArray[0], thisLineArray[1], thisLineArray[2], thisLineArray[3], thisLineArray[4], thisLineArray[5], thisLineArray[6], thisLineArray[7], thisLineArray[8], thisLineArray[9], thisLineArray[10], 0, 0, 0, stageOfFirstAppearance, stageOfFirstAppearanceType2, stageOfFirstAppearanceType3);
                if (!tile.hasNull()) {
                    tileList.add(tile);
                    if (!tile.tileType.equals("SAD")) {
                        tileListNoSAD.add(tile);
                    }
                }
            }
        }

        if (differentiateTypes) {

            tileListWithMultipleTypes = new TileListWithMultipleTypes();
            tileListWithMultipleTypesNoSAD = new TileListWithMultipleTypes();
            tileTypeHashMapWithMultipleTypes = new TileTypeHashMapWithMultipleTypes();
            tileTypeHashMapWithMultipleTypesNoSAD = new TileTypeHashMapWithMultipleTypes();

            for (Tile tile : tileList) {
                tileListWithMultipleTypes.add(tile.baseTile);
                tileTypeHashMapWithMultipleTypes.put(tile.baseTile, tile.tileType);
                if (!tile.tileType.equals("SAD")) {
                    tileListWithMultipleTypesNoSAD.add(tile.baseTile);
                    tileTypeHashMapWithMultipleTypesNoSAD.put(tile.baseTile, tile.tileType);
                }
                // SAD should never have a 2nd or 3rd type other than "none"
                if (!tile.tileTypeB.equals("none")) {
                    tileListWithMultipleTypes.add(tile.baseTile + "B");
                    tileTypeHashMapWithMultipleTypes.put(tile.baseTile + "B", tile.tileTypeB);
                    tileListWithMultipleTypesNoSAD.add(tile.baseTile + "B");
                    tileTypeHashMapWithMultipleTypesNoSAD.put(tile.baseTile + "B", tile.tileTypeB);
                }
                if (!tile.tileTypeC.equals("none")) {
                    tileListWithMultipleTypes.add(tile.baseTile + "C");
                    tileTypeHashMapWithMultipleTypes.put(tile.baseTile + "C", tile.tileTypeC);
                    tileListWithMultipleTypesNoSAD.add(tile.baseTile + "C");
                    tileTypeHashMapWithMultipleTypesNoSAD.put(tile.baseTile + "C", tile.tileTypeC);
                }
            }
        }

        buildTileHashMap();
    }


    public void buildTileStagesLists(){
        // LM, Apr 2023
        // Tile stages lists do NOT include SAD characters
        tileStagesLists = new ArrayList<ArrayList<String>>();
        ArrayList<String> tileListStage1 = new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage1);
        ArrayList<String> tileListStage2 =  new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage2);
        ArrayList<String> tileListStage3 =  new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage3);
        ArrayList<String> tileListStage4 =  new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage4);
        ArrayList<String> tileListStage5 =  new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage5);
        ArrayList<String> tileListStage6 =  new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage6);
        ArrayList<String> tileListStage7 =  new TileListWithMultipleTypes();
        tileStagesLists.add(tileListStage7);

        for(int i=0; i<7; i++){
            if(differentiateTypes){
                for (Tile tile : tileList) {
                    if (tile.stageOfFirstAppearance==(i+1) && !tile.tileType.equals("SAD")) {
                        tileStagesLists.get(i).add(tile.baseTile);
                    }
                    if (!tile.tileTypeB.equals("none") && tile.stageOfFirstAppearanceType2==(i+1) && !tile.tileTypeB.equals("SAD")) {
                        tileStagesLists.get(i).add(tile.baseTile + "B");
                    }
                    if (!tile.tileTypeC.equals("none") && tile.stageOfFirstAppearanceType3==(i+1) && !tile.tileTypeC.equals("SAD")) {
                        tileStagesLists.get(i).add(tile.baseTile + "C");
                    }
                }
            } else {
                for (Tile tile : tileList){
                    if(!tile.tileType.equals("SAD") && (tile.stageOfFirstAppearance==(i+1) || tile.stageOfFirstAppearanceType2==(i+1) || tile.stageOfFirstAppearanceType3==(i+1))){
                        tileStagesLists.get(i).add(tile.baseTile);
                    }
                }
            }
        }
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
        for (int i = 0; i < syllableList.size(); i++) {
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

        Boolean firstLetterStageCorrespondence = false;
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
            if (tile.stageOfFirstAppearanceType2 > lastStage){
                lastStage = tile.stageOfFirstAppearanceType2;
            }
            if (tile.stageOfFirstAppearanceType3 > lastStage){
                lastStage = tile.stageOfFirstAppearanceType3;
            }
        }
        for(Word word : wordList){
            stagesOfFirstAppearance.put(word, lastStage);
        }
        // Keep trying to find an earlier stage that it corresponds with until knowing the earliest stage that it corresponds with.
        for(int i=5;i>-1; i--){
            ArrayList<String> cumulativeCorrespondingTiles = new ArrayList<String>();
            for(int s=0; s<=i; s++){
                cumulativeCorrespondingTiles.addAll(tileStagesLists.get(s));
            }
            for(Word word: wordList) {
                ArrayList<String> tilesInThisWord = tileList.parseWordIntoTiles(word.localWord);
                int correspondingTiles = 0;
                for(int t=0; t<tilesInThisWord.size(); t++){
                    for(int a=0; a<cumulativeCorrespondingTiles.size(); a++) {
                        String aTileInTheStage = cumulativeCorrespondingTiles.get(a);
                        String aTileInTheStageSuffix = Character.toString(aTileInTheStage.charAt(aTileInTheStage.length() -1));
                        String aTileInTheStageWithoutSuffix = aTileInTheStage;
                        if(aTileInTheStageSuffix.equals("B") || aTileInTheStageSuffix.equals("C")){
                            aTileInTheStageWithoutSuffix = aTileInTheStageWithoutSuffix.substring(0, aTileInTheStageWithoutSuffix.length()-1);
                        }
                        if(tilesInThisWord.get(t).equals(aTileInTheStageWithoutSuffix)){
                            if(differentiateTypes){
                                String aTileInTheStageType = tileTypeHashMapWithMultipleTypes.get(aTileInTheStage);
                                String tileInThisWordType;
                                if (MULTIFUNCTIONS.contains(tilesInThisWord.get(t))){
                                    tileInThisWordType = tileList.getInstanceTypeForMixedTile(t, word.nationalWord);
                                } else {
                                    tileInThisWordType = tileTypeHashMapWithMultipleTypes.get(tilesInThisWord.get(t));
                                }

                                if(aTileInTheStageType.equals(tileInThisWordType)){
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
                ArrayList<String> tilesInThisWord = tileList.parseWordIntoTiles(word.localWord);
                Tile firstTile = tileHashMap.get(tilesInThisWord.get(0));
                String firstTileType = "";
                int stageFirstTileBelongsTo = firstTile.stageOfFirstAppearance;
                if(MULTIFUNCTIONS.contains(firstTile)) { // Check if we need to get stageOfFirstAppearance2 or stageOfFirstAppearance3 instead
                    firstTileType = tileList.getInstanceTypeForMixedTile(0, word.nationalWord);
                    if(firstTile.tileTypeB.equals(firstTileType)){
                        stageFirstTileBelongsTo = firstTile.stageOfFirstAppearanceType2;
                    } else if (firstTile.tileTypeC.equals(firstTileType)) {
                        stageFirstTileBelongsTo = firstTile.stageOfFirstAppearanceType3;
                    }
                }

                if (stageFirstTileBelongsTo < stagesOfFirstAppearance.get(word)){ // Bump words to an earlier stage if their first tile matches a tile that's introduced in that stage
                    stagesOfFirstAppearance.put(word, stageFirstTileBelongsTo);
                }
            }
        }


        // Then override for any words that have explicit first stage info in the wordlist
        for (Word word : wordList){
            if(!word.stageOfFirstAppearance.equals("-")){
                stagesOfFirstAppearance.put(word, Integer.parseInt(word.stageOfFirstAppearance));
            }
        }

        // Then use the stage info found to make the sub-wordlists
        for(Word word : wordList){
            int stageOfFirstAppearance = stagesOfFirstAppearance.get(word);
            wordStagesLists.get(stageOfFirstAppearance-1).add(word);
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

        String localWordForName = langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            buildNamesArray();
        }

    }

    public void buildNamesArray() {

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
            tileHashMap.put(tileList.get(i).baseTile, tileList.get(i));
            if (!tileList.get(i).tileType.equals("SAD")) {
                tileHashMapNoSAD.put(tileList.get(i).baseTile, tileList.get(i));
            }
        }
    }

    public void buildWordHashMap() {
        wordHashMap = new WordHashMap();
        for (int i = 0; i < wordList.size(); i++) {
            wordHashMap.put(wordList.get(i).nationalWord, wordList.get(i));
        }
    }

    public class Word {
        public String nationalWord;
        public String localWord;
        public int duration;
        public String mixedDefs;
        public String adjustment;
        public String stageOfFirstAppearance;

        public Word(String nationalWord, String localWord, int duration, String mixedDefs, String adjustment, String stageOfFirstAppearance) {
            this.nationalWord = nationalWord;
            this.localWord = localWord;
            this.duration = duration;
            this.mixedDefs = mixedDefs;
            this.adjustment = adjustment;
            this.stageOfFirstAppearance = stageOfFirstAppearance;
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
        public int stageOfFirstAppearance;
        public int stageOfFirstAppearanceType2;
        public int stageOfFirstAppearanceType3;

        public Tile(String baseTile, String alt1Tile, String alt2Tile, String alt3Tile, String tileType, String audioForTile, String upperTile, String tileTypeB, String audioForTileB, String tileTypeC, String audioForTileC, int tileDuration1, int tileDuration2, int tileDuration3, int stageOfFirstAppearance, int stageOfFirstAppearanceType2, int stageOfFirstAppearanceType3) {
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
            this.stageOfFirstAppearance = stageOfFirstAppearance;
            this.stageOfFirstAppearanceType2 = stageOfFirstAppearanceType2;
            this.stageOfFirstAppearanceType3 = stageOfFirstAppearanceType3;
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
        public String stage; // LM The game will include tiles/words from all the stages up to and including the stage indicated in the row of aa_games.txt

        public Game(String gameNumber, String gameCountry, String gameLevel, String gameColor, String gameInstrLabel, String gameInstrDuration, String gameMode, String stage) {
            this.gameNumber = gameNumber;
            this.gameCountry = gameCountry;
            this.gameLevel = gameLevel;
            this.gameColor = gameColor;
            this.gameInstrLabel = gameInstrLabel;
            this.gameInstrDuration = gameInstrDuration;
            this.gameMode = gameMode;
            this.stage = stage;
        }

        public boolean hasNull() {
            return gameNumber == null || gameCountry == null || gameLevel == null || gameColor == null || gameInstrLabel == null || gameInstrDuration == null || gameMode == null;
        }
    }

    public static class WordList extends ArrayList<Word> {
        public String nationalTitle;    // e.g. languages like English or Spanish (LWCs = Languages of Wider Communication)
        public String localTitle;    // e.g. LOPS (language of play) like Me'phaa, Kayan or Romani Gabor
        public String durationTitle;    // the length of the clip in ms, relevant only if set to use SoundPool
        public String mixedDefsTitle;    // for languages with multi-function symbols (e.g. in the word <niwan'>, the first |n| is a consontant and the second |n| is a nasality indicator
        public String adjustmentTitle;    // a font-specific reduction in size for words with longer pixel width
        public String stageOfFirstAppearanceTitle; // an option indicator to override the default tile-based staging and assign this word to first appear in a certain stage

        public int numberOfWordsForActiveTile(String activeTile, int scanSetting) {
            // Scan setting 1: Words that start with the active tile
            // Scan setting 2: Part of getting word groups in scan setting 2 is getting words that contain the active tile, but not in starting position
            // Scan setting 3: Words that contain the active tile anywhere

            ArrayList<String> parsedWordArrayFinal;
            String tileInFocus;
            String tileInFocusType;
            String activeTileTypeSuffix;
            String activeTileType;
            String activeTileWithoutSuffix;

            if (activeTile.length() > 0){
                activeTileTypeSuffix = Character.toString(activeTile.charAt(activeTile.length() - 1));
            } else {
                activeTileTypeSuffix = ""; // Prevents issues with recognizing "zero-width" characters such as saltillo
            }

            if (activeTileTypeSuffix.equals("B")) {
                activeTileWithoutSuffix = activeTile.substring(0, activeTile.length() - 1);
                activeTileType = tileHashMap.find(activeTileWithoutSuffix).tileTypeB;
            } else if (activeTileTypeSuffix.equals("C")) {
                activeTileWithoutSuffix = activeTile.substring(0, activeTile.length() - 1);
                activeTileType = tileHashMap.find(activeTileWithoutSuffix).tileTypeC;
            } else {
                activeTileWithoutSuffix = activeTile;
                activeTileType = tileHashMap.find(activeTileWithoutSuffix).tileType;
            }

            int wordCount = 0;
            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);
                int startingScanIndex;
                int endingScanIndex;
                switch (scanSetting) {
                    case 1:
                        startingScanIndex = 0;
                        endingScanIndex = 1;
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
                    if (differentiateTypes) { // Check if both tile and type match
                        if (tileInFocus.equals(activeTileWithoutSuffix)) {
                            if (MULTIFUNCTIONS.contains(activeTileWithoutSuffix)) {
                                tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, get(i).nationalWord);
                            } else {
                                tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                            }

                            if (tileInFocusType.equals(activeTileType)) {
                                wordCount++;
                                break; // Add each word only once, even if it contains the active tile more than once
                            }
                        }
                    } else { // Don't differentiate types; simply match tile to tile
                        if (tileInFocus.equals(activeTileWithoutSuffix)) {
                            wordCount++;
                            break; // Add each word only once, even if it contains the active tile more than once
                        }
                    }
                }
            }
            return wordCount;
        }

        public String[][] wordsForActiveTile(String activeTile, int wordCount, int scanSetting) {
            // Scan setting 1: Words that start with the active tile
            // Scan setting 2: Part of getting word groups in scan setting 2 is getting words that contain the active tile, but not in starting position
            // Scan setting 3: Words that contain the active tile anywhere
            String[][] wordsForActiveTile = new String[wordCount][2];

            ArrayList<String> parsedWordArrayFinal;
            String tileInFocus;
            String tileInFocusType;
            String activeTileTypeSuffix;
            String activeTileType;
            String activeTileWithoutSuffix;

            if (activeTile.length() > 0) {
                activeTileTypeSuffix = Character.toString(activeTile.charAt(activeTile.length() - 1));
            } else { // Prevent mysterious issues with "zero-width" characters like saltillo
                activeTileTypeSuffix = "";
            }
            if (activeTileTypeSuffix.equals("B")) {
                activeTileWithoutSuffix = activeTile.substring(0, activeTile.length() - 1);
                activeTileType = tileHashMap.find(activeTileWithoutSuffix).tileTypeB;
            } else if (activeTileTypeSuffix.equals("C")) {
                activeTileWithoutSuffix = activeTile.substring(0, activeTile.length() - 1);
                activeTileType = tileHashMap.find(activeTileWithoutSuffix).tileTypeC;
            } else {
                activeTileWithoutSuffix = activeTile;
                activeTileType = tileHashMap.find(activeTileWithoutSuffix).tileType;
            }

            int hitsCounter = 0;
            for (int i = 0; i < size(); i++) {
                parsedWordArrayFinal = tileList.parseWordIntoTiles(get(i).localWord);
                int startingScanIndex;
                int endingScanIndex;
                switch (scanSetting) {
                    case 1: // Scan the initial tiles of words
                        startingScanIndex = 0;
                        endingScanIndex = 1;
                        break;
                    case 2: // Scan the non-initial tiles of words
                        startingScanIndex = 1;
                        endingScanIndex = parsedWordArrayFinal.size();
                        break;
                    default: // Scan all the tiles in words
                        startingScanIndex = 0;
                        endingScanIndex = parsedWordArrayFinal.size();
                }

                for (int k = startingScanIndex; k < endingScanIndex; k++) {
                    tileInFocus = parsedWordArrayFinal.get(k);
                    if (differentiateTypes) { // Check if both tile and type match
                        if (tileInFocus.equals(activeTileWithoutSuffix)) {
                            if (MULTIFUNCTIONS.contains(activeTileWithoutSuffix)) {
                                tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, get(i).nationalWord);
                            } else {
                                tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                            }
                            if (tileInFocusType.equals(activeTileType)) {
                                wordsForActiveTile[hitsCounter][0] = get(i).nationalWord;
                                wordsForActiveTile[hitsCounter][1] = get(i).localWord;
                                hitsCounter++;
                                break; // Add each word only once, even if it contains the active tile more than once
                            }
                        }
                    } else { // Don't differentiate types; simply match tile to tile
                        if (tileInFocus.equals(activeTileWithoutSuffix)) {
                            wordsForActiveTile[hitsCounter][0] = get(i).nationalWord;
                            wordsForActiveTile[hitsCounter][1] = get(i).localWord;
                            hitsCounter++;
                            break; // Add each word only once, even if it contains the active tile more than once
                        }
                    }
                }
            }
            return wordsForActiveTile;
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

            //}, float adjustmentCutoff) {

            ArrayList<String[]> fourChoices = new ArrayList();
            ArrayList<String[]> easyWords = new ArrayList();        // words that do not begin with same tile or with distractor tile
            ArrayList<String[]> moderateWords = new ArrayList();    // words that begin with distractor tiles
            ArrayList<String[]> hardWords = new ArrayList();        // words that begin with the same tile (but excluding wordInLOP
            ArrayList<String> parsedWordArrayFinal;

            // Note that the following are four non-overlapping groups: easyWords, moderateWords, hardWords, wordInLOP

            String partA = wordInLWC;
            String partB = wordInLOP;
            String[] wordEntry = new String[]{partA, partB};
            fourChoices.add(wordEntry);

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
                        wordEntry = new String[]{partA, partB};
                        moderateWords.add(wordEntry);
                    }
                }

                if (activeTile.equals(refTile) && !activeWord.equals(wordInLOP)) {
                    partA = Start.wordList.get(i).nationalWord;
                    partB = Start.wordList.get(i).localWord;
                    if (!wordInLOP.equals(partB)) {
                        wordEntry = new String[]{partA, partB};
                        hardWords.add(wordEntry);
                    }
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
                        String[] possibleWordArr;
                        String possibleWord;
                        String firstTile;

                        possibleWordArr = easyWords.get(i);

                        possibleWord = possibleWordArr[1]; //should be LOP word
                        parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                        firstTile = parsedWordArrayFinal.get(0);

                        while ((Character.toLowerCase(firstTile.charAt(0)) == Character.toLowerCase(refTile.charAt(0))) && (firstTile.length() > refTile.length())
                                || fourChoices.contains(possibleWordArr)) { // Loops continues until a non-conflicting tile is chosen
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());

                            possibleWordArr = easyWords.get(rand1);
                            possibleWord = possibleWordArr[1];
                            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                            firstTile = parsedWordArrayFinal.get(0);
                        }

                        fourChoices.add(possibleWordArr);
                    } else {
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
                            // First try to simply get a moderate word if there are enough moderate words
                            possibleWordArr = moderateWords.get(i);
                        } else {
                            // If there are not enough moderate words go straight to trying a random easy word
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());

                            possibleWordArr = easyWords.get(rand1);
                        }
                        possibleWord = possibleWordArr[1]; //should be LOP word
                        parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                        firstTile = parsedWordArrayFinal.get(0); //should be tile

                        // Then test whether this possible word is problematic, and if so, replace it with a (different) random easy word.
                        // The random easy word also needs to be tested, since some languages may have instances where tiles "c" and "ch" both exist
                        // But one is not listed as a distractor tile of the other
                        while ((Character.toLowerCase(firstTile.charAt(0)) == Character.toLowerCase(refTile.charAt(0))) && (firstTile.length() > refTile.length())) {
                            Random rand = new Random();
                            int rand1 = rand.nextInt(easyWords.size());

                            possibleWordArr = easyWords.get(rand1);
                            possibleWord = possibleWordArr[1];
                            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(possibleWord);
                            firstTile = parsedWordArrayFinal.get(0);
                        }

                        // After those tests, the possible word has been validated and can be added to the answer choices
                        fourChoices.add(possibleWordArr);
                    } else { // Ref is a word or picture
                        if (moderateWords.size() > i) {
                            fourChoices.add(moderateWords.get(i));
                        } else {
                            fourChoices.add(easyWords.get(i - moderateWords.size()));
                        }
                    }
                }
            }

            if (challengeLevel == 3) {
                // Use hard words and if the supply runs out use moderate words and if the supply runs out use easy words

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

            Collections.shuffle(fourChoices);
            return fourChoices;
        }
    }

    public class Syllable {
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

    public static class SyllableList extends ArrayList<Syllable> {
        public String syllableTitle;
        public String[] distractorsTitles;
        public String syllableAudioNameTitle;
        public String syllableDurationTitle;
        public String colorTitle;

        public ArrayList<String> parseWordIntoSyllables(String parseMe) {
            ArrayList<String> parsedWordArrayTemp = new ArrayList();
            StringTokenizer st = new StringTokenizer(parseMe, ".");
            while (st.hasMoreTokens()) {
                parsedWordArrayTemp.add(st.nextToken());
            }
            return parsedWordArrayTemp;
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

        public ArrayList<String[]> returnFourWords(String refTile, int chall) {
            ArrayList<String> potentialWordParsed;
            String potentialWord;
            String natWord;
            ArrayList<String[]> fourWords = new ArrayList<>();
            Set<String> trackWords = new HashSet<>(); // Prevents repeats
            Syllable refSyllable = syllableHashMap.find(refTile);
            Random rand = new Random();
            boolean correctRep = false;

            // Get a word that starts with the refTile (syllable)
            while (!correctRep) {
                int randomNum = rand.nextInt(wordList.size());
                potentialWord = wordList.get(randomNum).localWord;
                natWord = wordList.get(randomNum).nationalWord;
                potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                if (potentialWordParsed.get(0).equals(refTile)) {
                    fourWords.add(new String[]{natWord, potentialWord});
                    trackWords.add(potentialWord);
                    correctRep = true;
                }
            }


            if (chall == 1) { // Easy words = not same initial syllable and no distractor syllables word-initially
                while (fourWords.size() < 4) {
                    int randomNum = rand.nextInt(wordList.size());
                    potentialWord = wordList.get(randomNum).localWord;
                    natWord = wordList.get(randomNum).nationalWord;
                    potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                    if (!potentialWordParsed.get(0).equals(refTile) && !potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[0]) && !potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[1]) && !potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[2])) {
                        String[] tileEntry = new String[]{natWord, potentialWord};
                        if (!trackWords.contains(potentialWord)) {
                            trackWords.add(potentialWord);
                            fourWords.add(tileEntry);
                        }
                    }
                }
            } else if (chall == 2) { // Medium words = start w/distractor syllables
                int count = 0;
                while (fourWords.size() < 4 && count < wordList.size()) {
                    int randomNum = rand.nextInt(wordList.size());
                    potentialWord = wordList.get(randomNum).localWord;
                    natWord = wordList.get(randomNum).nationalWord;
                    potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                    if (!potentialWordParsed.get(0).equals(refTile) && (potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[0]) || potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[1]) || potentialWordParsed.get(0)
                            .equals(refSyllable.distractors[2]))) {
                        String[] tileEntry = new String[]{natWord, potentialWord};
                        if (!trackWords.contains(potentialWord)) {
                            trackWords.add(potentialWord);
                            fourWords.add(tileEntry);
                        }
                    }
                    count++;
                }
                while (fourWords.size() < 4) { // Maybe this is an infinite loop. Change to allow any word that doesn't begin with correct syll

                    int randomNum = rand.nextInt(wordList.size());
                    potentialWord = wordList.get(randomNum).localWord;
                    natWord = wordList.get(randomNum).nationalWord;
                    potentialWordParsed = syllableList.parseWordIntoSyllables(potentialWord);
                    if (!potentialWordParsed.get(0).equals(refTile)) {
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

        public ArrayList<String[]> returnFourSylls(String refTile, int chall) {
            ArrayList<String[]> fourSylls = new ArrayList<>();
            Syllable refSyllable = syllableHashMap.find(refTile);
            Set<String> trackSylls = new HashSet<>(); // Prevents repeats
            String potentialSyll;
            String potentialSyllAud;
            Random rand = new Random();
            fourSylls.add(new String[]{refSyllable.syllableAudioName, refSyllable.syllable}); // Correct
            trackSylls.add(refSyllable.syllable);
            if (chall == 1) { // Random wrong syllables
                while (fourSylls.size() < 4) {
                    int randomNum = rand.nextInt(syllableList.size());
                    potentialSyll = syllableList.get(randomNum).syllable;
                    potentialSyllAud = syllableList.get(randomNum).syllableAudioName;
                    if (!potentialSyll.equals(refTile) && !potentialSyll
                            .equals(refSyllable.distractors[0]) && !potentialSyll
                            .equals(refSyllable.distractors[1]) && !potentialSyll
                            .equals(refSyllable.distractors[2]) && !trackSylls.contains(potentialSyll)) {
                        trackSylls.add(potentialSyll);
                        fourSylls.add(new String[]{potentialSyllAud, potentialSyll});
                    }
                }
            } else if (chall == 2) { // Distractor syllables
                if (!trackSylls.contains(refSyllable.distractors[0])) {
                    trackSylls.add(refSyllable.distractors[0]);
                    fourSylls.add(new String[]{refSyllable.syllableAudioName, refSyllable.distractors[0]});
                }
                if (!trackSylls.contains(refSyllable.distractors[1])) {
                    trackSylls.add(refSyllable.distractors[1]);
                    fourSylls.add(new String[]{refSyllable.syllableAudioName, refSyllable.distractors[1]});
                }
                if (!trackSylls.contains(refSyllable.distractors[2])) {
                    trackSylls.add(refSyllable.distractors[2]);
                    fourSylls.add(new String[]{refSyllable.syllableAudioName, refSyllable.distractors[2]});
                }
                while (fourSylls.size() < 4) {
                    int randomNum = rand.nextInt(syllableList.size());
                    potentialSyll = syllableList.get(randomNum).syllable;
                    potentialSyllAud = syllableList.get(randomNum).syllableAudioName;
                    if (!potentialSyll.equals(refTile) && !trackSylls.contains(potentialSyll)) {
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

        public ArrayList<String> parseWordIntoTiles(String parseMe) {
            // Updates by KP, Oct 2020
            // AH, Nov 2020, extended to check up to four characters in a game tile

            ArrayList<String> parsedWordArrayTemp = new ArrayList();

            int charBlock;
            String next1; // the next one character from the string
            String next2; // the next two characters from the string
            String next3; // the next three characters from the string
            String next4; // the next four characters from the string

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

                    if (next1.equals(tileList.get(k).baseTile) && charBlock == 0) {
                        // If charBlock is already assigned 2 or 3 or 4, it should not overwrite with 1
                        charBlock = 1;
                    }
                    if (next2.equals(tileList.get(k).baseTile) && charBlock != 3 && charBlock != 4) {
                        // The value 2 can overwrite 1 but it can't overwrite 3 or 4
                        charBlock = 2;
                    }
                    if (next3.equals(tileList.get(k).baseTile) && charBlock != 4) {
                        // The value 3 can overwrite 1 or 2 but it can't overwrite 4
                        charBlock = 3;
                    }
                    if (next4.equals(tileList.get(k).baseTile)) {
                        // The value 4 can overwrite 1 or 2 or 3
                        charBlock = 4;
                    }
                    if (tileList.get(k).baseTile == null && k > 0) {
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

        public ArrayList<String[]> returnFourTiles(String correctTile, int challengeLevelX,
                                                   String choiceType, String refTileType) {

            ArrayList<String[]> fourChoices = new ArrayList();
            int correctRow = returnPositionInAlphabet(correctTile);

            String partA = Start.tileListNoSAD.get(correctRow).audioForTile;
            String partB = null;
            if (choiceType.equals("TILE_LOWER")) {
                partB = Start.tileListNoSAD.get(correctRow).baseTile;
            }
            if (choiceType.equals("TILE_UPPER")) {
                partB = Start.tileListNoSAD.get(correctRow).upperTile;
            }
            String[] tileEntry = new String[]{partA, partB};
            fourChoices.add(tileEntry);

            if (challengeLevelX == 1) {
                // use random tiles

                Random rand = new Random();
                int rand1 = 0; // forces into while loop
                int rand2 = 0; // forces into while loop
                int rand3 = 0; // forces into while loop
                String altTile = "";

                while (rand1 == 0) {
                    rand1 = rand.nextInt(tileListNoSAD.size());
                    altTile = Start.tileListNoSAD.get(rand1).baseTile;
                    // JP: added logic so that if refTileType is C or V, four choices must also be C or V
                    if (correctRow == rand1 || Character.toLowerCase(correctTile.charAt(0)) == Character
                            .toLowerCase(altTile.charAt(0)) || (!Start.tileListNoSAD.get(rand1)
                            .tileType.equals("C") && !Start.tileListNoSAD.get(rand1).tileType.equals("V")
                            && (refTileType.equals("C") || refTileType.equals("V")))) {
                        rand1 = 0;
                    } else {
                        altTile = Start.tileListNoSAD.get(rand1).baseTile;
                    }
                }
                if (choiceType.equals("TILE_LOWER")) {
                    partB = altTile;
                }
                if (choiceType.equals("TILE_UPPER")) {
                    partB = Start.tileListNoSAD.get(rand1).upperTile;
                }
                partA = Start.tileListNoSAD.get(returnPositionInAlphabet(altTile)).audioForTile;
                tileEntry = new String[]{partA, partB};
                fourChoices.add(tileEntry);

                while (rand2 == 0) {
                    rand2 = rand.nextInt(tileListNoSAD.size());
                    altTile = Start.tileListNoSAD.get(rand2).baseTile;
                    if (correctRow == rand2 || rand1 == rand2 || Character.toLowerCase(correctTile
                            .charAt(0)) == Character.toLowerCase(altTile.charAt(0)) || (!Start.tileListNoSAD.get(rand2)
                            .tileType.equals("C") && !Start.tileListNoSAD.get(rand2).tileType.equals("V")
                            && (refTileType.equals("C") || refTileType.equals("V")))) {
                        rand2 = 0;
                    } else {
                        altTile = Start.tileListNoSAD.get(rand2).baseTile;
                    }
                }
                if (choiceType.equals("TILE_LOWER")) {
                    partB = altTile;
                }
                if (choiceType.equals("TILE_UPPER")) {
                    partB = Start.tileListNoSAD.get(rand2).upperTile;
                }
                partA = Start.tileListNoSAD.get(returnPositionInAlphabet(altTile)).audioForTile;
                tileEntry = new String[]{partA, partB};
                fourChoices.add(tileEntry);

                while (rand3 == 0) {
                    rand3 = rand.nextInt(tileListNoSAD.size());
                    altTile = Start.tileListNoSAD.get(rand3).baseTile;
                    if (correctRow == rand3 || rand1 == rand2 || rand1 == rand3 || rand2 == rand3
                            || Character.toLowerCase(correctTile.charAt(0)) == Character.toLowerCase(altTile.charAt(0))
                            || (!Start.tileListNoSAD.get(rand3)
                            .tileType.equals("C") && !Start.tileListNoSAD.get(rand3).tileType.equals("V")
                            && (refTileType.equals("C") || refTileType.equals("V")))) {
                        rand3 = 0;
                    } else {
                        altTile = Start.tileListNoSAD.get(rand3).baseTile;
                    }
                }
                if (choiceType.equals("TILE_LOWER")) {
                    partB = altTile;
                }
                if (choiceType.equals("TILE_UPPER")) {
                    partB = Start.tileListNoSAD.get(rand3).upperTile;
                }
                partA = Start.tileListNoSAD.get(returnPositionInAlphabet(altTile)).audioForTile;
                tileEntry = new String[]{partA, partB};
                fourChoices.add(tileEntry);

            }


            if (challengeLevelX == 2) {
                // Use distractor tiles

                for (int i = 1; i < 4; i++) {

                    if (choiceType.equals("TILE_LOWER")) {
                        partB = Start.tileListNoSAD.get(correctRow).altTiles[i - 1];

                    }

                    if (choiceType.equals("TILE_UPPER")) {
                        partB = Start.tileListNoSAD.get(returnPositionInAlphabet(Start.tileList.get(correctRow).altTiles[i - 1])).upperTile;
                    }

                    //JP approach 2:
                    //while (replace) {

                    if (partB.charAt(0) == correctTile.charAt(0)) {
                        if (partB.length() <= correctTile.length()) {
                            Random rand = new Random();
                            int rand5 = rand.nextInt(tileListNoSAD.size());
                            if (choiceType.equals("TILE_UPPER")) {
                                partB = Start.tileListNoSAD.get(rand5).upperTile;
                                while ((Character.toLowerCase(partB.charAt(0)) == Character.toLowerCase(correctTile.charAt(0))) ||
                                        partB.equals(Start.tileListNoSAD.get(returnPositionInAlphabet(Start.tileListNoSAD.get(correctRow).altTiles[0])).upperTile) ||
                                        partB.equals(Start.tileListNoSAD.get(returnPositionInAlphabet(Start.tileListNoSAD.get(correctRow).altTiles[1])).upperTile) ||
                                        partB.equals(Start.tileListNoSAD.get(returnPositionInAlphabet(Start.tileListNoSAD.get(correctRow).altTiles[2])).upperTile)
                                        || ((refTileType.equals("C") || refTileType.equals("V"))
                                        && (!Start.tileListNoSAD.get(rand5).tileType.equals("C")
                                        && !Start.tileListNoSAD.get(rand5).tileType.equals("V")))) {
                                    rand5 = rand.nextInt(tileListNoSAD.size());
                                    partB = Start.tileListNoSAD.get(rand5).upperTile;
                                }
                            } else if (choiceType.equals("TILE_LOWER")) {
                                partB = Start.tileListNoSAD.get(rand5).baseTile;
                                while ((Character.toLowerCase(partB.charAt(0)) == Character.toLowerCase(correctTile.charAt(0))) ||
                                        partB.equals(Start.tileListNoSAD.get(correctRow).altTiles[0]) ||
                                        partB.equals(Start.tileListNoSAD.get(correctRow).altTiles[1]) ||
                                        partB.equals(Start.tileListNoSAD.get(correctRow).altTiles[2]) ||
                                        ((refTileType.equals("C") || refTileType.equals("V"))
                                                && (!Start.tileListNoSAD.get(rand5).tileType.equals("C")
                                                && !Start.tileListNoSAD.get(rand5).tileType.equals("V")))) {
                                    rand5 = rand.nextInt(tileListNoSAD.size());
                                    partB = Start.tileListNoSAD.get(rand5).baseTile;
                                }
                            }

                        }
                    }
                    //}
                    //
                    partA = Start.tileListNoSAD.get(returnPositionInAlphabet(partB)).audioForTile;
                    tileEntry = new String[]{partA, partB};
                    fourChoices.add(tileEntry);
                }
            }

            Collections.shuffle(fourChoices);

            return fourChoices;

        }

        public String getInstanceTypeForMixedTile(int index, String wordInLWC) {

            // Need to rethink this function for tone, SAD,

            String instanceType = null;

            String mixedDefinitionInfo = Start.wordHashMap.find(wordInLWC).mixedDefs;

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

    public class TileHashMap extends HashMap<String, Tile> {

        public Tile find(String key) {
            for (String k : keySet()) {
                if (k.equals(key)) {
                    return (get(k));
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

    public class TileTypeHashMapWithMultipleTypes extends HashMap<String, String> {
        public String text;
        public String type;
    }

    public static class TileListWithMultipleTypes extends ArrayList<String> {

        public String returnNextAlphabetTileDifferentiateTypes(String oldTile) {

            String nextTile = "";
            for (int i = 0; i < size(); i++) {
                if (get(i).equals(oldTile)) {
                    if (i < (size() - 1)) {
                        nextTile = get(i + 1);
                    } else// if (i == size() - 1) {
                        nextTile = get(0);
                }
            }

            return nextTile;

        }

        public String returnPreviousAlphabetTileDifferentiateTypes(String oldTile) {

            String previousTile = "";
            for (int i = size() - 1; i >= 0; i--) {

                if (get(i).equals(oldTile)) {
                    if (i > 0) {
                        previousTile = get(i - 1);
                    } else// if (i == 0) {
                        previousTile = get(size() - 1);
                }
            }

            return previousTile;

        }
    }

}
