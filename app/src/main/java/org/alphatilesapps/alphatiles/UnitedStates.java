package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Random;
import java.util.logging.Logger;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.graphics.Typeface;
import android.widget.Button;

public class UnitedStates extends GameActivity {

    int upperTileLimit = 5;
    int neutralFontSize;
    int tileButtonCount;
    String scriptLR;
    String[] selections = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}; // KP

    private static final int[] TILE_BUTTONS = {
            R.id.button01a, R.id.button01b, R.id.button02a, R.id.button02b, R.id.button03a, R.id.button03b, R.id.button04a, R.id.button04b, R.id.button05a, R.id.button05b,
            R.id.button06a, R.id.button06b, R.id.button07a, R.id.button07b, R.id.button08a, R.id.button08b, R.id.button09a, R.id.button09b
    };

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger(UnitedStates.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        setTitle(Start.localAppName + ": " + gameNumber);

        switch (challengeLevel) {
            case 2:
                setContentView(R.layout.united_states_cl2);
                upperTileLimit = 7;
                neutralFontSize = 24;
                break;
            case 3:
                setContentView(R.layout.united_states_cl3);
                upperTileLimit = 9;
                neutralFontSize = 18;
                break;
            default:
                setContentView(R.layout.united_states_cl1);
                upperTileLimit = 5;
                neutralFontSize = 30;
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        scriptLR = Start.langInfoList.find("Script direction (LR or RL)");

        playAgain();

        setTextSizes();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        int pixelHeight = 0;
        double scaling = 0.45;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int t = 0; t < tileButtonCount; t++) {

            LOGGER.info("Remember: t = " + t);

            TextView tileButton = findViewById(TILE_BUTTONS[t]);
            if (t == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) tileButton.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            tileButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        }

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) wordToBuild.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        wordToBuild.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

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

    public void repeatGame (View view) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain () {

        repeatLocked = true;

        selections = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}; // KP


        int lengthOfLOPWord = Integer.MAX_VALUE;  // KP // Arbitrarily high number to force into the loop

        while (lengthOfLOPWord > upperTileLimit) {
            // Ensure that the selected word is not too long for a 5/7/9-tile max game
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size()); // KP
            wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
            wordInLOP = Start.wordList.get(randomNum).localWord; // KP
            parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
            lengthOfLOPWord = parsedWordArrayFinal.size(); // KP
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        switch (challengeLevel)
        {
            case 2:
                tileButtonCount = 14;   // RR
                break;
            case 3:
                tileButtonCount = 18;   // RR
                break;
            default:
                tileButtonCount = 10;   // RR
        }

        int c = 0;      // iterate through the parsedWordArray2
        int randomNum2;
        int correspondingRow = 0;

        for (int b = 0; b < tileButtonCount; b+=2 ) {

            int bLRRL;
            if (scriptLR.equals("RL")) {
                bLRRL = tileButtonCount - 2 - b;
            } else {
                bLRRL = b;
            }

            Button tileButtonA = (Button) findViewById(TILE_BUTTONS[bLRRL]);
            Button tileButtonB = (Button) findViewById(TILE_BUTTONS[bLRRL + 1]);

            String tileColorStr = COLORS[(b % 5) / 2];
            int tileColor = Color.parseColor(tileColorStr);
            tileButtonA.setBackgroundColor(tileColor);
            tileButtonB.setBackgroundColor(tileColor);
            tileButtonA.setTextColor(Color.parseColor("#FFFFFF")); // white
            tileButtonB.setTextColor(Color.parseColor("#FFFFFF")); // white

            tileButtonA.setClickable(true);
            tileButtonB.setClickable(true);

            if (c < parsedWordArrayFinal.size()) {

                if ((parsedWordArrayFinal.get(c) == null) || (parsedWordArrayFinal.get(c).isEmpty())) {
                    c = Util.parsedWordTileLength;
                } else {
                    for (int d = 0; d < Start.tileList.size(); d++) {
                        if (Start.tileList.get(d).baseTile.equals(parsedWordArrayFinal.get(c))) {
                            correspondingRow = d;
                            break;
                        }
                    }
                }

                Random rand = new Random();
                int randomNum = rand.nextInt(2); // choosing whether correct tile goes above ( =0 ) or below ( =1 )
                randomNum2 = rand.nextInt(Start.ALT_COUNT); // KP // choosing between 2nd and 4th item of game tiles array
                if (randomNum == 0) {
                    tileButtonA.setText(parsedWordArrayFinal.get(c));   // the correct tile
                    tileButtonB.setText(Start.tileList.get(correspondingRow).altTiles[randomNum2]);   // the (incorrect) suggested alternative
                } else {
                    tileButtonA.setText(Start.tileList.get(correspondingRow).altTiles[randomNum2]);   // the (incorrect) suggested alternative
                    tileButtonB.setText(parsedWordArrayFinal.get(c));   // the correct tile
                }
                tileButtonA.setVisibility(View.VISIBLE);
                tileButtonB.setVisibility(View.VISIBLE);
            } else {
                tileButtonA.setText("");
                tileButtonB.setText("");
                tileButtonA.setVisibility(View.INVISIBLE);
                tileButtonB.setVisibility(View.INVISIBLE);
            }

            c++;
        }

        TextView constructedWord = findViewById(R.id.activeWordTextView); // KP
        constructedWord.setText(""); // KP

    }

    public void buildWord (String tileNumber, String tileString) {

        LOGGER.info("Remember: 38");
        TextView constructedWord = findViewById(R.id.activeWordTextView);
        LOGGER.info("Remember: 39");

        StringBuilder displayedWord = new StringBuilder(); // KP

        LOGGER.info("Remember: 45");

        // KP
        if(scriptLR.equals("RL")) {
            for (int j = selections.length - 1; j >= 0; j--) {

                if (!selections[j].equals("")) {

                    displayedWord.append(selections[j]);

                }
            }
        } else {
            for (int j = 0; j < selections.length; j++) {

                if (!selections[j].equals("")) {

                    displayedWord.append(selections[j]);

                }
            }
        }

        LOGGER.info("Remember: 55");

        constructedWord.setText(displayedWord);

        if (displayedWord.toString().equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {

            // Good job!
            repeatLocked = false;
            constructedWord.setTextColor(Color.parseColor("#006400")); // dark green
            constructedWord.setTypeface(constructedWord.getTypeface(), Typeface.BOLD);

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points +=2;
            pointsEarned.setText(String.valueOf(points));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply(); // requires API 9 as per https://developer.android.com/reference/android/content/SharedPreferences.Editor

            playCorrectSoundThenActiveWordClip();

        } else {
            constructedWord.setTextColor(Color.BLACK);
            constructedWord.setTypeface(constructedWord.getTypeface(), Typeface.NORMAL);
        }
    }

    public void onBtnClick(View view) {

        int justClickedTile = Integer.parseInt((String)view.getTag());
        int tileIndex = justClickedTile - 1; //  justClickedTile uses 1 to 10 (or 14 or 18), tileNo uses the array ID (between [0] and [9] (or [13] or [17])
        int otherTileIndex; // the corresponding tile that is above or below the justClickedTile
        if (justClickedTile % 2 == 0) {
            otherTileIndex = tileIndex - 1;
        } else {
            otherTileIndex = tileIndex + 1;
        }

        Button tile = findViewById(TILE_BUTTONS[tileIndex]);
        Button otherTile = findViewById(TILE_BUTTONS[otherTileIndex]);

        String tileColorStr = COLORS[(tileIndex / 2) % 5];
        int tileColorNo = Color.parseColor(tileColorStr);
        tile.setBackgroundColor(tileColorNo);
        tile.setTextColor(Color.parseColor("#FFFFFF")); // white

        String tileColorStr2 = "#A9A9A9"; // dark gray
        int tileColorNo2 = Color.parseColor(tileColorStr2);
        otherTile.setBackgroundColor(tileColorNo2);
        otherTile.setTextColor(Color.parseColor("#000000")); // black

        selections[tileIndex] = tile.getText().toString();
        selections[otherTileIndex] = "";

        buildWord((String)view.getTag(), selections[tileIndex]);

    }

}
