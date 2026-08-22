package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import static android.graphics.Color.WHITE;
import static org.alphatilesapps.alphatiles.Start.*;

public class Thailand extends GameActivity {
    ArrayList<Start.Word> fourWordChoices = new ArrayList<>();
    ArrayList<Start.Tile> fourTileChoices = new ArrayList<>();
    ArrayList<Start.Syllable> fourSyllableChoices = new ArrayList<>();

    private static final String[] TYPES = {"TILE_LOWER", "TILE_UPPER", "TILE_AUDIO", "WORD_TEXT",
            "WORD_IMAGE", "WORD_AUDIO", "SYLLABLE_TEXT", "SYLLABLE_AUDIO"};

    String refType;
    Start.Tile refTile;
    Start.Syllable refSyllable;
    String refString;
    String refTileType;
    String refStringLast = "";
    String refStringSecondToLast = "";
    String refStringThirdToLast = "";
    String choiceType;
    String chosenItemText;
    int refColor;
    int challengeLevelThai;
    int correctButtonIndex;

    protected static final int[] GAME_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04
    };
    @Override
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
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1)
                    .instructionAudioName, "raw", context.getPackageName());
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

        // If challengeLevel is 235, then:
        // challengeLevelThai = 2 (distractors not random)
        // refType = "TILE_AUDIO" (1-indexed index in the TYPES array)
        // choiceType = "WORD_IMAGE" (1-indexed index in the TYPES array)
        String clString = String.valueOf(challengeLevel);
        challengeLevelThai = Integer.parseInt(clString.substring(0, 1));
        refType = TYPES[Integer.parseInt(clString.substring(1, 2)) - 1];
        choiceType = TYPES[Integer.parseInt(clString.substring(2, 3)) - 1];

        int gameID = 0;
        if (choiceType.equals("WORD_TEXT")) {
            setContentView(R.layout.thailand2);
            gameID = R.id.thailand2CL;
        } else {
            setContentView(R.layout.thailand);
            gameID = R.id.thailandCL;
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

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        visibleGameButtons = GAME_BUTTONS.length;
        updateView();
        incorrectAnswersSelected = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.add("");
        }
        playAgain();
        setUpInitialView();
        updateView();

    }

    public void repeatGame(View view) {
        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        repeatLocked = true;
        setAdvanceArrowToGray();

        TextView refItem = findViewById(R.id.referenceItem);
        refItem.setText("");
        refItem.setBackgroundResource(0);

        Random rand = new Random();
        int randomNum = rand.nextInt(4); // 5 colors
        String refColorStr = colorList.get(randomNum);
        refColor = Color.parseColor(refColorStr);

        // If either or both elements are word-based, then three IF statements, but if both elements are tile-based, then WHILE LOOP

        // BLOCK_01
        if (refType.contains("WORD") || (choiceType.contains("WORD") && !refType.contains("SYLLABLE"))) {
            if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO") || choiceType.equals("TILE_LOWER")) {
                boolean freshTile = false;
                int freshChecks = 0;
                while (!freshTile) {
                    chooseWord();
                    freshChecks++;
                    parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                    refTile = firstAudibleTile(refWord);
                    refString = refTile.text;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        chooseWord();
                        parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                        refTile = firstAudibleTile(refWord);
                        refString = refTile.text;
                        refTileType = refTile.typeOfThisTileInstance;
                    }
                    freshTile = verifyFreshTile(refString, freshChecks);
                }

            // BLOCK_02
            } else if (refType.equals("TILE_UPPER") || choiceType.equals("TILE_UPPER")) {
                boolean freshTile = false;
                int freshChecks = 0;
                while (!freshTile) {
                    chooseWord();
                    freshChecks++;
                    parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                    refTile = firstAudibleTile(refWord);
                    refString = refTile.upper;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        chooseWord();
                        parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                        refTile = firstAudibleTile(refWord);
                        refString = refTile.upper;
                        refTileType =  refTile.typeOfThisTileInstance;
                    }
                    // SAD should never be first tile linguistically, so no need to programatically filter out
                    freshTile = verifyFreshTile(refString, freshChecks);
                }

            // BLOCK_03
            } else if (refType.contains("WORD") && choiceType.contains("WORD")) {
                boolean freshTile = false;
                int freshChecks = 0;
                while (!freshTile) {
                    chooseWord();
                    parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                    refTile = firstAudibleTile(refWord);
                    refString = refTile.text;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        chooseWord();
                        freshChecks++;
                        parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                        refTile = firstAudibleTile(refWord);
                        refString = refTile.text;
                        refTileType = refTile.typeOfThisTileInstance;
                    }
                    freshTile = verifyFreshTile(refString, freshChecks);
                }

            }

        // BLOCK_04
        } else if (choiceType.contains("SYLLABLE") && refType.contains("SYLLABLE")) {
            boolean freshSyllable = false;
            while (!freshSyllable) {
                int randomNum2 = rand.nextInt(syllableList.size());
                refSyllable = syllableList.get(randomNum2);
                refString = syllableList.get(randomNum2).text;
                if (!refString.equals(refStringLast)
                        && !refString.equals(refStringSecondToLast)
                        && !refString.equals(refStringThirdToLast)) {
                    freshSyllable = true;
                    refStringThirdToLast = refStringSecondToLast;
                    refStringSecondToLast = refStringLast;
                    refStringLast = refString;
                }
            }

        // BLOCK_05
        } else if (choiceType.contains("WORD") && refType.contains("SYLLABLE")) {
            boolean freshSyllable = false;
            while (!freshSyllable) {
                chooseWord();
                parsedRefWordSyllableArray = syllableList.parseWordIntoSyllables(refWord);
                refSyllable = parsedRefWordSyllableArray.get(0);
                refString = parsedRefWordSyllableArray.get(0).text;
                if (!refString.equals(refStringLast)
                        && !refString.equals(refStringSecondToLast)
                        && !refString.equals(refStringThirdToLast)) {
                    freshSyllable = true;
                    refStringThirdToLast = refStringSecondToLast;
                    refStringSecondToLast = refStringLast;
                    refStringLast = refString;
                }
            }

        // BLOCK_06
        } else {
            // Makes sure that the reference tile chosen is not a glottal stop for ex;
            // Ensure that chosen tile is a consonant or vowel
            if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) {
                boolean freshTile = false;
                int freshChecks = 0;
                while (!freshTile || !(CorV.contains(refTile))) {
                    int randomTileIndex = rand.nextInt(cumulativeStageBasedTileList.size());
                    freshChecks++;
                    refTile = cumulativeStageBasedTileList.get(randomTileIndex);
                    refString = refTile.text;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.matches("(T|AD|D|PC)")) {
                        // JP: Disallow tone marks, diacritics, and silent consonants from being reference in level 1
                        freshChecks++;
                        randomTileIndex = rand.nextInt(cumulativeStageBasedTileList.size());
                        refTile = cumulativeStageBasedTileList.get(randomTileIndex);
                        refString = refTile.text;
                        refTileType = refTile.typeOfThisTileInstance;
                    }
                    freshTile = verifyFreshTile(refString, freshChecks);
                }
            }
            // BLOCK_07
            if (refType.equals("TILE_UPPER")) {
                boolean freshTile = false;
                int freshChecks = 0;

                while (!freshTile || refTileType.equals("X")) {
                    int randomTileIndex = rand.nextInt(cumulativeStageBasedTileList.size());
                    freshChecks++;
                    refTile = cumulativeStageBasedTileList.get(randomTileIndex);
                    refString = refTile.upper;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.matches("(T|AD|D|PC)")) {
                        // JP: Disallow tone marks, diacritics, and silent consonants from being reference in level 1
                        randomTileIndex = rand.nextInt(cumulativeStageBasedTileList.size());
                        freshChecks++;
                        refTile = cumulativeStageBasedTileList.get(randomTileIndex);
                        refString = refTile.upper;
                        refTileType = refTile.typeOfThisTileInstance;
                    }
                    freshTile = verifyFreshTile(refString, freshChecks);
                }
            }
        }
        switch (refType) {
            case "SYLLABLE_TEXT":
            case "TILE_LOWER":
            case "TILE_UPPER":
                refItem.setBackgroundColor(refColor);
                refItem.setTextColor(Color.parseColor("#FFFFFF")); // white
                refItem.setText(refString);
                break;
            case "TILE_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_tile_audio);
                break;
            case "SYLLABLE_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_syllable_audio);
                break;
            case "WORD_TEXT":
                refItem.setBackgroundColor(WHITE);
                refItem.setTextColor(Color.parseColor("#000000")); // black
                refItem.setText(wordList.stripInstructionCharacters(refWord.wordInLOP));
                break;
            case "WORD_IMAGE":
                int resID1 = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
                refItem.setBackgroundResource(resID1);
                refItem.setText("");
                break;
            case "WORD_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_word_audio);
                break;
            default:
                break;
        }

        if (choiceType.equals("TILE_LOWER") || choiceType.equals("TILE_UPPER")) {
            fourTileChoices = tileListNoSAD.returnFourTileChoices(refTile, challengeLevelThai, refTileType, cumulativeStageBasedTileList);
            // challengeLevelThai 1 = pull random tiles for wrong choices
            // challengeLevelThai 2 = pull distractor tiles for wrong choices
        } else if ((choiceType.equals("WORD_TEXT") || choiceType.equals("WORD_IMAGE")) && (!refType.contains("SYLLABLE"))) {
            fourWordChoices = wordList.returnFourWords(refWord, refTile, challengeLevelThai, refType);
            // challengeLevelThai 1 = pull words that begin with random tiles (not distractor, not same) for wrong choices
            // challengeLevelThai 2 = pull words that begin with distractor tiles (or if not random) for wrong choices
            // challengeLevelThai 3 = pull words that begin with same tile (as correct word) for wrong choices
        } else if (refType.contains("SYLLABLE") && (choiceType.contains("WORD"))) {
            fourWordChoices = syllableList.returnFourWordChoices(refString, challengeLevelThai);
        } else if (refType.contains("SYLLABLE") && (choiceType.contains("SYLLABLE"))) {
            fourSyllableChoices = syllableList.returnFourSyllableChoices(refString, challengeLevelThai);
        }

        switch (choiceType) {
            case "TILE_LOWER":
                for (int t = 0; t < GAME_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    choiceButton.setText(fourTileChoices.get(t).text);
                }
                break;
            case "TILE_UPPER":
                for (int t = 0; t < GAME_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    choiceButton.setText(fourTileChoices.get(t).upper);
                }
                break;
            case "WORD_TEXT":
                for (int t = 0; t < GAME_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray - JP edited
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    choiceButton.setText(wordList.stripInstructionCharacters(fourWordChoices.get(t).wordInLOP));
                    if (refType.contains("SYLLABLE") && !hasSyllableAudio) {
                        choiceButton.setClickable(true);
                    }
                }
                break;
            case "WORD_IMAGE":
                for (int t = 0; t < GAME_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                    int resID = getResources().getIdentifier(fourWordChoices.get(t).wordInLWC, "drawable", getPackageName());
                    choiceButton.setBackgroundResource(0);
                    choiceButton.setBackgroundResource(resID);
                    choiceButton.setText("");
                    if (refType.contains("SYLLABLE") && !hasSyllableAudio) {
                        choiceButton.setClickable(true);
                    }
                }
                break;
            case "SYLLABLE_TEXT":
                for (int t = 0; t < GAME_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray - JP edited
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    choiceButton.setText(fourSyllableChoices.get(t).text);
                    if (!hasSyllableAudio) {
                        choiceButton.setClickable(true);
                    }
                }
                break;
            default:
                break;
        }
        switch (refType) {
            case "SYLLABLE_AUDIO":
            case "SYLLABLE_TEXT":
                if (hasSyllableAudio) {
                    playActiveSyllableClip(false);
                }
                break;
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "TILE_AUDIO":
                playActiveTileClip(false);
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                playActiveWordClip(false);
                break;
        }
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        incorrectOnLevel = 0;
        determineCorrectButtonIndex();
        levelBegunTime = System.currentTimeMillis();
    }

    private boolean verifyFreshTile(String refString, int freshChecks) {
        if ((!refString.equalsIgnoreCase(refStringLast)
                && !refString.equalsIgnoreCase(refStringSecondToLast)
                && !refString.equalsIgnoreCase(refStringThirdToLast))
                || freshChecks > 25) {
            refStringThirdToLast = refStringSecondToLast;
            refStringSecondToLast = refStringLast;
            refStringLast = refTile.text;
            return true;
        }
        return false;
    }

    private void determineCorrectButtonIndex() {

        for (int b = 0; b < GAME_BUTTONS.length; b++) {

            String refItemText = null;
            TextView refItem = findViewById(R.id.referenceItem);

            switch (refType) {
                case "TILE_LOWER":
                case "TILE_UPPER":
                case "TILE_AUDIO":
                case "SYLLABLE_AUDIO":
                    refItemText = refString;
                    break;
                case "WORD_TEXT":
                case "SYLLABLE_TEXT":
                    refItemText = refItem.getText().toString();
                    break;
                case "WORD_IMAGE":
                case "WORD_AUDIO":
                    refItemText = wordList.stripInstructionCharacters(refWord.wordInLOP);
                    break;
                default:
                    break;
            }

            TextView chosenItem = findViewById(GAME_BUTTONS[b]);
            if (refType.contains("SYLLABLE") && choiceType.contains("WORD")) {
                chosenItemText = fourWordChoices.get(b).wordInLOP; // don't strip periods
            } else if (!choiceType.equals("WORD_IMAGE")) {
                chosenItemText = chosenItem.getText().toString(); // all cases except WORD_IMAGE
            } else {
                chosenItemText = wordList.stripInstructionCharacters(fourWordChoices.get(b).wordInLOP); // when WORD_IMAGE
            }

            switch (choiceType) {
                case "TILE_LOWER":
                    switch (refType) {
                        case "TILE_LOWER":
                        case "TILE_AUDIO":
                        case "TILE_UPPER":
                            if (refItemText != null && chosenItemText.equals(refTile.text)) {
                                correctButtonIndex = b;
                            }
                            break;
                        case "WORD_TEXT":
                        case "WORD_IMAGE":
                        case "WORD_AUDIO":
                            Tile firstAudibleTileInRefWord = firstAudibleTile(refWord);
                            if (firstAudibleTileInRefWord.text.equals(chosenItemText)) {
                                correctButtonIndex = b;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "TILE_UPPER":
                    switch (refType) {
                        case "TILE_LOWER":
                        case "TILE_AUDIO":
                        case "TILE_UPPER":
                            if (chosenItemText.equals(refTile.upper)) {
                                correctButtonIndex = b;
                            }
                            break;
                        case "WORD_TEXT":
                        case "WORD_IMAGE":
                        case "WORD_AUDIO":
                            Tile firstAudibleTileInRefWord = firstAudibleTile(refWord);
                            if (chosenItemText.equals(firstAudibleTileInRefWord.upper)) {
                                correctButtonIndex = b;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "WORD_TEXT":
                case "WORD_IMAGE":
                    ArrayList<Start.Syllable> parsedChosenWordSyllableArray;
                    Tile firstAudibleTileInWordChoice;
                    switch (refType) {
                        case "TILE_LOWER":
                        case "TILE_AUDIO":
                            firstAudibleTileInWordChoice = firstAudibleTile(fourWordChoices.get(b));
                            if (firstAudibleTileInWordChoice.text.equals(refItemText) && firstAudibleTileInWordChoice.typeOfThisTileInstance.equals(refTileType)) {
                                correctButtonIndex = b;
                            }
                            break;
                        case "SYLLABLE_TEXT":
                        case "SYLLABLE_AUDIO":
                            parsedChosenWordSyllableArray = syllableList.parseWordIntoSyllables(lopWordHashMap.find(chosenItemText));
                            // this needs to be word from wordlist w/the periods still in it
                            if (parsedChosenWordSyllableArray.get(0).text.equals(refItemText)) {
                                correctButtonIndex = b;
                            }
                            break;
                        case "TILE_UPPER":
                            firstAudibleTileInWordChoice = firstAudibleTile(fourWordChoices.get(b));
                            if (refItemText != null && refItemText.equals(firstAudibleTileInWordChoice.upper)) {
                                correctButtonIndex = b;
                            }
                            break;
                        case "WORD_TEXT":
                        case "WORD_IMAGE":
                        case "WORD_AUDIO":
                            if (refItemText != null && refItemText.equals(chosenItemText)) {
                                correctButtonIndex = b;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "SYLLABLE_TEXT":
                    if (refItemText != null && refItemText.equals(chosenItemText)) {
                        correctButtonIndex = b;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void respondToSelection(int justClickedItem) {

        int answerChoiceIndex = justClickedItem - 1; //  justClickedItem uses 1 to 4, answerChoiceIndex uses the array ID (between [0] and [3]

        if (answerChoiceIndex==correctButtonIndex) {
            // Good job!

            if (sendAnalytics) {
                // report time and number of incorrect guesses
                String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
                Properties info = new Properties().putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", refTile)
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < 3; i++) {
                    if (!incorrectAnswersSelected.get(i).equals("")) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }

            recordAttempt(true, 1);
            endRound(answerChoiceIndex);

            //JP: Added switch statement to determine which method to call: tile or word
            switch (refType) {
                case "SYLLABLE_TEXT":
                case "SYLLABLE_AUDIO":
                    if (hasSyllableAudio) {
                        playGameSoundThenActiveSyllableClip(true,false);
                    } else {
                        playCorrectSound();
                    }
                    break;
                case "TILE_LOWER":
                case "TILE_UPPER":
                case "TILE_AUDIO":
                    playGameSoundThenActiveTileClip(true, false);
                    break;
                case "WORD_TEXT":
                case "WORD_IMAGE":
                case "WORD_AUDIO":
                    playGameSoundThenActiveWordClip(true,false);
            }

        } else {
            recordAttempt(false, 0);
            incorrectOnLevel += 1;
            if(secondChances) {
                playIncorrectSound();
            } else {
                endRound(correctButtonIndex);
                }
                // @ToDo...update so that you can pass correct or incorrect to these methods:
                // @ToDo...then would probably make sense to merge these two switch statements into one inside of endRound()
                switch (refType) {
                    case "SYLLABLE_TEXT":
                    case "SYLLABLE_AUDIO":
                        if (hasSyllableAudio) {
                            playGameSoundThenActiveSyllableClip(false,false);
                        }
                        break;
                    case "TILE_LOWER":
                    case "TILE_UPPER":
                    case "TILE_AUDIO":
                        playGameSoundThenActiveTileClip(false, false);
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        playGameSoundThenActiveWordClip(false,false);
                }
            }

            for (int i = 0; i < 3; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(chosenItemText)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, chosenItemText);
                    break;
                }
            }
        }


    private void endRound(int answerChoiceIndex) {

        repeatLocked = false;
        setAdvanceArrowToBlue();

        for (int b = 0; b < GAME_BUTTONS.length; b++) {
            TextView nextButton = findViewById(GAME_BUTTONS[b]);
            nextButton.setClickable(false);
            if (b == answerChoiceIndex && !choiceType.equals("WORD_IMAGE")) {
                nextButton.setBackgroundColor(refColor);
                nextButton.setTextColor(Color.parseColor("#FFFFFF")); // white
            }
            if (b != answerChoiceIndex && choiceType.equals("WORD_IMAGE")) {
                nextButton.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
            }
        }

    }


    public Tile firstAudibleTile(Word word) {
        ArrayList<Tile> wordParsedIntoTiles = tileList.parseWordIntoTiles(word.wordInLOP, word);
        Tile tileToReturn = wordParsedIntoTiles.get(0);
        if ((tileToReturn.typeOfThisTileInstance.equals("LV") && !wordParsedIntoTiles.get(1).typeOfThisTileInstance.equals("PC"))
                || tileToReturn.typeOfThisTileInstance.matches("(PC|AD|D|T)")) { // These are not first sounds, though they can be written first
            tileToReturn = wordParsedIntoTiles.get(1);
        }
        int i = 2;
        while (tileToReturn.typeOfThisTileInstance.matches("(PC|AD|D|T)")) {
            tileToReturn = wordParsedIntoTiles.get(i);
            i++;
        }
        return tileToReturn;
    }

    public void onChoiceClick(View view) {
        respondToSelection(Integer.parseInt((String) view.getTag())); // KP
    }

    public void onRefClick(View view) {
        switch (refType) {
            case "SYLLABLE_TEXT":
            case "SYLLABLE_AUDIO":
                if (hasSyllableAudio) {
                    playActiveSyllableClip(false);
                }
                break;
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "TILE_AUDIO":
                playActiveTileClip(false);
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                playActiveWordClip(false);
                break;
        }

    }

    public void playActiveTileClip(final boolean playFinalSound) {
        super.tileAudioPress(playFinalSound, refTile);
    }

    private void playActiveSyllableClip(final boolean playFinalSound) { // We chose not to implement the Media Player option for syllable audio
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        if (syllableAudioIDs.containsKey(refSyllable.audioName)) {
            gameSounds.play(syllableAudioIDs.get(refSyllable.audioName), 1.0f, 1.0f, 2, 0, 1.0f);
        }
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (playFinalSound) {
                    repeatLocked = false;
                    playCorrectFinalSound();
                } else {
                    if (repeatLocked) {
                        setAllGameButtonsClickable();
                    }
                    if (uponMastery == 1 || !celebratingNow){
                        setOptionsRowClickable();
                    }
                }
            }
        }, refSyllable.duration);

    }

    private void playCorrectSound() {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                }
                if (uponMastery == 1 || !celebratingNow){
                    setOptionsRowClickable();
                }
            }
        }, 925);
    }

    private void playGameSoundThenActiveSyllableClip(boolean correctAnswer, final boolean playFinalSound) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        if (correctAnswer) {
            gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        } else {
            gameSounds.play(incorrectSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        }

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                playActiveSyllableClip(playFinalSound);
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                }
                if (uponMastery == 1 || !celebratingNow){
                    setOptionsRowClickable();
                }
            }
        }, correctSoundDuration);
    }

    public void playGameSoundThenActiveTileClip(boolean correctAnswer, final boolean playFinalSound) {

        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        if (correctAnswer) {
            gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        } else {
            gameSounds.play(incorrectSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        }

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                playActiveTileClip(playFinalSound);
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                }
                if (uponMastery == 1 || !celebratingNow){
                    setOptionsRowClickable();
                }
            }
        }, correctSoundDuration);
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
