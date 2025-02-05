package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileListNoSAD;

import org.alphatilesapps.alphatiles.Start.WordList;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.colorList;

public class Italy extends GameActivity {
    Start.TileList sortableTilesArray;
    Start.SyllableList sortableSyllArray;
    WordList gameCards = new WordList();
    final int CARDS_ON_BOARD = 16;
    boolean[] boardCardsFound = new boolean[CARDS_ON_BOARD];
    int deckIndex = 0;
    final String ITALY_DECK_SIZE = "Italy Deck Size";
    int deckSize = 54;  // 54 is the default size for the Mexican loteria game
    private static final Logger LOGGER = Logger.getLogger(Italy.class.getName());

    protected static final int[] GAME_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04, R.id.choice05, R.id.choice06,
            R.id.choice07, R.id.choice08, R.id.choice09, R.id.choice10, R.id.choice11, R.id.choice12,
            R.id.choice13, R.id.choice14, R.id.choice15, R.id.choice16
    };

    protected static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04, R.id.wordImage05, R.id.wordImage06,
            R.id.wordImage07, R.id.wordImage08, R.id.wordImage09, R.id.wordImage10, R.id.wordImage11, R.id.wordImage12,
            R.id.wordImage13, R.id.wordImage14, R.id.wordImage15, R.id.wordImage16
    };

    protected static final int[][] LOTERIA_SEQUENCES = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}, {1, 5, 9, 13}, {2, 6, 10, 14}, {3, 7, 11, 15}, {4, 8, 12, 16}, {1, 6, 11, 16}, {4, 7, 10, 13}};

    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1)
                    .instructionAudioName, "raw", context.getPackageName());
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

        setContentView(R.layout.italy);
        int gameID = R.id.italyCL;

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            ImageView playNextWordImage = (ImageView) findViewById(R.id.playNextWord);
            ImageView referenceItemImage = (ImageView) findViewById(R.id.referenceItem);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            playNextWordImage.setRotationY(180);
            referenceItemImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        if (syllableGame.equals("S")) {
            sortableSyllArray = (Start.SyllableList) syllableList.clone();
            Collections.shuffle(sortableSyllArray);
        } else {
            sortableTilesArray = (Start.TileList) tileListNoSAD.clone();
            Collections.shuffle(sortableTilesArray);
        }

        // override default deck size setting, if configured
        final String deckSizeSetting = Start.settingsList.find(ITALY_DECK_SIZE);
        if (! deckSizeSetting.isEmpty()) {
            deckSize = Integer.parseInt(deckSizeSetting);
            // don't go below CARDS_ON_BOARD
            if (deckSize < CARDS_ON_BOARD) {
                deckSize = CARDS_ON_BOARD;
            }
        }
        // else stay with the default deck size (54)

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        updatePointsAndTrackers(0);
        playAgain();
    }
    @Override
    public void setAllGameButtonsUnclickable() {
        super.setAllGameButtonsUnclickable();

        ImageView nextWordArrow = findViewById(R.id.playNextWord);
        nextWordArrow.setImageResource(R.drawable.zz_forward_inactive);
        nextWordArrow.setClickable(false);

        ImageView referenceItem = findViewById(R.id.referenceItem);
        referenceItem.setClickable(false);

        for (int t = 0; t < visibleGameButtons; t++) {
            ImageView wordImage = findViewById(WORD_IMAGES[t]);
            wordImage.setClickable(false);
        }
    }

    @Override
    public void setAllGameButtonsClickable() {
        super.setAllGameButtonsClickable();

        ImageView nextWordArrow = findViewById(R.id.playNextWord);
        nextWordArrow.setImageResource(R.drawable.zz_forward_green);
        nextWordArrow.setClickable(true);

        ImageView referenceItem = findViewById(R.id.referenceItem);
        referenceItem.setClickable(true);

        for (int t = 0; t < visibleGameButtons; t++) {
            ImageView wordImage = findViewById(WORD_IMAGES[t]);
            wordImage.setClickable(true);
        }
    }


    public void goBackToEarth(View view) {

        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view) {

        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void onRefClick(View view) {

        playActiveWordClip(false);

    }

    public void playAgain() {
        repeatLocked = true;
        setAdvanceArrowToGray();
        deckIndex = -1;
        gameCards.removeAll(gameCards);
        Arrays.fill(boardCardsFound, false);

        WordList wordListShuffle = cumulativeStageBasedWordList;
        Collections.shuffle(wordListShuffle);

        // Quit (go back to the home screen) if we don't have enough words
        if (wordListShuffle.size() < deckSize) {
            LOGGER.warning("playAgain: can't proceed - need at least "
                    + deckSize + " words");
            // return to the home screen
            goBackToEarth(null);
            return;
        }

        // Add a subset of the shuffled cards to gameCards
        for (int cardNumber = 0; cardNumber < deckSize; cardNumber++) {
            gameCards.add(wordListShuffle.get(cardNumber));
        }

        // Add 16 (CARDS_ON_BOARD) of the gameCards to the board
        WordList boardCards = new WordList();
        for (int tileNumber = 0; tileNumber < CARDS_ON_BOARD; tileNumber++) {
            boardCards.add(gameCards.get(tileNumber));
            TextView thisCardText = (TextView) findViewById(GAME_BUTTONS[tileNumber]);
            thisCardText.setText(wordList.stripInstructionCharacters(gameCards.get(tileNumber).wordInLOP));
            String tileColorStr = colorList.get(tileNumber % 5);
            int tileColor = Color.parseColor(tileColorStr);
            thisCardText.setTextColor(tileColor); // resets as in previous round some text fields set to black
            ImageView thisCardImage = (ImageView) findViewById(WORD_IMAGES[tileNumber]);
            int resID = getResources().getIdentifier(gameCards.get(tileNumber).wordInLWC +"2", "drawable", getPackageName());
            thisCardImage.setImageResource(0);
            thisCardImage.setImageResource(resID);
        }

        // Shuffle the gameCards again; they will be "called" in order from here on out
        Collections.shuffle(gameCards);

        // Display the first word
        nextWordFromGameSet();

    }

    public void getAnotherWord(View view) {

        nextWordFromGameSet();
    }

    public void nextWordFromGameSet() {
        deckIndex++;
        if (deckIndex == deckSize) {

            // The player went through all the cards without getting a loteria. Set up a new board
            playIncorrectSound();
            playIncorrectSound();
            playAgain();
        } else { // "Call out" the next word

            refWord = gameCards.get(deckIndex);
            playActiveWordClip(false);
        }
    }

    public void onSelection(View view) {
        respondToSelection(Integer.parseInt((String) view.getTag()));
    }

    public void respondToSelection(int indexOfTileJustSelected) {

        TextView tileJustSelected = findViewById(GAME_BUTTONS[indexOfTileJustSelected - 1]);

        if (tileJustSelected.getText().equals(wordList.stripInstructionCharacters(refWord.wordInLOP))) {
            respondToCorrectSelection(indexOfTileJustSelected);
        } else {
            respondToIncorrectSelection();
        }

    }

    public void respondToCorrectSelection(int indexOfTileJustSelected) {
        boardCardsFound[indexOfTileJustSelected - 1] = true;

        ImageView imageJustSelected = findViewById(WORD_IMAGES[indexOfTileJustSelected - 1]);
        imageJustSelected.setImageResource(R.drawable.zz_bean);

        TextView thisCardText = (TextView) findViewById(GAME_BUTTONS[indexOfTileJustSelected - 1]);
        thisCardText.setTextColor(Color.BLACK);

        if (loteria()) {
            respondToLoteria();
        } else {
            // Play sounds, then advance to the next word
            playCorrectSoundThenActiveWordClip(false);
            nextWordFromGameSet();
        }

    }

    public void respondToIncorrectSelection() {

        playIncorrectSound();
    }


    public boolean loteria() {
        // For each sequence in possibleLoteriaSequences[][], check if all the indeces inside have been marked as correctly selected

        for (int[] sequence : LOTERIA_SEQUENCES) {
            boolean thisSequence = true; // is this sequence the loteria? true until a non-bean is found
            for (int i = 0; i < sequence.length; i++)
                thisSequence &= boardCardsFound[sequence[i] - 1];
            if (thisSequence) {
                for (int i = 0; i < sequence.length; i++) {
                    ImageView bean = findViewById(WORD_IMAGES[sequence[i] - 1]);
                    bean.setImageResource(R.drawable.zz_bean_loteria);
                }
                return true;
            }
        }

        return false;
    }

    public void respondToLoteria() {
        setAdvanceArrowToBlue();
        playCorrectSoundThenActiveWordClip(true);
        updatePointsAndTrackers(4);

        // TODO: Draw a thin/transparent line across the loteria?
    }
}
