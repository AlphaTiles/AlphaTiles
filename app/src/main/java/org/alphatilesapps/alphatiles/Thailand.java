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

import com.segment.analytics.Analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

import static android.graphics.Color.WHITE;
import static org.alphatilesapps.alphatiles.Start.tileList;

public class Thailand extends GameActivity {

    Start.TileList sortableTilesArray;

//    ArrayList<String><ArrayList<String>> fourChoices = new ArrayList<String><ArrayList<String>>;  // will store LWC and LOP word or will store tile audio name and tile (lower or upper)
    ArrayList<String[]> fourChoices = new ArrayList();  // will store LWC and LOP word or will store tile audio name and tile (lower or upper)

    private static final String[] TYPES = {"TILE_LOWER", "TILE_UPPER", "TILE_AUDIO","WORD_TEXT","WORD_IMAGE","WORD_AUDIO"};

    String refType;
    String refTile;
    String choiceType;
    int refColor;
    int challengeLevelThai;
    int pixelHeightRef;
    int pixelHeightChoices;

    protected static final int[] TILE_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04
    };

    protected int[] getTileButtons() {return null;}

    protected int[] getWordImages() {return null;}

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger( Thailand.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.thailand);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0);
        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
        visibleTiles = TILE_BUTTONS.length;

        // So, if challengeLevel is 235, then...
            // challengeLevelThai = 2 (distractors not random)
            // refType = "TILE_AUDIO" ... note that one is subtracted below so you refer to the array at 1 to x + 1, not 0 to x
            // choiceType = "WORD_IMAGE"
        String clString = String.valueOf(challengeLevel);
        challengeLevelThai = Integer.parseInt(clString.substring(0, 1));
        refType = TYPES[Integer.parseInt(clString.substring(1, 2)) - 1];
        choiceType = TYPES[Integer.parseInt(clString.substring(2, 3)) - 1];

        setTitle(Start.localAppName + ": " + gameNumber);

        sortableTilesArray = (Start.TileList) tileList.clone();
        Collections.shuffle(sortableTilesArray);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        LOGGER.info("Remember: F");
        setTextSizes();

        LOGGER.info("Remember: G");
        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        double scaling = 0.45;
        double scalingRef;
        double scalingChoices;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        if(refType.equals("WORD_TEXT")) {
            scalingRef = 0.29;
        } else {
            scalingRef = 0.45;
        }
        if(choiceType.equals("WORD_TEXT")) {
            scalingChoices = 0.26;
        } else {
            scalingChoices = 0.45;
        }

        TextView gameTile = findViewById(R.id.referenceItem);
        ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) gameTile.getLayoutParams();
        int bottomToTopId1 = lp1.bottomToTop;
        int topToTopId1 = lp1.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId1).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId1).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeightRef = (int) (scalingRef * percentHeight * heightOfDisplay);
        gameTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeightRef);

        for (int t = 0; t < TILE_BUTTONS.length; t++) {

            TextView nextWord = findViewById(TILE_BUTTONS[t]);
            if (t == 0) {
                ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) nextWord.getLayoutParams();
                bottomToTopId = lp2.bottomToTop;
                topToTopId = lp2.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeightChoices = (int) (scalingChoices * percentHeight * heightOfDisplay);
            }
            nextWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeightChoices);

        }

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        int pixelHeightGem = (int) (0.7 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeightGem);

    }

    public void repeatGame (View view) {

//        if (!repeatLocked) {
            playAgain();
//        }

    }

    public void playAgain () {

        repeatLocked = true;

        LOGGER.info("Remember: H");

        TextView refItem = findViewById(R.id.referenceItem);
        refItem.setText("");
        refItem.setBackgroundResource(0);

        LOGGER.info("Remember: I");
        Random rand = new Random();
        int randomNum = rand.nextInt(COLORS.length - 1);
        String refColorStr = COLORS[randomNum];
        refColor = Color.parseColor(refColorStr);

        LOGGER.info("Remember: J");
        // if either or both elements are word-based, then three if statement, but if both elements are tile based, then while loop
        if (refType.contains("WORD") || choiceType.contains("WORD")) {
            chooseWord();
            parsedWordArrayFinal = tileList.parseWord(wordInLOP);
            if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO") || choiceType.equals("TILE_LOWER")) {
                refTile = parsedWordArrayFinal.get(0);
                LOGGER.info("Remember: J2: refTile = " + refTile);
            }
            if (refType.equals("TILE_UPPER")|| choiceType.equals("TILE_UPPER")) {
                refTile = tileList.get(tileList.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).upperTile;
                LOGGER.info("Remember: J3: refTile = " + refTile);
            }
            if (refType.contains("WORD") && choiceType.contains("WORD")) {
                refTile = parsedWordArrayFinal.get(0);
                LOGGER.info("Remember: J3.5: refTile = " + refTile);
            }
        } else {
            String refCVX = "X";
            while (refCVX.equals("X")) {
                int randomNum2 = rand.nextInt(sortableTilesArray.size());
                refCVX = sortableTilesArray.get(randomNum2).tileType;
                if (refType.equals("TILE_LOWER") || refType.equals("TILE_AUDIO")) {
                    refTile = sortableTilesArray.get(randomNum2).baseTile;
                    LOGGER.info("Remember: J4: refTile = " + refTile);
                }
                if (refType.equals("TILE_UPPER")) {
                    refTile = sortableTilesArray.get(randomNum2).upperTile;
                    LOGGER.info("Remember: J5: refTile = " + refTile);
                }
            }
        }
        switch (refType) {
            case "TILE_LOWER":
            case "TILE_UPPER":
                refItem.setBackgroundColor(refColor);
                refItem.setTextColor(Color.parseColor("#FFFFFF")); // white
                refItem.setText(refTile);
                break;
            case "TILE_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_tile_audio);
                break;
            case "WORD_TEXT":
                refItem.setBackgroundColor(WHITE);
                refItem.setTextColor(Color.parseColor("#000000")); // black
                refItem.setText(wordInLOP);
                float fontAdjustment = Float.parseFloat(Start.wordList.get(Start.wordList.returnPositionInWordList(wordInLWC)).adjustment);
                int thisCardPixelHeight = (int) (pixelHeightRef * fontAdjustment);
                refItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, thisCardPixelHeight);
                break;
            case "WORD_IMAGE":
                int resID1 = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
                refItem.setBackgroundResource(resID1);
                refItem.setText("");
                break;
            case "WORD_AUDIO":
                refItem.setBackgroundResource(R.drawable.zz_click_for_word_audio);
                break;
            default:
                break;
        }
        LOGGER.info("Remember: K");

        if (choiceType.equals("TILE_LOWER") || choiceType.equals("TILE_UPPER")) {
                fourChoices = tileList.returnFourTiles(refTile, challengeLevelThai, choiceType);
                // challengeLevelThai 1 = pull random tiles for wrong choices
                // challengeLevelThai 2 = pull distractor tiles for wrong choices
        }
        LOGGER.info("Remember: Z");
        if (choiceType.equals("WORD_TEXT") || choiceType.equals("WORD_IMAGE")) {
                fourChoices = Start.wordList.returnFourWords(wordInLOP, wordInLWC, refTile, challengeLevelThai, refType, choiceType);
            // challengeLevelThai 1 = pull words that begin with random tiles (not distractor, not same) for wrong choices
            // challengeLevelThai 2 = pull words that begin with distractor tiles (or if not random) for wrong choices
            // challengeLevelThai 3 = pull words that begin with same tile (as correct word) for wrong choices
        }

        switch (choiceType) {
            case "TILE_LOWER":
            case "TILE_UPPER":
                for (int t = 0; t < TILE_BUTTONS.length; t++ ) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    String choiceColorStr = "#A9A9A9"; // dark gray
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    LOGGER.info("Remember: AB1: fourChoices.get(t)[1] = " + fourChoices.get(t)[1]);
                    choiceButton.setText(fourChoices.get(t)[1]);
                }
                break;
            case "WORD_TEXT":
                for (int t = 0; t < TILE_BUTTONS.length; t++ ) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    String choiceColorStr = "#FFFFFF"; // white
                    int choiceColorNo = Color.parseColor(choiceColorStr);
                    choiceButton.setBackgroundColor(choiceColorNo);
                    choiceButton.setTextColor(Color.parseColor("#000000")); // black
                    LOGGER.info("Remember: AB1: fourChoices.get(t)[1] = " + fourChoices.get(t)[1]);
                    choiceButton.setText(fourChoices.get(t)[1]);
                    float fontAdjustment = Float.parseFloat(Start.wordList.get(Start.wordList.returnPositionInWordList(fourChoices.get(t)[0])).adjustment);
                    int thisCardPixelHeight = (int) (pixelHeightChoices * fontAdjustment);
                    choiceButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, thisCardPixelHeight);
                    LOGGER.info("Remember: AB2");
                }
                break;
            case "WORD_IMAGE":
                for (int t = 0; t < TILE_BUTTONS.length; t++ ) {
                    TextView choiceButton = findViewById(TILE_BUTTONS[t]);
                    int resID = getResources().getIdentifier(fourChoices.get(t)[0], "drawable", getPackageName());
                    choiceButton.setBackgroundResource(0);
                    choiceButton.setBackgroundResource(resID);
                    choiceButton.setText("");
                }
                break;
            default:
                break;
        }

        playActiveClip();

    }

    private void respondToSelection(int justClickedItem) {

        String refItemText = null;
        TextView refItem = findViewById(R.id.referenceItem);

        switch (refType) {
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "WORD_TEXT":
                refItemText = refItem.getText().toString();
                break;
            case "TILE_AUDIO":
                refItemText = refTile;
                break;
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                refItemText = wordInLOP;
                break;
            default:
                break;
        }

        int t = justClickedItem - 1; //  justClickedItem uses 1 to 4, t uses the array ID (between [0] and [3]
        TextView chosenItem = findViewById(TILE_BUTTONS[t]);

        String chosenItemText;
        if (!choiceType.equals("WORD_IMAGE")) {
            chosenItemText = chosenItem.getText().toString();   // all cases except WORD_IMAGE
        } else {
            chosenItemText = fourChoices.get(t)[1];             // when WORD_IMAGE
        }

        boolean goodMatch = false;

        switch (choiceType) {
            case "TILE_LOWER":
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                        if (refItemText.equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        if (refItemText.equals(tileList.get(tileList.returnPositionInAlphabet(chosenItemText)).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (parsedWordArrayFinal.get(0).equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case "TILE_UPPER":
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                        if (chosenItemText.equals(tileList.get(tileList.returnPositionInAlphabet(refItemText)).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        if (refItemText.equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (chosenItemText.equals(tileList.get(tileList.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
                ArrayList<String> parsedChosenWordArrayFinal;
                switch (refType) {
                    case "TILE_LOWER":
                    case "TILE_AUDIO":
                        parsedChosenWordArrayFinal = tileList.parseWord(chosenItemText);
                        if (parsedChosenWordArrayFinal.get(0).equals(refItemText)) {
                            goodMatch = true;
                        }
                        break;
                    case "TILE_UPPER":
                        parsedChosenWordArrayFinal = tileList.parseWord(chosenItemText);
                        if (refItemText.equals(tileList.get(tileList.returnPositionInAlphabet(parsedChosenWordArrayFinal.get(0))).upperTile)) {
                            goodMatch = true;
                        }
                        break;
                    case "WORD_TEXT":
                    case "WORD_IMAGE":
                    case "WORD_AUDIO":
                        if (refItemText.equals(chosenItemText)) {
                            goodMatch = true;
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }


        if (goodMatch) {
            // Good job!
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=1;
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

            for (int b = 0; b < TILE_BUTTONS.length; b++ ) {
                TextView nextButton = findViewById(TILE_BUTTONS[b]);
                nextButton.setClickable(false);
                if (b == t && !choiceType.equals("WORD_IMAGE")) {
                    nextButton.setBackgroundColor(refColor);
                    nextButton.setTextColor(Color.parseColor("#FFFFFF")); // white
                }
                if (b != t && choiceType.equals("WORD_IMAGE")) {
                    nextButton.setBackgroundColor(Color.parseColor("#FFFFFF")); // white
//                    String wordColorStr = "#A9A9A9"; // dark gray
//                    int wordColorNo = Color.parseColor(wordColorStr);
//                    nextButton.setBackgroundColor(wordColorNo);
//                    nextButton.setTextColor(Color.parseColor("#000000")); // black
                }
            }

            playCorrectSoundThenRefClip();

        } else {
            playIncorrectSound();
        }
    }

    private void chooseWord() {

        Random rand = new Random();
        int randomNum = rand.nextInt(Start.wordList.size());

        wordInLWC = Start.wordList.get(randomNum).nationalWord;
        wordInLOP = Start.wordList.get(randomNum).localWord;

    }

    public void onChoiceClick (View view) {
        respondToSelection(Integer.parseInt((String)view.getTag())); // KP
    }

    public void onRefClick (View view) {

        LOGGER.info("Remember: pre playActiveClip()");
        playActiveClip();
        LOGGER.info("Remember: post playActiveClip()");

    }

    public void playActiveClip() {
        LOGGER.info("Remember: pre setAllTilesUnclickable()");
        setAllTilesUnclickable();
        LOGGER.info("Remember: post setAllTilesUnclickable()");
        setOptionsRowUnclickable();
        LOGGER.info("Remember: post setOptionsRowUnclickable()()");


        LOGGER.info("Remember: wordInLWC = " + wordInLWC);
        LOGGER.info("Remember: wordInLOP = " + wordInLOP);

        String audioToPlay = null;
        switch (refType) {
            case "TILE_LOWER":
            case "TILE_UPPER":
            case "TILE_AUDIO":
                audioToPlay = tileList.get(tileList.returnPositionInAlphabet(refTile)).audioForTile;
                break;
            case "WORD_TEXT":
            case "WORD_IMAGE":
            case "WORD_AUDIO":
                audioToPlay = wordInLWC;
                break;
            default:
                break;
        }

        if (!audioToPlay.equals("X")) {
            int resID = getResources().getIdentifier(audioToPlay, "raw", getPackageName());
            MediaPlayer mp1 = MediaPlayer.create(this, resID);
            mediaPlayerIsPlaying = true;
            mp1.start();
            mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp1) {
                    mediaPlayerIsPlaying = false;
                    if (repeatLocked) {
                        setAllTilesClickable();
                    }
                    setOptionsRowClickable();
                    mp1.release();
                }
            });
        }
    }
    public void playCorrectSoundThenRefClip() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        MediaPlayer mp2 = MediaPlayer.create(this, R.raw.zz_correct);
        mediaPlayerIsPlaying = true;
        mp2.start();
        mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp2) {
                mp2.release();
                playActiveClip();
            }
        });
    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

}
