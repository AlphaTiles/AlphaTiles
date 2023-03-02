package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import java.util.Collections;
import java.util.Random;

import static android.graphics.Color.WHITE;
import static org.alphatilesapps.alphatiles.Start.*;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;

public class Thailand extends GameActivity {

    Start.TileList sortableTilesArray;
    Start.SyllableList sortableSyllArray;

    ArrayList<String[]> fourChoices = new ArrayList<>();  // Will store LWC and LOP word or will store tile audio name and tile (lower or upper)
    // Or syllable audio name and syllable

    private static final String[] TYPES = {"TILE_LOWER", "TILE_UPPER", "TILE_AUDIO", "WORD_TEXT",
            "WORD_IMAGE", "WORD_AUDIO", "SYLL_TEXT", "SYLL_AUDIO"};

    // 11, 12, 21, 31, 32, 14, 41, 24, 42, 45, 54, 51, 15,

    String refType;
    String refTile;
    String refTileType;
    String refTileLast = "";
    String refTileSecondToLast = "";
    String refTileThirdToLast = "";
    String choiceType;
    String refSyll;
    int refColor;
    int challengeLevelThai;
    int thailandPoints;
    boolean thailandHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04
    };

    //JP added Override
    @Override
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


        points = getIntent().getIntExtra("points", 0);
        thailandPoints = getIntent().getIntExtra("thailandPoints", 0);
        thailandHasChecked12Trackers = getIntent().getBooleanExtra("thailandHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        thailandPoints = prefs.getInt("storedThailandPoints_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, 0);
        thailandHasChecked12Trackers = prefs.getBoolean("storedThailandHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
        syllableGame = getIntent().getStringExtra("syllableGame");
        visibleTiles = TILE_BUTTONS.length;

        // So, if challengeLevel is 235, then...
        // challengeLevelThai = 2 (distractors not random)
        // refType = "TILE_AUDIO" ... note that one is subtracted below so you refer to the array as 1 to x + 1, not 0 to x
        // choiceType = "WORD_IMAGE"
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
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

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
        pointsEarned.setText(String.valueOf(thailandPoints));

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

    public void repeatGame(View view) {
        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        repeatLocked = true;

        TextView refItem = findViewById(R.id.referenceItem);
        refItem.setText("");
        refItem.setBackgroundResource(0);

        Random rand = new Random();
        int randomNum = rand.nextInt(4); // 5 colors
        String refColorStr = COLORS.get(randomNum);
        refColor = Color.parseColor(refColorStr);

        // If either or both elements are word-based, then three IF statements, but if both elements are tile-based, then WHILE LOOP

        if (refType.contains("WORD") || (choiceType.contains("WORD") && !refType.contains("SYLL"))) {

            if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO") || choiceType.equals("TILE_LOWER")) {
                boolean freshTile = false;
                while (!freshTile) {
                    chooseWord();
                    parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
                    refTile = parsedWordArrayFinal.get(0);
                    refTileType = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).tileType;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        chooseWord();
                        parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
                        refTile = parsedWordArrayFinal.get(0);
                        refTileType = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).tileType;
                    }
                    if (!refTile.equals(refTileLast)
                            && !refTile.equals(refTileSecondToLast)
                            && !refTile.equals(refTileThirdToLast)) {
                        freshTile = true;
                        refTileThirdToLast = refTileSecondToLast;
                        refTileSecondToLast = refTileLast;
                        refTileLast = refTile;
                    }
                }

            } else if (refType.equals("TILE_UPPER") || choiceType.equals("TILE_UPPER")) {
                boolean freshTile = false;
                while (!freshTile) {
                    chooseWord();
                    parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
                    refTile = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).upperTile;
                    refTileType = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).tileType;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        chooseWord();
                        parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
                        refTile = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).upperTile;
                        refTileType = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).tileType;
                    }
                    // SAD should never be first tile linguistically, so no need to programatically filter out
                    if (!refTile.equals(refTileLast)
                            && !refTile.equals(refTileSecondToLast)
                            && !refTile.equals(refTileThirdToLast)) {
                        freshTile = true;
                        refTileThirdToLast = refTileSecondToLast;
                        refTileSecondToLast = refTileLast;
                        refTileLast = refTile;
                    }
                }

            } else if (refType.contains("WORD") && choiceType.contains("WORD")) {
                boolean freshTile = false;
                while (!freshTile) {
                    chooseWord();
                    parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
                    refTile = parsedWordArrayFinal.get(0);
                    refTileType = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).tileType;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        chooseWord();
                        parsedWordArrayFinal = tileList.parseWordIntoTiles(wordInLOP);
                        refTile = parsedWordArrayFinal.get(0);
                        refTileType = tileListNoSAD.get(tileListNoSAD.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).tileType;
                    }
                    if (!refTile.equals(refTileLast)
                            && !refTile.equals(refTileSecondToLast)
                            && !refTile.equals(refTileThirdToLast)) {
                        freshTile = true;
                        refTileThirdToLast = refTileSecondToLast;
                        refTileSecondToLast = refTileLast;
                        refTileLast = refTile;
                    }
                }

            }

        } else if (choiceType.contains("SYLL") && refType.contains("SYLL")) {
            boolean freshTile = false;
            while (!freshTile) {
                int randomNum2 = rand.nextInt(sortableSyllArray.size());
                refTile = sortableSyllArray.get(randomNum2).syllable;
                if (!refTile.equals(refTileLast)
                        && !refTile.equals(refTileSecondToLast)
                        && !refTile.equals(refTileThirdToLast)) {
                    freshTile = true;
                    refTileThirdToLast = refTileSecondToLast;
                    refTileSecondToLast = refTileLast;
                    refTileLast = refTile;
                }
            }

        } else if (choiceType.contains("WORD") && refType.contains("SYLL")) {
            boolean freshTile = false;
            while (!freshTile) {
                chooseWord();
                parsedWordArrayFinal = syllableList.parseWordIntoSyllables(wordInLOP);
                refTile = parsedWordArrayFinal.get(0);
                if (!refTile.equals(refTileLast)
                        && !refTile.equals(refTileSecondToLast)
                        && !refTile.equals(refTileThirdToLast)) {
                    freshTile = true;
                    refTileThirdToLast = refTileSecondToLast;
                    refTileSecondToLast = refTileLast;
                    refTileLast = refTile;
                }
            }

        } else {
            // Makes sure that the reference tile chosen is not a glottal stop for ex;
            // Ensures that chosen tile is an actual consonant or vowel
            String refCVX = "X";
            if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) {
                boolean freshTile = false;
                while (!freshTile || refCVX.equals("X")) {
                    int randomNum2 = rand.nextInt(sortableTilesArray.size());
                    refCVX = sortableTilesArray.get(randomNum2).tileType;
                    refTile = sortableTilesArray.get(randomNum2).baseTile;
                    refTileType = sortableTilesArray.get(randomNum2).tileType;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        randomNum2 = rand.nextInt(sortableTilesArray.size());
                        refCVX = sortableTilesArray.get(randomNum2).tileType;
                        refTile = sortableTilesArray.get(randomNum2).baseTile;
                        refTileType = sortableTilesArray.get(randomNum2).tileType;
                    }
                    if (!refTile.equals(refTileLast)
                            && !refTile.equals(refTileSecondToLast)
                            && !refTile.equals(refTileThirdToLast)) {
                        freshTile = true;
                        refTileThirdToLast = refTileSecondToLast;
                        refTileSecondToLast = refTileLast;
                        refTileLast = refTile;
                    }
                }
            }
            if (refType.equals("TILE_UPPER")) {
                boolean freshTile = false;
                while (!freshTile || refCVX.equals("X")) {
                    int randomNum2 = rand.nextInt(sortableTilesArray.size());
                    refCVX = sortableTilesArray.get(randomNum2).tileType;
                    refTile = sortableTilesArray.get(randomNum2).upperTile;
                    refTileType = sortableTilesArray.get(randomNum2).tileType;
                    while (challengeLevelThai == 1 && refTileType.equals("T")) {
                        // JP: disallow tone marker from being reference in level 1
                        randomNum2 = rand.nextInt(sortableTilesArray.size());
                        refCVX = sortableTilesArray.get(randomNum2).tileType;
                        refTile = sortableTilesArray.get(randomNum2).upperTile;
                        refTileType = sortableTilesArray.get(randomNum2).tileType;
                    }
                    if (!refTile.equals(refTileLast)
                            && !refTile.equals(refTileSecondToLast)
                            && !refTile.equals(refTileThirdToLast)) {
                        freshTile = true;
                        refTileThirdToLast = refTileSecondToLast;
                        refTileSecondToLast = refTileLast;
                        refTileLast = refTile;
                    }
                }
            }
        }
        switch (refType) {
            case "SYLL_TEXT":
            case "TILE_LOWER":
            case "TILE_UPPER":
                refItem.setBackgroundColor(refColor);
                refItem.setTextColor(Color.parseColor("#FFFFFF")); // white
                refItem.setText(refTile);
                break;
            case "TILE_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_tile_audio);
                break;
            case "SYLL_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_syllable_audio);
                break;
            case "WORD_TEXT":
                refItem.setBackgroundColor(WHITE);
                refItem.setTextColor(Color.parseColor("#000000")); // black
                refItem.setText(wordList.stripInstructionCharacters(wordInLOP));
                break;
            case "WORD_IMAGE":
                int resID1 = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
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
            fourChoices = tileListNoSAD.returnFourTiles(refTile, challengeLevelThai, choiceType, refTileType);
            // challengeLevelThai 1 = pull random tiles for wrong choices
            // challengeLevelThai 2 = pull distractor tiles for wrong choices
        } else if ((choiceType.equals("WORD_TEXT") || choiceType.equals("WORD_IMAGE")) && (!refType.contains("SYLL"))) {
            fourChoices = Start.wordList.returnFourWords(wordInLOP, wordInLWC, refTile, challengeLevelThai, refType, choiceType);
            //, (float) 0.4);
            //so words with less than or equal to 0.4
            // challengeLevelThai 1 = pull words that begin with random tiles (not distractor, not same) for wrong choices
            // challengeLevelThai 2 = pull words that begin with distractor tiles (or if not random) for wrong choices
            // challengeLevelThai 3 = pull words that begin with same tile (as correct word) for wrong choices
        } else if (refType.contains("SYLL") && (choiceType.contains("WORD"))) {
            fourChoices = syllableList.returnFourWords(refTile, challengeLevelThai);
        } else if (refType.contains("SYLL") && (choiceType.contains("SYLL"))) {
            fourChoices = syllableList.returnFourSylls(refTile, challengeLevelThai);
        }

        Collections.shuffle(fourChoices);

        switch (choiceType) {
            case "TILE_LOWER":
            case "TILE_UPPER":
                for (int t = 0; t < TILE_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    choiceButton.setText(fourChoices.get(t)[1]);
                }
                break;
            case "WORD_TEXT":
                for (int t = 0; t < TILE_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray - JP edited
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    //LOGGER.info("Remember: AB1: fourChoices.get(t)[1] = " + fourChoices.get(t)[1]);
                    choiceButton.setText(wordList.stripInstructionCharacters((fourChoices.get(t)[1])));
                    if (refType.contains("SYLL") && !hasSyllableAudio) {
                        choiceButton.setClickable(true);
                    }
                }
                break;
            case "WORD_IMAGE":
                for (int t = 0; t < TILE_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    int resID = getResources().getIdentifier(fourChoices.get(t)[0], "drawable", getPackageName());
                    choiceButton.setBackgroundResource(0);
                    choiceButton.setBackgroundResource(resID);
                    choiceButton.setText("");
                    if (refType.contains("SYLL") && !hasSyllableAudio) {
                        choiceButton.setClickable(true);
                    }
                }
                break;
            case "SYLL_TEXT":
                for (int t = 0; t < TILE_BUTTONS.length; t++) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray - JP edited
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    choiceButton.setText(fourChoices.get(t)[1]);
                    if (!hasSyllableAudio) {
                        choiceButton.setClickable(true);
                    }
                }
                break;
            default:
                break;
        }
        switch (refType) {
            case "SYLL_AUDIO":
            case "SYLL_TEXT":
                if (hasSyllableAudio) {
                    playActiveSyllClip(); //never implemented media player for syll audio
                }
                break;
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "TILE_AUDIO":
                if (tempSoundPoolSwitch) {
                    playActiveTileClip1();
                } else {
                    playActiveTileClip0();
                }
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                if (tempSoundPoolSwitch) {
                    playActiveWordClip1(false);
                } else {
                    playActiveWordClip0(false);
                }
                break;
        }
    }


    private void respondToSelection(int justClickedItem) {
        String refItemText = null;
        TextView refItem = findViewById(R.id.referenceItem);

        switch (refType) {
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "WORD_TEXT":
            case "SYLL_TEXT":
                refItemText = refItem.getText().toString();
                break;
            case "SYLL_AUDIO":
            case "TILE_AUDIO":
                refItemText = refTile;
                break;
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                refItemText = wordList.stripInstructionCharacters(wordInLOP);
                break;
            default:
                break;
        }

        int t = justClickedItem - 1; //  justClickedItem uses 1 to 4, t uses the array ID (between [0] and [3]
        TextView chosenItem = findViewById(TILE_BUTTONS[t]);

        String chosenItemText;
        if (refType.contains("SYLL") && choiceType.contains("WORD")) {
            chosenItemText = fourChoices.get(t)[1]; // don't strip periods
        } else if (!choiceType.equals("WORD_IMAGE")) {
            chosenItemText = chosenItem.getText().toString();   // all cases except WORD_IMAGE
        } else {
            chosenItemText = wordList.stripInstructionCharacters(fourChoices.get(t)[1]);             // when WORD_IMAGE
        }

        boolean goodMatch = false;

        switch (choiceType) {
            case "TILE_LOWER":
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                        if (refItemText != null && refItemText.equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        if (refItemText != null && refItemText.equals(tileListNoSAD
                                .get(tileListNoSAD.returnPositionInAlphabet(chosenItemText)).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (parsedWordArrayFinal.get(0).equals(chosenItemText)) {
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
                        if (chosenItemText.equals(tileListNoSAD.get(tileListNoSAD
                                .returnPositionInAlphabet(refItemText)).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        if (refItemText != null && refItemText.equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (chosenItemText.equals(tileListNoSAD.get(tileListNoSAD
                                .returnPositionInAlphabet(parsedWordArrayFinal.get(0))).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
                ArrayList<String> parsedChosenWordArrayFinal;
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                        parsedChosenWordArrayFinal = tileList.parseWordIntoTiles(chosenItemText);
                        if (parsedChosenWordArrayFinal.get(0).equals(refItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "SYLL_TEXT":
                    case "SYLL_AUDIO":
                        parsedChosenWordArrayFinal = syllableList.parseWordIntoSyllables(chosenItemText);
                        // this needs to be word from wordlist w/the periods still in it
                        if (parsedChosenWordArrayFinal.get(0).equals(refItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        parsedChosenWordArrayFinal = tileList.parseWordIntoTiles(chosenItemText);
                        if (refItemText != null && refItemText.equals(tileListNoSAD.get(tileListNoSAD
                                .returnPositionInAlphabet(parsedChosenWordArrayFinal.get(0))).upperTile)) {
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
            case "SYLL_TEXT":
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

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points += 1;
            thailandPoints += 1;
            pointsEarned.setText(String.valueOf(thailandPoints));

            trackerCount++;
            if (trackerCount >= 12) {
                thailandHasChecked12Trackers = true;
            }
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedThailandPoints_level" + challengeLevel + "_player" + playerString + "_"
                    + syllableGame, thailandPoints);
            editor.putBoolean("storedThailandHasChecked12Trackers_level" + challengeLevel + "_player"
                    + playerString + "_" + syllableGame, thailandHasChecked12Trackers);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString
                    + syllableGame;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int b = 0; b < TILE_BUTTONS.length; b++) {
                TextView nextButton = findViewById(TILE_BUTTONS[b]);
                nextButton.setClickable(false);
                if (b == t && !choiceType.equals("WORD_IMAGE")) {
                    nextButton.setBackgroundColor(refColor);
                    nextButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                }
                if (b != t && choiceType.equals("WORD_IMAGE")) {
                    nextButton.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
                }
            }

            //JP: Added switch statement to determine which method to call: tile or word
            switch (refType) {
                case "SYLL_TEXT":
                case "SYLL_AUDIO":
                    if (hasSyllableAudio) {
                        playCorrectSoundThenActiveSyllClip();
                    } else {
                        playCorrectSound();
                    }
                    break;
                case "TILE_LOWER":
                case "TILE_UPPER":
                case "TILE_AUDIO":
                    playCorrectSoundThenActiveTileClip();
                    break;
                case "WORD_TEXT":
                case "WORD_IMAGE":
                case "WORD_AUDIO":
                    if (tempSoundPoolSwitch) {
                        playCorrectSoundThenActiveWordClip1(false);
                    } else {
                        playCorrectSoundThenActiveWordClip0(false);
                    }
            }

        } else {
            playIncorrectSound();
        }
    }


    private void chooseWord() {
        Random rand = new Random();
        int randomNum = rand.nextInt(Start.wordList.size());

        wordInLWC = Start.wordList.get(randomNum).nationalWord;
        wordInLOP = Start.wordList.get(randomNum).localWord;

    }

    public void onChoiceClick(View view) {
        respondToSelection(Integer.parseInt((String) view.getTag())); // KP
    }

    public void onRefClick(View view) {
        switch (refType) {
            case "SYLL_TEXT":
            case "SYLL_AUDIO":
                if (hasSyllableAudio) {
                    playActiveSyllClip();
                }
                break;
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "TILE_AUDIO":
                if (tempSoundPoolSwitch) {
                    playActiveTileClip1();
                } else {
                    playActiveTileClip0();
                }
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                if (tempSoundPoolSwitch) {
                    playActiveWordClip1(false); // SoundPool
                } else {
                    playActiveWordClip0(false); // MediaPlayer
                }
                break;
        }

    }

    public void playActiveTileClip() {
        if (tempSoundPoolSwitch) {
            playActiveTileClip1();
        } else {
            playActiveTileClip0();
        }
    }

    private void playActiveSyllClip() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String audioToPlay = null;
        audioToPlay = syllableList.get(syllableList.returnPositionInSyllList(refTile)).syllableAudioName;

        //since "X" represents no audio file
        if (!audioToPlay.equals("X")) {

            String tileText = null;
            tileText = syllableList.get(syllableList.returnPositionInSyllList(refTile)).syllable;

            if (syllableAudioIDs.containsKey(tileText)) {
                gameSounds.play(syllableAudioIDs.get(tileText), 1.0f, 1.0f, 2, 0, 1.0f);
            }
            soundSequencer.postDelayed(new Runnable() {
                public void run() {
                    if (repeatLocked) {
                        setAllTilesClickable();
                    }
                    setOptionsRowClickable();
                }
            }, syllableDurations.get(tileText));
        }

    }


    public void playActiveTileClip1() {     //JP: for SoundPool, for tile audio
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String audioToPlay = null;
        audioToPlay = tileList.get(tileList.returnPositionInAlphabet(refTile)).audioForTile;

        //since "X" represents no audio file
        if (!audioToPlay.equals("X")) {

            String tileText = null;
            tileText = tileList.get(tileList.returnPositionInAlphabet(refTile)).baseTile;
            if (tileAudioIDs.containsKey(tileText)) {
                gameSounds.play(tileAudioIDs.get(tileText), 1.0f, 1.0f, 2, 0, 1.0f);
            }
            soundSequencer.postDelayed(new Runnable() {
                public void run() {
                    if (repeatLocked) {
                        setAllTilesClickable();
                    }
                    setOptionsRowClickable();
                }
            }, tileDurations.get(tileText));
        }

    }


    public void playActiveTileClip0() {     //JP: for Media Player; tile audio

        String audioToPlay = null;
        audioToPlay = tileList.get(tileList.returnPositionInAlphabet(refTile)).audioForTile;

        int resID = getResources().getIdentifier(audioToPlay, "raw", getPackageName());
        MediaPlayer mp1 = MediaPlayer.create(this, resID);
        mediaPlayerIsPlaying = true;
        mp1.start();
        mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                mediaPlayerIsPlaying = false;
                if (repeatLocked) {
                    setAllTilesClickable();
                }
                setOptionsRowClickable();
                mp1.reset(); //JP: This fixes "mediaplayer went away with unhandled events" issue
                mp1.release();
            }
        });
    }

    private void playCorrectSound() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllTilesClickable();
                }
                setOptionsRowClickable();
            }
        }, 925);
    }

    private void playCorrectSoundThenActiveSyllClip() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        //JP: this delays word audio after correct sound audio so they don't overlap

        final String[] tileText = {null};
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                tileText[0] = syllableList.get(syllableList.returnPositionInSyllList(refTile)).syllable;
                if (syllableAudioIDs.containsKey(tileText[0])) {
                    gameSounds.play(syllableAudioIDs.get(tileText[0]), 1.0f, 1.0f, 1, 0, 1.0f);
                }
            }
        }, 925);
        // AH: The correct sound in 876 ms, so 925 is a hardcoded value for the current zz_correct.mp3 file

        //JP: this delays the blue arrow becoming clickable too soon, so that the word sound must be repeated again
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllTilesClickable();
                }
                setOptionsRowClickable();
            }
        }, 925 + syllableDurations.get(syllableList.get(syllableList.returnPositionInSyllList(refTile)).syllable));
    }

    public void playCorrectSoundThenActiveTileClip() {
        if (tempSoundPoolSwitch) {
            playCorrectSoundThenActiveTileClip1(); //SoundPool
        } else {
            playCorrectSoundThenActiveTileClip0(); //MediaPlayer
        }
    }

    public void playCorrectSoundThenActiveTileClip1() { //JP: Specifically for TILE audio. playCorrectSoundThenActiveWordClip is for WORDS
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        //JP: this delays word audio after correct sound audio so they don't overlap

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                String tileText = null;
                tileText = tileList.get(tileList.returnPositionInAlphabet(refTile)).baseTile;
                if (tileAudioIDs.containsKey(tileText)) {
                    gameSounds.play(tileAudioIDs.get(tileText), 1.0f, 1.0f, 1, 0, 1.0f);
                }
            }
        }, 925); //JP: having this fixed at 1200 should be fine since this is specifically for tile audio, NOT words
        // AH: The correct sound in 876 ms, so 925 is a hardcoded value for the current zz_correct.mp3 file

        //JP: this delays the blue arrow becoming clickable too soon, so that the word sound must be repeated again
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllTilesClickable();
                }
                setOptionsRowClickable();
            }
        }, 925 + tileDurations.get(tileList.get(tileList.returnPositionInAlphabet(refTile)).baseTile));
        // Above represents the hardcoded value of zz_correct.mp3 (876 ms) + duration of tile audio
    }

    public void playCorrectSoundThenActiveTileClip0() { // Media player
        MediaPlayer mp2 = MediaPlayer.create(this, R.raw.zz_correct);
        mediaPlayerIsPlaying = true;
        mp2.start();
        mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp2) {
                mp2.reset(); //JP: This fixes "mediaplayer went away with unhandled events" issue
                mp2.release();
                playActiveTileClip();
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
