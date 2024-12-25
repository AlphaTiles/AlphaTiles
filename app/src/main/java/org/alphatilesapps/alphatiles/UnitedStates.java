package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.graphics.Typeface;
import android.widget.Button;

import static org.alphatilesapps.alphatiles.Start.*;

/*LEVELS:

 */

public class UnitedStates extends GameActivity {

    int wordLengthLimitInTiles = 5;
    int neutralFontSize;
    String[] selections = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}; // KP

    int numberOfPairs;

    boolean[] pairHasSelection = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    ArrayList<Tile> tileOptions = new ArrayList<>();
    Tile[] tileSelections;

    protected static final int[] GAME_BUTTONS = {
            R.id.button01a, R.id.button01b, R.id.button02a, R.id.button02b, R.id.button03a, R.id.button03b, R.id.button04a, R.id.button04b, R.id.button05a, R.id.button05b,
            R.id.button06a, R.id.button06b, R.id.button07a, R.id.button07b, R.id.button08a, R.id.button08b, R.id.button09a, R.id.button09b
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
    protected void centerGamesHomeImage() {
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = 0;
        switch (challengeLevel) {
            case 1:
                gameID = R.id.united_states_cl1_CL;
                break;
            case 2:
                gameID = R.id.united_states_cl2_CL;
                break;
            case 3:
                gameID = R.id.united_states_cl3_CL;
                break;
            default:
                break;
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
        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        int gameID = 0;
        switch (challengeLevel) {
            case 2:
                setContentView(R.layout.united_states_cl2);
                wordLengthLimitInTiles = 7;
                neutralFontSize = 24;
                gameID = R.id.united_states_cl2_CL;
                break;
            case 3:
                setContentView(R.layout.united_states_cl3);
                wordLengthLimitInTiles = 9;
                neutralFontSize = 18;
                gameID = R.id.united_states_cl3_CL;
                break;
            default:
                setContentView(R.layout.united_states_cl1);
                wordLengthLimitInTiles = 5;
                neutralFontSize = 30;
                gameID = R.id.united_states_cl1_CL;
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
            centerGamesHomeImage();
        }

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
        selections = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}; // KP
        pairHasSelection = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
        int parsedLengthOfRefWord = Integer.MAX_VALUE;

        while(parsedLengthOfRefWord > wordLengthLimitInTiles) {
            chooseWord();
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
            parsedLengthOfRefWord = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord).size();
        }

        // Set up additional structures
        if (syllableGame.equals("S")) {
            parsedRefWordSyllableArray = syllableList.parseWordIntoSyllables(refWord);
            parsedLengthOfRefWord = parsedRefWordSyllableArray.size();
        } else {
            Tile emptyTile = new Tile("__", new ArrayList<String>(), "", "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, "", 0, "");
            tileSelections = new Tile[parsedLengthOfRefWord];
            for (int t = 0; t<parsedLengthOfRefWord; t++) {
                tileSelections[t] = new Tile(emptyTile);
            }
            tileOptions.clear();
        }

        // added by Camden. Delete if this does not work!
        numberOfPairs = parsedLengthOfRefWord;

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        ImageView wordImage = (ImageView) findViewById(R.id.wordImage);
        wordImage.setClickable(true);

        switch (challengeLevel) {
            case 2:
                visibleGameButtons = 14;   // RR
                break;
            case 3:
                visibleGameButtons = 18;   // RR
                break;
            default:
                visibleGameButtons = 10;   // RR
        }

        int parseIndex = 0;

        for (int buttonPair = 0; buttonPair < visibleGameButtons; buttonPair += 2) {

            Button gameButtonA = (Button) findViewById(GAME_BUTTONS[buttonPair]);
            Button gameButtonB = (Button) findViewById(GAME_BUTTONS[buttonPair + 1]);

            String tileColorStr = colorList.get((buttonPair % 5) / 2);
            int tileColor = Color.parseColor(tileColorStr);
            gameButtonA.setBackgroundColor(tileColor);
            gameButtonB.setBackgroundColor(tileColor);
            gameButtonA.setTextColor(Color.parseColor("#FFFFFF")); // white
            gameButtonB.setTextColor(Color.parseColor("#FFFFFF")); // white

            gameButtonA.setClickable(true);
            gameButtonB.setClickable(true);

            if (parseIndex < parsedLengthOfRefWord) {
                Random rand = new Random();
                int randomlyCorrectStringGoesBelow = rand.nextInt(2); // Choose whether correct tile goes above ( =0 ) or below ( =1 )
                int randomDistractor = rand.nextInt(Start.ALT_COUNT); // KP // Choose which distractor will be the alternative
                if (randomlyCorrectStringGoesBelow == 0) { // Correct string goes above
                    if (syllableGame.equals("S") && !SAD_STRINGS.contains(parsedRefWordSyllableArray.get(parseIndex).text)) {
                        gameButtonA.setText(parsedRefWordSyllableArray.get(parseIndex).text);
                        gameButtonB.setText(parsedRefWordSyllableArray.get(parseIndex).distractors.get(randomDistractor));
                    } else {
                        gameButtonA.setText(parsedRefWordTileArray.get(parseIndex).text);
                        gameButtonB.setText(parsedRefWordTileArray.get(parseIndex).distractors.get(randomDistractor));
                        tileOptions.add(parsedRefWordTileArray.get(parseIndex));
                        tileOptions.add(tileHashMap.find(parsedRefWordTileArray.get(parseIndex).distractors.get(randomDistractor)));
                    }
                } else { // Correct string goes below
                    if (syllableGame.equals("S") && !SAD_STRINGS.contains(parsedRefWordSyllableArray.get(parseIndex).text)) {
                        gameButtonB.setText(parsedRefWordSyllableArray.get(parseIndex).text);
                        gameButtonA.setText(parsedRefWordSyllableArray.get(parseIndex).distractors.get(randomDistractor));
                    } else {
                        gameButtonB.setText(parsedRefWordTileArray.get(parseIndex).text);
                        gameButtonA.setText(parsedRefWordTileArray.get(parseIndex).distractors.get(randomDistractor));
                        tileOptions.add(tileHashMap.find(parsedRefWordTileArray.get(parseIndex).distractors.get(randomDistractor)));
                        tileOptions.add(parsedRefWordTileArray.get(parseIndex));
                    }
                }
                gameButtonA.setVisibility(View.VISIBLE);
                gameButtonB.setVisibility(View.VISIBLE);

            } else {
                gameButtonA.setText("");
                gameButtonB.setText("");
                gameButtonA.setVisibility(View.INVISIBLE);
                gameButtonB.setVisibility(View.INVISIBLE);
            }
            parseIndex++;

        }
        //TextView constructedWord = findViewById(R.id.activeWordTextView); // KP
        //constructedWord.setText(""); // KP

        TextView constructedWord = findViewById(R.id.activeWordTextView);
        String initialDisplay = "";
        for (int i = 0; i < numberOfPairs; i++)
            initialDisplay += "__";
        constructedWord.setText(initialDisplay);

        setAllGameButtonsClickable();
    }

    public void buildWord(int tileIndex) {

        // added by Camden. Remove if this does not work!
        pairHasSelection[tileIndex / 2] = true;

        TextView constructedWord = findViewById(R.id.activeWordTextView);
        int lastSelectedIndex;
        if (tileIndex % 2 == 0) {
            lastSelectedIndex = tileIndex / 2;
        } else {
            lastSelectedIndex = (tileIndex - 1) / 2;
        }

        String displayedWord;
        if (syllableGame.equals("S")){
           StringBuilder stringBuilder = new StringBuilder();

           int index = 0;
           for (int i = 0; i < numberOfPairs; i++) {
               if (pairHasSelection[i]) {
                   stringBuilder.append(selections[2 * i]);
                   stringBuilder.append(selections[2 * i + 1]);
               } else {
                   stringBuilder.append("__");
               }
           }

           /*
           for (String s : selections) {
               stringBuilder.append(s);
           }
           */

            displayedWord = stringBuilder.toString();
            constructedWord.setText(displayedWord);
        } else {
            Tile[] tilesSelectedArray = new Tile[numberOfPairs];
            ArrayList<Tile> tilesSelected = new ArrayList<>();
            for (Tile tile : tileSelections) {
                tilesSelected.add(tile);
            }
            displayedWord = combineTilesToMakeWord(tilesSelected, refWord, lastSelectedIndex);
            constructedWord.setText(displayedWord);
        }


        if (displayedWord.equals(wordInLOPWithStandardizedSequenceOfCharacters(refWord))) {
            // Good job!
            repeatLocked = false;
            setAdvanceArrowToBlue();
            constructedWord.setTextColor(Color.parseColor("#006400")); // dark green
            constructedWord.setTypeface(constructedWord.getTypeface(), Typeface.BOLD);

            updatePointsAndTrackers(2);

            for (int i = 0; i < visibleGameButtons; i++) {
                TextView gameTile = findViewById(GAME_BUTTONS[i]);
                gameTile.setClickable(false);
            }

            playCorrectSoundThenActiveWordClip(false);
        } else {
            constructedWord.setTextColor(Color.BLACK);
            constructedWord.setTypeface(constructedWord.getTypeface(), Typeface.NORMAL);
        }
    }

    public void onBtnClick(View view) {
        int justClickedGameButton = Integer.parseInt((String) view.getTag());
        int selectionIndex = justClickedGameButton - 1; //  justClickedGameButton uses 1 to 10 (or 14 or 18), tileNo uses the array ID (between [0] and [9] (or [13] or [17])
        int otherOptionIndex; // the corresponding gameButton that is above or below the justClickedGameButton
        if (justClickedGameButton % 2 == 0) {
            otherOptionIndex = selectionIndex - 1;
        } else {
            otherOptionIndex = selectionIndex + 1;
        }

        Button gameButton = findViewById(GAME_BUTTONS[selectionIndex]);
        Button otherGameButton = findViewById(GAME_BUTTONS[otherOptionIndex]);
        selections[selectionIndex] = gameButton.getText().toString();
        selections[otherOptionIndex] = "";
        if(syllableGame.equals("T")) {
            tileSelections[selectionIndex/2] = tileOptions.get(selectionIndex);
        }

        String tileColorStr = colorList.get((selectionIndex / 2) % 5);
        int tileColorNo = Color.parseColor(tileColorStr);
        gameButton.setBackgroundColor(tileColorNo);
        gameButton.setTextColor(Color.parseColor("#FFFFFF")); // white

        String tileColorStr2 = "#A9A9A9"; // dark gray
        int tileColorNo2 = Color.parseColor(tileColorStr2);
        otherGameButton.setBackgroundColor(tileColorNo2);
        otherGameButton.setTextColor(Color.parseColor("#000000")); // black

        buildWord(selectionIndex);

    }

    protected void setAllGameButtonsUnclickable() {
        for (int t = 0; t < wordLengthLimitInTiles; t++) {
            TextView gameTile = findViewById(getGameButtons()[t]);
            gameTile.setClickable(false);
        }
    }

    protected void setAllGameButtonsClickable() {

        for (int t = 0; t < wordLengthLimitInTiles; t++) {
            TextView gameTile = findViewById(getGameButtons()[t]);
            gameTile.setClickable(true);
        }
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
