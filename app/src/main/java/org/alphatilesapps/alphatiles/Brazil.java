package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

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
    Set<String> answerChoices = new HashSet<String>();
    int numTones;
    Start.Tile correctTile;
    Start.Syllable correctSyllable;
    String correctString;

    protected static final int[] GAME_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15
    };

    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void hideInstructionAudioImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

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

        int gameID = 0;
        if (challengeLevel == 3 || challengeLevel == 6) {
            gameID = R.id.brazil_cl3_CL;
        } else {
            gameID = R.id.brazil_cl1_CL;
        }

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            fixConstraintsRTL(gameID);
        }

        if (challengeLevel < 4 && !syllableGame.equals("S")) {

            if (VOWELS.isEmpty()) {  // Makes sure VOWELS is populated only once when the app is running
                for (int d = 0; d < tileList.size(); d++) {
                    if (tileList.get(d).typeOfThisTileInstance.matches("(LV|AV|BV|FV|V)")) {
                        VOWELS.add(tileList.get(d));
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
                for (int d = 0; d < tileList.size(); d++) {
                    if (tileList.get(d).typeOfThisTileInstance.equals("C")) {
                        CONSONANTS.add(tileList.get(d));
                    }
                }
            }

            Collections.shuffle(CONSONANTS);

        }

        if (MULTITYPE_TILES.isEmpty()) {  // Makes sure MULTITYPE_TILES is populated only once when the app is running
            for (int d = 0; d < tileList.size(); d++) {
                if (!tileList.get(d).tileTypeB.equals("none")) {
                    MULTITYPE_TILES.add(tileList.get(d).text);
                }
            }
        }

        Collections.shuffle(MULTITYPE_TILES);

        if (syllableGame.equals("S")) {
            visibleGameButtons = 4;
        } else {
            switch (challengeLevel) {
                case 3:
                    visibleGameButtons = VOWELS.size();
                    if (visibleGameButtons > 15) {    // AH
                        visibleGameButtons = 15;      // AH
                    }                           // AH
                    break;
                case 6:
                    visibleGameButtons = CONSONANTS.size();
                    if (visibleGameButtons > 15) {    // AH
                        visibleGameButtons = 15;      // AH
                    }                           // AH
                    break;
                case 7:
                    numTones = TONES.size();
                    if (numTones > 4) {
                        numTones = 4;
                    }
                    visibleGameButtons = 4;
                    break;
                default:
                    visibleGameButtons = 4;
            }
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        updatePointsAndTrackers(0);
        incorrectAnswersSelected = new ArrayList<>(visibleGameButtons-1);
        for (int i = 0; i < visibleGameButtons-1; i++) {
            incorrectAnswersSelected.add("");
        }
        playAgain();
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

        setWord();
        removeTile();
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        if (syllableGame.equals("S")) {
            setUpSyllables();
        } else {
            setUpTiles();
        }
        playActiveWordClip(false);
        setAllGameButtonsClickable();
        setOptionsRowClickable();

        for (int i = 0; i < visibleGameButtons; i++) {
            TextView nextWord = (TextView) findViewById(GAME_BUTTONS[i]);
            nextWord.setClickable(true);
        }
        incorrectOnLevel = 0;
        for (int i = 0; i < visibleGameButtons-1; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        levelBegunTime = System.currentTimeMillis();
    }

    private void setWord() {

        chooseWord();
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        if (syllableGame.equals("S")) {
            parsedRefWordSyllableArray = syllableList.parseWordIntoSyllables(refWord);
        } else {
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
        }

        boolean proceed = false;
        Tile nextTile;

        // JP: this section is not relevant to syllable games, right?
        if (!syllableGame.equals("S")) {
            switch (challengeLevel) {
                case 4:
                case 5:
                case 6:
                    for (int i = 0; i < parsedRefWordTileArray.size(); i++) {
                        nextTile = parsedRefWordTileArray.get(i);
                        // Include if a simple consonant
                       if (nextTile.typeOfThisTileInstance.equals("C")){
                           proceed = true;
                       }

                    }
                    break;
                case 7:
                    for (int i = 0; i < parsedRefWordTileArray.size(); i++) {

                        nextTile = parsedRefWordTileArray.get(i);
                        if (nextTile.typeOfThisTileInstance.equals("T")){
                            proceed = true;
                        }

                    }
                    break;
                default:
                    for (int i = 0; i < parsedRefWordTileArray.size(); i++) {

                        nextTile = parsedRefWordTileArray.get(i);
                        if (nextTile.typeOfThisTileInstance.matches("(LV|AV|BV|FV|V)")) {
                            proceed = true;
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

        boolean repeat = true;

        if (!syllableGame.equals("S")) {
            ArrayList<Integer> possibleIndices = new ArrayList<>();
            for (int i = 0; i < parsedRefWordTileArray.size(); i++) {
                possibleIndices.add(i);
            }
            while (repeat && possibleIndices.size()>0) {
                index = rand.nextInt(possibleIndices.size());
                correctTile = parsedRefWordTileArray.get(possibleIndices.get(index));
                index_to_remove = possibleIndices.get(index);
                possibleIndices.remove(possibleIndices.get(index));
                while (SAD_STRINGS.contains(correctTile.text)) { // JP: Makes sure that SAD is never chosen as missing tile
                    index = rand.nextInt(possibleIndices.size());
                    correctTile = parsedRefWordTileArray.get(possibleIndices.get(index));
                    index_to_remove = possibleIndices.get(index);
                    possibleIndices.remove(possibleIndices.get(index));
                }

                if (challengeLevel < 4) {
                    if (correctTile.typeOfThisTileInstance.matches("(LV|AV|BV|FV|V)")) {
                        repeat = false;
                    }
                }

                if (challengeLevel > 3 && challengeLevel < 7) {
                    if (correctTile.typeOfThisTileInstance.equals("C")) {
                        repeat = false;
                    }
                }

                if (challengeLevel == 7) {
                    if (correctTile.typeOfThisTileInstance.equals("T")) {
                        repeat = false;
                    }
                }

            }
            correctString = correctTile.text;
        } else { // syllable game
            index_to_remove = rand.nextInt(parsedRefWordSyllableArray.size());
            correctSyllable = parsedRefWordSyllableArray.get(index_to_remove);
            while (SAD_STRINGS.contains(correctSyllable.text)) { // JP: makes sure that SAD is never chosen as missing syllable
                index_to_remove = rand.nextInt(parsedRefWordSyllableArray.size());
                correctSyllable = parsedRefWordSyllableArray.get(index_to_remove);
            }
            correctString = correctSyllable.text;
        }

        TextView constructedWord = findViewById(R.id.activeWordTextView);
        StringBuilder wordBuilder = new StringBuilder();
        String word;
        if (syllableGame.equals("S")) {
            Start.Syllable blankSyllable = new Start.Syllable("__", new ArrayList<>(),"X", 0, correctSyllable.color);
            parsedRefWordSyllableArray.set(index_to_remove, blankSyllable);
            for (Syllable s : parsedRefWordSyllableArray) {
                if (s != null) {
                    wordBuilder.append(s.text);
                }
            }
            word = wordBuilder.toString();
        } else { // Tile game
            Start.Tile blankTile = new Start.Tile("__", new ArrayList<>(), "", "", "", "", "", "", "", "", 0, 0, 0, 0, 0, correctTile.typeOfThisTileInstance, 1, "");
            parsedRefWordTileArray.set(index_to_remove, blankTile);
            if (scriptType.equals("Khmer") && correctTile.typeOfThisTileInstance.equals("C")){
                if(index_to_remove < parsedRefWordTileArray.size()-1 && parsedRefWordTileArray.get(index_to_remove + 1).typeOfThisTileInstance.matches("(V|AV|BV|D)")) {
                    blankTile.text = "\u200B"; // The word will default to containing a placeholder circle. Add zero-width space, instead of line.
                    parsedRefWordTileArray.set(index_to_remove, blankTile);
                } else {
                    blankTile.text = placeholderCharacter; // Since Khmer has lots of placeholder circles, we'll use them for all consonant blanks.
                    parsedRefWordTileArray.set(index_to_remove, blankTile);
                }
            }
            if (scriptType.matches("(Thai|Lao)") && correctTile.typeOfThisTileInstance.equals("C")){
                blankTile.text = placeholderCharacter;
                parsedRefWordTileArray.set(index_to_remove, blankTile);
            }
            word = combineTilesToMakeWord(parsedRefWordTileArray, refWord, index_to_remove);
        }
        constructedWord.setText(word);
    }

    private void setUpSyllables() {
        Collections.shuffle(SYLLABLES);
        boolean containsCorrectSyllable = false;
        Start.Syllable answer = syllableHashMap.find(correctSyllable.text); // Find corresponding syllable object for correct answer

        answerChoices.clear();
        answerChoices.add(correctSyllable.text);
        answerChoices.add(answer.distractors.get(0));
        answerChoices.add(answer.distractors.get(1));
        answerChoices.add(answer.distractors.get(2));

        Random rand = new Random();

        while (answerChoices.size() < 4) { // This shouldn't happen if distractors are set up correctly
            answerChoices.add(syllableList.get(rand.nextInt(syllableList.size())).text);
        }

        List<String> answerChoicesList = new ArrayList<>(answerChoices); // So we can index into answer choices now

        for (int t = 0; t < visibleGameButtons; t++) {
            TextView gameTile = findViewById(GAME_BUTTONS[t]);

            if (SYLLABLES.get(t).equals(correctSyllable.text) && t < visibleGameButtons) {
                containsCorrectSyllable = true;
            }

            String tileColorStr = colorList.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1) {
                if (t < visibleGameButtons) {
                    gameTile.setText(SYLLABLES.get(t));
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
                if (t < visibleGameButtons) {
                    // think through this logic more -- how to get distractor syllables in there but
                    // also fill other syllables beyond the 3 distractors

                    // first make a visibleGameButtons-sized array with the correct answer,
                    // its distractor syllables, and any other syllables that start with the same tile;
                    // filter out repeats

                    // then iterate through GAME_BUTTONS and fill them in using the other array, shuffled
                    if (answerChoicesList.get(t).equals(correctString)) {
                        containsCorrectSyllable = true;
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

        if (!containsCorrectSyllable) { // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles
            rand = new Random();
            int randomNum = rand.nextInt(visibleGameButtons - 1); // KP
            TextView gameTile = findViewById(GAME_BUTTONS[randomNum]);
            gameTile.setText(correctSyllable.text);
        }

    }

    private void setUpTiles() {
        boolean correctTileRepresented = false;
        Collections.shuffle(VOWELS);
        Collections.shuffle(CONSONANTS);
        if (challengeLevel == 3 || challengeLevel == 6) {
            for (int t = 0; t < visibleGameButtons; t++) {
                TextView gameTile = findViewById(GAME_BUTTONS[t]);
                if (challengeLevel == 3) {
                    gameTile.setText(VOWELS.get(t).text);
                    if (VOWELS.get(t).text.equals(correctTile.text)) {
                        correctTileRepresented = true;
                    }
                } else {
                    gameTile.setText(CONSONANTS.get(t).text);
                    if (CONSONANTS.get(t).text.equals(correctTile.text)) {
                        correctTileRepresented = true;
                    }
                }

                String tileColorStr = colorList.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }

            for (int i = visibleGameButtons; i < GAME_BUTTONS.length; i++) {
                TextView gameTile = findViewById(GAME_BUTTONS[i]);
                gameTile.setVisibility(View.INVISIBLE);
            }
        } else if (challengeLevel == 1) {

            for (int t = 0; t < visibleGameButtons; t++) {

                TextView gameTile = findViewById(GAME_BUTTONS[t]);

                if (VOWELS.get(t).text.equals(correctTile.text)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = colorList.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(VOWELS.get(t).text);
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }

        } else if (challengeLevel == 4) {

            for (int t = 0; t < visibleGameButtons; t++) {

                TextView gameTile = findViewById(GAME_BUTTONS[t]);

                if (CONSONANTS.get(t).text.equals(correctTile.text)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = colorList.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(CONSONANTS.get(t).text);
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }

        } else if (challengeLevel == 7) {
            for (int t = 0; t < numTones; t++) {

                TextView gameTile = findViewById(GAME_BUTTONS[t]);

                if (TONES.get(t).text.equals(correctTile.text)) {
                    correctTileRepresented = true;
                }

                String tileColorStr = colorList.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setText(TONES.get(t).text);
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);
            }
            for (int t = numTones; t < visibleGameButtons; t++) {
                TextView gameTile = findViewById(GAME_BUTTONS[t]);
                gameTile.setVisibility(View.INVISIBLE);
                gameTile.setClickable(false);
            }
        } else {
            // when Earth.challengeLevel == 2 || == 5
            correctTileRepresented = true;

            List<String> usedTiles = new ArrayList<>();
            Random rand = new Random();
            int randomNum;
            for (int t = 0; t < visibleGameButtons; t++) {
                TextView gameTile = findViewById(GAME_BUTTONS[t]);

                String tileColorStr = colorList.get(t % 5);
                int tileColor = Color.parseColor(tileColorStr);

                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameTile.setVisibility(View.VISIBLE);
                gameTile.setClickable(true);

                randomNum = rand.nextInt(visibleGameButtons); //
                String nextTile;
                if (randomNum == 3) {
                    nextTile = correctTile.text;
                } else {
                    nextTile = correctTile.distractors.get(randomNum);
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
            int max = visibleGameButtons - 1;
            Random rand = new Random();
            int randomNum = rand.nextInt((max - min) + 1) + min;

            TextView gameTile = findViewById(GAME_BUTTONS[randomNum]);
            gameTile.setText(correctTile.text);

        }
    }

    private void respondToTileSelection(int justClickedButton) {

        if (mediaPlayerIsPlaying) {
            return;
        }

        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        int tileNo = justClickedButton - 1; //  justClickedButton uses 1 to 15, t uses the array ID (between [0] and [14]
        TextView gameButton = findViewById(GAME_BUTTONS[tileNo]);
        String gameButtonString = gameButton.getText().toString();

        if (gameButtonString.equals(correctString)) {
            // Good job! You chose the right gameButton
            repeatLocked = false;
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(1);

            // report time and number of incorrect guesses
            if (sendAnalytics) {
                String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
                Properties info = new Properties().putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", correctString)
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < visibleGameButtons - 1; i++) {
                    if (!incorrectAnswersSelected.get(i).equals("")) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }

            TextView constructedWord = findViewById(R.id.activeWordTextView);
            String word = wordInLOPWithStandardizedSequenceOfCharacters(refWord);
            constructedWord.setText(word);

            for (int t = 0; t < visibleGameButtons; t++) {
                TextView gameTile = findViewById(GAME_BUTTONS[t]);
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
            for (int i = 0; i < visibleGameButtons-1; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(gameButtonString)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, gameButtonString);
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
