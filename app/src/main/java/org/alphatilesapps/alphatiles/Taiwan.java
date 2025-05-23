package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import static org.alphatilesapps.alphatiles.Start.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.wordStagesLists;

public class Taiwan extends GameActivity {

    private static final Logger LOGGER = Logger.getLogger(Taiwan.class.getName());

    // UI elements
    private ImageView wordImageView;
    private TextView tilesTextView; //changed to TextView
    private TextView syllablesTextView; //changed to TextView
    private TextView wordTextViewDisplay; //changed to TextView
    private ImageView forwardArrow;
    private ImageView backwardArrow;
    private TextView numberOfTotalTextView;

    // Game data
    private List<Start.Word> wordListInOrder; // Corrected to use Start.Word
    private int currentWordIndex = 0;
    private String currentWord;
    private String[] currentTiles;
    private String[] currentSyllables;
    private int currentStage = 0;
    private ArrayList<TextView> tileTextViews = new ArrayList<>();
    private ArrayList<TextView> syllableTextViews = new ArrayList<>();
    private TextView wordTextView; //keeping this for the word display
    private ImageView backwardArrowImage;
    private ImageView forwardArrowImage;
    private ImageView wordImage;
    private TextView numberOfTotalText;

    private ImageView gamesHomeImage;
    private ImageView instructions;
    private Start.Word currentWordObject; // Storing the Start.Word object
    private String currentWordString;

    private ArrayList<String> wordList; //  list of words
    private static final Random random = new Random();


    private int colorCounter = 0;
    private static final int[] TILE_COLORS = {
            Color.parseColor(colorList.get(0)),
            Color.parseColor(colorList.get(1)),
            Color.parseColor(colorList.get(2)),
            Color.parseColor(colorList.get(3)),
            Color.parseColor(colorList.get(4))
    };

    private boolean syllableAudioAvailable;
    private int wordsCompleted = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taiwan);

        // Initialize UI elements
        backwardArrowImage = findViewById(R.id.backwardArrowImage);
        forwardArrowImage = findViewById(R.id.forwardArrowImage);
        wordImage = findViewById(R.id.wordImage);
        wordTextViewDisplay = findViewById(R.id.wordTextViewDisplay);
        tilesTextView = findViewById(R.id.tilesTextView);
        syllablesTextView = findViewById(R.id.syllablesTextView);
        gamesHomeImage = findViewById(R.id.gamesHomeImage);
        instructions = findViewById(R.id.instructions);
        numberOfTotalText = findViewById(R.id.numberOfTotalText);


        //hide the syllablesTextView.
        syllablesTextView.setVisibility(View.GONE);

        // Set listeners
        backwardArrowImage.setOnClickListener(this::goToPreviousWord);
        forwardArrowImage.setOnClickListener(this::goToNextWord);
        wordImage.setOnClickListener(this::clickPicHearAudio);
        gamesHomeImage.setOnClickListener(this::goBackToEarth);
        instructions.setOnClickListener(this::playAudioInstructions);

        // Get syllable audio availability
        syllableAudioAvailable = Start.hasSyllableAudio;

        // Populate wordListInOrder with Start.Word objects from Start.wordList
        wordListInOrder = new ArrayList<>();
        for (Start.Word word : Start.wordList) { // Corrected to Start.Word
            wordListInOrder.add(word);
        }
        if (wordListInOrder.isEmpty()) {
            LOGGER.warning("Word list is empty!");
            return;
        }

        loadNewWord();
        updateNumberOfTotalTextView();
    }

    private void loadNewWord() {
        // Get the current Start.Word object
        currentWordObject = wordListInOrder.get(currentWordIndex);
        currentWordString = currentWordObject.wordInLOP; // Use wordInLOP from Start.Word

        //set the image
        int imageID = getResources().getIdentifier(currentWordString, "drawable", getPackageName());
        if (imageID != 0) {
            wordImage.setImageResource(imageID);
        } else {
            wordImage.setImageResource(R.drawable.ic_launcher_background);
            LOGGER.warning("Missing image for word: " + currentWordString);
        }

        // Split word into tiles
        currentTiles = getTiles(currentWordString);

        // Split into syllables if audio is available
        if (syllableAudioAvailable) {
            currentSyllables = getSyllables(currentWordString);
        }

        currentStage = 0;
        colorCounter = 0;
        tileTextViews.clear();
        tilesTextView.setText(""); // Clear textview
        syllablesTextView.setText(""); // Clear textview
        syllablesTextView.setVisibility(View.GONE);


        wordTextViewDisplay.setText(""); // Clear the word textview
        wordTextViewDisplay.setVisibility(View.GONE);

        createTiles();
        // Enable/disable backward arrow based on currentWordIndex
        backwardArrowImage.setEnabled(currentWordIndex > 0);
        backwardArrowImage.setImageResource(currentWordIndex > 0 ? R.drawable.zz_backward : R.drawable.zz_backward_inactive);
        forwardArrowImage.setEnabled(false); // Disable forward arrow until word is clicked
        forwardArrowImage.setImageResource(R.drawable.zz_forward_inactive);
    }

    private void createTiles() {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        tileTextViews.clear();

        for (String tileText : currentTiles) { // Renamed 'tile' to 'tileText' to avoid confusion
            final int start = ssb.length();
            ssb.append(tileText);
            final int end = ssb.length();

            // Create a custom clickable span that also changes background color
            ClickableTileSpan clickableSpan = new ClickableTileSpan(
                    TILE_COLORS[colorCounter % TILE_COLORS.length],
                    Color.GRAY, // Initial color
                    tileText // Pass the tileText to the span
            ) {
                @Override
                public void onClick(View widget) {
                    // Only process if it's still gray (not yet clicked)
                    if (getBackgroundColor() == Color.GRAY) {
                        int newColor = TILE_COLORS[colorCounter % TILE_COLORS.length];
                        setBackgroundColor(newColor); // Update the span's color
                        // Re-set the text to force redraw of the TextView to apply new span color
                        tilesTextView.setText(ssb, TextView.BufferType.SPANNABLE);
                        colorCounter++;

                        // Get the Start.Tile object for the clicked tileText
                        // Use tileHashMap.find() which returns a new Tile object
                        Start.Tile clickedTileObj = Start.tileHashMap.find(getTileText());
                        if (clickedTileObj != null) {
                            tileAudioPress(false, clickedTileObj); // Play tile audio using GameActivity's method
                        } else {
                            LOGGER.warning("Could not find Tile object for: " + getTileText() + ". Falling back to word audio.");
                            playActiveWordClip(false); // Fallback to playing the whole word sound
                        }


                        // Check if all "tiles" have been clicked
                        if (colorCounter >= currentTiles.length) {
                            playActiveWordClip(false); // Play word audio
                            currentStage++;
                            if (syllableAudioAvailable) {
                                createSyllables();
                                syllablesTextView.setVisibility(View.VISIBLE);
                                tilesTextView.setVisibility(View.GONE); // Hide tiles
                            } else {
                                currentStage++;
                                createWordDisplay();
                                wordTextViewDisplay.setVisibility(View.VISIBLE);
                                tilesTextView.setVisibility(View.GONE); // Hide tiles
                            }
                        }
                    }
                }
            };
            ssb.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Make it bold
            ssb.append(" "); // Add space between tiles
        }
        tilesTextView.setText(ssb, TextView.BufferType.SPANNABLE); // Set BufferType to SPANNABLE
        tilesTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance()); // Make spans clickable
    }

    private void createSyllables() {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        syllableTextViews.clear();

        for (String syllableText : currentSyllables) { // Renamed 'syllable' to 'syllableText'
            final int start = ssb.length();
            ssb.append(syllableText);
            final int end = ssb.length();

            ClickableTileSpan clickableSpan = new ClickableTileSpan(
                    TILE_COLORS[colorCounter % TILE_COLORS.length],
                    Color.GRAY, // Initial color
                    syllableText // Pass the syllableText to the span
            ) {
                @Override
                public void onClick(View widget) {
                    if (getBackgroundColor() == Color.GRAY) {
                        int newColor = TILE_COLORS[colorCounter % TILE_COLORS.length];
                        setBackgroundColor(newColor);
                        syllablesTextView.setText(ssb, TextView.BufferType.SPANNABLE); // Force redraw
                        colorCounter++;

                        // Get the Start.Syllable object for the clicked syllableText
                        Start.Syllable clickedSyllableObj = Start.syllableHashMap.find(getTileText());
                        if (clickedSyllableObj != null) {
                            // Create a dummy Tile object from the Syllable's data to pass to tileAudioPress
                            // This is a workaround since tileAudioPress expects a Start.Tile object
                            Start.Tile dummyTileForSyllable = new Start.Tile(
                                    clickedSyllableObj.text,
                                    clickedSyllableObj.distractors,
                                    "S", // Assuming a 'Syllable' type, adjust if your gametiles.txt has one
                                    clickedSyllableObj.audioName,
                                    "", "", "", "", "", 0, 0, 0, 0, 0, 0, "S", 0, clickedSyllableObj.audioName
                            );
                            tileAudioPress(false, dummyTileForSyllable); // Play syllable audio
                        } else {
                            LOGGER.warning("Could not find Syllable object for: " + getTileText() + ". Falling back to word audio.");
                            playActiveWordClip(false); // Fallback to playing the whole word sound
                        }

                        if (colorCounter >= currentTiles.length + currentSyllables.length) { // Adjusted count
                            playActiveWordClip(false); // Play word audio
                            currentStage = 2;
                            createWordDisplay();
                            wordTextViewDisplay.setVisibility(View.VISIBLE);
                            syllablesTextView.setVisibility(View.GONE); // Hide syllables
                        }
                    }
                }
            };
            ssb.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(" ");
        }
        syllablesTextView.setText(ssb, TextView.BufferType.SPANNABLE); // Set BufferType to SPANNABLE
        syllablesTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    private void createWordDisplay() {
        wordTextViewDisplay.setText(currentWordString); // Use currentWordString
        wordTextViewDisplay.setBackgroundColor(Color.GRAY); // Initial background
        wordTextViewDisplay.setOnClickListener(this::onWordClick);
        wordTextViewDisplay.setVisibility(View.VISIBLE);
    }

    // These methods are now redundant as clicks are handled by ClickableTileSpan
    public void onTileClick(View view) {
        // No longer directly used, clicks are handled by ClickableTileSpan
    }

    public void onSyllableClick(View view) {
        // No longer directly used, clicks are handled by ClickableTileSpan
    }

    public void onWordClick(View view) {
        playActiveWordClip(false); // Play word audio
        forwardArrowImage.setEnabled(true);
        forwardArrowImage.setImageResource(R.drawable.zz_forward);
    }

    public void goToNextWord(View view) {
        if (forwardArrowImage.isEnabled()) {
            wordsCompleted++;
            updateNumberOfTotalTextView();
            if (currentWordIndex < wordListInOrder.size() - 1) {
                currentWordIndex++;
                loadNewWord();
            } else {
                finishGame();
            }
            forwardArrowImage.setEnabled(false);
            forwardArrowImage.setImageResource(R.drawable.zz_forward_inactive); // Corrected to inactive
        }
    }

    public void goToPreviousWord(View view) {
        if (currentWordIndex > 0) {
            currentWordIndex--;
            loadNewWord();
            backwardArrowImage.setImageResource(currentWordIndex > 0 ? R.drawable.zz_backward : R.drawable.zz_backward_inactive); // Update backward arrow state
            forwardArrowImage.setEnabled(false); // Disable forward arrow on backward navigation
            forwardArrowImage.setImageResource(R.drawable.zz_forward_inactive); // Corrected to inactive
        }
    }

    private void finishGame() {
        LOGGER.info("Game finished!");
        currentWordIndex = 0;
        loadNewWord();
        wordsCompleted = 0;
        updateNumberOfTotalTextView();
    }

    //Helper functions
    private String[] getTiles(String word) {
        // Use Start.tileList.parseWordIntoTiles to get the correct tile breakdown
        // This method expects a Start.Word object, so we use currentWordObject
        ArrayList<Start.Tile> parsedTiles = Start.tileList.parseWordIntoTiles(word, currentWordObject);
        String[] tileStrings = new String[parsedTiles.size()];
        for (int i = 0; i < parsedTiles.size(); i++) {
            tileStrings[i] = parsedTiles.get(i).text;
        }
        return tileStrings;
    }

    private String[] getSyllables(String wordString) {
        // Use Start.syllableList.parseWordIntoSyllables to get the correct syllable breakdown
        // This method expects a Start.Word object, so we use currentWordObject
        if (currentWordObject != null && Start.syllableList != null) {
            ArrayList<Start.Syllable> parsedSyllables = Start.syllableList.parseWordIntoSyllables(currentWordObject);
            String[] syllableStrings = new String[parsedSyllables.size()];
            for (int i = 0; i < parsedSyllables.size(); i++) {
                syllableStrings[i] = parsedSyllables.get(i).text;
            }
            return syllableStrings;
        } else {
            LOGGER.warning("Could not parse syllables for word: " + wordString + ". Syllable list or word object not found.");
            return getTiles(wordString); // Fallback to tiles if syllables cannot be parsed
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateNumberOfTotalTextView() {
        String message = String.format(getResources().getString(R.string.number_of_total_words), wordsCompleted, wordListInOrder.size());
        numberOfTotalText.setText(message);
    }

    //Game Activity methods
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void goBackToEarth(View view) {
        super.goBackToEarth(view);
    }

    @Override
    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }

    @Override
    public void clickPicHearAudio(View view) {
        // This method is called when the main word image is clicked.
        // It should play the full word audio.
        playActiveWordClip(false);
    }

    @Override
    public int[] getGameButtons() { // Changed return type to int[]
        // Return resource IDs of the game buttons
        return new int[]{R.id.backwardArrowImage, R.id.forwardArrowImage, R.id.wordImage, R.id.gamesHomeImage, R.id.instructions, R.id.numberOfTotalText};
    }

    @Override
    protected int[] getWordImages() {
        return new int[0];
    }

    @Override
    protected int getAudioInstructionsResID() {
        return 0;
    }

    @Override
    protected void hideInstructionAudioImage() {

    }

    // Custom ClickableSpan to handle background color changes and clicks within a TextView
    private abstract class ClickableTileSpan extends android.text.style.ClickableSpan {
        private int backgroundColor;
        private final String tileText;

        public ClickableTileSpan(int initialColor, int intialDefaultColor, String tileText) {
            this.backgroundColor = intialDefaultColor; // Start with default gray
            this.tileText = tileText;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(int color) {
            this.backgroundColor = color;
        }

        public String getTileText() {
            return tileText;
        }

        @Override
        public void updateDrawState(android.text.TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.WHITE); // Text color
            ds.bgColor = backgroundColor; // Background color
            ds.setUnderlineText(false); // No underline
        }
    }
}
