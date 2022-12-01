package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

//Game of 15
public class China extends GameActivity {
    String[][] threeFourWordInLopLwc = new String[3][2];
    String[] oneThreeWordInLopLwc = new String[2];
    Boolean[] solvedLines = new Boolean[4];
    TextView blankTile;
    int moves;

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16
    };

    protected int[] getTileButtons() {return TILE_BUTTONS;}

    protected int[] getWordImages() {return null;}

    private static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04
    };

    private static final Logger LOGGER = Logger.getLogger(China.class.getName());

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.chinaCL;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOGGER.info("Remember: A: pre super.onCreate");
        super.onCreate(savedInstanceState);
        LOGGER.info("Remember: B: post super.onCreate");
        context = this;
        setContentView(R.layout.china);
        int gameID = R.id.chinaCL;
        LOGGER.info("Remember: C: setContentView complete");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        chinaPoints = getIntent().getIntExtra("chinaPoints", 0); // LM
        chinaHasChecked12Trackers = getIntent().getBooleanExtra("chinaHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        chinaPoints = prefs.getInt("storedchinaPoints_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, 0);
        chinaHasChecked12Trackers = prefs.getBoolean("storedchinaHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        LOGGER.info("Remember: D: three intents gotten");

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            ImageView playNextWordImage = (ImageView) findViewById(R.id.playNextWord);
            ImageView referenceItemImage = (ImageView) findViewById(R.id.referenceItem);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            playNextWordImage.setRotationY(180);
            referenceItemImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }


        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        if(getAudioInstructionsResID()==0) {
            centerGamesHomeImage();
        }

        visibleTiles = 16;

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
        //wip
    }

    private void chooseWords(){
        //For the first three words
        Random rand = new Random();
        int randomNum;
        int tileLength;
        for (int i = 0; i < 3; i++){
            randomNum = rand.nextInt(Start.wordList.size());

            threeFourWordInLopLwc[i][0] = Start.wordList.get(randomNum).nationalWord;
            threeFourWordInLopLwc[i][1] = Start.wordList.get(randomNum).localWord;

            tileLength = tilesInArray(Start.tileList.parseWordIntoTiles(threeFourWordInLopLwc[i][1]));
            for (int j = 0; j < i; j++) {
                if (threeFourWordInLopLwc[i][0].equals(threeFourWordInLopLwc[j][0])) {
                    LOGGER.info("Remember: word rejected for repeating already selected word");
                    i--;
                } else if (tileLength != 4) {
                    LOGGER.info("Remember: word rejected for not being 4 tiles long");
                    i--;
                }
            }
        }

        //For the last word
        boolean cont = true;
        while (cont) {
            randomNum = rand.nextInt(Start.wordList.size());

            oneThreeWordInLopLwc[0] = Start.wordList.get(randomNum).nationalWord;
            oneThreeWordInLopLwc[1] = Start.wordList.get(randomNum).localWord;
            tileLength = tilesInArray(Start.tileList.parseWordIntoTiles(oneThreeWordInLopLwc[1]));
            if (tileLength == 3) {
                LOGGER.info("Remember: word is 3 tiles long");
                cont = false;
            }
        }
    }

    private void setUpTiles() {
        ArrayList<String> tiles = new ArrayList<>();
        for (int t = 0; t < 3; t++){
            tiles.addAll(Start.tileList.parseWordIntoTiles(threeFourWordInLopLwc[t][1]));

            ImageView image = findViewById(WORD_IMAGES[t]);
            int resID = getResources().getIdentifier(threeFourWordInLopLwc[t][0] + "2", "drawable", getPackageName());
            image.setImageResource(resID);
            image.setVisibility(View.VISIBLE);
        }
        tiles.addAll(Start.tileList.parseWordIntoTiles(oneThreeWordInLopLwc[1]));
        Collections.shuffle(tiles);

        ImageView image = findViewById(WORD_IMAGES[3]);
        int resID = getResources().getIdentifier(oneThreeWordInLopLwc[0] + "2", "drawable", getPackageName());
        image.setImageResource(resID);
        image.setVisibility(View.VISIBLE);

        if (tiles.size() != 15){
            LOGGER.info("Remember: Words not long enough.  Trying again.");
            chooseWords();
            setUpTiles();
            return;
        }

        for (int i = 0; i < 15; i++){
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

        while (moves != 0){
            tileX = rand.nextInt(TILE_BUTTONS.length);

            if (isSlideable(tileX) && tileX != lastTile) {
                TextView t = findViewById(TILE_BUTTONS[tileX]);
                swapTiles(t, blankTile);
                lastTile = tileX;
                moves--;
            }
        }
    }

    private void swapTiles(TextView tile1, TextView tile2){
        CharSequence temp = tile1.getText();
        tile1.setText(tile2.getText());
        tile2.setText(temp);

        if (tile1.getText() == ""){
            tile1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tile2.setBackgroundColor(Color.parseColor("#000000"));
            blankTile = tile1;
        }
        else if (tile2.getText() == ""){
            tile2.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tile1.setBackgroundColor(Color.parseColor("#000000"));
            blankTile = tile2;
        }
    }

    private void respondToTileSelection(int justClickedTile) {

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        LOGGER.info("Remember: about to create tileOfTargetRow");

        String blankTag = String.valueOf(blankTile.getTag());

        LOGGER.info("Remember: blankTag = " + blankTag);

        int tileOfTargetRow = Integer.parseInt(blankTag);

        LOGGER.info("Remember: tileOfTargetRow = " + tileOfTargetRow);

        int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 16, tileNo uses the array ID (between [0] and [15]
        TextView tileSelected = findViewById(TILE_BUTTONS[tileNo]);

        if (isSlideable(tileNo)){
            swapTiles(tileSelected, blankTile);
            LOGGER.info("Remember: pre checkLineForSolve ... tileOfTargetRow = " + tileOfTargetRow);
        }

        checkLineForSolve(1);
        checkLineForSolve(5);
        checkLineForSolve(9);
        checkLineForSolve(13);

        if (areAllLinesSolved()) {
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points++;
            pointsEarned.setText(String.valueOf(points));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

//            for (int tileButton : TILE_BUTTONS) {
//                TextView gameTile = findViewById(tileButton);
//                if (gameTile != blankTile) {
//                    String wordColorStr = "#4CAF50"; // theme green
//                    int wordColorNo = Color.parseColor(wordColorStr);
//                    gameTile.setBackgroundColor(wordColorNo);
//                }
//            }
            setOptionsRowClickable();
        }
        else {
            setAllTilesClickable();
            setOptionsRowClickable();
        }

    }

    public void onBtnClick (View view) {
        respondToTileSelection(Integer.parseInt((String)view.getTag())); // KP
    }

    private void checkLineForSolve(int tileInRowToCheck){

        LOGGER.info("Remember: just started private void checkLineForSolve(int tileInRowToCheck) ");
        LOGGER.info("Remember: checkLineForSolve tileInRowToCheck = " + tileInRowToCheck);

        int row = ((tileInRowToCheck - 1) / 4) + 1;
        LOGGER.info("Remember: row = " + row);
        int leftMostTile = (row - 1) * 4;
        LOGGER.info("Remember: leftMostTile = " + leftMostTile);

        String gridWord = "";
        String correctWord = "";
        if (row < 4) {
            correctWord = Start.wordList.stripInstructionCharacters(threeFourWordInLopLwc[row - 1][1]);
            LOGGER.info("Remember: correctWord = " + correctWord);
        } else {
            correctWord = Start.wordList.stripInstructionCharacters(oneThreeWordInLopLwc[1]);
            LOGGER.info("Remember: correctWord = " + correctWord);
        }
        TextView gameTile1 = findViewById(TILE_BUTTONS[leftMostTile]);
        TextView gameTile2 = findViewById(TILE_BUTTONS[leftMostTile + 1]);
        TextView gameTile3 = findViewById(TILE_BUTTONS[leftMostTile + 2]);
        TextView gameTile4 = findViewById(TILE_BUTTONS[leftMostTile + 3]);
        gridWord = gameTile1.getText().toString() + gameTile2.getText().toString() + gameTile3.getText().toString() + gameTile4.getText().toString();

        if(row == 4) {
            if (blankTile.getTag().equals("14") || blankTile.getTag().equals("15")) {
                gridWord = ""; // For the word "cat", will only accept |c|a|t| | or | |c|a|t| but not |c| |a|t| or |c|a| |t|
            }
        }

        LOGGER.info("Remember: gridWord = " + gridWord);
        if (gridWord.equals(correctWord)) {
            LOGGER.info("Remember: gridWord matches correctWord");
            solvedLines[row - 1] = true;
            LOGGER.info("Remember: solvedLines set to true");
            for (int i = leftMostTile; i <= (leftMostTile + 3); i++) {
                LOGGER.info("Remember: i = " + i);
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                if (gameTile == blankTile) {
                    String wordColorStr = "#FFFFFF"; //white
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                    LOGGER.info("Remember: background color changed");
                } else {
                    String wordColorStr = "#4CAF50"; //theme green
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                    LOGGER.info("Remember: background color changed");
                }
            }
        } else {
            LOGGER.info("Remember: gridWord does NOT match correctWord");
            LOGGER.info("Remember: row = " + row);
            solvedLines[row - 1] = false;
            LOGGER.info("Remember: solvedLines set to false");
            for (int i = leftMostTile; i <= (leftMostTile + 3); i++) {
                LOGGER.info("Remember: i = " + i);
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                if (gameTile == blankTile) {
                    String wordColorStr = "#FFFFFF"; //white
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                    LOGGER.info("Remember: background color changed");
                } else {
                    String wordColorStr = "#000000"; //black
                    int wordColorNo = Color.parseColor(wordColorStr);
                    gameTile.setBackgroundColor(wordColorNo);
                    LOGGER.info("Remember: background color changed");
                }
            }
        }
    }

    private boolean areAllLinesSolved(){

        boolean solved = false;

        if (solvedLines[0] == true && solvedLines[1]==true && solvedLines[2] == true && solvedLines[3] == true) {
            solved = true;
        }
        return solved;

    }

    private boolean isSlideable(int tileNo){
        boolean slideable = false;
        TextView tileToCheck;

        if (tileNo != 0 && tileNo != 4 && tileNo != 8 && tileNo != 12){
            tileToCheck = findViewById(TILE_BUTTONS[tileNo - 1]);
            slideable = (tileToCheck == blankTile);
        }

        if (tileNo != 3 && tileNo != 7 && tileNo != 11 && tileNo != 15 && !slideable){
            tileToCheck = findViewById(TILE_BUTTONS[tileNo + 1]);
            slideable = (tileToCheck == blankTile);
        }

        if (tileNo >= 4 && !slideable){
            tileToCheck = findViewById(TILE_BUTTONS[tileNo - 4]);
            slideable = (tileToCheck == blankTile);
        }

        if (tileNo < 12 && !slideable){
            tileToCheck = findViewById(TILE_BUTTONS[tileNo + 4]);
            slideable = (tileToCheck == blankTile);
        }

        LOGGER.info("Remember: slideable " + slideable);
        return slideable;
    }

    @Override
    public void clickPicHearAudio(View view)
    {

        int justClickedImage = Integer.parseInt((String)view.getTag());

        if (justClickedImage == 20) {
            wordInLWC = oneThreeWordInLopLwc[0];
        } else {
            wordInLWC = threeFourWordInLopLwc[justClickedImage-17][0];
        }
        playActiveWordClip(false);

    }

}
