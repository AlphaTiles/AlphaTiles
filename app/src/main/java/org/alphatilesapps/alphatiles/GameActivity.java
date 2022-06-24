package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

import static org.alphatilesapps.alphatiles.ChoosePlayer.SHARED_PREFS;
import static org.alphatilesapps.alphatiles.Testing.tempSoundPoolSwitch;
import static org.alphatilesapps.alphatiles.Start.correctFinalSoundID;
import static org.alphatilesapps.alphatiles.Start.correctSoundDuration;
import static org.alphatilesapps.alphatiles.Start.correctSoundID;
import static org.alphatilesapps.alphatiles.Start.gameSounds;
import static org.alphatilesapps.alphatiles.Start.incorrectSoundID;
import static org.alphatilesapps.alphatiles.Start.wordDurations;
import static org.alphatilesapps.alphatiles.Start.wordAudioIDs;


public abstract class GameActivity extends AppCompatActivity {

	// KP, Oct 2020

	Context context;
	String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");

	int points;
	int brazilPoints, colombiaPoints, ecuadorPoints, georgiaPoints, mexicoPoints, myanmarPoints, peruPoints, thailandPoints, unitedStatesPoints;
	int challengeLevel = -1;
	int playerNumber = -1;
	int gameNumber = 0;
	String country;
	int visibleTiles;	
	String className;

	ArrayList<String> parsedWordArrayFinal;

	String wordInLWC = "";    // the lWC word (e.g. Spanish), which exactly matches the image and audio file names
	String wordInLOP = "";    // the corresponding word in the language of play (e.g. Me'phaa)
	int trackerCount = 0;
	boolean mediaPlayerIsPlaying = false;
	boolean repeatLocked = true;
	Handler soundSequencer;

	protected static final int[] TRACKERS = {
			R.id.tracker01, R.id.tracker02, R.id.tracker03, R.id.tracker04, R.id.tracker05, R.id.tracker06, R.id.tracker07, R.id.tracker08, R.id.tracker09, R.id.tracker10,
			R.id.tracker11, R.id.tracker12

	};
	
	protected abstract int[] getTileButtons();	
	protected abstract int[] getWordImages();
	protected abstract int getAudioInstructionsResID();
	protected abstract void centerGamesHomeImage();

	@Override
	protected void onCreate(Bundle state) {
		context = this;
		
		soundSequencer = new Handler(Looper.getMainLooper());	

		points = getIntent().getIntExtra("points", 0);
		brazilPoints = getIntent().getIntExtra("brazilPoints", 0);
		colombiaPoints = getIntent().getIntExtra("colombiaPoints", 0);
		ecuadorPoints = getIntent().getIntExtra("ecuadorPoints", 0);
		georgiaPoints = getIntent().getIntExtra("georgiaPoints", 0);
		mexicoPoints = getIntent().getIntExtra("mexicoPoints", 0);
		myanmarPoints = getIntent().getIntExtra("myanmarPoints", 0);
		peruPoints = getIntent().getIntExtra("peruPoints", 0);
		thailandPoints = getIntent().getIntExtra("brazilPoints", 0);
		unitedStatesPoints = getIntent().getIntExtra("brazilPoints", 0);
		playerNumber = getIntent().getIntExtra("playerNumber", -1);
		challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
		gameNumber = getIntent().getIntExtra("gameNumber", 0);
		country = getIntent().getStringExtra("country");

		className = getClass().getName();

		if(scriptDirection.compareTo("RTL") == 0){
			forceRTLIfSupported();
		}
		else{
			forceLTRIfSupported();
		}

		super.onCreate(state);

	}

	public void goBackToEarth(View view) {
		Intent intent = getIntent();
		intent.setClass(context, Earth.class);	// so we retain the Extras
		startActivity(intent);
		finish();

	}

	public void goBackToChoosePlayer(View view) {

		if (mediaPlayerIsPlaying)
		{
			return;
		}
		startActivity(new Intent(context, ChoosePlayer.class));
		finish();

	}

	public void goToAboutPage(View view) {

		Intent intent = getIntent();
		intent.setClass(context, About.class);
		startActivity(intent);

	}

	protected void updateTrackers() {

		for (int t = 0; t < TRACKERS.length; t++)
		{

			ImageView tracker = findViewById(TRACKERS[t]);
			if (t < trackerCount)
			{
				int resID = getResources().getIdentifier("zz_complete", "drawable", getPackageName());
				tracker.setImageResource(resID);
			}
			else
			{
				int resID2 = getResources().getIdentifier("zz_incomplete", "drawable", getPackageName());
				tracker.setImageResource(resID2);
			}
		}
	}

	public int tilesInArray(ArrayList<String> array) {

		int count = 0;
		for (String s : array)
		{    // RR
			if (s != null)
			{        // RR
				count++;
			}
		}

		return count;

	}
	
		
	protected void setAllTilesUnclickable()	
	{	
		for (int t = 0; t < visibleTiles; t++)	
		{	
			TextView gameTile = findViewById(getTileButtons()[t]);	
			gameTile.setClickable(false);	
		}	
	}	
	protected void setAllTilesClickable()	
	{	
		for (int t = 0; t < visibleTiles; t++)
		{	
			TextView gameTile = findViewById(getTileButtons()[t]);	
			gameTile.setClickable(true);	
		}

		/*for (int word : getTileButtons()) {
			TextView nextWord = findViewById(word);
			nextWord.setClickable(true);
		}*/

		/*for (int t = 0; t < getTileButtons().length; t++)
		{
			TextView gameTile = findViewById(getTileButtons()[t]);
			gameTile.setClickable(true);
		}*/
	}	
	protected void setOptionsRowUnclickable()	
	{	
		ImageView repeatImage = findViewById(R.id.repeatImage);
		ImageView wordImage = findViewById(R.id.wordImage);	
		repeatImage.setBackgroundResource(0);	
		repeatImage.setImageResource(R.drawable.zz_forward_inactive);	
		repeatImage.setClickable(false);	
		if (wordImage != null)	
			wordImage.setClickable(false);	
		if (getWordImages() != null)	
			for (int i = 0; i < 4; i++)	
			{	
				wordImage = findViewById(getWordImages()[i]);	
				wordImage.setClickable(false);	
			}	
	}	
	protected void setOptionsRowClickable()	
	{	
		ImageView repeatImage = findViewById(R.id.repeatImage);
		ImageView wordImage = findViewById(R.id.wordImage);	
		ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);	
		repeatImage.setBackgroundResource(0);	
		repeatImage.setImageResource(R.drawable.zz_forward);	
		repeatImage.setClickable(true);	
		gamesHomeImage.setClickable(true);	
		if (wordImage != null)	
			wordImage.setClickable(true);	
		if (getWordImages() != null)	
			for (int i = 0; i < 4; i++)	
			{	
				wordImage = findViewById(getWordImages()[i]);	
				wordImage.setClickable(true);	
			}	
	}	
	public void clickPicHearAudio(View view)	
	{	
		playActiveWordClip(false);	
	}	
	protected void playActiveWordClip(final boolean playFinalSound)	
	{	
		if (tempSoundPoolSwitch)	
			playActiveWordClip1(playFinalSound);	//SoundPool
		else	
			playActiveWordClip0(playFinalSound);	//MediaPlayer
	}	
	protected void playActiveWordClip1(final boolean playFinalSound)	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		gameSounds.play(wordAudioIDs.get(wordInLWC), 1.0f, 1.0f, 2, 0, 1.0f);	
		soundSequencer.postDelayed(new Runnable()	
		{	
			public void run()	
			{	
				if (playFinalSound)	
				{	
					trackerCount++;	
					updateTrackers();	
					repeatLocked = false;	
					SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit();	
					String playerString = Util.returnPlayerStringToAppend(playerNumber);	
					String uniqueGameLevelPlayerID = className + challengeLevel + playerString;	
					editor.putInt(uniqueGameLevelPlayerID, trackerCount);	
					editor.apply();	
					playCorrectFinalSound();	
				}	
				else	
				{	
					if (repeatLocked)	
					{	
						setAllTilesClickable();
					}	
					setOptionsRowClickable();	
				}	
			}	
		}, wordDurations.get(wordInLWC));	
	}	
	protected void playActiveWordClip0(final boolean playFinalSound)	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		int resID = getResources().getIdentifier(wordInLWC, "raw", getPackageName());	
		final MediaPlayer mp1 = MediaPlayer.create(this, resID);	
		mediaPlayerIsPlaying = true;	
		//mp1.start();	
		mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener()	
		{	
			@Override	
			public void onCompletion(MediaPlayer mp1)	
			{	
				mpCompletion(mp1, playFinalSound);	
			}	
		});	
		mp1.start();	
	}	
	protected void playCorrectSoundThenActiveWordClip(final boolean playFinalSound)	
	{	
		if (tempSoundPoolSwitch)	
			playCorrectSoundThenActiveWordClip1(playFinalSound);	
		else	
			playCorrectSoundThenActiveWordClip0(playFinalSound);	
	}	
	protected void playCorrectSoundThenActiveWordClip1(final boolean playFinalSound)	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		gameSounds.play(correctSoundID, 1.0f, 1.0f, 3, 0, 1.0f);	
		soundSequencer.postDelayed(new Runnable()	
		{	
			public void run()	
			{
				setAllTilesClickable();	
				setOptionsRowClickable();	
				playActiveWordClip(playFinalSound);	
			}	
		}, correctSoundDuration);	
	}	
	protected void playCorrectSoundThenActiveWordClip0(final boolean playFinalSound)	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		MediaPlayer mp2 = MediaPlayer.create(this, R.raw.zz_correct);	
		mediaPlayerIsPlaying = true;	
		mp2.start();	
		mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener()	
		{	
			@Override	
			public void onCompletion(MediaPlayer mp2)	
			{
				mp2.reset(); //JP: fixed "mediaplayer went away with unhandled events" issue
				mp2.release();	
				playActiveWordClip(playFinalSound);	
			}	
		});	
	}	
	protected void playIncorrectSound()	
	{	
		if (tempSoundPoolSwitch)	
			playIncorrectSound1();	
		else	
			playIncorrectSound0();	
	}	
	protected void playIncorrectSound1()	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();
		gameSounds.play(incorrectSoundID, 1.0f, 1.0f, 3, 0, 1.0f);
		setAllTilesClickable();	
		setOptionsRowClickable();	
	}	
	protected void playIncorrectSound0()	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		MediaPlayer mp3 = MediaPlayer.create(this, R.raw.zz_incorrect);	
		mediaPlayerIsPlaying = true;	
		mp3.start();	
		mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener()	
		{	
			@Override	
			public void onCompletion(MediaPlayer mp3)	
			{	
				mediaPlayerIsPlaying = false;	
				setAllTilesClickable();	
				setOptionsRowClickable();
				mp3.reset(); //JP
				mp3.release();	
			}	
		});	
	}	
	protected void playCorrectFinalSound()	
	{	
		if (tempSoundPoolSwitch)	
			playCorrectFinalSound1();	
		else	
			playCorrectFinalSound0();	
	}	
	protected void playCorrectFinalSound1()	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 1, 0, 1.0f);	
		setAllTilesClickable();	
		setOptionsRowClickable();	
	}	
	protected void playCorrectFinalSound0()	
	{	
		setAllTilesUnclickable();	
		setOptionsRowUnclickable();	
		mediaPlayerIsPlaying = true;	
		MediaPlayer mp3 = MediaPlayer.create(this, R.raw.zz_correct_final);	
		mp3.start();	
		mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener()	
		{	
			@Override	
			public void onCompletion(MediaPlayer mp3)	
			{	
				mediaPlayerIsPlaying = false;	
				setAllTilesClickable();	
				setOptionsRowClickable();
				mp3.reset(); //JP
				mp3.release();	
			}	
		});	
	}
	public void playAudioInstructions(View view){
		/*setAllTilesUnclickable();
		setOptionsRowUnclickable();
		int instructionsSoundID = gameSounds.load(context, getAudioInstructionsResID(), 2);
		gameSounds.play(instructionsSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
		setAllTilesClickable();
		setOptionsRowClickable();*/

		setAllTilesUnclickable();
		setOptionsRowUnclickable();
		mediaPlayerIsPlaying = true;
		MediaPlayer mp3 = MediaPlayer.create(this, getAudioInstructionsResID());
		mp3.start();
		mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp3)
			{
				mediaPlayerIsPlaying = false;
				setAllTilesClickable();
				setOptionsRowClickable();
				mp3.release();
			}
		});

	}

	protected void mpCompletion(MediaPlayer mp, boolean isFinal)	
	{	
		if (isFinal)	
		{	
			trackerCount++;	
			updateTrackers();	
			repeatLocked = false;	
			SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit();	
			String playerString = Util.returnPlayerStringToAppend(playerNumber);	
			String uniqueGameLevelPlayerID = className + challengeLevel + playerString;	
			editor.putInt(uniqueGameLevelPlayerID, trackerCount);	
			editor.apply();	
			playCorrectFinalSound();	
		}	
		else	
		{	
			mediaPlayerIsPlaying = false;	
			if (repeatLocked)	
			{	
				setAllTilesClickable();	
			}	
			setOptionsRowClickable();
			mp.reset(); //JP
			mp.release();	
		}

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void forceRTLIfSupported() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
		}
	}
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		private void forceLTRIfSupported ()
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
			}
		}
	}
