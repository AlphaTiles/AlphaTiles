package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import static org.alphatilesapps.alphatiles.Start.*;

// JP TO DO:
// 1. FIX SETBOXES() FUNCTION
// 2. FILTER DUPLICATE ANSWER CHOICES

public class Ecuador extends GameActivity {

    int[][] boxCoordinates;   // Will be 8 boxes, defined by 4 parameters each: x1, y1, x2, y2
    int justClickedWord = 0;
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    // # 1 memoryCollection[LWC word, e.g. Spanish]
    // # 2 [LOP word, e.g. Me'phaa]
    // # 3 [state: "TEXT" or "IMAGE"]
    // # 4 [state: "SELECTED" or "UNSELECTED" or "PAIRED"]

    protected static final int[] TILE_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04, R.id.word05, R.id.word06, R.id.word07, R.id.word08
    };

    protected int[] getTileButtons() {
        return TILE_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.ecuadorCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
//          audioInstructionsResID = res.getIdentifier("ecuador_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.ecuador);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.ecuadorCL);
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        visibleTiles = TILE_BUTTONS.length;
        updatePointsAndTrackers(0);
        playAgain();
    }

    @Override
    public void onBackPressed() {
        // no action
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
        Collections.shuffle(wordList); // KP
        setWords();
        setAllTilesClickable();
        setOptionsRowClickable();

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

//        Point xyz = getNavigationBarSize(this);
        int usableWidth = widthDisplay;

        int minX1 = 0;
        int minY1 = (int) (usableHeight * 0.22);    // This is taken from the gridline H2 (20%) plus 2% margin
        int maxX2 = usableWidth;
        int maxY2 = (int) (usableHeight * 0.85);    // This is taken from the gridline H8 (89%) which already has the 2% margin with an extra 4% added in

        int minStartX = minX1;
        int maxStartX = (int) (usableWidth * 0.65);

        int minWidth = (int) (usableWidth * 0.25);      // Height will be equal to (width / hwRatio)
        int maxWidth = (int) (usableWidth * 0.5);

        int bufferX = (int) (usableWidth * 0.05);
        int bufferY = (int) (usableHeight * 0.05);

        final int hwRatio = 4;

        int minStartY = minY1;
        int maxStartY = (int) (usableHeight * 0.75);
        //int maxStartY = (int) (usableHeight * 0.79) - (maxWidth / hwRatio);

        // JP: maxStartY should really be (usableHeight * 0.79) - (maxWidth / hwRatio)
        // to prevent it from going below bottom icons

        boolean verticalOverlap;
        boolean horizontalOverlap;
        boolean overlap;
        boolean outOfBounds;

        int boxWidth;

        Random rand = new Random();

        int extraLoops = 0;
        for (int currentBoxIndex = 0; currentBoxIndex < TILE_BUTTONS.length; currentBoxIndex++) {

            int coordX1 = rand.nextInt((maxStartX - minStartX) + 1) + minStartX;
            int coordY1 = rand.nextInt((maxStartY - minStartY) + 1) + minStartY;
            boxWidth = rand.nextInt((maxWidth - minWidth) + 1) + minWidth;
            int coordX2 = coordX1 + boxWidth;
            int coordY2 = coordY1 + (boxWidth / hwRatio);

            // Check to see if current box overlaps previous boxes or if current box goes out of bounds
            boolean setValues = true;
            if (currentBoxIndex == 0) {
                verticalOverlap = true;
                horizontalOverlap = true;
                overlap = true;
                if ((coordX2 + bufferX) < boxCoordinates[0][0] || (coordX1 - bufferX) > boxCoordinates[0][2]) {
                    horizontalOverlap = false;
                }
                if ((coordY2 + bufferY) < boxCoordinates[0][1] || (coordY1 - bufferY) > boxCoordinates[0][3]) {
                    verticalOverlap = false;
                }
                if (!horizontalOverlap || !verticalOverlap) {
                    overlap = false;
                }

                // Check if current box goes out of bounds
                outOfBounds = false;
                if (coordX2 > maxX2) {
                    outOfBounds = true;
                }
                if (coordY2 > maxY2) {
                    outOfBounds = true;
                }

                if (overlap || outOfBounds) {
                    setValues = false;
                }
            }
            for (int definedBoxIndex = 0; definedBoxIndex < currentBoxIndex; definedBoxIndex++) {
                // So, the very first pass (with currentBoxIndex = 0 and definedBoxIndex = 0), it will skip this loop
                // But this is wrong, because even in the very first pass, you need to check that it is inside bounds

                // Check for overlap of previous boxes
                verticalOverlap = true;
                horizontalOverlap = true;
                overlap = true;
                if ((coordX2 + bufferX) < boxCoordinates[definedBoxIndex][0] || (coordX1 - bufferX) > boxCoordinates[definedBoxIndex][2]) {
                    horizontalOverlap = false;
                }
                if ((coordY2 + bufferY) < boxCoordinates[definedBoxIndex][1] || (coordY1 - bufferY) > boxCoordinates[definedBoxIndex][3]) {
                    verticalOverlap = false;
                }
                if (!horizontalOverlap || !verticalOverlap) {
                    overlap = false;
                }

                // Check if current box goes out of bounds
                outOfBounds = false;
                if (coordX2 > maxX2) {
                    outOfBounds = true;
                }
                if (coordY2 > maxY2) {
                    outOfBounds = true;
                }

                if (overlap || outOfBounds) {
                    setValues = false;
                }
            }
            if (setValues) {
                boxCoordinates[currentBoxIndex][0] = coordX1;
                boxCoordinates[currentBoxIndex][1] = coordY1;
                boxCoordinates[currentBoxIndex][2] = coordX2;
                boxCoordinates[currentBoxIndex][3] = coordY2;
                extraLoops = 0;
            } else {
                if (extraLoops < 10000) {
                    currentBoxIndex = currentBoxIndex - 1;              // force repeat of setting parameters for current box
                    extraLoops++;
                } else {
                    // something has gone horribly wrong and I have no idea how to fix it
                    // other than to start over until we find a config that works
                    currentBoxIndex = 0;
                    extraLoops = 0;
                }
            }
        }

        for (int c = 0; c < TILE_BUTTONS.length; c++) {

            final TextView wordTile = findViewById(TILE_BUTTONS[c]);

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
        for (int c = 0; c < TILE_BUTTONS.length; c++) {
            int wordTile = TILE_BUTTONS[c];
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
            } else if (c == TILE_BUTTONS.length - 1) { // last word tile
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.END, R.id.parent, ConstraintSet.END, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.START, R.id.parent, ConstraintSet.START, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.TOP, TILE_BUTTONS[c - 1], ConstraintSet.BOTTOM, randInt);
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
                constraintSet.connect(wordTile, ConstraintSet.TOP, TILE_BUTTONS[c - 1], ConstraintSet.BOTTOM, randInt);
                randInt = rand.nextInt(100);
                constraintSet.connect(wordTile, ConstraintSet.BOTTOM, TILE_BUTTONS[c + 1], ConstraintSet.TOP, randInt);
                constraintSet.centerHorizontally(wordTile, gameID);
                constraintSet.applyTo(constraintLayout);
            }
        }
    }


    public void setTextBoxColors() {

        for (int w = 0; w < TILE_BUTTONS.length; w++) {

            TextView wordTile = findViewById(TILE_BUTTONS[w]);
            String tileColorStr = COLORS.get(w % 5);
            int tileColor = Color.parseColor(tileColorStr);
            wordTile.setBackgroundColor(tileColor);
            wordTile.setTextColor(Color.parseColor("#FFFFFF")); // white

        }

    }

    public void setWords() {
        chooseWord();

        TextView rightWordTile = findViewById(R.id.activeWordTextView);
        rightWordTile.setText(wordList.stripInstructionCharacters(wordInLOP));
        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC + "2", "drawable", getPackageName());
        image.setImageResource(resID);

        Random rand = new Random();
        int rightWordIndex = rand.nextInt(TILE_BUTTONS.length);
        TextView correctMatchTile = findViewById(TILE_BUTTONS[rightWordIndex]);
        correctMatchTile.setText(wordList.stripInstructionCharacters(wordInLOP));

        ArrayList<String> wordsAlreadyOnTheBoard = new ArrayList<String>();
        wordsAlreadyOnTheBoard.add(wordInLOP);
        for (int w = 0; w < TILE_BUTTONS.length; w++) {
            TextView wordTile = findViewById(TILE_BUTTONS[w]);
            if (w != rightWordIndex) {
                boolean duplicate = true;
                while (duplicate){
                    int randomIndexOfOtherWord = rand.nextInt(cumulativeStageBasedWordList.size());
                    String randomOtherWordInLOP = cumulativeStageBasedWordList.get(randomIndexOfOtherWord).localWord;
                    if (!randomOtherWordInLOP.equals(wordInLOP)){
                        wordTile.setText(wordList.stripInstructionCharacters(randomOtherWordInLOP));
                        duplicate = false;
                    }
                }
            }
        }
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
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
        }

        return size;
    }

    private void respondToWordSelection() {

        int t = justClickedWord - 1; //  justClickedWord uses 1 to 8, t uses the array ID (between [0] and [7]
        TextView chosenWord = findViewById(TILE_BUTTONS[t]);
        String chosenWordText = chosenWord.getText().toString();

        if (chosenWordText.equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
            // Good job!
            repeatLocked = false;
            setAdvanceArrowToBlue();

            updatePointsAndTrackers(2);

            for (int w = 0; w < TILE_BUTTONS.length; w++) {
                TextView nextWord = findViewById(TILE_BUTTONS[w]);
                nextWord.setClickable(false);
                if (w != t) {
                    String wordColorStr = "#A9A9A9"; // dark gray
                    int wordColorNo = Color.parseColor(wordColorStr);
                    nextWord.setBackgroundColor(wordColorNo);
                    nextWord.setTextColor(Color.parseColor("#000000")); // black
                }
            }

            playCorrectSoundThenActiveWordClip(false);

        } else {
            playIncorrectSound();
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
