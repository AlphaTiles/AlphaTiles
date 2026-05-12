package org.alphatilesapps.alphatiles;

import android.content.Intent;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class Launcher {
    public static <A extends GameActivity> ActivityController<A> launch(Class<A> cls, int challengeLevel, int stage, String mode) {
        Intent intent = new Intent();
        intent.putExtra("challengeLevel", challengeLevel);
        intent.putExtra("country", cls.getName());
        intent.putExtra("syllableGame", mode);
        intent.putExtra("stage", stage);
        intent.putExtra("gameNumber", 1);
        return Robolectric.buildActivity(cls, intent);
    }

    public static <A extends GameActivity> ActivityController<A> launch(Class<A> cls, int challengeLevel) {
        return launch(cls, challengeLevel, 1, "T");
    }
}
