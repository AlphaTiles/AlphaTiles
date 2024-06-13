package org.alphatilesapps.alphatiles;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Collections;

import static android.graphics.Color.BLACK;

import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.*;

public class Mexico extends GameActivity {

    ArrayList<String[]> memoryCollection = new ArrayList(); // KP
    int justClickedCard;
    int priorClickedCard;
    int activeSelections = 0;
    int pairsCompleted = 0;
    int cardHitA = 0;
    int cardHitB = 0;
    Handler handler; // KP

    protected static final int[] GAME_BUTTONS = {
            R.id.card01, R.id.card02, R.id.card03, R.id.card04, R.id.card05, R.id.card06, R.id.card07, R.id.card08, R.id.card09, R.id.card10,
            R.id.card11, R.id.card12, R.id.card13, R.id.card14, R.id.card15, R.id.card16, R.id.card17, R.id.card18, R.id.card19, R.id.card20
    };

    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
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
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.mexicoCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.mexico);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView repeatImage = findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.mexicoCL);
        }

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        // new levels
        // Level 1: 3 pairs = 6
        // Level 2: 4 pairs = 8
        // Level 3: 6 pairs = 12
        // Level 4: 8 pairs = 16
        // Level 5: 10 pairs = 20
        switch (challengeLevel) {
            case 2:
                visibleGameButtons = 8;
                break;
            case 3:
                visibleGameButtons = 12;
                break;
            case 4:
                visibleGameButtons = 16;
                break;
            case 5:
                visibleGameButtons = 20;
                break;
            default:
                visibleGameButtons = 6;
        }


        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        updatePointsAndTrackers(0);
        playAgain();
    }

    public void repeatGame(View View) {

        if (mediaPlayerIsPlaying) {
            return;
        }
        // Closing and restarting the activity for each round is not ideal, but it seemed to be helping with memory issues...
        Intent intent = getIntent();
        intent.setClass(this, Mexico.class);    // so we retain the Extras
        startActivity(intent);
        finish();
        playAgain();

    }

    public void playAgain() {

        repeatLocked = true;
        setAdvanceArrowToGray();
        setCardTextToEmpty();
        chooseMemoryWords();
        Collections.shuffle(memoryCollection); // KP
        pairsCompleted = 0;
        activeSelections = 0;
        cardHitA = 0;
        cardHitB = 0;

    }

    public void setCardTextToEmpty() {

        for (int i = 0; i < GAME_BUTTONS.length; i++) {    // RR
            TextView card = findViewById(GAME_BUTTONS[i]); // RR

            if (i < visibleGameButtons) {
                card.setText("");
                card.setTextColor(BLACK); // KP
                card.setBackgroundResource(R.drawable.zz_alphatileslogo2);
                card.setVisibility(View.VISIBLE);
                card.setClickable(true);
            } else {
                card.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void chooseMemoryWords() {
        // KP, Oct 2020
        int cardsToSetUp = visibleGameButtons / 2;   // this is half the number of cards

        for (int i = 0; i < cardsToSetUp; i++) {
            boolean wordAcceptable = true;
            chooseWord();
            for (int j = 0; j < i; j++) {
                if (refWord.wordInLWC.equals(memoryCollection.get(j*2)[0])) {
                    wordAcceptable = false;
                    j=i;
                }
            }
            if (!wordAcceptable) {
                i--;
            }

            if (wordAcceptable) {
                String[] content = new String[]
                        {
                                refWord.wordInLWC,
                                refWord.wordInLOP,
                                "TEXT",
                                "UNSELECTED",
                                String.valueOf(lwcWordHashMap.get(refWord.wordInLWC).duration),    // audio clip duration in seconds
                                lwcWordHashMap.get(refWord.wordInLWC).adjustment,    // font adjustment

                        };
                memoryCollection.add(content);
                content = new String[]
                        {
                                refWord.wordInLWC,
                                refWord.wordInLOP,
                                "IMAGE",
                                "UNSELECTED",
                                String.valueOf(lwcWordHashMap.get(refWord.wordInLWC).duration),    // audio clip duration in seconds
                                lwcWordHashMap.get(refWord.wordInLWC).adjustment,    // font adjustment

                        };
                memoryCollection.add(content);
            }
        }
    }

    public void respondToCardSelection() {

        int t = justClickedCard - 1; //  justClickedCard uses 1 to 12/16/20 (dep. on challengeLevel), t uses the array ID: between [0] and [11] / [15] / [19]

        if (memoryCollection.get(t)[3].equals("PAIRED")) {
            setAllGameButtonsClickable();
            setOptionsRowClickable();
            return;
        }

        if (justClickedCard == priorClickedCard && activeSelections == 1) {
            setAllGameButtonsClickable();
            setOptionsRowClickable();
            return;
        }

        activeSelections++;
        String[] currentItem = memoryCollection.get(t); // KP
        currentItem[3] = "SELECTED"; // KP
        TextView card = findViewById(GAME_BUTTONS[t]);
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

        if (activeSelections == 2) {
            setOptionsRowUnclickable();
            setAllGameButtonsUnclickable();

            handler = new Handler();
            handler.postDelayed(quickViewDelay, Long.valueOf(800));
        }

    }


    public void respondToTwoActiveCards() {

        // Two cards have been selected (which may or may not match)
        activeSelections = 0;       // (a reset)
        boolean firstHit = false;
        boolean secondHit = false;
        cardHitA = 0;
        cardHitB = 0;
        for (int i = 0; i < visibleGameButtons; i++) {

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

        if (memoryCollection.get(cardHitA)[0].equals(memoryCollection.get(cardHitB)[0])) { // Note: this is comparing the unstripped versions (e.g. with periods, etc.)

            // A match has been found!!
            memoryCollection.get(cardHitA)[3] = "PAIRED";
            memoryCollection.get(cardHitB)[3] = "PAIRED";
            pairsCompleted++;

            final TextView cardA = findViewById(GAME_BUTTONS[cardHitA]); // RR
            final TextView cardB = findViewById(GAME_BUTTONS[cardHitB]); // RR
            cardA.setBackgroundResource(0);
            cardB.setBackgroundResource(0);

            cardA.setText(Start.wordList.stripInstructionCharacters(memoryCollection.get(cardHitA)[1]));
            cardB.setText(Start.wordList.stripInstructionCharacters(memoryCollection.get(cardHitB)[1]));

            String tileColorStr = colorList.get(cardHitA % 5);
            int tileColor = Color.parseColor(tileColorStr);
            cardA.setTextColor(tileColor); // theme color
            cardB.setTextColor(tileColor); // theme color

            refWord.wordInLWC = memoryCollection.get(cardHitA)[0];

            if (pairsCompleted == (visibleGameButtons / 2)) {
                updatePointsAndTrackers((visibleGameButtons / 2));
                setAdvanceArrowToBlue();
            }

            playCorrectSoundThenActiveWordClip(pairsCompleted == (visibleGameButtons / 2));

        } else {
            // The two cards do NOT match
            long delay = 0;
            String delaySetting = Start.settingsList.find("View memory cards for _ milliseconds");
            if (!delaySetting.equals("")) {
                delay = Long.valueOf(delaySetting);
            }
            handler = new Handler();
            handler.postDelayed(flipCardsBackOver, delay);

        }

    }

    private Runnable quickViewDelay = new Runnable() {
        @Override
        public void run() {
            respondToTwoActiveCards();
        }
    };

    private Runnable flipCardsBackOver = new Runnable() {
        @Override
        public void run() {
            resetAfterIncorrectGuess();
        }
    };

    public void resetAfterIncorrectGuess() {
        TextView cardA = findViewById(GAME_BUTTONS[cardHitA]); // RR
        TextView cardB = findViewById(GAME_BUTTONS[cardHitB]); // RR
        cardA.setText("");
        cardB.setText("");
        cardA.setBackgroundResource(R.drawable.zz_alphatileslogo2);
        cardB.setBackgroundResource(R.drawable.zz_alphatileslogo2);
        memoryCollection.get(cardHitA)[3] = "UNSELECTED"; // KP
        memoryCollection.get(cardHitB)[3] = "UNSELECTED"; // KP
        setAllGameButtonsClickable();
        setOptionsRowClickable();
    }

    public void onBtnClick(View view) {

        priorClickedCard = justClickedCard;
        justClickedCard = Integer.parseInt((String) view.getTag());
        respondToCardSelection();

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


