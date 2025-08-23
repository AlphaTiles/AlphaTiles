package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import static org.alphatilesapps.alphatiles.Start.enhancedAudioLoadingLog;
import static org.alphatilesapps.alphatiles.Start.hasSyllableAudio;
import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;
import static org.alphatilesapps.alphatiles.Start.wordList;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.totalAudio;
import static org.alphatilesapps.alphatiles.Start.tileAudioIDs;
import static org.alphatilesapps.alphatiles.Start.tileList;
import static org.alphatilesapps.alphatiles.Start.langInfoList;
import static org.alphatilesapps.alphatiles.Start.tileDurations;
import static org.alphatilesapps.alphatiles.Start.hasTileAudio;
import static org.alphatilesapps.alphatiles.Start.syllableAudioIDs;
import static org.alphatilesapps.alphatiles.Start.syllableList;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.incorrectSoundID;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;
import static org.alphatilesapps.alphatiles.Start.correctSoundDuration;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class LoadingScreen extends AppCompatActivity {

    //JP June 2022: moved loading of all SoundPool audio into this activity
    //note: audio instructions use MediaPlayer, not SoundPool

    private Handler mHandler;
    Context context;
    ProgressBar progressBar;

    private static final Logger LOGGER = Logger.getLogger( LoadingScreen.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        ActivityLayouts.applyEdgeToEdge(this, R.id.activityloadingscreenCL);
        ActivityLayouts.setStatusAndNavColors(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mHandler = new Handler(Looper.getMainLooper());

        progressBar = findViewById(R.id.progressBar);
        String scriptDirection = langInfoList.find("Script direction (LTR or RTL)");
        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
            progressBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            forceLTRIfSupported();
        }
        context = this;

        TextView gameNameView = findViewById(R.id.gameName);
        String localAppName = langInfoList.find("Game Name");
        gameNameView.setText(localAppName);

        TextView langPackNameView = findViewById(R.id.langPackName);
        langPackNameView.setText(BuildConfig.FLAVOR);

        TextView versionNoView = findViewById(R.id.versionNo);
        versionNoView.setText(BuildConfig.VERSION_NAME);

        Intent intent = new Intent(this, ChoosePlayer.class);

        // load audio in background threads to avoid blocking UI thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadGameAudio();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadWordAudio();
            }
        }).start();

        if (hasTileAudio) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadTileAudio();
                }
            }).start();
        }

        if (hasSyllableAudio) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadSyllableAudio();
                }
            }).start();
        }

        //JP: alpha tiles colors separated into r,g,b
        //ex: the first color 6200EE corresponds to 98, 0, 1 in the 0 index of each array
        //for the progress bar
        int[] reds = {98, 55, 3, 0, 156, 33, 244, 76, 233};
        int[] greens = {0, 0, 218, 255, 39, 150, 67, 175, 30};
        int[] blues = {238, 179, 197, 0, 176, 243, 54, 80, 99};

        final int[] color_index = {0};
        final int[] mod_color = {0};

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                color_index[0]++; //JP: used w/mod_color to iterate through colors in reds, greens,
                // blues arrays
                mod_color[0] = color_index[0] % 9; //JP: 9 alpha tiles colors in use
                // (removed yellow and white), so indices 0-8
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        int color = Color.rgb(reds[mod_color[0]], greens[mod_color[0]], blues[mod_color[0]]);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
                            progressBar.getProgressDrawable().setColorFilter(
                                    new BlendModeColorFilter(color, BlendMode.SRC_IN));
                        } else {
                            progressBar.getProgressDrawable().setColorFilter(
                                    color,
                                    android.graphics.PorterDuff.Mode.SRC_IN);
                        }

                    }
                });
            }
        }, 0, 1500);//wait 0 ms before doing the action and do it every 1500ms (1.5 sec)

        final int[] audio_loaded = {0}; //JP: tracks how many audio files have already been loaded
        gameSounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            float percentage = 0.0F;

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                //JP: this function is called when ANY audio file in the gameSounds SoundPool
                //has finished loading, regardless of what activity that sound was loaded in

                audio_loaded[0]++; //JP: tracks how many audio files have been loaded so far

                percentage = ((float) audio_loaded[0] / (float) totalAudio) * 100;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) percentage);
                    }
                });

                //once all audio files have finished loading, launch ChoosePlayer
                if (audio_loaded[0] == totalAudio) {
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void loadWordAudio() {
        // load speech sounds
        Resources res = context.getResources();
        wordAudioIDs = new HashMap<>();

        int i = 0;
        for (Start.Word word : wordList) {
            if (enhancedAudioLoadingLog) {
                i++;
                LOGGER.info("LoadProgress: next task: load word audio " + i + " of " + wordList.size() + ": " + word.wordInLWC + " (" + word.wordInLOP + ")");
            }
            int resId = res.getIdentifier(word.wordInLWC, "raw", context.getPackageName());
            int duration = getAssetDuration(resId) + 100;
            wordAudioIDs.put(word.wordInLWC, gameSounds.load(context, resId, 1));
            word.duration = duration;
        }
        LOGGER.info("LoadProgress: completed loadWordAudio()");
    }

    public void loadSyllableAudio() {
        Resources res = context.getResources();
        syllableAudioIDs = new HashMap<>();

        int i = 0;
        for (Start.Syllable syllable : syllableList) {
            if (enhancedAudioLoadingLog) {
                i++;
                LOGGER.info("LoadProgress: next task: load syllable audio " + i + " of " + syllableList.size() + ": " + syllable.text + " (" + syllable.audioName + ")");
            }
            int resId = res.getIdentifier(syllable.audioName, "raw", context.getPackageName());
            int duration = getAssetDuration(resId) + 100;
            syllableAudioIDs.put(syllable.audioName, gameSounds.load(context, resId, 2));
            syllable.duration = duration;
        }
        LOGGER.info("LoadProgress: completed loadSyllableAudio()");
    }

    public void loadTileAudio() {
        Resources res = context.getResources();
        tileAudioIDs = new HashMap<>(0);
        tileDurations = new HashMap<>();

        int i = 0;
        for (Start.Tile tile : tileList) {
            if (enhancedAudioLoadingLog) {
                i++;
                LOGGER.info("LoadProgress: next task: load tile audio " + i + " of " + tileList.size() + ": " + tile.text + " (" + tile.audioName + ")");
            }
            if(!tile.audioForThisTileType.equals("zz_no_audio_needed")) {
                int resId = res.getIdentifier(tile.audioForThisTileType, "raw", context.getPackageName());
                int duration = getAssetDuration(resId) + 100;
                tileAudioIDs.put(tile.audioForThisTileType, gameSounds.load(context, resId, 2));
                tileDurations.put(tile.audioForThisTileType, duration);
            } else {
                totalAudio--;
            }

        }
        LOGGER.info("LoadProgress: completed loadTileAudio()");
    }

    public void loadGameAudio() {
        // load music sounds
        if (enhancedAudioLoadingLog) {
            LOGGER.info("LoadProgress: next task: load game audio 1 of 3: zz_correct.mp3");
        }
        correctSoundID = gameSounds.load(context, R.raw.zz_correct, 3);

        if (enhancedAudioLoadingLog) {
            LOGGER.info("LoadProgress: next task: load game audio 2 of 3: zz_incorrect.mp3");
        }
        incorrectSoundID = gameSounds.load(context, R.raw.zz_incorrect, 3);

        if (enhancedAudioLoadingLog) {
            LOGGER.info("LoadProgress: next task: load game audio 3 of 3: zz_correct_final.mp3");
        }
        correctFinalSoundID = gameSounds.load(context, R.raw.zz_correct_final, 1);

        correctSoundDuration = getAssetDuration(R.raw.zz_correct) + 200;
        //		incorrectSoundDuration = getAssetDuration(R.raw.zz_incorrect);	// not needed atm
        //		correctFinalSoundDuration = getAssetDuration(R.raw.zz_correct_final);	// not needed atm
        LOGGER.info("LoadProgress: completed loadGameAudio()");
    }


    private int getAssetDuration(int assetID) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(assetID);
        mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    private void forceRTLIfSupported() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    private void forceLTRIfSupported() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

}


