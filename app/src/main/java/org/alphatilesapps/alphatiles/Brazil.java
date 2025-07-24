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
import java.util.Objects;
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
    int numTones;
    int indexToRemove;
    Start.Tile correctTile;
    Start.Syllable correctSyllable;
    String correctString;

    ArrayList<String> parsedRefWordTileArrayStrings;
    ArrayList<String> parsedRefWordSyllableArrayStrings;

    Start.SyllableList syllableListCopy;

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
            syllableListCopy = (Start.SyllableList) Start.syllableList.clone();
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
            parsedRefWordSyllableArrayStrings = new ArrayList<String>();
            for(Syllable s: parsedRefWordSyllableArray) {
                parsedRefWordSyllableArrayStrings.add(s.text);
            }
        } else {
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
            parsedRefWordTileArrayStrings = new ArrayList<String>();
            for(Tile t: parsedRefWordTileArray) {
                parsedRefWordTileArrayStrings.add(t.text);
            }
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
        indexToRemove = 0;

        boolean repeat = true;

        if (!syllableGame.equals("S")) {
            ArrayList<Integer> possibleIndices = new ArrayList<>();
            for (int i = 0; i < parsedRefWordTileArray.size(); i++) {
                possibleIndices.add(i);
            }
            while (repeat && !possibleIndices.isEmpty()) {
                index = rand.nextInt(possibleIndices.size());
                correctTile = parsedRefWordTileArray.get(possibleIndices.get(index));
                indexToRemove = possibleIndices.get(index);
                possibleIndices.remove((Integer) indexToRemove);

                while (SAD_STRINGS.contains(correctTile.text)) { // JP: Makes sure that SAD is never chosen as missing tile
                    index = rand.nextInt(possibleIndices.size());
                    correctTile = parsedRefWordTileArray.get(possibleIndices.get(index));
                    indexToRemove = possibleIndices.get(index);
                    possibleIndices.remove((Integer) indexToRemove);
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
            indexToRemove = rand.nextInt(parsedRefWordSyllableArray.size());
            correctSyllable = parsedRefWordSyllableArray.get(indexToRemove);
            while (SAD_STRINGS.contains(correctSyllable.text)) { // JP: makes sure that SAD is never chosen as missing syllable
                indexToRemove = rand.nextInt(parsedRefWordSyllableArray.size());
                correctSyllable = parsedRefWordSyllableArray.get(indexToRemove);
            }
            correctString = correctSyllable.text;
        }

        TextView constructedWord = findViewById(R.id.activeWordTextView);
        StringBuilder wordBuilder = new StringBuilder();
        String word;
        if (syllableGame.equals("S")) {
            Start.Syllable blankSyllable = new Start.Syllable("__", new ArrayList<>(),"X", 0, correctSyllable.color, "No restrictions (default)");
            parsedRefWordSyllableArray.set(indexToRemove, blankSyllable);
            for (Syllable s : parsedRefWordSyllableArray) {
                if (s != null) {
                    wordBuilder.append(s.text);
                }
            }
            word = wordBuilder.toString();
        } else { // Tile game
            Start.Tile blankTile = new Start.Tile("__", new ArrayList<>(), "", "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, correctTile.typeOfThisTileInstance, 1, "", "No restrictions (default)", "none", "none", "none");
            parsedRefWordTileArray.set(indexToRemove, blankTile);
            if (scriptType.equals("Khmer") && correctTile.typeOfThisTileInstance.equals("C")){
                if(indexToRemove < parsedRefWordTileArray.size()-1 && parsedRefWordTileArray.get(indexToRemove + 1).typeOfThisTileInstance.matches("(V|AV|BV|D)")) {
                    blankTile.text = "\u200B"; // The word will default to containing a placeholder circle. Add zero-width space, instead of line.
                    parsedRefWordTileArray.set(indexToRemove, blankTile);
                } else {
                    blankTile.text = placeholderCharacter; // Since Khmer has lots of placeholder circles, we'll use them for all consonant blanks.
                    parsedRefWordTileArray.set(indexToRemove, blankTile);
                }
            }
            if (scriptType.matches("(Thai|Lao)") && correctTile.typeOfThisTileInstance.equals("C")){
                blankTile.text = placeholderCharacter;
                parsedRefWordTileArray.set(indexToRemove, blankTile);
            }
            if (useContextualFormsFITB) { // Setting used by some Arabic script apps to make tiles appear in contextual forms in answer choices and around blanks
                blankTile.text = contextualizedWordPieceString(blankTile.text, indexToRemove, parsedRefWordTileArrayStrings);
            }


            word = combineTilesToMakeWord(parsedRefWordTileArray, refWord, indexToRemove);
        }
        constructedWord.setText(word);
    }

    private void setUpSyllables() {
        if (challengeLevel == 1) { // Find and add random alternatives

            WordPieceStringPositionSet alreadyAddedPlacements = new WordPieceStringPositionSet();

            for (int b = 0; b < visibleGameButtons; b++) {
                TextView gameButton = findViewById(GAME_BUTTONS[b]);

                Syllable option = fittingSyllableAlternative(alreadyAddedPlacements, parsedRefWordSyllableArray, indexToRemove);
                if (Objects.isNull(option)) { // Fewer than 4 viable answer choices available. 'Restart' with new word.
                    playAgain();
                    return;
                } else { // Display viable options.
                    gameButton.setText(option.text);
                    gameButton.setBackgroundColor(Color.parseColor(colorList.get(b % 5)));
                    gameButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameButton.setVisibility(View.VISIBLE);
                    gameButton.setClickable(true);
                    alreadyAddedPlacements.add(new WordPieceStringPosition(indexToRemove, option.text));
                }

            }

            if (!(alreadyAddedPlacements.contains(new WordPieceStringPosition(indexToRemove, correctString)))) { // If the correct syllable wasn't randomly added as an answer choice, then here it overwrites one of the others
                Random rand = new Random();
                int randomNum = rand.nextInt(visibleGameButtons - 1); // KP
                TextView gameButton = findViewById(GAME_BUTTONS[randomNum]);
                gameButton.setText(correctSyllable.text);
            }
        } else { // Challenge level 2
            // Alternatives are challenging: first  distractors, then syllables with the same initial or final characters, then random.

            // First, add correct answer and distractors
            Set<String> challengingAnswerChoices = new HashSet<String>(); // Duplicates will be prevented since this is a Set
            challengingAnswerChoices.add(correctSyllable.text);
            for (int d=0; d<3; d++) {
                if(syllableHashMap.get(correctSyllable.distractors.get(d)).canBePlacedInPosition(parsedRefWordSyllableArray, indexToRemove)) {
                    challengingAnswerChoices.add(correctSyllable.distractors.get(d));
                }
            }

            // Then, add syllables with the same 2 initial characters
            int i = 0;
            while (challengingAnswerChoices.size() < visibleGameButtons && i < syllableListCopy.size()) {
                String option = syllableListCopy.get(i).text;
                if(option.length()>=2 && correctSyllable.text.length()>=2) {
                    if(option.charAt(0) == correctSyllable.text.charAt(0)
                            && option.charAt(1) == correctSyllable.text.charAt(1)
                            && syllableHashMap.get(option).canBePlacedInPosition(parsedRefWordSyllableArray, indexToRemove)) {
                        challengingAnswerChoices.add(option);
                    }
                }
                i++;
            }

            // The, add syllables with the same one initial or final character
            i = 0;
            while (challengingAnswerChoices.size() < visibleGameButtons && i < syllableListCopy.size()) {
                String option = syllableListCopy.get(i).text;
                if (syllableHashMap.get(option).canBePlacedInPosition(parsedRefWordSyllableArray, indexToRemove)) {
                    if (option.charAt(0) == correctSyllable.text.charAt(0)) {
                        challengingAnswerChoices.add(option);
                    } else if (option.charAt(option.length() - 1) == correctSyllable.text.charAt(correctSyllable.text.length() - 1)) {
                        challengingAnswerChoices.add(option);
                    }
                }
                i++;
            }

            // Finally, fill any remaining empty game buttons with random syllables
            int j = 0;
            while (challengingAnswerChoices.size()<visibleGameButtons && j<syllableListCopy.size()) {
                if (syllableListCopy.get(j).canBePlacedInPosition(parsedRefWordSyllableArray, indexToRemove)) {
                    challengingAnswerChoices.add(syllableListCopy.get(j).text);
                }
                j++;
            }

            // Make the gameButtons contain contextual forms for some Arabic script apps
            Set<String> contextualizedChallengingChoices = new HashSet<String>();
            if(useContextualFormsFITB){
                produceContextualSyllableAnswerChoices();
            }

            List<String> challengingAnswerChoicesList = new ArrayList<>(challengingAnswerChoices); // Index and shuffle
            Collections.shuffle(challengingAnswerChoicesList);

            for (int b = 0; b < visibleGameButtons; b++) { // Add the choices to buttons

                TextView gameButton = findViewById(GAME_BUTTONS[b]);

                if (b < visibleGameButtons && b < challengingAnswerChoicesList.size()) {
                    gameButton.setText(challengingAnswerChoicesList.get(b)); // KP
                    gameButton.setBackgroundColor(Color.parseColor(colorList.get(b % 5)));
                    gameButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameButton.setVisibility(View.VISIBLE);
                } else {
                    gameButton.setText(String.valueOf(b + 1));
                    gameButton.setBackgroundResource(R.drawable.textview_border);
                    gameButton.setTextColor(Color.parseColor("#000000")); // black
                    gameButton.setClickable(false);
                    gameButton.setVisibility(View.INVISIBLE);
                }
            }
        }

        if (useContextualFormsFITB) { // Setting used by some Arabic script apps
            produceContextualSyllableAnswerChoices();
        }
    }

    private void setUpTiles() {

        ArrayList<Tile> distractorTiles = new ArrayList<>();
        distractorTiles.add(correctTile);
        for(int d=0; d<3; d++) {
            Tile distractorTile = tileHashMap.get(correctTile.distractors.get(d));
            if (correctTile.typeOfThisTileInstance.equals(distractorTile.tileType)
                    || correctTile.typeOfThisTileInstance.equals(distractorTile.tileTypeB)
                    || correctTile.typeOfThisTileInstance.equals(distractorTile.tileTypeC)) {

                distractorTile.typeOfThisTileInstance = correctTile.typeOfThisTileInstance;

            }
            distractorTiles.add(distractorTile);
        }

        WordPieceStringPositionSet alreadyAddedPlacements = new WordPieceStringPositionSet();
        Start.Tile option;

        for (int b = 0; b < visibleGameButtons; b++) {
            TextView gameButton = findViewById(GAME_BUTTONS[b]);
            switch (challengeLevel) {
                case 1:
                case 3:
                    option = fittingTileAlternative(alreadyAddedPlacements, parsedRefWordTileArray, indexToRemove, VOWELS);
                    break;
                case 2:
                case 5:
                    option = fittingTileAlternative(alreadyAddedPlacements, parsedRefWordTileArray, indexToRemove, distractorTiles);
                    break;
                case 4:
                case 6:
                    option = fittingTileAlternative(alreadyAddedPlacements, parsedRefWordTileArray, indexToRemove, CONSONANTS);
                    break;
                case 7:
                    option = fittingTileAlternative(alreadyAddedPlacements, parsedRefWordTileArray, indexToRemove, TONES);
                    break;
                default:
                    option = fittingTileAlternative(alreadyAddedPlacements, parsedRefWordTileArray, indexToRemove, cumulativeStageBasedTileList);
                    break;
            }

            if (Objects.isNull(option)) {
                if (b < 4) {
                    option = fittingTileAlternative(alreadyAddedPlacements, parsedRefWordTileArray, indexToRemove, cumulativeStageBasedTileList);
                    if (Objects.isNull(option)) {
                        playAgain();
                        return;
                    }
                } else { // Viable answer choice beyond 4 not found
                    gameButton.setText(String.valueOf(b + 1));
                    gameButton.setBackgroundResource(R.drawable.textview_border);
                    gameButton.setTextColor(Color.parseColor("#000000")); // black
                    gameButton.setClickable(false);
                    gameButton.setVisibility(View.INVISIBLE);
                }
            } else { // Display viable answer choice
                gameButton.setText(option.text);
                gameButton.setBackgroundColor(Color.parseColor(colorList.get(b % 5)));
                gameButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                gameButton.setVisibility(View.VISIBLE);
                gameButton.setClickable(true);
                alreadyAddedPlacements.add(new WordPieceStringPosition(indexToRemove, option.text));
            }
        }

        for (int b=visibleGameButtons; b<GAME_BUTTONS.length; b++) { // Hide empty buttons
            TextView gameButton = findViewById(GAME_BUTTONS[b]);
            if(!Objects.isNull(gameButton)) {
                gameButton.setText(String.valueOf(b + 1));
                gameButton.setBackgroundResource(R.drawable.textview_border);
                gameButton.setTextColor(Color.parseColor("#000000")); // black
                gameButton.setClickable(false);
                gameButton.setVisibility(View.INVISIBLE);
            }
        }

        if (!(alreadyAddedPlacements.contains(new WordPieceStringPosition(indexToRemove, correctTile.text)))) { // If the correct syllable wasn't randomly added as an answer choice, then here it overwrites one of the others
            Random rand = new Random();
            int randomNum = rand.nextInt(visibleGameButtons - 1); // KP
            TextView gameButton = findViewById(GAME_BUTTONS[randomNum]);
            gameButton.setText(correctTile.text);
        }


        if (useContextualFormsFITB) { // Setting used by some Arabic script apps
            produceContextualTileAnswerChoices();
        }
    }

    /**
     * For Arabic script apps
     * Show the right form (medial, initial, final) of the missing tile choices
     */
    private void produceContextualTileAnswerChoices() {

        for(int t = 0; t< visibleGameButtons; t++) { // For all answer choices
            TextView answerChoiceButton = findViewById(GAME_BUTTONS[t]);
            String contextualizedChoice = contextualizedWordPieceString(answerChoiceButton.getText().toString(), indexToRemove, parsedRefWordTileArrayStrings);
            answerChoiceButton.setText(contextualizedChoice);
        }
    }

    /**
     * For Arabic script apps
     * Show the right form (medial, initial, final) of the missing sylllable choices
     */
    private void produceContextualSyllableAnswerChoices() {
        for(int t = 0; t< visibleGameButtons; t++) { // For all answer choices
            TextView answerChoiceButton = findViewById(GAME_BUTTONS[t]);
            String contextualizedChoice = contextualizedWordPieceString(answerChoiceButton.getText().toString(), indexToRemove, parsedRefWordSyllableArrayStrings);
            answerChoiceButton.setText(contextualizedChoice);
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

        if (isolateForm(gameButtonString).equals(isolateForm(correctString))) {
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
