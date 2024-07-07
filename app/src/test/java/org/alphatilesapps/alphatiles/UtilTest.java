package org.alphatilesapps.alphatiles;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UtilTest {

    @Test
    public void test_returnPlayerStringToAppend() {

        // This is probably invalid. Should expect "-01" or throw?
        assertEquals("0-1", Util.returnPlayerStringToAppend(-1));

        assertEquals("00", Util.returnPlayerStringToAppend(0));
        assertEquals("09", Util.returnPlayerStringToAppend(9));
        assertEquals("10", Util.returnPlayerStringToAppend(10));
    }

    @Test
    public void test_playTileAudio_tileHasNoAudio() {
        //shouldn't crash

        //also, it should do nothing
    }

    @Test
    public void test_playTileAudio_nonMultitypeSymbol() {
        // should play its audio
    }

    @Test
    public void test_playTileAudio_multitypeSymbol_differentiatesIsFalse() {
        // plays the audio tile from the first audio file name column (and ignores others)
    }

    @Test
    public void test_playTileAudio_multitypeSymbol_differentiatesIsTrue() {
        // plays the audio tile for the active grouping
    }
}