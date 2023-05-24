package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.COLORS;

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
        return 0;
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
        this.numPages = (int) Math.ceil(Start.wordList.size() / 9.0);
        for (int i = 0; i < this.numPages; i++){
            wordPages.add(new ArrayList<>());
        }
        for (int j = 0; j< Start.wordList.size(); j++){
            this.wordPages.get(j % 9).add(Start.wordList.get(j));
        }
    }
    private void setChoiceBlocks(){
        for (int i = 0; i < 9; i ++){
            TextView tileButton = (TextView) findViewById(getTileButtons()[i]);
            tileButton.setText(this.wordPages.get(this.currentPage).get(i).localWord);
            String refColorStr = COLORS.get(i % 5);
            tileButton.setBackgroundColor(Color.parseColor(refColorStr));
        }
    }
    public void pageLeft(View view) {
        if (this.currentPage > 0){
            currentPage -= 1;
            this.setChoiceBlocks();
        }
    }

    public void pageRight(View view) {
        if (this.currentPage < (this.numPages -1)){
            currentPage += 1;
            this.setChoiceBlocks();
        }
    }
}
