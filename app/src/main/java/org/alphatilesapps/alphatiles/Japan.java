package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

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

    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    ArrayList<String> parsedWordIntoTiles;
    ArrayList<String> parsedWordIntoSyllables;
    ArrayList<Integer> joinedTracker = new ArrayList<>();
    HashMap<String, ArrayList<Integer>> inProgSyllabification = new HashMap<>();
    ArrayList<String> correctSyllabification = new ArrayList<>();

    protected static final int[] TILES = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07,
            R.id.tile08, R.id.tile09, R.id.tile10, R.id.tile11, R.id.tile12
    };

    protected static final int[] BUTTONS = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11
    };

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336", "#4CAF50", "#E91E63"};


    @Override
    protected int[] getTileButtons() {
        return new int[0];
    }

    @Override
    protected int[] getWordImages() {
        return new int[0];
    }

    @Override
    protected int getAudioInstructionsResID() {
        return 0;
    }

    @Override
    protected void centerGamesHomeImage() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.japan);

        joinedTracker.add(TILES[0]);
        for (int i = 1; i < BUTTONS.length; i++){
            joinedTracker.add(BUTTONS[i]);
            joinedTracker.add(TILES[i+1]);
        }
    }

    private void chooseWord(){

        boolean freshWord = false;

        while(!freshWord) {
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size());

            wordInLWC = Start.wordList.get(randomNum).nationalWord;
            wordInLOP = Start.wordList.get(randomNum).localWord;

            parsedWordIntoTiles = tileList.parseWordIntoTiles(wordInLOP);
            /*
            parsedWordIntoSyllables = syllableList.parseWordIntoSyllables(wordInLOP);
            for (String syll : parsedWordIntoSyllables){
                correctSyllabification.add(syll);
                correctSyllabification.add("*");
            }
            // other option -- every time you change joinedTracker, convert it into a string and
            check if that string is equal to wordInLOP with the periods in it still
             */
            if (parsedWordIntoTiles.size() <= TILES.length){ //JP: choose word w/ <= 7 tiles
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
        for (int i = 0; i < parsedWordIntoTiles.size(); i++){
            String tileColorStr = COLORS[i % 5];
            int tileColor = Color.parseColor(tileColorStr);
            TextView tile = findViewById(TILES[i]);
            tile.setText(parsedWordIntoTiles.get(i));
            tile.setClickable(false);
            tile.setVisibility(View.VISIBLE);
            tile.setBackgroundColor(tileColor);
            tile.setTextColor(Color.parseColor("#FFFFFF")); // white;
        }
        for (int i = parsedWordIntoTiles.size(); i < TILES.length; i++){
            TextView tile = findViewById(TILES[i]);
            tile.setClickable(false);
            tile.setVisibility(View.INVISIBLE);
        }
    }

    private void setVisButtonsClickable(){
        for (int i = 0; i < parsedWordIntoTiles.size() - 1; i ++){
            Button button = findViewById(BUTTONS[i]);
            button.setClickable(true);
        }
    }

    private void setAllButtonsUnclickable(){
        for (int i = 0; i < BUTTONS.length; i++){
            Button button = findViewById(BUTTONS[i]);
            button.setClickable(false);
        }
    }

    private void setTilesUnclickable(){
        for (int i = 0; i < TILES.length; i++){
            TextView tile = findViewById(TILES[i]);
            tile.setClickable(false);
        }
    }

    public void onClickJapan(View view) {
        joinTiles((Integer) view.getTag());
        respondToSelection((Integer)view.getTag());
    }

    public void onClickTile(View view) {
        // change color back to grey
        // change constraints back to original
        // set visibility of button back to visible
        separateTiles((Integer) view.getTag());
    }

    private void separateTiles(Integer tag) {
        // find the clicked button in JoinedTracker
        // check if there is a button missing on either side
        // if there is, add it back in on that side
    }

    private void respondToSelection(int tag) {
    }

    private void joinTiles(int tag) {
        // make the button between the tiles invisible - DONE
        // change the constraints so the two tiles touch each other - DONE
        // change the color of the two tiles depending on whether they're correct - TO DO
        // make them clickable - DONE

        Button button = findViewById(BUTTONS[tag]);
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

        // check if correct and change color
        // if correct, set those Tiles unclickable to help the user
        joinedTracker.remove(BUTTONS[tag]);

        // build string of configuration that we have so far
        StringBuilder config = new StringBuilder();
        StringBuilder partialConfig = new StringBuilder(); // hold one in-progress syll at a time
        ArrayList<Integer> listOfIds = new ArrayList<>(); // list of ids that corresponds to
        // that one in-progress syll in partialConfig
        for (int i : joinedTracker){
            TextView view = findViewById(i);
            if (view.getText().equals(".")){
                inProgSyllabification.put(partialConfig.toString(), listOfIds);
                listOfIds.clear();
                partialConfig.setLength(0);
            }
            listOfIds.add(i);
            partialConfig.append(view.getText());
            config.append(view.getText());
        }

        if (config.equals(wordInLOP)){
            //great job!
            //play word audio
            //play correct sound
            playCorrectSoundThenActiveWordClip(false); //JP not sure what this bool is for

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=1;
            japanPoints+=1;
            pointsEarned.setText(String.valueOf(japanPoints));

            trackerCount++;
            updateTrackers();
            //increase points
            //update 12 trackers
            //make everything unclickable
        } else{
            for (String syll : parsedWordIntoSyllables){
                if (inProgSyllabification.containsKey(syll)){
                    // that one syllable is correct so turn them all green
                    for (int i : inProgSyllabification.get(syll)){
                        TextView view = findViewById(i);
                        view.setBackgroundColor(Color.parseColor("#4CAF50")); // theme green
                        view.setTextColor(Color.parseColor("#FFFFFF")); // white
                        view.setClickable(false);
                    }
                }
            }
        }




    }
}