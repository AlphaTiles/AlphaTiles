package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Collections;
import java.util.Random;

public class Georgia extends GameActivity {

    Start.TileList sortableTilesArray; // KP
    String initialTile = "";
    int visibleTiles; // will be 6, 12 or 18 based on challengeLevel 1, 2 or 3

    private static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18
    };

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.georgia);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        setTitle(Start.localAppName + ": " + gameNumber);

        switch (challengeLevel) {
            case 2:
                visibleTiles = 12;
                break;
            case 3:
                visibleTiles = 18;
                break;
            default:
                visibleTiles = 6;
        }

        sortableTilesArray = (Start.TileList)Start.tileList.clone(); // KP

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        setTextSizes();

        playAgain();

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        int pixelHeight = 0;
        double scaling = 0.45;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int t = 0; t < TILE_BUTTONS.length; t++) {

            TextView gameTile = findViewById(TILE_BUTTONS[t]);
            if (t == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) gameTile.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            gameTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        }

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.7 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        if (mediaPlayerIsPlaying) {
            return;
        }

        repeatLocked = true;
        Collections.shuffle(sortableTilesArray); // KP
        chooseWord();
        setAllTilesUnclickable();
        setOptionsRowUnclickable();
        setUpTiles();
        playActiveWordClip();
        setAllTilesClickable();
        setOptionsRowClickable();

    }

    private void chooseWord() {

        Random rand = new Random();
        int randomNum = rand.nextInt(Start.wordList.size()); // KP

        wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
        wordInLOP = Start.wordList.get(randomNum).localWord; // KP

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP); // KP
        initialTile = parsedWordArrayFinal.get(0);

    }

    private void setUpTiles() {

        boolean correctTileRepresented = false;

        for (int t = 0; t < TILE_BUTTONS.length; t++ ) {

            TextView gameTile = findViewById(TILE_BUTTONS[t]);

            if (sortableTilesArray.get(t).baseTile.equals(initialTile) && t < visibleTiles) {
                correctTileRepresented = true;
            }

            String tileColorStr = COLORS[t % 5];
            int tileColor = Color.parseColor(tileColorStr);

            if (t < visibleTiles) {
                gameTile.setText(sortableTilesArray.get(t).baseTile); // KP
                gameTile.setBackgroundColor(tileColor);
                gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
                    gameTile.setVisibility(View.VISIBLE);
            } else {
                gameTile.setText(String.valueOf(t + 1));
                gameTile.setBackgroundResource(R.drawable.textview_border);
                gameTile.setTextColor(Color.parseColor("#000000")); // black
                gameTile.setClickable(false);
                gameTile.setVisibility(View.INVISIBLE);
            }

        }

        if (!correctTileRepresented) {

            // If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles

            Random rand = new Random();
            int randomNum = rand.nextInt(visibleTiles - 1); // KP
            TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
            gameTile.setText(initialTile);

        }

    }

}
