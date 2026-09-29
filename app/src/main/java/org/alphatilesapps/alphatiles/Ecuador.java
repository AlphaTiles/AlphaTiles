package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

// JP TO DO:
// 1. FIX SETBOXES() FUNCTION
// 2. FILTER DUPLICATE ANSWER CHOICES

public class Ecuador extends GameActivity {
// testing for crawl #2
    int[][] boxCoordinates;   // Will be 8 boxes, defined by 4 parameters each: x1, y1, x2, y2
    int justClickedWord = 0;
    int rightWordIndex;
    ArrayList<Word> wordPool = new ArrayList<>();
    // # 1 memoryCollection[LWC word, e.g. Spanish]
    // # 2 [LOP word, e.g. Me'phaa]
    // # 3 [state: "TEXT" or "IMAGE"]
    // # 4 [state: "SELECTED" or "UNSELECTED" or "PAIRED"]

    protected static final int[] GAME_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04, R.id.word05, R.id.word06, R.id.word07, R.id.word08
    };

    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected void hideInstructionAudioImage() {

        ImageView instructionsButton = findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);
        
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
//          audioInstructionsResID = res.getIdentifier("ecuador_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    private static final int[][] GUIDELINE_MAPPINGS = {
            // Common Horizontal Guidelines
            {R.id.horGuidelineStatusTop, R.dimen.horGuidelineStatusTop},
            {R.id.horGuidelineStatusMiddle, R.dimen.horGuidelineStatusMiddle},
            {R.id.horGuidelineStatusBottom, R.dimen.horGuidelineStatusBottom},
            {R.id.horGuidelineOptionsTop, R.dimen.horGuidelineOptionsTop},
            {R.id.horGuidelineOptionsBottom, R.dimen.horGuidelineOptionsBottom},

            // Specific Horizontal Guidelines
            {R.id.ecuador_horGuidelineRefTop, R.dimen.ecuador_horGuidelineRefTop},
            {R.id.ecuador_horGuidelineRefBottom, R.dimen.ecuador_horGuidelineRefBottom},
            {R.id.ecuador_horGuidelineTextTop, R.dimen.ecuador_horGuidelineTextTop},
            {R.id.ecuador_horGuidelineTextBottom, R.dimen.ecuador_horGuidelineTextBottom},

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
            {R.id.ecuador_verGuidelineRefLeft, R.dimen.ecuador_verGuidelineRefLeft},
            {R.id.ecuador_verGuidelineRefRight, R.dimen.ecuador_verGuidelineRefRight},
            {R.id.ecuador_verGuidelineTextLeft, R.dimen.ecuador_verGuidelineTextLeft},
            {R.id.ecuador_verGuidelineTextRight, R.dimen.ecuador_verGuidelineTextRight}
    };

    private void updateGuidelines() {
        View rootView = findViewById(android.R.id.content);
        GuidelineUtils.applyGuidelines(rootView, this, GUIDELINE_MAPPINGS);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGuidelines();
        setBoxes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.ecuador);
        updateGuidelines();

        ActivityLayouts.applyEdgeToEdge(this, R.id.ecuadorCL);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView repeatImage = findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.ecuadorCL);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        visibleGameButtons = GAME_BUTTONS.length;
        updateView();
        incorrectAnswersSelected = new ArrayList<>(visibleGameButtons-1);
        for (int i = 0; i < visibleGameButtons-1; i++) {
            incorrectAnswersSelected.add("");
        }
        wordPool.addAll(cumulativeStageBasedWordList);
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
        setBoxes();
        setTextBoxColors();
        setWords();
        setAllGameButtonsClickable();
        setOptionsRowClickable();
        for (int i = 0; i < visibleGameButtons-1; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();

    }

    public void setBoxes() {

        boxCoordinates = new int[8][4];

        // JP: DisplayMetrics is deprecated after Android 11
        // must use WindowMetrics instead
        int heightDisplay;
        int widthDisplay;
        int usableHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics displayMetrics = getWindowManager().getCurrentWindowMetrics();
            Insets insets = displayMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            widthDisplay = displayMetrics.getBounds().width() - insets.left - insets.right;
            heightDisplay = displayMetrics.getBounds().height() - insets.top - insets.bottom;
            usableHeight = heightDisplay;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            heightDisplay = displayMetrics.heightPixels;
            widthDisplay = displayMetrics.widthPixels;
            usableHeight = heightDisplay - getNavigationBarSize(this).y;
        }

        int usableWidth = widthDisplay;

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        int minX1;
        int maxX2;
        int minY1;
        int maxY2;
        int minWidth;
        int maxWidth;
        int bufferX;
        int bufferY;

        final int hwRatio = 4;

        if (isLandscape) {
            minX1 = (int) (usableWidth * 0.30);
            maxX2 = (int) (usableWidth * 0.99);
            minY1 = (int) (usableHeight * 0.15);
            maxY2 = (int) (usableHeight * 0.85);

            minWidth = (int) (usableWidth * 0.16);
            maxWidth = (int) (usableWidth * 0.28);

            bufferX = (int) (usableWidth * 0.02);
            bufferY = (int) (usableHeight * 0.02);
        } else {
            minX1 = (int) (usableWidth * 0.02);
            maxX2 = (int) (usableWidth * 0.98);
            minY1 = (int) (usableHeight * 0.20);
            maxY2 = (int) (usableHeight * 0.91);

            minWidth = (int) (usableWidth * 0.28);
            maxWidth = (int) (usableWidth * 0.45);

            bufferX = (int) (usableWidth * 0.03);
            bufferY = (int) (usableHeight * 0.02);
        }

        int minStartX = minX1;
        int maxStartX = maxX2 - minWidth;
        int minStartY = minY1;
        int maxStartY = maxY2 - (minWidth / hwRatio);

        if (maxStartX <= minStartX) maxStartX = minStartX + 1;
        if (maxStartY <= minStartY) maxStartY = minStartY + 1;

        Random rand = new Random();

        int extraLoops = 0;
        for (int currentBoxIndex = 0; currentBoxIndex < GAME_BUTTONS.length; currentBoxIndex++) {

            int coordX1 = rand.nextInt((maxStartX - minStartX) + 1) + minStartX;
            int coordY1 = rand.nextInt((maxStartY - minStartY) + 1) + minStartY;
            int boxWidth = rand.nextInt((maxWidth - minWidth) + 1) + minWidth;
            int coordX2 = coordX1 + boxWidth;
            int coordY2 = coordY1 + (boxWidth / hwRatio);

            boolean valid = true;

            // Check out of bounds
            if (coordX1 < minX1 || coordX2 > maxX2 || coordY1 < minY1 || coordY2 > maxY2) {
                valid = false;
            }

            // Check overlap with previously placed boxes
            if (valid) {
                for (int definedBoxIndex = 0; definedBoxIndex < currentBoxIndex; definedBoxIndex++) {
                    int prevX1 = boxCoordinates[definedBoxIndex][0];
                    int prevY1 = boxCoordinates[definedBoxIndex][1];
                    int prevX2 = boxCoordinates[definedBoxIndex][2];
                    int prevY2 = boxCoordinates[definedBoxIndex][3];

                    boolean overlapX = (coordX1 - bufferX < prevX2) && (coordX2 + bufferX > prevX1);
                    boolean overlapY = (coordY1 - bufferY < prevY2) && (coordY2 + bufferY > prevY1);

                    if (overlapX && overlapY) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                boxCoordinates[currentBoxIndex][0] = coordX1;
                boxCoordinates[currentBoxIndex][1] = coordY1;
                boxCoordinates[currentBoxIndex][2] = coordX2;
                boxCoordinates[currentBoxIndex][3] = coordY2;
                extraLoops = 0;
            } else {
                if (extraLoops < 10000) {
                    currentBoxIndex--; // Try placing this box again
                    extraLoops++;
                } else {
                    // Start over with first box if placement gets stuck
                    currentBoxIndex = -1;
                    extraLoops = 0;
                }
            }
        }

        for (int c = 0; c < GAME_BUTTONS.length; c++) {

            final TextView wordTile = findViewById(GAME_BUTTONS[c]);

            final int finalC = c;
            wordTile.post(new Runnable() {
                @Override
                public void run() {

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) wordTile.getLayoutParams();

                    // X1, Y1, X2, Y2
                    params.width = boxCoordinates[finalC][2] - boxCoordinates[finalC][0];
                    params.height = params.width / hwRatio;
                    wordTile.setLayoutParams(params);

                    wordTile.setX(boxCoordinates[finalC][0]);
                    wordTile.setY(boxCoordinates[finalC][1]);
                }
            });
        }

    }

    // new approach:
    // set constraints dynamically with random start and end margins to parent
    // top and bottom constrained to previous and next words with random margins as well
    public void setBoxesJP() {
        int gameID = R.id.ecuadorCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        Random rand = new Random();
        int randInt = 0;
        for (int c = 0; c < GAME_BUTTONS.length; c++) {
            int wordTile = GAME_BUTTONS[c];
            if (c == 0) { // first word tile
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.END, R.id.parent, ConstraintSet.END, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.START, R.id.parent, ConstraintSet.START, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.TOP, R.id.activeWordTextView, ConstraintSet.BOTTOM, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.BOTTOM, R.id.word02, ConstraintSet.TOP, randInt);
                constraintSet.centerHorizontally(wordTile, gameID);
                constraintSet.applyTo(constraintLayout);
            } else if (c == GAME_BUTTONS.length - 1) { // last word tile
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.END, R.id.parent, ConstraintSet.END, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.START, R.id.parent, ConstraintSet.START, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.TOP, GAME_BUTTONS[c - 1], ConstraintSet.BOTTOM, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.BOTTOM, R.id.guidelineHSys1, ConstraintSet.TOP, randInt);
                constraintSet.centerHorizontally(wordTile, gameID);
                constraintSet.applyTo(constraintLayout);
            } else {
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.END, R.id.parent, ConstraintSet.END, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.START, R.id.parent, ConstraintSet.START, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.TOP, GAME_BUTTONS[c - 1], ConstraintSet.BOTTOM, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.BOTTOM, GAME_BUTTONS[c + 1], ConstraintSet.TOP, randInt);
                constraintSet.centerHorizontally(wordTile, gameID);
                constraintSet.applyTo(constraintLayout);
            }
        }
    }


    public void setTextBoxColors() {

        for (int w = 0; w < GAME_BUTTONS.length; w++) {

            TextView wordTile = findViewById(GAME_BUTTONS[w]);
            String tileColorStr = colorList.get(w % 5);
            int tileColor = Color.parseColor(tileColorStr);
            wordTile.setBackgroundColor(tileColor);
            wordTile.setTextColor(Color.parseColor("#FFFFFF")); // white

        }

    }

    public void setWords() {
        Collections.shuffle(wordPool);
        refWord = wordPool.get(0);
        for (int w = 0; w < GAME_BUTTONS.length; w++) {
            TextView wordTile = findViewById(GAME_BUTTONS[w]);
            Word word = wordPool.get(w + 1);
            wordTile.setText(wordList.stripInstructionCharacters(word.wordInLOP));
        }
        TextView rightWordTile = findViewById(R.id.activeWordTextView);
        rightWordTile.setText(wordList.stripInstructionCharacters(refWord.wordInLOP));
        ImageView image = findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC + "2", "drawable", getPackageName());
        image.setImageResource(resID);

        Random rand = new Random();
        rightWordIndex = rand.nextInt(GAME_BUTTONS.length);
        TextView correctMatchTile = findViewById(GAME_BUTTONS[rightWordIndex]);
        correctMatchTile.setText(wordList.stripInstructionCharacters(refWord.wordInLOP));
    }

    // https://stackoverflow.com/questions/20264268/how-do-i-get-the-height-and-width-of-the-android-navigation-bar-programmatically
    // Code from here used for the three Point methods
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the side
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // API 30+
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            int width = metrics.getBounds().width() - insets.left - insets.right;
            int height = metrics.getBounds().height() - insets.top - insets.bottom;
            return new Point(width, height);
        } else {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;
        }
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // API 30+
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            int width = metrics.getBounds().width();
            int height = metrics.getBounds().height();
            return new Point(width, height);
        } else {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            return size;
        }
    }

    private void respondToWordSelection() {

        int t = justClickedWord - 1; //  justClickedWord uses 1 to 8, t uses the array ID (between [0] and [7]
        TextView chosenWord = findViewById(GAME_BUTTONS[t]);
        String chosenWordText = chosenWord.getText().toString();

        if (chosenWordText.equals(Start.wordList.stripInstructionCharacters(refWord.wordInLOP))) {
            // Good job!

            if (sendAnalytics) {
                // report time and number of incorrect guesses
                String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
                Properties info = new Properties().putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", chosenWordText)
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < visibleGameButtons - 1; i++) {
                    if (!incorrectAnswersSelected.get(i).equals("")) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }

            recordAttempt(true,2);

            endRound(t);

            playGameSoundThenActiveWordClip(true,false);

        } else {
            incorrectOnLevel += 1;
            for (int i = 0; i < visibleGameButtons-1; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(chosenWordText)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, chosenWordText);
                    break;
                }
            }
            recordAttempt(false, 0);
            if(secondChances) {
                playIncorrectSound();
            } else {
                endRound(rightWordIndex);
                playGameSoundThenActiveWordClip(false,false);
            }
        }
    }

    private void endRound(int t) {

        repeatLocked = false;
        setAdvanceArrowToBlue();

        for (int w = 0; w < GAME_BUTTONS.length; w++) {
            TextView nextWord = findViewById(GAME_BUTTONS[w]);
            nextWord.setClickable(false);
            if (w != t) {
                String wordColorStr = "#A9A9A9"; // dark gray
                int wordColorNo = Color.parseColor(wordColorStr);
                nextWord.setBackgroundColor(wordColorNo);
                nextWord.setTextColor(Color.parseColor("#000000")); // black
            }
        }

    }

        public void onWordClick(View view) {
        justClickedWord = Integer.parseInt((String) view.getTag());
        respondToWordSelection();
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
