package org.alphatilesapps.alphatiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static android.graphics.Color.BLACK;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;

import static org.alphatilesapps.alphatiles.Start.*;

public class Mexico extends GameActivity {

    ArrayList<String[]> memoryCollection = new ArrayList(); // KP
        // # 1 memoryCollection[LWC word, e.g. Spanish]
        // # 2 [LOP word, e.g. Me'phaa]
        // # 3 [state: "TEXT" or "IMAGE"]
        // # 4 [state: "SELECTED" or "UNSELECTED" or "PAIRED"]
        // # 5 duration in ms
        // # 6 font adjustment for longer words

    String delaySetting = Start.settingsList.find("View memory cards for _ milliseconds");

    ArrayList<String[]> wordListArray; // KP

    int justClickedCard;
    int priorClickedCard;
    int activeSelections = 0;
    int pairsCompleted = 0;
    int cardHitA = 0;
    int cardHitB = 0;
    int pixelHeight = 0;
    double lowestAdjustment = 0.7;

    Handler handler; // KP

    int mexicoPoints;
    boolean mexicoHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.card01, R.id.card02, R.id.card03, R.id.card04, R.id.card05, R.id.card06, R.id.card07, R.id.card08, R.id.card09, R.id.card10,
            R.id.card11, R.id.card12, R.id.card13, R.id.card14, R.id.card15, R.id.card16, R.id.card17, R.id.card18, R.id.card19, R.id.card20
    };

    protected int[] getTileButtons() {return TILE_BUTTONS;}

    protected int[] getWordImages() {return null;}

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
//          audioInstructionsResID = res.getIdentifier("mexico_" + challengeLevel, "raw", context.getPackageName());
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

        int gameID = R.id.mexicoCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    private static final Logger LOGGER = Logger.getLogger( Mexico.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.mexico);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.mexicoCL);
        }

        points = getIntent().getIntExtra("points", 0); // KP
        mexicoPoints = getIntent().getIntExtra("mexicoPoints", 0); // LM
        mexicoHasChecked12Trackers = getIntent().getBooleanExtra("mexicoHasChecked12Trackers", false);

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        mexicoPoints = prefs.getInt("storedMexicoPoints_level" + challengeLevel + "_player"
                + playerString + "_" + syllableGame, 0);
        mexicoHasChecked12Trackers = prefs.getBoolean("storedMexicoHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP
        syllableGame = getIntent().getStringExtra("syllableGame");

        wordListArray = new ArrayList(); // KP

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        // new levels
        // Level 1: 3 pairs = 6
        // Level 2: 4 pairs = 8
        // Level 3: 6 pairs = 12
        // Level 4: 8 pairs = 16
        // Level 5: 10 pairs = 20
        switch (challengeLevel) {
            case 2:
                visibleTiles = 8;
                break;
            case 3:
                visibleTiles = 12;
                break;
            case 4:
                visibleTiles = 16;
                break;
            case 5:
                visibleTiles = 20;
                break;
            default:
                visibleTiles = 6;
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(mexicoPoints));

        /*SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);*/
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        if(trackerCount >= 12){
            mexicoHasChecked12Trackers = true;
        }
        updateTrackers();

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void repeatGame(View View) {

        if (mediaPlayerIsPlaying) {
            return;
        }

        // playAgain();

        // Closing and restarting the activity for each round is not ideal, but it seemed to be helping with memory issues...
        Intent intent = getIntent();
        intent.setClass(this, Mexico.class);    // so we retain the Extras
        startActivity(intent);
        finish();

    }

    public void playAgain() {

        setCardTextToEmpty();
        buildWordsArray();
        Collections.shuffle(wordListArray); // KP
        chooseMemoryWords();
        Collections.shuffle(memoryCollection); // KP
        pairsCompleted = 0;
        activeSelections = 0;
        cardHitA = 0;
        cardHitB = 0;

    }

    public void setCardTextToEmpty() {

        for (int i = 0; i < TILE_BUTTONS.length; i++) {    // RR
            TextView card = findViewById(TILE_BUTTONS[i]); // RR

//            card.getBackground().setAlpha(255);

            if (i < visibleTiles) {
                card.setText("");
                card.setTextColor(BLACK); // KP
                card.setBackgroundResource(R.drawable.zz_alphatileslogo2);
                card.setVisibility(View.VISIBLE);
                card.setClickable(true);
//                card.setTypeface(card.getTypeface(), Typeface.NORMAL);
            } else {
                card.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void buildWordsArray() {

        // This array will store every word available in wordlist.txt
        // Subsequently, this will be narrowed down to 10 words for use in the memory game
        // KP, Oct 2020
        // AH, Nov 2020, revised to allow for spaces in words

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_wordlist));
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }     // skip the header row

        while (scanner.hasNextLine()) {
            String thisLine = scanner.nextLine();
            String[] thisLineArray = thisLine.split("\t");
            double activeAdjustment = Double.parseDouble(thisLineArray[4]);
            if (activeAdjustment >= lowestAdjustment) {
//                LOGGER.info("Remember: thisLineArray[4] = " + thisLineArray[4]);
                wordListArray.add(thisLineArray);
            }
        }
    }

    public void chooseMemoryWords() {

        // KP, Oct 2020

        int cardsToSetUp = visibleTiles / 2 ;   // this is half the number of cards

        for (int i = 0; i < visibleTiles; i++) {

            int index = i < cardsToSetUp ? i : i - cardsToSetUp;
            String[] content = new String[]
                    {
                            wordListArray.get(index)[0],
                            wordListArray.get(index)[1],
                            i < cardsToSetUp ? "TEXT" : "IMAGE",
                            "UNSELECTED",
                            wordListArray.get(index)[2],    // audio clip duration in seconds
                            wordListArray.get(index)[4],    // font adjustment


                    };
            memoryCollection.add(content);

        }

    }

    public void respondToCardSelection() {

        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        int t = justClickedCard - 1; //  justClickedCard uses 1 to 12/16/20 (dep. on challengeLevel), t uses the array ID: between [0] and [11] / [15] / [19]

        if (memoryCollection.get(t)[3].equals("PAIRED")) {
            setAllTilesUnclickable();
            setOptionsRowClickable();
            return;
        }

        if (activeSelections == 2) {
            setAllTilesClickable();
            setOptionsRowClickable();
            return;
        }

        if (justClickedCard == priorClickedCard && activeSelections == 1) {
            setAllTilesClickable();
            setOptionsRowClickable();
            return;
        }

        activeSelections++;
        String[] currentItem = memoryCollection.get(t); // KP
        currentItem[3] = "SELECTED"; // KP
        TextView card = findViewById(TILE_BUTTONS[t]);
        int resID = getResources().getIdentifier(currentItem[0], "drawable", getPackageName()); // KP
        String wordInLOP = currentItem[1]; // KP
        String appearance = currentItem[2]; // KP

//        card.getBackground().setAlpha(255);
//        The commented out code in this and other modules relates to a previous version where upon a memory match:
//        Both cards displayed a semi-transparent version of the image and both cards showed the text in bold green font
//        If we could get this version to work, it would be preferable, but I (Aaron) had a problem with the images appearing
//        initially in new games already semi-transparent and I wasn't able to fix it
        // Can't remember if we tested if the problem went away once replaying the game involves finishing the activity and restarting

        if (appearance.equals("TEXT")) {
            card.setText(Start.wordList.stripInstructionCharacters(wordInLOP));
            card.setBackgroundResource(0);
        } else {
            card.setBackgroundResource(resID);
        }

//        card.getBackground().setAlpha(255);

        if (activeSelections == 2) {

            handler = new Handler();
            handler.postDelayed(quickViewDelay, 800);
            // Will run respondToTwoActiveCards() after delay...
            // https://www.youtube.com/watch?v=3pgGVBmSVq0
            // https://codinginflow.com/tutorials/android/handler-postdelayed-runnable
        }

        setAllTilesClickable();
        setOptionsRowClickable();

    }

    

    public void respondToTwoActiveCards() {

        // Two cards have been selected (which may or may not match)
        activeSelections = 0;       // (a reset)
        boolean firstHit = false;
        boolean secondHit = false;
        cardHitA = 0;
        cardHitB = 0;
        for (int i = 0; i < visibleTiles; i++) {

            // Scan through CARDS to find which two items are selected
            if (memoryCollection.get(i)[3].equals("SELECTED")) {

                if (firstHit && !secondHit) {
                    secondHit = true;
                    cardHitB = i; // cardHitB is the button number (1 to 12/16/20) not the array value ([0] to [11]/[15]/[19])
                }
                if (!firstHit) {    // RR
                    firstHit = true;
                    cardHitA = i; // cardHitA is the button number (1 to 12/16/20) not the array value ([0] to [11]/[15]/[19])
                }
            }
        }

        if (memoryCollection.get(cardHitA)[0].equals(memoryCollection.get(cardHitB)[0])) {
            // Note: this is comparing the unstripped versions (e.g. with periods, etc.)

            // A match has been found!!
            memoryCollection.get(cardHitA)[3] = "PAIRED";
            memoryCollection.get(cardHitB)[3] = "PAIRED";
            pairsCompleted++;

            final TextView cardA = findViewById(TILE_BUTTONS[cardHitA]); // RR
            final TextView cardB = findViewById(TILE_BUTTONS[cardHitB]); // RR
            cardA.setBackgroundResource(0);
            cardB.setBackgroundResource(0);

//            cardA.getBackground().setAlpha(100);
//            cardB.getBackground().setAlpha(100);

            cardA.setText(Start.wordList.stripInstructionCharacters(memoryCollection.get(cardHitA)[1]));
            cardB.setText(Start.wordList.stripInstructionCharacters(memoryCollection.get(cardHitB)[1]));

            String tileColorStr = COLORS.get(cardHitA % 5);
            int tileColor = Color.parseColor(tileColorStr);
            cardA.setTextColor(tileColor); // theme color
            cardB.setTextColor(tileColor); // theme color

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points++;
            mexicoPoints++;
            pointsEarned.setText(String.valueOf(mexicoPoints));

            if(trackerCount>=12){
                mexicoHasChecked12Trackers = true;
            }
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            editor.putInt("storedMexicoPoints_level" + challengeLevel + "_player" + playerString
                    + "_" + syllableGame, mexicoPoints);
            editor.apply();
            editor.putBoolean("storedMexicoHasChecked12Trackers_level" + challengeLevel + "_player"
                    + playerString + "_" + syllableGame, mexicoHasChecked12Trackers);
            editor.apply();
            editor.putInt(getClass().getName() + challengeLevel + playerString + syllableGame, trackerCount);
            editor.apply();

            wordInLWC = memoryCollection.get(cardHitA)[0];
            playCorrectSoundThenActiveWordClip(pairsCompleted == (visibleTiles / 2));

        } else {
            // The two cards do NOT match
            if(delaySetting.compareTo("") != 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(Long.valueOf(delaySetting));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            TextView cardA = findViewById(TILE_BUTTONS[cardHitA]); // RR
            TextView cardB = findViewById(TILE_BUTTONS[cardHitB]); // RR
            cardA.setText("");
            cardB.setText("");
            cardA.setBackgroundResource(R.drawable.zz_alphatileslogo2);
            cardB.setBackgroundResource(R.drawable.zz_alphatileslogo2);
            memoryCollection.get(cardHitA)[3] = "UNSELECTED"; // KP
            memoryCollection.get(cardHitB)[3] = "UNSELECTED"; // KP
        }

    }

    public void onBtnClick(View view) {

        priorClickedCard = justClickedCard;
        justClickedCard = Integer.parseInt((String)view.getTag());
        respondToCardSelection();

    }

    private Runnable quickViewDelay = new Runnable() {
        @Override
        public void run() {
            respondToTwoActiveCards();
        }
    };

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


