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
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Logger;

import android.os.Handler;

import static android.graphics.Color.BLACK;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;

public class Mexico extends GameActivity
{

	ArrayList<String[]> memoryCollection = new ArrayList(); // KRP
	// # 1 memoryCollection[LWC word, e.g. Spanish]
	// # 2 [LOP word, e.g. Me'phaa]
	// # 3 [state: "TEXT" or "IMAGE"]
	// # 4 [state: "SELECTED" or "UNSELECTED" or "PAIRED"]

	ArrayList<String[]> wordListArray; // KRP

	int justClickedCard;
	int priorClickedCard;
	int activeSelections = 0;
	int pairsCompleted = 0;
	int cardHitA = 0;
	int cardHitB = 0;
	int pixelHeight = 0;

	Handler handler; // KRP

	protected static final int[] TILE_BUTTONS = {
			R.id.card01, R.id.card02, R.id.card03, R.id.card04, R.id.card05, R.id.card06, R.id.card07, R.id.card08, R.id.card09, R.id.card10,
			R.id.card11, R.id.card12, R.id.card13, R.id.card14, R.id.card15, R.id.card16, R.id.card17, R.id.card18, R.id.card19, R.id.card20
	};

	protected int[] getTileButtons() {return TILE_BUTTONS;}

	protected int[] getWordImages() {return null;}

	private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336", "#4CAF50", "#E91E63"};

	private static final Logger LOGGER = Logger.getLogger(Mexico.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.mexico);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

		points = getIntent().getIntExtra("points", 0); // KRP
		playerNumber = getIntent().getIntExtra("playerNumber", -1); // KRP
		challengeLevel = getIntent().getIntExtra("challengeLevel", -1); // KRP

		wordListArray = new ArrayList(); // KRP

		setTitle(Start.localAppName + ": " + gameNumber);

		switch (challengeLevel)
		{
		case 2:
			visibleTiles = 16;                       // RR
			break;
		case 3:
			visibleTiles = 20;                       // RR
			break;
		default:
			visibleTiles = 12;                       // RR

		}

		TextView pointsEarned = findViewById(R.id.pointsTextView);
		pointsEarned.setText(String.valueOf(points));

		SharedPreferences prefs = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE);
		String playerString = Util.returnPlayerStringToAppend(playerNumber);
		String uniqueGameLevelPlayerID = getClass().getName() + challengeLevel + playerString;
		trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

		updateTrackers();

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
		double scaling = 0.45;
		double scalingCards = 0.6;
		int bottomToTopId;
		int topToTopId;
		float percentBottomToTop;
		float percentTopToTop;
		float percentHeight;

		for (int c = 0; c < visibleTiles; c++)
		{

			TextView cards = findViewById(TILE_BUTTONS[c]);
			if (c == 0)
			{
				ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams)cards.getLayoutParams();
				bottomToTopId = lp1.bottomToTop;
				topToTopId = lp1.topToTop;
				percentBottomToTop = ((ConstraintLayout.LayoutParams)findViewById(bottomToTopId).getLayoutParams()).guidePercent;
				percentTopToTop = ((ConstraintLayout.LayoutParams)findViewById(topToTopId).getLayoutParams()).guidePercent;
				percentHeight = percentBottomToTop - percentTopToTop;
				pixelHeight = (int)(scaling * scalingCards * percentHeight * heightOfDisplay);
			}
			cards.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

		}

		LOGGER.info("Remember: pixelHeight (initial value) = " + pixelHeight);


		// Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
		TextView pointsEarned = findViewById(R.id.pointsTextView);
		ImageView pointsEarnedImage = (ImageView)findViewById(R.id.pointsImage);
		ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams)pointsEarnedImage.getLayoutParams();
		int bottomToTopId3 = lp3.bottomToTop;
		int topToTopId3 = lp3.topToTop;
		percentBottomToTop = ((ConstraintLayout.LayoutParams)findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
		percentTopToTop = ((ConstraintLayout.LayoutParams)findViewById(topToTopId3).getLayoutParams()).guidePercent;
		percentHeight = percentBottomToTop - percentTopToTop;
		int pixelHeight2 = (int)(0.7 * scaling * percentHeight * heightOfDisplay);     // renamed because we want to access pixelHeight (from above) later on
		pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight2);

	}

	public void repeatGame(View View)
	{

		if (mediaPlayerIsPlaying)
		{
			return;
		}

		// playAgain();

		// Closing and restarting the activity for each round is not ideal, but it seemed to be helping with memory issues...
		Intent intent = getIntent();
		intent.setClass(this, Mexico.class);    // so we retain the Extras
		startActivity(intent);
		finish();

	}

	public void playAgain()
	{

		setTextSizes();

		setCardTextToEmpty();
		buildWordsArray();
		Collections.shuffle(wordListArray); // KRP
		chooseMemoryWords();
		Collections.shuffle(memoryCollection); // KRP
		pairsCompleted = 0;
		activeSelections = 0;
		cardHitA = 0;
		cardHitB = 0;

	}

	public void setCardTextToEmpty()
	{

		for (int i = 0; i < TILE_BUTTONS.length; i++)
		{    // RR
			TextView card = findViewById(TILE_BUTTONS[i]); // RR

//            card.getBackground().setAlpha(255);

			if (i < visibleTiles)
			{
				card.setText("");
				card.setTextColor(BLACK); // KRP
				card.setBackgroundResource(R.drawable.zz_alphatileslogo2);
				card.setVisibility(View.VISIBLE);
				card.setClickable(true);
//                card.setTypeface(card.getTypeface(), Typeface.NORMAL);
			}
			else
			{
				card.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void buildWordsArray()
	{

		// This array will store every word available in wordlist.txt
		// Subsequently, this will be narrowed down to 10 words for use in the memory game
		// KRP, Oct 2020
		// AH, Nov 2020, revised to allow for spaces in words

		Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_wordlist));
		if (scanner.hasNextLine())
		{
			scanner.nextLine();
		}     // skip the header row

		while (scanner.hasNextLine())
		{
			String thisLine = scanner.nextLine();
			String[] thisLineArray = thisLine.split("\t");
			wordListArray.add(thisLineArray);
		}
	}

	public void chooseMemoryWords()
	{

		// KRP, Oct 2020

		int cardsToSetUp = visibleTiles / 2;   // this is half the number of cards

		for (int i = 0; i < visibleTiles; i++)
		{

			int index = i < cardsToSetUp ? i : i - cardsToSetUp;
			String[] content = new String[]
					{
							wordListArray.get(index)[0],
							wordListArray.get(index)[1],
							i < cardsToSetUp ? "TEXT" : "IMAGE", "UNSELECTED",

					};
			memoryCollection.add(content);

		}

	}

	public void respondToCardSelection()
	{

		setAllTilesUnclickable();
		setOptionsRowUnclickable();

		int t = justClickedCard - 1; //  justClickedCard uses 1 to 12/16/20 (dep. on challengeLevel), t uses the array ID: between [0] and [11] / [15] / [19]

		if (memoryCollection.get(t)[3].equals("PAIRED"))
		{
			setAllTilesClickable();
			setOptionsRowClickable();
			return;
		}

		if (activeSelections == 2)
		{
			setAllTilesClickable();
			setOptionsRowClickable();
			return;
		}

		if (justClickedCard == priorClickedCard && activeSelections == 1)
		{
			setAllTilesClickable();
			setOptionsRowClickable();
			return;
		}

		activeSelections++;
		String[] currentItem = memoryCollection.get(t); // KRP
		currentItem[3] = "SELECTED"; // KRP
		TextView card = findViewById(TILE_BUTTONS[t]);
		int resID = getResources().getIdentifier(currentItem[0], "drawable", getPackageName()); // KRP
		String wordInLOP = currentItem[1]; // KRP
		String appearance = currentItem[2]; // KRP

//        card.getBackground().setAlpha(255);
//        The commented out code in this and other modules relates to a previous version where upon a memory match:
//        Both cards displayed a semi-transparent version of the image and both cards showed the text in bold green font
//        If we could get this version to work, it would be preferable, but I (Aaron) had a problem with the images appearing
//        initially in new games already semi-transparent and I wasn't able to fix it

		if (appearance.equals("TEXT"))
		{
			card.setText(Start.wordList.stripInstructionCharacters(wordInLOP));
			int thisCardPixelHeight = pixelHeight;
			while (card.getLineCount() > 1)
			{
				thisCardPixelHeight = (int)(thisCardPixelHeight * 0.7);
				card.setTextSize(TypedValue.COMPLEX_UNIT_PX, thisCardPixelHeight);
			}
			card.setBackgroundResource(0);
		}
		else
		{
			card.setBackgroundResource(resID);
		}

//        card.getBackground().setAlpha(255);

		if (activeSelections == 2)
		{

			handler = new Handler();
			handler.postDelayed(quickViewDelay, 800);
			// Will run respondToTwoActiveCards() after delay...
			// https://www.youtube.com/watch?v=3pgGVBmSVq0
			// https://codinginflow.com/tutorials/android/handler-postdelayed-runnable
		}

		setAllTilesClickable();
		setOptionsRowClickable();

	}
/**
	protected void setAllTilesUnclickable()
	{
		for (int i = 0; i < visibleTiles; i++)
		{
			TextView card = findViewById(TILE_BUTTONS[i]);
			card.setClickable(false);
		}
	}

	protected void setAllTilesClickable()
	{
		for (int i = 0; i < visibleTiles; i++)
		{
			TextView card = findViewById(TILE_BUTTONS[i]);
			card.setClickable(true);
		}
	}

	protected void setOptionsRowUnclickable()
	{

		ImageView repeatImage = findViewById(R.id.repeatImage);

		repeatImage.setBackgroundResource(0);
		repeatImage.setImageResource(R.drawable.zz_forward_inactive);

		repeatImage.setClickable(false);

	}

	protected void setOptionsRowClickable()
	{

		ImageView repeatImage = findViewById(R.id.repeatImage);
		ImageView gamesHomeImage = findViewById(R.id.gamesHomeImage);


		repeatImage.setBackgroundResource(0);
		repeatImage.setImageResource(R.drawable.zz_forward);

		repeatImage.setClickable(true);
		gamesHomeImage.setClickable(true);

	}
**/
	public void respondToTwoActiveCards()
	{

		// Two cards have been selected (which may or may not match)
		activeSelections = 0;       // (a reset)
		boolean firstHit = false;
		boolean secondHit = false;
		cardHitA = 0;
		cardHitB = 0;
		for (int i = 0; i < visibleTiles; i++)
		{

			// Scan through CARDS to find which two items are selected
			if (memoryCollection.get(i)[3].equals("SELECTED"))
			{

				if (firstHit && !secondHit)
				{
					secondHit = true;
					cardHitB = i; // cardHitB is the button number (1 to 12/16/20) not the array value ([0] to [11]/[15]/[19])
				}
				if (!firstHit)
				{    // RR
					firstHit = true;
					cardHitA = i; // cardHitA is the button number (1 to 12/16/20) not the array value ([0] to [11]/[15]/[19])
				}
			}
		}

		if (memoryCollection.get(cardHitA)[0].equals(memoryCollection.get(cardHitB)[0]))
		{
			// Note: this is comparing the unstripped versions (e.g. with periods, etc.)

			// A match has been found!!
			memoryCollection.get(cardHitA)[3] = "PAIRED";
			memoryCollection.get(cardHitB)[3] = "PAIRED";
			pairsCompleted++;

			final TextView cardA = findViewById(TILE_BUTTONS[cardHitA]); // RR
			final TextView cardB = findViewById(TILE_BUTTONS[cardHitB]); // RR
//            int resID = getResources().getIdentifier(memoryCollection.get(cardHitB)[0] + "2", "drawable", getPackageName());

//            cardA.setBackgroundResource(resID);
//            cardB.setBackgroundResource(resID);
			cardA.setBackgroundResource(0);
			cardB.setBackgroundResource(0);

//            cardA.getBackground().setAlpha(100);
//            cardB.getBackground().setAlpha(100);

			cardA.setText(Start.wordList.stripInstructionCharacters(memoryCollection.get(cardHitA)[1]));
			cardB.setText(Start.wordList.stripInstructionCharacters(memoryCollection.get(cardHitB)[1]));

			String tileColorStr = COLORS[cardHitA % 5];
			int tileColor = Color.parseColor(tileColorStr);
			cardA.setTextColor(tileColor); // theme color
			cardB.setTextColor(tileColor); // theme color

//            cardA.setTextColor(Color.parseColor("#006400")); // dark green
//            cardB.setTextColor(Color.parseColor("#006400")); // dark green

//            cardA.setTypeface(cardA.getTypeface(), Typeface.BOLD);
//            cardB.setTypeface(cardB.getTypeface(), Typeface.BOLD);

			for (int i = 0; i < pixelHeight; i++)
			{
				decreasePixelHeight(cardA);
			}
			for (int i = 0; i < pixelHeight; i++)
			{
				decreasePixelHeight(cardB);
			}

//            The below code relates to an experiment to size down the text (because it increases upon applying bold-green)
//            Ideally, the font size would reduce until the text displayed on one line
//            int lineCount = returnLineCount(cardA);
//            LOGGER.info("Remember: [outside while (lineCount > 1)]: lineCount = " + lineCount);
//            while (lineCount > 1) {
//                LOGGER.info("Remember: [inside while (lineCount > 1)]: lineCount = " + lineCount);
//                int cardAPixelHeight = pixelHeight;
//                cardAPixelHeight = cardAPixelHeight - 1;
//                cardA.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardAPixelHeight);
//                lineCount = returnLineCount(cardA);
//            }
//
//            lineCount = returnLineCount(cardB);
//            LOGGER.info("Remember: [outside while (lineCount > 1)]: lineCount = " + lineCount);
//            while (lineCount > 1) {
//                LOGGER.info("Remember: [inside while (lineCount > 1)]: lineCount = " + lineCount);
//                int cardBPixelHeight = pixelHeight;
//                cardBPixelHeight = cardBPixelHeight - 1;
//                cardB.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardBPixelHeight);
//                lineCount = returnLineCount(cardB);
//            }

//            int cardAPixelHeight = pixelHeight;
//            LOGGER.info("Remember: [failing routine][outside while]: cardA.getLineCount() = " + cardA.getLineCount());
//            while(cardA.getLineCount() > 1) {
//                LOGGER.info("Remember: [failing routine][inside while]: cardA.getLineCount() = " + cardA.getLineCount());
//                cardAPixelHeight = cardAPixelHeight - 1;
//                cardA.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardAPixelHeight);
//                cardA.setText(memoryCollection[cardHitA][1]);
//                cardA.requestLayout();
//                cardA.invalidate();
//                cardA.setText(memoryCollection[cardHitA][1]);
//                cardA.requestLayout();
//                cardA.invalidate();
//            }
//
//            int cardBPixelHeight = pixelHeight;
//            while(cardB.getLineCount() > 1) {
//                cardBPixelHeight = cardBPixelHeight - 1;
//                cardB.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardBPixelHeight);
//                cardB.setText(memoryCollection[cardHitB][1]);
//                cardB.requestLayout();
//                cardB.invalidate();
//                cardB.setText(memoryCollection[cardHitB][1]);
//                cardB.requestLayout();
//                cardB.invalidate();
//            }

			TextView pointsEarned = findViewById(R.id.pointsTextView);
			points++;
			pointsEarned.setText(String.valueOf(points));

			SharedPreferences.Editor editor = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE).edit();
			String playerString = Util.returnPlayerStringToAppend(playerNumber);
			editor.putInt("storedPoints_player" + playerString, points);
			editor.apply();

			wordInLWC = memoryCollection.get(cardHitA)[0];
			playCorrectSoundThenActiveWordClip(pairsCompleted == (visibleTiles / 2));

		}
		else
		{
			// The two cards do NOT match
			TextView cardA = findViewById(TILE_BUTTONS[cardHitA]); // RR
			TextView cardB = findViewById(TILE_BUTTONS[cardHitB]); // RR
			cardA.setText("");
			cardB.setText("");
			cardA.setBackgroundResource(R.drawable.zz_alphatileslogo2);
			cardB.setBackgroundResource(R.drawable.zz_alphatileslogo2);
			memoryCollection.get(cardHitA)[3] = "UNSELECTED"; // KRP
			memoryCollection.get(cardHitB)[3] = "UNSELECTED"; // KRP
		}

	}

	public static void decreasePixelHeight(final TextView card)
	{

		card.post(new Runnable()
		{
			@Override
			public void run()
			{
				int lineCount = card.getLineCount();
				if (lineCount > 1)
				{
					int pixelHeightResized = (int)(card.getTextSize() * 0.7);
					card.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeightResized);
					LOGGER.info("Remember: [runnable]: pixelHeightResized =  " + pixelHeightResized);
				}
			}
		});
	}

	public void onBtnClick(View view)
	{

		priorClickedCard = justClickedCard;
		justClickedCard = Integer.parseInt((String)view.getTag());
		respondToCardSelection();

	}

	private Runnable quickViewDelay = new Runnable()
	{
		@Override
		public void run()
		{
			respondToTwoActiveCards();
		}
	};
	/**
	public void playActiveWordClip()
	{
		setAllTilesUnclickable();
		setOptionsRowUnclickable();

		//final String className = getClass().getName();

		int resID = getResources().getIdentifier(memoryCollection.get(cardHitA)[0], "raw", getPackageName());
		MediaPlayer mp1 = MediaPlayer.create(this, resID);
		mediaPlayerIsPlaying = true;
		mp1.start();
		mp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp1)
			{
				if (pairsCompleted == (visibleTiles / 2))
				{
					trackerCount++;
					updateTrackers();

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
					setAllTilesClickable();
					setOptionsRowClickable();
				}
				mp1.release();
			}
		});

	}

	public void playCorrectSoundThenActiveWordClip()
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

	public void playCorrectFinalSound()
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

	@Override
	protected void mpCompletion(MediaPlayer mp)
	{
		if (pairsCompleted == (visibleTiles / 2))
		{
			trackerCount++;
			updateTrackers();

			SharedPreferences.Editor editor = getSharedPreferences(Start.SHARED_PREFS, MODE_PRIVATE).edit();
			String playerString = Util.returnPlayerStringToAppend(playerNumber);
			String uniqueGameLevelPlayerID = className + challengeLevel + playerString;
			editor.putInt(uniqueGameLevelPlayerID, trackerCount);
			editor.apply();

			playCorrectFinalSound();
			mp.release();
		}
		else
		{
			repeatLocked = false;
			super.mpCompletion(mp);
		}
	}
**/
}
