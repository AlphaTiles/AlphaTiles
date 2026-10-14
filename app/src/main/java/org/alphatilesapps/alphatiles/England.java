package org.alphatilesapps.alphatiles;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.alphatilesapps.alphatiles.Start.*;

import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.core.content.ContextCompat;

public class England extends GameActivity {
    final int BOARD_SIZE = 16;
    List<Word> gameWords = new ArrayList<>();
    List<Integer> cardOrder = new ArrayList<>();
    boolean[] flippedCards = new boolean[BOARD_SIZE];
    int currentTargetIndex = 0;
    Word firstWord;
    TextView currentActiveWordView = null; // Tracks the currently highlighted word
    int currentRoundIndex = 0; // round of current session; resets when activity is recreated
    String currentRoundColor; // hex color for current round, set once per round

    private static final int[][] GUIDELINE_MAPPINGS = {
            {R.id.guidelineHneg1, R.dimen.horGuidelineStatusTop},
            {R.id.guidelineHneg1_1, R.dimen.horGuidelineStatusMiddle},
            {R.id.guidelineH0, R.dimen.horGuidelineStatusBottom},
            {R.id.guidelineH1, R.dimen.england_horGuidelineH1},
            {R.id.guidelineH2, R.dimen.england_horGuidelineH2},
            {R.id.guidelineH3, R.dimen.england_horGuidelineH3},
            {R.id.england_horGridBottom, R.dimen.england_horGridBottom},
            {R.id.guidelineHSys1, R.dimen.horGuidelineOptionsTop},
            {R.id.guidelineHSys2, R.dimen.horGuidelineOptionsBottom},
            {R.id.horGuidelineOptionsTop, R.dimen.horGuidelineOptionsTop},
            {R.id.horGuidelineOptionsBottom, R.dimen.horGuidelineOptionsBottom},
            {R.id.guidelineV1, R.dimen.verGuidelineGameNoLeft},
            {R.id.guidelineV1_1, R.dimen.verGuidelineGameNoCLBorder},
            {R.id.guidelineV1_2, R.dimen.verGuidelineCLStageBorder},
            {R.id.guidelineV1_3, R.dimen.verGuidelineStageBarsBorder},
            {R.id.guidelineV1_4, R.dimen.verGuidelineBarsPointsBorder},
            {R.id.guidelineV5, R.dimen.verGuidelinePointsRight},
            {R.id.verGuidelineOptionsLeft, R.dimen.verGuidelineOptionsLeft},
            {R.id.verGuidelineOptionsRight, R.dimen.verGuidelineOptionsRight},
            {R.id.england_verWordBoxLeft, R.dimen.england_verWordBoxLeft},
            {R.id.england_verWordBoxRight, R.dimen.england_verWordBoxRight},
            {R.id.england_verGridLeft, R.dimen.england_verGridLeft},
            {R.id.england_verGridRight, R.dimen.england_verGridRight}
    };

    private void updateGuidelines() {
        View rootView = findViewById(android.R.id.content);
        GuidelineUtils.applyGuidelines(rootView, this, GUIDELINE_MAPPINGS);

        Flow flowGrid = findViewById(R.id.flowGrid);
        if (flowGrid != null) {
            int maxElements = getResources().getInteger(R.integer.england_flowMaxElementsWrap);
            flowGrid.setMaxElementsWrap(maxElements);
        }

        rescaleCardImages();
    }

    private void rescaleCardImages() {
        if (gameWords == null || gameWords.isEmpty()) return;

        for (int i = 0; i < gameWords.size() && i < GAME_BUTTONS.length; i++) {
            if (!flippedCards[i]) {
                TextView tile = findViewById(GAME_BUTTONS[i]);
                if (tile != null) {
                    int resID = getResources().getIdentifier(gameWords.get(i).wordInLWC, "drawable", getPackageName());
                    if (resID != 0) {
                        setScaledImage(tile, resID);
                    }
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGuidelines();
    }

    protected static final int[] GAME_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04,
            R.id.choice05, R.id.choice06, R.id.choice07, R.id.choice08,
            R.id.choice09, R.id.choice10, R.id.choice11, R.id.choice12,
            R.id.choice13, R.id.choice14, R.id.choice15, R.id.choice16
    };

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
            audioInstructionsResID = res.getIdentifier(gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void hideInstructionAudioImage() {
        ImageView instructionsButton = findViewById(R.id.instructions);
        if (instructionsButton != null) {
            instructionsButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.england);
        updateGuidelines();

        ActivityLayouts.applyEdgeToEdge(this, R.id.englandCL);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView repeatImage = findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.englandCL);
        }

        visibleGameButtons = BOARD_SIZE;
        incorrectAnswersSelected = new ArrayList<>(visibleGameButtons);
        for (int i = 0; i < visibleGameButtons; i++) {
            incorrectAnswersSelected.add("");
        }

        // Hide the instruction button if there is no audio file
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
        currentTargetIndex = 0;
        gameWords.clear();
        cardOrder.clear();

        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();
        for (int i = 0; i < visibleGameButtons; i++) {
            incorrectAnswersSelected.set(i, "");
        }

        List<Word> wordPool = new ArrayList<>(cumulativeStageBasedWordList);
        if (wordPool.isEmpty()) {
            wordPool = new ArrayList<>(wordList);
        }

        if (wordPool.isEmpty()) {
            goBackToEarth(null);
            return;
        }

        currentRoundColor = colorList.get(currentRoundIndex % colorList.size());
        currentRoundIndex++;

        Collections.shuffle(wordPool);
        for (int i = 0; i < BOARD_SIZE && i < wordPool.size(); i++) {
            gameWords.add(wordPool.get(i));
            cardOrder.add(i);
        }
        Collections.shuffle(cardOrder);

        firstWord = gameWords.get(cardOrder.get(0));
        refWord = firstWord;

        currentActiveWordView = null; // Reset tracker for a new game
        TextView targetBox = findViewById(R.id.targetWordBox);
        if (targetBox != null) {
            targetBox.setText(wordList.stripInstructionCharacters(firstWord.wordInLOP));
            highlightActiveWord(targetBox); // Apply the border!
        }

        int padPx = (int) (4 * context.getResources().getDisplayMetrics().density);
        for (int i = 0; i < gameWords.size(); i++) {
            flippedCards[i] = false;
            TextView tile = findViewById(GAME_BUTTONS[i]);
            if (tile != null) {
                tile.setText("");
                tile.setPadding(padPx, padPx, padPx, padPx);
                tile.setBackgroundResource(R.drawable.tile_white_background);
                if (tile.getBackground() != null) {
                    tile.getBackground().mutate().clearColorFilter();
                }

                int resID = getResources().getIdentifier(gameWords.get(i).wordInLWC, "drawable", getPackageName());
                if (resID != 0) {
                    setScaledImage(tile, resID);
                }
            }
        }

        // Unlocks the UI instead of playing the first word audio
        setAllGameButtonsClickable();
        setOptionsRowClickable();
    }

    private void highlightActiveWord(TextView newActiveView) {
        // 1. Remove the border from the PREVIOUS active view
        if (currentActiveWordView != null) {
            currentActiveWordView.setBackgroundResource(R.drawable.tile_white_background);
            if (currentActiveWordView.getBackground() != null) {
                // .mutate() is crucial here so we don't accidentally turn ALL tiles green!
                currentActiveWordView.getBackground().mutate().setColorFilter(Color.parseColor(currentRoundColor), PorterDuff.Mode.SRC_ATOP);
            }
        }

        // 2. Update the tracker
        currentActiveWordView = newActiveView;

        // 3. Apply the elegant black border to the NEW active view
        if (currentActiveWordView != null) {
            GradientDrawable border = new GradientDrawable();
            border.setColor(Color.parseColor(currentRoundColor)); // Round-specific background color
            border.setStroke(8, Color.BLACK); // 8-pixel elegant black border

            // Convert 2dp to pixels to perfectly match tile_white_background.xml
            float density = context.getResources().getDisplayMetrics().density;
            border.setCornerRadius(2f * density);

            currentActiveWordView.setBackground(border);
        }
    }

    private void setScaledImage(final TextView textView, final int resID) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable drawable = ContextCompat.getDrawable(context, resID);
                    if (drawable != null) {
                        int availableWidth = (int) (textView.getWidth() * 0.75);
                        int availableHeight = (int) (textView.getHeight() * 0.75);

                        if (availableWidth <= 0 || availableHeight <= 0) return;

                        float ratio = Math.min((float) availableWidth / drawable.getIntrinsicWidth(),
                                (float) availableHeight / drawable.getIntrinsicHeight());

                        int width = Math.max(1, (int) (drawable.getIntrinsicWidth() * ratio));
                        int height = Math.max(1, (int) (drawable.getIntrinsicHeight() * ratio));

                        drawable.setBounds(0, 0, width, height);

                        int topPadding = Math.max(0, (textView.getHeight() - height) / 2);
                        textView.setPadding(0, topPadding, 0, 0);
                        textView.setCompoundDrawables(null, drawable, null, null);
                    }
                } catch (Resources.NotFoundException e) {
                    // Fail silently
                }
            }
        });
    }

    public void onBtnClick(View view) {
        if (mediaPlayerIsPlaying || view.getTag() == null) return;
        int cardNo = Integer.parseInt(view.getTag().toString());
        respondToCardSelection(cardNo);
    }

    private void respondToCardSelection(int justClickedCard) {
        int index = justClickedCard - 1;
        if (index >= gameWords.size() || flippedCards[index]) return;

        if (gameWords.get(index).wordInLOP.equals(refWord.wordInLOP)) {
            flippedCards[index] = true;
            respondToCorrectSelection(index);
        } else {
            incorrectOnLevel++;
            playIncorrectSound();

            // The Wobble Animation
            View wrongTile = findViewById(GAME_BUTTONS[index]);
            if (wrongTile != null) {
                ObjectAnimator
                        .ofFloat(wrongTile, "translationX", 0, 15, -15, 15, -15, 5, -5, 0)
                        .setDuration(400)
                        .start();
            }
        }
    }

    private void respondToCorrectSelection(int index) {
        final TextView tile = findViewById(GAME_BUTTONS[index]);

        // 1 & 2: Play Correct Sound followed by Word Audio
        boolean isFinalCheck = (currentTargetIndex >= gameWords.size() - 1);
        playGameSoundThenActiveWordClip(true, isFinalCheck);

        // 3: Delay the revealing of the word/flip until after audio
        long totalAudioDelay = correctSoundDuration + refWord.duration;

        soundSequencer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tile != null) {
                    // Step 1: Animate the card to 90 degrees (edge-on)
                    tile.animate().rotationY(90f).setDuration(150).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            // Step 2: Swap the visuals while the card is invisible
                            tile.setCompoundDrawables(null, null, null, null);
                            int padPx = (int) (4 * context.getResources().getDisplayMetrics().density);
                            tile.setPadding(padPx, padPx, padPx, padPx);
                            tile.setTextColor(Color.WHITE);

                            // Handle the game logic
                            if (currentTargetIndex < gameWords.size() - 1) {
                                currentTargetIndex++;
                                Word nextWord = gameWords.get(cardOrder.get(currentTargetIndex));
                                tile.setText(wordList.stripInstructionCharacters(nextWord.wordInLOP));
                                refWord = nextWord;
                                highlightActiveWord(tile); // Move the black border here!
                            } else {
                                tile.setText("✓");
                                highlightActiveWord(null); // Clear the border when the game is won
                                tile.setBackgroundResource(R.drawable.tile_white_background);
                                if (tile.getBackground() != null) {
                                    tile.getBackground().mutate().setColorFilter(Color.parseColor(currentRoundColor), PorterDuff.Mode.SRC_ATOP);
                                }

                                repeatLocked = false;
                                setAdvanceArrowToBlue();
                                recordAttempt(true, gameWords.size());
                            }

                            // Step 3: Snap to -90 degrees and animate back to 0 to complete the flip
                            tile.setRotationY(-90f);
                            tile.animate().rotationY(0f).setDuration(150).start();
                        }
                    }).start();
                }
            }
        }, totalAudioDelay);
    }

    public void onRefClick(View view) {
        playActiveWordClip(false);
    }

    public void repeatGame(View view) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    @Override
    public void setAllGameButtonsUnclickable() {
        super.setAllGameButtonsUnclickable();
        ImageView repeatImage = findViewById(R.id.repeatImage);
        if (repeatImage != null) repeatImage.setClickable(false);
    }

    @Override
    public void setAllGameButtonsClickable() {
        super.setAllGameButtonsClickable();
        ImageView repeatImage = findViewById(R.id.repeatImage);
        if (repeatImage != null) repeatImage.setClickable(true);
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

}