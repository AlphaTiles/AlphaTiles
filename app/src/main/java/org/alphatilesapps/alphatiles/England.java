package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.alphatilesapps.alphatiles.Start.*;

import androidx.core.content.ContextCompat;

public class England extends GameActivity {
    final int BOARD_SIZE = 16;
    List<Start.Word> gameWords = new ArrayList<>();
    List<Integer> cardOrder = new ArrayList<>();
    boolean[] flippedCards = new boolean[BOARD_SIZE];
    int currentTargetIndex = 0;
    Start.Word firstWord;
    TextView currentActiveWordView = null; // Tracks the currently highlighted word
    int currentRoundIndex = 0; // round of current session; resets when activity is recreated
    String currentRoundColor; // hex color for current round, set once per round

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
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
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

        updatePointsAndTrackers(0);
        playAgain();
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

        List<Start.Word> wordPool = new ArrayList<>(cumulativeStageBasedWordList);
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

        for (int i = 0; i < gameWords.size(); i++) {
            flippedCards[i] = false;
            TextView tile = findViewById(GAME_BUTTONS[i]);
            if (tile != null) {
                tile.setText("");
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
            android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
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
                        int availableWidth = (int) (textView.getWidth() * 0.8);
                        int availableHeight = (int) (textView.getHeight() * 0.8);

                        float ratio = Math.min((float) availableWidth / drawable.getIntrinsicWidth(),
                                (float) availableHeight / drawable.getIntrinsicHeight());

                        int width = (int) (drawable.getIntrinsicWidth() * ratio);
                        int height = (int) (drawable.getIntrinsicHeight() * ratio);

                        drawable.setBounds(0, 0, width, height);
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
                android.animation.ObjectAnimator
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
        playCorrectSoundThenActiveWordClip(isFinalCheck);

        // 3: Delay the revealing of the word/flip until after audio
        long totalAudioDelay = Start.correctSoundDuration + refWord.duration;

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
                            tile.setTextColor(Color.WHITE);

                            // Handle the game logic
                            if (currentTargetIndex < gameWords.size() - 1) {
                                currentTargetIndex++;
                                Start.Word nextWord = gameWords.get(cardOrder.get(currentTargetIndex));
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

                                updatePointsAndTrackers(4); // This locks the UI
                                repeatLocked = false;
                                setAdvanceArrowToBlue();

                                // Manually unlock the UI so the blue arrow works
                                if (Start.after12checkedTrackers == 1 || (trackerCount > 0 && trackerCount % 12 != 0)) {
                                    setOptionsRowClickable();
                                    setAllGameButtonsClickable();
                                }
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