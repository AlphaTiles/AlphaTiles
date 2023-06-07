package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

//Game of 15
public class China extends GameActivity {
    String[][] threeFourWordInLopLwc = new String[3][2];
    String[] oneThreeWordInLopLwc = new String[2];
    Boolean[] solvedLines = new Boolean[4];
    TextView blankTile;
    int moves;
    boolean chinaHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16
    };

    protected int[] getTileButtons() {
        return TILE_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    private static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04
    };

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.chinaCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());

        } catch (Exception e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.china);
        int gameID = R.id.chinaCL;
        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        visibleTiles = 16;
        updatePointsAndTrackers(0);
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

        switch (challengeLevel) {
            case 2:
                moves = 10;
                break;
            case 3:
                moves = 15;
                break;
            default:
                moves = 5;
        }

        repeatLocked = true;
        chooseWords();
        setUpTiles();
        setAllTilesClickable();
        //wip
    }

    private void chooseWords() {
        // Find three four-tile words
        int tileLength;
        for (int i = 0; i < 3; i++) {
            chooseWord();

            threeFourWordInLopLwc[i][0] = wordInLWC;
            threeFourWordInLopLwc[i][1] = wordInLOP;

            tileLength = tileList.parseWordIntoTiles(threeFourWordInLopLwc[i][1]).size();
            for (int j = 0; j < i; j++) { // Find a word that's 4 tiles long and not a duplicate
                if (threeFourWordInLopLwc[i][0].equals(threeFourWordInLopLwc[j][0])) {
                    i--;
                } else if (tileLength != 4) {
                    i--;
                }
            }
        }

        // Find one three-tile word
        tileLength = -1;
        while (!(tileLength==3)) {
            chooseWord();
            oneThreeWordInLopLwc[0] = wordInLWC;
            oneThreeWordInLopLwc[1] = wordInLOP;
            tileLength = tileList.parseWordIntoTiles(oneThreeWordInLopLwc[1]).size();
        }
    }

    private void setUpTiles() {
        ArrayList<String> tiles = new ArrayList<>();
        for (int t = 0; t < 3; t++) {
            tiles.addAll(tileList.parseWordIntoTiles(threeFourWordInLopLwc[t][1]));

            ImageView image = findViewById(WORD_IMAGES[t]);
            int resID = getResources().getIdentifier(threeFourWordInLopLwc[t][0] + "2", "drawable", getPackageName());
            image.setImageResource(resID);
            image.setVisibility(View.VISIBLE);
        }
        tiles.addAll(tileList.parseWordIntoTiles(oneThreeWordInLopLwc[1]));
        Collections.shuffle(tiles);

        ImageView image = findViewById(WORD_IMAGES[3]);
        int resID = getResources().getIdentifier(oneThreeWordInLopLwc[0] + "2", "drawable", getPackageName());
        image.setImageResource(resID);
        image.setVisibility(View.VISIBLE);

        if (tiles.size() != 15) {
            chooseWords();
            setUpTiles();
            return;
        }

        for (int i = 0; i < 15; i++) {
            TextView gameTile = findViewById(TILE_BUTTONS[i]);
            gameTile.setText(tiles.get(i));
            gameTile.setBackgroundColor(Color.parseColor("#000000"));
            gameTile.setTextColor(Color.parseColor("#FFFFFF"));
        }
        TextView finalTile = findViewById(TILE_BUTTONS[15]);
        finalTile.setText("");
        finalTile.setBackgroundColor(Color.parseColor("#FFFFFF"));
        finalTile.setTextColor(Color.parseColor("#FFFFFF"));
        blankTile = finalTile;

        Random rand = new Random();
        int tileX;
        int lastTile = 16;

        while (moves != 0) {
            tileX = rand.nextInt(TILE_BUTTONS.length);

            if (isSlideable(tileX) && tileX != lastTile) {
                TextView t = findViewById(TILE_BUTTONS[tileX]);
                swapTiles(t, blankTile);
                lastTile = tileX;
                moves--;
            }
        }
    }

    private void swapTiles(TextView tile1, TextView tile2) {
        CharSequence temp = tile1.getText();
        tile1.setText(tile2.getText());
        tile2.setText(temp);

        if (tile1.getText() == "") {
            tile1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tile2.setBackgroundColor(Color.parseColor("#000000"));
            blankTile = tile1;
        } else if (tile2.getText() == "") {
            tile2.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tile1.setBackgroundColor(Color.parseColor("#000000"));
            blankTile = tile2;
        }
    }

    private void respondToTileSelection(int justClickedTile) {

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String blankTag = String.valueOf(blankTile.getTag());

        int tileOfTargetRow = Integer.parseInt(blankTag);

        int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 16, tileNo uses the array ID (between [0] and [15]
        TextView tileSelected = findViewById(TILE_BUTTONS[tileNo]);

        if (isSlideable(tileNo)) {
            swapTiles(tileSelected, blankTile);
        }

        checkLineForSolve(1);
        checkLineForSolve(5);
        checkLineForSolve(9);
        checkLineForSolve(13);

        if (areAllLinesSolved()) {
            repeatLocked = false;

            updatePointsAndTrackers(4);

            playCorrectFinalSound();
            setAllTilesUnclickable();
            setOptionsRowClickable();
        } else {
            setAllTilesClickable();
            setOptionsRowClickable();
        }

    }

    public void onBtnClick(View view) {
        respondToTileSelection(Integer.parseInt((String) view.getTag())); // KP
    }

    private void checkLineForSolve(int tileInRowToCheck) {

        int row = ((tileInRowToCheck - 1) / 4) + 1;
        int leftMostTile = (row - 1) * 4;

        String gridWord = "";
        String correctWord = "";
        if (row < 4) {
            correctWord = wordList.stripInstructionCharacters(threeFourWordInLopLwc[row - 1][1]);
        } else {
            correctWord = wordList.stripInstructionCharacters(oneThreeWordInLopLwc[1]);
        }
        TextView gameTile1 = findViewById(TILE_BUTTONS[leftMostTile]);
        TextView gameTile2 = findViewById(TILE_BUTTONS[leftMostTile + 1]);
        TextView gameTile3 = findViewById(TILE_BUTTONS[leftMostTile + 2]);
        TextView gameTile4 = findViewById(TILE_BUTTONS[leftMostTile + 3]);
        gridWord = gameTile1.getText().toString() + gameTile2.getText().toString() + gameTile3.getText().toString() + gameTile4.getText().toString();

        if (row == 4) {
            if (blankTile.getTag().equals("14") || blankTile.getTag().equals("15")) {
                gridWord = ""; // For the word "cat", will only accept |c|a|t| | or | |c|a|t| but not |c| |a|t| or |c|a| |t|
            }
        }

        if (gridWord.equals(correctWord)) {
            solvedLines[row - 1] = true;
            for (int i = leftMostTile; i <= (leftMostTile + 3); i++) {
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                if (gameTile == blankTile) {
                    String wordColorStr = "#FFFFFF"; //white
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                } else {
                    String wordColorStr = "#4CAF50"; //theme green
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                }
            }
        } else {
            solvedLines[row - 1] = false;
            for (int i = leftMostTile; i <= (leftMostTile + 3); i++) {
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                if (gameTile == blankTile) {
                    String wordColorStr = "#FFFFFF"; //white
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                } else {
                    String wordColorStr = "#000000"; //black
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                }
            }
        }
    }

    private boolean areAllLinesSolved() {

        boolean solved = false;

        if (solvedLines[0] == true && solvedLines[1] == true && solvedLines[2] == true && solvedLines[3] == true) {
            solved = true;
        }
        return solved;

    }

    private boolean isSlideable(int tileNo) {
        boolean slideable = false;
        TextView tileToCheck;

        if (tileNo != 0 && tileNo != 4 && tileNo != 8 && tileNo != 12) {
            tileToCheck = findViewById(TILE_BUTTONS[tileNo - 1]);
            slideable = (tileToCheck == blankTile);
        }

        if (tileNo != 3 && tileNo != 7 && tileNo != 11 && tileNo != 15 && !slideable) {
            tileToCheck = findViewById(TILE_BUTTONS[tileNo + 1]);
            slideable = (tileToCheck == blankTile);
        }

        if (tileNo >= 4 && !slideable) {
            tileToCheck = findViewById(TILE_BUTTONS[tileNo - 4]);
            slideable = (tileToCheck == blankTile);
        }

        if (tileNo < 12 && !slideable) {
            tileToCheck = findViewById(TILE_BUTTONS[tileNo + 4]);
            slideable = (tileToCheck == blankTile);
        }

        return slideable;
    }

    @Override
    public void clickPicHearAudio(View view) {

        int justClickedImage = Integer.parseInt((String) view.getTag());

        if (justClickedImage == 20) {
            wordInLWC = oneThreeWordInLopLwc[0];
        } else {
            wordInLWC = threeFourWordInLopLwc[justClickedImage - 17][0];
        }
        playActiveWordClip(false);

    }

}
