package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.*;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

public class Peru extends GameActivity {
    private static final Logger LOGGER = Logger.getLogger(Peru.class.getName());

    protected static final int[] GAME_BUTTONS = {
            R.id.word1, R.id.word2, R.id.word3, R.id.word4
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
    protected void hideInstructionAudioImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.peru);

        ActivityLayouts.applyEdgeToEdge(this, R.id.peruCL);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTL(R.id.peruCL);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        if (challengeLevel == 2) {

            Collections.shuffle(VOWELS);
            Collections.shuffle(CONSONANTS);
            Collections.shuffle(TONES);

        }
        visibleGameButtons = GAME_BUTTONS.length;
        updatePointsAndTrackers(0);
        incorrectAnswersSelected = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            incorrectAnswersSelected.add("");
        }
        playAgain();
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
        parsedRefWordTileArray = Start.tileList.parseWordIntoTiles(refWord.wordInLOP, refWord); // KP
        int tileLength = parsedRefWordTileArray.size();

        // Set thematic colors for four word choice TextViews, also make clickable
        for (int i = 0; i < GAME_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(GAME_BUTTONS[i]);
            String wordColorStr = colorList.get(i);
            int wordColorNo = Color.parseColor(wordColorStr);
            nextWord.setBackgroundColor(wordColorNo);
            nextWord.setTextColor(Color.parseColor("#FFFFFF")); // white
            nextWord.setClickable(true);
        }

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        ImageView wordImage = (ImageView) findViewById(R.id.wordImage);
        wordImage.setClickable(true);

        Random rand = new Random();
        int indexOfCorrectAnswerAmongChoices = rand.nextInt(4);
        List<String> shuffledDistractorTiles = parsedRefWordTileArray.get(0).distractors;
        Collections.shuffle(shuffledDistractorTiles);

        int incorrectLapNo = 0;
        for (int i = 0; i < GAME_BUTTONS.length; i++) {
            TextView nextWord = (TextView) findViewById(GAME_BUTTONS[i]);
            if (i == indexOfCorrectAnswerAmongChoices) {
                nextWord.setText(wordInLOPWithStandardizedSequenceOfCharacters(refWord)); // the correct answer (the unmodified version of the word)
            } else {

                incorrectLapNo++;
                boolean isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates (or dangerous combinations)
                switch (challengeLevel) {
                    case 1:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (THE FIRST TILE) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO
                        while (isDuplicateAnswerChoice) {
                            ArrayList<Start.Tile> tilesInIncorrectChoice = new ArrayList<>(parsedRefWordTileArray);
                            Tile replacementTile = tileHashMap.find(shuffledDistractorTiles.get(incorrectLapNo - 1));
                            replacementTile.typeOfThisTileInstance = parsedRefWordTileArray.get(0).typeOfThisTileInstance;
                            tilesInIncorrectChoice.set(0, replacementTile);
                            String incorrectChoiceString = combineTilesToMakeWord(tilesInIncorrectChoice, refWord, 0);
                            nextWord.setText(incorrectChoiceString);
                            isDuplicateAnswerChoice = false;
                            for (int j = 0; j < incorrectChoiceString.length() - 2; j++) {
                                if (incorrectChoiceString.substring(j, j + 3).equals("للہ")) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }

                        }
                        break;
                    case 2:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS ANY TILE OF THE SAME TYPE (C OR V OR T) FROM THE WHOLE ARRAY

                        //fix: some accidental duplicates
                        while (isDuplicateAnswerChoice) {
                            int randomIndexToReplace = rand.nextInt(tileLength - 1);       // KP // this represents which position in word string will be replaced
                            ArrayList<Tile> tilesInIncorrectChoice = new ArrayList<>(parsedRefWordTileArray);
                            int randomAlternateIndex;
                            if (VOWELS.contains(tilesInIncorrectChoice.get(randomIndexToReplace))) {
                                randomAlternateIndex = rand.nextInt(VOWELS.size());       // KP // this represents which game tile will overwrite some part of the correct wor
                                tilesInIncorrectChoice.set(randomIndexToReplace, VOWELS.get(randomAlternateIndex)); // JP
                            } else if (CONSONANTS.contains(tilesInIncorrectChoice.get(randomIndexToReplace))) {
                                randomAlternateIndex = rand.nextInt(CONSONANTS.size());
                                tilesInIncorrectChoice.set(randomIndexToReplace, CONSONANTS.get(randomAlternateIndex)); // JP
                            } else if (TONES.contains(tilesInIncorrectChoice.get(randomIndexToReplace))) {
                                randomAlternateIndex = rand.nextInt(TONES.size());
                                tilesInIncorrectChoice.set(randomIndexToReplace, TONES.get(randomAlternateIndex)); // JP
                            } else if (ADs.contains(tilesInIncorrectChoice.get(randomIndexToReplace))) {
                                randomAlternateIndex = rand.nextInt(ADs.size());
                                tilesInIncorrectChoice.set(randomIndexToReplace, ADs.get(randomAlternateIndex));
                            }

                            String incorrectChoice2 = combineTilesToMakeWord(tilesInIncorrectChoice, refWord, randomIndexToReplace);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for (int answerChoice = 0; answerChoice < i; answerChoice++) {
                                if (incorrectChoice2.equals(((TextView) findViewById(GAME_BUTTONS[answerChoice])).getText().toString())) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            if (incorrectChoice2.equals(wordInLOPWithStandardizedSequenceOfCharacters(refWord))) {
                                isDuplicateAnswerChoice = true;
                            }
                            for (int j = 0; j < incorrectChoice2.length() - 2; j++) {
                                if (incorrectChoice2.substring(j, j + 3).equals("للہ")) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            if (!isDuplicateAnswerChoice) {
                                nextWord.setText(incorrectChoice2);
                            }
                        }
                        break;

                    case 3:
                        // THE WRONG ANSWERS ARE LIKE THE RIGHT ANSWER EXCEPT HAVE ONLY ONE TILE (RANDOM POS IN SEQ) REPLACED
                        // REPLACEMENT IS FROM DISTRACTOR TRIO

                        isDuplicateAnswerChoice = true; // LM // generate answer choices until there are no duplicates

                        while (isDuplicateAnswerChoice) {
                            int randomIndexToReplace = rand.nextInt(tileLength - 1);       // this represents which position in word string will be replaced
                            ArrayList<Tile> tilesInIncorrectChoice = new ArrayList<>(parsedRefWordTileArray);
                            Tile incorrectTile = Start.tileList.returnRandomDistractorTile(parsedRefWordTileArray.get(randomIndexToReplace));
                            incorrectTile.typeOfThisTileInstance = parsedRefWordTileArray.get(randomIndexToReplace).typeOfThisTileInstance;
                            tilesInIncorrectChoice.set(randomIndexToReplace, incorrectTile);
                            String incorrectChoice3 = combineTilesToMakeWord(tilesInIncorrectChoice, refWord, randomIndexToReplace);

                            isDuplicateAnswerChoice = false; // LM // resets to true and keeps looping if a duplicate has been made:
                            for (int answerChoice = 0; answerChoice < i; answerChoice++) {
                                if (incorrectChoice3.equals(((TextView) findViewById(GAME_BUTTONS[answerChoice])).getText().toString())) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            for (int j = 0; j < incorrectChoice3.length() - 2; j++) {
                                if (incorrectChoice3.substring(j, j + 3).equals("للہ")) {
                                    isDuplicateAnswerChoice = true;
                                }
                            }
                            if(!isDuplicateAnswerChoice) {
                                nextWord.setText(incorrectChoice3);
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
        TextView chosenWord = findViewById(GAME_BUTTONS[t]);
        String chosenWordText = chosenWord.getText().toString();

        if (chosenWordText.equals(wordInLOPWithStandardizedSequenceOfCharacters(refWord))) {
            // Good job!

            if (sendAnalytics) {
                // report time and number of incorrect guesses
                String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + gameMode;
                Properties info = new Properties().putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", chosenWordText)
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < 3; i++) {
                    if (!incorrectAnswersSelected.get(i).equals("")) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }

            repeatLocked = false;
            setAdvanceArrowToBlue();

            updatePointsAndTrackers(2);

            for (int w = 0; w < GAME_BUTTONS.length; w++) {
                TextView nextWord = findViewById(GAME_BUTTONS[w]);
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
