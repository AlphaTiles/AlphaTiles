package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

	// KP, Oct 2020

	Context context;

	int points;
	int challengeLevel = -1;
	int playerNumber = -1;
	int gameNumber = 0;

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

	@Override
	protected void onCreate(Bundle state) {
		context = this;

		points = getIntent().getIntExtra("points", 0);
		playerNumber = getIntent().getIntExtra("playerNumber", -1);
		challengeLevel = getIntent().getIntExtra("challengeLevel", -1);
		gameNumber = getIntent().getIntExtra("gameNumber", 0);

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

}
