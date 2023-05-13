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
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static org.alphatilesapps.alphatiles.Start.MULTI_TYPE_TILES;
import static org.alphatilesapps.alphatiles.Start.SAD;
import static org.alphatilesapps.alphatiles.Start.differentiatesTileTypes;
import static org.alphatilesapps.alphatiles.Start.gameList;
import static org.alphatilesapps.alphatiles.Start.langInfoList;
import static org.alphatilesapps.alphatiles.Start.stageCorrespondenceRatio;
import static org.alphatilesapps.alphatiles.Start.tileHashMap;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.tileStagesLists;
import static org.alphatilesapps.alphatiles.Start.tileTypeHashMapWithMultipleTypes;
import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.wordStagesLists;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;
import static org.alphatilesapps.alphatiles.Start.correctSoundDuration;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.incorrectSoundID;
import static org.alphatilesapps.alphatiles.Start.wordDurations;
import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.after12checkedTrackers;

import java.util.logging.Logger;


public abstract class GameActivity extends AppCompatActivity {

    // KP, Oct 2020
    Context context;
    String className;
    String country;
    String scriptDirection = langInfoList.find("Script direction (LTR or RTL)");
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

    Start.TileListWithMultipleTypes cumulativeStageBasedTileList = new Start.TileListWithMultipleTypes();
    Start.WordList cumulativeStageBasedWordList = new Start.WordList();
    Start.TileListWithMultipleTypes previousStagesTileList = new Start.TileListWithMultipleTypes();
    Start.WordList previousStagesWordList = new Start.WordList();
    ArrayList<String> parsedWordArrayFinal;
    ArrayList<String> parsedWordSyllArrayFinal;

    int visibleTiles;
    String wordInLWC = "";    // the lWC word (e.g. Spanish), which exactly matches the image and audio file names
    String wordInLOP = "";    // the corresponding word in the language of play (e.g. Me'phaa)
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";

    boolean mediaPlayerIsPlaying = false;
    boolean repeatLocked = true;
    Handler soundSequencer;

    protected static final int[] TRACKERS = {
            R.id.tracker01, R.id.tracker02, R.id.tracker03, R.id.tracker04, R.id.tracker05, R.id.tracker06, R.id.tracker07, R.id.tracker08, R.id.tracker09, R.id.tracker10,
            R.id.tracker11, R.id.tracker12

    };

    protected abstract int[] getTileButtons();

    protected abstract int[] getWordImages();

    protected abstract int getAudioInstructionsResID();

    protected abstract void centerGamesHomeImage();
    private static final Logger LOGGER = Logger.getLogger(GameActivity.class.getName());

    /*
    This testing method can be run from onCreate() to make sure each word is parsed correctly into tiles
    and recombined correctly from those tiles.
    Uncomment the LOGGER.info(data) line to log this info for all of the words in the wordlist.
    REMOVE calls to this testing method before building an app.
     */
    protected void testParsingAndCombining() {

        String data = "Word\tTiles Parsed\tTiles recombined\n";
        for(Start.Word w : wordList){
            ArrayList<String> thisParsedTileArray = tileList.parseWordIntoTiles(w.localWord, w.localWord);
            data += w.localWord + "\t" + thisParsedTileArray.toString() + "\t" + combineTilesToMakeWord(thisParsedTileArray, thisParsedTileArray, -1, w.localWord) + "\n";
            if(!wordInLOPWithStandardizedSequenceOfCharacters(w.localWord).equals(combineTilesToMakeWord(thisParsedTileArray, thisParsedTileArray, -1, w.localWord))){
                LOGGER.info("Parsing/combining error: " + w.localWord + "\t" + thisParsedTileArray.toString() + "\t" + combineTilesToMakeWord(thisParsedTileArray, thisParsedTileArray, -1, w.localWord) + "\n");
            }
        }
        //LOGGER.info(data);
    }

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
            cumulativeStageBasedTileList.addAll(SAD);
            cumulativeStageBasedTileList.addAll(tileStagesLists.get(s));
            cumulativeStageBasedWordList.addAll(wordStagesLists.get(s));
        }

        for(int s=0; s<(stage-1); s++){
            previousStagesTileList.addAll(SAD);
            previousStagesTileList.addAll(tileStagesLists.get(s));
            previousStagesWordList.addAll(wordStagesLists.get(s));
        }

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        testParsingAndCombining();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(state);
    }

    public void goBackToEarth(View view) {
        gameSounds.stop(getAudioInstructionsResID());
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
            if (trackerCount > 0 && trackerCount % 12 == 0 && after12checkedTrackers == 2) {
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
                                challengeLevel = Integer.valueOf(gameList.get(gameNumber - 1).gameLevel);
                                if (gameList.get(gameNumber-1).stage.equals("-")) {
                                    stage = 1;
                                } else {
                                    stage = Integer.valueOf(gameList.get(gameNumber - 1).stage);
                                }
                                syllableGame = gameList.get(gameNumber - 1).gameMode;
                                country = gameList.get(gameNumber - 1).gameCountry;
                            } else {
                                gameNumber = 1;
                                challengeLevel = Integer.valueOf(gameList.get(0).gameLevel);
                                if (gameList.get(0).stage.equals("-")) {
                                    stage = 1;
                                } else {
                                    stage = Integer.valueOf(gameList.get(0).stage);
                                }
                                syllableGame = gameList.get(0).gameMode;
                                country = gameList.get(0).gameCountry;
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
                    ArrayList<String> tilesInThisWord = tileList.parseWordIntoTiles(word.localWord, word.localWord);
                    int nonSADtilesInThisWord = tilesInThisWord.size();
                    int correspondingTiles = 0;
                    for (int t = 0; t < tilesInThisWord.size(); t++) {
                        for (int a = 0; a < tileStagesLists.get(0).size(); a++) {
                            String aTileInTheStage = tileStagesLists.get(0).get(a);
                            String aTileInTheStageSuffix = Character.toString(aTileInTheStage.charAt(aTileInTheStage.length() - 1));
                            String aTileInTheStageWithoutSuffix = aTileInTheStage;
                            if (aTileInTheStageSuffix.equals("B") || aTileInTheStageSuffix.equals("C")) {
                                aTileInTheStageWithoutSuffix = aTileInTheStageWithoutSuffix.substring(0, aTileInTheStageWithoutSuffix.length() - 1);
                            }
                            if (tilesInThisWord.get(t).equals(aTileInTheStageWithoutSuffix)) {
                                if (differentiatesTileTypes) {
                                    String aTileInTheStageType = tileTypeHashMapWithMultipleTypes.get(aTileInTheStage);
                                    String tileInThisWordType;
                                    if (MULTI_TYPE_TILES.contains(tilesInThisWord.get(t))) {
                                        tileInThisWordType = tileList.getInstanceTypeForMixedTile(t, tilesInThisWord, word.localWord);
                                    } else {
                                        tileInThisWordType = tileTypeHashMapWithMultipleTypes.get(tilesInThisWord.get(t));
                                    }

                                    if (aTileInTheStageType.equals("SAD")) {
                                        nonSADtilesInThisWord--;
                                    }
                                    if (aTileInTheStageType.equals(tileInThisWordType)) {
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
                    wordInLWC = higherCorrespondenceWords.get(randomNumberForChoosingAHighCorrespondenceWord).nationalWord;
                    wordInLOP = higherCorrespondenceWords.get(randomNumberForChoosingAHighCorrespondenceWord).localWord;
                } else { // Select a word that corresponds to stage 1 at a lower ratio
                    int randomNumberForChoosingALowCorrespondenceWord = rand.nextInt(lowerCorrespondenceWords.size());
                    wordInLWC = lowerCorrespondenceWords.get(randomNumberForChoosingALowCorrespondenceWord).nationalWord;
                    wordInLOP = lowerCorrespondenceWords.get(randomNumberForChoosingALowCorrespondenceWord).localWord;

                }
            } else { // If stage > 1, weight toward words that are new in this stage
                int randomNumberForWeightingTowardNewWords = rand.nextInt(cumulativeStageBasedWordList.size());
                if (randomNumberForWeightingTowardNewWords < cumulativeStageBasedWordList.size() * 0.5) { // Select a word that's added in the current stage 50% of the time
                    int randomNumberForChoosingANewWord = rand.nextInt(wordStagesLists.get(stage - 1).size());
                    wordInLWC = wordStagesLists.get(stage - 1).get(randomNumberForChoosingANewWord).nationalWord;
                    wordInLOP = wordStagesLists.get(stage - 1).get(randomNumberForChoosingANewWord).localWord;
                } else { // Select a word that was added in any previous stage
                    int randomNumberForChoosingAnOlderWord = rand.nextInt(previousStagesWordList.size());
                    wordInLWC = previousStagesWordList.get(randomNumberForChoosingAnOlderWord).nationalWord;
                    wordInLOP = previousStagesWordList.get(randomNumberForChoosingAnOlderWord).localWord;
                }
            }

            // If this word isn't one of the 3 previously tested words, we're good
            if (!wordInLWC.equals(lastWord)
                    && !wordInLWC.equals(secondToLastWord)
                    && !wordInLWC.equals(thirdToLastWord)) {
                freshWord = true;
                thirdToLastWord = secondToLastWord;
                secondToLastWord = lastWord;
                lastWord = wordInLWC;
            }
        }
    }
    protected void setAllTilesUnclickable() {
        for (int t = 0; t < visibleTiles; t++) {
            TextView gameTile = findViewById(getTileButtons()[t]);
            gameTile.setClickable(false);
        }
    }

    protected void setAllTilesClickable() {
        for (int t = 0; t < visibleTiles; t++) {
            TextView gameTile = findViewById(getTileButtons()[t]);
            gameTile.setClickable(true);
        }
    }

    protected void setOptionsRowUnclickable() {
        ImageView repeatImage = findViewById(R.id.repeatImage);
        ImageView wordImage = findViewById(R.id.wordImage);
        repeatImage.setBackgroundResource(0);
        repeatImage.setImageResource(R.drawable.zz_forward_inactive);
        repeatImage.setClickable(false);
        if (wordImage != null)
            wordImage.setClickable(false);
        if (getWordImages() != null)
            for (int i = 0; i < 4; i++) {
                wordImage = findViewById(getWordImages()[i]);
                wordImage.setClickable(false);
            }
    }

    protected void setOptionsRowClickable() {
        ImageView repeatImage = findViewById(R.id.repeatImage);
        ImageView wordImage = findViewById(R.id.wordImage);
        ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);
        repeatImage.setBackgroundResource(0);
        repeatImage.setImageResource(R.drawable.zz_forward);
        repeatImage.setClickable(true);
        gamesHomeImage.setClickable(true);
        if (wordImage != null)
            wordImage.setClickable(true);
        if (getWordImages() != null)
            for (int i = 0; i < 4; i++) {
                wordImage = findViewById(getWordImages()[i]);
                wordImage.setClickable(true);
            }
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
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        if (wordAudioIDs.containsKey(wordInLWC)) {
            gameSounds.play(wordAudioIDs.get(wordInLWC), 1.0f, 1.0f, 2, 0, 1.0f);
        }
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (playFinalSound) {
                    updatePointsAndTrackers(0);
                    repeatLocked = false;
                    playCorrectFinalSound();
                } else {
                    if (repeatLocked) {
                        setAllTilesClickable();
                    }
                    setOptionsRowClickable();
                }
            }
        }, wordDurations.get(wordInLWC));
    }

    protected void playActiveWordClip0(final boolean playFinalSound) {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        int resID = getResources().getIdentifier(wordInLWC, "raw", getPackageName());
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
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                setAllTilesClickable();
                setOptionsRowClickable();
                playActiveWordClip(playFinalSound);
            }
        }, correctSoundDuration);
    }


    protected void playCorrectSoundThenActiveWordClip0(final boolean playFinalSound) {
        setAllTilesUnclickable();
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
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        gameSounds.play(incorrectSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        setAllTilesClickable();
        setOptionsRowClickable();
    }

    protected void playIncorrectSound0() {
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
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
        setAllTilesClickable();
        setOptionsRowClickable();
    }

    protected void playCorrectFinalSound0() {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        mediaPlayerIsPlaying = true;
        MediaPlayer mp3 = MediaPlayer.create(this, R.raw.zz_correct_final);
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                mediaPlayerIsPlaying = false;
                setAllTilesClickable();
                setOptionsRowClickable();
                mp3.reset(); //JP
                mp3.release();
            }
        });
    }

    public void playAudioInstructions(View view) {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        mediaPlayerIsPlaying = true;
        MediaPlayer mp3 = MediaPlayer.create(this, getAudioInstructionsResID());
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

    protected void mpCompletion(MediaPlayer mp, boolean isFinal) {
        if (isFinal) {
            updatePointsAndTrackers(0);
            repeatLocked = false;
            playCorrectFinalSound();
        } else {
            mediaPlayerIsPlaying = false;
            if (repeatLocked) {
                setAllTilesClickable();
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

    /*
    Sometimes, the assembled version of words differs slightly from what's in the wordlist, because of invisible character stacking differences.
    This method takes a String, word, and returns the assembled version so that when compared to other assembled tile lists, it will match up.

    word: The word you would like to represent in standardized assembled form
     */
    protected String wordInLOPWithStandardizedSequenceOfCharacters(String wordInLOPFromWordList) {

        ArrayList<String> tilesInWordSpelledCorrectly = tileList.parseWordIntoTiles(wordInLOPFromWordList, wordInLOPFromWordList);
        return combineTilesToMakeWord(tilesInWordSpelledCorrectly, tilesInWordSpelledCorrectly, -1, wordInLOPFromWordList);
    }

    /*
    This method takes the List of tiles that make up a word (or pseudoword) and assembles them into a String in the correct order.
    It supports assembly of complex vowels, which have components before and after/above/below the consonant pronounced before them
    When comparing words that have complex vowels, simple concatenation of tiles will not produce the correct string.
    Therefore, use this method rather than concatenating tiles.

    tilesInThisWordOption: The tile array being combined. It could be a wordlist word or an alternate word option
    tilesInWordListWord: The tile array for the target word list word. The word option tiles could be the same as this or one tile different.
    indexOfReplacedTile: default -1; If building a pseudoword, this is the index of the tile that has been replaced from the target word
    wordInLOP: The target word string, in the language of play
     */
    public static String combineTilesToMakeWord(List<String> tilesInThisWordOption, List<String> tilesInWordlistWord, int indexOfReplacedTile, String wordInLOP) {
        StringBuilder builder = new StringBuilder("");
        String previousConsonant = "";
        String previousDiacritics = "";
        String previousAboveOrBelowVowel = "";
        String previousString = "";
        String replacedTile = "";
        if (indexOfReplacedTile>-1){
            replacedTile = tilesInWordlistWord.get(indexOfReplacedTile);
        }
        if(indexOfReplacedTile>0){
            previousString = tilesInThisWordOption.get(indexOfReplacedTile-1);
        }

        int index = 0;
        for (String tileString : tilesInThisWordOption) {
            if (tileString != null) {
                String stringToAppend = tileString;
                Start.Tile tile = tileHashMap.get(tileString);
                if(tile != null){
                    if (tile.tileType.equals("C")){
                        previousConsonant = tileString;
                        previousAboveOrBelowVowel = ""; // Reset; new syllable
                        previousDiacritics = ""; // Reset; new syllable
                    }
                    if(tile.tileType.equals("AD")){
                        previousDiacritics = tileString;
                    }
                    if(tile.tileType.equals("AV") || tile.tileType.equals("BV")){
                        previousAboveOrBelowVowel = tileString;
                    }
                }
                boolean replacingLVwithOtherV;
                if(!tileHashMap.containsKey(replacedTile)){
                    replacingLVwithOtherV = false;
                } else {
                    replacingLVwithOtherV = !replacedTile.equals("") && tileHashMap.get(replacedTile).tileType.equals("LV");
                }

                if (replacingLVwithOtherV && index == indexOfReplacedTile && (tileHashMap.get(stringToAppend).tileType.equals("AV") || tileHashMap.get(stringToAppend).tileType.equals("BV")|| tileHashMap.get(stringToAppend).tileType.equals("FV") || tileHashMap.get(stringToAppend).tileType.equals("V"))) {
                    previousString = stringToAppend;
                    // Don't append this string. (That would put AV, FV, or V[LV+◌+FV] in LV position.)
                } else if (replacingLVwithOtherV && index == (indexOfReplacedTile + 1) && (tileHashMap.get(previousString).tileType.equals("AV") || tileHashMap.get(stringToAppend).tileType.equals("BV") || tileHashMap.get(previousString).tileType.equals("FV"))) { // Combine unappended AV/BV/FV now and append
                    stringToAppend = stringToAppend + previousString.replace("◌", ""); // previousString = AV, BV, FV
                    // ^Now it's in the right place.
                    builder.append(stringToAppend);
                    previousString = stringToAppend;
                } else if (replacingLVwithOtherV && index == (indexOfReplacedTile + 1) && tileHashMap.get(previousString).tileType.equals("V")){
                    stringToAppend = previousString.replace("◌", stringToAppend); // [LV+◌+FV], replace the ◌ with the consonant tile you are adding now
                    builder.append(stringToAppend);
                    previousString = stringToAppend;
                }else {
                    if (stringToAppend.contains("◌")) {
                        // Put the previous consonant and (optional) above/below vowel as the base of diacritics
                        // Or consonant and diacritics as the base of a vowel with a placeholder
                        // The stackInProperSequence() method will make some more fixes later if necessary
                        String base = "";
                        if(tileHashMap.get(stringToAppend).tileType.equals("AD")){
                            base = previousConsonant + previousAboveOrBelowVowel.replace("◌", "");
                        } else if (tileHashMap.get(stringToAppend).tileType.contains("V")){
                            base = previousConsonant + previousDiacritics.replace("◌", "");
                        }
                        stringToAppend = stringToAppend.replace("◌", base);
                        builder.delete(builder.length() - base.length(), builder.length());
                    }
                    builder.append(stringToAppend);
                    previousString = stringToAppend;
                }
            }
            index++;
        }
        String processedString = builder.toString();
        return stackInProperSequence(processedString, wordInLOP);
    }

    /*
    ADs (Above/Following Diacritics) should be placed above AVs (Above Vowels).
    If a gametiles list stores C+AD tiles, assembling these C+AD tiles with AV tiles or complex vowels
    will put ADs and AVS in the reverse order.
    This method fixes that.

    s: String to check and fix the stacking in, if necessary
     */
    public static String stackInProperSequence(String assembledWordInProgress, String wordInLOP) {

        if (assembledWordInProgress.length() > 0) {

            String correctlyStackedString = assembledWordInProgress;
            ArrayList<String> parsedWordArrayPreliminary = tileList.parseWordIntoTilesPreliminary(wordInLOP); // Might not be a full word
            ArrayList<String> prohibitedCharSequences = generateProhibitedCharSequences(parsedWordArrayPreliminary, wordInLOP); // Check on the bad combinations in the target word

            if (prohibitedCharSequences.size() > 0) {
                for (int c1 = assembledWordInProgress.length()-2; c1 > -1; c1--) { // Start with the second to last char and last char, work backwards
                    int c2 = c1 + 1;
                    String this2CharSequence = String.valueOf(correctlyStackedString.charAt(c1)) + String.valueOf(correctlyStackedString.charAt(c2));
                    if (prohibitedCharSequences.contains(this2CharSequence)) {
                        String fixed2CharSequence = String.valueOf(correctlyStackedString.charAt(c2)) + String.valueOf(correctlyStackedString.charAt(c1));
                        correctlyStackedString = correctlyStackedString.replace(this2CharSequence, fixed2CharSequence);
                    }
                }
            }
            return correctlyStackedString;
        }
        return assembledWordInProgress;
    }

    /*
    AVs should be stacked before ADs, not the other way around.
    ADs should not be placed on top of FVs, but only on top of a C+(AV)+(AD) base.
    This initializes the prohibitedCharSequences ArrayList with the prohibited stacking combinations in this word.
     */
    public static ArrayList<String> generateProhibitedCharSequences(ArrayList<String> parsedWordArrayPreliminary, String wordInLOP) {
        ArrayList<String> prohibitedCharSequences = new ArrayList<String>();
        ArrayList<String> AVs = new ArrayList<String>();
        ArrayList<String> ADs = new ArrayList<String>();
        ArrayList<String> FVs = new ArrayList<String>();

        // Since we're going char by char, it's important that teams store their ADs by themselves (single chars) in the gametiles list, even if they also store them
        // attached to consonants.
        for (int i = 0; i < wordInLOP.length(); i++) {
            Start.Tile thisTile = tileHashMap.get(String.valueOf(wordInLOP.charAt(i)));
            if(!(thisTile == null)){
                String thisTileChar = thisTile.baseTile;
                String typeOfThisInstanceOfThisTile = "";
                if(MULTI_TYPE_TILES.contains(thisTileChar)){
                    int indexInPreliminaryArray = -1; // Figure out which instance in the word of this char from the tile list we are looking at
                    String theStringUpToThisPoint = wordInLOP.substring(0, i);
                    int lastIndexOfThisMultifunctionTile = 0;
                    int instancesBeforeTheOneWeWant = 0;

                    while (lastIndexOfThisMultifunctionTile != -1) {
                        lastIndexOfThisMultifunctionTile = theStringUpToThisPoint.indexOf(thisTileChar, lastIndexOfThisMultifunctionTile);
                        if (lastIndexOfThisMultifunctionTile != -1) {
                            instancesBeforeTheOneWeWant++;
                            lastIndexOfThisMultifunctionTile += 1; // Start looking again after the instance we just found
                        }
                    }
                    // Figure out its instance type using the index of that instance in the preliminary parsed array
                    for(int t = 0; t<parsedWordArrayPreliminary.size(); t++){
                        if(parsedWordArrayPreliminary.get(t).contains(thisTileChar)){
                            if(instancesBeforeTheOneWeWant == 0){
                                indexInPreliminaryArray = t;
                            }
                            instancesBeforeTheOneWeWant--;
                        }
                    }
                    typeOfThisInstanceOfThisTile = tileList.getInstanceTypeForMixedTilePreliminary(indexInPreliminaryArray, parsedWordArrayPreliminary, wordInLOP);
                } else {
                    typeOfThisInstanceOfThisTile = thisTile.tileType;
                }
                if (typeOfThisInstanceOfThisTile.equals("AV")) {
                    AVs.add(thisTileChar);
                } else if (typeOfThisInstanceOfThisTile.equals("AD")) {
                    ADs.add(thisTileChar);
                } else if (typeOfThisInstanceOfThisTile.equals("FV")) {
                    FVs.add(thisTileChar);
                }
            }
        }
        // Create and add the prohibited two-char sequences from this word
        for (int d = 0; d < ADs.size(); d++) {
            for (int v = 0; v < AVs.size(); v++) {
                prohibitedCharSequences.add(ADs.get(d) + AVs.get(v));
            }
        }
        for (int f = 0; f < FVs.size(); f++) {
            for (int d = 0; d < ADs.size(); d++) {
                if(!tileHashMap.keySet().contains(FVs.get(f) + ADs.get(d))){
                    prohibitedCharSequences.add(FVs.get(f) + ADs.get(d));
                }
            }
        }
        for(int d = 0; d<ADs.size(); d++){
            prohibitedCharSequences.add("_" + ADs.get(d));
        }
        return prohibitedCharSequences;
    }

}
