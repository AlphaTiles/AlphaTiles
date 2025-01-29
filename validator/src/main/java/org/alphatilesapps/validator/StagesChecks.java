package org.alphatilesapps.validator;

import java.util.ArrayList;
import java.util.HashSet;

import org.alphatilesapps.validator.Validator.TileList;

public class StagesChecks {
    public static String check(ArrayList<Validator.Word> wordList, TileList tileList) {
        HashSet<String> seenTiles = new HashSet<>();
        int[] wordCounts = new int[7];
        for(Validator.Word word : wordList) {
            int i;
            try {
                i = Integer.parseInt(word.stageOfFirstAppearance);
            } catch(Exception ignored) {
                int max = 1;
                for(Validator.Tile tile : tileList.parseWordIntoTiles(word)) {
                    if(tile.stageOfFirstAppearanceForThisTileType > max) {
                        max = tile.stageOfFirstAppearanceForThisTileType;
                    }
                }
                i = max;
            }
            if(i > 0 && i < 7) {
                wordCounts[i - 1]++;
            }
        }
        int[] tileCounts = new int[7];
        for(Validator.Tile tile : tileList) {
            if(seenTiles.contains(tile.text)) {
                continue;
            } else {
                seenTiles.add(tile.text);
            }
            int[] abc = new int[3];
            abc[0] = tile.stageOfFirstAppearance;
            abc[1] = tile.stageOfFirstAppearanceB;
            abc[2] = tile.stageOfFirstAppearanceC;
            for(int i : abc) {
                System.out.println(tile.text);
                if(i > 0 && i < 7) {
                    System.out.println(i);
                    tileCounts[i - 1]++;
                }
            }
        }
        StringBuilder str = new StringBuilder();
        int wordCount = 0;
        int tileCount = 0;
        for(int i = 0; i < 7; i++) {
            wordCount += wordCounts[i];
            tileCount += tileCounts[i];
            str.append("Stage ");
            str.append(i + 1);
            str.append(": ");
            str.append(wordCount);
            str.append(" words ");
            if(i > 0) {
                str.append("(");
                str.append(wordCounts[i]);
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
