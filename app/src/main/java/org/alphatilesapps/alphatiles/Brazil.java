package org.alphatilesapps.alphatiles;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

// RR
//Game idea: Find the vowel missing from the word
//Challenge Level 1: Pick from correct tile and three random tiles
//Challenge Level 2: Pick from correct tile and its distractor trio
//Challenge Level 3: Pick from all vowel tiles (up to a max of 15)

public class Brazil extends GameActivity
{

	Start.TileList sortableTilesArray; // KRP
	String correctTile = "";

	protected static final int[] TILE_BUTTONS = {
			R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
			R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15
	};

	protected int[] getTileButtons() {return TILE_BUTTONS;}

	protected int[] getWordImages() {return null;}

	private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336", "#4CAF50", "#E91E63"};

	static List<String> VOWELS = new ArrayList<>();

	private static final Logger LOGGER = Logger.getLogger(Brazil.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		if (challengeLevel == 3)
		{
			setContentView(R.layout.brazil_cl3);
		}
		else
		{
			setContentView(R.layout.brazil_cl1);
		}
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

		points = getIntent().getIntExtra("points", 0); // KRP
		playerNumber = getIntent().getIntExtra("playerNumber", -1); // KRP
		challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KRP
		gameNumber = getIntent().getIntExtra("gameNumber", 0); // KRP

		if (VOWELS.isEmpty())
		{  //makes sure VOWELS is populated only once when the app is running
			for (int d = 0; d < Start.tileList.size(); d++)
			{
				if (Start.tileList.get(d).tileType.equals("V"))
				{
					VOWELS.add(Start.tileList.get(d).baseTile);
				}
			}
		}

		Collections.shuffle(VOWELS); // AH

		setTitle(Start.localAppName + ": " + gameNumber);
		switch (challengeLevel)
		{
		case 2:
			visibleTiles = 4;
			break;
		case 3:
			visibleTiles = VOWELS.size();
			if (visibleTiles > 15)
			{    // AH
				visibleTiles = 15;      // AH
			}                           // AH
			break;
		default:
			visibleTiles = 4;
		}

		sortableTilesArray = (Start.TileList)Start.tileList.clone(); // KRP

		TextView pointsEarned = findViewById(R.id.pointsTextView);
		pointsEarned.setText(String.valueOf(points));

		SharedPreferences prefs = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE);
		String playerString = Util.returnPlayerStringToAppend(playerNumber);
		String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
		trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

		updateTrackers();

		setTextSizes();

		playAgain();

	}

	@Override
	public void onBackPressed()
	{
		// no action
	}

	public void setTextSizes()
	{

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int heightOfDisplay = displayMetrics.heightPixels;
		int pixelHeight = 0;
		double scaling = 0.45;
		int bottomToTopId;
		int topToTopId;
		float percentBottomToTop;
		float percentTopToTop;
		float percentHeight;

		for (int t = 0; t < visibleTiles; t++)
		{

			TextView gameTile = findViewById(TILE_BUTTONS[t]);
			if (t == 0)
			{
				ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams)gameTile.getLayoutParams();
				bottomToTopId = lp1.bottomToTop;
				topToTopId = lp1.topToTop;
				percentBottomToTop = ((ConstraintLayout.LayoutParams)findViewById(bottomToTopId).getLayoutParams()).guidePercent;
				percentTopToTop = ((ConstraintLayout.LayoutParams)findViewById(topToTopId).getLayoutParams()).guidePercent;
				percentHeight = percentBottomToTop - percentTopToTop;
				pixelHeight = (int)(scaling * percentHeight * heightOfDisplay);
			}
			gameTile.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

		}

		TextView activeWord = (TextView)findViewById(R.id.activeWordTextView);
		ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams)activeWord.getLayoutParams();
		int bottomToTopId2 = lp2.bottomToTop;
		int topToTopId2 = lp2.topToTop;
		percentBottomToTop = ((ConstraintLayout.LayoutParams)findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
		percentTopToTop = ((ConstraintLayout.LayoutParams)findViewById(topToTopId2).getLayoutParams()).guidePercent;
		percentHeight = percentBottomToTop - percentTopToTop;
		pixelHeight = (int)(scaling * percentHeight * heightOfDisplay);
		activeWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

		// Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
		TextView pointsEarned = findViewById(R.id.pointsTextView);
		ImageView pointsEarnedImage = (ImageView)findViewById(R.id.pointsImage);
		ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams)pointsEarnedImage.getLayoutParams();
		int bottomToTopId3 = lp3.bottomToTop;
		int topToTopId3 = lp3.topToTop;
		percentBottomToTop = ((ConstraintLayout.LayoutParams)findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
		percentTopToTop = ((ConstraintLayout.LayoutParams)findViewById(topToTopId3).getLayoutParams()).guidePercent;
		percentHeight = percentBottomToTop - percentTopToTop;
		pixelHeight = (int)(0.7 * scaling * percentHeight * heightOfDisplay);
		pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

	}

	public void repeatGame(View View)
	{
		if (!repeatLocked)
		{
			playAgain();
		}
	}

	public void playAgain()
	{
		if (mediaPlayerIsPlaying)
		{
			return;
		}

		repeatLocked = true;
		Collections.shuffle(sortableTilesArray); // KRP
		chooseWord();
		setAllTilesUnclickable();
		setOptionsRowUnclickable();
		setUpTiles();
		playActiveWordClip(false);
		setAllTilesClickable();
		setOptionsRowClickable();
	}

	private void chooseWord()
	{
		Random rand = new Random();
		int randomNum = rand.nextInt(Start.wordList.size()); // KRP

		wordInLWC = Start.wordList.get(randomNum).nationalWord; // KRP
		wordInLOP = Start.wordList.get(randomNum).localWord; // KRP

		ImageView image = findViewById(R.id.wordImage);
		int resID = getResources().getIdentifier(wordInLWC, "drawable", getPackageName());
		image.setImageResource(resID);

		removeVowel();
	}

	private void removeVowel()
	{
		parsedWordArrayFinal = Start.tileList.parseWord(wordInLOP);
		int min = 0;
		int max = parsedWordArrayFinal.size() - 1;
		Random rand = new Random();
		int index = 0;
		correctTile = "";

		while (!VOWELS.contains(correctTile))
		{
			index = rand.nextInt((max - min) + 1) + min;
			correctTile = parsedWordArrayFinal.get(index);
			if (VOWELS.indexOf(correctTile) > 14)
			{     // AH
				correctTile = "";                       // AH
				Collections.shuffle(VOWELS);            // AH
			}                                           // AH
		}

		parsedWordArrayFinal.set(index, "_");
		TextView constructedWord = findViewById(R.id.activeWordTextView);
		StringBuilder word = new StringBuilder();
		for (String s : parsedWordArrayFinal)
		{
			if (s != null)
			{
				word.append(s);
			}
		}
		constructedWord.setText(word.toString());
	}

	private void setUpTiles()
	{

		boolean correctTileRepresented = false;
		if (challengeLevel == 3)
		{
			for (int t = 0; t < visibleTiles; t++)
			{
				TextView gameTile = findViewById(TILE_BUTTONS[t]);
				gameTile.setText(VOWELS.get(t));

				String tileColorStr = COLORS[t % 5];
				int tileColor = Color.parseColor(tileColorStr);

				gameTile.setBackgroundColor(tileColor);
				gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
				gameTile.setVisibility(View.VISIBLE);
			}

			for (int i = visibleTiles; i < TILE_BUTTONS.length; i++)
			{
				TextView gameTile = findViewById(TILE_BUTTONS[i]);
				gameTile.setVisibility(View.INVISIBLE);
			}
		}
		else if (challengeLevel == 1)
		{

			for (int t = 0; t < visibleTiles; t++)
			{

				TextView gameTile = findViewById(TILE_BUTTONS[t]);

				if (sortableTilesArray.get(t).baseTile.equals(correctTile))
				{
					correctTileRepresented = true;
				}

				String tileColorStr = COLORS[t % 5];
				int tileColor = Color.parseColor(tileColorStr);

				gameTile.setText(sortableTilesArray.get(t).baseTile);
				if (sortableTilesArray.get(t).baseTile.equals("tiles"))
				{
					gameTile.setText(sortableTilesArray.get(Start.tileList.size() - 1).baseTile);
				}
				gameTile.setBackgroundColor(tileColor);
				gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
				gameTile.setVisibility(View.VISIBLE);

			}

			if (!correctTileRepresented)
			{

				// If the right tile didn't randomly show up in the range, then here the right tile overwrites one of the other tiles
				LOGGER.info("Remember that inside loop for correctTileRepresented = false");

				int min = 0;
				int max = visibleTiles - 1;
				Random rand = new Random();
				int randomNum = rand.nextInt((max - min) + 1) + min;

				TextView gameTile = findViewById(TILE_BUTTONS[randomNum]);
				gameTile.setText(correctTile);

			}
		}
		else
		{
			// when Earth.challengeLevel == 2
			int correspondingRow = 0;
			for (int d = 0; d < Start.tileList.size(); d++)
			{
				if (Start.tileList.get(d).baseTile.equals(correctTile))
				{
					correspondingRow = d;
					break;
				}
			}

			List<String> usedTiles = new ArrayList<>();
			Random rand = new Random();
			int randomNum;
			for (int t = 0; t < visibleTiles; t++)
			{
				TextView gameTile = findViewById(TILE_BUTTONS[t]);

				String tileColorStr = COLORS[t % 5];
				int tileColor = Color.parseColor(tileColorStr);

				gameTile.setBackgroundColor(tileColor);
				gameTile.setTextColor(Color.parseColor("#FFFFFF")); // white
				gameTile.setVisibility(View.VISIBLE);

				randomNum = rand.nextInt(visibleTiles); //
				String nextTile;
				if (randomNum == 0)
				{
					nextTile = Start.tileList.get(correspondingRow).baseTile;
				}
				else
				{
					nextTile = Start.tileList.get(correspondingRow).altTiles[randomNum - 1];
				}
				if (!usedTiles.contains(nextTile))
				{
					gameTile.setText(nextTile);
					usedTiles.add(t, nextTile);
				}
				else
				{
					t--;
				}
			}
		}
	}
/**
	protected void setAllTilesUnclickable()
	{
		for (int t = 0; t < visibleTiles; t++)
		{
			TextView gameTile = findViewById(TILE_BUTTONS[t]);
			gameTile.setClickable(false);
		}
	}

	protected void setAllTilesClickable()
	{
		for (int t = 0; t < visibleTiles; t++)
		{
			TextView gameTile = findViewById(TILE_BUTTONS[t]);
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
		wordImage.setClickable(false);
	}

	protected void setOptionsRowClickable()
	{
		ImageView repeatImage = findViewById(R.id.repeatImage);
		ImageView wordImage = findViewById(R.id.wordImage);
		ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);

		repeatImage.setBackgroundResource(0);
		repeatImage.setImageResource(R.drawable.zz_forward);

		repeatImage.setClickable(true);
		wordImage.setClickable(true);
		gamesHomeImage.setClickable(true);
	}
**/
	protected void respondToTileSelection(int justClickedTile)
	{

		if (mediaPlayerIsPlaying)
		{
			return;
		}

		setAllTilesUnclickable();
		setOptionsRowUnclickable();

		int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 15, t uses the array ID (between [0] and [14]
		TextView tile = findViewById(TILE_BUTTONS[tileNo]);
		String gameTileString = tile.getText().toString();

		if (correctTile.equals(gameTileString))
		{
			// Good job! You chose the right tile
			repeatLocked = false;

			TextView pointsEarned = findViewById(R.id.pointsTextView);
			points++;
			pointsEarned.setText(String.valueOf(points));

			trackerCount++;
			updateTrackers();

			SharedPreferences.Editor editor = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE).edit();
			String playerString = Util.returnPlayerStringToAppend(playerNumber);
			editor.putInt("storedPoints_player" + playerString, points);
			editor.apply();
			String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
			editor.putInt(uniqueGameLevelPlayerID, trackerCount);
			editor.apply();

			for (int i = 0; i < parsedWordArrayFinal.size(); i++)
			{
				if ("_".equals(parsedWordArrayFinal.get(i)))
				{
					parsedWordArrayFinal.set(i, gameTileString);
				}
			}

			TextView constructedWord = findViewById(R.id.activeWordTextView);
			StringBuilder word = new StringBuilder();
			for (String s : parsedWordArrayFinal)
			{
				if (s != null)
				{
					word.append(s);
				}
			}
			constructedWord.setText(word.toString());

			for (int t = 0; t < visibleTiles; t++)
			{
				TextView gameTile = findViewById(TILE_BUTTONS[t]);
				if (t != (tileNo))
				{
					String wordColorStr = "#A9A9A9"; // dark gray
					int wordColorNo = Color.parseColor(wordColorStr);
					gameTile.setBackgroundColor(wordColorNo);
					gameTile.setTextColor(Color.parseColor("#000000")); // black
				}
			}

			playCorrectSoundThenActiveWordClip(false);

		}
		else
		{

			playIncorrectSound();

		}

	}

	public void onBtnClick(View view)
	{
		respondToTileSelection(Integer.parseInt((String)view.getTag())); // KRP
	}
	/**
	public void clickPicHearAudio(View view)
	{

		playActiveWordClip();

	}

	protected void playActiveWordClip()
	{
		setAllTilesUnclickable();
		setOptionsRowUnclickable();
		int resID = getResources().getIdentifier(wordInLWC, "raw", getPackageName());
		final MediaPlayer mp1 = MediaPlayer.create(this, resID);
		mediaPlayerIsPlaying = true;
		mp1.start();
		mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp1)
			{
				mediaPlayerIsPlaying = false;
				if (repeatLocked)
				{
					setAllTilesClickable();
				}
				setOptionsRowClickable();
				mp1.release();

			}
		});
	}

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
 **/
}
