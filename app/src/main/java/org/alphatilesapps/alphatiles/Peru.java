package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

public class Peru extends GameActivity {
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    boolean peruHasChecked12Trackers;

    protected static final int[] TILE_BUTTONS = {
            R.id.word1, R.id.word2, R.id.word3, R.id.word4
    };

    protected int[] getTileButtons() {
        return TILE_BUTTONS;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier("peru_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.peruCL;
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
        setContentView(R.layout.peru);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.peruCL);
        }

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        if (challengeLevel == 2) {

            Collections.shuffle(VOWELS);
            Collections.shuffle(CONSONANTS);
            Collections.shuffle(TONES);

        }
        visibleTiles = TILE_BUTTONS.length;
        updatePointsAndTrackers(0);
        incorrectAnswersSelected = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.add("");
        }
        playAgain();
    }

    @Override
    public void onBackPressed() {
        // no action
    }


    public void repeatGame(View view) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {
        repeatLocked = true;
        setAdvanceArrowToGray();
        chooseWord();
        parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
        int tileLength = parsedWordArrayFinal.size();

        // Set thematic colors for four word choice TextViews, also make clickable
        for (int i = 0; i < TILE_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(TILE_BUTTONS[i]);
            String wordColorStr = COLORS.get(i);
            int wordColorNo = Color.parseColor(wordColorStr);
            nextWord.setBackgroundColor(wordColorNo);
            nextWord.setTextColor(Color.parseColor("#FFFFFF")); // white
            nextWord.setClickable(true);
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        ImageView wordImage = (ImageView) findViewById(R.id.wordImage);
        wordImage.setClickable(true);

        Random rand = new Random();
        int indexOfCorrectAnswerAmongChoices = rand.nextInt(4);
        List<String> shuffledDistractorTiles = Arrays.asList(Start.tileList.get(Start.tileList.returnPositionInAlphabet(parsedWordArrayFinal.get(0))).altTiles);
        Collections.shuffle(shuffledDistractorTiles);

        int incorrectLapNo = 0;
        for (int i = 0; i < TILE_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(TILE_BUTTONS[i]);
            if (i == indexOfCorrectAnswerAmongChoices) {
                nextWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP)); // the correct answer (the unmodified version of the word)
            } else {

                incorrectLapNo++;
                boolean isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates (or dangerous combinations)
                switch (challengeLevel) {

                    case 1:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (THE FIRST TILE) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO
                        while (isDuplicateAnswerChoice) {
                            List<String> tempArray1 = new ArrayList<>(parsedWordArrayFinal);
                            tempArray1.set(0, shuffledDistractorTiles.get(incorrectLapNo - 1)); // KP // LM
                            StringBuilder builder1 = new StringBuilder("");
                            for (String s : tempArray1) {
                                builder1.append(s);
                            }
                            String incorrectChoice1 = builder1.toString();
                            nextWord.setText(incorrectChoice1);
                            isDuplicateAnswerChoice = false;
                            for (int j = 0; j < incorrectChoice1.length() - 2; j++) {
                                if (incorrectChoice1.substring(j, j + 3).equals("للہ")) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }

                        }
                        break;
                    case 2:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS ANY GAME TILE FROM THE WHOLE ARRAY
                        // JP changed: REPLACEMENT IS NOW ANY TILE OF THE SAME TYPE (C OR V OR T) FROM THE WHOLE ARRAY

                        //fix: some accidental duplicates
                        while (isDuplicateAnswerChoice) {
                            int randomNum3 = rand.nextInt(tileLength - 1);       // KP // this represents which position in word string will be replaced
                            List<String> tempArray2 = new ArrayList<>(parsedWordArrayFinal);
                            int randomNum4;
                            if (VOWELS.contains(tempArray2.get(randomNum3))) {
                                randomNum4 = rand.nextInt(VOWELS.size());       // KP // this represents which game tile will overwrite some part of the correct wor
                                tempArray2.set(randomNum3, VOWELS.get(randomNum4)); // JP
                            } else if (CONSONANTS.contains(tempArray2.get(randomNum3))) {
                                randomNum4 = rand.nextInt(CONSONANTS.size());
                                tempArray2.set(randomNum3, CONSONANTS.get(randomNum4)); // JP
                            } else if (TONES.contains(tempArray2.get(randomNum3))) {
                                randomNum4 = rand.nextInt(TONES.size());
                                tempArray2.set(randomNum3, TONES.get(randomNum4)); // JP
                            }

                            StringBuilder builder2 = new StringBuilder("");
                            for (String s : tempArray2) {
                                builder2.append(s);
                            }
                            String incorrectChoice2 = builder2.toString();
                            nextWord.setText(incorrectChoice2);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for (int answerChoice = 0; answerChoice < i; answerChoice++) {
                                if (incorrectChoice2.equals(((TextView) findViewById(TILE_BUTTONS[answerChoice])).getText().toString())) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            if (incorrectChoice2.equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
                                isDuplicateAnswerChoice = true;
                            }
                            for (int j = 0; j < incorrectChoice2.length() - 2; j++) {
                                if (incorrectChoice2.substring(j, j + 3).equals("للہ")) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }

                        }
                        break;

                    case 3:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO

                        isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates

                        while (isDuplicateAnswerChoice) {
                            int randomNum5 = rand.nextInt(tileLength - 1);       // this represents which position in word string will be replaced
                            List<String> tempArray3 = new ArrayList<>(parsedWordArrayFinal);
                            tempArray3.set(randomNum5, Start.tileList.returnRandomCorrespondingTile(parsedWordArrayFinal.get(randomNum5)));
                            StringBuilder builder3 = new StringBuilder("");
                            for (String s : tempArray3) {
                                builder3.append(s);
                            }
                            String incorrectChoice3 = builder3.toString();
                            nextWord.setText(incorrectChoice3);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for (int answerChoice = 0; answerChoice < i; answerChoice++) {
                                if (incorrectChoice3.equals(((TextView) findViewById(TILE_BUTTONS[answerChoice])).getText().toString())) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            for (int j = 0; j < incorrectChoice3.length() - 2; j++) {
                                if (incorrectChoice3.substring(j, j + 3).equals("للہ")) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                        }
                        break;
                    default:

                }
            }
        }
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.set(i, "");
        }
        incorrectOnLevel = 0;
        levelBegunTime = System.currentTimeMillis();

    }

    private void respondToWordSelection(int justClickedWord) {

        int t = justClickedWord - 1; //  justClickedWord uses 1 to 4, t uses the array ID (between [0] and [3]
        TextView chosenWord = findViewById(TILE_BUTTONS[t]);
        String chosenWordText = chosenWord.getText().toString();

        if (chosenWordText.equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
            // Good job!

            // report time and number of incorrect guesses
            String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
            Properties info = new Properties().putValue("time", System.currentTimeMillis() - levelBegunTime)
                    .putValue("prior incorrect", incorrectOnLevel)
                    .putValue("grade", studentGrade);
            for (int i = 0; i < 3; i++) {
                if (!incorrectAnswersSelected.get(i).equals("")) {
                    info.putValue("incorrect"+(i+1), incorrectAnswersSelected.get(i));
                }
            }
            Analytics.with(context).track(gameUniqueID, info);

            repeatLocked = false;
            setAdvanceArrowToBlue();

            updatePointsAndTrackers(2);

            for (int w = 0; w < TILE_BUTTONS.length; w++) {
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
            incorrectOnLevel += 1;
            for (int i = 0; i < 3; i++) {
                String item = incorrectAnswersSelected.get(i);
                if (item.equals(chosenWordText)) break;  // this incorrect answer already selected
                if (item.equals("")) {
                    incorrectAnswersSelected.set(i, chosenWordText);
                    break;
                }
            }
            playIncorrectSound();
        }
    }

    public void onWordClick(View view) {
        int wordNo = Integer.parseInt((String) view.getTag());
        respondToWordSelection(wordNo);
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
