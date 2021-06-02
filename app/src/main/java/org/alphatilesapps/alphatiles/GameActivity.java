package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;	
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public abstract class GameActivity extends AppCompatActivity {

	// KP, Oct 2020

	Context context;

	int points;
	int challengeLevel = -1;
	int playerNumber = -1;
	int gameNumber = 0;
	int visibleTiles;	
	String className;

	ArrayList<String> parsedWordArrayFinal;

	String wordInLWC = "";    // the lWC word (e.g. Spanish), which exactly matches the image and audio file names
	String wordInLOP = "";    // the corresponding word in the language of play (e.g. Me'phaa)
	int trackerCount = 0;
	boolean mediaPlayerIsPlaying = false;
	boolean repeatLocked = true;

	protected static final int[] TRACKERS = {
			R.id.tracker01, R.id.tracker02, R.id.tracker03, R.id.tracker04, R.id.tracker05, R.id.tracker06, R.id.tracker07, R.id.tracker08, R.id.tracker09, R.id.tracker10,
			R.id.tracker11, R.id.tracker12

	};
	
	protected abstract int[] getTileButtons();	
	protected abstract int[] getWordImages();

	@Override
	protected void onCreate(Bundle state) {
		context = this;

		points = getIntent().getIntExtra("points", 0);
		playerNumber = getIntent().getIntExtra("playerNumber", -1);
		challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
		gameNumber = getIntent().getIntExtra("gameNumber", 0);
		
		className = getClass().getName();

		super.onCreate(state);

	}

	public void goBackToEarth(View view) {
		Intent intent = getIntent();
		intent.setClass(context, Earth.class);	// so we retain the Extras
		startActivity(intent);
		finish();

	}

	public void goBackToStart(View view) {

		if (mediaPlayerIsPlaying)
		{
			return;
		}
		startActivity(new Intent(context, Start.class));
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
/*	
	protected void playActiveWordClip()	
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
				mpCompletion(mp1);	
			}	
		});	
		mp1.start();	
	}	
*/	
	protected void playActiveWordClip(final boolean playFinalSound)	
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
/*	
	protected void playCorrectSoundThenActiveWordClip()	
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
				mp2.release();	
				playActiveWordClip();	
			}	
		});	
	}	
*/	
	protected void playCorrectSoundThenActiveWordClip(final boolean playFinalSound)	
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
				mp2.release();	
				playActiveWordClip(playFinalSound);	
			}	
		});	
	}	
	protected void playIncorrectSound()	
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
				mp3.release();	
			}	
		});	
	}	
	protected void playCorrectFinalSound()	
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
				mp3.release();	
			}	
		});	
	}	
	protected void mpCompletion(MediaPlayer mp)	
	{	
		mediaPlayerIsPlaying = false;	
		if (repeatLocked)	
		{	
			setAllTilesClickable();	
		}	
		setOptionsRowClickable();	
		mp.release();	
	}	
	protected void mpCompletion(MediaPlayer mp, boolean isFinal)	
	{	
		if (isFinal)	
		{	
			trackerCount++;	
			updateTrackers();	
			repeatLocked = false;	
			SharedPreferences.Editor editor = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE).edit();	
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
			mp.release();	
		}	
	}

}
