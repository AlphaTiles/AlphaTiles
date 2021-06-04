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
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Romania extends GameActivity {

    boolean failedToMatchInitialTile = false;
    String activeTile;
    boolean directionIsForward = true;
    int scanSetting = 1; // will be set as 1, 2 or 3 from aa_settings.txt
    // 1 = only show word if tile is initial
    // 2 = for tiles with initial examples only, initial, for tiles without initial examples, non-initial acceptable
    // 3 = show all words regardless of where tile ocurrs

    boolean skipThisWord = false; // Set to true when it's a gray word (a word that demonstrates the tile with a medial instance not a word-initial instance)
    int wordTokenNoGroupOne = 0; // // Group One = words that START with the active tile
    int wordTokenNoGroupTwo = 0; // Group Two = words that contain the active tile non-initially (but excluding initially)
    int wordTokenNoGroupThree = 0; // Group Three = words containing the active tile anywhere (initial and/or non-initial)
    String firstAlphabetTile;

    ArrayList<String> settingsList;

    protected int[] getTileButtons() {return null;}

    protected int[] getWordImages() {return null;}

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger( Romania.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.romania);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP


        setTitle(Start.localAppName + ": " + gameNumber);

        // This is for the magnifying glass button (should probably be renamed)
        ImageView image = (ImageView) findViewById(R.id.repeatImage);
        image.setVisibility(View.INVISIBLE);

        scanSetting = Integer.parseInt(Start.settingsList.find("Game 001 Scan Setting"));

        switch (scanSetting) {
            case 2:
                setInitialPlusGaps();
                break;
            case 3:
                setAllOfAll();
                break;
            default:
                setInitialOnly();
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);

        pointsEarned.setText(String.valueOf(points));

        firstAlphabetTile = Start.tileList.get(0).baseTile; // KP
        SharedPreferences prefs = getSharedPreferences(Player.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String startingAlphabetTile = prefs.getString("lastActiveTileGame001_player" + playerString, firstAlphabetTile);

        activeTile = startingAlphabetTile;
        setUpBasedOnGameTile(activeTile);

        setTextSizes();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    private void setUpBasedOnGameTile(String activeTileString) {


        LOGGER.info("Remember: activeTileString = " + activeTileString);

        skipThisWord = false;

        int myMagCount = 0;
        int groupCount;

        switch (scanSetting) {
            case 2:
                // CASE 2: check Group One, if count is zero, then check Group Two
                // Group One = words that START with the active tile
                groupCount = Start.wordList.returnGroupOneCount(activeTileString);
                if (groupCount > 0) {
                    String[][] tempGroupOneWords = Start.wordList.returnGroupOneWords(activeTileString, groupCount);
                    wordInLWC = tempGroupOneWords[wordTokenNoGroupOne][0];
                    wordInLOP = tempGroupOneWords[wordTokenNoGroupOne][1];
                    wordTokenNoGroupOne++;
                    if (wordTokenNoGroupOne == groupCount) {
                        wordTokenNoGroupOne = 0;
                    }
                    myMagCount = groupCount;
                    failedToMatchInitialTile = false;
                } else {
                    // Group Two = words that contain the active tile non-initially (but excluding initially)
                    groupCount = Start.wordList.returnGroupTwoCount(activeTileString);
                    if (groupCount > 0) {
                        String[][] tempGroupTwoWords = Start.wordList.returnGroupTwoWords(activeTileString, groupCount); // Group Two = words that contain the active tile non-initially (but excluding initially)
                        wordInLWC = tempGroupTwoWords[wordTokenNoGroupTwo][0];
                        wordInLOP = tempGroupTwoWords[wordTokenNoGroupTwo][1];
                        wordTokenNoGroupTwo++;
                        if (wordTokenNoGroupTwo == groupCount) {
                            wordTokenNoGroupTwo = 0;
                        }
                        myMagCount = groupCount;
                        failedToMatchInitialTile = true;
                    }
                }
                break;
            case 3:
                // CASE 3: check Group Three
                // Group Three = words containing the active tile anywhere (initial and/or non-initial)
                groupCount = Start.wordList.returnGroupThreeCount(activeTileString);
                if (groupCount > 0) {

                    String[][] tempGroupThreeWords = Start.wordList.returnGroupThreeWords(activeTileString, groupCount); // Group Three = words containing the active tile anywhere (initial and/or non-initial)
                    wordInLWC = tempGroupThreeWords[wordTokenNoGroupThree][0];
                    wordInLOP = tempGroupThreeWords[wordTokenNoGroupThree][1];
                    wordTokenNoGroupThree++;
                    if (wordTokenNoGroupThree == groupCount) {
                        wordTokenNoGroupThree = 0;
                    }
                    myMagCount = groupCount;
                    parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
                    failedToMatchInitialTile = !activeTileString.equals(parsedWordArrayFinal.get(0));
                }
                break;
            default:
                // CASE 1: check Group One
                // Group One = words that START with the active tile
                groupCount = Start.wordList.returnGroupOneCount(activeTileString);
                if (groupCount > 0) {
                    String[][] tempGroupOneWords = Start.wordList.returnGroupOneWords(activeTileString, groupCount);
                    wordInLWC = tempGroupOneWords[wordTokenNoGroupOne][0];
                    wordInLOP = tempGroupOneWords[wordTokenNoGroupOne][1];
                    wordTokenNoGroupOne++;
                    if (wordTokenNoGroupOne == groupCount) {
                        wordTokenNoGroupOne = 0;
                    }
                    myMagCount = groupCount;
                    failedToMatchInitialTile = false;
                } else {
                    skipThisWord = true;
                }
        }

        TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
        gameTile.setText(activeTileString);

        TextView magTile = (TextView) findViewById(R.id.tileInMagnifyingGlass);
        magTile.setText(String.valueOf(myMagCount));

        if (!skipThisWord) {

            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            activeWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP));

            LOGGER.info("Remember: groupCount = " + groupCount);

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = Start.tileList.returnPositionInAlphabet(activeTileString);
            String tileColorStr = COLORS[alphabetPosition % 5];
            int tileColor = Color.parseColor(tileColorStr);
            gameTile.setBackgroundColor(tileColor);
            activeWord.setBackgroundColor(tileColor);
            magTile.setTextColor(tileColor);
            if (failedToMatchInitialTile) {
                tileColorStr = "#A9A9A9"; // dark gray
                tileColor = Color.parseColor(tileColorStr);
                activeWord.setBackgroundColor(tileColor);
                magTile.setTextColor(tileColor);
            }
            if (groupCount > 0) {
                playAudioForActiveWord();
            }

        } else {
            LOGGER.info("Remember: failed to find anything (skipWord = true) so advancing one more");
            if (directionIsForward) {
                goForwardOneTile(null);
            } else {
                goBackOneTile(null);
            }
        }
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        double scaling = 0.45;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
        ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) gameTile.getLayoutParams();
        int bottomToTopId1 = lp1.bottomToTop;
        int topToTopId1 = lp1.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId1).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId1).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        int pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        gameTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        TextView magTile = (TextView) findViewById(R.id.tileInMagnifyingGlass);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) magTile.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        magTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) activeWord.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        activeWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp4 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId4 = lp4.bottomToTop;
        int topToTopId4 = lp4.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId4).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId4).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.7 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void goForwardOneTile(View View) {
        directionIsForward = true;
        TextView tileBox = (TextView) findViewById(R.id.tileBoxTextView);
        String oldTile = tileBox.getText().toString();
        activeTile = Start.tileList.returnNextAlphabetTile(oldTile); // KP
        wordTokenNoGroupOne = 0;
        wordTokenNoGroupTwo = 0;
        wordTokenNoGroupThree = 0;
        SharedPreferences.Editor editor = getSharedPreferences(Player.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
    }

    public void goBackOneTile(View View) {
        directionIsForward = false;
        TextView tileBox = (TextView) findViewById(R.id.tileBoxTextView);
        String oldTile = tileBox.getText().toString();
        activeTile = Start.tileList.returnPreviousAlphabetTile(oldTile);
        wordTokenNoGroupOne = 0;
        wordTokenNoGroupTwo = 0;
        wordTokenNoGroupThree = 0;
        SharedPreferences.Editor editor = getSharedPreferences(Player.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
    }

    public void repeatGame(View View) {

        setUpBasedOnGameTile(activeTile);

    }

    public void setToggleToInitialOnly (View view) {

        setInitialOnly();

    }
    public void setInitialOnly() {

        scanSetting = 1;

        ImageView toggleOne = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView toggleTwo = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView toggleThree = (ImageView) findViewById(R.id.toggleAllOfAll);

        int resID1 = getResources().getIdentifier("zz_toggle_initial_only_on", "drawable", getPackageName());
        int resID2 = getResources().getIdentifier("zz_toggle_initial_plus_gaps_off", "drawable", getPackageName());
        int resID3 = getResources().getIdentifier("zz_toggle_all_of_all_off", "drawable", getPackageName());

        toggleOne.setImageResource(resID1);
        toggleTwo.setImageResource(resID2);
        toggleThree.setImageResource(resID3);

    }

    public void setToggleToInitialPlusGaps (View view) {

        setInitialPlusGaps();

    }
    public void setInitialPlusGaps() {

        scanSetting = 2;

        ImageView toggleOne = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView toggleTwo = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView toggleThree = (ImageView) findViewById(R.id.toggleAllOfAll);

        int resID1 = getResources().getIdentifier("zz_toggle_initial_only_off", "drawable", getPackageName());
        int resID2 = getResources().getIdentifier("zz_toggle_initial_plus_gaps_on", "drawable", getPackageName());
        int resID3 = getResources().getIdentifier("zz_toggle_all_of_all_off", "drawable", getPackageName());

        toggleOne.setImageResource(resID1);
        toggleTwo.setImageResource(resID2);
        toggleThree.setImageResource(resID3);

    }

    public void setToggleToAllOfAll (View view) {

        setAllOfAll();

    }
    public void setAllOfAll() {

        scanSetting = 3;

        ImageView toggleOne = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView toggleTwo = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView toggleThree = (ImageView) findViewById(R.id.toggleAllOfAll);

        int resID1 = getResources().getIdentifier("zz_toggle_initial_only_off", "drawable", getPackageName());
        int resID2 = getResources().getIdentifier("zz_toggle_initial_plus_gaps_off", "drawable", getPackageName());
        int resID3 = getResources().getIdentifier("zz_toggle_all_of_all_on", "drawable", getPackageName());

        toggleOne.setImageResource(resID1);
        toggleTwo.setImageResource(resID2);
        toggleThree.setImageResource(resID3);

    }

    public void playAudioForActiveWord() {
        mediaPlayerIsPlaying = true;
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        int resID = getResources().getIdentifier(wordInLWC, "raw", getPackageName());
        MediaPlayer mp = MediaPlayer.create(this, resID);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayerIsPlaying = false;
                setAllTilesClickable();
                setOptionsRowClickable();
                mp.release();
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mediaPlayerIsPlaying = false;
                setAllTilesClickable();
                setOptionsRowClickable();
                mp.release();
                return false;
            }
        });
        mp.start();

    }

    @Override
    protected void setAllTilesUnclickable()
    {

        TextView tileBox = findViewById(R.id.tileBoxTextView);
        tileBox.setClickable(false);

        ImageView word = findViewById(R.id.wordImage);
        word.setClickable(false);

        ImageView rightArrow = findViewById(R.id.rightArrowImage);
        rightArrow.setClickable(false);
        rightArrow.setBackgroundResource(0);
        rightArrow.setImageResource(R.drawable.zz_forward_inactive);

        ImageView leftArrow = findViewById(R.id.leftArrowImage);
        leftArrow.setClickable(false);
        leftArrow.setBackgroundResource(0);
        leftArrow.setImageResource(R.drawable.zz_backward_inactive);

        TextView magTile = findViewById(R.id.tileInMagnifyingGlass);
        magTile.setClickable(false);

        ImageView magGlass = findViewById(R.id.findMoreOfSameTile);
        magGlass.setClickable(false);

    }

    @Override
    protected void setAllTilesClickable()
    {

        TextView tileBox = findViewById(R.id.tileBoxTextView);
        tileBox.setClickable(true);

        ImageView word = findViewById(R.id.wordImage);
        word.setClickable(true);

        ImageView rightArrow = findViewById(R.id.rightArrowImage);
        rightArrow.setClickable(true);
        rightArrow.setBackgroundResource(0);
        rightArrow.setImageResource(R.drawable.zz_forward);

        ImageView leftArrow = findViewById(R.id.leftArrowImage);
        leftArrow.setClickable(true);
        leftArrow.setBackgroundResource(0);
        leftArrow.setImageResource(R.drawable.zz_backward);

        TextView magTile = findViewById(R.id.tileInMagnifyingGlass);
        magTile.setClickable(true);

        ImageView magGlass = findViewById(R.id.findMoreOfSameTile);
        magGlass.setClickable(true);

    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

}
