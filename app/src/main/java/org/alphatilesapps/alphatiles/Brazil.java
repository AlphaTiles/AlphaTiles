package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.syllableHashMap;
import static org.alphatilesapps.alphatiles.Start.syllableList;

// RR
//Game idea: Find the vowel missing from the word
//Challenge Level 1: VOWELS: Pick from correct tile and three random tiles
//Challenge Level 2: VOWELS: Pick from correct tile and its distractor trio
//Challenge Level 3: VOWELS: Pick from all vowel tiles (up to a max of 15)

// AH
//Challenge Level 4: CONSONANTS: Pick from correct tile and three random tiles
//Challenge Level 5: CONSONANTS: Pick from correct tile and its distractor trio
//Challenge Level 6: CONSONANTS: Pick from all consonant tiles (up to a max of 15)

// JP (ideas)
// Syllable Level 1: Pick from correct syllable and three random syllables (4 choices)
// Syllable Level 2: Pick from correct syllable and its distractor trio (if we add that to syllables tab) (4 choices)
// No reason to accommodate 15 syllables, right?

public class Brazil extends GameActivity {

    Start.TileList sortableTilesArray; // KP
    Start.SyllableList sortableSyllArray; //JP
    Set<String> answerChoices = new HashSet<String>();
    int visibleTiles;
    String correctTile = "";
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int brazilPoints;

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15
    };
    
    protected int[] getTileButtons() {return TILE_BUTTONS;}
    
    protected int[] getWordImages() {return null;}

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
//          audioInstructionsResID = res.getIdentifier("brazil_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        }
        catch (NullPointerException e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = 0;
        if (challengeLevel == 3 || challengeLevel == 6) {
            gameID = R.id.brazil_cl3_CL;
        } else {
            gameID = R.id.brazil_cl1_CL;
        }
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336", "#4CAF50", "#E91E63"};

    static List<String> VOWELS = new ArrayList<>();
    static List<String> CONSONANTS = new ArrayList<>();
    static List<String> SYLLABLES = new ArrayList<>();
    static List<String> MULTIFUNCTIONS = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(Brazil.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if (challengeLevel == 3 || challengeLevel == 6) {
            setContentView(R.layout.brazil_cl3);
        } else {
            setContentView(R.layout.brazil_cl1);
        }
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only


        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
        }

//        LOGGER.info("Remember APR 21 21 # 1");

        points = getIntent().getIntExtra("points", 0); // KP
        brazilPoints = getIntent().getIntExtra("brazilPoints", 0); // KP

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        brazilPoints = prefs.getInt("storedBrazilPoints_level" + String.valueOf(challengeLevel) + "_player" + playerString, 0);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP
        gameNumber = getIntent().getIntExtra("gameNumber", 0); // KP
        syllableGame = getIntent().getStringExtra("syllableGame");

        if (challengeLevel < 4 && !syllableGame.equals("S")) {

            if (VOWELS.isEmpty()) {  //makes sure VOWELS is populated only once when the app is running
                for (int d = 0; d < Start.tileList.size(); d++) {
                    if (Start.tileList.get(d).tileType.equals("V")) {
                        VOWELS.add(Start.tileList.get(d).baseTile);
                    }
                }
            }

            Collections.shuffle(VOWELS); // AH

        }else if (syllableGame.equals("S")){
            if (SYLLABLES.isEmpty()) {
                for (int d = 0; d < syllableList.size(); d++) {
                    SYLLABLES.add(syllableList.get(d).toString());
                }
            }
        }
        else {

            if (CONSONANTS.isEmpty()) {  //makes sure CONSONANTS is populated only once when the app is running
                for (int d = 0; d < Start.tileList.size(); d++) {
                    if (Start.tileList.get(d).tileType.equals("C")) {
                        CONSONANTS.add(Start.tileList.get(d).baseTile);
                    }
                }
            }

            Collections.shuffle(CONSONANTS);

        }

//        LOGGER.info("Remember APR 21 21 # 2");

        if (MULTIFUNCTIONS.isEmpty()) {  //makes sure MULTIFUNCTIONS is populated only once when the app is running
            for (int d = 0; d < Start.tileList.size(); d++) {
//                LOGGER.info("Remember Start.tileList.get(" + d + ").tileType = " + Start.tileList.get(d).tileType);
//                LOGGER.info("Remember Start.tileList.get(" + d + ").tileType2 = " + Start.tileList.get(d).tileTypeB);
//                LOGGER.info("Remember Start.tileList.get(" + d + ").tileType3 = " + Start.tileList.get(d).tileTypeC);
//                LOGGER.info("Remember Start.tileList.get(" + d + ").audioForTile = " + Start.tileList.get(d).audioForTile);
//                LOGGER.info("Remember Start.tileList.get(" + d + ").audioForTile2 = " + Start.tileList.get(d).audioForTileB);
//                LOGGER.info("Remember Start.tileList.get(" + d + ").audioForTile3 = " + Start.tileList.get(d).audioForTileC);
                if (!Start.tileList.get(d).tileTypeB.equals("none")) {
                    MULTIFUNCTIONS.add(Start.tileList.get(d).baseTile);
                }
            }
        }

//        LOGGER.info("Remember MULTIFUNCTIONS.size() = " + MULTIFUNCTIONS.size());
//
//        LOGGER.info("Remember APR 21 21 # 3");

        Collections.shuffle(MULTIFUNCTIONS);

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel  + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");
        if (syllableGame.equals("S")){
            visibleTiles = 4;
        }else{
            switch (challengeLevel) {
                case 3:
                    visibleTiles = VOWELS.size();
                    if (visibleTiles > 15) {    // AH
                        visibleTiles = 15;      // AH
                    }                           // AH
                    break;
                case 6:
                    visibleTiles = CONSONANTS.size();
                    if (visibleTiles > 15) {    // AH
                        visibleTiles = 15;      // AH
                    }                           // AH
                    break;
                default:
                    visibleTiles = 4;
            }
        }


//        LOGGER.info("Remember APR 21 21 # 4");

        if (syllableGame.equals("S")){
            sortableSyllArray = (Start.SyllableList) syllableList.clone(); //JP
        }else{
            sortableTilesArray = (Start.TileList)Start.tileList.clone(); // KP
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(brazilPoints));

        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

        updateTrackers();

        setTextSizes();

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

//        LOGGER.info("Remember APR 21 21 # 5");

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        int pixelHeight = 0;
        double scaling = 0.45;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int t = 0; t < visibleTiles; t++) {

            TextView gameTile = findViewById(TILE_BUTTONS[t]);
            if (t == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) gameTile.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            gameTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        }

        TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) activeWord.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        activeWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.5 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void repeatGame(View View) {
        if (!repeatLocked) {
            playAgain();
        }
    }

    public void playAgain() {
        if (mediaPlayerIsPlaying) {
            return;
        }

        repeatLocked = true;
        if (syllableGame.equals("S")){
            Collections.shuffle(sortableSyllArray);
        }
        else{
            Collections.shuffle(sortableTilesArray); // KP
        }

//        LOGGER.info("Remember APR 21 21 # 5.1");
        chooseWord();
//        LOGGER.info("Remember APR 21 21 # 5.2");
        removeTile();
//        LOGGER.info("Remember APR 21 21 # 5.3");
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
//        LOGGER.info("Remember APR 21 21 # 6");
        if (syllableGame.equals("S")){
            setUpSyllables();
        }else{
            setUpTiles();
        }
//        LOGGER.info("Remember APR 21 21 # 7");
        playActiveWordClip(false);
//        LOGGER.info("Remember APR 21 21 # 8");
        setAllTilesClickable();
        setOptionsRowClickable();
    }

    private void chooseWord() {

        boolean freshWord = false;

        while(!freshWord) {
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size()); // KP

            wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
            wordInLOP = Start.wordList.get(randomNum).localWord; // KP

            //If this word isn't one of the 3 previously tested words, we're good // LM
            if(wordInLWC.compareTo(lastWord)!=0
            && wordInLWC.compareTo(secondToLastWord)!=0
            && wordInLWC.compareTo(thirdToLastWord)!=0){
                freshWord = true;
                thirdToLastWord = secondToLastWord;
                secondToLastWord = lastWord;
                lastWord = wordInLWC;
            }

        }//generates a new word if it got one of the last three tested words // LM

        LOGGER.info("Remember wordInLOP = " + wordInLOP);

        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        if(syllableGame.equals("S")){
            parsedWordArrayFinal = syllableList.parseWordIntoSyllables(wordInLOP);
        }else{
            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP);
        }


        boolean proceed = false;
        String nextTile;

        // JP: this section is not relevant to syllable games, right?
        if (!syllableGame.equals("S")){
            switch (challengeLevel) {
                case 4:
                case 5:
                case 6:
                    for (int i = 0; i < parsedWordArrayFinal.size(); i++) {

                        nextTile = parsedWordArrayFinal.get(i);
                        // include if a simple consonant
                        if(CONSONANTS.contains(nextTile) && !MULTIFUNCTIONS.contains(nextTile)) {
                            proceed = true;
                        }
                        // include if a multi-function symbol that is a consonant in this instance
                        if(MULTIFUNCTIONS.contains(nextTile)) {
                            String instanceType = Start.tileList.getInstanceTypeForMixedTile(i, wordInLWC);
                            if (instanceType.equals("C")) {
                                proceed = true;
                            }
                        }

                    }
                    break;
                default:
                    for (int i = 0; i < parsedWordArrayFinal.size(); i++) {

                        nextTile = parsedWordArrayFinal.get(i);
                        // include if a simple vowel
                        if(VOWELS.contains(nextTile) && !MULTIFUNCTIONS.contains(nextTile)) {
                            proceed = true;
                        }
                        // include if a multi-function symbol that is a vowel in this instance
                        if(MULTIFUNCTIONS.contains(nextTile)) {
                            String instanceType = Start.tileList.getInstanceTypeForMixedTile(i, wordInLWC);
                            if (instanceType.equals("V")) {
                                proceed = true;
                            }
                        }

                    }
            }

            if (!proceed) { // some languages (e.g. skr) have words without vowels (as defined by game tiles), so we filter out those words
                chooseWord();
            }
        }
    }

    private void removeTile() {

        int min = 0;
        int max = parsedWordArrayFinal.size() - 1;
        Random rand = new Random();
        int index = 0;
        correctTile = "";

        boolean repeat = true;
        String instanceType = null;
        int counter = 0;



//        LOGGER.info("Remember APR 21 21 # 5.2.1");
        // JP: this section doesn't apply to syllable games, right?
        if (!syllableGame.equals("S")){
            while (repeat && counter < 200) {
                counter++;
                index = rand.nextInt((max - min) + 1) + min; // 200 chances to randomly draw a functional letter (e.g. a "V" if looking for "V"
                correctTile = parsedWordArrayFinal.get(index);
                if (MULTIFUNCTIONS.contains(correctTile)) {
                    instanceType = Start.tileList.getInstanceTypeForMixedTile(index, wordInLWC);
                    LOGGER.info("Remember MIXED: wordInLOP / correctTile / instanceType = " + wordInLOP + " / " + correctTile + " / " + instanceType);
                } else {
                    instanceType = Start.tileList.get(Start.tileList.returnPositionInAlphabet(correctTile)).tileType;
                    LOGGER.info("Remember NOT MIXED wordInLOP / correctTile / instanceType = " +  wordInLOP + " / " + correctTile + " / " + instanceType);
                }

                if (challengeLevel < 4) {

                    if (instanceType.equals("V")) {

                        repeat = false;

                    }

                }

                if (challengeLevel > 3) {

                    if (instanceType.equals("C")) {

                        repeat = false;

                    }

                }

            }
        }else{ //syllable game
            index = rand.nextInt((max - min) + 1) + min;
            correctTile = parsedWordArrayFinal.get(index);
        }


//        LOGGER.info("Remember APR 21 21 # 5.2.6");
        parsedWordArrayFinal.set(index, "_");
        TextView constructedWord = findViewById(R.id.activeWordTextView);
        StringBuilder word = new StringBuilder();
        for (String s : parsedWordArrayFinal) {
            if (s != null) {
                word.append(s);
            }
        }
        constructedWord.setText(word.toString());
//        LOGGER.info("Remember APR 21 21 # 5.2.7");

    }

    private void setUpSyllables() {
        boolean correctSyllRepresented = false;

        //find corresponding syllable object for correct answer
        Start.Syllable answer = syllableHashMap.find(correctTile);

        // TO DO: FIX ISSUE OF SOMETIMES RIGHT ANSWER NOT SHOWING UP
        answerChoices.clear();
        answerChoices.add(correctTile);
        answerChoices.add(answer.distractors[0]);
        answerChoices.add(answer.distractors[1]);
        answerChoices.add(answer.distractors[2]);

        List<String> answerChoicesList = new ArrayList<>(answerChoices); //so we can index into answer choices now

        for (int t = 0; t < visibleTiles; t++){
            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (sortableSyllArray.get(t).syllable.equals(correctTile) && t < visibleTiles) {
                correctSyllRepresented = true;
            }

            String tileColorStr = COLORS[t % 5];
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1){
                if (t < visibleTiles) {
                    gameTile.setText(sortableSyllArray.get(t).syllable); // KP
                    gameTile.setBackgroundColor(tileColor);
                    gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameTile.setVisibility(View.VISIBLE);
                } else {
                    gameTile.setText(String.valueOf(t + 1));
                    gameTile.setBackgroundResource(R.drawable.textview_border);
                    gameTile.setTextColor(Color.parseColor("#000000")); // black
                    gameTile.setClickable(false);
                    gameTile.setVisibility(View.INVISIBLE);
                }
            }else{
                if (t < visibleTiles) {
                    // think through this logic more -- how to get distractor syllables in there but
                    // also fill other syllables beyond the 3 distractors

                    // first make a visibleTiles-sized array with the correct answer,
                    // its distractor syllables, and any other syllables that start with the same tile;
                    // filter out repeats

                    // then iterate through TILE_BUTTONS and fill them in using the other array, shuffled
                    if (answerChoicesList.get(t) == correctTile){
                        correctSyllRepresented = true;
                    }
                    gameTile.setText(answerChoicesList.get(t)); // KP
                    gameTile.setBackgroundColor(tileColor);
                    gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameTile.setVisibility(View.VISIBLE);
                }else {
                    gameTile.setText(String.valueOf(t + 1));
                    gameTile.setBackgroundResource(R.drawable.textview_border);
                    gameTile.setTextColor(Color.parseColor("#000000")); // black
                    gameTile.setClickable(false);
                    gameTile.setVisibility(View.INVISIBLE);
                }
            }


        }

        if (!correctSyllRepresented) {

            // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles

            Random rand = new Random();
            int randomNum = rand.nextInt(visibleTiles - 1); // KP
            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(correctTile);

        }

    }

    private void setUpTiles() {

        boolean correctTileRepresented = false;
        if (challengeLevel == 3 || challengeLevel == 6) {
            for (int t = 0; t < visibleTiles; t++) {
                TextView gameTile = findViewById(TILE_BUTTONS[t]);
                if (challengeLevel == 3) {
                    gameTile.setText(VOWELS.get(t));
                    if (VOWELS.get(t).equals(correctTile)) {
                        correctTileRepresented = true;
                    }
                } else {
                    gameTile.setText(CONSONANTS.get(t));
                    if (CONSONANTS.get(t).equals(correctTile)) {
                        correctTileRepresented = true;
                    }
                }

                String tileColorStr = COLORS[t % 5];
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
            }

            for (int i = visibleTiles; i < TILE_BUTTONS.length; i++) {
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                gameTile.setVisibility(View.INVISIBLE);
            }
        } else if (challengeLevel == 1 || challengeLevel == 4) {

            for (int t = 0; t < visibleTiles; t++) {

                TextView gameTile = findViewById(TILE_BUTTONS[t]);

                if (sortableTilesArray.get(t).baseTile.equals(correctTile)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = COLORS[t % 5];
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(sortableTilesArray.get(t).baseTile);
                if (sortableTilesArray.get(t).baseTile.equals("tiles")) {
                    gameTile.setText(sortableTilesArray.get(Start.tileList.size() - 1).baseTile);
                }
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);

            }

        } else {
            // when Earth.challengeLevel == 2 || == 5
            correctTileRepresented = true;
            int correspondingRow = 0;
            for (int d = 0; d < Start.tileList.size(); d++) {
                if (Start.tileList.get(d).baseTile.equals(correctTile)) {
                    correspondingRow = d;
                    break;
                }
            }

            List<String> usedTiles = new ArrayList<>();
            Random rand = new Random();
            int randomNum;
            for (int t = 0; t < visibleTiles; t++) {
                TextView gameTile = findViewById(TILE_BUTTONS[t]);

                String tileColorStr = COLORS[t % 5];
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);

                randomNum = rand.nextInt(visibleTiles); //
                String nextTile;
                if (randomNum == 0) {
                    nextTile = Start.tileList.get(correspondingRow).baseTile;
                } else {
                    nextTile = Start.tileList.get(correspondingRow).altTiles[randomNum - 1];
                }
                if (!usedTiles.contains(nextTile)) {
                    gameTile.setText(nextTile);
                    usedTiles.add(t, nextTile);
                } else {
                    t--;
                }
            }
        }


        if (!correctTileRepresented) {

            // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles
            // This check is not necessary for challengeLevel 2 and 5, so at beginning of code above correctTileRepresented set to true
//            LOGGER.info("Remember that inside loop for correctTileRepresented = false");

            int min = 0;
            int max = visibleTiles - 1;
            Random rand = new Random();
            int randomNum = rand.nextInt((max - min) + 1) + min;

            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(correctTile);

        }

    }

    private void respondToTileSelection(int justClickedTile) {

        if (mediaPlayerIsPlaying) {
            return;
        }

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 15, t uses the array ID (between [0] and [14]
        TextView tile = findViewById(TILE_BUTTONS[tileNo]);
        String gameTileString = tile.getText().toString();

        if (correctTile.equals(gameTileString)) {
            // Good job! You chose the right tile
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points++;
            brazilPoints++;
            pointsEarned.setText(String.valueOf(brazilPoints));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            editor.putInt("storedBrazilPoints_level" + String.valueOf(challengeLevel) + "_player" + playerString, brazilPoints);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int i = 0; i < parsedWordArrayFinal.size(); i++) {
                if ("_".equals(parsedWordArrayFinal.get(i))) {
                    parsedWordArrayFinal.set(i, gameTileString);
                }
            }

            TextView constructedWord = findViewById(R.id.activeWordTextView);
            StringBuilder word = new StringBuilder();
            for (String s : parsedWordArrayFinal) {
                if (s != null) {
                    word.append(s);
                }
            }
            constructedWord.setText(word.toString());

            for (int t = 0; t < visibleTiles; t++) {
                TextView gameTile = findViewById(TILE_BUTTONS[t]);
                if (t != (tileNo)) {
                    String wordColorStr = "#A9A9A9"; // dark gray
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                    gameTile.setTextColor(Color.parseColor("#000000")); // black
                }
            }

            playCorrectSoundThenActiveWordClip(false);

        } else {

            playIncorrectSound();

        }

    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);

    }

    public void onBtnClick (View view) {
        respondToTileSelection(Integer.parseInt((String)view.getTag())); // KP
    }

    public void playAudioInstructions(View view){
        if(getAudioInstructionsResID() > -1) {
            super.playAudioInstructions(view);
        }
    }

}
