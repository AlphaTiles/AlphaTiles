package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import static org.alphatilesapps.alphatiles.Start.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

public class Taiwan extends GameActivity {

    boolean failedToMatchInitialTile = false;
    Start.Tile activeTile;
    boolean directionIsForward = true;
    static int maxWordLength = 100;
    static int minWordLength = 3;
    String scriptDirection; // aa_langinfo.txt value for Script Direction (LTR or RTL)
    int groupCount; // Number of words selected for an active tile, based on settings
    int index = 0; // Index of the word being viewed within the group of all words for the tile
    boolean skipThisTile = false; // True when it's a gray word (a word that demonstrates the tile with a medial instance not a word-initial instance)
    String tileToStartOn;
    String typeOfTileToStartOn;

    // settings to see tiles in focus bolded or not
    private static final Logger LOGGER = Logger.getLogger(Taiwan.class.getName());

    protected int[] getGameButtons() {
        return null;
    }

    protected int[] getWordImages() {
        return null;
    }

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).instructionAudioName, "raw", context.getPackageName());
        } catch (Resources.NotFoundException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void hideInstructionAudioImage() {
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.taiwan);

        ActivityLayouts.applyEdgeToEdge(this, R.id.taiwanCL);
        ActivityLayouts.setStatusAndNavColors(this);


        // tileToStartOn = cumulativeStageBasedTileList.get(0).text;
        // typeOfTileToStartOn = cumulativeStageBasedTileList.get(0).typeOfThisTileInstance;

        String tileToStartOn = prefs.getString("lastActiveTileGame001_player" + playerString, this.tileToStartOn);
        String typeOfTileToStartOn = prefs.getString("typeOfLastActiveTileGame001_player" + playerString, this.typeOfTileToStartOn);

        // Load bold settings

        scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
        ImageView backwardArrowImage = (ImageView) findViewById(R.id.backwardArrowImage);
        ImageView forwardArrowImage = (ImageView) findViewById(R.id.forwardArrowImage);
        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            backwardArrowImage.setRotationY(180);
            forwardArrowImage.setRotationY(180);
            instructionsImage.setRotationY(180);
        }
        if(Start.changeArrowColor) {
            backwardArrowImage.setImageResource(R.drawable.zz_backward_inactive);
            forwardArrowImage.setImageResource(R.drawable.zz_forward_inactive);
        }
        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        int i = 0;
        while (!(cumulativeStageBasedTileList.get(i).text.equals(tileToStartOn)
                && cumulativeStageBasedTileList.get(i).typeOfThisTileInstance.equals(typeOfTileToStartOn))) {
            i++;
            if (i >= cumulativeStageBasedTileList.size()) {
                // saved tile not in this stage, so start with the 0th tile
                LOGGER.info("onCreate: saved tile=" + tileToStartOn + " not in this stage, start with tile 0");
                i = 0;
                break;
            }
        }
        activeTile = cumulativeStageBasedTileList.get(i);
        setUp(activeTile);
    }



    private void setUp(Start.Tile activeTile) {

    }

    public void onRefClick(View view) {
        super.tileAudioPress(false, activeTile);
    }

    public void goToNextWord(Start.Tile activeTile) {
        index++;
        if (index == groupCount) {
            index = 0;
        }
        refWord = groupOfWordsForActiveTile[index];

        if (!skipThisTile) {
            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            //String wordToDisplay = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
            SpannableStringBuilder boldedWord = boldActiveLetterInWord(refWord, activeTile);
            activeWord.setText(boldedWord);

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = tileList.returnPositionInAlphabet(activeTile);
            String tileColorStr = colorList.get(alphabetPosition % 5);
            int tileColor = Color.parseColor(tileColorStr);
            TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
            gameTile.setBackgroundColor(tileColor);
            activeWord.setBackgroundColor(tileColor);
            TextView numberOfTotal = (TextView) findViewById(R.id.numberOfTotalText);
            numberOfTotal.setText(index + 1 + " / " + String.valueOf(groupCount));

            if (failedToMatchInitialTile) {
                tileColorStr = "#A9A9A9"; // dark gray
                tileColor = Color.parseColor(tileColorStr);
                activeWord.setBackgroundColor(tileColor);
            }

            if (groupCount > 0) {
                playActiveWordClip(false);
            }
        } else {
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }
    }
    public void goToPreviousWord(Start.Tile activeTile) {
        index--;
        if (index == -1) {
            index = groupCount - 1;
        }
        refWord = groupOfWordsForActiveTile[index];


        if (!skipThisTile) {
            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            //String wordToDisplay = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
            SpannableStringBuilder boldedWord = boldActiveLetterInWord(refWord, activeTile);
            activeWord.setText(boldedWord);

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = tileList.returnPositionInAlphabet(activeTile);
            String tileColorStr = colorList.get(alphabetPosition % 5);
            int tileColor = Color.parseColor(tileColorStr);
            TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
            gameTile.setBackgroundColor(tileColor);
            activeWord.setBackgroundColor(tileColor);
            TextView numberOfTotal = (TextView) findViewById(R.id.numberOfTotalText);

            numberOfTotal.setText(index + 1 + " / " + String.valueOf(groupCount));
            if (failedToMatchInitialTile) {
                tileColorStr = "#A9A9A9"; // dark gray
                tileColor = Color.parseColor(tileColorStr);
                activeWord.setBackgroundColor(tileColor);
            }
            if (groupCount > 0) {
                playActiveWordClip(false);
            }
        } else {
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }
    }

    public void goToNextTile(View View) {
        directionIsForward = true;
        Start.Tile oldTile = activeTile;
        activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);
        // LOGGER.info("goToNextTile: " + oldTile.text +"/" + activeTile.text);

            while (Start.wordList.numberOfWordsForActiveTile(activeTile, 1) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);
            }

        // If we are not distinguishing between multitype tiles, and this is a multitype tile,
        // loop until we find a new one (same text - the type will be different).
        if (!differentiatesTileTypes && MULTITYPE_TILES.contains(activeTile.text)) {
            while (activeTile.text.equals(oldTile.text)) {
                LOGGER.info("goToNextTile: skip to one more after " + activeTile.text);
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);
            }
        }

        index = 0;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile.text);
        editor.putString("typeOfLastActiveTileGame001_player" + playerString, activeTile.typeOfThisTileInstance);
        editor.apply();
        // LOGGER.info("goToNextTile: " + oldTile.text +"/" + activeTile.text);
        setUp(activeTile);
    }

    public void goToPreviousTile(View View) {
        directionIsForward = false;
        Start.Tile oldTile = activeTile;
        activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);

            while (Start.wordList.numberOfWordsForActiveTile(activeTile, 1) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);
            }



        // If we are not distinguishing between multitype tiles, and this is a multitype tile,
        // loop until we find a new one (same text - the type will be different).
        if (!differentiatesTileTypes && MULTITYPE_TILES.contains(activeTile.text)) {
            while (activeTile.text.equals(oldTile.text)) {
                LOGGER.info("goToPreviousTile: skip to one more before " + activeTile.text);
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);
            }
        }

        index = 0;
        SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile.text);
        editor.putString("typeOfLastActiveTileGame001_player" + playerString, activeTile.typeOfThisTileInstance);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
    }

    public void repeatGame(View View) {
        setUp(activeTile);
    }

    public void scrollForward(View view) {
        goToNextWord(activeTile);
    }

    public void scrollBack(View view) {
        goToPreviousWord(activeTile);
    }

    @Override
    protected void setAllGameButtonsUnclickable() {
        TextView tileBox = findViewById(R.id.tileBoxTextView);
        tileBox.setClickable(false);

        ImageView word = findViewById(R.id.wordImage);
        word.setClickable(false);

        ImageView forwardArrow = findViewById(R.id.forwardArrowImage);
        forwardArrow.setClickable(false);

        ImageView backwardArrow = findViewById(R.id.backwardArrowImage);
        backwardArrow.setClickable(false);
        if(Start.changeArrowColor) {
            forwardArrow.setImageResource(R.drawable.zz_forward_inactive);
            backwardArrow.setImageResource(R.drawable.zz_backward_inactive);
        }
        backwardArrow.setBackgroundResource(0);

        TextView numberOfTotal = findViewById(R.id.numberOfTotalText);
        numberOfTotal.setClickable(false);
    }

    @Override
    protected void setAllGameButtonsClickable() {
        TextView tileBox = findViewById(R.id.tileBoxTextView);
        tileBox.setClickable(true);

        ImageView word = findViewById(R.id.wordImage);
        word.setClickable(true);

        ImageView forwardArrow = findViewById(R.id.forwardArrowImage);
        forwardArrow.setClickable(true);
        forwardArrow.setBackgroundResource(0);
        forwardArrow.setImageResource(R.drawable.zz_forward);

        ImageView backwardArrow = findViewById(R.id.backwardArrowImage);
        backwardArrow.setClickable(true);
        backwardArrow.setBackgroundResource(0);
        backwardArrow.setImageResource(R.drawable.zz_backward);

        TextView numberOfTotal = findViewById(R.id.numberOfTotalText);
        numberOfTotal.setClickable(true);
    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }
    public static TaiwanData taiwanDataPreProcess() {
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
        return new TaiwanData(splitWords, kbArray, keyboardWidth, fontScale);
    }
    public static class TaiwanData {
        public int guesses;
        public int keyboardWidth;
        public String[] keys;
        public ArrayList<String[]> words;
        public float fontScale;
        public TaiwanData(ArrayList<String[]> words, String[] keys, int keyboardWidth, float fontScale) {
            this.keyboardWidth = keyboardWidth;
            this.keys = keys;
            this.words = words;
            this.fontScale = fontScale;
        }
    }
}
