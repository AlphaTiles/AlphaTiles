package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;
import static org.alphatilesapps.alphatiles.Start.tileDurations;
import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.*;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Indonesia extends GameActivity {

    // ── 4 word text buttons (act as GAME_BUTTONS for clickable enable/disable) ──
    protected static final int[] GAME_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04
    };

    // ── 4 word image views ────────────────────────────────────────────────────
    protected static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04
    };

    // ── 4 LinearLayouts that hold the tile audio buttons ──────────────────────
    private static final int[] TILE_BANDS = {
            R.id.tileBand1, R.id.tileBand2, R.id.tileBand3, R.id.tileBand4
    };

    // Max tiles shown per word band (spec: 6 tile audio buttons per band)
    private static final int MAX_TILES_SHOWN = 6;

    final int wordsPerPage = GAME_BUTTONS.length; // 4
    int numPages           = 0;
    int currentPageNumber  = 0;

    /** All pages; each is a list of at most 4 words. */
    List<List<Start.Word>> wordPagesList = new ArrayList<>();

    // ── GameActivity abstract overrides ───────────────────────────────────────
    @Override protected int[] getGameButtons() { return GAME_BUTTONS; }
    @Override protected int[] getWordImages()  { return WORD_IMAGES; }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        try {
            return res.getIdentifier(
                    Start.gameList.get(gameNumber - 1).instructionAudioName,
                    "raw", context.getPackageName());
        } catch (NullPointerException e) {
            return -1;
        }
    }

    @Override
    protected void hideInstructionAudioImage() {
        ImageView btn = (ImageView) findViewById(R.id.instructions);
        if (btn != null) btn.setVisibility(View.GONE);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.indonesia);

        ActivityLayouts.applyEdgeToEdge(this, R.id.indonesiaCL);
        ActivityLayouts.setStatusAndNavColors(this);

        // Required before updatePointsAndTrackers — setAllGameButtonsUnclickable
        // loops up to visibleGameButtons; 0 (the default) means it does nothing.
        visibleGameButtons = GAME_BUTTONS.length;

        // Initialise the top bar (game number, challenge level, tracker dots)
        updatePointsAndTrackers(0);

        if (scriptDirection.equals("RTL")) {
            fixConstraintsRTLIndonesia();
        }
        if (getAudioInstructionsResID() == 0) hideInstructionAudioImage();

        // Passive game: no rounds, no winning — repeatLocked stays true so
        // the postDelayed re-enable callbacks always fire after audio ends.
        repeatLocked = true;

        buildWordPages();
        displayPage(0);
        showOrHideScrollArrows();
    }

    // ── Paging ────────────────────────────────────────────────────────────────
    /** Splits the stage word list into pages of 4. */
    private void buildWordPages() {
        List<Start.Word> stageWords = wordStagesLists.get(stage - 1);
        int total = stageWords.size();
        numPages  = Math.max(1, (int) Math.ceil((double) total / wordsPerPage));
        for (int p = 0; p < numPages; p++) {
            List<Start.Word> page = new ArrayList<>();
            int from = p * wordsPerPage;
            int to   = Math.min(from + wordsPerPage, total);
            for (int w = from; w < to; w++) page.add(stageWords.get(w));
            wordPagesList.add(page);
        }
    }

    /**
     * Populates the four word bands and four tile bands for the given page.
     * Unused slots are hidden.
     */
    private void displayPage(int page) {
        List<Start.Word> pageWords = wordPagesList.get(page);
        int wordsOnPage = pageWords.size();

        for (int i = 0; i < GAME_BUTTONS.length; i++) {
            TextView     wordText  = (TextView)     findViewById(GAME_BUTTONS[i]);
            ImageView    wordImage = (ImageView)    findViewById(WORD_IMAGES[i]);
            LinearLayout band      = (LinearLayout) findViewById(TILE_BANDS[i]);

            if (i < wordsOnPage) {
                Start.Word word = pageWords.get(i);

                // ── Word image (left 20%) ─────────────────────────────────────
                int resID = getResources().getIdentifier(
                        word.wordInLWC, "drawable", getPackageName());
                wordImage.setImageResource(resID);
                wordImage.setVisibility(View.VISIBLE);
                wordImage.setClickable(true);

                // ── Word text (right 80%) — coloured band ─────────────────────
                // Colour cycles: 0=purple, 1=blue, 2=red, 3=green
                wordText.setText(wordInLOPWithStandardizedSequenceOfCharacters(word));
                wordText.setBackgroundColor(Color.parseColor(colorList.get(i)));
                wordText.setTextColor(Color.WHITE);
                wordText.setVisibility(View.VISIBLE);
                wordText.setClickable(true);

                // ── Tile audio buttons (up to MAX_TILES_SHOWN) ────────────────
                band.removeAllViews();
                ArrayList<Start.Tile> tiles =
                        Start.tileList.parseWordIntoTiles(word.wordInLOP, word);
                int numTiles = Math.min(tiles.size(), MAX_TILES_SHOWN);
                for (int t = 0; t < numTiles; t++) {
                    band.addView(makeTileButton(tiles.get(t)));
                }
                band.setVisibility(View.VISIBLE);

            } else {
                // ── Hide unused slots ─────────────────────────────────────────
                wordText.setVisibility(View.INVISIBLE);
                wordText.setClickable(false);
                wordImage.setVisibility(View.INVISIBLE);
                wordImage.setClickable(false);
                band.removeAllViews();
                band.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Creates a coloured TextView that shows the tile's text and plays its
     * audio clip when tapped. Each tile gets equal weight so the buttons fill
     * the full band width left to right.
     */
    private TextView makeTileButton(Start.Tile tile) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.setMargins(3, 3, 3, 3);
        tv.setLayoutParams(lp);
        tv.setText(tile.text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(20f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setBackgroundColor(Color.parseColor(colorList.get(tile.tileColor)));
        tv.setClickable(true);
        tv.setOnClickListener(v -> {
            try {
                int audioId = tileAudioIDs.get(tile.audioForThisTileType);
                gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);
            } catch (NullPointerException ignored) {}
        });
        return tv;
    }

    // ── Word audio ─────────────────────────────────────────────────────────────
    /**
     * Plays the word audio for the slot identified by {@code view.getTag()}.
     * Word and image buttons are briefly disabled during playback to prevent
     * audio overlap (matching Malaysia's behaviour).
     */
    private void playWordAudio(View view) {
        int idx = Integer.parseInt((String) view.getTag()) - 1;
        List<Start.Word> pageWords = wordPagesList.get(currentPageNumber);
        if (idx >= pageWords.size()) return;
        Start.Word word = pageWords.get(idx);
        try {
            int audioId  = wordAudioIDs.get(word.wordInLWC);
            int duration = word.duration;
            setAllGameButtonsUnclickable();
            setAllWordImagesClickable(false);
            gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);
            soundSequencer.postDelayed(() -> {
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                    setAllWordImagesClickable(true);
                }
            }, duration);
        } catch (NullPointerException ignored) {}
    }

    /** android:onClick="clickPicHearAudio" on wordImage01–04. */
    public void clickPicHearAudio(View view) { playWordAudio(view); }

    /** android:onClick="onWordTextClick" on word01–04. */
    public void onWordTextClick(View view)   { playWordAudio(view); }

    /** Enables/disables all four word image views. */
    private void setAllWordImagesClickable(boolean status) {
        for (int id : WORD_IMAGES) {
            View v = findViewById(id);
            if (v != null) v.setClickable(status);
        }
    }

    // ── Scroll arrows ──────────────────────────────────────────────────────────
    /** android:onClick="nextPageArrow" on forwardArrowImage. */
    public void nextPageArrow(View view) {
        if (currentPageNumber >= numPages - 1) return;
        currentPageNumber++;
        displayPage(currentPageNumber);
        showOrHideScrollArrows();
    }

    /** android:onClick="prevPageArrow" on backwardArrowImage. */
    public void prevPageArrow(View view) {
        if (currentPageNumber <= 0) return;
        currentPageNumber--;
        displayPage(currentPageNumber);
        showOrHideScrollArrows();
    }

    /** Shows/hides forward and backward arrows based on current page. */
    public void showOrHideScrollArrows() {
        View fwd = findViewById(R.id.forwardArrowImage);
        View bwd = findViewById(R.id.backwardArrowImage);
        if (fwd != null) fwd.setVisibility(
                currentPageNumber >= numPages - 1 ? View.INVISIBLE : View.VISIBLE);
        if (bwd != null) bwd.setVisibility(
                currentPageNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    /** Mirrors the scroll arrows for RTL scripts. */
    private void fixConstraintsRTLIndonesia() {
        ImageView fwd = (ImageView) findViewById(R.id.forwardArrowImage);
        ImageView bwd = (ImageView) findViewById(R.id.backwardArrowImage);
        if (fwd != null) fwd.setRotationY(180);
        if (bwd != null) bwd.setRotationY(180);
    }

    // ── Delegated callbacks ────────────────────────────────────────────────────
    public void goBackToEarth(View view) { super.goBackToEarth(view); }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) super.playAudioInstructions(view);
    }
}
