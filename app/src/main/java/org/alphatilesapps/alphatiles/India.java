package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Crucial import to access Start.sendAnalytics, Start.wordList, etc.
import static org.alphatilesapps.alphatiles.Start.*;

public class India extends GameActivity {

    String correctString;

    protected static final int[] GAME_BUTTONS = {
            R.id.wordText1, R.id.wordText2, R.id.wordText3, R.id.wordText4
    };

    protected static final int[] WORD_IMAGES = {
            R.id.wordImage1, R.id.wordImage2, R.id.wordImage3, R.id.wordImage4
    };

    protected static final int[] PROMPT_TILES = {
            R.id.promptTile01, R.id.promptTile02, R.id.promptTile03,
            R.id.promptTile04, R.id.promptTile05, R.id.promptTile06
    };

    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return WORD_IMAGES;
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
        instructionsButton.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.india);

        ActivityLayouts.applyEdgeToEdge(this, R.id.indiaCL);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView repeatImage = findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.indiaCL);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        visibleGameButtons = GAME_BUTTONS.length;

        incorrectAnswersSelected = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.add("");
        }

        playAgain();
        setUpInitialView();
        updateView();
    }

    public void repeatGame(View view) {
        if (!repeatLocked) {
            playAgain();
        }
    }

    public void playAgain() {
        if (mediaPlayerIsPlaying) {
            return;
        }

        repeatLocked = true;
        setAdvanceArrowToGray();

        // 1. Select a target word with a maximum of 6 tiles (Fail-safe: break after 50 attempts)
        int attempts = 0;
        do {
            chooseWord();
            parsedRefWordTileArray = Start.tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
            attempts++;
        } while (parsedRefWordTileArray.size() > 6 && attempts < 50);

        Word targetWord = refWord;
        correctString = wordList.stripInstructionCharacters(targetWord.wordInLOP);
        Tile refTile = parsedRefWordTileArray.get(0);

        // 2. Load the prompt tiles into the top area
        // Cap the iteration at 6 to prevent an IndexOutOfBoundsException if the fail-safe was tripped
        int tilesToShow = Math.min(parsedRefWordTileArray.size(), 6);
        for (int i = 0; i < PROMPT_TILES.length; i++) {
            ImageView promptTile = findViewById(PROMPT_TILES[i]);
            if (i < tilesToShow) {
                promptTile.setVisibility(View.VISIBLE);
                promptTile.setClickable(true);
            } else {
                promptTile.setVisibility(View.INVISIBLE);
                promptTile.setClickable(false);
            }
        }

        // 3. Decode Challenge Level
        // 11-13: showImages = true; 21-23: showImages = false
        // Second digit (1, 2, or 3): wordSelectionMode for returnFourWords
        int firstDigit = (challengeLevel >= 10) ? (challengeLevel / 10) : (challengeLevel == 2 ? 2 : 1);
        int secondDigit = (challengeLevel >= 10) ? (challengeLevel % 10) : 1;
        boolean showImages = (firstDigit == 1);
        int wordSelectionMode = Math.max(1, Math.min(3, secondDigit));

        // 4. Generate 4 word choices using returnFourWords
        List<Word> wordChoices = wordList.returnFourWords(refWord, refTile, wordSelectionMode, "WORD_TEXT");

        // Restore refWord to the correct target word globally so game logic doesn't break
        refWord = targetWord;

        // 5. Adjust layout constraints based on whether images are shown
        ConstraintLayout constraintLayout = findViewById(R.id.indiaCL);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if (showImages) {
            constraintSet.connect(R.id.wordText1, ConstraintSet.TOP, R.id.horGuidelineTextRowOneTop, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.wordText2, ConstraintSet.TOP, R.id.horGuidelineTextRowOneTop, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.wordText3, ConstraintSet.TOP, R.id.horGuidelineTextRowTwoTop, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.wordText4, ConstraintSet.TOP, R.id.horGuidelineTextRowTwoTop, ConstraintSet.BOTTOM, 0);
        } else {
            constraintSet.connect(R.id.wordText1, ConstraintSet.TOP, R.id.horGuidelineImageRowOneTop, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.wordText2, ConstraintSet.TOP, R.id.horGuidelineImageRowOneTop, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.wordText3, ConstraintSet.TOP, R.id.horGuidelineImageRowTwoTop, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.wordText4, ConstraintSet.TOP, R.id.horGuidelineImageRowTwoTop, ConstraintSet.BOTTOM, 0);
        }
        constraintSet.applyTo(constraintLayout);

        // 6. Populate the 2x2 grid
        for (int i = 0; i < GAME_BUTTONS.length; i++) {
            Word choice = wordChoices.get(i);
            TextView wordText = findViewById(GAME_BUTTONS[i]);
            ImageView wordImage = findViewById(WORD_IMAGES[i]);

            // Setup Text
            wordText.setText(wordList.stripInstructionCharacters(choice.wordInLOP));
            wordText.setBackgroundColor(Color.parseColor(colorList.get(i % 5)));
            wordText.setTextColor(Color.parseColor("#FFFFFF"));
            wordText.setClickable(true);

            // Setup Image
            int resID = getResources().getIdentifier(choice.wordInLWC, "drawable", getPackageName());
            wordImage.setImageResource(resID);
            wordImage.setClickable(true);

            if (showImages) {
                wordImage.setVisibility(View.VISIBLE);
            } else {
                wordImage.setVisibility(View.GONE);
            }
        }

        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();
    }

    public void onPromptTileClick(View view) {
        if (mediaPlayerIsPlaying) {
            return;
        }
        int tileIndex = Integer.parseInt((String) view.getTag()) - 1;
        if (tileIndex < parsedRefWordTileArray.size()) {
            Start.Tile tile = parsedRefWordTileArray.get(tileIndex);
            tileAudioPress(false, tile);
        }
    }

    public void onWordClick(View view) {
        if (mediaPlayerIsPlaying) {
            return;
        }

        int index = Integer.parseInt((String) view.getTag()) - 1;
        TextView chosenWordText = findViewById(GAME_BUTTONS[index]);
        String selectedWord = chosenWordText.getText().toString();

        if (selectedWord.equals(correctString)) {
            // Correct Answer!
            if (sendAnalytics) {
                String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
                Properties info = new Properties().putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", selectedWord)
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < 3; i++) {
                    if (!incorrectAnswersSelected.get(i).isEmpty()) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }

            repeatLocked = false;
            setAdvanceArrowToBlue();
            recordAttempt(true, 1);

            // Dim incorrect options
            for (int w = 0; w < GAME_BUTTONS.length; w++) {
                TextView nextWordText = findViewById(GAME_BUTTONS[w]);
                ImageView nextWordImage = findViewById(WORD_IMAGES[w]);

                nextWordText.setClickable(false);
                nextWordImage.setClickable(false);

                if (w != index) {
                    nextWordText.setBackgroundColor(Color.parseColor("#A9A9A9"));
                    nextWordText.setTextColor(Color.parseColor("#000000"));
                }
            }

            playGameSoundThenActiveWordClip(true, false);

        } else {
            // Incorrect Answer!
            incorrectOnLevel++;
            recordAttempt(false,0);
            for (int i = 0; i < 3; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(selectedWord)) break; // Already selected
                if (item.isEmpty()) {
                    incorrectAnswersSelected.set(i, selectedWord);
                    break;
                }
            }
            playIncorrectSound();
        }
    }

    @Override
    protected void setAllGameButtonsUnclickable() {
        super.setAllGameButtonsUnclickable();
        for (int t = 0; t < PROMPT_TILES.length; t++) {
            ImageView promptTile = findViewById(PROMPT_TILES[t]);
            if (promptTile != null) {
                promptTile.setClickable(false);
            }
        }
    }

    @Override
    protected void setAllGameButtonsClickable() {
        super.setAllGameButtonsClickable();
        // Only make the visible prompt tiles clickable again
        int tilesToShow = Math.min(parsedRefWordTileArray.size(), 6);
        for (int t = 0; t < tilesToShow; t++) {
            ImageView promptTile = findViewById(PROMPT_TILES[t]);
            if (promptTile != null) {
                promptTile.setClickable(true);
            }
        }
    }
}