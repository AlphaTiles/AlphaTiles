package org.alphatilesapps.validator;

import java.util.ArrayList;
import java.util.HashSet;

import org.alphatilesapps.validator.Validator.TileList;

public class StagesChecks {
    public static String check(
            ArrayList<Validator.Word> wordList,
            TileList tileList,
            double stageCorrespondenceRatio,
            boolean firstTileCorrespondence
    ) {
        int maxStage = 7;
        int[] wordCounts = new int[maxStage];
        for(int idx = 0; idx < wordList.size(); ++idx) {
            Validator.Word word = wordList.get(idx);
            float[] correspondences = new float[maxStage];
            try {
                int stage = Integer.parseInt(word.stageOfFirstAppearance);
                for(int i = stage - 1; i < maxStage; ++i) {
                    correspondences[i] = 1.0f;
                }
            } catch(Exception ignored) {
                ArrayList<Validator.Tile> tiles = tileList.parseWordIntoTiles(word);
                if(tiles == null) {
                    continue;
                }
                for(int stage = 1; stage <= maxStage; ++stage) {
                    int corresponding = 0;
                    if(firstTileCorrespondence && tiles.get(0).stageOfFirstAppearanceForThisTileType > stage) {
                        correspondences[stage - 1] = 0.0f;
                        continue;
                    }
                    for(Validator.Tile tile : tiles) {
                        if(tile.stageOfFirstAppearanceForThisTileType <= stage) {
                            corresponding += 1;
                        }
                    }
                    correspondences[stage - 1] = corresponding / (float) tiles.size();
                }
            }
            for(int i = 0; i < maxStage; ++i) {
                if(correspondences[i] >= stageCorrespondenceRatio) {
                    wordCounts[i]++;
                }
            }
        }
        HashSet<String> seenTiles = new HashSet<>();
        int[] tileCounts = new int[maxStage];
        for(Validator.Tile tile : tileList) {
            String key = tile.text + "-" + tile.typeOfThisTileInstance;
            if(seenTiles.contains(key)) {
                continue;
            } else {
                seenTiles.add(key);
            }
            if(tile.stageOfFirstAppearanceForThisTileType <= maxStage) {
                tileCounts[tile.stageOfFirstAppearanceForThisTileType - 1]++;
            }
        }
        StringBuilder str = new StringBuilder();
        int tileCount = 0;
        for(int i = 0; i < maxStage; i++) {
            tileCount += tileCounts[i];
            str.append("Stage ");
            str.append(i + 1);
            str.append(": ");
            str.append(wordCounts[i]);
            str.append(" words ");
            if(i > 0) {
                str.append("(");
                str.append(wordCounts[i] - wordCounts[i - 1]);
                str.append(" new), ");
            }
            str.append(tileCount);
            str.append(" tiles");
            if(i > 0) {
                str.append(" (");
                str.append(tileCounts[i]);
                str.append(" new)");
            }
            str.append("\n");
               
        }
        return str.toString();
    }
}
