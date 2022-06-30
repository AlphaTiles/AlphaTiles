package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.totalAudio;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.tileDurations;
import static org.alphatilesapps.alphatiles.Start.hasTileAudio;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.incorrectSoundID;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;
import static org.alphatilesapps.alphatiles.Start.correctSoundDuration;

import com.google.android.material.progressindicator.LinearProgressIndicator;

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
                loadGameAudio();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadWordAudio(0, num_of_words);
            }
        }).start();

        if (hasTileAudio){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadTileAudio();
                }
            }).start();
        }


        // do I need to worry about a race condition?
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                totalLoadingProgress();
            }
        }).start();

         */
        final int[] audio_loaded = {0};
        gameSounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){

           float percentage = 0.0F;

           //alpha tiles colors
           int[] reds = {98,55,3,0,156,33,244,76,233};
           int[] greens = {0,0,218,255,39,150,67,175,30};
           int[] blues = {238, 179,197,0,176,243,54,80,99};

           int color_index = 0;
           int mod_color = 0;


            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
            {
                audio_loaded[0]++;
                color_index++;
                mod_color = color_index % 8; // 9 alpha tiles colors in use (removed yellow and white)
                percentage = ((float) audio_loaded[0] / (float) totalAudio) * 100;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.getProgressDrawable().setColorFilter(
                                Color.rgb(reds[mod_color],greens[mod_color],blues[mod_color]), android.graphics.PorterDuff.Mode.SRC_IN);
                        progressBar.setProgress((int) percentage);
                    }
                });
                if (audio_loaded[0] == totalAudio){
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

    public void loadTileAudio(){
        Resources res = context.getResources();
        tileAudioIDs = new HashMap(0);
        tileDurations = new HashMap();

        for (Start.Tile tile : tileList) {
            int resId = res.getIdentifier(tile.audioForTile, "raw", context.getPackageName());
            tileAudioIDs.put(tile.baseTile, gameSounds.load(context, resId, 2));
            tileDurations.put(tile.baseTile, tile.tileDuration1 + 100);
//                LOGGER.info("Remember tile.tileDuration1 = " + tile.tileDuration1);

            if (tile.tileTypeB.compareTo("none")!= 0) {
                if (tile.audioForTileB.compareTo("X") != 0) {
                    resId = res.getIdentifier(tile.audioForTileB, "raw", context.getPackageName());
                    tileAudioIDs.put(tile.baseTile + "B", gameSounds.load(context, resId, 2));
                    totalAudio++;
                }
            }
            if(tile.tileTypeC.compareTo("none")!= 0) {
                if (tile.audioForTileC.compareTo("X") != 0) {
                    resId = res.getIdentifier(tile.audioForTileC, "raw", context.getPackageName());
                    tileAudioIDs.put(tile.baseTile + "C", gameSounds.load(context, resId, 2));
                    totalAudio++;
                }
            }

        }
    }

    public void loadGameAudio() {
        // load music sounds
        //^ this constructor is deprecated
        correctSoundID = gameSounds.load(context, R.raw.zz_correct, 3);
        incorrectSoundID = gameSounds.load(context, R.raw.zz_incorrect, 3);
        correctFinalSoundID = gameSounds.load(context, R.raw.zz_correct_final, 1);

        correctSoundDuration = getAssetDuration(R.raw.zz_correct) + 200;
        //		incorrectSoundDuration = getAssetDuration(R.raw.zz_incorrect);	// not needed atm
        //		correctFinalSoundDuration = getAssetDuration(R.raw.zz_correct_final);	// not needed atm
    }

    private int getAssetDuration(int assetID)
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(assetID);
        mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }
}


