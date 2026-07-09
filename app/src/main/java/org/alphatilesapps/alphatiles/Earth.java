package org.alphatilesapps.alphatiles;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static org.alphatilesapps.alphatiles.Start.*;

import java.util.Scanner;
import java.util.logging.Logger;


public class Earth extends AppCompatActivity {
    Context context;

    int playerNumber = -1;
    String playerString;
    char grade;
    SharedPreferences prefs;
    int pageNumber; // Games 001 to 033 are displayed on page 1, games 034 to 066 are displayed on page 2, etc.
    int globalPoints;
//    int totalCorrect;
    int doorsPerPage = 33;
    ConstraintLayout earthCL;

    private static final int MAX_TRACKER_MARK_IMAGES = 22;

    private static final int[] MARK_VIEW_IDS = {
            R.id.textMark001, R.id.textMark002, R.id.textMark003, R.id.textMark004, R.id.textMark005,
            R.id.textMark006, R.id.textMark007, R.id.textMark008, R.id.textMark009, R.id.textMark010,
            R.id.textMark011, R.id.textMark012, R.id.textMark013, R.id.textMark014, R.id.textMark015,
            R.id.textMark016, R.id.textMark017, R.id.textMark018, R.id.textMark019, R.id.textMark020,
            R.id.textMark021, R.id.textMark022, R.id.textMark023, R.id.textMark024, R.id.textMark025,
            R.id.textMark026, R.id.textMark027, R.id.textMark028, R.id.textMark029, R.id.textMark030,
            R.id.textMark031, R.id.textMark032, R.id.textMark033
    };

    private static final Logger LOGGER = Logger.getLogger( Earth.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Start.langInfoList == null) {
            // Process was killed and restarted directly into this screen.
            // Relaunch from the beginning so static state gets repopulated.
            Intent intent = new Intent(this, Start.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Disable back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Intentionally empty — do nothing
            }
        });

        context = this;
        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        playerString = Util.returnPlayerStringToAppend(playerNumber);
        setContentView(R.layout.earth);
        earthCL = findViewById(R.id.earthCL);

        ActivityLayouts.applyEdgeToEdge(this, R.id.earthCL);
        ActivityLayouts.setStatusAndNavColors(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
        if (scriptDirection.equals("RTL")) {
            ImageView goForwardImage = findViewById(R.id.goForward);
            ImageView goBackImage = findViewById(R.id.goBack);
            ImageView activePlayerImage = findViewById(R.id.activePlayerImage);

            goForwardImage.setRotationY(180);
            goBackImage.setRotationY(180);
            activePlayerImage.setRotationY(180);
        }

        prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        globalPoints = prefs.getInt(playerString + "_globalPoints", 0);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(globalPoints));

        ImageView avatar = findViewById(R.id.activePlayerImage);
        int resID = getResources().getIdentifier(String.valueOf(ChoosePlayer.AVATAR_JPG_IDS[playerNumber - 1]), "drawable", getPackageName());
        avatar.setImageResource(resID);

        String defaultName;
        String playerName;
        String localWordForName = langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            defaultName = Start.nameList.get(playerNumber - 1);
        } else {
            defaultName = localWordForName + " " + playerNumber;
        }
        playerName = prefs.getString("storedName" + playerString, defaultName);

        // find one-digit grade level of student (ASSUMES GRADE IS ONLY ONE DIGIT LONG AND ONLY DIGIT IN NAME)
        for (int i = 0; i < playerName.length(); i++) {
            char nameChar = playerName.charAt(i);
            if (Character.isDigit(nameChar)) grade = nameChar;
        }

        TextView name = findViewById(R.id.avatarName);
        name.setText(playerName);

        pageNumber = getIntent().getIntExtra("pageNumber", 0);

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        resID = context.getResources().getIdentifier("zzz_earth", "raw", context.getPackageName());
        if (resID == 0) {
            // hide audio instructions icon
            ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
            instructionsButton.setVisibility(View.GONE);

            ConstraintLayout constraintLayout = findViewById(R.id.earthCL);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.centerHorizontally(R.id.resourcePromo, R.id.earthCL);
            constraintSet.applyTo(constraintLayout);
        }

        boolean noShareIcon = false;
        if (context.getResources().getIdentifier("aa_share", "raw", context.getPackageName()) == 0) {
            noShareIcon = true;
        } else {
            Scanner shareScanner = new Scanner(getResources().openRawResource(R.raw.aa_share));
            if (shareScanner.hasNext()) {
                shareScanner.nextLine(); // skip the header line
                if (!shareScanner.hasNext())
                    noShareIcon = true;
                else if (shareScanner.next().isEmpty())
                    noShareIcon = true;
            } else {
                noShareIcon = true;
            }
        }
        if (noShareIcon) { // if aa_share does not have a second line, don't display a share icon
            //if (true) {
            ImageView shareIcon = findViewById(R.id.share);
            shareIcon.setVisibility(View.GONE);
            shareIcon.setOnClickListener(null);
        }
        boolean noResources = true;
        if (context.getResources().getIdentifier("aa_resources", "raw", context.getPackageName()) != 0) { // Checks if resource file exists
            Scanner resourceScanner = new Scanner(getResources().openRawResource(R.raw.aa_resources));
            if (resourceScanner.hasNext()) { // See if there is anything in resource file
                resourceScanner.nextLine(); // Skips the header line
                if (resourceScanner.hasNext() && !resourceScanner.next().isEmpty()) { // If there is a line after the header that is not an empty string ""
                    noResources = false;
                }
            }
        }
        if (noResources) {
            ImageView resourcesIcon = findViewById(R.id.resourcePromo);
            resourcesIcon.setVisibility(View.GONE);
            resourcesIcon.setOnClickListener(null);
        }

        // Run after layout and other onCreate constraint changes so marks are not left at XML defaults
        earthCL.post(this::updateDoors);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (earthCL != null) {
            updateDoors();
        }
    }

    public void updateDoors() {

        prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);

        for (int j = 0; j < earthCL.getChildCount(); j++) {
            View child = earthCL.getChildAt(j);
            if (child instanceof TextView && child.getTag() != null) {
                try {
                    String viewEntryName = getResources().getResourceEntryName(child.getId());
                    if (viewEntryName.startsWith("textMark")) {
                        continue;
                    }
                    int doorIndex = Integer.parseInt((String) earthCL.getChildAt(j).getTag()) - 1;
                    String doorText = String.valueOf((pageNumber * doorsPerPage) + doorIndex + 1);
                    ((TextView) child).setText(doorText);
                    if (((pageNumber * doorsPerPage) + doorIndex) >= Start.gameList.size()) {
                        ((TextView) child).setVisibility(View.INVISIBLE);
                    } else {
                        String uniqueGameLevelPlayerModeStageID = getUniqueID((pageNumber * doorsPerPage) + doorIndex);
                        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).country;

//                      totalCorrect = prefs.getInt(uniqueGameLevelPlayerModeStageID + "_totalCorrect", 0);

                        prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
                        int points = prefs.getInt(uniqueGameLevelPlayerModeStageID + "_points", 0);
                        boolean masteryAchieved = prefs.getBoolean(uniqueGameLevelPlayerModeStageID + "_masteryAchieved", false);

                        // This is currently the only game that has no right/wrong responses with an incrementing totalCorrect variable
                        // So we are forcing this game's door to initialize with a start
                        // This code is in two places
                        // If other "no right or wrong" games are added, probably better to add a new column in aa_games.txt with a classification
                        if (country.equals("Romania") || country.equals("Sudan") || country.equals("Malaysia")|| country.equals("Iraq")) {
//                            totalCorrect = 12;
                            ((TextView) child).setTextColor(Color.parseColor("#000000")); // black;
                        } else if (!masteryAchieved) {
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFF")); // white;
                        } else {
                            String textColor = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).color;
                            ((TextView) child).setTextColor(Color.parseColor(colorList.get(Integer.parseInt(textColor))));
                        }

                        boolean changeColor = true;
                        String doorStyle = "";
                        if (country.equals("Romania") || country.equals("Sudan") || country.equals("Malaysia")|| country.equals("Iraq")) {                            doorStyle = "_inprocess";
                        } else if (points > 0 && !masteryAchieved) {
                            doorStyle = "_inprocess";
                        } else if (masteryAchieved) {
                            doorStyle = "_mastery";
                            changeColor = false;
                        } else { // 0
                            doorStyle = "";
                        }

                        String drawableBase = "zz_door";

                        String drawableEntryName = drawableBase + doorStyle;

                        int resId = getResources().getIdentifier(drawableEntryName, "drawable", getPackageName());
                        Drawable unwrappedDrawable = AppCompatResources.getDrawable(context, resId);
                        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                        if (changeColor) {
                            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(colorList.get(
                                    Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage)
                                            + doorIndex).color))));
                        }
                        ((TextView) child).setBackground(wrappedDrawable);
                        ((TextView) child).setVisibility(View.VISIBLE);

                    }
                } catch (Throwable ex)    // Never reached if tags are well formed!
                {
                    ex.printStackTrace();
                }
            }
        }

        updateAllMarks(prefs);

        ImageView backArrow = findViewById(R.id.goBack);
        if (pageNumber == 0) {
            backArrow.setVisibility(View.INVISIBLE);
        } else {
            backArrow.setVisibility(View.VISIBLE);
        }

        ImageView forwardArrow = findViewById(R.id.goForward);
        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            forwardArrow.setVisibility(View.VISIBLE);
        } else {
            forwardArrow.setVisibility(View.INVISIBLE);
        }

    }

    private String getUniqueID(int gameIndex) {
        String project = "org.alphatilesapps.alphatiles.";
        String country = Start.gameList.get(gameIndex).country;
        String challengeLevel = Start.gameList.get(gameIndex).level;
        String syllableGame = gameList.get(gameIndex).mode;
        String stage;
        if (gameList.get(gameIndex).stage.equals("-")) {
            stage = "1";
        } else {
            stage = gameList.get(gameIndex).stage;
        }

        return project + country + challengeLevel + playerString + syllableGame + stage;
    }

    private void updateAllMarks(SharedPreferences prefs) {
        for (int markSlot = 0; markSlot < MARK_VIEW_IDS.length; markSlot++) {
            TextView mark = findViewById(MARK_VIEW_IDS[markSlot]);
            if (mark == null || mark.getTag() == null) {
                continue;
            }
            int doorIndex = Integer.parseInt(mark.getTag().toString()) - 1;
            int gameIndex = (pageNumber * doorsPerPage) + doorIndex;
            if (gameIndex >= Start.gameList.size()) {
                mark.setVisibility(View.INVISIBLE);
                continue;
            }
            LOGGER.info("EarthX1: getUniqueID(gameIndex) = " + getUniqueID(gameIndex));
            int correct = prefs.getInt(getUniqueID(gameIndex) + "_totalCorrect", 0);
            LOGGER.info("EarthX2: updateAllMarks: correct = " + correct);
            int masteryLookBackWindow = gameList.get((pageNumber * doorsPerPage) + doorIndex).lookBack;
            updateMarkForTotalCorrect(mark, gameIndex, correct, masteryLookBackWindow);
        }
    }

    /**
     * tracker tiers (each tier is the size of masteryLookBackWindow):
     * Gem: none: 0 to one less than masteryLookBackWindow
     * Gem: # 1 : masteryLookBackWindow to ((2*masteryLookBackWindow)–1), but only if masteryAchieve = true
     * Gem: # 2 : (2*masteryLookBackWindow) to ((3*masteryLookBackWindow)–1), but only if masteryAchieve = true
     * etc.
     * Position comes from earth.xml (one mark view per door); only the image changes here.
     */
    private void updateMarkForTotalCorrect(TextView mark, int gameIndex, int correct, int masteryLookBackWindow) {

        LOGGER.info("EarthX3: gameIndex = " + gameIndex);
        LOGGER.info("EarthX4: correct = " + correct);
        LOGGER.info("EarthX5: masteryLookBackWindow = " + masteryLookBackWindow);

        int trackerTier = correct / masteryLookBackWindow;
        LOGGER.info("EarthX6: trackerTier = " + trackerTier);
        if (trackerTier < 1) {
            mark.setVisibility(View.INVISIBLE);
            LOGGER.info("EarthX7: View.INVISIBLE");
            return;
        }

        boolean masteryAchieved = prefs.getBoolean(getUniqueID(gameIndex) + "_masteryAchieved", false);

        if (masteryAchieved) {
            int imageIndex = Math.min(MAX_TRACKER_MARK_IMAGES, trackerTier);
            int trackerDrawableId = getResources().getIdentifier(
                    "tracker_" + String.format("%02d", imageIndex), "drawable", getPackageName());
            if (trackerDrawableId != 0) {
                mark.setBackgroundResource(trackerDrawableId);
            }
            mark.setVisibility(View.VISIBLE);
            LOGGER.info("EarthX8: View.VISIBLE");

        }
    }

    public void goToAboutPage(View view) {

        Intent intent = getIntent();
        intent.setClass(context, About.class);
        startActivity(intent);

    }

    public void goBackToChoosePlayer(View view) {

        startActivity(new Intent(context, ChoosePlayer.class));
        finish();

    }

    public void goToResources(View view) {

        Intent intent = getIntent();
        intent.setClass(context, Resources.class);
        startActivity(intent);

    }

    public void goToShare(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Share.class);
        startActivity(intent);
    }

    public void goToDoor(View view) {

        finish();
        int doorIndex = Integer.parseInt((String) view.getTag()) - 1;
        String project = "org.alphatilesapps.alphatiles.";  // how to call this with code? It seemed to produce variable results
        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).country;
        String activityClass = project + country;

        int challengeLevel = Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).level);
        int gameNumber = (pageNumber * doorsPerPage) + doorIndex + 1;
        String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).mode;
        int masteryLookBackWindow = gameList.get((pageNumber * doorsPerPage) + doorIndex).lookBack;
        int masteryRequiredAccuracy = gameList.get((pageNumber * doorsPerPage) + doorIndex).accuracy;
        int masteryMinAttempts = gameList.get((pageNumber * doorsPerPage) + doorIndex).attempts;
        int stage;
        if (gameList.get((pageNumber * doorsPerPage) + doorIndex).stage.equals("-")) {
            stage = 1;
        } else {
            stage = Integer.parseInt(gameList.get((pageNumber * doorsPerPage) + doorIndex).stage);
        }

        Intent intent = getIntent();    // preserve Extras
        try {
            intent.setClass(context, Class.forName(activityClass));    // so we retain the Extras
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(playerString + "_globalPoints", globalPoints);
        editor.apply();

        intent.putExtra("challengeLevel", challengeLevel);
        intent.putExtra("gameNumber", gameNumber);
        intent.putExtra("pageNumber", pageNumber);
        intent.putExtra("country", country);
        intent.putExtra("syllableGame", syllableGame);
        intent.putExtra("masteryLookBackWindow", masteryLookBackWindow);
        intent.putExtra("masteryRequiredAccuracy", masteryRequiredAccuracy);
        intent.putExtra("masteryMinAttempts", masteryMinAttempts);
        intent.putExtra("stage", stage);
        intent.putExtra("studentGrade", grade);
        startActivity(intent);
        finish();

    }

    public void goBackward(View view) {

        if (pageNumber > 0) {
            pageNumber--;
        }
        updateDoors();

    }

    public void goForward(View view) {

        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            pageNumber++;
        }
        updateDoors();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    public void playAudioInstructionsEarth(View view) {
        setAllElemsUnclickable();
        int resID = context.getResources().getIdentifier("zzz_earth", "raw", context.getPackageName());
        MediaPlayer mp3 = MediaPlayer.create(this, resID);
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                setAllElemsClickable();
                mp3.release();
            }
        });

    }

    protected void setAllElemsUnclickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.earthCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(false);
        }
    }

    protected void setAllElemsClickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.earthCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(true);
        }
    }

}