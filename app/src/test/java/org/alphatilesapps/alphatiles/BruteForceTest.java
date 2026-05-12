package org.alphatilesapps.alphatiles;

import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class BruteForceTest {
    public static void testGeorgia() {
        for (int challengeLevel = 1; challengeLevel <= 12; challengeLevel++) {
            try (ActivityController<Georgia> controller = Launcher.launch(Georgia.class, challengeLevel)) {
                controller.setup();
                Georgia activity = controller.get();
                int[] buttons = Arrays.copyOfRange(activity.getGameButtons(), 0, activity.visibleGameButtons);
                play(activity, buttons, 12);
            }
        }
        if (!Start.hasSyllableGames) return;
        for (int challengeLevel = 1; challengeLevel <= 6; challengeLevel++) {
            try (ActivityController<Georgia> controller = Launcher.launch(Georgia.class, challengeLevel, 1, "S")) {
                controller.setup();
                Georgia activity = controller.get();
                int[] buttons = Arrays.copyOfRange(activity.getGameButtons(), 0, activity.visibleGameButtons);
                play(activity, buttons, 12);
            }
        }
    }

    public static void testThailand() {
        List<Integer> refs = new ArrayList<>(Arrays.asList(1, 2, 4, 5, 6));
        List<Integer> choices = new ArrayList<>(Arrays.asList(1, 2, 4, 5));

        if (Start.hasTileAudio) {
            refs.add(3);
        }
        if (Start.hasSyllableGames) {
            refs.add(7);
            choices.add(7);
        }
        if (Start.hasSyllableAudio) {
            refs.add(8);
        }
        Set<Integer> skip = Set.of(17, 27, 37, 47, 57, 67, 71, 72, 81, 82);
        for (int ref : refs) {
            for (int choice : choices) {
                int challengeLevel = 10 * ref + choice;
                if (skip.contains(challengeLevel)) continue;
                for (int i = 1; i <= 2; i++) {
                    try (ActivityController<Thailand> controller = Launcher.launch(Thailand.class, i * 100 + challengeLevel)) {
                        controller.setup();
                        Thailand activity = controller.get();
                        play(activity, activity.getGameButtons(), 12);
                    }
                }
            }
        }
    }

    @Test
    public void bruteForceTest() {
        try (ActivityController<Start> controller = Robolectric.buildActivity(Start.class)) {
            controller.setup();
        }
        DummyStart.initAudios();
        testThailand();
        testGeorgia();
    }

    public static void play(GameActivity activity, int[] buttons, int rounds) {
        for (int round = 0; round < rounds; round++) {
            assert activity.repeatLocked;
            for (int id : buttons) {
                View button = activity.findViewById(id);
                button.performClick();
            }
            assert !activity.repeatLocked;
            activity.findViewById(R.id.repeatImage).performClick();
        }
    }
}
