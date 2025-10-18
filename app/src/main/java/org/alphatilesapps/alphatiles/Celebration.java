package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
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
