package org.alphatilesapps.alphatiles;

import android.content.Intent;

import org.alphatilesapps.alphatiles.ChoosePlayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.wordList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LoadingScreen extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.loading_screen);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        float percentage = 0.0F;
        int one = 0;
        int two = 0;

        while (percentage != 1.0F){
            progressBar.setProgress((int) percentage);
            if (!wordAudioIDs.isEmpty()){
                one = wordAudioIDs.size();
                two = wordList.size();
                percentage = (float)one / (float)two;
            }
        }

        Intent intent = new Intent(this, ChoosePlayer.class);

        startActivity(intent);
    }
}
