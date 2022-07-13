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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.*;

public class Colombia extends GameActivity {

    // JP:
    // syllables level 1: only necessary syllables, scrambled
    // syllables level 2: necessary syllables + 1 distractor syllable per syllable, scrambled
    // syllables level 3: necessary syllables + all 3 distractor syllables per syllable
    // (filter out any repeats), scrambled

    String initial = "";
    Set<String> keys = new HashSet<String>();
    int keysInUse; // number of keys in the language's total keyboard
    int keyboardScreenNo; // for languages with more than 35 keys, page 1 will have 33 buttons and a forward/backward button
    int totalScreens; // the total number of screens required to show all keys
    int partial; // the number of visible keys on final partial screen
    String lastWord = "";
    String secondToLastWord = "";
    String thirdToLastWord = "";
    int colombiaPoints;
    static List<String> keysList = new ArrayList<>();
    static List<String> keysClicked = new ArrayList<>(); // will be used to keep track of the order
    // that the user clicked keys so that we can backtrack in deleteLastKeyedSyllable, and used in evaluateStatus for orange color

    protected static final int[] TILE_BUTTONS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07, R.id.key08, R.id.key09, R.id.key10,
            R.id.key11, R.id.key12, R.id.key13, R.id.key14, R.id.key15, R.id.key16, R.id.key17, R.id.key18, R.id.key19, R.id.key20,
            R.id.key21, R.id.key22, R.id.key23, R.id.key24, R.id.key25, R.id.key26, R.id.key27, R.id.key28, R.id.key29, R.id.key30,
            R.id.key31, R.id.key32, R.id.key33, R.id.key34, R.id.key35
    };

    protected static final int[] SYLL_BUTTONS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07, R.id.key08, R.id.key09, R.id.key10,
            R.id.key11, R.id.key12, R.id.key13, R.id.key14, R.id.key15, R.id.key16, R.id.key17, R.id.key18
    };
    
    protected int[] getTileButtons() {return TILE_BUTTONS;}
    protected int[] getWordImages() {return null;}

    @Override
    protected int getAudioInstructionsResID() {
        Resources res = context.getResources();
        int audioInstructionsResID;
        try{
//          audioInstructionsResID = res.getIdentifier("colombia_" + challengeLevel, "raw", context.getPackageName());
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

        int gameID;
        if (syllableGame.equals("S")){
            gameID = R.id.colombiaCL_syll;
        }else{
            gameID = R.id.colombiaCL;
        }

        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.gamesHomeImage,ConstraintSet.END,R.id.repeatImage,ConstraintSet.START,0);
        constraintSet.connect(R.id.repeatImage,ConstraintSet.START,R.id.gamesHomeImage,ConstraintSet.END,0);
        constraintSet.centerHorizontally(R.id.gamesHomeImage, gameID);
        constraintSet.applyTo(constraintLayout);

    }

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63","#6200EE"};


    private static final Logger LOGGER = Logger.getLogger(Colombia.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if (syllableGame.equals("S")){
            setContentView(R.layout.colombia_syllables);
        }else{
            setContentView(R.layout.colombia);
        }

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        if (scriptDirection.compareTo("RTL") == 0){ //LM: flips images for RTL layouts. LTR is default
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            ImageView deleteImage = (ImageView) findViewById(R.id.deleteImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            deleteImage.setRotationY(180);
        }


        points = getIntent().getIntExtra("points", 0); // KP
        colombiaPoints = getIntent().getIntExtra("colombiaPoints", 0); // LM

        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        colombiaPoints = prefs.getInt("storedColombiaPoints_level" + challengeLevel + "_player" + playerString, 0);

        playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
        challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

        String gameUniqueID = country.toLowerCase().substring(0,2) + challengeLevel + syllableGame;

        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(colombiaPoints));

        LOGGER.info("Remember: oC2");

        /*SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);*/
        String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
        trackerCount = prefs.getInt(uniqueGameLevelPlayerID,0);

        updateTrackers();

        LOGGER.info("Remember: oC3");

        setTextSizes();

        LOGGER.info("Remember: oC4");

        if(getAudioInstructionsResID()==0){
            centerGamesHomeImage();
        }

        keyboardScreenNo = 1;
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

        if (syllableGame.equals("S")){
            for (int k = 0; k < SYLL_BUTTONS.length; k++) {

                TextView key = findViewById(SYLL_BUTTONS[k]);
                if (k == 0) {
                    ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) key.getLayoutParams();
                    bottomToTopId = lp1.bottomToTop;
                    topToTopId = lp1.topToTop;
                    percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                    percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                    percentHeight = percentBottomToTop - percentTopToTop;
                    pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
                }
                key.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

            }
        }else{
            for (int k = 0; k < TILE_BUTTONS.length; k++) {

                TextView key = findViewById(TILE_BUTTONS[k]);
                if (k == 0) {
                    ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) key.getLayoutParams();
                    bottomToTopId = lp1.bottomToTop;
                    topToTopId = lp1.topToTop;
                    percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                    percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                    percentHeight = percentBottomToTop - percentTopToTop;
                    pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
                }
                key.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

            }
        }


        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) wordToBuild.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        wordToBuild.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

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
        //CHANGED TO 0.5 BY JP SO THAT 4-DIGIT SCORE WILL FIT IN GEM
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }
    
    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }
    public void playAgain() {

        repeatLocked = true;

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        wordToBuild.setText("");
        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
        wordToBuild.setTextColor(Color.parseColor("#000000")); // black

        LOGGER.info("Remember: pA1");

        chooseWord();

        LOGGER.info("Remember: pA2");
        ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
        deleteArrow.setClickable(true);

        LOGGER.info("Remember: pA3");
        loadKeyboard();

    }

    private void chooseWord() {

        boolean freshWord = false;
        keysClicked.clear();

        while(!freshWord) {
            Random rand = new Random();
            int randomNum = rand.nextInt(Start.wordList.size()); // KP

            wordInLWC = Start.wordList.get(randomNum).nationalWord; // KP
            wordInLOP = Start.wordList.get(randomNum).localWord; // KP

            //If this word isn't one of the 3 previously tested words, we're good // LM
            if(wordInLWC.compareTo(lastWord)!=0
                    && wordInLWC.compareTo(secondToLastWord)!=0
                    && wordInLWC.compareTo(thirdToLastWord)!=0){
                freshWord = true;
                thirdToLastWord = secondToLastWord;
                secondToLastWord = lastWord;
                lastWord = wordInLWC;
            }

        }//generates a new word if it got one of the last three tested words // LM

        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        // KP
        if (syllableGame.equals("S")){
            parsedWordArrayFinal = Start.syllableList.parseWordIntoSyllables(wordInLOP); // KP
        }else{
            parsedWordArrayFinal = Start.tileList.parseWordIntoTiles(wordInLOP); // KP
        }
        initial = parsedWordArrayFinal.get(0); // KP


    }
    public void loadKeyboard() {
        if (!keys.isEmpty()){
            keys.clear();
        }
        if (!keysList.isEmpty()){
            keysList.clear();
        }

        switch (challengeLevel)
        {
            case 1:
                // Build an array of only the required tiles
                // Will it list <a> twice if <a> is needed twice? Yes, that's what it does
                // The limited set keyboard is built with GAME TILES not with KEYS

                for (String key : parsedWordArrayFinal){
                    keys.add(key);
                }
                keysList = new ArrayList<>(keys);
                Collections.shuffle(keysList); // KRP
                visibleTiles = keysList.size();
                for (int k = 0; k < visibleTiles; k++)
                {
                    TextView key;
                    if (syllableGame.equals("S")){
                        key = findViewById(SYLL_BUTTONS[k]);
                    }else{
                        key = findViewById(TILE_BUTTONS[k]);
                    }
                    key.setText(keysList.get(k));

                }
                break;
            case 2:
                // Build an array of the required tiles plus a corresponding tile from the distractor trio for each tile
                // So, for a five tile word, there will be 10 tiles
                // The limited-set keyboard is built with GAME TILES not with KEYS

                for (String key : parsedWordArrayFinal){
                    keys.add(key);
                }

                int unique_keys = keys.size();
                int a = 0;
                int a_mod = 0;
                while (keys.size() < unique_keys*2) {
                    if (syllableGame.equals("S")){
                        keys.add(syllableList.returnRandomCorrespondingSyllable(parsedWordArrayFinal.get(a_mod))); // JP
                    }else{
                        keys.add(tileList.returnRandomCorrespondingTile(parsedWordArrayFinal.get(a_mod))); // KRP
                    }

                    a++;
                    a_mod = a % parsedWordArrayFinal.size();
                }
                keysList = new ArrayList<>(keys);
                Collections.shuffle(keysList); // KRP
                visibleTiles = keysList.size();
                for (int k = 0; k < visibleTiles; k++)
                {
                    TextView key;
                    if (syllableGame.equals("S")){
                        key = findViewById(SYLL_BUTTONS[k]);
                    }else{
                        key = findViewById(TILE_BUTTONS[k]);
                    }
                    key.setText(keysList.get(k));

                }
                break;
            case 3:

                if (syllableGame.equals("S")){ //all distractor syllables up to 18

                    Start.SyllableList sortableSyllArray = (Start.SyllableList)Start.syllableList.clone();
                    Collections.shuffle(sortableSyllArray);

                    for (String syll : parsedWordArrayFinal){
                        keys.add(syll);
                        if (keys.size() < 18){
                            keys.add(syllableHashMap.find(syll).distractors[0]);
                        }
                        if (keys.size() < 18){
                            keys.add(syllableHashMap.find(syll).distractors[1]);
                        }
                        if (keys.size() < 18){
                            keys.add(syllableHashMap.find(syll).distractors[2]);
                        }
                    }

                    visibleTiles = keys.size();
                    keysList = new ArrayList<>(keys);
                    Collections.shuffle(keysList); // KRP

                    for (int k = 0; k < visibleTiles; k++)
                    {
                        String syllKey = keysList.get(k);
                        TextView key = findViewById(SYLL_BUTTONS[k]);
                        key.setText(syllKey); // JP
                        String tileColorStr = COLORS[Integer.parseInt(syllableHashMap.find(syllKey).color)];
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }
                }else{
                    keysInUse = keyList.size(); // KP
                    partial = keysInUse % (TILE_BUTTONS.length - 2);
                    totalScreens = keysInUse / (TILE_BUTTONS.length - 2);

                    LOGGER.info("Remember: partial = " + partial);
                    LOGGER.info("Remember: totalScreens = " + totalScreens);
                    if (partial != 0) {
                        totalScreens++;
                    }

                    LOGGER.info("Remember: April 22 2021 A1");
                    if (keysInUse > TILE_BUTTONS.length) {
                        visibleTiles = TILE_BUTTONS.length;
                    } else {
                        visibleTiles = keysInUse;
                    }
                    for (int k = 0; k < visibleTiles; k++)
                    {
                        TextView key = findViewById(TILE_BUTTONS[k]);
                        key.setText(keyList.get(k).baseKey); // KRP
                        String tileColorStr = COLORS[Integer.parseInt(keyList.get(k).keyColor)];
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }
                    LOGGER.info("Remember: April 22 2021 A2");
                    if (keysInUse > TILE_BUTTONS.length) {
                        LOGGER.info("Remember: keysInUse = " + keysInUse);
                        LOGGER.info("Remember: KEYS.length = " + TILE_BUTTONS.length);
                        LOGGER.info("Remember: April 22 2021 A2");
                        TextView key34 = findViewById(TILE_BUTTONS[TILE_BUTTONS.length - 2]);
                        key34.setBackgroundResource(R.drawable.zz_backward_green);
                        if(scriptDirection.compareTo("RTL")==0) { //LTR is default
                            key34.setRotationY(180);
                        }
                        key34.setText("");
                        LOGGER.info("key34's text: " + key34.getText());
                        TextView key35 = findViewById(TILE_BUTTONS[TILE_BUTTONS.length - 1]);
                        key35.setBackgroundResource(R.drawable.zz_forward_green);
                        if(scriptDirection.compareTo("RTL")==0) { //LTR is default
                            key35.setRotationY(180);
                        }
                        key35.setText("");
                        LOGGER.info("key35's text: " + key35.getText());
                    }
                    LOGGER.info("Remember: April 22 2021 A3");

                }
                break;
            default:
        }

        if (syllableGame.equals("S")){
            for (int k = 0; k < SYLL_BUTTONS.length; k++)
            {

                TextView key = findViewById(SYLL_BUTTONS[k]);
                if (k < visibleTiles)
                {
                    key.setVisibility(View.VISIBLE);
                    key.setClickable(true);
                }
                else
                {
                    key.setVisibility(View.INVISIBLE);
                    key.setClickable(false);
                }

            }
        }else{
            for (int k = 0; k < TILE_BUTTONS.length; k++)
            {

                TextView key = findViewById(TILE_BUTTONS[k]);
                if (k < visibleTiles)
                {
                    key.setVisibility(View.VISIBLE);
                    key.setClickable(true);
                }
                else
                {
                    key.setVisibility(View.INVISIBLE);
                    key.setClickable(false);
                }

            }
        }


    }

    private void respondToKeySelection(int justClickedIndex) {

        String tileToAdd = "";

        switch (challengeLevel) {
            // KP
            case 3:
                if (syllableGame.equals("S")){
                    tileToAdd = keysList.get(justClickedIndex);
                }else{
                    tileToAdd = Start.keyList.get(justClickedIndex).baseKey;
                }
                keysClicked.add(tileToAdd);

                break;
            default:
                tileToAdd = keysList.get(justClickedIndex); // KP (case 1)
                keysClicked.add(tileToAdd);
        }

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        String currentWord = wordToBuild.getText() + tileToAdd;     // RR
        wordToBuild.setText(currentWord);                           // RR

        evaluateStatus();

    }
    private void evaluateStatus() {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        if (wordToBuild.getText().equals(Start.wordList.stripInstructionCharacters(wordInLOP))) {
            // Word spelled correctly!
            wordToBuild.setBackgroundColor(Color.parseColor("#4CAF50"));      // theme green
            wordToBuild.setTextColor(Color.parseColor("#FFFFFF")); // white

            if (syllableGame.equals("S")){
                for (int i : SYLL_BUTTONS) {
                    TextView key = findViewById(i);
                    key.setClickable(false);
                }
            }else{
                for (int i : TILE_BUTTONS) {                    // RR
                    TextView key = findViewById(i);     // RR
                    key.setClickable(false);
                }
            }


            ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
            deleteArrow.setClickable(false);

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            points+=4;
            colombiaPoints+=4;
            pointsEarned.setText(String.valueOf(colombiaPoints));

            trackerCount++;
            updateTrackers();

            SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
            String playerString = Util.returnPlayerStringToAppend(playerNumber);
            editor.putInt("storedPoints_player" + playerString, points);
            editor.putInt("storedColombiaPoints_level" + challengeLevel + "_player" + playerString, colombiaPoints);
            editor.apply();
            String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
            editor.putInt(uniqueGameLevelPlayerID, trackerCount);
            editor.apply();

            playCorrectSoundThenActiveWordClip(false);

            repeatLocked = false;

        } else {

            // Word is partial and, for the moment, assumed to be incorrect
            wordToBuild.setBackgroundColor(Color.parseColor("#A9A9A9")); // gray for wrong
            wordToBuild.setTextColor(Color.parseColor("#000000")); // black

            if (wordInLOP.length() > wordToBuild.getText().length()) {

                //orange if there is no tile/key option that would allow you to continue correctly

                //use keysClicked and parsedWordArrayFinal

                if (wordToBuild.getText().equals(Start.wordList.stripInstructionCharacters(wordInLOP)
                        .substring(0, wordToBuild.getText().length()))) {
                    // problem when what's in textbox is longer than the correct word??
                    // doesn't prev if statement take care of that?
                    // specifically on level 3 syllables ??
                    
                    // Word, so far, spelled correctly, but a less than complete match
                    if (challengeLevel == 1 || challengeLevel == 2 || syllableGame.equals("S")){
                        boolean orange = false;
                        for (int i = 0; i < keysClicked.size(); i++){
                            if (!keysClicked.get(i).equals(parsedWordArrayFinal.get(i))){
                                orange = true;
                                break;
                            }
                        }

                        if (orange){
                            // indicates that there is no key available to continue correctly; ex: you typed i but need í
                            wordToBuild.setBackgroundColor(Color.parseColor("#F44336")); // orange
                        }else{
                            wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
                        }
                        wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                    }else {
                        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B"));
                        wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                    }

                }

            }
        }
    }

    public void deleteLastKeyedLetter (View view) {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        String typedLettersSoFar = wordToBuild.getText().toString();
        String nowWithOneLessChar = "";

        if (typedLettersSoFar.length() > 0) {       // RR
            nowWithOneLessChar = typedLettersSoFar.substring(0, typedLettersSoFar.length() - 1);
            keysClicked.remove(keysClicked.size() -1);
        }

        wordToBuild.setText(nowWithOneLessChar);
        evaluateStatus();

    }

    public void deleteLastKeyed (View view) { //JP

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);


        String typedLettersSoFar = wordToBuild.getText().toString();
        String nowWithOneLessSyll = "";

        if (typedLettersSoFar.length() > 0) {       // RR
            int shortenFromThisIndex = typedLettersSoFar.lastIndexOf(keysClicked.get(keysClicked.size() -1));
            nowWithOneLessSyll = typedLettersSoFar.substring(0, shortenFromThisIndex);
            keysClicked.remove(keysClicked.size() -1);
        }

        wordToBuild.setText(nowWithOneLessSyll);
        evaluateStatus();

    }

    public void onBtnClick(View view) {

        int justClickedKey = Integer.parseInt((String)view.getTag());
        // Next line says ... if a basic keyboard (which all fits on one screen) or (even when on a complex keyboard) if something other than the last two buttons (the two arrows) are tapped...
        if (syllableGame.equals("S")){
            if (keysInUse <= SYLL_BUTTONS.length || justClickedKey <= (SYLL_BUTTONS.length - 2)) {
                int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
                respondToKeySelection(keyIndex);
            } else {
                // This branch = when a backward or forward arrow is clicked on
                if (justClickedKey == SYLL_BUTTONS.length - 1) {
                    keyboardScreenNo--;
                    if (keyboardScreenNo < 1) {
                        keyboardScreenNo = 1;
                    }
                }
                if (justClickedKey == SYLL_BUTTONS.length) {
                    keyboardScreenNo++;
                    if (keyboardScreenNo > totalScreens) {
                        keyboardScreenNo = totalScreens;
                    }
                }
                updateKeyboard();
            }
        }else{
            if (keysInUse <= TILE_BUTTONS.length || justClickedKey <= (TILE_BUTTONS.length - 2)) {
                int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
                respondToKeySelection(keyIndex);
            } else {
                // This branch = when a backward or forward arrow is clicked on
                if (justClickedKey == TILE_BUTTONS.length - 1) {
                    keyboardScreenNo--;
                    if (keyboardScreenNo < 1) {
                        keyboardScreenNo = 1;
                    }
                }
                if (justClickedKey == TILE_BUTTONS.length) {
                    keyboardScreenNo++;
                    if (keyboardScreenNo > totalScreens) {
                        keyboardScreenNo = totalScreens;
                    }
                }
                updateKeyboard();
            }
        }

    }

    private void updateKeyboard() {

        // This routine will only be called from complex keyboards (more keys than will fit on the basic 35-key layout)

        int keysLimit;
        if(totalScreens == keyboardScreenNo) {
            keysLimit = partial;
            for (int k = keysLimit; k < (TILE_BUTTONS.length - 2); k++) {
                TextView key = findViewById(TILE_BUTTONS[k]);
                key.setVisibility(View.INVISIBLE);
            }
        } else {
            keysLimit = TILE_BUTTONS.length - 2;
        }

        for (int k = 0; k < keysLimit; k++) {
            TextView key = findViewById(TILE_BUTTONS[k]);
            int keyIndex = (33 * (keyboardScreenNo - 1)) + k;
            key.setText(keyList.get(keyIndex).baseKey); // KP
            key.setVisibility(View.VISIBLE);
            // Added on May 15th, 2021, so that second and following screens use their own color coding
            String tileColorStr = COLORS[Integer.parseInt(keyList.get(keyIndex).keyColor)];
            int tileColor = Color.parseColor(tileColorStr);
            key.setBackgroundColor(tileColor);
        }

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
