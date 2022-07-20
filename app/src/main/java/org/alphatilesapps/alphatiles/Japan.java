package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
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

    protected static final int[] TILES = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07,
            R.id.tile08, R.id.tile09, R.id.tile10, R.id.tile11, R.id.tile12
    };

    protected static final int[] BUTTONS = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6
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
    }

    private void chooseWord(){

        boolean freshWord = false;

        while(!freshWord) {
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size());

            wordInLWC = Start.wordList.get(randomNum).nationalWord;
            wordInLOP = Start.wordList.get(randomNum).localWord;

            parsedWordIntoTiles = tileList.parseWordIntoTiles(wordInLOP);
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
        //
    }

    private void respondToSelection(int tag) {
    }

    private void joinTiles(int tag) {
        // make the button between the tiles invisible
        // change the constraints so the two tiles touch each other
        // change the color of the two tiles depending on whether they're correct
        // make them clickable -- if either tile is clicked, restore the button between them

        Button button = findViewById(BUTTONS[tag]);
        button.setClickable(false);
        button.setVisibility(View.INVISIBLE);

        TextView leftTile = findViewById(TILES[tag]);
        leftTile.setClickable(true);
        //leftTile. - change constraints

        TextView rightTile = findViewById(TILES[tag+1]);
    }
}