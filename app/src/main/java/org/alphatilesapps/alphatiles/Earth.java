package org.alphatilesapps.alphatiles;

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

import java.io.File;


public class Earth extends AppCompatActivity {
    Context context;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");

    int playerNumber = -1;
    String playerString;
    int pageNumber; // Games 001 to 023 are displayed on page 1, games 024 to 046 are displayed on page 2, etc.
    int globalPoints;
    int doorsPerPage = 23;
    ConstraintLayout earthCL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        playerString = Util.returnPlayerStringToAppend(playerNumber);
        setContentView(R.layout.earth);
        earthCL = findViewById(R.id.earthCL);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (scriptDirection.equals("RTL")) {
            ImageView goForwardImage = (ImageView) findViewById(R.id.goForward);
            ImageView goBackImage = (ImageView) findViewById(R.id.goBack);
            ImageView activePlayerImage = (ImageView) findViewById(R.id.activePlayerImage);

            goForwardImage.setRotationY(180);
            goBackImage.setRotationY(180);
            activePlayerImage.setRotationY(180);
        }

        setTitle(Start.localAppName);

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        globalPoints = getIntent().getIntExtra("globalPoints", 0);

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
        TextView name = findViewById(R.id.avatarName);
        name.setText(playerName);

        pageNumber = getIntent().getIntExtra("pageNumber", 0);

        updateDoors();

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

        if (context.getResources().getIdentifier("aa_share", "raw", context.getPackageName()) == 0) {
            ImageView shareIcon = findViewById(R.id.share);
            shareIcon.setVisibility(View.GONE);
            shareIcon.setOnClickListener(null);
        }

    }

    public void updateDoors() {

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        int trackerCount;

        for (int j = 0; j < earthCL.getChildCount(); j++) {
            View child = earthCL.getChildAt(j);
            if (child instanceof TextView && child.getTag() != null) {
                try {
                    int doorIndex = Integer.parseInt((String) earthCL.getChildAt(j).getTag()) - 1;
                    String doorText = String.valueOf((pageNumber * doorsPerPage) + doorIndex + 1);
                    ((TextView) child).setText(doorText);
                    if (((pageNumber * doorsPerPage) + doorIndex) >= Start.gameList.size()) {
                        ((TextView) child).setVisibility(View.INVISIBLE);
                    } else {
                        String project = "org.alphatilesapps.alphatiles.";
                        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).country;
                        String challengeLevel = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).level;
                        String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).mode;
                        String stage;
                        if (gameList.get((pageNumber * doorsPerPage) + doorIndex).stage.equals("-")) {
                            stage = "1";
                        } else {
                            stage = gameList.get((pageNumber * doorsPerPage) + doorIndex).stage;
                        }
                        String uniqueGameLevelPlayerModeStageID = project + country + challengeLevel + playerString + syllableGame + stage;

                        trackerCount = prefs.getInt(uniqueGameLevelPlayerModeStageID + "_trackerCount", 0);

                        // This is currently the only game that has no right/wrong responses with an incrementing trackerCount variable
                        // So we are forcing this game's door to initialize with a start
                        // This code is in two places
                        // If other "no right or wrong" games are added, probably better to add a new column in aa_games.txt with a classification
                        if (country.equals("Romania") || country.equals("Sudan")) {
                            trackerCount = 12;
                            ((TextView) child).setTextColor(Color.parseColor("#000000")); // black;
                        } else if (trackerCount < 12) {
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFF")); // white;
                        } else { // >= 12
                            String textColor = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).color;
                            ((TextView) child).setTextColor(Color.parseColor(colorList.get(Integer.parseInt(textColor))));
                        }

                        boolean changeColor = true;
                        String doorStyle = "";
                        if (country.equals("Sudan") || country.equals("Romania")) {
                            doorStyle = "_inprocess";
                        } else if (trackerCount > 0 && trackerCount < 12) {
                            doorStyle = "_inprocess";
                        } else if (trackerCount >= 12) {
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
        intent.putExtra("challengeLevel", challengeLevel);
        intent.putExtra("globalPoints", globalPoints);
        intent.putExtra("gameNumber", gameNumber);
        intent.putExtra("pageNumber", pageNumber);
        intent.putExtra("country", country);
        intent.putExtra("syllableGame", syllableGame);
        intent.putExtra("stage", stage);
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

    @Override
    public void onBackPressed() {
        // no action
    }
}