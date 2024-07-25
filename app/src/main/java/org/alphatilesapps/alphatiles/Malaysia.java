package org.alphatilesapps.alphatiles;
//adding an edit to shelve
import static org.alphatilesapps.alphatiles.Start.SAD;
import static org.alphatilesapps.alphatiles.Start.SILENT_PLACEHOLDER_CONSONANTS;
import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.hasSyllableAudio;
import static org.alphatilesapps.alphatiles.Start.syllableAudioIDs;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;
import static org.alphatilesapps.alphatiles.Start.tileDurations;

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

public class Malaysia extends GameActivity {
    // will contain, for example:
    // List page 1 (which will contain <= 35 tiles or syllables)
    // List page 2, etc.
    //List<List<Start.Tile>> tilePagesLists = new ArrayList<>();
    //List<List<Start.Syllable>> syllablePagesLists = new ArrayList<>();
    List<List<Start.Word>> wordPagesLists = new ArrayList<>();
    //List<Start.Tile> tilePage = new ArrayList<>();
    //List<Start.Syllable> syllablePage = new ArrayList<>();

    List<Start.Word> wordPage = new ArrayList<>();
    int numPages = 0;
    int currentPageNumber = 0;

    final int wordsPerPage = 11;
    //final int syllablesPerPage = 35;
    //final int tilesPerPage = 63;
    protected static final int[] GAME_BUTTONS = {
            R.id.word01, R.id.word02, R.id.word03, R.id.word04, R.id.word05, R.id.word06,
            R.id.word07, R.id.word08, R.id.word09, R.id.word10, R.id.word11
    };
    //Would it be better to make all these ids into an array?!?!
    //repeated code bad?

    protected static final int[] WORD_IMAGES = {
            R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04, R.id.wordImage05,
            R.id.wordImage06, R.id.wordImage07, R.id.wordImage08, R.id.wordImage09, R.id.wordImage10,
            R.id.wordImage11
    };
    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return WORD_IMAGES;
    } //CHANGE THIS?!?!

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
//        if (syllableGame.equals("S")) {
//            gameID = R.id.sudansyllCL;
//        } else if (syllableGame.equals("W")) { //FIGURE OUT WHERE TO CHANGE THIS TO ADD A W!!
//            gameID = R.id.sudanwordCL;
//        } else {
//            gameID = R.id.sudanCL;
//        }
        //gameID = R.id.malaysia

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
        setContentView(R.layout.malaysia);//R.id.MalaysiaCL);
        context = this;
        int gameID = 0;
        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");
        determineNumPages(); // JP

//        if (syllableGame.equals("S")) {
//            setContentView(R.layout.sudan_syll);
//            gameID = R.id.sudansyllCL;
//            splitSyllablesListAcrossPages();
//            showCorrectNumSyllables(0);
//        } else {
//            setContentView(R.layout.sudan);
//            gameID = R.id.sudanCL;
//            splitTileListAcrossPages();
//            showCorrectNumTiles(0);
//        }
        //setContentView(R.layout.malaysia);
        gameID = R.id.malaysiaCL;
        splitWordListAcrossPages();
        showCorrectNumWords(0);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            ImageView repeatImage2 = (ImageView) findViewById(R.id.repeatImage2);
            repeatImage2.setRotationY(180);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTLMalaysia(gameID);
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }
        showOrHideScrollingArrows();
    }

    private void fixConstraintsRTLMalaysia(int gameID) {
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

    public void determineNumPages() {
        wordPagesLists.add(wordPage);
        int total = wordList.size() - wordsPerPage; // 1 page is accounted for in numPages init
        while (total >= 0) {
            numPages++;
            List<Start.Word> page = new ArrayList<Start.Word>();
            wordPagesLists.add(page); //add another page(list of syllables) to list
            total = total - wordsPerPage;//syllablesPerPage;
        }
    }

    public void splitWordListAcrossPages() {
        //int numTiles = cumulativeStageBasedTileList.size() - SAD.size() - SILENT_PLACEHOLDER_CONSONANTS.size();
        //int tileIndex = 0;
        int numWords = cumulativeStageBasedWordList.size();//234; //FIX THIS!!!
        int wordIndex = 0;
        for (int i = 0; i <= numPages; i++) {
            for (int j = 0; j < wordsPerPage; j++) {
                if(wordIndex < numWords) {
                    Start.Word thisWord = cumulativeStageBasedWordList.get(wordIndex);
                    //if (!SAD.contains(thisWord) && !SILENT_PLACEHOLDER_CONSONANTS.contains(thisTile)) {
                        wordPagesLists.get(i).add(thisWord);
                    //}
                    wordIndex++;
                }
            }
        }
    }


//    public void splitSyllablesListAcrossPages() {
//        int numSyllables = syllableList.size();
//        int syllableIndex = 0;
//        for (int i = 0; i < numPages + 1; i++) {
//            for (int j = 0; j < syllablesPerPage; j++) {
//                if (syllableIndex < numSyllables) {
//                    syllablePagesLists.get(i).add(syllableList.get(syllableIndex));
//                }
//                syllableIndex++;
//            }
//        }
//    }

//    public void showCorrectNumTiles(int page) {
//
//        visibleGameButtons = wordPagesLists.get(page).size();
//
//        for (int k = 0; k < visibleGameButtons; k++) {
//            TextView wordView = findViewById(GAME_BUTTONS[k]);
//            wordView.setText(wordPagesLists.get(page).get(k).text);
//            String type = wordPagesLists.get(page).get(k).typeOfThisTileInstance;
//            String typeColor;
//            switch (type) {
//                case "C":
//                    typeColor = colorList.get(1);
//                    break;
//                case "V":
//                    typeColor = colorList.get(2);
//                    break;
//                case "T":
//                    typeColor = colorList.get(3);
//                    break;
//                default:
//                    typeColor = colorList.get(4);
//                    break;
//            }
//            int tileColor = Color.parseColor(typeColor);
//            tileView.setBackgroundColor(tileColor);
//        }
//
//        for (int k = 0; k < tilesPerPage; k++) {
//
//            TextView key = findViewById(GAME_BUTTONS[k]);
//            if (k < visibleGameButtons) {
//                key.setVisibility(View.VISIBLE);
//                key.setClickable(true);
//            } else {
//                key.setVisibility(View.INVISIBLE);
//                key.setClickable(false);
//            }
//        }
//    }
//
//    public void showCorrectNumSyllables(int page) {
//        visibleGameButtons = syllablePagesLists.get(page).size();
//
//        for (int i = 0; i < visibleGameButtons; i++) {
//            TextView tile = findViewById(GAME_BUTTONS[i]);
//            tile.setText(syllablePagesLists.get(page).get(i).text);
//            String color = syllablePagesLists.get(page).get(i).color;
//            String typeColor = colorList.get(Integer.parseInt(color));
//            int tileColor = Color.parseColor(typeColor);
//            tile.setBackgroundColor(tileColor);
//        }
//
//        for (int k = 0; k < syllablesPerPage; k++) {
//            TextView key = findViewById(GAME_BUTTONS[k]);
//            if (k < visibleGameButtons) {
//                key.setVisibility(View.VISIBLE);
//                if (hasSyllableAudio) {
//                    key.setClickable(true);
//                } else {
//                    key.setClickable(false);
//                }
//            } else {
//                key.setVisibility(View.INVISIBLE);
//                key.setClickable(false);
//            }
//        }
//    }

    public void showCorrectNumWords(int page){
        visibleGameButtons = wordPagesLists.get(page).size();
        for(int i = 0; i < visibleGameButtons; i++){
            TextView word = findViewById(GAME_BUTTONS[i]);
            //word.setText(wordPagesLists.get(page).get(i).wordInLWC); //IS THIS THE RIGHT PART? (wordInLWC)
            //Log.d("Malaysia", word.toString());
            if(word != null)  word.setText(wordInLOPWithStandardizedSequenceOfCharacters(wordPagesLists.get(page).get(i)));
            else Log.e("Malaysia", "Word is still null");
            String color = "0";//wordPagesLists.get(page).get(i). //FIX THIS!
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

    }

    public void nextPageArrow(View view) {
        if (currentPageNumber < numPages) {
            currentPageNumber++;
//            if (syllableGame.equals("S")) {
//                showCorrectNumSyllables(currentPageNumber);
//            } else {
//                showCorrectNumTiles(currentPageNumber);
//            }
            showCorrectNumWords(currentPageNumber);
            showOrHideScrollingArrows();
        }
    }

    public void prevPageArrow(View view) {
        if (currentPageNumber > 0) {
            currentPageNumber--;
//            if (syllableGame.equals("S")) {
//                showCorrectNumSyllables(currentPageNumber);
//            } else {
//                showCorrectNumTiles(currentPageNumber);
//            }
            showCorrectNumWords(currentPageNumber);
            showOrHideScrollingArrows();
        }
    }

    public void showOrHideScrollingArrows() {
        ImageView nextPageArrow = findViewById(R.id.repeatImage);
        ImageView prevPageArrow = findViewById(R.id.repeatImage2);
        if (currentPageNumber == numPages) {
            nextPageArrow.setVisibility(View.INVISIBLE);
        } else {
            nextPageArrow.setVisibility(View.VISIBLE);
        }
        if(currentPageNumber == 0) {
            prevPageArrow.setVisibility(View.INVISIBLE);
        } else {
            prevPageArrow.setVisibility(View.VISIBLE);
        }
    }

    public void onBtnClick(View view) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        int audioId;
        int duration;
//        if (syllableGame.equals("S")) {
//            Start.Syllable thisSyllable = syllablePagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
//            audioId = syllableAudioIDs.get(thisSyllable.audioName);
//            duration = thisSyllable.duration;
//
//        } else {
//            Start.Tile thisTile = tilePagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
//            audioId = tileAudioIDs.get(thisTile.audioForThisTileType);
//            duration = tileDurations.get(thisTile.audioForThisTileType);
//        }
        Start.Word thisWord = wordPagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1); //This casting makes me antsy
        audioId = wordAudioIDs.get(thisWord); //FIX THIS!!! //wait... how is it allowing a word object where an integer goes?
        duration = thisWord.duration;

        gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllGameButtonsClickable();
                }
                setOptionsRowClickable();
            }

        }, duration);
    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    @Override
    public void onBackPressed() {
        // no action
        super.onBackPressed();
    }
}
