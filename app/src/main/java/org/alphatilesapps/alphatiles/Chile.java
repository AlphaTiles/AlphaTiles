package org.alphatilesapps.alphatiles;


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chile extends GameActivity {
    static ChileData data;
    private static final Logger LOGGER = Logger.getLogger(Chile.class.getName());
    static int maxWordLength = 6;
    static int minWordLength = 4;
    static int maxKeyboardSize = 50;
    static int baseGuessCount = 8;
    static final int GREEN = 0xFF00FF00;
    static final int YELLOW = 0xFFFFFF00;
    static final int EMPTY = 0xFF222222;
    static final int GRAY = 0xFF444444;
    static final int KEY_COLOR = 0xFF552222;
    boolean finished = false;
    int currentRow;

    String[] secret;
    TileAdapter guessAdapter;
    TileAdapter keyAdapter;
    GridView guessBox;
    Random rng = new Random();
    ArrayList<String[]> wordList = new ArrayList<>();
    ArrayList<TileAdapter.ColorTile> tiles = new ArrayList<>();
    ArrayList<TileAdapter.ColorTile> keys = new ArrayList<>();
    @Override
    protected int[] getTileButtons() {
        return null;
    }

    @Override
    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        // Copied from Colombia.java
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        }
        catch (NullPointerException e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        // Copied from Sudan.java
        View instructionsButton = findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.chileCL;

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGGER.log(Level.INFO, "Chile start");
        context = this;
        setContentView(R.layout.chile);
        updatePointsAndTrackers(0);

        data.guesses = baseGuessCount - challengeLevel;
        int guessBoxID = R.id.guessBox;
        guessBox = findViewById(guessBoxID);
        guessBox.setNumColumns(data.wordLength);
        guessAdapter = new TileAdapter(tiles);
        guessBox.setAdapter(guessAdapter);

        for(int row = 0; row < data.guesses; row++) {
            for (int col = 0; col < data.wordLength; col++) {
                tiles.add(new TileAdapter.ColorTile("", EMPTY));
            }
        }
        int keyboardID = R.id.keyboard;
        GridView keyboard = findViewById(keyboardID);
        keyboard.setNumColumns(data.keyboardWidth);
        keyAdapter = new TileAdapter(keys);

        for(int row = 0; row <= data.keys.length / data.keyboardWidth; row++) {
            for(int col = 0; col < data.keyboardWidth; col++) {
                if(row * data.keyboardWidth + col < data.keys.length) {
                    keys.add(new TileAdapter.ColorTile(
                            data.keys[row * data.keyboardWidth + col], KEY_COLOR));
                }
            }
        }
        findViewById(R.id.backspace).setOnClickListener(view -> backSpace());
        findViewById(R.id.complete_word).setOnClickListener(view -> completeWord());
        findViewById(R.id.repeatImage).setOnClickListener(view -> reset());
        keyboard.setAdapter(keyAdapter);

        keyboard.setOnItemClickListener((board, key, i, l) -> keyPressed(key));
        wordList = new ArrayList<>(data.words);
        // shuffle
        for(int i = wordList.size() - 1; i > 0; i--) {
            String[] tmp = wordList.get(i);
            int j = rng.nextInt(i + 1);
            wordList.set(i, wordList.get(j));
            wordList.set(j, wordList.get(i));
        }
        secret = wordList.remove(wordList.size() - 1);
        LOGGER.log(Level.INFO, Arrays.toString(secret));
        currentRow = 0;
        int iID = getAudioInstructionsResID();
        if(iID == 0 || iID == -1) {
            centerGamesHomeImage();
        }
    }
    private void backSpace() {
        for(int i = (currentRow + 1) * data.wordLength - 1; i >= (currentRow) * data.wordLength; i--) {
            if(!tiles.get(i).text.equals("")) {
                tiles.get(i).text = "";
                guessAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    private void reset() {
        if(!finished) return;
        for(int i = 0; i < tiles.size(); i++) {
            TileAdapter.ColorTile tile = tiles.get(i);
            tile.text = "";
            tile.color = EMPTY;
        }
        for(int i = 0; i < keys.size(); i++) {
            TileAdapter.ColorTile key = keys.get(i);
            key.color = KEY_COLOR;
        }
        currentRow = 0;
        finished = false;
        if(wordList.isEmpty()) {
            wordList = new ArrayList<>(data.words);
            // shuffle
            for(int i = wordList.size() - 1; i > 0; i--) {
                String[] tmp = wordList.get(i);
                int j = rng.nextInt(i + 1);
                wordList.set(i, wordList.get(j));
                wordList.set(j, tmp);
            }
        }
        secret = wordList.remove(wordList.size() - 1);
        LOGGER.log(Level.INFO, Arrays.toString(secret));
        guessAdapter.notifyDataSetChanged();
        keyAdapter.notifyDataSetChanged();
    }

    private void completeWord() {
        TileAdapter.ColorTile[] row = new TileAdapter.ColorTile[data.wordLength];
        int j = 0;
        for(int i = currentRow * data.wordLength; i < (currentRow + 1) * data.wordLength; i++) {
            if(tiles.get(i).text.equals("")) {
                return;
            }
            row[j] = tiles.get(i);
            j++;
        }
        ArrayList<String> checkedYellows = new ArrayList<>();
        int greenCount = 0;
        for(int i = 0; i < row.length; i++) {

            if(row[i].text.equals(secret[i])) {
                row[i].color = GREEN;
                greenCount++;
            }
            else if(Arrays.asList(secret).contains(row[i].text)) {
                if(!checkedYellows.contains(row[i].text)) {
                    checkedYellows.add(row[i].text);
                    row[i].color = YELLOW;
                }
                else {
                    row[i].color =  GRAY;
                }
            }
            else {
                row[i].color = GRAY;
                for(TileAdapter.ColorTile key : keys) {
                    if(key.text.equals(row[i].text)) {
                        key.color = GRAY;
                    }
                }
            }
        }
        guessAdapter.notifyDataSetChanged();
        keyAdapter.notifyDataSetChanged();
        if(greenCount == data.wordLength && !finished) {
            finished = true;
            Start.gameSounds.play(Start.correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
            updatePointsAndTrackers(1);
        }
        else if(currentRow == data.guesses - 1) {
            finished = true;
        }
        else {
            currentRow++;
        }

    }

    private void keyPressed(View key) {
        LOGGER.log(Level.INFO, "Key pressed");
        String text =
                ((AutoTextView)((LinearLayout)(((SquareConstraintLayout)key).getChildAt(0)))
                        .getChildAt(0))
                        .getText()
                        .toString(); // ._.
        LOGGER.log(Level.INFO, text);

        for(int i = currentRow * data.wordLength; i < (currentRow + 1) * data.wordLength; i++) {
            if(tiles.get(i).text.equals("")) {
                tiles.get(i).text = text;
                guessAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    public static ChileData chilePreProcess() {
        int keyboardWidth = 7;
        try {
            keyboardWidth = Integer.parseInt(Start.settingsList.find("Chile keyboard width"));
        }
        catch (Exception ignored) {}
        try {
            Chile.baseGuessCount = Integer.parseInt(Start.settingsList.find("Chile base guess count"));
        }
        catch (Exception ignored) {}
        try {
            Chile.minWordLength = Integer.parseInt(Start.settingsList.find("Chile minimum word length"));
        }
        catch (Exception ignored) {}
        try {
            Chile.maxWordLength = Integer.parseInt(Start.settingsList.find("Chile maximum word length"));
        }
        catch (Exception ignored) {}
        ArrayList<String[]> splitWords = new ArrayList<>();

        for(Start.Word word : Start.wordList) {
            ArrayList<String> split = Start.tileList.parseWordIntoTiles(word.localWord);
            if(split != null) {
                splitWords.add(split.toArray(new String[0]));
            }
        }
        LOGGER.log(Level.INFO, Integer.toString(splitWords.size()));
        int bestLength = 0;
        int bestCount = 0;
        for(int i = minWordLength; i <= maxWordLength; i++) {
            int count = 0;
            for(String[] word : splitWords) {
                if(word.length == i) {
                    count++;
                }
            }
            if(count > bestCount) {
                bestLength = i;
            }
        }
        for(int i = 0; i < splitWords.size();) {
            if(splitWords.get(i).length != bestLength) {
                splitWords.remove(i);
            }
            else {
                i++;
            }
        }
        HashSet<String> keyboard = new HashSet<>();

        for(int i = 0; i < splitWords.size();) {
            boolean canUseWord = true;
            for(String tile : splitWords.get(i)) {
                if(keyboard.size() < maxKeyboardSize) {
                    keyboard.add(tile);
                }
                else if(!keyboard.contains(tile)) {
                    splitWords.remove(i);
                    canUseWord = false;
                    break;
                }
            }
            if(canUseWord) {
                i++;
            }
        }
        String[] sortedKeyboard = keyboard.toArray(new String[0]);
        Arrays.sort(sortedKeyboard);
        return new ChileData(bestLength, splitWords, sortedKeyboard, keyboardWidth);
    }
    public static class ChileData {
        public int guesses;
        public int wordLength;
        public int keyboardWidth;
        public String[] keys;
        public ArrayList<String[]> words;

        public ChileData(int wordLength, ArrayList<String[]> words, String[] keys, int keyboardWidth) {
            this.wordLength = wordLength;
            this.keyboardWidth = keyboardWidth;
            this.keys = keys;
            this.words = words;
        }
    }
}
