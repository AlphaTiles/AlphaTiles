package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Random;

import static android.graphics.Color.WHITE;
import static org.alphatilesapps.alphatiles.Start.*;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;

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
    int refColor;
    int challengeLevelThai;

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
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID;
        if (choiceType.equals("WORD_TEXT")) {
            gameID = R.id.thailand2CL;
        } else {
            gameID = R.id.thailandCL;
        }
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

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            fixConstraintsRTL(gameID);
        }

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        visibleGameButtons = GAME_BUTTONS.length;
        updatePointsAndTrackers(0);
        playAgain();

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
                    if ((!refString.equalsIgnoreCase(refStringLast)
                            && !refString.equalsIgnoreCase(refStringSecondToLast)
                            && !refString.equalsIgnoreCase(refStringThirdToLast))
                            || freshChecks > 25) {
                        freshTile = true;
                        refStringThirdToLast = refStringSecondToLast;
                        refStringSecondToLast = refStringLast;
                        refStringLast = refTile.text;
                    }
                }

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
                    // SAD should never be first tile linguistically, so no need to programmatically filter out
                    if ((!refString.equalsIgnoreCase(refStringLast)
                            && !refString.equalsIgnoreCase(refStringSecondToLast)
                            && !refString.equalsIgnoreCase(refStringThirdToLast))
                            || freshChecks > 25) {
                        freshTile = true;
                        refStringThirdToLast = refStringSecondToLast;
                        refStringSecondToLast = refStringLast;
                        refStringLast = refTile.text;
                    }
                }

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
                    if ((!refString.equalsIgnoreCase(refStringLast)
                            && !refString.equalsIgnoreCase(refStringSecondToLast)
                            && !refString.equalsIgnoreCase(refStringThirdToLast))
                            || freshChecks > 25) {
                        freshTile = true;
                        refStringThirdToLast = refStringSecondToLast;
                        refStringSecondToLast = refStringLast;
                        refStringLast = refTile.text;
                    }
                }

            }

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

        } else {
            // Makes sure that the reference tile chosen is not a glottal stop for ex;
            // Ensure that chosen tile is a consonant or vowel
            if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) {
                boolean freshTile = false;
                int freshChecks = 0;
                while (!freshTile || !(CorV.contains(refTile))) {
                    int randomTileIndex = rand.nextInt(tileListNoSAD.size());
                    freshChecks++;
                    refTile = tileListNoSAD.get(randomTileIndex);
                    refString = refTile.text;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.matches("(T|AD|C|PC)")) {
                        // JP: Disallow tone marks, diacritics, and silent consonants from being reference in level 1
                        freshChecks++;
                        randomTileIndex = rand.nextInt(tileListNoSAD.size());
                        refTile = tileListNoSAD.get(randomTileIndex);
                        refString = refTile.text;
                        refTileType = refTile.typeOfThisTileInstance;
                    }
                    if ((!refString.equalsIgnoreCase(refStringLast)
                            && !refString.equalsIgnoreCase(refStringSecondToLast)
                            && !refString.equalsIgnoreCase(refStringThirdToLast))
                            || freshChecks > 25) {
                        freshTile = true;
                        refStringThirdToLast = refStringSecondToLast;
                        refStringSecondToLast = refStringLast;
                        refStringLast = refTile.text;
                    }
                }
            }
            if (refType.equals("TILE_UPPER")) {
                boolean freshTile = false;
                int freshChecks = 0;

                while (!freshTile || refTileType.equals("X")) {
                    int randomTileIndex = rand.nextInt(tileListNoSAD.size());
                    freshChecks++;
                    refTile = tileListNoSAD.get(randomTileIndex);
                    refString = refTile.upper;
                    refTileType = refTile.typeOfThisTileInstance;
                    while (challengeLevelThai == 1 && refTileType.matches("(T|AD|D|PC)")) {
                        // JP: Disallow tone marks, diacritics, and silent consonants from being reference in level 1
                        randomTileIndex = rand.nextInt(tileListNoSAD.size());
                        freshChecks++;
                        refTile = tileListNoSAD.get(randomTileIndex);
                        refString = refTile.upper;
                        refTileType = refTile.typeOfThisTileInstance;
                    }
                    if ((!refString.equalsIgnoreCase(refStringLast)
                            && !refString.equalsIgnoreCase(refStringSecondToLast)
                            && !refString.equalsIgnoreCase(refStringThirdToLast))
                            || freshChecks > 25) {
                        freshTile = true;
                        refStringThirdToLast = refStringSecondToLast;
                        refStringSecondToLast = refStringLast;
                        refStringLast = refTile.text;
                    }
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
            fourTileChoices = tileListNoSAD.returnFourTileChoices(refTile, challengeLevelThai, refTileType);
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
    }


    private void respondToSelection(int justClickedItem) {
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

        int answerChoiceIndex = justClickedItem - 1; //  justClickedItem uses 1 to 4, answerChoiceIndex uses the array ID (between [0] and [3]
        TextView chosenItem = findViewById(GAME_BUTTONS[answerChoiceIndex]);
        String chosenItemText;
        if (refType.contains("SYLLABLE") && choiceType.contains("WORD")) {
            chosenItemText = fourWordChoices.get(answerChoiceIndex).wordInLOP; // don't strip periods
        } else if (!choiceType.equals("WORD_IMAGE")) {
            chosenItemText = chosenItem.getText().toString(); // all cases except WORD_IMAGE
        } else {
            chosenItemText = wordList.stripInstructionCharacters(fourWordChoices.get(answerChoiceIndex).wordInLOP); // when WORD_IMAGE
        }

        boolean goodMatch = false;

        switch (choiceType) {
            case "TILE_LOWER":
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                    case "TILE_UPPER":
                        if (refItemText != null && chosenItemText.equals(refTile.text)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        Tile firstAudibleTileInRefWord = firstAudibleTile(refWord);
                        if (firstAudibleTileInRefWord.text.equals(chosenItemText)) {
                            goodMatch = true;
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
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        Tile firstAudibleTileInRefWord = firstAudibleTile(refWord);
                        if (chosenItemText.equals(firstAudibleTileInRefWord.text)) {
                            goodMatch = true;
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
                        firstAudibleTileInWordChoice = firstAudibleTile(fourWordChoices.get(answerChoiceIndex));
                        if (firstAudibleTileInWordChoice.text.equals(refItemText) && firstAudibleTileInWordChoice.typeOfThisTileInstance.equals(refTileType)) {
                            goodMatch = true;
                        }
                        break;
                    case "SYLLABLE_TEXT":
                    case "SYLLABLE_AUDIO":
                        parsedChosenWordSyllableArray = syllableList.parseWordIntoSyllables(lopWordHashMap.find(chosenItemText));
                        // this needs to be word from wordlist w/the periods still in it
                        if (parsedChosenWordSyllableArray.get(0).text.equals(refItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        firstAudibleTileInWordChoice = firstAudibleTile(fourWordChoices.get(answerChoiceIndex));
                        if (refItemText != null && refItemText.equals(firstAudibleTileInWordChoice.upper)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (refItemText != null && refItemText.equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case "SYLLABLE_TEXT":
                if (refItemText != null && refItemText.equals(chosenItemText)) {
                    goodMatch = true;
                }
                break;
            default:
                break;
        }

        if (goodMatch) {
            // Good job!
            repeatLocked = false;
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(1);

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

            //JP: Added switch statement to determine which method to call: tile or word
            switch (refType) {
                case "SYLLABLE_TEXT":
                case "SYLLABLE_AUDIO":
                    if (hasSyllableAudio) {
                        playCorrectSoundThenActiveSyllableClip(false);
                    } else {
                        playCorrectSound();
                    }
                    break;
                case "TILE_LOWER":
                case "TILE_UPPER":
                case "TILE_AUDIO":
                    playCorrectSoundThenActiveTileClip(false);
                    break;
                case "WORD_TEXT":
                case "WORD_IMAGE":
                case "WORD_AUDIO":
                    playCorrectSoundThenActiveWordClip(false);
            }

        } else {
            playIncorrectSound();
        }
    }

    public Tile firstAudibleTile(Word word) {
        ArrayList<Tile> wordParsedIntoTiles = tileList.parseWordIntoTiles(word.wordInLOP, word);
        Tile tileToReturn = wordParsedIntoTiles.get(0);
        if ((tileToReturn.typeOfThisTileInstance.equals("LV") && !wordParsedIntoTiles.get(1).typeOfThisTileInstance.equals("PC"))
                || tileToReturn.typeOfThisTileInstance.matches("(PC|AD|D|T|X)")) { // These are not first sounds, though they can be written first
            tileToReturn = wordParsedIntoTiles.get(1);
        }
        int i = 2;
        while (tileToReturn.typeOfThisTileInstance.matches("(PC|AD|D|T|X)")) {
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
                if (tempSoundPoolSwitch) {
                    playActiveTileClip1(false);
                } else {
                    playActiveTileClip0(false);
                }
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                playActiveWordClip(false);
                break;
        }

    }

    public void playActiveTileClip(final boolean playFinalSound) {
        if (tempSoundPoolSwitch) {
            playActiveTileClip1(playFinalSound);
        } else {
            playActiveTileClip0(playFinalSound);
        }
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
                    updatePointsAndTrackers(0);
                    repeatLocked = false;
                    playCorrectFinalSound();
                } else {
                    if (repeatLocked) {
                        setAllGameButtonsClickable();
                    }
                    if (after12checkedTrackers == 1){
                        setOptionsRowClickable();
                        // JP: In setting 1, the player can always keep advancing to the next tile/word/image
                    }
                    else if (trackerCount >0 && trackerCount % 12 != 0) {
                        setOptionsRowClickable();
                        // Otherwise, updatePointsAndTrackers will set it clickable only after
                        // the player returns to earth (2) or sees the celebration screen (3)
                    }
                }
            }
        }, refSyllable.duration);

    }


    public void playActiveTileClip1(final boolean playFinalSound) {     //JP: for SoundPool, for tile audio
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        if (tileAudioIDs.containsKey(refTile.audioForThisTileType)) {
            gameSounds.play(tileAudioIDs.get(refTile.audioForThisTileType), 1.0f, 1.0f, 2, 0, 1.0f);
        }
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (playFinalSound) {
                    updatePointsAndTrackers(0);
                    repeatLocked = false;
                    playCorrectFinalSound();
                } else {
                    if (repeatLocked) {
                        setAllGameButtonsClickable();
                    }
                    if (after12checkedTrackers == 1){
                        setOptionsRowClickable();
                        // JP: In setting 1, the player can always keep advancing to the next tile/word/image
                    }
                    else if (trackerCount >0 && trackerCount % 12 != 0) {
                        setOptionsRowClickable();
                        // Otherwise, updatePointsAndTrackers will set it clickable only after
                        // the player returns to earth (2) or sees the celebration screen (3)
                    }
                }
            }
        }, tileDurations.get(refTile.audioForThisTileType));
    }


    public void playActiveTileClip0(final boolean playFinalSound) {     //JP: for Media Player; tile audio

        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        int resID = getResources().getIdentifier(refTile.audioForThisTileType, "raw", getPackageName());
        final MediaPlayer mp1 = MediaPlayer.create(this, resID);
        mediaPlayerIsPlaying = true;
        //mp1.start();
        mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                mpCompletion(mp1, playFinalSound);
            }
        });
        mp1.start();
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
                if (after12checkedTrackers == 1){
                    setOptionsRowClickable();
                    //JP: In setting 1, the player can always keep advancing to the next tile/word/image
                }
                else if (trackerCount >0 && trackerCount % 12 != 0) {
                    setOptionsRowClickable();
                    // Otherwise, updatePointsAndTrackers will set it clickable only after
                    // the player returns to earth (2) or sees the celebration screen (3)
                }
            }
        }, 925);
    }

    private void playCorrectSoundThenActiveSyllableClip(final boolean playFinalSound) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                playActiveSyllableClip(playFinalSound);
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                }
                if (after12checkedTrackers == 1){
                    setOptionsRowClickable();
                    // JP: In setting 1, the player can always keep advancing to the next tile/word/image
                }
                else if (trackerCount >0 && trackerCount % 12 != 0) {
                    setOptionsRowClickable();
                    // Otherwise, updatePointsAndTrackers will set it clickable only after
                    // the player returns to earth (2) or sees the celebration screen (3)
                }
            }
        }, correctSoundDuration);
    }

    public void playCorrectSoundThenActiveTileClip(final boolean playFinalSound) {
        if (tempSoundPoolSwitch) {
            playCorrectSoundThenActiveTileClip1(playFinalSound); //SoundPool
        } else {
            playCorrectSoundThenActiveTileClip0(playFinalSound); //MediaPlayer
        }
    }

    public void playCorrectSoundThenActiveTileClip1(final boolean playFinalSound) { //JP: Specifically for TILE audio. playCorrectSoundThenActiveWordClip is for WORDS
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                playActiveTileClip(playFinalSound);
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                if (after12checkedTrackers == 1){
                }
                    setOptionsRowClickable();
                }
                    //JP: in setting 1 we always want to keep advancing to the next tile/word/image regardless
                else if (trackerCount >0 && trackerCount % 12 != 0) {
                    setOptionsRowClickable();
                    //JP: because updatePointsAndTrackers will take care of setting it clickable otherwise
                    // and we don't want the user to be able to advance before returning to earth (2) or
                    // before seeing the celebration screen (3)
                }
            }
        }, correctSoundDuration);
    }

    public void playCorrectSoundThenActiveTileClip0(final boolean playFinalSound) { // Media player
        MediaPlayer mp2 = MediaPlayer.create(this, R.raw.zz_correct);
        mediaPlayerIsPlaying = true;
        mp2.start();
        mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp2) {
                mp2.reset(); //JP: This fixes "mediaplayer went away with unhandled events" issue
                mp2.release();
                playActiveTileClip(playFinalSound);
            }
        });
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

    @Override
    public void onBackPressed() {
        // no action
    }

}
