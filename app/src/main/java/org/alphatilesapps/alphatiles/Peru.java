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
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Peru extends GameActivity {

    String gameIDString;

    private static final int[] WORD_CHOICES = {
            R.id.word1, R.id.word2, R.id.word3, R.id.word4
    };

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger( Peru.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.peru);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        setTitle(Start.localAppName + ": " + gameNumber);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        setTextSizes();

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

        for (int w = 0; w < WORD_CHOICES.length; w++) {

            TextView nextWord = findViewById(WORD_CHOICES[w]);
            if (w == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) nextWord.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            nextWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

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

    public void repeatGame (View view) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain () {

        repeatLocked = true;
        Random rand = new Random();
        int randomNum = rand.nextInt(Start.wordList.size()); // KP
        wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
        wordInLOP = Start.wordList.get(randomNum).localWord; // KP
        parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
        int tileLength = tilesInArray(parsedWordArrayFinal);

        // Set thematic colors for four word choice TextViews, also make clickable
        for (int i = 0; i < WORD_CHOICES.length; i++) {
            TextView nextWord = (TextView) findViewById(WORD_CHOICES[i]);
            String wordColorStr = COLORS[i];
            int wordColorNo = Color.parseColor(wordColorStr);
            nextWord.setBackgroundColor(wordColorNo);
            nextWord.setTextColor(Color.parseColor("#FFFFFF")); // white
            nextWord.setClickable(true);
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        int randomNum2 = rand.nextInt(4);    // need to choose which of four words will be spelled correctly (and other three will be modified to become incorrect

        int incorrectLapNo = 0;
        for (int i = 0; i < WORD_CHOICES.length; i++) {
            TextView nextWord = (TextView) findViewById(WORD_CHOICES[i]);
            if (i == randomNum2) {
                nextWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP)); // the correct answer (the unmodified version of the word)
            } else {

                incorrectLapNo++;

                switch (challengeLevel) {
                    case 1:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (THE FIRST TILE) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO
                        List<String> tempArray1 = new ArrayList<>(parsedWordArrayFinal);
                        tempArray1.set(0, Start.tileList.get(Start.tileList.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).altTiles[incorrectLapNo - 1]); // KP
                        StringBuilder builder1 = new StringBuilder("");
                        for(String s : tempArray1) {
                            builder1.append(s);
                        }
                        String incorrectChoice1 = builder1.toString();
                        nextWord.setText(incorrectChoice1);
                        break;
                    case 2:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS ANY GAME TILE FROM THE WHOLE ARRAY

                        boolean isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates

                        while(isDuplicateAnswerChoice) {
                            int randomNum3 = rand.nextInt(tileLength - 1);       // KP // this represents which position in word string will be replaced
                            int randomNum4 = rand.nextInt(Start.tileList.size());       // KP // this represents which game tile will overwrite some part of the correct wor

                            List<String> tempArray2 = new ArrayList<>(parsedWordArrayFinal);

                            tempArray2.set(randomNum3, Start.tileList.get(randomNum4).baseTile); // KP
                            StringBuilder builder2 = new StringBuilder("");
                            for (String s : tempArray2) {
                                builder2.append(s);
                            }
                            String incorrectChoice2 = builder2.toString();
                            nextWord.setText(incorrectChoice2);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for(int answerChoice = 0; answerChoice < i; answerChoice++){
                                if(incorrectChoice2.compareTo(((TextView)findViewById(WORD_CHOICES[answerChoice])).getText().toString()) == 0){
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            if(incorrectChoice2.compareTo(wordInLOP) == 0){
                                isDuplicateAnswerChoice = true;
                            }
                        }
                        break;

                    case 3:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO
                        int randomNum5 = rand.nextInt(tileLength - 1);       // this represents which position in word string will be replaced
                        List<String> tempArray3 = new ArrayList<>(parsedWordArrayFinal);
                        tempArray3.set(randomNum5, Start.tileList.returnRandomCorrespondingTile(parsedWordArrayFinal.get(randomNum5)));
                        StringBuilder builder3 = new StringBuilder("");
                        for(String s : tempArray3) {
                            builder3.append(s);
                        }
                        String incorrectChoice3 = builder3.toString();
                        nextWord.setText(incorrectChoice3);
                        break;
                    default:

                }
            }
        }
    }

    private void respondToWordSelection(int justClickedWord) {

        int t = justClickedWord - 1; //  justClickedWord uses 1 to 4, t uses the array ID (between [0] and [3]
        TextView chosenWord = findViewById(WORD_CHOICES[t]);
        String chosenWordText = chosenWord.getText().toString();

        if (chosenWordText.equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
            // Good job!
            repeatLocked = false;

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=2;
            pointsEarned.setText(String.valueOf(points));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            for (int w = 0; w < WORD_CHOICES.length; w++ ) {
                TextView nextWord = findViewById(WORD_CHOICES[w]);
                nextWord.setClickable(false);
                if (w != t) {
                    String wordColorStr = "#A9A9A9"; // dark gray
                    int wordColorNo = Color.parseColor(wordColorStr);
                    nextWord.setBackgroundColor(wordColorNo);
                    nextWord.setTextColor(Color.parseColor("#000000")); // black
                }
            }

            playCorrectSoundThenActiveWordClip();

        } else {
            playIncorrectSound();
        }
    }

    public void onWordClick (View view) {
        int wordNo = Integer.parseInt((String)view.getTag());
        respondToWordSelection(wordNo);
    }

    private void setAllTilesUnclickable() {

        for (int wordChoice : WORD_CHOICES) {               // RR
            TextView nextWord = findViewById(wordChoice);   // RR
            nextWord.setClickable(false);
        }

    }
    private void setAllTilesClickable() {

        for (int wordChoice : WORD_CHOICES) {               // RR
            TextView nextWord = findViewById(wordChoice);   // RR
            nextWord.setClickable(true);
        }

    }
    private void setOptionsRowUnclickable() {

        ImageView repeatImage = findViewById(R.id.repeatImage);
        ImageView wordImage = findViewById(R.id.wordImage);

        repeatImage.setBackgroundResource(0);
        repeatImage.setImageResource(R.drawable.zz_forward_inactive);

        repeatImage.setClickable(false);
        wordImage.setClickable(false);

    }
    private void setOptionsRowClickable() {

        ImageView repeatImage = findViewById(R.id.repeatImage);
        ImageView wordImage = findViewById(R.id.wordImage);
        ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);

        repeatImage.setBackgroundResource(0);
        repeatImage.setImageResource(R.drawable.zz_forward);

        repeatImage.setClickable(true);
        wordImage.setClickable(true);
        gamesHomeImage.setClickable(true);

    }

    public void clickPicHearAudio (View view) {

        playActiveWordClip();

    }
    public void playActiveWordClip() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        int resID = getResources().getIdentifier(wordInLWC, "raw", getPackageName());
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
    public void playCorrectSoundThenActiveWordClip() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        MediaPlayer mp2 = MediaPlayer.create(this, R.raw.zz_correct);
        mediaPlayerIsPlaying = true;
        mp2.start();
        mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp2) {
                mp2.release();
                playActiveWordClip();
            }
        });
    }
    public void playIncorrectSound() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        MediaPlayer mp3 = MediaPlayer.create(this, R.raw.zz_incorrect);
        mediaPlayerIsPlaying = true;
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                mediaPlayerIsPlaying = false;
                setAllTilesClickable();
                setOptionsRowClickable();
                mp3.release();
            }
        });
    }

}
