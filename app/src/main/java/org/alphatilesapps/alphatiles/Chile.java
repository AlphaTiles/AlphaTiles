package org.alphatilesapps.alphatiles;


import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    static int maxWordLength = 100;
    static int minWordLength = 3;
    static int maxKeyboardSize = 50;
    static int baseGuessCount = 8;
    static final int GREEN = Color.parseColor(Start.COLORS.get(3));
    static final int BLUE = Color.parseColor(Start.COLORS.get(1));
    static final int EMPTY = Color.parseColor(Start.COLORS.get(6));
    static final int YELLOW = Color.parseColor(Start.COLORS.get(5));
    static final int GRAY = Color.parseColor(Start.COLORS.get(8));
    static final int KEY_COLOR = Color.parseColor(Start.COLORS.get(0));
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
    protected int[] getGameButtons() {
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
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
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
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.backspace,ConstraintSet.START,0);
        //constraintSet.connect(R.id.backspace,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.applyTo(constraintLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.log(Level.INFO, "Chile start");
        context = this;
        setContentView(R.layout.chile);
        updatePointsAndTrackers(0);
        setAdvanceArrowToGray();
        if (scriptDirection.equals("RTL")) {
            findViewById(R.id.backspace).setScaleX(-1);
            findViewById(R.id.repeatImage).setScaleX(-1);
        }
        data.guesses = baseGuessCount - challengeLevel;
        int guessBoxID = R.id.guessBox;
        guessBox = findViewById(guessBoxID);
        guessBox.setNumColumns(data.wordLength);
        guessAdapter = new TileAdapter(tiles);
        guessAdapter.setFontScale(data.fontScale);
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
        keyAdapter.setFontScale(data.fontScale);
        for(int row = 0; row <= data.keys.length / data.keyboardWidth; row++) {
            for(int col = 0; col < data.keyboardWidth; col++) {
                if(row * data.keyboardWidth + col < data.keys.length) {
                    keys.add(new TileAdapter.ColorTile(
                            data.keys[row * data.keyboardWidth + col], KEY_COLOR));
                }
            }
        }
        keyAdapter.notifyDataSetChanged();

        findViewById(R.id.backspace).setOnClickListener(view -> backSpace());
        findViewById(R.id.complete_word).setOnClickListener(view -> completeWord());
        findViewById(R.id.repeatImage).setOnClickListener(view -> reset());
        findViewById(R.id.repeatImage).setVisibility(View.INVISIBLE);
        findViewById(R.id.complete_word).setVisibility(View.VISIBLE);

        keyboard.setAdapter(keyAdapter);
        keyboard.setOnItemClickListener((board, key, i, l) -> keyPressed(key));
        wordList = new ArrayList<>(data.words);
        // shuffle
        for(int i = wordList.size() - 1; i > 0; i--) {
            String[] tmp = wordList.get(i);
            int j = rng.nextInt(i + 1);
            wordList.set(i, wordList.get(j));
            wordList.set(j, tmp);
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
        if(finished) return;
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
        guessBox.smoothScrollToPosition(0);
        setAdvanceArrowToGray();
        while (tiles.size() > data.guesses * data.wordLength) {
            tiles.remove(tiles.size() - 1);
        }
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
        findViewById(R.id.repeatImage).setVisibility(View.INVISIBLE);
        findViewById(R.id.complete_word).setVisibility(View.VISIBLE);
    }

    private void completeWord() {
        if (finished) return;
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
                    row[i].color = BLUE;
                }
                else {
                    row[i].color = GRAY;
                }
            }
            else {
                row[i].color = GRAY;
            }
            for(TileAdapter.ColorTile key : keys) {
                if(key.text.equals(row[i].text)) {
                    if((key.color != BLUE && key.color != GREEN) || (key.color == BLUE && row[i].color == GREEN)) {
                        key.color = row[i].color;
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
            for(String tile : secret) {
                tiles.add(new TileAdapter.ColorTile(tile, GREEN, YELLOW));
            }
            guessBox.smoothScrollToPosition(tiles.size());
            guessAdapter.notifyDataSetChanged();
            Start.gameSounds.play(Start.incorrectSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
        }
        else {
            currentRow++;
        }
        if(finished) {
            findViewById(R.id.repeatImage).setVisibility(View.VISIBLE);
            findViewById(R.id.complete_word).setVisibility(View.INVISIBLE);
            setAdvanceArrowToBlue();
            setOptionsRowClickable();
        }
    }

    private void keyPressed(View key) {
        LOGGER.log(Level.INFO, "Key pressed");
        guessBox.smoothScrollToPosition(currentRow * data.wordLength);
        String text =
                ((TextView)((LinearLayout)(((SquareConstraintLayout)key).getChildAt(0)))
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
            ArrayList<String> split = new ArrayList<>();
            for(Start.Tile tile : Start.tileList.parseWordIntoTiles(word.wordInLOP, word)) {
                split.add(tile.text);
            }
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
        String[] kbArray = keyboard.toArray(new String[0]);
        int[] indexArray = new int[kbArray.length];
        int j = 0;
        for (String key : kbArray) {
            for(int idx = 0; idx < Start.tileList.size(); idx++) {
                if (key.equals(Start.tileList.get(idx).text)) {
                    indexArray[j] = idx;
                    break;
                }
            }
            j++;
        }
        Arrays.sort(indexArray);
        j = 0;
        for (int idx : indexArray) {
            kbArray[j] = Start.tileList.get(idx).text;
            j++;
        }
        float fontScale = Util.getMinFontSize(kbArray);
        return new ChileData(bestLength, splitWords, kbArray, keyboardWidth, fontScale);
    }
    public static class ChileData {
        public int guesses;
        public int wordLength;
        public int keyboardWidth;
        public String[] keys;
        public ArrayList<String[]> words;
        public float fontScale;
        public ChileData(int wordLength, ArrayList<String[]> words, String[] keys, int keyboardWidth, float fontScale) {
            this.wordLength = wordLength;
            this.keyboardWidth = keyboardWidth;
            this.keys = keys;
            this.words = words;
            this.fontScale = fontScale;
        }
    }
}
