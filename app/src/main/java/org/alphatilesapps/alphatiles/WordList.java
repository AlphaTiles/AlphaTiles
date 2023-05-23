package org.alphatilesapps.alphatiles;

import android.os.Bundle;

public class WordList extends GameActivity{


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
        return new int[0];
    }

    @Override
    protected int getAudioInstructionsResID() {
        return 0;
    }

    @Override
    protected void centerGamesHomeImage() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.word_list);
    }
}
