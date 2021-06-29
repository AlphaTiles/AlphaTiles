package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.segment.analytics.Analytics;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

public class Ecuador extends GameActivity {

    ArrayList<String[]> wordListArray; // KP // the full list of words
    ArrayList<String[]> searchCollection = new ArrayList(); // KP // the correct word plus the seven incorrect words

    int[][] boxCoordinates;   // Will be 8 boxes, defined by 4 parameters each: x1, y1, x2, y2
    int justClickedWord = 0;
    // # 1 memoryCollection[LWC word, e.g. Spanish]
    // # 2 [LOP word, e.g. Me'phaa]
    // # 3 [state: "TEXT" or "IMAGE"]
    // # 4 [state: "SELECTED" or "UNSELECTED" or "PAIRED"]

    protected static final int[] TILE_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04, R.id.word05, R.id.word06, R.id.word07, R.id.word08
    };

    protected int[] getTileButtons() {return TILE_BUTTONS;}

    protected int[] getWordImages() {return null;}

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger(Ecuador.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.ecuador);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP

        wordListArray = new ArrayList(); // KP

        visibleTiles = TILE_BUTTONS.length;

        setTitle(Start.localAppName + ": " + gameNumber);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void repeatGame(View View) {

//        if (!repeatLocked) {
            playAgain();
//        }

    }

    public void playAgain() {

        repeatLocked = true;
        setBoxes();
        setTextSizes();
        setTextBoxColors();
        buildWordsArray();
        Collections.shuffle(wordListArray); // KP
        setWords();
        setAllTilesClickable();
        setOptionsRowClickable();

    }

    public void setBoxes() {

        boxCoordinates = new int[8][4];

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightDisplay = displayMetrics.heightPixels;
        int widthDisplay = displayMetrics.widthPixels;
        LOGGER.info("Remember: heightDisplay = " + heightDisplay);
        LOGGER.info("Remember: widthDisplay = " + widthDisplay);

//        Point xyz = getNavigationBarSize(this);
        int usableHeight = heightDisplay - getNavigationBarSize(this).y;
        int usableWidth = widthDisplay;
        LOGGER.info("Remember: usableHeight = " + usableHeight);
        LOGGER.info("Remember: usableWidth = " + usableWidth);
        LOGGER.info("Remember: NavigationBarHeight = " + (heightDisplay - usableHeight));

        int minX1 = 0;
        int minY1 = (int) (usableHeight * 0.22);    // This is taken from the gridline H2 (20%) plus 2% margin
        int maxX2 = usableWidth;
        int maxY2 = (int) (usableHeight * 0.85);    // This is taken from the gridline H8 (89%) which already has the 2% margin with an extra 4% added in
        
        int minStartX = minX1;
        int maxStartX = (int) (usableWidth * 0.65);

        int minStartY = minY1;
        int maxStartY = (int) (usableHeight * 0.79);

        int minWidth = (int) (usableWidth * 0.25);      // Height will be equal to (width / hwRatio)
        int maxWidth = (int) (usableWidth * 0.5);

        int bufferX = (int) (usableWidth * 0.05);
        int bufferY = (int) (usableHeight * 0.05);

        LOGGER.info("Remember: minX1 = " + minX1);
        LOGGER.info("Remember: maxX2 = " + maxX2);
        LOGGER.info("Remember: minY1 = " + minY1);
        LOGGER.info("Remember: maxY2 = " + maxY2);

        final int hwRatio = 4;

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
            for (int definedBoxIndex = 0; definedBoxIndex < currentBoxIndex; definedBoxIndex++) {
                // So, the very first pass (with currentBoxIndex = 0 and definedBoxIndex = 0), it will skip this loop
                // But this is wrong, because even in the very first pass, you need to check that it is inside bounds

                // Check for overlap of previous boxes
                verticalOverlap = true;
                horizontalOverlap = true;
                overlap = true;
                if ((coordX2 + bufferX) < boxCoordinates[definedBoxIndex][0] || (coordX1 - bufferX) > boxCoordinates[definedBoxIndex][2]) {horizontalOverlap = false;}
                if ((coordY2 + bufferY) < boxCoordinates[definedBoxIndex][1] || (coordY1 - bufferY) > boxCoordinates[definedBoxIndex][3]) {verticalOverlap = false;}
                if (!horizontalOverlap || !verticalOverlap) {overlap = false;}

                // Check if current box goes out of bounds
                outOfBounds = false;
                if (coordX2 > maxX2) {outOfBounds = true;}
                if (coordY2 > maxY2) {outOfBounds = true;}

//                LOGGER.info("Remember: overlap = " + overlap + " and outOfBounds = " + outOfBounds);

                if (overlap || outOfBounds) {
                    setValues = false;
                }
            }
            if(setValues) {
                boxCoordinates[currentBoxIndex][0] = coordX1;
                boxCoordinates[currentBoxIndex][1] = coordY1;
                boxCoordinates[currentBoxIndex][2] = coordX2;
                boxCoordinates[currentBoxIndex][3] = coordY2;
            } else {
                if (extraLoops < 10000) {
                    currentBoxIndex = currentBoxIndex - 1;              // force repeat of setting parameters for current box
                    extraLoops++;
                }
            }
//            LOGGER.info("Remember: currentBoxIndex =" + currentBoxIndex + " and extraLoops = " + extraLoops);
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
                    LOGGER.info("Remember: " + wordTile.getText() + ": X = (" + boxCoordinates[finalC][0] + "-" + boxCoordinates[finalC][2] + "), Y = (" + boxCoordinates[finalC][1] + "-" + boxCoordinates[finalC][3] + ")");
                }
            });
        }

    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        int pixelHeight;
        double scaling = 0.45;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int w = 0; w < TILE_BUTTONS.length; w++) {

            TextView wordTile = findViewById(TILE_BUTTONS[w]);
            pixelHeight = (int) (0.5 * (boxCoordinates[w][3] - boxCoordinates[w][1]));
            wordTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

            int[] location = new int[2];
            wordTile.getLocationOnScreen(location);
            LOGGER.info("Remember: for box index [" + w + "], (x,y) = (" + location[0] + ", " + location[1] + ")");

        }

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.7 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void setTextBoxColors() {

        for (int w = 0; w < TILE_BUTTONS.length; w++) {

            TextView wordTile = findViewById(TILE_BUTTONS[w]);
            String tileColorStr = COLORS[w % 5];
            int tileColor = Color.parseColor(tileColorStr);
            wordTile.setBackgroundColor(tileColor);
            wordTile.setTextColor(Color.parseColor("#FFFFFF")); // white

        }

    }

    public void buildWordsArray() {

        // This array will store every word available in wordlist.txt
        // Subsequently, this will be narrowed down to 8 words for use in the Where's Waldo word game
        // updated module by KP, Oct 2020
        // AH, Nov 2020, revised to allow for spaces in words

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_wordlist));
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }     // skip the header row

        while (scanner.hasNextLine()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            wordListArray.add(thisLineArray);
        }

    }

    public void setWords() {
        Random rand = new Random();
        int min = 0;
        int max = TILE_BUTTONS.length - 1;
        int rightWordIndex = rand.nextInt((max - min) + 1) + min;
        TextView rightWordTile = findViewById(R.id.activeWordTextView);
        wordInLOP = wordListArray.get(rightWordIndex)[1];
        wordInLWC = wordListArray.get(rightWordIndex)[0];
        rightWordTile.setText(Start.wordList.stripInstructionCharacters(wordInLOP));

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordListArray.get(rightWordIndex)[0] + "2", "drawable", getPackageName());
        image.setImageResource(resID);

        for (int w = 0; w < TILE_BUTTONS.length; w++ ) {
            TextView wordTile = findViewById(TILE_BUTTONS[w]);
            wordTile.setText(Start.wordList.stripInstructionCharacters(wordListArray.get(w)[1]));
            if (w != rightWordIndex) {
//                wordTile.setText(w + " " + wordListArray[w][1]);  // for testing purposes, make right answer clear on screen
                wordTile.setText(Start.wordList.stripInstructionCharacters(wordListArray.get(w)[1]));
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
            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
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

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=2;
            pointsEarned.setText(String.valueOf(points));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int w = 0; w < TILE_BUTTONS.length; w++ ) {
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

    public void onWordClick (View view) {
        justClickedWord = Integer.parseInt((String)view.getTag());
        respondToWordSelection();
    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

}
