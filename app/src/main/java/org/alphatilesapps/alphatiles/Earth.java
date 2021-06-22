package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.logging.Logger;

import static android.view.View.VISIBLE;

import static org.alphatilesapps.alphatiles.Start.*;
import static org.alphatilesapps.alphatiles.Settings.forceRTL;

public class Earth extends AppCompatActivity {
    Context context;

    int points = 0;

    int challengeLevel;

    int playerNumber = -1;

    int gameNumber;

    int pageNumber; // Games 001 to 023 are displayed on page 1, games 024 to 046 are displayed on page 2, etc.

    int doorsPerPage = 23;

    ConstraintLayout earthCL;

    private static final Logger LOGGER = Logger.getLogger(Earth.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        context = this;

        playerNumber = getIntent().getIntExtra("playerNumber", -1);

        setContentView(R.layout.earth);

        earthCL = findViewById(R.id.earthCL);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        setTitle(Start.localAppName);

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        points = prefs.getInt("storedPoints_player" + playerString, 0);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(points));

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
        TextView name = findViewById(R.id.avatarName);
        name.setText(playerName);

        pageNumber = getIntent().getIntExtra("pageNumber", 0);

        updateDoors();

        setTextSizes();

        if(forceRTL){
            forceRTLIfSupported();
        }
        else{
            forceLTRIfSupported();
        }
    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void setTextSizes() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightOfDisplay = displayMetrics.heightPixels;
        int pixelHeight;
        double scaling = 0.45;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        TextView avatarName = (TextView) findViewById(R.id.avatarName);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) avatarName.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        avatarName.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        // Requires an extra step since the image is anchored to guidelines NOT the textview whose font size we want to edit
        TextView pointsEarned = findViewById(R.id.pointsTextView);
        ImageView pointsEarnedImage = (ImageView) findViewById(R.id.pointsImage);
        ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pointsEarnedImage.getLayoutParams();
        int bottomToTopId3 = lp3.bottomToTop;
        int topToTopId3 = lp3.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId3).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId3).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (0.7 * scaling * percentHeight * heightOfDisplay);
        pointsEarned.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        // The size of the text will depend on whether a star or a square/circle is loaded
        String project = "org.alphatilesapps.alphatiles.";  // how to call this with code? It seemed to produce variable results
        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);

        for (int j = 0; j < earthCL.getChildCount(); j++) {

            View child = earthCL.getChildAt(j);

            if (child instanceof TextView && child.getVisibility() == VISIBLE && child.getTag() != null) {
                int doorIndex = Integer.parseInt((String) earthCL.getChildAt(j).getTag()) - 1;
                String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameCountry;
                String challengeLevel = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameLevel;
                String uniqueGameLevelPlayerID = String.format("%s%s%s%s", project, country, challengeLevel, playerString);
                int trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

                // This is currently the only game that has no right/wrong responses with an incrementing trackerCount variable
                // So we are forcing this game's door to initialize with a start
                // This code is in two places
                // If other "no right or wrong" games are added, probably better to add a new column in aa_games.txt with a classification
                if (country.equals("Romania")) {
                    trackerCount = 12;
                }

                scaling = 0.45;
                if (trackerCount > 11) {
                    scaling = 0.25;
                }

                ConstraintLayout.LayoutParams lp4 = (ConstraintLayout.LayoutParams) child.getLayoutParams();
                int bottomToTopId4 = lp4.bottomToTop;
                int topToTopId4 = lp4.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId4).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId4).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

            }
        }
    }

    public void updateDoors() {

        String project = "org.alphatilesapps.alphatiles.";  // how to call this with code? It seemed to produce variable results

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        int trackerCount;

        for (int j = 0; j < earthCL.getChildCount(); j++) {
            View child = earthCL.getChildAt(j);
            if (child instanceof TextView && child.getTag() != null) {
                try {
                    int doorIndex = Integer.parseInt((String) earthCL.getChildAt(j).getTag()) - 1;
                    String doorText = String.valueOf((pageNumber * doorsPerPage) + doorIndex + 1);
                    ((TextView) child).setText(doorText);
                    if (((pageNumber * doorsPerPage) + doorIndex) >= Start.gameList.size() ) {
                        ((TextView) child).setVisibility(View.INVISIBLE);
                    } else {
                        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameCountry;
                        String challengeLevel = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameLevel;
                        String uniqueGameLevelPlayerID = String.format("%s%s%s%s", project, country, challengeLevel, playerString);

                        trackerCount = prefs.getInt(uniqueGameLevelPlayerID, 0);

                        // This is currently the only game that has no right/wrong responses with an incrementing trackerCount variable
                        // So we are forcing this game's door to initialize with a start
                        // This code is in two places
                        // If other "no right or wrong" games are added, probably better to add a new column in aa_games.txt with a classification
                        if (country.equals("Romania")) {
                            trackerCount = 12;
                        }

                        String color = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameColor;
                        String drawableBase = "zz_door_color" + color;

                        String doorStyle = "";
                        if (trackerCount > 0) {
                            doorStyle = trackerCount >= 12 ? "_mastery" : "_inprocess";
                        }

                        String drawableEntryName = drawableBase + doorStyle;

                        int resId = getResources().getIdentifier(drawableEntryName, "drawable", getPackageName());
                        Drawable doorDrawable = getResources().getDrawable(resId);
                        ((TextView) child).setBackground(doorDrawable);
                        ((TextView) child).setVisibility(View.VISIBLE);

                    }
                }
                catch (Throwable ex)	// never reached if tags are well formed!
                {
                    ex.printStackTrace();
                    continue;
                }
            }
        }

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

    public void goToAboutPage(View view)
    {
        Intent intent = getIntent();
        intent.setClass(context, About.class);
        startActivity(intent);
    }

    public void goBackToChoosePlayer(View view)
    {
        startActivity(new Intent(context, ChoosePlayer.class));
        finish();
    }

    public void goToResources(View view)
    {
        Intent intent = getIntent();
        intent.setClass(context, Resources.class);
        startActivity(intent);
    }

    public void goToDoor(View view) {

        int doorIndex = Integer.parseInt((String)view.getTag()) - 1;
        String project = "org.alphatilesapps.alphatiles.";  // how to call this with code? It seemed to produce variable results
        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameCountry;
        String activityClass = project + country;

        challengeLevel = Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).gameLevel);
        gameNumber = (pageNumber * doorsPerPage) + doorIndex + 1;

        Intent intent = getIntent();	// preserve Extras
        try {
            intent.setClass(context, Class.forName(activityClass));	// so we retain the Extras
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.putExtra("challengeLevel", challengeLevel);
        intent.putExtra("points", points);
        intent.putExtra("gameNumber", gameNumber);
        intent.putExtra("pageNumber", pageNumber);
        startActivity(intent);
        finish();

    }

    public void goBackward(View view) {

        if (pageNumber > 0) {
            pageNumber--;
        }
        LOGGER.info("Remember: pre updateDoors (Backward): pageNumber = " + pageNumber);
        updateDoors();
        LOGGER.info("Remember: post updateDoors (Backward): pageNumber = " + pageNumber);
        setTextSizes();

    }

    public void goForward(View view) {

        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            pageNumber++;
        }
        LOGGER.info("Remember: pre updateDoors (Forward): pageNumber = " + pageNumber);
        updateDoors();
        LOGGER.info("Remember: post updateDoors (Forward): pageNumber = " + pageNumber);
        setTextSizes();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }
}