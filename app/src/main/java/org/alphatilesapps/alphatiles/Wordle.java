package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Wordle extends GameActivity {
    static WordleData data;
    private static final Logger LOGGER = Logger.getLogger(Wordle.class.getName());
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
    boolean hasChecked12Trackers;
    int currentRow;

    String[] secret;
    TileAdapter wordleAdapter;
    TileAdapter keyAdapter;
    GridView wordleBox;
    Random rng = new Random();
    ArrayList<TileAdapter.ColorTile> tiles = new ArrayList<>();
    ArrayList<TileAdapter.ColorTile> keys = new ArrayList<>();
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
    public void goBackToEarth(View view) {
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        prefs.edit().putInt(uniqueGameLevelPlayerID, trackerCount).apply();
        super.goBackToEarth(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        hasChecked12Trackers = prefs.getBoolean("storedWordleHasChecked12Trackers_level"
                + challengeLevel + "_player" + playerString + "_" + syllableGame, false);
        challengeLevel = getIntent().getIntExtra("challengeLevel", 0);
        syllableGame = getIntent().getStringExtra("syllableGame");
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString + syllableGame;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        LOGGER.log(Level.INFO, "Wordle start");

        context = this;
        setContentView(R.layout.wordle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        updateTrackers();

        data.guesses = baseGuessCount - challengeLevel;
        LOGGER.log(Level.INFO, ""+data.guesses);
        int wordleBoxID = R.id.wordleBox;
        wordleBox = findViewById(wordleBoxID);
        wordleBox.setNumColumns(data.wordLength);
        wordleAdapter = new TileAdapter(tiles);
        wordleBox.setAdapter(wordleAdapter);

        for(int row = 0; row < data.guesses; row++) {
            for (int col = 0; col < data.wordLength; col++) {
                tiles.add(new TileAdapter.ColorTile("", EMPTY));
            }
        }
        int keyboardID = R.id.wordleKeyboard;
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
        findViewById(R.id.wordle_backspace).setOnClickListener(view -> backSpace());
        findViewById(R.id.wordle_complete_word).setOnClickListener(view -> completeWord());
        findViewById(R.id.repeatImage).setOnClickListener(view -> reset());
        keyboard.setAdapter(keyAdapter);

        keyboard.setOnItemClickListener((board, key, i, l) -> keyPressed((GridView)board, key, i, l));
        secret = data.words.get(rng.nextInt(data.words.size()));
        LOGGER.log(Level.INFO, Arrays.toString(secret));
        currentRow = 0;

    }
    private void backSpace() {
        for(int i = (currentRow + 1) * data.wordLength - 1; i >= (currentRow) * data.wordLength; i--) {
            if(!tiles.get(i).text.equals("")) {
                tiles.get(i).text = "";
                wordleAdapter.notifyDataSetChanged();
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
        secret = data.words.get(rng.nextInt(data.words.size()));
        LOGGER.log(Level.INFO, Arrays.toString(secret));
        wordleAdapter.notifyDataSetChanged();
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
        wordleAdapter.notifyDataSetChanged();
        keyAdapter.notifyDataSetChanged();
        if(greenCount == data.wordLength) {
            finished = true;
            int sound;
            if(++trackerCount >= 12) {
                sound = Start.correctFinalSoundID;
                hasChecked12Trackers = true;
            }
            else {
                sound = Start.correctSoundID;
            }
            updateTrackers();
            Start.gameSounds.play(sound, 1.0f, 1.0f, 3, 0, 1.0f);
        }
        else if(currentRow == data.guesses - 1) {
            finished = true;
        }
        else {
            currentRow++;
        }

    }

    private void keyPressed(GridView board, View key, int position, long id) {
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
                wordleAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    public static WordleData wordlePreProcess() {
        try {
            Wordle.baseGuessCount = Integer.parseInt(Start.settingsList.find("Wordle base guess count"));
        }
        catch (Exception e) {}
        try {
            Wordle.minWordLength = Integer.parseInt(Start.settingsList.find("Wordle minimum word length"));
        }
        catch (Exception e) {}
        try {
            Wordle.maxWordLength = Integer.parseInt(Start.settingsList.find("Wordle maximum word length"));
        }
        catch (Exception e) {}
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
        return new WordleData(bestLength, splitWords, sortedKeyboard);
    }
    public static class WordleData {
        public int guesses;
        public int wordLength;
        public int keyboardWidth;
        public String[] keys;
        public ArrayList<String[]> words;

        public WordleData(int wordLength, ArrayList<String[]> words, String[] keys) {
            this.wordLength = wordLength;
            keyboardWidth = 8;
            this.keys = keys;
            this.words = words;
        }
    }
}
