package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.*;

import java.util.ArrayList;
import java.util.List;

public class Sudan extends GameActivity {

    // will contain, for example:
    // List page 1 (which will contain <= 35 tiles or syllables)
    // List page 2, etc.
    List<List<String>> pagesList = new ArrayList<List<String>>();
    List<String> page = new ArrayList<>();

    int numPages = 0;
    int currentPageNumber = 0;


    protected static final int[] SYLL_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
            R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
            R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35
    };

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
            R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
            R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35, R.id.tile36, R.id.tile37, R.id.tile38, R.id.tile39, R.id.tile40,
            R.id.tile41, R.id.tile42, R.id.tile43, R.id.tile44, R.id.tile45, R.id.tile46, R.id.tile47, R.id.tile48, R.id.tile49, R.id.tile50,
            R.id.tile51, R.id.tile52, R.id.tile53, R.id.tile54, R.id.tile55, R.id.tile56, R.id.tile57, R.id.tile58, R.id.tile59, R.id.tile60,
            R.id.tile61, R.id.tile62, R.id.tile63
    };

    @Override
    protected int[] getTileButtons() {
        return TILE_BUTTONS;
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
            audioInstructionsResID = res.getIdentifier(gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        // TO DO: TEST THIS WITH A LANGUAGE THAT DOESN'T HAVE INSTRUCTION AUDIO, CONNECT BACK ARROW
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID;
        if (syllableGame.equals("S")) {
            gameID = R.id.sudansyllCL;
        } else {
            gameID = R.id.sudanCL;
        }

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        int gameID = 0;
        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");
        determineNumPages(); // JP

        if (syllableGame.equals("S")) {
            setContentView(R.layout.sudan_syll);
            gameID = R.id.sudansyllCL;
            splitSyllablesListAcrossPages();
            showCorrectNumSyllables(0);
        } else {
            setContentView(R.layout.sudan);
            gameID = R.id.sudanCL;
            splitTileListAcrossPages();
            showCorrectNumTiles(0);
        }

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            ImageView repeatImage2 = (ImageView) findViewById(R.id.repeatImage2);
            repeatImage2.setRotationY(180);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);

            fixConstraintsRTLSudan(gameID);
        }

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }
    }

    private void fixConstraintsRTLSudan(int gameID) {
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.repeatImage2, ConstraintSet.END, R.id.gamesHomeImage, ConstraintSet.START, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.START, R.id.repeatImage2, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
        constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.instructions, ConstraintSet.START, 0);
        constraintSet.connect(R.id.repeatImage, ConstraintSet.START, R.id.instructions, ConstraintSet.END, 0);
        constraintSet.connect(R.id.instructions, ConstraintSet.END, R.id.repeatImage, ConstraintSet.START, 0);
        constraintSet.applyTo(constraintLayout);
    }

    public void determineNumPages() {

        pagesList.add(page);
        if (syllableGame.equals("S")) {
            int total = syllableList.size() - SYLL_BUTTONS.length; // 1 page is accounted for in numPages init
            while (total >= 0) {
                numPages++;
                List<String> page = new ArrayList<>();
                pagesList.add(page); //add another page(list of syllables) to list
                total = total - SYLL_BUTTONS.length;
            }
        } else {
            int total = 0;
            total = cumulativeStageBasedTileList.size() - TILE_BUTTONS.length;
            while (total >= 0) {
                numPages++;
                List<String> page = new ArrayList<>();
                pagesList.add(page); //add another page(list of tiles) to list
                total = total - TILE_BUTTONS.length;
            }
        }
    }

    public void splitTileListAcrossPages() {
        int numTiles = cumulativeStageBasedTileList.size() - SAD.size();
        int cont = 0;
        for (int i = 0; i < numPages + 1; i++) {
            for (int j = 0; j < TILE_BUTTONS.length; j++) {
                if (cont < numTiles && !SAD.contains(cumulativeStageBasedTileList.get(cont))) {
                    pagesList.get(i).add(cumulativeStageBasedTileList.get(cont));
                }
                cont++;
            }
        }
    }

    public void splitSyllablesListAcrossPages() {
        int numSylls = syllableList.size();
        int cont = 0;
        for (int i = 0; i < numPages + 1; i++) {
            for (int j = 0; j < SYLL_BUTTONS.length; j++) {
                if (cont < numSylls) {
                    pagesList.get(i).add(syllableList.get(cont).syllable);
                }
                cont++;
            }
        }
    }
    public void showCorrectNumTiles(int page) {

        visibleTiles = pagesList.get(page).size();

        for (int k = 0; k < visibleTiles; k++) {
            TextView tileView = findViewById(TILE_BUTTONS[k]);
            if (pagesList.get(page).get(k).endsWith("B") || pagesList.get(page).get(k).endsWith("C")) {
                tileView.setText(pagesList.get(page).get(k).substring(0, pagesList.get(page).get(k).length() - 1));
            } else {
                tileView.setText(pagesList.get(page).get(k));
            }
            String type;
            if (differentiatesTileTypes) {
                type = tileTypeHashMapWithMultipleTypesNoSAD.get(pagesList.get(page).get(k));
            } else {
                type = tileHashMapNoSAD.find(pagesList.get(page).get(k)).tileType;
            }
            String typeColor;
            switch (type) {
                case "C":
                    typeColor = COLORS.get(1);
                    break;
                case "V":
                case "LV":
                case "AV":
                case "BV":
                case "FV":
                    typeColor = COLORS.get(0);
                    break;
                case "T":
                    typeColor = COLORS.get(3);
                    break;
                default:
                    typeColor = COLORS.get(4);
                    break;
            }
            int tileColor = Color.parseColor(typeColor);
            tileView.setBackgroundColor(tileColor);
        }

        for (int k = 0; k < TILE_BUTTONS.length; k++) {

            TextView key = findViewById(TILE_BUTTONS[k]);
            if (k < visibleTiles) {
                key.setVisibility(View.VISIBLE);
                key.setClickable(true);
            } else {
                key.setVisibility(View.INVISIBLE);
                key.setClickable(false);
            }
        }
    }

    public void showCorrectNumSyllables(int page) {

        visibleTiles = pagesList.get(page).size();

        for (int i = 0; i < visibleTiles; i++) {
            TextView tile = findViewById(SYLL_BUTTONS[i]);
            tile.setText(pagesList.get(page).get(i));
            String color = syllableHashMap.find(pagesList.get(page).get(i)).color;
            String typeColor = COLORS.get(Integer.parseInt(color));
            int tileColor = Color.parseColor(typeColor);
            tile.setBackgroundColor(tileColor);
        }

        for (int k = 0; k < SYLL_BUTTONS.length; k++) {

            TextView key = findViewById(SYLL_BUTTONS[k]);
            if (k < visibleTiles) {
                key.setVisibility(View.VISIBLE);
                if (hasSyllableAudio) {
                    key.setClickable(true);
                } else {
                    key.setClickable(false);
                }
            } else {
                key.setVisibility(View.INVISIBLE);
                key.setClickable(false);
            }
        }
    }

    public void nextPageArrow(View view) {
        if (currentPageNumber < numPages) {
            currentPageNumber++;
            if (syllableGame.equals("S")) {
                showCorrectNumSyllables(currentPageNumber);
            } else {
                showCorrectNumTiles(currentPageNumber);
            }
        }
    }

    public void prevPageArrow(View view) {
        if (currentPageNumber > 0) {
            currentPageNumber--;
            if (syllableGame.equals("S")) {
                showCorrectNumSyllables(currentPageNumber);
            } else {
                showCorrectNumTiles(currentPageNumber);
            }
        }
    }

    public void onBtnClick(View view) {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String viewText = pagesList.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
        int audioId = 0;
        int duration = 0;
        if (syllableGame.equals("S")) {
            audioId = syllableAudioIDs.get(viewText);
            duration = syllableDurations.get(viewText);

        } else {
            audioId = tileAudioIDs.get(viewText);
            duration = tileDurations.get(viewText);
        }

        gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllTilesClickable();
                }
                setOptionsRowClickable();
            }

        }, duration);
    }

    public void clickPicHearAudio(View view) {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }
}
