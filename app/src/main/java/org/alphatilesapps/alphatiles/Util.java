package org.alphatilesapps.alphatiles;

import android.graphics.Paint;
import android.graphics.Rect;

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
    public static float getMinFontSize(String[] strings, float baseline) {
        Paint paint = new Paint();
        float size = 100;
        paint.setTextSize(size);
        float min = 10000;
        for(String s : strings) {
            Rect r = new Rect();
            paint.getTextBounds(s, 0, s.length(), r);
            float width = r.width();
            float ascent = r.top;
            float descent = r.bottom;
            float descentScale = Math.abs((size - baseline * size) / descent);
            float ascentScale = Math.abs(baseline * size / ascent);
            float widthScale = size / width;
            float scale = Math.min(descentScale, Math.min(ascentScale, widthScale));
            LOGGER.log(Level.INFO, s + " " + scale);
            min = Math.min(scale, min);
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

}