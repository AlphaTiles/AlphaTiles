package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Start.keyList;

public class SetPlayerName extends AppCompatActivity {

    Context context;

    int keysInUse;
    int keyboardScreenNo; // for languages with more than 35 keys, page 1 will have 33 buttons and a forward/backward button
    int totalScreens; // the total number of screens required to show all keys
    int partial; // the number of visible keys on final partial screen

    static int playerNumber = -1;

    private static final int[] KEYS = {
            R.id.key01, R.id.key02, R.id.key03, R.id.key04, R.id.key05, R.id.key06, R.id.key07, R.id.key08, R.id.key09, R.id.key10,
            R.id.key11, R.id.key12, R.id.key13, R.id.key14, R.id.key15, R.id.key16, R.id.key17, R.id.key18, R.id.key19, R.id.key20,
            R.id.key21, R.id.key22, R.id.key23, R.id.key24, R.id.key25, R.id.key26, R.id.key27, R.id.key28, R.id.key29, R.id.key30,
            R.id.key31, R.id.key32, R.id.key33, R.id.key34, R.id.key35
    };

    private static final String[] COLORS = {"#9C27B0", "#2196F3", "#F44336","#4CAF50","#E91E63"};

    private static final Logger LOGGER = Logger.getLogger(SetPlayerName.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_update_avatars);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     // forces portrait mode only

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setTitle(Start.localAppName);

        playerNumber = getIntent().getIntExtra("playerNumber", -1);

        ImageView avatar = findViewById(R.id.avatar);
        int resID = getResources().getIdentifier(String.valueOf(ChoosePlayer.AVATAR_JPG_IDS[playerNumber - 1]), "drawable", getPackageName());
        avatar.setImageResource(resID);

        String defaultName;
        String playerName;
        // defaultName = Start.localWordForName + " " + playerNumber;

        // nameID = n + 1;
        String localWordForName = Start.langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            defaultName = Start.nameList.get(playerNumber);
        } else {
            defaultName = localWordForName + " " + playerNumber;
        }

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        playerName = prefs.getString("storedName" + playerString, defaultName);

        EditText name = findViewById(R.id.avatarName);
        name.setText(playerName);

        keyboardScreenNo = 1;
        loadKeyboard();

        setTextSizes();

        if (name.getText().length() > 0 ) {
            name.setSelection(name.getText().length());
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
        int pixelHeight = 0;
        double scaling = 0.45;
        int bottomToTopId;
        int topToTopId;
        float percentBottomToTop;
        float percentTopToTop;
        float percentHeight;

        for (int k = 0; k < KEYS.length; k++) {

            TextView key = findViewById(KEYS[k]);
            if (k == 0) {
                ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) key.getLayoutParams();
                bottomToTopId = lp1.bottomToTop;
                topToTopId = lp1.topToTop;
                percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId).getLayoutParams()).guidePercent;
                percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId).getLayoutParams()).guidePercent;
                percentHeight = percentBottomToTop - percentTopToTop;
                pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
            }
            key.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

        }

        EditText avatarName = (EditText) findViewById(R.id.avatarName);
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) avatarName.getLayoutParams();
        int bottomToTopId2 = lp2.bottomToTop;
        int topToTopId2 = lp2.topToTop;
        percentBottomToTop = ((ConstraintLayout.LayoutParams) findViewById(bottomToTopId2).getLayoutParams()).guidePercent;
        percentTopToTop = ((ConstraintLayout.LayoutParams) findViewById(topToTopId2).getLayoutParams()).guidePercent;
        percentHeight = percentBottomToTop - percentTopToTop;
        pixelHeight = (int) (scaling * percentHeight * heightOfDisplay);
        avatarName.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelHeight);

    }

    public void loadKeyboard() {

        // There are 35 key buttons available (KEYS.length), but the language may need a smaller amount (Start.keysArraySize)
        // Starting with k = 1 to skip the header row
        keysInUse = keyList.size();
        partial = keysInUse % (KEYS.length - 2);
        totalScreens = keysInUse / (KEYS.length - 2);
        if (partial != 0) {
            totalScreens++;
        }

        int visibleKeys;
        if (keysInUse > KEYS.length) {
            visibleKeys = KEYS.length;
        } else {
            visibleKeys = keysInUse;
        }

        for (int k = 0; k < visibleKeys; k++) {
            TextView key = findViewById(KEYS[k]);
            key.setText(keyList.get(k).baseKey);
            String tileColorStr = COLORS[Integer.parseInt(keyList.get(k).keyColor)];
            int tileColor = Color.parseColor(tileColorStr);
            key.setBackgroundColor(tileColor);
        }
        if (keysInUse > KEYS.length) {
            TextView key34 = findViewById(KEYS[KEYS.length - 2]);
            key34.setBackgroundResource(R.drawable.zz_backward_green);
            key34.setText("");
            TextView key35 = findViewById(KEYS[KEYS.length - 1]);
            key35.setBackgroundResource(R.drawable.zz_forward_green);
            key35.setText("");
        }

        for (int k = 0; k < KEYS.length; k++) {

            TextView key = findViewById(KEYS[k]);
            if (k < keysInUse) {
                key.setVisibility(View.VISIBLE);
                key.setClickable(true);
            } else {
                key.setVisibility(View.INVISIBLE);
                key.setClickable(false);
            }

        }

    }

    private void respondToKeySelection(int justClickedIndex) {

        String tileToAdd = keyList.get(justClickedIndex).baseKey;

        EditText avatarName = (EditText) findViewById(R.id.avatarName);
        String currentName = avatarName.getText() + tileToAdd;
        avatarName.setText(currentName);

        if (avatarName.getText().length() > 0 ) {
            avatarName.setSelection(avatarName.getText().length());
        }

    }

    public void deleteLastKeyedLetter (View view) {

        EditText avatarName = (EditText) findViewById(R.id.avatarName);

        String typedLettersSoFar = avatarName.getText().toString();
        String nowWithOneLessChar = "";

        if (typedLettersSoFar.length() > 0) {

            nowWithOneLessChar = typedLettersSoFar.substring(0, typedLettersSoFar.length() - 1);

        }

        avatarName.setText(nowWithOneLessChar);

        if (avatarName.getText().length() > 0 ) {
            avatarName.setSelection(avatarName.getText().length());
        }

    }

    public void onBtnClick(View view) {

        int justClickedKey = Integer.parseInt((String)view.getTag());
        // Next line says ... if a basic keyboard (which all fits on one screen) or (even when on a complex keyboard) if something other than the last two buttons (the two arrows) are tapped...
        if (keysInUse <= KEYS.length || justClickedKey <= (KEYS.length - 2)) {
            int keyIndex = (33 * (keyboardScreenNo - 1)) + justClickedKey - 1;
            respondToKeySelection(keyIndex);
        } else {
            // This branch = when a backward or forward arrow is clicked on
            if (justClickedKey == KEYS.length - 1) {
                keyboardScreenNo--;
                if (keyboardScreenNo < 1) {
                    keyboardScreenNo = 1;
                }
            }
            if (justClickedKey == KEYS.length) {
                keyboardScreenNo++;
                if (keyboardScreenNo > totalScreens) {
                    keyboardScreenNo = totalScreens;
                }
            }
            updateKeyboard();
        }
    }

    private void updateKeyboard() {

        // This routine will only be called from complex keyboards (more keys than will fit on the basic 35-key layout)

        int keysLimit;
        if(totalScreens == keyboardScreenNo) {
            keysLimit = partial;
            for (int k = keysLimit; k < (KEYS.length - 2); k++) {
                TextView key = findViewById(KEYS[k]);
                key.setVisibility(View.INVISIBLE);
            }
        } else {
            keysLimit = KEYS.length - 2;
        }

        for (int k = 0; k < keysLimit; k++) {
            TextView key = findViewById(KEYS[k]);
            int keyIndex = (33 * (keyboardScreenNo - 1)) + k;
            key.setText(keyList.get(keyIndex).baseKey); // KP
            key.setVisibility(View.VISIBLE);
            // Added on May 15th, 2021, so that second and following screens use their own color coding
            String tileColorStr = COLORS[Integer.parseInt(keyList.get(keyIndex).keyColor)];
            int tileColor = Color.parseColor(tileColorStr);
            key.setBackgroundColor(tileColor);
        }

    }

    public void acceptName (View view) {

        String playerName;

        LOGGER.info("Remember: about to set redID" );

        EditText name = findViewById(R.id.avatarName);
        playerName = name.getText().toString();

        LOGGER.info("Remember: playerName = " + playerName);

        SharedPreferences.Editor editor = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE).edit();
        String playerString = Util.returnPlayerStringToAppend(playerNumber);
        editor.putString("storedName" + playerString, playerName);
        editor.apply();

        LOGGER.info("Remember: editor.apply() complete");

        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();

    }

}