package org.alphatilesapps.alphatiles;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    private static final int[][] GUIDELINE_MAPPINGS = {
            {R.id.horGuidelineStatusTop, R.dimen.horGuidelineStatusTop},
            {R.id.horGuidelineStatusMiddle, R.dimen.horGuidelineStatusMiddle},
            {R.id.horGuidelineStatusBottom, R.dimen.horGuidelineStatusBottom},
            {R.id.horGuidelineOptionsTop, R.dimen.horGuidelineOptionsTop},
            {R.id.horGuidelineOptionsBottom, R.dimen.horGuidelineOptionsBottom},
            {R.id.verGuidelineOptionsLeft, R.dimen.verGuidelineOptionsLeft},
            {R.id.verGuidelineOptionsRight, R.dimen.verGuidelineOptionsRight},
            {R.id.guidelineV1, R.dimen.verGuidelineGameNoLeft},
            {R.id.guidelineV1_1, R.dimen.verGuidelineGameNoCLBorder},
            {R.id.guidelineV1_2, R.dimen.verGuidelineCLStageBorder},
            {R.id.guidelineV1_3, R.dimen.verGuidelineStageBarsBorder},
            {R.id.guidelineV1_4, R.dimen.verGuidelineBarsPointsBorder},
            {R.id.guidelineV2, R.dimen.verGuidelinePointsRight},
            {R.id.horGuidelineRefTop, R.dimen.india_horGuidelineRefTop},
            {R.id.horGuidelineRefBottom, R.dimen.india_horGuidelineRefBottom},
            {R.id.india_horGuidelineImageRowOneTop, R.dimen.india_horGuidelineImageRowOneTop},
            {R.id.india_horGuidelineImageRowOneBottom, R.dimen.india_horGuidelineImageRowOneBottom},
            {R.id.india_horGuidelineTextRowOneTop, R.dimen.india_horGuidelineTextRowOneTop},
            {R.id.india_horGuidelineTextRowOneBottom, R.dimen.india_horGuidelineTextRowOneBottom},
            {R.id.india_horGuidelineImageRowTwoTop, R.dimen.india_horGuidelineImageRowTwoTop},
            {R.id.india_horGuidelineImageRowTwoBottom, R.dimen.india_horGuidelineImageRowTwoBottom},
            {R.id.india_horGuidelineTextRowTwoTop, R.dimen.india_horGuidelineTextRowTwoTop},
            {R.id.india_horGuidelineTextRowTwoBottom, R.dimen.india_horGuidelineTextRowTwoBottom},
            {R.id.india_verChoice1Left, R.dimen.india_verChoice1Left},
            {R.id.india_verChoice1Right, R.dimen.india_verChoice1Right},
            {R.id.india_verChoice2Left, R.dimen.india_verChoice2Left},
            {R.id.india_verChoice2Right, R.dimen.india_verChoice2Right},
            {R.id.india_verChoice3Left, R.dimen.india_verChoice3Left},
            {R.id.india_verChoice3Right, R.dimen.india_verChoice3Right},
            {R.id.india_verChoice4Left, R.dimen.india_verChoice4Left},
            {R.id.india_verChoice4Right, R.dimen.india_verChoice4Right},
            {R.id.guidelineV_t1, R.dimen.india_guidelineV_t1},
            {R.id.guidelineV_t2, R.dimen.india_guidelineV_t2},
            {R.id.guidelineV_t3, R.dimen.india_guidelineV_t3},
            {R.id.guidelineV_t4, R.dimen.india_guidelineV_t4},
            {R.id.guidelineV_t5, R.dimen.india_guidelineV_t5},
            {R.id.guidelineV_t6, R.dimen.india_guidelineV_t6},
            {R.id.guidelineV_t7, R.dimen.india_guidelineV_t7}
    };

    private void updateGuidelines() {
        View rootView = findViewById(android.R.id.content);
        GuidelineUtils.applyGuidelines(rootView, this, GUIDELINE_MAPPINGS);

        int firstDigit = (challengeLevel >= 10) ? (challengeLevel / 10) : (challengeLevel == 2 ? 2 : 1);
        boolean showImages = (firstDigit == 1);

        int orientation = getResources().getConfiguration().orientation;
        boolean isLandscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);

        // Reposition TextViews vertically when images are not shown else position as defined in dimens.xml files
        if (showImages && isLandscape) {
            androidx.constraintlayout.widget.Guideline guide1Top = rootView.findViewById(R.id.india_horGuidelineTextRowOneTop);
            if (guide1Top != null) guide1Top.setGuidelinePercent(0.69f);
            androidx.constraintlayout.widget.Guideline guide1Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowOneBottom);
            if (guide1Bottom != null) guide1Bottom.setGuidelinePercent(0.85f);
            androidx.constraintlayout.widget.Guideline guide2Top = rootView.findViewById(R.id.india_horGuidelineTextRowTwoTop);
            if (guide2Top != null) guide2Top.setGuidelinePercent(0.69f);
            androidx.constraintlayout.widget.Guideline guide2Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowTwoBottom);
            if (guide2Bottom != null) guide2Bottom.setGuidelinePercent(0.85f);
        }
        if (showImages && !isLandscape) {
            androidx.constraintlayout.widget.Guideline guide1Top = rootView.findViewById(R.id.india_horGuidelineTextRowOneTop);
            if (guide1Top != null) guide1Top.setGuidelinePercent(0.45f);
            androidx.constraintlayout.widget.Guideline guide1Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowOneBottom);
            if (guide1Bottom != null) guide1Bottom.setGuidelinePercent(0.55f);
            androidx.constraintlayout.widget.Guideline guide2Top = rootView.findViewById(R.id.india_horGuidelineTextRowTwoTop);
            if (guide2Top != null) guide2Top.setGuidelinePercent(0.81f);
            androidx.constraintlayout.widget.Guideline guide2Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowTwoBottom);
            if (guide2Bottom != null) guide2Bottom.setGuidelinePercent(0.91f);
        }
        if (!showImages && isLandscape) {
            androidx.constraintlayout.widget.Guideline guide1Top = rootView.findViewById(R.id.india_horGuidelineTextRowOneTop);
            if (guide1Top != null) guide1Top.setGuidelinePercent(0.51f);
            androidx.constraintlayout.widget.Guideline guide1Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowOneBottom);
            if (guide1Bottom != null) guide1Bottom.setGuidelinePercent(0.67f);
            androidx.constraintlayout.widget.Guideline guide2Top = rootView.findViewById(R.id.india_horGuidelineTextRowTwoTop);
            if (guide2Top != null) guide2Top.setGuidelinePercent(0.51f);
            androidx.constraintlayout.widget.Guideline guide2Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowTwoBottom);
            if (guide2Bottom != null) guide2Bottom.setGuidelinePercent(0.67f);        }
        if (!showImages && !isLandscape) {
            androidx.constraintlayout.widget.Guideline guide1Top = rootView.findViewById(R.id.india_horGuidelineTextRowOneTop);
            if (guide1Top != null) guide1Top.setGuidelinePercent(0.29f);
            androidx.constraintlayout.widget.Guideline guide1Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowOneBottom);
            if (guide1Bottom != null) guide1Bottom.setGuidelinePercent(0.39f);
            androidx.constraintlayout.widget.Guideline guide2Top = rootView.findViewById(R.id.india_horGuidelineTextRowTwoTop);
            if (guide2Top != null) guide2Top.setGuidelinePercent(0.44f);
            androidx.constraintlayout.widget.Guideline guide2Bottom = rootView.findViewById(R.id.india_horGuidelineTextRowTwoBottom);
            if (guide2Bottom != null) guide2Bottom.setGuidelinePercent(0.54f);
        }


    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGuidelines();
    }

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

        updateGuidelines();

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
        if (mediaPlayerIsPlaying || !repeatLocked) {
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

            recordAttempt(true, 2);
            endRound();
            playGameSoundThenActiveWordClip(true, false);

        } else {
            // Incorrect Answer!
            incorrectOnLevel++;
            for (int i = 0; i < 3; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(selectedWord)) break; // Already selected
                if (item.isEmpty()) {
                    incorrectAnswersSelected.set(i, selectedWord);
                    break;
                }
            }
            recordAttempt(false, 0);
            if (secondChances) {
                playIncorrectSound();
            } else {
                endRound();
                playGameSoundThenActiveWordClip(false, false);
            }
        }
    }

    private void endRound() {

        repeatLocked = false;
        setAdvanceArrowToBlue();

        for (int w = 0; w < GAME_BUTTONS.length; w++) {
            TextView nextWord = findViewById(GAME_BUTTONS[w]);
            if (!nextWord.getText().toString().equals(correctString)) {
                String wordColorStr = "#A9A9A9"; // dark gray
                int wordColorNo = Color.parseColor(wordColorStr);
                nextWord.setBackgroundColor(wordColorNo);
                nextWord.setTextColor(Color.parseColor("#000000")); // black

            }
        }

    }
}