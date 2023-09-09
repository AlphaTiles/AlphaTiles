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

import static org.alphatilesapps.alphatiles.Start.COLORS;
import static org.alphatilesapps.alphatiles.Start.CorV;

//level 1: 6 visible tiles, random wrong choices
//level 2: 12 visible tiles, random wrong choices
//level 3: 18 visible tiles, random wrong choices
//level 4: 6 visible tiles, distractor wrong choices
//level 5: 12 visible tiles, distractor wrong choices
//level 6: 18 visible tiles, distractor wrong choices

//Identify the first SOUND (esp. for Thai and Lao script languages, when this may differ from starting tile).
//level 7: 6 visible tiles, random wrong choices
//level 8: 12 visible tiles, random wrong choices
//level 9: 18 visible tiles, random wrong choices
//level 10: 6 visible tiles, distractor wrong choices
//level 11: 12 visible tiles, distractor wrong choices
//level 12: 18 visible tiles, distractor wrong choices

//level 1 + S: 6 visible syllables, random wrong choices
//level 2 + S: 12 visible syllables, random wrong choices
//level 3 + S: 18 visible syllables, random wrong choices
//level 4 + S: 6 visible syllables, distractor wrong choices
//level 5 + S: 12 visible syllables, distractor wrong choices
//level 6 + S: 18 visible syllables, distractor wrong choices


// IF A LANGUAGE HAS WORDS THAT START WITH SOMETHING OTHER THAN C OR V, THIS WILL CRASH

public class Georgia extends GameActivity {

    Start.SyllableList syllableListCopy; //JP
    Set<String> challengingAnswerChoices = new HashSet<String>();
    Start.Tile initialTile;
    Start.Syllable initialSyllable;

    protected static final int[] GAME_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18
    };

    protected int[] getGameButtons() {
        return GAME_BUTTONS;
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
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());

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
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        switch (challengeLevel) {
            case 11:
            case 8:
            case 5:
            case 2:
                visibleGameButtons = 12;
                break;
            case 12:
            case 9:
            case 6:
            case 3:
                visibleGameButtons = 18;
                break;
            default:
                visibleGameButtons = 6;
        }

        if (syllableGame.equals("S")) {
            syllableListCopy = (Start.SyllableList) Start.syllableList.clone();
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
        setAdvanceArrowToGray();
        if (syllableGame.equals("S")) {
            Collections.shuffle(syllableListCopy); //JP
        }

        setWord();
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

        for (int i = 0; i < GAME_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(GAME_BUTTONS[i]);
            nextWord.setClickable(true);
        }

    }

    private void setWord() {
        chooseWord();
        if (syllableGame.equals("S")) {
            parsedRefWordSyllableArray = Start.syllableList.parseWordIntoSyllables(refWord); // JP
            initialSyllable = parsedRefWordSyllableArray.get(0);
        } else {
            parsedRefWordTileArray = Start.tileList.parseWordIntoTiles(refWord.wordInLOP, refWord); // KP
            initialTile = parsedRefWordTileArray.get(0);
            String initialTileType = initialTile.typeOfThisTileInstance;
            if(challengeLevel>6 && challengeLevel<13) { // Find first non-LV tile (first sound, since LVs are pronounced after the consonants they precede)
                Start.Tile initialLV = null;
                int t = 0;
                while (initialTileType.equals("LV") && t < parsedRefWordTileArray.size()) { // Unless there is a placeholder consonant, the consonant is the first sound
                    initialLV = initialTile;
                    t++;
                    initialTile = parsedRefWordTileArray.get(t);
                    initialTileType = initialTile.typeOfThisTileInstance;
                }
                if (initialTileType.equals("PC") && t < parsedRefWordTileArray.size()) { // If there is a placeholder consonant, vowel is the first sound
                    if (!(initialLV==null)) {
                        initialTile = initialLV;
                    } else {
                        initialTile = parsedRefWordTileArray.get(t+1);
                    }
                }
            }
            if (!CorV.contains(initialTile)) { // Make sure chosen word begins with C or V
                setWord();
            }
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);
    }

    private void setUpSyllables() {
        boolean correctSyllableRepresented = false;

        // To the challengingAnswerChoices Set, first add the distractors, then syllables with same first and second unicode character,
        // then with same last letter, then random.
        // Note that duplicates will automatically be excluded because challengingAnswerChoices is a Set.
        challengingAnswerChoices.clear();
        challengingAnswerChoices.add(initialSyllable.text);
        challengingAnswerChoices.add(initialSyllable.distractors.get(0));
        challengingAnswerChoices.add(initialSyllable.distractors.get(1));
        challengingAnswerChoices.add(initialSyllable.distractors.get(2));

        int i = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons && i < syllableListCopy.size()) {
            String option = syllableListCopy.get(i).text;
            if (option.length() >= 2 && initialSyllable.text.length() >= 2) {
                if (option.charAt(0) == initialSyllable.text.charAt(0)
                        && option.charAt(1) == initialSyllable.text.charAt(1)) {
                    challengingAnswerChoices.add(option);
                } else if (option.charAt(0) == initialSyllable.text.charAt(0)) {
                    challengingAnswerChoices.add(option);
                }
            } else {
                if (option.charAt(0) == initialSyllable.text.charAt(0)) {
                    challengingAnswerChoices.add(option);
                } else if (option.charAt(option.length() - 1) == initialSyllable.text.charAt(initialSyllable.text.length() - 1)) {
                    challengingAnswerChoices.add(option);
                }
            }
            i++;
        }

        int j = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons) { // Generally unlikely
            // Fill any remaining empty game buttons with random syllables
            challengingAnswerChoices.add(syllableListCopy.get(j).text);
            j++;
        }

        List<String> challengingAnswerChoicesList = new ArrayList<>(challengingAnswerChoices); // to index the answer choices

        for (int t = 0; t < GAME_BUTTONS.length; t++) {
            TextView gameButton = findViewById(GAME_BUTTONS[t]);

            if (syllableListCopy.get(t).text.equals(initialSyllable.text) && t<visibleGameButtons) {
                correctSyllableRepresented = true;
            }

            int buttonColor = Color.parseColor(COLORS.get(t % 5));

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3) { // random alternatives
                if (t < visibleGameButtons) {
                    gameButton.setText(syllableListCopy.get(t).text); // KP
                    gameButton.setBackgroundColor(buttonColor);
                    gameButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameButton.setVisibility(View.VISIBLE);
                } else {
                    gameButton.setText(String.valueOf(t + 1));
                    gameButton.setBackgroundResource(R.drawable.textview_border);
                    gameButton.setTextColor(Color.parseColor("#000000")); // black
                    gameButton.setClickable(false);
                    gameButton.setVisibility(View.INVISIBLE);
                }
            } else { // distractor + similar alternatives
                if (t < visibleGameButtons) {
                    if (challengingAnswerChoicesList.get(t).equals(initialSyllable.text)) {
                        correctSyllableRepresented = true;
                    }
                    gameButton.setText(challengingAnswerChoicesList.get(t)); // KP
                    gameButton.setBackgroundColor(buttonColor);
                    gameButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameButton.setVisibility(View.VISIBLE);
                } else {
                    gameButton.setText(String.valueOf(t + 1));
                    gameButton.setBackgroundResource(R.drawable.textview_border);
                    gameButton.setTextColor(Color.parseColor("#000000")); // black
                    gameButton.setClickable(false);
                    gameButton.setVisibility(View.INVISIBLE);
                }
            }
        }

        if (!correctSyllableRepresented) { // If the correct syllable didn't randomly show up in the range, then here the correct syllable overwrites one of the others
            Random rand = new Random();
            int randomNum = rand.nextInt(visibleGameButtons - 1); // KP
            TextView gameTile = findViewById(GAME_BUTTONS[randomNum]);
            gameTile.setText(initialSyllable.text);
        }

    }

    private void setUpTiles() {

        boolean correctTileRepresented = false;

        // For harder challenge levels, first add distractors, then add tiles that start with the same chars, then add random tiles
        // Note that duplicates will automatically not be added because challengingAnswerChoices is a Set
        challengingAnswerChoices.clear();
        challengingAnswerChoices.add(initialTile.text);
        challengingAnswerChoices.add(initialTile.distractors.get(0));
        challengingAnswerChoices.add(initialTile.distractors.get(1));
        challengingAnswerChoices.add(initialTile.distractors.get(2));

        int i = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons && i < CorV.size()) {
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            String option = CorV.get(index).text;
            if (option.length() >= 2 && initialTile.text.length() >= 2) {
                if (option.charAt(0) == initialTile.text.charAt(0)
                        && option.charAt(1) == initialTile.text.charAt(1)) {
                    challengingAnswerChoices.add(option);
                } else if (option.charAt(0) == initialTile.text.charAt(0)) {
                    challengingAnswerChoices.add(option);
                }
            } else {
                if (option.charAt(0) == initialTile.text.charAt(0)) {
                    challengingAnswerChoices.add(option);
                } else if (option.charAt(option.length() - 1) == initialTile.text.charAt(initialTile.text.length() - 1)) {
                    challengingAnswerChoices.add(option);
                }
            }
            i++;
        }

        while (challengingAnswerChoices.size() < visibleGameButtons) {
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            challengingAnswerChoices.add(CorV.get(index).text);
        }

        List<String> challengingAnswerChoicesList = new ArrayList<>(challengingAnswerChoices); // To index the answer choices

        for (int t = 0; t < GAME_BUTTONS.length; t++) {

            TextView gameTile = findViewById(GAME_BUTTONS[t]);

            if (CorV.get(t).equals(initialTile) && t < visibleGameButtons) {
                correctTileRepresented = true;
            }

            String tileColorStr = COLORS.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3) { // alternatives are random
                if (t < visibleGameButtons) {
                    gameTile.setText(CorV.get(t).text); // KP
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
            } else { // alternatives are challenging
                if (t < visibleGameButtons) {
                    if (challengingAnswerChoicesList.get(t).equals(initialTile.text)) {
                        correctTileRepresented = true;
                    }
                    gameTile.setText(challengingAnswerChoicesList.get(t)); // KP
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
            // If the correct tile didn't randomly show up in the range, then here the correct tile overwrites one of the others
            Random rand = new Random();
            int randomNum = rand.nextInt(visibleGameButtons - 1); // KP
            TextView gameTile = findViewById(GAME_BUTTONS[randomNum]);
            gameTile.setText(initialTile.text);
        }

    }

    private void respondToTileSelection(int justClickedTile) { //for both tiles and syllables

        if (mediaPlayerIsPlaying) {
            return;
        }

        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        String correctString = "";
        if (syllableGame.equals("S")) {
            correctString = initialSyllable.text;
        } else {
            correctString = initialTile.text;
        }

        int tileNo = justClickedTile - 1; // justClickedTile uses 1 to 18, t uses the array ID (between [0] and [17]
        TextView tile = findViewById(GAME_BUTTONS[tileNo]);
        String selectedTileString = tile.getText().toString();

        if (correctString.equals(selectedTileString)) {
            repeatLocked = false;
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(1);

            for (int t = 0; t < GAME_BUTTONS.length; t++) {
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
