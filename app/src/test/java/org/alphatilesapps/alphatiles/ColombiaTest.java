package org.alphatilesapps.alphatiles;

import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.robolectric.android.controller.ActivityController;

public class ColombiaTest extends GameTest {
    @Test
    public void colombiaTest() {
        for (int challengeLevel = 1; challengeLevel <= 3; challengeLevel++) {
            play(challengeLevel, "T", 12);
            play(challengeLevel, "S", 12);
        }
    }

    public void play(int challengeLevel, String mode, int rounds) {
        try (ActivityController<Colombia> controller = Launcher.launch(Colombia.class, challengeLevel, 1, mode)) {
            controller.setup();
            Colombia activity = controller.get();
            int[] buttons = activity.getGameButtons();
            View kbBack = activity.findViewById(buttons[buttons.length - 2]);
            View kbForward = activity.findViewById(buttons[buttons.length - 1]);
            for (int i = 0; i < rounds; i++) {
                String target = Start.wordList.stripInstructionCharacters(activity.refWord.wordInLOP);
                int screens = activity.totalScreens;
                if (screens == 0) screens = 1;
                while (!target.isEmpty()) {
                    TextView longest = null;
                    int length = -1;
                    int targetScreen = -1;
                    for (int screen = 1; screen <= screens; screen++) {
                        for (int idx = 0; idx < activity.visibleGameButtons; idx++) {
                            TextView keyView = activity.findViewById(buttons[idx]);
                            String text = keyView.getText().toString();
                            if (!text.isEmpty()) {
                                if (target.startsWith(text)) {
                                    if (text.length() > length) {
                                        length = text.length();
                                        longest = keyView;
                                        targetScreen = screen;
                                    }
                                }
                            }

                        }
                        if (screens > 1)
                            kbForward.performClick();
                    }
                    assert longest != null;
                    for (int screen = screens; screen > targetScreen; screen--) {
                        kbBack.performClick();
                    }
                    assert longest.isClickable();
                    longest.performClick();
                    target = target.substring(longest.getText().length());
                    if (target.isEmpty()) break;
                    for (int screen = screens; screen > 1; screen--) {
                        kbBack.performClick();
                    }
                }
                assert !activity.repeatLocked;
                activity.findViewById(R.id.repeatImage).performClick();
            }
        }

    }
}
