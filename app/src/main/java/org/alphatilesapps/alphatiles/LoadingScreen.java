package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;

import java.util.HashMap;
import java.util.logging.Logger;

public class LoadingScreen extends AppCompatActivity {

    //JP: moved loading of word audio into this activity

    private static final Logger LOGGER = Logger.getLogger( Start.class.getName() );
    private Handler mHandler = new Handler();
    Context context;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        LOGGER.info("Loading Screen launched");

        progressBar = findViewById(R.id.progressBar);
        context = this;


        int num_of_words = wordList.size();
        Intent intent = new Intent(this, ChoosePlayer.class);

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadWordAudio(0, num_of_words);
            }
        }).start();

        // do I need to worry about a race condition?
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                totalLoadingProgress();
            }
        }).start();

         */
        final int[] words_loaded = {0};
        gameSounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){

           float percentage = 0.0F;

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
            {
                words_loaded[0]++;
                percentage = ((float) words_loaded[0] / (float) wordList.size()) * 100;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) percentage);
                    }
                });
                if (words_loaded[0] == wordList.size()){
                    startActivity(intent);

                    finish();
                }
            }});



    }

    public void loadWordAudio(int start, int end) {
        // load speech sounds
        Resources res = context.getResources();
        wordAudioIDs = new HashMap();
        //final float[] percentage = {0.0F};

        for (int i = start; i < end; i++)
        {
            int resId = res.getIdentifier(wordList.get(i).nationalWord, "raw", context.getPackageName());
            wordAudioIDs.put(wordList.get(i).nationalWord, gameSounds.load(context, resId, 1));

            //Each time a sound is loaded in the SoundPool, the OnLoadComplete listener is triggered.
            // If you have more than one sound clip, you should keep track of which ones have been loaded.
            /*
            gameSounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
                {
                    percentage[0] = (float) finalI / (float)end;
                    float finalPercentage = percentage[0] *100;
                }});

             */
            // some samples still not ready
            // how to make it wait for loading to finish? - onLoadCompleteListener but how to use for
            // one sample?


        }
    }
}


