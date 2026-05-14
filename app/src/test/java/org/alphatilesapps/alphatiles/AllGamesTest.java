package org.alphatilesapps.alphatiles;

import org.junit.Test;
import org.robolectric.android.controller.ActivityController;

public class AllGamesTest extends GameTest {
    @Test
    public void allGames() throws ClassNotFoundException {
        String project = "org.alphatilesapps.alphatiles.";
        for (Start.Game game : Start.gameList) {
            // If there's a way to not need this I didn't find it
            @SuppressWarnings(value = "unchecked")
            Class<? extends GameActivity> cls = (Class<? extends GameActivity>) Class.forName(project + game.country);
            int challengeLevel = Integer.parseInt(game.level);
            int stage = 1;
            if (!game.stage.equals("-")) {
                stage = Integer.parseInt(game.stage);
            }
            try (ActivityController<? extends GameActivity> controller = Launcher.launch(cls, challengeLevel, stage, game.mode)) {
                controller.setup();
            }
        }
    }
}
