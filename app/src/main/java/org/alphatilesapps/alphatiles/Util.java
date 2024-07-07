package org.alphatilesapps.alphatiles;

import static org.alphatilesapps.alphatiles.Start.after12checkedTrackers;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.tileSoundIDs;
import static org.alphatilesapps.alphatiles.Start.tileDurations;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;

import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

    static int parsedWordTileLength = 0;  // This is an exact requirement, no more, no less; each word in the LOP (language of play) has an exact length in game tiles

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    public static String returnPlayerStringToAppend(int playerNumber) {

        String appendedPlayerNumber;

        if (playerNumber < 10) {
            appendedPlayerNumber = "0" + playerNumber;
        } else {
            appendedPlayerNumber = String.valueOf(playerNumber);
        }

        return appendedPlayerNumber;

    }
    /// Gets the minimum font size required to fit all given strings in a 1px area
    public static float getMinFontSize(String[] strings) {
        Paint paint = new Paint();
        float size = 1000;
        paint.setTextSize(size);
        float min = 0.5f;
        for(String s : strings) {
            Rect r = new Rect();
            paint.getTextBounds(s, 0, s.length(), r);
            float width = r.width();
            float widthScale = size / width;
            min = Math.min(widthScale, min);
        }
        LOGGER.log(Level.INFO, min + "");
        return min;
    }
    public static void logMemory() {
        // https://stackoverflow.com/questions/3571203/what-are-runtime-getruntime-totalmemory-and-freememory
        float dataSize = 1024 * 1024;
        float maxMemory = Runtime.getRuntime().maxMemory(); // Total designated memory, this will equal the configured -Xmx value
        float freeMemory = Runtime.getRuntime().freeMemory(); // Current allocated free memory, is the current allocated space ready for new objects. Caution this is not the total free available memory
        float totalMemory = Runtime.getRuntime().totalMemory(); // Total allocated memory: total allocated space reserved for the java process
        float usedMemory = totalMemory - freeMemory; //
        float totalFreeMemory = maxMemory - usedMemory;
        float unallocatedMemory = maxMemory - totalMemory;

        LOGGER.info("Remember: usedMemory = " + usedMemory / dataSize + " MB");
        LOGGER.info("Remember: .......................freeMemory = " + freeMemory / dataSize + " MB");
        LOGGER.info("Remember: ..............................................totalMemory = " + totalMemory / dataSize + " MB");
        LOGGER.info("Remember: .....................................................................unallocatedMemory = " + unallocatedMemory / dataSize + " MB");
        LOGGER.info("Remember: ............................................................................................maxMemory = " + maxMemory / dataSize + " MB");

    }

    public static void playTileAudio(final boolean playFinalSound, GameActivity activity, Start.Tile tile) {
        if(activity.mediaPlayerIsPlaying) {
            return;
        }

        // checks if the tile's audio string has a corresponding sound ID
        if (!tileSoundIDs.containsKey(tile.audioForThisTileType)) {
            return;
        }

        // checks if the activity contains the resource ID for the tile's audio name
        int resID;
        try{
            resID = activity.getResources().getIdentifier(tile.audioForThisTileType, "raw", activity.getPackageName());
        } catch (NullPointerException e) {
            return;
        }

        activity.setAllGameButtonsUnclickable();
        activity.setOptionsRowUnclickable();

        if (tempSoundPoolSwitch) {
            playTileAudioUsingSoundPool(playFinalSound, activity, tile);
        } else {
            playTileAudioUsingMediaPlayer(playFinalSound, activity, resID);
        }
    }

    private static void playTileAudioUsingMediaPlayer(final boolean playFinalSound, GameActivity activity, int tileAudioResID) {     //JP: for Media Player; tile audio

        final MediaPlayer mp1 = MediaPlayer.create(activity, tileAudioResID);
        activity.mediaPlayerIsPlaying = true;

        mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                activity.mpCompletion(mp1, playFinalSound);
            }
        });
        mp1.start();
    }

    private static void playTileAudioUsingSoundPool(final boolean playFinalSound, GameActivity activity, Start.Tile tile) {     //JP: for SoundPool, for tile audio

        gameSounds.play(tileSoundIDs.get(tile.audioForThisTileType), 1.0f, 1.0f, 2, 0, 1.0f);

        activity.soundSequencer.postDelayed(new Runnable() {
            public void run() {
                if (playFinalSound) {
                    activity.updatePointsAndTrackers(0);
                    activity.repeatLocked = false;
                    activity.playCorrectFinalSound();
                } else {
                    if (activity.repeatLocked) {
                        activity.setAllGameButtonsClickable();
                    }
                    if (after12checkedTrackers == 1){
                        activity.setOptionsRowClickable();
                        // JP: In setting 1, the player can always keep advancing to the next tile/word/image
                    }
                    else if (activity.trackerCount >0 && activity.trackerCount % 12 != 0) {
                        activity.setOptionsRowClickable();
                        // Otherwise, updatePointsAndTrackers will set it clickable only after
                        // the player returns to earth (2) or sees the celebration screen (3)
                    }
                }
            }
        }, tileDurations.get(tile.audioForThisTileType));
    }

}