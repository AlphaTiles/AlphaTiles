package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static org.alphatilesapps.alphatiles.Start.differentiatesTileTypes;
import static org.alphatilesapps.alphatiles.Start.gameList;
import static org.alphatilesapps.alphatiles.Start.stageCorrespondenceRatio;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.tileStagesLists;
import static org.alphatilesapps.alphatiles.Start.wordStagesLists;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;
import static org.alphatilesapps.alphatiles.Start.correctSoundDuration;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.incorrectSoundID;
import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.after12checkedTrackers;


public abstract class GameActivity extends AppCompatActivity {

    // KP, Oct 2020
    Context context;
    String className;
    String country;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
    int gameNumber = 0;
    int playerNumber = -1;
    String playerString;
    int challengeLevel = -1;
    int stage = 7;
    String syllableGame;

    SharedPreferences prefs;
    String uniqueGameLevelPlayerModeStageID;
    boolean hasChecked12Trackers;
    int points;
    int globalPoints;
    int trackerCount = 0;

    Start.TileList cumulativeStageBasedTileList = new Start.TileList();
    Start.WordList cumulativeStageBasedWordList = new Start.WordList();
    Start.TileList previousStagesTileList = new Start.TileList();
    Start.WordList previousStagesWordList = new Start.WordList();
    ArrayList<Start.Tile> parsedRefWordTileArray;
    ArrayList<Start.Syllable> parsedRefWordSyllableArray;

    int visibleGameButtons;
    Start.Word refWord;
    Queue<String> last12Words = new PriorityQueue<>();


    boolean mediaPlayerIsPlaying = false;
    boolean repeatLocked = true;
    Handler soundSequencer;

    protected static final int[] TRACKERS = {
            R.id.tracker01, R.id.tracker02, R.id.tracker03, R.id.tracker04, R.id.tracker05, R.id.tracker06, R.id.tracker07, R.id.tracker08, R.id.tracker09, R.id.tracker10,
            R.id.tracker11, R.id.tracker12

    };

    protected abstract int[] getGameButtons();

    protected abstract int[] getWordImages();

    protected abstract int getAudioInstructionsResID();

    protected abstract void centerGamesHomeImage();

    @Override
    protected void onCreate(Bundle state) {
        context = this;

        soundSequencer = new Handler(Looper.getMainLooper());

        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
        stage = getIntent().getIntExtra("stage", 7);
        syllableGame = getIntent().getStringExtra("syllableGame");
        gameNumber = getIntent().getIntExtra("gameNumber", 0);
        country = getIntent().getStringExtra("country");
        playerString = Util.returnPlayerStringToAppend(playerNumber);
        globalPoints = getIntent().getIntExtra("globalPoints", 0);

        prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        className = getClass().getName();
        uniqueGameLevelPlayerModeStageID = className + challengeLevel + playerString + syllableGame + stage;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerModeStageID + "_trackerCount", 0);
        hasChecked12Trackers = prefs.getBoolean(uniqueGameLevelPlayerModeStageID + "_hasChecked12Trackers", false);
        points = prefs.getInt(uniqueGameLevelPlayerModeStageID + "_points", 0);

        for(int s=0; s<stage; s++){
            cumulativeStageBasedTileList.addAll(Start.SAD);
            cumulativeStageBasedTileList.addAll(tileStagesLists.get(s));
            cumulativeStageBasedWordList.addAll(wordStagesLists.get(s));
        }

        for(int s=0; s<(stage-1); s++){
            previousStagesTileList.addAll(Start.SAD);
            previousStagesTileList.addAll(tileStagesLists.get(s));
            previousStagesWordList.addAll(wordStagesLists.get(s));
        }

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(state);
    }

    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);    // so we retain the Extras
        startActivity(intent);
        finish();

    }

    public void goBackToChoosePlayer(View view) {

        if (mediaPlayerIsPlaying) {
            return;
        }
        startActivity(new Intent(context, ChoosePlayer.class));
        finish();

    }

    public void goToAboutPage(View view) {

        Intent intent = getIntent();
        intent.setClass(context, About.class);
        startActivity(intent);

    }

    protected void updatePointsAndTrackers(int pointsIncrease) {
        setOptionsRowUnclickable();
        setAllTilesUnclickable();
        // Update global points and game points gem
        globalPoints+=pointsIncrease;
        points+=pointsIncrease;
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        // Update tracker icons
        for (int t = 0; t < TRACKERS.length; t++) {
            ImageView tracker = findViewById(TRACKERS[t]);
            if (t < trackerCount) {
                int resID = getResources().getIdentifier("zz_complete", "drawable", getPackageName());
                tracker.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_incomplete", "drawable", getPackageName());
                tracker.setImageResource(resID2);
            }
        }

        if (pointsIncrease > 0){ // Check whether 12 trackers were checked and how to proceed based on settings
            trackerCount++;

            if (trackerCount >= 12) {
                hasChecked12Trackers = true;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(uniqueGameLevelPlayerModeStageID + "_points", points);
            editor.apply();
            editor.putBoolean(uniqueGameLevelPlayerModeStageID + "_hasChecked12Trackers",
                    hasChecked12Trackers);
            editor.apply();
            editor.putInt(uniqueGameLevelPlayerModeStageID + "_trackerCount", trackerCount);
            editor.apply();
            getIntent().putExtra("globalPoints", globalPoints);

            // Update tracker icons
            for (int t = 0; t < TRACKERS.length; t++) {
                ImageView tracker = findViewById(TRACKERS[t]);
                if (t < trackerCount) {
                    int resID = getResources().getIdentifier("zz_complete", "drawable", getPackageName());
                    tracker.setImageResource(resID);
                } else {
                    int resID2 = getResources().getIdentifier("zz_incomplete", "drawable", getPackageName());
                    tracker.setImageResource(resID2);
                }
            }
            // LM
            // after12CheckedTrackers option 1: nothing happens; players keep playing even after checking all 12 trackers
            // after12CheckedTrackers option 2: app returns players to Earth after checking all 12 trackers. They can get back in. Will return to Earth again after another 12 correct answers.
            if (trackerCount > 0 && trackerCount % 12 == 0 && after12checkedTrackers == 2){
                soundSequencer.postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = getIntent();
                        intent.setClass(context, Earth.class); // so we retain the Extras
                        startActivity(intent);
                        finish();
                    }
                }, correctSoundDuration);

            }
            // after12CheckedTrackers option 3: app displays celebration screen and moves on to the next unchecked game after checking all 12 trackers.
            if (trackerCount > 0 && trackerCount % 12 == 0 && after12checkedTrackers == 3) {
                setOptionsRowUnclickable();
                setAllTilesUnclickable();
                soundSequencer.postDelayed(new Runnable() {
                    public void run() {
                        // Show celebration screen
                        Intent intent = getIntent();
                        intent.setClass(context, Celebration.class);
                        startActivity(intent);
                        finish();
                    }
                }, correctSoundDuration + 1800);

                // Then switch to next uncompleted game after 4 seconds
                Timer nextScreenTimer = new Timer();
                nextScreenTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Select and go to the next unfinished game to play
                        // If the game with gameNumber (gameNumber+1) has not checked all 12 trackers, go to it. If not, keep looking for one like this.
                        Intent intent = getIntent(); //gets intent that launched the current activity
                        String project = "org.alphatilesapps.alphatiles.";
                        boolean foundNextUncompletedGame = false;
                        int repeat = 0;

                        while (foundNextUncompletedGame == false && repeat < gameList.size()) {
                            // Get the info about the next game
                            gameNumber = gameNumber + 1;
                            if (gameNumber - 1 < gameList.size()) {
                                challengeLevel = Integer.valueOf(gameList.get(gameNumber - 1).level);
                                if (gameList.get(gameNumber-1).stage.equals("-")) {
                                    stage = 1;
                                } else {
                                    stage = Integer.valueOf(gameList.get(gameNumber - 1).stage);
                                }
                                syllableGame = gameList.get(gameNumber - 1).mode;
                                country = gameList.get(gameNumber - 1).country;
                            } else {
                                gameNumber = 1;
                                challengeLevel = Integer.valueOf(gameList.get(0).level);
                                if (gameList.get(0).stage.equals("-")) {
                                    stage = 1;
                                } else {
                                    stage = Integer.valueOf(gameList.get(0).stage);
                                }
                                syllableGame = gameList.get(0).mode;
                                country = gameList.get(0).country;
                            }
                            String activityClass = project + country;

                            try {
                                intent.setClass(context, Class.forName(activityClass));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            String nextUniqueGameLevelPlayerModeStageID = activityClass + challengeLevel + playerString + syllableGame + stage;
                            hasChecked12Trackers = prefs.getBoolean(nextUniqueGameLevelPlayerModeStageID + "_hasChecked12Trackers", false);

                            if (!hasChecked12Trackers) {
                                foundNextUncompletedGame = true;
                                intent.putExtra("challengeLevel", challengeLevel);
                                intent.putExtra("stage", stage);
                                intent.putExtra("syllableGame", syllableGame);
                                intent.putExtra("globalPoints", globalPoints);
                                intent.putExtra("gameNumber", gameNumber);
                                intent.putExtra("country", country);
                                startActivity(intent);
                                finish();
                            } else {
                                //keep looping
                            }
                            repeat++;
                        }

                        // If it's looped through all of the games and they're all complete, return to Earth
                        if (!foundNextUncompletedGame) {
                            intent.setClass(context, Earth.class); // so we retain the Extras
                            startActivity(intent);
                            finish();
                        }
                    }
                }, 4500);
            }
        }
    }

    protected void chooseWord() {
        boolean freshWord = false;

        while (!freshWord) { // Generates a new word if it got one of the last three tested words
            Random rand = new Random();

            if(stage == 1) { // Weight toward words with the highest correspondence ratio to stage 1 tiles
                double higherCorrespondenceThreshold  = stageCorrespondenceRatio + (1-stageCorrespondenceRatio)/2;
                Start.WordList higherCorrespondenceWords = new Start.WordList();
                Start.WordList lowerCorrespondenceWords = new Start.WordList();

                for (Start.Word word : wordStagesLists.get(0)) {
                    ArrayList<Start.Tile> tilesInThisWord = tileList.parseWordIntoTiles(word);
                    int nonSADtilesInThisWord = tilesInThisWord.size();
                    int correspondingTiles = 0;
                    for (int t = 0; t < tilesInThisWord.size(); t++) {
                        Start.Tile aTileInThisWord = tilesInThisWord.get(t);
                        for (int a = 0; a < tileStagesLists.get(0).size(); a++) {
                            Start.Tile aTileInTheStage = tileStagesLists.get(0).get(a);
                            if (aTileInThisWord.text.equals(aTileInTheStage.text)) {
                                if (differentiatesTileTypes) {
                                    String aTileInTheStageType = aTileInTheStage.typeOfThisTileInstance;
                                    if (aTileInTheStageType.equals("SAD")) {
                                        nonSADtilesInThisWord--;
                                    }
                                    String aTileInThisWordType = aTileInThisWord.typeOfThisTileInstance;
                                    if (aTileInTheStageType.equals(aTileInThisWordType)) {
                                        correspondingTiles++;
                                        break;
                                    }
                                } else {
                                    correspondingTiles++;
                                    break;
                                }
                            }
                        }
                    }
                    if((double)correspondingTiles/nonSADtilesInThisWord > higherCorrespondenceThreshold){
                        higherCorrespondenceWords.add(word);
                    } else {
                        lowerCorrespondenceWords.add(word);
                    }
                }

                int randomNumberForWeightingTowardHighCorrespondence = rand.nextInt(wordStagesLists.get(0).size());
                if (randomNumberForWeightingTowardHighCorrespondence < wordStagesLists.get(0).size() * 0.5 || lowerCorrespondenceWords.size()==0) { // Select a word with higher correspondence to stage 1 tiles (only tiles introduced so far)
                    int randomNumberForChoosingAHighCorrespondenceWord = rand.nextInt(higherCorrespondenceWords.size());
                    refWord = higherCorrespondenceWords.get(randomNumberForChoosingAHighCorrespondenceWord);
                } else { // Select a word that corresponds to stage 1 at a lower ratio
                    int randomNumberForChoosingALowCorrespondenceWord = rand.nextInt(lowerCorrespondenceWords.size());
                    refWord = lowerCorrespondenceWords.get(randomNumberForChoosingALowCorrespondenceWord);
                }
            } else { // If stage > 1, weight toward words that are new in this stage
                int randomNumberForWeightingTowardNewWords = rand.nextInt(cumulativeStageBasedWordList.size());
                if (randomNumberForWeightingTowardNewWords < cumulativeStageBasedWordList.size() * 0.5) { // Select a word that's added in the current stage 50% of the time
                    int randomNumberForChoosingANewWord = rand.nextInt(wordStagesLists.get(stage - 1).size());
                    refWord = wordStagesLists.get(stage - 1).get(randomNumberForChoosingANewWord);
                } else { // Select a word that was added in any previous stage
                    int randomNumberForChoosingAnOlderWord = rand.nextInt(previousStagesWordList.size());
                    refWord = previousStagesWordList.get(randomNumberForChoosingAnOlderWord);
                }
            }

            // If this word isn't one of the 12 previously tested words, we're good
            if (!last12Words.contains(wordInLWC)) {
                freshWord = true;
                if(last12Words.size()==12){
                    last12Words.poll();
                }
                last12Words.add(wordInLWC);
            } else {
                freshWord = false;
            }
        }
    }
    protected void setAllGameButtonsUnclickable() {
        for (int t = 0; t < visibleGameButtons; t++) {
            TextView gameTile = findViewById(getGameButtons()[t]);
            gameTile.setClickable(false);
        }
    }

    protected void setAllGameButtonsClickable() {
        for (int t = 0; t < visibleGameButtons; t++) {
            TextView gameTile = findViewById(getGameButtons()[t]);
            gameTile.setClickable(true);
        }
    }

    protected void setOptionsRowUnclickable() {
        ImageView wordImage = findViewById(R.id.wordImage);
        if (wordImage != null)
            wordImage.setClickable(false);
        if (getWordImages() != null)
            for (int i = 0; i < 4; i++) {
                wordImage = findViewById(getWordImages()[i]);
                wordImage.setClickable(false);
            }
        ImageView repeatImage = findViewById(R.id.repeatImage);
        repeatImage.setClickable(false);
    }

    protected void setOptionsRowClickable() {
        ImageView wordImage = findViewById(R.id.wordImage);
        ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);
        gamesHomeImage.setClickable(true);
        if (wordImage != null)
            wordImage.setClickable(true);
        if (getWordImages() != null)
            for (int i = 0; i < 4; i++) {
                wordImage = findViewById(getWordImages()[i]);
                wordImage.setClickable(true);
            }
        ImageView repeatImage = findViewById(R.id.repeatImage);
        repeatImage.setClickable(true);
    }

    protected void setAdvanceArrowToBlue() {
        ImageView repeatImage = findViewById(R.id.repeatImage);
        repeatImage.setBackgroundResource(0);
        repeatImage.setImageResource(R.drawable.zz_forward);
    }

    protected void setAdvanceArrowToGray() {
        ImageView repeatImage = findViewById(R.id.repeatImage);
        repeatImage.setBackgroundResource(0);
        repeatImage.setImageResource(R.drawable.zz_forward_inactive);
    }

    public void clickPicHearAudio(View view) {
        playActiveWordClip(false);
    }

    protected void playActiveWordClip(final boolean playFinalSound) {
        if (tempSoundPoolSwitch) {
            playActiveWordClip1(playFinalSound);    //SoundPool
        } else
            playActiveWordClip0(playFinalSound);    //MediaPlayer
    }

    protected void playActiveWordClip1(final boolean playFinalSound) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        if (wordAudioIDs.containsKey(refWord.wordInLWC)) {
            gameSounds.play(wordAudioIDs.get(refWord.wordInLWC), 1.0f, 1.0f, 2, 0, 1.0f);
        }
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (playFinalSound) {
                    updatePointsAndTrackers(0);
                    repeatLocked = false;
                    playCorrectFinalSound();
                } else {
                    if (repeatLocked) {
                        setAllGameButtonsClickable();
                    }
                    if (after12checkedTrackers == 1){
                        setOptionsRowClickable();
                        //JP: in setting 1 we always want to keep advancing to the next tile/word/image regardless
                    }
                    else if (trackerCount >0 && trackerCount % 12 != 0) {
                        setOptionsRowClickable();
                        //JP: because updatePointsAndTrackers will take care of setting it clickable otherwise
                        // and we don't want the user to be able to advance before returning to earth (2) or
                        // before seeing the celebration screen (3)
                    }
                    else if (trackerCount == 0){
                        setOptionsRowClickable();
                    }
                }
            }
        }, refWord.duration);
    }

    protected void playActiveWordClip0(final boolean playFinalSound) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        int resID = getResources().getIdentifier(refWord.wordInLWC, "raw", getPackageName());
        final MediaPlayer mp1 = MediaPlayer.create(this, resID);
        mediaPlayerIsPlaying = true;
        //mp1.start();
        mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                mpCompletion(mp1, playFinalSound);
            }
        });
        mp1.start();
    }

    protected void playCorrectSoundThenActiveWordClip(final boolean playFinalSound) {
        if (tempSoundPoolSwitch)
            playCorrectSoundThenActiveWordClip1(playFinalSound);
        else
            playCorrectSoundThenActiveWordClip0(playFinalSound);
    }

    protected void playCorrectSoundThenActiveWordClip1(final boolean playFinalSound) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                setAllTilesClickable();
                if (after12checkedTrackers == 1){
                    setOptionsRowClickable();
                    //JP: in setting 1 we always want to keep advancing to the next tile/word/image regardless
                }
                else if (trackerCount >0 && trackerCount % 12 != 0) {
                    setOptionsRowClickable();
                    //JP: because updatePointsAndTrackers will take care of setting it clickable otherwise
                    // and we don't want the user to be able to advance before returning to earth (2) or
                    // before seeing the celebration screen (3)
                }
                playActiveWordClip(playFinalSound);
            }
        }, correctSoundDuration);
    }


    protected void playCorrectSoundThenActiveWordClip0(final boolean playFinalSound) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        MediaPlayer mp2 = MediaPlayer.create(this, R.raw.zz_correct);
        mediaPlayerIsPlaying = true;
        mp2.start();
        mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp2) {
                mp2.reset(); //JP: fixed "mediaplayer went away with unhandled events" issue
                mp2.release();
                playActiveWordClip(playFinalSound);
            }
        });
    }

    protected void playIncorrectSound() {
        if (tempSoundPoolSwitch)
            playIncorrectSound1();
        else
            playIncorrectSound0();
    }

    protected void playIncorrectSound1() {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        gameSounds.play(incorrectSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        setAllGameButtonsClickable();
        setOptionsRowClickable();
    }

    protected void playIncorrectSound0() {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        MediaPlayer mp3 = MediaPlayer.create(this, R.raw.zz_incorrect);
        mediaPlayerIsPlaying = true;
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                mediaPlayerIsPlaying = false;
                setAllGameButtonsClickable();
                setOptionsRowClickable();
                mp3.reset(); //JP
                mp3.release();
            }
        });
    }

    protected void playCorrectFinalSound() {
        if (tempSoundPoolSwitch)
            playCorrectFinalSound1();
        else
            playCorrectFinalSound0();
    }

    protected void playCorrectFinalSound1() {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        setAllTilesClickable();
        if (after12checkedTrackers == 1){
            setOptionsRowClickable();
            //JP: in setting 1 we always want to keep advancing to the next tile/word/image regardless
        }
        else if (trackerCount >0 && trackerCount % 12 != 0) {
            setOptionsRowClickable();
            //JP: because updatePointsAndTrackers will take care of setting it clickable otherwise
            // and we don't want the user to be able to advance before returning to earth (2) or
            // before seeing the celebration screen (3)
        }
    }

    protected void playCorrectFinalSound0() {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        mediaPlayerIsPlaying = true;
        MediaPlayer mp3 = MediaPlayer.create(this, R.raw.zz_correct_final);
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                mediaPlayerIsPlaying = false;
                setAllGameButtonsClickable();
                setOptionsRowClickable();
                mp3.reset(); //JP
                mp3.release();
            }
        });
    }

    public void playAudioInstructions(View view) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();
        mediaPlayerIsPlaying = true;
        MediaPlayer mp3 = MediaPlayer.create(this, getAudioInstructionsResID());
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                mediaPlayerIsPlaying = false;
                setAllGameButtonsClickable();
                setOptionsRowClickable();
                mp3.release();
            }
        });

    }

    protected void mpCompletion(MediaPlayer mp, boolean isFinal) {
        if (isFinal) {
            updatePointsAndTrackers(0);
            repeatLocked = false;
            playCorrectFinalSound();
        } else {
            mediaPlayerIsPlaying = false;
            if (repeatLocked) {
                setAllGameButtonsClickable();
            }
            setOptionsRowClickable();
            mp.reset(); //JP
            mp.release();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    protected void fixConstraintsRTL(int gameID) {
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.pointsImage, ConstraintSet.END, R.id.gamesHomeImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.START, R.id.pointsImage, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.instructions, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.instructions, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.applyTo(constraintLayout);
    }

}
