package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

/**
 * Cameroon – Segmenting Game
 *
 * Screen layout (top → bottom):
 *   [status/tracker bar]
 *   wordImage  (upper third, VISIBLE or INVISIBLE per CL digit 1)
 *   wordText   (immediately below wordImage)
 *   ── lower grid: 4 rows × 7 columns ──
 *   col 0      : selector buttons (yellow circle → green circle on correct)
 *   cols 1–6   : tile-audio buttons (one per tile position; extras INVISIBLE)
 *   [bottom nav: home | instructions | advance arrow]
 *
 * Challenge levels (3-digit encoding):
 *   Digit 1 (hundreds):  1__ = wordImage VISIBLE,   2__ = wordImage INVISIBLE
 *   Digit 2 (tens):      _1_ = substitute ONE tile,  _2_ = substitute ALL tiles
 *   Digit 3 (units):     __1 = random tiles,         __2 = distractor tiles
 *
 *   Valid levels: 111, 112, 121, 122, 211, 212, 221, 222
 *
 * Tile-audio lookup follows Sudan's pattern:
 *   tileAudioIDs.get(tile.audioForThisTileType)
 *   tileDurations.get(tile.audioForThisTileType)
 *
 * Wrong-row and correct-row feedback follows Brazil/Peru patterns.
 */
public class Cameroon extends GameActivity {

    // ── constants ──────────────────────────────────────────────────────────────

    /** Number of rows in the answer grid. */
    private static final int NUM_ROWS = 4;
    /** Max number of tile-audio columns (cols 1–6). */
    private static final int MAX_TILES = 6;
    /** Total game buttons exposed to GameActivity (selectors + tile-audio = 4 + 24 = 28).
     *  We register ALL of them so setAllGameButtonsUnclickable works correctly. */
    protected static final int[] GAME_BUTTONS = {
            // Row 0
            R.id.selector0,
            R.id.tileBtn_0_0, R.id.tileBtn_0_1, R.id.tileBtn_0_2,
            R.id.tileBtn_0_3, R.id.tileBtn_0_4, R.id.tileBtn_0_5,
            // Row 1
            R.id.selector1,
            R.id.tileBtn_1_0, R.id.tileBtn_1_1, R.id.tileBtn_1_2,
            R.id.tileBtn_1_3, R.id.tileBtn_1_4, R.id.tileBtn_1_5,
            // Row 2
            R.id.selector2,
            R.id.tileBtn_2_0, R.id.tileBtn_2_1, R.id.tileBtn_2_2,
            R.id.tileBtn_2_3, R.id.tileBtn_2_4, R.id.tileBtn_2_5,
            // Row 3
            R.id.selector3,
            R.id.tileBtn_3_0, R.id.tileBtn_3_1, R.id.tileBtn_3_2,
            R.id.tileBtn_3_3, R.id.tileBtn_3_4, R.id.tileBtn_3_5
    };

    // Selector button IDs (col 0, one per row)
    private static final int[] SELECTOR_IDS = {
            R.id.selector0, R.id.selector1, R.id.selector2, R.id.selector3
    };

    // Tile-audio button IDs [row][tilePos]
    private static final int[][] TILE_BTN_IDS = {
            { R.id.tileBtn_0_0, R.id.tileBtn_0_1, R.id.tileBtn_0_2,
              R.id.tileBtn_0_3, R.id.tileBtn_0_4, R.id.tileBtn_0_5 },
            { R.id.tileBtn_1_0, R.id.tileBtn_1_1, R.id.tileBtn_1_2,
              R.id.tileBtn_1_3, R.id.tileBtn_1_4, R.id.tileBtn_1_5 },
            { R.id.tileBtn_2_0, R.id.tileBtn_2_1, R.id.tileBtn_2_2,
              R.id.tileBtn_2_3, R.id.tileBtn_2_4, R.id.tileBtn_2_5 },
            { R.id.tileBtn_3_0, R.id.tileBtn_3_1, R.id.tileBtn_3_2,
              R.id.tileBtn_3_3, R.id.tileBtn_3_4, R.id.tileBtn_3_5 }
    };

    // ── challenge-level decoded fields ─────────────────────────────────────────
    /** 1 = image visible, 2 = image invisible */
    private int clImageMode;
    /** 1 = substitute one tile, 2 = substitute all tiles */
    private int clSubstMode;
    /** 1 = random substitution, 2 = distractor substitution */
    private int clTileMode;

    // ── round state ────────────────────────────────────────────────────────────
    /** Index (0–3) of the row that holds the correct answer. */
    private int correctRowIndex;
    /**
     * rowTiles[row] holds the ArrayList<Tile> for that row.
     * Row correctRowIndex = parsedRefWordTileArray (the real word).
     * Other rows = nonce words derived from it.
     */
    private final List<ArrayList<Start.Tile>> rowTiles = new ArrayList<>();

    // ── GameActivity overrides ─────────────────────────────────────────────────

    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
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
            audioInstructionsResID = res.getIdentifier(
                    Start.gameList.get(gameNumber - 1).instructionAudioName,
                    "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void hideInstructionAudioImage() {
        ImageView instructionsButton = findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);
    }

    // ── lifecycle ──────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.cameroon);

        ActivityLayouts.applyEdgeToEdge(this, R.id.cameroonCL);
        ActivityLayouts.setStatusAndNavColors(this);

        // Decode 3-digit challenge level
        decodeChallengeLevel();

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView repeatImage = findViewById(R.id.repeatImage);
            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            fixConstraintsRTL(R.id.cameroonCL);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        visibleGameButtons = GAME_BUTTONS.length;
        updatePointsAndTrackers(0);

        incorrectAnswersSelected = new ArrayList<>(NUM_ROWS - 1);
        for (int i = 0; i < NUM_ROWS - 1; i++) {
            incorrectAnswersSelected.add("");
        }

        playAgain();
    }

    // ── challenge-level decoding ───────────────────────────────────────────────

    /**
     * Decodes the 3-digit challenge level stored in {@code challengeLevel}.
     * E.g. challengeLevel == 112 → clImageMode=1, clSubstMode=1, clTileMode=2.
     */
    private void decodeChallengeLevel() {
        clImageMode  = challengeLevel / 100;          // hundreds digit
        clSubstMode  = (challengeLevel % 100) / 10;  // tens digit
        clTileMode   = challengeLevel % 10;           // units digit
    }

    // ── round setup ────────────────────────────────────────────────────────────

    /** Public entry-point called by the repeat / advance arrow. */
    public void repeatGame(View view) {
        if (!repeatLocked) {
            playAgain();
        }
    }

    public void playAgain() {
        repeatLocked = true;
        setAdvanceArrowToGray();

        // Choose a word; reject if tile length >= 7
        do {
            chooseWord();
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
        } while (parsedRefWordTileArray.size() >= 7);

        int tileLength = parsedRefWordTileArray.size();

        // Apply wordImage visibility
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);
        image.setClickable(true);

        if (clImageMode == 1) {
            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.INVISIBLE);
        }

        // Set wordText
        TextView wordText = findViewById(R.id.wordText);
        wordText.setText(wordInLOPWithStandardizedSequenceOfCharacters(refWord));
        wordText.setClickable(true);

        // Build the 4 rows: one correct + three nonce words
        buildRows(tileLength);

        // Populate the UI grid
        populateGrid(tileLength);

        // Reset selectors to yellow
        for (int row = 0; row < NUM_ROWS; row++) {
            setSelectorColor(row, false);
        }

        // Make everything clickable
        for (int row = 0; row < NUM_ROWS; row++) {
            findViewById(SELECTOR_IDS[row]).setClickable(true);
            for (int col = 0; col < tileLength; col++) {
                TextView btn = findViewById(TILE_BTN_IDS[row][col]);
                btn.setClickable(true);
            }
        }

        // Reset trackers
        for (int i = 0; i < NUM_ROWS - 1; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();

        // Play the word audio at round start
        playActiveWordClip(false);
    }

    // ── row / nonce-word construction ─────────────────────────────────────────

    /**
     * Builds {@code rowTiles}: correct row at {@code correctRowIndex},
     * three nonce rows generated according to the challenge-level sub-modes.
     */
    private void buildRows(int tileLength) {
        rowTiles.clear();
        for (int i = 0; i < NUM_ROWS; i++) {
            rowTiles.add(null);
        }

        Random rand = new Random();
        correctRowIndex = rand.nextInt(NUM_ROWS);

        // Place the correct word
        rowTiles.set(correctRowIndex, new ArrayList<>(parsedRefWordTileArray));

        // Build three nonce words
        int nonceRow = 0;
        for (int row = 0; row < NUM_ROWS; row++) {
            if (row == correctRowIndex) continue;

            ArrayList<Start.Tile> nonce = buildNonceWord(tileLength, rand);
            rowTiles.set(row, nonce);
            nonceRow++;
        }
    }

    /**
     * Builds one nonce word from parsedRefWordTileArray according to:
     * <ul>
     *   <li>clSubstMode 1 → replace ONE randomly chosen tile</li>
     *   <li>clSubstMode 2 → replace ALL tiles</li>
     *   <li>clTileMode  1 → replacement from random tiles of same type</li>
     *   <li>clTileMode  2 → replacement from distractor tiles</li>
     * </ul>
     */
    private ArrayList<Start.Tile> buildNonceWord(int tileLength, Random rand) {
        boolean isDuplicate = true;
        ArrayList<Start.Tile> nonce = null;

        while (isDuplicate) {
            nonce = new ArrayList<>(parsedRefWordTileArray);

            if (clSubstMode == 1) {
                // Replace ONE random position
                int idx = rand.nextInt(tileLength);
                Start.Tile replacement = getReplacementTile(nonce.get(idx), rand);
                if (replacement != null) {
                    replacement.typeOfThisTileInstance = nonce.get(idx).typeOfThisTileInstance;
                    nonce.set(idx, replacement);
                }
            } else {
                // Replace ALL positions
                for (int i = 0; i < tileLength; i++) {
                    Start.Tile replacement = getReplacementTile(nonce.get(i), rand);
                    if (replacement != null) {
                        replacement.typeOfThisTileInstance = nonce.get(i).typeOfThisTileInstance;
                        nonce.set(i, replacement);
                    }
                }
            }

            // Check it is different from the correct word
            String nonceStr = combineTilesToMakeWord(nonce, refWord, -1);
            String correctStr = wordInLOPWithStandardizedSequenceOfCharacters(refWord);
            isDuplicate = nonceStr.equals(correctStr);

            // Also guard against dangerous Arabic ligature
            for (int j = 0; j < nonceStr.length() - 2; j++) {
                if (nonceStr.startsWith("للہ", j)) {
                    isDuplicate = true;
                    break;
                }
            }
        }
        return nonce;
    }

    /**
     * Returns a single replacement tile for {@code original}.
     * Mode 1 (random): picks a random tile of the same broad type.
     * Mode 2 (distractor): picks from the tile's distractor list.
     */
    private Start.Tile getReplacementTile(Start.Tile original, Random rand) {
        if (clTileMode == 1) {
            // Random tile of same broad type
            String type = original.typeOfThisTileInstance;
            if (VOWELS.contains(original)) {
                int idx = rand.nextInt(VOWELS.size());
                return new Start.Tile(VOWELS.get(idx));
            } else if (CONSONANTS.contains(original)) {
                int idx = rand.nextInt(CONSONANTS.size());
                return new Start.Tile(CONSONANTS.get(idx));
            } else if (TONES.contains(original)) {
                int idx = rand.nextInt(TONES.size());
                return new Start.Tile(TONES.get(idx));
            } else if (ADs.contains(original)) {
                int idx = rand.nextInt(ADs.size());
                return new Start.Tile(ADs.get(idx));
            }
            // fallback: return self unchanged (shouldn't happen for normal tile types)
            return new Start.Tile(original);
        } else {
            // Distractor tile
            return tileList.returnRandomDistractorTile(original);
        }
    }

    // ── UI grid population ─────────────────────────────────────────────────────

    /**
     * Sets text on tile-audio buttons for every row and manages visibility so
     * that only the first {@code tileLength} columns are shown.
     */
    private void populateGrid(int tileLength) {
        for (int row = 0; row < NUM_ROWS; row++) {
            ArrayList<Start.Tile> tiles = rowTiles.get(row);
            for (int col = 0; col < MAX_TILES; col++) {
                TextView btn = findViewById(TILE_BTN_IDS[row][col]);
                if (col < tileLength) {
                    // Display this tile
                    Start.Tile tile = tiles.get(col);
                    btn.setText(tile.text);
                    // Color by tile type
                    String typeColor = getTileTypeColor(tile.typeOfThisTileInstance);
                    btn.setBackgroundColor(Color.parseColor(typeColor));
                    btn.setTextColor(Color.parseColor("#FFFFFF"));
                    btn.setVisibility(View.VISIBLE);
                    btn.setClickable(true);
                    // Store row and col in the tag for click handling: "row,col"
                    btn.setTag(row + "," + col);
                } else {
                    btn.setVisibility(View.INVISIBLE);
                    btn.setClickable(false);
                }
            }
        }
    }

    // ── selector circle drawing ────────────────────────────────────────────────

    /**
     * Sets the selector button for {@code row} to yellow (unanswered) or
     * green (correct answer selected).
     */
    private void setSelectorColor(int row, boolean correct) {
        TextView selector = findViewById(SELECTOR_IDS[row]);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        if (correct) {
            circle.setColor(Color.parseColor("#4CAF50")); // green
        } else {
            circle.setColor(Color.parseColor("#FFD700")); // yellow
        }
        selector.setBackground(circle);
    }

    // ── click handlers ────────────────────────────────────────────────────────

    /**
     * Called when a selector button (col 0) is tapped.
     * Tag on each selector is set to its row index as a String.
     */
    public void onSelectorClick(View view) {
        int row = Integer.parseInt((String) view.getTag());
        respondToRowSelection(row);
    }

    /**
     * Called when a tile-audio button (cols 1–6) is tapped.
     * Tag format: "row,col" where col is 0-based tile index.
     */
    public void onTileAudioClick(View view) {
        String tag = (String) view.getTag();
        String[] parts = tag.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        // Get the tile for this row/col and play its audio (Sudan-style lookup)
        ArrayList<Start.Tile> tiles = rowTiles.get(row);
        if (col < tiles.size()) {
            Start.Tile tile = tiles.get(col);
            playTileAudioForTile(tile);
        }
    }

    /** Clicking wordImage or wordText replays the word audio. */
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

    // ── row-selection logic ────────────────────────────────────────────────────

    private void respondToRowSelection(int row) {
        if (mediaPlayerIsPlaying) return;

        if (row == correctRowIndex) {
            // Correct!
            if (sendAnalytics) {
                String gameUniqueID = country.toLowerCase().substring(0, 2)
                        + challengeLevel + syllableGame;
                Properties info = new Properties()
                        .putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", wordInLOPWithStandardizedSequenceOfCharacters(refWord))
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < NUM_ROWS - 1; i++) {
                    if (!incorrectAnswersSelected.get(i).isEmpty()) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }

            repeatLocked = false;
            setSelectorColor(row, true);      // turn green
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(2);

            // Disable all selectors
            for (int r = 0; r < NUM_ROWS; r++) {
                findViewById(SELECTOR_IDS[r]).setClickable(false);
            }

            playCorrectSoundThenActiveWordClip(false);

        } else {
            // Wrong row
            incorrectOnLevel++;
            String rowLabel = "row" + row;
            for (int i = 0; i < NUM_ROWS - 1; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(rowLabel)) break;
                if (item.isEmpty()) {
                    incorrectAnswersSelected.set(i, rowLabel);
                    break;
                }
            }
            playIncorrectSound();
        }
    }

    // ── tile audio playback (Sudan-style) ─────────────────────────────────────

    /**
     * Plays the audio for a single tile using the SoundPool / MediaPlayer
     * infrastructure from GameActivity, mirroring Sudan's onBtnClick pattern.
     */
    private void playTileAudioForTile(Start.Tile tile) {
        if (mediaPlayerIsPlaying) return;

        String audioKey = tile.audioForThisTileType;
        if (audioKey == null || audioKey.equals("X") || audioKey.equals("zz_no_audio_needed")) return;

        if (!tileAudioIDs.containsKey(audioKey)) return;

        int audioId = tileAudioIDs.get(audioKey);
        int duration = tileDurations.containsKey(audioKey) ? tileDurations.get(audioKey) : 500;

        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                }
                setOptionsRowClickable();
            }
        }, duration);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Maps a tile-type string to an app theme color string. */
    private String getTileTypeColor(String type) {
        switch (type) {
            case "C":  return colorList.get(1);
            case "V":  return colorList.get(2);
            case "T":  return colorList.get(3);
            default:   return colorList.get(4);
        }
    }
}
