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
    import android.graphics.Color;
    import android.graphics.drawable.Drawable;
    import android.media.MediaPlayer;
    import android.os.Build;
    import android.os.Bundle;
    import android.util.Log;
    import android.util.Patterns;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    import static org.alphatilesapps.alphatiles.Start.*;

    import androidx.annotation.NonNull;

    import java.util.Scanner;

    public class Earth extends AppCompatActivity {
        Context context;

        int playerNumber = -1;
        String playerString;
        String playerName;
        char grade;
        SharedPreferences prefs;
        int pageNumber; // Games 001 to 006 are displayed on page 1, games 007 to 012 on page 2, etc.
        int globalPoints;
        int doorsPerPage = 6;
        ConstraintLayout earthCL;

        private static final int MAX_TRACKER_MARK_IMAGES = 22;

        private static final int[] DOOR_VIEW_IDS = {
                R.id.textDoor001, R.id.textDoor002, R.id.textDoor003,
                R.id.textDoor004, R.id.textDoor005, R.id.textDoor006
        };
        private static final int[] SCREENSHOT_VIEW_IDS = {
                R.id.gameScreenshot001, R.id.gameScreenshot002, R.id.gameScreenshot003,
                R.id.gameScreenshot004, R.id.gameScreenshot005, R.id.gameScreenshot006
        };
        private static final int[] MARK_VIEW_IDS = {
                R.id.textMark001, R.id.textMark002, R.id.textMark003,
                R.id.textMark004, R.id.textMark005, R.id.textMark006
        };
        private static final int[] POINTS_VIEW_IDS = {
                R.id.gamePoints001, R.id.gamePoints002, R.id.gamePoints003,
                R.id.gamePoints004, R.id.gamePoints005, R.id.gamePoints006
        };
        private static final int[] LEVEL_VIEW_IDS = {
                R.id.gameLevel001, R.id.gameLevel002, R.id.gameLevel003,
                R.id.gameLevel004, R.id.gameLevel005, R.id.gameLevel006
        };
        private static final int[] STAGE_VIEW_IDS = {
                R.id.gameStage001, R.id.gameStage002, R.id.gameStage003,
                R.id.gameStage004, R.id.gameStage005, R.id.gameStage006
        };
        private static final int[] ROW_VIEW_IDS = {
                R.id.row001, R.id.row002, R.id.row003,
                R.id.row004, R.id.row005, R.id.row006
        };

        @Override
        protected void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("pageNumber", pageNumber);
            outState.putInt("playerNumber", playerNumber);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (langInfoList == null) {
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

            if (savedInstanceState != null) {
                pageNumber = savedInstanceState.getInt("pageNumber", pageNumber);
                playerNumber = savedInstanceState.getInt("playerNumber", -1);
            } else {
                pageNumber = getIntent().getIntExtra("pageNumber", 0);
                playerNumber = getIntent().getIntExtra("playerNumber", -1);
            }

            context = this;
            playerString = Util.returnPlayerStringToAppend(playerNumber);
            prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);

            String defaultName;
            String localWordForName = langInfoList.find("NAME in local language");
            if (localWordForName.equals("custom")) {
                defaultName = nameList.get(playerNumber - 1);
            } else {
                defaultName = localWordForName + " " + playerNumber;
            }
            playerName = prefs.getString("storedName" + playerString, defaultName);

            // find one-digit grade level of student (ASSUMES GRADE IS ONLY ONE DIGIT LONG AND ONLY DIGIT IN NAME)
            for (int i = 0; i < playerName.length(); i++) {
                char nameChar = playerName.charAt(i);
                if (Character.isDigit(nameChar)) grade = nameChar;
            }

            loadEarthLayout();
//            earthCL.post(this::updateDoors);
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

            for (int i = 0; i < doorsPerPage; i++) {
                int gameIndex = (pageNumber * doorsPerPage) + i;

                View row = findViewById(ROW_VIEW_IDS[i]);
                TextView door = findViewById(DOOR_VIEW_IDS[i]);
                ImageView screenshot = findViewById(SCREENSHOT_VIEW_IDS[i]);
                TextView mark = findViewById(MARK_VIEW_IDS[i]);
                TextView pointsView = findViewById(POINTS_VIEW_IDS[i]);
                TextView levelView = findViewById(LEVEL_VIEW_IDS[i]);
                TextView stageView = findViewById(STAGE_VIEW_IDS[i]);

                if (gameIndex >= Start.gameList.size()) {
                    if (row != null) row.setVisibility(View.INVISIBLE);
                    if (door != null) door.setVisibility(View.INVISIBLE);
                    continue;
                }

                if (row != null) row.setVisibility(View.VISIBLE);

                String doorTag = String.valueOf(i + 1);
                View.OnClickListener rowClickListener = this::goToDoor;

                if (door != null) { door.setTag(doorTag); door.setOnClickListener(rowClickListener); }
                if (screenshot != null) { screenshot.setTag(doorTag); screenshot.setOnClickListener(rowClickListener); }

                Start.Game game = Start.gameList.get(gameIndex);
                String country = game.country;
                String uniqueGameID = getUniqueID(gameIndex);
                int points = prefs.getInt(uniqueGameID + "_points", 0);
    //            boolean masteryAchieved = prefs.getBoolean(uniqueGameID + "_masteryAchieved", false);

                boolean isPassive = country.equals("Romania") || country.equals("Sudan") || country.equals("Malaysia") || country.equals("Iraq") || country.equals("Indonesia");

                // (a) Door setup - shape remains constant (zz_door), colored with game color
                if (door != null) {
                    door.setText(String.valueOf(gameIndex + 1));
                    door.setVisibility(View.VISIBLE);

                    Drawable unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.zz_door_new);
                    if (unwrappedDrawable != null) {
                        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable).mutate();
                        try {
                            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(colorList.get(Integer.parseInt(game.color))));
                        } catch (Exception e) {
                            Log.w("Earth", "Bad door color '" + game.color + "' for game " + (gameIndex + 1), e);
                        }
                        door.setBackground(wrappedDrawable);
                    }
                    // Specified color from aa_games.txt, else black for passive and white for active.
                    int doorTextColorInt = isPassive ? Color.BLACK : Color.WHITE;
                    try {
                        if (!game.doorTextColor.isEmpty()) {
                            doorTextColorInt = Color.parseColor(
                            colorList.get(Integer.parseInt(game.doorTextColor)));
                        }
                    } catch (Exception e) {
                        Log.w("Earth", "Bad door text color '" + game.doorTextColor + "' for game " + (gameIndex + 1), e);
                    }
                    door.setTextColor(doorTextColorInt);
                }

                // (b) Game Screenshot
                if (screenshot != null) {
                    String scriptType = Start.scriptType != null ? Start.scriptType.toLowerCase() : "roman";
                    if (scriptType.isEmpty()) {
                        scriptType = "roman";
                    }

                    String base = "zc_" + country.toLowerCase() + "_" + game.level.toLowerCase() + "_" + game.mode.toLowerCase();
                    boolean hasStage = !game.stage.equals("-");
                    String stage = game.stage.toLowerCase();

                    int resId = 0;
                    String imageName;

                    // 1. Look for zc file with a final stage identifier and then the script identifier (e.g. _7_roman or _5_arabic)
                    if (hasStage) {
                        imageName = base + "_" + stage + "_" + scriptType;
                        resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    }

                    // 2. If not found, next look for a stage unidentified version with the correct script identifier (i.e. _arabic)
                    if (resId == 0) {
                        imageName = base + "_" + scriptType;
                        resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    }

                    // 3. If not found, next look for a stage identified version for Roman script (i.e. _2_roman)
                    if (resId == 0 && hasStage && !scriptType.equals("roman")) {
                        imageName = base + "_" + stage + "_roman";
                        resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    }

                    // 4. Last, display a non-stage identified Roman script version ("_roman")
                    if (resId == 0 && !scriptType.equals("roman")) {
                        imageName = base + "_roman";
                        resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    }

                    if (resId != 0) {
                        screenshot.setImageResource(resId);
                    } else {
                        screenshot.setImageResource(R.drawable.zz_no_image_found);
                    }
                    screenshot.setVisibility(View.VISIBLE);
                }

                // (c) Gem progress mark & (d) Points
                if (isPassive) {
                    if (mark != null) mark.setVisibility(View.INVISIBLE);
                    if (pointsView != null) pointsView.setVisibility(View.INVISIBLE);
                } else {
                    if (mark != null) {
                        int correct = prefs.getInt(uniqueGameID + "_totalCorrect", 0);
                        updateMarkForTotalCorrect(mark, gameIndex, correct, game.lookBack);
                    }
                    if (pointsView != null) {
                        pointsView.setText("\u200E+" + points);
                        pointsView.setVisibility(View.VISIBLE);
                    }
                }

                // (e) Challenge Level / Passive Exclusivity Indicator
                if (levelView != null) {
                    if (isPassive) {
                        int stagePlusExclusivity = 71;
                        if (!game.stage.equals("-")) {
                            try {
                                stagePlusExclusivity = Integer.parseInt(game.stage);
                            } catch (NumberFormatException ignored) {}
                        }
                        boolean exclusive = true;
                        if (stagePlusExclusivity >= 10) {
                            exclusive = ((stagePlusExclusivity % 10) == 0);
                        }
                        if (exclusive) {
                            levelView.setText("◎\n◉\n◎");
                        } else {
                            levelView.setText("◉\n◉\n◉");
                        }
                    } else {
                        try {
                            int challengeLevel = Integer.parseInt(game.level);
                            int displayedChallengeLevel = Util.getDisplayedChallengeLevel(country, challengeLevel);
                            levelView.setText(String.valueOf(displayedChallengeLevel));
                        } catch (NumberFormatException ex) {
                            levelView.setText(game.level);
                        }
                    }
                    levelView.setVisibility(View.VISIBLE);
                }

                // (f) Stage Indicator
                if (stageView != null) {
                    if (stagesInUse > 1) {
                        int stageVal = 1;
                        if (!game.stage.equals("-")) {
                            try {
                                int stagePlusExclusivity = Integer.parseInt(game.stage);
                                if (stagePlusExclusivity >= 10) {
                                    stageVal = stagePlusExclusivity / 10;
                                } else {
                                    stageVal = stagePlusExclusivity;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                        stageView.setText(String.valueOf(stageVal));
                    } else {
                        stageView.setText("★");
                    }
                    stageView.setVisibility(View.VISIBLE);
                }
            }

            ImageView backArrow = findViewById(R.id.goBack);
            if (backArrow != null) {
                if (pageNumber == 0) {
                    backArrow.setVisibility(View.INVISIBLE);
                } else {
                    backArrow.setVisibility(View.VISIBLE);
                }
            }

            ImageView forwardArrow = findViewById(R.id.goForward);
            if (forwardArrow != null) {
                if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
                    forwardArrow.setVisibility(View.VISIBLE);
                } else {
                    forwardArrow.setVisibility(View.INVISIBLE);
                }
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

        private void updateMarkForTotalCorrect(TextView mark, int gameIndex, int correct, int masteryLookBackWindow) {
            if (masteryLookBackWindow <= 0) {
                mark.setVisibility(View.INVISIBLE);
                return;
            }

            int trackerTier = correct / masteryLookBackWindow;
            if (trackerTier < 1) {
                mark.setVisibility(View.INVISIBLE);
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
            } else {
                mark.setVisibility(View.INVISIBLE);
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

        public void exitApp(View view) {
            finishAffinity();
        }

        public void goToDoor(View view) {
            int doorIndex = Integer.parseInt((String) view.getTag()) - 1;
            String project = "org.alphatilesapps.alphatiles.";
            String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).country;
            String activityClass = project + country;

            int challengeLevel = Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).level);
            int gameNumber = (pageNumber * doorsPerPage) + doorIndex + 1;
            String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).mode;
            int masteryLookBackWindow = gameList.get((pageNumber * doorsPerPage) + doorIndex).lookBack;
            int masteryRequiredAccuracy = gameList.get((pageNumber * doorsPerPage) + doorIndex).accuracy;
            int masteryMinAttempts = gameList.get((pageNumber * doorsPerPage) + doorIndex).attempts;
            int stagePlusExclusivity = 71;
            String stageStr = gameList.get((pageNumber * doorsPerPage) + doorIndex).stage;
            if (stageStr.equals("-")) {
                stagePlusExclusivity = 1;
            } else {
                try {
                    stagePlusExclusivity = Integer.parseInt(stageStr.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }

            Intent intent = getIntent();
            try {
                intent.setClass(context, Class.forName(activityClass));
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
            intent.putExtra("stage", stagePlusExclusivity);
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
            setClickableRecursive(findViewById(R.id.earthCL), false);
        }

        protected void setAllElemsClickable() {
            setClickableRecursive(findViewById(R.id.earthCL), true);
        }

        private void loadEarthLayout() {
            boolean showLevelStage = prefs.getBoolean(playerString + "_showLevelStage", false);
            if (showLevelStage) {
                setContentView(R.layout.earth);
            } else {
                setContentView(R.layout.earth_minimal);
            }
            earthCL = findViewById(R.id.earthCL);

            ActivityLayouts.applyEdgeToEdge(this, R.id.earthCL);
            ActivityLayouts.setStatusAndNavColors(this);

            String scriptDirection = langInfoList.find("Script direction (LTR or RTL)");
            ImageView activePlayerImage = findViewById(R.id.activePlayerImage);
            if (scriptDirection.equals("RTL")) {
                ImageView goForwardImage = findViewById(R.id.goForward);
                ImageView goBackImage = findViewById(R.id.goBack);
                ImageView resourcePromo = findViewById(R.id.resourcePromo);
                ImageView instructionsButton = findViewById(R.id.instructions);
                ImageView share = findViewById(R.id.share);
                ImageView exit = findViewById(R.id.exit);

                if (goForwardImage != null) goForwardImage.setRotationY(180);
                if (goBackImage != null) goBackImage.setRotationY(180);
                if (activePlayerImage != null) activePlayerImage.setRotationY(180);
                if (resourcePromo != null) resourcePromo.setRotationY(180);
                if (instructionsButton != null) instructionsButton.setRotationY(180);
                if (share != null) share.setRotationY(180);
                if (exit != null) exit.setRotationY(180);
            }

            globalPoints = prefs.getInt(playerString + "_globalPoints", 0);

            TextView pointsEarned = findViewById(R.id.pointsTextView);
            if (pointsEarned != null) {
                pointsEarned.setText(String.valueOf(globalPoints));
                pointsEarned.setOnClickListener(v -> {
                    boolean current = prefs.getBoolean(playerString + "_showLevelStage", false);
                    prefs.edit().putBoolean(playerString + "_showLevelStage", !current).apply();
                    loadEarthLayout();
                    if (scriptDirection.equals("RTL")) {
                        forceRTLIfSupported();
                    } else {
                        forceLTRIfSupported();
                    }
                    updateDoors();
                });
            }

            if (activePlayerImage != null && playerNumber > 0 && playerNumber <= ChoosePlayer.AVATAR_JPG_IDS.length) {
                int resID = getResources().getIdentifier(String.valueOf(ChoosePlayer.AVATAR_JPG_IDS[playerNumber - 1]), "drawable", getPackageName());
                activePlayerImage.setImageResource(resID);
            }

            // Setup that must be redone every time a fresh layout is inflated
            TextView name = findViewById(R.id.avatarName);
            if (name != null) name.setText(playerName);

            if (scriptDirection.equals("RTL")) {
                forceRTLIfSupported();
            } else {
                forceLTRIfSupported();
            }

            int zzzEarthResID = context.getResources().getIdentifier("zzz_earth", "raw", context.getPackageName());
            if (zzzEarthResID == 0) {
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
                    if (!shareScanner.hasNext()) {
                        noShareIcon = true;
                    } else {
                        String shareLink = shareScanner.nextLine().trim();
                        if (shareLink.isEmpty() || !Patterns.WEB_URL.matcher(shareLink).matches()) {
                            noShareIcon = true;
                        }
                    }
                } else {
                    noShareIcon = true;
                }
            }
            if (noShareIcon) {
                ImageView shareIcon = findViewById(R.id.share);
                shareIcon.setVisibility(View.GONE);
                shareIcon.setOnClickListener(null);
            }

            boolean noResources = true;
            if (context.getResources().getIdentifier("aa_resources", "raw", context.getPackageName()) != 0) {
                Scanner resourceScanner = new Scanner(getResources().openRawResource(R.raw.aa_resources));
                if (resourceScanner.hasNext()) {
                    resourceScanner.nextLine();
                    if (resourceScanner.hasNext() && !resourceScanner.next().isEmpty()) {
                        noResources = false;
                    }
                }
            }
            if (noResources) {
                ImageView resourcesIcon = findViewById(R.id.resourcePromo);
                resourcesIcon.setVisibility(View.GONE);
                resourcesIcon.setOnClickListener(null);
            }

            ImageView exitButtonImage = findViewById(R.id.exit);
            if (exitButton) {
                exitButtonImage.setVisibility(View.VISIBLE);
            } else {
                exitButtonImage.setVisibility(View.INVISIBLE);
            }
        }

        // Only touches views that have a click listener (set in code or via android:onClick),
        // including views nested inside the game rows.
        private void setClickableRecursive(ViewGroup parent, boolean clickable) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child.hasOnClickListeners()) {
                    child.setClickable(clickable);
                }
                if (child instanceof ViewGroup) {
                    setClickableRecursive((ViewGroup) child, clickable);
                }
            }
        }
    }