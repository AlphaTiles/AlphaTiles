package org.alphatilesapps.alphatiles;

import android.media.SoundPool;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
public class GameTest {
    @Before
    public void initialize() {
        try (ActivityController<Start> controller = Robolectric.buildActivity(Start.class)) {
            controller.setup();
        }
        initAudios();
    }

    public static void initAudios() {
        Start.tileAudioIDs = new HashMap<>();
        Start.tileDurations = new HashMap<>();
        for (Start.Tile tile : Start.tileList) {
            Start.tileAudioIDs.put(tile.audioForThisTileType, -1);
            Start.tileDurations.put(tile.audioForThisTileType, 0);
        }
        Start.syllableAudioIDs = new HashMap<>();
        if (Start.syllableList != null) {
            for (Start.Syllable syl : Start.syllableList) {
                Start.syllableAudioIDs.put(syl.audioName, -1);
            }
        }
        Start.wordAudioIDs = new HashMap<>();
        for (Start.Word word : Start.wordList) {
            Start.wordAudioIDs.put(word.wordInLWC, -1);
        }
        Start.gameSounds = new SoundPool.Builder().build();
    }
}
