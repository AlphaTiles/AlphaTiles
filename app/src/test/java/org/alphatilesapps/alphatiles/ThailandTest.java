package org.alphatilesapps.alphatiles;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.Robolectric;

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
            for(int i = 0; i < 12; i++) {
                activity.findViewById(R.id.choice01).performClick();
                activity.findViewById(R.id.choice02).performClick();
                activity.findViewById(R.id.choice03).performClick();
                activity.findViewById(R.id.choice04).performClick();
                assert !activity.repeatLocked;
                activity.findViewById(R.id.repeatImage).performClick();
                assert activity.repeatLocked;
            }
        }
    }

}
