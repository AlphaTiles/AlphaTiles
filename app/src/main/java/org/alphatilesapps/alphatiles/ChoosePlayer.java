package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;   // KRP
import java.util.logging.Logger;


import static org.alphatilesapps.alphatiles.Start.*;
import static org.alphatilesapps.alphatiles.Settings.forceRTL;
//import static org.alphatilesapps.alphatiles.Util.parseWord;   // KRP

public class ChoosePlayer extends AppCompatActivity
{
	Context context;

	public static final int ALT_COUNT = 3;  // KRP
/*
	public static String localAppName; // KRP add "public"

	public static TileList tileList; // KRP // from aa_gametiles.txt

	public static WordList wordList;     // KRP  // from aa_wordlist.txt

	public static KeyList keyList; // KRP // from aa_keyboard.txt

	public static GameList gameList; // from aa_games.text

	public static LangInfoList langInfoList; // KRP / from aa_langinfo.txt

	public static SettingsList settingsList; // KRP // from aa_settings.txt

	public static AvatarNameList nameList; // KRP / from aa_names.txt
*/
	public static ArrayList<Integer> avatarIdList;
	public static ArrayList<Drawable> avatarJpgList;

	public static final int[] AVATAR_JPG_IDS = {
			R.drawable.zz_avataricon01, R.drawable.zz_avataricon02, R.drawable.zz_avataricon03, R.drawable.zz_avataricon04,
			R.drawable.zz_avataricon05, R.drawable.zz_avataricon06, R.drawable.zz_avataricon07, R.drawable.zz_avataricon08,
			R.drawable.zz_avataricon09, R.drawable.zz_avataricon10, R.drawable.zz_avataricon11, R.drawable.zz_avataricon12,
	};

	public static final int[] AVATAR_NAMES = {
			R.id.playername01, R.id.playername02, R.id.playername03, R.id.playername04, R.id.playername05, R.id.playername06, R.id.playername07, R.id.playername08, R.id.playername09, R.id.playername10,
			R.id.playername11, R.id.playername12
	};

	int playerNumber; // KRP, drop "static"

	public static final String SHARED_PREFS = "sharedPrefs";

	private static final Logger LOGGER = Logger.getLogger(ChoosePlayer.class.getName());

	ConstraintLayout startCL;

	public static SoundPool gameSounds;
	public static int correctSoundID;
	public static int incorrectSoundID;
	public static int correctFinalSoundID;
	public static HashMap<String, Integer> speechIDs;
	public static int correctSoundDuration;
	public static int incorrectSoundDuration;
	public static int correctFinalSoundDuration;
	public static HashMap<String, Integer> speechDurations;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		context = this;

		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_player);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

		startCL = findViewById(R.id.startCL);

		// populate arrays from what is actually in the layout
		avatarIdList = new ArrayList();
		avatarJpgList = new ArrayList();

		for (int j = 0; j < startCL.getChildCount(); j++)
		{
			View child = startCL.getChildAt(j);
			if (child instanceof ImageView && child.getTag() != null)
			{
				avatarIdList.add(child.getId());
				avatarJpgList.add(((ImageView)child).getDrawable());
			}
		}
/*
		buildLangInfoArray();
		LOGGER.info("Remember: completed buildLangInfoArray() and buildNamesArray()");

		buildKeysArray();
		LOGGER.info("Remember: completed buildKeysArray()");

		buildSettingsArray();
		LOGGER.info("Remember: completed buildSettingsArray()");

		buildGamesArray();
		LOGGER.info("Remember: completed buildGamesArray()");
*/
		SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);

		int nameID;
		String defaultName;
		String playerName;
		for (int n = 0; n < AVATAR_NAMES.length; n++)
		{

			String localWordForName = langInfoList.find("NAME in local language");
			nameID = n + 1;
			if (localWordForName.equals("custom"))
			{
				defaultName = nameList.get(nameID - 1);
			}
			else
			{
				defaultName = localWordForName + " " + nameID;
			}

			String playerString = Util.returnPlayerStringToAppend(nameID);
			playerName = prefs.getString("storedName" + playerString, defaultName);

			TextView name = findViewById(AVATAR_NAMES[n]);
			name.setText(playerName);

		}

		setTextSizes();

		if(forceRTL){
			forceRTLIfSupported();
		}

	}

	@Override
	public void onBackPressed()
	{
		// no action
	}

	private int getAssetDuration(int assetID)
	{
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		AssetFileDescriptor afd = context.getResources().openRawResourceFd(assetID);
		mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
		return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
	}

	public void setTextSizes()
	{

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int heightOfDisplay = displayMetrics.heightPixels;
		int pixelHeight = 0;
		double scaling = 0.35;
		int bottomToTopId;
		int topToTopId;
		float percentBottomToTop;
		float percentTopToTop;
		float percentHeight;

		for (int n = 0; n < AVATAR_NAMES.length; n++)
		{

			TextView key = findViewById(AVATAR_NAMES[n]);
			if (n == 0)
			{
				ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams)key.getLayoutParams();
				bottomToTopId = lp1.bottomToTop;
				topToTopId = lp1.topToTop;
				percentBottomToTop = ((ConstraintLayout.LayoutParams)findViewById(bottomToTopId).getLayoutParams()).guidePercent;
				percentTopToTop = ((ConstraintLayout.LayoutParams)findViewById(topToTopId).getLayoutParams()).guidePercent;
				percentHeight = percentBottomToTop - percentTopToTop;
				pixelHeight = (int)(scaling * percentHeight * heightOfDisplay);
			}
			key.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

		}

	}

/*
		public void buildWordAndTileArrays()
		{
			LOGGER.info("Remember: entered buildAllArrays() method");
	//        Util.logMemory();
			buildTilesArray();
			LOGGER.info("Remember: completed buildTilesArray()");
	//        Util.logMemory();
			buildWordsArray();
			LOGGER.info("Remember: completed buildWordsArray()");
	//        Util.logMemory();

			// load music sounds
			gameSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
			correctSoundID = gameSounds.load(context, R.raw.zz_correct, 3);
			incorrectSoundID = gameSounds.load(context, R.raw.zz_incorrect, 3);
			correctFinalSoundID = gameSounds.load(context, R.raw.zz_correct_final, 1);

			correctSoundDuration = getAssetDuration(R.raw.zz_correct) + 200;
	//		incorrectSoundDuration = getAssetDuration(R.raw.zz_incorrect);	// not needed atm
	//		correctFinalSoundDuration = getAssetDuration(R.raw.zz_correct_final);	// not needed atm

			// load speech sounds
			Resources res = context.getResources();
			speechIDs = new HashMap();
			speechDurations = new HashMap();
			for (Word word : wordList)
			{
				int resId = res.getIdentifier(word.nationalWord, "raw", context.getPackageName());
				speechIDs.put(word.nationalWord, gameSounds.load(context, resId, 2));
				speechDurations.put(word.nationalWord, getAssetDuron(resId) + 200);
			}

		}

		public void buildTilesArray()
		{
			// KRP, Oct 2020
			// AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces

			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_gametiles));
			boolean header = true;
			tileList = new TileList();

			while (scanner.hasNext())
			{
				String thisLine = scanner.nextLine();
				String[] thisLineArray = thisLine.split("\t", 5);
				if (header)
				{
					tileList.baseTitle = thisLineArray[0];
					tileList.alt1Title = thisLineArray[1];
					tileList.alt2Title = thisLineArray[2];
					tileList.alt3Title = thisLineArray[3];
					tileList.tileTypeTitle = thisLineArray[4];
					header = false;
				}
				else
				{
					Tile tile = new Tile(thisLineArray[0], thisLineArray[1], thisLineArray[2], thisLineArray[3], thisLineArray[4]);
					if (!tile.hasNull())
					{
						tileList.add(tile);
					}
				}
			}
		}

		public void buildWordsArray()
		{
			// KRP, Oct 2020 (updated by AH to allow for spaces in fields (some common nouns in some languages have spaces)

			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_wordlist));
			boolean header = true;
			wordList = new WordList();
			while (scanner.hasNext())
			{
				String thisLine = scanner.nextLine();
				String[] thisLineArray = thisLine.split("\t");
				if (header)
				{
					wordList.nationalTitle = thisLineArray[0];
					wordList.localTitle = thisLineArray[1];
					header = false;
				}
				else
				{
					Word word = new Word(thisLineArray[0], thisLineArray[1]);
					if (!word.hasNull())
					{
						wordList.add(word);
					}
				}
			}
		}

		public void buildKeysArray()
		{
			// KRP, Oct 2020
			// AH, Nov 2020, updates to add second column (color theme)
			// AH Nov 2020, updated by AH to allow for spaces in fields (some common nouns in some languages have spaces

			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_keyboard)); // prep scan of aa_keyboard.txt
			boolean header = true;
			keyList = new KeyList();
			while (scanner.hasNext())
			{
				String thisLine = scanner.nextLine();
				String[] thisLineArray = thisLine.split("\t");
				if (header)
				{
					keyList.keysTitle = thisLineArray[0];
					keyList.colorTitle = thisLineArray[1];
					header = false;
				}
				else
				{
					Key key = new Key(thisLineArray[0], thisLineArray[1]);
					if (!key.hasNull())
					{
						keyList.add(key);
					}
				}
			}
		}

		public void buildGamesArray()
		{

			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_games)); // prep scan of aa_games.txt
			boolean header = true;
			gameList = new GameList();
			while (scanner.hasNext())
			{
				String thisLine = scanner.nextLine();
				String[] thisLineArray = thisLine.split("\t");
				if (header)
				{
					gameList.gameNumber = thisLineArray[0];
					gameList.gameCountry = thisLineArray[1];
					gameList.gameLevel = thisLineArray[2];
					gameList.gameColor = thisLineArray[3];
					header = false;
				}
				else
				{
					Game game = new Game(thisLineArray[0], thisLineArray[1], thisLineArray[2], thisLineArray[3]);
					if (!game.hasNull())
					{
						gameList.add(game);
					}
				}
			}
		}

		public void buildSettingsArray()
		{

			boolean header = true;
			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_settings)); // prep scan of aa_settings.txt

			settingsList = new SettingsList();
			while (scanner.hasNext())
			{
				if (scanner.hasNextLine())
				{
					if (header)
					{
						settingsList.title = scanner.nextLine();
						header = false;
					}
					else
					{
						String thisLine = scanner.nextLine();
						String[] thisLineArray = thisLine.split("\t");
						settingsList.put(thisLineArray[0], thisLineArray[1]);
					}
				}
			}

		}

		public void buildLangInfoArray()
		{

			boolean header = true;
			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_langinfo)); // prep scan of aa_langinfo.txt

			langInfoList = new LangInfoList();
			while (scanner.hasNext())
			{
				if (scanner.hasNextLine())
				{
					if (header)
					{
						langInfoList.title = scanner.nextLine();
						header = false;
					}
					else
					{
						String thisLine = scanner.nextLine();
						String[] thisLineArray = thisLine.split("\t");
						langInfoList.put(thisLineArray[0], thisLineArray[1]);
					}
				}
			}

			localAppName = langInfoList.find("Game Name");

			String localWordForName = langInfoList.find("NAME in local language");
			if (localWordForName.equals("custom"))
			{
				buildNamesArray();
			}

		}

		public void buildNamesArray()
		{

			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_names)); // prep scan of aa_names.txt

			boolean header = true;

			nameList = new AvatarNameList();
			while (scanner.hasNext())
			{
				if (scanner.hasNextLine())
				{
					if (header)
					{
						nameList.title = scanner.nextLine();
						header = false;
					}
					else
					{
						String thisLine = scanner.nextLine();
						String[] thisLineArray = thisLine.split("\t");
						nameList.add(thisLineArray[1]);
					}
				}
			}

			localAppName = langInfoList.find("Game Name");

		}
	*/
	public void goToEarthFromAvatar(View view)
	{

		playerNumber = Integer.parseInt((String)view.getTag());
		//buildWordAndTileArrays();
		Intent intent = new Intent(context, Earth.class);
		intent.putExtra("playerNumber", playerNumber);
		intent.putExtra("settingsList", settingsList);
		startActivity(intent);
		finish();

	}

	public void goToNameAvatarFromAvatar(View view)
	{

		LOGGER.info("Remember: just entered goToNameAvatarFromAvatar(View view) method");
		playerNumber = Integer.parseInt((String)view.getTag());
		//buildWordAndTileArrays();

		Intent intent = new Intent(context, SetPlayerName.class);
		intent.putExtra("playerNumber", playerNumber);
//        intent.putExtra("wordsArraySize", wordsArraySize);
//        intent.putExtra("langInfoList", langInfoList);
		intent.putExtra("settingsList", settingsList);
		startActivity(intent);
		finish();

	}
/*
	public class Word
	{
		public String nationalWord;
		public String localWord;

		public Word(String nationalWord, String localWord)
		{
			this.nationalWord = nationalWord;
			this.localWord = localWord;
		}

		public boolean hasNull()
		{
			return nationalWord == null || localWord == null;
		}
	}

	public class Tile
	{
		public String baseTile;
		public String[] altTiles;
		public String tileType;

		public Tile(String baseTile, String alt1Tile, String alt2Tile, String alt3Tile, String tileType)
		{
			this.baseTile = baseTile;
			altTiles = new String[ALT_COUNT];
			altTiles[0] = alt1Tile;
			altTiles[1] = alt2Tile;
			altTiles[2] = alt3Tile;
			this.tileType = tileType;
		}

		public boolean hasNull()
		{
			if (baseTile == null || tileType == null)
				return true;
			for (String tile : altTiles)
				if (tile == null)
					return true;
			return false;
		}
	}

	public class Key
	{
		public String baseKey;
		public String keyColor;

		public Key(String baseKey, String keyColor)
		{
			this.baseKey = baseKey;
			this.keyColor = keyColor;
		}

		public boolean hasNull()
		{
			return baseKey == null || keyColor == null;
		}
	}

	public class Game
	{
		public String gameNumber;
		public String gameCountry;
		public String gameLevel;
		public String gameColor;

		public Game(String gameNumber, String gameCountry, String gameLevel, String gameColor)
		{
			this.gameNumber = gameNumber;
			this.gameCountry = gameCountry;
			this.gameLevel = gameLevel;
			this.gameColor = gameColor;
		}

		public boolean hasNull()
		{
			return gameNumber == null || gameCountry == null || gameLevel == null || gameColor == null;
		}
	}

	public class WordList extends ArrayList<Word>
	{
		public String nationalTitle;    // e.g. languages like English or Spanish (LWCs = Languages of Wider Communication)
		public String localTitle;    // e.g. LOPS (language of play) like Me'phaa, Kayan or Romani Gabor

		public int returnGroupOneCount(String someGameTile)
		{
			// Group One = words that START with the active tile

			ArrayList<String> parsedWordArrayFinal;

			int tilesCount = 0;

			for (int i = 0; i < size(); i++)
			{
				parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
				if (parsedWordArrayFinal.get(0).equals(someGameTile))
				{
					tilesCount++;
				}
			}

			return tilesCount;

		}

		public String[][] returnGroupOneWords(String someGameTile, int tilesCount)
		{
			// Group One = words that START with the active tile

			ArrayList<String> parsedWordArrayFinal;
			int hitsCounter = 0;

			String[][] wordsStartingWithTileArray = new String[tilesCount][2];

			for (int i = 0; i < wordList.size(); i++)
			{
				parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
				if (parsedWordArrayFinal.get(0).equals(someGameTile))
				{
					wordsStartingWithTileArray[hitsCounter][0] = get(i).nationalWord;
					wordsStartingWithTileArray[hitsCounter][1] = get(i).localWord;
					hitsCounter++;
				}
			}

			return wordsStartingWithTileArray;

		}

		public int returnGroupTwoCount(String someGameTile)
		{
			// Group Two = words that contain the active tile non-initially (but excluding initially)

			ArrayList<String> parsedWordArrayFinal;

			int tilesCount = 0;

			for (int i = 0; i < size(); i++)
			{
				parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
				for (int k = 1; k < parsedWordArrayFinal.size(); k++)
				{
					// k = 1, not 0, because you're looking for non-initial tiles
					if (parsedWordArrayFinal.get(k) != null)
					{
						if (parsedWordArrayFinal.get(k).equals(someGameTile))
						{
							tilesCount++;
							break;
						}
					}
				}
			}

			return tilesCount;

		}

		public String[][] returnGroupTwoWords(String someGameTile, int tilesCount)
		{
			// Group Two = words that contain the active tile non-initially (but excluding initially)

			ArrayList<String> parsedWordArrayFinal;
			int hitsCounter = 0;

			String[][] wordsWithNonInitialTiles = new String[tilesCount][2];

			for (int i = 0; i < size(); i++)
			{
				parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
				for (int k = 1; k < parsedWordArrayFinal.size(); k++)
				{
					// k = 1, not 0, because you're looking for non-initial tiles
					if (parsedWordArrayFinal.get(k) != null)
					{
						if (parsedWordArrayFinal.get(k).equals(someGameTile))
						{
							wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
							wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
							hitsCounter++;
							break;
						}
					}
				}
			}

			return wordsWithNonInitialTiles;

		}

		public int returnGroupThreeCount(String someGameTile)
		{
			// Group Three = words containing the active tile anywhere (initial and/or non-initial)

			ArrayList<String> parsedWordArrayFinal;

			int tilesCount = 0;

			for (int i = 0; i < size(); i++)
			{
				parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
				for (int k = 0; k < parsedWordArrayFinal.size(); k++)
				{
					if (parsedWordArrayFinal.get(k) != null)
					{
						if (parsedWordArrayFinal.get(k).equals(someGameTile))
						{
							tilesCount++;
							break;
						}
					}
				}
			}

			return tilesCount;

		}

		public String[][] returnGroupThreeWords(String someGameTile, int tilesCount)
		{
			// Group Three = words containing the active tile anywhere (initial and/or non-initial)

			ArrayList<String> parsedWordArrayFinal;

			int hitsCounter = 0;

			String[][] wordsWithNonInitialTiles = new String[tilesCount][2];

			for (int i = 0; i < size(); i++)
			{
				parsedWordArrayFinal = tileList.parseWord(get(i).localWord);
				for (int k = 0; k < parsedWordArrayFinal.size(); k++)
				{
					if (parsedWordArrayFinal.get(k) != null)
					{
						if (parsedWordArrayFinal.get(k).equals(someGameTile))
						{
							wordsWithNonInitialTiles[hitsCounter][0] = get(i).nationalWord;
							wordsWithNonInitialTiles[hitsCounter][1] = get(i).localWord;
							hitsCounter++;
							break;
						}
					}
				}
			}

			return wordsWithNonInitialTiles;

		}

		public String stripInstructionCharacters(String localWord)
		{
			// The period instructs the parseWord method to force a tile break
			String newString = localWord.replaceAll("[.]", "");
			return newString;
		}

	}

	public class TileList extends ArrayList<Tile>
	{
		public String baseTitle;
		public String alt1Title;
		public String alt2Title;
		public String alt3Title;
		public String tileTypeTitle;

		public ArrayList<String> parseWord(String parseMe)
		{
			// Updates by KRP, Oct 2020
			// AH, Nov 2020, extended to check up to four characters in a game tile

			ArrayList<String> parsedWordArrayTemp = new ArrayList();

			int charBlock;
			String next1; // the next one character from the string
			String next2; // the next two characters from the string
			String next3; // the next three characters from the string
			String next4; // // the next four characters from the string

			int i; // counter to iterate through the characters of the analyzed word
			int k; // counter to scroll through all game tiles for hits on the analyzed character(s) of the word string

			for (i = 0; i < parseMe.length(); i++)
			{

				// Create blocks of the next one, two, three and four Unicode characters for analysis
				next1 = parseMe.substring(i, i + 1);

				if (i < parseMe.length() - 1)
				{
					next2 = parseMe.substring(i, i + 2);
				}
				else
				{
					next2 = "XYZXYZ";
				}

				if (i < parseMe.length() - 2)
				{
					next3 = parseMe.substring(i, i + 3);
				}
				else
				{
					next3 = "XYZXYZ";
				}

				if (i < parseMe.length() - 3)
				{
					next4 = parseMe.substring(i, i + 4);
				}
				else
				{
					next4 = "XYZXYZ";
				}

				// See if the blocks of length one, two, three or four Unicode characters matches game tiles
				// Choose the longest block that matches a game tile and add that as the next segment in the parsed word array
				charBlock = 0;
				for (k = 0; k < size(); k++)
				{

//                    LOGGER.info("Remember: tileList.get(" + k + ").baseTile = " +  tileList.get(k).baseTile);

					if (next1.equals(tileList.get(k).baseTile) && charBlock == 0)
					{
						// If charBlock is already assigned 2 or 3 or 4, it should not overwrite with 1
//                        LOGGER.info("Remember: next1 = " + next1);
						charBlock = 1;
					}
					if (next2.equals(tileList.get(k).baseTile) && charBlock != 3 && charBlock != 4)
					{
						// The value 2 can overwrite 1 but it can't overwrite 3 or 4
//                        LOGGER.info("Remember: next2 = " + next2);
						charBlock = 2;
					}
					if (next3.equals(tileList.get(k).baseTile) && charBlock != 4)
					{
						// The value 3 can overwrite 1 or 2 but it can't overwrite 4
//                        LOGGER.info("Remember: next3 = " + next3);
						charBlock = 3;
					}
					if (next4.equals(tileList.get(k).baseTile))
					{
						// The value 4 can overwrite 1 or 2 or 3
						charBlock = 4;
					}
					if ((tileList.get(k).baseTile == null && k > 0))
					{
						k = tileList.size();
					}
				}

				// Add the selected game tile (the longest selected from the previous loop) to the parsed word array
				switch (charBlock)
				{
				case 1:
					parsedWordArrayTemp.add(next1);
					break;
				case 2:
					parsedWordArrayTemp.add(next2);
					i++;
					break;
				case 3:
					parsedWordArrayTemp.add(next3);
					i += 2;
					break;
				case 4:
					parsedWordArrayTemp.add(next4);
					i += 3;
					break;
				default:
					break;
				}

			}

//            for (int q =0; q < parsedWordArrayTemp.size(); q++) {
//                LOGGER.info("Remember parsedWordArrayTemp.get(" + q + ") = " + parsedWordArrayTemp.get(q));
//            }

			return parsedWordArrayTemp;
		}

		public String returnNextAlphabetTile(String oldTile)
		{

			String nextTile = "";
			for (int i = 0; i < size(); i++)
			{
				if (get(i).baseTile.equals(oldTile))
				{
					if (i < (size() - 1))
					{
						nextTile = get(i + 1).baseTile;
					}
					else// if (i == size() - 1) {
						nextTile = get(0).baseTile;
				}
			}

			return nextTile;

		}

		public String returnPreviousAlphabetTile(String oldTile)
		{

			String previousTile = "";
			for (int i = size() - 1; i >= 0; i--)
			{

				if (get(i).baseTile.equals(oldTile))
				{
					if (i > 0)
					{
						previousTile = get(i - 1).baseTile;
					}
					else// if (i == 0) {
						previousTile = get(size() - 1).baseTile;
				}
			}

			return previousTile;

		}

		public int returnPositionInAlphabet(String someGameTile)
		{

			int alphabetPosition = 0;
			for (int i = 0; i < size(); i++)
			{

				if (get(i).baseTile.equals(someGameTile))
				{
					alphabetPosition = i;
				}
			}

			return alphabetPosition;

		}

		public String returnRandomCorrespondingTile(String correctTile)
		{

			String wrongTile = "";
			Random rand = new Random();

			for (int i = 0; i < size(); i++)
			{
				if (get(i).baseTile.equals(correctTile))
				{
					int randomNum = rand.nextInt(get(i).altTiles.length);
					wrongTile = get(i).altTiles[randomNum];
					break;
				}
			}

			return wrongTile;

		}

	}

	public class KeyList extends ArrayList<Key>
	{

		public String keysTitle;
		public String colorTitle;

	}

	public class GameList extends ArrayList<Game>
	{

		public String gameNumber;
		public String gameCountry;
		public String gameLevel;
		public String gameColor;

	}

	public class LangInfoList extends HashMap<String, String>
	{

		public String title;

		public String find(String keyContains)
		{
			for (String k : keySet())
			{
				if (k.contains(keyContains))
				{
					return (get(k));
				}
			}
			return "";
		}
	}

	public class SettingsList extends HashMap<String, String>
	{

		public String title;

		public String find(String keyContains)
		{
			for (String k : keySet())
			{
				if (k.contains(keyContains))
				{
					return (get(k));
				}
			}
			return "";
		}
	}
*/
	public class AvatarNameList extends ArrayList<String>
	{

		public String title;

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void forceRTLIfSupported()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
			getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
		}
	}

}
