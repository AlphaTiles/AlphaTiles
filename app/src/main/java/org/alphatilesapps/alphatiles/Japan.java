package org.alphatilesapps.alphatiles;

import androidx.constraintlayout.widget.ConstraintLayout;

import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

public class Japan extends GameActivity {
    ArrayList<TextView> currentViews = new ArrayList<>();
    ArrayList<TextView> originalViews = new ArrayList<>();
    ArrayList<Integer> linkButtonIDs = new ArrayList<>();
    HashMap<Integer, Integer> numbersToLinkButtonIDs = new HashMap<>();

    int MAX_TILES = 10;
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

    private static final int[][] MAPPINGS_JAPAN = {
            // Common Horizontal Guidelines
            {R.id.horGuidelineStatusTop, R.dimen.horGuidelineStatusTop},
            {R.id.horGuidelineStatusMiddle, R.dimen.horGuidelineStatusMiddle},
            {R.id.horGuidelineStatusBottom, R.dimen.horGuidelineStatusBottom},
            {R.id.horGuidelineOptionsTop, R.dimen.horGuidelineOptionsTop},
            {R.id.horGuidelineOptionsBottom, R.dimen.horGuidelineOptionsBottom},

            // Specific Horizontal Guidelines
            {R.id.japan_horGuidelineRefTop, R.dimen.japan_horGuidelineRefTop},
            {R.id.japan_horGuidelineRefBottom, R.dimen.japan_horGuidelineRefBottom},
            {R.id.japan_horGuidelineTextTop, R.dimen.japan_horGuidelineTextTop},
            {R.id.japan_horGuidelineTextBottom, R.dimen.japan_horGuidelineTextBottom},
            {R.id.japan_horGuidelineTileTop, R.dimen.japan_horGuidelineTileTop},
            {R.id.japan_horGuidelineTileBottom, R.dimen.japan_horGuidelineTileBottom},

            // Common Vertical Guidelines
            {R.id.verGuidelineGameNoLeft, R.dimen.verGuidelineGameNoLeft},
            {R.id.verGuidelineGameNoCLBorder, R.dimen.verGuidelineGameNoCLBorder},
            {R.id.verGuidelineCLStageBorder, R.dimen.verGuidelineCLStageBorder},
            {R.id.verGuidelineStageBarsBorder, R.dimen.verGuidelineStageBarsBorder},
            {R.id.verGuidelineBarsPointsBorder, R.dimen.verGuidelineBarsPointsBorder},
            {R.id.verGuidelinePointsRight, R.dimen.verGuidelinePointsRight},
            {R.id.verGuidelineOptionsLeft, R.dimen.verGuidelineOptionsLeft},
            {R.id.verGuidelineOptionsRight, R.dimen.verGuidelineOptionsRight},

            // Specific Vertical Guidelines
            {R.id.japan_verGuidelineRefLeft, R.dimen.japan_verGuidelineRefLeft},
            {R.id.japan_verGuidelineRefRight, R.dimen.japan_verGuidelineRefRight},
            {R.id.japan_verGuidelineTextLeft, R.dimen.japan_verGuidelineTextLeft},
            {R.id.japan_verGuidelineTextRight, R.dimen.japan_verGuidelineTextRight},
            {R.id.japan_verGuidelineTileLeft, R.dimen.japan_verGuidelineTileLeft},
            {R.id.japan_verGuidelineTileRight, R.dimen.japan_verGuidelineTileRight}
    };

    private void updateGuidelines() {
        View rootView = findViewById(android.R.id.content);
        GuidelineUtils.applyGuidelines(rootView, this, MAPPINGS_JAPAN);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGuidelines();
        // The layout-change listener added in onCreate re-runs relayoutViews() once the new size is known
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.japan);
        ALL_GAME_VIEW_IDS = new int[23];
        for (int i = 0; i < 23; i++) {
            ALL_GAME_VIEW_IDS[i] = TILE_VIEW_IDs[i];
        }
        int gameID = R.id.japanCL;

        updateGuidelines();

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        // Re-run the tile row layout whenever the game layout changes size (first layout, rotation, etc.)
        findViewById(getGameLayoutId()).addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if ((right - left) != (oldRight - oldLeft) || (bottom - top) != (oldBottom - oldTop)) {
                        v.post(this::relayoutViews);
                    }
                });

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

        playAgain();
        setUpInitialView();
        updateView();
    }

    private void playAgain() {
        repeatLocked = true;
        setAdvanceArrowToGray();

        // Detect orientation each round: landscape allows words up to 10 tiles, portrait up to 5
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MAX_TILES = 10;
        } else {
            MAX_TILES = 5;
        }

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
        relayoutViews();
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
        relayoutViews();
        evaluateCombination();
    }

    public void onClickTile(View view) {
        separateTiles((TextView) view);
        relayoutViews();
        evaluateCombination();
    }

    public void onClickWord(View view) {
        playActiveWordClip(false);
    }

    public void repeatGame(View view) {
        if (!repeatLocked) {
            playAgain();
        }
    }

    // Positioning is no longer done here: relayoutViews() (called after every tap) sets the size and
    // position of every view in the row. These methods only restore/hide link buttons, update
    // currentViews, and recolor tiles.
    private void separateTiles(TextView clickedTile) {
        // find the clicked tile in JoinedTracker
        // check if there is a button missing on either side
        // if there is, add it back in on that side

        int indexOfClickedTile = currentViews.indexOf(clickedTile);

        if (currentViews.size() == 1) {
            // TO DO: only one tile ?
        } else if (indexOfClickedTile == 0) { // the clicked tile is the first tile
            // check index + 1
            if (!currentViews.get(1).getText().toString().equals(".")) { // if the next view is a tile, separate
                // restore the link button to the right of the clicked tile
                TextView restoredLinkButton = findViewById(ALL_GAME_VIEW_IDS[1]);
                restoredLinkButton.setVisibility(View.VISIBLE);
                restoredLinkButton.setClickable(true);

                TextView nextTile = findViewById(ALL_GAME_VIEW_IDS[2]);

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

                TextView previousTile = findViewById(ALL_GAME_VIEW_IDS[restoredLinkButtonIndex - 1]);

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

                TextView previousTile = originalViews.get(indexOfRestoredButton - 1);

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

                    TextView nextTile = originalViews.get(indexOfRestoredButton + 1);

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

                TextView nextTile = originalViews.get(indexOfRestoredLinkButton + 1);

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
            playGameSoundThenActiveWordClip(true,false);
            recordAttempt(true,1);
            for (int v = 0; v < ALL_GAME_VIEW_IDS.length; v++) {
                TextView view = findViewById(ALL_GAME_VIEW_IDS[v]);
                if (v % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#006600")); // dark green
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
                                tileView.setBackgroundColor(Color.parseColor("#006600")); // dark green
                                tileView.setTextColor(Color.parseColor("#FFFFFF")); // white
                                tileView.setClickable(false);
                            }
                            thisView.setClickable(false); // Set the link button at the end of the combination unclickable
                            firstLinkButton.setClickable(false); // Set the link button (or tile if index 0) at the beginning of the combination unclickable
                        }

                        // This button is a syllable boundary, so the next syllable starts here whether or not
                        // the previous one was complete. Reset so each syllable is judged on its own tiles.
                        firstLinkButton = thisView;
                        intermediateTiles.clear();
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

            // The last syllable has no link button after it, so the loop never evaluates it.
            // The final entry of finalCorrectLinkButtonIDs marks the end of the word (it is not a real
            // link button), so the last real syllable boundary is the second-to-last entry.
            int lastBoundaryIndex = finalCorrectLinkButtonIDs.size() - 2;
            if (buildingIntermediate && lastBoundaryIndex >= 0
                && !intermediateTiles.isEmpty()
                && intermediateTiles.size() != parsedRefWordTileArray.size()
                && finalCorrectLinkButtonIDs.get(lastBoundaryIndex).equals(firstLinkButton.getId())) {
                    for (TextView tileView : intermediateTiles) {
                        tileView.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                        tileView.setTextColor(Color.parseColor("#FFFFFF")); // white
                        tileView.setClickable(false);
                    }
                    firstLinkButton.setClickable(false); // Set the link button at the start of the last syllable unclickable
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

        currentViews.remove(linkButton);

    }

    private int getGameLayoutId() {
        return R.id.japanCL;
    }

    private float getGuidePercent(int guidelineId) {
        View guideline = findViewById(guidelineId);
        return ((ConstraintLayout.LayoutParams) guideline.getLayoutParams()).guidePercent;
    }

    /**
     * Sizes and positions the whole tile / link button row as one gap-free, centered strip.
     *
     * Every view in ALL_GAME_VIEW_IDS is given:
     *   - a fixed square size (same width and height for tiles and link buttons)
     *   - ALL of its old constraints removed (chains, neighbor links, guideline links, ratio, bias)
     *   - exactly two constraints: start-to-parent-start and top-to-parent-top, with explicit
     *     pixel margins
     *
     * So the position of view i is simply x = rowStart + i * size. Because every view is exactly
     * `size` wide, view i's right edge is exactly view i+1's left edge: no gaps, no overlap. Nothing
     * the XML or the old click handlers set can interfere, because every constraint is replaced here.
     *
     * Size is based on the word's full slot count (2n - 1), so short words get big tiles and long
     * words get small tiles. When link buttons are joined, the row gets narrower and re-centers.
     */
    private void relayoutViews() {
        ConstraintLayout layout = findViewById(getGameLayoutId());
        if (layout == null || currentViews.isEmpty() || parsedRefWordTileArray == null) {
            return;
        }

        // Percent guidelines are measured inside the layout's padding
        int contentWidth = layout.getWidth() - layout.getPaddingLeft() - layout.getPaddingRight();
        int contentHeight = layout.getHeight() - layout.getPaddingTop() - layout.getPaddingBottom();
        if (contentWidth <= 0 || contentHeight <= 0) {
            return; // not laid out yet; the layout-change listener calls this again after the first layout
        }

        float spanLeft = getGuidePercent(R.id.japan_verGuidelineTileLeft) * contentWidth;
        float spanRight = getGuidePercent(R.id.japan_verGuidelineTileRight) * contentWidth;
        float spanTop = getGuidePercent(R.id.japan_horGuidelineTileTop) * contentHeight;
        float spanBottom = getGuidePercent(R.id.japan_horGuidelineTileBottom) * contentHeight;
        float spanWidth = spanRight - spanLeft;
        float spanHeight = spanBottom - spanTop;

        int fullSlotCount = Math.max(1, parsedRefWordTileArray.size() * 2 - 1);
        int size = (int) Math.floor(Math.min(spanWidth / fullSlotCount, spanHeight));
        if (size <= 0) {
            return;
        }

        int count = currentViews.size();
        int rowStartX = Math.round(spanLeft + (spanWidth - (float) size * count) / 2f); // centered row
        int rowTopY = Math.round(spanTop + (spanHeight - size) / 2f);                  // centered in the band

        for (int id : ALL_GAME_VIEW_IDS) {
            View view = findViewById(id);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();

            // Remove every constraint the XML / earlier code put on this view
            lp.leftToLeft = ConstraintLayout.LayoutParams.UNSET;
            lp.leftToRight = ConstraintLayout.LayoutParams.UNSET;
            lp.rightToLeft = ConstraintLayout.LayoutParams.UNSET;
            lp.rightToRight = ConstraintLayout.LayoutParams.UNSET;
            lp.startToStart = ConstraintLayout.LayoutParams.UNSET;
            lp.startToEnd = ConstraintLayout.LayoutParams.UNSET;
            lp.endToStart = ConstraintLayout.LayoutParams.UNSET;
            lp.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            lp.topToTop = ConstraintLayout.LayoutParams.UNSET;
            lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
            lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
            lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            lp.baselineToBaseline = ConstraintLayout.LayoutParams.UNSET;
            lp.dimensionRatio = null;
            lp.horizontalBias = 0f;
            lp.verticalBias = 0f;
            lp.horizontalWeight = 0f;
            lp.verticalWeight = 0f;

            // Identical fixed square size for every tile and link button
            lp.width = size;
            lp.height = size;

            // Slot index in the row (views that are not showing are parked at the row start, invisible)
            int index = currentViews.indexOf(view);
            int x = rowStartX + (index < 0 ? 0 : index) * size;

            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.leftMargin = 0;
            lp.rightMargin = 0;
            lp.setMarginStart(x);
            lp.setMarginEnd(0);
            lp.topMargin = rowTopY;
            lp.bottomMargin = 0;

            view.setLayoutParams(lp);
        }
    }
}