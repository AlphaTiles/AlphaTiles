package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class Iraq extends GameActivity { 

    // 5x7 grid (35 tiles per page)
    protected static final int[] GAME_BUTTONS = {
        R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05,
        R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
        R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15,
        R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
        R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25,
        R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
        R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35
    };

    final int tilesPerPage = 35;
    int numPages = 0;
    int currentPageNumber = 0;
    List<List<Start.Tile>> tilePagesLists = new ArrayList<>();
    Handler handler = new Handler();
    boolean isAnimating = false;
    int scanSetting = 1;

    private static final Logger LOGGER = Logger.getLogger(Iraq.class.getName());

    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
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
        if (instructionsButton != null) { //added if there is no view with this ID 
            instructionsButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.iraq_cl1);
        int gameID = R.id.iraqcl;
        scanSetting = Integer.parseInt(Start.settingsList.find("Game 001 Scan Setting"));

        splitTileListAcrossPages();
        showCorrectNumTiles(currentPageNumber);

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView nextSet = findViewById(R.id.nextSet);
            ImageView previousSet = findViewById(R.id.previousSet);
            previousSet.setRotationY(180);
            instructionsImage.setRotationY(180);
            nextSet.setRotationY(180);
            fixConstraintsRTL(gameID);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }
        showOrHideScrollingArrows();
    }

    protected void fixConstraintsRTL(int gameID) {
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.applyTo(constraintLayout);
    }

    private void splitTileListAcrossPages() {
        // Alphabetical order, skip SAD and SILENT_PLACEHOLDER_CONSONANTS
        List<Tile> allTiles = new ArrayList<>();
        for (Tile t : cumulativeStageBasedTileList) {
            if (!SAD.contains(t) && !SILENT_PLACEHOLDER_CONSONANTS.contains(t)) {
                allTiles.add(t);
            }
        }
        // Sort alphabetically by tile.text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(allTiles, Comparator.comparing(t -> t.text));
        }
        int totalTiles = allTiles.size();
        numPages = (totalTiles - 1) / tilesPerPage;
        tilePagesLists.clear();
        for (int i = 0; i <= numPages; i++) {
            int start = i * tilesPerPage;
            int end = Math.min(start + tilesPerPage, totalTiles);
            tilePagesLists.add(new ArrayList<>(allTiles.subList(start, end)));
        }
    }

    private void setPageArrowsClickable(boolean clickable) {
        ImageView nextPageArrow = findViewById(R.id.nextSet);
        ImageView prevPageArrow = findViewById(R.id.previousSet);
        nextPageArrow.setClickable(clickable);
        prevPageArrow.setClickable(clickable);
    }

    private void showCorrectNumTiles(int page) {
        List<Tile> allTiles = new ArrayList<>();
        for (List<Tile> pageTiles : tilePagesLists) {
            allTiles.addAll(pageTiles);
        }

        List<Tile> displayTiles = new ArrayList<>();
        int startIdx = page * tilesPerPage;
        for (Tile tile : allTiles) {
            // Only filter duplicates if differentiatesTileTypes is false
            if (!differentiatesTileTypes && MULTITYPE_TILES.contains(tile.text)) {
                boolean alreadyShown = false;
                for (Tile shown : displayTiles) {
                    if (shown.text.equals(tile.text)) {
                        alreadyShown = true;
                        break;
                    }
                }
                if (alreadyShown) {
                    continue;
                }
            }
            displayTiles.add(tile);
        }

        // for the current page, show the correct slice of displayTiles
        int buttonIdx = 0;
        for (int i = startIdx; i < Math.min(startIdx + tilesPerPage, displayTiles.size()); i++, buttonIdx++) {
            Tile tile = displayTiles.get(i);
            TextView tileView = findViewById(GAME_BUTTONS[buttonIdx]);
            tileView.setText(tile.text);
            String type = tile.typeOfThisTileInstance;
            String typeColor;
            switch (type) {
                case "C":
                    typeColor = colorList.get(1);
                    break;
                case "V":
                    typeColor = colorList.get(2);
                    break;
                case "T":
                    typeColor = colorList.get(3);
                    break;
                default:
                    typeColor = colorList.get(4);
                    break;
            }
            int tileColor = Color.parseColor(typeColor);
            tileView.setBackgroundColor(tileColor);
            tileView.setVisibility(View.VISIBLE);
            tileView.setClickable(true);
        }
        // Hide remaining buttons
        for (; buttonIdx < GAME_BUTTONS.length; buttonIdx++) {
            TextView tileView = findViewById(GAME_BUTTONS[buttonIdx]);
            tileView.setVisibility(View.INVISIBLE);
            tileView.setClickable(false);
        }
        setPageArrowsClickable(true);
    }

    public void nextPageArrow(View view) {
        if (!view.isClickable()) return;
        if (currentPageNumber < numPages) {
            currentPageNumber++;
            showCorrectNumTiles(currentPageNumber);
            showOrHideScrollingArrows();
        }
    }

    public void prevPageArrow(View view) {
        if (!view.isClickable()) return;
        if (currentPageNumber > 0) {
            currentPageNumber--;
            showCorrectNumTiles(currentPageNumber);
            showOrHideScrollingArrows();
        }
    }

    public void showOrHideScrollingArrows() {
        ImageView nextPageArrow = findViewById(R.id.nextSet);
        ImageView prevPageArrow = findViewById(R.id.previousSet);
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
        setPageArrowsClickable(true);
    }

    public void onBtnClick(View view) {
        try {
            if (isAnimating) {
                return;
            }
            isAnimating = true;
            setAllGameButtonsUnclickable();
            setOptionsRowUnclickable();
            setPageArrowsClickable(false);

            int tileIndex = Integer.parseInt((String) view.getTag()) - 1;

            // Build the filtered displayTiles list exactly as in showCorrectNumTiles
            List<Tile> allTiles = new ArrayList<>();
            for (List<Tile> pageTiles : tilePagesLists) {
                allTiles.addAll(pageTiles);
            }
            List<Tile> displayTiles = new ArrayList<>();
            for (Tile tile : allTiles) {
                if (!differentiatesTileTypes && MULTITYPE_TILES.contains(tile.text)) {  
                    boolean alreadyShown = false;
                    for (Tile shown : displayTiles) {
                        if (shown.text.equals(tile.text)) {
                            alreadyShown = true;
                            break;
                        }
                    }
                    if (alreadyShown) {
                        continue;
                    }
                }
                displayTiles.add(tile);
            }
            int startIdx = currentPageNumber * tilesPerPage;
            int displayIdx = startIdx + tileIndex;
            if (displayIdx < 0 || displayIdx >= displayTiles.size()) {
                isAnimating = false;
                setAllGameButtonsClickable();
                setOptionsRowClickable();
                setPageArrowsClickable(true);
                return;
            }
            Tile thisTile = displayTiles.get(displayIdx);

            int tileAudioId = tileAudioIDs.get(thisTile.audioForThisTileType);
            int tileAudioDuration = tileDurations.get(thisTile.audioForThisTileType);

            TextView tileView = (TextView) view;
            String originalText = tileView.getText().toString();
            int originalColor = ((ColorDrawable) tileView.getBackground()).getColor();

            // Step 1: Play tile audio
            gameSounds.play(tileAudioId, 1.0f, 1.0f, 2, 0, 1.0f);

            // Step 2: Wait 0.5s, then show a random image according to the scan setting
            handler.postDelayed(() -> {
                 try {
                // iconic word logic
                if (challengeLevel == 2 && thisTile.iconicWord != null && !thisTile.iconicWord.isEmpty() && !thisTile.iconicWord.equals("-")) {
                    // Use only the iconic word for this tile
                    Start.Word iconicWordObj = null;
                    for (Start.Word w : Start.wordList) {
                        if (w.wordInLOP != null && thisTile.iconicWord != null &&
                            w.wordInLOP.trim().equalsIgnoreCase(thisTile.iconicWord.trim())) {
                            iconicWordObj = w;
                            break;
                        }
                    }
                    if (iconicWordObj != null) {
                        String wordText = Start.wordList.stripInstructionCharacters(iconicWordObj.wordInLOP);
                        tileView.setText(wordText);
                        tileView.setBackgroundColor(Color.WHITE);

                        // Show the image
                        ImageView wordImage = new ImageView(context);
                        int resID = context.getResources().getIdentifier(iconicWordObj.wordInLWC, "drawable", context.getPackageName());
                        if (resID != 0) {
                            wordImage.setImageResource(resID);
                            ConstraintLayout layout = (ConstraintLayout) tileView.getParent();
                            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(tileView.getWidth(), tileView.getHeight());
                            params.leftToLeft = tileView.getId();
                            params.topToTop = tileView.getId();
                            wordImage.setLayoutParams(params);
                            wordImage.setId(View.generateViewId());
                            layout.addView(wordImage);
                            wordImage.bringToFront();

                            tileView.setText("");

                            handler.postDelayed(() -> {
                                try {
                                    layout.removeView(wordImage);
                                    tileView.setText(wordText);
                                } catch (Exception e) { }
                            }, 2000);
                        }

                        // Play the word audio
                        if (iconicWordObj.wordInLWC != null && wordAudioIDs.containsKey(iconicWordObj.wordInLWC)) {
                            int wordAudioId = wordAudioIDs.get(iconicWordObj.wordInLWC);
                            gameSounds.play(wordAudioId, 1.0f, 1.0f, 2, 0, 1.0f);
                        }
                    } else {
                        // fallback: just show the tile text
                        tileView.setText(originalText);
                        tileView.setBackgroundColor(Color.WHITE);
                        tileView.setTextColor(originalColor);
                    }
                } else {
                    Start.Word[] groupOfWordsForActiveTile = null;
                    int groupCount;
                    boolean skipThisTile = false;

                    switch (scanSetting) {
                        case 2:
                            groupCount = Start.wordList.numberOfWordsForActiveTile(thisTile, 1);
                            if (groupCount > 0) {
                                groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(thisTile, groupCount, 1);
                            } else {
                                groupCount = Start.wordList.numberOfWordsForActiveTile(thisTile, 2);
                                if (groupCount > 0) {
                                    groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(thisTile, groupCount, 2);
                                } else {
                                    skipThisTile = true;
                                }
                            }
                            break;
                        case 3:
                            groupCount = Start.wordList.numberOfWordsForActiveTile(thisTile, 3);
                            if (groupCount > 0) {
                                groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(thisTile, groupCount, 3);
                            } else {
                                skipThisTile = true;
                            }
                            break;
                        default:
                            groupCount = Start.wordList.numberOfWordsForActiveTile(thisTile, 1);
                            if (groupCount > 0) {
                                groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(thisTile, groupCount, 1);
                            } else {
                                skipThisTile = true;
                            }
                            break;
                    }

                    if (!skipThisTile && groupOfWordsForActiveTile != null && groupOfWordsForActiveTile.length > 0) {
                        int randIdx = (int) (Math.random() * groupOfWordsForActiveTile.length);
                        Start.Word chosenWord = groupOfWordsForActiveTile[randIdx];
                        String wordText = Start.wordList.stripInstructionCharacters(chosenWord.wordInLOP);

                        // Show the word in the tile
                        tileView.setText(wordText);
                        tileView.setBackgroundColor(Color.WHITE);

                        // Show the image
                        ImageView wordImage = new ImageView(context);
                        int resID = context.getResources().getIdentifier(chosenWord.wordInLWC, "drawable", context.getPackageName());
                        if (resID != 0) {
                            wordImage.setImageResource(resID);
                            ConstraintLayout layout = (ConstraintLayout) tileView.getParent();
                            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(tileView.getWidth(), tileView.getHeight());
                            params.leftToLeft = tileView.getId();
                            params.topToTop = tileView.getId();
                            wordImage.setLayoutParams(params);
                            wordImage.setId(View.generateViewId());
                            layout.addView(wordImage);
                            wordImage.bringToFront();

                            // Hide the word text while the image is shown
                            tileView.setText("");

                            // Remove the image after 2 seconds and restore the word text
                            handler.postDelayed(() -> {
                                try {
                                    layout.removeView(wordImage);
                                    tileView.setText(wordText);
                                } catch (Exception e) { }
                            }, 2000);
                        }

                        // Play the word audio (no duration logic, just play)
                        if (chosenWord.wordInLWC != null && wordAudioIDs.containsKey(chosenWord.wordInLWC)) {
                            int wordAudioId = wordAudioIDs.get(chosenWord.wordInLWC);
                            gameSounds.play(wordAudioId, 1.0f, 1.0f, 2, 0, 1.0f);
                        }
                    } else {
                        // fallback: just show the tile text
                        tileView.setText(originalText);
                        tileView.setBackgroundColor(Color.WHITE);
                        tileView.setTextColor(originalColor);
                    }
                } 
            }catch (Exception e) { }

                // Step 3: Wait 2 seconds, then restore tile
                handler.postDelayed(() -> {
                    try {
                        tileView.setText(originalText);
                        tileView.setBackgroundColor(originalColor);
                        tileView.setTextColor(Color.WHITE);
                        setAllGameButtonsClickable();
                        setOptionsRowClickable();
                        setPageArrowsClickable(true);
                        isAnimating = false;
                    } catch (Exception e) {
                        // ignore
                    }
                }, 2000);

            }, tileAudioDuration + 500); // Wait for tile audio + 0.5 second
        } catch (Exception e) {
            isAnimating = false;
            setAllGameButtonsClickable();
            setOptionsRowClickable();
            setPageArrowsClickable(true);
        }
    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }
}
