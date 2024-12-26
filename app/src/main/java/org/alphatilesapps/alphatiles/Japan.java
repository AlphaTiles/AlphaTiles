package org.alphatilesapps.alphatiles;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

public class Japan extends GameActivity {
    ArrayList<TextView> currentViews = new ArrayList<>();
    ArrayList<TextView> originalViews = new ArrayList<>();
    ArrayList<Integer> linkButtonIDs = new ArrayList<>();
    HashMap<Integer, Integer> numbersToLinkButtonIDs = new HashMap<>();
    int MAX_TILES = 0;
    ArrayList<Integer> finalCorrectLinkButtonIDs = new ArrayList<>();
    protected static final int[] TILE_VIEW_IDs = {
            R.id.tile01, R.id.button1, R.id.tile02, R.id.button2, R.id.tile03, R.id.button3,
            R.id.tile04, R.id.button4, R.id.tile05, R.id.button5, R.id.tile06, R.id.button6,
            R.id.tile07, R.id.button7, R.id.tile08, R.id.button8, R.id.tile09, R.id.button9,
            R.id.tile10, R.id.button10, R.id.tile11, R.id.button11, R.id.tile12
    };

    protected static int[] ALL_GAME_VIEW_IDS;


    @Override
    protected int[] getGameButtons() {
        return ALL_GAME_VIEW_IDS;
    }

    @Override
    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());

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

        // Load in the tile view IDs first
        int gameID = 0;
        if (challengeLevel == 1) {
            setContentView(R.layout.japan_7);
            ALL_GAME_VIEW_IDS = new int[13];
            for (int i = 0; i < 13; i++) {
                ALL_GAME_VIEW_IDS[i] = TILE_VIEW_IDs[i];
            }
            MAX_TILES = 7;
            gameID = R.id.japancl_7;
        } else if (challengeLevel == 2) {
            setContentView(R.layout.japan_12);
            ALL_GAME_VIEW_IDS = new int[23];
            for (int i = 0; i < 23; i++) {
                ALL_GAME_VIEW_IDS[i] = TILE_VIEW_IDs[i];
            }
            MAX_TILES = 12;
            gameID = R.id.japancl_12;
        }

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);     // forces landscape mode only

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

        updatePointsAndTrackers(0);
        play();
    }

    private void play() {
        repeatLocked = true;
        setAdvanceArrowToGray();
        chooseWord();

        while(tileList.parseWordIntoTiles(refWord.wordInLOP, refWord).size() > MAX_TILES) {
            chooseWord();
        }

        parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
        parsedRefWordTileArray.removeAll(SAD);
        parsedRefWordSyllableArray = syllableList.parseWordIntoSyllables(refWord);
        for (Syllable syllable : parsedRefWordSyllableArray) {
            if (SAD_STRINGS.equals(syllable.text)) {
                parsedRefWordSyllableArray.remove(syllable);
            }
        }

        currentViews.clear();
        originalViews.clear();
        int linkButtonNumber = 1;
        for (int v = 0; v < parsedRefWordTileArray.size()*2-1; v++) {
            currentViews.add(findViewById(ALL_GAME_VIEW_IDS[v]));
            originalViews.add(findViewById(ALL_GAME_VIEW_IDS[v]));
            findViewById(ALL_GAME_VIEW_IDS[v]).setVisibility(View.VISIBLE);
            if (v % 2 == 1) { // link button
                numbersToLinkButtonIDs.put(linkButtonNumber, ALL_GAME_VIEW_IDS[v]);
                linkButtonIDs.add(ALL_GAME_VIEW_IDS[v]);
                linkButtonNumber++;
                findViewById(ALL_GAME_VIEW_IDS[v]).setClickable(true);
            } else {
                findViewById(ALL_GAME_VIEW_IDS[v]).setClickable(false);
            }
        }

        int gameID;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        for (int v = 1; v < ALL_GAME_VIEW_IDS.length; v++) {
            constraintSet.connect(ALL_GAME_VIEW_IDS[v - 1], ConstraintSet.END, ALL_GAME_VIEW_IDS[v],
                    ConstraintSet.START, 0); //end of game button 1 to start of link button 1
            constraintSet.connect(ALL_GAME_VIEW_IDS[v], ConstraintSet.START, ALL_GAME_VIEW_IDS[v - 1],
                    ConstraintSet.END, 0); // start of link button 1 to end of game button 1
            constraintSet.applyTo(constraintLayout);
        }


        ArrayList<Integer> tilesPerCorrectSyllable = new ArrayList<>();
        for (Syllable syllable : parsedRefWordSyllableArray) {
            Start.Word syllableWord = new Start.Word(refWord.wordInLWC, syllable.text, 0, "-", "1", "1");
            ArrayList<Tile> syllableParsedIntoTiles = tileList.parseWordIntoTiles(syllableWord.wordInLOP, syllableWord);
            tilesPerCorrectSyllable.add(syllableParsedIntoTiles.size());
            syllableParsedIntoTiles.clear();
        }

        finalCorrectLinkButtonIDs = new ArrayList<>();
        int viewIndex = 0;
        for (int numberOfTilesInThisSyllable : tilesPerCorrectSyllable) {
            viewIndex = viewIndex + numberOfTilesInThisSyllable;
            finalCorrectLinkButtonIDs.add(numbersToLinkButtonIDs.get(viewIndex)); // TODO: would ALL_BUTTON_IDs.get(viewIndex) be better?
        }

        displayRefWord();
        displayTileChoices();
        setVisibleLinkButtonsClickable();
        setTilesUnclickable();
    }

    private void displayRefWord() {
        TextView ref = findViewById(R.id.word);
        ref.setText(wordList.stripInstructionCharacters(refWord.wordInLOP));
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);
    }

    private void displayTileChoices() {

        int tileIndex = 0;
        for (int v = 0; v < currentViews.size(); v = v + 2) { // Every other view is a tile
            TextView thisTileView = findViewById(ALL_GAME_VIEW_IDS[v]);
            thisTileView.setText(parsedRefWordTileArray.get(tileIndex).text);
            thisTileView.setClickable(false);
            thisTileView.setVisibility(View.VISIBLE);
            thisTileView.setBackgroundColor(Color.parseColor(colorList.get(v % 5)));
            thisTileView.setTextColor(Color.parseColor("#FFFFFF")); // white;
            tileIndex++;
        }
        for (int v = currentViews.size(); v < ALL_GAME_VIEW_IDS.length; v++) {
            TextView tile = findViewById(ALL_GAME_VIEW_IDS[v]);
            tile.setClickable(false);
            tile.setVisibility(View.INVISIBLE);
        }

    }

    private void setVisibleLinkButtonsClickable() {
        for (int i = 1; i < currentViews.size(); i = i + 2) {
            TextView button = findViewById(ALL_GAME_VIEW_IDS[i]);
            button.setClickable(true);
        }
    }

    private void setTilesUnclickable() {
        for (int i = 0; i < ALL_GAME_VIEW_IDS.length; i = i + 2) {
            TextView tile = findViewById(ALL_GAME_VIEW_IDS[i]);
            tile.setClickable(false);
        }
    }

    public void onClickLinkButton(View view) {
        joinTiles((TextView) view);
        evaluateCombination();
    }

    public void onClickTile(View view) {
        separateTiles((TextView) view);
        evaluateCombination();
    }

    public void onClickWord(View view) {
        playActiveWordClip(false);
    }

    public void repeatGame(View view) {
        if (!repeatLocked) {
            play();
        }
    }

    private void separateTiles(TextView clickedTile) {
        // find the clicked tile in JoinedTracker
        // check if there is a button missing on either side
        // if there is, add it back in on that side

        int indexOfClickedTile = currentViews.indexOf(clickedTile);

        int gameID;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }

        if (currentViews.size() == 1) {
            // TO DO: only one tile ?
        } else if (indexOfClickedTile == 0) { // the clicked tile is the first tile
            // check index + 1
            if (!currentViews.get(1).getText().toString().equals(".")) { // if the next view is a tile, separate
                // restore the link button to the right of the clicked tile
                TextView restoredLinkButton = findViewById(ALL_GAME_VIEW_IDS[1]);
                restoredLinkButton.setVisibility(View.VISIBLE);
                restoredLinkButton.setClickable(true);

                // reapply constraints of the clicked tile and the restored link button
                TextView nextTile = findViewById(ALL_GAME_VIEW_IDS[2]);

                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(ALL_GAME_VIEW_IDS[0], ConstraintSet.END, ALL_GAME_VIEW_IDS[1],
                        ConstraintSet.START, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[1], ConstraintSet.START, ALL_GAME_VIEW_IDS[0],
                        ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[2], ConstraintSet.START, ALL_GAME_VIEW_IDS[1],
                        ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[1], ConstraintSet.END, ALL_GAME_VIEW_IDS[2], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int randomColorIndex = rand.nextInt(10);
                nextTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                randomColorIndex = rand.nextInt(10);
                clickedTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                clickedTile.setClickable(false);

                currentViews.add(1, restoredLinkButton);
            }
        } else if (indexOfClickedTile == currentViews.size()-1) { // clicked tile is the final tile
            if (!currentViews.get(indexOfClickedTile-1).getText().toString().equals(".")) { // if the prior view is a tile, separate

                // restore the link button to the left of the clicked tile
                int restoredLinkButtonIndex = originalViews.size() - 2;
                TextView restoredLinkButton = findViewById(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex]);
                restoredLinkButton.setVisibility(View.VISIBLE);
                restoredLinkButton.setClickable(true);

                // reapply constraints of clicked tile and its previous tile with the restored link button
                TextView previousTile = findViewById(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex - 1]);
                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex - 1], ConstraintSet.END,
                        ALL_GAME_VIEW_IDS[restoredLinkButtonIndex], ConstraintSet.START, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex], ConstraintSet.START,
                        ALL_GAME_VIEW_IDS[restoredLinkButtonIndex - 1], ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex + 1], ConstraintSet.START,
                        ALL_GAME_VIEW_IDS[restoredLinkButtonIndex], ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex], ConstraintSet.END,
                        ALL_GAME_VIEW_IDS[restoredLinkButtonIndex + 1], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int randomColorIndex = rand.nextInt(10);
                previousTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                randomColorIndex = rand.nextInt(10);
                clickedTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                clickedTile.setClickable(false);

                currentViews.add(indexOfClickedTile, restoredLinkButton);
            }
        } else {  // the clicked tile is a non-initial, non-final tile
            if (!currentViews.get(indexOfClickedTile - 1).getText().toString().equals(".")) { // if the prior view is a tile, separate

                int indexOfRestoredButton = originalViews.indexOf(clickedTile) - 1;
                // restore the link button
                TextView restoredButton = originalViews.get(indexOfRestoredButton);
                restoredButton.setVisibility(View.VISIBLE);
                restoredButton.setClickable(true);

                // reapply constraints of clicked tile, previous tile, and link button
                TextView previousTile = originalViews.get(indexOfRestoredButton - 1);
                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();

                constraintSet.clone(constraintLayout);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton - 1], ConstraintSet.END,
                        ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.START, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.START,
                        ALL_GAME_VIEW_IDS[indexOfRestoredButton - 1], ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton + 1], ConstraintSet.START,
                        ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.END,
                        ALL_GAME_VIEW_IDS[indexOfRestoredButton + 1], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int randomColorIndex = rand.nextInt(10);
                previousTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                randomColorIndex = rand.nextInt(10);
                clickedTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                clickedTile.setClickable(false);

                currentViews.add(indexOfClickedTile, restoredButton);

                // Check the next view after the prior link button has already been added back in
                // indexOfClickedTile is now the index of the next view
                if (!currentViews.get(indexOfClickedTile).getText().toString().equals(".")) { // if the next view is a tile, separate

                    indexOfRestoredButton = originalViews.indexOf(clickedTile) + 1;

                    // restore the button
                    restoredButton = originalViews.get(indexOfRestoredButton);
                    restoredButton.setVisibility(View.VISIBLE);
                    restoredButton.setClickable(true);

                    // reapply constraints of clicked tile, next tile, and link button
                    TextView nextTile = originalViews.get(indexOfRestoredButton + 1);
                    constraintLayout = findViewById(gameID);
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);

                    constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.END,
                            ALL_GAME_VIEW_IDS[indexOfRestoredButton + 1], ConstraintSet.START, 0);
                    constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.START,
                            ALL_GAME_VIEW_IDS[indexOfRestoredButton - 1], ConstraintSet.END, 0);
                    constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton + 1], ConstraintSet.START,
                            ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.END, 0);
                    constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredButton - 1], ConstraintSet.END,
                            ALL_GAME_VIEW_IDS[indexOfRestoredButton], ConstraintSet.START, 0);
                    constraintSet.applyTo(constraintLayout);

                    rand = new Random();
                    randomColorIndex = rand.nextInt(10);
                    nextTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                    nextTile.setClickable(false);
                    randomColorIndex = rand.nextInt(10);
                    clickedTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                    clickedTile.setClickable(false);

                    currentViews.add(indexOfClickedTile, restoredButton);
                }
            }
            // For checking the next view when the prior link button did NOT have to be restored
            else if (!currentViews.get(indexOfClickedTile + 1).getText().toString().equals(".")) { // if the next view is a tile rather than a link button

                int indexOfRestoredLinkButton = originalViews.indexOf(clickedTile) + 1;

                // restore the link button to the right of the clicked tile
                TextView restoredLinkButton = originalViews.get(indexOfRestoredLinkButton);
                restoredLinkButton.setVisibility(View.VISIBLE);
                restoredLinkButton.setClickable(true);

                // reset constraints of the clicked tile, restored link button, and next tile
                TextView nextTile = originalViews.get(indexOfRestoredLinkButton + 1);
                ConstraintLayout constraintLayout = findViewById(gameID);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);

                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton], ConstraintSet.END,
                        ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton + 1], ConstraintSet.START, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton], ConstraintSet.START,
                        ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton - 1], ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton + 1], ConstraintSet.START,
                        ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton], ConstraintSet.END, 0);
                constraintSet.connect(ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton - 1], ConstraintSet.END,
                        ALL_GAME_VIEW_IDS[indexOfRestoredLinkButton], ConstraintSet.START, 0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int randomColorIndex = rand.nextInt(10);
                nextTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                randomColorIndex = rand.nextInt(10);
                clickedTile.setBackgroundColor(Color.parseColor(colorList.get(randomColorIndex % 5)));
                clickedTile.setClickable(false);

                int newIndex = currentViews.indexOf(clickedTile) + 1;
                currentViews.add(newIndex, restoredLinkButton);
            }

        }
    }

    private String removeSADFromWordInLOP(String wordInLOP) {
        String stringToReturn = wordInLOP;
        for (String ch : SAD_STRINGS) {
            stringToReturn = stringToReturn.replaceAll("." + ch, ""); // assumes SAD tiles don't occur word-initially
        }
        return stringToReturn;
    }

    private void evaluateCombination() {
        // If a combination is correct, set the component tiles green and unclickable to solidify the user's progress

        StringBuilder currentSegmentsAppended = new StringBuilder();
        for (int v = 0; v < currentViews.size(); v++) {
            TextView view = currentViews.get(v);
            currentSegmentsAppended.append(view.getText());
        }
        String wordInLOPNoSAD = removeSADFromWordInLOP(refWord.wordInLOP);
        if (currentSegmentsAppended.toString().equals(wordInLOPNoSAD)) { // Whole word combo is correct!
            repeatLocked = false;
            setAdvanceArrowToBlue();
            playCorrectSoundThenActiveWordClip(false);
            updatePointsAndTrackers(1);
            for (int v = 0; v < ALL_GAME_VIEW_IDS.length; v++) {
                TextView view = findViewById(ALL_GAME_VIEW_IDS[v]);
                if (v % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                    view.setTextColor(Color.parseColor("#FFFFFF")); // white
                }
                view.setClickable(false);

            }
            setOptionsRowClickable();
        } else { // not all combinations correct; color code any that are

            // Check if sequence of buttons in joinedTiles anywhere matches finalCorrectLinkButtonIDs
            // If so, turn all tiles between those two buttons in joinedTiles green and make them unClickable

            // All of the odd indexes in ALL_BUTTON_IDs are buttons
            // When we find an ID in both currentViews and finalCorrectLinkButtonIDs,
            // keep iterating through currentViews and store intermediate tiles in a list
            // until you reach another link button, then check if that next button is also the next link button in
            // finalCorrectLinkButtonIDs
            // if so, go back and turn all the intermediate tiles in the list green and unclickable
            // if not, empty the list and pick a new first button and repeat the process until
            // you have iterated over all the views in currentViews

            boolean buildingIntermediate = true;
            TextView firstLinkButton = currentViews.get(0);
            ArrayList<TextView> intermediateTiles = new ArrayList<>();
            for (TextView thisView : currentViews) {
                if (linkButtonIDs.contains(thisView.getId())) {
                    if (!finalCorrectLinkButtonIDs.contains(thisView.getId())) {
                        intermediateTiles.clear();
                        buildingIntermediate = false;
                    } else if (finalCorrectLinkButtonIDs.contains(thisView.getId()) && buildingIntermediate) {
                        int secondLinkButtonIndex = finalCorrectLinkButtonIDs.indexOf(thisView.getId());
                        boolean buttonPairComplete = true; // starts off true in case this is the final syllable (therefore no pair of buttons needed)
                        if (secondLinkButtonIndex > 0) {
                            buttonPairComplete = finalCorrectLinkButtonIDs.get(secondLinkButtonIndex - 1).equals(firstLinkButton.getId());
                        }
                        if (intermediateTiles.size()!=parsedRefWordTileArray.size() && buttonPairComplete) { // prevent all tiles from turning green if combos are wrong
                            for (TextView tileView : intermediateTiles) {
                                tileView.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                                tileView.setTextColor(Color.parseColor("#FFFFFF")); // white
                                tileView.setClickable(false);
                            }
                            thisView.setClickable(false); // Set the link button at the end of the combination unclickable
                            firstLinkButton.setClickable(false); // Set the link button (or tile if index 0) at the beginning of the combination unclickable
                        }
                    } else if (finalCorrectLinkButtonIDs.contains(thisView.getId())) {
                        buildingIntermediate = true;
                        firstLinkButton = thisView;
                    }
                } else { // thisView is a tile
                    if (buildingIntermediate) {
                        intermediateTiles.add(thisView);
                    }
                }
            }

        }
    }

    private void joinTiles(TextView linkButton) {

        linkButton.setClickable(false);
        linkButton.setVisibility(View.INVISIBLE);

        int linkButtonIndex = originalViews.indexOf(linkButton);
        TextView leftTile = originalViews.get(linkButtonIndex - 1);
        leftTile.setClickable(true);

        TextView rightTile = originalViews.get(linkButtonIndex + 1);
        rightTile.setClickable(true);

        int gameID;
        if (challengeLevel == 1) {
            gameID = R.id.japancl_7;
        } else {
            gameID = R.id.japancl_12;
        }

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        // start of right tile to end of left tile
        constraintSet.connect(ALL_GAME_VIEW_IDS[linkButtonIndex - 1], ConstraintSet.END,
                ALL_GAME_VIEW_IDS[linkButtonIndex + 1], ConstraintSet.START, 0);
        // end of left tile to start of right tile
        constraintSet.connect(ALL_GAME_VIEW_IDS[linkButtonIndex + 1], ConstraintSet.START,
                ALL_GAME_VIEW_IDS[linkButtonIndex - 1], ConstraintSet.END, 0);
        constraintSet.applyTo(constraintLayout);

        currentViews.remove(linkButton);

    }
}