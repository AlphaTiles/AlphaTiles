package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;


public class Celebration extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.celebration);

        gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 2, 0, 1.0f);

    }


    public void goBackToEarth(View view)
    {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }



}
