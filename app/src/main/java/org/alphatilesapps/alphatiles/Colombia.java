package org.alphatilesapps.alphatiles;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.alphatilesapps.alphatiles.Start.*;

public class Colombia extends GameActivity {

    // JP:
    // syllables level 1: only necessary syllables, scrambled
    // syllables level 2: necessary syllables + 1 distractor syllable per syllable, scrambled
    // syllables level 3: necessary syllables + all 3 distractor syllables per syllable
    // (filter out any repeats), scrambled
    int keysInUse; // Number of keys in the language's total keyboard
    int keyboardScreenNo; // For languages with more than 35 keys, page 1 will have 33 buttons and a forward/backward button
    int totalScreens; // Total number of screens required to show all keys
    int partial; // Number of visible keys on final partial screen
    static List<Start.Tile> tileKeysList = new ArrayList<>();
    static List<Start.Syllable> syllableKeysList = new ArrayList<>();
    static List<WordPiece> clickedKeys = new ArrayList<>(); // Keys clicked, in order
    static ArrayList<Start.Tile> tilesInBuiltWord = new ArrayList<>();

    final int tilesPerPage = 35;
    final int syllablesPerPage = 18;

    protected static final int[] GAME_BUTTONS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07, R.id.key08, R.id.key09, R.id.key10,
            R.id.key11, R.id.key12, R.id.key13, R.id.key14, R.id.key15, R.id.key16, R.id.key17, R.id.key18, R.id.key19, R.id.key20,
            R.id.key21, R.id.key22, R.id.key23, R.id.key24, R.id.key25, R.id.key26, R.id.key27, R.id.key28, R.id.key29, R.id.key30,
            R.id.key31, R.id.key32, R.id.key33, R.id.key34, R.id.key35
    };

    protected int[] getGameButtons() {
        return GAME_BUTTONS;
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
        } catch (NullPointerException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {
        ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
        instructionsButton.setVisibility(View.GONE);

        int gameID;
        if (syllableGame.equals("S")) {
            gameID = R.id.colombiaCL_syll;
        } else {
            gameID = R.id.colombiaCL;
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
        if (syllableGame.equals("S")) {
            setContentView(R.layout.colombia_syllables);
            gameID = R.id.colombiaCL_syll;
        } else {
            setContentView(R.layout.colombia);
            gameID = R.id.colombiaCL;
        }

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = (ImageView) findViewById(R.id.instructions);
            ImageView repeatImage = (ImageView) findViewById(R.id.repeatImage);
            ImageView deleteImage = (ImageView) findViewById(R.id.deleteImage);

            instructionsImage.setRotationY(180);
            repeatImage.setRotationY(180);
            deleteImage.setRotationY(180);

            fixConstraintsRTL(gameID);
        }

        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel + syllableGame;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        keyboardScreenNo = 1;
        updatePointsAndTrackers(0);
        playAgain();
    }
    public void repeatGame(View View) {

        if (!repeatLocked) {
            playAgain();
        }

    }

    public void playAgain() {

        repeatLocked = true;
        setAdvanceArrowToGray();

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        wordToBuild.setText("");
        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
        wordToBuild.setTextColor(Color.parseColor("#000000")); // black

        setWord();

        ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
        deleteArrow.setClickable(true);

        ImageView wordImage = (ImageView) findViewById(R.id.wordImage);
        wordImage.setClickable(true);

    }

    private void setWord() {
        clickedKeys.clear();
        tilesInBuiltWord.clear();
        chooseWord();
        ImageView image = (ImageView) findViewById(R.id.wordImage);
        int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
        image.setImageResource(resID);

        if (syllableGame.equals("S")) {
            parsedRefWordSyllableArray = Start.syllableList.parseWordIntoSyllables(refWord); // KP
        } else {
            parsedRefWordTileArray = Start.tileList.parseWordIntoTiles(refWord.wordInLOP, refWord); // KP
        }

        loadKeyboard();

    }

    public void loadKeyboard() {
        tileKeysList.clear();
        syllableKeysList.clear();

        switch (challengeLevel) {
            case 1:
                // Build an array of only the required tiles
                // Will list <a> twice if <a> is needed twice
                // The limited set keyboard is built with GAME TILES not with KEYS

                if (syllableGame.equals("S")) {
                    syllableKeysList = new ArrayList<>(parsedRefWordSyllableArray);
                    Collections.shuffle(syllableKeysList);
                    visibleGameButtons = syllableKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(syllableKeysList.get(k).text);
                        Random rand = new Random();
                        int index = rand.nextInt(4);
                        int tileColor = Color.parseColor(COLORS.get(index));
                        key.setBackgroundColor(tileColor);
                    }

                } else {
                    tileKeysList = new ArrayList<>(parsedRefWordTileArray);
                    Collections.shuffle(tileKeysList);
                    visibleGameButtons = tileKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(tileKeysList.get(k).text);
                        Random rand = new Random();
                        int colorInt = rand.nextInt(4);
                        String tileColorStr = COLORS.get(colorInt);
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }

                }
                break;
            case 2:
                // Build an array of the required tiles plus a corresponding tile from the distractor trio for each tile
                // So, for a five tile word, there will be 10 tiles
                // The limited-set keyboard is built with GAME TILES not with KEYS
                if (syllableGame.equals("S")) {
                    syllableKeysList = new ArrayList<>(parsedRefWordSyllableArray);
                    int numberOfCorrectKeys = parsedRefWordSyllableArray.size();
                    for (int n=0; n<numberOfCorrectKeys; n++) {
                        Start.Syllable syllableInTheList = syllableKeysList.get(n);
                        if(SAD_STRINGS.contains(syllableInTheList.text)){
                            Start.Syllable distractorSADSyllable = syllableInTheList;
                            distractorSADSyllable.text = tileList.returnRandomDistractorTile(tileHashMap.find(syllableInTheList.text)).text;
                            if (distractorSADSyllable.distractors.contains(distractorSADSyllable.text)) {
                                distractorSADSyllable.distractors.remove(distractorSADSyllable.text);
                                distractorSADSyllable.distractors.add(syllableInTheList.text);
                            }
                            syllableKeysList.add(distractorSADSyllable);
                        } else {
                            syllableKeysList.add(syllableList.returnRandomDistractorSyllable(syllableInTheList));
                        }
                    }
                    Collections.shuffle(syllableKeysList);
                    visibleGameButtons = syllableKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(syllableKeysList.get(k).text);
                        Random rand = new Random();
                        int index = rand.nextInt(4);
                        int tileColor = Color.parseColor(COLORS.get(index));
                        key.setBackgroundColor(tileColor);
                    }

                } else {
                    tileKeysList = new ArrayList<>(parsedRefWordTileArray);
                    int numberOfCorrectKeys = parsedRefWordTileArray.size();
                    for (int n=0; n<numberOfCorrectKeys; n++) {
                        tileKeysList.add(tileList.returnRandomDistractorTile(tileKeysList.get(n)));
                    }
                    Collections.shuffle(tileKeysList);
                    visibleGameButtons = tileKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(tileKeysList.get(k).text);
                        Random rand = new Random();
                        int colorInt = rand.nextInt(4);
                        String tileColorStr = COLORS.get(colorInt);
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }

                }
                break;
            case 3:
                if (syllableGame.equals("S")) { // 18 tiles; distractors for the wrong answers
                    syllableKeysList = new ArrayList<>(parsedRefWordSyllableArray);
                    for (int n=0; n<(18-parsedRefWordSyllableArray.size()); n++) {
                        Start.Syllable syllableInTheList = syllableKeysList.get(n);
                        if(SAD_STRINGS.contains(syllableInTheList.text)){
                            Start.Syllable distractorSADSyllable = syllableInTheList;
                            distractorSADSyllable.text = tileList.returnRandomDistractorTile(tileHashMap.find(syllableInTheList.text)).text;
                            if(distractorSADSyllable.distractors.contains(distractorSADSyllable.text)){
                                distractorSADSyllable.distractors.remove(distractorSADSyllable.text);
                                distractorSADSyllable.distractors.add(syllableInTheList.text);
                            }
                            syllableKeysList.add(distractorSADSyllable);
                        } else {
                            syllableKeysList.add(syllableList.returnRandomDistractorSyllable(syllableInTheList));
                        }
                    }
                    Collections.shuffle(syllableKeysList);
                    visibleGameButtons = syllableKeysList.size();
                    TextView key;
                    for(int k = 0; k< visibleGameButtons; k++){
                        key = findViewById(GAME_BUTTONS[k]);
                        key.setText(syllableKeysList.get(k).text);
                        Random rand = new Random();
                        int index = rand.nextInt(4);
                        int tileColor = Color.parseColor(COLORS.get(index));
                        key.setBackgroundColor(tileColor);
                    }

                } else { // Full key list
                    for (Start.Key key: keyList) {
                        tileKeysList.add(tileHashMap.find(key.text));
                    }
                    keysInUse = keyList.size(); // KP
                    partial = keysInUse % (GAME_BUTTONS.length - 2);
                    totalScreens = keysInUse / (GAME_BUTTONS.length - 2);

                    if (partial != 0) {
                        totalScreens++;
                    }

                    if (keysInUse > GAME_BUTTONS.length) {
                        visibleGameButtons = GAME_BUTTONS.length;
                    } else {
                        visibleGameButtons = keysInUse;
                    }
                    for (int k = 0; k < visibleGameButtons; k++) {
                        TextView key = findViewById(GAME_BUTTONS[k]);
                        key.setText(keyList.get(k).text); // KRP
                        String tileColorStr = COLORS.get(Integer.parseInt(keyList.get(k).color));
                        int tileColor = Color.parseColor(tileColorStr);
                        key.setBackgroundColor(tileColor);
                    }
                    if (keysInUse > GAME_BUTTONS.length) {
                        TextView key34 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 2]);
                        key34.setBackgroundResource(R.drawable.zz_backward_green);
                        if (scriptDirection.equals("RTL")) {
                            key34.setRotationY(180);
                        }
                        key34.setText("");
                        TextView key35 = findViewById(GAME_BUTTONS[GAME_BUTTONS.length - 1]);
                        key35.setBackgroundResource(R.drawable.zz_forward_green);
                        if (scriptDirection.equals("RTL")) {
                            key35.setRotationY(180);
                        }
                        key35.setText("");
                    }
                }
                break;
            default:
        }

        if (syllableGame.equals("S")) {
            for (int k = 0; k < syllablesPerPage; k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                if (k < visibleGameButtons) {
                    key.setVisibility(View.VISIBLE);
                    key.setClickable(true);
                } else {
                    key.setVisibility(View.INVISIBLE);
                    key.setClickable(false);
                }
            }
        } else {
            for (int k = 0; k < GAME_BUTTONS.length; k++) {
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
    }

    private void respondToKeySelection(int justClickedIndex) {

        WordPiece clickedKey;
        Tile typedTile;
        String currentWord = "";
        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);
        if (syllableGame.equals("S")) {
            clickedKey = syllableKeysList.get(justClickedIndex);
            clickedKeys.add(clickedKey);
            for(WordPiece key : clickedKeys) {
                currentWord+= key.text;
            }
        } else {
            if(!(challengeLevel==3)){
                clickedKey = tileKeysList.get(justClickedIndex);
                typedTile = tileKeysList.get(justClickedIndex);
                tilesInBuiltWord.add(typedTile);
                clickedKeys.add(clickedKey);
                currentWord = combineTilesToMakeWord(tilesInBuiltWord, refWord, -1);
            } else {
                clickedKey = new WordPiece(keyList.get(justClickedIndex).text);
                clickedKeys.add(clickedKey);
                currentWord = wordToBuild.getText() + clickedKey.text;
            }

        }

        wordToBuild.setText(currentWord);
        evaluateStatus();
    }

    private void evaluateStatus() {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        String correctString = wordInLOPWithStandardizedSequenceOfCharacters(refWord);
        String currentAttempt;
        if (syllableGame.equals("S") || (syllableGame.equals("T") && challengeLevel == 3)) {
            currentAttempt = wordToBuild.getText().toString();
        } else {
            currentAttempt = combineTilesToMakeWord(tilesInBuiltWord, refWord, -1);
        }

        if (currentAttempt.equals(correctString)) { // Word spelled correctly!
            wordToBuild.setBackgroundColor(Color.parseColor("#4CAF50"));      // theme green
            wordToBuild.setTextColor(Color.parseColor("#FFFFFF")); // white
            for (int i=0; i<visibleGameButtons; i++) {
                TextView key = findViewById(GAME_BUTTONS[i]);
                key.setClickable(false);
            }
            ImageView deleteArrow = (ImageView) findViewById(R.id.deleteImage);
            deleteArrow.setClickable(false);
            updatePointsAndTrackers(4);
            playCorrectSoundThenActiveWordClip(false);
            repeatLocked = false;
            setAdvanceArrowToBlue();

        } else { // Word is partial and, for the moment, assumed to be incorrect
            wordToBuild.setBackgroundColor(Color.parseColor("#A9A9A9")); // gray for wrong
            wordToBuild.setTextColor(Color.parseColor("#000000")); // black

            if (correctString.length() > currentAttempt.length()) {
                ArrayList<WordPiece> firstNCorrectTiles = new ArrayList<>();
                for (int t=0; t<clickedKeys.size(); t++) {
                    if (syllableGame.equals("S")) {
                        if (t<parsedRefWordSyllableArray.size()){
                            firstNCorrectTiles.add(parsedRefWordSyllableArray.get(t));
                        }
                    } else {
                        if (t<parsedRefWordTileArray.size()) {
                            firstNCorrectTiles.add(parsedRefWordTileArray.get(t));
                        }
                    }
                }
                if (currentAttempt.equals(correctString.substring(0, currentAttempt.length()))
                || clickedKeys.equals(firstNCorrectTiles)) { // Word is incomplete but spelled correctly so far
                    // orange=true if there is no key option that would allow the player to continue correctly
                    if (challengeLevel == 1 || challengeLevel == 2 || syllableGame.equals("S")) {
                        boolean orange = false;
                        for (int i = 0; i < clickedKeys.size(); i++) {
                            if (syllableGame.equals("S")) {
                                if (!clickedKeys.get(i).text.equals(parsedRefWordSyllableArray.get(i).text)) {
                                    orange = true;
                                    break;
                                }
                            } else {
                                if (!clickedKeys.get(i).text.equals(parsedRefWordTileArray.get(i).text)) {
                                    orange = true;
                                    break;
                                }
                            }
                        }
                        if (orange) {
                            wordToBuild.setBackgroundColor(Color.parseColor("#F44336")); // orange
                        } else {
                            wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B")); // the yellow that the xml design tab suggested
                        }
                        wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                    } else {
                        wordToBuild.setBackgroundColor(Color.parseColor("#FFEB3B"));
                        wordToBuild.setTextColor(Color.parseColor("#000000")); // black
                    }
                }
            }

        }
    }

    public void deleteLastKeyedLetter(View view) {

        TextView wordToBuild = (TextView) findViewById(R.id.activeWordTextView);

        String typedLettersSoFar = wordToBuild.getText().toString();
        String nowWithOneLessWordPiece = "";

        if (typedLettersSoFar.length() > 0) {
            clickedKeys.remove(clickedKeys.size() - 1);
            if (syllableGame.equals("S")) {
                nowWithOneLessWordPiece = typedLettersSoFar.substring(0, typedLettersSoFar.length() - 1);
            } else if (syllableGame.equals("T") && challengeLevel == 3) { // Using keyboard keys, not tiles
                nowWithOneLessWordPiece = typedLettersSoFar.substring(0, typedLettersSoFar.length() - 1);
            } else if (syllableGame.equals("T")) {
                tilesInBuiltWord.remove(tilesInBuiltWord.size() - 1);
                nowWithOneLessWordPiece = combineTilesToMakeWord(tilesInBuiltWord, refWord, -1);
            }
        }

        wordToBuild.setText(nowWithOneLessWordPiece);
        evaluateStatus();

    }

    public void onBtnClick(View view) {

        int justClickedKey = Integer.parseInt((String) view.getTag());
        // Next line says ... if a basic keyboard (which all fits on one screen) or (even when on a complex keyboard) if something other than the last two buttons (the two arrows) are tapped...
        if (syllableGame.equals("S")) {
            if (keysInUse <= syllablesPerPage || justClickedKey <= (syllablesPerPage - 2)) {
                int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
                respondToKeySelection(keyIndex);
            } else {
                // This branch = when a backward or forward arrow is clicked on
                if (justClickedKey == syllablesPerPage - 1) {
                    keyboardScreenNo--;
                    if (keyboardScreenNo < 1) {
                        keyboardScreenNo = 1;
                    }
                }
                if (justClickedKey == syllablesPerPage) {
                    keyboardScreenNo++;
                    if (keyboardScreenNo > totalScreens) {
                        keyboardScreenNo = totalScreens;
                    }
                }
                updateKeyboard();
            }
        } else {
            if (keysInUse <= tilesPerPage || justClickedKey <= (tilesPerPage - 2)) {
                int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
                respondToKeySelection(keyIndex);
            } else {
                // This branch = when a backward or forward arrow is clicked on
                if (justClickedKey == tilesPerPage - 1) {
                    keyboardScreenNo--;
                    if (keyboardScreenNo < 1) {
                        keyboardScreenNo = 1;
                    }
                }
                if (justClickedKey == tilesPerPage) {
                    keyboardScreenNo++;
                    if (keyboardScreenNo > totalScreens) {
                        keyboardScreenNo = totalScreens;
                    }
                }
                updateKeyboard();
            }
        }

    }

    private void updateKeyboard() { // This routine is only called when there are more keys than will fit on the basic 35-key layout

        int keysLimit;
        if (totalScreens == keyboardScreenNo) {
            keysLimit = partial;
            for (int k = keysLimit; k < (tilesPerPage - 2); k++) {
                TextView key = findViewById(GAME_BUTTONS[k]);
                key.setVisibility(View.INVISIBLE);
            }
        } else {
            keysLimit = tilesPerPage - 2;
        }

        for (int k = 0; k < keysLimit; k++) {
            TextView key = findViewById(GAME_BUTTONS[k]);
            int keyIndex = (33 * (keyboardScreenNo - 1)) + k;
            key.setText(keyList.get(keyIndex).text); // KP
            key.setVisibility(View.VISIBLE);

            String tileColorStr = COLORS.get(Integer.parseInt(keyList.get(keyIndex).color)); // Added on May 15th, 2021, so that second and following screens use their own color coding
            int tileColor = Color.parseColor(tileColorStr);
            key.setBackgroundColor(tileColor);
        }
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

}
