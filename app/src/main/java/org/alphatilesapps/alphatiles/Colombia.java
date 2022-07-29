package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.*;

public class Colombia extends GameActivity {

    String initialLetter = "";
    ArrayList<String> tempKeys; // KP
    int keysInUse; // number of keys in the language's total keyboard
    int keyboardScreenNo; // for languages with more than 35 keys, page 1 will have 33 buttons and a forward/backward button
    int totalScreens; // the total number of screens required to show all keys
    int partial; // the number of visible keys on final partial screen
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int colombiaPoints;
    boolean colombiaHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07, R.id.key08, R.id.key09, R.id.key10,
            R.id.key11, R.id.key12, R.id.key13, R.id.key14, R.id.key15, R.id.key16, R.id.key17, R.id.key18, R.id.key19, R.id.key20,
            R.id.key21, R.id.key22, R.id.key23, R.id.key24, R.id.key25, R.id.key26, R.id.key27, R.id.key28, R.id.key29, R.id.key30,
            R.id.key31, R.id.key32, R.id.key33, R.id.key34, R.id.key35
    };
    
    protected int[] getTileButtons() {return TILE_BUTTONS;}
    protected int[] getWordImages() {return null;}

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
//          audioInstructionsResID = res.getIdentifier("colombia_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());

        }
        catch (NullPointerException e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.colombiaCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger(Colombia.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.colombia);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            ImageView deleteImage = (ImageView) findViewById(R.id.deleteImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            deleteImage.setRotationY(180);

            fixConstraintsRTL(R.id.colombiaCL);
        }


        points = getIntent().getIntExtra("points", 0); // KP
        colombiaPoints = getIntent().getIntExtra("colombiaPoints", 0); // LM
        colombiaHasChecked12Trackers = getIntent().getBooleanExtra("colombiaHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        colombiaPoints = prefs.getInt("storedColombiaPoints_level" + challengeLevel + "_player" + playerString, 0);
        colombiaHasChecked12Trackers = prefs.getBoolean("storedColombiaHasChecked12Trackers_level" + challengeLevel + "_player" + playerString, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(colombiaPoints));

        LOGGER.info("Remember: oC2");

        /*SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);*/
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        LOGGER.info("Remember: oC3");

        setTextSizes();

        LOGGER.info("Remember: oC4");

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

        keyboardScreenNo = 1;
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
        int pixelHeight = 0;
        double scaling = 0.45;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int k = 0; k < TILE_BUTTONS.length; k++) {

            TextView key = findViewById(TILE_BUTTONS[k]);
            if (k == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) key.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            key.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

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
        pixelHeight = (int) (0.5 * scaling * percentHeight * heightOfDisplay);
        //CHANGED TO 0.5 BY JP SO THAT 4-DIGIT SCORE WILL FIT IN GEM
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }
    
    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }
    public void playAgain() {

        repeatLocked = true;

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        wordToBuild.setText("");
        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
        wordToBuild.setTextColor(Color.parseColor("#000000")); // black

        LOGGER.info("Remember: pA1");

        chooseWord();

        LOGGER.info("Remember: pA2");
        ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
        deleteArrow.setClickable(true);

        LOGGER.info("Remember: pA3");
        loadKeyboard();

    }

    private void chooseWord() {

        boolean freshWord = false;

        while(!freshWord) {
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size()); // KP

            wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
            wordInLOP = Start.wordList.get(randomNum).localWord; // KP

            //If this word isn't one of the 3 previously tested words, we're good // LM
            if(wordInLWC.compareTo(lastWord)!=0
                    && wordInLWC.compareTo(secondToLastWord)!=0
                    && wordInLWC.compareTo(thirdToLastWord)!=0){
                freshWord = true;
                thirdToLastWord = secondToLastWord;
                secondToLastWord = lastWord;
                lastWord = wordInLWC;
            }

        }//generates a new word if it got one of the last three tested words // LM

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
        initialLetter = parsedWordArrayFinal.get(0); // KP

    }
    public void loadKeyboard() {
        switch (challengeLevel)
        {
            case 1:
                // Build an array of only the required tiles
                // Will it list <a> twice if <a> is needed twice? Yes, that's what it does
                // The limited set keyboard is built with GAME TILES not with KEYS
                visibleTiles = tilesInArray(parsedWordArrayFinal);
                tempKeys = (ArrayList<String>)parsedWordArrayFinal.clone(); // KRP
                Collections.shuffle(tempKeys); // KRP
                for (int k = 0; k < visibleTiles; k++)
                {
                    TextView key = findViewById(TILE_BUTTONS[k]);
                    key.setText(tempKeys.get(k));
                }
                break;
            case 2:
                // Build an array of the required tiles plus a corresponding tile from the distractor trio for each tile
                // So, for a five tile word, there will be 10 tiles
                // The limited-set keyboard is built with GAME TILES not with KEYS
                int firstHalf = tilesInArray(parsedWordArrayFinal); // KRP
                visibleTiles = 2 * firstHalf; // KRP
                tempKeys = (ArrayList<String>)parsedWordArrayFinal.clone(); // KRP
                int a = 0;
                for (int i = firstHalf; i < visibleTiles; i++)
                {
                    tempKeys.add(tileList.returnRandomCorrespondingTile(parsedWordArrayFinal.get(a))); // KRP
                    a++;
                }
                Collections.shuffle(tempKeys); // KRP
                for (int k = 0; k < visibleTiles; k++)
                {
                    TextView key = findViewById(TILE_BUTTONS[k]);
                    key.setText(tempKeys.get(k));
                }
                break;
            case 3:
                // There are 35 key buttons available (KEYS.length), but the language may need a smaller amount (Start.keysArraySize)
                // Starting with k = 1 to skip the header row
                keysInUse = keyList.size(); // KP
                partial = keysInUse % (TILE_BUTTONS.length - 2);
                totalScreens = keysInUse / (TILE_BUTTONS.length - 2);

                LOGGER.info("Remember: partial = " + partial);
                LOGGER.info("Remember: totalScreens = " + totalScreens);
                if (partial != 0) {
                    totalScreens++;
                }

                LOGGER.info("Remember: April 22 2021 A1");
                if (keysInUse > TILE_BUTTONS.length) {
                    visibleTiles = TILE_BUTTONS.length;
                } else {
                    visibleTiles = keysInUse;
                }
                for (int k = 0; k < visibleTiles; k++)
                {
                    TextView key = findViewById(TILE_BUTTONS[k]);
                    key.setText(keyList.get(k).baseKey); // KRP
                    String tileColorStr = COLORS[Integer.parseInt(keyList.get(k).keyColor)];
                    int tileColor = Color.parseColor(tileColorStr);
                    key.setBackgroundColor(tileColor);
                }
                LOGGER.info("Remember: April 22 2021 A2");
                if (keysInUse > TILE_BUTTONS.length) {
                    LOGGER.info("Remember: keysInUse = " + keysInUse);
                    LOGGER.info("Remember: KEYS.length = " + TILE_BUTTONS.length);
                    LOGGER.info("Remember: April 22 2021 A2");
                    TextView key34 = findViewById(TILE_BUTTONS[TILE_BUTTONS.length - 2]);
                    key34.setBackgroundResource(R.drawable.zz_backward_green);
                    if(scriptDirection.compareTo("RTL")==0) { //LTR is default
                        key34.setRotationY(180);
                    }
                    key34.setText("");
                    LOGGER.info("key34's text: " + key34.getText());
                    TextView key35 = findViewById(TILE_BUTTONS[TILE_BUTTONS.length - 1]);
                    key35.setBackgroundResource(R.drawable.zz_forward_green);
                    if(scriptDirection.compareTo("RTL")==0) { //LTR is default
                        key35.setRotationY(180);
                    }
                    key35.setText("");
                    LOGGER.info("key35's text: " + key35.getText());
                }
                LOGGER.info("Remember: April 22 2021 A3");
                break;
            default:
        }

        for (int k = 0; k < TILE_BUTTONS.length; k++)
        {

            TextView key = findViewById(TILE_BUTTONS[k]);
            if (k < visibleTiles)
            {
                key.setVisibility(View.VISIBLE);
                key.setClickable(true);
            }
            else
            {
                key.setVisibility(View.INVISIBLE);
                key.setClickable(false);
            }

        }

    }

    private void respondToKeySelection(int justClickedIndex) {

        String tileToAdd = "";

        switch (challengeLevel) {
            case 2:
                tileToAdd = tempKeys.get(justClickedIndex); // KP
                break;
            case 3:
                tileToAdd = Start.keyList.get(justClickedIndex).baseKey;
                break;
            default:
                tileToAdd = tempKeys.get(justClickedIndex); // KP (case 1)
        }

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        String currentWord = wordToBuild.getText() + tileToAdd;     // RR
        wordToBuild.setText(currentWord);                           // RR

        evaluateStatus();

    }
    private void evaluateStatus() {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        if (wordToBuild.getText().equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
            // Word spelled correctly!
            wordToBuild.setBackgroundColor(Color.parseColor("#4CAF50"));      // theme green
            wordToBuild.setTextColor(Color.parseColor("#FFFFFF")); // white

            for (int i : TILE_BUTTONS) {                    // RR
                TextView key = findViewById(i);     // RR
                key.setClickable(false);
            }

            ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
            deleteArrow.setClickable(false);

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=4;
            colombiaPoints+=4;
            pointsEarned.setText(String.valueOf(colombiaPoints));

            trackerCount++;
            if(trackerCount>=12){
                colombiaHasChecked12Trackers = true;
            }
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedColombiaPoints_level" + challengeLevel + "_player" + playerString, colombiaPoints);
            editor.putBoolean("storedColumbiaHasChecked12Trackers_level" + challengeLevel + "_player" + playerString, colombiaHasChecked12Trackers);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            playCorrectSoundThenActiveWordClip(false);

            repeatLocked = false;

        } else {

            // Word is partial and, for the moment, assumed to be incorrect
            wordToBuild.setBackgroundColor(Color.parseColor("#A9A9A9")); // gray for wrong
            wordToBuild.setTextColor(Color.parseColor("#000000")); // black

            if (wordInLOP.length() > wordToBuild.getText().length()) {

                if (wordToBuild.getText().equals(Start.wordList.stripInstructionCharacters(wordInLOP).substring(0, wordToBuild.getText().length()))) {
                    // Word, so far, spelled correctly, but a less than complete match
                    wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
                    wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                }

            }
        }
    }

    public void deleteLastKeyedLetter (View view) {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        String typedLettersSoFar = wordToBuild.getText().toString();
        String nowWithOneLessChar = "";

        if (typedLettersSoFar.length() > 0) {       // RR
            nowWithOneLessChar = typedLettersSoFar.substring(0, typedLettersSoFar.length() - 1);
        }

        wordToBuild.setText(nowWithOneLessChar);
        evaluateStatus();

    }

    public void onBtnClick(View view) {

        int justClickedKey = Integer.parseInt((String)view.getTag());
        // Next line says ... if a basic keyboard (which all fits on one screen) or (even when on a complex keyboard) if something other than the last two buttons (the two arrows) are tapped...
        if (keysInUse <= TILE_BUTTONS.length || justClickedKey <= (TILE_BUTTONS.length - 2)) {
            int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
            respondToKeySelection(keyIndex);
        } else {
            // This branch = when a backward or forward arrow is clicked on
            if (justClickedKey == TILE_BUTTONS.length - 1) {
                keyboardScreenNo--;
                if (keyboardScreenNo < 1) {
                    keyboardScreenNo = 1;
                }
            }
            if (justClickedKey == TILE_BUTTONS.length) {
                keyboardScreenNo++;
                if (keyboardScreenNo > totalScreens) {
                    keyboardScreenNo = totalScreens;
                }
            }
            updateKeyboard();
        }
    }

    private void updateKeyboard() {

        // This routine will only be called from complex keyboards (more keys than will fit on the basic 35-key layout)

        int keysLimit;
        if(totalScreens == keyboardScreenNo) {
            keysLimit = partial;
            for (int k = keysLimit; k < (TILE_BUTTONS.length - 2); k++) {
                TextView key = findViewById(TILE_BUTTONS[k]);
                key.setVisibility(View.INVISIBLE);
            }
        } else {
            keysLimit = TILE_BUTTONS.length - 2;
        }

        for (int k = 0; k < keysLimit; k++) {
            TextView key = findViewById(TILE_BUTTONS[k]);
            int keyIndex = (33 * (keyboardScreenNo - 1)) + k;
            key.setText(keyList.get(keyIndex).baseKey); // KP
            key.setVisibility(View.VISIBLE);
            // Added on May 15th, 2021, so that second and following screens use their own color coding
            String tileColorStr = COLORS[Integer.parseInt(keyList.get(keyIndex).keyColor)];
            int tileColor = Color.parseColor(tileColorStr);
            key.setBackgroundColor(tileColor);
        }

    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view){
        if(getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

}
