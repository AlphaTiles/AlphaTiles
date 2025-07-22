package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import static android.graphics.Color.WHITE;
import static org.alphatilesapps.alphatiles.Start.*;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;

import androidx.annotation.NonNull;

public class Thailand extends GameActivity {
    ArrayList<Start.Word> fourWordChoices = new ArrayList<>();
    ArrayList<Start.Tile> fourTileChoices = new ArrayList<>();
    ArrayList<Start.Syllable> fourSyllableChoices = new ArrayList<>();

    private static final String[] TYPES = {"TILE_LOWER", "TILE_UPPER", "TILE_AUDIO", "WORD_TEXT",
            "WORD_IMAGE", "WORD_AUDIO", "SYLLABLE_TEXT", "SYLLABLE_AUDIO",
            "CONTEXTUAL"};

    private static final String[] CONTEXTUAL_TILE_POSITION = { "INITIAL", "MEDIAL", "FINAL" };
    String refType;
    Start.Tile refTile;
    Start.Syllable refSyllable;
    String refString;
    String refTileType;
    String refStringLast = "";
    String refStringSecondToLast = "";
    String refStringThirdToLast = "";
    String choiceType;
    String contextualTilePosition = "";
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
        if(clString.length()>3){ // True only if contextual forms are being used
            contextualTilePosition = CONTEXTUAL_TILE_POSITION[Integer.parseInt(clString.substring(3,4)) - 1];
        }

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
        updatePointsAndTrackers(0);
        incorrectAnswersSelected = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.add("");
        }
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
                    freshTile = verifyFreshTile(refString, freshChecks);
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
                    // SAD should never be first tile linguistically, so no need to programatically filter out
                    freshTile = verifyFreshTile(refString, freshChecks);
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
                    freshTile = verifyFreshTile(refString, freshChecks);
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
            if (refType.contains("TILE") || refType.equals("CONTEXTUAL")) { // Set a permissible refTile and set refTileType and refString
                boolean permissibleRefTile = false;
                int freshChecks = 0;
                while (!permissibleRefTile) {
                    freshChecks++;
                    int randomTileIndex = rand.nextInt(tileListNoSAD.size());
                    refTile = tileListNoSAD.get(randomTileIndex);
                    refTileType = refTile.typeOfThisTileInstance;
                    boolean choicesContainContextualizersOrPlaceholders = false;
                    if (refTile.text.contains(contextualizingCharacter) || refTile.text.contains(placeholderCharacter)) {
                        choicesContainContextualizersOrPlaceholders = true;
                    } else {
                        for (String t : refTile.distractors) {
                            if (t.contains(contextualizingCharacter) || t.contains(placeholderCharacter)) {
                                choicesContainContextualizersOrPlaceholders = true;
                                break;
                            }
                        }
                    }

                    switch (refType) { // Set refString based on the type of tile reference
                        case "TILE_LOWER":
                        case "TILE_AUDIO":
                            refString = refTile.text;
                            break;
                        case "TILE_UPPER":
                            refString = refTile.upper;
                            break;
                        case "CONTEXTUAL":
                            switch (contextualTilePosition) {
                                case "INITIAL":
                                    if (!refTile.wordInitialVariant.equals("none")) {
                                        refString = refTile.wordInitialVariant;
                                    } else {
                                        refString = contextualizedForm_Initial(refTile.text);
                                    }
                                    break;
                                case "FINAL":
                                    if (!refTile.wordFinalVariant.equals("none")) {
                                        refString = refTile.wordFinalVariant;
                                    } else {
                                        refString = contextualizedForm_Final(refTile.text);
                                    }
                                    break;
                                default: // MEDIAL
                                    if (!refTile.wordMedialVariant.equals("none")) {
                                        refString = refTile.wordMedialVariant;
                                    } else {
                                        refString = contextualizedForm_Medial(refTile.text);
                                    }
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    // Disallow non-fresh reference tiles
                    // Ensure reference tiles are either consonants or vowels
                    // Disallow placeholder consonants as reference items
                    // Disallow non-joining and non-spacing (non-contextual) characters from contextual forms matching games (Arabic script)
                    // Disallow tiles with placeholders from contextual forms matching games (Arabic script)
                    // Disallow tiles that are already displayed with a contextual character in the tile list (Arabic script)
                    // Disallow testing a tile in a certain contextual position that it cannot occur in (see: position restrictions)
                    permissibleRefTile = verifyFreshTile(refString, freshChecks)
                            && CorV.contains(refTile)
                            && !(refTileType.matches("(PC)"))
                            && !((refType.matches("CONTEXTUAL") || choiceType.matches("CONTEXTUAL")) && (NON_JOINERS_ARABIC.contains(refTile) || NON_SPACERS_ARABIC.contains(refTile) || choicesContainContextualizersOrPlaceholders || (!refTile.canBePlacedInPosition(contextualTilePosition))))
                            && !(contextualTilePosition.matches("INITIAL") && (RIGHT_JOINERS_ARABIC.contains(refTile)))
                           ;
                }
            }
        }
        switch (refType) {
            case "SYLLABLE_TEXT":
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "CONTEXTUAL":
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

        if (choiceType.matches("(TILE_LOWER|TILE_UPPER|CONTEXTUAL)")) {
            // challengeLevelThai 1 = pull random tiles for wrong choices
            // challengeLevelThai 2 = pull distractor tiles for wrong choices
            fourTileChoices = tileListNoSAD.returnFourTileChoices(refTile, challengeLevelThai, refTileType);
            if (choiceType.equals("CONTEXTUAL")) {
                for (Tile t: fourTileChoices) {
                    if (!t.canBePlacedInPosition(contextualTilePosition)) {
                        fourTileChoices.remove(t);
                        ArrayList<Start.Tile> tilesToDrawFrom = cumulativeStageBasedTileList;
                        if(challengeLevelThai==2) {
                            tilesToDrawFrom.clear();
                            tilesToDrawFrom.addAll(CorV);
                        }
                        Tile alternativeTileChoice = fittingTileAlternative(fourTileChoices, contextualTilePosition, tilesToDrawFrom);
                        if (Objects.isNull(alternativeTileChoice)) {
                            // Couldn't find three fitting answer choices for this contextual tile position
                            playAgain();
                            return;
                        } else {
                            fourTileChoices.add(alternativeTileChoice);
                        }
                    }
                }
            }

        } else if (choiceType.matches("(WORD_TEXT|WORD_IMAGE)") && (!refType.contains("SYLLABLE"))) {
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
            case "CONTEXTUAL":
                switch(contextualTilePosition) { // Arabic script challengeLevel option; position specified by the 4th character of the challengeLevel number
                    case "INITIAL":
                        for (int t = 0; t < GAME_BUTTONS.length; t++) {
                            TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                            String choiceColorStr = "#A9A9A9"; // dark gray
                            int choiceColorNo = Color.parseColor(choiceColorStr);
                            choiceButton.setBackgroundColor(choiceColorNo);
                            choiceButton.setTextColor(Color.parseColor("#000000")); // black
                            if (!fourTileChoices.get(t).wordInitialVariant.equals("none")) {
                                choiceButton.setText(fourTileChoices.get(t).wordInitialVariant);
                            } else {
                                choiceButton.setText(contextualizedForm_Initial(fourTileChoices.get(t).text));
                            }
                        }
                        break;
                    case "FINAL":
                        for (int t = 0; t < GAME_BUTTONS.length; t++) {
                            TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                            String choiceColorStr = "#A9A9A9"; // dark gray
                            int choiceColorNo = Color.parseColor(choiceColorStr);
                            choiceButton.setBackgroundColor(choiceColorNo);
                            choiceButton.setTextColor(Color.parseColor("#000000")); // black
                            if (!fourTileChoices.get(t).wordFinalVariant.equals("none")) {
                                choiceButton.setText(fourTileChoices.get(t).wordFinalVariant);
                            } else {
                                choiceButton.setText(contextualizedForm_Final(fourTileChoices.get(t).text));
                            }
                        }
                        break;
                    default: // MEDIAL
                        for (int t = 0; t < GAME_BUTTONS.length; t++) {
                            TextView choiceButton = findViewById(GAME_BUTTONS[t]);
                            String choiceColorStr = "#A9A9A9"; // dark gray
                            int choiceColorNo = Color.parseColor(choiceColorStr);
                            choiceButton.setBackgroundColor(choiceColorNo);
                            choiceButton.setTextColor(Color.parseColor("#000000")); // black
                            if (!fourTileChoices.get(t).wordMedialVariant.equals("none")) {
                                choiceButton.setText(fourTileChoices.get(t).wordMedialVariant);
                            } else {
                                choiceButton.setText(contextualizedForm_Medial(fourTileChoices.get(t).text));
                            }
                        }
                        break;
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
                } else {
                    setAllGameButtonsClickable();
                }
                break;
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "CONTEXTUAL":
                if (hasTileAudio) {
                    playActiveTileClip(false);
                } else {
                    setAllGameButtonsClickable();
                }
                break;
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


    private void respondToSelection(int justClickedItem) {

        int answerChoiceIndex = justClickedItem - 1; //  justClickedItem uses 1 to 4, answerChoiceIndex uses the array ID (between [0] and [3]
        boolean goodMatch = false;
        String chosenItemText = "";

        switch (choiceType) {
            case "TILE_LOWER":
            case "CONTEXTUAL":
            case "TILE_UPPER":
                Tile chosenTile = fourTileChoices.get(answerChoiceIndex);
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                    case "TILE_UPPER":
                    case "CONTEXTUAL":
                        if (chosenTile.text.equals(refTile.text)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        Tile firstAudibleTileInRefWord = firstAudibleTile(refWord);
                        if (firstAudibleTileInRefWord.text.equals(chosenTile.text)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }

                // Store the chosen answer choice text for analytics/teacher information
                if (choiceType.equals("TILE_UPPER")) {
                    chosenItemText = chosenTile.upper;
                } else if (choiceType.equals("CONTEXTUAL")) {
                    switch(contextualTilePosition) { // specified by the 4th character of the challengeLevel number
                        case "INITIAL":
                            chosenItemText = contextualizedForm_Initial(chosenTile.text);
                            break;
                        case "FINAL":
                            chosenItemText = contextualizedForm_Final(chosenTile.text);
                            break;
                        default: // MEDIAL
                            chosenItemText = contextualizedForm_Medial(chosenTile.text);
                            break;
                    }
                } else {
                    chosenItemText = chosenTile.text;
                }
                break;
            case "WORD_TEXT": // CHOICE TYPEs
            case "WORD_IMAGE":
                Word chosenWord = fourWordChoices.get(answerChoiceIndex);
                ArrayList<Start.Syllable> parsedChosenWordSyllableArray;
                Tile firstAudibleTileInWordChoice;
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                    case "TILE_UPPER":
                        firstAudibleTileInWordChoice = firstAudibleTile(chosenWord);
                        if (firstAudibleTileInWordChoice.text.equals(refTile.text) && firstAudibleTileInWordChoice.typeOfThisTileInstance.equals(refTileType)) {
                            goodMatch = true;
                        }
                        break;
                    case "SYLLABLE_TEXT":
                    case "SYLLABLE_AUDIO":
                        parsedChosenWordSyllableArray = syllableList.parseWordIntoSyllables(chosenWord);
                        if (parsedChosenWordSyllableArray.get(0).text.equals(refSyllable.text)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (chosenWord.wordInLOP.equals(refWord.wordInLOP)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                chosenItemText = wordList.stripInstructionCharacters(chosenWord.wordInLOP);
                break;
            case "SYLLABLE_TEXT": // CHOICE TYPE
                Syllable chosenSyllable = fourSyllableChoices.get(answerChoiceIndex);
                switch (refType) {
                    case "SYLLABLE_AUDIO":
                        if (chosenSyllable.text.equals(refSyllable.text)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        parsedRefWordSyllableArray = syllableList.parseWordIntoSyllables(refWord);
                        if (chosenSyllable.text.equals(parsedRefWordSyllableArray.get(0).text)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                chosenItemText = chosenSyllable.text;
                break;
            default:
                break;
        }

        if (goodMatch) {
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
                case "TILE_AUDIO":
                    playCorrectSoundThenActiveTileClip(false);
                    break;
                case "TILE_LOWER":
                case "TILE_UPPER":
                case "CONTEXTUAL":
                    if (hasTileAudio) {
                        playCorrectSoundThenActiveTileClip(false);
                    } else {
                        playCorrectSound();
                    }
                    break;
                case "WORD_TEXT":
                case "WORD_IMAGE":
                case "WORD_AUDIO":
                    playCorrectSoundThenActiveWordClip(false);
                    break;
                default:
                    break;
            }

        } else {
            incorrectOnLevel += 1;
            playIncorrectSound();

            for (int i = 0; i < 3; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(chosenItemText)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, chosenItemText);
                    break;
                }
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

    public void onChoiceClick(@NonNull View view) {
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
            default:
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
                    else if (trackerCount == 0){
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

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                setAllGameButtonsClickable();
                if (after12checkedTrackers == 1){
                    setOptionsRowClickable();
                    //JP: In setting 1, the player can always keep advancing to the next tile/word/image
                }
                else if (trackerCount >0 && trackerCount % 12 != 0) {
                    setOptionsRowClickable();
                    // Otherwise, updatePointsAndTrackers will set it clickable only after
                    // the player returns to earth (2) or sees the celebration screen (3)
                }
                playActiveSyllableClip(playFinalSound);
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

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                setAllGameButtonsClickable();
                if (after12checkedTrackers == 1){
                    setOptionsRowClickable();
                    //JP: In setting 1, the player can always keep advancing to the next tile/word/image
                }
                else if (trackerCount >0 && trackerCount % 12 != 0) {
                    setOptionsRowClickable();
                    // Otherwise, updatePointsAndTrackers will set it clickable only after
                    // the player returns to earth (2) or sees the celebration screen (3)
                }
                playActiveTileClip(playFinalSound);
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
}
