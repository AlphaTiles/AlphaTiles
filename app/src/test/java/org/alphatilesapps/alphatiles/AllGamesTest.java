package org.alphatilesapps.alphatiles;

import android.app.Activity;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

@RunWith(AndroidJUnit4.class)
public class AllGamesTest {
    @Test
    public void allGames() throws ClassNotFoundException {
        try(ActivityController<Start> controller = Robolectric.buildActivity(Start.class)) {
            controller.setup();
        }
        DummyStart.initAudios();
        String project = "org.alphatilesapps.alphatiles.";
        for(Start.Game game : Start.gameList) {
            // If there's a way to not need this I didn't find it
            @SuppressWarnings(value = "unchecked")
            Class<? extends GameActivity> cls = (Class<? extends GameActivity>) Class.forName(project + game.country);
            Intent intent = new Intent();
            int challengeLevel = Integer.parseInt(game.level);
            int stage = 1;
            if(!game.stage.equals("-")) {
                stage = Integer.parseInt(game.stage);
            }
            intent.putExtra("challengeLevel", challengeLevel);
            intent.putExtra("country", game.country);
            intent.putExtra("syllableGame", game.mode);
            intent.putExtra("stage", stage);
            intent.putExtra("gameNumber", 1);
            try(ActivityController<? extends GameActivity> controller = Robolectric.buildActivity(cls, intent)) {
                controller.setup();
            }
        }
    }
}
