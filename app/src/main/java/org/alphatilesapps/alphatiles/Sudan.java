package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.settingsList;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;

//To work on:
//Do any languages have more than 49 tiles?
//Should the repeat image be hidden?
//How should we color tiles that are multi-type?

public class Sudan extends GameActivity {

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    protected static final int[] TILE_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16, R.id.tile17, R.id.tile18, R.id.tile19, R.id.tile20,
            R.id.tile21, R.id.tile22, R.id.tile23, R.id.tile24, R.id.tile25, R.id.tile26, R.id.tile27, R.id.tile28, R.id.tile29, R.id.tile30,
            R.id.tile31, R.id.tile32, R.id.tile33, R.id.tile34, R.id.tile35, R.id.tile36, R.id.tile37, R.id.tile38, R.id.tile39, R.id.tile40,
            R.id.tile41, R.id.tile42, R.id.tile43, R.id.tile44, R.id.tile45, R.id.tile46, R.id.tile47, R.id.tile48, R.id.tile49,

            R.id.tile50, R.id.tile51, R.id.tile52, R.id.tile53, R.id.tile54, R.id.tile55, R.id.tile56, R.id.tile57, R.id.tile58, R.id.tile59,
            R.id.tile60, R.id.tile61, R.id.tile62, R.id.tile63
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

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.sudanCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
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

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        setTitle(Start.localAppName + ": " + gameNumber);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);

        View repeatArrow = findViewById(R.id.repeatImage);
        repeatArrow.setVisibility(View.INVISIBLE);

        String differentiateTypesSetting = settingsList.find("Differentiates types of multitype symbols");
        if(differentiateTypesSetting.compareTo("") != 0){
            differentiateTypes = Boolean.parseBoolean(differentiateTypesSetting);
        }
        else{
            differentiateTypes = false;
        }

        showCorrectNumTiles();

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

    }

    public void showCorrectNumTiles(){

        if(differentiateTypes){
            showCorrectNumTiles1PerSymbolAndType();
        }
        else {
            showCorrectNumTiles1PerSymbol();
        }

    }

    public void showCorrectNumTiles1PerSymbolAndType(){
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

    public void showCorrectNumTiles1PerSymbol(){

        visibleTiles = tileList.size();

        for (int k = 0; k < visibleTiles; k++)
        {
            TextView tile = findViewById(TILE_BUTTONS[k]);
            tile.setText(tileList.get(k).baseTile);
            String type = tileList.get(k).tileType;
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

    public void onBtnClick(View view) {
        setAllTilesUnclickable();
        setOptionsRowUnclickable();

        String tileText = "";
        int justClickedKey = Integer.parseInt((String)view.getTag());
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

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }
}
