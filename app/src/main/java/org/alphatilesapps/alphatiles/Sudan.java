package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.settingsList;
import static org.alphatilesapps.alphatiles.Start.syllableAudioIDs;
import static org.alphatilesapps.alphatiles.Start.syllableHashMap;
import static org.alphatilesapps.alphatiles.Start.tileHashMap;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;

import java.util.ArrayList;
import java.util.List;

//To work on:
//Do any languages have more than 49 tiles? - now can scroll through multiple pages of 35 each (JP)
//Should the repeat image be hidden? - need it to scroll through pages now (JP)
//How should we color tiles that are multi-type?

//every time user clicks right arrow, go to next item in pagesList and display it
//every time user clicks left arrow, go to previous item in pagesList and display it
//look at Romania for an example

public class Sudan extends GameActivity {

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63", "#6200EE"};

    // will contain, for example:
    // List page 1 (which will contain <= 35 tiles or syllables)
    // List page 2, etc.
    List<List<String>> pagesList = new ArrayList<List<String>>();
    List<String> page = new ArrayList<>();

    int numPages = 0;
    int currentPageNumber = 0; //increment whenever user clicks right arrow; decrement whenever user clicks left arrow
    //use as index for pagesList

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
            R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
            R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35
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
        try{
//          audioInstructionsResID = res.getIdentifier("sudan_" + challengeLevel, "raw", context.getPackageName());
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        }
        catch (NullPointerException e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        // TO DO: TEST THIS WITH A LANGUAGE THAT DOESN'T HAVE INSTRUCTION AUDIO, CONNECT BACK ARROW
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.sudanCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);
    }

    Boolean differentiateTypes;
  
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.sudan);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
        }

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel  + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);

        //View repeatArrow = findViewById(R.id.repeatImage);
        //repeatArrow.setVisibility(View.INVISIBLE);

        String differentiateTypesSetting = settingsList.find("Differentiates types of multitype symbols");
        if(differentiateTypesSetting.compareTo("") != 0){
            differentiateTypes = Boolean.parseBoolean(differentiateTypesSetting);
        }
        else{
            differentiateTypes = false;
        }

        determineNumPages(); //JP

        if(syllableGame.equals("S")){
            splitSyllablesListAcrossPages();
            showCorrectNumSyllables(0);
        }else{
            splitTileListAcrossPages();
            showCorrectNumTiles(0);
        }

        setTextSizes();

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

    }

    public void determineNumPages(){
        pagesList.add(page);
        if (syllableGame.equals("S")){
            int total = syllableList.size() - 35; // 1 page is accounted for in numPages init
            while (total >= 0){
                numPages++;
                List<String> page = new ArrayList<>();
                pagesList.add(page); //add another page(list of syllables) to list
                total = total - 35;
            }
        }else{
            int total = tileList.size() - 35; // 1 page is accounted for in numPages init
            while (total >= 0){
                numPages++;
                List<String> page = new ArrayList<>();
                pagesList.add(page); //add another page(list of tiles) to list
                total = total - 35;
            }
        }
    }

    public void splitTileListAcrossPages(){
        int numTiles = tileList.size();
        int cont = 0;
        for (int i = 0; i < numPages + 1; i++){
            for (int j = 0; j < TILE_BUTTONS.length; j++){
                if (cont < numTiles){
                    pagesList.get(i).add(tileList.get(cont).baseTile);
                }
                cont++;
            }
        }
    }

    public void splitSyllablesListAcrossPages(){
        int numSylls = syllableList.size();
        int cont = 0;
        for (int i = 0; i < numPages + 1; i++){
            for (int j = 0; j < TILE_BUTTONS.length; j++){
                if (cont < numSylls){
                    pagesList.get(i).add(syllableList.get(cont).syllable);
                }
                cont++;
            }
        }
    }

    public void showCorrectNumTiles(int page){

        if(differentiateTypes){
            showCorrectNumTiles1PerSymbolAndType(page);
        }
        else {
            showCorrectNumTiles1PerSymbol(page);
        }

    }

    public void showCorrectNumTiles1PerSymbolAndType(int page){
        // visibleTiles must now be <= 35
        // if tileList.size() > 35, the rest will go on next page
        //visibleTiles = pagesList.get(page).size();
        visibleTiles = 0;

        for(int tileListLine = 0; tileListLine < tileList.size();  tileListLine++){

            Start.Tile t = tileList.get(tileListLine);

            TextView tileView = findViewById(TILE_BUTTONS[visibleTiles]);
            tileView.setText(t.baseTile);
            visibleTiles++;

            String type = t.tileType;
            String typeColor = COLORS[1];
            switch(type){
                case "C":
                    typeColor = COLORS[1];
                    break;
                case "V":
                    typeColor = COLORS[4];
                    break;
                case "X":
                    typeColor = COLORS[3];
                    break;
                default:
            }
            int tileColor = Color.parseColor(typeColor);
            tileView.setBackgroundColor(tileColor);

            if(t.tileTypeB.compareTo("none") != 0){
                tileView = findViewById(TILE_BUTTONS[visibleTiles]);
                tileView.setText(t.baseTile);
                visibleTiles++;

                String typeB = t.tileTypeB;
                String typeColorB = COLORS[1];
                switch(typeB){
                    case "C":
                        typeColorB = COLORS[1];
                        break;
                    case "V":
                        typeColorB = COLORS[4];
                        break;
                    case "X":
                        typeColorB = COLORS[3];
                        break;
                    default:
                }
                int tileColorB = Color.parseColor(typeColorB);
                tileView.setBackgroundColor(tileColorB);

            }

            if(t.tileTypeC.compareTo("none") != 0){

                tileView = findViewById(TILE_BUTTONS[visibleTiles]);
                tileView.setText(t.baseTile);
                visibleTiles++;

                String typeC = t.tileTypeC;
                String typeColorC = COLORS[1];
                switch(typeC){
                    case "C":
                        typeColorC = COLORS[1];
                        break;
                    case "V":
                        typeColorC = COLORS[4];
                        break;
                    case "X":
                        typeColorC = COLORS[3];
                        break;
                    default:
                }
                int tileColorC = Color.parseColor(typeColorC);
                tileView.setBackgroundColor(tileColorC);

            }

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

    public void showCorrectNumTiles1PerSymbol(int page){

        visibleTiles = pagesList.get(page).size();

        for (int k = 0; k < visibleTiles; k++)
        {
            TextView tile = findViewById(TILE_BUTTONS[k]);
            tile.setText(pagesList.get(page).get(k));
            String type = tileHashMap.find(pagesList.get(page).get(k)).tileType;
            String typeColor = COLORS[1];
            switch(type){
                case "C":
                    typeColor = COLORS[1];
                    break;
                case "V":
                    typeColor = COLORS[4];
                    break;
                case "X":
                    typeColor = COLORS[3];
                    break;
                default:
            }
            int tileColor = Color.parseColor(typeColor);
            tile.setBackgroundColor(tileColor);
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
        pixelHeight = (int) (0.5 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void showCorrectNumSyllables(int page) {

        visibleTiles = pagesList.get(page).size();

        for (int i = 0; i < visibleTiles; i++){
            TextView tile = findViewById(TILE_BUTTONS[i]);
            tile.setText(pagesList.get(page).get(i));
            String color = syllableHashMap.find(pagesList.get(page).get(i)).color;
            String typeColor = COLORS[Integer.parseInt(color)];
            int tileColor = Color.parseColor(typeColor);
            tile.setBackgroundColor(tileColor);
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

    public void nextPageArrow(View view){
        if (currentPageNumber < numPages){
            currentPageNumber++;
            if (syllableGame.equals("S")){
                showCorrectNumSyllables(currentPageNumber);
            }else{
                showCorrectNumTiles(currentPageNumber);
            }
        }
    }

    public void prevPageArrow(View view){
        if (currentPageNumber > 0){
            currentPageNumber--;
            if (syllableGame.equals("S")){
                showCorrectNumSyllables(currentPageNumber);
            }else{
                showCorrectNumTiles(currentPageNumber);
            }
        }
    }

    public void onBtnClick(View view) {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String tileText = "";
        int justClickedKey = Integer.parseInt((String)view.getTag());
        if (syllableGame.equals("S")){
            tileText = Start.syllableList.get(justClickedKey-1).syllable;


            gameSounds.play(syllableAudioIDs.get(tileText), 1.0f, 1.0f, 2, 0, 1.0f);
            soundSequencer.postDelayed(new Runnable()
            {
                public void run()
                {
                    if (repeatLocked)
                    {
                        setAllTilesClickable();
                    }
                    setOptionsRowClickable();
                }

            }, 925);
        }else{
            if(!differentiateTypes){//Not differentiating the uses of multifunction tiles
                tileText = tileList.get(justClickedKey-1).baseTile;
            }
            else{ //differentiateMultipleTypes ==2,we ARE differentiating the uses of multifunction tiles
                tileText = Start.tileListWithMultipleTypes.get(justClickedKey-1);
            }

            gameSounds.play(tileAudioIDs.get(tileText), 1.0f, 1.0f, 2, 0, 1.0f);
            soundSequencer.postDelayed(new Runnable()
            {
                public void run()
                {
                    if (repeatLocked)
                    {
                        setAllTilesClickable();
                    }
                    setOptionsRowClickable();
                }

            }, 925);
        }
    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }
}
