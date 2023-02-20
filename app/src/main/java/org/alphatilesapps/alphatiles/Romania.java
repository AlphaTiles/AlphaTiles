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
import java.util.ArrayList;

import static org.alphatilesapps.alphatiles.Start.*;

public class Romania extends GameActivity {

    boolean failedToMatchInitialTile = false;
    String activeTile;
    boolean directionIsForward = true;
    int scanSetting = 1; // 1, 2 or 3 from aa_settings.txt
    // 1 = Only show word if tile is initial
    // 2 = For tiles with initial examples only, initial; for tiles without initial examples, non-initial acceptable
    // 3 = Show all words regardless of where tile ocurrs
    String scriptDirection; // aa_langinfo.txt value for Script Direction (LTR or RTL)
    boolean forceRTL; // True if lang info has RTL for script direction; false if lang info has LTR for script direction

    int groupCount; // Number of words selected for an active tile, based on settings
    int indexWithinGroup = 0; // Index of the word being viewed within the group of all words for the tile
    boolean skipThisTile = false; // True when it's a gray word (a word that demonstrates the tile with a medial instance not a word-initial instance)
    String [][] groupOfWordsForActiveTile;
    String firstAlphabetTile;

    Boolean differentiateTypes;

    protected int[] getTileButtons() {return null;}

    protected int[] getWordImages() {return null;}

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        }
        catch (Resources.NotFoundException e){
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {

        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID = R.id.romaniaCL;
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.romania);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        points = getIntent().getIntExtra("points", 0); // KP
        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP


        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        // Magnifying glass button (should probably be renamed)
        ImageView image = (ImageView) findViewById(R.id.repeatImage);
        image.setVisibility(View.INVISIBLE);

        // Added Dec 30th, 2021
        // Display or show the three filter options based on value in aa_settings.txt
        Boolean showFilterOptions;
        String hasFilterSetting = settingsList.find("Show filter options for Game 001");
        if(hasFilterSetting.compareTo("")!=0){
            showFilterOptions = Boolean.parseBoolean(hasFilterSetting);
        }
        else{
            showFilterOptions = false;
        }

        ImageView button1 = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView button2 = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView button3 = (ImageView) findViewById(R.id.toggleAllOfAll);

        if (showFilterOptions) {
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
            button3.setVisibility(View.VISIBLE);
        } else {
            button1.setVisibility(View.INVISIBLE);
            button2.setVisibility(View.INVISIBLE);
            button3.setVisibility(View.INVISIBLE);
        }

        scanSetting = Integer.parseInt(Start.settingsList.find("Game 001 Scan Setting"));

        switch (scanSetting) {
            case 2:
                setInitialPlusGaps();
                break;
            case 3:
                setAllOfAll();
                break;
            default:
                setInitialOnly();
        }

        TextView pointsEarned = findViewById(R.id.pointsTextView);

        pointsEarned.setText(String.valueOf(points));

        String differentiateTypesSetting = Start.settingsList.find("Differentiates types of multitype symbols");
        if(differentiateTypesSetting.compareTo("") != 0){
            differentiateTypes = Boolean.parseBoolean(differentiateTypesSetting);

            if(differentiateTypes) {
                firstAlphabetTile = Start.tileListWithMultipleTypes.get(0); // LM
            }
        }
        else{
            differentiateTypes = false;
            firstAlphabetTile = Start.tileList.get(0).baseTile; // KP
        }

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        String startingAlphabetTile = prefs.getString("lastActiveTileGame001_player" + playerString, firstAlphabetTile);

        scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
        if(scriptDirection.compareTo("RTL")==0){
            forceRTL = true;
        }
        else{
            forceRTL = false;
        }

        if (forceRTL){ //LM: flips images for RTL layouts. LTR is default
            ImageView backwardArrowImage = (ImageView) findViewById(R.id.backwardArrowImage);
            ImageView forwardArrowImage = (ImageView) findViewById(R.id.forwardArrowImage);
            ImageView scrollForwardImage = (ImageView) findViewById(R.id.scrollForward);
            ImageView scrollBackImage = (ImageView) findViewById(R.id.scrollBack);
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);

            backwardArrowImage.setRotationY(180);
            forwardArrowImage.setRotationY(180);
            scrollForwardImage.setRotationY(180);
            scrollBackImage.setRotationY(180);
            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
        }

        activeTile = startingAlphabetTile;
        setUpBasedOnGameTile(activeTile);

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    private void setUpBasedOnGameTile(String activeTileString) { // Called every time a player starts the Romania game, and every time they click the arrows to go to a new tile

        skipThisTile = false;

        // Build the groupOfWordsForActiveTile and configure settings based on the setting for Romania in aa_settings.txt
        switch (scanSetting) {
            case 2:
                // CASE 2: check Group One, if count is zero, then check Group Two
                // Group One = words that START with the active tile
                groupCount = Start.wordList.returnGroupOneCount(activeTileString);
                if (groupCount > 0) {
                    groupOfWordsForActiveTile = Start.wordList.returnGroupOneWords(activeTileString, groupCount);
                    failedToMatchInitialTile = false;
                } else {
                    // Group Two = words that contain the active tile non-initially (but excluding initially)
                    failedToMatchInitialTile = true;
                    groupCount = Start.wordList.returnGroupTwoCount(activeTileString);

                    if (groupCount > 0) {
                        groupOfWordsForActiveTile = Start.wordList.returnGroupTwoWords(activeTileString, groupCount); // Group Two = words that contain the active tile non-initially (but excluding initially)
                    }
                }
                break;
            case 3:
                // CASE 3: check Group Three
                // Group Three = words containing the active tile anywhere (initial and/or non-initial)
                groupCount = Start.wordList.returnGroupThreeCount(activeTileString);
                if (groupCount > 0) {
                    groupOfWordsForActiveTile = Start.wordList.returnGroupThreeWords(activeTileString, groupCount); // Group Three = words containing the active tile anywhere (initial and/or non-initial)
                }
                break;
            default:
                // CASE 1: check Group One
                // Group One = words that START with the active tile
                groupCount = Start.wordList.returnGroupOneCount(activeTileString);
                if (groupCount > 0) {
                    groupOfWordsForActiveTile = Start.wordList.returnGroupOneWords(activeTileString, groupCount);
                    failedToMatchInitialTile = false;
                } else { //there are no words at begin with the active tile
                    failedToMatchInitialTile = true;
                    skipThisTile = true;
                }
        }

        // Put the tile text in the view, as well as the count for the words for that tile
        TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
        String tileText = activeTileString;
        if(activeTileString.endsWith("B") || activeTileString.endsWith("C")){
            tileText = activeTileString.substring(0, activeTileString.length() -1);
        }
        gameTile.setText(tileText);
        TextView magTile = (TextView) findViewById(R.id.tileInMagnifyingGlass);
        magTile.setText(indexWithinGroup + 1 + " / " + String.valueOf(String.valueOf(groupCount)));

        if (!skipThisTile) { // If we DO have words in the group for this tile given the scan setting, then...

            //display a word (should normally be the first word) from the group of words for the active tile
            wordInLWC = groupOfWordsForActiveTile[indexWithinGroup][0];
            wordInLOP = groupOfWordsForActiveTile[indexWithinGroup][1];

            //Group 3 has all words containing the tile anywhere. This checks whether the current word is active-tile-initial or not
            if (scanSetting == 3) {
                parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
                failedToMatchInitialTile = !activeTileString.equals(parsedWordArrayFinal.get(0));
            }

            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            activeWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP));


            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = Start.tileList.returnPositionInAlphabet(activeTileString);
            String tileColorStr = COLORS.get(alphabetPosition % 5);
            int tileColor = Color.parseColor(tileColorStr);
            gameTile = (TextView) findViewById(R.id.tileBoxTextView);
            gameTile.setBackgroundColor(tileColor);
            activeWord.setBackgroundColor(tileColor);

            if (failedToMatchInitialTile) {
                tileColorStr = "#A9A9A9"; // dark gray
                tileColor = Color.parseColor(tileColorStr);
                activeWord.setBackgroundColor(tileColor);
            }
            if (groupCount > 0) {
                playActiveWordClip(false);
            }

        } else { //Goes to next tile
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }

    }

    public void goToNextWord(String activeTileString){

        indexWithinGroup++;
        if (indexWithinGroup == groupCount) {
            indexWithinGroup = 0;
        }
        wordInLWC = groupOfWordsForActiveTile[indexWithinGroup][0];
        wordInLOP = groupOfWordsForActiveTile[indexWithinGroup][1];


        if (scanSetting == 3) {
            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
            failedToMatchInitialTile = !activeTileString.equals(parsedWordArrayFinal.get(0));
        }

        //display the next word in groupOfWordsForActiveTile[][]
        if (!skipThisTile) {

            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            activeWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP));

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = Start.tileList.returnPositionInAlphabet(activeTileString);
            String tileColorStr = COLORS.get(alphabetPosition % 5);
            int tileColor = Color.parseColor(tileColorStr);
            TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
            gameTile.setBackgroundColor(tileColor);
            activeWord.setBackgroundColor(tileColor);
            TextView magTile = (TextView) findViewById(R.id.tileInMagnifyingGlass);
            magTile.setText(indexWithinGroup + 1 + " / " + String.valueOf(groupCount));

            if (failedToMatchInitialTile) {
                tileColorStr = "#A9A9A9"; // dark gray
                tileColor = Color.parseColor(tileColorStr);
                activeWord.setBackgroundColor(tileColor);
            }

            if (groupCount > 0) {
                playActiveWordClip(false);
            }

        } else { //Goes to next tile
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }

    }

    public void goToPreviousWord(String activeTileString){
        indexWithinGroup--;
        if (indexWithinGroup == -1) { // Got to the beginning of the word group list
            indexWithinGroup = groupCount-1; // Wrap back to the end of the list
        }
        wordInLWC = groupOfWordsForActiveTile[indexWithinGroup][0];
        wordInLOP = groupOfWordsForActiveTile[indexWithinGroup][1];


        if (scanSetting == 3) {
            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
            failedToMatchInitialTile = !activeTileString.equals(parsedWordArrayFinal.get(0));
        }

        // Display the previous word in groupOfWordsForActiveTile[][]
        if (!skipThisTile) {

            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            activeWord.setText(Start.wordList.stripInstructionCharacters(wordInLOP));

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = Start.tileList.returnPositionInAlphabet(activeTileString);
            String tileColorStr = COLORS.get(alphabetPosition % 5);
            int tileColor = Color.parseColor(tileColorStr);
            TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
            gameTile.setBackgroundColor(tileColor);
            activeWord.setBackgroundColor(tileColor);
            TextView magTile = (TextView) findViewById(R.id.tileInMagnifyingGlass);

            magTile.setText(indexWithinGroup + 1 + " / " + String.valueOf(groupCount));
            if (failedToMatchInitialTile) {
                tileColorStr = "#A9A9A9"; // dark gray
                tileColor = Color.parseColor(tileColorStr);
                activeWord.setBackgroundColor(tileColor);
            }
            if (groupCount > 0) {
                playActiveWordClip(false);
            }

        } else { //Goes to next tile
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }
    }


    public void goToNextTile(View View) {
        directionIsForward = true;
        String oldTile = activeTile;
        if(differentiateTypes){
            activeTile = Start.tileListWithMultipleTypes.returnNextAlphabetTileDifferentiateTypes(oldTile);
        }
        else {
            activeTile = Start.tileList.returnNextAlphabetTile(oldTile); // KP
        }
        if (scanSetting == 1){
            while (returnGroupOneCountRomania(activeTile) == 0){ // JP: prevents user from having to click
                // the arrow multiple times to skip irrelevant tiles that are never word-initial
                oldTile = activeTile;
                if(differentiateTypes){
                    activeTile = Start.tileListWithMultipleTypes.returnNextAlphabetTileDifferentiateTypes(oldTile);
                }
                else {
                    activeTile = Start.tileList.returnNextAlphabetTile(oldTile); // KP
                }
            }
        }else if (scanSetting == 2) {
            while ((activeTile.length() == 1 && Character.isWhitespace(activeTile.charAt(0))) || returnGroupTwoCountRomania(activeTile) == 0){
                oldTile = activeTile;
                if(differentiateTypes){
                    activeTile = Start.tileListWithMultipleTypes.returnNextAlphabetTileDifferentiateTypes(oldTile);
                }
                else {
                    activeTile = Start.tileList.returnNextAlphabetTile(oldTile); // KP
                }
            }
        }else{ // scanSetting 3
            while ((activeTile.length() == 1 && Character.isWhitespace(activeTile.charAt(0))) ||
                    returnGroupThreeCountRomania(activeTile) == 0){
                oldTile = activeTile;
                if(differentiateTypes){
                    activeTile = Start.tileListWithMultipleTypes.returnNextAlphabetTileDifferentiateTypes(oldTile);
                }
                else {
                    activeTile = Start.tileList.returnNextAlphabetTile(oldTile); // KP
                }
            }
        }

        indexWithinGroup = 0;
        SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
    }

    public void goToPreviousTile(View View) {
        directionIsForward = false;
        String oldTile = activeTile;
        if(differentiateTypes){
            activeTile = Start.tileListWithMultipleTypes.returnPreviousAlphabetTileDifferentiateTypes(oldTile);
        }
        else {
            activeTile = Start.tileList.returnPreviousAlphabetTile(oldTile); // KP
        }
        if (scanSetting == 1){
            while (returnGroupOneCountRomania(activeTile) == 0){
                // JP: prevents user from having to click
                // the arrow multiple times to skip irrelevant tiles that are never word-initial
                oldTile = activeTile;
                if(differentiateTypes){
                    activeTile = Start.tileListWithMultipleTypes.returnPreviousAlphabetTileDifferentiateTypes(oldTile);
                }
                else {
                    activeTile = Start.tileList.returnPreviousAlphabetTile(oldTile); // KP
                }
            }
        }else if (scanSetting == 2) {
            while ((activeTile.length() == 1 && Character.isWhitespace(activeTile.charAt(0))) || returnGroupTwoCountRomania(activeTile) == 0){
                oldTile = activeTile;
                if(differentiateTypes){
                    activeTile = Start.tileListWithMultipleTypes.returnPreviousAlphabetTileDifferentiateTypes(oldTile);
                }
                else {
                    activeTile = Start.tileList.returnPreviousAlphabetTile(oldTile); // KP
                }
            }
        }else{ // scanSetting 3
            while ((activeTile.length() == 1 && Character.isWhitespace(activeTile.charAt(0))) ||
                    returnGroupThreeCountRomania(activeTile) == 0){
                oldTile = activeTile;
                if(differentiateTypes){
                    activeTile = Start.tileListWithMultipleTypes.returnPreviousAlphabetTileDifferentiateTypes(oldTile);
                }
                else {
                    activeTile = Start.tileList.returnPreviousAlphabetTile(oldTile); // KP
                }
            }
        }

        indexWithinGroup = 0;
        SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
    }

    // JP copied this function into here because of
    // "non-static method cannot be accessed in static context" issue
    public int returnGroupOneCountRomania(String someGameTile) {
        // Group One = words that START with the active tile

        ArrayList<String> parsedWordArrayFinal;
        String wordInitialTile;
        String wordInitialTileType;
        String someGameTileType;
        String someGameTileWithoutSuffix;

        someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
        if (someGameTileType.compareTo("B") == 0) {
            someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
        } else if (someGameTileType.compareTo("C") == 0) {
            someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
        } else {
            someGameTileWithoutSuffix = someGameTile;
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
        }

        int tilesCount = 0;

        for (int i = 0; i < wordList.size(); i++) {
            parsedWordArrayFinal = tileList.parseWordIntoTiles(wordList.get(i).localWord);

            wordInitialTile = parsedWordArrayFinal.get(0);

            if (wordInitialTile != null) {

                if(differentiateTypes){// Checking if both tile and type match
                    if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                        wordInitialTileType = Start.tileList.getInstanceTypeForMixedTile(0, wordList.get(i).nationalWord);
                    }
                    else{// Not dealing with a multifunction symbol
                        wordInitialTileType = tileHashMap.find(wordInitialTile).tileType;
                    }

                    if(wordInitialTile.equals(someGameTileWithoutSuffix) && someGameTileType.equals(wordInitialTileType)){
                        tilesCount++;
                    }

                }
                else {// Not differentiating types, only matching tile to tile
                    if (parsedWordArrayFinal.get(0).equals(someGameTile)) {
                        tilesCount++;
                    }
                }

            }
        }

        return tilesCount;

    }

    public int returnGroupTwoCountRomania(String someGameTile) {
        // Group Two = words that contain the active tile non-initially (but excluding initially)

        ArrayList<String> parsedWordArrayFinal;
        String tileInFocus;
        String tileInFocusType;
        String someGameTileType;
        String someGameTileWithoutSuffix;

        someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
        if (someGameTileType.compareTo("B") == 0) {
            someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
        } else if (someGameTileType.compareTo("C") == 0) {
            someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
        } else {
            someGameTileWithoutSuffix = someGameTile;
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
        }

        int tilesCount = 0;

        for (int i = 0; i < wordList.size(); i++) {
            parsedWordArrayFinal = tileList.parseWordIntoTiles(wordList.get(i).localWord);

            for (int k = 1; k < parsedWordArrayFinal.size(); k++) {
                tileInFocus = parsedWordArrayFinal.get(k);

                if (tileInFocus != null) {

                    if(differentiateTypes){// Checking if both tile and type match
                        if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                            tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, wordList.get(i).nationalWord);
                        }
                        else{// Not dealing with a multifunction symbol
                            tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                        }

                        if(tileInFocus.equals(someGameTileWithoutSuffix) && someGameTileType.equals(tileInFocusType)){
                            tilesCount++;
                        }

                    }
                    else {// Not differentiating types, only matching tile to tile
                        if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                            tilesCount++;
                        }
                    }
                }
            }
        }

        return tilesCount;

    }

    public int returnGroupThreeCountRomania(String someGameTile) {
        // Group Three = words containing the active tile anywhere (initial and/or non-initial)

        ArrayList<String> parsedWordArrayFinal;
        String tileInFocus;
        String tileInFocusType;
        String someGameTileType;
        String someGameTileWithoutSuffix;

        someGameTileType = Character.toString(someGameTile.charAt(someGameTile.length() - 1));
        if (someGameTileType.compareTo("B") == 0) {
            someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeB;
        } else if (someGameTileType.compareTo("C") == 0) {
            someGameTileWithoutSuffix = someGameTile.substring(0, someGameTile.length() -1);
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileTypeC;
        } else {
            someGameTileWithoutSuffix = someGameTile;
            someGameTileType = tileHashMap.find(someGameTileWithoutSuffix).tileType;
        }

        int tilesCount = 0;

        for (int i = 0; i < wordList.size(); i++) {
            parsedWordArrayFinal = tileList.parseWordIntoTiles(wordList.get(i).localWord);

            for (int k = 0; k < parsedWordArrayFinal.size(); k++) {
                tileInFocus = parsedWordArrayFinal.get(k);

                if (tileInFocus != null) {

                    if(differentiateTypes){// Checking if both tile and type match
                        if(MULTIFUNCTIONS.contains(someGameTileWithoutSuffix)) {
                            tileInFocusType = Start.tileList.getInstanceTypeForMixedTile(k, wordList.get(i).nationalWord);
                        }
                        else{// Not dealing with a multifunction symbol
                            tileInFocusType = tileHashMap.find(tileInFocus).tileType;
                        }

                        if(tileInFocus.equals(someGameTileWithoutSuffix) && someGameTileType.equals(tileInFocusType)){
                            tilesCount++;
                        }
                    }
                    else {// Not differentiating types, only matching tile to tile
                        if (parsedWordArrayFinal.get(k).equals(someGameTile)) {
                            tilesCount++;
                        }
                    }
                }
            }
        }

        return tilesCount;

    }

    public void repeatGame(View View) {

        setUpBasedOnGameTile(activeTile);

    }

    public void scrollForward(View view) {

        goToNextWord(activeTile);
    }

    public void scrollBack(View view) {

        goToPreviousWord(activeTile);
    }

    public void setToggleToInitialOnly (View view) {

        setInitialOnly();

    }
    public void setInitialOnly() {

        scanSetting = 1;

        ImageView toggleOne = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView toggleTwo = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView toggleThree = (ImageView) findViewById(R.id.toggleAllOfAll);

        int resID1 = getResources().getIdentifier("zz_toggle_initial_only_on", "drawable", getPackageName());
        int resID2 = getResources().getIdentifier("zz_toggle_initial_plus_gaps_off", "drawable", getPackageName());
        int resID3 = getResources().getIdentifier("zz_toggle_all_of_all_off", "drawable", getPackageName());

        toggleOne.setImageResource(resID1);
        toggleTwo.setImageResource(resID2);
        toggleThree.setImageResource(resID3);

    }

    public void setToggleToInitialPlusGaps (View view) {

        setInitialPlusGaps();

    }
    public void setInitialPlusGaps() {

        scanSetting = 2;

        ImageView toggleOne = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView toggleTwo = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView toggleThree = (ImageView) findViewById(R.id.toggleAllOfAll);

        int resID1 = getResources().getIdentifier("zz_toggle_initial_only_off", "drawable", getPackageName());
        int resID2 = getResources().getIdentifier("zz_toggle_initial_plus_gaps_on", "drawable", getPackageName());
        int resID3 = getResources().getIdentifier("zz_toggle_all_of_all_off", "drawable", getPackageName());

        toggleOne.setImageResource(resID1);
        toggleTwo.setImageResource(resID2);
        toggleThree.setImageResource(resID3);

    }

    public void setToggleToAllOfAll (View view) {

        setAllOfAll();

    }
    public void setAllOfAll() {

        scanSetting = 3;

        ImageView toggleOne = (ImageView) findViewById(R.id.toggleInitialOnly);
        ImageView toggleTwo = (ImageView) findViewById(R.id.toggleInitialPlusGaps);
        ImageView toggleThree = (ImageView) findViewById(R.id.toggleAllOfAll);

        int resID1 = getResources().getIdentifier("zz_toggle_initial_only_off", "drawable", getPackageName());
        int resID2 = getResources().getIdentifier("zz_toggle_initial_plus_gaps_off", "drawable", getPackageName());
        int resID3 = getResources().getIdentifier("zz_toggle_all_of_all_on", "drawable", getPackageName());

        toggleOne.setImageResource(resID1);
        toggleTwo.setImageResource(resID2);
        toggleThree.setImageResource(resID3);

    }

    @Override
    protected void setAllTilesUnclickable()
    {

        TextView tileBox = findViewById(R.id.tileBoxTextView);
        tileBox.setClickable(false);

        ImageView word = findViewById(R.id.wordImage);
        word.setClickable(false);

        ImageView forwardArrow = findViewById(R.id.forwardArrowImage);
        forwardArrow.setClickable(false);
        forwardArrow.setBackgroundResource(0);
        forwardArrow.setImageResource(R.drawable.zz_forward_inactive);

        ImageView backwardArrow = findViewById(R.id.backwardArrowImage);
        backwardArrow.setClickable(false);
        backwardArrow.setBackgroundResource(0);
        backwardArrow.setImageResource(R.drawable.zz_backward_inactive);

        TextView magTile = findViewById(R.id.tileInMagnifyingGlass);
        magTile.setClickable(false);

    }

    @Override
    protected void setAllTilesClickable()
    {

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

        TextView magTile = findViewById(R.id.tileInMagnifyingGlass);
        magTile.setClickable(true);

    }

    public void clickPicHearAudio(View view)
    {
        super.clickPicHearAudio(view);
    }

    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    public void playAudioInstructions(View view){
        if(getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

}
