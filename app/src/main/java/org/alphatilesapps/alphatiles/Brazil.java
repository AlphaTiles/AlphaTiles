package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.alphatilesapps.alphatiles.Start.*;

// RR
//Game idea: Find the vowel missing from the word
//Challenge Level 1: VOWELS: Pick from correct tile and three random VOWEL tiles
//Challenge Level 2: VOWELS: Pick from correct tile and its distractor trio
//Challenge Level 3: VOWELS: Pick from all vowel tiles (up to a max of 15)

// AH
//Challenge Level 4: CONSONANTS: Pick from correct tile and three random CONSONANT tiles
//Challenge Level 5: CONSONANTS: Pick from correct tile and its distractor trio
//Challenge Level 6: CONSONANTS: Pick from all consonant tiles (up to a max of 15)

//JP
//Challenge Level 7: TONES: Pick from <= 4 tone markers; if the lang has >= 2 and < 4 tone markers,
// make other views invisible

// JP
// Syllable Level 1: Pick from correct syllable and three random syllables (4 choices)
// Syllable Level 2: Pick from correct syllable and its distractor trio (4 choices)
// No reason to accommodate 15 syllables, right?

public class Brazil extends GameActivity {

    Start.TileList sortableTilesArray; // KP
    Start.SyllableList sortableSyllArray; //JP
    Set<String> answerChoices = new HashSet<String>();
    int visibleTiles;
    int numTones;
    String correctTile = "";

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15
    };

    protected int[] getTileButtons() {
        return TILE_BUTTONS;
    }

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
        if (challengeLevel == 3 || challengeLevel == 6) {
            gameID = R.id.brazil_cl3_CL;
        } else {
            gameID = R.id.brazil_cl1_CL;
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
        context = this;
        if (challengeLevel == 3 || challengeLevel == 6) {
            setContentView(R.layout.brazil_cl3);
        } else {
            setContentView(R.layout.brazil_cl1);
        }

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            int gameID = 0;
            if (challengeLevel == 3 || challengeLevel == 6) {
                gameID = R.id.brazil_cl3_CL;
            } else {
                gameID = R.id.brazil_cl1_CL;
            }
            fixConstraintsRTL(gameID);
        }

        if (challengeLevel < 4 && !syllableGame.equals("S")) {

            if (VOWELS.isEmpty()) {  // Makes sure VOWELS is populated only once when the app is running
                for (int d = 0; d < Start.tileList.size(); d++) {
                    if (Start.tileList.get(d).tileType.equals("V")) {
                        VOWELS.add(Start.tileList.get(d).baseTile);
                    }
                }
            }

            Collections.shuffle(VOWELS); // AH

        } else if (syllableGame.equals("S")) {
            if (SYLLABLES.isEmpty()) {
                for (int d = 0; d < syllableList.size(); d++) {
                    SYLLABLES.add(syllableList.get(d).toString());
                }
            }
            Collections.shuffle(SYLLABLES);
        } else {

            if (CONSONANTS.isEmpty()) {  // Makes sure CONSONANTS is populated only once when the app is running
                for (int d = 0; d < Start.tileList.size(); d++) {
                    if (Start.tileList.get(d).tileType.equals("C")) {
                        CONSONANTS.add(Start.tileList.get(d).baseTile);
                    }
                }
            }

            Collections.shuffle(CONSONANTS);

        }

        if (MULTIFUNCTIONS.isEmpty()) {  // Makes sure MULTIFUNCTIONS is populated only once when the app is running
            for (int d = 0; d < Start.tileList.size(); d++) {
                if (!Start.tileList.get(d).tileTypeB.equals("none")) {
                    MULTIFUNCTIONS.add(Start.tileList.get(d).baseTile);
                }
            }
        }

        Collections.shuffle(MULTIFUNCTIONS);

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");
        if (syllableGame.equals("S")) {
            visibleTiles = 4;
        } else {
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
                case 7:
                    numTones = TONES.size();
                    if (numTones > 4) {
                        numTones = 4;
                    }
                    visibleTiles = 4;
                    break;
                default:
                    visibleTiles = 4;
            }
        }

        if (syllableGame.equals("S")) {
            sortableSyllArray = (Start.SyllableList) syllableList.clone(); // JP
        } else {
            sortableTilesArray = (Start.TileList) Start.tileList.clone(); // KP
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        updatePointsAndTrackers(0);
        incorrectAnswersSelected = new ArrayList<>(visibleTiles-1);
        for (int i = 0; i < visibleTiles-1; i++) {
            incorrectAnswersSelected.add("");
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
        setAdvanceArrowToGray();
        if (syllableGame.equals("S")) {
            Collections.shuffle(sortableSyllArray);
        } else {
            Collections.shuffle(sortableTilesArray); // KP
        }

        setWord();
        removeTile();
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

        for (int i = 0; i < visibleTiles; i++) {
            TextView nextWord = (TextView) findViewById(TILE_BUTTONS[i]);
            nextWord.setClickable(true);
        }
        incorrectOnLevel = 0;
        for (int i = 0; i < visibleTiles-1; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        levelBegunTime = System.currentTimeMillis();
    }

    private void setWord() {

        chooseWord();
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        if (syllableGame.equals("S")) {
            parsedWordArrayFinal = syllableList.parseWordIntoSyllables(wordInLOP);
        } else {
            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP);
        }


        boolean proceed = false;
        String nextTile;

        // JP: this section is not relevant to syllable games, right?
        if (!syllableGame.equals("S")) {
            switch (challengeLevel) {
                case 4:
                case 5:
                case 6:
                    for (int i = 0; i < parsedWordArrayFinal.size(); i++) {

                        nextTile = parsedWordArrayFinal.get(i);
                        // Include if a simple consonant
                        if (CONSONANTS.contains(nextTile) && !MULTIFUNCTIONS.contains(nextTile)) {
                            proceed = true;
                        }
                        // Include if a multi-function symbol that is a consonant in this instance
                        if (MULTIFUNCTIONS.contains(nextTile)) {
                            String instanceType = Start.tileList.getInstanceTypeForMixedTile(i, wordInLWC);
                            if (instanceType.equals("C")) {
                                proceed = true;
                            }
                        }

                    }
                    break;
                case 7:
                    for (int i = 0; i < parsedWordArrayFinal.size(); i++) {

                        nextTile = parsedWordArrayFinal.get(i);
                        // Include if a simple tone marker
                        if (TONES.contains(nextTile) && !MULTIFUNCTIONS.contains(nextTile)) {
                            proceed = true;
                        }
                        // Include if a multi-function symbol that is a tone marker in this instance
                        if (MULTIFUNCTIONS.contains(nextTile)) {
                            String instanceType = Start.tileList.getInstanceTypeForMixedTile(i, wordInLWC);
                            if (instanceType.equals("T")) {
                                proceed = true;
                            }
                        }

                    }
                    break;
                default:
                    for (int i = 0; i < parsedWordArrayFinal.size(); i++) {

                        nextTile = parsedWordArrayFinal.get(i);
                        // Include if a simple vowel
                        if (VOWELS.contains(nextTile) && !MULTIFUNCTIONS.contains(nextTile)) {
                            proceed = true;
                        }
                        // Include if a multi-function symbol that is a vowel in this instance
                        if (MULTIFUNCTIONS.contains(nextTile)) {
                            String instanceType = Start.tileList.getInstanceTypeForMixedTile(i, wordInLWC);
                            if (instanceType.equals("V")) {
                                proceed = true;
                            }
                        }

                    }
            }

            if (!proceed) { // Some languages (e.g. skr) have words without vowels (as defined by game tiles), so we filter out those words
                setWord();
            }
        }
    }

    private void removeTile() {

        Random rand = new Random();
        int index = 0;
        int index_to_remove = 0;
        correctTile = "";

        boolean repeat = true;
        String instanceType = null;

        if (!syllableGame.equals("S")) {
            ArrayList<Integer> possibleIndices = new ArrayList<>();
            for (int i = 0; i < parsedWordArrayFinal.size(); i++) {
                possibleIndices.add(i);
            }
            while (repeat) { // JP: changed from 200 chances to keeping track

                // JP: index is no longer corresponding to the index we remove from the word
                index = rand.nextInt(possibleIndices.size());
                correctTile = parsedWordArrayFinal.get(possibleIndices.get(index));
                index_to_remove = possibleIndices.get(index);
                possibleIndices.remove(possibleIndices.get(index));
                while (SAD.contains(correctTile)) { // JP: Makes sure that SAD is never chosen as missing tile
                    index = rand.nextInt(possibleIndices.size());
                    correctTile = parsedWordArrayFinal.get(possibleIndices.get(index));
                    index_to_remove = possibleIndices.get(index);
                    possibleIndices.remove(possibleIndices.get(index));
                }
                if (MULTIFUNCTIONS.contains(correctTile)) {
                    instanceType = Start.tileList.getInstanceTypeForMixedTile(index, wordInLWC);
                } else {
                    instanceType = Start.tileList.get(Start.tileList.returnPositionInAlphabet(correctTile)).tileType;
                }

                if (challengeLevel < 4) {
                    if (instanceType.equals("V")) {
                        repeat = false;
                    }
                }

                if (challengeLevel > 3 && challengeLevel < 7) {
                    if (instanceType.equals("C")) {
                        repeat = false;
                    }
                }

                if (challengeLevel == 7) {
                    if (instanceType.equals("T")) {
                        repeat = false;
                    }
                }

            }
        } else { // syllable game
            index_to_remove = rand.nextInt(parsedWordArrayFinal.size());
            correctTile = parsedWordArrayFinal.get(index_to_remove);
            while (SAD.contains(correctTile)) { // JP: makes sure that SAD is never chosen as missing syllable
                index_to_remove = rand.nextInt(parsedWordArrayFinal.size());
                correctTile = parsedWordArrayFinal.get(index_to_remove);
            }
        }

        parsedWordArrayFinal.set(index_to_remove, "__");
        TextView constructedWord = findViewById(R.id.activeWordTextView);
        StringBuilder word = new StringBuilder();
        for (String s : parsedWordArrayFinal) {
            if (s != null) {
                word.append(s);
            }
        }
        constructedWord.setText(word.toString());

    }

    private void setUpSyllables() {
        boolean correctSyllRepresented = false;
        Start.Syllable answer = syllableHashMap.find(correctTile); // Find corresponding syllable object for correct answer

        answerChoices.clear();
        answerChoices.add(correctTile);
        answerChoices.add(answer.distractors[0]);
        answerChoices.add(answer.distractors[1]);
        answerChoices.add(answer.distractors[2]);

        Random rand = new Random();

        while (answerChoices.size() < 4) { // This shouldn't happen if distractors are set up correctly
            answerChoices.add(syllableList.get(rand.nextInt(syllableList.size())).syllable);
        }

        List<String> answerChoicesList = new ArrayList<>(answerChoices); // So we can index into answer choices now

        for (int t = 0; t < visibleTiles; t++) {
            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (sortableSyllArray.get(t).syllable.equals(correctTile) && t < visibleTiles) {
                correctSyllRepresented = true;
            }

            String tileColorStr = COLORS.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1) {
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
                    if (answerChoicesList.get(t) == correctTile) {
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

            rand = new Random();
            int randomNum = rand.nextInt(visibleTiles - 1); // KP
            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(correctTile);

        }

    }

    private void setUpTiles() {
        boolean correctTileRepresented = false;
        Collections.shuffle(VOWELS);
        Collections.shuffle(CONSONANTS);
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

                String tileColorStr = COLORS.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }

            for (int i = visibleTiles; i < TILE_BUTTONS.length; i++) {
                TextView gameTile = findViewById(TILE_BUTTONS[i]);
                gameTile.setVisibility(View.INVISIBLE);
            }
        } else if (challengeLevel == 1) {

            for (int t = 0; t < visibleTiles; t++) {

                TextView gameTile = findViewById(TILE_BUTTONS[t]);

                if (VOWELS.get(t).equals(correctTile)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = COLORS.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(VOWELS.get(t));
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }

        } else if (challengeLevel == 4) {

            for (int t = 0; t < visibleTiles; t++) {

                TextView gameTile = findViewById(TILE_BUTTONS[t]);

                if (CONSONANTS.get(t).equals(correctTile)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = COLORS.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(CONSONANTS.get(t));
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }

        } else if (challengeLevel == 7) {
            for (int t = 0; t < numTones; t++) {

                TextView gameTile = findViewById(TILE_BUTTONS[t]);

                if (TONES.get(t).equals(correctTile)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = COLORS.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(TONES.get(t));
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }
            for (int t = numTones; t < visibleTiles; t++) {
                TextView gameTile = findViewById(TILE_BUTTONS[t]);
                gameTile.setVisibility(View.INVISIBLE);
                gameTile.setClickable(false);
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

                String tileColorStr = COLORS.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);

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

            // report time and number of incorrect guesses
            String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
            Properties info = new Properties().putValue("time", System.currentTimeMillis() - levelBegunTime)
                    .putValue("prior incorrect", incorrectOnLevel)
                    .putValue("correct answer", correctTile)
                    .putValue("grade", studentGrade);
            for (int i = 0; i < visibleTiles-1; i++) {
                if (!incorrectAnswersSelected.get(i).equals("")) {
                    info.putValue("incorrect"+(i+1), incorrectAnswersSelected.get(i));
                }
            }
            Analytics.with(context).track(gameUniqueID, info);

            repeatLocked = false;
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(1);

            for (int i = 0; i < parsedWordArrayFinal.size(); i++) {
                if ("__".equals(parsedWordArrayFinal.get(i))) {
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
            incorrectOnLevel += 1;
            for (int i = 0; i < visibleTiles-1; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(gameTileString)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, gameTileString);
                    break;
                }
            }
            playIncorrectSound();
        }

    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);

    }

    public void onBtnClick(View view) {
        respondToTileSelection(Integer.parseInt((String) view.getTag())); // KP
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > -1) {
            super.playAudioInstructions(view);
        }
    }

}
