package org.alphatilesapps.alphatiles;


import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    static final int GREEN = Color.parseColor(Start.colorList.get(3));
    static final int BLUE = Color.parseColor(Start.colorList.get(1));
    static final int EMPTY = Color.parseColor(Start.colorList.get(6));
    static final int YELLOW = Color.parseColor(Start.colorList.get(5));
    static final int GRAY = Color.parseColor(Start.colorList.get(8));
    static final int KEY_COLOR = Color.parseColor(Start.colorList.get(0));
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
    protected void hideInstructionAudioImage() {
        // Copied from Sudan.java
        View instructionsButton = findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.log(Level.INFO, "Chile start");
        context = this;
        setContentView(R.layout.chile);

        ActivityLayouts.applyEdgeToEdge(this, R.id.chileCL);
        ActivityLayouts.setStatusAndNavColors(this);

        updatePointsAndTrackers(0);
        setAdvanceArrowToGray();
        if (scriptDirection.equals("RTL")) {
            findViewById(R.id.backspace).setScaleX(-1);
            findViewById(R.id.repeatImage).setScaleX(-1);
        }
        data.guesses = baseGuessCount - challengeLevel + 1;
        int guessBoxID = R.id.guessBox;
        guessBox = findViewById(guessBoxID);
        guessAdapter = new TileAdapter(tiles);
        guessAdapter.setFontScale(data.fontScale);
        guessBox.setAdapter(guessAdapter);
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
        resetTiles();
        LOGGER.log(Level.INFO, Arrays.toString(secret));
        currentRow = 0;
        int iID = getAudioInstructionsResID();
        if(iID == 0 || iID == -1) {
            hideInstructionAudioImage();
        }
    }
    private void resetTiles() {
        tiles.clear();
        guessBox.setAdapter(null);
        guessBox.setAdapter(guessAdapter);
        for(int row = 0; row <  data.guesses; row++) {
            for (int col = 0; col < secret.length; col++) {
                tiles.add(new TileAdapter.ColorTile("", EMPTY));

            }
        }
        guessBox.setNumColumns(secret.length);
        guessAdapter.notifyDataSetChanged();
    }
    private void backSpace() {
        if(finished) return;
        for(int i = (currentRow + 1) * secret.length - 1; i >= (currentRow) * secret.length; i--) {
            if(!tiles.get(i).text.isEmpty()) {
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
        resetTiles();
        LOGGER.log(Level.INFO, Arrays.toString(secret));
        guessAdapter.notifyDataSetChanged();
        keyAdapter.notifyDataSetChanged();
        findViewById(R.id.repeatImage).setVisibility(View.INVISIBLE);
        findViewById(R.id.complete_word).setVisibility(View.VISIBLE);
    }

    private void completeWord() {
        if (finished) return;
        int[] correct = new int[secret.length];
        boolean[] frontCor = new boolean[secret.length];
        TileAdapter.ColorTile[] row = new TileAdapter.ColorTile[secret.length];
        int j = 0;
        for(int i = currentRow * secret.length; i < (currentRow + 1) * secret.length; i++) {
            if(tiles.get(i).text.isEmpty()) {
                return;
            }
            row[j] = tiles.get(i);
            j++;
        }
       // ArrayList<String> checkedYellows = new ArrayList<>();
        int greenCount = 0;

        for(int i = 0; i < row.length; i++) {

            if (row[i].text.equals(secret[i])) {
                frontCor[i] = true;
                correct[i] = 1;
                greenCount++;

            }else{
            for(int x = 0; x < row.length; x++) {
                if (row[x].text.equals(secret[i]) && !(row[x].text.equals(secret[x])) && !(frontCor[i]) && correct[x] == 0) {
                    frontCor[i] = true;
                    correct[x] = 2;
                    break;
                }
            }
            }
        }
        for(int i = 0; i < row.length; i++){
            if(correct[i] == 2){
                row[i].color = BLUE;
            }
            if(correct[i] == 1){
                row[i].color = GREEN;
            }
            if(correct[i] == 0){
                row[i].color = GRAY;
            }
        }

           /** if(row[i].text.equals(secret[i])) {
                row[i].color = GREEN;
                greenCount++;
            }
            else if(Arrays.asList(tempS).contains(row[i].text)) {
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
            }**/
           for(int i = 0; i < row.length; i++) {
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
        if(greenCount == secret.length && !finished) {
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
        guessBox.smoothScrollToPosition(currentRow * secret.length);
        String text =
                ((TextView)((LinearLayout)(((SquareConstraintLayout)key).getChildAt(0)))
                        .getChildAt(0))
                        .getText()
                        .toString(); // ._.

        for(int i = currentRow * secret.length; i < (currentRow + 1) * secret.length; i++) {
            if(tiles.get(i).text.isEmpty()) {
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
            if(split.size() > maxWordLength || split.size() < minWordLength) {
                continue;
            }
            splitWords.add(split.toArray(new String[0]));
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
        return new ChileData(splitWords, kbArray, keyboardWidth, fontScale);
    }
    public static class ChileData {
        public int guesses;
        public int keyboardWidth;
        public String[] keys;
        public ArrayList<String[]> words;
        public float fontScale;
        public ChileData(ArrayList<String[]> words, String[] keys, int keyboardWidth, float fontScale) {
            this.keyboardWidth = keyboardWidth;
            this.keys = keys;
            this.words = words;
            this.fontScale = fontScale;
        }
    }
}
