package org.alphatilesapps.alphatiles;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.Robolectric;

import java.util.HashMap;


@RunWith(AndroidJUnit4.class)
public class ThailandTest {
    @Test
    public void thailandTestTest() {
        try(ActivityController<Start> controller = Robolectric.buildActivity(Start.class)) {
            controller.setup();
        }
        DummyStart.initAudios();

        Intent intent = new Intent();
        intent.putExtra("challengeLevel", 211);
        intent.putExtra("gameNumber", 1);

        try(ActivityController<Thailand> controller = Robolectric.buildActivity(Thailand.class, intent)) {
            controller.setup();
            Thailand activity = controller.get();
            activity.findViewById(R.id.choice01).performClick();
        }
    }

}
