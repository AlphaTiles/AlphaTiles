package org.alphatilesapps.alphatiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;   // KRP

import static org.alphatilesapps.alphatiles.Start.*;

public class ChoosePlayer extends AppCompatActivity {
    Context context;
    String scriptDirection = langInfoList.find("Script direction (LTR or RTL)");
    String singleColorHex = settingsList.find("Single hex color on avatar screen");

    public static ArrayList<Integer> avatarIdList;
    public static ArrayList<Drawable> avatarJpgList;

    public static final int[] AVATAR_IMAGE_IDS = {
            R.id.avatar01, R.id.avatar02, R.id.avatar03, R.id.avatar04, R.id.avatar05, R.id.avatar06,
            R.id.avatar07, R.id.avatar08, R.id.avatar09, R.id.avatar10, R.id.avatar11, R.id.avatar12
    };

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
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private final long ONE_DAY = 24 * 60 * 60 * 1000;


    ConstraintLayout choosePlayerCL;

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
    protected void onCreate(Bundle savedInstanceState) {

        context = this;

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_player);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (scriptDirection.equals("RTL")) {
            ImageView avatar01Image = (ImageView) findViewById(R.id.avatar01);
            ImageView avatar02Image = (ImageView) findViewById(R.id.avatar02);
            ImageView avatar03Image = (ImageView) findViewById(R.id.avatar03);
            ImageView avatar04Image = (ImageView) findViewById(R.id.avatar04);
            ImageView avatar05Image = (ImageView) findViewById(R.id.avatar05);
            ImageView avatar06Image = (ImageView) findViewById(R.id.avatar06);
            ImageView avatar07Image = (ImageView) findViewById(R.id.avatar07);
            ImageView avatar08Image = (ImageView) findViewById(R.id.avatar08);
            ImageView avatar09Image = (ImageView) findViewById(R.id.avatar09);
            ImageView avatar10Image = (ImageView) findViewById(R.id.avatar10);
            ImageView avatar11Image = (ImageView) findViewById(R.id.avatar11);
            ImageView avatar12Image = (ImageView) findViewById(R.id.avatar12);

            avatar01Image.setRotationY(180);
            avatar02Image.setRotationY(180);
            avatar03Image.setRotationY(180);
            avatar04Image.setRotationY(180);
            avatar05Image.setRotationY(180);
            avatar06Image.setRotationY(180);
            avatar07Image.setRotationY(180);
            avatar08Image.setRotationY(180);
            avatar09Image.setRotationY(180);
            avatar10Image.setRotationY(180);
            avatar11Image.setRotationY(180);
            avatar12Image.setRotationY(180);

        }
        choosePlayerCL = findViewById(R.id.choosePlayerCL);

        // Populate arrays from what is actually in the layout
        avatarIdList = new ArrayList();
        avatarJpgList = new ArrayList();

        for (int j = 0; j < choosePlayerCL.getChildCount(); j++) {
            View child = choosePlayerCL.getChildAt(j);
            if (child instanceof ImageView && child.getTag() != null) {
                avatarIdList.add(child.getId());
                avatarJpgList.add(((ImageView) child).getDrawable());
            }
        }

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);

        int nameID;
        String defaultName;
        String playerName;
        for (int n = 0; n < AVATAR_NAMES.length; n++) {

            String localWordForName = langInfoList.find("NAME in local language");
            nameID = n + 1;
            if (localWordForName.equals("custom")) {
                defaultName = nameList.get(nameID - 1);
            } else {
                defaultName = localWordForName + " " + nameID;
            }

            String playerString = Util.returnPlayerStringToAppend(nameID);
            playerName = prefs.getString("storedName" + playerString, defaultName);

            TextView name = findViewById(AVATAR_NAMES[n]);
            name.setText(playerName);
            if (!singleColorHex.equals("")) {
                name.setBackgroundColor(Color.parseColor(singleColorHex));
            }
        }

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        // Show correct number of avatars
        for (int i = 0; i < numberOfAvatars; i++) {
            ImageView animal = findViewById(AVATAR_IMAGE_IDS[i]);
            animal.setVisibility(View.VISIBLE);
            animal.setClickable(true);

            TextView name = findViewById(AVATAR_NAMES[i]);
            name.setVisibility(View.VISIBLE);
            name.setClickable(true);
        }

        for (int i = numberOfAvatars; i < 12; i++) {
            ImageView animal = findViewById(AVATAR_IMAGE_IDS[i]);
            animal.setVisibility(View.INVISIBLE);
            animal.setClickable(false);

            TextView name = findViewById(AVATAR_NAMES[i]);
            name.setVisibility(View.INVISIBLE);
            name.setClickable(false);
        }

        String daysUntilExpirationSetting = settingsList.find("Days until expiration");

        if (!daysUntilExpirationSetting.equals("")) {
            int daysUntilExpiration = Integer.valueOf(daysUntilExpirationSetting);
            String installDate = prefs.getString("InstallDate", null);
            if (installDate == null) {
                // First run, so save the current date
                SharedPreferences.Editor editor = prefs.edit();
                Date now = new Date();
                String dateString = formatter.format(now);
                editor.putString("InstallDate", dateString);
                // Commit the edits!
                editor.commit();
            } else {
                // This is not the 1st run, check install date
                Date before = null;
                try {
                    before = (Date) formatter.parse(installDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date now = new Date();
                long diff = now.getTime() - before.getTime();
                long days = diff / ONE_DAY;

                if (days > daysUntilExpiration) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    // Set message manually and perform action on button click
                    builder.setMessage(R.string.expiration_dialog_message)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    Toast.makeText(getApplicationContext(), "Removing " + localAppName,
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Intent.ACTION_DELETE);
                                    intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                }
                            });

                    // Alert
                    AlertDialog alert = builder.create();
                    alert.setTitle(R.string.expiration_dialog_title);
                    alert.show();
                }
            }
        }

        int resID = context.getResources().getIdentifier("zzz_choose_player", "raw", context.getPackageName());
        if (resID == 0) {
            // hide audio instructions icon
            ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
            instructionsButton.setVisibility(View.GONE);
        } else{
            ImageView avatar12image = (ImageView) findViewById(R.id.avatar12);
            avatar12image.setVisibility(View.GONE);
            TextView playername12 = (TextView) findViewById(R.id.playername12);
            playername12.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void goToEarthFromAvatar(View view) {

        playerNumber = Integer.parseInt((String) view.getTag());
        Intent intent = new Intent(context, Earth.class);
        intent.putExtra("playerNumber", playerNumber);
        intent.putExtra("settingsList", settingsList);
        startActivity(intent);
        finish();

    }

    public void goToNameAvatarFromAvatar(View view) {
        playerNumber = Integer.parseInt((String) view.getTag());

        Intent intent = new Intent(context, SetPlayerName.class);
        intent.putExtra("playerNumber", playerNumber);
        intent.putExtra("settingsList", settingsList);
        startActivity(intent);
        finish();

    }

    public void playAudioInstructionsChoosePlayer(View view) {
        setAllElemsUnclickable();
        int resID = context.getResources().getIdentifier("zzz_choose_player", "raw", context.getPackageName());
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
        ConstraintLayout parentLayout = findViewById(R.id.choosePlayerCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(false);
        }
    }

    protected void setAllElemsClickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.choosePlayerCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(true);
        }
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

}
