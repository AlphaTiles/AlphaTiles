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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static org.alphatilesapps.alphatiles.Start.syllableHashMap;
import static org.alphatilesapps.alphatiles.Start.tileHashMap;
import static org.alphatilesapps.alphatiles.Start.CorV;

//level 1: 6 visible tiles, random wrong choices
//level 2: 12 visible tiles, random wrong choices
//level 3: 18 visible tiles, random wrong choices
//level 4: 6 visible tiles, distractor wrong choices
//level 5: 12 visible tiles, distractor wrong choices
//level 6: 18 visible tiles, distractor wrong choices

//level 1 + S: 6 visible syllables, random wrong choices
//level 2 + S: 12 visible syllables, random wrong choices
//level 3 + S: 18 visible syllables, random wrong choices
//level 4 + S: 6 visible syllables, distractor wrong choices
//level 5 + S: 12 visible syllables, distractor wrong choices
//level 6 + S: 18 visible syllables, distractor wrong choices


// IF A LANGUAGE HAS WORDS THAT START WITH SOMETHING OTHER THAN C OR V, THIS WILL CRASH

public class Georgia extends GameActivity {

    Start.SyllableList sortableSyllArray; //JP
    Set<String> answerChoices = new HashSet<String>();
    String initialTile = "";
    String initialSyll = "";
    int visibleTiles; // will be 6, 12 or 18 based on challengeLevel 1, 2 or 3
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int georgiaPoints;
    boolean georgiaHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18
    };

    protected int[] getTileButtons() { return TILE_BUTTONS;}

    protected int[] getWordImages() {return null;}

    @Override
    protected void centerGamesHomeImage() {

            ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
            instructionsButton.setVisibility(View.GONE);
            int gameID = 0;
            if (syllableGame.equals("S")){
                gameID = R.id.georgiaCL_syll;
            }else{
                gameID = R.id.georgiaCL;
            }
            ConstraintLayout constraintLayout = findViewById(gameID);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
            constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
            constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
            constraintSet.applyTo(constraintLayout);
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
//          audioInstructionsResID = res.getIdentifier("georgia_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());

        }
        catch (Exception e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        int gameID = 0;
        if (syllableGame.equals("S")){
            setContentView(R.layout.georgia_syll);
            gameID = R.id.georgiaCL_syll;
        }else{
            setContentView(R.layout.georgia);
            gameID = R.id.georgiaCL;
        }

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        points = getIntent().getIntExtra("points", 0); // KP
        georgiaPoints = getIntent().getIntExtra("georgiaPoints", 0); // LM
        georgiaHasChecked12Trackers = getIntent().getBooleanExtra("georgiaHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        georgiaPoints = prefs.getInt("storedGeorgiaPoints_level" + challengeLevel + "_player" + playerString, 0);
        georgiaHasChecked12Trackers = prefs.getBoolean("storedGeorgiaHasChecked12Trackers_level" + challengeLevel + "_player" + playerString, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP
        syllableGame = getIntent().getStringExtra("syllableGame");

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");


        switch (challengeLevel) {
            case 5:
            case 2:
                visibleTiles = 12;
                break;
            case 6:
            case 3:
                visibleTiles = 18;
                break;
            default:
                visibleTiles = 6;
        }

        if (syllableGame.equals("S")){
            sortableSyllArray = (Start.SyllableList)Start.syllableList.clone();
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(georgiaPoints));

        /*SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);*/
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
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
            Collections.shuffle(sortableSyllArray); //JP
        }

        chooseWord();
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        if (syllableGame.equals("S")){
            setUpSyllables();
        }else{
            setUpTiles();
        }
        playActiveWordClip(false);
        setAllTilesClickable();
        setOptionsRowClickable();

        for (int i = 0; i < TILE_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(TILE_BUTTONS[i]);
            nextWord.setClickable(true);
        }

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
        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        if (syllableGame.equals("S")){
            parsedWordSyllArrayFinal = Start.syllableList.parseWordIntoSyllables(wordInLOP); //JP
            initialSyll = parsedWordSyllArrayFinal.get(0);
        }else{
            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
            initialTile = parsedWordArrayFinal.get(0);
        }
    }

    private void setUpSyllables() {
        boolean correctSyllRepresented = false;

        //find corresponding syllable object for initialSyll
        Start.Syllable answer = syllableHashMap.find(initialSyll);

        answerChoices.clear();
        answerChoices.add(initialSyll);
        answerChoices.add(answer.distractors[0]);
        answerChoices.add(answer.distractors[1]);
        answerChoices.add(answer.distractors[2]);

        // first distractors, then syllables with same first and second unicode character,
        // then with same last letter, then random

        int i = 0;
        while (answerChoices.size() < visibleTiles && i < sortableSyllArray.size()){
            // and does so while skipping repeats because it is a set
            // and a set has no order so it will be randomized anyways
            String option = sortableSyllArray.get(i).syllable;
            if (option.length() >= 2 && initialSyll.length() >= 2){
                if (option.charAt(0) == initialSyll.charAt(0)
                        && option.charAt(1) == initialSyll.charAt(1)){
                    answerChoices.add(option);
                }else if(option.charAt(0) == initialSyll.charAt(0)){
                    answerChoices.add(option);
                }
            }else{
                if(option.charAt(0) == initialSyll.charAt(0)){
                    answerChoices.add(option);
                }else if(option.charAt(option.length()-1) == initialSyll.charAt(initialSyll.length()-1)){
                    answerChoices.add(option);
                }
            }

            i++;
        }

        int j = 0;
        while (answerChoices.size() < visibleTiles){
            //this will probably never happen
            answerChoices.add(sortableSyllArray.get(j).syllable);
            j++;
        }

        List<String> answerChoicesList = new ArrayList<>(answerChoices); //so we can index into answer choices now

        for (int t = 0; t < TILE_BUTTONS.length; t++){
            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (sortableSyllArray.get(t).syllable.equals(initialSyll) && t < visibleTiles) {
                correctSyllRepresented = true;
            }

            String tileColorStr = COLORS[t % 5];
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3){
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
                    if (answerChoicesList.get(t) == initialSyll){
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
            gameTile.setText(initialSyll);

        }

    }

    private void setUpTiles() {

        boolean correctTileRepresented = false;

        Start.Tile answer = tileHashMap.find(initialTile);

        // LINES 369 - 404 ARE SET UP FOR LEVEL 2
        answerChoices.clear();
        answerChoices.add(initialTile);
        answerChoices.add(answer.altTiles[0]);
        answerChoices.add(answer.altTiles[1]);
        answerChoices.add(answer.altTiles[2]);

        int i = 0;
        while (answerChoices.size() < visibleTiles && i < CorV.size()){
            // and does so while skipping repeats because it is a set
            // and a set has no order so it will be randomized anyways
            String option = CorV.get(i);
            if (option.length() >= 2 && initialTile.length() >= 2){
                if (option.charAt(0) == initialTile.charAt(0)
                        && option.charAt(1) == initialTile.charAt(1)){
                    answerChoices.add(option);
                }else if(option.charAt(0) == initialTile.charAt(0)){
                    answerChoices.add(option);
                }
            }else{
                if(option.charAt(0) == initialTile.charAt(0)){
                    answerChoices.add(option);
                }else if(option.charAt(option.length()-1) == initialTile.charAt(initialTile.length()-1)){
                    answerChoices.add(option);
                }
            }

            i++;
        }

        int j = 0;
        while (answerChoices.size() < visibleTiles){
            answerChoices.add(CorV.get(j));
            j++;
        }

        List<String> answerChoicesList = new ArrayList<>(answerChoices); //so we can index into answer choices now

        for (int t = 0; t < TILE_BUTTONS.length; t++ ) {

            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (CorV.get(t).equals(initialTile) && t < visibleTiles) {
                correctTileRepresented = true;
            }

            String tileColorStr = COLORS[t % 5];
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3){ //random wrong
            if (t < visibleTiles) {
                gameTile.setText(CorV.get(t)); // KP
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
                } else {
                    gameTile.setText(String.valueOf(t + 1));
                    gameTile.setBackgroundResource(R.drawable.textview_border);
                    gameTile.setTextColor(Color.parseColor("#000000")); // black
                    gameTile.setClickable(false);
                    gameTile.setVisibility(View.INVISIBLE);
                }
            }else{ //distractors
                if (t < visibleTiles) {
                    // think through this logic more -- how to get distractor syllables in there but
                    // also fill other syllables beyond the 3 distractors

                    // first make a visibleTiles-sized array with the correct answer,
                    // its distractor syllables, and any other syllables that start with the same tile;
                    // filter out repeats

                    // then iterate through TILE_BUTTONS and fill them in using the other array, shuffled
                    if (answerChoicesList.get(t) == initialTile){
                        correctTileRepresented = true;
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

        if (!correctTileRepresented) {

            // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles

            Random rand = new Random();
            int randomNum = rand.nextInt(visibleTiles - 1); // KP
            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(initialTile);

        }

    }

    private void respondToTileSelection(int justClickedTile) { //for both tiles and syllables

        if (mediaPlayerIsPlaying) {
            return;
        }

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String correct = "";
        if (syllableGame.equals("S")){
            correct = initialSyll;
        }else{
            correct = initialTile;
        }

        int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 18, t uses the array ID (between [0] and [17]
        TextView tile = findViewById(TILE_BUTTONS[tileNo]);
        String selectedTile = tile.getText().toString();

        if (correct.equals(selectedTile)) {
            // Good job! You chose the right tile
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points++;
            georgiaPoints++;
            pointsEarned.setText(String.valueOf(georgiaPoints));

            trackerCount++;
            if(trackerCount>=12){
                georgiaHasChecked12Trackers = true;
            }
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedGeorgiaPoints_level" + challengeLevel + "_player" + playerString, georgiaPoints);
            editor.putBoolean("storedGeorgiaHasChecked12Trackers_level" + challengeLevel +"_player" + playerString, georgiaHasChecked12Trackers);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int t = 0; t < TILE_BUTTONS.length; t++ ) {
                TextView gameTile = findViewById(TILE_BUTTONS[t]);
                gameTile.setClickable(false);
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


    public void onBtnClick (View view) {
        respondToTileSelection(Integer.parseInt((String)view.getTag())); // KP
    }


    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view){
        if(getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

}
