package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.alphatilesapps.alphatiles.Start.sendAnalytics;
import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.CorV;
import static org.alphatilesapps.alphatiles.Start.useContextualFormsITI;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

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
    protected void hideInstructionAudioImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);
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

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

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
            hideInstructionAudioImage();
        }

        incorrectAnswersSelected = new ArrayList<>(visibleGameButtons-1);
        for (int i = 0; i < visibleGameButtons-1; i++) {
            incorrectAnswersSelected.add("");
        }
        updatePointsAndTrackers(0);
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

        for (int i = 0; i < visibleGameButtons-1; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();
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

        // To the challengingAnswerChoices Set, first add the distractors, then syllables with the same initial or final characters,
        // then random.
        // Note that duplicates will automatically be excluded because challengingAnswerChoices is a Set.

        // First, add correct answer and distractors
        challengingAnswerChoices.clear();
        challengingAnswerChoices.add(initialSyllable.text);
        challengingAnswerChoices.add(initialSyllable.distractors.get(0));
        challengingAnswerChoices.add(initialSyllable.distractors.get(1));
        challengingAnswerChoices.add(initialSyllable.distractors.get(2));

        // Then, add syllables with the same 2 initial characters
        int i = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons && i < syllableListCopy.size()) {
            String option = syllableListCopy.get(i).text;
            if(option.length()>=2 && initialSyllable.text.length()>=2) {
                if(option.charAt(0) == initialSyllable.text.charAt(0)
                        && option.charAt(1) == initialSyllable.text.charAt(1)) {
                    challengingAnswerChoices.add(option);
                }
            }
            i++;
        }

        // The, add syllables with the same one initial or final character
        i = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons && i < syllableListCopy.size()) {
            String option = syllableListCopy.get(i).text;
            if (option.charAt(0) == initialSyllable.text.charAt(0)) {
                challengingAnswerChoices.add(option);
            } else if (option.charAt(option.length() - 1) == initialSyllable.text.charAt(initialSyllable.text.length() - 1)) {
                challengingAnswerChoices.add(option);
            }
            i++;
        }

        // Finally, fill any remaining empty game buttons with random syllables
        int j = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons) { // Generally unlikely
            challengingAnswerChoices.add(syllableListCopy.get(j).text);
            j++;
        }

        // Make the gameButtons contain contextual forms for some Arabic script apps
        Set<String> contextualizedChallengingChoices = new HashSet<String>();
        if(useContextualFormsITI){
            for (String answerChoiceString : challengingAnswerChoices) {
                contextualizedChallengingChoices.add(contextualizedForm_Initial(answerChoiceString));
            }
            challengingAnswerChoices = contextualizedChallengingChoices;
        }

        List<String> challengingAnswerChoicesList = new ArrayList<>(challengingAnswerChoices); // to index the answer choices

        ArrayList<String> stringsAdded = new ArrayList<>();
        Random rand = new Random();
        int randomNum;
        // Add them to buttons
        for (int t = 0; t < GAME_BUTTONS.length; t++) {

            TextView gameTile = findViewById(GAME_BUTTONS[t]);
            String tileColorStr = colorList.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3) { // Alternatives are random
                if (t < visibleGameButtons) {
                    randomNum = rand.nextInt(syllableListCopy.size());
                    String syllableOptionText = syllableListCopy.get(randomNum).text;
                    while (stringsAdded.contains(syllableOptionText)) {
                        randomNum = rand.nextInt(syllableListCopy.size());
                        syllableOptionText = syllableListCopy.get(randomNum).text;
                    }
                    if (useContextualFormsITI) { // For some Arabic script apps
                        gameTile.setText(contextualizedForm_Initial(syllableOptionText));
                    } else {
                        gameTile.setText(syllableOptionText);
                    }
                    gameTile.setBackgroundColor(tileColor);
                    gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameTile.setVisibility(View.VISIBLE);
                    gameTile.setClickable(true);
                    stringsAdded.add(syllableOptionText);
                    if (stringsAdded.contains(initialSyllable.text)) {
                        correctSyllableRepresented = true;
                    }
                } else {
                    gameTile.setText(String.valueOf(t + 1));
                    gameTile.setBackgroundResource(R.drawable.textview_border);
                    gameTile.setTextColor(Color.parseColor("#000000")); // black
                    gameTile.setClickable(false);
                    gameTile.setVisibility(View.INVISIBLE);
                }
            } else { // Alternatives are challenging
                if (t < visibleGameButtons) {
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

        if (!correctSyllableRepresented) { // If the correct syllable didn't randomly come up for less-challenging levels, then here the correct syllable overwrites one of the others
            rand = new Random();
            randomNum = rand.nextInt(visibleGameButtons - 1); // KP
            TextView gameButton = findViewById(GAME_BUTTONS[randomNum]);
            gameButton.setText(initialSyllable.text);
        }

    }


    private void setUpTiles() {

        boolean correctTileRepresented = false;

        // For harder challenge levels, first add distractors, then add tiles that start with the same chars, then add random tiles
        // Duplicates will automatically not be added because challengingAnswerChoices is a Set
        challengingAnswerChoices.clear();
        // First add the correct answer and distractors
        challengingAnswerChoices.add(initialTile.text);
        challengingAnswerChoices.add(initialTile.distractors.get(0));
        challengingAnswerChoices.add(initialTile.distractors.get(1));
        challengingAnswerChoices.add(initialTile.distractors.get(2));


        // Then add tiles that begin with the same two chars, if they exist
        int i = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons && i < CorV.size()) {
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            String option = CorV.get(index).text;
            if(option.length()>=2 && initialTile.text.length()>=2) {
                if(option.charAt(0) == initialTile.text.charAt(0)
                        && option.charAt(1) == initialTile.text.charAt(1)) {
                    challengingAnswerChoices.add(option);
                }
            }
            i++;
        }

        // Then add tiles that begin or end with the same char
        i = 0;
        while (challengingAnswerChoices.size() < visibleGameButtons && i < CorV.size()) {
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            String option = CorV.get(index).text;
            if (option.charAt(0) == initialTile.text.charAt(0)) {
                challengingAnswerChoices.add(option);
            } else if (option.charAt(option.length() - 1) == initialTile.text.charAt(initialTile.text.length() - 1)) {
                challengingAnswerChoices.add(option);
            }
            i++;
        }

        // Then fill the remaining options with random tiles
        while (challengingAnswerChoices.size() < visibleGameButtons) {
            Random rand = new Random();
            int index = rand.nextInt(CorV.size() - 1);
            challengingAnswerChoices.add(CorV.get(index).text);
        }

        // Make the gameButtons contain contextual forms for some Arabic script apps
        Set<String> contextualizedChallengingChoices = new HashSet<String>();
        if(useContextualFormsITI) { // For some Arabic script apps
            for (String answerChoiceString : challengingAnswerChoices) {
                contextualizedChallengingChoices.add(contextualizedForm_Initial(answerChoiceString));
            }
            challengingAnswerChoices = contextualizedChallengingChoices;
        }

        // Index the answer choices
        List<String> challengingAnswerChoicesList = new ArrayList<>(challengingAnswerChoices);


        ArrayList<String> stringsAdded = new ArrayList<>();
        Random rand = new Random();
        int randomNum;
        // Add them to buttons
        for (int t = 0; t < GAME_BUTTONS.length; t++) {

            TextView gameTile = findViewById(GAME_BUTTONS[t]);
            String tileColorStr = colorList.get(t % 5);
            int tileColor = Color.parseColor(tileColorStr);

            if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 3) { // Alternatives are random
                if (t < visibleGameButtons) {
                    randomNum = rand.nextInt(CorV.size());
                    String tileOptionText = CorV.get(randomNum).text;
                    while (stringsAdded.contains(tileOptionText)) {
                        randomNum = rand.nextInt(CorV.size());
                        tileOptionText = CorV.get(randomNum).text;
                    }
                    if (useContextualFormsITI) { // For some Arabic script apps
                        gameTile.setText(contextualizedForm_Initial(tileOptionText));
                    } else {
                        gameTile.setText(tileOptionText);
                    }
                    gameTile.setBackgroundColor(tileColor);
                    gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameTile.setVisibility(View.VISIBLE);
                    gameTile.setClickable(true);
                    stringsAdded.add(tileOptionText);
                    if (stringsAdded.contains(initialTile.text)) {
                        correctTileRepresented = true;
                    }
                } else {
                    gameTile.setText(String.valueOf(t + 1));
                    gameTile.setBackgroundResource(R.drawable.textview_border);
                    gameTile.setTextColor(Color.parseColor("#000000")); // black
                    gameTile.setClickable(false);
                    gameTile.setVisibility(View.INVISIBLE);
                }
            } else { // Alternatives are challenging
                if (t < visibleGameButtons) {
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
                correctTileRepresented = true;
            }
        }

        if (!correctTileRepresented) {
            // If the correct tile didn't randomly show up for the non-challenging choices, then here the correct tile overwrites one of the others
            rand = new Random();
            randomNum = rand.nextInt(visibleGameButtons - 1);
            TextView gameTile = findViewById(GAME_BUTTONS[randomNum]);
            if(useContextualFormsITI) {
                gameTile.setText(contextualizedForm_Initial(initialTile.text));
            } else {
                gameTile.setText(initialTile.text);
            }
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
        String selectedTileString = tile.getText().toString().replace("\u200D", ""); // To account for any contextual forms in Arabic script

        if (correctString.equals(selectedTileString)) {
            repeatLocked = false;
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(1);

            if (sendAnalytics) {
                // report time and number of incorrect guesses
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
            incorrectOnLevel += 1;
            playIncorrectSound();
            for (int i = 0; i < visibleGameButtons - 1; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(selectedTileString)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, selectedTileString);
                    break;
                }
            }
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
