package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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

import static org.alphatilesapps.alphatiles.Start.MULTI_TYPE_TILES;
import static org.alphatilesapps.alphatiles.Start.gameList;
import static org.alphatilesapps.alphatiles.Start.localAppName;
import static org.alphatilesapps.alphatiles.Start.syllableHashMap;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileHashMap;
import static org.alphatilesapps.alphatiles.Start.COLORS;
import static org.alphatilesapps.alphatiles.Start.CorV;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.Tile;


//Identify the first TILE
//level 1: 6 visible tiles, random wrong choices
//level 2: 12 visible tiles, random wrong choices
//level 3: 18 visible tiles, random wrong choices
//level 4: 6 visible tiles, distractor wrong choices
//level 5: 12 visible tiles, distractor wrong choices
//level 6: 18 visible tiles, distractor wrong choices

//Identify the first SOUND (esp. for Thai Script, when this may differ from starting tile).
//level 7: 6 visible tiles, random wrong choices
//level 8: 12 visible tiles, random wrong choices
//level 9: 18 visible tiles, random wrong choices
//level 10: 6 visible tiles, distractor wrong choices
//level 11: 12 visible tiles, distractor wrong choices
//level 12: 18 visible tiles, distractor wrong choices

//Identify the first SYLLABLE (Mark 'S' for game's 'Mode')
//level 1 (with S): 6 visible syllables, random wrong choices
//level 2 (with S): 12 visible syllables, random wrong choices
//level 3 (with S): 18 visible syllables, random wrong choices
//level 4 (with S): 6 visible syllables, distractor wrong choices
//level 5 (with S): 12 visible syllables, distractor wrong choices
//level 6 (with S): 18 visible syllables, distractor wrong choices


// IF A LANGUAGE HAS WORDS THAT START WITH SOMETHING OTHER THAN C OR V, THIS WILL CRASH

public class Georgia extends GameActivity {

    Start.SyllableList sortableSyllArray; //JP
    Set<String> answerChoices = new HashSet<String>();
    String initialTile = "";
    String initialSyll = "";
    int visibleTiles; // will be 6, 12 or 18 based on challengeLevel

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18
    };

    protected int[] getTileButtons() {
        return TILE_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);
        int gameID = 0;
        if (syllableGame.equals("S")) {
            gameID = R.id.georgiaCL_syll;
        } else {
            gameID = R.id.georgiaCL;
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
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
//          audioInstructionsResID = res.getIdentifier("georgia_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());

        } catch (Exception e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        int gameID = 0;
        if (syllableGame.equals("S")) {
            setContentView(R.layout.georgia_syll);
            gameID = R.id.georgiaCL_syll;
        } else {
            setContentView(R.layout.georgia);
            gameID = R.id.georgiaCL;
        }

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        switch (challengeLevel) {
            case 11:
            case 8:
            case 5:
            case 2:
                visibleTiles = 12;
                break;
            case 12:
            case 9:
            case 6:
            case 3:
                visibleTiles = 18;
                break;
            default:
                visibleTiles = 6;
        }

        if (syllableGame.equals("S")) {
            sortableSyllArray = (Start.SyllableList) syllableList.clone();
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

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

        repeatLocked = true;
        if (syllableGame.equals("S")) {
            Collections.shuffle(sortableSyllArray); //JP
        }

        setWord();
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        if (syllableGame.equals("S")) {
            setUpSyllables();
        } else {
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

    private void setWord() {
        chooseWord();
        if (syllableGame.equals("T")) {
            if (!CorV.contains(initialTile)) { // Make sure chosen word begins with C or V
                chooseWord();
            }
            parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP, wordInLOP); // KP
            initialTile = parsedWordArrayFinal.get(0);
            if(challengeLevel>6 && challengeLevel<13) { // Find first non-LV tile (first sound, since LVs are pronounced after the consonants they precede)
                Tile initialTileObject;
                String initialTileType = "LV";
                int t = -1;
                while (initialTileType.equals("LV") && t < parsedWordArrayFinal.size()) {
                    t++;
                    initialTile = parsedWordArrayFinal.get(t);
                    initialTileObject = tileHashMap.get(initialTile);
                    ArrayList<String> parsedWordArrayPreliminary = tileList.parseWordIntoTilesPreliminary(wordInLOP);
                    if (MULTI_TYPE_TILES.contains(initialTile)) {
                        int indexInPreliminaryArray = tileList.returnInstanceIndexInPreliminaryParsedWordArray(initialTile, 0, parsedWordArrayFinal, wordInLOP);
                        initialTileType = tileList.getInstanceTypeForMixedTilePreliminary(indexInPreliminaryArray, parsedWordArrayPreliminary, wordInLOP);
                    } else {
                        initialTileType = initialTileObject.tileType;
                    }
                }
            }
        } else {
            parsedWordSyllArrayFinal = syllableList.parseWordIntoSyllables(wordInLOP); // JP
            initialSyll = parsedWordSyllArrayFinal.get(0);
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);
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
        while (answerChoices.size() < visibleTiles && i < sortableSyllArray.size()) {
            // and does so while skipping repeats because it is a set
            // and a set has no order so it will be randomized anyways
            String option = sortableSyllArray.get(i).syllable;
            if (option.length() >= 2 && initialSyll.length() >= 2) {
                if (option.charAt(0) == initialSyll.charAt(0)
                        && option.charAt(1) == initialSyll.charAt(1)) {
                    answerChoices.add(option);
                } else if (option.charAt(0) == initialSyll.charAt(0)) {
                    answerChoices.add(option);
                }
            } else {
                if (option.charAt(0) == initialSyll.charAt(0)) {
                    answerChoices.add(option);
                } else if (option.charAt(option.length() - 1) == initialSyll.charAt(initialSyll.length() - 1)) {
                    answerChoices.add(option);
                }
            }

            i++;
        }

        int j = 0;
        while (answerChoices.size() < visibleTiles) {
            //this will probably never happen
            answerChoices.add(sortableSyllArray.get(j).syllable);
            j++;
        }

        List<String> answerChoicesList = new ArrayList<>(answerChoices); //so we can index into answer choices now

        for (int t = 0; t < TILE_BUTTONS.length; t++) {
            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (sortableSyllArray.get(t).syllable.equals(initialSyll) && t < visibleTiles) {
                correctSyllRepresented = true;
            }

            String tileColorStr = COLORS.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3) {
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
            } else {
                if (t < visibleTiles) {
                    // think through this logic more -- how to get distractor syllables in there but
                    // also fill other syllables beyond the 3 distractors

                    // first make a visibleTiles-sized array with the correct answer,
                    // its distractor syllables, and any other syllables that start with the same tile;
                    // filter out repeats

                    // then iterate through TILE_BUTTONS and fill them in using the other array, shuffled
                    if (answerChoicesList.get(t) == initialSyll) {
                        correctSyllRepresented = true;
                    }
                    gameTile.setText(answerChoicesList.get(t)); // KP
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


        }

        if (!correctSyllRepresented) { // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles

            Random rand = new Random();
            int randomNum = rand.nextInt(visibleTiles - 1); // KP
            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(initialSyll);

        }

    }

    private void setUpTiles() {

        boolean correctTileRepresented = false;

        Tile answer = tileHashMap.find(initialTile);

        // LINES 369 - 404 ARE SET UP FOR LEVEL 2
        answerChoices.clear();
        answerChoices.add(initialTile);
        answerChoices.add(answer.altTiles[0]);
        answerChoices.add(answer.altTiles[1]);
        answerChoices.add(answer.altTiles[2]);

        int i = 0;
        while (answerChoices.size() < visibleTiles && i < CorV.size()) {
            // and does so while skipping repeats because it is a set
            // and a set has no order so it will be randomized anyways
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            String option = CorV.get(index);
            if (option.length() >= 2 && initialTile.length() >= 2) {
                if (option.charAt(0) == initialTile.charAt(0)
                        && option.charAt(1) == initialTile.charAt(1)) {
                    answerChoices.add(option);
                } else if (option.charAt(0) == initialTile.charAt(0)) {
                    answerChoices.add(option);
                }
            } else {
                if (option.charAt(0) == initialTile.charAt(0)) {
                    answerChoices.add(option);
                } else if (option.charAt(option.length() - 1) == initialTile.charAt(initialTile.length() - 1)) {
                    answerChoices.add(option);
                }
            }

            i++;
        }

        int j = 0;
        while (answerChoices.size() < visibleTiles) {
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            answerChoices.add(CorV.get(index));
            j++;
        }

        List<String> answerChoicesList = new ArrayList<>(answerChoices); //so we can index into answer choices now

        for (int t = 0; t < TILE_BUTTONS.length; t++) {

            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (CorV.get(t).equals(initialTile) && t < visibleTiles) {
                correctTileRepresented = true;
            }

            String tileColorStr = COLORS.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3) { //random wrong
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
            } else { //distractors
                if (t < visibleTiles) {
                    // think through this logic more -- how to get distractor syllables in there but
                    // also fill other syllables beyond the 3 distractors

                    // first make a visibleTiles-sized array with the correct answer,
                    // its distractor syllables, and any other syllables that start with the same tile;
                    // filter out repeats

                    // then iterate through TILE_BUTTONS and fill them in using the other array, shuffled
                    if (answerChoicesList.get(t) == initialTile) {
                        correctTileRepresented = true;
                    }
                    gameTile.setText(answerChoicesList.get(t)); // KP
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
        if (syllableGame.equals("S")) {
            correct = initialSyll;
        } else {
            correct = initialTile;
        }

        int tileNo = justClickedTile - 1; // justClickedTile uses 1 to 18, t uses the array ID (between [0] and [17]
        TextView tile = findViewById(TILE_BUTTONS[tileNo]);
        String selectedTile = tile.getText().toString();

        if (correct.equals(selectedTile)) {
            // Good job! You chose the right tile
            repeatLocked = false;
            updatePointsAndTrackers(1);

            for (int t = 0; t < TILE_BUTTONS.length; t++) {
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


    public void onBtnClick(View view) {
        respondToTileSelection(Integer.parseInt((String) view.getTag())); // KP
    }


    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

}
