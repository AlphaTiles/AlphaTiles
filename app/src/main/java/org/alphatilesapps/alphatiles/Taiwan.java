package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.COLORS;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.gameSounds;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Random;

public class Taiwan extends GameActivity{

    private ArrayList<ArrayList<Start.Word>> wordPages = new ArrayList<>();
    private final int NUM_Per_Page = 9;
    int numPages;
    int currentPage = 0;

    protected static final int[] TILE_BUTTONS = {
            R.id.choice01, R.id.choice02, R.id.choice03, R.id.choice04, R.id.choice05, R.id.choice06,
            R.id.choice07, R.id.choice08, R.id.choice09,
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
        android.content.res.Resources res = context.getResources();
        int audioInstructionsResID;
        try {
            audioInstructionsResID = res.getIdentifier(Start.gameList.get(gameNumber - 1).gameInstrLabel, "raw", context.getPackageName());
        } catch (Resources.NotFoundException e) {
            audioInstructionsResID = -1;
        }
        return audioInstructionsResID;
    }

    @Override
    protected void centerGamesHomeImage() {

        int gameID = R.id.taiwanCl;

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
        setContentView(R.layout.taiwan);
        this.createWordPages();
        this.setChoiceBlocks();

    }
    private void createWordPages(){
        this.numPages = (int) Math.ceil(Start.wordList.size() / (double) NUM_Per_Page);
        for (int i = 0; i < this.numPages; i++){
            wordPages.add(new ArrayList<>());
        }
        for (int j = 0; j< Start.wordList.size(); j++){
            this.wordPages.get(j / NUM_Per_Page).add(Start.wordList.get(j));
        }
    }
    private void setChoiceBlocks(){
        for (int i = 0; i < NUM_Per_Page; i ++){
            TextView tileButton = (TextView) findViewById(getTileButtons()[i]);
            if (i < this.wordPages.get(this.currentPage).size()) {
                tileButton.setText(this.wordPages.get(this.currentPage).get(i).localWord);
                String refColorStr = Start.COLORS.get(i % 5);
                tileButton.setBackgroundColor(Color.parseColor(refColorStr));
                tileButton.setVisibility(View.VISIBLE);
            }
            else {
                tileButton.setVisibility(View.INVISIBLE);
            }
        }
    }
    public void pageLeft(View view) {
        if (this.currentPage > 0){
            currentPage -= 1;
            this.setChoiceBlocks();
        }
    }

    public void pageRight(View view) {
        if (this.currentPage < (this.numPages - 1)){
            currentPage += 1;
            this.setChoiceBlocks();
        }
    }

    public void onChoiceClick(View view){
        super.wordInLWC = this.wordPages.get(this.currentPage).get(
                Integer.parseInt((String) view.getTag())).nationalWord;
        super.clickPicHearAudio(view);
    }

    public void playAudioInstructions(View view) {
        if (getAudioInstructionsResID() > 0) {
            super.playAudioInstructions(view);
        }
    }


}
