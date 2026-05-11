package org.alphatilesapps.alphatiles;

import android.media.SoundPool;

import java.util.HashMap;

public class DummyStart {
    public static void initAudios() {
        Start.tileAudioIDs = new HashMap<>();
        Start.tileDurations = new HashMap<>();
        for(Start.Tile tile : Start.tileList) {
            Start.tileAudioIDs.put(tile.audioForThisTileType, -1);
            Start.tileDurations.put(tile.audioForThisTileType, 0);
        }
        Start.syllableAudioIDs = new HashMap<>();
        for(Start.Syllable syl : Start.syllableList) {
            Start.syllableAudioIDs.put(syl.audioName, -1);
        }
        Start.wordAudioIDs = new HashMap<>();
        for(Start.Word word : Start.wordList) {
            Start.wordAudioIDs.put(word.wordInLWC, -1);
        }
        Start.gameSounds = new SoundPool.Builder().build();
    }
}
