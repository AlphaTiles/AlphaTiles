package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.colorList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.sendAnalytics;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;
import static org.alphatilesapps.alphatiles.Start.tileDurations;
import static org.alphatilesapps.alphatiles.Start.wordList;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cameroon extends GameActivity {
    boolean useImage = false;
    boolean substituteAll = false;
    boolean useDistractors = false;
    int correct = -1;
    String correctString = "";
    ArrayList<LinearLayout> rows = new ArrayList<>();
    List<List<Start.Tile>> words;
    int[] selectors = {R.id.selector0, R.id.selector1, R.id.selector2, R.id.selector3};
    final int selectorColor = Color.parseColor(colorList.get(5));
    final int selectorColorCorrect = Color.parseColor(colorList.get(3));
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        context = this;
        // challengeLevel has three slots, each with a 1 or 2, i.e. 111 = use image + substitute one random slot + substitute with
        useImage = challengeLevel / 100 != 2; // slot 1: TRUE = image visible, FALSE = image hidden
        substituteAll = (challengeLevel / 10) % 10 == 2; // slot 2: TRUE = substitute all, FALSE = substitute one
        useDistractors = challengeLevel % 10 == 2; // slot 3: TRUE = substitute with distractors, FALSE = substitute with random
        setContentView(R.layout.cameroon);
        ActivityLayouts.applyEdgeToEdge(this, R.id.cameroonCL);
        ActivityLayouts.setStatusAndNavColors(this);
        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView repeatImage = findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            fixConstraintsRTL(R.id.cameroonCL);
        }
        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }
        if (!useImage) {
            findViewById(R.id.wordImage).setVisibility(View.GONE);
        } else {
            findViewById(R.id.wordImage).setClickable(true);
        }
        updatePointsAndTrackers(0);
        for (int id : new int[]{R.id.rowlayout0, R.id.rowlayout1, R.id.rowlayout2, R.id.rowlayout3}) {
            rows.add(findViewById(id));
        }

        int color = Color.parseColor(colorList.get(5));

        for (int idx = 0; idx < 4; idx++) {
            View selector = findViewById(selectors[idx]);
            int i = idx;
            selector.setOnClickListener((view) -> onSelector(selector, i));
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(color);
            selector.setBackground(circle);
        }
        playAgain();

    }

    private void onSelector(View selector, int idx) {
        if(!repeatLocked) return;
        if (idx == correct) {
            GradientDrawable circle = (GradientDrawable) selector.getBackground();
            circle.setColor(selectorColorCorrect);
            repeatLocked = false;
            setAdvanceArrowToBlue();
            updatePointsAndTrackers(1);
            if (sendAnalytics) {
                String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
                Properties info = new Properties().putValue("Time Taken", System.currentTimeMillis() - levelBegunTime)
                        .putValue("Number Incorrect", incorrectOnLevel)
                        .putValue("Correct Answer", correctString)
                        .putValue("Grade", studentGrade);
                for (int i = 0; i < 4; i++) {
                    if (!incorrectAnswersSelected.get(i).isEmpty()) {
                        info.putValue("Incorrect_" + (i + 1), incorrectAnswersSelected.get(i));
                    }
                }
                Analytics.with(context).track(gameUniqueID, info);
            }
            playCorrectSoundThenActiveWordClip(false);
        } else {
            // TODO: this won't work for Thai, but the method for combining Thai characters only works when only one tile is changed
            StringBuilder incorrect = new StringBuilder();
            for (Start.Tile tile : words.get(idx)) {
                incorrect.append(tile.text);
            }
            incorrectAnswersSelected.set(idx, incorrect.toString());
        }
    }

    private List<Start.Tile> tilesOfType(String type) {
        switch (type) {
            case "V":
                return Start.VOWELS;
            case "C":
                return Start.CONSONANTS;
            case "T":
                return Start.TONES;
            default:
                return null;
        }
    }

    private Start.Tile findTile(String text, String type) {
        Start.Tile fallback = null;
        for (Start.Tile tile : Start.tileList) {
            if (fallback == null && tile.text.equals(text))
                fallback = tile;
            if (tile.text.equals(text) && tile.typeOfThisTileInstance.equals(type))
                return tile;
        }
        return fallback;
    }

    private <T> List<T> choose(List<T> list, int n) {
        if (n > list.size()) return null;
        Random rand = new Random();
        List<Integer> indices = new ArrayList<>();
        List<T> out = new ArrayList<>();
        while (indices.size() < n) {
            while (true) {
                int x = rand.nextInt(list.size());
                if (indices.contains(x)) continue;
                indices.add(x);
                out.add(list.get(x));
                break;
            }
        }
        return out;
    }

    private List<Start.Tile> replacementTiles(Start.Tile tile, boolean useDistractors) {
        List<Start.Tile> tiles;
        String type = tile.typeOfThisTileInstance;
        if (useDistractors) {
            tiles = new ArrayList<>();
            // This is a little weird because we throw it out half the time, but it makes it simpler :sob:
            tiles.add(tile);
            for (String distractor : tile.distractors) {
                tiles.add(findTile(distractor, type));
            }
        } else {
            tiles = tilesOfType(type);
        }
        return tiles;
    }

    private List<List<Start.Tile>> mutateWord(ArrayList<Start.Tile> word) {
        List<List<Start.Tile>> rows = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            List<Start.Tile> row = new ArrayList<>(word);
            rows.add(row);
        }
        if (substituteAll) {
            // Pick one column to be all different so that the other columns can choose anything
            int col;
            Start.Tile tile;
            List<Start.Tile> tiles;
            do {
                col = rand.nextInt(word.size());
                tile = word.get(col);
                tiles = replacementTiles(tile, useDistractors);
            } while (tiles == null || tiles.size() < 4);
            // Draw 4 so that if we accidentally draw the original tile we can throw it out and still have 3
            List<Start.Tile> choice = choose(tiles, 4);
            for (int row = 0; row < 3; row++) {
                Start.Tile replacement;
                do {
                    replacement = choice.remove(0);
                } while (replacement.text.equals(tile.text));
                rows.get(row).set(col, replacement);
            }
            for (int c = 0; c < word.size(); c++) {
                if (c == col) continue;
                List<Start.Tile> replacements = replacementTiles(word.get(c), useDistractors);
                // Non-replaceable tile type
                if(replacements == null) continue;
                for (int row = 0; row < 3; row++) {
                    // Fine to draw original
                    Start.Tile replacement = replacements.get(rand.nextInt(replacements.size()));
                    rows.get(row).set(c, replacement);
                }
            }
        } else {
            List<List<String>> taken = new ArrayList<>();
            for(int col = 0; col < word.size(); col++) {
                taken.add(new ArrayList<>());
            }
            for(int row = 0; row < 3; row++) {
                int col = rand.nextInt(word.size());
                List<Start.Tile> replacements = replacementTiles(word.get(col), useDistractors);
                Start.Tile tile;
                while(true) {
                    tile = replacements.get(rand.nextInt(replacements.size()));
                    if(taken.get(col).contains(tile.text) || tile.text.equals(word.get(col).text)) continue;
                    taken.get(col).add(tile.text);
                    break;
                }
                rows.get(row).set(col, tile);
            }
        }
        return rows;
    }

    public void playAgain() {
        for (int idx = 0; idx < 4; idx++) {
            View selector = findViewById(selectors[idx]);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(selectorColor);
            selector.setBackground(circle);
        }
        incorrectAnswersSelected = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            incorrectAnswersSelected.add("");
        }
        repeatLocked = true;
        setAdvanceArrowToGray();
        do {
            chooseWord();
            parsedRefWordTileArray = Start.tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
        } while (parsedRefWordTileArray.size() > 7);
        if (useImage) {
            int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
            ImageView wordImage = findViewById(R.id.wordImage);
            wordImage.setImageResource(resID);
        }
        TextView text = findViewById(R.id.wordText);
        text.setText(wordList.stripInstructionCharacters(refWord.wordInLOP));
        correct = new Random().nextInt(4);
        words = mutateWord(parsedRefWordTileArray);
        words.add(correct, parsedRefWordTileArray);
        correctString = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
        for (int row = 0; row < 4; row++) {
            LinearLayout layout = rows.get(row);
            layout.removeAllViews();
            List<Start.Tile> word = words.get(row);
            for (int col = 0; col < word.size(); col++) {
                layout.addView(tileButton(word.get(col)));
            }
        }

        playActiveWordClip(false);
        setAllGameButtonsClickable();
        setOptionsRowClickable();

    }

    private ImageView tileButton(Start.Tile tile) {
        ImageView view = new ImageView(context);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        view.setLayoutParams(lparams);
        view.setPadding(10, 10, 10, 10);
        view.setImageResource(R.drawable.zz_click_for_tile_audio_simple);
        view.setOnClickListener((v) -> {
            setAllGameButtonsUnclickable();
            setOptionsRowUnclickable();
            try {
                int audioId = tileAudioIDs.get(tile.audioForThisTileType);
                int duration = tileDurations.get(tile.audioForThisTileType);

                gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);
                soundSequencer.postDelayed(() -> {
                    if (repeatLocked) {
                        setAllGameButtonsClickable();
                    }
                    setOptionsRowClickable();
                }, duration);
            } catch (NullPointerException ignored) {

            }
        });
        return view;
    }

    public void repeatGame(View View) {
        if (!repeatLocked && !mediaPlayerIsPlaying) {
            playAgain();
        }
    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    @Override
    protected int[] getGameButtons() {
        return new int[]{R.id.selector0, R.id.selector1, R.id.selector2, R.id.selector3};
    }

    @Override
    protected int[] getWordImages() {
        return null;
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
        ImageView instructionsButton = findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);
    }
}