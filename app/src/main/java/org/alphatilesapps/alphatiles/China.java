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

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import androidx.constraintlayout.widget.ConstraintLayout;

//Game of 15
public class China extends GameActivity
{
	Start.TileList sortableTilesArray;
	String[][] threeFourWordInLopLwc = new String[3][2];
	String[] oneThreeWordInLopLwc = new String[2];
	ArrayList<String> allWords = new ArrayList<>();
	TextView blankTile;
	int moves;

	protected static final int[] TILE_BUTTONS = {
			R.id.tile01, R.id.tile02, R.id.tile03, R.id.tile04, R.id.tile05, R.id.tile06, R.id.tile07, R.id.tile08, R.id.tile09, R.id.tile10,
			R.id.tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15, R.id.tile16
	};

	protected static final int[] WORD_IMAGES = {
			R.id.wordImage01, R.id.wordImage02, R.id.wordImage03, R.id.wordImage04
	};

	protected int[] getTileButtons() {return TILE_BUTTONS;}

	protected int[] getWordImages() {return WORD_IMAGES;}

	private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336", "#4CAF50", "#E91E63"};
	private static final Logger LOGGER = Logger.getLogger(China.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.china);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

		points = getIntent().getIntExtra("points", 0); // KP
		playerNumber = getIntent().getIntExtra("playerNumber", -1); // KP
		challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KP

		setTitle(Start.localAppName + ": " + gameNumber);

		TextView pointsEarned = findViewById(R.id.pointsTextView);
		pointsEarned.setText(String.valueOf(points));

		SharedPreferences prefs = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE);
		String playerString = Util.returnPlayerStringToAppend(playerNumber);
		String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
		trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

		updateTrackers();

		playAgain();

		setTextSizes();

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

		for (int t = 0; t < TILE_BUTTONS.length; t++)
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

		switch (challengeLevel)
		{
		case 2:
			moves = 10;
			break;
		case 3:
			moves = 15;
			break;
		default:
			moves = 5;
		}

		repeatLocked = true;
		choseWords();
		setUpTiles();
		//wip
	}

	private void choseWords()
	{
		//For the first three words
		Random rand = new Random();
		int randomNum;
		int tileLength;
		for (int i = 0; i < 3; i++)
		{
			randomNum = rand.nextInt(Start.wordList.size());

			threeFourWordInLopLwc[i][0] = Start.wordList.get(randomNum).nationalWord;
			threeFourWordInLopLwc[i][1] = Start.wordList.get(randomNum).localWord;

			tileLength = tilesInArray(Start.tileList.parseWord(threeFourWordInLopLwc[i][1]));
			for (int j = 0; j < i; j++)
			{
				if (threeFourWordInLopLwc[i][0].equals(threeFourWordInLopLwc[j][0]))
				{
					LOGGER.info("Remember: word rejected for repeating already selected word");
					i--;
				}
				else if (tileLength != 4)
				{
					LOGGER.info("Remember: word rejected for not being 4 tiles long");
					i--;
				}
			}
		}

		//For the last word
		boolean cont = true;
		while (cont)
		{
			randomNum = rand.nextInt(Start.wordList.size());

			oneThreeWordInLopLwc[0] = Start.wordList.get(randomNum).nationalWord;
			oneThreeWordInLopLwc[1] = Start.wordList.get(randomNum).localWord;
			tileLength = tilesInArray(Start.tileList.parseWord(oneThreeWordInLopLwc[1]));
			if (tileLength == 3)
			{
				LOGGER.info("Remeber: word is 3 tiles long");
				cont = false;
			}
		}
	}

	private void setUpTiles()
	{
		ArrayList<String> tiles = new ArrayList<>();
		for (int t = 0; t < 3; t++)
		{
			tiles.addAll(Start.tileList.parseWord(threeFourWordInLopLwc[t][1]));

			ImageView image = findViewById(WORD_IMAGES[t]);
			int resID = getResources().getIdentifier(threeFourWordInLopLwc[t][0] + "2", "drawable", getPackageName());
			image.setImageResource(resID);
			image.setVisibility(View.VISIBLE);
		}
		tiles.addAll(Start.tileList.parseWord(oneThreeWordInLopLwc[1]));

		ImageView image = findViewById(WORD_IMAGES[3]);
		int resID = getResources().getIdentifier(oneThreeWordInLopLwc[0] + "2", "drawable", getPackageName());
		image.setImageResource(resID);
		image.setVisibility(View.VISIBLE);

		if (tiles.size() != 15)
		{
			LOGGER.info("Words not long enough.  Trying again.");
			choseWords();
			setUpTiles();
			return;
		}
		allWords = tiles;

		for (int i = 0; i < 15; i++)
		{
			TextView gameTile = findViewById(TILE_BUTTONS[i]);
			gameTile.setText(tiles.get(i));
			gameTile.setBackgroundColor(Color.parseColor("#000000"));
			gameTile.setTextColor(Color.parseColor("#FFFFFF"));
		}
		TextView finalTile = findViewById(TILE_BUTTONS[15]);
		finalTile.setText("");
		finalTile.setBackgroundColor(Color.parseColor("#FFFFFF"));
		finalTile.setTextColor(Color.parseColor("#FFFFFF"));
		blankTile = finalTile;

		Random rand = new Random();
		int tile;
		int lastTile = 16;

		while (moves != 0)
		{
			tile = rand.nextInt(TILE_BUTTONS.length);

			if (isSildable(tile) && tile != lastTile)
			{
				TextView t = findViewById(TILE_BUTTONS[tile]);
				swapTiles(t, blankTile);
				lastTile = tile;
				moves--;
			}
		}
	}

	private void swapTiles(TextView tile1, TextView tile2)
	{
		CharSequence temp = tile1.getText();
		tile1.setText(tile2.getText());
		tile2.setText(temp);

		if (tile1.getText() == "")
		{
			tile1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			tile2.setBackgroundColor(Color.parseColor("#000000"));
			blankTile = tile1;
		}
		else if (tile2.getText() == "")
		{
			tile2.setBackgroundColor(Color.parseColor("#FFFFFF"));
			tile1.setBackgroundColor(Color.parseColor("#000000"));
			blankTile = tile2;
		}
	}

	private void respondToTileSelection(int justClickedTile)
	{

		if (mediaPlayerIsPlaying)
		{
			return;
		}

		setAllTilesUnclickable();
		setOptionsRowUnclickable();

		int tileNo = justClickedTile - 1; //  justClickedTile uses 1 to 18, t uses the array ID (between [0] and [17]
		TextView tile = findViewById(TILE_BUTTONS[tileNo]);

		if (isSildable(tileNo))
		{
			swapTiles(tile, blankTile);
		}

		if (isSolved())
		{
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

			for (int tileButton : TILE_BUTTONS)
			{
				TextView gameTile = findViewById(tileButton);
				if (gameTile != blankTile)
				{
					String wordColorStr = "#00FF00"; //green
					int wordColorNo = Color.parseColor(wordColorStr);
					gameTile.setBackgroundColor(wordColorNo);
				}
			}
			setOptionsRowClickable();
		}
		else
		{
			setAllTilesClickable();
			setOptionsRowClickable();
		}

	}

	public void onBtnClick(View view)
	{
		respondToTileSelection(Integer.parseInt((String)view.getTag())); // KP
	}

	private boolean isSolved()
	{
		boolean solved = false;
		if (blankTile == findViewById(TILE_BUTTONS[15]))
		{
			TextView tile;
			for (int i = 0; i < 15; i++)
			{
				tile = findViewById(TILE_BUTTONS[i]);
				solved = (allWords.get(i) == tile.getText());

				if (!solved)
				{
					break;
				}
			}
		}
		return solved;
	}

	private boolean isSildable(int tileNo)
	{
		boolean sildable = false;
		TextView tile;

		if (tileNo != 0 && tileNo != 4 && tileNo != 8 && tileNo != 12)
		{
			tile = findViewById(TILE_BUTTONS[tileNo - 1]);
			sildable = (tile == blankTile);
		}

		if (tileNo != 3 && tileNo != 7 && tileNo != 11 && tileNo != 15 && !sildable)
		{
			tile = findViewById(TILE_BUTTONS[tileNo + 1]);
			sildable = (tile == blankTile);
		}

		if (tileNo >= 4 && !sildable)
		{
			tile = findViewById(TILE_BUTTONS[tileNo - 4]);
			sildable = (tile == blankTile);
		}

		if (tileNo < 12 && !sildable)
		{
			tile = findViewById(TILE_BUTTONS[tileNo + 4]);
			sildable = (tile == blankTile);
		}

		return sildable;
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
		ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);
		ImageView wordImage;

		repeatImage.setBackgroundResource(0);
		repeatImage.setImageResource(R.drawable.zz_forward_inactive);

		repeatImage.setClickable(false);
		gamesHomeImage.setClickable(false);

		for (int i = 0; i < 4; i++)
		{
			wordImage = findViewById(WORD_IMAGES[i]);
			wordImage.setClickable(false);
		}
	}

	protected void setOptionsRowClickable()
	{

		ImageView repeatImage = findViewById(R.id.repeatImage);
		ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);
		ImageView wordImage;

		repeatImage.setBackgroundResource(0);
		repeatImage.setImageResource(R.drawable.zz_forward);

		repeatImage.setClickable(true);
		gamesHomeImage.setClickable(true);

		for (int i = 0; i < 4; i++)
		{
			wordImage = findViewById(WORD_IMAGES[i]);
			wordImage.setClickable(true);
		}

	}

	public void clickPicHearAudio(View view)
	{

		playActiveWordClip();

	}

	public void playActiveWordClip()
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
**/
}
