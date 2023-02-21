package org.alphatilesapps.alphatiles;

import java.util.logging.Logger;

public class Util {

    static int parsedWordTileLength = 0;  // This is an exact requirement, no more, no less; each word in the LOP (language of play) has an exact length in game tiles

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    public static int tilesInArray(String[] array) {

        int count = 0;
        for (String s : array) {    // RR
            if (s != null) {        // RR
                count++;
            }
        }
        return count;
    }

    public static String returnPlayerStringToAppend(int playerNumber) {

        String appendedPlayerNumber;

        if (playerNumber < 10) {
            appendedPlayerNumber = "0" + playerNumber;
        } else {
            appendedPlayerNumber = String.valueOf(playerNumber);
        }

        return appendedPlayerNumber;

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