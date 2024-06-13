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

        // copy and pasted from https://developer.android.com/guide/navigation/custom-back#java
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // do nothing
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        context = this;
        setContentView(R.layout.celebration);

        gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 2, 0, 1.0f);

    }


    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }

}
