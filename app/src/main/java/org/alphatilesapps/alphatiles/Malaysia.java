package org.alphatilesapps.alphatiles;
//adding an edit to shelve
import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;

import android.util.Log;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.*; //one new

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Malaysia extends GameActivity {
    List<List<Start.Word>> wordPagesLists = new ArrayList<>();
    List<Start.Word> wordPage = new ArrayList<>();
    int numPages = 0;
    int currentPageNumber = 0;
    final int wordsPerPage = 11;

    protected static final int[] GAME_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04,
            R.id.word05, R.id.word06, R.id.word07, R.id.word08,
            R.id.word09, R.id.word10, R.id.word11,
            //R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04,
            //R.id.wordImage05, R.id.wordImage06, R.id.wordImage07, R.id.wordImage08,
            //R.id.wordImage09, R.id.wordImage10, R.id.wordImage11
    };

    protected static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04,
            R.id.wordImage05, R.id.wordImage06, R.id.wordImage07, R.id.wordImage08,
            R.id.wordImage09, R.id.wordImage10, R.id.wordImage11
    };
    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }
    @Override
    protected int[] getWordImages() { return WORD_IMAGES; }

    private static final Logger LOGGER = Logger.getLogger(Malaysia.class.getName() );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        int gameID = R.id.malaysiaCL;
        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");
        determineNumPages(); // JP
        setContentView(R.layout.malaysia);
        assignPages();
        displayWords(0);

        if (scriptDirection.equals("RTL")) fixConstraintsRTLMalaysia(gameID);
        if (getAudioInstructionsResID() == 0) centerGamesHomeImage();
        showOrHideScrollingArrows();
        setAllGameButtonsClickable();
    }

    public void determineNumPages() {
        wordPagesLists.add(wordPage);
        int total = wordList.size() - wordsPerPage; // 1 page is accounted for in numPages init
        while (total >= 0) {
            numPages++;
            List<Start.Word> page = new ArrayList<Start.Word>();
            wordPagesLists.add(page);
            total -= wordsPerPage;
        }
        LOGGER.info("(NumPages = " + numPages + ")");
    }

    public void assignPages() {
        int numWords = wordList.size();
        int wordIndex = 0;
        for (int i = 0; i <= numPages; i++)
            for (int j = 0; j < wordsPerPage; j++)
                if(wordIndex < numWords) {
                    wordPagesLists.get(i).add(wordList.get(wordIndex));
                    wordIndex++;
                }
    }

    public void displayWords(int page){
        visibleGameButtons = wordPagesLists.get(page).size();
        for(int i = 0; i < visibleGameButtons; i++){
            TextView word = findViewById(GAME_BUTTONS[i]);
            word.setText(wordInLOPWithStandardizedSequenceOfCharacters(wordPagesLists.get(page).get(i)));
            String color = ""+(i%11+2==5?1:i%11+2);
            String typeColor = colorList.get(Integer.parseInt(color));
            int wordColor = Color.parseColor(typeColor);
            word.setBackgroundColor(wordColor);
        }
        for(int k = 0; k < wordsPerPage; k++){
            TextView key = findViewById(GAME_BUTTONS[k]);
            if(k < visibleGameButtons){
                key.setVisibility(View.VISIBLE);
                //if (hasWordAudio)
                key.setClickable(true);
                //else key.setClickable(false);
            } else {
                key.setVisibility(View.INVISIBLE);
                key.setClickable(false);
            }
        }
        //add in two loops here to make the images load in.
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
        // TO DO: TEST THIS WITH A LANGUAGE THAT DOESN'T HAVE INSTRUCTION AUDIO, CONNECT BACK ARROW
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.malaysiaCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        //constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        //constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);
    }

    private void fixConstraintsRTLMalaysia(int gameID) {
        if (scriptDirection.equals("LTR")) return;

        findViewById(R.id.forwardArrowImage).setRotationY(180);
        findViewById(R.id.backwardArrowImage).setRotationY(180);

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.repeatImage2, ConstraintSet.END, R.id.gamesHomeImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.START, R.id.repeatImage2, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.instructions, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.instructions, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.applyTo(constraintLayout);
    }

    public void nextPageArrow(View view) {
        if (currentPageNumber >= numPages) {
            currentPageNumber = numPages - 1;
            return;
        }
        currentPageNumber++;
        displayWords(currentPageNumber);
        showOrHideScrollingArrows();
    }

    public void prevPageArrow(View view) {
        if (currentPageNumber <= 0) {
            currentPageNumber = 0;
            return;
        }
        currentPageNumber--;
        displayWords(currentPageNumber);
        showOrHideScrollingArrows();
    }

    public void showOrHideScrollingArrows() {
        findViewById(R.id.forwardArrowImage).setVisibility(currentPageNumber >= numPages ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.backwardArrowImage).setVisibility(currentPageNumber <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    public void onWordClick(View view) {
        setAllGameButtonsUnclickable(); //remove these lines to enable faster listening
        setAllGameImagesClickable(false);

        Start.Word thisWord = wordPagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
        int audioId = wordAudioIDs.get(thisWord.wordInLWC);
        int duration = thisWord.duration;
        gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllGameButtonsClickable(); //remove these lines to enable faster listening
                    setAllGameImagesClickable(true);
                }
            }
        }, duration);
    }

    public void clickPicHearAudio(View view) {
        onWordClick(view);
        //using super here is not possible because it is hard wired to alter repeatImage which is disabled here.
    }

    protected void setAllGameImageClickable(){ setAllGameImagesClickable(true); }
    protected void setAllGameImagesClickable(boolean status) {
        for (int t = 0; t < WORD_IMAGES.length; t++) {
            ImageView imageView = findViewById(WORD_IMAGES[t]);
            if(status) imageView.setClickable(true);
            else imageView.setClickable(false);
        }
    }
}
