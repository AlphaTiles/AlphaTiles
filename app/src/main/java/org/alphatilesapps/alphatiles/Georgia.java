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

import java.util.Collections;
import java.util.Random;

public class Georgia extends GameActivity {

    Start.TileList sortableTilesArray; // KP
    String initialTile = "";
    int visibleTiles; // will be 6, 12 or 18 based on challengeLevel 1, 2 or 3
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int georgiaPoints;

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18
    };

    protected int[] getTileButtons() {return TILE_BUTTONS;}

    protected int[] getWordImages() {return null;}

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.georgiaCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
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
        setContentView(R.layout.georgia);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        georgiaPoints = getIntent().getIntExtra("georgiaPoints", 0); // LM

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        georgiaPoints = prefs.getInt("storedGeorgiaPoints_level" + challengeLevel + "_player" + playerString, 0);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        switch (challengeLevel) {
            case 2:
                visibleTiles = 12;
                break;
            case 3:
                visibleTiles = 18;
                break;
            default:
                visibleTiles = 6;
        }

        sortableTilesArray = (Start.TileList)Start.tileList.clone(); // KP

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(georgiaPoints));

        /*SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);*/
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

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

        for (int t = 0; t < TILE_BUTTONS.length; t++) {

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

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.7 * scaling * percentHeight * heightOfDisplay);
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
        Collections.shuffle(sortableTilesArray); // KP
        chooseWord();
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        setUpTiles();
        playActiveWordClip(false);
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
        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
        initialTile = parsedWordArrayFinal.get(0);

    }

    private void setUpTiles() {

        boolean correctTileRepresented = false;

        for (int t = 0; t < TILE_BUTTONS.length; t++ ) {

            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (sortableTilesArray.get(t).baseTile.equals(initialTile) && t < visibleTiles) {
                correctTileRepresented = true;
            }

            String tileColorStr = COLORS[t % 5];
            int tileColor = Color.parseColor(tileColorStr);

            if (t < visibleTiles) {
                gameTile.setText(sortableTilesArray.get(t).baseTile); // KP
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

        }

        if (!correctTileRepresented) {

            // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles

            Random rand = new Random();
            int randomNum = rand.nextInt(visibleTiles - 1); // KP
            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(initialTile);

        }

    }

    private void respondToTileSelection(int justClickedTile) {

        if (mediaPlayerIsPlaying) {
            return;
        }

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 18, t uses the array ID (between [0] and [17]
        TextView tile = findViewById(TILE_BUTTONS[tileNo]);
        String selectedTile = tile.getText().toString();

        if (initialTile.equals(selectedTile)) {
            // Good job! You chose the right tile
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points++;
            georgiaPoints++;
            pointsEarned.setText(String.valueOf(georgiaPoints));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedGeorgiaPoints_level" + challengeLevel + "_player" + playerString, georgiaPoints);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int t = 0; t < TILE_BUTTONS.length; t++ ) {
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
