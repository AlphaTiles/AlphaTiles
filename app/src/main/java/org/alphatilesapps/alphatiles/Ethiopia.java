package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.*;

public class Ethiopia extends GameActivity {

    // ── Keyboard button IDs ───────────────────────────────────────────────────
    protected static final int[] GAME_BUTTONS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07,
            R.id.key08, R.id.key09, R.id.key10, R.id.key11, R.id.key12, R.id.key13, R.id.key14,
            R.id.key15, R.id.key16, R.id.key17, R.id.key18, R.id.key19, R.id.key20, R.id.key21,
            R.id.key22, R.id.key23, R.id.key24, R.id.key25, R.id.key26, R.id.key27, R.id.key28,
            R.id.key29, R.id.key30, R.id.key31, R.id.key32, R.id.key33, R.id.key34, R.id.key35
    };

    // ── Life-heart ImageView IDs ──────────────────────────────────────────────
    private static final int[] LIFE_VIEWS = {
            R.id.life1, R.id.life2, R.id.life3,
            R.id.life4, R.id.life5, R.id.life6
    };

    private static final int MAX_LIVES = 6;

    // ── Round state ───────────────────────────────────────────────────────────
    int     keysInUse;
    private int     livesRemaining = MAX_LIVES;
    private boolean roundComplete  = false;

    /** Every tile text guessed this round (correct + wrong). */
    Set<String>         keys           = new HashSet<>();
    /** Tile texts confirmed to be in the word. */
    Set<String>         correctGuesses = new HashSet<>();
    static List<String> keysList       = new ArrayList<>();
    static List<String> keysClicked    = new ArrayList<>();

    public static final Logger LOGGER = Logger.getLogger(Ethiopia.class.getName());

    // ── Abstract-method implementations ───────────────────────────────────────
    @Override protected int[] getGameButtons() { return GAME_BUTTONS; }
    @Override protected int[] getWordImages()  { return null; }

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
        ImageView btn = findViewById(R.id.instructions);
        if (btn != null) btn.setVisibility(View.GONE);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.ethiopia);

        ActivityLayouts.applyEdgeToEdge(this, R.id.ethiopiaCL);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView inst   = findViewById(R.id.instructions);
            ImageView repeat = findViewById(R.id.repeatImage);
            if (inst   != null) inst.setRotationY(180);
            if (repeat != null) repeat.setRotationY(180);
            fixConstraintsRTL(R.id.ethiopiaCL);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        visibleGameButtons = GAME_BUTTONS.length;
        updatePointsAndTrackers(0);

        incorrectAnswersSelected = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) incorrectAnswersSelected.add("");

        playAgain();
    }

    // ── Round management ──────────────────────────────────────────────────────
    public void repeatGame(View view) {
        if (!repeatLocked) playAgain();
    }

    public void playAgain() {
        repeatLocked  = true;
        roundComplete = false;
        setAdvanceArrowToGray();

        livesRemaining = MAX_LIVES;
        keys.clear();
        correctGuesses.clear();
        keysClicked.clear();
        keysList.clear();

        chooseWord();
        parsedRefWordTileArray =
                Start.tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);

        // Word picture — tap to hear pronunciation
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(
                refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);
        image.setClickable(true);

        updateLifeDisplay();
        updateWordDisplay();
        loadKeyboard();

        for (int i = 0; i < 3; i++) incorrectAnswersSelected.set(i, "");
        incorrectOnLevel = 0;
        levelBegunTime   = System.currentTimeMillis();
    }

    // ── Keyboard ──────────────────────────────────────────────────────────────
    /**
     * Builds a smart ≤35-key keyboard using the tile system (mirroring Chile):
     *
     * <ol>
     *   <li><b>Required</b> — the distinct, non-SAD {@link Start.Tile} objects
     *       taken directly from {@code parsedRefWordTileArray}. These tiles
     *       <em>must</em> appear on the board or the round is unsolvable.</li>
     *   <li><b>Optional filler</b> — remaining non-SAD tiles from
     *       {@code cumulativeStageBasedTileList} (tiles the student has already
     *       learned at this stage), shuffled randomly.</li>
     *   <li>The combined list is sorted by each tile's natural position in
     *       {@code tileList} (exactly as Chile sorts its keyboard) so the
     *       layout is consistent and readable across rounds.</li>
     *   <li>Text and colour both come from the {@link Start.Tile} object
     *       ({@code tile.text}, {@code tile.color}).</li>
     * </ol>
     */
    public void loadKeyboard() {
        keysClicked.clear();
        keysList.clear();

        // ── Step 1: Required tiles from the word ──────────────────────────────
        // Pull Tile objects directly from parsedRefWordTileArray so text + colour
        // are guaranteed to match the parsed word.  Deduplicate by text (a tile
        // that appears twice in the word only needs one keyboard button).
        List<Start.Tile> required     = new ArrayList<>();
        Set<String>      requiredSeen = new HashSet<>();

        for (Start.Tile tile : parsedRefWordTileArray) {
            if (SAD_STRINGS.contains(tile.text))  continue; // diacritics are auto-revealed
            if (requiredSeen.contains(tile.text)) continue; // deduplicate
            requiredSeen.add(tile.text);
            required.add(tile);
        }

        // ── Step 2: Optional filler from cumulativeStageBasedTileList ─────────
        // Use the stage-scoped tile list (only tiles the student knows so far)
        // so the keyboard stays pedagogically appropriate.
        List<Start.Tile> optional = new ArrayList<>();
        Set<String>      allSeen  = new HashSet<>(requiredSeen);

        for (Start.Tile tile : cumulativeStageBasedTileList) {
            if (SAD_STRINGS.contains(tile.text)) continue;
            if (allSeen.contains(tile.text))     continue;
            allSeen.add(tile.text);
            optional.add(tile);
        }

        // ── Step 3: Fill to 35 ────────────────────────────────────────────────
        Collections.shuffle(optional);
        List<Start.Tile> keyboard = new ArrayList<>(required);
        int slots = Math.min(GAME_BUTTONS.length - keyboard.size(), optional.size());
        for (int i = 0; i < slots; i++) keyboard.add(optional.get(i));

        // Sort by natural tile order in tileList — same technique Chile uses so
        // the keyboard reads in a consistent, linguistically meaningful order.
        // Build a text→index lookup first to avoid O(n²) searches in sort.
        Map<String, Integer> tileOrder = new HashMap<>();
        for (int t = 0; t < tileList.size(); t++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tileOrder.putIfAbsent(tileList.get(t).text, t);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            keyboard.sort((a, b) -> {
                int ia = tileOrder.getOrDefault(a.text, Integer.MAX_VALUE);
                int ib = tileOrder.getOrDefault(b.text, Integer.MAX_VALUE);
                return Integer.compare(ia, ib);
            });
        }

        keysInUse          = keyboard.size();
        visibleGameButtons = keysInUse;

        // ── Step 4: Bind to key views ─────────────────────────────────────────
        for (int k = 0; k < GAME_BUTTONS.length; k++) {
            TextView keyView = (TextView) findViewById(GAME_BUTTONS[k]);
            if (keyView == null) continue;
            if (k < keyboard.size()) {
                Start.Tile tile     = keyboard.get(k);
                String     colorStr = colorList.get(tile.tileColor);
                keyView.setText(tile.text);
                keyView.setBackgroundColor(Color.parseColor(colorStr));
                keyView.setTextColor(Color.WHITE);
                keyView.setAlpha(1.0f);
                keyView.setClickable(true);
                keyView.setVisibility(View.VISIBLE);
                keysList.add(tile.text);
            } else {
                keyView.setVisibility(View.INVISIBLE);
                keyView.setClickable(false);
            }
        }
    }

    // ── Key-press handler ─────────────────────────────────────────────────────
    public void onBtnClick(View view) {
        if (roundComplete) return;

        TextView key      = (TextView) view;
        String   tileText = key.getText().toString().trim();

        if (tileText.isEmpty())      return;  // blank / invisible slot
        if (keys.contains(tileText)) return;  // already guessed this round

        keys.add(tileText);
        keysClicked.add(tileText);
        key.setClickable(false);

        // Check whether this tile text appears anywhere in the target word.
        // Because the keyboard is built from the same tileList that
        // parseWordIntoTiles uses, tile.text values are guaranteed to be
        // the same String instances — no normalisation needed.
        boolean found = false;
        for (Start.Tile tile : parsedRefWordTileArray) {
            if (tile.text.equals(tileText)) { found = true; break; }
        }

        if (found) {
            // ── Correct guess ─────────────────────────────────────────────────
            correctGuesses.add(tileText);
            key.setAlpha(0.45f);   // dim but keep colour so player sees which worked
            updateWordDisplay();
            if (isWordComplete()) onWordComplete();

        } else {
            // ── Wrong guess ───────────────────────────────────────────────────
            key.setBackgroundColor(Color.parseColor("#A9A9A9"));
            key.setTextColor(Color.parseColor("#000000"));
            livesRemaining--;
            incorrectOnLevel++;
            updateLifeDisplay();

            for (int i = 0; i < 3; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(tileText)) break;
                if (item.isEmpty()) { incorrectAnswersSelected.set(i, tileText); break; }
            }

            final boolean gameIsOver = (livesRemaining <= 0);

            // playIncorrectSound() internally calls setAllGameButtonsClickable()
            // after the clip ends; schedule a re-disable pass to undo that for
            // keys the player has already used this round.
            playIncorrectSound();
            soundSequencer.postDelayed(() -> {
                for (int i = 0; i < GAME_BUTTONS.length; i++) {
                    TextView k = (TextView) findViewById(GAME_BUTTONS[i]);
                    if (k == null) continue;
                    if (keys.contains(k.getText().toString().trim())) k.setClickable(false);
                }
                if (gameIsOver && !roundComplete) onGameOver();
            }, 800);
        }
    }

    // ── Win / loss ────────────────────────────────────────────────────────────
    private void onWordComplete() {
        roundComplete = true;
        repeatLocked  = false;
        setAdvanceArrowToBlue();
        updatePointsAndTrackers(2);               // +2 points, fill one tracker dot
        playCorrectSoundThenActiveWordClip(false); // chime → pronounce the word
    }

    private void onGameOver() {
        roundComplete = true;
        TextView wordDisplay = findViewById(R.id.activeWordTextView);
        if (wordDisplay != null)
            wordDisplay.setText(wordInLOPWithStandardizedSequenceOfCharacters(refWord));
        setAllGameButtonsUnclickable();
        repeatLocked = false;
        setAdvanceArrowToBlue();   // let player advance — no points for losing
    }

    // ── Display helpers ───────────────────────────────────────────────────────
    /**
     * Renders the word-display bar — one visual slot per non-SAD tile.
     *
     * <p>Each slot shows □ when unrevealed, or the tile text when the player
     * has guessed it correctly.  Any SAD tiles immediately following a base
     * letter are appended to that letter's slot so they render as a single
     * combined glyph (e.g. the base letter "a" + overhead accent "¯" → "ā").
     */
    private void updateWordDisplay() {
        StringBuilder sb        = new StringBuilder();
        boolean       firstSlot = true;
        int i = 0;

        while (i < parsedRefWordTileArray.size()) {
            Start.Tile tile = parsedRefWordTileArray.get(i);

            // SAD tiles are rendered as part of their preceding letter.
            if (SAD_STRINGS.contains(tile.text)) { i++; continue; }

            if (!firstSlot) sb.append("  ");
            firstSlot = false;

            // Peek forward: collect SAD tiles that immediately follow this letter
            // so they display as one combined unit.
            StringBuilder visual = new StringBuilder(tile.text);
            int j = i + 1;
            while (j < parsedRefWordTileArray.size()
                    && SAD_STRINGS.contains(parsedRefWordTileArray.get(j).text)) {
                visual.append(parsedRefWordTileArray.get(j).text);
                j++;
            }

            sb.append(correctGuesses.contains(tile.text)
                    ? visual            // revealed: letter + any attached diacritics
                    : "\u25A1");        // □ placeholder

            i = j; // jump past the SADs we just consumed
        }

        TextView wordDisplay = findViewById(R.id.activeWordTextView);
        if (wordDisplay != null) wordDisplay.setText(sb.toString());
    }

    /**
     * Returns true when every non-SAD tile in the word has been guessed.
     * SAD tiles are auto-revealed with their base letter and never required
     * as independent guesses.
     */
    private boolean isWordComplete() {
        for (Start.Tile tile : parsedRefWordTileArray) {
            if (SAD_STRINGS.contains(tile.text))    continue;
            if (!correctGuesses.contains(tile.text)) return false;
        }
        return true;
    }

    /** Syncs the six life icons with {@code livesRemaining}. */
    private void updateLifeDisplay() {
        for (int i = 0; i < LIFE_VIEWS.length; i++) {
            ImageView life = findViewById(LIFE_VIEWS[i]);
            if (life == null) continue;
            String name = (i < livesRemaining) ? "zz_complete" : "zz_incomplete";
            int id = getResources().getIdentifier(name, "drawable", getPackageName());
            life.setImageResource(id);
        }
    }

    // ── Delegated view callbacks ──────────────────────────────────────────────
    public void clickPicHearAudio(View view) { super.clickPicHearAudio(view); }
    public void goBackToEarth(View view)     { super.goBackToEarth(view); }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) super.playAudioInstructions(view);
    }
}
