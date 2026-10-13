package org.alphatilesapps.alphatiles;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.*;

public class Colombia extends GameActivity {

    // JP:
    // syllables level 1: only necessary syllables, scrambled
    // syllables level 2: necessary syllables + 1 distractor syllable per syllable, scrambled
    // syllables level 3: necessary syllables + all 3 distractor syllables per syllable
    // (filter out any repeats), scrambled
    int keysInUse; // Number of keys in the language's total keyboard
    int keyboardScreenNo; // For languages with more than 35 keys, page 1 will have 33 buttons and a forward/backward button
    int totalScreens; // Total number of screens required to show all keys
    int partial; // Number of visible keys on final partial screen
    static List<Tile> tileKeysList = new ArrayList<>();
    static List<Syllable> syllableKeysList = new ArrayList<>();
    static List<WordPiece> clickedKeys = new ArrayList<>(); // Keys clicked, in order
    static ArrayList<Tile> tilesInBuiltWord = new ArrayList<>();

    final int tilesPerPage = 35;
    final int syllablesPerPage = 18;

    protected static final int[] GAME_BUTTONS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07, R.id.key08, R.id.key09, R.id.key10,
            R.id.key11, R.id.key12, R.id.key13, R.id.key14, R.id.key15, R.id.key16, R.id.key17, R.id.key18, R.id.key19, R.id.key20,
            R.id.key21, R.id.key22, R.id.key23, R.id.key24, R.id.key25, R.id.key26, R.id.key27, R.id.key28, R.id.key29, R.id.key30,
            R.id.key31, R.id.key32, R.id.key33, R.id.key34, R.id.key35
    };

    private static final int[][] MAPPINGS_COLOMBIA = {
            // Common Horizontal Guidelines
            {R.id.horGuidelineStatusTop, R.dimen.horGuidelineStatusTop},
            {R.id.horGuidelineStatusMiddle, R.dimen.horGuidelineStatusMiddle},
            {R.id.horGuidelineStatusBottom, R.dimen.horGuidelineStatusBottom},
            {R.id.horGuidelineOptionsTop, R.dimen.horGuidelineOptionsTop},
            {R.id.horGuidelineOptionsBottom, R.dimen.horGuidelineOptionsBottom},

            // Specific Horizontal Guidelines
            {R.id.colombia_horGuidelineRefTop, R.dimen.colombia_horGuidelineRefTop},
            {R.id.colombia_horGuidelineRefBottom, R.dimen.colombia_horGuidelineRefBottom},
            {R.id.colombia_horGuidelineTextTop, R.dimen.colombia_horGuidelineTextTop},
            {R.id.colombia_horGuidelineTextBottom, R.dimen.colombia_horGuidelineTextBottom},
            {R.id.colombia_horGuidelineRow1Top, R.dimen.colombia_horGuidelineRow1Top},
            {R.id.colombia_horGuidelineRow1Bottom, R.dimen.colombia_horGuidelineRow1Bottom},
            {R.id.colombia_horGuidelineRow2Top, R.dimen.colombia_horGuidelineRow2Top},
            {R.id.colombia_horGuidelineRow2Bottom, R.dimen.colombia_horGuidelineRow2Bottom},
            {R.id.colombia_horGuidelineRow3Top, R.dimen.colombia_horGuidelineRow3Top},
            {R.id.colombia_horGuidelineRow3Bottom, R.dimen.colombia_horGuidelineRow3Bottom},
            {R.id.colombia_horGuidelineRow4Top, R.dimen.colombia_horGuidelineRow4Top},
            {R.id.colombia_horGuidelineRow4Bottom, R.dimen.colombia_horGuidelineRow4Bottom},
            {R.id.colombia_horGuidelineRow5Top, R.dimen.colombia_horGuidelineRow5Top},
            {R.id.colombia_horGuidelineRow5Bottom, R.dimen.colombia_horGuidelineRow5Bottom},

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
            {R.id.colombia_verGuidelineRefLeft, R.dimen.colombia_verGuidelineRefLeft},
            {R.id.colombia_verGuidelineRefRight, R.dimen.colombia_verGuidelineRefRight},
            {R.id.colombia_verGuidelineTextLeft, R.dimen.colombia_verGuidelineTextLeft},
            {R.id.colombia_verGuidelineTextRight, R.dimen.colombia_verGuidelineTextRight},
            {R.id.colombia_verGuidelineDeleteLeft, R.dimen.colombia_verGuidelineDeleteLeft},
            {R.id.colombia_verGuidelineDeleteRight, R.dimen.colombia_verGuidelineDeleteRight},
            {R.id.colombia_verGuidelineCol1Left, R.dimen.colombia_verGuidelineCol1Left},
            {R.id.colombia_verGuidelineCol1Right, R.dimen.colombia_verGuidelineCol1Right},
            {R.id.colombia_verGuidelineCol2Left, R.dimen.colombia_verGuidelineCol2Left},
            {R.id.colombia_verGuidelineCol2Right, R.dimen.colombia_verGuidelineCol2Right},
            {R.id.colombia_verGuidelineCol3Left, R.dimen.colombia_verGuidelineCol3Left},
            {R.id.colombia_verGuidelineCol3Right, R.dimen.colombia_verGuidelineCol3Right},
            {R.id.colombia_verGuidelineCol4Left, R.dimen.colombia_verGuidelineCol4Left},
            {R.id.colombia_verGuidelineCol4Right, R.dimen.colombia_verGuidelineCol4Right},
            {R.id.colombia_verGuidelineCol5Left, R.dimen.colombia_verGuidelineCol5Left},
            {R.id.colombia_verGuidelineCol5Right, R.dimen.colombia_verGuidelineCol5Right},
            {R.id.colombia_verGuidelineCol6Left, R.dimen.colombia_verGuidelineCol6Left},
            {R.id.colombia_verGuidelineCol6Right, R.dimen.colombia_verGuidelineCol6Right},
            {R.id.colombia_verGuidelineCol7Left, R.dimen.colombia_verGuidelineCol7Left},
            {R.id.colombia_verGuidelineCol7Right, R.dimen.colombia_verGuidelineCol7Right}
    };

    private static final int[][] MAPPINGS_COLOMBIA_SYLLABLES = {
            // Common Horizontal Guidelines
            {R.id.horGuidelineStatusTop, R.dimen.horGuidelineStatusTop},
            {R.id.horGuidelineStatusMiddle, R.dimen.horGuidelineStatusMiddle},
            {R.id.horGuidelineStatusBottom, R.dimen.horGuidelineStatusBottom},
            {R.id.horGuidelineOptionsTop, R.dimen.horGuidelineOptionsTop},
            {R.id.horGuidelineOptionsBottom, R.dimen.horGuidelineOptionsBottom},

            // Specific Horizontal Guidelines
            {R.id.colombia_syll_horGuidelineRefTop, R.dimen.colombia_syll_horGuidelineRefTop},
            {R.id.colombia_syll_horGuidelineRefBottom, R.dimen.colombia_syll_horGuidelineRefBottom},
            {R.id.colombia_syll_horGuidelineTextTop, R.dimen.colombia_syll_horGuidelineTextTop},
            {R.id.colombia_syll_horGuidelineTextBottom, R.dimen.colombia_syll_horGuidelineTextBottom},

            {R.id.colombia_syll_horGuidelineKey01Top, R.dimen.colombia_syll_horGuidelineKey01Top},
            {R.id.colombia_syll_horGuidelineKey01Bottom, R.dimen.colombia_syll_horGuidelineKey01Bottom},
            {R.id.colombia_syll_horGuidelineKey02Top, R.dimen.colombia_syll_horGuidelineKey02Top},
            {R.id.colombia_syll_horGuidelineKey02Bottom, R.dimen.colombia_syll_horGuidelineKey02Bottom},
            {R.id.colombia_syll_horGuidelineKey03Top, R.dimen.colombia_syll_horGuidelineKey03Top},
            {R.id.colombia_syll_horGuidelineKey03Bottom, R.dimen.colombia_syll_horGuidelineKey03Bottom},
            {R.id.colombia_syll_horGuidelineKey04Top, R.dimen.colombia_syll_horGuidelineKey04Top},
            {R.id.colombia_syll_horGuidelineKey04Bottom, R.dimen.colombia_syll_horGuidelineKey04Bottom},
            {R.id.colombia_syll_horGuidelineKey05Top, R.dimen.colombia_syll_horGuidelineKey05Top},
            {R.id.colombia_syll_horGuidelineKey05Bottom, R.dimen.colombia_syll_horGuidelineKey05Bottom},
            {R.id.colombia_syll_horGuidelineKey06Top, R.dimen.colombia_syll_horGuidelineKey06Top},
            {R.id.colombia_syll_horGuidelineKey06Bottom, R.dimen.colombia_syll_horGuidelineKey06Bottom},
            {R.id.colombia_syll_horGuidelineKey07Top, R.dimen.colombia_syll_horGuidelineKey07Top},
            {R.id.colombia_syll_horGuidelineKey07Bottom, R.dimen.colombia_syll_horGuidelineKey07Bottom},
            {R.id.colombia_syll_horGuidelineKey08Top, R.dimen.colombia_syll_horGuidelineKey08Top},
            {R.id.colombia_syll_horGuidelineKey08Bottom, R.dimen.colombia_syll_horGuidelineKey08Bottom},
            {R.id.colombia_syll_horGuidelineKey09Top, R.dimen.colombia_syll_horGuidelineKey09Top},
            {R.id.colombia_syll_horGuidelineKey09Bottom, R.dimen.colombia_syll_horGuidelineKey09Bottom},
            {R.id.colombia_syll_horGuidelineKey10Top, R.dimen.colombia_syll_horGuidelineKey10Top},
            {R.id.colombia_syll_horGuidelineKey10Bottom, R.dimen.colombia_syll_horGuidelineKey10Bottom},
            {R.id.colombia_syll_horGuidelineKey11Top, R.dimen.colombia_syll_horGuidelineKey11Top},
            {R.id.colombia_syll_horGuidelineKey11Bottom, R.dimen.colombia_syll_horGuidelineKey11Bottom},
            {R.id.colombia_syll_horGuidelineKey12Top, R.dimen.colombia_syll_horGuidelineKey12Top},
            {R.id.colombia_syll_horGuidelineKey12Bottom, R.dimen.colombia_syll_horGuidelineKey12Bottom},
            {R.id.colombia_syll_horGuidelineKey13Top, R.dimen.colombia_syll_horGuidelineKey13Top},
            {R.id.colombia_syll_horGuidelineKey13Bottom, R.dimen.colombia_syll_horGuidelineKey13Bottom},
            {R.id.colombia_syll_horGuidelineKey14Top, R.dimen.colombia_syll_horGuidelineKey14Top},
            {R.id.colombia_syll_horGuidelineKey14Bottom, R.dimen.colombia_syll_horGuidelineKey14Bottom},
            {R.id.colombia_syll_horGuidelineKey15Top, R.dimen.colombia_syll_horGuidelineKey15Top},
            {R.id.colombia_syll_horGuidelineKey15Bottom, R.dimen.colombia_syll_horGuidelineKey15Bottom},
            {R.id.colombia_syll_horGuidelineKey16Top, R.dimen.colombia_syll_horGuidelineKey16Top},
            {R.id.colombia_syll_horGuidelineKey16Bottom, R.dimen.colombia_syll_horGuidelineKey16Bottom},
            {R.id.colombia_syll_horGuidelineKey17Top, R.dimen.colombia_syll_horGuidelineKey17Top},
            {R.id.colombia_syll_horGuidelineKey17Bottom, R.dimen.colombia_syll_horGuidelineKey17Bottom},
            {R.id.colombia_syll_horGuidelineKey18Top, R.dimen.colombia_syll_horGuidelineKey18Top},
            {R.id.colombia_syll_horGuidelineKey18Bottom, R.dimen.colombia_syll_horGuidelineKey18Bottom},

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
            {R.id.colombia_syll_verGuidelineRefLeft, R.dimen.colombia_syll_verGuidelineRefLeft},
            {R.id.colombia_syll_verGuidelineRefRight, R.dimen.colombia_syll_verGuidelineRefRight},
            {R.id.colombia_syll_verGuidelineTextLeft, R.dimen.colombia_syll_verGuidelineTextLeft},
            {R.id.colombia_syll_verGuidelineTextRight, R.dimen.colombia_syll_verGuidelineTextRight},
            {R.id.colombia_syll_verGuidelineDeleteLeft, R.dimen.colombia_syll_verGuidelineDeleteLeft},
            {R.id.colombia_syll_verGuidelineDeleteRight, R.dimen.colombia_syll_verGuidelineDeleteRight},

            {R.id.colombia_syll_verGuidelineKey01Left, R.dimen.colombia_syll_verGuidelineKey01Left},
            {R.id.colombia_syll_verGuidelineKey01Right, R.dimen.colombia_syll_verGuidelineKey01Right},
            {R.id.colombia_syll_verGuidelineKey02Left, R.dimen.colombia_syll_verGuidelineKey02Left},
            {R.id.colombia_syll_verGuidelineKey02Right, R.dimen.colombia_syll_verGuidelineKey02Right},
            {R.id.colombia_syll_verGuidelineKey03Left, R.dimen.colombia_syll_verGuidelineKey03Left},
            {R.id.colombia_syll_verGuidelineKey03Right, R.dimen.colombia_syll_verGuidelineKey03Right},
            {R.id.colombia_syll_verGuidelineKey04Left, R.dimen.colombia_syll_verGuidelineKey04Left},
            {R.id.colombia_syll_verGuidelineKey04Right, R.dimen.colombia_syll_verGuidelineKey04Right},
            {R.id.colombia_syll_verGuidelineKey05Left, R.dimen.colombia_syll_verGuidelineKey05Left},
            {R.id.colombia_syll_verGuidelineKey05Right, R.dimen.colombia_syll_verGuidelineKey05Right},
            {R.id.colombia_syll_verGuidelineKey06Left, R.dimen.colombia_syll_verGuidelineKey06Left},
            {R.id.colombia_syll_verGuidelineKey06Right, R.dimen.colombia_syll_verGuidelineKey06Right},
            {R.id.colombia_syll_verGuidelineKey07Left, R.dimen.colombia_syll_verGuidelineKey07Left},
            {R.id.colombia_syll_verGuidelineKey07Right, R.dimen.colombia_syll_verGuidelineKey07Right},
            {R.id.colombia_syll_verGuidelineKey08Left, R.dimen.colombia_syll_verGuidelineKey08Left},
            {R.id.colombia_syll_verGuidelineKey08Right, R.dimen.colombia_syll_verGuidelineKey08Right},
            {R.id.colombia_syll_verGuidelineKey09Left, R.dimen.colombia_syll_verGuidelineKey09Left},
            {R.id.colombia_syll_verGuidelineKey09Right, R.dimen.colombia_syll_verGuidelineKey09Right},
            {R.id.colombia_syll_verGuidelineKey10Left, R.dimen.colombia_syll_verGuidelineKey10Left},
            {R.id.colombia_syll_verGuidelineKey10Right, R.dimen.colombia_syll_verGuidelineKey10Right},
            {R.id.colombia_syll_verGuidelineKey11Left, R.dimen.colombia_syll_verGuidelineKey11Left},
            {R.id.colombia_syll_verGuidelineKey11Right, R.dimen.colombia_syll_verGuidelineKey11Right},
            {R.id.colombia_syll_verGuidelineKey12Left, R.dimen.colombia_syll_verGuidelineKey12Left},
            {R.id.colombia_syll_verGuidelineKey12Right, R.dimen.colombia_syll_verGuidelineKey12Right},
            {R.id.colombia_syll_verGuidelineKey13Left, R.dimen.colombia_syll_verGuidelineKey13Left},
            {R.id.colombia_syll_verGuidelineKey13Right, R.dimen.colombia_syll_verGuidelineKey13Right},
            {R.id.colombia_syll_verGuidelineKey14Left, R.dimen.colombia_syll_verGuidelineKey14Left},
            {R.id.colombia_syll_verGuidelineKey14Right, R.dimen.colombia_syll_verGuidelineKey14Right},
            {R.id.colombia_syll_verGuidelineKey15Left, R.dimen.colombia_syll_verGuidelineKey15Left},
            {R.id.colombia_syll_verGuidelineKey15Right, R.dimen.colombia_syll_verGuidelineKey15Right},
            {R.id.colombia_syll_verGuidelineKey16Left, R.dimen.colombia_syll_verGuidelineKey16Left},
            {R.id.colombia_syll_verGuidelineKey16Right, R.dimen.colombia_syll_verGuidelineKey16Right},
            {R.id.colombia_syll_verGuidelineKey17Left, R.dimen.colombia_syll_verGuidelineKey17Left},
            {R.id.colombia_syll_verGuidelineKey17Right, R.dimen.colombia_syll_verGuidelineKey17Right},
            {R.id.colombia_syll_verGuidelineKey18Left, R.dimen.colombia_syll_verGuidelineKey18Left},
            {R.id.colombia_syll_verGuidelineKey18Right, R.dimen.colombia_syll_verGuidelineKey18Right}
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

    private void updateGuidelines() {
        View rootView = findViewById(android.R.id.content);
        if (syllableGame.equals("S")) {
            GuidelineUtils.applyGuidelines(rootView, this, MAPPINGS_COLOMBIA_SYLLABLES);
        } else {
            GuidelineUtils.applyGuidelines(rootView, this, MAPPINGS_COLOMBIA);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGuidelines();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        int gameID = 0;
        if (syllableGame.equals("S")) {
            setContentView(R.layout.colombia_syllables);
            gameID = R.id.colombiaCL_syll;

            if (challengeLevel == 4){//kicks the user back to Earth if it's a syllable game with cl 4.
                goBackToEarth(null);// Later there should be a more permanent fix to remove it
            }// from the screen entirely.
        } else {
            setContentView(R.layout.colombia);
            gameID = R.id.colombiaCL;
        }
        updateGuidelines();

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            ImageView deleteImage = (ImageView) findViewById(R.id.deleteImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            deleteImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        totalScreens = 1;
        keyboardScreenNo = 1;
        playAgain();
        setUpInitialView();
        updateView();
    }
    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        repeatLocked = true;
        setAdvanceArrowToGray();

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        wordToBuild.setText("");
        // This line will need to be changed
        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
        wordToBuild.setTextColor(Color.parseColor("#000000")); // black

        setWord();

        ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
        deleteArrow.setClickable(true);

        ImageView wordImage = (ImageView) findViewById(R.id.wordImage);
        wordImage.setClickable(true);

        if (totalScreens > 1) {
            keyboardScreenNo = 1;
            updateKeyboard();
        }

    }

    private void setWord() {
        clickedKeys.clear();
        tilesInBuiltWord.clear();
        chooseWord();
        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        if (syllableGame.equals("S")) {
            parsedRefWordSyllableArray = syllableList.parseWordIntoSyllables(refWord); // KP
        } else {
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord); // KP
        }

        loadKeyboard();

    }

    public void loadKeyboard() {
        tileKeysList.clear();
        syllableKeysList.clear();

        switch (challengeLevel) {
            case 1:
                // Build an array of only the required tiles
                // Will list <a> twice if <a> is needed twice
                // The limited set keyboard is built with GAME TILES not with KEYS

                if (syllableGame.equals("S")) {
                    syllableKeysList = new ArrayList<>(parsedRefWordSyllableArray);
                    Collections.shuffle(syllableKeysList);
                    visibleGameButtons = syllableKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(syllableKeysList.get(k).text);
                        int index = k%5;
                        int tileColor = Color.parseColor(colorList.get(index));
                        key.setBackgroundColor(tileColor);
                    }

                } else {
                    tileKeysList = new ArrayList<>(parsedRefWordTileArray);
                    Collections.shuffle(tileKeysList);
                    visibleGameButtons = tileKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(tileKeysList.get(k).text);
                        int index = k%5;
                        String tileColorStr = colorList.get(index);
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }

                }
                break;
            case 2:
                // Build an array of the required tiles plus a corresponding tile from the distractor trio for each tile
                // So, for a five tile word, there will be 10 tiles
                // The limited-set keyboard is built with GAME TILES not with KEYS
                if (syllableGame.equals("S")) {
                    syllableKeysList = new ArrayList<>(parsedRefWordSyllableArray);
                    int numberOfCorrectKeys = parsedRefWordSyllableArray.size();
                    for (int n=0; n<numberOfCorrectKeys; n++) {
                        Syllable syllableInTheList = syllableKeysList.get(n);
                        if(SAD_STRINGS.contains(syllableInTheList.text)){
                            Syllable distractorSADSyllable = syllableInTheList;
                            distractorSADSyllable.text = tileList.returnRandomDistractorTile(tileHashMap.find(syllableInTheList.text)).text;
                            if (distractorSADSyllable.distractors.contains(distractorSADSyllable.text)) {
                                distractorSADSyllable.distractors.remove(distractorSADSyllable.text);
                                distractorSADSyllable.distractors.add(syllableInTheList.text);
                            }
                            syllableKeysList.add(distractorSADSyllable);
                        } else {
                            syllableKeysList.add(syllableList.returnRandomDistractorSyllable(syllableInTheList));
                        }
                    }
                    Collections.shuffle(syllableKeysList);
                    visibleGameButtons = syllableKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(syllableKeysList.get(k).text);
                        int index = k%5;
                        int tileColor = Color.parseColor(colorList.get(index));
                        key.setBackgroundColor(tileColor);
                    }

                } else {
                    tileKeysList = new ArrayList<>(parsedRefWordTileArray);
                    int numberOfCorrectKeys = parsedRefWordTileArray.size();
                    for (int n=0; n<numberOfCorrectKeys; n++) {
                        tileKeysList.add(tileList.returnRandomDistractorTile(tileKeysList.get(n)));
                    }
                    Collections.shuffle(tileKeysList);
                    visibleGameButtons = tileKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(tileKeysList.get(k).text);
                        int index = k%5;
                        String tileColorStr = colorList.get(index);
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }

                }
                break;
            case 3:
                if (syllableGame.equals("S")) { // 18 tiles; distractors for the wrong answers
                    syllableKeysList = new ArrayList<>(parsedRefWordSyllableArray);
                    for (int n=0; n<(18-parsedRefWordSyllableArray.size()); n++) {
                        Syllable syllableInTheList = syllableKeysList.get(n);
                        if(SAD_STRINGS.contains(syllableInTheList.text)){
                            Syllable distractorSADSyllable = syllableInTheList;
                            distractorSADSyllable.text = tileList.returnRandomDistractorTile(tileHashMap.find(syllableInTheList.text)).text;
                            if(distractorSADSyllable.distractors.contains(distractorSADSyllable.text)){
                                distractorSADSyllable.distractors.remove(distractorSADSyllable.text);
                                distractorSADSyllable.distractors.add(syllableInTheList.text);
                            }
                            syllableKeysList.add(distractorSADSyllable);
                        } else {
                            syllableKeysList.add(syllableList.returnRandomDistractorSyllable(syllableInTheList));
                        }
                    }
                    Collections.shuffle(syllableKeysList);
                    visibleGameButtons = syllableKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(syllableKeysList.get(k).text);
                        int index = k%5;
                        int tileColor = Color.parseColor(colorList.get(index));
                        key.setBackgroundColor(tileColor);
                    }

                } else { // Full key list
                    for (Start.Key key: keyList) {
                        tileKeysList.add(tileHashMap.find(key.text));
                    }
                    keysInUse = keyList.size(); // KP
                    if (keysInUse <= tilesPerPage) {
                        totalScreens = keysInUse / (GAME_BUTTONS.length);
                        partial = keysInUse % (GAME_BUTTONS.length);
                    } else {
                        totalScreens = keysInUse / (GAME_BUTTONS.length - 2);
                        partial = keysInUse % (GAME_BUTTONS.length - 2);
                    }
                    if (partial == 0) {
                        partial = GAME_BUTTONS.length - 2; // if partial is zero, then the prior screen's partial is 100% full
                    } else {
                        totalScreens++; // at least one partial tile requires an additional screen
                    }

                    if (keysInUse > GAME_BUTTONS.length) {
                        visibleGameButtons = GAME_BUTTONS.length;
                    } else {
                        visibleGameButtons = keysInUse;
                    }
                    for (int k = 0; k < visibleGameButtons; k++) {
                        TextView key = findViewById(GAME_BUTTONS[k]);
                        key.setText(keyList.get(k).text); // KRP
                        String tileColorStr = colorList.get(Integer.parseInt(keyList.get(k).color));
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }

                    if (keysInUse > GAME_BUTTONS.length) {
                        TextView key34 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 2]);
                        key34.setBackgroundResource(R.drawable.zz_backward_inactive);
                        if (scriptDirection.equals("RTL")) {
                            key34.setRotationY(180);
                        }
                        key34.setText("");
                        TextView key35 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 1]);
                        key35.setBackgroundResource(R.drawable.zz_forward_green);
                        if (scriptDirection.equals("RTL")) {
                            key35.setRotationY(180);
                        }
                        key35.setText("");
                    }
                }
                break;

            // Sets up a scrolling keyboard with all tiles.
            case 4:// should only be selected if !SyllableGame.equals("S")
                tileKeysList.addAll(tileList);

                // removes all duplicate keys from tileKeysList
                int i = 1;
                while (i < tileKeysList.size()){
                    //currently runs in constant time, update if tileKeysList becomes a linked list.
                    if (tileKeysList.get(i).equals(tileKeysList.get(i - 1))){
                        tileKeysList.remove(i-1);
                        i--;
                    }
                    i++;
                }
                keysInUse = tileKeysList.size(); // KP
                totalScreens = keysInUse / (GAME_BUTTONS.length - 2);
                if (totalScreens == 1) {
                    partial = keysInUse % (GAME_BUTTONS.length);
                } else {
                    partial = keysInUse % (GAME_BUTTONS.length - 2);
                }

                if (partial != 0) {
                    totalScreens++;
                }

                if (keysInUse > GAME_BUTTONS.length) {
                    visibleGameButtons = GAME_BUTTONS.length;
                } else {
                    visibleGameButtons = keysInUse;
                }
                // Sets the color of each button
                for (int k = 0; k < visibleGameButtons; k++) {
                    TextView key = findViewById(GAME_BUTTONS[k]);
                    key.setText(tileKeysList.get(k).text);
                    // To change the color, go to gametiles Or3
                    String type = tileKeysList.get(k).typeOfThisTileInstance;
                    String typeColor;
                    switch (type) {
                        case "C":
                            typeColor = colorList.get(1);
                            break;
                        case "V":
                            typeColor = colorList.get(2);
                            break;
                        case "T":
                            typeColor = colorList.get(3);
                            break;
                        default:
                            typeColor = colorList.get(4);
                            break;
                    }
                    int tileColor = Color.parseColor(typeColor);
                    key.setBackgroundColor(tileColor);
                }

                if (keysInUse > GAME_BUTTONS.length) {
                    TextView key34 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 2]);
                    key34.setBackgroundResource(R.drawable.zz_backward_inactive);//@tag
                    if (scriptDirection.equals("RTL")) {
                        key34.setRotationY(180);
                    }
                    key34.setText("");
                    TextView key35 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 1]);
                    key35.setBackgroundResource(R.drawable.zz_forward_green);
                    if (scriptDirection.equals("RTL")) {
                        key35.setRotationY(180);
                    }
                    key35.setText("");
                }
                break;
            default:
        }

        if (syllableGame.equals("S")) {
            for (int k = 0; k < syllablesPerPage; k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                if (k < visibleGameButtons) {
                    key.setVisibility(View.VISIBLE);
                    key.setClickable(true);
                } else {
                    key.setVisibility(View.INVISIBLE);
                    key.setClickable(false);
                }
            }
        } else {
            for (int k = 0; k < GAME_BUTTONS.length; k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                if (k < visibleGameButtons) {
                    key.setVisibility(View.VISIBLE);
                    key.setClickable(true);
                } else {
                    key.setVisibility(View.INVISIBLE);
                    key.setClickable(false);
                }
            }
        }
    }

    private void respondToKeySelection(int justClickedIndex) {

        WordPiece clickedKey;
        Tile typedTile;
        String currentWord = "";
        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        if (syllableGame.equals("S")) {
            clickedKey = syllableKeysList.get(justClickedIndex);
            clickedKeys.add(clickedKey);
            for(WordPiece key : clickedKeys) {
                currentWord+= key.text;
            }
        } else {
            if(!(challengeLevel==3)){
                clickedKey = tileKeysList.get(justClickedIndex);
                typedTile = tileKeysList.get(justClickedIndex);
                tilesInBuiltWord.add(typedTile);
                clickedKeys.add(clickedKey);
                currentWord = combineTilesToMakeWord(tilesInBuiltWord, refWord, -1);
            } else {
                clickedKey = new WordPiece(keyList.get(justClickedIndex).text);
                clickedKeys.add(clickedKey);
                currentWord = wordToBuild.getText() + clickedKey.text;
            }

        }

        wordToBuild.setText(currentWord);
        evaluateStatus();
    }

    private void evaluateStatus() {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        String correctString = wordInLOPWithStandardizedSequenceOfCharacters(refWord);
        String currentAttempt;
        if (syllableGame.equals("S") || (syllableGame.equals("T") && challengeLevel == 3)) {
            currentAttempt = wordToBuild.getText().toString();
        } else {
            currentAttempt = combineTilesToMakeWord(tilesInBuiltWord, refWord, -1);
        }

        if (currentAttempt.equals(correctString)) { // Word spelled correctly!
            wordToBuild.setBackgroundColor(Color.parseColor("#4CAF50"));      // theme green
            wordToBuild.setTextColor(Color.parseColor("#FFFFFF")); // white
            for (int i=0; i<visibleGameButtons; i++) {
                TextView key = findViewById(GAME_BUTTONS[i]);
                key.setClickable(false);
            }
            ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
            deleteArrow.setClickable(false);
            recordAttempt(true,4);
            playGameSoundThenActiveWordClip(true,false);
            repeatLocked = false;
            setAdvanceArrowToBlue();

        } else { // Word is partial and, for the moment, assumed to be incorrect
            wordToBuild.setBackgroundColor(Color.parseColor("#A9A9A9")); // gray for wrong
            wordToBuild.setTextColor(Color.parseColor("#000000")); // black

            if (correctString.length() > currentAttempt.length()) {
                ArrayList<WordPiece> firstNCorrectTiles = new ArrayList<>();
                for (int t=0; t<clickedKeys.size(); t++) {
                    if (syllableGame.equals("S")) {
                        if (t<parsedRefWordSyllableArray.size()){
                            firstNCorrectTiles.add(parsedRefWordSyllableArray.get(t));
                        }
                    } else {
                        if (t<parsedRefWordTileArray.size()) {
                            firstNCorrectTiles.add(parsedRefWordTileArray.get(t));
                        }
                    }
                }
                if (currentAttempt.equals(correctString.substring(0, currentAttempt.length()))
                || clickedKeys.equals(firstNCorrectTiles)) { // Word is incomplete but spelled correctly so far
                    // orange=true if there is no key option that would allow the player to continue correctly
                    if (challengeLevel == 1 || challengeLevel == 2 || challengeLevel == 4 ||syllableGame.equals("S")) {
                        // AGH: unclear why CL=3 is excluded, as teams often put complex keys (tiles more or less) in their keyboard
                        boolean orange = false;
                        for (int i = 0; i < clickedKeys.size(); i++) {
                            if (syllableGame.equals("S")) {
                                if (!clickedKeys.get(i).text.equals(parsedRefWordSyllableArray.get(i).text)) {
                                    orange = true;
                                    break;
                                }
                            } else {
                                if (!clickedKeys.get(i).text.equals(parsedRefWordTileArray.get(i).text)) {
                                    orange = true;
                                    break;
                                }
                            }
                        }
                        if (orange) {
                            wordToBuild.setBackgroundColor(Color.parseColor("#F44336")); // orange
                        } else {
                            wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
                        }
                        wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                    } else {
                        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B"));
                        wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                    }
                }
            }

        }
    }

    public void deleteLastKeyed(View view) {

        if (clickedKeys.isEmpty()) {
            return;
        }

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        String typedLettersSoFar = wordToBuild.getText().toString();
        String nowWithOneLessWordPiece = "";

        if (typedLettersSoFar.length() > 0) {
            if (syllableGame.equals("S")
                    || (syllableGame.equals("T") && challengeLevel == 3)) { // Using keyboard keys, not tile texts
                nowWithOneLessWordPiece = typedLettersSoFar.substring(0, typedLettersSoFar.length() - clickedKeys.get(clickedKeys.size()-1).text.length());
            } else if (syllableGame.equals("T")) { // Using tile texts
                tilesInBuiltWord.remove(tilesInBuiltWord.size() - 1);
                nowWithOneLessWordPiece = combineTilesToMakeWord(tilesInBuiltWord, refWord, -1);
            }
            clickedKeys.remove(clickedKeys.size() - 1);
        }

        wordToBuild.setText(nowWithOneLessWordPiece);
        evaluateStatus();

    }

    public void onBtnClick(View view) {

        int justClickedKey = Integer.parseInt((String) view.getTag());
        // Next line says ... if a basic keyboard (which all fits on one screen) or (even when on a complex keyboard) if something other than the last two buttons (the two arrows) are tapped...
        if (syllableGame.equals("S")) {
            if (keysInUse <= syllablesPerPage || justClickedKey <= (syllablesPerPage - 2)) {
                int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
                respondToKeySelection(keyIndex);
            } else {
                // This branch = when a backward or forward arrow is clicked on
                if (justClickedKey == syllablesPerPage - 1) {
                    keyboardScreenNo--;
                    if (keyboardScreenNo < 1) {
                        keyboardScreenNo = 1;
                    }
                }
                if (justClickedKey == syllablesPerPage) {
                    keyboardScreenNo++;
                    if (keyboardScreenNo > totalScreens) {
                        keyboardScreenNo = totalScreens;
                    }
                }
                updateKeyboard();
            }
        } else {
            if (totalScreens == 1 || (totalScreens >= 2 && justClickedKey <= (tilesPerPage - 2))) {
                int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
                respondToKeySelection(keyIndex);
            } else {
                // This branch = when a backward or forward arrow is clicked on
                if (justClickedKey == tilesPerPage - 1) {
                    keyboardScreenNo--;
                    if (keyboardScreenNo < 1) {
                        keyboardScreenNo = 1;
                    }
                }
                if (justClickedKey == tilesPerPage) {
                    keyboardScreenNo++;
                    if (keyboardScreenNo > totalScreens) {
                        keyboardScreenNo = totalScreens;
                    }
                }
                updateKeyboard();
            }
        }

    }

    private void updateKeyboard() { // This routine is only called when there are more keys than will fit on the basic 35-key layout

        int keysLimit;

        TextView key34;
        TextView key35;
        key34 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 2]);
        key35 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 1]);

        // This if block accounts for the partial screen at the end, and also adjusts the arrow color.
        if (totalScreens == keyboardScreenNo) {// on last page

            if (totalScreens > 1) { // if on the last page of a multipage
                key35.setBackgroundResource(R.drawable.zz_forward_inactive);
                key34.setBackgroundResource(R.drawable.zz_backward_green);
            }

            keysLimit = partial;

            for (int k = keysLimit; k < (tilesPerPage - 2); k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                key.setVisibility(View.INVISIBLE);
            }

        } else if (keyboardScreenNo == 1){// on first page

            if (totalScreens > 1) { // if on the first page of a multipage
                key34.setBackgroundResource(R.drawable.zz_backward_inactive);
                key35.setBackgroundResource(R.drawable.zz_forward_green);
            }
            keysLimit = tilesPerPage - 2;

        } else {// in a middle page

            key35.setBackgroundResource(R.drawable.zz_forward_green);
            key34.setBackgroundResource(R.drawable.zz_backward_green);
            keysLimit = tilesPerPage - 2;

        }

        if (scriptDirection.equals("RTL")) {
            key34.setRotationY(180);
            key35.setRotationY(180);
        }

        // This if block resets text and color. It can be refactored,
        // but is more easily worked on this way
        if (challengeLevel == 3) {
            for (int k = 0; k < keysLimit; k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                int keyIndex = (33 * (keyboardScreenNo - 1)) + k;
                key.setText(keyList.get(keyIndex).text); // KRP
                String tileColorStr = colorList.get(Integer.parseInt(keyList.get(keyIndex).color));
                int tileColor = Color.parseColor(tileColorStr);
                key.setBackgroundColor(tileColor);
                key.setVisibility(View.VISIBLE);
            }
        } else {// challengeLevel == 4
            for(int k = 0; k < keysLimit; k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                int keyIndex = (33 * (keyboardScreenNo - 1)) + k;
                key.setText(tileKeysList.get(keyIndex).text);
                String type = tileKeysList.get(keyIndex).typeOfThisTileInstance;
                String typeColor;
                switch (type) {
                    case "C":
                        typeColor = colorList.get(1);
                        break;
                    case "V":
                        typeColor = colorList.get(2);
                        break;
                    case "T":
                        typeColor = colorList.get(3);
                        break;
                    default:
                        typeColor = colorList.get(4);
                        break;
                }
                int tileColor = Color.parseColor(typeColor);
                key.setBackgroundColor(tileColor);
                key.setVisibility(View.VISIBLE);
            }
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
