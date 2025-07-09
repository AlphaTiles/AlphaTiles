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
import java.util.logging.Logger;

public class Taiwan extends GameActivity {

    protected static final int[] GAME_BUTTONS = {
            R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05,
            R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
            R.id.tile011, R.id.tile012, R.id.tile013, R.id.tile014, R.id.tile015
    };

    private List<Start.Tile> tileButtonsList = new ArrayList<>();
    private boolean[] tileTapped;
    private int tilesTappedCount = 0;
    private int currentPhase = 0; // 0 = tiles, 1 = word

    private int visibleTileButtons;

    private static final Logger LOGGER = Logger.getLogger(Taiwan.class.getName());

    @Override
    protected int[] getGameButtons() {
        return GAME_BUTTONS;
    }

    @Override
    protected int[] getWordImages() {
        return null; // no word image buttons
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
    protected void hideInstructionAudioImage() {
        ImageView instructionsButton = findViewById(R.id.instructions);
        if (instructionsButton != null) {
            instructionsButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.taiwan);
        initializeForNewWord();
        ActivityLayouts.applyEdgeToEdge(this, R.id.taiwanCL);
        ActivityLayouts.setStatusAndNavColors(this);

        if (scriptDirection.equals("RTL")) {
            ImageView instructionsImage = findViewById(R.id.instructions);
            ImageView forwardArrowImage = findViewById(R.id.forwardArrowImage);


            if (instructionsImage != null) instructionsImage.setRotationY(180);
            if (forwardArrowImage != null) forwardArrowImage.setRotationY(180);


            fixConstraintsRTL(R.id.taiwanCL);
        }

        if (getAudioInstructionsResID() == 0) {
            hideInstructionAudioImage();
        }

        initializeForNewWord();
    }

    private void initializeForNewWord() {
        currentPhase = 0;
        clearAllButtons();

        chooseWord();

        setupTileButtons();

        displayWordImage();

        hideWordRow();

        disableAdvanceArrow();
    }

    private void clearAllButtons() {
        for (int i = 0; i < GAME_BUTTONS.length; i++) {
            TextView button = findViewById(GAME_BUTTONS[i]);
            if (button != null) {
                button.setVisibility(View.INVISIBLE);
                button.setClickable(false);
                button.setText("");
                button.setBackgroundColor(Color.GRAY);
            }
        }
    }

    private void setupTileButtons() {
        tileButtonsList = tileList.parseWordIntoTiles(refWord.wordInLOP, refWord);
        visibleTileButtons = tileButtonsList.size();

        tileTapped = new boolean[visibleTileButtons];
        tilesTappedCount = 0;

        for (int i = 0; i < visibleTileButtons; i++) {
            TextView tileButton = findViewById(GAME_BUTTONS[i]);
            if (tileButton != null) {
                String tileText = tileButtonsList.get(i).text;
                tileButton.setText(tileText);
                tileButton.setVisibility(View.VISIBLE);
                tileButton.setClickable(true);
                tileButton.setBackgroundColor(Color.GRAY);
                tileButton.setTag(i);
            }
        }

        for (int i = visibleTileButtons; i < GAME_BUTTONS.length; i++) {
            TextView button = findViewById(GAME_BUTTONS[i]);
            if (button != null) {
                button.setVisibility(View.INVISIBLE);
                button.setClickable(false);
                button.setText("");
            }
        }
    }

    private void displayWordImage() {
        ImageView wordImageView = findViewById(R.id.wordImage);
        if (wordImageView != null) {
            int resID = getResources().getIdentifier(refWord.wordInLWC, "drawable", getPackageName());
            if (resID == 0) {
                resID = getResources().getIdentifier("zz_no_image_found", "drawable", getPackageName());
            }
            wordImageView.setImageResource(resID);
            wordImageView.setVisibility(View.VISIBLE);
            wordImageView.setClickable(true);
        }
    }

    private void hideWordRow() {
        TextView wordText = findViewById(R.id.wordTextViewDisplay);
        if (wordText != null) {
            wordText.setText("");
            wordText.setVisibility(View.INVISIBLE);
            wordText.setClickable(false);
            wordText.setBackgroundColor(Color.GRAY);
        }
    }

    private void showWordRow() {
        TextView wordText = findViewById(R.id.wordTextViewDisplay);
        if (wordText != null) {
            String displayWord = Start.wordList.stripInstructionCharacters(refWord.wordInLOP);
            wordText.setText(displayWord);
            wordText.setVisibility(View.VISIBLE);
            wordText.setClickable(true);
            wordText.setBackgroundColor(Color.GRAY);
        }
    }

    private void disableAdvanceArrow() {
        ImageView advanceArrow = findViewById(R.id.forwardArrowImage);
        if (advanceArrow != null) {
            advanceArrow.setClickable(false);

            if (Start.changeArrowColor) {
                advanceArrow.setImageResource(R.drawable.zz_forward_inactive);

            } else {
                advanceArrow.setImageResource(R.drawable.zz_forward);
            }
        }
    }

    private void enableAdvanceArrow() {
        ImageView advanceArrow = findViewById(R.id.forwardArrowImage);

        if (advanceArrow != null) {
            advanceArrow.setClickable(true);
            if (Start.changeArrowColor) {
                advanceArrow.setImageResource(R.drawable.zz_forward);

            }
        }
    }

    public void onBtnClick(View view) {

        if (!(view instanceof TextView)) return;

        int index = (int) view.getTag();
        if (index < 0 || index >= tileButtonsList.size()) return;

        TextView tileButton = (TextView) view;
        Start.Tile tappedTile = tileButtonsList.get(index);
        tileAudioPress(false, tappedTile);

        if (!tileTapped[index]) {
            tileTapped[index] = true;
            tilesTappedCount++;
            int colorIndex = index % 5;
            tileButton.setBackgroundColor(Color.parseColor(colorList.get(colorIndex)));
        }

        if (tilesTappedCount == tileButtonsList.size()) {
            playWordAudioAndRevealWordRow();
        }
    }

    public void onWordTextViewClick(View view) {
        if (currentPhase != 1) return;
        TextView wordText = findViewById(R.id.wordTextViewDisplay);
        double test = Math.random() * 4;
        switch ((int) test) {
            case(1):
                wordText.setBackgroundColor(Color.GREEN);
                break;
            case(2):
                wordText.setBackgroundColor(Color.RED);
                break;
            case(3):
                wordText.setBackgroundColor(Color.MAGENTA);
                break;
            case(4):
                wordText.setBackgroundColor(Color.BLUE);
                break;
            default:
                wordText.setBackgroundColor(Color.YELLOW);
        }

        super.clickPicHearAudio(view);
        enableAdvanceArrow();
    }

    public void clickPicHearAudio(View view) {
        ImageView wordImageView = findViewById(R.id.wordImage);
        super.clickPicHearAudio(view);
        wordImageView.setClickable(true);
    }

    public void goToNextWord(View view) {
        initializeForNewWord();
    }

    private void playWordAudioAndRevealWordRow() {


        for (int i = 0; i < visibleTileButtons; i++) {
            TextView tileButton = findViewById(GAME_BUTTONS[i]);
            if (tileButton != null) tileButton.setClickable(false);
        }

        currentPhase = 1;
        showWordRow();
        disableAdvanceArrow();
    }

    protected void fixConstraintsRTL(int gameID) {
        ConstraintLayout constraintLayout = findViewById(gameID);
        ConstraintSet constraintSet = new ConstraintSet();
        if (constraintLayout != null) {
            constraintSet.clone(constraintLayout);

            constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.instructions, ConstraintSet.START, 0);
            constraintSet.connect(R.id.instructions, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
            constraintSet.connect(R.id.forwardArrowImage, ConstraintSet.START, R.id.instructions, ConstraintSet.END, 0);
            constraintSet.connect(R.id.instructions, ConstraintSet.END, R.id.forwardArrowImage, ConstraintSet.START, 0);

            constraintSet.applyTo(constraintLayout);
        }
    }
}