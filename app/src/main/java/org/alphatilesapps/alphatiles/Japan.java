package org.alphatilesapps.alphatiles;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

public class Japan extends GameActivity {

    /*JP:
    1. choose a word with <= 7 tiles
    2. display the word up top
    3. parse the word into tiles and fill in the layout tiles
    4. parse the word into syllables, and every time the user clicks a button, check if the adjacent
    two tiles together form one of the syllables ?
        - alternative approach: calculate which buttons should be pressed by parsing the word into syllables,
        determining how many tiles are in the first syllable, in the second, etc. and choosing the buttons that way

     */

    // TO DO:
    // fix gem text size
    // write better comments and documentation
    // ask literacy advisors about levels -- what else should be unique about 1 vs 2
    // centerGamesHomeImage


    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    ArrayList<TextView> joinedTracker = new ArrayList<>();
    ArrayList<TextView> originalLayout = new ArrayList<>();
    ArrayList<Integer> buttonIDs = new ArrayList<>();
    HashMap<Integer, Integer> numsToButtons = new HashMap<>();
    int visibleViews = 0;
    int visibleViewsImm = 0;
    int MAX_TILES = 0;
    ArrayList<String> correctSyllabification = new ArrayList<>();

    protected static final int[] TILES_AND_BUTTONS_12 = {
            R.id.tile01, R.id.button1, R.id.tile02, R.id.button2, R.id.tile03, R.id.button3,
            R.id.tile04, R.id.button4, R.id.tile05, R.id.button5, R.id.tile06, R.id.button6,
            R.id.tile07, R.id.button7, R.id.tile08, R.id.button8, R.id.tile09, R.id.button9,
            R.id.tile10, R.id.button10, R.id.tile11, R.id.button11, R.id.tile12
    };

    protected static final int[] TILES_AND_BUTTONS_7 = {
            R.id.tile01, R.id.button1, R.id.tile02, R.id.button2, R.id.tile03, R.id.button3,
            R.id.tile04, R.id.button4, R.id.tile05, R.id.button5, R.id.tile06, R.id.button6,
            R.id.tile07
    };

    protected static int[] TILES_AND_BUTTONS;


    @Override
    protected int[] getTileButtons() {
        return TILES_AND_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());

        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = 0;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int gameID = 0;
        if (challengeLevel == 1) {
            setContentView(R.layout.japan_7);
            TILES_AND_BUTTONS = new int[13];
            for (int i = 0; i < TILES_AND_BUTTONS_7.length; i++) {
                TILES_AND_BUTTONS[i] = TILES_AND_BUTTONS_7[i];
            }
            MAX_TILES = 7;
            gameID = R.id.japancl_7;
        } else if (challengeLevel == 2) {
            setContentView(R.layout.japan_12);
            TILES_AND_BUTTONS = new int[23];
            for (int i = 0; i < TILES_AND_BUTTONS_12.length; i++) {
                TILES_AND_BUTTONS[i] = TILES_AND_BUTTONS_12[i];
            }
            MAX_TILES = 12;
            gameID = R.id.japancl_12;
        }

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);     // forces landscape mode only

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        int j = 1;
        for (int i = 0; i < TILES_AND_BUTTONS.length; i++) {
            joinedTracker.add(findViewById(TILES_AND_BUTTONS[i]));
            originalLayout.add(findViewById(TILES_AND_BUTTONS[i]));
            if (i % 2 == 1) {
                //button
                numsToButtons.put(j, TILES_AND_BUTTONS[i]);
                buttonIDs.add(TILES_AND_BUTTONS[i]);
                j++;
            }
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        updatePointsAndTrackers(0);
        play();
    }

    private void play() {
        repeatLocked = true;
        setAdvanceArrowToGray();
        setWord();
        displayWordRef();
        displayTileChoices();
        setVisButtonsClickable();
        setTilesUnclickable();
        ImageView wordImage = (ImageView) findViewById(R.id.wordImage);
        wordImage.setClickable(true);
    }

    private void setWord() {

        chooseWord();

        while(tileList.parseWordIntoTiles(wordInLOP).size() > MAX_TILES) { //JP: choose word w/ <= 12 tiles
            chooseWord();
        }

        parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
        parsedWordArrayFinal.removeAll(SAD);
        parsedWordSyllArrayFinal = syllableList.parseWordIntoSyllables(wordInLOP);
        parsedWordSyllArrayFinal.removeAll(SAD);
    }

    private void displayWordRef() {
        TextView ref = findViewById(R.id.word);
        ref.setText(wordList.stripInstructionCharacters(wordInLOP));
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);
    }

    private void displayTileChoices() {
        visibleViews = parsedWordArrayFinal.size() * 2 - 1; // accounts for both buttons and tiles
        visibleViewsImm = parsedWordArrayFinal.size() * 2 - 1;

        int j = 0;
        for (int i = 0; i < visibleViews; i = i + 2) {
            String tileColorStr = COLORS.get(i % 5);
            int tileColor = Color.parseColor(tileColorStr);
            TextView tile = findViewById(TILES_AND_BUTTONS[i]);
            tile.setText(parsedWordArrayFinal.get(j));
            tile.setClickable(false);
            tile.setVisibility(View.VISIBLE);
            tile.setBackgroundColor(tileColor);
            tile.setTextColor(Color.parseColor("#FFFFFF")); // white;
            j++;
        }
        for (int i = visibleViews; i < TILES_AND_BUTTONS.length; i++) {
            TextView tile = findViewById(TILES_AND_BUTTONS[i]);
            tile.setClickable(false);
            tile.setVisibility(View.INVISIBLE);
        }

    }

    private void setVisButtonsClickable() {
        for (int i = 1; i < visibleViews; i = i + 2) {
            TextView button = findViewById(TILES_AND_BUTTONS[i]);
            button.setClickable(true);
        }
    }

    private void setAllButtonsUnclickable() {
        for (int i = 1; i < MAX_TILES - 1; i = i + 2) {
            TextView button = findViewById(TILES_AND_BUTTONS[i]);
            button.setClickable(false);
        }
    }

    private void setTilesUnclickable() {
        for (int i = 0; i < TILES_AND_BUTTONS.length; i = i + 2) {
            TextView tile = findViewById(TILES_AND_BUTTONS[i]);
            tile.setClickable(false);
        }
    }

    public void onClickJapan(View view) {
        joinTiles((TextView) view);
        respondToSelection();
    }

    public void onClickTile(View view) {
        // change color back to grey
        // change constraints back to original
        // set visibility of button back to visible
        separateTiles((TextView) view);
        respondToSelection();
    }

    public void onClickWord(View view) {
        playActiveWordClip(false);
    }

    public void repeatGame(View view) {
        if (!repeatLocked) {
            resetLayout();
            play();
        }
    }

    private void resetLayout() {
        joinedTracker.clear();
        for (int i = 0; i < TILES_AND_BUTTONS.length; i++) {
            joinedTracker.add(findViewById(TILES_AND_BUTTONS[i]));
            findViewById(TILES_AND_BUTTONS[i]).setVisibility(View.VISIBLE);
            if (i % 2 == 0) {
                findViewById(TILES_AND_BUTTONS[i]).setClickable(false);
            } else {
                findViewById(TILES_AND_BUTTONS[i]).setClickable(true);
            }
        }

        int gameID = 0;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        for (int i = 1; i < TILES_AND_BUTTONS.length; i++) {
            constraintSet.connect(TILES_AND_BUTTONS[i - 1], ConstraintSet.END, TILES_AND_BUTTONS[i],
                    ConstraintSet.START, 0); //end of t1 to start of b1
            constraintSet.connect(TILES_AND_BUTTONS[i], ConstraintSet.START, TILES_AND_BUTTONS[i - 1],
                    ConstraintSet.END, 0); //start of b1 to end of t1
            constraintSet.applyTo(constraintLayout); //end of b1 to start of t2
        }


/*
        ConstraintLayout constraintLayout = findViewById(R.id.japancl);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        for (int i = 1; i < TILES_AND_BUTTONS.length - 1; i++){
            constraintSet.connect(TILES_AND_BUTTONS[i-1],ConstraintSet.END,TILES_AND_BUTTONS[i],
                    ConstraintSet.START,0); //end of one to start of next
            constraintSet.connect(TILES_AND_BUTTONS[i],ConstraintSet.START,TILES_AND_BUTTONS[i-1],
                    ConstraintSet.END,0); // start of next to end of one

 */
            /*
            constraintSet.connect(TILES_AND_BUTTONS[i],ConstraintSet.END,TILES_AND_BUTTONS[i+1],
                    ConstraintSet.START,0); // end of b to start of next t
            constraintSet.connect(TILES_AND_BUTTONS[i+1],ConstraintSet.START,TILES_AND_BUTTONS[i],
                    ConstraintSet.END,0); // start of next t to end of b

             */
        //}
    }

    private void separateTiles(TextView clickedTile) {
        // find the clicked tile in JoinedTracker
        // check if there is a button missing on either side
        // if there is, add it back in on that side

        int indexOfTileJT = joinedTracker.indexOf(clickedTile);

        int gameID = 0;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }

        if (visibleViews == 1) {
            // TO DO: only one tile ?
        } else if (indexOfTileJT == 0) { //first tile
            // check index + 1
            if (!joinedTracker.get(1).getText().toString().equals(".".toString())) { // if it's NOT a button
                // restore the button
                TextView button = findViewById(TILES_AND_BUTTONS[1]);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and nextTile
                TextView nextTile = findViewById(TILES_AND_BUTTONS[2]);

                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(TILES_AND_BUTTONS[0], ConstraintSet.END, TILES_AND_BUTTONS[1],
                        ConstraintSet.START, 0); //end of t1 to start of b1
                constraintSet.connect(TILES_AND_BUTTONS[1], ConstraintSet.START, TILES_AND_BUTTONS[0],
                        ConstraintSet.END, 0); //start of b1 to end of t1
                constraintSet.connect(TILES_AND_BUTTONS[2], ConstraintSet.START, TILES_AND_BUTTONS[1],
                        ConstraintSet.END, 0); //start of t2 to end of b1
                constraintSet.connect(TILES_AND_BUTTONS[1], ConstraintSet.END, TILES_AND_BUTTONS[2], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout); //end of b1 to start of t2

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS.get(i % 5);
                int tileColor = Color.parseColor(tileColorStr);
                nextTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS.get(i % 5);
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);
                clickedTile.setClickable(false);

                joinedTracker.add(1, button);
                visibleViews = visibleViews + 1;
            }
        } else if (indexOfTileJT == visibleViews - 1) { //final tile
            // check index - 1
            if (!joinedTracker.get(indexOfTileJT - 1).getText().toString().equals(".".toString())) { // if it's NOT a button

                int indexOfMissingButton = visibleViewsImm - 2;
                // restore the button
                TextView button = findViewById(TILES_AND_BUTTONS[indexOfMissingButton]);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and prevTile
                TextView prevTile = findViewById(TILES_AND_BUTTONS[indexOfMissingButton - 1]);
                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                //end of prevTile to start of button
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton - 1], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMissingButton], ConstraintSet.START, 0);
                //start of button to end of prevTile
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton], ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMissingButton - 1], ConstraintSet.END, 0);
                //start of last tile to end of button
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton + 1], ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMissingButton], ConstraintSet.END, 0);
                //end of button to start of last tile
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMissingButton + 1], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS.get(i % 5);
                int tileColor = Color.parseColor(tileColorStr);
                prevTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS.get(i % 5);
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);
                clickedTile.setClickable(false);

                joinedTracker.add(indexOfTileJT, button);
                visibleViews = visibleViews + 1;
            }
        } else {  //any other tile
            // check index + 1 and index -1
            if (!joinedTracker.get(indexOfTileJT - 1).getText().toString().equals(".".toString())) { // if - 1 NOT a button

                int indexOfMBinOG = originalLayout.indexOf(clickedTile) - 1;
                // restore the button
                TextView button = originalLayout.get(indexOfMBinOG);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and prevTile
                TextView prevTile = originalLayout.get(indexOfMBinOG - 1);
                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();

                constraintSet.clone(constraintLayout);
                // end of left tile to start of button
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG - 1], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.START, 0);
                // start of button to end of left tile
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMBinOG - 1], ConstraintSet.END, 0);
                // start of right tile to end of button
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG + 1], ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.END, 0);
                // end of button to start of right tile
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMBinOG + 1], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS.get(i % 5);
                int tileColor = Color.parseColor(tileColorStr);
                prevTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS.get(i % 5);
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);
                clickedTile.setClickable(false);


                joinedTracker.add(indexOfTileJT, button);
                visibleViews = visibleViews + 1;

                //check next button after prior button already added back in
                //indexOfTileJT now becomes the location of the next button
                if (!joinedTracker.get(indexOfTileJT).getText().toString().equals(".".toString())) { // if it's NOT a button

                    indexOfMBinOG = originalLayout.indexOf(clickedTile) + 1;

                    // restore the button
                    button = originalLayout.get(indexOfMBinOG);
                    button.setVisibility(View.VISIBLE);
                    button.setClickable(true);

                    // reset constraints of clickedTile and nextTile
                    TextView nextTile = originalLayout.get(indexOfMBinOG + 1);
                    constraintLayout = findViewById(gameID);
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);

                    // end of button to start of right tile
                    constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.END,
                            TILES_AND_BUTTONS[indexOfMBinOG + 1], ConstraintSet.START, 0);
                    // start of button to end of left tile
                    constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.START,
                            TILES_AND_BUTTONS[indexOfMBinOG - 1], ConstraintSet.END, 0);
                    // start of right tile to end of button
                    constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG + 1], ConstraintSet.START,
                            TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.END, 0);
                    // end of left tile to start of button
                    constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG - 1], ConstraintSet.END,
                            TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.START, 0);
                    constraintSet.applyTo(constraintLayout);

                    rand = new Random();
                    i = rand.nextInt(10);
                    tileColorStr = COLORS.get(i % 5);
                    tileColor = Color.parseColor(tileColorStr);
                    nextTile.setBackgroundColor(tileColor);

                    i = rand.nextInt(10);
                    tileColorStr = COLORS.get(i % 5);
                    tileColor = Color.parseColor(tileColorStr);
                    clickedTile.setBackgroundColor(tileColor);
                    clickedTile.setClickable(false);

                    joinedTracker.add(indexOfTileJT, button);
                    visibleViews = visibleViews + 1;
                }
            }
            // i think the issue is joinedTrack being dynamic; it changes here before this next if statement
            // so now this else if deals with next button if prior button did NOT have to be restored
            else if (!joinedTracker.get(indexOfTileJT + 1).getText().toString().equals(".".toString())) { // if it's NOT a button

                int indexOfMBinOG = originalLayout.indexOf(clickedTile) + 1;

                // restore the button
                TextView button = originalLayout.get(indexOfMBinOG);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and nextTile
                TextView nextTile = originalLayout.get(indexOfMBinOG + 1);
                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);

                // end of button to start of right tile
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMBinOG + 1], ConstraintSet.START, 0);
                // start of button to end of left tile
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMBinOG - 1], ConstraintSet.END, 0);
                // start of right tile to end of button
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG + 1], ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.END, 0);
                // end of left tile to start of button
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG - 1], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMBinOG], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS.get(i % 5);
                int tileColor = Color.parseColor(tileColorStr);
                nextTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS.get(i % 5);
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);
                clickedTile.setClickable(false);

                int newIndex = joinedTracker.indexOf(clickedTile) + 1;
                joinedTracker.add(newIndex, button);
                visibleViews = visibleViews + 1;
            }

        }
    }

    private String removeSADFromWordInLOP(String wordInLOP) {
        // JP: not working how I want
        String finalStr = wordInLOP;
        for (String ch : SAD) {
            finalStr = finalStr.replaceAll("." + ch, ""); // only remove one period
            // so that there is still a syllable break after the SAD is removed
        }

        return finalStr;
    }

    private void respondToSelection() {
        // check if correct and change color
        // if correct, set those Tiles unclickable to help the user
        // build string of configuration that we have so far

        // ISSUE TO FIX: NOT ONLY IF SYLL IN INPROGRESSSYLLABIFICATION BUT
        // MUST BE IN CORRECT POSITION TOO
        StringBuilder config = new StringBuilder();
        // that one in-progress syll in partialConfig
        for (int i = 0; i < visibleViews; i++) {
            TextView view = joinedTracker.get(i);
            config.append(view.getText());
        }
        String wordInLOPNoSAD = removeSADFromWordInLOP(wordInLOP);
        if (config.toString().equals(wordInLOPNoSAD)) { // completely correct
            //great job!
            repeatLocked = false;
            setAdvanceArrowToBlue();
            playCorrectSoundThenActiveWordClip(false); //JP not sure what this bool is for
            updatePointsAndTrackers(1);

            for (int i = 0; i < visibleViewsImm; i++) {
                if (i % 2 == 0) {
                    TextView view = findViewById(TILES_AND_BUTTONS[i]);
                    view.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                    view.setTextColor(Color.parseColor("#FFFFFF")); // white
                    view.setClickable(false);
                } else {
                    TextView view = findViewById(TILES_AND_BUTTONS[i]);
                    view.setClickable(false);
                }

            }
            setOptionsRowClickable();
        } else { // one or more syllables correct

            // find number of tiles per correct syllable
            ArrayList<Integer> numTilesPerSyll = new ArrayList<>();
            for (String syll : parsedWordSyllArrayFinal) {
                ArrayList<String> parsedSyllIntoTiles = tileList.parseWordIntoTiles(syll);
                numTilesPerSyll.add(parsedSyllIntoTiles.size());
                parsedSyllIntoTiles.clear();
            }

            ArrayList<Integer> correctButtons = new ArrayList<>();
            int sum = 0;
            for (int num : numTilesPerSyll) {
                sum = sum + num;
                correctButtons.add(numsToButtons.get(sum)); // maps numbers to R.id's
            }

            // now somehow check if sequence of buttons in joinedTiles anywhere matches correctButtons
            // if so, turn all tiles between those two buttons in joinedTiles green and make them unClickable

            // we know that all of the odd indexes in TILES_AND_BUTTONS are buttons
            // when we find an id in joinedTracker that matches an id in correctButtons,
            // keep iterating through joinedTracker and store intermediate tiles in a list
            // until you reach another button, then check if that next button is also the next button in
            // correctButtons
            // if so, go back and turn all the intermediate tiles in the list green and unclickable
            // if not, empty the list and pick a new first button and repeat the process until
            // you have iterated over visibleViews number of items in joinedTracker

            // but what about when all tiles are gone?

            boolean buildingIntermediate = true;
            TextView firstButton = joinedTracker.get(0);
            ArrayList<TextView> intermediateTiles = new ArrayList<>();
            for (TextView view : joinedTracker) {
                if (buttonIDs.contains(view.getId())) { // must be button
                    if (!correctButtons.contains(view.getId())) {
                        // not a correct button
                        intermediateTiles.clear();
                        buildingIntermediate = false;
                    } else if (correctButtons.contains(view.getId()) && buildingIntermediate) {
                        // is a correct button and its 2nd in sequence
                        // that one syllable is correct so turn them all green

                        int secondButtonIndex = correctButtons.indexOf(view.getId());
                        // JP: this also needs to check that it didn't skip a correct button?
                        boolean buttonPairComplete = true;
                        if (secondButtonIndex > 0) {
                            buttonPairComplete = correctButtons.get(secondButtonIndex - 1)
                                    .equals(firstButton.getId());
                        }
                        if (intermediateTiles.size() != sum
                                && buttonPairComplete) {
                            // this prevents all tiles from turning green if all buttons have been clicked
                            // but in wrong order
                            for (TextView tile : intermediateTiles) {
                                tile.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                                tile.setTextColor(Color.parseColor("#FFFFFF")); // white
                                tile.setClickable(false);
                            }
                            view.setClickable(false); //set button at end of sequence unclickable
                            firstButton.setClickable(false); //set button (or tile if index 0) at beginning of sequence unclickable
                        }
                    } else if (correctButtons.contains(view.getId())) {
                        buildingIntermediate = true;
                        firstButton = view; // maybe use firstButton to indicate whether two syllables are incorrectly put together?
                    }
                } else { //must be tile
                    if (buildingIntermediate) {
                        intermediateTiles.add(view);
                    }
                }
            }

        }
    }

    private void joinTiles(TextView button) {
        // make the button between the tiles invisible - DONE
        // change the constraints so the two tiles touch each other - DONE
        // change the color of the two tiles depending on whether they're correct - TO DO
        // make them clickable - DONE

        button.setClickable(false);
        button.setVisibility(View.INVISIBLE);

        int buttonIndex = originalLayout.indexOf(button);
        TextView leftTile = originalLayout.get(buttonIndex - 1);
        leftTile.setClickable(true);

        TextView rightTile = originalLayout.get(buttonIndex + 1);
        rightTile.setClickable(true);

        int gameID = 0;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        // start of right tile to end of left tile
        constraintSet.connect(TILES_AND_BUTTONS[buttonIndex - 1], ConstraintSet.END,
                TILES_AND_BUTTONS[buttonIndex + 1], ConstraintSet.START, 0);
        // end of left tile to start of right tile
        constraintSet.connect(TILES_AND_BUTTONS[buttonIndex + 1], ConstraintSet.START,
                TILES_AND_BUTTONS[buttonIndex - 1], ConstraintSet.END, 0);
        constraintSet.applyTo(constraintLayout);

        joinedTracker.remove(button);
        visibleViews = visibleViews - 1;

    }
}