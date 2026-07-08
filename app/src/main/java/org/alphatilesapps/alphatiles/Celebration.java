package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;


public class Celebration extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (gameSounds == null) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            gameSounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
            correctFinalSoundID = gameSounds.load(this, R.raw.zz_correct_final, 1);
        }

        // Disable back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Intentionally empty — do nothing
            }
        });

        context = this;
        setContentView(R.layout.celebration);

        ActivityLayouts.applyEdgeToEdge(this, R.id.celebrationCL);
        ActivityLayouts.setStatusAndNavColors(this);

        gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 2, 0, 1.0f);

    }


    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }

}
