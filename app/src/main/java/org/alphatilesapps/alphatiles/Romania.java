package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import static org.alphatilesapps.alphatiles.Start.*;

public class Romania extends GameActivity {

    boolean failedToMatchInitialTile = false;
    Start.Tile activeTile;
    boolean directionIsForward = true;
    int scanSetting = 1; // 1, 2 or 3 from aa_settings.txt
    // 1 = Only show word if tile is initial
    // 2 = For tiles with initial examples only, initial; for tiles without initial examples, non-initial acceptable
    // 3 = Show all words regardless of where tile occurs
    String scriptDirection; // aa_langinfo.txt value for Script Direction (LTR or RTL)
    int groupCount; // Number of words selected for an active tile, based on settings
    int indexWithinGroup = 0; // Index of the word being viewed within the group of all words for the tile
    boolean skipThisTile = false; // True when it's a gray word (a word that demonstrates the tile with a medial instance not a word-initial instance)
    Start.Word[] groupOfWordsForActiveTile;
    String tileToStartOn;
    String typeOfTileToStartOn;
    String boldNonIntialFocusTilesTest =  Start.settingsList.find("boldNonInitialFocusTiles");
    String boldInitialfocusTilesTest = settingsList.find("boldInitialFocusTiles");
    // settings to see tiles in focus bolded or not
    boolean boldNonInitialFocusTiles = Boolean.parseBoolean(boldNonIntialFocusTilesTest); // bold non-initial tiles that are in focus
    boolean boldInitialFocusTiles = Boolean.parseBoolean(boldInitialfocusTilesTest);     // bold initial tiles that are in focus

    protected int[] getGameButtons() {
        return null;
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
        } catch (Resources.NotFoundException e) {
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
        String gameUniqueID = country.toLowerCase().substring(0, 2) + challengeLevel;
        setTitle(Start.localAppName + ": " + gameNumber + "    (" + gameUniqueID + ")");

        ImageView image = (ImageView) findViewById(R.id.repeatImage);
        image.setVisibility(View.INVISIBLE);

        Boolean showFilterOptions;
        String hasFilterSetting = settingsList.find("Show filter options for Game 001");
        if (!hasFilterSetting.equals("")) {
            showFilterOptions = Boolean.parseBoolean(hasFilterSetting);
        } else {
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

        tileToStartOn = cumulativeStageBasedTileList.get(0).text;
        typeOfTileToStartOn = cumulativeStageBasedTileList.get(0).typeOfThisTileInstance;
        String tileToStartOn = prefs.getString("lastActiveTileGame001_player" + playerString, this.tileToStartOn);
        String typeOfTileToStartOn = prefs.getString("typeOfLastActiveTileGame001_player" + playerString, this.typeOfTileToStartOn);

        // Load bold settings
        boldNonInitialFocusTiles = prefs.getBoolean("boldNonInitialFocusTiles_player" + playerString, boldNonInitialFocusTiles);
        boldInitialFocusTiles = prefs.getBoolean("boldInitialFocusTiles_player" + playerString, boldInitialFocusTiles);

        scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
        if (scriptDirection.equals("RTL")) {
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

        if (getAudioInstructionsResID() == 0) {
            centerGamesHomeImage();
        }

        int i = 0;
        while(!(cumulativeStageBasedTileList.get(i).text.equals(tileToStartOn) && cumulativeStageBasedTileList.get(i).typeOfThisTileInstance.equals(typeOfTileToStartOn))){
            i++;
        }
        activeTile = cumulativeStageBasedTileList.get(i);
        setUpBasedOnGameTile(activeTile);
    }

    private SpannableStringBuilder boldActiveLetterInWord(String word, String activeLetter) {
        SpannableStringBuilder result = new SpannableStringBuilder(word);
        String lowercaseWord = word.toLowerCase();
        String lowercaseActiveLetter = activeLetter.toLowerCase();
        int startIndex = 0;

        while (startIndex < lowercaseWord.length()) {
            int index = lowercaseWord.indexOf(lowercaseActiveLetter, startIndex);
            if (index == -1) break;

            boolean isInitial = (index == 0);
            if ((isInitial && boldInitialFocusTiles) || (!isInitial && boldNonInitialFocusTiles)) {
                result.setSpan(new StyleSpan(Typeface.BOLD), index, index + activeLetter.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            startIndex = index + 1;
        }

        return result;
    }

    private void setUpBasedOnGameTile(Start.Tile activeTile) {
        skipThisTile = false;

        switch (scanSetting) {
            case 2:
                groupCount = Start.wordList.numberOfWordsForActiveTile(activeTile, 1);
                if (groupCount > 0) {
                    groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(activeTile, groupCount, 1);
                    failedToMatchInitialTile = false;
                } else {
                    failedToMatchInitialTile = true;
                    groupCount = Start.wordList.numberOfWordsForActiveTile(activeTile, 2);
                    if (groupCount > 0) {
                        groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(activeTile, groupCount, 2);
                    } else {
                        skipThisTile = true;
                    }
                }
                break;
            case 3:
                groupCount = Start.wordList.numberOfWordsForActiveTile(activeTile, 3);
                if (groupCount > 0) {
                    groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(activeTile, groupCount, 3);
                } else {
                    skipThisTile = true;
                }
                break;
            default:
                groupCount = Start.wordList.numberOfWordsForActiveTile(activeTile, 1);
                if (groupCount > 0) {
                    groupOfWordsForActiveTile = Start.wordList.wordsForActiveTile(activeTile, groupCount, 1);
                    failedToMatchInitialTile = false;
                } else {
                    failedToMatchInitialTile = true;
                    skipThisTile = true;
                }
        }

        TextView gameTile = (TextView) findViewById(R.id.tileBoxTextView);
        String tileText = activeTile.text;
        gameTile.setText(tileText);
        TextView magTile = (TextView) findViewById(R.id.tileInMagnifyingGlass);
        magTile.setText(indexWithinGroup + 1 + " / " + String.valueOf(String.valueOf(groupCount)));

        if (!skipThisTile) {
            refWord = groupOfWordsForActiveTile[indexWithinGroup];

            if (scanSetting == 3) {
                parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
                failedToMatchInitialTile = !activeTile.text.equals(parsedRefWordTileArray.get(0).text);
            }

            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            String wordToDisplay = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
            SpannableStringBuilder boldedWord = boldActiveLetterInWord(wordToDisplay, activeTile.text);
            activeWord.setText(boldedWord);

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = tileList.returnPositionInAlphabet(activeTile);
            String tileColorStr = colorList.get(alphabetPosition % 5);
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
        } else {
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }
    }

    public void goToNextWord(Start.Tile activeTile) {
        indexWithinGroup++;
        if (indexWithinGroup == groupCount) {
            indexWithinGroup = 0;
        }
        refWord = groupOfWordsForActiveTile[indexWithinGroup];

        if (scanSetting == 3) {
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
            failedToMatchInitialTile = !activeTile.text.equals(parsedRefWordTileArray.get(0).text);
        }

        if (!skipThisTile) {
            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            String wordToDisplay = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
            SpannableStringBuilder boldedWord = boldActiveLetterInWord(wordToDisplay, activeTile.text);
            activeWord.setText(boldedWord);

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = tileList.returnPositionInAlphabet(activeTile);
            String tileColorStr = colorList.get(alphabetPosition % 5);
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
        } else {
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }
    }
    public void goToPreviousWord(Start.Tile activeTile) {
        indexWithinGroup--;
        if (indexWithinGroup == -1) {
            indexWithinGroup = groupCount - 1;
        }
        refWord = groupOfWordsForActiveTile[indexWithinGroup];

        if (scanSetting == 3) {
            parsedRefWordTileArray = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
            failedToMatchInitialTile = !activeTile.text.equals(parsedRefWordTileArray.get(0).text);
        }

        if (!skipThisTile) {
            TextView activeWord = (TextView) findViewById(R.id.activeWordTextView);
            String wordToDisplay = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
            SpannableStringBuilder boldedWord = boldActiveLetterInWord(wordToDisplay, activeTile.text);
            activeWord.setText(boldedWord);

            ImageView image = (ImageView) findViewById(R.id.wordImage);
            if (groupCount > 0) {
                int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
                image.setImageResource(resID);
            } else {
                int resID2 = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
                image.setImageResource(resID2);
                activeWord.setText("");
            }

            int alphabetPosition = tileList.returnPositionInAlphabet(activeTile);
            String tileColorStr = colorList.get(alphabetPosition % 5);
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
        } else {
            if (directionIsForward) {
                goToNextTile(null);
            } else {
                goToPreviousTile(null);
            }
        }
    }

    public void goToNextTile(View View) {
        directionIsForward = true;
        Start.Tile oldTile = activeTile;
        activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);

        if (scanSetting == 1) {
            while (Start.wordList.numberOfWordsForActiveTile(activeTile, 1) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);
            }
        } else if (scanSetting == 2) {
            while ((activeTile.text.length() == 1 && Character.isWhitespace(activeTile.text.charAt(0))) || Start.wordList.numberOfWordsForActiveTile(activeTile, 2) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);
            }
        } else {
            while ((activeTile.text.length() == 1 && Character.isWhitespace(activeTile.text.charAt(0))) ||
                    Start.wordList.numberOfWordsForActiveTile(activeTile, 3) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnNextTile(oldTile);
            }
        }

        indexWithinGroup = 0;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile.text);
        editor.putString("typeOfLastActiveTileGame001_player" + playerString, activeTile.typeOfThisTileInstance);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
    }

    public void goToPreviousTile(View View) {
        directionIsForward = false;
        Start.Tile oldTile = activeTile;
        activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);
        if (scanSetting == 1) {
            while (Start.wordList.numberOfWordsForActiveTile(activeTile, 1) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);
            }
        } else if (scanSetting == 2) {
            while ((activeTile.text.length() == 1 && Character.isWhitespace(activeTile.text.charAt(0))) || Start.wordList.numberOfWordsForActiveTile(activeTile, 2) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);
            }
        } else {
            while ((activeTile.text.length() == 1 && Character.isWhitespace(activeTile.text.charAt(0))) || Start.wordList.numberOfWordsForActiveTile(activeTile, 3) == 0) {
                oldTile = activeTile;
                activeTile = cumulativeStageBasedTileList.returnPreviousTile(oldTile);
            }
        }

        indexWithinGroup = 0;
        SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("lastActiveTileGame001_player" + playerString, activeTile.text);
        editor.putString("typeOfLastActiveTileGame001_player" + playerString, activeTile.typeOfThisTileInstance);
        editor.apply();
        setUpBasedOnGameTile(activeTile);
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

    public void setToggleToInitialOnly(View view) {
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

    public void setToggleToInitialPlusGaps(View view) {
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

    public void setToggleToAllOfAll(View view) {
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
    protected void setAllGameButtonsUnclickable() {
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
    protected void setAllGameButtonsClickable() {
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

    private void updateBoldSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("boldNonInitialFocusTiles_player" + playerString, boldNonInitialFocusTiles);
        editor.putBoolean("boldInitialFocusTiles_player" + playerString, boldInitialFocusTiles);
        editor.apply();
    }

    private void toggleBoldNonInitialFocusTiles() {
        boldNonInitialFocusTiles = !boldNonInitialFocusTiles;
        updateBoldSettings();
        setUpBasedOnGameTile(activeTile);
    }

    private void toggleBoldInitialFocusTiles() {
        boldInitialFocusTiles = !boldInitialFocusTiles;
        updateBoldSettings();
        setUpBasedOnGameTile(activeTile);
    }

    @Override
    public void onBackPressed() {
        // no action
    }
}
