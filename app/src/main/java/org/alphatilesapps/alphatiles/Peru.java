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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Peru extends GameActivity {

    String gameIDString;
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int peruPoints;
    boolean peruHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.word1, R.id.word2, R.id.word3, R.id.word4
    };

    protected int[] getTileButtons() {return TILE_BUTTONS;}

    protected int[] getWordImages() {return null;}

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
            audioInstructionsResID = res.getIdentifier("peru_" + challengeLevel, "raw", context.getPackageName());
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

        int gameID = R.id.peruCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger( Peru.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.peru);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.peruCL);
        }

        points = getIntent().getIntExtra("points", 0); // KP
        peruPoints = getIntent().getIntExtra("peruPoints", 0); // LM
        peruHasChecked12Trackers = getIntent().getBooleanExtra("peruHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        peruPoints = prefs.getInt("storedPeruPoints_level" + challengeLevel + "_player" + playerString, 0);
        peruHasChecked12Trackers = prefs.getBoolean("storedPeruHasChecked12Trackers_level" + challengeLevel + "_player" + playerString, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP
        visibleTiles = TILE_BUTTONS.length;

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(peruPoints));

        /*SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);*/
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);
        if(trackerCount >= 12){
            peruHasChecked12Trackers = true;
        }

        updateTrackers();

        setTextSizes();

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

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

        for (int w = 0; w < TILE_BUTTONS.length; w++) {

            TextView nextWord = findViewById(TILE_BUTTONS[w]);
            if (w == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) nextWord.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            nextWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        }

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

    public void repeatGame (View view) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain () {

        repeatLocked = true;

        boolean freshWord = false;
        Random rand = new Random();

        while(!freshWord) {
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

        parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
        int tileLength = tilesInArray(parsedWordArrayFinal);

        // Set thematic colors for four word choice TextViews, also make clickable
        for (int i = 0; i < TILE_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(TILE_BUTTONS[i]);
            String wordColorStr = COLORS[i];
            int wordColorNo = Color.parseColor(wordColorStr);
            nextWord.setBackgroundColor(wordColorNo);
            nextWord.setTextColor(Color.parseColor("#FFFFFF")); // white
            nextWord.setClickable(true);
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        int randomNum2 = rand.nextInt(4);    // need to choose which of four words will be spelled correctly (and other three will be modified to become incorrect
        List<String> shuffledDistractorTiles = Arrays.asList(Start.tileList.get(Start.tileList.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).altTiles);
        Collections.shuffle(shuffledDistractorTiles);

        int incorrectLapNo = 0;
        for (int i = 0; i < TILE_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(TILE_BUTTONS[i]);
            if (i == randomNum2) {
                nextWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP)); // the correct answer (the unmodified version of the word)
            } else {

                incorrectLapNo++;
                boolean isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates (or dangerous combinations)

                switch (challengeLevel) {

                    case 1:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (THE FIRST TILE) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO
                        while(isDuplicateAnswerChoice){
                            List<String> tempArray1 = new ArrayList<>(parsedWordArrayFinal);
                            tempArray1.set(0, shuffledDistractorTiles.get(incorrectLapNo-1)); // KP // LM
                            StringBuilder builder1 = new StringBuilder("");
                            for(String s : tempArray1) {
                                builder1.append(s);
                            }
                            String incorrectChoice1 = builder1.toString();
                            nextWord.setText(incorrectChoice1);
                            isDuplicateAnswerChoice = false;
                            for(int j = 0; j< incorrectChoice1.length() -2; j++){
                                if(incorrectChoice1.substring(j, j+3).compareTo("للہ") == 0){
                                    isDuplicateAnswerChoice = true;
                                }
                            }

                        }
                        break;
                    case 2:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS ANY GAME TILE FROM THE WHOLE ARRAY

                        while(isDuplicateAnswerChoice) {
                            int randomNum3 = rand.nextInt(tileLength - 1);       // KP // this represents which position in word string will be replaced
                            int randomNum4 = rand.nextInt(Start.tileList.size());       // KP // this represents which game tile will overwrite some part of the correct wor

                            List<String> tempArray2 = new ArrayList<>(parsedWordArrayFinal);

                            tempArray2.set(randomNum3, Start.tileList.get(randomNum4).baseTile); // KP
                            StringBuilder builder2 = new StringBuilder("");
                            for (String s : tempArray2) {
                                builder2.append(s);
                            }
                            String incorrectChoice2 = builder2.toString();
                            nextWord.setText(incorrectChoice2);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for(int answerChoice = 0; answerChoice < i; answerChoice++){
                                if(incorrectChoice2.compareTo(((TextView)findViewById(TILE_BUTTONS[answerChoice])).getText().toString()) == 0){
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            if(incorrectChoice2.compareTo(wordInLOP) == 0){
                                isDuplicateAnswerChoice = true;
                            }
                            for(int j = 0; j< incorrectChoice2.length() -2; j++){
                                if(incorrectChoice2.substring(j, j+3).compareTo("للہ") == 0){
                                    isDuplicateAnswerChoice = true;
                                }
                            }

                        }
                        break;

                    case 3:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO

                        isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates

                        while(isDuplicateAnswerChoice) {
                            int randomNum5 = rand.nextInt(tileLength - 1);       // this represents which position in word string will be replaced
                            List<String> tempArray3 = new ArrayList<>(parsedWordArrayFinal);
                            tempArray3.set(randomNum5, Start.tileList.returnRandomCorrespondingTile(parsedWordArrayFinal.get(randomNum5)));
                            StringBuilder builder3 = new StringBuilder("");
                            for (String s : tempArray3) {
                                builder3.append(s);
                            }
                            String incorrectChoice3 = builder3.toString();
                            nextWord.setText(incorrectChoice3);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for(int answerChoice = 0; answerChoice < i; answerChoice++){
                                if(incorrectChoice3.compareTo(((TextView)findViewById(TILE_BUTTONS[answerChoice])).getText().toString()) == 0){
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            for(int j = 0; j< incorrectChoice3.length() -2; j++){
                                if(incorrectChoice3.substring(j, j+3).compareTo("للہ") == 0){
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                        }
                        break;
                    default:

                }
            }
        }
    }

    private void respondToWordSelection(int justClickedWord) {

        int t = justClickedWord - 1; //  justClickedWord uses 1 to 4, t uses the array ID (between [0] and [3]
        TextView chosenWord = findViewById(TILE_BUTTONS[t]);
        String chosenWordText = chosenWord.getText().toString();

        if (chosenWordText.equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
            // Good job!
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=2;
            peruPoints+=2;
            pointsEarned.setText(String.valueOf(peruPoints));

            trackerCount++;

            if(trackerCount>=12){
                peruHasChecked12Trackers = true;
            }
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedPeruPoints_level" + challengeLevel + "_player" + playerString, peruPoints);
            editor.putBoolean("storedPeruHasChecked12Trackers_level" + challengeLevel + "_player" + playerString, peruHasChecked12Trackers);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int w = 0; w < TILE_BUTTONS.length; w++ ) {
                TextView nextWord = findViewById(TILE_BUTTONS[w]);
                nextWord.setClickable(false);
                if (w != t) {
                    String wordColorStr = "#A9A9A9"; // dark gray
                    int wordColorNo = Color.parseColor(wordColorStr);
                    nextWord.setBackgroundColor(wordColorNo);
                    nextWord.setTextColor(Color.parseColor("#000000")); // black
                }
            }

            playCorrectSoundThenActiveWordClip(false);

        } else {
            playIncorrectSound();
        }
    }

    public void onWordClick (View view) {
        int wordNo = Integer.parseInt((String)view.getTag());
        respondToWordSelection(wordNo);
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
