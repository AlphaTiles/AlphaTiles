package org.alphatilesapps.alphatiles;
//adding an edit to shelve
import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;

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

public class Malaysia extends GameActivity {
    List<List<Start.Word>> wordPagesLists = new ArrayList<>();
    List<Start.Word> wordPage = new ArrayList<>();
    int numPages = 0;
    int currentPageNumber = 0;
    boolean colorless = false;
    //final int wordsPerPage = 11;

    protected static final int[] GAME_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04,
            R.id.word05, R.id.word06, R.id.word07, R.id.word08,
            R.id.word09, R.id.word10, R.id.word11,
    };
    final int wordsPerPage = GAME_BUTTONS.length;

    protected static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04,
            R.id.wordImage05, R.id.wordImage06, R.id.wordImage07, R.id.wordImage08,
            R.id.wordImage09, R.id.wordImage10, R.id.wordImage11
    };
    //maybe have some check here to see if (GAME_BUTTONS.length!=WORD_IMAGES.length)?
    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }
    @Override
    protected int[] getWordImages() { return WORD_IMAGES; }

    //private static final Logger LOGGER = Logger.getLogger(Malaysia.class.getName() );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        int gameID = R.id.malaysiaCL;
        determineNumPages();
        setContentView(R.layout.malaysia);

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        assignPages();
        displayWords(0);

        if (scriptDirection.equals("RTL")) fixConstraintsRTLMalaysia(gameID);
        if (getAudioInstructionsResID() == 0) hideInstructionAudioImage();
        showOrHideScrollingArrows();
        setAllImagesBlank();
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
    public void setAllImagesBlank(){
        for(int i = 0; i < WORD_IMAGES.length; i++){
            ImageView image = findViewById(WORD_IMAGES[i]);
            image.setImageDrawable(null);
        }
    }

    public void displayWords(int page){
        visibleGameButtons = wordPagesLists.get(page).size();
        if(page >= numPages - 1)
            for(int i = visibleGameButtons; i < wordsPerPage; i++)
                findViewById(WORD_IMAGES[i]).setBackgroundResource(0);
                //could put this in the bigger loop but just in case the optimizer misses this (it takes maybe 0.1s longer per page)

        for(int i = 0; i < visibleGameButtons; i++){
            TextView word = findViewById(GAME_BUTTONS[i]);
            word.setText(wordInLOPWithStandardizedSequenceOfCharacters(wordPagesLists.get(page).get(i)));
            //String color = "" + (i<5?i:i>5?10-i:7); //this theme works well with the Me'phaa colors, but might not for all types. Is this ok?
            //if(colorless) color = "8";
            //String typeColor = colorList.get(Integer.parseInt(color));
            int color = i<5?i:i>5?10-i:7;
            //String typeColor = colorList.get(colorless?8:color);
            int wordColor = Color.parseColor(colorList.get(colorless?8:color));//typeColor);
            word.setBackgroundColor(wordColor);
            findViewById(WORD_IMAGES[i]).setBackgroundResource(getResources().getIdentifier(wordPagesLists.get(page).get(i).wordInLWC, "drawable", getPackageName())); //thailand 365
        }
        for(int i = 0; i < wordsPerPage; i++){
            TextView key = findViewById(GAME_BUTTONS[i]);
            if(i < visibleGameButtons){
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
        //LOGGER.info("JIRANIMO 1");
        for(int i = 0; i < wordsPerPage; i++) {
            //findViewById(WORD_IMAGES[i]).setBackgroundResource(getResources().getIdentifier(wordPagesLists.get(page).get(i).wordInLWC, "drawable", getPackageName())); //thailand 365
            //LOGGER.info("JIRANIMO 4");
            //image.setImageDrawable(null);
        }
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

    private void fixConstraintsRTLMalaysia(int gameID) {
        if (scriptDirection.equals("LTR")) return;

        findViewById(R.id.forwardArrowImage).setRotationY(180);
        findViewById(R.id.backwardArrowImage).setRotationY(180);

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.backwardArrowImage, ConstraintSet.END, R.id.gamesHomeImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.START, R.id.backwardArrowImage, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.instructions, ConstraintSet.START, 0);
        constraintSet.connect(R.id.forwardArrowImage, ConstraintSet.START, R.id.instructions, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.END, R.id.forwardArrowImage, ConstraintSet.START, 0);
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
        setAllGameImagesClickable(false); //

        Start.Word thisWord = wordPagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
        int audioId = wordAudioIDs.get(thisWord.wordInLWC);
        int duration = thisWord.duration;
        gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);

        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllGameButtonsClickable(); //remove these lines to enable faster listening
                    setAllGameImagesClickable(true); //
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
