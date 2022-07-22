package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Japan extends GameActivity {

    /*JP:
    1. choose a word with <= 7 tiles
    2. display the word up top
    3. parse the word into tiles and fill in the layout tiles
    4. parse the word into syllables, and every time the user clicks a button, check if the adjacent
    two tiles together form one of the syllables ?
        - alternative approach: calculate which buttons should be pressed by parsing the word into syllables,
        determining how many tiles are in the first syllable, in the second, etc. and choosing the buttons that way

     */

    // TO DO:
    // add onClick for word up top to play audio
    // make a reset layout function for repeatGame() before play()

    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    ArrayList<String> parsedWordIntoTiles;
    ArrayList<String> parsedWordIntoSyllables;
    ArrayList<TextView> joinedTracker = new ArrayList<>();
    ArrayList<TextView> originalLayout = new ArrayList<>();
    HashMap<String, ArrayList<TextView>> inProgSyllabification = new HashMap<>();
    int visibleViews = 0;
    int visibleViewsImm = 0;
    int MAX_TILES = 12;
    ArrayList<String> correctSyllabification = new ArrayList<>();

    protected static final int[] TILES_AND_BUTTONS = {
            R.id.tile01, R.id.button1, R.id.tile02, R.id.button2, R.id.tile03, R.id.button3,
            R.id.tile04, R.id.button4, R.id.tile05, R.id.button5, R.id.tile06, R.id.button6,
            R.id.tile07, R.id.button7, R.id.tile08, R.id.button8, R.id.tile09, R.id.button9,
            R.id.tile10, R.id.button10, R.id.tile11, R.id.button11, R.id.tile12
    };

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336", "#FFFF00", "#E91E63"};


    @Override
    protected int[] getTileButtons() {
        return TILES_AND_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
//          audioInstructionsResID = res.getIdentifier("colombia_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());

        }
        catch (NullPointerException e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        // TO DO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.japan);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);     // forces landscape mode only

        for (int i = 0; i < TILES_AND_BUTTONS.length; i++){
            joinedTracker.add(findViewById(TILES_AND_BUTTONS[i]));
            originalLayout.add(findViewById(TILES_AND_BUTTONS[i]));
        }

        play();
    }

    private void play() {
        chooseWord();
        displayWordRef();
        displayTileChoices();
        setVisButtonsClickable();
        setTilesUnclickable();
    }

    private void chooseWord(){

        boolean freshWord = false;

        while(!freshWord) {
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size());

            wordInLWC = Start.wordList.get(randomNum).nationalWord;
            wordInLOP = Start.wordList.get(randomNum).localWord;

            parsedWordIntoTiles = tileList.parseWordIntoTiles(wordInLOP);
            parsedWordIntoSyllables = syllableList.parseWordIntoSyllables(wordInLOP);

            /*
            for (String syll : parsedWordIntoSyllables){
                correctSyllabification.add(syll);
                correctSyllabification.add("*");
            }
            // other option -- every time you change joinedTracker, convert it into a string and
            check if that string is equal to wordInLOP with the periods in it still
             */
            if (parsedWordIntoTiles.size() <= MAX_TILES){ //JP: choose word w/ <= 12 tiles
                //If this word isn't one of the 3 previously tested words, we're good // LM
                if(wordInLWC.compareTo(lastWord)!=0
                        && wordInLWC.compareTo(secondToLastWord)!=0
                        && wordInLWC.compareTo(thirdToLastWord)!=0){
                    freshWord = true;
                    thirdToLastWord = secondToLastWord;
                    secondToLastWord = lastWord;
                    lastWord = wordInLWC;
                }
            }

        }//generates a new word if it got one of the last three tested words // LM
    }

    private void displayWordRef(){
        TextView ref = findViewById(R.id.word);
        ref.setText(wordList.stripInstructionCharacters(wordInLOP));
    }

    private void displayTileChoices(){
        for (int i = 0; i < parsedWordIntoTiles.size(); i = i +2){
            String tileColorStr = COLORS[i % 5];
            int tileColor = Color.parseColor(tileColorStr);
            TextView tile = findViewById(TILES_AND_BUTTONS[i]);
            tile.setText(parsedWordIntoTiles.get(i));
            tile.setClickable(false);
            tile.setVisibility(View.VISIBLE);
            tile.setBackgroundColor(tileColor);
            tile.setTextColor(Color.parseColor("#FFFFFF")); // white;
        }
        for (int i = parsedWordIntoTiles.size(); i < TILES_AND_BUTTONS.length; i = i + 2){
            TextView tile = findViewById(TILES_AND_BUTTONS[i]);
            tile.setClickable(false);
            tile.setVisibility(View.INVISIBLE);
        }
        visibleViews = parsedWordIntoTiles.size()*2 - 1; // accounts for both buttons and tiles
        visibleViewsImm = parsedWordIntoTiles.size()*2 - 1;
    }

    private void setVisButtonsClickable(){
        for (int i = 1; i < parsedWordIntoTiles.size() - 1; i = i + 2){
            TextView button = findViewById(TILES_AND_BUTTONS[i]);
            button.setClickable(true);
        }
    }

    private void setAllButtonsUnclickable(){
        for (int i = 1; i < MAX_TILES - 1; i = i + 2){
            TextView button = findViewById(TILES_AND_BUTTONS[i]);
            button.setClickable(false);
        }
    }

    private void setTilesUnclickable(){
        for (int i = 0; i < TILES_AND_BUTTONS.length; i = i + 2){
            TextView tile = findViewById(TILES_AND_BUTTONS[i]);
            tile.setClickable(false);
        }
    }

    public void onClickJapan(View view) {
        joinTiles(Integer.parseInt((String) view.getTag()));
        respondToSelection(Integer.parseInt((String) view.getTag()));
    }

    public void onClickTile(View view) {
        // change color back to grey
        // change constraints back to original
        // set visibility of button back to visible
        separateTiles((TextView) view);
    }

    public void repeatGame(View view){
        resetLayout();
        play();
    }

    private void resetLayout() {

    }

    private void separateTiles(TextView clickedTile) {
        // find the clicked tile in JoinedTracker
        // check if there is a button missing on either side
        // if there is, add it back in on that side

        int indexOfTileJT = joinedTracker.indexOf(clickedTile);

        if (visibleViews == 1){
            // only one tile ?
        } else if (indexOfTileJT == 0){ //first tile
            // check index + 1
            if (!joinedTracker.get(1).getText().equals(".")){ // if it's NOT a button
                // restore the button
                TextView button = findViewById(TILES_AND_BUTTONS[1]);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and nextTile
                TextView nextTile = findViewById(TILES_AND_BUTTONS[2]);
                ConstraintLayout constraintLayout = findViewById(R.id.japancl);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(TILES_AND_BUTTONS[0],ConstraintSet.END,TILES_AND_BUTTONS[1],
                        ConstraintSet.START,0); //end of t1 to start of b1
                constraintSet.connect(TILES_AND_BUTTONS[1],ConstraintSet.START,TILES_AND_BUTTONS[0],
                        ConstraintSet.END,0); //start of b1 to end of t1
                constraintSet.connect(TILES_AND_BUTTONS[2],ConstraintSet.START,TILES_AND_BUTTONS[1],
                        ConstraintSet.END,0); //start of t2 to end of b1
                constraintSet.connect(TILES_AND_BUTTONS[1],ConstraintSet.END,TILES_AND_BUTTONS[2],ConstraintSet.START,0);
                constraintSet.applyTo(constraintLayout); //end of b1 to start of t2

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS[i % 5];
                int tileColor = Color.parseColor(tileColorStr);
                nextTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS[i % 5];
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);

                joinedTracker.add(1, button);
                visibleViews = visibleViews + 1;
            }
        } else if (indexOfTileJT == visibleViews - 1){ //final tile
            // check index - 1
            if (!joinedTracker.get(indexOfTileJT - 1).getText().equals(".")){ // if it's NOT a button

                int indexOfMissingButton = visibleViews - 2;
                // restore the button
                TextView button = findViewById(TILES_AND_BUTTONS[indexOfMissingButton]);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and prevTile
                TextView prevTile = findViewById(TILES_AND_BUTTONS[indexOfMissingButton - 1]);
                ConstraintLayout constraintLayout = findViewById(R.id.japancl);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton - 1], ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMissingButton],ConstraintSet.START,0);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton],ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMissingButton -1],ConstraintSet.END,0);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton + 1],ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMissingButton],ConstraintSet.END,0);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMissingButton],ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMissingButton + 1],ConstraintSet.START,0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS[i % 5];
                int tileColor = Color.parseColor(tileColorStr);
                prevTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS[i % 5];
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);

                joinedTracker.add(indexOfTileJT - 1, button);
                visibleViews = visibleViews + 1;
            }
        } else{  //any other tile
            // check index + 1 and index -1
            if (!joinedTracker.get(indexOfTileJT - 1).getText().equals(".")){ // if - 1 NOT a button

                int indexOfMBinOG = originalLayout.indexOf(clickedTile) - 1;
                // restore the button
                TextView button = originalLayout.get(indexOfMBinOG);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and prevTile
                TextView prevTile = originalLayout.get(indexOfMBinOG - 1);
                ConstraintLayout constraintLayout = findViewById(R.id.japancl);
                ConstraintSet constraintSet = new ConstraintSet();

                //CHECK THIS
                constraintSet.clone(constraintLayout);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG - 1],ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMBinOG],ConstraintSet.START,0);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG],ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMBinOG - 1],ConstraintSet.END,0);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG + 1],ConstraintSet.START,
                        TILES_AND_BUTTONS[indexOfMBinOG],ConstraintSet.END,0);
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG],ConstraintSet.END,
                        TILES_AND_BUTTONS[indexOfMBinOG + 1],ConstraintSet.START,0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS[i % 5];
                int tileColor = Color.parseColor(tileColorStr);
                prevTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS[i % 5];
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);

                joinedTracker.add(indexOfTileJT - 1, button);
                visibleViews = visibleViews + 1;
            }
            if (!joinedTracker.get(indexOfTileJT + 1).getText().equals(".")){ // if it's NOT a button

                int indexOfMBinOG = originalLayout.indexOf(clickedTile) + 1;

                // restore the button
                TextView button = originalLayout.get(indexOfMBinOG);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);

                // reset constraints of clickedTile and nextTile
                TextView nextTile = originalLayout.get(indexOfMBinOG + 1);
                ConstraintLayout constraintLayout = findViewById(R.id.japancl);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                // CHECK THIS AND FINISH IT
                constraintSet.connect(TILES_AND_BUTTONS[indexOfMBinOG],ConstraintSet.END,TILES_AND_BUTTONS[],ConstraintSet.START,0);
                constraintSet.connect(BUTTONS[],ConstraintSet.START,TILES[],ConstraintSet.END,0);
                constraintSet.connect(TILES[1],ConstraintSet.START,BUTTONS[],ConstraintSet.END,0);
                constraintSet.connect(BUTTONS[],ConstraintSet.END,TILES[1],ConstraintSet.START,0);
                constraintSet.applyTo(constraintLayout);

                Random rand = new Random();
                int i = rand.nextInt(10);
                String tileColorStr = COLORS[i % 5];
                int tileColor = Color.parseColor(tileColorStr);
                nextTile.setBackgroundColor(tileColor);

                i = rand.nextInt(10);
                tileColorStr = COLORS[i % 5];
                tileColor = Color.parseColor(tileColorStr);
                clickedTile.setBackgroundColor(tileColor);

                joinedTracker.add(, button);
                visibleViews = visibleViews + 1;
            }

        }
    }

    private void respondToSelection(int tag) {
        // check if correct and change color
        // if correct, set those Tiles unclickable to help the user
        // build string of configuration that we have so far
        StringBuilder config = new StringBuilder();
        StringBuilder partialConfig = new StringBuilder(); // hold one in-progress syll at a time
        ArrayList<TextView> listOfIds = new ArrayList<>(); // list of ids that corresponds to
        // that one in-progress syll in partialConfig
        for (int i = 0; i < visibleViews; i++){ //why was this size 20?
            TextView view = joinedTracker.get(i);
            if (view.getText().equals(".")){
                inProgSyllabification.put(partialConfig.toString(), listOfIds);
                listOfIds = new ArrayList<TextView>();
                partialConfig.setLength(0);
            }else{
                listOfIds.add(view);
                partialConfig.append(view.getText());
            }
            config.append(view.getText());
        }

        if (config.toString().equals(wordInLOP)){ // completely correct
            //great job!
            playCorrectSoundThenActiveWordClip(false); //JP not sure what this bool is for

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=1;
            japanPoints+=1;
            pointsEarned.setText(String.valueOf(japanPoints));

            trackerCount++;
            updateTrackers();

            TextView view = findViewById(TILES[0]);
            view.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
            view.setTextColor(Color.parseColor("#FFFFFF")); // white
            view.setClickable(false);
            for (int i = 0; i < BUTTONS.length; i++){
                view = findViewById(TILES[i+1]);
                view.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                view.setTextColor(Color.parseColor("#FFFFFF")); // white
                view.setClickable(false);
                view = findViewById(BUTTONS[i]);
                view.setClickable(false);
            }
        } else{ // one or more syllables correct
            for (String syll : parsedWordIntoSyllables){
                if (inProgSyllabification.containsKey(syll)){
                    // that one syllable is correct so turn them all green
                    for (TextView view : inProgSyllabification.get(syll)){
                        view.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                        view.setTextColor(Color.parseColor("#FFFFFF")); // white
                        view.setClickable(false);
                    }
                }
            }
        }
        inProgSyllabification.clear();
    }

    private void joinTiles(int tag) {
        // make the button between the tiles invisible - DONE
        // change the constraints so the two tiles touch each other - DONE
        // change the color of the two tiles depending on whether they're correct - TO DO
        // make them clickable - DONE

        TextView button = findViewById(BUTTONS[tag]);
        button.setClickable(false);
        button.setVisibility(View.INVISIBLE);

        TextView leftTile = findViewById(TILES[tag]);
        leftTile.setClickable(true);

        TextView rightTile = findViewById(TILES[tag+1]);
        rightTile.setClickable(true);

        ConstraintLayout constraintLayout = findViewById(R.id.japancl);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(TILES[tag],ConstraintSet.END,TILES[tag+1],ConstraintSet.START,0);
        constraintSet.connect(TILES[tag+1],ConstraintSet.START,TILES[tag],ConstraintSet.END,0);
        constraintSet.applyTo(constraintLayout);

        joinedTracker.remove(button);
        visibleViews = visibleViews - 1;

    }
}