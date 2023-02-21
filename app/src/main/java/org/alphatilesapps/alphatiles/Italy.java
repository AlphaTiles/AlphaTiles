package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileListNoSAD;

import org.alphatilesapps.alphatiles.Start.WordList;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Collections;

public class Italy extends GameActivity {

    int italyPoints;
    boolean italyHasChecked12Trackers;
    Start.TileList sortableTilesArray;
    Start.SyllableList sortableSyllArray;
    WordList gameCards = new WordList();
    boolean[] boardCardsFound = new boolean[16];
    int deckIndex = 0;

    protected static final int[] TILE_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04, R.id.choice05, R.id.choice06,
            R.id.choice07, R.id.choice08, R.id.choice09, R.id.choice10, R.id.choice11, R.id.choice12,
            R.id.choice13, R.id.choice14, R.id.choice15, R.id.choice16
    };

    protected static final int[] TILE_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04, R.id.wordImage05, R.id.wordImage06,
            R.id.wordImage07, R.id.wordImage08, R.id.wordImage09, R.id.wordImage10, R.id.wordImage11, R.id.wordImage12,
            R.id.wordImage13, R.id.wordImage14, R.id.wordImage15, R.id.wordImage16
    };

    protected static final int[][] LOTERIA_SEQUENCES = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}, {1, 5, 9, 13}, {2, 6, 10, 14}, {3, 7, 11, 15}, {4, 8, 12, 16}, {1, 6, 11, 16}, {4, 7, 10, 13}};

    @Override
    protected int[] getTileButtons() {
        return TILE_BUTTONS;
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
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1)
                    .gameInstrLabel, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.italyCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet
                .START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet
                .END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;


        points = getIntent().getIntExtra("points", 0);
        italyPoints = getIntent().getIntExtra("italyPoints", 0);
        italyHasChecked12Trackers = getIntent().getBooleanExtra("italyHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        italyPoints = prefs.getInt("storedItalyPoints_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, 0);
        italyHasChecked12Trackers = prefs.getBoolean("storedItalyHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
        syllableGame = getIntent().getStringExtra("syllableGame");
        visibleTiles = TILE_BUTTONS.length;

        setContentView(R.layout.italy);
        int gameID = R.id.italyCL;
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        if (scriptDirection.compareTo("RTL") == 0) { //LM: flips images for RTL layouts. LTR is default
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

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        if (syllableGame.equals("S")) {
            sortableSyllArray = (Start.SyllableList) syllableList.clone();
            Collections.shuffle(sortableSyllArray);
        } else {
            sortableTilesArray = (Start.TileList) tileListNoSAD.clone();
            Collections.shuffle(sortableTilesArray);
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(italyPoints));

        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

        updateTrackers();

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    @Override
    public void setAllTilesUnclickable() {
        super.setAllTilesUnclickable();

        ImageView nextWordArrow = findViewById(R.id.playNextWord);
        nextWordArrow.setImageResource(R.drawable.zz_forward_inactive);
        nextWordArrow.setClickable(false);

        ImageView referenceItem = findViewById(R.id.referenceItem);
        referenceItem.setClickable(false);

        for (int t = 0; t < visibleTiles; t++) {
            ImageView wordImage = findViewById(TILE_IMAGES[t]);
            wordImage.setClickable(false);
        }
    }

    @Override
    public void setAllTilesClickable() {
        super.setAllTilesClickable();

        ImageView nextWordArrow = findViewById(R.id.playNextWord);
        nextWordArrow.setImageResource(R.drawable.zz_forward_green);
        nextWordArrow.setClickable(true);

        ImageView referenceItem = findViewById(R.id.referenceItem);
        referenceItem.setClickable(true);

        for (int t = 0; t < visibleTiles; t++) {
            ImageView wordImage = findViewById(TILE_IMAGES[t]);
            wordImage.setClickable(true);
        }
    }


    public void goBackToEarth(View view) {

        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view) {

        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void onRefClick(View view) {

        playActiveWordClip(false);

    }


    public void playAgain() {
        repeatLocked = true;
        deckIndex = -1;
        gameCards.removeAll(gameCards);
        for (int card = 0; card < 16; card++) {
            boardCardsFound[card] = false;
        }
        WordList wordListShuffle = wordList;
        Collections.shuffle(wordListShuffle);

        //Add 54 of the shuffled cards to gameCards
        for (int cardNumber = 0; cardNumber < 54; cardNumber++) {
            gameCards.add(wordListShuffle.get(cardNumber));
        }

        //Add 16 of the gameCards to the board
        WordList boardCards = new WordList();
        for (int tileNumber = 0; tileNumber < 16; tileNumber++) {
            boardCards.add(gameCards.get(tileNumber));
            TextView thisCardText = (TextView) findViewById(TILE_BUTTONS[tileNumber]);
            thisCardText.setText(wordList.stripInstructionCharacters(gameCards.get(tileNumber).localWord));
            ImageView thisCardImage = (ImageView) findViewById(TILE_IMAGES[tileNumber]);
            int resID = getResources().getIdentifier(gameCards.get(tileNumber).nationalWord, "drawable", getPackageName());
            thisCardImage.setImageResource(0);
            thisCardImage.setImageResource(resID);
        }

        //Shuffle the gameCards again; they will be "called" in order from here on out
        Collections.shuffle(gameCards);

        //Display the first word
        nextWordFromGameSet();

    }

    public void getAnotherWord(View view) {

        nextWordFromGameSet();
    }

    public void nextWordFromGameSet() {
        deckIndex++;
        if (deckIndex == 54) {

            //The player went through all the cards without getting a loteria. Set up a new board
            playIncorrectSound();
            playIncorrectSound();
            playAgain();
        } else { //"Call out" the next word

            wordInLOP = wordList.stripInstructionCharacters(gameCards.get(deckIndex).localWord);
            wordInLWC = gameCards.get(deckIndex).nationalWord;
            playActiveWordClip(false);
        }
    }

    public void onSelection(View view) {
        respondToSelection(Integer.parseInt((String) view.getTag()));
    }

    public void respondToSelection(int indexOfTileJustSelected) {

        TextView tileJustSelected = findViewById(TILE_BUTTONS[indexOfTileJustSelected - 1]);

        if (((String) tileJustSelected.getText()).compareTo(wordInLOP) == 0) {
            respondToCorrectSelection(indexOfTileJustSelected);
        } else {
            respondToIncorrectSelection();
        }

    }

    public void respondToCorrectSelection(int indexOfTileJustSelected) {
        boardCardsFound[indexOfTileJustSelected - 1] = true;

        ImageView imageJustSelected = findViewById(TILE_IMAGES[indexOfTileJustSelected - 1]);
        imageJustSelected.setImageResource(R.drawable.zz_bean);

        if (loteria()) {
            respondToLoteria();
        } else {
            //Play sounds, then advance to the next word
            playCorrectSoundThenActiveWordClip(false);
            nextWordFromGameSet();
        }

    }

    public void respondToIncorrectSelection() {

        playIncorrectSound();
    }


    public boolean loteria() {
        //for each sequence in possibleLoteriaSequences[][], check if all the indeces inside have been marked as correctly selected

        for (int[] sequence : LOTERIA_SEQUENCES) {
            if (boardCardsFound[sequence[0] - 1] && boardCardsFound[sequence[1] - 1] && boardCardsFound[sequence[2] - 1] && boardCardsFound[sequence[3] - 1]) {
                return true;
            }
        }

        return false;
    }

    public void respondToLoteria() {
        //play finalCorrectSound with final sound, which also updates the trackers
        playCorrectSoundThenActiveWordClip(true);

        //draw a thin/transparent line across the loteria?


        //update points
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        points += 4;
        italyPoints += 4;
        pointsEarned.setText(String.valueOf(italyPoints));
        SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putInt("storedPoints_player" + playerString, points);
        editor.putInt("storedItalyPoints_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, italyPoints);
        editor.putBoolean("storedItalyHasChecked12Trackers_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, italyHasChecked12Trackers);
        editor.apply();
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString
                + syllableGame;
        editor.putInt(uniqueGameLevelPlayerID, trackerCount);
        editor.apply();

        //update trackers
        if (trackerCount >= 12) {
            italyHasChecked12Trackers = true;
        }
        updateTrackers();

    }


}
