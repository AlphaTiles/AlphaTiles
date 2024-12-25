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
    List<List<Start.Tile>> tilePagesLists = new ArrayList<>();
    List<List<Start.Syllable>> syllablePagesLists = new ArrayList<>();
    List<Start.Tile> tilePage = new ArrayList<>();
    List<Start.Syllable> syllablePage = new ArrayList<>();
    int numPages = 0;
    int currentPageNumber = 0;

    final int syllablesPerPage = 35;
    final int tilesPerPage = 63;
    protected static final int[] GAME_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
            R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
            R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35, R.id.tile36, R.id.tile37, R.id.tile38, R.id.tile39, R.id.tile40,
            R.id.tile41, R.id.tile42, R.id.tile43, R.id.tile44, R.id.tile45, R.id.tile46, R.id.tile47, R.id.tile48, R.id.tile49, R.id.tile50,
            R.id.tile51, R.id.tile52, R.id.tile53, R.id.tile54, R.id.tile55, R.id.tile56, R.id.tile57, R.id.tile58, R.id.tile59, R.id.tile60,
            R.id.tile61, R.id.tile62, R.id.tile63
    };

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
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");
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

        ActivityLayouts.applyEdgeToEdge(this, gameID);
        ActivityLayouts.setStatusAndNavColors(this);

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
        showOrHideScrollingArrows();
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

        tilePagesLists.add(tilePage);
        syllablePagesLists.add(syllablePage);
        if (syllableGame.equals("S")) {
            int total = syllableList.size() - syllablesPerPage; // 1 page is accounted for in numPages init
            while (total >= 0) {
                numPages++;
                List<Start.Syllable> page = new ArrayList<>();
                syllablePagesLists.add(page); //add another page(list of syllables) to list
                total = total - syllablesPerPage;
            }
        } else {
            int total = cumulativeStageBasedTileList.size() - SAD.size() - SILENT_PLACEHOLDER_CONSONANTS.size() - tilesPerPage;
            while (total >= 0) {
                numPages++;
                List<Start.Tile> page = new ArrayList<>();
                tilePagesLists.add(page); //add another page(list of tiles) to list
                total = total - tilesPerPage;
            }
        }
    }

    public void splitTileListAcrossPages() {

        int numTiles = cumulativeStageBasedTileList.size() - SAD.size() - SILENT_PLACEHOLDER_CONSONANTS.size();
        int tileIndex = 0;
        for (int i = 0; i <= numPages; i++) {
            for (int j = 0; j < tilesPerPage; j++) {
                if(tileIndex < numTiles) {
                    Tile thisTile = cumulativeStageBasedTileList.get(tileIndex);
                    if (!SAD.contains(thisTile) && !SILENT_PLACEHOLDER_CONSONANTS.contains(thisTile)) {
                        tilePagesLists.get(i).add(thisTile);
                    }
                    tileIndex++;
                }
            }
        }

    }

    public void splitSyllablesListAcrossPages() {
        int numSyllables = syllableList.size();
        int syllableIndex = 0;
        for (int i = 0; i < numPages + 1; i++) {
            for (int j = 0; j < syllablesPerPage; j++) {
                if (syllableIndex < numSyllables) {
                    syllablePagesLists.get(i).add(syllableList.get(syllableIndex));
                }
                syllableIndex++;
            }
        }
    }
    public void showCorrectNumTiles(int page) {

        visibleGameButtons = tilePagesLists.get(page).size();

        for (int k = 0; k < visibleGameButtons; k++) {
            TextView tileView = findViewById(GAME_BUTTONS[k]);
            tileView.setText(tilePagesLists.get(page).get(k).text);
            String type = tilePagesLists.get(page).get(k).typeOfThisTileInstance;
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
        }

        for (int k = 0; k < tilesPerPage; k++) {

            TextView key = findViewById(GAME_BUTTONS[k]);
            if (k < visibleGameButtons) {
                key.setVisibility(View.VISIBLE);
                key.setClickable(true);
            } else {
                key.setVisibility(View.INVISIBLE);
                key.setClickable(false);
            }
        }
    }

    public void showCorrectNumSyllables(int page) {
        visibleGameButtons = syllablePagesLists.get(page).size();

        for (int i = 0; i < visibleGameButtons; i++) {
            TextView tile = findViewById(GAME_BUTTONS[i]);
            tile.setText(syllablePagesLists.get(page).get(i).text);
            String color = syllablePagesLists.get(page).get(i).color;
            String typeColor = colorList.get(Integer.parseInt(color));
            int tileColor = Color.parseColor(typeColor);
            tile.setBackgroundColor(tileColor);
        }

        for (int k = 0; k < syllablesPerPage; k++) {
            TextView key = findViewById(GAME_BUTTONS[k]);
            if (k < visibleGameButtons) {
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
            showOrHideScrollingArrows();
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
            showOrHideScrollingArrows();
        }
    }

    public void showOrHideScrollingArrows() {
        ImageView nextPageArrow = findViewById(R.id.repeatImage);
        ImageView prevPageArrow = findViewById(R.id.repeatImage2);
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
    }

    public void onBtnClick(View view) {
        setAllGameButtonsUnclickable();
        setOptionsRowUnclickable();

        int audioId;
        int duration;
        if (syllableGame.equals("S")) {
            Start.Syllable thisSyllable = syllablePagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
            audioId = syllableAudioIDs.get(thisSyllable.audioName);
            duration = thisSyllable.duration;

        } else {
            Start.Tile thisTile = tilePagesLists.get(currentPageNumber).get(Integer.parseInt((String) view.getTag())-1);
            audioId = tileAudioIDs.get(thisTile.audioForThisTileType);
            duration = tileDurations.get(thisTile.audioForThisTileType);
        }

        gameSounds.play(audioId, 1.0f, 1.0f, 2, 0, 1.0f);
        soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (repeatLocked) {
                    setAllGameButtonsClickable();
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
