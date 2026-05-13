package org.alphatilesapps.alphatiles;

import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

@RunWith(AndroidJUnit4.class)
public class KeyboardTest {
    @Test
    public void keyboardTest() {
        try (ActivityController<Start> controller = Robolectric.buildActivity(Start.class)) {
            controller.setup();
        }
        DummyStart.initAudios();
    }

    public static void play(GameActivity activity, int[] keys, String target, int[] rounds) {

        for (int key : keys) {
            TextView keyView = activity.findViewById(key);
            if (target.startsWith(keyView.getText().toString())) {

            }
        }
    }
}
